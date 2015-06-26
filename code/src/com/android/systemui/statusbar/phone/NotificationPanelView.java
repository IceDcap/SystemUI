/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.MathUtils;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.keyguard.KeyguardStatusView;
import com.android.systemui.EventLogTags;
import com.android.systemui.EventLogConstants;
import com.android.systemui.R;
import com.android.systemui.gionee.GnBlurHelper;
import com.android.systemui.gionee.nc.ui.GnNotficationHandlerView;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.statusbar.ExpandableView;
import com.android.systemui.statusbar.FlingAnimationUtils;
import com.android.systemui.statusbar.GestureRecorder;
import com.android.systemui.statusbar.KeyguardAffordanceView;
import com.android.systemui.statusbar.StatusBarState;
import com.android.systemui.statusbar.policy.KeyguardUserSwitcher;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;
import com.android.systemui.statusbar.stack.StackStateAnimator;

public class NotificationPanelView extends PanelView implements
        ExpandableView.OnHeightChangedListener, ObservableScrollView.Listener,
        View.OnClickListener, NotificationStackScrollLayout.OnOverscrollTopChangedListener,
        NotificationStackScrollLayout.OnEmptySpaceClickListener {

    private static final boolean DEBUG = false;

    // Cap and total height of Roboto font. Needs to be adjusted when font for the big clock is
    // changed.
    private static final int CAP_HEIGHT = 1456;
    private static final int FONT_HEIGHT = 2163;

    private static final float HEADER_RUBBERBAND_FACTOR = 2.05f;
    private static final float LOCK_ICON_ACTIVE_SCALE = 1.2f;

    public static final long DOZE_ANIMATION_DURATION = 700;
    private GnNotficationHandlerView mNotficationHandlerView;
    private StatusBarHeaderView mHeader;
    private KeyguardUserSwitcher mKeyguardUserSwitcher;
//    private KeyguardStatusBarView mKeyguardStatusBar;
//    private KeyguardStatusView mKeyguardStatusView;
    private ObservableScrollView mScrollView;
    private TextView mClockView;
    private NotificationStackScrollLayout mNotificationStackScroller;
    private int mNotificationTopPadding;
    private boolean mAnimateNextTopPaddingChange;

    private int mTrackingPointer;
    private VelocityTracker mVelocityTracker;
    private boolean mQsTracking;

    /**
     * Handles launching the secure camera properly even when other applications may be using the
     * camera hardware.
     */

    /**
     * If set, the ongoing touch gesture might both trigger the expansion in {@link PanelView} and
     * the expansion for quick settings.
     */
    private boolean mConflictingQsExpansionGesture;

    /**
     * Whether we are currently handling a motion gesture in #onInterceptTouchEvent, but haven't
     * intercepted yet.
     */
    private boolean mIntercepting;
    private boolean mQsExpandedWhenExpandingStarted;
    private boolean mQsFullyExpanded;
    private boolean mKeyguardShowing;
    private boolean mDozing;
    private int mStatusBarState;
    private float mInitialHeightOnTouch;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private float mLastTouchX;
    private float mLastTouchY;
    private float mQsExpansionHeight;
    private int mQsMinExpansionHeight;
    private int mQsMaxExpansionHeight;
    private int mQsPeekHeight;
    private boolean mStackScrollerOverscrolling;
    private boolean mQsExpansionFromOverscroll;
    private float mLastOverscroll;
    private FlingAnimationUtils mFlingAnimationUtils;
    private int mStatusBarMinHeight;
    private boolean mUnlockIconActive;
    private int mNotificationsHeaderCollideDistance;
    private int mUnlockMoveDistance;
    private float mEmptyDragAmount;

    private Interpolator mFastOutSlowInInterpolator;
    private Interpolator mFastOutLinearInterpolator;
    private Interpolator mDozeAnimationInterpolator;
    private ObjectAnimator mClockAnimator;
    private int mClockAnimationTarget = -1;
    private int mTopPaddingAdjustment;
    private KeyguardClockPositionAlgorithm mClockPositionAlgorithm =
            new KeyguardClockPositionAlgorithm();
    private KeyguardClockPositionAlgorithm.Result mClockPositionResult =
            new KeyguardClockPositionAlgorithm.Result();
    private boolean mIsExpanding;

    private boolean mBlockTouches;
    private int mNotificationScrimWaitDistance;

    /**
     * If we are in a panel collapsing motion, we reset scrollY of our scroll view but still
     * need to take this into account in our panel height calculation.
     */
    private int mScrollYOverride = -1;
    private boolean mIsLaunchTransitionFinished;
    private boolean mIsLaunchTransitionRunning;
    private Runnable mLaunchAnimationEndRunnable;
//    private boolean mOnlyAffordanceInThisMotion;
//    private boolean mKeyguardStatusViewAnimating;
    private boolean mHeaderAnimatingIn;
    private boolean mShadeEmpty;

    private boolean mQsScrimEnabled = true;
    private boolean mLastAnnouncementWasQuickSettings;
    private boolean mQsTouchAboveFalsingThreshold;
    private int mQsFalsingThreshold;

    private float mKeyguardStatusBarAnimateAlpha = 1f;
    private int mOldLayoutDirection;
    private boolean mOnTouching = false;
    private float mTouchingOffset = 0f;
    private float mTouchingY = 0f;
    private int mNotificationHandlerHeight = 0;
    //private int mNavigationBarHeight = 0;
    private int mCarrierLabelHeight = 0;
    // GIONEE <wujj> <2015-06-03> add for CR01494595 begin
    // The panel height is not adapt to screen height when first expanding notification panel
    // after boot up. However, there is no QS panel in notification panel view in Amigo style UI.
    // So we can use a fixed height for Notification panel view.
    private int mScrollPadding = 0;
    private int mMaxPanelHeight = 0;
    // GIONEE <wujj> <2015-06-03> add for CR01494595 end

    public NotificationPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(!DEBUG);
    }

    public void setStatusBar(PhoneStatusBar bar) {
        mStatusBar = bar;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHeader = (StatusBarHeaderView) findViewById(R.id.header);
        mHeader.setOnClickListener(this);
//        mKeyguardStatusBar = (KeyguardStatusBarView) findViewById(R.id.keyguard_header);
//        mKeyguardStatusView = (KeyguardStatusView) findViewById(R.id.keyguard_status_view);
        mClockView = (TextView) findViewById(R.id.clock_view);
        mScrollView = (ObservableScrollView) findViewById(R.id.scroll_view);
        mScrollView.setListener(this);
        mScrollView.setFocusable(false);
        mNotificationStackScroller = (NotificationStackScrollLayout)
                findViewById(R.id.notification_stack_scroller);
        mNotificationStackScroller.setOnHeightChangedListener(this);
        mNotificationStackScroller.setOverscrollTopChangedListener(this);
        mNotificationStackScroller.setOnEmptySpaceClickListener(this);
        mNotificationStackScroller.setScrollView(mScrollView);
        
        mNotficationHandlerView = (GnNotficationHandlerView)findViewById(R.id.notification_handler);
        
        mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(getContext(),
                android.R.interpolator.fast_out_slow_in);
        mFastOutLinearInterpolator = AnimationUtils.loadInterpolator(getContext(),
                android.R.interpolator.fast_out_linear_in);
        mDozeAnimationInterpolator = AnimationUtils.loadInterpolator(getContext(),
                android.R.interpolator.linear_out_slow_in);
    }

    @Override
    protected void loadDimens() {
        super.loadDimens();
        Resources res = getResources();
        mNotificationTopPadding = res.getDimensionPixelSize(R.dimen.notifications_top_padding);
        mFlingAnimationUtils = new FlingAnimationUtils(getContext(), 0.4f);
        mStatusBarMinHeight = res.getDimensionPixelSize(com.android.internal.R.dimen.status_bar_height);
        mQsPeekHeight = res.getDimensionPixelSize(R.dimen.qs_peek_height);
        mNotificationsHeaderCollideDistance =
                res.getDimensionPixelSize(R.dimen.header_notifications_collide_distance);
        mUnlockMoveDistance = res.getDimensionPixelOffset(R.dimen.unlock_move_distance);
        mClockPositionAlgorithm.loadDimens(res);
        mNotificationScrimWaitDistance =
                res.getDimensionPixelSize(R.dimen.notification_scrim_wait_distance);
        mQsFalsingThreshold = res.getDimensionPixelSize(R.dimen.qs_falsing_threshold);
        
        mCarrierLabelHeight = res.getDimensionPixelSize(R.dimen.carrier_label_height);
        mNotificationHandlerHeight = res.getDimensionPixelSize(R.dimen.gn_notification_drawner_height);
        //mNavigationBarHeight = res.getDimensionPixelSize(com.android.internal.R.dimen.navigation_bar_height);
        
        // GIONEE <wujj> <2015-06-03> add for CR01494595 begin
        mScrollPadding = res.getDimensionPixelSize(R.dimen.close_handle_height);
        int screenHeight = res.getDisplayMetrics().heightPixels;
        mMaxPanelHeight = screenHeight - mScrollPadding;
        Log.v(TAG, "loadDimens: mMaxPanelHeight = "+mMaxPanelHeight);
        // GIONEE <wujj> <2015-06-03> add for CR01494595 end
    }

    public void updateResources() {
        int panelWidth = getResources().getDimensionPixelSize(R.dimen.notification_panel_width);
        int panelGravity = getResources().getInteger(R.integer.notification_panel_layout_gravity);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mHeader.getLayoutParams();
        if (lp.width != panelWidth) {
            lp.width = panelWidth;
            lp.gravity = panelGravity;
            mHeader.setLayoutParams(lp);
            mHeader.post(mUpdateHeader);
        }

        lp = (FrameLayout.LayoutParams) mNotificationStackScroller.getLayoutParams();
        if (lp.width != panelWidth) {
            lp.width = panelWidth;
            lp.gravity = panelGravity;
            mNotificationStackScroller.setLayoutParams(lp);
        }

        lp = (FrameLayout.LayoutParams) mScrollView.getLayoutParams();
        if (lp.width != panelWidth) {
            lp.width = panelWidth;
            lp.gravity = panelGravity;
            mScrollView.setLayoutParams(lp);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // Update Clock Pivot
//        mKeyguardStatusView.setPivotX(getWidth() / 2);
//        mKeyguardStatusView.setPivotY((FONT_HEIGHT - CAP_HEIGHT) / 2048f * mClockView.getTextSize());

        // Calculate quick setting heights.
        int oldMaxHeight = mQsMaxExpansionHeight;
        mQsMinExpansionHeight = mKeyguardShowing ? 0 : mHeader.getCollapsedHeight() + mQsPeekHeight;
        mQsMaxExpansionHeight = mHeader.getExpandedHeight();
        positionClockAndNotifications();
		setQsExpansion(mQsMinExpansionHeight + mLastOverscroll);
		// mNotificationStackScroller.setStackHeight(getExpandedHeight());
		mNotificationStackScroller.setStackHeight(getContentHeight() - mNotificationHandlerHeight);
        updateHeader();
        updateNotificationHandler(getContentHeight());
        mNotificationStackScroller.updateIsSmallScreen(
                mHeader.getCollapsedHeight() + mQsPeekHeight);
    }

    @Override
    public void onAttachedToWindow() {
    }

    @Override
    public void onDetachedFromWindow() {
    }

    /**
     * Positions the clock and notifications dynamically depending on how many notifications are
     * showing.
     */
    private void positionClockAndNotifications() {
        boolean animate = mNotificationStackScroller.isAddOrRemoveAnimationPending();
        int stackScrollerPadding;
        //jiating modify for keyguard begin
//        if (mStatusBarState != StatusBarState.KEYGUARD) {
            int bottom = mHeader.getCollapsedHeight();
            stackScrollerPadding =/* mStatusBarState == StatusBarState.SHADE
                    ? */bottom + mQsPeekHeight + mNotificationTopPadding;
                   // : mKeyguardStatusBar.getHeight() + mNotificationTopPadding;
            mTopPaddingAdjustment = 0;
       /* } else {
            mClockPositionAlgorithm.setup(
                    mStatusBar.getMaxKeyguardNotifications(),
                    getMaxPanelHeight(),
                    getExpandedHeight(),
                    mNotificationStackScroller.getNotGoneChildCount(),
                    getHeight(),
                    mKeyguardStatusView.getHeight(),
                    mEmptyDragAmount);
            mClockPositionAlgorithm.run(mClockPositionResult);
            if (animate || mClockAnimator != null) {
                startClockAnimation(mClockPositionResult.clockY);
            } else {
                mKeyguardStatusView.setY(mClockPositionResult.clockY);
            }
            updateClock(mClockPositionResult.clockAlpha, mClockPositionResult.clockScale);
            stackScrollerPadding = mClockPositionResult.stackScrollerPadding;
            mTopPaddingAdjustment = mClockPositionResult.stackScrollerPaddingAdjustment;
        }*/
            //jiating modify for keyguard end
        mNotificationStackScroller.setIntrinsicPadding(stackScrollerPadding);
        requestScrollerTopPaddingUpdate(animate);
    }

//    private void startClockAnimation(int y) {
//        if (mClockAnimationTarget == y) {
//            return;
//        }
//        mClockAnimationTarget = y;
//        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
//            @Override
//            public boolean onPreDraw() {
//                getViewTreeObserver().removeOnPreDrawListener(this);
//                if (mClockAnimator != null) {
//                    mClockAnimator.removeAllListeners();
//                    mClockAnimator.cancel();
//                }
//                mClockAnimator = ObjectAnimator
//                        .ofFloat(mKeyguardStatusView, View.Y, mClockAnimationTarget);
//                mClockAnimator.setInterpolator(mFastOutSlowInInterpolator);
//                mClockAnimator.setDuration(StackStateAnimator.ANIMATION_DURATION_STANDARD);
//                mClockAnimator.addListener(new AnimatorListenerAdapter() {
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        mClockAnimator = null;
//                        mClockAnimationTarget = -1;
//                    }
//                });
//                mClockAnimator.start();
//                return true;
//            }
//        });
//    }

//    private void updateClock(float alpha, float scale) {
//        if (!mKeyguardStatusViewAnimating) {
//            mKeyguardStatusView.setAlpha(alpha);
//        }
//        mKeyguardStatusView.setScaleX(scale);
//        mKeyguardStatusView.setScaleY(scale);
//    }

    public void animateToFullShade(long delay) {
        mAnimateNextTopPaddingChange = true;
        mNotificationStackScroller.goToFullShade(delay);
        requestLayout();
    }

    public void setQsExpansionEnabled(boolean qsExpansionEnabled) {
        mHeader.setClickable(qsExpansionEnabled);
    }

    @Override
    public void resetViews() {
        mIsLaunchTransitionFinished = false;
        mBlockTouches = false;
        mUnlockIconActive = false;
        mStatusBar.dismissPopups();
        mNotificationStackScroller.setOverScrollAmount(0f, true /* onTop */, false /* animate */,
                true /* cancelAnimators */);
    }

    @Override
    public void fling(float vel, boolean expand) {
        GestureRecorder gr = ((PhoneStatusBarView) mBar).mBar.getGestureRecorder();
        if (gr != null) {
            gr.tag("fling " + ((vel > 0) ? "open" : "closed"), "notifications,v=" + vel);
        }
        super.fling(vel, expand);
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            event.getText().add(getKeyguardOrLockScreenString());
            mLastAnnouncementWasQuickSettings = false;
            return true;
        }

        return super.dispatchPopulateAccessibilityEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mBlockTouches) {
            return false;
        }
        resetDownStates(event);
        int pointerIndex = event.findPointerIndex(mTrackingPointer);
        if (pointerIndex < 0) {
            pointerIndex = 0;
            mTrackingPointer = event.getPointerId(pointerIndex);
        }
        final float x = event.getX(pointerIndex);
        final float y = event.getY(pointerIndex);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mIntercepting = true;
                mInitialTouchY = y;
                mInitialTouchX = x;
                initVelocityTracker();
                trackMovement(event);
                if (shouldQuickSettingsIntercept(mInitialTouchX, mInitialTouchY, 0)) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                final int upPointer = event.getPointerId(event.getActionIndex());
                if (mTrackingPointer == upPointer) {
                    // gesture is ongoing, find a new pointer to track
                    final int newIndex = event.getPointerId(0) != upPointer ? 0 : 1;
                    mTrackingPointer = event.getPointerId(newIndex);
                    mInitialTouchX = event.getX(newIndex);
                    mInitialTouchY = event.getY(newIndex);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                final float h = y - mInitialTouchY;
                trackMovement(event);
                if (mQsTracking) {

                    // Already tracking because onOverscrolled was called. We need to update here
                    // so we don't stop for a frame until the next touch event gets handled in
                    // onTouchEvent.
                    setQsExpansion(h + mInitialHeightOnTouch);
                    trackMovement(event);
                    mIntercepting = false;
                    return true;
                }
                if (Math.abs(h) > mTouchSlop && Math.abs(h) > Math.abs(x - mInitialTouchX)
                        && shouldQuickSettingsIntercept(mInitialTouchX, mInitialTouchY, h)) {
                    mQsTracking = true;
                    onQsExpansionStarted();
                    mInitialHeightOnTouch = mQsExpansionHeight;
                    mInitialTouchY = y;
                    mInitialTouchX = x;
                    mIntercepting = false;
                    mNotificationStackScroller.removeLongPressCallback();
                    return true;
                }
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                trackMovement(event);
                if (mQsTracking) {
                    flingQsWithCurrentVelocity(
                            event.getActionMasked() == MotionEvent.ACTION_CANCEL);
                    mQsTracking = false;
                }
                mIntercepting = false;
                break;
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    protected boolean isInContentBounds(float x, float y) {
        float yTransformed = y - mNotificationStackScroller.getY();
        float stackScrollerX = mNotificationStackScroller.getX();
        return mNotificationStackScroller.isInContentBounds(yTransformed) && stackScrollerX < x
                && x < stackScrollerX + mNotificationStackScroller.getWidth();
    }

    private void resetDownStates(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
//            mOnlyAffordanceInThisMotion = false;
            mQsTouchAboveFalsingThreshold = mQsFullyExpanded;
        }
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        // Block request when interacting with the scroll view so we can still intercept the
        // scrolling when QS is expanded.
        if (mScrollView.isHandlingTouchEvent()) {
            return;
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    private void flingQsWithCurrentVelocity(boolean isCancelMotionEvent) {
        float vel = getCurrentVelocity();
        flingSettings(vel, flingExpandsQs(vel) && !isCancelMotionEvent);
    }

    private boolean flingExpandsQs(float vel) {
        if (isBelowFalsingThreshold()) {
            return false;
        }
        if (Math.abs(vel) < mFlingAnimationUtils.getMinVelocityPxPerSecond()) {
            return getQsExpansionFraction() > 0.5f;
        } else {
            return vel > 0;
        }
    }

    private boolean isBelowFalsingThreshold() {
    	//jiating modify for keyguard begin
        return false;/*!mQsTouchAboveFalsingThreshold; && mStatusBarState == StatusBarState.KEYGUARD*/
        //jiating modify for keyguard end
    }

    private float getQsExpansionFraction() {
        return Math.min(1f, (mQsExpansionHeight - mQsMinExpansionHeight)
                / (getTempQsMaxExpansion() - mQsMinExpansionHeight));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mBlockTouches) {
            return false;
        }
        resetDownStates(event);
      //jiating modify for keyguard begin
        /*if ((!mIsExpanding || mHintAnimationRunning)
                && !mQsExpanded
                && mStatusBar.getBarState() != StatusBarState.SHADE) {
            mAfforanceHelper.onTouchEvent(event);
        }*/
     //jiating modify for keyguard end
//        if (mOnlyAffordanceInThisMotion) {
//            return true;
//        }
		// Initial touching event for panel expanding
		if (mOnTouching == false) {
			mTouchingOffset = mNotficationHandlerView.getTop() - event.getY();
			mOnTouching = true;
		}
        
		mTouchingY = event.getY();
        
        if (event.getActionMasked() == MotionEvent.ACTION_CANCEL
                || event.getActionMasked() == MotionEvent.ACTION_UP) {
            mConflictingQsExpansionGesture = false;
            
            // Reset touch flag
            mTouchingOffset = mNotficationHandlerView.getTop() - event.getY();
            mOnTouching = false;
        }
        
        super.onTouchEvent(event);
        return true;
    }

    @Override
    protected boolean flingExpands(float vel, float vectorVel) {
        boolean expands = super.flingExpands(vel, vectorVel);

        return expands;
    }

    @Override
    protected boolean hasConflictingGestures() {
    	   //jiating modify for keyguard begin
        return false;//mStatusBar.getBarState() != StatusBarState.SHADE;
          //jiating modify for keyguard end
    }

    private void onQsTouch(MotionEvent event) {
        int pointerIndex = event.findPointerIndex(mTrackingPointer);
        if (pointerIndex < 0) {
            pointerIndex = 0;
            mTrackingPointer = event.getPointerId(pointerIndex);
        }
        final float y = event.getY(pointerIndex);
        final float x = event.getX(pointerIndex);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mQsTracking = true;
                mInitialTouchY = y;
                mInitialTouchX = x;
                onQsExpansionStarted();
                mInitialHeightOnTouch = mQsExpansionHeight;
                initVelocityTracker();
                trackMovement(event);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                final int upPointer = event.getPointerId(event.getActionIndex());
                if (mTrackingPointer == upPointer) {
                    // gesture is ongoing, find a new pointer to track
                    final int newIndex = event.getPointerId(0) != upPointer ? 0 : 1;
                    final float newY = event.getY(newIndex);
                    final float newX = event.getX(newIndex);
                    mTrackingPointer = event.getPointerId(newIndex);
                    mInitialHeightOnTouch = mQsExpansionHeight;
                    mInitialTouchY = newY;
                    mInitialTouchX = newX;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                final float h = y - mInitialTouchY;
                setQsExpansion(h + mInitialHeightOnTouch);
                if (h >= getFalsingThreshold()) {
                    mQsTouchAboveFalsingThreshold = true;
                }
                trackMovement(event);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mQsTracking = false;
                mTrackingPointer = -1;
                trackMovement(event);
                float fraction = getQsExpansionFraction();
                if ((fraction != 0f || y >= mInitialTouchY)
                        && (fraction != 1f || y <= mInitialTouchY)) {
                    flingQsWithCurrentVelocity(
                            event.getActionMasked() == MotionEvent.ACTION_CANCEL);
                } else {
                    mScrollYOverride = -1;
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            default:
                break;
        }
    }

    private int getFalsingThreshold() {
        float factor = mStatusBar.isScreenOnComingFromTouch() ? 1.5f : 1.0f;
        return (int) (mQsFalsingThreshold * factor);
    }

    @Override
    public void onOverscrolled(float lastTouchX, float lastTouchY, int amount) {
        if (mIntercepting && shouldQuickSettingsIntercept(lastTouchX, lastTouchY,
                -1 /* yDiff: Not relevant here */)) {
            mQsTracking = true;
            onQsExpansionStarted(amount);
            mInitialHeightOnTouch = mQsExpansionHeight;
            mInitialTouchY = mLastTouchY;
            mInitialTouchX = mLastTouchX;
        }
    }

    @Override
    public void onOverscrollTopChanged(float amount, boolean isRubberbanded) {
        cancelAnimation();
		amount = 0f;
        float rounded = amount >= 1f ? amount : 0f;
        mStackScrollerOverscrolling = rounded != 0f && isRubberbanded;
        mQsExpansionFromOverscroll = rounded != 0f;
        mLastOverscroll = rounded;
        updateQsState();
        setQsExpansion(mQsMinExpansionHeight + rounded);
    }

    @Override
    public void flingTopOverscroll(float velocity, boolean open) {
        mLastOverscroll = 0f;
        setQsExpansion(mQsExpansionHeight);
        flingSettings(open ? 0f : velocity, false,
                new Runnable() {
            @Override
            public void run() {
                mStackScrollerOverscrolling = false;
                mQsExpansionFromOverscroll = false;
                updateQsState();
            }
        });
    }

    private void onQsExpansionStarted() {
        onQsExpansionStarted(0);
    }

    private void onQsExpansionStarted(int overscrollAmount) {
        cancelAnimation();

        // Reset scroll position and apply that position to the expanded height.
        float height = mQsExpansionHeight - mScrollView.getScrollY() - overscrollAmount;
        if (mScrollView.getScrollY() != 0) {
            mScrollYOverride = mScrollView.getScrollY();
        }
        mScrollView.scrollTo(0, 0);
        setQsExpansion(height);
        requestPanelHeightUpdate();
    }

    public void setBarState(int statusBarState, boolean keyguardFadingAway,
            boolean goingToFullShade) {
    	   //jiating modify for keyguard begin
        boolean keyguardShowing =false; /*statusBarState == StatusBarState.KEYGUARD
                || statusBarState == StatusBarState.SHADE_LOCKED;*/
 
      /*  if (!mKeyguardShowing && keyguardShowing) {
            setQsTranslation(mQsExpansionHeight);
            mHeader.setTranslationY(0f);
        }
        setKeyguardStatusViewVisibility(statusBarState, keyguardFadingAway, goingToFullShade);
        setKeyguardBottomAreaVisibility(statusBarState, goingToFullShade);
        if (goingToFullShade) {
            animateKeyguardStatusBarOut();
        } else {
            mKeyguardStatusBar.setAlpha(1f);
            mKeyguardStatusBar.setVisibility(keyguardShowing ? View.VISIBLE : View.INVISIBLE);
        }*/
      //jiating modify for keygurad end
        mStatusBarState = statusBarState;
        mKeyguardShowing = keyguardShowing;
        updateQsState();
        if (goingToFullShade) {
            animateHeaderSlidingIn();
        }
    }

//    private final Runnable mAnimateKeyguardStatusViewInvisibleEndRunnable = new Runnable() {
//        @Override
//        public void run() {
//            mKeyguardStatusViewAnimating = false;
//            mKeyguardStatusView.setVisibility(View.GONE);
//        }
//    };

//    private final Runnable mAnimateKeyguardStatusViewVisibleEndRunnable = new Runnable() {
//        @Override
//        public void run() {
//            mKeyguardStatusViewAnimating = false;
//        }
//    };

    private final Animator.AnimatorListener mAnimateHeaderSlidingInListener
            = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            mHeaderAnimatingIn = false;
        }
    };

    private final ViewTreeObserver.OnPreDrawListener mStartHeaderSlidingIn
            = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            getViewTreeObserver().removeOnPreDrawListener(this);
            mHeader.setTranslationY(-mHeader.getCollapsedHeight() - mQsPeekHeight);
            mHeader.animate()
                    .translationY(0f)
                    .setStartDelay(mStatusBar.calculateGoingToFullShadeDelay())
                    .setDuration(StackStateAnimator.ANIMATION_DURATION_GO_TO_FULL_SHADE)
                    .setInterpolator(mFastOutSlowInInterpolator)
                    .start();
            return true;
        }
    };
    
    private void animateHeaderSlidingIn() {
        mHeaderAnimatingIn = true;
        getViewTreeObserver().addOnPreDrawListener(mStartHeaderSlidingIn);

    }

