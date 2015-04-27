package com.amigo.navi.keyguard;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout.LayoutParams;

import com.amigo.navi.keyguard.AmigoKeyguardBouncer.KeyguardBouncerCallback;
import com.amigo.navi.keyguard.skylight.SkylightActivity;
import com.amigo.navi.keyguard.skylight.SkylightHost;
import com.amigo.navi.keyguard.skylight.SkylightUtil;
import com.amigo.navi.keyguard.util.AmigoKeyguardUtils;
import com.android.keyguard.ViewMediatorCallback;
import com.android.keyguard.KeyguardHostView.OnDismissAction;
import com.android.internal.widget.LockPatternUtils;
import static com.android.keyguard.KeyguardHostView.OnDismissAction;

public class KeyguardViewHostManager {

    final static boolean DEBUG=true;
    private static final String LOG_TAG="KeyguardViewHostManager";
    
    private static final int MSG_HIDE_SKYLIGHT=0;
    private static final int MSG_SHOW_SKYLIGHT=1;
    private static final int MSG_CONFIGURATION_CHANGED = 2;
    
    
    private static KeyguardViewHostManager sInstance=null;
    
    private Context mContext;
    
    private KeyguardViewHost mKeyguardViewHost;
    private SkylightHost mSkylightHost;
    
    private ViewMediatorCallback mViewMediatorCallback;
    
    private boolean mIsSkylightShown=false;
    private LockPatternUtils mLockPatternUtils;
    private MyHandler mHandler=new MyHandler();
    private ViewHostReceiver mReceiver=new ViewHostReceiver();
    private KeyguardNotificationCallback mKeyguardNotificationCallback;
    
    public KeyguardViewHostManager(Context context,KeyguardViewHost host,LockPatternUtils lockPatternUtils,ViewMediatorCallback callback){
        mContext=context;
        mKeyguardViewHost=host;
        mLockPatternUtils = lockPatternUtils;
        registerReceivers();
        sInstance=this;
        initKeyguard(callback);
        
    }
  
    
    public static KeyguardViewHostManager getInstance(){
    	return sInstance;
    }
    public void initKeyguard(ViewMediatorCallback callback){
        setViewMediatorCallback(callback);
        mKeyguardViewHost.initKeyguard(callback, mLockPatternUtils);
        
    }
    
    private void registerReceivers(){
        IntentFilter filter=new IntentFilter();
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        mContext.registerReceiver(mReceiver, filter);
    }
    
    
    public void show(Bundle options){
        initSkylightHost();
        mKeyguardViewHost.show(options);
        
    }
    
    public void showBouncerOrKeyguard(){
    	mKeyguardViewHost.showBouncerOrKeyguard();
    }
    	 
    
    
    public void hide() {
        destroyAcivityIfNeed();
        mKeyguardViewHost.hide();
    }
    
    
    public void onScreenTurnedOff(){
        mKeyguardViewHost.onScreenTurnedOff();
    }
    
    public void onScreenTurnedOn(){
        mKeyguardViewHost.onScreenTurnedOn();
    }
    
    public boolean isShowing(){
        if(mViewMediatorCallback != null){
            return mViewMediatorCallback.isShowing();
        }
        return false;
    }
    
    public boolean isSecure(){
    	return mKeyguardViewHost.isSecure();
    }
    
    public void dismissWithDismissAction(OnDismissAction r){
        mKeyguardViewHost.dismissWithAction(r);
    }
    
    public void dismissWithDismissAction(OnDismissAction r,boolean afterKeyguardGone){
    	mKeyguardNotificationCallback.dismissWithAction(r,afterKeyguardGone);
    }
    
    public void dismiss(){
        mKeyguardViewHost.dismiss();
    }
    
    public void verifyUnlock(){
    	show(null);
    	dismiss();
    }
    
    public void setViewMediatorCallback(ViewMediatorCallback callback){
        mViewMediatorCallback=callback;
    }
    
    
    public boolean needsFullscreenBouncer(){
        return mKeyguardViewHost.needsFullscreenBouncer();
    }
    
    public void showBouncer(boolean resetSecuritySelection){
        mKeyguardViewHost.showBouncer(resetSecuritySelection);
    }
    
    
    
    protected boolean initSkylightHost() {
        boolean initSuccess = false;
        if (SkylightHost.isSkylightSizeExist()) {
            Log.d(LOG_TAG, "addSkylightToHost host is null? " + (mSkylightHost == null));
            if (mSkylightHost == null) {
                mSkylightHost = new SkylightHost(mContext);
                mSkylightHost.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                mSkylightHost.setVisibility(View.GONE);
                mKeyguardViewHost.addView(mSkylightHost);
                mSkylightHost.bringToFront();
                initSuccess = true;
            } else if (mSkylightHost != null) {
                initSuccess = true;
            }
        }
        return initSuccess;
    }
    
    private void setIsSkylightShown(boolean isShown){
        mIsSkylightShown=isShown;
    }
    public boolean getIsSkylightShown(){
        return mIsSkylightShown;
    }
    
    public void showSkylight(){
        mHandler.sendEmptyMessage(MSG_SHOW_SKYLIGHT);
    }

