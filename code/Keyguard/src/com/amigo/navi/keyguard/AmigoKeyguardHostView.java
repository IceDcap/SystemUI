package com.amigo.navi.keyguard;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Scroller;

import com.android.keyguard.KeyguardSecurityContainer.SecurityViewRemoveAnimationUpdateCallback;
import com.android.keyguard.KeyguardSecurityView;
import com.android.keyguard.KeyguardViewBase;
import com.android.keyguard.KeyguardSecurityModel.SecurityMode;
//import com.gionee.navi.keyguard.KeyguardSecurityModel.SecurityMode;
//import com.gionee.navi.keyguard.KeyguardUpdateMonitor;
//import com.gionee.navi.keyguard.KeyguardUpdateMonitorCallback;
//import com.gionee.navi.keyguard.KeyguardViewManager;
//import com.gionee.navi.keyguard.NavilLockScreenService;
import com.android.keyguard.R;
import com.android.keyguard.ViewMediatorCallback;
//import com.gionee.navi.keyguard.amigo.wallpaper.AmigoWallpaperUtils;
//import com.gionee.navi.keyguard.amigo.wallpaper.KeyguardWallpaperManager;
import com.amigo.navi.keyguard.AppConstants;
import com.amigo.navi.keyguard.AmigoKeyguardBouncer.KeyguardBouncerCallback;
import com.amigo.navi.keyguard.haokan.UIController;
import com.amigo.navi.keyguard.picturepage.widget.OnViewTouchListener;
import com.amigo.navi.keyguard.fingerprint.FingerIdentifyManager;
import com.android.internal.widget.LockPatternUtils;
//import com.gionee.navi.keyguard.everydayphoto.WallpaperData;
import static com.android.keyguard.KeyguardHostView.OnDismissAction;
import com.android.internal.widget.LockPatternUtils;

public class AmigoKeyguardHostView extends LinearLayout implements SecurityViewRemoveAnimationUpdateCallback{
    private static final String TAG = "AmigoKeyguardHostView";
	private static final String LOG_TAG = "NaviKg_HostView";
	
	public static final String KEY_LOCK_BY_LAUNCHER = "lock_by_launcher";
	public static final String KEY_LOCK_BY_SENSOR = "lock_by_sensor";
	/****define this key with framework,so don't change it*****/
	public static final String KEY_LOCK_BY_SKYLIGHT = "lock_by_skylight";
	/**********************************************************/
	public static final String KEY_LOCK_BY_FANFAN = "lock_by_fanfan";
	public static final String KEY_LOCK_BY_SIMLOCK = "lock_by_simlock";
	public static final String KEY_LOCK_BY_SIMLOCK_SUBID = "lock_by_simlock_subid";
	public static final String KEY_LOCK_BY_FLIGHT_MODE = "lock_by_flight_mode";
	public static final String KEY_LOCK_BY_SYSTEM_READY = "lock_by_system_ready";
	
	public static final String KEY_LOCK_BY_VERIFY = "lock_by_verify";
	
	private Rect mViewport = new Rect();
	
	boolean isFirstViewBringToFront = false;
	
	private static final int TOUCH_STATE_REST = 1;
	private static final int TOUCH_STATE_MOVE = 2;

//	private int mTouchState = TOUCH_STATE_REST;
	
	private static boolean SLIDING_ENABLE = true;
	
	private float mDownMotionX = 0;
	private float mDownMotionY = 0;
//	private float mLastMotionX = 0;
	private float mLastMotionY = 0;
//	private float mLastMotionRemainderX = 0;
	private float mLastMotionRemainderY = 0;
//	private int mUnboundedScrollX = 0;
//	private int mUnboundedScrollY = 0;
	private int mMinBoundY = 0;
	private int mMaxBoundY = 1920;
	
	private LinearLayout mLinearLayoutRotation;
//    private boolean mLinearLayoutRotationVisible = false;

	private int[] mPagerOffset = null;
	private int[] mRelativePagerOffset = null;
	private static final int INVALID_PAGER_OFFSET = -1;
	
	private Scroller mScroller = null;
	private int mTouchSlop = 0;
//	private boolean mUnlockAnimStart = false;

	private VelocityTracker mVelocityTracker = null;
	private int mMinFlingVelocity = 0;
	private int mMinFlingDistance = 0;
	
	private static final int DIRECTION_DOWN = 0;
	private static final int DIRECTION_UP = 1;
    private static final int DIRECTION_H = 2;
	private static final int DIRECTION_NONE = -1;
	private int mFlingDirection=0;
	
	private int mTouchCallTime = 0;
	private int mScrollDirection = DIRECTION_NONE;
	private boolean mScrollDirectionConfirmed = false;
	
	private  int mMinScrollDirectionConfirmationDistance = 12;
	
//	private KeyguardViewManager mKeyguardViewManager = null;
	
//	private boolean mShouldAddLauncherShot = true;
	
	private AmigoKeyguardPage mKeyguardPage = null;
//	private KeyguardViewBase mKeyguardView;
//	private AmigoLauncherShotPage mLauncherShotPage = null;
	

//	private SecurityMode mCurrentSecuritySelection = SecurityMode.Invalid;
	
    // wallpaper background
    
    private Matrix mBgMatrix = new Matrix();
    
    
    private boolean mHasSetWallpaperOnKeyguardPage = false;
    
	public static final int CHANGE_BACKGROUND_LOCATIONY = 0;
	public static final int CHANGE_BACKGROUND_BLUR = 1;
	public static final int CHANGE_BACKGROUND_BLIND = 2;
	public static final int CHANGE_BACKGROUND_BLUR_AND_BLIND = 3;
	public static final int CHANGE_BACKGROUND_DO_NOTING = 4;
	
	
	private float mDegreeX;
	private float mDegreeRatio;
	
//	private ShortcutMediator mShortcutMeidator;
	private int mDisplayHeight;
//    private int mShortcutHeight;
//	private boolean mIsTriggerShortcut=false;
//	private boolean mIsShortcutShow=false;
//	private boolean mIsHomePage=true;
//    private KeyguardWallpaperManager mKeyguardWallpaperManager;
	
	protected ViewMediatorCallback mViewMediatorCallback;
	private LockPatternUtils mLockPatternUtils;

	private float mDisplayDensity=1.0f;
	