//    private final Runnable mAnimateKeyguardStatusBarInvisibleEndRunnable = new Runnable() {
//        @Override
//        public void run() {
//            mKeyguardStatusBar.setVisibility(View.INVISIBLE);
//            mKeyguardStatusBar.setAlpha(1f);
//            mKeyguardStatusBarAnimateAlpha = 1f;
//        }
//    };
//
//    private void animateKeyguardStatusBarOut() {
//        mKeyguardStatusBar.animate()
//                .alpha(0f)
//                .setStartDelay(mStatusBar.getKeyguardFadingAwayDelay())
//                .setDuration(mStatusBar.getKeyguardFadingAwayDuration()/2)
//                .setInterpolator(PhoneStatusBar.ALPHA_OUT)
//                .setUpdateListener(mStatusBarAnimateAlphaListener)
//                .withEndAction(mAnimateKeyguardStatusBarInvisibleEndRunnable)
//                .start();
//    }

//    private final ValueAnimator.AnimatorUpdateListener mStatusBarAnimateAlphaListener =
//            new ValueAnimator.AnimatorUpdateListener() {
//        @Override
//        public void onAnimationUpdate(ValueAnimator animation) {
//            mKeyguardStatusBarAnimateAlpha = mKeyguardStatusBar.getAlpha();
//        }
//    };

