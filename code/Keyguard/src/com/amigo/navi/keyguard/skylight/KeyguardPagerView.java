package com.amigo.navi.keyguard.skylight;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.KWDataCache;
import com.android.keyguard.R;

/**
 * An abstraction of the original Workspace which supports browsing through a sequential list of "pages"
 */
public abstract class KeyguardPagerView extends ViewGroup implements ViewGroup.OnHierarchyChangeListener {
    private static final String LOG_TAG = "KeyguardPagerView";
    private static final boolean DEBUG = false;
    
    protected static final int INVALID_PAGE = -1;

    // the min drag distance for a fling to register, 
    // to prevent random page shifts
    private static final int MIN_LENGTH_FOR_FLING = 25;

    protected static final int PAGE_SNAP_ANIMATION_DURATION = 750;
    protected static final float NANOTIME_DIV = 1000000000.0f;

    private static final float OVERSCROLL_ACCELERATE_FACTOR = 2;
    private static final float OVERSCROLL_DAMP_FACTOR = 0.14f;

    private static final float RETURN_TO_ORIGINAL_PAGE_THRESHOLD = 0.33f;
    // The page is moved more than halfway, automatically move to the next page
    // on touch up.
    private static final float SIGNIFICANT_MOVE_THRESHOLD = 0.4f;

    // The following constants need to be scaled based on density. The scaled
    // versions will be
    // assigned to the corresponding member variables below.
    private static final int FLING_THRESHOLD_VELOCITY = 500;
    private static final int MIN_SNAP_VELOCITY = 1500;
    private static final int MIN_FLING_VELOCITY = 250;

    // We are disabling touch interaction of the widget region for factory ROM.
    private static final boolean DISABLE_TOUCH_INTERACTION = false;
    private static final boolean DISABLE_TOUCH_SIDE_PAGES = true;
    private static final boolean DISABLE_FLING_TO_DELETE = false;

    static final int AUTOMATIC_PAGE_SPACING = -1;

    protected int mFlingThresholdVelocity;
    protected int mMinFlingVelocity;
    protected int mMinSnapVelocity;

    protected float mDensity;
    // protected float mSmoothingTime;
    // protected float mTouchX;

    protected boolean mFirstLayout = true;

    protected int mCurrentPageIndex;
    protected int mChildCountOnLastMeasure;

    protected int mNextPage = INVALID_PAGE;
    protected int mMaxScrollX;
    protected Scroller mScroller;
    private VelocityTracker mVelocityTracker;

    private float mParentDownMotionX;
    private float mParentDownMotionY;
    private float mDownMotionX;
    private float mDownMotionY;
    private float mDownScrollX;
    protected float mLastMotionX;
    protected float mLastMotionXRemainder;
    protected float mLastMotionY;
    protected float mTotalMotionX;
    private int mLastScreenCenter = -1;
    private int[] mChildOffsets;
    private int[] mChildRelativeOffsets;
    private int[] mChildOffsetsWithLayoutScale;

    protected final static int TOUCH_STATE_REST = 0;
    protected final static int TOUCH_STATE_SCROLLING = 1;
    protected final static int TOUCH_STATE_PREV_PAGE = 2;
    protected final static int TOUCH_STATE_NEXT_PAGE = 3;
    protected final static int TOUCH_STATE_REORDERING = 4;

    protected int mTouchState = TOUCH_STATE_REST;
    protected boolean mForceScreenScrolled = false;

//    protected OnLongClickListener mLongClickListener;

    protected int mTouchSlop;
    private int mPagingTouchSlop;
    private int mMaximumVelocity;
    private int mMinimumWidth;
    protected int mPageSpacing;

    protected boolean mAllowOverScroll = true;
    protected int mUnboundedScrollX;
    protected int[] mTempVisiblePagesRange = new int[2];
    protected boolean mForceDrawAllChildrenNextFrame;

    // mOverScrollX is equal to getScrollX() when we're within the normal scroll
    // range. Otherwise
    // it is equal to the scaled overscroll position. We use a separate value so
    // as to prevent
    // the screens from continuing to translate beyond the normal bounds.
    protected int mOverScrollX;

    // parameter that adjusts the layout to be optimized for pages with that
    // scale factor
    protected float mLayoutScale = 1.0f;

    protected static final int INVALID_POINTER = -1;

    protected int mActivePointerId = INVALID_POINTER;

    private PageSwitchCallback mPageSwitchListener;

    protected ArrayList<Boolean> mDirtyPageContent;

    // It true, use a different slop parameter (pagingTouchSlop = 2 * touchSlop)
    // for deciding
    // to switch to a new page
    protected boolean mUsePagingTouchSlop = true;

    // If true, the subclass should directly update scrollX itself in its
    // computeScroll method
    // (SmoothPagedView does this)
    protected boolean mDeferScrollUpdate = false;

    protected boolean mIsPageMoving = false;

    // The viewport whether the pages are to be contained (the actual view may
    // be larger than the
    // viewport)
    private Rect mViewport = new Rect();

    // Reordering
    // We use the min scale to determine how much to expand the actually
    // PagedView measured
    // dimensions such that when we are zoomed out, the view is not clipped
    private int REORDERING_DROP_REPOSITION_DURATION = 200;
    protected int REORDERING_REORDER_REPOSITION_DURATION = 300;
    protected int REORDERING_ZOOM_IN_OUT_DURATION = 250;
    private int REORDERING_SIDE_PAGE_HOVER_TIMEOUT = 300;
    private float REORDERING_SIDE_PAGE_BUFFER_PERCENTAGE = 0.1f;
    private long REORDERING_DELETE_DROP_TARGET_FADE_DURATION = 150;
    private float mMinScale = 1f;
    protected View mDragView;
    protected AnimatorSet mZoomInOutAnim;
    private Runnable mSidePageHoverRunnable;
    private int mSidePageHoverIndex = -1;
    // This variable's scope is only for the duration of startReordering() and
    // endReordering()
    private boolean mReorderingStarted = false;
    // This variable's scope is for the duration of startReordering() and after
    // the zoomIn() animation after endReordering()
    private boolean mIsReordering;
    // The runnable that settles the page after snapToPage and
    // animateDragViewToOriginalPosition
    private int NUM_ANIMATIONS_RUNNING_BEFORE_ZOOM_OUT = 2;
    private int mPostReorderingPreZoomInRemainingAnimationCount;
    private Runnable mPostReorderingPreZoomInRunnable;

    // Edge swiping
    private boolean mOnlyAllowEdgeSwipes = false;
    private boolean mDownEventOnEdge = false;
    private int mEdgeSwipeRegionSize = 0;

    // Convenience/caching
    private Matrix mTmpInvMatrix = new Matrix();
    private float[] mTmpPoint = new float[2];
    private Rect mTmpRect = new Rect();
    private Rect mAltTmpRect = new Rect();

    // Fling to delete
    private int FLING_TO_DELETE_FADE_OUT_DURATION = 350;
    private float FLING_TO_DELETE_FRICTION = 0.035f;
    // The degrees specifies how much deviation from the up vector to still
    // consider a fling "up"
    private float FLING_TO_DELETE_MAX_FLING_DEGREES = 65f;
    protected int mFlingToDeleteThresholdVelocity = -1400;
    // Drag to delete
    private boolean mDeferringForDelete = false;
    private int DELETE_SLIDE_IN_SIDE_PAGE_DURATION = 250;
    private int DRAG_TO_DELETE_FADE_OUT_DURATION = 350;

    // Drop to delete
    private View mDeleteDropTarget;

    // Bouncer
    

    public interface PageSwitchCallback {
        void onPageSwitching(View newPage, int newPageIndex);
        void onPageSwitched(View newPage, int newPageIndex);
    }
    
  //Gionee <pengwei><2013-11-18> modify for CR00948963 begin
    private ArrayList<PageSwitchCallback> mPageSwitchCallback = 
    		new ArrayList<PageSwitchCallback>();
    public void registerPageSwitchCallback(PageSwitchCallback callback) {
    	if(mPageSwitchCallback.contains(callback) == false) {
    		mPageSwitchCallback.add(callback);
    	}
    }
    
    public void unregisterPageSwitchCallback(PageSwitchCallback callback) {
    	if(mPageSwitchCallback.contains(callback)) {
    		mPageSwitchCallback.remove(callback);
    	}
    }
    
    private void notifySwitching(int whichPage) {
    	for(PageSwitchCallback switchPage : mPageSwitchCallback) {
    		switchPage.onPageSwitching(getPageAt(whichPage), whichPage);
    	}
    }
    
    private void notifySwitched() {
    	for(PageSwitchCallback switchPage : mPageSwitchCallback) {
    	    if(DebugLog.DEBUG)DebugLog.d(LOG_TAG,"notifySwitched mCurrentPageIndex:" + mCurrentPageIndex);
    		switchPage.onPageSwitched(getPageAt(mCurrentPageIndex), mCurrentPageIndex);
    	}
    }
    //Gionee <pengwei><2013-11-18> modify for CR00948963 end
    
    public KeyguardPagerView(Context context) {
        this(context, null);
    }

    public KeyguardPagerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyguardPagerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setPageSpacing(0);

        // mEdgeSwipeRegionSize = getResources().getDimensionPixelSize(R.dimen.zzzzz_gn_navil_edge_swipe_region_size);

