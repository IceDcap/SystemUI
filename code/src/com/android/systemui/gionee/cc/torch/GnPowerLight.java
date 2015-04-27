package com.android.systemui.gionee.cc.torch;

// Gionee <caody><2013-11-21> modify for CR00955377 begin
import java.lang.reflect.Method;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

public class GnPowerLight extends GnLight {

    private static final String LOG_TAG = "PowerLight";

    static String className = "android.os.PowerManager";
    private static PowerManager service = null;
    private String on = "setTorchBrightness";
    private String check = "isTorchOpened";

    public GnPowerLight(Context mContext) {
        if (service == null) {
            service = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        }
    }

    @Override
    public void on() {
        Log.d(LOG_TAG, "power light on()  ");
        try {
            Object obj = (Object) service;
            Method th = PowerManager.class.getMethod(on, int.class);
            th.invoke(obj, 40);
            notifyLightUiState();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // service.setTorchBrightness(40);

    }

    @Override
    public void off() {
        Log.d(LOG_TAG, "power light off()  ");
        try {
            Object obj = (Object) service;
            Method th = PowerManager.class.getMethod(on, int.class);
            th.invoke(obj, 0);
            notifyLightUiState();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // service.setTorchBrightness(0);

    }

    @Override
    public void release() {
        off();
    }

    @Override
    public boolean getLightState() {

        try {
            Object obj = (Object) service;
            Method th = PowerManager.class.getMethod(check, new Class[] {});
            Object result = th.invoke(obj, new Object[] {});
            return Boolean.parseBoolean(result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    // Gionee <caody><2013-11-21> modify for CR00955377 end

}
