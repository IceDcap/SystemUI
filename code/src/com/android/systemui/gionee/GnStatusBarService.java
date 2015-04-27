/*
  *
  * MODULE DESCRIPTION
  *   GnStatusBar Service
  *   Receive the msg from framework : the event of Swipe From Bottom
  * 
  * add by hanbj for GnStatusBar at 20141120.
  * 
  */

package com.android.systemui.gionee;  
  
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.MotionEvent;
import com.android.internal.statusbar.IGnStatusBar;
import com.android.systemui.SystemUIApplication;
import com.android.systemui.statusbar.phone.PhoneStatusBar;

public class GnStatusBarService extends Service {
    
    private static final boolean DEBUG = true;
    private static final String TAG = "GnStatusBarService";  
    private GnStatusBarStub mStub;
    
    private static PhoneStatusBar mPhoneStatusBar;

    @Override
    public void onCreate() {
        super.onCreate();

        if (mStub == null) {
            mStub = new GnStatusBarStub();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (DEBUG) Log.i(TAG, "onBind() called");
        
        if (mStub == null) {
            mStub = new GnStatusBarStub();
        }
        
        return mStub;
    }

    @Override  
    public boolean onUnbind(Intent intent) {
        if (DEBUG) Log.i(TAG, "onUnbind() called");
        
        if (mStub != null) {
            mStub = null;
        }

        return true;  
    }  
      
    @Override  
    public void onDestroy() {  
        super.onDestroy();  
        
        if (DEBUG) Log.i(TAG, "onDestroy() called");
        
        if (mStub != null) {
            mStub = null;
        }
    }  

    private PhoneStatusBar getPhoneStatusBar(){
        if (mPhoneStatusBar == null) {
            mPhoneStatusBar = ((SystemUIApplication) getApplication()).getComponent(PhoneStatusBar.class);
        }

        return mPhoneStatusBar;
    }

    private class GnStatusBarStub extends IGnStatusBar.Stub {
        
        @Override  
        public void onSwipeFromBottom(boolean immerse) throws RemoteException {
            if (DEBUG) Log.v(TAG, "onSwipeFromBottom : start Control center !! ");
            
            Log.d(TAG, "isFullScreen = " + immerse);
            
            if (mPhoneStatusBar == null) {
                mPhoneStatusBar = getPhoneStatusBar();
            }
            
            if (mPhoneStatusBar != null) {
                mPhoneStatusBar.showGnContorlCenter(immerse);
            }
        }  

        @Override  
        public void onPointerEvent(MotionEvent event) throws RemoteException {
            if (DEBUG)
                Log.v(TAG, "onPointerEvent : event = " + event + " mPhoneStatusBar = "
                        + mPhoneStatusBar);

            if (mPhoneStatusBar != null) {
                mPhoneStatusBar.onPointerEvent(event);
            }
        }
        
        @Override
        public void onNotificationPriorityChanged(java.lang.String pkgName, int priority) throws android.os.RemoteException {
            
        }
        
        public void showSimIndicator(String businessType) {
//            if (mPhoneStatusBar != null) {
//                mPhoneStatusBar.showSimIndicator(businessType);                
//            }
        }
        
        public void hideSimIndicator() {
//            if (mPhoneStatusBar != null) {
//                mPhoneStatusBar.hideSimIndicator();
//            }
        }
    };
}  

