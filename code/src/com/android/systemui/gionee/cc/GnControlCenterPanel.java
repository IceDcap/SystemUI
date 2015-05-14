package com.android.systemui.gionee.cc;
/*
*
* MODULE DESCRIPTION
*   GnControlCenterView 
* add by huangwt for Android L at 20141210.
* 
*/

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.android.systemui.R;
import com.android.systemui.gionee.GnBlurHelper;
import com.android.systemui.screenshot.GnSnapshotService;

public class GnControlCenterPanel extends ViewGroup implements GestureDetector.OnGestureListener{
    
    private static final boolean DEBUG = GnControlCenter.DEBUG;
    private static final String TAG = "GnControlCenterPanel";

    public static final int ORIENTATION_HORIZONTAL = 0;
    public static final int ORIENTATION_VERTICAL = 1;

    private static final int TAP_THRESHOLD = 6;
    private static final float MAXIMUM_TAP_VELOCITY = 50.0f;
    private static final float MAXIMUM_MINOR_VELOCITY = 225.0f;
    private static final float MAXIMUM_MAJOR_VELOCITY = 300.0f;
    private static final float MAXIMUM_ACCELERATION = 5000.0f;
    private static final int VELOCITY_UNITS = 500;
    private static final int MSG_ANIMATE = 1000;
    private static final int MSG_SHAKE_ANIMATE = 1001;
    private static final int MSG_INVALIDATE = 1002;
    private static final int ANIMATION_FRAME_DURATION = 1000 / 60;

    private static final int EXPANDED_FULL_OPEN = -10001;
    static final int COLLAPSED_FULL_CLOSED = -10002;

    private Context mContext;
    private GnControlCenterView mGnControlCenterView;
    
    private View mHandle;
    private View mContent;

    private final Rect mFrame = new Rect();
    private final Rect mInvalidate = new Rect();
    private boolean mTracking;
    private boolean mLocked;

    private VelocityTracker mVelocityTracker;
    
    private GestureDetector mDetector;

    private boolean mFling;
    private boolean mExpanded;
    private int mBottomOffset;
    private int mTopOffset;
    private int mHandleHeight;
    private int mCCHeight;
    private int mHeight;

    private OnDrawerOpenListener mOnDrawerOpenListener;
    private OnDrawerCloseListener mOnDrawerCloseListener;
    private OnDrawerScrollListener mOnDrawerScrollListener;

    private final Handler mHandler = new SlidingHandler();
    private float mAnimatedAcceleration;
    private float mAnimatedVelocity;
    private float mAnimatedVelocityRatio;
    private float mAnimationPosition;
    private long mAnimationLastTime;
    private long mCurrentAnimationTime;
    private int mTouchDelta;
    private boolean mAnimating;
    private boolean mAllowSingleTap;
    private boolean mAnimateOnClick;

    private final int mTapThreshold;
    private final int mMaximumTapVelocity;
    private final int mMaximumMinorVelocity;
    private final int mMaximumMajorVelocity;
    private final int mMaximumAcceleration;
    private final int mVelocityUnits;

    private float mPonterDown_X = 0;
    private float mPonterDown_Y = 0;
    private boolean mNeedSwip = false;
    private int mShakeDeltaIndex = 0;
    private static final int[] sShakeDelta = {
        -15, -15, -10, -10, -10, -10, -10, -10, 10, 10, 10, 10, 10, 10, 10, 5, 5, 5, 5, 5, 5,
        5, -5, -5, -2, -2, -2, -2, -2
    };

    /**
     * Callback invoked when the drawer is opened.
     */
    public static interface OnDrawerOpenListener {
        /**
         * Invoked when the drawer becomes fully open.
         */
        public void onDrawerOpened();
    }

    /**
     * Callback invoked when the drawer is closed.
     */
    public static interface OnDrawerCloseListener {
        /**
         * Invoked when the drawer becomes fully closed.
         */
        public void onDrawerClosed();
    }

    /**
     * Callback invoked when the drawer is scrolled.
     */
    public static interface OnDrawerScrollListener {
        /**
         * Invoked when the user starts dragging/flinging the drawer's handle.
         */
        public void onScrollStarted();