	private float mTriggerShortcutTouchHeight=30;
	
	private static final double  minAngle = 45;
	private AmigoKeyguardBouncer mKeyguardBouncer;
	private boolean mBouncerIsShowing=false;
	
	private GestureDetector mGestureDetector;
	
	public AmigoKeyguardHostView(Context context) {
		this(context, null,0);
		mDisplayDensity = getResources().getDisplayMetrics().density;
		mMinScrollDirectionConfirmationDistance = (int) (mMinScrollDirectionConfirmationDistance*mDisplayDensity);
		mTriggerShortcutTouchHeight=mTriggerShortcutTouchHeight*mDisplayDensity;
	}
	
	public AmigoKeyguardHostView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}
	
    public AmigoKeyguardHostView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


	public void initKeyguard(ViewMediatorCallback callback,LockPatternUtils lockPatternUtils){
    	setViewMediatorCallback(callback);
    	mKeyguardBouncer.initKeyguardBouncer(callback, lockPatternUtils,this);
    	
    }
    public void setViewMediatorCallback(ViewMediatorCallback viewMediatorCallback) {
        mViewMediatorCallback = viewMediatorCallback;
    }
//	public void setKeyguardViewManager(KeyguardViewManager mgr) {
//		mKeyguardViewManager = mgr;
//	}
	
	public static void setFieldSliding_enableToFalse() {
		SLIDING_ENABLE = false;
	}

	private void init(Context context) {
		setOverScrollMode(View.OVER_SCROLL_NEVER);
		mMaxBoundY = KWDataCache.getXPageHeight(getResources());
		initScrollerData();
		initScreenHeight();
		addPageIntoLockScreenPagedView(context);
		setBackButtonEnabled(false);
		mKeyguardBouncer=new AmigoKeyguardBouncer(context,  this);
		mBouncerIsShowing=false;
		mUIController = UIController.getInstance();
		mUIController.setAmigoKeyguardHostView(this);
		initLongPressListener();
	}
	
	private void initScreenHeight(){
        mDisplayHeight=KWDataCache.getAllScreenHeigt(getContext());
        
    }
	
	
	
	private void addPageIntoLockScreenPagedView(Context context) {
		addKeyguardPage();
//		initKeyguardPageFurther();
//		addXPage(context);
		
	}
	
	
	private void resetMaxBound() {
		mDegreeX = mMaxBoundY / 2.3f;
		mDegreeRatio = mDegreeX / 90;
		if(DebugLog.DEBUG){
			DebugLog.d(LOG_TAG, "resetMaxBound()");
		}
	}
	
    private void setBackButtonEnabled(boolean enabled) {
        if (mContext instanceof Activity)
            return; // always enabled in activity mode
        setSystemUiVisibility(enabled ? getSystemUiVisibility() & ~View.STATUS_BAR_DISABLE_BACK
                : getSystemUiVisibility() | View.STATUS_BAR_DISABLE_BACK);
    }
	
	private void addXPage(Context context) {
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,mMaxBoundY);
		ViewGroup bouncer= (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.keyguard_bouncer, null);
//		mLauncherShotPage = new AmigoLauncherShotPage(context);
		addView(bouncer,lp);
//		mKeyguardView = (KeyguardViewBase) bouncer.findViewById(R.id.keyguard_host_view);

//		AmigoXPageManager.getInstance(mContext).setUpXPage(mLauncherShotPage);
	}

	private void addKeyguardPage() {
		LayoutInflater.from(getContext()).inflate(
				R.layout.amigo_keyguard_page_view, this, true);
		mKeyguardPage = (AmigoKeyguardPage) findViewById(R.id.amigo_keyguard_page);
	}
	
	private void initScrollerData() {
		mScroller = new Scroller(getContext(), new Interpolator() {
			@Override
			public float getInterpolation(float v) {
				v -=  1.0f;
				return v * v * v + 1.0f;
			}
		});
		
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		
		mMinFlingVelocity = Math.round(800 * mDisplayDensity);
//		mMinFlingDistance = Math.round((float)dm.heightPixels / 3);
		mMinFlingDistance = (int)((float)KWDataCache.getAllScreenHeigt(mContext)) / 3;
	}

	private int getViewportOffsetX() {
		return (getMeasuredWidth() - mViewport.width()) / 2;
	}

	private int getViewportOffsetY() {
		return (getMeasuredHeight() - mViewport.height()) / 2;
	}
	
	private boolean isTouchedInViewport(int x, int y) {
		boolean touched = mViewport.contains(x, y);

		return touched;
	}

//	@Override
//	protected void onLayout(boolean changed, int l, int t, int r, int b) {
//		if (getChildCount() == 0) {
//			return;
//		}
//
//		int offsetX = getViewportOffsetX();
//		int offsetY = getViewportOffsetY();
//		mViewport.offset(offsetX, offsetY);
//		
//		int childLeft = offsetX + getPaddingLeft();
//		int childTop = offsetY + getPaddingTop();
//		final int childCount = getChildCount();
//		if (!isFirstViewBringToFront) {
//			isFirstViewBringToFront = true;
//			for (int i = 0; i < childCount; ++i) {
//				View child = getChildAt(i);
//				int childRight = childLeft + child.getMeasuredWidth();
//				int childBottom = childTop + child.getMeasuredHeight();
//				child.layout(childLeft, childTop, childRight, childBottom);
//				childTop += child.getMeasuredHeight();
//			}
//			View child = getChildAt(0);
//			child.bringToFront();
//		} else {
//			childTop += getChildAt(childCount-1).getMeasuredHeight();
//            for (int i = 0; i < childCount; i++) {
//                View child = getChildAt(i);
//                if (i == childCount - 1) {
//                    int childRight = childLeft + child.getMeasuredWidth();
//                    int childBottom = offsetY + getPaddingTop() + child.getMeasuredHeight();
//                    child.layout(childLeft, offsetY + getPaddingTop(), childRight, childBottom);
//                    DebugLog.d(LOG_TAG, "childTop: 222"+(offsetY + getPaddingTop())+"  childBottom: "+childBottom);
//                } else {
//                    int childRight = childLeft + child.getMeasuredWidth();
//                    int childBottom = childTop + child.getMeasuredHeight();
//                    child.layout(childLeft, childTop, childRight, childBottom);
//                    DebugLog.d(LOG_TAG, "childTop: 111"+childTop+"  childBottom: "+childBottom);
//                    childTop += child.getMeasuredHeight();
//                }
//            }
//		}
//	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (getChildCount() == 0) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			return;
		}

		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		if (widthMode == MeasureSpec.UNSPECIFIED
				|| heightMode == MeasureSpec.UNSPECIFIED
				|| widthSize <= 0 || heightSize <= 0) {
		    Log.d(LOG_TAG, "onMeasure  UNSPECIFIED---------");
		    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			return;
		}

		mViewport.set(0, 0, widthSize, heightSize);

