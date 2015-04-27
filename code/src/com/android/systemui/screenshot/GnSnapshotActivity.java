package com.android.systemui.screenshot;

import java.io.File;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaActionSound;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.systemui.R;

public class GnSnapshotActivity extends Activity {
	private static final String TAG = "ScreenSnapshot";
	private static final int ACTION_MOVE_DEFAULT = 0x00;
	private static final int ACTION_MOVE_TOP = 0x01;
	private static final int ACTION_MOVE_LEFT = ACTION_MOVE_TOP << 1;
	private static final int ACTION_MOVE_RIGHT = ACTION_MOVE_TOP << 2;
	private static final int ACTION_MOVE_BOTTOM = ACTION_MOVE_TOP << 3;

	private static final int CAPTURE_EVENT = 0;
	private static final int INFLATE_EVENT = 1;

	/** Offset to zoom in the snap area */
	private int SNAPAREA_INIT_OFFSET = 250;
	/** Offset to adjust corner touch area */
	private int CORNER_TOUCH_OFFSET = 50;
	/** Screen width and height */
	private int mScreenWidth = -1;
	private int mScreenHeight = -1;
	/** Hover width and height */
	private int mHoverWidth = -1;
	private int mHoverHeight = -1;

	FrameLayout mTopSlider;
	FrameLayout mBottomSlider;
	FrameLayout mLeftSlider;
	FrameLayout mRightSlider;

	View mTopMargins;
	View mBottomMargins;
	View mLeftMargins;
	View mRightMargins;

	View mSnapshotView;

	private MediaActionSound mCameraSound;
	
	private final SliderTouchListener mTouchListener = new SliderTouchListener();
	private final ButtonClickListener mClickListener = new ButtonClickListener();

