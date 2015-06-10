package com.amigo.navi.keyguard;


import com.amigo.navi.keyguard.haokan.UIController;
import com.android.keyguard.KeyguardViewBase;
import com.android.internal.widget.LockPatternUtils;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Choreographer;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout.LayoutParams;

import com.android.keyguard.KeyguardHostView.OnDismissAction;
import com.android.keyguard.KeyguardSecurityContainer.SecurityViewRemoveAnimationUpdateCallback;
import com.android.keyguard.KeyguardSecurityView;
import com.android.keyguard.ViewMediatorCallback;
import com.android.keyguard.R;
import com.android.keyguard.KeyguardSecurityModel.SecurityMode;



public class AmigoKeyguardBouncer {
	
	  private static String TAG = "AmigoKeyguardBouncer";
	  private ViewGroup mContainer;
	  private ViewGroup mRoot;
	  private Context mContext;
	  private KeyguardViewBase mKeyguardView;
	  private LockPatternUtils mLockPatternUtils;
	  private ViewMediatorCallback mCallback;
	  private int mMaxBoundY = 1920;
	  private KeyguardBouncerCallback mBouncerCallback;
	  
	  public AmigoKeyguardBouncer(Context context, ViewGroup container) {
	        mContext = context;
	        mContainer = container;
	    	mMaxBoundY = KWDataCache.getXPageHeight(context.getResources());
	    	inflateView();
	    	UIController.getInstance().setKeyguardBouncer(this);
	    }