//		final int verticalPadding = getPaddingTop() + getPaddingBottom();
//		final int horizontalPadding = getPaddingLeft() + getPaddingRight();
//
//		final int childCount = getChildCount();
//		for (int i = 0; i < childCount; ++i) {
//			View child = getChildAt(i);
//			ViewGroup.LayoutParams lp = child.getLayoutParams();
//
//			final int childWidthMode = getChildMeasureSpecMode(lp.width);
//			final int childHeightMode = getChildMeasureSpecMode(lp.height);
//			int childWidthMeausred = MeasureSpec.makeMeasureSpec(widthSize
//					- horizontalPadding, childWidthMode);
//			int childHeightMeasured = MeasureSpec.makeMeasureSpec(heightSize
//					- verticalPadding, childHeightMode);
//			child.measure(childWidthMeausred, childHeightMeasured);
//		}
//
//		setMeasuredDimension(widthSize, heightSize);
		
		invalidPagerOffset();
		
		resetMaxBound();
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	private int getChildMeasureSpecMode(int dimension) {
		return dimension == LayoutParams.WRAP_CONTENT ? 
				MeasureSpec.AT_MOST : MeasureSpec.EXACTLY;
	}
	
	private int getPagerOffset(int pageId) {
		if (pageId < 0 || pageId >= getChildCount()) {
			if(DebugLog.DEBUG){
				DebugLog.e(LOG_TAG, "index out of range");
			}
			return INVALID_PAGER_OFFSET;
		}

		int offset = 0;
		if (mPagerOffset != null) {
			if (mPagerOffset[pageId] == INVALID_PAGER_OFFSET) {
				int result = 0;
				for (int i = 0; i < pageId; ++i) {
					result += getChildAt(i).getMeasuredHeight();
				}
				mPagerOffset[pageId] = result;
			}

			offset = mPagerOffset[pageId];
		}
		if(DebugLog.DEBUG){
			DebugLog.d(LOG_TAG, "pager offset " + offset + ", pageId=" + pageId);
		}
		return offset;
	}

	private void invalidPagerOffset() {
		final int childCount = getChildCount();
		mPagerOffset = new int[childCount];
		mRelativePagerOffset = new int[childCount];
		for (int i = 0; i < childCount; ++i) {
			mPagerOffset[i] = INVALID_PAGER_OFFSET;
			mRelativePagerOffset[i] = INVALID_PAGER_OFFSET;
		}
	}
	