    private void handleShowSkylight() {
        if (!SkylightHost.isSkylightSizeExist()) {
            return;
        }

        if (SkylightUtil.getIsHallOpen(mContext)) {
            return;
        }
        

        if(DEBUG){Log.d(LOG_TAG, "showSkylight  skylight is null? " + (mSkylightHost == null));}
        mViewMediatorCallback.userActivity();
        if (mSkylightHost != null) {
            ViewParent parent = mSkylightHost.getParent();
            if (parent == null) {
                mKeyguardViewHost.addView(mSkylightHost);
            }
            mSkylightHost.showSkylight();
            mSkylightHost.bringToFront();
            mSkylightHost.setVisibility(View.VISIBLE);
            mKeyguardViewHost.resetHostYToHomePosition();
//            updateStatusBarExpandlable(true);
            startActivityIfNeed();
            mIsSkylightShown=true;
        }
    }
    public void hideSkylight(boolean isGotoUnlock) {
        Message msg=mHandler.obtainMessage(MSG_HIDE_SKYLIGHT, isGotoUnlock);
        mHandler.sendMessage(msg);
     
    }
    private void handleHideSkylight(boolean isGotoUnlock){
        if(!SkylightUtil.getIsHallOpen(mContext)){
            return;
        }
        mViewMediatorCallback.userActivity();
        destroyAcivityIfNeed();
        if (mSkylightHost != null) {
            mSkylightHost.hideSkylight();
            mSkylightHost.setVisibility(View.GONE);
            boolean isLockScreenDisabled=mLockPatternUtils.isLockScreenDisabled();
            if (isLockScreenDisabled) {
                // updateStatusBarExpandlable(false);
                mViewMediatorCallback.keyguardDone(true);
            }
            mIsSkylightShown=false;
        }
    }
    
    
    
    public void unLockByOther(boolean animation){
        mKeyguardViewHost.unLockByOther(animation);
    }


    /**
     * start an empty activity to pause a running activity(eg. video or game) in activitytask when close skylight
     */
    private List<SkylightActivity> mActivitys=new ArrayList<SkylightActivity>();
    
    protected void startActivityIfNeed() {
        /**
         * if alarm boot , do not start activity 
         */
        if (/*mViewMediatorCallback.isShowingAndNotOccluded()&&*/!AmigoKeyguardUtils.isAlarmBoot()) {
            if (mActivitys.size() == 0) {
                Intent intent = new Intent(mContext, SkylightActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        }
    }
    
    public void destroyAcivityIfNeed() {
        for (int i = 0; i < mActivitys.size(); i++) {
            DebugLog.d(LOG_TAG, "destroyAcivityIfNeed");
            SkylightActivity activity=mActivitys.get(i);
            if(activity!=null){
                activity.finish();
                activity.overridePendingTransition(0, 0);
            }
        }
        mActivitys.clear();
    }
    
    public void notifySkylightActivityCreated(SkylightActivity activity){
        mActivitys.add(activity);
    }
    

    
    
    public void handleConfigurationChanged() {
        if(mSkylightHost!=null){
            mSkylightHost.onConfigurationChanged();
        }
    }
    
    
    
    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_HIDE_SKYLIGHT:
                boolean isUnlock = (Boolean) msg.obj;
                handleHideSkylight(isUnlock);
                break;
            case MSG_SHOW_SKYLIGHT:
                handleShowSkylight();
                break;

            case MSG_CONFIGURATION_CHANGED:
                handleConfigurationChanged();
                break;
            default:
                break;

            }
        }
    }
    
    private class ViewHostReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            DebugLog.d(LOG_TAG, "onReceive action: "+action);
            if(Intent.ACTION_CONFIGURATION_CHANGED.equals(action)){
                mHandler.sendEmptyMessage(MSG_CONFIGURATION_CHANGED);
            }
        }
        
    }
    
    public void scrollToKeyguardPageByAnimation(){
    	if(mKeyguardViewHost != null){
    		mKeyguardViewHost.scrollToKeyguardPageByAnimation();
    	}
    		
    }

    public long getUserActivityTimeout(){
    	return mKeyguardViewHost.getUserActivityTimeout();
    }
    
    public boolean keyguardBouncerIsShowing(){
    	return mKeyguardViewHost.keyguardBouncerIsShowing();
    }
    
    public void registerBouncerCallback(KeyguardBouncerCallback bouncerCallback){
    	mKeyguardViewHost.registerBouncerCallback(bouncerCallback);
    }
    
    public void registerKeyguardNotificationCallback(KeyguardNotificationCallback notificationCallback){
    	mKeyguardNotificationCallback=notificationCallback;
    }
    
    public interface KeyguardNotificationCallback {
    	void dismissWithAction(OnDismissAction r, boolean afterKeyguardGone);
    }
    
    public void dismissWithAction(OnDismissAction r, boolean afterKeyguardGone){
    	if(mKeyguardNotificationCallback!=null){
    		mKeyguardNotificationCallback.dismissWithAction(r,afterKeyguardGone);
    	}
    }
    
    public boolean  onBackPress(){
    	if(mKeyguardViewHost!=null){
    		return mKeyguardViewHost.onBackPress();
    	}
    	return false;
    }
    
    
}
