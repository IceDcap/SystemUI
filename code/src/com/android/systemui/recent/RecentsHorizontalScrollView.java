/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.recent;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.res.Configuration;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.OverScroller;
import com.android.systemui.R;
import com.android.systemui.SwipeHelper;
import com.android.systemui.recent.RecentsPanelView.TaskDescriptionAdapter;
import com.android.systemui.recent.RecentsPanelView.ViewHolder;
import java.util.HashSet;
import java.util.Iterator;
import android.app.IActivityManager;
import android.app.ActivityManagerNative;
import android.os.UserHandle;

public class RecentsHorizontalScrollView extends HorizontalScrollView
        implements SwipeHelper.Callback, RecentsPanelView.RecentsScrollView {
    private static final int VELOCITY_DECAY_COEFFICIENT = 500;

	private static final int MIN_SCROLL_TIME = 300;
    private static final String TAG = RecentsPanelView.TAG;
    private static final boolean DEBUG = true;//RecentsPanelView.DEBUG;
    private LinearLayout mLinearLayout;
    private TaskDescriptionAdapter mAdapter;
    private RecentsCallback mCallback;
    protected int mLastScrollPosition;
    private SwipeHelper mSwipeHelper;
    private FadedEdgeDrawHelper mFadedEdgeDrawHelper;
    private HashSet<View> mRecycledViews;
    private int mNumItemsInOneScreenful;
    private Runnable mOnScrollListener;
    
    private int mCellWidth = 0;
    private Handler mHandler = new Handler();

    /** The m scroller. */
    private OverScroller mScroller;
    /**
     * Position of the last motion event.
     */
    private int mLastMotionX;
    
    /** The m scroll margin top. */
    private int mScrollMarginTop;
    
    /**
     * True if the user is currently dragging this ScrollView around. This is not the same
     * as 'is being flinged', which can be checked by mScroller.isFinished() (flinging
     * begins when the user lifts his finger).
     */
    private boolean mIsBeingDragged = false;

    /** Determines speed during touch scrolling. */
    private VelocityTracker mVelocityTracker;

    /** The m touch slop. */
    private int mTouchSlop;
    
    /** The m minimum velocity. */
    private int mMinimumVelocity;
    
    /** The m maximum velocity. */
    private int mMaximumVelocity;

    /** The m overscroll distance. */
    private int mOverscrollDistance;
    
    /** The m overfling distance. */
    private int mOverflingDistance;

    /**
     * ID of the active pointer. This is used to retain consistency during drags/flings if
     * multiple pointers are used.
     */
    private int mActivePointerId = INVALID_POINTER;

    /**
     * Sentinel value for no current active pointer. Used by {@link #mActivePointerId}.
     */
    private static final int INVALID_POINTER = -1;

    /** The m start activity. */
    private boolean mStartingActivity = false;
    private boolean mNeedScroll = true;
//	protected MotionEvent mInitEvent;

    /**
 * Instantiates a new recents horizontal scroll view.
 *
 * @param context the context
 * @param attrs the attrs
 */
 	private IActivityManager mAm;
    public RecentsHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        initScrollView();
        float densityScale = getResources().getDisplayMetrics().density;
        float pagingTouchSlop = ViewConfiguration.get(context)
                .getScaledPagingTouchSlop();
        mSwipeHelper = new SwipeHelper(SwipeHelper.Y, this, context);
        mFadedEdgeDrawHelper = FadedEdgeDrawHelper.create(context, attrs, this, false);
        mRecycledViews = new HashSet<View>();
        mAm = ActivityManagerNative.getDefault();
    }

    /**
     * Inits the scroll view.
     */
    private void initScrollView() {

        mScroller = new OverScroller(getContext(), new DecelerateInterpolator());
        setFocusable(true);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setWillNotDraw(false);
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mOverscrollDistance = configuration.getScaledOverscrollDistance();
        mOverflingDistance = configuration.getScaledOverflingDistance();
        mScrollMarginTop = getResources().getDimensionPixelSize(R.dimen.gn_recent_info_zone_height);
    }

    public void setMinSwipeAlpha(float minAlpha) {
        mSwipeHelper.setMinSwipeProgress(minAlpha);
    }

    private int scrollPositionOfMostRecent() {
        boolean loadFromHome = RecentTasksLoader.getInstance(mContext).loadFromHome();
        Log.d(TAG, "loadFromHome " + loadFromHome);
        if (loadFromHome) {
            return 0;
        } else {
            return mCellWidth;
        }
    }

    private void addToRecycledViews(View v) {
        if (mRecycledViews.size() < mNumItemsInOneScreenful) {
            mRecycledViews.add(v);
        }
    }

    public View findViewForTask(int persistentTaskId) {
        for (int i = 0; i < mLinearLayout.getChildCount(); i++) {
            View v = mLinearLayout.getChildAt(i);
            RecentsPanelView.ViewHolder holder = (RecentsPanelView.ViewHolder) v.getTag();
            if (holder.taskDescription.persistentTaskId == persistentTaskId) {
                return v;
            }
        }
        return null;
    }

    private void update() {
        for (int i = 0; i < mLinearLayout.getChildCount(); i++) {
            View v = mLinearLayout.getChildAt(i);
            addToRecycledViews(v);
            mAdapter.recycleView(v);
        }
        LayoutTransition transitioner = getLayoutTransition();
        setLayoutTransition(null);

        mLinearLayout.removeAllViews();
        Iterator<View> recycledViews = mRecycledViews.iterator();
        
        for (int i = mAdapter.getCount() -1; i >= 0 ; i--) {
            View old = null;
            if (recycledViews.hasNext()) {
                old = recycledViews.next();
                recycledViews.remove();
                old.setVisibility(VISIBLE);
            }

            final View view = mAdapter.getView(i, old, mLinearLayout);

            if (mFadedEdgeDrawHelper != null) {
                mFadedEdgeDrawHelper.addViewCallback(view);
            }

            OnTouchListener noOpListener = new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            };

            view.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                }
            });
            // We don't want a click sound when we dimiss recents
            view.setSoundEffectsEnabled(false);

            OnClickListener launchAppListener = new OnClickListener() {
                public void onClick(View v) {
                    mCallback.handleOnClick(view);
                }
            };
            
            OnClickListener lockToScreenClickListener =  new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					mCallback.handleLockToScreen(view);
				}
			};

            RecentsPanelView.ViewHolder holder = (RecentsPanelView.ViewHolder) view.getTag();
            final View thumbnailView = holder.thumbnailView;
            final View lockToScreenView = holder.lockToScreenView;
            /*OnLongClickListener longClickListener = new OnLongClickListener() {
                public boolean onLongClick(View v) {
                    final View anchorView = view.findViewById(R.id.app_description);
                    mCallback.handleLongPress(view, anchorView, thumbnailView);
                    return true;
                }
            };*/
            thumbnailView.setClickable(true);
            thumbnailView.setOnClickListener(launchAppListener);
            lockToScreenView.setClickable(true);
            lockToScreenView.setOnClickListener(lockToScreenClickListener);
            //thumbnailView.setOnLongClickListener(longClickListener);

            // We don't want to dismiss recents if a user clicks on the app title
            // (we also don't want to launch the app either, though, because the
            // app title is a small target and doesn't have great click feedback)
            final View appTitle = view.findViewById(R.id.app_label);
            appTitle.setContentDescription(" ");
            appTitle.setOnTouchListener(noOpListener);
            mLinearLayout.addView(view);
        }
        setLayoutTransition(transitioner);

        // Scroll to end after initial layout.

        final OnGlobalLayoutListener updateScroll = new OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    mLastScrollPosition = scrollPositionOfMostRecent();
                    scrollTo(mLastScrollPosition, 0);
                    final ViewTreeObserver observer = getViewTreeObserver();
                    if (observer.isAlive()) {
                        observer.removeOnGlobalLayoutListener(this);
                    }
                }
            };
        getViewTreeObserver().addOnGlobalLayoutListener(updateScroll);
    }

    @Override
    public void removeViewInLayout(final View view) {
        dismissChild(view);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (DEBUG) Log.v(TAG, "onInterceptTouchEvent()");
        return mSwipeHelper.onInterceptTouchEvent(ev) || interceptTouchEvent(ev);
		//super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mSwipeHelper.onTouchEvent(ev) || touchEvent(ev);
		//super.onTouchEvent(ev);
    }

    public boolean canChildBeDismissed(View v) {
        View contentView = getChildContentView(v);
        ViewHolder holder = (ViewHolder) v.getTag();
        if ((contentView != null && contentView.getTranslationY() > 0) 
           || (holder != null && holder.isLockApp)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean isAntiFalsingNeeded() {
        return true;
    }

    @Override
    public float getFalsingThresholdFactor() {
        return 1.0f;
    }

    public void dismissChild(View v) {
        mSwipeHelper.dismissChild(v, 0);
    }

    public void onChildDismissed(View v) {
        addToRecycledViews(v);
        mLinearLayout.removeView(v);
        mCallback.handleSwipe(v);
        // Restore the alpha/translation parameters to what they were before swiping
        // (for when these items are recycled)
        View contentView = getChildContentView(v);
        contentView.setAlpha(1f);
        contentView.setTranslationY(0);
    }
    
    public void removeChild(View v) {
        addToRecycledViews(v);
        mLinearLayout.removeView(v);
        mCallback.removeTaskDescription(v);
        // Restore the alpha/translation parameters to what they were before swiping
        // (for when these items are recycled)
        View contentView = getChildContentView(v);
        contentView.setAlpha(1f);
        contentView.setTranslationY(0);
    }

    public void onBeginDrag(View v) {
        // We do this so the underlying ScrollView knows that it won't get
        // the chance to intercept events anymore
        requestDisallowInterceptTouchEvent(true);
    }

    public void onDragCancelled(View v) {
    }

    @Override
    public void onChildSnappedBack(View animView) {
    }

    @Override
    public boolean updateSwipeProgress(View animView, boolean dismissable, float swipeProgress) {
        return true;//false;
    }

    public View getChildAtPosition(MotionEvent ev) {
        final float x = ev.getX() + getScrollX();
        final float y = ev.getY() + getScrollY();
        for (int i = 0; i < mLinearLayout.getChildCount(); i++) {
            View item = mLinearLayout.getChildAt(i);
            if (x >= item.getLeft() && x < item.getRight()
                && y >= item.getTop() && y < item.getBottom()) {
                return item;
            }
        }
        return null;
    }

    public View getChildContentView(View v) {
        return v.findViewById(R.id.recent_item);
    }

    @Override
    public void drawFadedEdges(Canvas canvas, int left, int right, int top, int bottom) {
        if (mFadedEdgeDrawHelper != null) {

            mFadedEdgeDrawHelper.drawCallback(canvas,
                    left, right, top, bottom, getScrollX(), getScrollY(),
                    0, 0,
                    getLeftFadingEdgeStrength(), getRightFadingEdgeStrength(), getPaddingTop());
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
       super.onScrollChanged(l, t, oldl, oldt);
       if (mOnScrollListener != null) {
           mOnScrollListener.run();
       }
    }

    public void setOnScrollListener(Runnable listener) {
        mOnScrollListener = listener;
    }

    @Override
    public int getVerticalFadingEdgeLength() {
        if (mFadedEdgeDrawHelper != null) {
            return mFadedEdgeDrawHelper.getVerticalFadingEdgeLength();
        } else {
            return super.getVerticalFadingEdgeLength();
        }
    }

    @Override
    public int getHorizontalFadingEdgeLength() {
        if (mFadedEdgeDrawHelper != null) {
            return mFadedEdgeDrawHelper.getHorizontalFadingEdgeLength();
        } else {
            return super.getHorizontalFadingEdgeLength();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setScrollbarFadingEnabled(true);
        mLinearLayout = (LinearLayout) findViewById(R.id.recents_linear_layout);
        final int leftPadding = getContext().getResources()
            .getDimensionPixelOffset(R.dimen.status_bar_recents_thumbnail_left_margin);
        setOverScrollEffectPadding(leftPadding, 0);
    }

    @Override
    public void onAttachedToWindow() {
        if (mFadedEdgeDrawHelper != null) {
            mFadedEdgeDrawHelper.onAttachedToWindowCallback(mLinearLayout, isHardwareAccelerated());
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        float densityScale = getResources().getDisplayMetrics().density;
        mSwipeHelper.setDensityScale(densityScale);
        float pagingTouchSlop = ViewConfiguration.get(getContext()).getScaledPagingTouchSlop();
        mSwipeHelper.setPagingTouchSlop(pagingTouchSlop);
    }

    private void setOverScrollEffectPadding(int leftPadding, int i) {
        // TODO Add to (Vertical)ScrollView
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Skip this work if a transition is running; it sets the scroll values independently
        // and should not have those animated values clobbered by this logic
        LayoutTransition transition = mLinearLayout.getLayoutTransition();
        if (transition != null && transition.isRunning()) {
            return;
        }
        // Keep track of the last visible item in the list so we can restore it
        // to the bottom when the orientation changes.
        mLastScrollPosition = scrollPositionOfMostRecent();

        // This has to happen post-layout, so run it "in the future"
        post(new Runnable() {
            public void run() {
                // Make sure we're still not clobbering the transition-set values, since this
                // runnable launches asynchronously
                LayoutTransition transition = mLinearLayout.getLayoutTransition();
                if (transition == null || !transition.isRunning()) {
                    scrollTo(mLastScrollPosition, 0);
                }
            }
        });
    }

    public void setAdapter(TaskDescriptionAdapter adapter) {
    	Log.d(TAG, "adapter.getCount() = " + adapter.getCount());
        mAdapter = adapter;
        mAdapter.registerDataSetObserver(new DataSetObserver() {
            public void onChanged() {
                update();
            }

            public void onInvalidated() {
                update();
            }
        });
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int childWidthMeasureSpec =
                MeasureSpec.makeMeasureSpec(dm.widthPixels, MeasureSpec.AT_MOST);
        int childheightMeasureSpec =
                MeasureSpec.makeMeasureSpec(dm.heightPixels, MeasureSpec.AT_MOST);
        View child = mAdapter.createView(mLinearLayout);
        child.measure(childWidthMeasureSpec, childheightMeasureSpec);
        mNumItemsInOneScreenful =
                (int) FloatMath.ceil(dm.widthPixels / (float) child.getMeasuredWidth());
        addToRecycledViews(child);
        
        mCellWidth = child.getMeasuredWidth();

        for (int i = 0; i < mNumItemsInOneScreenful - 1; i++) {
            addToRecycledViews(mAdapter.createView(mLinearLayout));
        }
    }

    public int numItemsInOneScreenful() {
        return mNumItemsInOneScreenful;
    }

    @Override
    public void setLayoutTransition(LayoutTransition transition) {
        // The layout transition applies to our embedded LinearLayout
        mLinearLayout.setLayoutTransition(transition);
    }

    public void setCallback(RecentsCallback callback) {
        mCallback = callback;
    }

    private boolean interceptTouchEvent(MotionEvent ev) {
        /*
         * This method JUST determines whether we want to intercept the motion. If we
         * return true, onMotionEvent will be called and we do the actual scrolling there.
         */

        /*
         * Shortcut the most recurring case: the user is in the dragging state and he is
         * moving his finger. We want to intercept this motion.
         */
        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mIsBeingDragged)) {
            return true;
        }

        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_MOVE: {
            /*
             * mIsBeingDragged == false, otherwise the shortcut would have caught it.
             * Check whether the user has moved far enough from his original down touch.
             */

            /*
             * Locally do absolute value. mLastMotionX is set to the x value of the down
             * event.
             */
            final int activePointerId = mActivePointerId;
            if (activePointerId == INVALID_POINTER) {
                // If we don't have a valid id, the touch down wasn't on content.
                break;
            }

            final int pointerIndex = ev.findPointerIndex(activePointerId);
            if (pointerIndex == -1) {
                Log.e(TAG, "Invalid pointerId=" + activePointerId
                        + " in onInterceptTouchEvent");
                break;
            }

            final int x = (int) ev.getX(pointerIndex);
            final int xDiff = (int) Math.abs(x - mLastMotionX);
            if (xDiff > mTouchSlop) {
                setHorizontalScrollBarEnabled(true);
                mIsBeingDragged = true;
                mLastMotionX = x;
                initVelocityTrackerIfNotExists();
                mVelocityTracker.addMovement(ev);
                if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(
                        true);
            }
            break;
        }

        case MotionEvent.ACTION_DOWN: {
            final int x = (int) ev.getX();
            if (!inChild((int) x, (int) ev.getY())) {
                mIsBeingDragged = false;
                recycleVelocityTracker();
                break;
            }

            /*
             * Remember location of down touch. ACTION_DOWN always refers to pointer index
             * 0.
             */
            mLastMotionX = x;
            mActivePointerId = ev.getPointerId(0);

            initOrResetVelocityTracker();
            mVelocityTracker.addMovement(ev);

            /*
             * If being flinged and user touches the screen, initiate drag; otherwise
             * don't. mScroller.isFinished should be false when being flinged.
             */
            mIsBeingDragged = false;
            break;
        }

        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            /* Release the drag */
            mIsBeingDragged = false;
            mActivePointerId = INVALID_POINTER;
            if (mScroller.springBack(getScrollX(), getScrollY(), 0, getScrollRange(), 0,
                    0)) {
                postInvalidateOnAnimation();
            }
            break;
        case MotionEvent.ACTION_POINTER_DOWN: {
            final int index = ev.getActionIndex();
            mLastMotionX = (int) ev.getX(index);
            mActivePointerId = ev.getPointerId(index);
            break;
        }
        case MotionEvent.ACTION_POINTER_UP:
            onSecondaryPointerUp(ev);
            mLastMotionX = (int) ev.getX(ev.findPointerIndex(mActivePointerId));
            break;
        }

        /*
         * The only time we want to intercept motion events is if we are in the drag mode.
         */
        return mIsBeingDragged;
    }
    
    /**
     * On secondary pointer up.
     *
     * @param ev the ev
     */
    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionX = (int) ev.getX(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }
    
    /**
     * Inits the or reset velocity tracker.
     */
    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }
    
    /**
     * In child.
     *
     * @param x the x
     * @param y the y
     * @return true, if successful
     */
    private boolean inChild(int x, int y) {
        if (getChildCount() > 0) {
            final int scrollX = getScrollX();
            final View child = getChildAt(0);
            return !(y < child.getTop() || y >= child.getBottom()
                    || x < child.getLeft() - scrollX || x >= child.getRight() - scrollX);
        }
        return false;
    }

    
    private int getScrollRange() {
        int scrollRange = 0;
        if (getChildCount() > 0) {
            View child = getChildAt(0);
            scrollRange = Math.max(0, child.getWidth()
                    - (getWidth() - getPaddingLeft() - getPaddingRight()));
        }
        return scrollRange;
    }

    private boolean touchEvent(MotionEvent ev) {
        initVelocityTrackerIfNotExists();
        mVelocityTracker.addMovement(ev);

        final int action = ev.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN: {
            if (getChildCount() == 0 || !inScrollArea(ev)) {
                return false;
            }
            if ((mIsBeingDragged = !mScroller.isFinished())) {
                final ViewParent parent = getParent();
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
            }

            /*
             * If being flinged and user touches, stop the fling. isFinished will be false
             * if being flinged.
             */
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }

            // Remember where the motion event started
            mLastMotionX = (int) ev.getX();
            mActivePointerId = ev.getPointerId(0);
            break;
        }
        case MotionEvent.ACTION_MOVE:
            final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
            if (activePointerIndex == -1) {
                Log.e(TAG, "Invalid pointerId=" + mActivePointerId + " in onTouchEvent");
                break;
            }

            final int x = (int) ev.getX(activePointerIndex);
            int deltaX = mLastMotionX - x;
            if (!mIsBeingDragged && Math.abs(deltaX) > mTouchSlop) {
                final ViewParent parent = getParent();
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
                mIsBeingDragged = true;
                if (deltaX > 0) {
                    deltaX -= mTouchSlop;
                } else {
                    deltaX += mTouchSlop;
                }
            }
            if (mIsBeingDragged) {
                // Scroll to follow the motion event
                mLastMotionX = x;

                final int oldX = getScrollX();
                final int oldY = getScrollY();
                final int range = getScrollRange();
                final int overscrollMode = getOverScrollMode();
                final boolean canOverscroll = overscrollMode == OVER_SCROLL_ALWAYS
                        || (overscrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && range > 0);

                if (overScrollBy(deltaX, 0, getScrollX(), 0, range, 0,
                        mOverscrollDistance, 0, true)) {
                    // Break our velocity if we hit a scroll barrier.
                    mVelocityTracker.clear();
                }
                onScrollChanged(getScrollX(), getScrollY(), oldX, oldY);

            }
            break;
        case MotionEvent.ACTION_UP:
            if (mIsBeingDragged) {
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int initialVelocity = (int) velocityTracker
                        .getXVelocity(mActivePointerId);

                if (getChildCount() > 0) {
                    if ((Math.abs(initialVelocity) > mMinimumVelocity)) {
                        fling(-initialVelocity);
                    } else {
                        int finalX = getScrollX();
                        finalX = getScrollXFromFinalX(finalX);
                        if (mScroller.springBack(getScrollX(), getScrollY(), finalX,
                                finalX, 0, 0)) {
                            postInvalidateOnAnimation();
                        }
                    }
                }

                mActivePointerId = INVALID_POINTER;
                mIsBeingDragged = false;
                recycleVelocityTracker();
            }
            break;
        case MotionEvent.ACTION_CANCEL:
            if (mIsBeingDragged && getChildCount() > 0) {
                if (mScroller.springBack(getScrollX(), getScrollY(), 0, getScrollRange(),
                        0, 0)) {
                    postInvalidateOnAnimation();
                }
                mActivePointerId = INVALID_POINTER;
                mIsBeingDragged = false;
                recycleVelocityTracker();

            }
            break;
        case MotionEvent.ACTION_POINTER_UP:
            onSecondaryPointerUp(ev);
            break;
        }
        return true;
    }
    
    /**
     * In scroll area.
     *
     * @param ev the ev
     * @return true, if successful
     */
    private boolean inScrollArea(MotionEvent ev) {
        return ev.getY() > mScrollMarginTop ? true : false;
    }
    
    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            // This is called at drawing time by ViewGroup. We don't want to
            // re-show the scrollbars at this point, which scrollTo will do,
            // so we replicate most of scrollTo here.
            //
            // It's a little odd to call onScrollChanged from inside the drawing.
            //
            // It is, except when you remember that computeScroll() is used to
            // animate scrolling. So unless we want to defer the onScrollChanged()
            // until the end of the animated scrolling, we don't really have a
            // choice here.
            //
            // I agree. The alternative, which I think would be worse, is to post
            // something and tell the subclasses later. This is bad because there
            // will be a window where mScrollX/Y is different from what the app
            // thinks it is.
            //
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();

            if (oldX != x || oldY != y) {
                final int range = getScrollRange();
                final int overscrollMode = getOverScrollMode();
                final boolean canOverscroll = overscrollMode == OVER_SCROLL_ALWAYS
                        || (overscrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && range > 0);

                overScrollBy(x - oldX, y - oldY, oldX, oldY, range, 0,
                        mOverflingDistance, 0, false);
                onScrollChanged(getScrollX(), getScrollY(), oldX, oldY);

            }

            if (!awakenScrollBars()) {
                postInvalidateOnAnimation();
            }
        } else {
            int scrollX = getScrollX();
            int finalX = getScrollXFromFinalX(scrollX);

            if (!mIsBeingDragged && scrollX != finalX) {
                if (mScroller.springBack(getScrollX(), getScrollY(), finalX, finalX, 0, 0)) {
                    postInvalidateOnAnimation();
                }
            }               
        }
    }

    public void fling(int velocityX) {
        if (getChildCount() > 0) {
            int width = getWidth() - getPaddingRight() - getPaddingLeft();
            int right = getChildAt(0).getWidth();

            if(DEBUG){
                Log.d(TAG, "fling -> velocityX = " + velocityX);    
                Log.d(TAG, "fling -> getScrollX() = " + getScrollX()); 
            }
            mScroller.fling(getScrollX(), getScrollY(), velocityX, 0, 0,

                    Math.max(0, right - width), 0, 0, width / 2, 0);

            int finalX = mScroller.getFinalX();
            int overX = 0;
            if (finalX <= 0 || finalX >= scrollPositionOfMostRecent()) {
                overX = width/2;
            }

            finalX = getScrollXFromFinalX(finalX);
            if(DEBUG){  
                Log.d(TAG, "fling -> finalX = " + finalX); 
            }
            mScroller.startScroll(getScrollX(), getScrollY(), finalX - getScrollX(), 0,
                        getScrollTime(finalX));             
            final boolean movingRight = velocityX > 0;

            postInvalidateOnAnimation();
        }
    }
    
    private int getScrollTime(int finalX) {
        return Math.max(300, (finalX - getScrollX()) * 300/(int)(1.5 * mCellWidth));
    }
    
    public int getScrollXFromFinalX(int x) {
        if (x <= 0) {
            return 0;
        }
        
        if (x >= (mLinearLayout.getWidth() - getWidth())) {
            return mLinearLayout.getWidth() - getWidth();
        }
        
        return (int) ((float) x / mCellWidth + 0.5) * mCellWidth;
    }
    
    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }
    
    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    public void clearRecentApps() {
		int delay = 0;
		for (int i = mLinearLayout.getChildCount() - 1; i >=0 ; i--) {
            final View item = mLinearLayout.getChildAt(i);
            if (item != null && item.getVisibility() == View.VISIBLE && canChildBeCleared(item)) {
                delay += 100;
                mHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        mSwipeHelper.dismissChild(item);
                    }
                }, delay);
            }
        }
		
        delay += 300;
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                
                for (int i = mLinearLayout.getChildCount() - 1; i >= 0; i--) {
                    final View view = mLinearLayout.getChildAt(i);
                    if (view != null && canChildBeCleared(view)) {
                        removeChild(view);
                    }
                }
                
                mCallback.removeTask();
            }
        }, delay);
	}

	private boolean canChildBeCleared(View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        if ((holder != null && holder.isLockApp)) {
            return false;
        } else {
            return true;
        }
    }
}