//	private int getOverlappedHeight(Context context) {
//		return KWDataCache.getStatusBarHeight() + mKeyguardPage.getInfozoneHeight();
//	}
	
	

	@Override
	protected void dispatchDraw(Canvas canvas) {
//		drawBackground(canvas);
		super.dispatchDraw(canvas);
	}

	private void drawBackground(Canvas canvas) {
		mBgMatrix.reset();
//		Matrix matrix = new Matrix();
//
//		if(mWallpaperIsTranslation){
//			matrix.setTranslate(mWallpaperOffsetX, 0);		
//		} else {
//			matrix.setTranslate(mWallpaperOffsetX, getScrollY());
//		}
//
//		if(mBackground != null){
//			canvas.drawBitmap(mBackground, matrix, null);			
//		}else {
//			DebugLog.e("Keyguard", "Keyguard Exception: Background == null");
//		}
//		
//		if(mBlurBackground != null && !mWallpaperIsTranslation && mWallpaperIsBlur){
//			Paint paint = new Paint();
//			paint.setAlpha((getScrollY()*255)/mMaxBoundY);
//			canvas.drawBitmap(mBlurBackground, matrix, paint);			
//		}
	}

    public void dispatch(MotionEvent event) {
    	if(DebugLog.DEBUG){
    		DebugLog.d(LOG_TAG, "dispatch-------");
		}
        dispatchTouchEvent(event);
    }
	
	@Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        super.dispatchTouchEvent(event);
        userActivity(event);
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
        	if (isControllerCenterArea(event)) {
				return false;
			}
        } 
        if (!isAtSecurity()) {
            if(action==MotionEvent.ACTION_DOWN||action==MotionEvent.ACTION_UP){
            	if(DebugLog.DEBUG){
            		DebugLog.d(LOG_TAG, "AmigoKeyguardHostView dispatchTouchEvent gotoSleepIfDoubleTap action:"+action);
    			}
                gotoSleepIfDoubleTap(event);
            }
        }
        return true;
    }
	
	private boolean isControllerCenterArea(MotionEvent event){
		float pointerY=event.getY();
		if(DebugLog.DEBUG){
			DebugLog.d(LOG_TAG, "isControllerCenterArea  displayHeight: "+mDisplayHeight+"  pointerY: "+pointerY);
		}
		if(mDisplayHeight-pointerY<=(AppConstants.CONTROLER_CENTER_AREA*mDisplayDensity)){
			return true;
		}
		return false;
	}
	
	
	
	private void userActivity(MotionEvent ev){
		switch (ev.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
//			mViewMediatorCallback.userActivity();
			break;
		default:
			if (mTouchCallTime++ % 50 == 0) {
				mTouchCallTime = 1;
//				mViewMediatorCallback.userActivity();
			}
			break;
		}
	}
	
    private boolean isAtSecurity() {
//        boolean isSecurityViewAdded = mLauncherShotPage.isSecurityViewAdded();
        int xpageHeight = KWDataCache.getXPageHeight(getResources());
        int scrollY = getScrollY();
        boolean tag = scrollY >= xpageHeight /*&& isSecurityViewAdded*/;
        if (DebugLog.DEBUG) {
            DebugLog.d(LOG_TAG, "xpageHeight: " + xpageHeight + " scrollY: " + scrollY+"  isAtSecurity: "+tag);
        }
        return tag;
    }
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		int action = event.getActionMasked();
		 if(DebugLog.DEBUGMAYBE){
			 Log.d(LOG_TAG, "touchdebug--hostview--onInterceptTouchEvent:"+action);
		 }
		
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				onInterceptActionDown(event);
				break;

			case MotionEvent.ACTION_MOVE:
				confirmIfIntercept(event);
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				 if(DebugLog.DEBUGMAYBE){
					DebugLog.d(LOG_TAG, "ddddd=========:");
				}
				 if(onInterceptActionUp(event)) {
						return true;
					}
				break;
				default:
					break;
		}
		 if(DebugLog.DEBUGMAYBE){
			 Log.d("jings", "onInterceptTouchEvent return "+mScrollDirection );
		 }
	    if(mOnViewTouchListener != null){
	        mOnViewTouchListener.onInterceptTouch(event);
	    }
		return (mScrollDirection != DIRECTION_NONE);
	}
	
    private void gotoSleepIfDoubleTap(MotionEvent event) {
        if (mKeyguardPage != null) {
            if (DebugLog.DEBUG) {
                DebugLog.d(LOG_TAG, "gotoSleepIfDoubleTap workSpace!=null");
            }
            mKeyguardPage.gotoSleepIfDoubleTap(event);
        }
    }
	
	private boolean onInterceptActionDown(MotionEvent event) {
//		mLauncherShotPage.onTouchDown();
	    
		mDownMotionX = event.getX();
		mDownMotionY = event.getY();
//		mLastMotionX = mDownMotionX;
		mScrollDirection = DIRECTION_NONE;
		mScrollDirectionConfirmed = false;
		SLIDING_ENABLE = true;
		if (isTouchedInViewport((int) mDownMotionX, (int) mDownMotionY)) {
//			mTouchState = TOUCH_STATE_MOVE;
		} else {
//			mTouchState = TOUCH_STATE_REST;
		}
		return false;
	}
	
	
	private void confirmIfIntercept(MotionEvent event) {
		if(SLIDING_ENABLE){
			confirmScrollDirection(event.getY(), event.getX());
		}
	}
	
	private boolean onInterceptActionUp(MotionEvent event) {
		mScrollDirectionConfirmed = false;
//		mShouldAddLauncherShot = true;
		if(mScrollDirection == DIRECTION_UP) {
			int x = (int) event.getX();
			int y = (int) event.getY();
			if(!isTouchedInViewport(x, y)) {
				return true;
			}
		}
        releaseVelocityTracker();
        return false;
	}
	
	private void confirmScrollDirection(float currY, float currX) {
		if(DebugLog.DEBUG){
			DebugLog.d(LOG_TAG, "confirmScrollDirection...mScrollDirectionConfirmed="+mScrollDirectionConfirmed);
		}
		if(mScrollDirectionConfirmed) return;
        DebugLog.d(TAG, "confirmScrollDirection currY:"+currY);
        DebugLog.d(TAG, "confirmScrollDirection mDownMotionY:"+mDownMotionY);
        DebugLog.d(TAG, "confirmScrollDirection currX:"+currX);
        DebugLog.d(TAG, "confirmScrollDirection mDownMotionX:"+mDownMotionX);
		float deltaY = currY - mDownMotionY;
		float deltaX = currX - mDownMotionX;
		boolean angleConform = isAngleConform(deltaX, deltaY);
        DebugLog.d(TAG, "confirmScrollDirection angleConform:"+angleConform);
        DebugLog.d(TAG, "confirmScrollDirection mScrollDirectionConfirmed:"+mScrollDirectionConfirmed);
		if(angleConform){
		    if(deltaY > mMinScrollDirectionConfirmationDistance && getScrollY() >0) {
		        mScrollDirection = DIRECTION_DOWN;
		        mScrollDirectionConfirmed = true;
		        mLastMotionY = currY;
		    } else if(deltaY < -mMinScrollDirectionConfirmationDistance && isHostYAtHomePostion()) {
		        mScrollDirection = DIRECTION_UP;
		        mScrollDirectionConfirmed = true;
		        mLastMotionY = currY;
//			AmigoXPageManager.getInstance(mContext).onUnlockStart();
//			reverseToTimeWidget();
		    }
		}else{
            mScrollDirection = DIRECTION_H;
		}
		if(DebugLog.DEBUG){
			DebugLog.d(LOG_TAG, "confirmScrollDirection,deltaY"+deltaY+",Direction:"+mScrollDirection+"  getScrollY: "+getScrollY()+"   MIN_SCROLL_DIRECTION_CONFIRMATION_DISTANCE="+mMinScrollDirectionConfirmationDistance);
		}
	}
	
	private boolean isAngleConform(float x, float y){
	    DebugLog.d(TAG,"isAngleConform x:" + x);
	    DebugLog.d(TAG,"isAngleConform y:" + y);
	    if(x == 0){
	        return true;
	    }
		double tangent = y/x;
		double angle =  Math.toDegrees(Math.atan(Math.abs(tangent)));
	    DebugLog.d(TAG,"isAngleConform angle:" + angle);
		if(angle >= minAngle){
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean handled = false;
		int action = event.getActionMasked();
		if(DebugLog.DEBUGMAYBE){
			DebugLog.d(LOG_TAG, "touchdebug--hostview--onTouchEvent:"+action);
		}
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				handled = onActionDown(event);
				break;
			case MotionEvent.ACTION_MOVE:
				handled = onActionMove(event);
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				handled = onActionUp(event);
				break;
				default:
					break;
		}
		mGestureDetector.onTouchEvent(event);
		return handled;
	}
	
	private boolean onActionDown(MotionEvent me) {
		if (isScrollingFinished()) {
			mScroller.abortAnimation();
		}

		/*mLastMotionX = */mDownMotionX = me.getX();
		mLastMotionY = mDownMotionY = me.getY();
//		mLastMotionRemainderX = 0;
		mLastMotionRemainderY = 0;
        if(mOnViewTouchListener != null){
            mOnViewTouchListener.onTouch(me);
        }
		return true;
	}
	
	private boolean missCountShow = true;
	
	private boolean onActionMove(MotionEvent event) {
//		if(!mKeyguardViewManager.isShowing()){
//			return false;
//		}
		DebugLog.d(TAG,"onActionMove mScrollDirection:" + mScrollDirection);
		if(mScrollDirection == DIRECTION_NONE){
			confirmIfIntercept(event);
		}else if(mScrollDirection == DIRECTION_DOWN || mScrollDirection == DIRECTION_UP){
			
			float currY = event.getY();
			float deltaY = mLastMotionY + mLastMotionRemainderY - currY;
			obtainVelocityTracker();
			addMotionEventIntoTracker(event);
			scrollBy(0, (int) deltaY);
			mLastMotionY = currY;
			mLastMotionRemainderY = deltaY - (int) deltaY;
			
//			if(mShouldAddLauncherShot) {
//				if(DebugLog.DEBUGMAYBE){
//					DebugLog.d(LOG_TAG, "can update snapshot? " + (mLauncherShotPage != null));
//				}
//				mShouldAddLauncherShot = false;
//			}
		}
	      if(mScrollDirection == DIRECTION_H){
	            if(mOnViewTouchListener != null){
	                mOnViewTouchListener.onTouch(event);
	            }
	        }
		return true;
	}
	

	
	@Override
	public void scrollBy(int x, int y) {
		scrollTo(0, getScrollY() + y);
	}
	
	@Override
	public void scrollTo(int x, int y) {
		int adjustedY = y;
		adjustedY = Math.min(mMaxBoundY, Math.max(mMinBoundY, adjustedY));
		
		if(!missCountShow && adjustedY != mMaxBoundY){
			missCountShow = true;
		}
		
		// 姣忔棩涓�浘鏂囨
//		rotationDayPictureComment(adjustedY);
		
		super.scrollTo(x, adjustedY);
	}
	