	private GnSnapshotService mService = null;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == CAPTURE_EVENT) {
				// Play the shutter sound to notify that we've taken a screenshot
				mCameraSound.play(MediaActionSound.SHUTTER_CLICK);
				mService.mLocation.startX = (int) mSnapshotView.getX();
				mService.mLocation.startY = (int) mSnapshotView.getY();
				mService.mLocation.width = mSnapshotView.getWidth();
				mService.mLocation.height = mSnapshotView.getHeight();
				mService.captureSelectedArea(mService.mLocation);
				// finish();
			} else if (msg.what == INFLATE_EVENT) {
				resizeLayout(mSnapshotView, mLeftMargins.getRight(), mTopMargins.getBottom(),
						mScreenWidth - mRightMargins.getLeft(),
						mScreenHeight - mBottomMargins.getTop(), 0, 0, ACTION_MOVE_DEFAULT);
				updateCornerTouchArea();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gn_screen_snapshot);
		mService = GnSnapshotService.getService(this);
		
		Display display = getWindowManager().getDefaultDisplay();
		mScreenHeight = display.getHeight();
		mScreenWidth = display.getWidth();

		int orierention = mService.getScreenOrierention();
		setRequestedOrientation(orierention);
		
		DisplayMetrics displayMetrics ;
		Resources res = getResources();
		displayMetrics = res.getDisplayMetrics();
		float density = displayMetrics.density;
		CORNER_TOUCH_OFFSET = (int) (res.getDimension(R.dimen.corner_touch_offset) / density);
		SNAPAREA_INIT_OFFSET = mScreenHeight / 4;

		log("CORNER_TOUCH_OFFSET = " + CORNER_TOUCH_OFFSET, "density = " + density);

		mTopSlider = (FrameLayout) findViewById(R.id.top);
		mTopSlider.setOnTouchListener(mTouchListener);
		mBottomSlider = (FrameLayout) findViewById(R.id.bottom);
		mBottomSlider.setOnTouchListener(mTouchListener);
		mLeftSlider = (FrameLayout) findViewById(R.id.left);
		mLeftSlider.setOnTouchListener(mTouchListener);
		mRightSlider = (FrameLayout) findViewById(R.id.right);
		mRightSlider.setOnTouchListener(mTouchListener);

		mTopMargins = findViewById(R.id.top_margin);
		mBottomMargins = findViewById(R.id.bottom_margin);
		mLeftMargins = findViewById(R.id.left_margin);
		mRightMargins = findViewById(R.id.right_margin);

		mSnapshotView = findViewById(R.id.snap_area);
		mSnapshotView.setOnTouchListener(mTouchListener);

		LinearLayout exitButton = (LinearLayout) findViewById(R.id.exit);
		exitButton.setOnClickListener(mClickListener);
		LinearLayout saveButton = (LinearLayout) findViewById(R.id.save);
		saveButton.setOnClickListener(mClickListener);

		inflateViews();
		initConerTouchView();
		
        // Setup the Camera shutter sound
        mCameraSound = new MediaActionSound();
        mCameraSound.load(MediaActionSound.SHUTTER_CLICK);
	}

	void inflateViews() {
	    FrameLayout mView = (FrameLayout) findViewById(R.id.snap_shot);
	    mView.setBackgroundDrawable(new BitmapDrawable(GnSnapshotService.getService(this).getBitmap()));
	    
		// Get hover's width and height
		ImageView hover = (ImageView) mTopSlider.findViewById(R.id.hover);
		hover.measure(0, 0);
		mHoverWidth = hover.getMeasuredWidth() / 2;
		mHoverHeight = hover.getMeasuredHeight() / 2;

		// Adjust sliders
		resizeLayout(mTopSlider, mHoverWidth, SNAPAREA_INIT_OFFSET, mHoverWidth, 0, 0, 0,
				ACTION_MOVE_DEFAULT);
		resizeLayout(mBottomSlider, mHoverWidth, 0, mHoverWidth, SNAPAREA_INIT_OFFSET, 0, 0,
				ACTION_MOVE_DEFAULT);
		resizeLayout(mLeftSlider, 0, mHoverHeight + SNAPAREA_INIT_OFFSET, 0, mHoverHeight
				+ SNAPAREA_INIT_OFFSET, 0, 0, ACTION_MOVE_DEFAULT);
		resizeLayout(mRightSlider, 0, mHoverHeight + SNAPAREA_INIT_OFFSET, 0, mHoverHeight
				+ SNAPAREA_INIT_OFFSET, 0, 0, ACTION_MOVE_DEFAULT);

		// Adjust Margins
		resizeLayout(mTopMargins, 0, 0, 0, 0, 0, mHoverHeight + SNAPAREA_INIT_OFFSET,
				ACTION_MOVE_TOP);
		resizeLayout(mBottomMargins, 0, 0, 0, 0, 0, mHoverHeight + SNAPAREA_INIT_OFFSET,
				ACTION_MOVE_BOTTOM);
		resizeLayout(mLeftMargins, 0, mHoverHeight + SNAPAREA_INIT_OFFSET, 0, mHoverHeight
				+ SNAPAREA_INIT_OFFSET, mHoverWidth, 0, ACTION_MOVE_LEFT);
		resizeLayout(mRightMargins, 0, mHoverHeight + SNAPAREA_INIT_OFFSET, 0, mHoverHeight
				+ SNAPAREA_INIT_OFFSET, mHoverWidth, 0, ACTION_MOVE_RIGHT);

		resizeLayout(mSnapshotView, mHoverWidth, mHoverHeight + SNAPAREA_INIT_OFFSET, mHoverWidth,
				mHoverHeight + SNAPAREA_INIT_OFFSET, 0, 0, ACTION_MOVE_DEFAULT);
	}

	void initConerTouchView() {
		FrameLayout topLeftToucher = (FrameLayout) findViewById(R.id.top_left_touch);
		topLeftToucher.setOnTouchListener(mTouchListener);
		resizeLayout(topLeftToucher, 0, SNAPAREA_INIT_OFFSET, 0, 0, 0, 0, ACTION_MOVE_DEFAULT);

		FrameLayout topRightToucher = (FrameLayout) findViewById(R.id.top_right_touch);
		topRightToucher.setOnTouchListener(mTouchListener);
		resizeLayout(topRightToucher, 0, SNAPAREA_INIT_OFFSET, 0, 0, 0, 0, ACTION_MOVE_DEFAULT);

		FrameLayout bottomLeftToucher = (FrameLayout) findViewById(R.id.bottom_left_touch);
		bottomLeftToucher.setOnTouchListener(mTouchListener);
		resizeLayout(bottomLeftToucher, 0, 0, 0, SNAPAREA_INIT_OFFSET, 0, 0, ACTION_MOVE_DEFAULT);

		FrameLayout bottomRightToucher = (FrameLayout) findViewById(R.id.bottom_right_touch);
		bottomRightToucher.setOnTouchListener(mTouchListener);
		resizeLayout(bottomRightToucher, 0, 0, 0, SNAPAREA_INIT_OFFSET, 0, 0, ACTION_MOVE_DEFAULT);
	}

	void resizeLayout(View layout, int left, int top, int right, int bottom, int width, int height,
			int mask) {
		FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) layout.getLayoutParams();
		layoutParams.setMargins(left, top, right, bottom);
		if ((mask & ACTION_MOVE_TOP) == ACTION_MOVE_TOP
				|| (mask & ACTION_MOVE_BOTTOM) == ACTION_MOVE_BOTTOM) {
			layoutParams.height = height;
		}

		if ((mask & ACTION_MOVE_LEFT) == ACTION_MOVE_LEFT
				|| (mask & ACTION_MOVE_RIGHT) == ACTION_MOVE_RIGHT) {
			layoutParams.width = width;
		}
		layout.setLayoutParams(layoutParams);
	}

	@Override
	protected void onResume() {
		super.onResume();
		/*
		Drawable drawable = new BitmapDrawable(mService.getBitmap());
		if (drawable != null) {
			getWindow().getDecorView().setBackground(drawable);
		}
		*/
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		finish();
	}
	/**
	 * Extend corner touch area, the offset is set by CORNER_TOUCH_OFFSET
	 * */
	void updateCornerTouchArea() {
		FrameLayout topLeftToucher = (FrameLayout) findViewById(R.id.top_left_touch);
		resizeLayout(topLeftToucher, mSnapshotView.getLeft() - CORNER_TOUCH_OFFSET,
				mSnapshotView.getTop() - CORNER_TOUCH_OFFSET - mHoverHeight, 0, 0, 0, 0,
				ACTION_MOVE_DEFAULT);
		FrameLayout topRightToucher = (FrameLayout) findViewById(R.id.top_right_touch);
		resizeLayout(topRightToucher, 0, mSnapshotView.getTop() - CORNER_TOUCH_OFFSET
				- mHoverHeight, mScreenWidth - (mSnapshotView.getRight() + CORNER_TOUCH_OFFSET), 0,
				0, 0, ACTION_MOVE_DEFAULT);
		FrameLayout bottomLeftToucher = (FrameLayout) findViewById(R.id.bottom_left_touch);
		resizeLayout(bottomLeftToucher, mSnapshotView.getLeft() - CORNER_TOUCH_OFFSET, 0, 0,
				mScreenHeight - (mSnapshotView.getBottom() + CORNER_TOUCH_OFFSET), 0, 0,
				ACTION_MOVE_DEFAULT);
		FrameLayout bottomRightToucher = (FrameLayout) findViewById(R.id.bottom_right_touch);
		resizeLayout(bottomRightToucher, 0, 0, mScreenWidth
				- (mSnapshotView.getRight() + CORNER_TOUCH_OFFSET),
				mScreenHeight - (mSnapshotView.getBottom() + CORNER_TOUCH_OFFSET), 0, 0,
				ACTION_MOVE_DEFAULT);
	}

	class SliderTouchListener implements OnTouchListener {
		// Mark whether down point is on SnapshotView or not
		private boolean mInSnapShotArea = false;
		private Point mMovePoint = new Point();
		private Point mPrivPoint = new Point();
		// View's Position on the screen
		private int[] locations = new int[2];

		@Override
		public boolean onTouch(View view, MotionEvent event) {
			int action = event.getAction();
			int pointerNumber = event.getPointerCount();
			int pointerId = event.getPointerId(pointerNumber - 1);
			// Only deal with one finger toucher event
			if (pointerId > 0)
				return true;
			view.getLocationOnScreen(locations);
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				if (view.getId() == R.id.snap_area) {
					mInSnapShotArea = true;
					mPrivPoint.x = (int) (event.getX() + locations[0]);
					mPrivPoint.y = (int) (event.getY() + locations[1]);
				}
				break;
			case MotionEvent.ACTION_MOVE:
				mMovePoint.x = (int) (event.getX() + locations[0]);
				mMovePoint.y = (int) (event.getY() + locations[1]);
				adjustViews(view, event);
				break;

			case MotionEvent.ACTION_UP:
				mInSnapShotArea = false;
				mHandler.sendEmptyMessage(INFLATE_EVENT);
				break;

			default:
				break;
			}
			return true;
		}

		void adjustViews(View view, MotionEvent event) {
			int id = view.getId();
			switch (id) {
			case R.id.top:
				dragTopSlider(view, event);
				break;
			case R.id.bottom:
				dragBottomSlider(view, event);
				break;
			case R.id.left:
				dragLeftSlider();
				break;
			case R.id.right:
				dragRightSlider(view, event);
				break;
			case R.id.snap_area:
				dragSnapArea(view, event);
				break;
			default:
				dragCorners(view, event);
				break;
			}

			resizeLayout(mSnapshotView, mLeftMargins.getRight(), mTopMargins.getBottom(),
					mScreenWidth - mRightMargins.getLeft(),
					mScreenHeight - mBottomMargins.getTop(), 0, 0, ACTION_MOVE_DEFAULT);
			updateCornerTouchArea();
		}

		void dragLeftSlider() {
			// Left slider cann't be in right of right slider
			if (mMovePoint.x > mRightSlider.getLeft() - mHoverWidth)
				return;
			resizeLayout(mLeftSlider, mMovePoint.x - mHoverWidth, mLeftSlider.getTop(), 0,
					mScreenHeight - mLeftSlider.getBottom(), 0, 0, ACTION_MOVE_DEFAULT);
			resizeLayout(mTopSlider, mMovePoint.x, mTopSlider.getTop(),
					mScreenWidth - mTopSlider.getRight(), 0, 0, 0, ACTION_MOVE_DEFAULT);
			resizeLayout(mBottomSlider, mMovePoint.x, 0, mScreenWidth - mBottomSlider.getRight(),
					mScreenHeight - mBottomSlider.getBottom(), 0, 0, ACTION_MOVE_DEFAULT);

			resizeLayout(mLeftMargins, 0, mLeftMargins.getTop(), 0,
					mScreenHeight - mLeftMargins.getBottom(), mMovePoint.x, 0, ACTION_MOVE_LEFT);
			// No need to adjust top and Bottom margins
		}

		void dragRightSlider(View view, MotionEvent event) {
			// Right slider cann't be in left of left slider
			if (mMovePoint.x < mLeftSlider.getRight() + mHoverWidth || mMovePoint.x > mScreenWidth)
				return;
			resizeLayout(mRightSlider, 0, mRightSlider.getTop(), mScreenWidth - mMovePoint.x
					- mHoverWidth, mScreenHeight - mRightSlider.getBottom(), 0, 0,
					ACTION_MOVE_DEFAULT);
			resizeLayout(mTopSlider, mTopSlider.getLeft(), mTopSlider.getTop(), mScreenWidth
					- mMovePoint.x, 0, 0, 0, ACTION_MOVE_DEFAULT);
			resizeLayout(mBottomSlider, mBottomSlider.getLeft(), 0, mScreenWidth - mMovePoint.x,
					mScreenHeight - mBottomSlider.getBottom(), 0, 0, ACTION_MOVE_DEFAULT);

			resizeLayout(mRightMargins, 0, mRightMargins.getTop(), 0,
					mScreenHeight - mRightMargins.getBottom(), mScreenWidth - mMovePoint.x, 0,
					ACTION_MOVE_RIGHT);
			// No need to adjust top and Bottom margins
		}

		void dragTopSlider(View view, MotionEvent event) {
			// Top slider cann't below bottom slider
			if (mMovePoint.y > mBottomSlider.getTop() - mHoverHeight)
				return;

			resizeLayout(mTopSlider, mTopSlider.getLeft(), mMovePoint.y - mHoverHeight,
					mScreenWidth - mTopSlider.getRight(), mHoverHeight, 0, 0, ACTION_MOVE_DEFAULT);
			resizeLayout(mLeftSlider, mLeftSlider.getLeft(), mMovePoint.y, 0, mScreenHeight
					- mLeftSlider.getBottom(), 0, 0, ACTION_MOVE_DEFAULT);
			resizeLayout(mRightSlider, 0, mMovePoint.y, mScreenWidth - mRightSlider.getRight(),
					mScreenHeight - mRightSlider.getBottom(), 0, 0, ACTION_MOVE_DEFAULT);

			resizeLayout(mTopMargins, 0, 0, 0, 0, 0, mMovePoint.y, ACTION_MOVE_TOP);
			resizeLayout(mLeftMargins, 0, mMovePoint.y, 0,
					mScreenHeight - mLeftMargins.getBottom(), mLeftMargins.getWidth(), 0,
					ACTION_MOVE_LEFT);
			resizeLayout(mRightMargins, 0, mMovePoint.y, 0,
					mScreenHeight - mRightMargins.getBottom(), mRightMargins.getWidth(), 0,
					ACTION_MOVE_RIGHT);

		}

		void dragBottomSlider(View view, MotionEvent event) {
			// Bottom slider cann't above top slider
			if (mMovePoint.y < mTopSlider.getBottom() + mHoverHeight
					|| mMovePoint.y > mScreenHeight)
				return;
			resizeLayout(mBottomSlider, mBottomSlider.getLeft(), 0,
					mScreenWidth - mBottomSlider.getRight(), mScreenHeight - mMovePoint.y
							- mHoverHeight, 0, 0, ACTION_MOVE_DEFAULT);
			resizeLayout(mLeftSlider, mLeftSlider.getLeft(), mLeftSlider.getTop(), 0, mScreenHeight
					- mMovePoint.y, 0, 0, ACTION_MOVE_DEFAULT);
			resizeLayout(mRightSlider, 0, mRightSlider.getTop(),
					mScreenWidth - mRightSlider.getRight(), mScreenHeight - mMovePoint.y, 0, 0,
					ACTION_MOVE_DEFAULT);

			resizeLayout(mBottomMargins, 0, 0, 0, 0, 0, mScreenHeight - mMovePoint.y,
					ACTION_MOVE_TOP);
			resizeLayout(mLeftMargins, 0, mLeftMargins.getTop(), 0, mScreenHeight - mMovePoint.y,
					mLeftMargins.getWidth(), 0, ACTION_MOVE_LEFT);
			resizeLayout(mRightMargins, 0, mRightMargins.getTop(), 0, mScreenHeight - mMovePoint.y,
					mRightMargins.getWidth(), 0, ACTION_MOVE_RIGHT);
		}

		boolean isSnapAreaDragable() {
			if ((mMovePoint.x - mLeftSlider.getRight()) < mHoverWidth * 2
					|| (mMovePoint.x - mRightSlider.getLeft()) < mHoverWidth * 2
					|| (mMovePoint.y - mTopSlider.getBottom()) < mHoverHeight * 2
					|| (mMovePoint.y - mBottomSlider.getTop()) < mHoverHeight * 2) {
				return true;
			}
			return false;
		}

		void dragSnapArea(View view, MotionEvent event) {
			if (!mInSnapShotArea)
				return;

			// Resize the touch area for moving
			if (!isSnapAreaDragable()) {
				return;
			}

			int deltaX = mMovePoint.x - mPrivPoint.x;
			int deltaY = mMovePoint.y - mPrivPoint.y;

			// prevent move out of the screen
			if ((mLeftSlider.getLeft() + deltaX + mHoverWidth) < 0
					|| (mScreenWidth - (mRightSlider.getRight() + deltaX) + mHoverWidth) < 0) {
				deltaX = 0;
			}

			if ((mTopSlider.getTop() + deltaY + mHoverHeight) < 0
					|| (mScreenHeight - (mBottomSlider.getBottom() + deltaY) + mHoverHeight) < 0) {
				deltaY = 0;
			}

			// save previous point
			mPrivPoint.x = mMovePoint.x;
			mPrivPoint.y = mMovePoint.y;

			resizeLayout(mTopSlider, mTopSlider.getLeft() + deltaX, mTopSlider.getTop() + deltaY,
					mScreenWidth - (mTopSlider.getRight() + deltaX), 0, 0, 0, ACTION_MOVE_DEFAULT);
			resizeLayout(mLeftSlider, mLeftSlider.getLeft() + deltaX,
					mLeftSlider.getTop() + deltaY, 0, mScreenHeight
							- (mLeftSlider.getBottom() + deltaY), 0, 0, ACTION_MOVE_DEFAULT);
			resizeLayout(mRightSlider, 0, mRightSlider.getTop() + deltaY, mScreenWidth
					- (mRightSlider.getRight() + deltaX), mScreenHeight
					- (mRightSlider.getBottom() + deltaY), 0, 0, ACTION_MOVE_DEFAULT);
			resizeLayout(mBottomSlider, mBottomSlider.getLeft() + deltaX, 0, mScreenWidth
					- (mBottomSlider.getRight() + deltaX),
					mScreenHeight - (mBottomSlider.getBottom() + deltaY), 0, 0, ACTION_MOVE_DEFAULT);

			resizeLayout(mTopMargins, 0, 0, 0, 0, 0, mTopMargins.getHeight() + deltaY,
					ACTION_MOVE_TOP);
			resizeLayout(mLeftMargins, 0, mLeftMargins.getTop() + deltaY, 0, mScreenHeight
					- (mLeftMargins.getBottom() + deltaY), mLeftMargins.getWidth() + deltaX, 0,
					ACTION_MOVE_LEFT);
			resizeLayout(mRightMargins, 0, mRightMargins.getTop() + deltaY, 0, mScreenHeight
					- (mRightMargins.getBottom() + deltaY), mRightMargins.getWidth() - deltaX, 0,
					ACTION_MOVE_RIGHT);
			resizeLayout(mBottomMargins, 0, 0, 0, 0, 0, mBottomMargins.getHeight() - deltaY,
					ACTION_MOVE_BOTTOM);
		}

		void dragCorners(View view, MotionEvent event) {
			int id = view.getId();

			switch (id) {
			case R.id.top_left_touch:
				if (mMovePoint.x > mRightSlider.getLeft() - mHoverWidth * 3
						|| mMovePoint.y > mBottomSlider.getTop() - mHoverHeight)
					return;
				resizeLayout(mLeftSlider, mMovePoint.x, mMovePoint.y, 0, mScreenHeight
						- mBottomSlider.getTop() - mHoverHeight, 0, 0, ACTION_MOVE_DEFAULT);
				resizeLayout(mTopSlider, mMovePoint.x + mHoverWidth, mMovePoint.y - mHoverHeight,
						mScreenWidth - mRightSlider.getLeft() - mHoverWidth, mHoverHeight, 0, 0,
						ACTION_MOVE_DEFAULT);
				resizeLayout(mRightSlider, 0, mMovePoint.y, mScreenWidth - mRightSlider.getRight(),
						mScreenHeight - mRightSlider.getBottom(), 0, 0, ACTION_MOVE_DEFAULT);
				resizeLayout(mBottomSlider, mMovePoint.x + mHoverWidth, 0, mScreenWidth
						- mBottomSlider.getRight(), mScreenHeight - mBottomSlider.getBottom(), 0,
						0, ACTION_MOVE_DEFAULT);

				resizeLayout(mLeftMargins, 0, mMovePoint.y, 0,
						mScreenHeight - mLeftMargins.getBottom(), mMovePoint.x + mHoverWidth, 0,
						ACTION_MOVE_LEFT);
				resizeLayout(mTopMargins, 0, 0, 0, 0, 0, mMovePoint.y, ACTION_MOVE_TOP);
				resizeLayout(mRightMargins, 0, mMovePoint.y, 0,
						mScreenHeight - mRightMargins.getBottom(), mRightMargins.getWidth(), 0,
						ACTION_MOVE_RIGHT);

				break;
			case R.id.top_right_touch:
				if (mMovePoint.x < mLeftSlider.getRight() + mHoverWidth * 3
						|| mMovePoint.y > mBottomSlider.getTop() - mHoverHeight)
					return;
				resizeLayout(mLeftSlider, mLeftSlider.getLeft(), mMovePoint.y, 0, mScreenHeight
						- mLeftSlider.getBottom(), 0, 0, ACTION_MOVE_DEFAULT);
				resizeLayout(mTopSlider, mTopSlider.getLeft(), mMovePoint.y - mHoverHeight,
						mScreenWidth - mMovePoint.x + mHoverWidth, mHoverHeight, 0, 0,
						ACTION_MOVE_DEFAULT);
				resizeLayout(mRightSlider, 0, mMovePoint.y, mScreenWidth - mMovePoint.x,
						mScreenHeight - mRightSlider.getBottom(), 0, 0, ACTION_MOVE_DEFAULT);
				resizeLayout(mBottomSlider, mBottomSlider.getLeft(), 0, mScreenWidth - mMovePoint.x
						+ mHoverWidth, mScreenHeight - mBottomSlider.getBottom(), 0, 0,
						ACTION_MOVE_DEFAULT);

				resizeLayout(mLeftMargins, 0, mMovePoint.y, 0,
						mScreenHeight - mLeftMargins.getBottom(), mLeftMargins.getWidth(), 0,
						ACTION_MOVE_LEFT);
				resizeLayout(mRightMargins, 0, mMovePoint.y, 0,
						mScreenHeight - mRightMargins.getBottom(), mScreenWidth - mMovePoint.x
								+ mHoverWidth, 0, ACTION_MOVE_RIGHT);
				resizeLayout(mTopMargins, 0, 0, 0, 0, 0, mMovePoint.y, ACTION_MOVE_TOP);

				break;
			case R.id.bottom_left_touch:
				if (mMovePoint.x > mRightSlider.getLeft() - mHoverWidth
						|| mMovePoint.y < mTopSlider.getBottom() + mHoverHeight * 3)
					return;
				resizeLayout(mLeftSlider, mMovePoint.x - mHoverWidth, mLeftSlider.getTop(), 0,
						mScreenHeight - mMovePoint.y + mHoverHeight, 0, 0, ACTION_MOVE_DEFAULT);
				resizeLayout(mTopSlider, mMovePoint.x, mTopSlider.getTop(), mScreenWidth
						- mTopSlider.getRight(), 0, 0, 0, ACTION_MOVE_DEFAULT);
				resizeLayout(mRightSlider, 0, mRightSlider.getTop(),
						mScreenWidth - mRightSlider.getRight(), mScreenHeight - mMovePoint.y
								+ mHoverHeight, 0, 0, ACTION_MOVE_DEFAULT);
				resizeLayout(mBottomSlider, mMovePoint.x, 0,
						mScreenWidth - mBottomSlider.getRight(), mScreenHeight - mMovePoint.y, 0,
						0, ACTION_MOVE_DEFAULT);

				resizeLayout(mLeftMargins, 0, mLeftMargins.getTop(), 0, mScreenHeight
						- mMovePoint.y + mHoverHeight, mMovePoint.x, 0, ACTION_MOVE_LEFT);
				resizeLayout(mRightMargins, 0, mRightMargins.getTop(), 0, mScreenHeight
						- mMovePoint.y + mHoverHeight, mRightMargins.getWidth(), 0,
						ACTION_MOVE_RIGHT);
				resizeLayout(mBottomMargins, 0, 0, 0, 0, 0, mScreenHeight - mMovePoint.y
						+ mHoverHeight, ACTION_MOVE_TOP);
				break;
			case R.id.bottom_right_touch:
				if (mMovePoint.x < mLeftSlider.getRight() + mHoverWidth * 3
						|| mMovePoint.y < mTopSlider.getBottom() + mHoverHeight)
					return;
				resizeLayout(mLeftSlider, mLeftSlider.getLeft(), mLeftSlider.getTop(), 0,
						mScreenHeight - mMovePoint.y, 0, 0, ACTION_MOVE_DEFAULT);
				resizeLayout(mTopSlider, mTopSlider.getLeft(), mTopSlider.getTop(), mScreenWidth
						- mMovePoint.x + mHoverWidth, 0, 0, 0, ACTION_MOVE_DEFAULT);
				resizeLayout(mRightSlider, 0, mRightSlider.getTop(), mScreenWidth - mMovePoint.x,
						mScreenHeight - mMovePoint.y, 0, 0, ACTION_MOVE_DEFAULT);
				resizeLayout(mBottomSlider, mBottomSlider.getLeft(), 0, mScreenWidth - mMovePoint.x
						+ mHoverWidth, mScreenHeight - mMovePoint.y - mHoverHeight, 0, 0,
						ACTION_MOVE_DEFAULT);

				resizeLayout(mLeftMargins, 0, mLeftMargins.getTop(), 0, mScreenHeight
						- mMovePoint.y, mLeftMargins.getWidth(), 0, ACTION_MOVE_LEFT);
				resizeLayout(mRightMargins, 0, mRightMargins.getTop(), 0, mScreenHeight
						- mMovePoint.y, mScreenWidth - mMovePoint.x + mHoverWidth, 0,
						ACTION_MOVE_RIGHT);
				resizeLayout(mBottomMargins, 0, 0, 0, 0, 0, mScreenHeight - mMovePoint.y,
						ACTION_MOVE_TOP);

				break;
			default:
				break;
			}
			// Why is there a move event when pointer down the corner?
			// For that we need to update the snap area immediately
			mHandler.sendEmptyMessage(INFLATE_EVENT);
		}
	}

	class ButtonClickListener implements OnClickListener {

		@Override
		public void onClick(View view) {
			int id = view.getId();

			switch (id) {
			case R.id.exit:
				onBackPressed();
				break;
			case R.id.save:
//				new Thread(new Runnable() {
//					@Override
//					public void run() {
//						mHandler.sendEmptyMessage(CAPTURE_EVENT);
//					}
//				}).start();
				mHandler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						mHandler.sendEmptyMessage(CAPTURE_EVENT);
					}
				}, 300);
				finish();
				break;
			default:
				break;
			}
		}

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}

	final void log(Object... args) {
		if (args == null)
			return;
		for (Object obj : args) {
			if (obj != null) {
				Log.v(TAG, obj.toString());
			}
		}
	}

}
