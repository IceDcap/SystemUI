package com.amigo.navi.keyguard;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.amigo.navi.keyguard.AmigoKeyguardBouncer.KeyguardBouncerCallback;
import com.amigo.navi.keyguard.util.AmigoKeyguardUtils;
import com.amigo.navi.keyguard.util.KeyguardWidgetUtils;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardHostView;
import com.android.keyguard.ViewMediatorCallback;

public class KeyguardViewHost extends FrameLayout {
	
    private Configuration mConfiguration = null;
    private String mOldFontStyle="";
    
    final static boolean DEBUG=true;
    private static final String LOG_TAG="KeyguardViewHost";
    
    private int mTouchCallTime = 0;
    
    
    private Context mContext;
    
    private AmigoKeyguardHostView mAmigoKeyguardView;
    private ViewMediatorCallback mViewMediatorCallback;
    
    
    public KeyguardViewHost(Context context) {
        this(context, null);
    }

    public KeyguardViewHost(Context context, AttributeSet attrs) {
       this(context, attrs,0);
       mConfiguration = new Configuration(getContext().getResources().getConfiguration());
       mOldFontStyle  = AmigoKeyguardUtils.getmOldFontStyle();;
       
       if(DEBUG){
       	Log.d(LOG_TAG, "onConfigurationChanged() ..KeyguardHostView..mOldFontStyle="+mOldFontStyle);            	
       }
    }

    public KeyguardViewHost(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext=context;
        amigoInflateKeyguardView(null);

    }
    
    
    public void onConfigurationChanged() {
    	Configuration newConfig = mContext.getResources().getConfiguration();
		if (DEBUG)
			DebugLog.d(LOG_TAG, "onConfigurationChanged  mConfiguration:"
					+ mConfiguration);
		String currentFontStyle=AmigoKeyguardUtils.getCurrretFontStyle(newConfig,mOldFontStyle);
		boolean isChangeFontStyle=false;
		if(!currentFontStyle.equals(mOldFontStyle)){
			mOldFontStyle=currentFontStyle;
			isChangeFontStyle=true;
			DebugLog.d(LOG_TAG, "onConfigurationChanged() newConfig....amigoFont1111111="+currentFontStyle+"oldFontStyle="+mOldFontStyle);
		}
		if (DEBUG)
			DebugLog.d(LOG_TAG, "onConfigurationChanged  newConfig:" + newConfig+"---"+"isChangeFontStyle:"+isChangeFontStyle);
		if (mConfiguration != null
				&& (!newConfig.locale.equals(mConfiguration.locale) || newConfig.fontScale != mConfiguration.fontScale) || isChangeFontStyle) {
			// only propagate configuration messages if we're currently
			// showing
			mConfiguration.locale = newConfig.locale;
			mConfiguration.fontScale = newConfig.fontScale;
			
			resetKeyguardView();
			
		} else {
			if (DEBUG)
				DebugLog.d(LOG_TAG, "onConfigurationChanged: congfiguration not change");
		}
	}

	private void resetKeyguardView() {
		removeAllViews();
		amigoInflateKeyguardView(null);
		KeyguardViewHostManager.getInstance().initKeyguardReset();
	}
    
    
    private void amigoInflateKeyguardView(Bundle options) {
        mAmigoKeyguardView = new AmigoKeyguardHostView(mContext);
        mAmigoKeyguardView.setOrientation(LinearLayout.VERTICAL);
//        mAmigoKeyguardView.setLockPatterUtils(mLockPatternUtils);
        addView(mAmigoKeyguardView);
    }

    public void initKeyguard(ViewMediatorCallback callback,LockPatternUtils lockPatternUtils){
    	setViewMediatorCallback(callback);
    	if(mAmigoKeyguardView!=null){
    		mAmigoKeyguardView.initKeyguard(callback, lockPatternUtils);
    	}
    	
    }
    
    public void setViewMediatorCallback(ViewMediatorCallback callback){
        mViewMediatorCallback=callback;
    }
//
//    
//    public ViewGroup tempGetAmigoKeyguardHostView(){
//        return mAmigoKeyguardView;
//    }
    
    public void show(Bundle options){
        setVisibility(View.VISIBLE);
        if(mAmigoKeyguardView==null){
            amigoInflateKeyguardView(options);
        }
        mAmigoKeyguardView.show();
    }
    
    public void showBouncerOrKeyguard(){
    	show(null);
    	mAmigoKeyguardView.showBouncerOrKeyguard();
    }
    	
    
    public void hide() {
        setVisibility(View.GONE);
        mAmigoKeyguardView.hide();
        updateNotifiOnkeyguard(false);
    }
    
    
    public void onScreenTurnedOff(){
        mAmigoKeyguardView.onScreenTurnedOff();
    }
    
    public void onScreenTurnedOn(){
    	 mAmigoKeyguardView.onScreenTurnedOn();
    }
    
    
    public void dismissWithAction(KeyguardHostView.OnDismissAction r){
        DebugLog.d(LOG_TAG, "dismissWithAction--- mAmigoKeyguardView != null"+(mAmigoKeyguardView != null));
        if(mAmigoKeyguardView != null){
            mAmigoKeyguardView.dismissWithAction(r);
        }
    }
    
  
    
    public void dismiss(){
        mAmigoKeyguardView.dismiss();
    }
    