        setHapticFeedbackEnabled(false);
        init();
    }

    /**
     * Initializes various states for this workspace.
     */
    protected void init() {
    	DebugLog.d(LOG_TAG, "init");
        mDirtyPageContent = new ArrayList<Boolean>();
        mDirtyPageContent.ensureCapacity(32);
        mScroller = new Scroller(getContext(), new ScrollInterpolator());
        mCurrentPageIndex = 0;

        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mPagingTouchSlop = configuration.getScaledPagingTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

        mEdgeSwipeRegionSize = getResources().getDimensionPixelSize(R.dimen.zzzzz_gn_navil_edge_swipe_region_size);
        mDensity = getResources().getDisplayMetrics().density;

        // Scale the fling-to-delete threshold by the density
        mFlingToDeleteThresholdVelocity = (int) (mFlingToDeleteThresholdVelocity * mDensity);

        mFlingThresholdVelocity = (int) (FLING_THRESHOLD_VELOCITY * mDensity);
        mMinFlingVelocity = (int) (MIN_FLING_VELOCITY * mDensity);
        mMinSnapVelocity = (int) (MIN_SNAP_VELOCITY * mDensity);
        setOnHierarchyChangeListener(this);
    }

    public void setDeleteDropTarget(View v) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "setDeleteDropTarget");
        mDeleteDropTarget = v;
    }

    // Convenience methods to map points from self to parent and vice versa
    protected float[] mapPointFromChildToParent(View parent, float x, float y) {
        // KeyguardUtils.logD(LOG_TAG, "mapPointFromChildToParent v=" + v + ",x=" + x + ",y=" + y);
        mTmpPoint[0] = x;
        mTmpPoint[1] = y;
        parent.getMatrix().mapPoints(mTmpPoint);
        mTmpPoint[0] += parent.getLeft();
        mTmpPoint[1] += parent.getTop();
        return mTmpPoint;
    }

    protected float[] mapPointFromParentToChild(View child, float x, float y) {
        // KeyguardUtils.logD(LOG_TAG, "mapPointFromParentToChild v=" + v + ",x=" + x + ",y=" + y);
        mTmpPoint[0] = x - child.getLeft();
        mTmpPoint[1] = y - child.getTop();
        child.getMatrix().invert(mTmpInvMatrix);
        mTmpInvMatrix.mapPoints(mTmpPoint);
        return mTmpPoint;
    }

    void updateDragViewTranslationDuringDrag() {
        float x = mLastMotionX - mDownMotionX + getScrollX() - mDownScrollX;
        float y = mLastMotionY - mDownMotionY;
        // KeyguardUtils.logD(LOG_TAG, "updateDragViewTranslationDuringDrag x=" + x + ",y=" + y);
        mDragView.setTranslationX(x);
        mDragView.setTranslationY(y);
    }

    public void setMinScale(float f) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "setMinScale f=" + f);
        mMinScale = f;
        requestLayout();
    }

    @Override
    public void setScaleX(float scaleX) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "setScaleX scaleX=" + scaleX);
        super.setScaleX(scaleX);
        if (isReordering(true)) {
            if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "setScaleX 1");
            float[] p = mapPointFromParentToChild(this, mParentDownMotionX, mParentDownMotionY);
            mLastMotionX = p[0];
            mLastMotionY = p[1];
            updateDragViewTranslationDuringDrag();
        }
    }

    // Convenience methods to get the actual width/height of the PagedView
    // (since it is measured
    // to be larger to account for the minimum possible scale)
    int getViewportWidth() {
        return mViewport.width();
    }

    int getViewportHeight() {
        return mViewport.height();
    }

    // Convenience methods to get the offset ASSUMING that we are centering the
    // pages in the PagedView both horizontally and vertically
    
    int getViewportOffsetX() {
        return (getMeasuredWidth() - getViewportWidth()) / 2;
    }

    int getViewportOffsetY() {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "getViewportOffsetY: "+getMeasuredHeight()+" getViewportHeight:"+getViewportHeight());
        return (getMeasuredHeight() - getViewportHeight()) / 2;
