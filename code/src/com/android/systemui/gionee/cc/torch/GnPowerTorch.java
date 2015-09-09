package com.android.systemui.gionee.cc.torch;

import java.lang.reflect.Method;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

public class GnPowerTorch extends GnTorch {

    private static final String TAG = "GnPowerTorch";

    private static PowerManager mPowerManager = null;

    public GnPowerTorch(Context mContext) {
        if (mPowerManager == null) {
            mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        }
    }

    @Override
    public void on() {
        Log.d(TAG, "power torch on");
        try {
            Object obj = (Object) mPowerManager;
            Method th = PowerManager.class.getMethod(GnReflectTorchType.METHOD_SET_TORCH_ON, int.class);
            th.invoke(obj, 40);
            notifyLightUiState();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void off() {
        Log.d(TAG, "power torch off");
        try {
            Object obj = (Object) mPowerManager;
            Method th = PowerManager.class.getMethod(GnReflectTorchType.METHOD_SET_TORCH_ON, int.class);
            th.invoke(obj, 0);
            notifyLightUiState();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void release() {
        off();
    }

    @Override
    public boolean getTorchState() {
        try {
            Object obj = (Object) mPowerManager;
            Method th = PowerManager.class.getMethod(GnReflectTorchType.METHOD_IS_TORCH_OPENED, new Class[] {});
            Object result = th.invoke(obj, new Object[] {});
            return Boolean.parseBoolean(result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
