package com.amigo.navi.keyguard.picturepage.widget;

import java.util.ArrayList;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.KWDataCache;
import com.amigo.navi.keyguard.haokan.UIController;


import android.content.Context;
import android.database.DataSetObserver;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.OverScroller;

public class HorizontalListView extends AdapterView<ListAdapter> {

    private static final String TAG = "HorizontalListView";

    /**
     * The adapter containing the data to be displayed by this view
     */

    private static final int BUFFER_SIZE = 2;
    private static final int RECYCLE_OR_ADD_VIEW_THRESHOLD = 300;

    private static final int INVALID_POINTER = -1;
    private static final int MAX_VELOCITY = 4000;
    private static final int MIN_SCROLL_TIME = 200;

    private static final float INFLEXION = 0.35f; // Tension lines cross at
    // (INFLEXION, 1)
    private static float DECELERATION_RATE = (float) (Math.log(0.78) / Math
            .log(0.9));

    private float mFlingFriction = ViewConfiguration.getScrollFriction();
    // A context-specific coefficient adjusted to physical values.
    private float mPhysicalCoeff;

    private int mVisibleViewCount = 1;

    protected ListAdapter mAdapter;
    private AdapterDataSetObserver mDataSetObserver;

    private ArrayList<View> mRecyclerViews = new ArrayList<View>();

    private int mFirstPosition = 0;
    // private int mOffsetX = 0;
    protected int mChildWidth = 0;

    private int mLeftAndRightOffset = 0;

    private int mLastMotionX = 0;
    private int mDownMotionX = 0;

    private int mTouchSlop = 0;

    private OverScroller mScroller;
    OnScrollListener mScrollListener;
    private boolean mIsFling = false;

    private Context mContext;
    private VelocityTracker mVelocityTracker;
    private int mActivePointerId = INVALID_POINTER;
    protected int mScreenWid = 0;
    protected int mScreenHei = 0;
    private boolean isCanLoop = false;
    private int mPage;
    protected boolean isBeginScroll = false;
    protected int mCurrentPage = 0;
    protected int mNextPage = INVALID_PAGE;

    private int FLING_THRESHOLD_VELOCITY  = 300;

    private float mDensity;

    private int mFlingThresholdVelocity;
    public static final int INVALID_PAGE = -1;
    private static final int MIN_LENGTH_FOR_FLING = 25;