        /**
         * Invoked when the user stops dragging/flinging the drawer's handle.
         */
        public void onScrollEnded();
    }

    public GnControlCenterPanel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GnControlCenterPanel(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public GnControlCenterPanel(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        
        mContext = context;
        
        boolean hasNavigationBar = mContext.getResources().getBoolean(com.android.internal.R.bool.config_showNavigationBar);
        int navigationBarH = mContext.getResources().getDimensionPixelSize(com.android.internal.R.dimen.navigation_bar_height);
        int heightPixels = getResources().getDisplayMetrics().heightPixels;
        if (hasNavigationBar) {            
            mHeight = navigationBarH + heightPixels;
        } else {
            mHeight = heightPixels;
        }
        mCCHeight = mContext.getResources().getDimensionPixelSize(R.dimen.gn_cc_height);
        mTopOffset = mHeight - mCCHeight;
        mBottomOffset = 0;
        mAllowSingleTap = true;
        mAnimateOnClick = true;

        final float density = getResources().getDisplayMetrics().density;
        mTapThreshold = (int) (TAP_THRESHOLD * density + 0.5f);
        mMaximumTapVelocity = (int) (MAXIMUM_TAP_VELOCITY * density + 0.5f);
        mMaximumMinorVelocity = (int) (MAXIMUM_MINOR_VELOCITY * density + 0.5f);
        mMaximumMajorVelocity = (int) (MAXIMUM_MAJOR_VELOCITY * density + 0.5f);
        mMaximumAcceleration = (int) (MAXIMUM_ACCELERATION * density + 0.5f);
        mVelocityUnits = (int) (VELOCITY_UNITS * density + 0.5f);

        setAlwaysDrawnWithCacheEnabled(false);
        
        mDetector = new GestureDetector(context, this);
        GnBlurHelper.addCallbacks(mBlurCallback);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mTopOffset = 0;
        } else {
            mTopOffset = mHeight - mCCHeight;
        }
    }

    @Override
    protected void onFinishInflate() {
        mHandle = findViewById(R.id.handle);
        if (mHandle == null) {
            throw new IllegalArgumentException("The handle attribute is must refer to an"
                    + " existing child.");
        }
        mHandle.setOnClickListener(new DrawerToggler());

        mContent = findViewById(R.id.content);
        if (mContent == null) {
            throw new IllegalArgumentException("The content attribute is must refer to an" 
                    + " existing child.");
        }
        mContent.setVisibility(View.GONE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthSpecMode == MeasureSpec.UNSPECIFIED || heightSpecMode == MeasureSpec.UNSPECIFIED) {
            throw new RuntimeException("SlidingDrawer cannot have UNSPECIFIED dimensions");
        }

        final View handle = mHandle;
        measureChild(handle, widthMeasureSpec, heightMeasureSpec);

        int height = heightSpecSize - handle.getMeasuredHeight() - mTopOffset;
        mContent.measure(MeasureSpec.makeMeasureSpec(widthSpecSize, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));

        setMeasuredDimension(widthSpecSize, heightSpecSize);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        
        final long drawingTime = getDrawingTime();
        final View handle = mHandle;

        // draw blur
        if (mGnControlCenterView.isHighDevice()) {
            synchronized (GnBlurHelper.LOCK) {
                if (GnBlurHelper.mBlur != null && !GnBlurHelper.mBlur.isRecycled()) {
                    if (false) Log.d(TAG, "GnControlCenterView draw blur");
                    Rect src = new Rect(0, handle.getTop(), GnBlurHelper.mBlur.getWidth(), GnBlurHelper.mBlur.getHeight());
                    Rect dst = new Rect(0, handle.getTop(), GnBlurHelper.mBlur.getWidth(), mCCHeight + handle.getTop() + 90);
                    canvas.drawBitmap(GnBlurHelper.mBlur, src, dst, null);
                }
            }
        }

        // draw black
        Paint paint = new Paint();
        paint.setColor(getColor(handle.getTop()));
        paint.setStyle(Style.FILL);
        canvas.drawRect(new Rect(0, handle.getTop(), mRight, mBottom), paint);

        // draw handle
        drawChild(canvas, handle, drawingTime);

        // draw content
        if (mTracking || mAnimating) {
            final Bitmap cache = mContent.getDrawingCache();
            if (cache != null) {
                canvas.drawBitmap(cache, 0, handle.getBottom(), null);
            } else {
                canvas.save();
                canvas.translate(0, handle.getTop() - mTopOffset);
                drawChild(canvas, mContent, drawingTime);
                canvas.restore();
            }
        } else if (mExpanded) {
            drawChild(canvas, mContent, drawingTime);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mTracking) {
            return;
        }

        final int width = r - l;
        final int height = b - t;

        final View handle = mHandle;

        int childWidth = handle.getMeasuredWidth();
        int childHeight = handle.getMeasuredHeight();

        int childLeft;
        int childTop;

        final View content = mContent;

        childLeft = (width - childWidth) / 2;
        childTop = mExpanded ? mTopOffset : height - childHeight + mBottomOffset;

        content.layout(0, mTopOffset + childHeight, content.getMeasuredWidth(), mTopOffset
                + childHeight + content.getMeasuredHeight());

        handle.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
        mHandleHeight = handle.getHeight();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mLocked) {
            return false;
        }
        
