package com.amigo.navi.keyguard.skylight;

import com.amigo.navi.keyguard.DebugLog;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class SkyPagerView extends KeyguardPagerView {

    private static final String LOG_TAG="SkyPagerView";
    private static final int ANIMATOR_DURATION=500;
    
    
    private static final int MSG_TOAST_INDICATOR=0;
    
//    private boolean mIsFinishInitData=false;
    
    
    private KeyguardPagerIndicator mIndicator = null;
    private ValueAnimator mAnimator;
    
    public SkyPagerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAnimator();
    }

    public SkyPagerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAnimator();
    }

    public SkyPagerView(Context context) {
        super(context);
        initAnimator();
    }
    
    private AnimatorListener mAnimatorListener=new AnimatorListener() {
        
        @Override
        public void onAnimationStart(Animator animation) {
            // TODO Auto-generated method stub
            
        }
        
        @Override
        public void onAnimationRepeat(Animator animation) {
            // TODO Auto-generated method stub
            
        }
        
        @Override
        public void onAnimationEnd(Animator animation) {
            // TODO Auto-generated method stub
            
        }
        
        @Override
        public void onAnimationCancel(Animator animation) {
            // TODO Auto-generated method stub
            
        }
    };
    
    private AnimatorUpdateListener mUpdateListener=new AnimatorUpdateListener() {
        
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            Float animationValue=(Float)animation.getAnimatedValue();
            if(DebugLog.DEBUG)Log.d(LOG_TAG, "animationValue: "+animationValue);
            if(mIndicator!=null){
                mIndicator.setAlpha(animationValue);
            }
        }
    };

    private void initAnimator(){
        mAnimator = new ValueAnimator();
        mAnimator.addListener(mAnimatorListener);
        mAnimator.addUpdateListener(mUpdateListener);
       
    }
    @Override
    protected void screenScrolled(int screenCenter) {
        super.screenScrolled(screenCenter);
        updatePageIndicator(screenCenter);
    }
    
    public void setIndicator(KeyguardPagerIndicator indicator) {
        mIndicator = indicator;
        resetIndicator();
    }
    

    public void resetIndicator() {
        if (mIndicator != null) {
            mIndicator.setCountPage(getChildCount());
            mIndicator.setCurPage(getCurrentPageIndex());
        }
    }

    private void updatePageIndicator(int screenCenter) {
        if (mIndicator != null) {
            mIndicator.move(screenCenter);
        }
    }
    
    @Override
    protected void onPageBeginMoving() {
        super.onPageBeginMoving();
        if(DebugLog.DEBUG)Log.d(LOG_TAG, "onPageBeginMoving");
        showPageIndicator(ANIMATOR_DURATION);
    }
    
    private void showPageIndicator(long duration) {
        if (getPageCount() <= 1) {
            return;
        }
        mHandler.removeMessages(MSG_TOAST_INDICATOR);
        mIndicator.setVisibility(View.VISIBLE);
        if (mAnimator.isRunning()) {
            mAnimator.cancel();
        }
        mAnimator.setDuration(duration);
        mAnimator.setFloatValues(0.0f, 1.0f);
        mAnimator.start();
    }

    @Override
    protected void onPageEndMoving() {
        super.onPageEndMoving();
        if(DebugLog.DEBUG)Log.d(LOG_TAG, "onPageEndMoving");
        hidePageIndicator(ANIMATOR_DURATION);
    }
    
    private void hidePageIndicator(long duration) {
        if (getPageCount() <= 1) {
            return;
        }
        mHandler.removeMessages(MSG_TOAST_INDICATOR);
        mIndicator.setVisibility(View.VISIBLE);
        if (mAnimator.isRunning()) {
            mAnimator.cancel();
        }
        mAnimator.setDuration(duration);
        mAnimator.setFloatValues(1.0f, 0.0f);
        mAnimator.start();
    }

    @Override
    protected void notifyPageSwitched() {
        super.notifyPageSwitched();
        if(DebugLog.DEBUG)Log.d(LOG_TAG, "notifyPageSwitched");
    }
    
    
    @Override
    public void onRemoveView(View v, boolean draging) {
        if(DebugLog.DEBUG)Log.d(LOG_TAG, "onRemoveView");
    }

    @Override
    public void onAddView(View v, int index) {
        if(DebugLog.DEBUG)Log.d(LOG_TAG, "onAddView");

    }
    
    public void toast(){
        if (mAnimator.isRunning()) {
            mAnimator.cancel();
        }
        mIndicator.setVisibility(VISIBLE);
        mHandler.sendEmptyMessage(MSG_TOAST_INDICATOR);
    }
    
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_TOAST_INDICATOR:
                hidePageIndicator(2000);
                break;
                default:
                	break;
            }
        };
    };
    

}