    UIController controller;
    public HorizontalListView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        init(context);
    }

    public HorizontalListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        init(context);
    }

    public HorizontalListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        init(context);
    }
    private int mMinDistance = 0;
    private void init(Context context) {
        mContext = context;
        mScroller = new OverScroller(getContext(), new DecelerateInterpolator());
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        final float ppi = context.getResources().getDisplayMetrics().density * 160.0f;
        mPhysicalCoeff = SensorManager.GRAVITY_EARTH // g (m/s^2)
                * 39.37f // inch/meter
                * ppi * 0.84f; // look and feel tuning
        mDensity = getContext().getResources().getDisplayMetrics().density;
        mFlingThresholdVelocity = (int) (FLING_THRESHOLD_VELOCITY * mDensity);
        controller = UIController.getInstance();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        onLayout();
    }

	private void onLayout() {
    	if(PRINT_LOG){
    		DebugLog.d(TAG, "onLayout");
    	}
        int childLeft = mFirstPosition * mChildWidth;
		DebugLog.d(TAG, "test position mFirstPosition:" + mFirstPosition);
        final int count = getChildCount();
    	if(PRINT_LOG){
    		DebugLog.d(TAG, "onLayout getChildCount():" + getChildCount());
    		DebugLog.d(TAG, "onLayout getScrollX():" + getScrollX());
    	}
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
        	if(PRINT_LOG){
        		DebugLog.d(TAG, "onLayout i:" + i);
        	}
            if (child.getVisibility() != View.GONE) {
            	if(PRINT_LOG){
            		DebugLog.d(TAG, "onLayout 1");
            	}
//                mChildWidth = child.getMeasuredWidth();
            	mChildWidth = KWDataCache.getScreenWidth(getResources());
                int childHei = child.getMeasuredHeight();
            	if(PRINT_LOG){
            		DebugLog.d(TAG,"onLayout child:" + child);
            		DebugLog.d(TAG,"onLayout childLeft:" + childLeft);
            	}
                child.layout(childLeft, 0, childLeft + mChildWidth, childHei);
                childLeft += mChildWidth;
            }
        }
       

        int shouldVisibleViewCount = getWidth() / mChildWidth + 1;
    	if(PRINT_LOG){
    		DebugLog.d(TAG, "onLayout shouldVisibleViewCount:"
    				+ shouldVisibleViewCount);
    	}
    	if(PRINT_LOG){
    		DebugLog.d(TAG, "onLayout mVisibleViewCount:" + mVisibleViewCount);
    	}
        if (mVisibleViewCount < shouldVisibleViewCount) {
            while (mVisibleViewCount < shouldVisibleViewCount) {
            	if(PRINT_LOG){
            		DebugLog.d(TAG, "onLayout getChildCount:" + getChildCount());
            	}
                makeAndAddView(mFirstPosition + getChildCount(), true);
                mVisibleViewCount++;
            }
            requestLayout();
        }
	}

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        // super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMinDistance = (int) (mChildWidth * 0.1);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
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

            final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                    widthSize, childWidthMode);
            final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                    heightSize, childHeightMode);

            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            // getChildAt(i).measure(200, heightMeasureSpec);
        }
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public ListAdapter getAdapter() {
        // TODO Auto-generated method stub
        return mAdapter;
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        // TODO Auto-generated method stub
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }

        mAdapter = adapter;

        if (mAdapter != null) {
            mDataSetObserver = new AdapterDataSetObserver();
            mAdapter.registerDataSetObserver(mDataSetObserver);
        }

        if (mAdapter == null || mAdapter.getCount() == 0)
            return;

        setSelection(0);
    }

    class AdapterDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            DebugLog.d(TAG,"AdapterDataSetObserver mFirstPosition:" + mFirstPosition);
//            if(mFirstPosition < 0){
//                mFirstPosition = 0;
//            }
//            mFirstPosition = 0;
            if(mAdapter.getCount() == 1){
            	setSelection(0);
            }else{
                setSelection(mFirstPosition);
            }