        if (!mExpanded) {
            return true;
        }
        
        if (mAnimating) {
            Log.d(TAG, "mAnimating return");
            return false;
        }

        final int action = event.getAction();

        float x = event.getX();
        float y = event.getY();

        final Rect frame = mFrame;
        final View handle = mHandle;

        handle.getHitRect(frame);
        if (!mTracking && y > frame.bottom) {
            return false;
        }

        if (action == MotionEvent.ACTION_DOWN) {

            mPonterDown_X = event.getX();
            mPonterDown_Y = event.getY();
            
            if (y > frame.top) {
                mNeedSwip = false;
            } else {
                mNeedSwip = true;
            }

            handle.setPressed(true);
            // Must be called before prepareTracking()
            prepareContent();

            // Must be called after prepareContent()
            if (mOnDrawerScrollListener != null) {
                mOnDrawerScrollListener.onScrollStarted();
            }

            final int top = mHandle.getTop();
            mTouchDelta = (int) y - top;
            prepareTracking(top);

            mVelocityTracker.addMovement(event);
        }

        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mLocked) {
            return true;
        }
        
        if (!mExpanded) {
            return true;
        }

        mDetector.onTouchEvent(event);
        
        final int action = event.getAction();
        if (mTracking) {
            mVelocityTracker.addMovement(event);
            switch (action) {
                case MotionEvent.ACTION_MOVE:
                    if (mNeedSwip) {
                        swiping(event);
                    } else {
                        moveHandle((int) (event.getY()) - mTouchDelta);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (isClickEvent(event)) {
                        animateClose();
                    } else {
                        goFling();
                    }
                    break;
                default:
                    break;
            }
        }

        // return mTracking || mAnimating || super.onTouchEvent(event);
        super.onTouchEvent(event);
        return true;
    }

    private boolean isClickEvent(MotionEvent event) {
        return Math.abs(mPonterDown_X - event.getX()) <= 5 || Math.abs(mPonterDown_Y - event.getY()) <= 5;
    }

    private void goFling() {
        final VelocityTracker velocityTracker = mVelocityTracker;
        velocityTracker.computeCurrentVelocity(mVelocityUnits);

        float yVelocity = velocityTracker.getYVelocity();
        float xVelocity = velocityTracker.getXVelocity();
        boolean negative;

        negative = yVelocity < 0;
        if (xVelocity < 0) {
            xVelocity = -xVelocity;
        }
        if (xVelocity > mMaximumMinorVelocity) {
            xVelocity = mMaximumMinorVelocity;
        }

        float velocity = (float) Math.hypot(xVelocity, yVelocity);
        if (negative) {
            velocity = -velocity;
        }

        final int top = mHandle.getTop();

        if (Math.abs(velocity) < mMaximumTapVelocity) {
            if ((mExpanded && top < mTapThreshold + mTopOffset)
                    || (!mExpanded && top > mBottomOffset + mBottom - mTop - mHandleHeight
                            - mTapThreshold)) {

                if (mAllowSingleTap) {
                    playSoundEffect(SoundEffectConstants.CLICK);

                    if (mExpanded) {
                        animateClose(top);
                    } else {
                        animateOpen(top);
                    }
                } else {
                    performFling(top, velocity, false);
                }

            } else {
                performFling(top, velocity, false);
            }
        } else {
            performFling(top, velocity, false);
        }
    }

    private void animateClose(int position) {
        prepareTracking(position);
        performFling(position, mMaximumMajorVelocity, true);
    }

    public void animateOpen(int position) {
        prepareTracking(position);
        performFling(position, -mMaximumMajorVelocity, true);
    }

    private void performFling(int position, float velocity, boolean always) {
        mAnimationPosition = position;
        mAnimatedVelocity = velocity;

        if (mExpanded) {
            if (always || (velocity > mMaximumMajorVelocity ||
                    (position > mTopOffset + mHandleHeight &&
                            velocity > -mMaximumMajorVelocity))) {
                // We are expanded, but they didn't move sufficiently to cause
                // us to retract.  Animate back to the expanded position.
                mAnimatedAcceleration = mMaximumAcceleration;
                if (velocity < 0) {
                    mAnimatedVelocity = 0;
                }
            } else {
                // We are expanded and are now going to animate away.
                mAnimatedAcceleration = -mMaximumAcceleration;
                if (velocity > 0) {
                    mAnimatedVelocity = 0;
                }
            }
        } else {
            if (!always && (velocity > mMaximumMajorVelocity ||
                    (position > getHeight() / 1.2 &&
                            velocity > -mMaximumMajorVelocity))) {
                // We are collapsed, and they moved enough to allow us to expand.
                mAnimatedAcceleration = mMaximumAcceleration;
                if (velocity < 0) {
                    mAnimatedVelocity = 0;
                }
            } else {
                // We are collapsed, but they didn't move sufficiently to cause
                // us to retract.  Animate back to the collapsed position.
                mAnimatedAcceleration = -mMaximumAcceleration;
                if (velocity > 0) {
                    mAnimatedVelocity = 0;
                }
            }
        }

        Log.d(TAG, "go Fling");
        mAnimatedVelocityRatio = mAnimatedVelocity / (float) mMaximumMajorVelocity / 15.0f;
        mAnimatedVelocityRatio = Math.abs(mAnimatedVelocityRatio);
        Log.d(TAG, " mAnimatedVelocity = " + mAnimatedVelocity + "mAnimatedVelocityRatio = " + mAnimatedVelocityRatio);
        if (mAnimatedVelocityRatio - 1.0f > 0.0f) {
            mAnimatedVelocityRatio = 1.0f;
        }
        long now = SystemClock.uptimeMillis();
        mAnimationLastTime = now;
        mCurrentAnimationTime = now + ANIMATION_FRAME_DURATION;
        mAnimating = true;
        mHandler.removeMessages(MSG_ANIMATE);
        mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE), mCurrentAnimationTime);
        stopTracking();
    }

    private void prepareTracking(int position) {
        Log.d(TAG, "prepareTracking");
        mTracking = true;
        mVelocityTracker = VelocityTracker.obtain();
        boolean opening = !mExpanded;
        if (opening) {
            mAnimatedAcceleration = mMaximumAcceleration;
            mAnimatedVelocity = mMaximumMajorVelocity;
            mAnimationPosition = mBottomOffset + getHeight() - mHandleHeight;
            moveHandle((int) mAnimationPosition);
            mAnimating = true;
            mHandler.removeMessages(MSG_ANIMATE);
            long now = SystemClock.uptimeMillis();
            mAnimationLastTime = now;
            mCurrentAnimationTime = now + ANIMATION_FRAME_DURATION;
            mAnimating = true;
        } else {
            if (mAnimating) {
                mAnimating = false;
                mHandler.removeMessages(MSG_ANIMATE);
            }
            moveHandle(position);
        }
    }

    private void moveHandle(int position) {
        final View handle = mHandle;

        if (position == EXPANDED_FULL_OPEN) {
            handle.offsetTopAndBottom(mTopOffset - handle.getTop());
            invalidate();
            if (mGnControlCenterView.isShown()) {
                mGnControlCenterView.go(GnControlCenter.STATE_OPEN);
            }
        } else if (position == COLLAPSED_FULL_CLOSED) {
            handle.offsetTopAndBottom(mBottomOffset + mBottom - mTop - mHandleHeight
                    - handle.getTop());
            invalidate();
            mGnControlCenterView.setVisibility(View.GONE);
        } else {
            moving(position, handle);
        }
    }
    
    public void swiping(MotionEvent event) {

        if (mLocked) {
            Log.d(TAG, "swiping mLocked");
            return;
        }
        
        // if (mExpanded) {
        // return;
        // }

        if (!mTracking) {
            prepareMoving(event);
        }
        
        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                mVelocityTracker.addMovement(event);
                int position = (int) event.getY() - getTop();
                final View handle = mHandle;
                moving(position, handle);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                Log.d(TAG, "swiping " + event.getAction() + " " + event.getY());
                mFling = true;
                goFling();
                break;
            default:
                break;
        }
    }

    private void moving(int position, final View handle) {
        final int top = handle.getTop();
        int deltaY = position - top;
        if (position < mTopOffset) {
            deltaY = mTopOffset - top;
        } else if (deltaY > mBottomOffset + mBottom - mTop - top) {
            deltaY = mBottomOffset + mBottom - mTop - top;
        }
        
        if (DEBUG) Log.d(TAG, "moving  deltaY = " + deltaY);
        
        invalidateRegion(handle, deltaY);
    }

    private void shaking(final View handle) {
        int deltaY = (int) (sShakeDelta[mShakeDeltaIndex] * mAnimatedVelocityRatio);
        
        invalidateRegion(handle, deltaY);
    }

    private void invalidateRegion(final View handle, int deltaY) {
        handle.offsetTopAndBottom(deltaY);

        final Rect frame = mFrame;
        final Rect region = mInvalidate;

        handle.getHitRect(frame);
        region.set(frame);

        region.union(frame.left, frame.top - deltaY, frame.right, frame.bottom - deltaY);
        region.union(0, frame.bottom - deltaY, getWidth(), frame.bottom - deltaY
                + mContent.getHeight());

        invalidate(region);
    }

    public void prepareMoving(MotionEvent event) {
        final View handle = mHandle;

        handle.setPressed(true);
        // Must be called before prepareTracking()
        prepareContent();

        // Must be called after prepareContent()
        if (mOnDrawerScrollListener != null) {
            mOnDrawerScrollListener.onScrollStarted();
        }

        final int top = mHandle.getTop();
        prepareTracking(top);

        mVelocityTracker.addMovement(event);
    }

    private void prepareContent() {
        if (mAnimating) {
            return;
        }

        // Something changed in the content, we need to honor the layout request
        // before creating the cached bitmap
        final View content = mContent;
        if (content.isLayoutRequested()) {
            final int childHeight = mHandleHeight;
            int height = mBottom - mTop - childHeight - mTopOffset;
            content.measure(MeasureSpec.makeMeasureSpec(mRight - mLeft, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
            content.layout(0, mTopOffset + childHeight, content.getMeasuredWidth(), mTopOffset
                    + childHeight + content.getMeasuredHeight());
        }
        // Try only once... we should really loop but it's not a big deal
        // if the draw was cancelled, it will only be temporary anyway
        content.getViewTreeObserver().dispatchOnPreDraw();
        if (!content.isHardwareAccelerated()) content.buildDrawingCache();

        content.setVisibility(View.GONE);
    }

    private void stopTracking() {
        Log.d(TAG, "stopTracking");
        mHandle.setPressed(false);
        mTracking = false;

        if (mOnDrawerScrollListener != null) {
            mOnDrawerScrollListener.onScrollEnded();
        }

        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void doAnimation() {
        if (mAnimating) {
            incrementAnimation();
            if (mAnimationPosition >= mBottomOffset + getHeight() - 1) {
                Log.d(TAG, "doAnimation close");
                mAnimating = false;
                mFling = false;
                closeDrawer();
            } else if (mAnimationPosition < mTopOffset) {
                if (mAnimatedVelocityRatio - 0.3f < 0.0f) {
                    Log.d(TAG, "doAnimation open");
                    mAnimating = false;
                    openDrawer();
                } else {
                    Log.d(TAG, "doAnimation start ShakeAnimation");
                    moveHandle(EXPANDED_FULL_OPEN);
                    mCurrentAnimationTime += ANIMATION_FRAME_DURATION;
                    mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_SHAKE_ANIMATE),
                            mCurrentAnimationTime);
                }
            } else {
                moveHandle((int) mAnimationPosition);
                mCurrentAnimationTime += ANIMATION_FRAME_DURATION;
                mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE),
                        mCurrentAnimationTime);
            }
        }
    }

    private void doShakeAnimation() {
        if (mShakeDeltaIndex < 29) {
            shaking(mHandle);
            mCurrentAnimationTime += ANIMATION_FRAME_DURATION;
            mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_SHAKE_ANIMATE),
                    mCurrentAnimationTime);
            mShakeDeltaIndex++;
        } else {
            Log.d(TAG, "top = " + mHandle.getTop() + " mTopOffset = " + mTopOffset);
            if (mHandle.getTop() - 5 * mAnimatedVelocityRatio < mTopOffset) {
                mShakeDeltaIndex = 0;
                mAnimating = false;
                mFling = false;
                openDrawer();
            } else {
                invalidateRegion(mHandle, (int)(-5 * mAnimatedVelocityRatio));
                mCurrentAnimationTime += ANIMATION_FRAME_DURATION;
                mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_SHAKE_ANIMATE),
                        mCurrentAnimationTime);
            }
        }
    }

    private void incrementAnimation() {
        long now = SystemClock.uptimeMillis();
        float t = (now - mAnimationLastTime) / 1000.0f;                   // ms -> s
        final float position = mAnimationPosition;
        final float v = mAnimatedVelocity;                                // px/s
        final float a = mAnimatedAcceleration;                            // px/s/s
        mAnimationPosition = position + (v * t) + (0.5f * a * t * t);     // px
        mAnimatedVelocity = v + (a * t);                                  // px/s
        mAnimationLastTime = now;                                         // ms
    }

    public void toggle() {
        if (!mExpanded) {
            openDrawer();
        } else {
            closeDrawer();
        }
        invalidate();
        requestLayout();
    }

    public void animateToggle() {
        if (!mExpanded) {
            animateOpen();
        } else {
            animateClose();
        }
    }

    public void open() {
        openDrawer();
        invalidate();
        requestLayout();

        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
    }

    public void close() {
        closeDrawer();
        invalidate();
        requestLayout();
    }

    public void animateClose() {
        prepareContent();
        final OnDrawerScrollListener scrollListener = mOnDrawerScrollListener;
        if (scrollListener != null) {
            scrollListener.onScrollStarted();
        }
        animateClose(mHandle.getTop());

        if (scrollListener != null) {
            scrollListener.onScrollEnded();
        }
    }

    public void animateOpen() {
        prepareContent();
        final OnDrawerScrollListener scrollListener = mOnDrawerScrollListener;
        if (scrollListener != null) {
            scrollListener.onScrollStarted();
        }
        animateOpen(mHandle.getTop());

        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);

        if (scrollListener != null) {
            scrollListener.onScrollEnded();
        }
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(GnControlCenterPanel.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(GnControlCenterPanel.class.getName());
    }

    private void closeDrawer() {
        Log.d(TAG, "closeDrawer");
        moveHandle(COLLAPSED_FULL_CLOSED);
        mContent.setVisibility(View.GONE);
        mContent.destroyDrawingCache();

        if (!mExpanded) {
            return;
        }

        mExpanded = false;
        if (mOnDrawerCloseListener != null) {
            mOnDrawerCloseListener.onDrawerClosed();
        }
    }

    private void openDrawer() {
        Log.d(TAG, "openDrawer");
        moveHandle(EXPANDED_FULL_OPEN);
        mContent.setVisibility(View.VISIBLE);

        if (mExpanded) {
            return;
        }

        mExpanded = true;

        if (mOnDrawerOpenListener != null) {
            mOnDrawerOpenListener.onDrawerOpened();
        }
    }

    /**
     * Sets the listener that receives a notification when the drawer becomes open.
     *
     * @param onDrawerOpenListener The listener to be notified when the drawer is opened.
     */
    public void setOnDrawerOpenListener(OnDrawerOpenListener onDrawerOpenListener) {
        mOnDrawerOpenListener = onDrawerOpenListener;
    }

    /**
     * Sets the listener that receives a notification when the drawer becomes close.
     *
     * @param onDrawerCloseListener The listener to be notified when the drawer is closed.
     */
    public void setOnDrawerCloseListener(OnDrawerCloseListener onDrawerCloseListener) {
        mOnDrawerCloseListener = onDrawerCloseListener;
    }

    /**
     * Sets the listener that receives a notification when the drawer starts or ends
     * a scroll. A fling is considered as a scroll. A fling will also trigger a
     * drawer opened or drawer closed event.
     *
     * @param onDrawerScrollListener The listener to be notified when scrolling
     *        starts or stops.
     */
    public void setOnDrawerScrollListener(OnDrawerScrollListener onDrawerScrollListener) {
        mOnDrawerScrollListener = onDrawerScrollListener;
    }

    public View getHandle() {
        return mHandle;
    }

    public View getContent() {
        return mContent;
    }

    public void unlock() {
        mLocked = false;
    }

    public void lock() {
        mLocked = true;
    }

    public boolean isOpened() {
        return mExpanded;
    }

    public boolean isMoving() {
        return mTracking || mAnimating;
    }
    
    public boolean isFling() {
        return mFling;
    }

    private class DrawerToggler implements OnClickListener {
        public void onClick(View v) {
            if (mLocked) {
                return;
            }
            // mAllowSingleTap isn't relevant here; you're *always*
            // allowed to open/close the drawer by clicking with the
            // trackball.

            if (mAnimateOnClick) {
                animateToggle();
            } else {
                toggle();
            }
        }
    }

    private class SlidingHandler extends Handler {
        public void handleMessage(Message m) {
            switch (m.what) {
                case MSG_ANIMATE:
                    doAnimation();
                    break;
                case MSG_SHAKE_ANIMATE:
                    doShakeAnimation();
                    break;
                case MSG_INVALIDATE:
                    invalidate();
                    break;
                default:
                    break;
            }
        }
    }

    public void setControlCenterView(GnControlCenterView view) {
        mGnControlCenterView = view;
    }
    
    private int getColor(int position) {
        /*int mScrimColor = 0xBF131313;
        float frac = ((float)(getBottom() - position - getTop())) / ((float)(getBottom() - getTop()));
        
        if (DEBUG) Log.d(TAG, "frac = " + frac);
        
        final float k = (float)(1f-0.5f*(1f-Math.cos(3.14159f * Math.pow(1f-frac, 2f))));
        // attenuate background color alpha by k
        final int color = (int) ((mScrimColor >>> 24) * k) << 24 | (mScrimColor & 0xFFFFFF);
        
        if (DEBUG) Log.d(TAG, "color = " + color);
        return color;*/
        
        int scrimColor;
        if (mGnControlCenterView.isHighDevice()) {
            scrimColor = 0xBF131313;
        } else {
            scrimColor = 0xFA222222;            
        }
        return scrimColor;
    }

    public void createBlurBg(Context context) {
    	GnBlurHelper.getBlurHelper().createBlurBg(context);
    }
    
    private void releaseBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (velocityY > 0) {
            animateClose();
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }
    
    private GnBlurHelper.Callback mBlurCallback = new GnBlurHelper.Callback() {
        
        @Override
        public void completeBlur() {
            Log.d(TAG, "completeBlur");
            mHandler.sendEmptyMessage(MSG_INVALIDATE);
        }
    };
}
