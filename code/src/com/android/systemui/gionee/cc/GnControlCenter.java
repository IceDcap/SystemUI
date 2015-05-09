package com.android.systemui.gionee.cc;
/*
*
* MODULE DESCRIPTION
*   GnControlCenter
* add by huangwt for Android L at 20141210.
* 
*/

import java.util.ArrayList;
import java.util.List;

import com.android.systemui.R;
import com.android.systemui.gionee.GnBlurHelper;
import com.android.systemui.gionee.GnUtil;
import com.android.systemui.statusbar.phone.NavigationBarView;
import com.android.systemui.statusbar.phone.PhoneStatusBar;

import amigo.provider.AmigoSettings;
import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class GnControlCenter extends FrameLayout{

    public static final boolean DEBUG = true;
    public static final String TAG = "GnControlCenter";
    
    // database
    private static final String AMIGO_SETTING_CC_SWITCH = "control_center_switch";
    
    // Context
    private Context mContext;
    
    // bool
    private boolean isLockScreenAccess;
    private boolean isAppAccess;
    private boolean isHighDevice = false;
    private boolean mLock;
    private boolean mImmerseState;
    
    // status bar
    private PhoneStatusBar mStatusBar;
    
    // ControlCenter
    private GnControlCenterView mControlCenterView;
    
    // Immerse    
    private GnControlCenterImmerseView mImmerseView;

    // state
    public static final int STATE_CLOSED = 0;
    public static final int STATE_OPENING = 1;
    public static final int STATE_OPEN = 2;
    public static final int STATE_IMMERSE_CLOSING = 3;
    public static final int STATE_IMMERSE_OPENING = 4;
    public static final int STATE_IMMERSE_OPEN = 5;
    private static int sState = STATE_CLOSED;
    
    public void go(int state) {
        if (DEBUG) Log.d(TAG, "go state: %d " + sState + " -> %d " + state);
        sState = state;
    }
    
    public int getState() {
        return sState;
    }
    
    // Callback list
    private ArrayList<Callback> mCallbackList = new ArrayList<GnControlCenter.Callback>();
    
    public interface Callback {
        void dismissPanel();
    }
    
    public void addCallback(Callback callback) {
        mCallbackList.add(callback);
    }
    
    public void dismiss() {
        for(Callback callback : mCallbackList) {
            callback.dismissPanel();
        }
        GnUtil.setLockState(GnUtil.STATE_LOCK_UNLOCK);
    }
    
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String actions = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(actions)) {
                if (mControlCenterView != null) {
                    mControlCenterView.setVisibility(GONE);
                }
                if (mImmerseView != null) {
                    mImmerseView.setVisibility(GONE);
                }
                GnUtil.setLockState(GnUtil.STATE_LOCK_UNLOCK);
            } else if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(actions)) {
                TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Service.TELEPHONY_SERVICE);
                if (tm.getCallState() == TelephonyManager.CALL_STATE_RINGING) {
                    dismiss();
                }
            } else if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(actions)) {
                dismiss();
            } else if (Intent.ACTION_USER_PRESENT.equals(actions)) {
                if (mStatusBar != null) {
                    NavigationBarView naviBar = mStatusBar.getNavigationBarView();
                    if (naviBar != null) {
                        Log.d(TAG, "set NavigationBar visible");
                        naviBar.setVisibility(VISIBLE);
                    }
                }
            }
        }
    };

    public GnControlCenter(Context context) {
        this(context, null);
    }
    
    public GnControlCenter(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        mContext.registerReceiver(mReceiver, filter);
        
        ContentResolver res = mContext.getContentResolver();
        isLockScreenAccess = AmigoSettings.getInt(res, AmigoSettings.LOCKSCREEN_ACCESS, 0) == 1;
        isAppAccess = AmigoSettings.getInt(res, AmigoSettings.APPLICATIONS_ACCESS, 0) == 1;
        mLock = AmigoSettings.getInt(res, AMIGO_SETTING_CC_SWITCH, 1) == 0;
        Log.d(TAG, "GnControlCenter  mLock = " + mLock);

        res.registerContentObserver(AmigoSettings.getUriFor(AmigoSettings.LOCKSCREEN_ACCESS), 
                true, mLockScreenAccessObserver);
        res.registerContentObserver(AmigoSettings.getUriFor(AmigoSettings.APPLICATIONS_ACCESS), 
                true, mAppAccessObserver);
        res.registerContentObserver(AmigoSettings.getUriFor(AMIGO_SETTING_CC_SWITCH), 
                true, mSwitchObserver);
        
        isHighDevice = GnUtil.isHighDevice(mContext);
        Log.d(TAG, "isHighDevice:" + isHighDevice);
    }
    
    public void addControlCenter(GnControlCenterView center) {
        mControlCenterView = center;
    }
    
    public void addGnImmerseModeView(GnControlCenterImmerseView immerse) {
        mImmerseView = immerse;
    }

    public void setVisible(boolean visible, Boolean immerse) {
        mImmerseState = immerse;
        setVisible(visible);
    }

    public void setVisible(boolean visible) {
        if (mControlCenterView.isShown()) {
            return;
        }
        
        if (mLock) {
            Log.d(TAG, "setVisible lock");
            return;
        }

        boolean isLockScrenn = inKeyguardRestrictedInputMode();
        
        if (!isLockScreenAccess) {
            if (isLockScrenn) {
                Log.d(TAG, "return isLockScrenn");
                return;
            }
        }
        
        if (!isAppAccess) {
            if (!isHomes() && !isLockScrenn) {
                Log.d(TAG, "return isApp");
                return;
            }
        }
        
        if (GnUtil.getLockState() == GnUtil.STATE_LOCK_BY_NOTIFICATION) {
            Log.d(TAG, "return lock by notification");
            return;
        }

        if (GnUtil.getLockState() == GnUtil.STATE_LOCK_UNLOCK) {
            Log.d(TAG, "Lock by cc");
            GnUtil.setLockState(GnUtil.STATE_LOCK_BY_CONTROLCENTER);
        }

        Log.d(TAG, "mState = " + sState + " mImmerseState = " + mImmerseState);
        if (isLandscape()) {
            if (sState == STATE_CLOSED) {
                if (isHighDevice) {
                    Log.d(TAG, "setVisible createBlurBg");
                    createBlurBg(mContext);
                }

                mImmerseView.setVisibility(VISIBLE);
                mImmerseView.pushUpIn();
                go(STATE_IMMERSE_OPENING);
            }
        } else {
            if (isHighDevice) {
                Log.d(TAG, "setVisible createBlurBg");
                createBlurBg(mContext);
            }

            boolean hasNavigationBar = mStatusBar.hasNavigationBar();
            Log.d(TAG, "hasNavigationBar = " + hasNavigationBar);
            if (mImmerseState && !hasNavigationBar) {
                if (sState == STATE_CLOSED) {
                    mImmerseView.setVisibility(VISIBLE);
                    mImmerseView.pushUpIn();
                    go(STATE_IMMERSE_OPENING);
                }
            } else {
                mControlCenterView.setVisibility(VISIBLE);
                go(STATE_OPENING);
            }
        }
    }
    
    public void swipingView(MotionEvent event) {
        if (DEBUG) Log.d(TAG, "swipingView " + event.getAction());
        
        if (GnUtil.getLockState() == GnUtil.STATE_LOCK_BY_NOTIFICATION) {
            if (false) Log.d(TAG, "swipingView return lock by nc");
            return;
        }
        
        if (mControlCenterView.isOpened() || mControlCenterView.isFling()) {
            if (DEBUG) Log.d(TAG, "swipingView  open:" + mControlCenterView.isOpened()
                    + " fling:" + mControlCenterView.isFling());
            return;
        }
        
        Log.d(TAG, " swipingView  mState = " + sState);
        if (sState == STATE_IMMERSE_OPEN) {
            mImmerseView.setVisibility(GONE);
            mControlCenterView.setVisibility(VISIBLE);
            go(STATE_OPENING);
            return;
        }
        
        if (sState == STATE_IMMERSE_OPENING && 
                (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP)) {
            go(STATE_IMMERSE_OPEN);
        }

        if (mControlCenterView.isShown()) {
            if (DEBUG) Log.d(TAG, "go swiping");
            mControlCenterView.swiping(event);
        }
    }
    
    public boolean inKeyguardRestrictedInputMode() {
        boolean lockscreen = mStatusBar.isKeyguardShowing();
        if (DEBUG) Log.d(TAG, "inKeyguardRestrictedInputMode  lockscreen = " + lockscreen);
        return lockscreen;
    }
    
    private boolean isHomes() {
        List<String> names = getHomes();
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = am.getRunningTasks(1).get(0).topActivity.getPackageName();
        boolean isHome = names.contains(packageName);
        Log.d(TAG, "isHome = " + isHome);
        return isHome;
    }
    
    private List<String> getHomes() {
        List<String> names = new ArrayList<String>();
        PackageManager packageManager = mContext.getPackageManager();
        
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri : resolveInfo) {
            names.add(ri.activityInfo.packageName);
        }
        return names;
    }
    
    private boolean isLandscape() {
        return mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }
    
    public boolean isHighDevice() {
        return isHighDevice;
    }
    
    private ContentObserver mLockScreenAccessObserver = new ContentObserver(new Handler()) {

        @Override
        public void onChange(boolean selfChange) {
            isLockScreenAccess = AmigoSettings.getInt(mContext.getContentResolver(),
                    AmigoSettings.LOCKSCREEN_ACCESS, 0) == 1;
            Log.d(TAG, "isLockScreenAccess = " + isLockScreenAccess);
        }
    };
    
    private ContentObserver mAppAccessObserver = new ContentObserver(new Handler()) {

        @Override
        public void onChange(boolean selfChange) {
            isAppAccess = AmigoSettings.getInt(mContext.getContentResolver(),
                    AmigoSettings.APPLICATIONS_ACCESS, 0) == 1;
            Log.d(TAG, "isAppAccess = " + isAppAccess);
        }
    };
    
    private ContentObserver mSwitchObserver = new ContentObserver(new Handler()) {

        @Override
        public void onChange(boolean selfChange) {
            mLock = AmigoSettings.getInt(mContext.getContentResolver(), AMIGO_SETTING_CC_SWITCH, 1) == 0;
            Log.d(TAG, "mSwitchObserver  mLock = " + mLock);
        }
    };
    
    public void setBar(PhoneStatusBar phoneStatusBar) {
        mStatusBar = phoneStatusBar;
    }
    
    public void createBlurBg(Context context) {
        GnBlurHelper.getBlurHelper().createBlurBg(context);
    }
}