//    private void animateKeyguardStatusBarIn() {
//        mKeyguardStatusBar.setVisibility(View.VISIBLE);
//        mKeyguardStatusBar.setAlpha(0f);
//        mKeyguardStatusBar.animate()
//                .alpha(1f)
//                .setStartDelay(0)
//                .setDuration(DOZE_ANIMATION_DURATION)
//                .setInterpolator(mDozeAnimationInterpolator)
//                .setUpdateListener(mStatusBarAnimateAlphaListener)
//                .start();
//    }

    private void setKeyguardStatusViewVisibility(int statusBarState, boolean keyguardFadingAway,
            boolean goingToFullShade) {
    	   //jiating modify for keyguard begin
       /* if ((!keyguardFadingAway && mStatusBarState == StatusBarState.KEYGUARD
                && statusBarState != StatusBarState.KEYGUARD) || goingToFullShade) {
            mKeyguardStatusView.animate().cancel();
            mKeyguardStatusViewAnimating = true;
            mKeyguardStatusView.animate()
                    .alpha(0f)
                    .setStartDelay(0)
                    .setDuration(160)
                    .setInterpolator(PhoneStatusBar.ALPHA_OUT)
                    .withEndAction(mAnimateKeyguardStatusViewInvisibleEndRunnable);
            if (keyguardFadingAway) {
                mKeyguardStatusView.animate()
                        .setStartDelay(mStatusBar.getKeyguardFadingAwayDelay())
                        .setDuration(mStatusBar.getKeyguardFadingAwayDuration()/2)
                        .start();
            }
        } else if (mStatusBarState == StatusBarState.SHADE_LOCKED
                && statusBarState == StatusBarState.KEYGUARD) {
            mKeyguardStatusView.animate().cancel();
            mKeyguardStatusView.setVisibility(View.VISIBLE);
            mKeyguardStatusViewAnimating = true;
            mKeyguardStatusView.setAlpha(0f);
            mKeyguardStatusView.animate()
                    .alpha(1f)
                    .setStartDelay(0)
                    .setDuration(320)
                    .setInterpolator(PhoneStatusBar.ALPHA_IN)
                    .withEndAction(mAnimateKeyguardStatusViewVisibleEndRunnable);
        } else if (statusBarState == StatusBarState.KEYGUARD) {
            mKeyguardStatusView.animate().cancel();
            mKeyguardStatusViewAnimating = false;
            mKeyguardStatusView.setVisibility(View.VISIBLE);
            mKeyguardStatusView.setAlpha(1f);
        } else {
            mKeyguardStatusView.animate().cancel();
            mKeyguardStatusViewAnimating = false;
            mKeyguardStatusView.setVisibility(View.GONE);
            mKeyguardStatusView.setAlpha(1f);
        }*/
    	   //jiating modify for keyguard end
    }

    private void updateQsState() {
        boolean expandVisually = mStackScrollerOverscrolling;
        mHeader.setVisibility((!mKeyguardShowing) ? View.VISIBLE : View.INVISIBLE);
        mHeader.setExpanded(mKeyguardShowing);
        //jiating modify for keyguard begin
        mNotificationStackScroller.setScrollingEnabled(true
                /*mStatusBarState != StatusBarState.KEYGUARD &&*/);
       //jiating modify for keyguard end
        mScrollView.setTouchEnabled(false);
        updateEmptyShadeView();
    }

    private void setQsExpansion(float height) {
        height = Math.min(Math.max(height, mQsMinExpansionHeight), mQsMaxExpansionHeight);
        mQsFullyExpanded = height == mQsMaxExpansionHeight;
        mQsExpansionHeight = height;
        mHeader.setExpansion(getHeaderExpansionFraction());
        setQsTranslation(height);
        requestScrollerTopPaddingUpdate(false /* animate */);
        updateNotificationScrim(height);
        if (mKeyguardShowing) {
            updateHeaderKeyguard();
        }

        // Upon initialisation when we are not layouted yet we don't want to announce that we are
        // fully expanded, hence the != 0.0f check.
        if (height != 0.0f && mQsFullyExpanded && !mLastAnnouncementWasQuickSettings) {
            announceForAccessibility(getContext().getString(
                    R.string.accessibility_desc_quick_settings));
            mLastAnnouncementWasQuickSettings = true;
        }
        if (DEBUG) {
            invalidate();
        }
    }

    private String getKeyguardOrLockScreenString() {
    	   //jiating modify for keyguard begin
       /* if (mStatusBarState == StatusBarState.KEYGUARD) {
            return getContext().getString(R.string.accessibility_desc_lock_screen);
        } else {*/
            return getContext().getString(R.string.accessibility_desc_notification_shade);
//        }
            //jiating modify for keyguard end
    }

    private void updateNotificationScrim(float height) {
        int startDistance = mQsMinExpansionHeight + mNotificationScrimWaitDistance;
        float progress = (height - startDistance) / (mQsMaxExpansionHeight - startDistance);
        progress = Math.max(0.0f, Math.min(progress, 1.0f));
    }

    private float getHeaderExpansionFraction() {
        if (!mKeyguardShowing) {
            return getQsExpansionFraction();
        } else {
            return 1f;
        }
    }

    private void setQsTranslation(float height) {
        if (mKeyguardShowing) {
            mHeader.setY(interpolate(getQsExpansionFraction(), -mHeader.getHeight(), 0));
        }
    }

    private float calculateQsTopPadding() {
        if (mKeyguardShowing) {

            // Either QS pushes the notifications down when fully expanded, or QS is fully above the
            // notifications (mostly on tablets). maxNotifications denotes the normal top padding
            // on Keyguard, maxQs denotes the top padding from the quick settings panel. We need to
            // take the maximum and linearly interpolate with the panel expansion for a nice motion.
            int maxNotifications = mClockPositionResult.stackScrollerPadding
                    - mClockPositionResult.stackScrollerPaddingAdjustment
                    - mNotificationTopPadding;
            int maxQs = getTempQsMaxExpansion();
            //jiating modify for keyguard begin
            int max = /*mStatusBarState == StatusBarState.KEYGUARD
                    ? Math.max(maxNotifications, maxQs)
                    :*/ maxQs;
            //jiating modify for keyguard end
            return (int) interpolate(getExpandedFraction(),
                    mQsMinExpansionHeight, max);
        } else if (mKeyguardShowing && mScrollYOverride == -1) {

            // We can only do the smoother transition on Keyguard when we also are not collapsing
            // from a scrolled quick settings.
            return interpolate(getQsExpansionFraction(),
                    mNotificationStackScroller.getIntrinsicPadding() - mNotificationTopPadding,
                    mQsMaxExpansionHeight);
        } else {
            return mQsExpansionHeight;
        }
    }

    private void requestScrollerTopPaddingUpdate(boolean animate) {
        mNotificationStackScroller.updateTopPadding(calculateQsTopPadding(),
                mScrollView.getScrollY(),
                mAnimateNextTopPaddingChange || animate,
                false);
        mAnimateNextTopPaddingChange = false;
    }

    private void trackMovement(MotionEvent event) {
        if (mVelocityTracker != null) mVelocityTracker.addMovement(event);
        mLastTouchX = event.getX();
        mLastTouchY = event.getY();
    }

    private void initVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
        }
        mVelocityTracker = VelocityTracker.obtain();
    }

    private float getCurrentVelocity() {
        if (mVelocityTracker == null) {
            return 0;
        }
        mVelocityTracker.computeCurrentVelocity(1000);
        return mVelocityTracker.getYVelocity();
    }

    private void cancelAnimation() {
    }

    private void flingSettings(float vel, boolean expand) {
        flingSettings(vel, expand, null);
    }

    private void flingSettings(float vel, boolean expand, final Runnable onFinishRunnable) {
        float target = expand ? mQsMaxExpansionHeight : mQsMinExpansionHeight;
        if (target == mQsExpansionHeight) {
            mScrollYOverride = -1;
            if (onFinishRunnable != null) {
                onFinishRunnable.run();
            }
            return;
        }
        boolean belowFalsingThreshold = isBelowFalsingThreshold();
        if (belowFalsingThreshold) {
            vel = 0;
        }
        mScrollView.setBlockFlinging(true);
        ValueAnimator animator = ValueAnimator.ofFloat(mQsExpansionHeight, target);
        mFlingAnimationUtils.apply(animator, mQsExpansionHeight, target, vel);
        if (belowFalsingThreshold) {
            animator.setDuration(350);
        }
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setQsExpansion((Float) animation.getAnimatedValue());
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mScrollView.setBlockFlinging(false);
                mScrollYOverride = -1;
                if (onFinishRunnable != null) {
                    onFinishRunnable.run();
                }
            }
        });
        animator.start();
    }

    /**
     * @return Whether we should intercept a gesture to open Quick Settings.
     */
    private boolean shouldQuickSettingsIntercept(float x, float y, float yDiff) {
        return false;
    }

  //jiating modify for keyguard begin
    @Override
    protected boolean isScrolledToBottom() {
		return /*mStatusBar.getBarState() == StatusBarState.KEYGUARD
                    ||*/mNotificationStackScroller.isScrolledToBottom();
    }

    @Override
    protected int getMaxPanelHeight() {
//        int min = mStatusBarMinHeight;
//        if (/*mStatusBar.getBarState() != StatusBarState.KEYGUARD
//                &&*/ mNotificationStackScroller.getNotGoneChildCount() == 0) {
//            int minHeight = (int) ((mQsMinExpansionHeight + getOverExpansionAmount())
//                    * HEADER_RUBBERBAND_FACTOR);
//            min = Math.max(min, minHeight);
//        }
//        int maxHeight = calculatePanelHeightShade();
//        maxHeight = Math.max(maxHeight, min);
//        return maxHeight;
    	return mMaxPanelHeight;
    }
      //jiating modify for keyguard end

    @Override
    protected void onHeightUpdated(float expandedHeight) {
        mNotificationStackScroller.setStackHeight(getContentHeight()-mNotificationHandlerHeight);
        updateHeader();
        updateUnlockIcon();
        updateNotificationTranslucency();
        updateNotificationHandler(getContentHeight());
        if (DEBUG) {
            invalidate();
        }
    }

    /**
     * @return a temporary override of {@link #mQsMaxExpansionHeight}, which is needed when
     *         collapsing QS / the panel when QS was scrolled
     */
    private int getTempQsMaxExpansion() {
        int qsTempMaxExpansion = mQsMaxExpansionHeight;
        if (mScrollYOverride != -1) {
            qsTempMaxExpansion -= mScrollYOverride;
        }
        return qsTempMaxExpansion;
    }

    private int calculatePanelHeightShade() {
    	// Modify by GIONEE, adjust panel's maxHeight
    	//int emptyBottomMargin = mNotificationStackScroller.getEmptyBottomMargin();
        int maxHeight = mNotificationStackScroller.getHeight()/* - emptyBottomMargin*/
                - mTopPaddingAdjustment;
        maxHeight += mNotificationStackScroller.getTopPaddingOverflow();
        return maxHeight;
    }

    private int calculatePanelHeightQsExpanded() {
        float notificationHeight = mNotificationStackScroller.getHeight()
                - mNotificationStackScroller.getEmptyBottomMargin()
                - mNotificationStackScroller.getTopPadding();

        // When only empty shade view is visible in QS collapsed state, simulate that we would have
        // it in expanded QS state as well so we don't run into troubles when fading the view in/out
        // and expanding/collapsing the whole panel from/to quick settings.
        if (mNotificationStackScroller.getNotGoneChildCount() == 0
                && mShadeEmpty) {
            notificationHeight = mNotificationStackScroller.getEmptyShadeViewHeight()
                    + mNotificationStackScroller.getBottomStackPeekSize()
                    + mNotificationStackScroller.getCollapseSecondCardPadding();
        }
        int maxQsHeight = mQsMaxExpansionHeight;

      //jiating modify for keyguard begin
        float totalHeight = Math.max(
                maxQsHeight + mNotificationStackScroller.getNotificationTopPadding(),
               /* mStatusBarState == StatusBarState.KEYGUARD
                        ? mClockPositionResult.stackScrollerPadding - mTopPaddingAdjustment
                        :*/ 0)
       //jiating modify for keyguard end
                + notificationHeight;
        if (totalHeight > mNotificationStackScroller.getHeight()) {
            float fullyCollapsedHeight = maxQsHeight
                    + mNotificationStackScroller.getMinStackHeight()
                    + mNotificationStackScroller.getNotificationTopPadding()
                    - getScrollViewScrollY();
            totalHeight = Math.max(fullyCollapsedHeight, mNotificationStackScroller.getHeight());
        }
        return (int) totalHeight;
    }

    private int getScrollViewScrollY() {
        if (mScrollYOverride != -1 && !mQsTracking) {
            return mScrollYOverride;
        } else {
            return mScrollView.getScrollY();
        }
    }
    private void updateNotificationTranslucency() {
        float alpha = (getNotificationsTopY() + mNotificationStackScroller.getItemHeight())
                / (mQsMinExpansionHeight + mNotificationStackScroller.getBottomStackPeekSize()
                        - mNotificationStackScroller.getCollapseSecondCardPadding());
        alpha = Math.max(0, Math.min(alpha, 1));
        alpha = (float) Math.pow(alpha, 0.75);
        if (alpha != 1f && mNotificationStackScroller.getLayerType() != LAYER_TYPE_HARDWARE) {
            mNotificationStackScroller.setLayerType(LAYER_TYPE_HARDWARE, null);
        } else if (alpha == 1f
                && mNotificationStackScroller.getLayerType() == LAYER_TYPE_HARDWARE) {
            mNotificationStackScroller.setLayerType(LAYER_TYPE_NONE, null);
        }
        mNotificationStackScroller.setAlpha(alpha);
    }

    @Override
    protected float getOverExpansionAmount() {
        return mNotificationStackScroller.getCurrentOverScrollAmount(true /* top */);
    }

    @Override
    protected float getOverExpansionPixels() {
        return mNotificationStackScroller.getCurrentOverScrolledPixels(true /* top */);
    }

    private void updateUnlockIcon() {
//jiating modify for keyguard begin
        /*if (mStatusBar.getBarState() == StatusBarState.KEYGUARD
                || mStatusBar.getBarState() == StatusBarState.SHADE_LOCKED) {
            boolean active = getMaxPanelHeight() - getExpandedHeight() > mUnlockMoveDistance;
            KeyguardAffordanceView lockIcon = mKeyguardBottomArea.getLockIcon();
            if (active && !mUnlockIconActive && mTracking) {
                lockIcon.setImageAlpha(1.0f, true, 150, mFastOutLinearInterpolator, null);
                lockIcon.setImageScale(LOCK_ICON_ACTIVE_SCALE, true, 150,
                        mFastOutLinearInterpolator);
            } else if (!active && mUnlockIconActive && mTracking) {
                lockIcon.setImageAlpha(KeyguardAffordanceHelper.SWIPE_RESTING_ALPHA_AMOUNT, true,
                        150, mFastOutLinearInterpolator, null);
                lockIcon.setImageScale(1.0f, true, 150,
                        mFastOutLinearInterpolator);
            }
            mUnlockIconActive = active;
        }*/
    	//jiating modify for keyguard end
    }

    /**
     * Update notification handler in the bottom
     * Hanlder's final position: top = ScreenHeight - NavigatorbarHeight - offset
     * */
	private void updateNotificationHandler(float expandedHeight) {
		int handlerPadding = (int) expandedHeight;
		final int maxHeight = getKeyguardMaxPanelHeight();
		
		if (mOnTouching) {
			handlerPadding = (int) (mTouchingY + mTouchingOffset);
		}
		
		int maxHandlerPadding = maxHeight- mNotificationHandlerHeight;
		if (isFullyExpanded() || (handlerPadding > maxHandlerPadding)) {
			handlerPadding = maxHandlerPadding;
		}
		
		mNotficationHandlerView.setTop(handlerPadding);
		setBottom(handlerPadding + mNotificationHandlerHeight);
		//<GIONEE> <wujj> <2015-02-27> modify for CR01449220 begin
		mStatusBar.updateCarrierLabelVisibility(handlerPadding - mCarrierLabelHeight);
		//<GIONEE> <wujj> <2015-02-27> modify for CR01449220 end
		
		// GIONEE <wujj> <2015-04-24> add begin
		if (mStatusBar.getUpdateNaviFlag()) {
			if (isFullyCollapsed()) {
				mStatusBar.refreshNavigationBar(BarTransitions.MODE_TRANSPARENT, 
						android.app.StatusBarManager.WINDOW_STATE_SHOWING);
			} else if(isFullyExpanded()) {
				mStatusBar.refreshNavigationBar(BarTransitions.MODE_OPAQUE, 
						android.app.StatusBarManager.WINDOW_STATE_SHOWING);
			}
		}
		// GIONEE <wujj> <2015-04-24> add end
	}
	
	private int getKeyguardMaxPanelHeight() {
		KeyguardViewMediator keyguardViewMediator = mStatusBar.getComponent(KeyguardViewMediator.class);
//		final KeyguardTouchDelegate keyguard = KeyguardTouchDelegate.getInstance(getContext());
		//jiating modify for CR01444743  begin
		if (keyguardViewMediator.isShowingAndNotOccluded()) {
		//jiating modify for CR01444743  end
			hideNavigatorBar();
			return Math.max(getHeight(), mScrollView.getHeight());
		} else {
			return mScrollView.getHeight();
		}
	}
	private void hideNavigatorBar() {
		NavigationBarView navigationBarView = mStatusBar.getNavigationBarView();
		if (navigationBarView != null) {
			navigationBarView.setVisibility(View.INVISIBLE);
		}
	}

	private boolean isLandScape() {
		return getResources().getConfiguration().orientation ==
				Configuration.ORIENTATION_LANDSCAPE;
	}
	
    /**
     * Hides the header when notifications are colliding with it.
     */
    private void updateHeader() {
    	//jiating modify for keyguard begin
       /* if (mStatusBar.getBarState() == StatusBarState.KEYGUARD
                || mStatusBar.getBarState() == StatusBarState.SHADE_LOCKED) {
            updateHeaderKeyguard();
        } else {*/
            updateHeaderShade();
//        }
       //jiating modify for keyguard end

    }

    private void updateHeaderShade() {
        if (!mHeaderAnimatingIn) {
            mHeader.setTranslationY(getHeaderTranslation());
        }
        setQsTranslation(mQsExpansionHeight);
    }

    private float getHeaderTranslation() {
    	//jiating modify for keyguard begin
     /*   if (mStatusBar.getBarState() == StatusBarState.KEYGUARD
                || mStatusBar.getBarState() == StatusBarState.SHADE_LOCKED) {
            return 0;
        }*/
    	//jiating modify for keyguard end
        if (mNotificationStackScroller.getNotGoneChildCount() == 0) {
            if (mExpandedHeight / HEADER_RUBBERBAND_FACTOR >= mQsMinExpansionHeight) {
                return 0;
            } else {
                return mExpandedHeight / HEADER_RUBBERBAND_FACTOR - mQsMinExpansionHeight;
            }
        }
        return Math.min(0, mNotificationStackScroller.getTranslationY()) / HEADER_RUBBERBAND_FACTOR;
    }

    private void updateHeaderKeyguard() {
        float alphaNotifications;
      //jiating modify for keyguard begin
        /*if (mStatusBar.getBarState() == StatusBarState.KEYGUARD) {

            // When on Keyguard, we hide the header as soon as the top card of the notification
            // stack scroller is close enough (collision distance) to the bottom of the header.
            alphaNotifications = getNotificationsTopY()
                    /
                    (mKeyguardStatusBar.getHeight() + mNotificationsHeaderCollideDistance);
        } else {*/

            // In SHADE_LOCKED, the top card is already really close to the header. Hide it as
            // soon as we start translating the stack.
//            alphaNotifications = getNotificationsTopY() / mKeyguardStatusBar.getHeight();
//        }
          //jiating modify for keyguard end
//        alphaNotifications = MathUtils.constrain(alphaNotifications, 0, 1);
//        alphaNotifications = (float) Math.pow(alphaNotifications, 0.75);
//        float alphaQsExpansion = 1 - Math.min(1, getQsExpansionFraction() * 2);
//        mKeyguardStatusBar.setAlpha(Math.min(alphaNotifications, alphaQsExpansion)
//                * mKeyguardStatusBarAnimateAlpha);
//        setQsTranslation(mQsExpansionHeight);
    }

    private float getNotificationsTopY() {
        if (mNotificationStackScroller.getNotGoneChildCount() == 0) {
            return getExpandedHeight();
        }
        return mNotificationStackScroller.getNotificationsTopY();
    }

    @Override
    protected void onExpandingStarted() {
        super.onExpandingStarted();
        mNotificationStackScroller.onExpansionStarted();
        mIsExpanding = true;
        mQsExpandedWhenExpandingStarted = mQsFullyExpanded;
    }

    @Override
    protected void onExpandingFinished() {
        super.onExpandingFinished();
        mNotificationStackScroller.onExpansionStopped();
        mIsExpanding = false;
        mScrollYOverride = -1;
        if (mExpandedHeight == 0f) {
            setListening(false);
        } else {
            setListening(true);
        }
    }

    private void setListening(boolean listening) {
//        mKeyguardStatusBar.setListening(listening);
    }

    @Override
    public void instantExpand() {
        super.instantExpand();
        setListening(true);
    }

    @Override
    protected void setOverExpansion(float overExpansion, boolean isPixels) {
      //jiating modify for keyguard begin
//        if (mStatusBar.getBarState() != StatusBarState.KEYGUARD) {
            mNotificationStackScroller.setOnHeightChangedListener(null);
            if (isPixels) {
                mNotificationStackScroller.setOverScrolledPixels(
                        overExpansion, true /* onTop */, false /* animate */);
            } else {
                mNotificationStackScroller.setOverScrollAmount(
                        overExpansion, true /* onTop */, false /* animate */);
            }
            mNotificationStackScroller.setOnHeightChangedListener(this);
//        }
          //jiating modify for keyguard end
    }

    @Override
    protected void onTrackingStarted() {
        super.onTrackingStarted();
          //jiating modify for keyguard begin
      /*  if (mStatusBar.getBarState() == StatusBarState.KEYGUARD
                || mStatusBar.getBarState() == StatusBarState.SHADE_LOCKED) {
            mAfforanceHelper.animateHideLeftRightIcon();
        }*/
      //jiating modify for keyguard end
    }

    @Override
    protected void onTrackingStopped(boolean expand) {
        super.onTrackingStopped(expand);
        if (expand) {
            mNotificationStackScroller.setOverScrolledPixels(
                    0.0f, true /* onTop */, true /* animate */);
        }
      //jiating modify for keyguard begin
        /*if (expand && (mStatusBar.getBarState() == StatusBarState.KEYGUARD
                || mStatusBar.getBarState() == StatusBarState.SHADE_LOCKED)) {
            if (!mHintAnimationRunning) {
                mAfforanceHelper.reset(true);
            }
        }*/
        /*if (!expand && (mStatusBar.getBarState() == StatusBarState.KEYGUARD
                || mStatusBar.getBarState() == StatusBarState.SHADE_LOCKED)) {
            KeyguardAffordanceView lockIcon = mKeyguardBottomArea.getLockIcon();
            lockIcon.setImageAlpha(0.0f, true, 100, mFastOutLinearInterpolator, null);
            lockIcon.setImageScale(2.0f, true, 100, mFastOutLinearInterpolator);
        }*/
      //jiating modify for keyguard end
    }

    @Override
    public void onHeightChanged(ExpandableView view) {

        // Block update if we are in quick settings and just the top padding changed
        // (i.e. view == null).
        requestPanelHeightUpdate();
    }

    @Override
    public void onReset(ExpandableView view) {
    }

    @Override
    public void onScrollChanged() {
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // GIONEE <wujj> <2015-06-03> add for CR01494595 begin
        int screenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
        mMaxPanelHeight = screenHeight - mScrollPadding;
        Log.v(TAG, "onConfigurationChanged: mMaxPanelHeight = "+mMaxPanelHeight);
        // GIONEE <wujj> <2015-06-03> add for CR01494595 end
    }

    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
        if (layoutDirection != mOldLayoutDirection) {
            mOldLayoutDirection = layoutDirection;
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mHeader) {
            onQsExpansionStarted();
        }
    }

    @Override
    protected void onEdgeClicked(boolean right) {
    	return;
    }

    @Override
    protected void startUnlockHintAnimation() {
        super.startUnlockHintAnimation();
    }

    @Override
    protected float getPeekHeight() {
        if (mNotificationStackScroller.getNotGoneChildCount() > 0) {
            return mNotificationStackScroller.getPeekHeight();
        } else {
            return mQsMinExpansionHeight * HEADER_RUBBERBAND_FACTOR;
        }
    }

    @Override
    protected float getCannedFlingDurationFactor() {
		return 0.6f;
    }

    @Override
    protected boolean fullyExpandedClearAllVisible() {
    	return false;
    }

    @Override
    protected boolean isClearAllVisible() {
//        return mNotificationStackScroller.isDismissViewVisible();
    	return false;
    }

    @Override
    protected int getClearAllHeight() {
        return 0;
    }

    @Override
    protected boolean isTrackingBlocked() {
        return false;
    }

    public void notifyVisibleChildrenChanged() {
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return true;
    }

    public boolean isLaunchTransitionFinished() {
        return mIsLaunchTransitionFinished;
    }

    public boolean isLaunchTransitionRunning() {
        return mIsLaunchTransitionRunning;
    }

    public void setLaunchTransitionEndRunnable(Runnable r) {
        mLaunchAnimationEndRunnable = r;
    }

    public void setEmptyDragAmount(float amount) {
        float factor = 0.8f;
        if (mNotificationStackScroller.getNotGoneChildCount() > 0) {
            factor = 0.4f;
        } else if (!mStatusBar.hasActiveNotifications()) {
            factor = 0.4f;
        }
        mEmptyDragAmount = amount * factor;
        positionClockAndNotifications();
    }

    private static float interpolate(float t, float start, float end) {
        return (1 - t) * start + t * end;
    }

    public void setDozing(boolean dozing, boolean animate) {
        if (dozing == mDozing) return;
        mDozing = dozing;
//        if (mDozing) {
//            mKeyguardStatusBar.setVisibility(View.INVISIBLE);
//        } else {
//            mKeyguardStatusBar.setVisibility(View.VISIBLE);
//            if (animate) {
//                animateKeyguardStatusBarIn();
//            }
//        }
    }

    @Override
    public boolean isDozing() {
        return mDozing;
    }

    public void setShadeEmpty(boolean shadeEmpty) {
        mShadeEmpty = shadeEmpty;
        updateEmptyShadeView();
    }

    private void updateEmptyShadeView() {

        // Hide "No notifications" in QS.
        mNotificationStackScroller.updateEmptyShadeView(mShadeEmpty);
    }

    public void setQsScrimEnabled(boolean qsScrimEnabled) {
        boolean changed = mQsScrimEnabled != qsScrimEnabled;
        mQsScrimEnabled = qsScrimEnabled;
        if (changed) {
            updateQsState();
        }
    }

    public void setKeyguardUserSwitcher(KeyguardUserSwitcher keyguardUserSwitcher) {
        mKeyguardUserSwitcher = keyguardUserSwitcher;
    }

    private final Runnable mUpdateHeader = new Runnable() {
        @Override
        public void run() {
            mHeader.updateEverything();
        }
    };

    public void onScreenTurnedOn() {
//        mKeyguardStatusView.refreshTime();
    }

    @Override
    public void onEmptySpaceClicked(float x, float y) {
        onEmptySpaceClick(x);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
    	if (mBar.isHighconfigDevice()) {
	    	synchronized (GnBlurHelper.LOCK) {
	    		if (GnBlurHelper.mBlur != null && !GnBlurHelper.mBlur.isRecycled()) {
	    			int bitMapWidth = GnBlurHelper.mBlur.getWidth();
	    			int panelWidth = getWidth();
	    			int panelHeight = getHeight();
					Rect src = new Rect(0, 0, bitMapWidth, panelHeight);
					Rect dst = new Rect(0, 0, panelWidth, panelHeight);
	
	    			Paint paint = new Paint();
	    			//GIONEE <wujj> <2015-06-19> modify for CR01489937 begin
	    			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
	    			canvas.drawBitmap(GnBlurHelper.mBlur, src, dst, paint);
	    			canvas.drawColor(0xF2181818);
	    			//GIONEE <wujj> <2015-06-19> modify for CR01489937 end
	    		} else {
	    			Log.v(TAG,"NotificationPanelView: blur bitmap is null");
	    			canvas.drawColor(0xF2181818);
	    		}
			}
    	} else {
    		canvas.drawColor(0xF2131313);
    	}
        super.dispatchDraw(canvas);
        if (DEBUG) {
            Paint p = new Paint();
            p.setColor(Color.RED);
            p.setStrokeWidth(2);
            p.setStyle(Paint.Style.STROKE);
            canvas.drawLine(0, getMaxPanelHeight(), getWidth(), getMaxPanelHeight(), p);
            p.setColor(Color.BLUE);
            canvas.drawLine(0, getExpandedHeight(), getWidth(), getExpandedHeight(), p);
            p.setColor(Color.GREEN);
            canvas.drawLine(0, calculatePanelHeightQsExpanded(), getWidth(),
                    calculatePanelHeightQsExpanded(), p);
            p.setColor(Color.YELLOW);
            canvas.drawLine(0, calculatePanelHeightShade(), getWidth(),
                    calculatePanelHeightShade(), p);
            p.setColor(Color.MAGENTA);
            canvas.drawLine(0, calculateQsTopPadding(), getWidth(),
                    calculateQsTopPadding(), p);
            p.setColor(Color.CYAN);
            canvas.drawLine(0, mNotificationStackScroller.getTopPadding(), getWidth(),
                    mNotificationStackScroller.getTopPadding(), p);
        }
    }
	
	// GIONEE <wujj> <2015-03-12> optimize code begin
	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			invalidate();
		}
	};

	public void updateBackground() {
		mHandler.sendEmptyMessage(0);
	}
	// GIONEE <wujj> <2015-03-12> optimize code end
}
