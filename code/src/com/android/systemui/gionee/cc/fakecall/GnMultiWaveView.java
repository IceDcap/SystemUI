package com.android.systemui.gionee.cc.fakecall;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;


import android.os.AsyncTask;
import android.os.Environment;
import android.view.ViewConfiguration;
import android.view.VelocityTracker;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.*;
import android.widget.ImageView.ScaleType;
import android.os.Handler;
import android.provider.Settings;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;

import android.telecom.VideoProfile;

import com.android.systemui.R;

public class GnMultiWaveView extends ViewGroup {
	private final static boolean DEBUG = true;
	private final static  String TAG = GnMultiWaveView.class.getSimpleName();
	private final static int MAX_VERTICAl_DISTANCE =  25;
	private Context mContext = null;
	private Slider mSlider = null;
	private int mScreenWidth = 0;
	private int mScreenHeight = 0;
	private int mCenterX = 0;
	private int mCenterY = 0;
	private float mOuterRadius = 0;
	private float mHitRadius = 0;
	private float mSnapMargin = 0;
	private int mCenterTap = 2;
	private static int mVibrationDuration = 0;
	private float mVerticalOffset;
	private float mHorizontalOffset;

	private int mCenterAnimResourceId = 0;
	private int mRightWaveAnimResourceId = 0;
	private int mBottomWaveAnimResourceId = 0;
	private int mLeftWaveAnimResourceId = 0;
	private int mTopWaveAnimResourceId = 0;
	private float mBottomSmsWidth = 0;
	private float mBottomSmsHeight = 0;
	private Animation mRightWaveAnim = null;
	private Animation mTopWaveAnim = null;
	private Animation mLeftWaveAnim = null;
	private Animation mBottomWaveAnim = null;

	private boolean mDragging = false;
	private boolean mLeftTarget = false;
	private boolean mTopTarget = false;
	private boolean mRightTarget = false;
	private boolean mBottomTarget = false;
	private Point mLeftTargetCenterPoint = null;
	private Point mTopTargetCenterPoint = null;
	private Point mRightTargetCenterPoint = null;
	private Point mBottomTargetCenterPoint = null;
	private Point mHandleViewCenterPoint = null;
	private Paint mPaint = null;

	private Animation mFadeOutAnim = null;
	private Animation mFadeInAnim = null;
	private Animation mHiddenAnim = null;
	private Animation mLeftFadeInAnim = null;
	private Animation mRightFadeInAnim = null;
	private Vibrator mVibrator = null;
	private boolean mActionCancel = false;
	public static final int TARGET_LEFT = 0;
	public static final int TARGET_TOP = 1;
	public static final int TARGET_RIGHT = 2;
	public static final int TARGET_BOTTOM = 3;
	private int mActiveTarget = -1;
	private int mGrabbedState;

	private int mTargetResourceId;
	private int mTargetDescriptionsResourceId;
	private int mDirectionDescriptionsResourceId;
	private ArrayList<String> mTargetDescriptions;
	private ArrayList<String> mDirectionDescriptions;
	private int mHandleDrawableResourceId = 0;
	private Drawable mHandleDrawable;
	private Drawable mTargetDrawable;

	private static final int[] STATE_ACTIVE = { android.R.attr.state_enabled,
			android.R.attr.state_active };
	private static final int[] STATE_INACTIVE = { android.R.attr.state_enabled,
			-android.R.attr.state_active };
	private static final int[] STATE_FOCUSED = { android.R.attr.state_enabled,
			android.R.attr.state_focused };
	// Wave state machine
	private static final int STATE_IDLE = 0;
	private static final int STATE_FIRST_TOUCH = 1;
	private static final int STATE_TRACKING = 2;
	private static final int STATE_SNAP = 3;
	private static final int STATE_FINISH = 4;
	private static final int RETURN_TO_HOME_DURATION = 300;
	private static final int DEFAULT_REDUNDANCY = 50;
	private Drawable mCenterAnimDrawable = null;

	private boolean mAllowMoveVibration = false;
	
	private AnswerListener mAnswerListener;
	private RippleListener mRippleListener;
	
	private RectF mRectF;
	private boolean mOnlyHorizontal;


	public GnMultiWaveView(Context context) {
		this(context, null);
	}

	public GnMultiWaveView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GnMultiWaveView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Log.d(TAG, "GnMultiWaveView Object Created-------------");
		mContext = context;