//            setSelection(0);
        }

        @Override
        public void onInvalidated() {
            setSelection(0);
        }
    }

    @Override
    public View getSelectedView() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setSelection(int position) {
        mFirstPosition = position;
        int childCount = getChildCount();
    	if(PRINT_LOG){
    		DebugLog.d(TAG, "setSelection mFirstPosition:" + mFirstPosition);
    		DebugLog.d(TAG, "setSelection childCount:" + childCount);
    	}
        for (int index = 0; index < childCount; index++) {
            View view = getChildAt(0);
            mRecyclerViews.add(view);
            detachViewFromParent(view);
        }
    	if(PRINT_LOG){
    		DebugLog.d(TAG, "setSelection mVisibleViewCount:" + mVisibleViewCount);
    	}
        int dataCount = mAdapter.getCount();
        int addChildCount = mVisibleViewCount + BUFFER_SIZE;
        if(dataCount < 3){
            addChildCount = dataCount;
        }
    	if(position < 0){
    		position = mAdapter.getCount() + position;
    	}
        for (int index = 0; index < addChildCount; index++) {
        	if(PRINT_LOG){
        		DebugLog.d(TAG, "setSelection position:" + position);
        		DebugLog.d(TAG, "setSelection index:" + index);
        	}
            View view = makeAndAddView(position + index, true);
        }

        requestLayout();
    }

    public void setLeftAndRightOffset(int leftAndRightOffset) {
        mLeftAndRightOffset = leftAndRightOffset;
    }

    public void setOnScrollListener(OnScrollListener listener) {
        mScrollListener = listener;
    }

    public int getChildPositon(int x, int y) {
        int count = getChildCount();
        x += getScrollX();
        x += getScrollY();
        for (int index = 0; index < count; index++) {
            View view = getChildAt(index);
            if (x > view.getLeft() && x < view.getRight() && y > view.getTop()
                    && y < view.getBottom()) {
                return index;
            }
        }
        return -1;
    }

    public View getChild(int x, int y) {
        int position = getChildPositon(x, y);
        if (position == -1) {
            return null;
        }
        return getChildAt(position);
    }

    public int getChildWidth() {
        return mChildWidth;
    }

    private View makeAndAddView(int position, boolean isEnd) {
    	if(PRINT_LOG){
    		DebugLog.d(TAG, "makeAndAddView position:" + position);
    		DebugLog.d(TAG, "makeAndAddView mAdapter.getCount():" + mAdapter.getCount());
    	}
        if (position >= mAdapter.getCount()) {
            if (isCanLoop) {
                if(mAdapter.getCount() == 0){
                    return null;
                }
                position = position % mAdapter.getCount();
            } else {
                return null;
            }
        }
        if(position < 0){
            position = position + mAdapter.getCount();
        }

        View convertView = null;
        if (mRecyclerViews.size() > 0) {
            convertView = mRecyclerViews.remove(0);
        }
        boolean recycled = false;

        View child = mAdapter.getView(position, convertView, this);
    	if(PRINT_LOG){
    		DebugLog.d(TAG,"makeAndAddView------begin");
        	DebugLog.d(TAG,"makeAndAddView position:" + position);
        	DebugLog.d(TAG,"makeAndAddView left:" + child.getLeft());
        	DebugLog.d(TAG,"makeAndAddView right:" + child.getRight());
    	}
        if (convertView != null && child != null && convertView == child) {
        	if(PRINT_LOG){
        		DebugLog.d(TAG,"makeAndAddView------recycled");
        	}
            recycled = true;
        }
    	if(PRINT_LOG){
    		DebugLog.d(TAG,"makeAndAddView------end");
    	}
        return setupChild(child, isEnd, recycled);
    }
    
    private static final boolean PRINT_LOG = false;
    private View setupChild(View child, boolean isEnd, boolean recycled) {
        ViewGroup.LayoutParams p = (ViewGroup.LayoutParams) child
                .getLayoutParams();
        if (p == null) {
            p = new AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 0);
        }
    	View firstChildView = getChildAt(0);
    	int firstChildLeft = 0;
    	if(firstChildView != null){
    		firstChildLeft = firstChildView.getLeft();
    	}
    	int childCount = getChildCount();
    	if(PRINT_LOG){
		DebugLog.d(TAG,"setupChild lastChildView childCount:" + childCount);
    	}
    	View lastChildView = getChildAt(childCount - 1);
    	int lastChildLeft = 0;
    	if(lastChildView != null){
    		lastChildLeft = lastChildView.getLeft();
    	}
    	if(PRINT_LOG){
		DebugLog.d(TAG,"setupChild lastChildView begin");
		DebugLog.d(TAG,"setupChild lastChildView isEnd:" + isEnd);
		DebugLog.d(TAG,"setupChild lastChildView getScrollX():" + getScrollX());
    	}
		int scrollX = getScrollX();
    	if(isEnd){
        	if(PRINT_LOG){
        		DebugLog.d(TAG,"setupChild lastChildView lastChildView:" + lastChildView);
        	}
			if(lastChildView != null){
	        	if(PRINT_LOG){
    			DebugLog.d(TAG,"setupChild lastChildView left:" + lastChildLeft);
	        	}
	        	if(PRINT_LOG){
	    			DebugLog.d(TAG,"setupChild lastChildView child left:" + child.getLeft());
	        	}
    			child.setLeft(lastChildLeft + mScreenWid);
    			child.setRight(lastChildLeft + 2 * mScreenWid);
	        	if(PRINT_LOG){
    			DebugLog.d(TAG,"setupChild lastChildView child1 left:" + child.getLeft());
	        	}
	        }else{
	        	if(PRINT_LOG){
	        		DebugLog.d(TAG,"setupChild lastChildView child2 left:" + child.getLeft());
	        	}
    			child.setLeft(scrollX);
    			child.setRight(scrollX + mScreenWid);
    			recycled = false;
	        	if(PRINT_LOG){
    			DebugLog.d(TAG,"setupChild lastChildView child3 recycled:" + recycled);
    			DebugLog.d(TAG,"setupChild lastChildView child3 left:" + child.getLeft());
	        	}
	        }
    	}else{
        	if(PRINT_LOG){
			DebugLog.d(TAG,"setupChild lastChildView firstChildView:" + firstChildView);
        	}
			if(firstChildView != null){
	        	if(PRINT_LOG){
    			DebugLog.d(TAG,"setupChild firstChildView left:" + firstChildLeft);
	        	}
	        	if(PRINT_LOG){
	        		DebugLog.d(TAG,"setupChild firstChildView1 child left:" + child.getLeft());
	        	}
    			child.setLeft(firstChildLeft - mScreenWid);
    			child.setRight(firstChildLeft);
	        	if(PRINT_LOG){
	        		DebugLog.d(TAG,"setupChild firstChildView1 child left:" + child.getLeft());
	        	}
	        	}else{
		        	if(PRINT_LOG){
		        		DebugLog.d(TAG,"setupChild firstChildView2 child left:" + child.getLeft());
		        	}
    			child.setLeft(scrollX);
    			child.setRight(scrollX + mScreenWid);
    			recycled = false;
	        	if(PRINT_LOG){
	        		DebugLog.d(TAG,"setupChild firstChildView3 child recycled:" + recycled);
	        		DebugLog.d(TAG,"setupChild firstChildView3 child left:" + child.getLeft());
	        	}
	        }
    	}
    	if(PRINT_LOG){
    		DebugLog.d(TAG,"setupChild lastChildView end recycled:" + recycled);
    	}
        if (recycled) {
            attachViewToParent(child, isEnd ? -1 : 0, p);
        } else {
            addViewInLayout(child, isEnd ? -1 : 0, p, true);
        }

        child.postInvalidate();

        return child;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return false;//never accept touch event by parent dispatch
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return interceptTouchEvent(event);
    }

    /**
     * @param event
     * @return
     */
    public boolean interceptTouchEvent(MotionEvent event) {
        boolean flag = false;
        final int action = event.getAction();
        int motionX = (int) event.getX();
        if(PRINT_LOG){
        	Log.d(TAG, "onInterceptTouchEvent -> action = " + action);
        	Log.d(TAG, "onInterceptTouchEvent -> motionX = " + motionX);
        }
        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
            initOrResetVelocityTracker();
            mActivePointerId = event.getPointerId(0);
            motionDown(motionX);
            break;
        case MotionEvent.ACTION_MOVE:
            flag = isBeginScroll(motionX);
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            isBeginScroll = false;
            break;
        }
        return flag;
    }

    public boolean isBeginScroll(int motionX) {
        if (Math.abs(motionX - mDownMotionX) > 2 * mTouchSlop) {
            if (mScrollListener != null) {
                mScrollListener.onScrollBegin();
            }
            isBeginScroll = true;
            return true;
        }else{
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        return dealwithTouchEvent(event);
    }

    
    public boolean dealwithTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        if(PRINT_LOG){
        	DebugLog.d(TAG, "motionUp -> action1 = " + action);
        }
        if (controller.isArcExpanded()) {
            return false;
        }
        if(PRINT_LOG){
        	DebugLog.d(TAG, "motionUp -> action2 = " + action);
        }
        acquireVelocityTrackerAndAddMovement(event);
        int motionX = (int) event.getX();
        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
            initOrResetVelocityTracker();
            mActivePointerId = event.getPointerId(0);
            motionDown(motionX);
            break;
        case MotionEvent.ACTION_MOVE:
            motionMove(motionX);
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            motionUp(motionX);
            recycleVelocityTracker();
            isBeginScroll = false;
            mMoveLastMotionX = 0;
            break;
        }

        return true;
    }

    public boolean isBeginScroll() {
        return isBeginScroll;
    }

    private void motionDown(int motionX) {
        mDownMotionX = motionX;
        mLastMotionX = motionX;
        stopScroll();
    }

    /**
     * 
     */
    public void stopScroll() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
    }

    private int mMoveLastMotionX = 0;
    private void motionMove(int motionX) {

        int dx = motionX - mLastMotionX;
        if (scrollOutBound(getScrollX() - dx) && !isCanLoop) {
            dx /= 2;
        }
        scrollBy(-dx, 0);
        mLastMotionX = motionX;
        mMoveLastMotionX = motionX;
        if (mTouchlListener != null) {
            mTouchlListener.OnTouchMove(motionX-mDownMotionX,dx);
        }
    }

    private void motionUp(int motionX) {
        final float x = motionX;
        final VelocityTracker velocityTracker = mVelocityTracker;
        velocityTracker.computeCurrentVelocity(1000, MAX_VELOCITY);
        int velocityX = (int) velocityTracker.getXVelocity(mActivePointerId);
        final int deltaX = (int) (x - mDownMotionX);
//        boolean isSignificantMove = Math.abs(deltaX) > minDistance;
//        boolean isFling = Math.abs(velocityX) > mFlingThresholdVelocity;
        boolean returnToOriginalPage = false;
//        if (isSignificantMove && Math.signum(velocityX) != Math.signum(deltaX) && isFling) {
//            returnToOriginalPage = true;
//        }
        int finalPage = 0;
        int childCount = mAdapter.getCount();
        if (deltaX > mMinDistance || velocityX > 3000) {
            finalPage = returnToOriginalPage ? mCurrentPage : mCurrentPage - 1;
            if(!isCanLoop && mCurrentPage < 0){
            	finalPage = mCurrentPage;
            }
            if(PRINT_LOG){
            	DebugLog.d(TAG,"testscroll motionUp1:" + finalPage);
            }
            snapToPage(finalPage,motionX);
            mIsFling = true;
        }else if (deltaX < -mMinDistance || velocityX < -3000) {
            finalPage = returnToOriginalPage ? mCurrentPage : mCurrentPage + 1;
            if(!isCanLoop && mCurrentPage > childCount - 1){
            	finalPage = mCurrentPage;
            }
            if(PRINT_LOG){
            	DebugLog.d(TAG,"testscroll motionUp2:" + finalPage);
            }
            snapToPage(finalPage,motionX);
            mIsFling = true;
        }else{
            snapToPage(mCurrentPage,motionX);
            mIsFling = true;
        }
    }

    protected void snapToPage(int page,int motionX){
        mNextPage = page;
        mNextPage = reviseFinalPage(mNextPage);
        if(PRINT_LOG){
        	DebugLog.d(TAG,"testscroll snapToPage mNextPage:" + mNextPage);
        }
        int scrollToX = page * mChildWidth; 
        if (mTouchlListener != null && getScrollX() != scrollToX) {
//            if (Math.abs(motionX - mDownMotionX) > 2 * mTouchSlop) {
            mTouchlListener.OnTouchUp();
//            }
        }
        smoothScrollTo(scrollToX, 0);
        
    }
    
    protected int getFinalScrollX(int scrollX) {
        return scrollX;
    }

    private int getDistanceFromVelocity(int initialVelocity) {
        double totalDistance = getSplineFlingDistance(initialVelocity);

        return (int) -(totalDistance * Math.signum(initialVelocity));
    }

    private double getSplineDeceleration(int velocity) {
        return Math.log(INFLEXION * Math.abs(velocity)
                / (mFlingFriction * mPhysicalCoeff));
    }

    private double getSplineFlingDistance(int velocity) {
        final double l = getSplineDeceleration(velocity);
        final double decelMinusOne = DECELERATION_RATE - 1.0;
        return mFlingFriction * mPhysicalCoeff
                * Math.exp(DECELERATION_RATE / decelMinusOne * l);
    }

    private boolean scrollOutBound(int scrollX) {
        return scrollOutLeftBound(scrollX) || scrollOutRightBound(scrollX);
    }

    private boolean scrollOutLeftBound(int scrollX) {
        return scrollX < 0;
    }

    private boolean scrollOutRightBound(int scrollX) {
        return scrollX > getMaxScrollX();
    }

    private boolean scrollOutFirstPageBound(int scrollX) {
        DebugLog.d(TAG,"testscroll scrollOutFirstPageBound scrollX:" + scrollX);
        DebugLog.d(TAG,"testscroll scrollOutFirstPageBound getMaxScrollX():" + getMaxScrollX());
        DebugLog.d(TAG,"testscroll scrollOutFirstPageBound mScreenWid:" + mScreenWid);
        return scrollX <= -mScreenWid;
    }

    private boolean scrollOutLastPageBound(int scrollX) {
        DebugLog.d(TAG,"testscroll scrollOutLastPageBound scrollX:" + scrollX);
        DebugLog.d(TAG,"testscroll scrollOutLastPageBound getMaxScrollX():" + getMaxScrollX());
        DebugLog.d(TAG,"testscroll scrollOutLastPageBound mScreenWid:" + mScreenWid);
        return scrollX >= getMaxScrollX() + mScreenWid;
    }

    private int getMaxScrollX() {
        if (mAdapter == null) {
            return 0;
        }
        int adapterCount = mAdapter.getCount();
        int totalWidth = mChildWidth * adapterCount + mLeftAndRightOffset * 2;
        return totalWidth - getWidth();
    }

    public void setPosition(int position) {
    	DebugLog.d(TAG, "setPosition mChildWidth:" + mChildWidth);
    	DebugLog.d(TAG, "setPosition position:" + position);
        mCurrentPage = position;
        smoothScrollTo(mChildWidth * position, 0, 0);
    }

    public void smoothScrollTo(int x, int y) {
        if (!isCanLoop) {
            if (scrollOutLeftBound(x)) {
                x = 0;
            }
            if (scrollOutRightBound(x)) {
                x = getMaxScrollX();
            }
        }
        x = getFinalScrollX(x);
        int scrollX = getScrollX();
        if(PRINT_LOG){
        	DebugLog.d(TAG,"testscroll smoothScrollTo x:" + x);
        	DebugLog.d(TAG,"testscroll smoothScrollTo scrollX:" + scrollX);
        }
        mScroller.startScroll(scrollX, 0, x - scrollX, 0, MIN_SCROLL_TIME);
        postInvalidate();
    }

    public void smoothScrollTo(int x, int y, int time) {
        setSelection(mCurrentPage);
        onLayout();
        int scrollX = getScrollX();
        x = getFinalScrollX(x);
        scrollTo(x, getScrollY());
    	DebugLog.d(TAG, "smoothScrollTo scrollX:" + scrollX);
    	DebugLog.d(TAG, "smoothScrollTo x:" + x);
        postInvalidate();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        // TODO Auto-generated method stub
        super.onScrollChanged(l, t, oldl, oldt);
    }

    @Override
    public void scrollTo(int x, int y) {
        DebugLog.d(TAG,"testscroll scrollTo");
        //<Keyguard> <jingyn> modify for CR01491250 begin 
        if(mAdapter.getCount()==1)return;
        //<Keyguard> <jingyn> modify for CR01491250 end 
        if (scrollOutFirstPageBound(x)) {
            DebugLog.d(TAG,"testscroll scrollTo1");
            x = getMaxScrollX();
            int count = mAdapter.getCount();
//            mCurrentPage = Math.max(0, Math.min(mNextPage, count - 1));
            mCurrentPage = count - 1;
//            scrollTo(x, y);
//            return;
            setSelection(mCurrentPage);
            onLayout();
        }
        if (scrollOutLastPageBound(x)) {
            DebugLog.d(TAG,"testscroll scrollTo2");
            x = 0;
//            int count = mAdapter.getCount();
//            mCurrentPage = Math.max(0, Math.min(mNextPage, count - 1));
              mCurrentPage = 0;
              setSelection(mCurrentPage);
              onLayout();
//            scrollTo(x, y);
//            return;

        }
        DebugLog.d(TAG,"testscroll scrollTo x:" + x);
        super.scrollTo(x, y);
        recycleOrAddView();
        if (mScrollListener != null) {
            mScrollListener.onScrollMoving(x);
        }
    }

    @Override
    public void scrollBy(int x, int y) {
        // TODO Auto-generated method stub
        scrollTo(getScrollX() + x, getScrollY() + y);
    }

    @Override
    public void computeScroll() {
        // TODO Auto-generated method stub
//        DebugLog.d(DEBUG_TAG,"computeScroll getScrollX():" + getScrollX());
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), 0);
            postInvalidate();
        }else{
        	if(mNextPage != INVALID_PAGE){
        		int count = mAdapter.getCount();
        		mCurrentPage = Math.max(0, Math.min(mNextPage, count - 1));
        		mNextPage = INVALID_PAGE;
        	}
            if (mScrollListener != null && mIsFling) {
                mIsFling = false;
                mScrollListener.onScrollEnd();
            }
        }
    }
    
    private void recycleOrAddView() {
//        DebugLog.d(TAG, "onLayout recycleOrAddView begin");
        int count = getChildCount();
        DebugLog.d(TAG, "onLayout recycleOrAddView count:" + count);
        if (count <= 0) {
            return;
        }

        boolean requestLayout = false;

        View firstView = getChildAt(0);
        View lastView = getChildAt(count - 1);

        int firstViewLeftOffsetX = firstView.getLeft() - getScrollX();
        int firstViewRightOffsetX = firstView.getRight() - getScrollX();
        
        int lastViewLeftOffsetX = lastView.getLeft() - getScrollX();
        int lastViewRightOffsetX = lastView.getRight() - getScrollX();
        DebugLog.d(TAG, "test position count:" + count);
		DebugLog.d(TAG, "test position lastView.getRight():" + lastView.getRight());
		DebugLog.d(TAG, "test position getScrollX():" + getScrollX());
		DebugLog.d(TAG, "test position lastViewRightOffsetX:" + lastViewRightOffsetX);
		//        DebugLog.d(TAG, "recycleOrAddView mFirstPosition1:" + mFirstPosition);
        if (firstViewRightOffsetX < -RECYCLE_OR_ADD_VIEW_THRESHOLD) {
            DebugLog.d(TAG, "recycleOrAddView firstView");
            detachViewFromParent(firstView);
            mRecyclerViews.add(firstView);
            mFirstPosition++;
            // mOffsetX = mOffsetX + firstView.getWidth();
        }

//        DebugLog.d(TAG, "recycleOrAddView lastViewLeftOffsetX:" + lastViewLeftOffsetX);
        if (lastViewLeftOffsetX > getWidth() + RECYCLE_OR_ADD_VIEW_THRESHOLD) {
            DebugLog.d(TAG, "recycleOrAddView lastView");
            detachViewFromParent(lastView);
            mRecyclerViews.add(lastView);
        }

//        DebugLog.d(TAG, "recycleOrAddView test firstViewLeftOffsetX:" + firstViewLeftOffsetX);
//        DebugLog.d(TAG, "recycleOrAddView test mFirstPosition:" + mFirstPosition);
        while (firstViewLeftOffsetX > -RECYCLE_OR_ADD_VIEW_THRESHOLD && (mFirstPosition > 0
                || isCanLoop)) {
                mFirstPosition--;
//                DebugLog.d(TAG, "recycleOrAddView mFirstPosition2:" + mFirstPosition);
                // mOffsetX = mOffsetX - firstView.getWidth();
                if (mFirstPosition >= 0) {
                    makeAndAddView(mFirstPosition, false);
                } else {
                    int position = (mAdapter.getCount() - Math
                            .abs(mFirstPosition) % mAdapter.getCount());
                    DebugLog.d(TAG, "recycleOrAddView position:" + position);
                    makeAndAddView(position, false);
                }
                firstViewLeftOffsetX -= mChildWidth;
                requestLayout = true;
        }

//        DebugLog.d(TAG, "recycleOrAddView test lastViewRightOffsetX:" + lastViewRightOffsetX);
//        DebugLog.d(TAG, "recycleOrAddView test lastViewRightOffsetX getScrollX():" + getScrollX());
//        DebugLog.d(TAG, "recycleOrAddView test getWidth():" + getWidth());
        while (lastViewRightOffsetX < getWidth() + RECYCLE_OR_ADD_VIEW_THRESHOLD) {
//            DebugLog.d(TAG, "recycleOrAddView mFirstPosition3:" + mFirstPosition);
//            DebugLog.d(TAG, "recycleOrAddView mFirstPosition3 getChildCount:" + getChildCount());
            makeAndAddView(mFirstPosition + getChildCount(), true);
            lastViewRightOffsetX += mChildWidth;
            requestLayout = true;
        }
//        DebugLog.d(TAG, "recycleOrAddView requestLayout:" + requestLayout);
        if (requestLayout) {
            requestLayout();
        }
        // invalidate();

    }

    private void acquireVelocityTrackerAndAddMovement(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
    }

    /**
     * Recycle velocity tracker.
     */
    protected void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
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

    public interface OnScrollListener {
        public void onScrollBegin();

        public void onScrollMoving(int motionX);

        public void onScrollEnd();
        
    }
    
    public interface OnTouchlListener {
        void OnTouchMove(int x,int dx);
        void OnTouchUp();
    }
    
    OnTouchlListener mTouchlListener;
    
    public void setTouchlListener(OnTouchlListener mTouchlListener) {
        this.mTouchlListener = mTouchlListener;
    }

    public boolean isCanLoop() {
        return isCanLoop;
    }

    public void setCanLoop(boolean isCanLoop) {
        this.isCanLoop = isCanLoop;
    }
    
    public int getPage(){
        DebugLog.d(TAG,"getPage:" + mCurrentPage);
        return mCurrentPage;
    }
    
    public int getPageWhenMoving(){
    	DebugLog.d(TAG,"getPageWhenMoving mCurrentPage:" + mCurrentPage);
        final int deltaX = (int) (mMoveLastMotionX - mDownMotionX);
    	DebugLog.d(TAG,"getPageWhenMoving deltaX:" + deltaX);
    	DebugLog.d(TAG,"getPageWhenMoving mMinDistance:" + mMinDistance);
    	if(deltaX > mMinDistance){
    		return reviseFinalPage(mCurrentPage - 1);
    	}else if(deltaX < -mMinDistance){
    		return reviseFinalPage(mCurrentPage + 1);
    	}else{
    		return reviseFinalPage(mCurrentPage);
    	}
    }
    
    public int getNextPage(){
        if(mNextPage == INVALID_PAGE){
            return mCurrentPage;
        }
        return mNextPage;
    }
    
    private int reviseFinalPage(int finalpage){
        int page = finalpage;
        int count = mAdapter.getCount();
        if(isCanLoop){
        	if (finalpage < 0) {
            	page = count - 1;
        	} else if (finalpage > count - 1) {
            	page = 0;
        	}
        }else{
        	if (finalpage < 0) {
            	page = 0;
        	} else if (finalpage > count - 1) {
            	page = count - 1;
        	}
        }
        return page;
    }
}
