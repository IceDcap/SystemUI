package com.amigo.navi.keyguard.fingerprint;

import java.lang.reflect.Method;
import java.util.Arrays;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.KeyguardViewHostManager;
import com.amigo.navi.keyguard.util.VibatorUtil;
import com.gionee.fingerprint.IGnIdentifyCallback;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;


public class FingerIdentifyManager {
    
    private static final String LOG_TAG="FingerIdentifyManager";
    
    private static final int MSG_FINGER_NO_MATCH=0;
    private static final int MSG_FINGER_IDENTIFY=1;
    
    public static final String FINGERPRINT_FOR_UNLOCK_SWITCH_KEY = "fingerprint_used_for_unlock";
    private static final String CLASS_GNFPMANAGER = "com.gionee.fingerprint.GnFingerPrintManager";       
    
    private Context mContext;
    private Class<?> mGnFingerPrintManagerClass;
    private Object mObj;
    private static FingerIdentifyManager sInstance=null;
    
    private int mIdentifyFailedTimes=0;
    private boolean mFingerprintSwitchOpen=false;
    
    public FingerIdentifyManager(Context  context){
        mContext=context;
        sInstance=this  ;
    }
            
    public static FingerIdentifyManager  getInstance(){
        return sInstance;
    }
    
    
    private Handler mHandler=new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_FINGER_NO_MATCH:
                onFingerNoMatch(msg.arg1);
                break;
            case MSG_FINGER_IDENTIFY:
                onFingerIdentify();
                break;