		initScreen();
		mSlider = new Slider(this);
		mLeftTargetCenterPoint = new Point();
		mTopTargetCenterPoint = new Point();
		mRightTargetCenterPoint = new Point();
		mBottomTargetCenterPoint = new Point();
		mHandleViewCenterPoint = new Point();
		initPaint();
		initAnim();
		initValues(context, attrs);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		setCenterViewAnimDrawable(R.anim.gn_fc_center_view_bg_common);
		setRightWaveViewAnimDrawable(R.anim.gn_fc_answer_indicate);
		setAlwaysShowTargets(true);
	}
	
	@Override
	public void setVisibility(int visibility) {
		super.setVisibility(visibility);
		if(visibility == View.VISIBLE) {
			showTargetViews(true);
		} else {
			reset(true);
		}
	}

	private void initValues(Context context, AttributeSet attrs) {
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.GnMultiWaveView);
		mOuterRadius = a.getDimension(
				R.styleable.GnMultiWaveView_gnOuterRadius, mOuterRadius);
		mHitRadius = a.getDimension(R.styleable.GnMultiWaveView_gnHitRadius,
				mHitRadius);
		mSnapMargin = a.getDimension(R.styleable.GnMultiWaveView_gnSnapMargin,
				mSnapMargin);
		mVibrationDuration = a.getInt(
				R.styleable.GnMultiWaveView_gnVibrationDuration,
				mVibrationDuration);
		mHorizontalOffset = a.getDimension(
				R.styleable.GnMultiWaveView_gnHorizontalOffset,
				mHorizontalOffset);
		mVerticalOffset = a.getDimension(
				R.styleable.GnMultiWaveView_gnVerticalOffset, mVerticalOffset);
		mCenterTap = a.getInt(R.styleable.GnMultiWaveView_gnCenterTap,
				mCenterTap);

		mBottomSmsWidth = a.getDimension(R.styleable.GnMultiWaveView_gnBottomWidth, 120);
		mBottomSmsHeight = a.getDimension(R.styleable.GnMultiWaveView_gnBottomHeight, 48);
		mOnlyHorizontal = a.getBoolean(R.styleable.GnMultiWaveView_gnHorizontal, false);

		TypedValue outValue = new TypedValue();
		// Set handle drawable
		if (a.getValue(R.styleable.GnMultiWaveView_gnHandleDrawable, outValue)) {
			final int resourceId = outValue.resourceId;
			if (resourceId == 0) {
				throw new IllegalStateException(
						"Must specify target descriptions");
			}
			setCenterViewDrawable(resourceId);
		}

		// Read array of target drawables
		if (a.getValue(R.styleable.GnMultiWaveView_gnTargetDrawables, outValue)) {
			final int resourceId = outValue.resourceId;
			if (resourceId == 0) {
				throw new IllegalStateException(
						"Must specify target descriptions");
			}
			internalSetTargetResources(outValue.resourceId);
		}

		// Read array of target descriptions
		if (a.getValue(R.styleable.GnMultiWaveView_gnTargetDescriptions,
				outValue)) {
			final int resourceId = outValue.resourceId;
			if (resourceId == 0) {
				throw new IllegalStateException(
						"Must specify target descriptions");
			}
			setTargetDescriptionsResourceId(resourceId);
		}

		// Read array of direction descriptions
		if (a.getValue(R.styleable.GnMultiWaveView_gnDirectionDescriptions,
				outValue)) {
			final int resourceId = outValue.resourceId;
			if (resourceId == 0) {
				throw new IllegalStateException(
						"Must specify direction descriptions");
			}
			setDirectionDescriptionsResourceId(resourceId);
		}

		// Read chevron animation drawables
		setRightWaveViewDrawable(a
				.getDrawable(R.styleable.GnMultiWaveView_gnRightChevronDrawable));
		setTopWaveViewDrawable(a
				.getDrawable(R.styleable.GnMultiWaveView_gnTopChevronDrawable));
		setLeftWaveViewDrawable(a
				.getDrawable(R.styleable.GnMultiWaveView_gnLeftChevronDrawable));
		setBottomWaveViewDrawable(a
				.getDrawable(R.styleable.GnMultiWaveView_gnBottomChevronDrawable));

		a.recycle();
		setVibrateEnabled(mVibrationDuration > 0);
	}

	private void internalSetTargetResources(int resourceId) {
		Resources res = getContext().getResources();
		TypedArray array = res.obtainTypedArray(resourceId);
		int count = array.length();
		for (int i = 0; i < count; i++) {
			switch (i) {
			case TARGET_LEFT:
				mSlider.mLeftView.setBackground(array.getDrawable(i));
				break;
			case TARGET_TOP:
//				mSlider.mTopView.setBackground(array.getDrawable(i));
				break;
			case TARGET_RIGHT:
				mSlider.mRightView.setBackground(array.getDrawable(i));
				break;
			
			case TARGET_BOTTOM:
				mSlider.mBottomView.setBackground(array.getDrawable(i));
				break;
			default:
				break;
			}
			if (null != array.getDrawable(i)) {
				mTargetDrawable = array.getDrawable(i);
			}
		}
		array.recycle();
		mTargetResourceId = resourceId;
		initViewPosition();
	}

	public void setTargetResources(int resourceId) {
		resetTargetValue();
		internalSetTargetResources(resourceId);
	}

	private void resetTargetValue() {
		mTopTarget = false;
		mLeftTarget = false;
		mRightTarget = false;
		mBottomTarget = false;
	}

	public int getTargetResourceId() {
		return mTargetResourceId;
	}

	public void setTargetDescriptionsResourceId(int resourceId) {
		mTargetDescriptionsResourceId = resourceId;
		if (mTargetDescriptions != null) {
			mTargetDescriptions.clear();
		}
	}

	public int getTargetDescriptionsResourceId() {
		return mTargetDescriptionsResourceId;
	}

	public String getTargetDescription(int index) {
		if (mTargetDescriptions == null || mTargetDescriptions.isEmpty()) {
			mTargetDescriptions = loadDescriptions(mTargetDescriptionsResourceId);
		}
		return mTargetDescriptions.get(index);
	}

	public void setDirectionDescriptionsResourceId(int resourceId) {
		mDirectionDescriptionsResourceId = resourceId;
		if (mDirectionDescriptions != null) {
			mDirectionDescriptions.clear();
		}
	}

	public int getDirectionDescriptionsResourceId() {
		return mDirectionDescriptionsResourceId;
	}

	public String getDirectionDescription(int index) {
		if (mDirectionDescriptions == null || mDirectionDescriptions.isEmpty()) {
			mDirectionDescriptions = loadDescriptions(mDirectionDescriptionsResourceId);
		}
		return mDirectionDescriptions.get(index);
	}

	private ArrayList<String> loadDescriptions(int resourceId) {
		TypedArray array = getContext().getResources().obtainTypedArray(
				resourceId);
		final int count = array.length();
		ArrayList<String> targetContentDescriptions = new ArrayList<String>(
				count);
		for (int i = 0; i < count; i++) {
			String contentDescription = array.getString(i);
			targetContentDescriptions.add(contentDescription);
		}
		array.recycle();
		return targetContentDescriptions;
	}
	private void dispatchTriggerEvent(int whichHandle) {
		LinearVibrator linearVibrator = LinearVibrator.getInstance(mContext);
		Log.d(TAG, "dispatchTriggerEvent");
		linearVibrator.vibrateForUp();
		dispatchAnswerEvent(whichHandle);
	}
	

	private void dispatchGrabbedEvent(int whichHandler) {
		vibrate();
		Log.d(TAG, "dispatchGrabbedEvent");
		LinearVibrator linearVibrator = LinearVibrator.getInstance(mContext);
		linearVibrator.vibrateForGrabbed();

	}
	
    private void dispatchAnswerEvent(int whichHandle) {
        if (mAnswerListener != null) {
            switch (whichHandle) {
                case TARGET_LEFT:
                    mAnswerListener.onDecline();
                    break;
                case TARGET_RIGHT:
                    mAnswerListener.onAnswer(VideoProfile.VideoState.AUDIO_ONLY, mContext);
                    break;
                case TARGET_BOTTOM:
                    mAnswerListener.onText();
                    break;
                default:
                    break;
            }
        }
    }

	private class AllowMoveVibrationRunnable implements Runnable {

		@Override
		public void run() {
			mAllowMoveVibration = true;
		}
	}


	private AllowMoveVibrationRunnable mAllowMoveRunnable;

	private void setGrabbedState(int newState) {
		if (mAllowMoveRunnable == null) {
			mAllowMoveRunnable = new AllowMoveVibrationRunnable();
		}
		Log.d(TAG, "setGrabbedState 1");
		if (newState != mGrabbedState) {
			if (newState != AnswerListener.NO_HANDLE) {
				// start
				Log.d(TAG, "setGrabbedState 2 mContext=" + mContext);
				LinearVibrator linearVibrator = LinearVibrator
						.getInstance(mContext);
				mAllowMoveVibration = false;
				linearVibrator.vibrateForDown();
				mHandler.removeCallbacks(mAllowMoveRunnable);
				mHandler.postDelayed(mAllowMoveRunnable, 150);
			}
			mGrabbedState = newState;
		}
	}

	private void initPaint() {
		mPaint = new Paint();
		mPaint.setStyle(Style.STROKE);
		mPaint.setAntiAlias(true);
		mPaint.setColor(Color.TRANSPARENT);
		mPaint.setStrokeWidth(100);
	}

	private void initAnim() {
		mFadeInAnim = AnimationUtils.loadAnimation(mContext, R.anim.gn_fc_fade_out);
		mFadeOutAnim = AnimationUtils.loadAnimation(mContext, R.anim.gn_fc_fade_in);
		mFadeInAnim.setFillAfter(true);
		mFadeOutAnim.setFillAfter(true);
		mFadeInAnim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				mPaint.setColor(Color.TRANSPARENT);
			}
		});

		mHiddenAnim = AnimationUtils.loadAnimation(mContext,
				R.anim.gn_fc_hidden);
		mHiddenAnim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				//hiddenTargetViews(true);
				//startCenterViewAnim();
			}
		});

		mLeftFadeInAnim = AnimationUtils.loadAnimation(mContext, R.anim.gn_fc_answer_left_fade_in);
		mRightFadeInAnim = AnimationUtils.loadAnimation(mContext, R.anim.gn_fc_answer_right_fade_in);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
	}

	private void vibrate() {
		if (mVibrator != null) {
			mVibrator.vibrate(mVibrationDuration);
		}
	}

	public void setVibrateEnabled(boolean enabled) {
		if (enabled && mVibrator == null) {
			mVibrator = (Vibrator) getContext().getSystemService(
					Context.VIBRATOR_SERVICE);
		} else {
			mVibrator = null;
		}
	}

	private void initScreen() {
		DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
		mScreenWidth = metrics.widthPixels;
		mScreenHeight = metrics.heightPixels;
		mCenterX = mScreenWidth / 2;
		mCenterY = mScreenHeight / 2;
	}

	private void initViewPosition() {
		if (mHitRadius >= mOuterRadius) {
			initScreen();
			resetRadius();
		}
        if (mHitRadius == 0 && null != mSlider.mCenterView.getBackground()) {
            mHitRadius = ((float) mSlider.mCenterView.getBackground().getIntrinsicWidth()) / 2.0f;
        }
		mCenterY = (int) mOuterRadius
				+ (mTargetDrawable == null ? 0 : mTargetDrawable
						.getIntrinsicHeight() / 2) + (int) mVerticalOffset;
		mCenterX = (mScreenHeight / 2 > mScreenWidth / 2 ? mScreenWidth / 2
								: mScreenHeight / 2) + (int) mHorizontalOffset;

		setLeftViewPosition();
		setTopViewPosition();
		setRightViewPosition();
		setBottomViewPosition();
		setCenterViewPosition();
		setRightWaveViewPosition();
		setTopWaveViewPosition();
		setLeftWaveViewPosition();
		setBottomWaveViewPosition();
	}

	private void setLeftViewPosition() {
		if (null != mSlider.mLeftView.getBackground()) {
			int left = (int) (mCenterX - mOuterRadius - mSlider.mLeftView
					.getBackground().getIntrinsicWidth() / 2);
			int top = mCenterY
					- mSlider.mLeftView.getBackground().getIntrinsicHeight()
					/ 2;
			int right = (int) (mCenterX - mOuterRadius + mSlider.mLeftView
					.getBackground().getIntrinsicWidth() / 2);
			int bottom = mCenterY
					+ mSlider.mLeftView.getBackground().getIntrinsicHeight()
					/ 2;
			mSlider.mLeftView.layout(left, top, right, bottom);
			mLeftTargetCenterPoint.set((left + right) / 2, (top + bottom) / 2);
			mLeftTarget = true;
		}
	}

	private void setTopViewPosition() {
		if (null != mSlider.mTopView.getBackground()) {
			int left = mCenterX
					- mSlider.mTopView.getBackground().getIntrinsicWidth() / 2;
			int top = (int) (mCenterY - mOuterRadius - mSlider.mTopView
					.getBackground().getIntrinsicHeight() / 2);
			int right = mCenterX
					+ mSlider.mTopView.getBackground().getIntrinsicWidth() / 2;
			int bottom = (int) (mCenterY - mOuterRadius + mSlider.mTopView
					.getBackground().getIntrinsicHeight() / 2);
			mSlider.mTopView.layout(left, top, right, bottom);
			mTopTargetCenterPoint.set((left + right) / 2, (top + bottom) / 2);
			mTopTarget = true;
		}
	}

	private void setRightViewPosition() {
		if (null != mSlider.mRightView.getBackground()) {
			int left = (int) (mCenterX + mOuterRadius - mSlider.mRightView
					.getBackground().getIntrinsicWidth() / 2);
			int top = mCenterY
					- mSlider.mRightView.getBackground().getIntrinsicHeight()
					/ 2;
			int right = (int) (mCenterX + mOuterRadius + mSlider.mRightView
					.getBackground().getIntrinsicWidth() / 2);
			int bottom = mCenterY
					+ mSlider.mRightView.getBackground().getIntrinsicHeight()
					/ 2;
			mSlider.mRightView.layout(left, top, right, bottom);
			mRightTargetCenterPoint.set((left + right) / 2, (top + bottom) / 2);
			mRightTarget = true;
		}
	}

	private void setBottomViewPosition() {
		int width = (int)mBottomSmsWidth;
		int height = (int)mBottomSmsHeight;
		int left = mCenterX
				- width / 2;
		int top = (int) (mCenterY + mOuterRadius - height / 2);
		int right = mCenterX
				+ width / 2;
		int bottom = (int) (mCenterY + mOuterRadius + height / 2);
		mSlider.mBottomView.layout(left, top, right, bottom);
		mBottomTargetCenterPoint
				.set((left + right) / 2, (top + bottom) / 2);
		mBottomTarget = true;
	}

	private void setCenterViewPosition() {
		if (null != mSlider.mCenterView.getBackground()) {
			int left = mCenterX
					- mSlider.mCenterView.getBackground().getIntrinsicWidth()
					/ 2;
			int top = mCenterY
					- mSlider.mCenterView.getBackground().getIntrinsicHeight()
					/ 2;
			int right = mCenterX
					+ mSlider.mCenterView.getBackground().getIntrinsicWidth()
					/ 2;
			int bottom = mCenterY
					+ mSlider.mCenterView.getBackground().getIntrinsicHeight()
					/ 2;
			mSlider.mCenterView.layout(left, top, right, bottom);
			mHandleViewCenterPoint.set((left + right) / 2, (top + bottom) / 2);
		}
	}

	private void setRightWaveViewPosition() {
		if (null != mSlider.mRightWaveView.getBackground()) {
			if (mCenterTap == 0) {
				mCenterTap = 2;
			}
			int offset = (mSlider.mRightView.getLeft() - mSlider.mCenterView.getRight() -
					mSlider.mRightWaveView.getBackground().getIntrinsicWidth()) / 10; 
			int left = mSlider.mCenterView.getRight() + offset * 9 ;
//			int left = mSlider.mCenterView.getBackground() == null ? mCenterX
//					: mCenterX
//							+ mSlider.mCenterView.getBackground()
//									.getIntrinsicWidth() / mCenterTap;
			int top = mCenterY
					- mSlider.mRightWaveView.getBackground()
							.getIntrinsicHeight() / 2;
//			int right = (mSlider.mCenterView.getBackground() == null ? mCenterX
//					: mCenterX
//							+ mSlider.mCenterView.getBackground()
//									.getIntrinsicWidth() / mCenterTap)
//					+ mSlider.mRightWaveView.getBackground()
//							.getIntrinsicWidth();
			int right = mSlider.mRightView.getLeft() - offset;
			int bottom = mCenterY
					+ mSlider.mRightWaveView.getBackground()
							.getIntrinsicHeight() / 2;
			mSlider.mRightWaveView.layout(left, top, right, bottom);
		}
	}

	private void setTopWaveViewPosition() {
		if (mCenterTap == 0) {
			mCenterTap = 2;
		}
		if (null != mSlider.mTopWaveView.getBackground()) {
			int left = mCenterX
					- mSlider.mTopWaveView.getBackground().getIntrinsicWidth()
					/ 2;
			int top = (mSlider.mCenterView.getBackground() == null ? mCenterY
					: mCenterY
							- mSlider.mCenterView.getBackground()
									.getIntrinsicHeight() / mCenterTap)
					- mSlider.mTopWaveView.getBackground().getIntrinsicHeight();
			int right = mCenterX
					+ mSlider.mTopWaveView.getBackground().getIntrinsicWidth()
					/ 2;
			int bottom = mSlider.mCenterView.getBackground() == null ? mCenterY
					: mCenterY
							- mSlider.mCenterView.getBackground()
									.getIntrinsicHeight() / mCenterTap;
			mSlider.mTopWaveView.layout(left, top, right, bottom);
		}
	}

	private void setLeftWaveViewPosition() {
		if (mCenterTap == 0) {
			mCenterTap = 2;
		}
		if (null != mSlider.mLeftWaveView.getBackground()) {
			int left = (mSlider.mCenterView.getBackground() == null ? mCenterX
					: mCenterX
							- mSlider.mCenterView.getBackground()
									.getIntrinsicWidth() / mCenterTap)
					- mSlider.mLeftWaveView.getBackground().getIntrinsicWidth();
			int top = mCenterY
					- mSlider.mLeftWaveView.getBackground()
							.getIntrinsicHeight() / 2;
			int right = mSlider.mCenterView.getBackground() == null ? mCenterX
					: mCenterX
							- mSlider.mCenterView.getBackground()
									.getIntrinsicWidth() / mCenterTap;
			int bottom = mCenterY
					+ mSlider.mLeftWaveView.getBackground()
							.getIntrinsicHeight() / 2;
			mSlider.mLeftWaveView.layout(left, top, right, bottom);
		}
	}

	private void setBottomWaveViewPosition() {
		if (mCenterTap == 0) {
			mCenterTap = 2;
		}
		if (null != mSlider.mBottomWaveView.getBackground()) {
			int left = mCenterX
					- mSlider.mBottomWaveView.getBackground()
							.getIntrinsicWidth() / 2;
			int top = mSlider.mCenterView.getBackground() == null ? mCenterY
					: mCenterY
							+ mSlider.mCenterView.getBackground()
									.getIntrinsicHeight() / mCenterTap;
			int right = mCenterX
					+ mSlider.mBottomWaveView.getBackground()
							.getIntrinsicWidth() / 2;
			int bottom = (mSlider.mCenterView.getBackground() == null ? mCenterY
					: mCenterY
							+ mSlider.mCenterView.getBackground()
									.getIntrinsicHeight() / mCenterTap)
					+ mSlider.mBottomWaveView.getBackground()
							.getIntrinsicHeight();
			mSlider.mBottomWaveView.layout(left, top, right, bottom);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
        if (!mAlwaysShowTargets) {
            canvas.drawCircle(mCenterX, mCenterY, mOuterRadius, mPaint);
        }
        drawSingleCircle(canvas);
		super.onDraw(canvas);
	}

	public void setOuterRadius(int outRadius) {
		mOuterRadius = outRadius;
		initViewPosition();
	}

	public void setHitRadius(int innerRadisu) {
		mHitRadius = innerRadisu;
		initViewPosition();
	}

	public void resetRadius() {
		if (mScreenHeight > mScreenWidth) {
			if (null != mSlider.mLeftView.getBackground()) {
				mOuterRadius = mCenterX
						- mSlider.mLeftView.getBackground().getIntrinsicWidth()
						/ 2;
			} else {
				mOuterRadius = mCenterX - DEFAULT_REDUNDANCY;
			}
		} else {
			if (null != mSlider.mTopView.getBackground()) {
				mOuterRadius = mCenterY
						- mSlider.mTopView.getBackground().getIntrinsicHeight()
						/ 2;
			} else {
				mOuterRadius = mCenterY - DEFAULT_REDUNDANCY;
			}
		}
		if (null != mSlider.mCenterView.getBackground()) {
			mHitRadius = ((float) mSlider.mCenterView.getBackground().getIntrinsicWidth()) / 2.0f;
		}
		mSnapMargin = mOuterRadius / 2;
	}

	public void setCenterViewDrawable(int resource) {
		mSlider.mCenterView.setBackground(null);
		mHandleDrawableResourceId = resource;
		mSlider.mCenterView.setBackgroundResource(resource);
		mHandleDrawable = mSlider.mCenterView.getBackground();
		initViewPosition();
	}

	public void setCenterViewDrawable(Drawable drawable) {
		if (null != drawable) {
			mHandleDrawable = drawable;
			initViewPosition();
		}
	}

	public void setRightWaveViewDrawable(Drawable drawable) {
		mSlider.mRightWaveView.setBackground(null);
		mSlider.mRightWaveView.setBackground(drawable);
		mSlider.mRightWaveView.setVisibility(View.GONE);
		initViewPosition();
	}

	public void setRightWaveViewAnimDrawable(int resource) {
		mSlider.mRightWaveView.setBackground(null);
		mRightWaveAnimResourceId = resource;
		mSlider.mRightWaveView.setBackgroundResource(resource);
		mSlider.mRightWaveView.setVisibility(View.GONE);
		initViewPosition();
	}

	public void setTopWaveViewDrawable(Drawable drawable) {
		mSlider.mTopWaveView.setBackground(null);
		mSlider.mTopWaveView.setBackground(drawable);
		mSlider.mTopWaveView.setVisibility(View.GONE);
		initViewPosition();
	}

	public void setTopWaveViewAnimDrawable(int resource) {
		mSlider.mTopWaveView.setBackground(null);
		mTopWaveAnimResourceId = resource;
		mSlider.mTopWaveView.setBackgroundResource(resource);
		mSlider.mTopWaveView.setVisibility(View.GONE);
		initViewPosition();
	}

	public void setLeftWaveViewDrawable(Drawable drawable) {
		mSlider.mLeftWaveView.setBackground(null);
		mSlider.mLeftWaveView.setBackground(drawable);
		mSlider.mLeftWaveView.setVisibility(View.GONE);
		initViewPosition();
	}

	public void setLeftWaveViewAnimDrawable(int resource) {
		mSlider.mLeftWaveView.setBackground(null);
		mLeftWaveAnimResourceId = resource;
		mSlider.mLeftWaveView.setBackgroundResource(resource);
		mSlider.mLeftWaveView.setVisibility(View.GONE);
		initViewPosition();
	}

	public void setBottomWaveViewDrawable(Drawable drawable) {
		mSlider.mBottomWaveView.setBackground(null);
		mSlider.mBottomWaveView.setBackground(drawable);
		mSlider.mBottomWaveView.setVisibility(View.GONE);
		initViewPosition();
	}

	public void setBottomWaveViewAnimDrawable(int resource) {
		mSlider.mBottomWaveView.setBackground(null);
		mBottomWaveAnimResourceId = resource;
		mSlider.mBottomWaveView.setBackgroundResource(resource);
		mSlider.mBottomWaveView.setVisibility(View.GONE);
		initViewPosition();
	}


	public void setCenterViewAnimDrawable(int resource) {
		mSlider.mCenterView.setBackground(null);
		mCenterAnimResourceId = resource;
		mSlider.mCenterView.setBackgroundResource(resource);
		mCenterAnimDrawable = mSlider.mCenterView.getBackground();
		initViewPosition();
		if (null != mSlider.mCenterView.getBackground()
				&& mSlider.mCenterView.getBackground() instanceof AnimationDrawable) {
			AnimationDrawable anim = (AnimationDrawable) mSlider.mCenterView
					.getBackground();
			anim.stop();
			anim.start();
		}
	}

	public void  setCenterViewAnimDrawable(Drawable animDrawable) {
		mSlider.mCenterView.setBackground(null);
		mCenterAnimDrawable = animDrawable;
		mSlider.mCenterView.setBackground(animDrawable);
		initViewPosition();
		if (null != mSlider.mCenterView.getBackground()
				&& mSlider.mCenterView.getBackground() instanceof AnimationDrawable) {
			AnimationDrawable anim = (AnimationDrawable) mSlider.mCenterView
					.getBackground();
			anim.stop();
			anim.start();
		}
	}


	private void stopCenterViewAnim() {
		Log.i(TAG,
				"GnMultiWaveView Objece stopCenterViewAnim()----------------");
		if (mSlider.mCenterView.getBackground() instanceof AnimationDrawable) {
			AnimationDrawable anim = (AnimationDrawable) mSlider.mCenterView
					.getBackground();
			anim.stop();
//			if (null != mHandleDrawable) {
//				setCenterViewDrawable(mHandleDrawable);
//			}
		}
	}

	private void stopRightWaveViewAnim() {
		if (!mRightTarget) {
			return;
		}
		if (null != mSlider.mRightWaveView.getBackground()
				&& mSlider.mRightWaveView.getBackground() instanceof AnimationDrawable) {
			AnimationDrawable anim = (AnimationDrawable) mSlider.mRightWaveView
					.getBackground();
			if (anim.isRunning()) {
				anim.stop();
			}

		}
	}

	private void startCenterViewAnim() {
		if (null != mCenterAnimDrawable) {
			setCenterViewAnimDrawable(mCenterAnimDrawable);
		} else
		if (mCenterAnimResourceId != 0) {
			setCenterViewAnimDrawable(mCenterAnimResourceId);
		} else {
			if (null != mHandleDrawable) {
				setCenterViewDrawable(mHandleDrawable);
			}
		}
	}

	private void startRightWaveViewAnim() {
		if (!mRightTarget) {
			return;
		}
		mSlider.mRightWaveView.setVisibility(View.VISIBLE);
		if (null != mSlider.mRightWaveView.getBackground()
				&& mSlider.mRightWaveView.getBackground() instanceof AnimationDrawable) {
			AnimationDrawable anim = (AnimationDrawable) mSlider.mRightWaveView
					.getBackground();
			if(!anim.isRunning()) {
				anim.start();
			}
			
		} else if (null != mRightWaveAnim) {
			if(!mRightWaveAnim.hasStarted()) {
				mSlider.mRightWaveView.startAnimation(mRightWaveAnim);
			}
			
		} else {
			mSlider.mRightWaveView.setVisibility(View.VISIBLE);
		}
	}

	private void stopTopWaveViewAnim() {
		if (!mTopTarget) {
			return;
		}
		if (null != mSlider.mTopWaveView.getBackground()
				&& mSlider.mTopWaveView.getBackground() instanceof AnimationDrawable) {
			AnimationDrawable anim = (AnimationDrawable) mSlider.mTopWaveView
					.getBackground();
			anim.stop();
		}
		mSlider.mTopWaveView.setVisibility(View.GONE);
	}

	private void startTopWaveViewAnim() {
		if (!mTopTarget) {
			return;
		}
		mSlider.mTopWaveView.setVisibility(View.VISIBLE);
		if (null != mSlider.mTopWaveView.getBackground()
				&& mSlider.mTopWaveView.getBackground() instanceof AnimationDrawable) {
			AnimationDrawable anim = (AnimationDrawable) mSlider.mTopWaveView
					.getBackground();
			anim.start();
		} else if (null != mTopWaveAnim) {
			mSlider.mTopWaveView.startAnimation(mTopWaveAnim);
		} else {
			mSlider.mTopWaveView.setVisibility(View.GONE);
		}
	}

	private void stopLeftWaveViewAnim() {
		if (!mLeftTarget) {
			return;
		}
		if (null != mSlider.mLeftWaveView.getBackground()
				&& mSlider.mLeftWaveView.getBackground() instanceof AnimationDrawable) {
			AnimationDrawable anim = (AnimationDrawable) mSlider.mLeftWaveView
					.getBackground();
			anim.stop();
		}
		mSlider.mLeftWaveView.setVisibility(View.GONE);
	}

	private void startLeftWaveViewAnim() {
		if (!mLeftTarget) {
			return;
		}
		mSlider.mLeftWaveView.setVisibility(View.VISIBLE);
		if (null != mSlider.mLeftWaveView.getBackground()
				&& mSlider.mLeftWaveView.getBackground() instanceof AnimationDrawable) {
			AnimationDrawable anim = (AnimationDrawable) mSlider.mLeftWaveView
					.getBackground();
			anim.start();
		} else if (null != mLeftWaveAnim) {
			mSlider.mLeftWaveView.startAnimation(mLeftWaveAnim);
		} else {
			mSlider.mLeftWaveView.setVisibility(View.GONE);
		}
	}

	private void stopBottomWaveViewAnim() {
		if (!mBottomTarget) {
			return;
		}
		if (null != mSlider.mBottomWaveView.getBackground()
				&& mSlider.mBottomWaveView.getBackground() instanceof AnimationDrawable) {
			AnimationDrawable anim = (AnimationDrawable) mSlider.mBottomWaveView
					.getBackground();
			anim.stop();
		}
		mSlider.mBottomWaveView.setVisibility(View.GONE);
	}

	private void startBottomWaveViewAnim() {
		if (!mBottomTarget) {
			return;
		}
		mSlider.mBottomWaveView.setVisibility(View.VISIBLE);
		if (null != mSlider.mBottomWaveView.getBackground()
				&& mSlider.mBottomWaveView.getBackground() instanceof AnimationDrawable) {
			AnimationDrawable anim = (AnimationDrawable) mSlider.mBottomWaveView
					.getBackground();
			anim.start();
		} else if (null != mBottomWaveAnim) {
			mSlider.mBottomWaveView.startAnimation(mBottomWaveAnim);
		} else {
			mSlider.mBottomWaveView.setVisibility(View.GONE);
		}
	}

	private void showTargetViews(boolean anim) {
		mSlider.mLeftView.setVisibility(View.VISIBLE);
		mSlider.mTopView.setVisibility(View.VISIBLE);
		mSlider.mRightView.setVisibility(View.VISIBLE);
		mSlider.mBottomView.setVisibility(View.VISIBLE);
		mSlider.mCenterView.setVisibility(View.VISIBLE);
		if (anim) {
			mSlider.mLeftView.startAnimation(mLeftFadeInAnim);
			mSlider.mTopView.startAnimation(mFadeOutAnim);
			mSlider.mRightView.startAnimation(mRightFadeInAnim);
			mSlider.mBottomView.startAnimation(mFadeOutAnim);
			mSlider.mCenterView.startAnimation(mFadeOutAnim);
		}
		startRightWaveViewAnim();
		
		mPaint.setColor(Color.LTGRAY);
	}

	private void hiddenTargetViews(boolean anim) {
		mSlider.mLeftView.setVisibility(View.INVISIBLE);
		mSlider.mTopView.setVisibility(View.INVISIBLE);
		mSlider.mRightView.setVisibility(View.INVISIBLE);
		mSlider.mBottomView.setVisibility(View.INVISIBLE);
		if (anim) {
			mSlider.mLeftView.startAnimation(mFadeInAnim);
			mSlider.mTopView.startAnimation(mFadeInAnim);
			mSlider.mRightView.startAnimation(mFadeInAnim);
			mSlider.mBottomView.startAnimation(mFadeInAnim);
		}

		mPaint.setColor(Color.TRANSPARENT);
		if (mRippleListener != null) {
			mRippleListener.stopRipple();
		}
		stopRightWaveViewAnim();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getAction();

		boolean handled = false;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (mOnlyHorizontal) {
                    mRectF = new RectF();
                    mRectF.offsetTo(event.getX(), event.getY());
                }
                handleDown(event);
                handled = true;
                break;

            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "ACTION_MOVE");
                if (mDragging) {
                    mVelocityTracker.addMovement(event);
                    LinearVibrator linearVibrator = LinearVibrator.getInstance(mContext);
                    if (linearVibrator.isAvaliable()) {
                        float[] velocityXY = new float[2];
                        linearVibrator.getCurrentVelocity(velocityXY, 0, 255, 0, -255, mContext,
                                mVelocityTracker);
                        Log.d(TAG, "velocityXY=" + Arrays.toString(velocityXY));
                        final float vx = velocityXY[0];
                        final float vy = velocityXY[1];
                        int speed = (int) Math.sqrt(((vx * vx) + (vy * vy)));
                        if (speed > 255) {
                            speed = 255;
                        }
                        if (mAllowMoveVibration) {
                            linearVibrator.vibrateForMove(speed);
                        }
                    }
                }
                handleMove(event);
                handled = true;

                break;

            case MotionEvent.ACTION_UP:
                Log.d(TAG, "ACTION_UP");
                mVelocityTracker.clear();
                mActionCancel = false;
                handleMove(event);
                handleUp(event);
                handled = true;
                mRectF = null;
                break;

            case MotionEvent.ACTION_CANCEL:
                Log.d(TAG, "ACTION_CANCEL");
                mVelocityTracker.clear();
                mActionCancel = true;
                handleMove(event);
                handleUp(event);
                handled = true;
                mRectF = null;
                break;
            default:
                break;
        }
		return handled ? true : super.onTouchEvent(event);
	}

	private void handleDown(MotionEvent event) {
		if (!trySwitchToFirstTouchState(event)) {
			mDragging = false;
		}
	}

	private void handleMove(MotionEvent event) {
		if (!mDragging) {
			trySwitchToFirstTouchState(event);
			return;
		}
		stopCenterViewAnim();
		if (mRippleListener != null) {
			mRippleListener.stopRipple();
		}
		stopRightWaveViewAnim();
		if (null != mHandleDrawable) {
			setState(mHandleDrawable, STATE_ACTIVE);
		}

		addMoveStrokes(event);
		int maxDistance = (MAX_VERTICAl_DISTANCE > mOuterRadius/2 ? MAX_VERTICAl_DISTANCE : (int)mOuterRadius/2);
		if (mRectF != null && mRectF.height() >= maxDistance){
			return;
		}
		moveHandleTo(event.getX(), event.getY());
	}


	private void addMoveStrokes(MotionEvent event) {
		if (mOnlyHorizontal) {
			int size = event.getHistorySize();
			for (int i = 0; i < size; i++) {
				float x = event.getHistoricalX(i);
				float y = event.getHistoricalY(i);
				mRectF.union(x, y);
			}
		}
	}



	private void handleUp(MotionEvent event) {
		if (mActiveTarget == -1) {
			if (mDragging) {
				Animation tranAnim = getTranslateAnim(event.getX(),
						event.getY());
				tranAnim.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {

					}

					@Override
					public void onAnimationRepeat(Animation animation) {

					}

					@Override
					public void onAnimationEnd(Animation animation) {
                        mSlider.mCenterView.clearAnimation();
						switchToState(STATE_FINISH);
						moveHandleTo(mCenterX, mCenterY);
						mHandler.sendEmptyMessageDelayed(1, DEFAULT_REDUNDANCY);
					}
				});
                tranAnim.setFillAfter(true);
				mSlider.mCenterView.startAnimation(tranAnim);
			} else {
			}
		} else {
			mSlider.mCenterView.startAnimation(mHiddenAnim);
			hiddenTargetViews(true);
			mPaint.setColor(Color.TRANSPARENT);
			invalidate();
			setTargetsState(mActiveTarget, STATE_ACTIVE);
			mHandler.sendEmptyMessageDelayed(0, DEFAULT_REDUNDANCY);
		}
		setGrabbedState(AnswerListener.NO_HANDLE);
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				dispatchTriggerEvent(mActiveTarget);
				mActiveTarget = -1;
				break;

			case 1:
				startCenterViewAnim();
				if (mRippleListener != null) {
					mRippleListener.startRipple();
				}
				startRightWaveViewAnim();
				break;

			default:
				break;
			}
		}
	};


	private Animation getTranslateAnim(float x, float y) {
		Animation anim = new TranslateAnimation(0, mCenterX
				- mHandleViewCenterPoint.x, 0, mCenterY
				- mHandleViewCenterPoint.y);
		anim.setDuration(RETURN_TO_HOME_DURATION);
		return anim;
	}

	private boolean trySwitchToFirstTouchState(MotionEvent event) {
		final float x = event.getX();
		final float y = event.getY();
		final float dx = x - mCenterX;
		final float dy = y - mCenterY;

		if (dy >= 100) {
			return false;
		}
		if (null != mSlider.mCenterView.getBackground()
				&& Math.abs(dx) <= mHitRadius && Math.abs(dy) <= mHitRadius) {
			stopCenterViewAnim();
			switchToState(STATE_FIRST_TOUCH);
			setGrabbedState(AnswerListener.CENTER_HANDLE);
			moveHandleTo(x, y);
			if (mRippleListener != null) {
				mRippleListener.stopRipple();
			}
			stopRightWaveViewAnim();
			mDragging = true;
			return true;
		}
		return false;
	}

	private void moveHandleTo(float x, float y) {
		isOutOfCircle(x, y);
		int activeTarget = -1;
		if (mLeftTarget) {
			if (isNearTarget(mLeftTargetCenterPoint)) {
				activeTarget = TARGET_LEFT;
			}
		}
		if (mTopTarget) {
			if (isNearTarget(mTopTargetCenterPoint)) {
				activeTarget = TARGET_TOP;
			}
		}
		if (mRightTarget) {
			if (isNearTarget(mRightTargetCenterPoint)) {
				activeTarget = TARGET_RIGHT;
			}
		}
		if (mBottomTarget) {
			if (isNearTarget(mBottomTargetCenterPoint)) {
				activeTarget = TARGET_BOTTOM;
			}
		}

		if (mActiveTarget != activeTarget && activeTarget != -1) {
			dispatchGrabbedEvent(activeTarget);
		}
		if (mActiveTarget != activeTarget) {
			setTargetsState(activeTarget, STATE_FOCUSED);
		}
		mActiveTarget = activeTarget;
		if (null != mSlider.mCenterView.getBackground()) {
			mSlider.mCenterView.layout(mHandleViewCenterPoint.x
					- mSlider.mCenterView.getBackground().getIntrinsicWidth()
					/ 2, mHandleViewCenterPoint.y
					- mSlider.mCenterView.getBackground().getIntrinsicHeight()
					/ 2, mHandleViewCenterPoint.x
					+ mSlider.mCenterView.getBackground().getIntrinsicWidth()
					/ 2, mHandleViewCenterPoint.y
					+ mSlider.mCenterView.getBackground().getIntrinsicHeight()
					/ 2);
		} else {
			mSlider.mCenterView.layout(mHandleViewCenterPoint.x,
					mHandleViewCenterPoint.y, mHandleViewCenterPoint.x,
					mHandleViewCenterPoint.y);
		}
	}

	private boolean isNearTarget(Point targetCenterPoint) {
		final double xZ = Math.pow(
				Math.abs(mHandleViewCenterPoint.x - targetCenterPoint.x), 2);
		final double yZ = Math.pow(
				Math.abs(mHandleViewCenterPoint.y - targetCenterPoint.y), 2);
		final double innerRZ = Math.pow(mHitRadius, 2);
		if (yZ + xZ <= innerRZ) {
			mHandleViewCenterPoint
					.set(targetCenterPoint.x, targetCenterPoint.y);
			return true;
		}
		return false;
	}

	private boolean isOutOfCircle(float x, float y) {
		final double xz = Math.pow(Math.abs(x - mCenterX), 2);
		final double yz = Math.pow(Math.abs(mCenterY - y), 2);
		final double rz = Math.pow(mOuterRadius, 2);
		if (xz + yz > rz) {
			double dis = Math.sqrt(xz + yz);
			double centerX = (x - mCenterX) / dis * mOuterRadius + mCenterX;
			double centerY = (y - mCenterY) / dis * mOuterRadius + mCenterY;
			mHandleViewCenterPoint.set((int) centerX, (int) centerY);
			return true;
		}
		mHandleViewCenterPoint.set((int) x, (int) y);
		return false;
	}

	public void reset(boolean animate) {
		mGrabbedState = AnswerListener.NO_HANDLE;
		setTargetsState(-1, STATE_INACTIVE);
		initViewPosition();
		hiddenTargetViews(false);
	}

	private void switchToState(int state) {
        switch (state) {
            case STATE_IDLE:
                break;
            case STATE_FIRST_TOUCH:
                setTargetsState(-1, STATE_INACTIVE);
                if (null != mHandleDrawable) {
                    setState(mHandleDrawable, STATE_ACTIVE);
                }
                break;
            case STATE_TRACKING:
                break;
            case STATE_SNAP:
                break;
            case STATE_FINISH:
                if (null != mHandleDrawable) {
                    setState(mHandleDrawable, STATE_INACTIVE);
                }
                break;
            default:
                break;
        }
	}

	private void setTargetsState(int target, int[] state) {
		if (target == TARGET_LEFT) {
			if (null != mSlider.mLeftView.getBackground()) {
				if (hasState(mSlider.mLeftView.getBackground(), state)) {
					setState(mSlider.mLeftView.getBackground(), state);
				} else {
					setState(mSlider.mLeftView.getBackground(), STATE_INACTIVE);
				}
			}
		} else {
			if (null != mSlider.mLeftView.getBackground()) {
				setState(mSlider.mLeftView.getBackground(), STATE_INACTIVE);
			}
		}

		if (target == TARGET_TOP) {
			if (null != mSlider.mTopView.getBackground()) {
				if (hasState(mSlider.mTopView.getBackground(), state)) {
					setState(mSlider.mTopView.getBackground(), state);
				} else {
					setState(mSlider.mTopView.getBackground(), STATE_INACTIVE);
				}
			}
		} else {
			if (null != mSlider.mTopView.getBackground()) {
				setState(mSlider.mTopView.getBackground(), STATE_INACTIVE);
			}
		}

		if (target == TARGET_RIGHT) {
			if (null != mSlider.mRightView.getBackground()) {
				if (hasState(mSlider.mRightView.getBackground(), state)) {
					setState(mSlider.mRightView.getBackground(), state);
				} else {
					setState(mSlider.mRightView.getBackground(), STATE_INACTIVE);
				}
			}
		} else {
			if (null != mSlider.mRightView.getBackground()) {
				setState(mSlider.mRightView.getBackground(), STATE_INACTIVE);
			}
		}

		if (target == TARGET_BOTTOM) {
			if (null != mSlider.mBottomView.getBackground()) {
				if (hasState(mSlider.mBottomView.getBackground(), state)) {
					setState(mSlider.mBottomView.getBackground(), state);
				} else {
					setState(mSlider.mBottomView.getBackground(),
							STATE_INACTIVE);
				}
			}
		} else {
			if (null != mSlider.mBottomView.getBackground()) {
				setState(mSlider.mBottomView.getBackground(), STATE_INACTIVE);
			}
		}
		mActiveTarget = target;
	}

	private void setState(Drawable drawable, int[] state) {
		if (drawable instanceof StateListDrawable) {
			StateListDrawable d = (StateListDrawable) drawable;
			if (d.getState() != state) {
				d.setState(state);
			}
		}
	}

	private boolean hasState(Drawable drawable, int[] state) {
		if (drawable instanceof StateListDrawable) {
			StateListDrawable d = (StateListDrawable) drawable;
			return d.getStateDrawableIndex(state) != -1;
		}
		
		return false;
	}

	public void release() {
		Log.i(TAG, "GnMultiWaveView Object Relesed-------------");
		try {
			mVelocityTracker.recycle();
		}  catch (Exception e) {
			Log.e(TAG, "release().. e =" + e.toString());
		}

		stopCenterViewAnim();
		mHandleDrawable = null;
		mTargetDrawable = null;
		mCenterAnimDrawable = null;
		mSlider.mCenterView.setBackground(null);
		mSlider.mLeftView.setBackground(null);
		mSlider.mTopView.setBackground(null);
		mSlider.mRightView.setBackground(null);
		mSlider.mBottomView.setBackground(null);
		mSlider.mLeftWaveView.setBackground(null);
		mSlider.mTopWaveView.setBackground(null);
		mSlider.mRightWaveView.setBackground(null);
		mSlider.mBottomWaveView.setBackground(null);
		if (mRippleListener != null) {
			mRippleListener.stopRipple();
		}
		stopRightWaveViewAnim();

		mFadeInAnim = null;
		mFadeOutAnim = null;
		mLeftFadeInAnim = null;
		mRightFadeInAnim = null;
		mHiddenAnim= null;
		mLeftWaveAnim = null;
		mTopWaveAnim = null;
		mRightWaveAnim = null;
		mBottomWaveAnim = null;
	}

	public void startAnimation() {
		Log.i(TAG, "GnMultiWaveView Object startAnimation()-------------");
		startCenterViewAnim();
	}

	public void stopAnimation() {
		Log.i(TAG, "GnMultiWaveView Object stopAnimation()-------------");
		if (mRightFadeInAnim != null) {
			mRightFadeInAnim.cancel();
		}
		if (mLeftFadeInAnim != null) {
			mLeftFadeInAnim.cancel();
		}
	}

	private void stopTurnOffCentViewAnim() {
		Log.i(TAG,
				"GnMultiWaveView Object stopTurnOffCentViewAnim()-------------");
		if (mSlider.mCenterView.getBackground() instanceof AnimationDrawable) {
			AnimationDrawable anim = (AnimationDrawable) mSlider.mCenterView
					.getBackground();
			anim.stop();
		}
	}


	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		Log.e(TAG, "GnMultiWaveView onConfigurationChanged()-----");
		if (mTargetResourceId != 0) {
			internalSetTargetResources(mTargetResourceId);
		}
		if (mHandleDrawableResourceId != 0) {
			setCenterViewDrawable(mHandleDrawableResourceId);
		}
		if (mCenterAnimResourceId != 0) {
			setCenterViewAnimDrawable(mCenterAnimResourceId);
		}
		if (mRightWaveAnimResourceId != 0) {
			setRightWaveViewAnimDrawable(mRightWaveAnimResourceId);
		}
		if (mBottomWaveAnimResourceId != 0) {
			setBottomWaveViewAnimDrawable(mBottomWaveAnimResourceId);
		}
		if (mLeftWaveAnimResourceId != 0) {
			setLeftWaveViewAnimDrawable(mLeftWaveAnimResourceId);
		}
		if (mTopWaveAnimResourceId != 0) {
			setTopWaveViewAnimDrawable(mTopWaveAnimResourceId);
		}
	}

	private void setVisible(boolean show){
		mSlider.mLeftView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
		mSlider.mTopView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
		mSlider.mRightView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
		mSlider.mBottomView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
		mSlider.mCenterView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
		mSlider.mRightWaveView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
		mSlider.mTopWaveView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
		mSlider.mLeftWaveView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
		mSlider.mBottomWaveView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
	}


	private class Slider {
		public ImageView mLeftView;
		public ImageView mTopView;
		public ImageView mRightView;
		public Button mBottomView;
		public ImageView mCenterView;
		public ImageView mRightWaveView;
		public ImageView mTopWaveView;
		public ImageView mLeftWaveView;
		public ImageView mBottomWaveView;

		public Slider(ViewGroup parent) {
			initViews(parent);
			addedViews(parent);
		}

		private void initViews(ViewGroup parent) {
			mLeftView = new ImageView(parent.getContext());
			mTopView = new ImageView(parent.getContext());
			mRightView = new ImageView(parent.getContext());
			mBottomView = new Button(parent.getContext());
			mCenterView = new ImageView(parent.getContext());
			mRightWaveView = new ImageView(parent.getContext());
			mTopWaveView = new ImageView(parent.getContext());
			mLeftWaveView = new ImageView(parent.getContext());
			mBottomWaveView = new ImageView(parent.getContext());

		}

		private void addedViews(ViewGroup parent) {
			mLeftView.setScaleType(ScaleType.CENTER);
			mLeftView.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			mLeftView.setVisibility(View.INVISIBLE);
			parent.addView(mLeftView);

			mTopView.setScaleType(ScaleType.CENTER);
			mTopView.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			mTopView.setVisibility(View.INVISIBLE);
			parent.addView(mTopView);

			mRightView.setScaleType(ScaleType.CENTER);
			mRightView.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			mRightView.setVisibility(View.INVISIBLE);
			parent.addView(mRightView);

			mBottomView.setText(R.string.gn_fc_response_sms);
			mBottomView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 
			        getResources().getDimensionPixelSize(R.dimen.gn_fc_sms_reject_size));
			mBottomView.setTextColor(getResources().getColor(R.color.gn_fc_color_white));
			
			mBottomView.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			mBottomView.setVisibility(View.INVISIBLE);
			parent.addView(mBottomView);

			mCenterView.setScaleType(ScaleType.CENTER);
			mCenterView.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			parent.addView(mCenterView);

			mRightWaveView.setScaleType(ScaleType.CENTER);
			mRightWaveView.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			mRightWaveView.setVisibility(View.INVISIBLE);
			parent.addView(mRightWaveView);

			mTopWaveView.setScaleType(ScaleType.CENTER);
			mTopWaveView.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			mTopWaveView.setVisibility(View.INVISIBLE);
			parent.addView(mTopWaveView);

			mLeftWaveView.setScaleType(ScaleType.CENTER);
			mLeftWaveView.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			mLeftWaveView.setVisibility(View.INVISIBLE);
			parent.addView(mLeftWaveView);

			mBottomWaveView.setScaleType(ScaleType.CENTER);
			mBottomWaveView.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			mBottomWaveView.setVisibility(View.INVISIBLE);
			parent.addView(mBottomWaveView);
		}


		private void setVisibility(boolean show) {
			mLeftView.setVisibility(show ? View.VISIBLE : View.GONE);
			mTopView.setVisibility(show ? View.VISIBLE : View.GONE);
			mRightView.setVisibility(show ? View.VISIBLE : View.GONE);
			mRightView.setVisibility(show ? View.VISIBLE : View.GONE);
			mCenterView.setVisibility(show ? View.VISIBLE : View.GONE);
			mRightWaveView.setVisibility(show ? View.VISIBLE : View.GONE);
			mTopWaveView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLeftWaveView.setVisibility(show ? View.VISIBLE : View.GONE);
			mBottomWaveView.setVisibility(show ? View.VISIBLE :View.GONE);
		}
	}

	private VelocityTracker mVelocityTracker = VelocityTracker.obtain();

	private static class LinearVibrator {
		
		private static LinearVibrator mLinearVibrator;
		private Vibrator mVibrator;
		private final static int DRAGGING_DATA_LEN = 16;
		private final static String OP_DOWN = "gn_haptic/GnLockScreen/op_down.htxt";
		private final static String OP_MOVE = "gn_haptic/GnLockScreen/op_move.htxt";
		private final static String OP_UP = "gn_haptic/GnLockScreen/op_up.htxt";
		private final static String OP_GRABBED = "gn_haptic/GnLockScreen/op_grabbed.htxt";
		private final static String SYSTEM_ETC = "/system/etc/";

		private byte[] mOpDownData;
		private ArrayList<byte[]> mOpMoveData = new ArrayList<byte[]>();
		private byte[] mOpUpData;
		private byte[] mOpGrabbedData;
		private boolean mbVibrateInterfaceAvalible;
		private AssetManager mAssetManager;
		private boolean mVibrationOpen;
		private boolean mOpenSystemVibrate = true;

		private class VibrationOpenObserver extends ContentObserver {
			private ContentResolver mCR;
			private Handler mHandler = new Handler();
			private AsyncTask<Object, Object, Integer> mCurTask;
			private Runnable mDoQuery = new Runnable() {
				public void run() {
					mCurTask = new VibrationOpenTask().execute();
				}
			};

			private class VibrationOpenTask extends
					AsyncTask<Object, Object, Integer> {

				@Override
				protected Integer doInBackground(Object... arg0) {
					return Settings.System.getInt(mCR,
							"lockscreen_vibration_enabled", 0);
				}

				@Override
				protected void onPostExecute(Integer result) {
					super.onPostExecute(result);
					boolean enabled = (result == 1);
					Log.d(TAG, "enabled=" + enabled);
					mVibrationOpen = enabled;

				}

			}

			public VibrationOpenObserver(Context context) {
				super(null);
				mCR = context.getContentResolver();
				mHandler.post(mDoQuery);
			}

			@Override
			public void onChange(boolean selfChange) {

				mHandler.removeCallbacks(mDoQuery);
				mHandler.postDelayed(mDoQuery, 1000);

			}

			public void reset() {
				if (mHandler != null) {
					mHandler.removeCallbacks(mDoQuery);
					mHandler = null;
				}
				if (mCurTask != null) {
					mCurTask.cancel(true);
					mCurTask = null;
				}
			}
		}

		private LinearVibrator(Context context) {
			mVibrator = (Vibrator) context
					.getSystemService(Context.VIBRATOR_SERVICE);
			mAssetManager = context.getAssets();
			Resources res = context.getResources();
			int id = res.getIdentifier("gn_lock_screen_open_system_vibrate",
					"bool", context.getPackageName());
			if (id > 0) {
				mOpenSystemVibrate = res.getBoolean(id);
			}
			context.getContentResolver().registerContentObserver(
					Settings.System.getUriFor("lockscreen_vibration_enabled"),
					false, new VibrationOpenObserver(context));

			mbVibrateInterfaceAvalible = isVibrateInterfaceAvaliable();
			if (mbVibrateInterfaceAvalible) {
				createOpDownData();
				createOpMoveData();
				createOpUpData();
				createOpGrabbedData();
			}
		}

		public boolean isVibrateInterfaceAvaliable() {
				if (DEBUG) {
					Log.d(TAG, "isVibrateInterfaceAvaliable() 1");
				}
			if (DEBUG) {
				Log.d(TAG, "isVibrateInterfaceAvaliable()");
			}
			Class<?>[] parameterTypes = new Class[1];
			parameterTypes[0] = byte[].class;
			Method method;
			try {
				method = Vibrator.class.getMethod("vibrateEx", parameterTypes);
				if (method != null) {
					if (DEBUG) {
						Log.d(TAG, "isVibrateInterfaceAvaliable() 2");
					}
					return true;
				}
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
			return false;
		}

		public static LinearVibrator getInstance(Context context) {
			if (DEBUG) {
				Log.d(TAG, "getInstance() mLinearVibrator=" + mLinearVibrator);
			}
			if (mLinearVibrator == null) {
				mLinearVibrator = new LinearVibrator(context);
			}
			return mLinearVibrator;
		}

		public static void reload() {
			if (mLinearVibrator != null) {
				if (DEBUG) {
					Log.d(TAG, "reload");
				}
				if (mLinearVibrator.isVibrateInterfaceAvaliable()) {
					mLinearVibrator.createOpDownData();
					mLinearVibrator.createOpMoveData();
					mLinearVibrator.createOpUpData();
					mLinearVibrator.createOpGrabbedData();
				}
			}
		}

		private void createOpDownData() {
			if (DEBUG) {
				Log.d(TAG, "createOpDownData fileName=" + OP_DOWN);
			}
			mOpDownData = readHapticFile(OP_DOWN);
			return;
		}

		private void createOpMoveData() {
			if (DEBUG) {
				Log.d(TAG, "createOpMoveData fileName=" + OP_MOVE);
			}
			readVarySpeedFile(OP_MOVE, mOpMoveData);
		}

		private void createOpUpData() {
			if (DEBUG) {
				Log.d(TAG, "createOpUpData fileName=" + OP_UP);
			}
			mOpUpData = readHapticFile(OP_UP);
			return;
		}

		private void createOpGrabbedData() {
			if (DEBUG) {
				Log.d(TAG, "createOpGrabbedData fileName=" + OP_GRABBED);
			}
			mOpGrabbedData = readHapticFile(OP_GRABBED);
			return;
		}

		public boolean isAvaliable() {
			if (DEBUG) {
				Log.d(TAG, "isAvaliable mbVibrateInterfaceAvalible="
						+ mbVibrateInterfaceAvalible);
			}
			return mbVibrateInterfaceAvalible && mVibrationOpen;
		}

		public boolean vibrateForDown() {
			if (isAvaliable()) {
				boolean b = vibrate(mOpDownData);
				Log.d(TAG, "vibrateForDown b=" + b);
				return b;
			} else if (mOpenSystemVibrate) {
				vibrate();
				return true;
			}
			return false;

		}

		public boolean vibrateForUp() {
			if (isAvaliable()) {
				boolean b = vibrate(mOpUpData);
				Log.d(TAG, "vibrateForUp b=" + b);
				return b;
			} else if (mOpenSystemVibrate) {
				vibrate();
				return true;
			}
			return false;
		}

		public boolean vibrateForGrabbed() {
			if (isAvaliable()) {
				boolean b = vibrate(mOpGrabbedData);
				Log.d(TAG, "vibrateForGrabbed b=" + b);
				return b;
			} else if (mOpenSystemVibrate) {
				vibrate();
				return true;
			}
			return false;
		}

		public boolean vibrateForMove(final int speed) {
			if (DEBUG) {
				Log.d(TAG, "vibrateForMove");
			}
			byte[] buffer = getBufferMatch(speed);
			if (buffer == null || buffer.length <= 0) {
				return false;
			}
			if (DEBUG) {
				Log.d(TAG, "matched buffer :" + Arrays.toString(buffer));
			}
			byte firstSaved = buffer[0];
			buffer[0] = 0;
			boolean ret = vibrate(buffer);
			buffer[0] = firstSaved;
			return ret;
		}

		private byte[] getBufferMatch(int speed) {
			ArrayList<byte[]> varyData = mOpMoveData;
			final int count = varyData.size();
			if (count <= 0) {
				return null;
			}
			byte curFirst;
			byte nextFirst = varyData.get(0)[0];
			if (speed < nextFirst) {
				return varyData.get(0);
			}
			for (int i = 0; i < count - 1; ++i) {
				curFirst = nextFirst;
				nextFirst = varyData.get(i + 1)[0];
				if (speed >= curFirst && speed < nextFirst) {
					return varyData.get(i);
				}
			}
			return varyData.get(count - 1);
		}

		private void vibrate() {
			if (mVibrator != null) {
//				mVibrator.vibrate(mVibrationDuration);
			}
		}

		private boolean vibrate(final byte[] data) {
			if (data == null || !isAvaliable()) {
				return false;
			}
			try {
				Class<?>[] parameterTypes = new Class[1];
				parameterTypes[0] = byte[].class;
				Method method = Vibrator.class.getMethod("vibrateEx",
						parameterTypes);
				method.invoke(mVibrator, new Object[] { data });
				return true;
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			return false;
		}

		private byte[] readHapticFile(String fileName) {
			byte[] bytes = readHapticFileFromSdCard(fileName);
			if (bytes == null) {
				bytes = readHapticFileFromAssets(fileName);
			}
			return bytes;
		}

		private byte[] readHapticFile(BufferedReader reader) {
			byte[] bytes = null;
			try {
				ArrayList<Integer> codeArray = new ArrayList<Integer>();
				String line = null;
				while ((line = reader.readLine()) != null) {
					line = line.replace(',', ' ');
					String[] numbers = line.split(" ");
					for (String number : numbers) {
						if (number.isEmpty()) {
							continue;
						}
						int code = Integer.parseInt(number);
						codeArray.add(code);
					}
				}
				bytes = new byte[codeArray.size()];
				convertIntergeArrayToByteArray(codeArray, bytes);
				return bytes;
			} catch (IOException e) {
				if (DEBUG) {
					Log.w(TAG, e);
				}
				e.printStackTrace();
			}
			return null;
		}

		private byte[] readHapticFileFromSdCard(String fileName) {
			String f = SYSTEM_ETC + fileName;
			if (DEBUG) {
				Log.d(TAG, "readHapticFileFromSdCard f=" + f);
			}
			File file = new File(f);
			if (!file.exists()) {
				if (DEBUG) {
					Log.d(TAG, "readHapticFileFromSdCard f=" + f
							+ ",not exists");
				}
				return null;
			}
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				return readHapticFile(reader);
			} catch (FileNotFoundException e) {
				if (DEBUG) {
					Log.w(TAG, e);
				}
				e.printStackTrace();
			}
			return null;
		}

		private byte[] readHapticFileFromAssets(String fileName) {
			if (DEBUG) {
				Log.d(TAG, "readHapticFileFromAssets fileName=" + fileName);
			}
			BufferedReader reader = null;
			try {
				InputStreamReader inputReader = new InputStreamReader(
						mAssetManager.open(fileName));
				reader = new BufferedReader(inputReader);
				return readHapticFile(reader);
			} catch (IOException e) {
				if (DEBUG) {
					Log.w(TAG, e);
				}
				e.printStackTrace();
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						if (DEBUG) {
							Log.w(TAG, e);
						}
						e.printStackTrace();
					}
				}
			}
			return null;
		}

		private void convertIntergeArrayToByteArray(
				ArrayList<Integer> codeArray, byte[] bytes) {
			if (DEBUG) {
				Log.d(TAG, "convertIntergeArrayToByteArray");
			}
			final int count = codeArray.size();
			for (int i = 0; i < count; ++i) {
				bytes[i] = (byte) (codeArray.get(i).intValue());
			}
		}

		private void readVarySpeedFile(BufferedReader reader,
				ArrayList<byte[]> varySpeedBuffer) {

			try {
				ArrayList<Integer> codeArray = new ArrayList<Integer>();
				String line = null;
				while ((line = reader.readLine()) != null) {
					line = line.replace(',', ' ');
					String[] numbers = line.split(" ");
					for (String number : numbers) {
						if (number.isEmpty()) {
							continue;
						}
						int code = Integer.parseInt(number);
						codeArray.add(code);
					}
					if (codeArray.size() > 0) {
						byte[] bytes = new byte[codeArray.size()];
						convertIntergeArrayToByteArray(codeArray, bytes);
						varySpeedBuffer.add(bytes);
						codeArray.clear();
					}
				}

				return;
			} catch (IOException e) {
				if (DEBUG) {
					Log.w(TAG, e);
				}
				e.printStackTrace();
			}
		}

		private boolean readVarySpeedFileFromSdCard(String fileName,
				ArrayList<byte[]> varySpeedBuffer) {
			String f = SYSTEM_ETC + fileName;
			if (DEBUG) {
				Log.d(TAG, "readVarySpeedFileFromSdCard f=" + f);
			}
			varySpeedBuffer.clear();
			File file = new File(f);
			if (!file.exists()) {
				if (DEBUG) {
					Log.d(TAG, "readVarySpeedFileFromSdCard f=" + f
							+ ",not exists");
				}
				return false;
			}
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(file));
				readVarySpeedFile(reader, varySpeedBuffer);
				return true;
			} catch (FileNotFoundException e) {
				if (DEBUG) {
					Log.w(TAG, e);
				}
				e.printStackTrace();
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						if (DEBUG) {
							Log.w(TAG, e);
						}
						e.printStackTrace();
					}
				}
			}
			return false;
		}

		private void readVarySpeedFileFromAssets(String fileName,
				ArrayList<byte[]> varySpeedBuffer) {
			if (DEBUG) {
				Log.d(TAG, "readVarySpeedFileFromAssets fileName=" + fileName);
			}
			varySpeedBuffer.clear();
			BufferedReader reader = null;
			try {
				InputStreamReader inputReader = new InputStreamReader(
						mAssetManager.open(fileName));
				reader = new BufferedReader(inputReader);
				readVarySpeedFile(reader, varySpeedBuffer);
			} catch (IOException e) {
				if (DEBUG) {
					Log.w(TAG, e);
				}
				e.printStackTrace();
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						if (DEBUG) {
							Log.w(TAG, e);
						}
						e.printStackTrace();
					}
				}
			}
		}

		private void readVarySpeedFile(String fileName,
				ArrayList<byte[]> varySpeedBuffer) {
			if (!readVarySpeedFileFromSdCard(fileName, varySpeedBuffer)) {
				readVarySpeedFileFromAssets(fileName, varySpeedBuffer);
			}
		}

		public void getCurrentVelocity(float[] velocityXY, float spanStartPlus,
				float spanEndPlus, float spanStartMinus, float spanEndMinus,
				Context context, VelocityTracker velocityTracker) {
			ViewConfiguration vConfig = ViewConfiguration.get(context);
			int mMaximumFlingVelocity = vConfig.getScaledMaximumFlingVelocity();
			float density = context.getResources().getDisplayMetrics().density;

			velocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
			float vx = velocityTracker.getXVelocity() / density;
			float vy = velocityTracker.getYVelocity() / density;
			Log.d("linearV", "vx=" + vx + ";vy=" + vy);
			float projVx;
			if (vx >= 0) {
				projVx = spanStartPlus + vx / 1000
						* (spanEndPlus - spanStartPlus);
			} else {
				projVx = spanStartMinus + vx / 1000
						* (spanEndMinus - spanStartMinus);
			}
			float projVy;
			if (vy >= 0) {
				projVy = spanStartPlus + vy / 1000
						* (spanEndPlus - spanStartPlus);
			} else {
				projVy = spanStartMinus + vy / 1000
						* (spanEndMinus - spanStartMinus);
			}
			Log.d("linearV", "projVx=" + projVx + ";projVy=" + projVy);
			velocityXY[0] = projVx;
			velocityXY[1] = projVy;
		}
	}

    private boolean mAlwaysShowTargets = false;
    public void setAlwaysShowTargets(boolean flag) {
        mAlwaysShowTargets = flag;
    }
    
    public boolean isAlwaysShowTargets() {
        return mAlwaysShowTargets;
    }
    
    private boolean mIsSingleHandMode = false;
    private Paint mSingleHandPaint = null;
    private Drawable mHandModeBgDrawable = null;
    public void setSingleHandMode(boolean flag) {
        mIsSingleHandMode = flag;
    }
    
    public boolean isSingleHandMode() {
        return mIsSingleHandMode;
    }
    
    public void setHandModeBg(Drawable bg) {
        mHandModeBgDrawable = bg;
    }
    
    private void drawSingleCircle(Canvas canvas) {
        if (mIsSingleHandMode) {
            Bitmap bt = mHandModeBgDrawable != null ? drawableToBitmap(mHandModeBgDrawable) : null;
            if (null == mSingleHandPaint) {
                mSingleHandPaint = new Paint();
            }
            if (null != bt) {
                float intrinsicWidth = (float) mSlider.mCenterView.getBackground().getIntrinsicWidth();
                canvas.drawBitmap(bt, mCenterX-mOuterRadius-intrinsicWidth*2/5, 
                        mCenterY-mOuterRadius-intrinsicWidth*2/5, mSingleHandPaint);
            }
        }
    }
    
    private Bitmap drawableToBitmap(Drawable drawable) {
        int width = 0;
        int height = 0;
        if (null != drawable) {
            width = drawable.getIntrinsicWidth();
            height = drawable.getIntrinsicHeight();
            Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
            Bitmap bitmap = Bitmap.createBitmap(width, height, config);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, width, height);
            drawable.draw(canvas);
            return bitmap;
        }
        return null;
    }
    

    public void setAnswerListener(AnswerListener listener) {
        mAnswerListener = listener;
    }

	public void setRippleListener(RippleListener rippleListener) {
		this.mRippleListener = rippleListener;
	}

    
    public interface AnswerListener {
    	
    	int NO_HANDLE = 0;
		int CENTER_HANDLE = 1;
		
        void onAnswer(int videoState, Context context);
        void onDecline();
        void onText();
    }

	public interface RippleListener {
		public void stopRipple();
		public void startRipple();
	}
}