/*	private void rotationDayPictureComment(int adjustedY){
//		if (mLinearLayoutRotationVisible) {
			if(mLinearLayoutRotation==null){
				KWWorkspace workSpace = KWDragController.getInstance().getWorkspace();
				if(workSpace!=null){
					mLinearLayoutRotation = workSpace.getComment();
				}
			}
//			DebugLog.d(LOG_TAG, "mLinearLayoutRotation----" + mLinearLayoutRotation + "====degreeex===" + mDegreeX + "==mDegreeRatio===" + mDegreeRatio);
			if(mLinearLayoutRotation!=null){
				if (adjustedY <= mDegreeX) {
					mLinearLayoutRotation.setRotationX(adjustedY / mDegreeRatio);
				}else {
					mLinearLayoutRotation.setRotationX(90);
				}
//			}
		}
	}*/
	
	private static final int FLING_NONE=-1;
	private static final int FLING_UP=0;
	private static final int FLING_DOWN=1;
	
	private boolean onActionUp(MotionEvent event) {
		mScrollDirectionConfirmed = false;
//		mShouldAddLauncherShot = true;
		
//		 if(DebugLog.DEBUGMAYBE){
//			DebugLog.d(LOG_TAG, "onActionUp=========:"+ mKeyguardViewManager.isShowing());
//		}
//		if(!mKeyguardViewManager.isShowing()) {
//			return true;
//		}
		int totalY = Math.round(mDownMotionY - event.getY());
		float velY = computeVelocityY();
		releaseVelocityTracker();
		int flingDirection = FLING_NONE;
		if(mScrollDirection == DIRECTION_UP){
			boolean velocityYUnlock = (Math.abs(velY) > mMinFlingVelocity) && velY < 0;
			if((totalY > mMinFlingDistance) || velocityYUnlock){
				flingDirection=FLING_UP;
			}else{
				flingDirection=FLING_NONE;
			}
			if(DebugLog.DEBUG){
				DebugLog.d(LOG_TAG, "totalY:  up "+totalY+" mMinFlingDistance: "+mMinFlingDistance);
			}
		}else if(mScrollDirection == DIRECTION_DOWN){
			boolean isDownFling = false;
			boolean velocitylock = (Math.abs(velY) > mMinFlingVelocity) && velY > 0;
			isDownFling=(totalY < -mMinFlingDistance) || velocitylock;
//			isUpFling = !isDownFling;
			if(isDownFling){
				flingDirection=FLING_DOWN;
			}else{
				flingDirection=FLING_NONE;
			}
			if(DebugLog.DEBUG){
				DebugLog.d(LOG_TAG, "totalY: down "+totalY+" mMinFlingDistance: "+mMinFlingDistance);
			}
		}else if(mScrollDirection == DIRECTION_H){
		    if(mOnViewTouchListener != null){
			       mOnViewTouchListener.onTouch(event);
			}
		}
		
		if(DebugLog.DEBUG){
			DebugLog.d(LOG_TAG, "onActionUp,totalY:"+totalY+",velY:"+velY+",flingDirection:"+flingDirection);
		}
		
//        if (!mIsTriggerShortcut) {
		unlockWithVelocity((int) velY, flingDirection);
//        }
//		mIsTriggerShortcut=false;
		return true;
	}
	
	private void unlockWithVelocity(int velocity, boolean isUpFling) {
		int pageId = isUpFling ? 1 : 0;
		if(pageId == 1) {
			mFlingDirection = DIRECTION_UP;
		} else {
			mFlingDirection = DIRECTION_DOWN;
		}
	
		int deltaY = getPagerOffset(pageId) - getScrollY();
		if (mFlingDirection == DIRECTION_UP) {
			deltaY = mMaxBoundY - getScrollY();
		}else if(mFlingDirection == DIRECTION_DOWN){
			if(getScrollY()>mMaxBoundY/2){
				deltaY = getScrollY();
			}else{
				deltaY =  - getScrollY();
			}
			
		}
		DebugLog.d(LOG_TAG, "deltaY:"+deltaY+",mMaxBoundY:"+mMaxBoundY+",getscrolly:"+getScrollY());
		
		int duration = calculateDurationByVelocity(velocity);
		
		if (!mScroller.isFinished()) {
			mScroller.abortAnimation();
		}
		
		if (duration <= 0) {
			duration = Math.abs(deltaY/3);
		}
		
		mScroller.startScroll(0, getScrollY(), 0, deltaY, duration);
//		mUnlockAnimStart = true;

		invalidate();
	}
	

	private void unlockWithVelocity(int velocity, int flingDirection) {
		int deltaY =0;
		if (flingDirection== FLING_UP) {
			deltaY = mMaxBoundY - getScrollY();
		}else if(flingDirection== FLING_DOWN){
				deltaY =  - getScrollY();	
		}else {
			if(mBouncerIsShowing){
				deltaY = mMaxBoundY - getScrollY();
			}else{
				deltaY =  - getScrollY();
			}
		}
		DebugLog.d(LOG_TAG, "deltaY:"+deltaY+",mMaxBoundY:"+mMaxBoundY+",getscrolly:"+getScrollY()+"mBouncerIsShowing="+mBouncerIsShowing);
		
		int duration = calculateDurationByVelocity(velocity);
		
		if (!mScroller.isFinished()) {
			mScroller.abortAnimation();
		}
		
		if (duration <= 0) {
			duration = Math.abs(deltaY/3);
		}
		
		mScroller.startScroll(0, getScrollY(), 0, deltaY, duration);
//		mUnlockAnimStart = true;

		invalidate();
	}
	
	
	private int calculateDurationByVelocity(int velocity) {
		int duration = 0;
		if(velocity != 0) {
			duration = (mViewport.height() / Math.abs(velocity)) << 9;
			duration = Math.min(duration, 300);
		}
//		duration = Math.max(100, Math.min(duration, 300));
		return duration;
	}

	private void obtainVelocityTracker() {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
	}

	private void releaseVelocityTracker() {
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
		}
		mVelocityTracker = null;
	}

	private void addMotionEventIntoTracker(MotionEvent event) {
		mVelocityTracker.addMovement(event);
	}

	private int computeVelocityY() {
		int velY = 0;
		if (mVelocityTracker != null) {
			mVelocityTracker.computeCurrentVelocity(1000);
			velY = (int) mVelocityTracker.getYVelocity();
		}

		return velY;
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			int currX = mScroller.getCurrX();
			int currY = mScroller.getCurrY();
			scrollTo(currX, currY);
			
			invalidate();
		}
	}

	private boolean isScrollingFinished() {
		int deltaY = Math.abs(mScroller.getFinalY() - mScroller.getCurrY());
		boolean finished = mScroller.isFinished() || (deltaY < mTouchSlop);
		if(DebugLog.DEBUG){
			DebugLog.d(LOG_TAG, "scrolling finished? " + finished);
		}

		return finished;
	}
	
	boolean mifLauncherInfozoneReverse = true;
	
	public void setLauncherInfozoneReverseTrue(){
		mifLauncherInfozoneReverse = true;
	}

	@SuppressWarnings("unused")
	@Override
	protected void onScrollChanged(int left, final int top, int oldLeft, int oldTop) {
		super.onScrollChanged(left, top, oldLeft, oldTop);
		
		if(top>=mMaxBoundY){
			if(DebugLog.DEBUG){
				DebugLog.d(LOG_TAG, "onScrollChanged--top>=mMaxBoundY : "+mMaxBoundY+"  isSecure: "+isSecure());
			}
			mScroller.forceFinished(true);
			if(!isSecure()){
//			    mKeyguardViewManager.destroyAcivityIfNeed();
//			        keyguardDone();
				mBouncerIsShowing=false;
			    finish();
			    	
			 }else{
			    	mKeyguardBouncer.bouncerShowing();
                    mBouncerIsShowing=true;
			    /**
			     * do it for insure the skylight will be hide
			     */
//			    mKeyguardViewManager.hideSkylightWhenKeyguardDone();
				if(mKeyguardBouncer!=null){
					mKeyguardBouncer.onResumeSecurityView(KeyguardSecurityView.KEYGUARD_HOSTVIEW_SCROLL_AT_UNLOCKH_EIGHT);
				}
			}
			missCountShow = false;
//			mKeyguardViewManager.resetUnlockWithWindowAnimFlag();
//			else{
//				mKeyguardPage.hideIndicator();
//				changeBackground(mBackgroundChangeMode,-top,1-(float)top/mMaxBoundY/1.5f);
//			}
		}else if(top <= 0){
			if(DebugLog.DEBUG){
				DebugLog.d(LOG_TAG, "onScrollChanged--top <= 0");
			}
			mScroller.forceFinished(true);
//			mKeyguardPage.showTimeInfo(true);
//			mKeyguardPage.showIndicator();
			mifLauncherInfozoneReverse = true;
			
			if(mKeyguardBouncer!=null){
				mKeyguardBouncer.onPauseSecurityView(KeyguardSecurityView.KEYGUARD_HOSTVIEW_SCROLL_AT_HOMEPAGE);
			}
			resetDisMissAction();
			mKeyguardBouncer.KeyguardShowing();
			mBouncerIsShowing=false;
		}
//		Log.e("ddd", "========-top=========" + top);
		//use owner wallpaper
		if(isSecure()){
			mUIController.onKeyguardScrollChanged(top, mMaxBoundY, UIController.SCROLL_TO_SECURTY);
		}else{
			mUIController.onKeyguardScrollChanged(top, mMaxBoundY, UIController.SCROLL_TO_UNLOCK);
		}
	}
	
	public void onScreenTurnedOff() {
		if(DebugLog.DEBUG){
			DebugLog.d(LOG_TAG, "onScreenTurnedOff");
		}
//		if(!mHasSetWallpaperOnKeyguardPage) {
//			mKeyguardPage.showWallpaper();
//		}
//		
//		clearFocus();
		if(mKeyguardPage!=null){
			mKeyguardPage.onScreenTurnedOff();
		}

//		
////		setToHomePage();
		resetDisMissAction();
		resetHostYToHomePosition();
	    mKeyguardBouncer.onScreenTurnedOff();
	    mBouncerIsShowing=false;

	}

	public void onScreenTurnedOn() {
		requestFocus();
		if(DebugLog.DEBUG){
			DebugLog.d(LOG_TAG, "onScreenTurnedOn");
		}
		if(mKeyguardPage!=null){
			mKeyguardPage.onScreenTurnedOn();
		}
		
//		boolean ignoreScrollY=mKeyguardViewManager.getIgnoreSomeScreenTurnOnFlag();
//		if(!ignoreScrollY){
//		    resetHostYToHomePosition();
//		}
		if(mKeyguardBouncer!=null){
			mKeyguardBouncer.onScreenTurnedOn();
		}
		mifLauncherInfozoneReverse = true;
	}

	public void lockNowShowHomePage(){
	    resetHostYToHomePosition();
		postDelayed(new Runnable() {
			@Override
			public void run() {
//				mKeyguardPage.showTimeInfo(true);
			}
		}, 100);
	}
	
	public void show() {
		// TODO Auto-generated method stub
		if(mKeyguardPage!=null){
			if(DebugLog.DEBUG){
				DebugLog.d(LOG_TAG, "show()");
			}
			mKeyguardPage.show();
			
		}
		if(mKeyguardBouncer!=null){
			mKeyguardBouncer.show(true);
		}
	}
	
    public void showBouncerOrKeyguard() {
        if (needsFullscreenBouncer()) {
           KeyguardViewHostManager.getInstance().cancelFingerIdentify();
            // The keyguard might be showing (already). So we need to hide it.
            showBouncer(true);
            scrollToUnlockByOther();
        } else {
            // scrollToKeyguardPage(100);
            mKeyguardBouncer.prepare();
        }

    }

	
    public void hide() {
        // TODO Auto-generated method stub
        if(mKeyguardPage!=null)mKeyguardPage.hide();
        if(mKeyguardBouncer!=null)mKeyguardBouncer.hide(false);
//        mGuestModeUtil.checkAndOpenGuestMode();
    }
	
    public void showBouncer(boolean resetSecuritySelection){
        mKeyguardBouncer.show(resetSecuritySelection);
    }
	
	public void scrollToSnapshotPage() {
		setScrollY(KWDataCache.getXPageHeight(getResources()));
	}
	
	public void scrollToKeyguardPage(int duration) {
		if(DebugLog.DEBUG){
			DebugLog.d("test_scroll", "scrollToKeyguardPage");
		}
		if (!mScroller.isFinished()) {
			mScroller.abortAnimation();
		}
		
		mScroller.startScroll(0, getScrollY(), 0, -getScrollY(), duration);
		invalidate();
	}

	public void scrollToUnlockByOther(){
//		AmigoXPageManager.getInstance(mContext).lockFromLaunchFail();
		if (!mScroller.isFinished()) {
			mScroller.abortAnimation();
		}
		if(DebugLog.DEBUG){
			DebugLog.d(LOG_TAG, "scrollToUnlockByOther()  mMaxBoundY: "+mMaxBoundY);
		}
		mScroller.startScroll(0,getScrollY() , 0, mMaxBoundY, 500);
		invalidate();
	}
	

	
	public void lockFromVerify(){
//		AmigoXPageManager.getInstance(mContext).lockFromVerify();
	}
	
	public void lockFromSystemReady(){
		if(DebugLog.DEBUG){
			DebugLog.d(LOG_TAG, "lockFromSystemReady");
		}
//		AmigoXPageManager.getInstance(mContext).lockFromSystemReady();
	}
	


	public void lockFromSkylight(){
		if(DebugLog.DEBUG){
			DebugLog.d(LOG_TAG, "lockFromSkylight");
		}
//	    AmigoXPageManager.getInstance(mContext).lockFromSkylightClose();
		resetHostYToHomePosition();
	    if(!mHasSetWallpaperOnKeyguardPage) {
			mKeyguardPage.showWallpaper();
		}
	}
	public void lockFromSimCard(long subId){
//	    AmigoSecurityManager.getInstance(mContext).addSimLock(subId);
	    if(DebugLog.DEBUG){
	    	DebugLog.d(LOG_TAG, "lockFromSimCard");
		}
	   
	}
	
	public static interface UpdateListener {
        public void onUpdate(AmigoKeyguardHostView which);
    }
	
	
	private void finish(){
	     boolean deferKeyguardDone = false;
	        if(DebugLog.DEBUG)Log.d(LOG_TAG, "KeyguardViewBase finish  mViewMediatorCallback: "+mViewMediatorCallback+"  deferKeyguardDone: "+deferKeyguardDone);
	        if (mOnDisMissAction != null) {
	            deferKeyguardDone = mOnDisMissAction.onDismiss();
	            mOnDisMissAction = null;
	        }
	        if (mViewMediatorCallback != null) {
	            if (deferKeyguardDone) {
	                mViewMediatorCallback.keyguardDonePending();
	            } else {
	                mViewMediatorCallback.keyguardDone(true);
	            }
	        }
	}

	public void keyguardDone(){
	    if(mViewMediatorCallback!=null){
	        Log.d("jings", "host view  keyguardDone");
	        mViewMediatorCallback.keyguardDone(true);
	    }
//	    boolean unlockWithWindowAnim=KeyguardViewManager.getInstance().getUnlockWithWindowAnimFlag();
	    
//	    Log.d(LOG_TAG, "unlockWithoutWindowAnim: "+unlockWithWindowAnim);
	    
	/*    if(unlockWithWindowAnim){
	        
	        try {
	            // Don't actually hide the Keyguard at the moment, wait for window
	            // manager until it tells us it's safe to do so with
	            // startKeyguardExitAnimation.
	            IWindowManager wm = WindowManagerGlobal.getWindowManagerService();
	            wm.keyguardGoingAway(
	                    true,
	                    false);
	        } catch (RemoteException e) {
	            Log.e(LOG_TAG, "Error while calling WindowManager", e);
	        }
	    }*/
		// Keyguard
		reverseToTimeWidget();
				
//		boolean unLockNowByAIDLSuccess = AmigoLauncherSnapshotManager.getInstance(mContext).showLauncherByAIDL();
//		if(DebugLog.DEBUG){
//	    	DebugLog.d(LOG_TAG, "showLauncherByAIDL unLockNowByAIDLSuccess閿涳拷"+unLockNowByAIDLSuccess);
//		}
//		if(!unLockNowByAIDLSuccess){
//			sendShowLauncher();
//		}
	    
//		if (mViewMediatorCallback != null) {
//			mViewMediatorCallback.keyguardDone(true);
//		}
		/*if(mOnDisMissAction != null){
			mOnDisMissAction.onDismiss();
		}*/
		// Keyguard
//		reverseToTimeWidget();
		// Launcher
//		reverseToWeatherWidget(mifLauncherInfozoneReverse);
		
//		if(AmigoWallpaperUtils.getInstance(mContext).isNaviLauncherAtTop()){
//			reverseToWeatherWidget(mifLauncherInfozoneReverse);
//		}else{
//			reverseToWeatherWidget(false);
//		}
		mifLauncherInfozoneReverse = false;
//		KeyguardViewManager.getInstance().setExtTurnOn(false);
	}
	
	 public boolean isSecure() {
	        return mKeyguardBouncer.isSecure();
	}
	
	private static final String ACTION_SHOWLAUNCHER = "keyguard_send_showlauncher";
	private void sendShowLauncher() {
			Intent intent = new Intent();
			intent.setAction(ACTION_SHOWLAUNCHER);
			mContext.sendBroadcast(intent);		
	}

	@SuppressLint("NewApi")
	public void notifyUnlockScenario(int scenario){
		if(DebugLog.DEBUG){
			DebugLog.d(LOG_TAG, "notifyUnlockScenario:"+scenario+",mBackground:");
		}
//		mKeyguardWallpaperManager.notifyUnlockScenario(scenario);
	}
	
	@SuppressLint("NewApi")
	public void setChangeBackgroundModeForAnima(){
//		mKeyguardWallpaperManager.setChangeBackgroundModeForAnima();
	}
	
	UIController mUIController;
	
	// use owner wallpaper
	public void changeBackground(float y,float blind,boolean isAnima){
//		mKeyguardWallpaperManager.changeBackground(y,blind,isAnima, this);
	}
	
    public void reverseToWeatherWidget(boolean mifLauncherInfozoneReverse) {
    	if(DebugLog.DEBUG){
    		DebugLog.d(LOG_TAG, "reverseToWeatherWidget()");
		}
//    	mKeyguardPage.showAllWeatherCondition(mifLauncherInfozoneReverse);
	}
    
    private void reverseToTimeWidget(){
    	if(DebugLog.DEBUG){
    		DebugLog.d(LOG_TAG, "reverseToTimeWidget()");
		}
//    	mKeyguardPage.showAllTimeInfo(false);
    }
    
    public void showTimeInfo(boolean withAnim){
    	if(DebugLog.DEBUG){
    		DebugLog.d(LOG_TAG, "showTimeInfo()");
		}
//    	mKeyguardPage.showTimeInfo(withAnim);
    }
    