            default:
                break;
            }
        };
    };
    
    public void startIdentifyIfNeed(){
        boolean isStartFingerPrint=isActiveFingerPrint();
        DebugLog.d(LOG_TAG, "startIdentifyIfNeed  isStartFingerPrint:"+isStartFingerPrint);
        if(isStartFingerPrint){
            int[] ids=getIds();
            if(ids!=null){
                startIdentifyTimeout(ids, 25*1000);
            }
        }
    }

    private boolean isActiveFingerPrint() {
        KeyguardViewHostManager manager = KeyguardViewHostManager.getInstance();
        if (manager == null) {
            return false;
        }
        if (!mFingerprintSwitchOpen) {
            return false;
        }
        boolean isSecure = manager.isSecure();
        if (!isSecure) {
            return false;
        }
        boolean isSkylightShown = manager.getIsSkylightShown();
        if (isSkylightShown) {
            return false;
        }
        boolean isKeyguardShown = manager.isShowingAndNotOccluded();
        if (!isKeyguardShown) {
            return false;
        }
        boolean isSimRequired = manager.needsFullscreenBouncer();
        if (isSimRequired) {
            return false;
        }
        return true;
    }
    
    public void readFingerprintSwitchValue(){
        //0 is close;1 is open
        int unlockValue = Settings.Secure.getInt(mContext.getContentResolver(),
                FINGERPRINT_FOR_UNLOCK_SWITCH_KEY, 0);
        if(unlockValue==0){
            mFingerprintSwitchOpen=false;
        }else{
            mFingerprintSwitchOpen=true;
        }
        
    }
    
    
    private int[] getIds() {
        Log.d(LOG_TAG, "getIds() start  time: "+SystemClock.uptimeMillis());
        try {
            Class<?> GnFingerPrintManager = (Class<?>) Class.forName(CLASS_GNFPMANAGER);
            Method getIds = GnFingerPrintManager.getMethod("getIds");
                Object obj = GnFingerPrintManager.newInstance();
                mGnFingerPrintManagerClass=GnFingerPrintManager;
                mObj=obj;

            int[] ids = (int[]) getIds.invoke(mObj);
            
            Log.d(LOG_TAG, "getIds() end ids=" + Arrays.toString(ids)+"  time: "+SystemClock.uptimeMillis());
            return ids;
        } catch (Exception e) {
            Log.d(LOG_TAG, Log.getStackTraceString(e));
            
        }
        return null;
    }
    
    public void cancel( ) {
        Log.d(LOG_TAG, "cancel()-start--");
        try {
            Method cancel = mGnFingerPrintManagerClass.getMethod("cancel");
            cancel.invoke(mObj);
        } catch (Exception e) {
            e.toString();
        }finally{
//            mObj=null;
        }
    }
    
    public void startIdentify(int[] ids) {
        try {
            Log.d(LOG_TAG, "testStartIdentify() start");
            Class<?> GnFingerPrintManager = (Class<?>) Class.forName(CLASS_GNFPMANAGER);
            Object obj = GnFingerPrintManager.newInstance();
            mGnFingerPrintManagerClass = GnFingerPrintManager;
            mObj = obj;
            
            Method startIdentify = GnFingerPrintManager.getMethod("startIdentify", IGnIdentifyCallback.class,
                    int[].class);
            startIdentify.invoke(obj, mIdentifyCb, ids);
            Log.d(LOG_TAG, "testStartIdentify() end");

        } catch (Exception e) {
            Log.d(LOG_TAG, Log.getStackTraceString(e));
        }
    }
    
    private void startIdentifyTimeout(int[] ids, long timeout) {
        try {
            Log.d(LOG_TAG, "testStartIdentifyTimeout() start");
            
            Class<?> GnFingerPrintManager = (Class<?>) Class.forName(CLASS_GNFPMANAGER);
            Object obj = GnFingerPrintManager.newInstance();
            mGnFingerPrintManagerClass = GnFingerPrintManager;
            mObj = obj;
            
            Method startIdentify = GnFingerPrintManager.getMethod("startIdentify", IGnIdentifyCallback.class, int[].class, long.class);
            startIdentify.invoke(obj, mIdentifyCb, ids, timeout);
            
            Log.d(LOG_TAG, "testStartIdentifyTimeout() end");

        } catch (Exception e) {
            Log.d(LOG_TAG, Log.getStackTraceString(e));
        }
    }

    
    private IGnIdentifyCallback mIdentifyCb = new IGnIdentifyCallback() {

        public void onWaitingForInput() {
            Log.d(LOG_TAG, "onWaitingForInput()---");
        }

        public void onInput() {
            Log.d(LOG_TAG, "onInput()---");
        }

        public void onCaptureCompleted() {
            Log.d(LOG_TAG, "onCaptureCompleted()---");
        }

        public void onCaptureFailed(int reason) {
            Log.d(LOG_TAG, "onCaptureFailed()---");
        }

        public void onIdentified(int fingerId, boolean updated) {
            Log.d(LOG_TAG, "onIdentified()---");
            mHandler.sendEmptyMessage(MSG_FINGER_IDENTIFY);
        }

        /**
         * @params reason: 0 failed;1 timeout; 2 cancel
         */
        public void onNoMatch(int reason) {
            Log.d(LOG_TAG, "onNoMatch()---reason=" + reason+" threadname:  "+Thread.currentThread().getName());
            Message msg=mHandler.obtainMessage(MSG_FINGER_NO_MATCH);
            msg.arg1=reason;
            mHandler.sendMessage(msg);
//            testStartIdentifyTimeout(getIds(), 25*1000);
        }

        public void onExtIdentifyMsg(Message msg, String description) {
            Log.d(LOG_TAG, "onExtIdentifyMsg()---");
        }
    };

    private void onFingerNoMatch(int reason) {
        if (reason == 2) {// cancel&exception
            return;
        } else if (reason == 1) {// timeout
            startIdentifyIfNeed();
            
        } else if (reason == 0) {
            DebugLog.d(LOG_TAG, "onFingerNoMatch mIdentifyFailedTimes: " + mIdentifyFailedTimes);
            fingerMatchFail();
        }
    }

    private void fingerMatchFail() {
        boolean isSecureFrozen = KeyguardViewHostManager.getInstance().passwordViewIsForzen();
        if (mIdentifyFailedTimes < 2 && !isSecureFrozen) {
            mIdentifyFailedTimes++;
            boolean isAtHomePosition = KeyguardViewHostManager.getInstance().isAmigoHostYAtHomePostion();
            if (isAtHomePosition) {
                KeyguardViewHostManager.getInstance().shakeFingerIdentifyTip();
            } else {
                KeyguardViewHostManager.getInstance().fingerPrintFailed();
            }
        } else {
            mIdentifyFailedTimes = 0;
            KeyguardViewHostManager.getInstance().scrollToUnlockHeightByOther(true);
        }
        VibatorUtil.amigoVibrate(mContext, VibatorUtil.LOCKSCREEN_UNLOCK_CODE_ERROR,
                VibatorUtil.UNLOCK_ERROR_VIBRATE_TIME);
        startIdentifyIfNeed();
    }
    
    private void onFingerIdentify() {
        if(!isActiveFingerPrint()){
            return;
        }
        boolean isSecureFrozen = KeyguardViewHostManager.getInstance().passwordViewIsForzen();
        boolean isAtHomePosition = KeyguardViewHostManager.getInstance().isAmigoHostYAtHomePostion();
        if (isSecureFrozen) {
            if (isAtHomePosition) {
                KeyguardViewHostManager.getInstance().scrollToUnlockHeightByOther(true);
            }else{
                startIdentifyIfNeed();
            }
        } else {
            KeyguardViewHostManager.getInstance().fingerPrintSuccess();
            KeyguardViewHostManager.getInstance().unlockByFingerIdentify();
        }

    }

}