	  private void inflateView() {
		  if(DebugLog.DEBUG) DebugLog.d(TAG, "inflateView");
	        removeView();
	        mRoot = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.keyguard_bouncer, null);
	        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,mMaxBoundY);
			mContainer.addView(mRoot, lp);
	        mKeyguardView = (KeyguardViewBase) mRoot.findViewById(R.id.keyguard_host_view);  
	        mRoot.setVisibility(View.INVISIBLE);
	        mRoot.setSystemUiVisibility(View.STATUS_BAR_DISABLE_HOME);
	    }
	  
	 public void  initKeyguardBouncer(ViewMediatorCallback callback,LockPatternUtils lockPatternUtils,SecurityViewRemoveAnimationUpdateCallback removeViewCallback){
		    mLockPatternUtils=lockPatternUtils;
	    	setViewMediatorCallback(callback);
	    	mKeyguardView.setViewMediatorCallback(mCallback);
	    	mKeyguardView.setSecurityViewRemoveAnimationUpdateCallback(removeViewCallback);
	    	mKeyguardView.setLockPatternUtils(mLockPatternUtils);  
	    	
	 }
	 
	 
	  
	   public void setViewMediatorCallback(ViewMediatorCallback viewMediatorCallback) {
		   mCallback = viewMediatorCallback;
	        
	    }

	    private void removeView() {
	        if (mRoot != null && mRoot.getParent() == mContainer) {
	            mContainer.removeView(mRoot);
	            mRoot = null;
	        }
	    }
	    
	    private void ensureView() {
	        if (mRoot == null) {
	            inflateView();
	        }
	    }
	    
	    public void show(boolean resetSecuritySelection) {
	    	if(DebugLog.DEBUG) DebugLog.d(TAG, "show....resetSecuritySelection="+resetSecuritySelection);
	        ensureView();
	        if (resetSecuritySelection) {
	            // showPrimarySecurityScreen() updates the current security method. This is needed in
	            // case we are already showing and the current security method changed.
	            mKeyguardView.showPrimarySecurityScreen();
	        }
	        if (mRoot.getVisibility() == View.VISIBLE ) {
	            return;
	        }

	        mRoot.setVisibility(View.VISIBLE);
	        mKeyguardView.onResume(KeyguardSecurityView.SCREEN_ON);
            mKeyguardView.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
	    }
	    

	    public void hide(boolean destroyView) {
	         if (mKeyguardView != null) {
	            mKeyguardView.setOnDismissAction(null);
	            mKeyguardView.cleanUp();
	        }
	        if (destroyView) {
	            removeView();
	        } else if (mRoot != null) {
	            mRoot.setVisibility(View.INVISIBLE);
	        }
	    }
	    
	    public void prepare() {
	        boolean wasInitialized = mRoot != null;
	        ensureView();
	        if (wasInitialized) {
	            mKeyguardView.showPrimarySecurityScreen();
	        }
	    }
	    
	    public boolean isSecure() {
            return mKeyguardView == null || mKeyguardView.getSecurityMode() != SecurityMode.None;
        }
	    
	    public boolean isSimSecure() {
	        if (mKeyguardView != null) {
                SecurityMode mode = mKeyguardView.getSecurityMode();
                return mode == SecurityMode.SimPin || mode == SecurityMode.SimPuk;
            }
            return false;
        }
	    
	    public boolean needsFullscreenBouncer() {
	        if (mKeyguardView != null) {
	            SecurityMode mode = mKeyguardView.getSecurityMode();
	            return mode == SecurityMode.SimPin || mode == SecurityMode.SimPuk;
	        }
	        return false;
	    }
	    
	    public boolean isShowing() {
	        return (mRoot != null && mRoot.getVisibility() == View.VISIBLE);
	    }
		
 
	    public void showWithDismissAction(OnDismissAction r) {
	        show(false);
	        mKeyguardView.setOnDismissAction(r);
	    }
	 
	    
		 public void onScreenTurnedOff() {
			   if (mKeyguardView != null && mRoot != null && mRoot.getVisibility() == View.VISIBLE) {
		            mKeyguardView.onPause();
		        }
		    }
		 
		 public void onScreenTurnedOn() {
		        if (mKeyguardView != null && mRoot != null ) {
		            mKeyguardView.onResume(KeyguardSecurityView.SCREEN_ON);
		        }
		        
		 }
		 
		 public void onResumeSecurityView(int reason) {
		        if (mKeyguardView != null && mRoot != null ) {
		            mKeyguardView.onResume(reason);
		            mKeyguardView.startAppearAnimation();
		        }
		        
		 }
		 
		 public void onPauseSecurityView(int reason) {
			 if (mKeyguardView != null && mRoot != null ) {
		            mKeyguardView.onPauseSecurityView(reason);
		        }
		 }
		 
    public void fingerPrintFailed() {
        if (mKeyguardView != null && mRoot != null) {
            mKeyguardView.fingerPrintFailed();
        }
    }

    public void fingerPrintSuccess() {
        if (mKeyguardView != null && mRoot != null) {
            mKeyguardView.fingerPrintSuccess();
        }
    }
		 
    public boolean passwordViewIsForzen(){
    	 if (mKeyguardView != null && mRoot != null) {
             return mKeyguardView.passwordViewIsForzen();
         }
    	 return false;
    }
		 public long getUserActivityTimeout() {
		    	long timeout=-1;
		        if (mKeyguardView != null) {
		           timeout = mKeyguardView.getUserActivityTimeout();		          
		        }
		        return timeout; 
		    }
		    
		    
		    
		    public interface KeyguardBouncerCallback {
		    	void bouncerShowing();
		    	void KeyguardShowing(); 	
		    }
		    
		    public void registerBouncerCallback(KeyguardBouncerCallback bouncerCallback){
		    	mBouncerCallback=bouncerCallback;
		    }
		    
		    
		    public void bouncerShowing(){
		    	if(DebugLog.DEBUG) DebugLog.d(TAG, "bouncerShowing");
		    	if(mBouncerCallback!=null){
		    		mBouncerCallback.bouncerShowing();
		    	}
		    }
		    
		    public void KeyguardShowing(){
		    	if(DebugLog.DEBUG) DebugLog.d(TAG, "KeyguardShowing");
		    	if(mBouncerCallback!=null){
		    		mBouncerCallback.KeyguardShowing();
		    	}
		    }
		    
		   
		    
		 

}
