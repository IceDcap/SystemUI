package com.amigo.navi.keyguard;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.amigo.navi.keyguard.AmigoKeyguardBouncer.KeyguardBouncerCallback;
import com.amigo.navi.keyguard.sensor.KeyguardSensorModule;
import com.amigo.navi.keyguard.util.KeyguardWidgetUtils;
import com.android.keyguard.KeyguardHostView;
import com.android.keyguard.ViewMediatorCallback;
import com.android.internal.widget.LockPatternUtils;

public class KeyguardViewHost extends FrameLayout {
    
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
    }

    public KeyguardViewHost(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext=context;
        amigoInflateKeyguardView(null);

    }
    
    
    private void amigoInflateKeyguardView(Bundle options) {
        mAmigoKeyguardView = new AmigoKeyguardHostView(mContext);
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
}