//    public void showComment(WallpaperData data){
//    	mKeyguardPage.showComment(data);
//    }
    
//    public void hiddenComment(){
//    	mKeyguardPage.hiddenComment();
//    }
    
//    public void addKeyguardWallpaperManager(KeyguardWallpaperManager keyguardWallpaperManager){
//    	mKeyguardWallpaperManager=keyguardWallpaperManager;
//    }
	
	public void scrollToKeyguardPageHome() {
		if(DebugLog.DEBUG){
			DebugLog.d("test_scroll", "guo scrollToKeyguardPageByLancher");
		}
		if (!mScroller.isFinished()) {
			mScroller.abortAnimation();
		}
		mScroller.startScroll(0, getScrollY(), 0, -getScrollY(), 300);
		invalidate();
	}
	
	private OnDismissAction mOnDisMissAction;

    private OnViewTouchListener mOnViewTouchListener;
	public void dismissWithAction(OnDismissAction r){
		mOnDisMissAction = r;
		scrollToUnlockByOther();
		 if(isSecure()){
			 resetDisMissAction();
			 mKeyguardBouncer.showWithDismissAction(r);
		 }
		
	}
	
	
	public void dismiss(){
		scrollToUnlockByOther();

	}
	
    public boolean needsFullscreenBouncer() {
        return mKeyguardBouncer.needsFullscreenBouncer();
    }
	
	public void resetDisMissAction(){
		mOnDisMissAction = null;
	}
	
