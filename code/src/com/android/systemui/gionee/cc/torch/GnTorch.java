package com.android.systemui.gionee.cc.torch;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public abstract class GnTorch {
    
    private static final String TAG = "GnTorch";

    static List<GnUpdateUiStateCallback> mUiStateCallbacks = new ArrayList<GnUpdateUiStateCallback>();

    public abstract void on();
    public abstract void off();
    public abstract void release();
    public abstract boolean getTorchState();
    
    public interface GnUpdateUiStateCallback {
        public void updateUiState();
    }

    public void notifyLightUiState() {
        for (GnUpdateUiStateCallback callback : mUiStateCallbacks) {
            Log.d(TAG, "notifyLightUiState--------------");
            callback.updateUiState();
        }
    };

    public void registerUiStateCallback(GnUpdateUiStateCallback callback) {
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

    public void unregisterUiStateCallback(GnUpdateUiStateCallback callback) {
        mUiStateCallbacks.remove(callback);
    };
}
