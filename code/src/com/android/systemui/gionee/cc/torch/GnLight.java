package com.android.systemui.gionee.cc.torch;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.android.systemui.gionee.cc.fakecall.GnUpdateUiStateCallback;

public abstract class GnLight {
    private static final String LOG_TAG="Light";
    static List<GnUpdateUiStateCallback> mUiStateCallbacks=new ArrayList<GnUpdateUiStateCallback>();
    
    public abstract void on();

    public abstract void off();
    
    public abstract void release();
    
    public abstract boolean getLightState();

    public  void notifyLightUiState(){
        for (GnUpdateUiStateCallback callback : mUiStateCallbacks) {
            Log.d(LOG_TAG, "notifyLightUiState--------------");
            callback.updateUiState();
        }
    };
    
    public  void registerUiStateCallback(GnUpdateUiStateCallback callback){
        if (callback != null) {
            int size = mUiStateCallbacks.size();
            for (int i = size - 1; i >= 0; i--) {
                if (mUiStateCallbacks.get(i) == callback) {
                    mUiStateCallbacks.remove(i);
                }
            }
            mUiStateCallbacks.add(callback);
        }
    };
    
    public void unregisterUiStateCallback(GnUpdateUiStateCallback callback){
        mUiStateCallbacks.remove(callback);
    };
}