//        return 0;
    }

    public void setPageSwitchListener(PageSwitchCallback pageSwitchListener) {
        mPageSwitchListener = pageSwitchListener;
        if (mPageSwitchListener != null) {
            if(DebugLog.DEBUG)Log.v(LOG_TAG,"setPageSwitchListener mCurrentPageIndex:" + mCurrentPageIndex);
            mPageSwitchListener.onPageSwitched(getPageAt(mCurrentPageIndex), mCurrentPageIndex);
        }
    }

    /**
     * Sets the current page.
     */
    public void setCurrentPageIndex(int pageIndex) {
        if(DebugLog.DEBUG)Log.v(LOG_TAG, "setCurrentPageIndex");
//        notifyPageSwitching(pageIndex);
    	notifySwitching(pageIndex);
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        // don't introduce any checks like mCurrentPage == currentPage here--
        // if we change the the default
        if (getChildCount() == 0) {
            return;
        }

        mForceScreenScrolled = true;
        mCurrentPageIndex = Math.max(0, Math.min(pageIndex, getPageCount() - 1));
        updateCurrentPageScroll();
        // updateScrollingIndicator();
//        notifyPageSwitched();
        notifySwitched();
        invalidate();
    }

    
    /**
     * Returns the index of the currently displayed page.
     * 
     * @return The index of the currently displayed page.
     */
    public int getCurrentPageIndex() {
        return mCurrentPageIndex;
    }

    int getNextPage() {
        return (mNextPage != INVALID_PAGE) ? mNextPage : mCurrentPageIndex;
    }

    int getPageCount() {
        return getChildCount();
    }

    View getPageAt(int index) {
        return getChildAt(index);
    }

    protected int indexToPage(int index) {
        return index;
    }

    /**
     * Updates the scroll of the current page immediately to its final scroll position. We use this in
     * CustomizePagedView to allow tabs to share the same PagedView while resetting the scroll of the previous
     * tab page.
     */
    protected void updateCurrentPageScroll() {
        int offset         = getChildOffset(mCurrentPageIndex);
        int relativeOffset = getRelativeChildOffset(mCurrentPageIndex);
        int newX = offset - relativeOffset;
        if(DebugLog.DEBUG){DebugLog.d(LOG_TAG, "updateCurrentPageScroll offset=" + offset
        		+ ",relOffset=" + relativeOffset + ",newX=" + newX);}
        scrollTo(newX, 0);
        mScroller.setFinalX(newX);
        mScroller.forceFinished(true);
    }

    public void setOnlyAllowEdgeSwipes(boolean enable) {
        mOnlyAllowEdgeSwipes = enable;
    }

    protected void notifyPageSwitching(int whichPage) {
        if (mPageSwitchListener != null) {
            mPageSwitchListener.onPageSwitching(getPageAt(whichPage), whichPage);
        }
    }

    protected void notifyPageSwitched() {
        if (mPageSwitchListener != null) {
            if(DebugLog.DEBUG)Log.v(LOG_TAG,"notifyPageSwitched mCurrentPageIndex:" + mCurrentPageIndex);
            mPageSwitchListener.onPageSwitched(getPageAt(mCurrentPageIndex), mCurrentPageIndex);
        }
    }

    protected void pageBeginMoving() {
        if (!mIsPageMoving) {
            mIsPageMoving = true;
            onPageBeginMoving();
        }
    }

    protected void pageEndMoving() {
        if (mIsPageMoving) {
            mIsPageMoving = false;
            onPageEndMoving();
        }
    }

    protected boolean isPageMoving() {
        return mIsPageMoving;
    }

    // a method that subclasses can override to add behavior
    protected void onPageBeginMoving() {
    }

    // a method that subclasses can override to add behavior
    protected void onPageEndMoving() {
    }

    /**
     * Registers the specified listener on each page contained in this workspace.
     * 
     * @param listener The listener used to respond to long clicks.
     */
    @Override
    public void setOnLongClickListener(OnLongClickListener listener) {
//        mLongClickListener = listener;
        final int count = getPageCount();
        for (int i = 0; i < count; i++) {
            getPageAt(i).setOnLongClickListener(listener);
        }
    }

    @Override
    public void scrollBy(int x, int y) {
        scrollTo(mUnboundedScrollX + x, getScrollY() + y);
    }

    @Override
    public void scrollTo(int x, int y) {
        mUnboundedScrollX = x;

        if (x < 0) {
            super.scrollTo(0, y);
            if (mAllowOverScroll) {
                overScroll(x);
            }
        } else if (x > mMaxScrollX) {
            super.scrollTo(mMaxScrollX, y);
            if (mAllowOverScroll) {
                overScroll(x - mMaxScrollX);
            }
        } else {
            mOverScrollX = x;
            super.scrollTo(x, y);
        }

        // mTouchX = x;
        // mSmoothingTime = System.nanoTime() / NANOTIME_DIV;

        // Update the last motion events when scrolling
        if (isReordering(true)) {
            float[] p = mapPointFromParentToChild(this, mParentDownMotionX, mParentDownMotionY);
            mLastMotionX = p[0];
            mLastMotionY = p[1];
            updateDragViewTranslationDuringDrag();
        }
    }

    // we moved this functionality to a helper function
    //  so SmoothPagedView canreuse it
    protected boolean computeScrollHelper() {
        if(DebugLog.DEBUG)Log.v(LOG_TAG, "computeScrollHelper");
        // KeyguardUtils.logD(LOG_TAG, "computeScrollHelper");
        if (mScroller.computeScrollOffset()) {
            // Don't bother scrolling if the page does not need to be moved
            if (getScrollX() != mScroller.getCurrX() || getScrollY() != mScroller.getCurrY()
                    || mOverScrollX != mScroller.getCurrX()) {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            }
            invalidate();
            return true;
        } else if (mNextPage != INVALID_PAGE) {
            mCurrentPageIndex = Math.max(0, Math.min(mNextPage, getPageCount() - 1));
            mNextPage = INVALID_PAGE;
			// notifyPageSwitched();
            notifySwitched();
            // We don't want to trigger a page end moving unless the page has
            // settled
            // and the user has stopped scrolling
            if (mTouchState == TOUCH_STATE_REST) {
                pageEndMoving();
            }

            onPostReorderingAnimationCompleted();
            return true;
        }
        return false;
    }

    @Override
    public void computeScroll() {
        // KeyguardUtils.logD(LOG_TAG, "computeScroll");
        computeScrollHelper();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "onMeasure");
        if (getChildCount() == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        // We measure the dimensions of the PagedView to be larger than the
        // pages so that when we
        // zoom out (and scale down), the view is still contained in the parent
        // View parent = (View) getParent();
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        // NOTE: We multiply by 1.5f to account for the fact that depending on
        // the offset of the
        // viewport, we can be at most one and a half screens offset once we
        // scale down
//        DisplayMetrics dm = getResources().getDisplayMetrics();
        int maxSize = Math.max(KWDataCache.getScreenWidth(getResources()), KWDataCache.getAllScreenHeigt(mContext));
        int parentWidthSize = (int) (1.5f * maxSize);
        int parentHeightSize = maxSize;
        int scaledWidthSize = (int) (parentWidthSize / mMinScale);
        int scaledHeightSize = (int) (parentHeightSize / mMinScale);
        mViewport.set(0, 0, widthSize, heightSize);
        if (widthMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.UNSPECIFIED) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        // Return early if we aren't given a proper dimension
        if (widthSize <= 0 || heightSize <= 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        /*
         * Allow the height to be set as WRAP_CONTENT. This allows the
         * particular case of the All apps view on XLarge displays to not take
         * up more space then it needs. Width is still not allowed to be set as
         * WRAP_CONTENT since many parts of the code expect each page to have
         * the same width.
         */
        final int verticalPadding = getPaddingTop() + getPaddingBottom();
        final int horizontalPadding = getPaddingLeft() + getPaddingRight();

        // The children are given the same width and height as the workspace
        // unless they were set to WRAP_CONTENT
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            // disallowing padding in paged view (just pass 0)
            final View child = getPageAt(i);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            int childWidthMode;
            if (lp.width == LayoutParams.WRAP_CONTENT) {
                childWidthMode = MeasureSpec.AT_MOST;
            } else {
                childWidthMode = MeasureSpec.EXACTLY;
            }

            int childHeightMode;
            if (lp.height == LayoutParams.WRAP_CONTENT) {
                childHeightMode = MeasureSpec.AT_MOST;
            } else {
                childHeightMode = MeasureSpec.EXACTLY;
            }

            final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize - horizontalPadding,
                    childWidthMode);
            
            final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize - verticalPadding,
                    childHeightMode);

            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            
        }
        setMeasuredDimension(scaledWidthSize, scaledHeightSize);
        // We can't call getChildOffset/getRelativeChildOffset until we set the
        // measured dimensions.
        // We also wait until we set the measured dimensions before flushing the
        // cache as well, to
        // ensure that the cache is filled with good values.
        invalidateCachedOffsets();

        if (mChildCountOnLastMeasure != getChildCount() && !mDeferringForDelete) {
            setCurrentPageIndex(mCurrentPageIndex);
        }
        mChildCountOnLastMeasure = getChildCount();

        if (childCount > 0) {
            if (DEBUG)
//          DebugLog.d(LOG_TAG, "getRelativeChildOffset(): " + getViewportWidth() + ", " + getChildWidth(0));
            if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "getRelativeChildOffset(): " + getViewportHeight() + ", " + getChildHeight(0));

            // Calculate the variable page spacing if necessary
            if (mPageSpacing == AUTOMATIC_PAGE_SPACING) {
                // The gap between pages in the PagedView should be equal to the
                // gap from the page
                // to the edge of the screen (so it is not visible in the
                // current screen). To
                // account for unequal padding on each side of the paged view,
                // we take the maximum
                // of the left/right gap and use that as the gap between each
                // page.
                int offset = getRelativeChildOffset(0);
                int spacing = Math.max(offset, widthSize - offset - getChildAt(0).getMeasuredWidth());
                setPageSpacing(spacing);
            }
        }

        // updateScrollingIndicatorPosition();

        if (childCount > 0) {
            mMaxScrollX = getChildOffset(childCount - 1) - getRelativeChildOffset(childCount - 1);
        } else {
            mMaxScrollX = 0;
        }
    }

    public void setPageSpacing(int pageSpacing) {
        mPageSpacing = pageSpacing;
        invalidateCachedOffsets();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int childCount = getChildCount();
        if (childCount == 0) {
            return;
        }

        int offsetX = getViewportOffsetX();
        int offsetY = getViewportOffsetY();

        // Update the viewport offsets
        mViewport.offset(offsetX, offsetY);
        int childLeft = offsetX + getRelativeChildOffset(0);
        for (int i = 0; i < childCount; i++) {
            final View child = getPageAt(i);
            int childTop = offsetY + getPaddingTop();
            if (child.getVisibility() != View.GONE) {
                int childWidth = getScaledMeasuredWidth(child);
                int childHeight = child.getMeasuredHeight();
                int childRight = childLeft + child.getMeasuredWidth();
                int childBottom = childTop + childHeight;
                child.layout(childLeft, childTop, childRight, childBottom);
                childLeft += childWidth + mPageSpacing;
            }
        }

        if (mFirstLayout && mCurrentPageIndex >= 0 && mCurrentPageIndex < getChildCount()) {
            setHorizontalScrollBarEnabled(false);
            updateCurrentPageScroll();
            setHorizontalScrollBarEnabled(true);
            
            mFirstLayout = false;
        }
    }

    protected void screenScrolled(int screenCenter) {
      
    }

    // impl methods of ViewGroup.OnHierarchyChangeListener
    
    @Override
    public void onChildViewAdded(View parent, View child) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "onChildViewAdded");
        // This ensures that when children are added, they get the correct
        // transforms / alphas
        // in accordance with any scroll effects.
        mForceScreenScrolled = true;
        invalidate();
        invalidateCachedOffsets();
    }

    @Override
    public void onChildViewRemoved(View parent, View child) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "onChildViewRemoved");
        mForceScreenScrolled = true;
    }

    protected void invalidateCachedOffsets() {
        int count = getChildCount();
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "invalidateCachedOffsets count=" + count);
        if (count == 0) {
            mChildOffsets = null;
            mChildRelativeOffsets = null;
            mChildOffsetsWithLayoutScale = null;
            return;
        }

        mChildOffsets = new int[count];
        mChildRelativeOffsets = new int[count];
        mChildOffsetsWithLayoutScale = new int[count];
        for (int i = 0; i < count; i++) {
            mChildOffsets[i] = -1;
            mChildRelativeOffsets[i] = -1;
            mChildOffsetsWithLayoutScale[i] = -1;
        }
    }

    protected int getChildOffset(int index) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "getChildOffset index=" + index);
        if (index < 0 || index > getChildCount() - 1)
            return 0;

        int[] childOffsets = Float.compare(mLayoutScale, 1f) == 0 ? mChildOffsets
                : mChildOffsetsWithLayoutScale;

        if (childOffsets != null && childOffsets[index] != -1) {
            return childOffsets[index];
        } else {
            if (getChildCount() == 0)
                return 0;

            int offset = getRelativeChildOffset(0);
            for (int i = 0; i < index; ++i) {
                offset += getScaledMeasuredWidth(getPageAt(i)) + mPageSpacing;
            }
            if (childOffsets != null) {
                childOffsets[index] = offset;
            }
            return offset;
        }
    }

    protected int getRelativeChildOffset(int index) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "getRelativeChildOffset index=" + index);
        if (index < 0 || index > getChildCount() - 1)
            return 0;

        if (mChildRelativeOffsets != null && mChildRelativeOffsets[index] != -1) {
            return mChildRelativeOffsets[index];
        } else {
            final int padding = getPaddingLeft() + getPaddingRight();
            final int offset = getPaddingLeft() + (getViewportWidth() - padding - getChildWidth(index)) / 2;
            if (mChildRelativeOffsets != null) {
                mChildRelativeOffsets[index] = offset;
            }
            return offset;
        }
    }

    protected int getScaledMeasuredWidth(View child) {
        // This functions are called enough times that it actually makes a
        // difference in the
        // profiler -- so just inline the max() here
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "getScaledMeasuredWidth child=" + child);
        final int measuredWidth = child.getMeasuredWidth();
        final int minWidth = mMinimumWidth;
        final int maxWidth = (minWidth > measuredWidth) ? minWidth : measuredWidth;
        return (int) (maxWidth * mLayoutScale + 0.5f);
    }

    void boundByReorderablePages(boolean isReordering, int[] range) {
        // Do nothing
    }

    // TODO: Fix this
    protected void getVisiblePages(int[] range) {
        range[0] = 0;
        range[1] = getPageCount() - 1;
    }

    protected boolean shouldDrawChild(View child) {
        return child.getAlpha() > 0;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        // KeyguardUtils.logD(LOG_TAG, "dispatchDraw");
        int halfScreenWidth = getViewportWidth() / 2;
        // mOverScrollX is equal to getScrollX() when we're within the normal
        // scroll range. Otherwise it is equal to the scaled overscroll position.
        int screenCenter = mOverScrollX + halfScreenWidth;

        if (screenCenter != mLastScreenCenter || mForceScreenScrolled) {
            // set mForceScreenScrolled before calling screenScrolled so that
            // screenScrolled can set it for the next frame
            mForceScreenScrolled = false;
            screenScrolled(screenCenter);
            mLastScreenCenter = screenCenter;
        }

        // Find out which screens are visible; as an optimization we only call
        // draw on them
        final int pageCount = getChildCount();
        if (pageCount > 0) {
            getVisiblePages(mTempVisiblePagesRange);
            final int leftScreen = mTempVisiblePagesRange[0];
            final int rightScreen = mTempVisiblePagesRange[1];
            if (leftScreen != -1 && rightScreen != -1) {
                final long drawingTime = getDrawingTime();
                // Clip to the bounds
                canvas.save();
                
                int left   = getScrollX();
                int top    = getScrollY();
                int right  = getScrollX() + getRight() - getLeft();
                int bottom = getScrollY() + getBottom() - getTop();
                canvas.clipRect(left, top, right, bottom);
//                canvas.clipRect(getScrollX(), getScrollY(), getScrollX() + getRight() - getLeft(),
//                        getScrollY() + getBottom() - getTop());

                // Draw all the children, leaving the drag view for last
                for (int i = pageCount - 1; i >= 0; i--) {
                    final View v = getPageAt(i);
                    if (v == mDragView) {
                        continue;
                    }
                    
                    boolean pageInRange = (leftScreen <= i && i <= rightScreen);
                    if (mForceDrawAllChildrenNextFrame || (pageInRange && shouldDrawChild(v))) {
                        drawChild(canvas, v, drawingTime);
                    }
                }
                // Draw the drag view on top (if there is one)
                if (mDragView != null) {
                    drawChild(canvas, mDragView, drawingTime);
                }

                mForceDrawAllChildrenNextFrame = false;
                canvas.restore();
            }
        }
    }

    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "requestChildRectangleOnScreen");
        int page = indexToPage(indexOfChild(child));
        if (page != mCurrentPageIndex || !mScroller.isFinished()) {
            snapToPage(page);
            return true;
        }
        return false;
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "onRequestFocusInDescendants");
        int focusablePage;
        if (mNextPage != INVALID_PAGE) {
            focusablePage = mNextPage;
        } else {
            focusablePage = mCurrentPageIndex;
        }
        View v = getPageAt(focusablePage);
        if (v != null) {
            return v.requestFocus(direction, previouslyFocusedRect);
        }
        return false;
    }

    @Override
    public boolean dispatchUnhandledMove(View focused, int direction) {
        if (direction == View.FOCUS_LEFT) {
            if (getCurrentPageIndex() > 0) {
                snapToPage(getCurrentPageIndex() - 1);
                return true;
            }
        } else if (direction == View.FOCUS_RIGHT) {
            if (getCurrentPageIndex() < getPageCount() - 1) {
                snapToPage(getCurrentPageIndex() + 1);
                return true;
            }
        }
        return super.dispatchUnhandledMove(focused, direction);
    }

    @Override
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "addFocusables");
        if (mCurrentPageIndex >= 0 && mCurrentPageIndex < getPageCount()) {
            getPageAt(mCurrentPageIndex).addFocusables(views, direction, focusableMode);
        }
        if (direction == View.FOCUS_LEFT) {
            if (mCurrentPageIndex > 0) {
                getPageAt(mCurrentPageIndex - 1).addFocusables(views, direction, focusableMode);
            }
        } else if (direction == View.FOCUS_RIGHT) {
            if (mCurrentPageIndex < getPageCount() - 1) {
                getPageAt(mCurrentPageIndex + 1).addFocusables(views, direction, focusableMode);
            }
        }
    }

    /**
     * If one of our descendant views decides that it could be focused now, only pass that along if it's on
     * the current page.
     * 
     * This happens when live folders requery, and if they're off page, they end up calling requestFocus,
     * which pulls it on page.
     */
    @Override
    public void focusableViewAvailable(View focused) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "focusableViewAvailable");
        View current = getPageAt(mCurrentPageIndex);
        View v = focused;
        while (true) {
            if (v == current) {
                super.focusableViewAvailable(focused);
                return;
            }
            if (v == this) {
                return;
            }
            ViewParent parent = v.getParent();
            if (parent instanceof View) {
                v = (View) v.getParent();
            } else {
                return;
            }
        }
    }

    /**
     * Return true if a tap at (x, y) should trigger a flip to the previous page.
     */
    protected boolean hitsPreviousPage(float x, float y) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "hitsPreviousPage");
        return (x < getViewportOffsetX() + getRelativeChildOffset(mCurrentPageIndex) - mPageSpacing);
    }

    /**
     * Return true if a tap at (x, y) should trigger a flip to the next page.
     */
    protected boolean hitsNextPage(float x, float y) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "hitsNextPage");
        return (x > (getViewportOffsetX() + getViewportWidth() - getRelativeChildOffset(mCurrentPageIndex) + mPageSpacing));
    }

    /**
     * Returns whether x and y originated within the buffered/unbuffered viewport
     */
    private boolean isTouchPointInViewport(int x, int y, boolean buffer) {
        // KeyguardUtils.logD(LOG_TAG, "isTouchPointInViewport");
        if (buffer) {
            mTmpRect.set(mViewport.left - mViewport.width() / 2, mViewport.top,
                    mViewport.right + mViewport.width() / 2, mViewport.bottom);
            return mTmpRect.contains(x, y);
        } else {
            return mViewport.contains(x, y);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // KeyguardUtils.logD(LOG_TAG, "onInterceptTouchEvent");
        if (DISABLE_TOUCH_INTERACTION) {
            return false;
        }

        /*
         * This method JUST determines whether we want to intercept the motion.
         * If we return true, onTouchEvent will be called and we do the actual
         * scrolling there.
         */
        obtainVelocityTrackerAndAddMovement(ev);

        // Skip touch handling if there are no pages to swipe
        if (getChildCount() <= 0)
            return super.onInterceptTouchEvent(ev);

        /*
         * Shortcut the most recurring case: the user is in the dragging state
         * and he is moving his finger. We want to intercept this motion.
         */
        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mTouchState == TOUCH_STATE_SCROLLING)) {
            return true;
        }

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE: {
                /*
                 * mIsBeingDragged == false, otherwise the shortcut would have
                 * caught it. Check whether the user has moved far enough from his
                 * original down touch.
                 */
                if (mActivePointerId != INVALID_POINTER) {
                    determineScrollingStart(ev);
                    break;
                }
                // if mActivePointerId is INVALID_POINTER, then we must have missed
                // an ACTION_DOWN
                // event. in that case, treat the first occurrence of a move event as
                // a ACTION_DOWN
                // i.e. fall through to the next case (don't break)
                // (We sometimes miss ACTION_DOWN events in Workspace because it
                // ignores all events
                // while it's small- this was causing a crash before we checked for
                // INVALID_POINTER)
            }

            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();
                // Remember location of down touch
                mDownMotionX = x;
                mDownMotionY = y;
                mDownScrollX = getScrollX();
                mLastMotionX = x;
                mLastMotionY = y;
                float[] p = mapPointFromChildToParent(this, x, y);
                mParentDownMotionX = p[0];
                mParentDownMotionY = p[1];
                mLastMotionXRemainder = 0;
                mTotalMotionX = 0;
                mActivePointerId = ev.getPointerId(0);

                // Determine if the down event is within the threshold to be an edge
                // swipe
                int leftEdgeBoundary = getViewportOffsetX() + mEdgeSwipeRegionSize;
                int rightEdgeBoundary = getMeasuredWidth() - getViewportOffsetX() - mEdgeSwipeRegionSize;
                if ((mDownMotionX <= leftEdgeBoundary || mDownMotionX >= rightEdgeBoundary)) {
                    mDownEventOnEdge = true;
                }

                /*
                 * If being flinged and user touches the screen, initiate drag;
                 * otherwise don't. mScroller.isFinished should be false when being
                 * flinged.
                 */
                // Gionee <jiangxiao> <2014-03-19> add for CR01102109 begin
                // should not remove xDist judgement, because it will cause child view
                // can not receive touch event
                //Gionee <huangxc><2013-11-28> modify for CR00955334 begin
                final int xDist = Math.abs(mScroller.getFinalX() - mScroller.getCurrX());
                final boolean finishedScrolling = (mScroller.isFinished() || xDist < mTouchSlop);
                //Gionee <huangxc><2013-11-28> modify for CR00955334 end
                // Gionee <jiangxiao> <2014-03-19> add for CR01102109 end
                if (finishedScrolling) {
                    mTouchState = TOUCH_STATE_REST;
                    mScroller.abortAnimation();
                } else {
                    if (isTouchPointInViewport((int) mDownMotionX, (int) mDownMotionY, true)) {
                        mTouchState = TOUCH_STATE_SCROLLING;
                    } else {
                        mTouchState = TOUCH_STATE_REST;
                    }
                }

                // check if this can be the beginning of a tap on the side of the
                // pages
                // to scroll the current page
                if (!DISABLE_TOUCH_SIDE_PAGES) {
                    if (mTouchState != TOUCH_STATE_PREV_PAGE && mTouchState != TOUCH_STATE_NEXT_PAGE) {
                        if (getChildCount() > 0) {
                            if (hitsPreviousPage(x, y)) {
                                mTouchState = TOUCH_STATE_PREV_PAGE;
                            } else if (hitsNextPage(x, y)) {
                                mTouchState = TOUCH_STATE_NEXT_PAGE;
                            }
                        }
                    }
                }
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                resetTouchState();
                // Just intercept the touch event on up if we tap outside the strict
                // viewport
                if (!isTouchPointInViewport((int) mLastMotionX, (int) mLastMotionY, false)) {
                    return true;
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
			handleActionPointerUp(ev);
                releaseVelocityTracker();
                break;
                default:
                	break;
        }

        /*
         * The only time we want to intercept motion events is if we are in the
         * drag mode.
         */
        return mTouchState != TOUCH_STATE_REST;
    }

    protected void determineScrollingStart(MotionEvent ev) {
        // KeyguardUtils.logD(LOG_TAG, "determineScrollingStart");
        determineScrollingStart(ev, 1.0f);
    }

    /*
     * Determines if we should change the touch state to start scrolling after
     * the user moves their touch point too far.
     */
    protected void determineScrollingStart(MotionEvent ev, float touchSlopScale) {
        // KeyguardUtils.logD(LOG_TAG, "determineScrollingStart");
        // Disallow scrolling if we don't have a valid pointer index
        final int pointerIndex = ev.findPointerIndex(mActivePointerId);
        if (pointerIndex == -1)
            return;

        // Disallow scrolling if we started the gesture from outside the viewport
        final float x = ev.getX(pointerIndex);
        final float y = ev.getY(pointerIndex);
        if (isTouchPointInViewport((int) x, (int) y, true) == false)
            return;

        // If we're only allowing edge swipes, we break out early if the down
        // event wasn't at the edge.
        if (mOnlyAllowEdgeSwipes && !mDownEventOnEdge)
            return;

        final int xDiff = (int) Math.abs(x - mLastMotionX);
        final int yDiff = (int) Math.abs(y - mLastMotionY);

        final int touchSlop = Math.round(touchSlopScale * mTouchSlop);
        boolean xPaged = xDiff > mPagingTouchSlop;
        boolean xMoved = xDiff > touchSlop;
        boolean yMoved = yDiff > touchSlop;

        if (xMoved || xPaged || yMoved) {
            if (mUsePagingTouchSlop ? xPaged : xMoved) {
                // Scroll if the user moved far enough along the X axis
                mTouchState = TOUCH_STATE_SCROLLING;
                mTotalMotionX += Math.abs(mLastMotionX - x);
                mLastMotionX = x;
                mLastMotionXRemainder = 0;
                // mTouchX = getViewportOffsetX() + getScrollX();
                // mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
                pageBeginMoving();
            }
        }
    }

    protected float getMaxScrollProgress() {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "getMaxScrollProgress");
        return 1.0f;
    }

    protected float getScrollProgress(int screenCenter, View v, int page) {
        // KeyguardUtils.logD(LOG_TAG, "getScrollProgress");
        final int halfScreenSize = getViewportWidth() / 2;

        int totalDistance = getScaledMeasuredWidth(v) + mPageSpacing;
        int delta = screenCenter - (getChildOffset(page) - getRelativeChildOffset(page) + halfScreenSize);

        float scrollProgress = delta / (totalDistance * 1.0f);
        scrollProgress = Math.min(scrollProgress, getMaxScrollProgress());
        scrollProgress = Math.max(scrollProgress, -getMaxScrollProgress());
        return scrollProgress;
    }

    protected void acceleratedOverScroll(float amount) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "acceleratedOverScroll");
        int screenSize = getViewportWidth();

        // We want to reach the max over scroll effect when the user has
        // over scrolled half the size of the screen
        float f = OVERSCROLL_ACCELERATE_FACTOR * (amount / screenSize);

        if (f == 0)
            return;

        // Clamp this factor, f, to -1 < f < 1
        if (Math.abs(f) >= 1) {
            f /= Math.abs(f);
        }

        int overScrollAmount = (int) Math.round(f * screenSize);
        if (amount < 0) {
            mOverScrollX = overScrollAmount;
            super.scrollTo(0, getScrollY());
        } else {
            mOverScrollX = mMaxScrollX + overScrollAmount;
            super.scrollTo(mMaxScrollX, getScrollY());
        }
        invalidate();
    }

    protected void overScroll(float amount) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "overScroll");
        dampedOverScroll(amount);
    }

    protected void dampedOverScroll(float offsetX) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "dampedOverScroll(), offsetX=" + offsetX);
        if(offsetX == 0) return;
        
        int screenWidth = getViewportWidth();
        float f = (offsetX / screenWidth);
        f = f / Math.abs(f) * overScrollInfluenceCurve(Math.abs(f));

        // Clamp this factor, f, to -1 < f < 1
        if (Math.abs(f) >= 1) {
            f /= Math.abs(f);
        }

        int overScrollAmount = (int) Math.round(OVERSCROLL_DAMP_FACTOR * f * screenWidth);
        if (offsetX < 0) {
            mOverScrollX = overScrollAmount;
            super.scrollTo(0, getScrollY());
        } else {
            mOverScrollX = mMaxScrollX + overScrollAmount;
            super.scrollTo(mMaxScrollX, getScrollY());
        }
        
        invalidate();
    }

    protected float maxOverScroll() {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "maxOverScroll");
        // Using the formula in overScroll, assuming that f = 1.0 (which it
        // should generally not
        // exceed). Used to find out how much extra wallpaper we need for the
        // over scroll effect
        float f = 1.0f;
        f = f / Math.abs(f) * overScrollInfluenceCurve(Math.abs(f));
        return OVERSCROLL_DAMP_FACTOR * f;
    }

    // This curve determines how the effect of scrolling over the limits of the
    // page dimishes as the user pulls further and further from the bounds
    private float overScrollInfluenceCurve(float f) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "overScrollInfluenceCurve");
        f -= 1.0f;
        return f * f * f + 1.0f;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (DISABLE_TOUCH_INTERACTION) {
            return false;
        }

        // Skip touch handling if there are no pages to swipe
        if (getChildCount() <= 0) {
            return super.onTouchEvent(ev);
        }

        obtainVelocityTrackerAndAddMovement(ev);

        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
			    handleActionDown(ev);
                break;

            case MotionEvent.ACTION_MOVE:
			    handleActionMove(ev);
                break;

            case MotionEvent.ACTION_UP:
            	handleActionUp(ev);
            	DebugLog.d("hxcdebug", "pagerview--ontouch--up");
                break;

            case MotionEvent.ACTION_CANCEL:
			    handleActionCancel();
                break;

            case MotionEvent.ACTION_POINTER_UP:
			    handleActionPointerUp(ev);
                break;
            default:
            	break;
        }

        return true;
    }

	private void handleActionPointerUp(MotionEvent ev) {
		onSecondaryPointerUp(ev);
	}

	private void handleActionCancel() {
		if (mTouchState == TOUCH_STATE_SCROLLING) {
		    snapToDestination();
		}
		resetTouchState();
	}

	private void handleActionUp(MotionEvent ev) {
		DebugLog.d(LOG_TAG, "handleActionUp()");
		// Gionee <jiangxiao> <2014-04-18> modify for CR01200286 begin
//		if (mTouchState == TOUCH_STATE_SCROLLING) {
//		    handleActionUpScrolling(ev);
//		} else if (mTouchState == TOUCH_STATE_PREV_PAGE) {
//		    handleActionUpPrevPage();
//		} else if (mTouchState == TOUCH_STATE_NEXT_PAGE) {
//		    handleActionUpNextPage();
//		} else if (mTouchState == TOUCH_STATE_REORDERING) {
//		    handleActionUpReordering(ev);
//		} else {
//		    onUnhandledTap(ev);
//		}
		handleActionUpScrolling(ev);
		// Gionee <jiangxiao> <2014-04-18> modify for CR01200286 end

		// Remove the callback to wait for the side page hover timeout
		removeCallbacks(mSidePageHoverRunnable);
		// End any intermediate reordering states
		resetTouchState();
	}

	private void handleActionUpReordering(MotionEvent ev) {
		DebugLog.d(LOG_TAG, "handleActionUpReordering()");
		// Update the last motion position
		mLastMotionX = ev.getX();
		mLastMotionY = ev.getY();

		// Update the parent down so that our zoom animations take this
		// new movement into
		// account
		float[] pt = mapPointFromChildToParent(this, mLastMotionX, mLastMotionY);
		mParentDownMotionX = pt[0];
		mParentDownMotionY = pt[1];
		updateDragViewTranslationDuringDrag();
		boolean handledFling = false;
		if (!DISABLE_FLING_TO_DELETE) {
		    // Check the velocity and see if we are flinging-to-delete
		    PointF flingToDeleteVector = isFlingingToDelete();
		    if (flingToDeleteVector != null) {
		        onFlingToDelete(flingToDeleteVector);
		        handledFling = true;
		    }
		}
		if (!handledFling
		        && isHoveringOverDeleteDropTarget((int) mParentDownMotionX,
		                (int) mParentDownMotionY)) {
		    onDropToDelete();
		}
	}

	private void handleActionUpNextPage() {
		DebugLog.d(LOG_TAG, "handleActionUpNextPage()");
		// at this point we have not moved beyond the touch slop
		// (otherwise mTouchState would be TOUCH_STATE_SCROLLING), so
		// we can just page
		int nextPage = Math.min(getChildCount() - 1, mCurrentPageIndex + 1);
		if (nextPage != mCurrentPageIndex) {
		    snapToPage(nextPage);
		} else {
		    snapToDestination();
		}
	}

	private void handleActionUpPrevPage() {
		DebugLog.d(LOG_TAG, "handleActionUpPrevPage()");
		// at this point we have not moved beyond the touch slop
		// (otherwise mTouchState would be TOUCH_STATE_SCROLLING), so
		// we can just page
		int nextPage = Math.max(0, mCurrentPageIndex - 1);
		if (nextPage != mCurrentPageIndex) {
		    snapToPage(nextPage);
		} else {
		    snapToDestination();
		}
	}

	private void handleActionUpScrolling(MotionEvent ev) {
	    if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "handleActionUpScrolling()");
		final int activePointerId = mActivePointerId;
		// Gionee <jiangxiao> <2014-04-18> modify for CR01200286 begin
		float x = ev.getX();
		// try {
		//     final int pointerIndex = ev.findPointerIndex(activePointerId);
		//     x = ev.getX(pointerIndex);
		// } catch(IllegalArgumentException e) {
		//     x = ev.getX();
		// }
		// Gionee <jiangxiao> <2014-04-18> modify for CR01200286 end
		
		final VelocityTracker velocityTracker = mVelocityTracker;
		velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
		int velocityX = (int) velocityTracker.getXVelocity(activePointerId);
		final int deltaX = (int) (x - mDownMotionX);
		final int pageWidth = getScaledMeasuredWidth(getPageAt(mCurrentPageIndex));
		boolean isSignificantMove = Math.abs(deltaX) > pageWidth * SIGNIFICANT_MOVE_THRESHOLD;

		mTotalMotionX += Math.abs(mLastMotionX + mLastMotionXRemainder - x);

		boolean isFling = mTotalMotionX > MIN_LENGTH_FOR_FLING
		        && Math.abs(velocityX) > mFlingThresholdVelocity;

		// In the case that the page is moved far to one direction and
		// then is flung
		// in the opposite direction, we use a threshold to determine
		// whether we should
		// just return to the starting page, or if we should skip one
		// further.
		boolean returnToOriginalPage = false;
		if (Math.abs(deltaX) > pageWidth * RETURN_TO_ORIGINAL_PAGE_THRESHOLD
		        && Math.signum(velocityX) != Math.signum(deltaX) && isFling) {
		    returnToOriginalPage = true;
		}

		int finalPage;
		// We give flings precedence over large moves, which is why we
		// short-circuit our
		// test for a large move if a fling has been registered. That
		// is, a large
		// move to the left and fling to the right will register as a
		// fling to the right.
		if (((isSignificantMove && deltaX > 0 && !isFling) || (isFling && velocityX > 0))
		        && mCurrentPageIndex > 0) {
		    finalPage = returnToOriginalPage ? mCurrentPageIndex : mCurrentPageIndex - 1;
		    snapToPageWithVelocity(finalPage, velocityX);
		} else if (((isSignificantMove && deltaX < 0 && !isFling) || (isFling && velocityX < 0))
		        && mCurrentPageIndex < getChildCount() - 1) {
		    finalPage = returnToOriginalPage ? mCurrentPageIndex : mCurrentPageIndex + 1;
		    snapToPageWithVelocity(finalPage, velocityX);
		} else {
		    snapToDestination();
		}
	}

	private void handleActionMove(MotionEvent ev) {
		if (mTouchState == TOUCH_STATE_SCROLLING) {
		    handleActionMoveScrolling(ev);
		} else if (mTouchState == TOUCH_STATE_REORDERING) {
		    handleActionMoveReordering(ev);
		} else {
		    determineScrollingStart(ev);
		}
	}

	private void handleActionMoveReordering(MotionEvent ev) {
		// Update the last motion position
		mLastMotionX = ev.getX();
		mLastMotionY = ev.getY();

		// Update the parent down so that our zoom animations take this
		// new movement into account
		float[] pt = mapPointFromChildToParent(this, mLastMotionX, mLastMotionY);
		mParentDownMotionX = pt[0];
		mParentDownMotionY = pt[1];
		updateDragViewTranslationDuringDrag();

		// Find the closest page to the touch point
		final int dragViewIndex = indexOfChild(mDragView);

		// Change the drag view if we are hovering over the drop target
		boolean isHoveringOverDelete = isHoveringOverDeleteDropTarget(
				(int) mParentDownMotionX, (int) mParentDownMotionY);
		setPageHoveringOverDeleteDropTarget(dragViewIndex, isHoveringOverDelete);
		
		
		int bufferSize = (int) (REORDERING_SIDE_PAGE_BUFFER_PERCENTAGE * getViewportWidth());
		int leftBufferEdge = (int) (mapPointFromChildToParent(this, mViewport.left, 0)[0] + bufferSize);
		int rightBufferEdge = (int) (mapPointFromChildToParent(this, mViewport.right, 0)[0] - bufferSize);

		if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "leftBufferEdge: " + leftBufferEdge);
		if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "rightBufferEdge: " + rightBufferEdge);
		if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "mLastMotionX: " + mLastMotionX);
		if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "mLastMotionY: " + mLastMotionY);
		if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "mParentDownMotionX: " + mParentDownMotionX);
		if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "mParentDownMotionY: " + mParentDownMotionY);

		int pageIndexToSnapTo = -1;
		if((mParentDownMotionX < leftBufferEdge) && (dragViewIndex > 0)) {
		    pageIndexToSnapTo = dragViewIndex - 1;
		} else if((mParentDownMotionX > rightBufferEdge) && (dragViewIndex < getChildCount() - 1)) {
		    pageIndexToSnapTo = dragViewIndex + 1;
		}

		final int pageIndexUnderPoint = pageIndexToSnapTo;
		if (pageIndexUnderPoint > -1 && !isHoveringOverDelete) {
		    mTempVisiblePagesRange[0] = 0;
		    mTempVisiblePagesRange[1] = getPageCount() - 1;
		    boundByReorderablePages(true, mTempVisiblePagesRange); // impl by KeyguardWidgetPager
		    
		    boolean isPageIndexInRange = (pageIndexUnderPoint >= mTempVisiblePagesRange[0]
		    		&& pageIndexUnderPoint <= mTempVisiblePagesRange[1]);
		    boolean isPageIndexNotHover = pageIndexUnderPoint != mSidePageHoverIndex;
		    if (isPageIndexInRange && isPageIndexNotHover && mScroller.isFinished()) {
		        mSidePageHoverIndex = pageIndexUnderPoint;
		        mSidePageHoverRunnable = new Runnable() {
		            @Override
		            public void run() {
		                // Update the down scroll position to account
		                // for the fact that the
		                // current page is moved
		                mDownScrollX = getChildOffset(pageIndexUnderPoint)
		                        - getRelativeChildOffset(pageIndexUnderPoint);

		                // Setup the scroll to the correct page before
		                // we swap the views
		                snapToPage(pageIndexUnderPoint);

		                // For each of the pages between the paged view
		                // and the drag view,
		                // animate them from the previous position to
		                // the new position in
		                // the layout (as a result of the drag view
		                // moving in the layout)
		                int shiftDelta = (dragViewIndex < pageIndexUnderPoint) ? -1 : 1;
		                int lowerIndex = (dragViewIndex < pageIndexUnderPoint) ? dragViewIndex + 1
		                        : pageIndexUnderPoint;
		                int upperIndex = (dragViewIndex > pageIndexUnderPoint) ? dragViewIndex - 1
		                        : pageIndexUnderPoint;
		                for (int i = lowerIndex; i <= upperIndex; ++i) {
		                    View v = getChildAt(i);
		                    // dragViewIndex < pageUnderPointIndex, so
		                    // after we remove the
		                    // drag view all subsequent views to
		                    // pageUnderPointIndex will
		                    // shift down.
		                    int oldX = getViewportOffsetX() + getChildOffset(i);
		                    int newX = getViewportOffsetX() + getChildOffset(i + shiftDelta);

		                    // Animate the view translation from its old
		                    // position to its new
		                    // position
		                    AnimatorSet anim = (AnimatorSet) v.getTag();
		                    if (anim != null) {
		                        anim.cancel();
		                    }

		                    v.setTranslationX(oldX - newX);
		                    anim = new AnimatorSet();
		                    anim.setDuration(REORDERING_REORDER_REPOSITION_DURATION);
		                    anim.playTogether(ObjectAnimator.ofFloat(v, "translationX", 0f));
		                    anim.start();
		                    v.setTag(anim);
		                }

		                removeView(mDragView);
		                // gionee gaojt modify for CR00797101 2013-05-06 start
		                onRemoveView(mDragView, true);
		                // gionee gaojt modify for CR00797101 2013-05-06 end
		                addView(mDragView, pageIndexUnderPoint);
		                onAddView(mDragView, pageIndexUnderPoint);
		                mSidePageHoverIndex = -1;
		            }
		        };
		        postDelayed(mSidePageHoverRunnable, REORDERING_SIDE_PAGE_HOVER_TIMEOUT);
		    }
		} else {
		    removeCallbacks(mSidePageHoverRunnable);
		    mSidePageHoverIndex = -1;
		}
	}

	private void handleActionMoveScrolling(MotionEvent ev) {
		// Scroll to follow the motion event
		// / M: Before using Active pointer id, check if this point
		// still exists @{
		int pointerIndex = ev.findPointerIndex(mActivePointerId);
		if (pointerIndex == -1) {
		    pointerIndex = 0;
		    mActivePointerId = ev.getPointerId(pointerIndex);
		}
		// / @}
		final float x = ev.getX(pointerIndex);
		final float deltaX = mLastMotionX + mLastMotionXRemainder - x;

		mTotalMotionX += Math.abs(deltaX);

		// Only scroll and update mLastMotionX if we have moved some
		// discrete amount. We keep the remainder because we are actually
		// testing if we've moved from the last scrolled position (which is discrete).
		if (Math.abs(deltaX) >= 1.0f) {
		    // mTouchX += deltaX;
		    // mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
		    if (mDeferScrollUpdate == false) {
		        scrollBy((int) deltaX, 0);
		    } else {
		        invalidate();
		    }
		    mLastMotionX = x;
		    mLastMotionXRemainder = deltaX - (int) deltaX;
		} else {
		    awakenScrollBars();
		}
	}

	private void handleActionDown(MotionEvent ev) {
		DebugLog.d(LOG_TAG, "handleActionDown()");
		/*
		 * If being flinged and user touches, stop the fling.
		 * isFinished() will be false if being flinged.
		 */
		// Gionee <jiangxiao> <2014-03-19> modify for CR01102109 begin
		// make the page scroll can not be aborted
		final int xDist = Math.abs(mScroller.getFinalX() - mScroller.getCurrX());
		if (!mScroller.isFinished() && xDist < mTouchSlop) {
		    mScroller.abortAnimation();
		}
	    // Gionee <jiangxiao> <2014-03-19> modify for CR01102109 end

		// Remember where the motion event started
		mDownMotionX = mLastMotionX = ev.getX();
		mDownMotionY = mLastMotionY = ev.getY();
		mDownScrollX = getScrollX();
		float[] p = mapPointFromChildToParent(this, mLastMotionX, mLastMotionY);
		mParentDownMotionX = p[0];
		mParentDownMotionY = p[1];
		mLastMotionXRemainder = 0;
		mTotalMotionX = 0;
		mActivePointerId = ev.getPointerId(0);

		// Determine if the down event is within the threshold
		// to be an edge swipe
		int leftEdgeBoundary = getViewportOffsetX() + mEdgeSwipeRegionSize;
		int rightEdgeBoundary = getMeasuredWidth() - getViewportOffsetX() - mEdgeSwipeRegionSize;
		if ((mDownMotionX <= leftEdgeBoundary || mDownMotionX >= rightEdgeBoundary)) {
		    mDownEventOnEdge = true;
		}

		if (mTouchState == TOUCH_STATE_SCROLLING) {
		    pageBeginMoving();
		}
	}

    public abstract void onRemoveView(View v, boolean draging);

    public abstract void onAddView(View v, int index);

    private void resetTouchState() {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "resetTouchState");
        releaseVelocityTracker();
        endReordering();
        mTouchState = TOUCH_STATE_REST;
        mActivePointerId = INVALID_POINTER;
        mDownEventOnEdge = false;
    }

    protected void onUnhandledTap(MotionEvent ev) {
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "onGenericMotionEvent");
        if ((event.getSource() & InputDevice.SOURCE_CLASS_POINTER) != 0) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_SCROLL: {
                    // Handle mouse (or ext. device) by shifting the page depending
                    // on the scroll
                    final float vscroll;
                    final float hscroll;
                    if ((event.getMetaState() & KeyEvent.META_SHIFT_ON) != 0) {
                        vscroll = 0;
                        hscroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
                    } else {
                        vscroll = -event.getAxisValue(MotionEvent.AXIS_VSCROLL);
                        hscroll = event.getAxisValue(MotionEvent.AXIS_HSCROLL);
                    }
                    if (hscroll != 0 || vscroll != 0) {
                        if (hscroll > 0 || vscroll > 0) {
                            scrollRight();
                        } else {
                            scrollLeft();
                        }
                        return true;
                    }
                }
            }
        }
        return super.onGenericMotionEvent(event);
    }

    private void obtainVelocityTrackerAndAddMovement(MotionEvent ev) {
        // KeyguardUtils.logD(LOG_TAG, "acquireVelocityTrackerAndAddMovement");
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
    }

    private void releaseVelocityTracker() {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "releaseVelocityTracker");
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "onSecondaryPointerUp");
        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionX = mDownMotionX = ev.getX(newPointerIndex);
            mLastMotionY = ev.getY(newPointerIndex);
            mLastMotionXRemainder = 0;
            mActivePointerId = ev.getPointerId(newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "requestChildFocus");
        super.requestChildFocus(child, focused);
        int page = indexToPage(indexOfChild(child));
        if (page >= 0 && page != getCurrentPageIndex() && !isInTouchMode()) {
            snapToPage(page);
        }
    }

    protected int getChildIndexForRelativeOffset(int relativeOffset) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "getChildIndexForRelativeOffset");
        final int childCount = getChildCount();
        int left;
        int right;
        for (int i = 0; i < childCount; ++i) {
            left = getRelativeChildOffset(i);
            right = (left + getScaledMeasuredWidth(getPageAt(i)));
            if (left <= relativeOffset && relativeOffset <= right) {
                return i;
            }
        }
        return -1;
    }

    protected int getChildWidth(int index) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "getChildWidth");
        // This functions are called enough times that it actually makes a
        // difference in the
        // profiler -- so just inline the max() here
        final int measuredWidth = getPageAt(index).getMeasuredWidth();
        final int minWidth = mMinimumWidth;
        return (minWidth > measuredWidth) ? minWidth : measuredWidth;
    }
    protected int getChildHeight(int index) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "getChildHeight");
        // This functions are called enough times that it actually makes a
        // difference in the
        // profiler -- so just inline the max() here
        final int measuredWidth = getPageAt(index).getMeasuredHeight();
       
        return measuredWidth;
    }
    
    

    int getPageNearestToPoint(float x) {
        int index = 0;
        for (int i = 0; i < getChildCount(); ++i) {
            if (x < getChildAt(i).getRight() - getScrollX()) {
                return index;
            } else {
                index++;
            }
        }
        return Math.min(index, getChildCount() - 1);
    }

    int getPageNearestToCenterOfScreen() {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "getPageNearestToCenterOfScreen");
        int minDistanceFromScreenCenter = Integer.MAX_VALUE;
        int minDistanceFromScreenCenterIndex = -1;
        int screenCenter = getViewportOffsetX() + getScrollX() + (getViewportWidth() / 2);
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; ++i) {
            View layout = (View) getPageAt(i);
            int childWidth = getScaledMeasuredWidth(layout);
            int halfChildWidth = (childWidth / 2);
            int childCenter = getViewportOffsetX() + getChildOffset(i) + halfChildWidth;
            int distanceFromScreenCenter = Math.abs(childCenter - screenCenter);
            if (distanceFromScreenCenter < minDistanceFromScreenCenter) {
                minDistanceFromScreenCenter = distanceFromScreenCenter;
                minDistanceFromScreenCenterIndex = i;
            }
        }
        return minDistanceFromScreenCenterIndex;
    }

    protected void snapToDestination() {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "snapToDestination");
        snapToPage(getPageNearestToCenterOfScreen(), PAGE_SNAP_ANIMATION_DURATION);
    }

    private static class ScrollInterpolator implements Interpolator {
        public ScrollInterpolator() {
        }

        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1;
        }
    }

    // We want the duration of the page snap animation to be influenced by the
    // distance that
    // the screen has to travel, however, we don't want this duration to be
    // effected in a
    // purely linear fashion. Instead, we use this method to moderate the effect
    // that the distance
    // of travel has on the overall snap duration.
    float distanceInfluenceForSnapDuration(float f) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "distanceInfluenceForSnapDuration");
        f -= 0.5f; // center the values about 0.
        f *= 0.3f * Math.PI / 2.0f;
        return (float) Math.sin(f);
    }

    protected void snapToPageWithVelocity(int whichPage, int velocity) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "snapToPageWithVelocity");
        whichPage = Math.max(0, Math.min(whichPage, getChildCount() - 1));
        int halfScreenSize = getViewportWidth() / 2;

        if (DEBUG)
            DebugLog.d(LOG_TAG, "snapToPage.getChildOffset(): " + getChildOffset(whichPage));
        if (DEBUG)
            DebugLog.d(LOG_TAG, "snapToPageWithVelocity.getRelativeChildOffset(): " + getViewportWidth() + ", "
                    + getChildWidth(whichPage));
        final int newX = getChildOffset(whichPage) - getRelativeChildOffset(whichPage);
        int delta = newX - mUnboundedScrollX;
        int duration = 0;

        if (Math.abs(velocity) < mMinFlingVelocity) {
            // If the velocity is low enough, then treat this more as an
            // automatic page advance
            // as opposed to an apparent physical response to flinging
            snapToPage(whichPage, PAGE_SNAP_ANIMATION_DURATION);
            return;
        }

        // Here we compute a "distance" that will be used in the computation of
        // the overall
        // snap duration. This is a function of the actual distance that needs
        // to be traveled;
        // we keep this value close to half screen size in order to reduce the
        // variance in snap
        // duration as a function of the distance the page needs to travel.
        float distanceRatio = Math.min(1f, 1.0f * Math.abs(delta) / (2 * halfScreenSize));
        float distance = halfScreenSize + halfScreenSize * distanceInfluenceForSnapDuration(distanceRatio);

        velocity = Math.abs(velocity);
        velocity = Math.max(mMinSnapVelocity, velocity);

        // we want the page's snap velocity to approximately match the velocity
        // at which the
        // user flings, so we scale the duration by a value near to the
        // derivative of the scroll
        // interpolator at zero, ie. 5. We use 4 to make it a little slower.
        duration = 4 * Math.round(1000 * Math.abs(distance / velocity));

        snapToPage(whichPage, delta, duration);
    }

    protected void snapToPage(int whichPage) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "snapToPage");
        snapToPage(whichPage, PAGE_SNAP_ANIMATION_DURATION);
    }

    protected void snapToPageImmediately(int whichPage) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "snapToPageImmediately");
        snapToPage(whichPage, PAGE_SNAP_ANIMATION_DURATION, true);
    }

    protected void snapToPage(int whichPage, int duration) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "snapToPage");
        snapToPage(whichPage, duration, false);
    }

    protected void snapToPage(int whichPage, int duration, boolean immediate) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "snapToPage");
        whichPage = Math.max(0, Math.min(whichPage, getPageCount() - 1));

        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "snapToPage.getChildOffset(): " + getChildOffset(whichPage));
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "snapToPage.getRelativeChildOffset(): " + getViewportWidth() + ", "
                + getChildWidth(whichPage));
        int newX = getChildOffset(whichPage) - getRelativeChildOffset(whichPage);
        final int sX = mUnboundedScrollX;
        final int delta = newX - sX;
        snapToPage(whichPage, delta, duration, immediate);
    }

    protected void snapToPage(int whichPage, int delta, int duration) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "snapToPage");
        snapToPage(whichPage, delta, duration, false);
    }

    protected void snapToPage(int whichPage, int delta, int duration, boolean immediate) {
        mNextPage = whichPage;
//        notifyPageSwitching(whichPage);
        notifySwitching(whichPage);
        View focusedChild = getFocusedChild();
        if (focusedChild != null && whichPage != mCurrentPageIndex && focusedChild == getPageAt(mCurrentPageIndex)) {
            focusedChild.clearFocus();
        }

        pageBeginMoving();
        awakenScrollBars(duration);
        if (immediate) {
            duration = 0;
        } else if (duration == 0) {
            duration = Math.abs(delta);
        }

        if (!mScroller.isFinished())
            mScroller.abortAnimation();
        mScroller.startScroll(mUnboundedScrollX, 0, delta, 0, duration);

//      notifyPageSwitched();
//        notifySwitched();

        // Trigger a compute() to finish switching pages if necessary
        if (immediate) {
            computeScroll();
        }

        mForceScreenScrolled = true;
        invalidate();
    }

    public void scrollLeft() {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "scrollLeft");
        if (mScroller.isFinished()) {
            if (mCurrentPageIndex > 0)
                snapToPage(mCurrentPageIndex - 1);
        } else {
            if (mNextPage > 0)
                snapToPage(mNextPage - 1);
        }
    }

    public void scrollRight() {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "scrollRight");
        if (mScroller.isFinished()) {
            if (mCurrentPageIndex < getChildCount() - 1)
                snapToPage(mCurrentPageIndex + 1);
        } else {
            if (mNextPage < getChildCount() - 1)
                snapToPage(mNextPage + 1);
        }
    }

    public int getPageForView(View v) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "getPageForView");
        int result = -1;
        if (v != null) {
            ViewParent vp = v.getParent();
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                if (vp == getPageAt(i)) {
                    return i;
                }
            }
        }
        return result;
    }

    // Animate the drag view back to the original position
    void animateDragViewToOriginalPosition() {
        if (mDragView != null) {
            AnimatorSet anim = new AnimatorSet();
            anim.setDuration(REORDERING_DROP_REPOSITION_DURATION);
            anim.playTogether(ObjectAnimator.ofFloat(mDragView, "translationX", 0f),
                    ObjectAnimator.ofFloat(mDragView, "translationY", 0f));
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    onPostReorderingAnimationCompleted();
                }
            });
            anim.start();
        }
    }

    // "Zooms out" the PagedView to reveal more side pages
    protected boolean zoomOut() {
        if (mZoomInOutAnim != null && mZoomInOutAnim.isRunning()) {
            mZoomInOutAnim.cancel();
        }

        if (!(getScaleX() < 1f || getScaleY() < 1f)) {
            mZoomInOutAnim = new AnimatorSet();
            mZoomInOutAnim.setDuration(REORDERING_ZOOM_IN_OUT_DURATION);
            mZoomInOutAnim.playTogether(ObjectAnimator.ofFloat(this, "scaleX", mMinScale),
                    ObjectAnimator.ofFloat(this, "scaleY", mMinScale));
            mZoomInOutAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    // Show the delete drop target
                    if (mDeleteDropTarget != null) {
                        mDeleteDropTarget.setVisibility(View.VISIBLE);
                        mDeleteDropTarget.animate().alpha(1f)
                                .setDuration(REORDERING_DELETE_DROP_TARGET_FADE_DURATION)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {
                                        mDeleteDropTarget.setAlpha(0f);
                                    }
                                });
                    }
                }
            });
            mZoomInOutAnim.start();
            return true;
        }
        return false;
    }

    protected void onStartReordering() {
        if (AccessibilityManager.getInstance(getContext()).isEnabled()) {
            announceForAccessibility(getContext().getString(
                    R.string.zzzzz_gn_navil_keyguard_accessibility_widget_reorder_start));
        }

        // Set the touch state to reordering (allows snapping to pages, dragging
        // a child, etc.)
        mTouchState = TOUCH_STATE_REORDERING;
        mIsReordering = true;

        // Mark all the non-widget pages as invisible
        getVisiblePages(mTempVisiblePagesRange);
        boundByReorderablePages(true, mTempVisiblePagesRange);
        for (int i = 0; i < getPageCount(); ++i) {
            if (i < mTempVisiblePagesRange[0] || i > mTempVisiblePagesRange[1]) {
                getPageAt(i).setAlpha(0f);
            }
        }

        // We must invalidate to trigger a redraw to update the layers such that
        // the drag view
        // is always drawn on top
        invalidate();
    }

    private void onPostReorderingAnimationCompleted() {
        // Trigger the callback when reordering has settled
        --mPostReorderingPreZoomInRemainingAnimationCount;
        if (mPostReorderingPreZoomInRunnable != null && mPostReorderingPreZoomInRemainingAnimationCount == 0) {
            mPostReorderingPreZoomInRunnable.run();
            mPostReorderingPreZoomInRunnable = null;
        }
    }

    protected void onEndReordering() {
        if (AccessibilityManager.getInstance(getContext()).isEnabled()) {
            announceForAccessibility(getContext().getString(
                    R.string.zzzzz_gn_navil_keyguard_accessibility_widget_reorder_end));
        }
        mIsReordering = false;

        // Mark all the non-widget pages as visible again
        getVisiblePages(mTempVisiblePagesRange);
        boundByReorderablePages(true, mTempVisiblePagesRange);
        for (int i = 0; i < getPageCount(); ++i) {
            if (i < mTempVisiblePagesRange[0] || i > mTempVisiblePagesRange[1]) {
                getPageAt(i).setAlpha(1f);
            }
        }
    }

    public boolean startReordering() {
        int dragViewIndex = getPageNearestToCenterOfScreen();
        mTempVisiblePagesRange[0] = 0;
        mTempVisiblePagesRange[1] = getPageCount() - 1;
        boundByReorderablePages(true, mTempVisiblePagesRange);
        mReorderingStarted = true;

        // Check if we are within the reordering range
        if (mTempVisiblePagesRange[0] <= dragViewIndex && dragViewIndex <= mTempVisiblePagesRange[1]) {
            if (zoomOut()) {
                // Find the drag view under the pointer
                mDragView = getChildAt(dragViewIndex);

                onStartReordering();
            }
            return true;
        }
        return false;
    }

    boolean isReordering(boolean testTouchState) {
        boolean state = mIsReordering;
        if (testTouchState) {
            state &= (mTouchState == TOUCH_STATE_REORDERING);
        }
        return state;
    }

    void endReordering() {
        // For simplicity, we call endReordering sometimes even if reordering
        // was never started.
        // In that case, we don't want to do anything.
        if (!mReorderingStarted)
            return;
        mReorderingStarted = false;

        // If we haven't flung-to-delete the current child, then we just animate
        // the drag view
        // back into position
        final Runnable onCompleteRunnable = new Runnable() {
            @Override
            public void run() {
                onEndReordering();
            }
        };
        if (!mDeferringForDelete) {
            mPostReorderingPreZoomInRunnable = new Runnable() {
                public void run() {
                    zoomIn(onCompleteRunnable);
                };
            };

            mPostReorderingPreZoomInRemainingAnimationCount = NUM_ANIMATIONS_RUNNING_BEFORE_ZOOM_OUT;
            // Snap to the current page
            snapToPage(indexOfChild(mDragView), 0);
            // Animate the drag view back to the front position
            animateDragViewToOriginalPosition();
        } else {
            // Handled in post-delete-animation-callbacks
        }
    }

    // "Zooms in" the PagedView to highlight the current page
    protected boolean zoomIn(final Runnable onCompleteRunnable) {
        if (mZoomInOutAnim != null && mZoomInOutAnim.isRunning()) {
            mZoomInOutAnim.cancel();
        }
        if (getScaleX() < 1f || getScaleY() < 1f) {
            mZoomInOutAnim = new AnimatorSet();
            mZoomInOutAnim.setDuration(REORDERING_ZOOM_IN_OUT_DURATION);
            mZoomInOutAnim.playTogether(ObjectAnimator.ofFloat(this, "scaleX", 1f),
                    ObjectAnimator.ofFloat(this, "scaleY", 1f));
            mZoomInOutAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    // Hide the delete drop target
                    if (mDeleteDropTarget != null) {
                        mDeleteDropTarget.animate().alpha(0f)
                                .setDuration(REORDERING_DELETE_DROP_TARGET_FADE_DURATION)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        mDeleteDropTarget.setVisibility(View.GONE);
                                    }
                                });
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    mDragView = null;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mDragView = null;
                    if (onCompleteRunnable != null) {
                        onCompleteRunnable.run();
                    }
                }
            });
            mZoomInOutAnim.start();
            return true;
        } else {
            if (onCompleteRunnable != null) {
                onCompleteRunnable.run();
            }
        }
        return false;
    }

    /*
     * Flinging to delete - IN PROGRESS
     */
    private PointF isFlingingToDelete() {
        ViewConfiguration config = ViewConfiguration.get(getContext());
        mVelocityTracker.computeCurrentVelocity(1000, config.getScaledMaximumFlingVelocity());

        if (mVelocityTracker.getYVelocity() < mFlingToDeleteThresholdVelocity) {
            // Do a quick dot product test to ensure that we are flinging
            // upwards
            PointF vel = new PointF(mVelocityTracker.getXVelocity(), mVelocityTracker.getYVelocity());
            PointF upVec = new PointF(0f, -1f);
            float theta = (float) Math.acos(((vel.x * upVec.x) + (vel.y * upVec.y))
                    / (vel.length() * upVec.length()));
            if (theta <= Math.toRadians(FLING_TO_DELETE_MAX_FLING_DEGREES)) {
                return vel;
            }
        }
        return null;
    }

    /**
     * Creates an animation from the current drag view along its current velocity vector. For this animation,
     * the alpha runs for a fixed duration and we update the position progressively.
     */
    private static class FlingAlongVectorAnimatorUpdateListener implements AnimatorUpdateListener {
        private View mDragView;
        private PointF mVelocity;
        private Rect mFrom;
        private long mPrevTime;
        private float mFriction;

        private final TimeInterpolator mAlphaInterpolator = new DecelerateInterpolator(0.75f);

        public FlingAlongVectorAnimatorUpdateListener(View dragView, PointF vel, Rect from, long startTime,
                float friction) {
            mDragView = dragView;
            mVelocity = vel;
            mFrom = from;
            mPrevTime = startTime;
            mFriction = 1f - (mDragView.getResources().getDisplayMetrics().density * friction);
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float t = ((Float) animation.getAnimatedValue()).floatValue();
            long curTime = AnimationUtils.currentAnimationTimeMillis();

            mFrom.left += (mVelocity.x * (curTime - mPrevTime) / 1000f);
            mFrom.top += (mVelocity.y * (curTime - mPrevTime) / 1000f);

            mDragView.setTranslationX(mFrom.left);
            mDragView.setTranslationY(mFrom.top);
            mDragView.setAlpha(1f - mAlphaInterpolator.getInterpolation(t));

            mVelocity.x *= mFriction;
            mVelocity.y *= mFriction;
            mPrevTime = curTime;
        }
    };

    private Runnable createPostDeleteAnimationRunnable(final View dragView) {
        return new Runnable() {
            @Override
            public void run() {
                int dragViewIndex = indexOfChild(dragView);

                // For each of the pages around the drag view, animate them from
                // the previous
                // position to the new position in the layout (as a result of
                // the drag view moving
                // in the layout)
                // NOTE: We can make an assumption here because we have
                // side-bound pages that we
                // will always have pages to animate in from the left
                getVisiblePages(mTempVisiblePagesRange);
                boundByReorderablePages(true, mTempVisiblePagesRange);
                boolean isLastWidgetPage = (mTempVisiblePagesRange[0] == mTempVisiblePagesRange[1]);
                boolean slideFromLeft = (isLastWidgetPage || dragViewIndex > mTempVisiblePagesRange[0]);

                // Setup the scroll to the correct page before we swap the views
                if (slideFromLeft) {
                    snapToPageImmediately(dragViewIndex - 1);
                }

                int firstIndex = (isLastWidgetPage ? 0 : mTempVisiblePagesRange[0]);
                int lastIndex = Math.min(mTempVisiblePagesRange[1], getPageCount() - 1);
                int lowerIndex = (slideFromLeft ? firstIndex : dragViewIndex + 1);
                int upperIndex = (slideFromLeft ? dragViewIndex - 1 : lastIndex);
                ArrayList<Animator> animations = new ArrayList<Animator>();
                for (int i = lowerIndex; i <= upperIndex; ++i) {
                    View v = getChildAt(i);
                    // dragViewIndex < pageUnderPointIndex, so after we remove
                    // the
                    // drag view all subsequent views to pageUnderPointIndex
                    // will
                    // shift down.
                    int oldX = 0;
                    int newX = 0;
                    if (slideFromLeft) {
                        if (i == 0) {
                            // Simulate the page being offscreen with the page
                            // spacing
                            oldX = getViewportOffsetX() + getChildOffset(i) - getChildWidth(i) - mPageSpacing;
                        } else {
                            oldX = getViewportOffsetX() + getChildOffset(i - 1);
                        }
                        newX = getViewportOffsetX() + getChildOffset(i);
                    } else {
                        oldX = getChildOffset(i) - getChildOffset(i - 1);
                        newX = 0;
                    }

                    // Animate the view translation from its old position to its
                    // new
                    // position
                    AnimatorSet anim = (AnimatorSet) v.getTag();
                    if (anim != null) {
                        anim.cancel();
                    }

                    // Note: Hacky, but we want to skip any optimizations to not
                    // draw completely
                    // hidden views
                    v.setAlpha(Math.max(v.getAlpha(), 0.01f));
                    v.setTranslationX(oldX - newX);
                    anim = new AnimatorSet();
                    anim.playTogether(ObjectAnimator.ofFloat(v, "translationX", 0f),
                            ObjectAnimator.ofFloat(v, "alpha", 1f));
                    animations.add(anim);
                    v.setTag(anim);
                }

                AnimatorSet slideAnimations = new AnimatorSet();
                slideAnimations.playTogether(animations);
                slideAnimations.setDuration(DELETE_SLIDE_IN_SIDE_PAGE_DURATION);
                slideAnimations.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        final Runnable onCompleteRunnable = new Runnable() {
                            @Override
                            public void run() {
                                mDeferringForDelete = false;
                                onEndReordering();
                            }
                        };
                        zoomIn(onCompleteRunnable);
                    }
                });
                slideAnimations.start();

                removeView(dragView);
                // gionee gaojt modify for CR00797101 2013-05-06 start
                onRemoveView(dragView, false);
                // gionee gaojt modify for CR00797101 2013-05-06 end
            }
        };
    }

    public void onFlingToDelete(PointF vel) {
        final long startTime = AnimationUtils.currentAnimationTimeMillis();

        // NOTE: Because it takes time for the first frame of animation to
        // actually be
        // called and we expect the animation to be a continuation of the fling,
        // we have
        // to account for the time that has elapsed since the fling finished.
        // And since
        // we don't have a startDelay, we will always get call to update when we
        // call
        // start() (which we want to ignore).
        final TimeInterpolator tInterpolator = new TimeInterpolator() {
            private int mCount = -1;
            private long mStartTime;
            private float mOffset;
            /* Anonymous inner class ctor */{
                mStartTime = startTime;
            }

            @Override
            public float getInterpolation(float t) {
                if (mCount < 0) {
                    mCount++;
                } else if (mCount == 0) {
                    mOffset = Math.min(0.5f,
                            (float) (AnimationUtils.currentAnimationTimeMillis() - mStartTime)
                                    / FLING_TO_DELETE_FADE_OUT_DURATION);
                    mCount++;
                }
                return Math.min(1f, mOffset + t);
            }
        };

        final Rect from = new Rect();
        final View dragView = mDragView;
        from.left = (int) dragView.getTranslationX();
        from.top = (int) dragView.getTranslationY();
        AnimatorUpdateListener updateCb = new FlingAlongVectorAnimatorUpdateListener(dragView, vel, from,
                startTime, FLING_TO_DELETE_FRICTION);

        final Runnable onAnimationEndRunnable = createPostDeleteAnimationRunnable(dragView);

        // Create and start the animation
        ValueAnimator mDropAnim = new ValueAnimator();
        mDropAnim.setInterpolator(tInterpolator);
        mDropAnim.setDuration(FLING_TO_DELETE_FADE_OUT_DURATION);
        mDropAnim.setFloatValues(0f, 1f);
        mDropAnim.addUpdateListener(updateCb);
        mDropAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                onAnimationEndRunnable.run();
            }
        });
        mDropAnim.start();
        mDeferringForDelete = true;
    }

    /* Drag to delete */
    private boolean isHoveringOverDeleteDropTarget(int x, int y) {
        if (mDeleteDropTarget != null) {
            mAltTmpRect.set(0, 0, 0, 0);
            View parent = (View) mDeleteDropTarget.getParent();
            if (parent != null) {
                parent.getGlobalVisibleRect(mAltTmpRect);
            }
            mDeleteDropTarget.getGlobalVisibleRect(mTmpRect);
            mTmpRect.offset(-mAltTmpRect.left, -mAltTmpRect.top);
            return mTmpRect.contains(x, y);
        }
        return false;
    }

    protected void setPageHoveringOverDeleteDropTarget(int viewIndex, boolean isHovering) {
    }

    private void onDropToDelete() {
        final View dragView = mDragView;

        final float toScale = 0f;
        final float toAlpha = 0f;

        // Create and start the complex animation
        ArrayList<Animator> animations = new ArrayList<Animator>();
        AnimatorSet motionAnim = new AnimatorSet();
        motionAnim.setInterpolator(new DecelerateInterpolator(2));
        motionAnim.playTogether(ObjectAnimator.ofFloat(dragView, "scaleX", toScale),
                ObjectAnimator.ofFloat(dragView, "scaleY", toScale));
        animations.add(motionAnim);

        AnimatorSet alphaAnim = new AnimatorSet();
        alphaAnim.setInterpolator(new LinearInterpolator());
        alphaAnim.playTogether(ObjectAnimator.ofFloat(dragView, "alpha", toAlpha));
        animations.add(alphaAnim);

        final Runnable onAnimationEndRunnable = createPostDeleteAnimationRunnable(dragView);

        AnimatorSet anim = new AnimatorSet();
        anim.playTogether(animations);
        anim.setDuration(DRAG_TO_DELETE_FADE_OUT_DURATION);
        anim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                onAnimationEndRunnable.run();
            }
        });
        anim.start();

        mDeferringForDelete = true;
    }

    /* Accessibility */
    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setScrollable(getPageCount() > 1);
        if (getCurrentPageIndex() < getPageCount() - 1) {
            info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
        }
        if (getCurrentPageIndex() > 0) {
            info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
        }
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setScrollable(true);
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            event.setFromIndex(mCurrentPageIndex);
            event.setToIndex(mCurrentPageIndex);
            event.setItemCount(getChildCount());
        }
    }

    @Override
    public boolean performAccessibilityAction(int action, Bundle arguments) {
        if (super.performAccessibilityAction(action, arguments)) {
            return true;
        }
        switch (action) {
            case AccessibilityNodeInfo.ACTION_SCROLL_FORWARD: {
                if (getCurrentPageIndex() < getPageCount() - 1) {
                    scrollRight();
                    return true;
                }
            }
                break;
            case AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD: {
                if (getCurrentPageIndex() > 0) {
                    scrollLeft();
                    return true;
                }
            }
                break;
        }
        return false;
    }

    @Override
    public boolean onHoverEvent(android.view.MotionEvent event) {
        return true;
    }

    @Override
    protected void finalize() throws Throwable {
        // TODO Auto-generated method stub
        super.finalize();
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "finalize");
    }
}