    public boolean needsFullscreenBouncer(){
       return mAmigoKeyguardView.needsFullscreenBouncer();
    }
    private void updateNotifiOnkeyguard(boolean isShow) {
//      if (isShow) {
//          KeyguardUpdateMonitor.getInstance(mContext).getNotificationModule()
//                  .setLastKeyguardShowTime(System.currentTimeMillis());
//          KeyguardUpdateMonitor.getInstance(mContext).getNotificationModule().updateNotifications();
//      } else {
//          KeyguardUpdateMonitor.getInstance(mContext).getNotificationModule().removeAllNotifications();
//      }
  }
    
    public void unLockByOther(boolean animation){
        if(animation){
            mAmigoKeyguardView.scrollToUnlockByOther();
        }else{
            mAmigoKeyguardView.scrollToSnapshotPage();
        }
    }
    
    public void showBouncer(boolean resetSecuritySelection){
        mAmigoKeyguardView.showBouncer(resetSecuritySelection);
    }
    
    public void resetHostYToHomePosition(){
        mAmigoKeyguardView.resetHostYToHomePosition();
    }
    
    public void scrollToKeyguardPageByAnimation(){
    	if(mAmigoKeyguardView != null){
    		mAmigoKeyguardView.scrollToKeyguardPage(300);
    	}
    }
    
    public void scrollToUnlockHeightByOther(boolean withAnim){
    	if(mAmigoKeyguardView != null){
    		if(withAnim){
    			mAmigoKeyguardView.scrollToUnlockByOther();
    		}else{
    			mAmigoKeyguardView.scrollToSnapshotPage();
    		}
    	}
    }
    
	public boolean isAmigoHostYAtHomePostion() {
		if (mAmigoKeyguardView != null) {
			return mAmigoKeyguardView.isHostYAtHomePostion();
		}
		return false;
	}

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        KeyguardWidgetUtils.getInstance(mContext).startHostListening();
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        KeyguardWidgetUtils.getInstance(mContext).stopHostListening();
    }

    public boolean isSecure(){
    	return mAmigoKeyguardView.isSecure();
    }
    
    public long getUserActivityTimeout(){
    	return mAmigoKeyguardView.getUserActivityTimeout();
    }
    
    public boolean keyguardBouncerIsShowing(){
    	return mAmigoKeyguardView.keyguardBouncerIsShowing();
    }

    public void registerBouncerCallback(KeyguardBouncerCallback bouncerCallback){
    	mAmigoKeyguardView.registerBouncerCallback(bouncerCallback);
    }
    
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
    	Log.i("jiating", "hostView...dispatchKeyEvent...event.getKeyCode()="+event.getKeyCode());
    	 boolean down = event.getAction() == KeyEvent.ACTION_DOWN;
         switch (event.getKeyCode()) {
             case KeyEvent.KEYCODE_BACK:
                 if (!down && mAmigoKeyguardView!=null) {
                	 mAmigoKeyguardView.onBackPress();
                	 
                 }
                 return true;
         }
     
    	return super.dispatchKeyEvent(event);
    }
    
    public boolean  onBackPress(){
    	Log.i("jiating", "hostView...onBackPress");
    	if ( mAmigoKeyguardView!=null) {
       	 return mAmigoKeyguardView.onBackPress(); 
        }
    	return false;
    }
    
    
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        userActivity(ev);
        return super.dispatchTouchEvent(ev);
    }
    
    private void userActivity(MotionEvent ev){
        switch (ev.getActionMasked()) {
        case MotionEvent.ACTION_DOWN:
            mViewMediatorCallback.userActivity();
            break;
        default:
            if (mTouchCallTime++ % 50 == 0) {
                mTouchCallTime = 1;
                mViewMediatorCallback.userActivity();
            }
            break;
        }
    }
    
    public void showBouncerOrKeyguardDone(){
    	if ( mAmigoKeyguardView!=null) {
          	  mAmigoKeyguardView.showBouncerOrKeyguardDone(); 
           }
    }
    

    public void shakeFingerIdentifyTip() {
        mAmigoKeyguardView.shakeFingerIdentifyTip();
    }
    
    
    private AnimatorSet mScaleHostAnimator = null;

    public void unlockByFingerIdentify() {
        if (mScaleHostAnimator == null) {
            ObjectAnimator animator1 = ObjectAnimator.ofFloat(this, "scaleX", 1f, 0.6f);
            ObjectAnimator animator2 = ObjectAnimator.ofFloat(this, "scaleY", 1f, 0.6f);
            ObjectAnimator animator3 = ObjectAnimator.ofFloat(this, "alpha", 1f, 0.4f);
            mScaleHostAnimator = new AnimatorSet();
            mScaleHostAnimator.setDuration(200);
            mScaleHostAnimator.playTogether(animator1, animator2, animator3);
            mScaleHostAnimator.addListener(new Animator.AnimatorListener() {

                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mViewMediatorCallback.keyguardDone(true);
                    setVisibility(View.GONE);
                    resetHostView();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    resetHostView();
                }
            });
        }
        mScaleHostAnimator.start();

    }

    private void resetHostView() {
        setScaleX(1f);
        setScaleY(1f);
        setAlpha(1f);
    }
    
    public void fingerPrintFailed() {
        mAmigoKeyguardView.fingerPrintFailed();
    }

    public void fingerPrintSuccess() {
        mAmigoKeyguardView.fingerPrintSuccess();
    }
}
