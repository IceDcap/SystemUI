package com.amigo.navi.keyguard.fingerprint;

import java.lang.reflect.Method;
import java.util.Arrays;

import com.gionee.fingerprint.IGnIdentifyCallback;

import android.os.SystemClock;
import android.util.Log;


public class FingerIndentifyManager {
    
    private static final String LOG_TAG="FingerIndentifyManager";
    
    private static final String CLASS_GNFPMANAGER = "com.gionee.fingerprint.GnFingerPrintManager";        
    private Class<?> mGnFingerPrintManagerClass;
    private Object mObj;
    
    
    public FingerIndentifyManager(){
   
    }
            
    public void startIdentify(IGnIdentifyCallback cb, int[] ids) {
        try {
            Log.d(LOG_TAG, "testStartIdentify() start  time: "+SystemClock.uptimeMillis());
            Class<?> GnFingerPrintManager = (Class<?>) Class.forName(CLASS_GNFPMANAGER);
            Method startIdentify = GnFingerPrintManager.getMethod("startIdentify", IGnIdentifyCallback.class,
                    int[].class);
            if(mObj==null){
                Object obj = GnFingerPrintManager.newInstance();
                mGnFingerPrintManagerClass = GnFingerPrintManager;
                mObj = obj;
            }
            startIdentify.invoke(mObj, cb, ids);
            Log.d(LOG_TAG, "testStartIdentify() end time: "+SystemClock.uptimeMillis());

        } catch (Exception e) {
            Log.d(LOG_TAG, Log.getStackTraceString(e));
        }
    }
    
    public void startIdentifyTimeout(IGnIdentifyCallback cb, int[] ids, long timeout) {
        try {
            Log.d(LOG_TAG, "testStartIdentifyTimeout() start");
            
            Class<?> GnFingerPrintManager = (Class<?>) Class.forName(CLASS_GNFPMANAGER);
            Method startIdentify = GnFingerPrintManager.getMethod("startIdentify", IGnIdentifyCallback.class, int[].class, long.class);
            if(mObj==null){
            Object obj = GnFingerPrintManager.newInstance();
            mGnFingerPrintManagerClass = GnFingerPrintManager;
            mObj = obj;
            }
            startIdentify.invoke(mObj, cb, ids, timeout);
            
            Log.d(LOG_TAG, "testStartIdentifyTimeout() end");

        } catch (Exception e) {
            Log.d(LOG_TAG, Log.getStackTraceString(e));
        }
    }
    
    public int[] getIds() {
        Log.d(LOG_TAG, "getIds() start  time: "+SystemClock.uptimeMillis());
        try {
            Class<?> GnFingerPrintManager = (Class<?>) Class.forName(CLASS_GNFPMANAGER);
            Method getIds = GnFingerPrintManager.getMethod("getIds");
            if(mObj==null){
                Object obj = GnFingerPrintManager.newInstance();
                mGnFingerPrintManagerClass=GnFingerPrintManager;
                mObj=obj;
            }

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
            mObj=null;
        }
    }


}