//    public interface OnDismissAction {
//        /**
//         * @return true if the dismiss should be deferred
//         */
//        boolean onDismiss();
//    }
    
    public boolean isHostYAtHomePostion(){
        return getScrollY() == 0;
    }
    
    public void  resetHostYToHomePosition(){
        setScrollY(0);
    }
    
    
    public long getUserActivityTimeout(){
    	return mKeyguardBouncer.getUserActivityTimeout();
    }
    
    
    public boolean keyguardBouncerIsShowing(){
    	return getScaleY()>0 && mKeyguardBouncer.isShowing();
    }
    
    public void registerBouncerCallback(KeyguardBouncerCallback bouncerCallback){
    	mKeyguardBouncer.registerBouncerCallback(bouncerCallback);
    }
    
    public boolean onBackPress(){
        UIController.getInstance().onBackPress();
    	if(mBouncerIsShowing){
    		scrollToKeyguardPage(300);
    		return true;
    	}
    	
    	return false;
    }


    public void setOnViewTouchListener(OnViewTouchListener touchListener){
        mOnViewTouchListener = touchListener;
    }
    
    private void initLongPressListener() {
        
        mGestureDetector = new GestureDetector(getContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public void onLongPress(MotionEvent event) {
                        UIController.getInstance().onLongPress(event.getX(), event.getY());
                    }
                    
                    
                    @Override
                    public boolean onDown(MotionEvent e) {
                        UIController.getInstance().onDown(e);
                        return super.onDown(e);
                    }
                });
        
        setLongClickable(true);
 
//        this.setOnTouchListener(new OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent event) {
//                 return mGestureDetector.onTouchEvent(event);
//            }
//        });
    }

    
    public void showBouncerOrKeyguardDone(){
		 if(isSecure()){
			 scrollToUnlockByOther();
			 resetDisMissAction();
		 }else{
			 finish();
		 }
    }
 
    
    public void shakeFingerIdentifyTip(){
        mKeyguardPage.shakeIdentifyTip();
    }
    
    public void fingerPrintFailed() {
        if (mKeyguardBouncer != null) {
            mKeyguardBouncer.fingerPrintFailed();
        }
    }

    public void fingerPrintSuccess() {
        if (mKeyguardBouncer != null) {
            mKeyguardBouncer.fingerPrintSuccess();
        }
    }
    
    public boolean passwordViewIsForzen(){
        if (mKeyguardBouncer != null) {
           return mKeyguardBouncer.passwordViewIsForzen();
        }
        return false;
    }

	@Override
	public void securityViewRemoveAnimationUpdating(int top ,int maxBoundY) {
		mUIController.securityViewRemoveAnimationUpdating(top, maxBoundY);
		
	}

}

