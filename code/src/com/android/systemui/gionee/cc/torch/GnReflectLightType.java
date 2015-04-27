package com.android.systemui.gionee.cc.torch;

import java.lang.reflect.Method;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

public class GnReflectLightType implements GnLightType {
    private static final String TAG = "ReflectLightType";
    private String className = "android.os.PowerManager";
    private boolean[] methdExit = new boolean[] { false, false };

    private String on = "setTorchBrightness";
    private String check = "isTorchOpened";

    public GnReflectLightType(Context mContext) {
        super();
    }

    @Override
    public int getType() {
        return isExist();
//    	return -1;
    }

    private int isExist() {
        try {
            Class c = Class.forName(className);
            Method[] methods = c.getDeclaredMethods();
            for (Method m : methods) {
                if (on.equals(m.getName())) {
                    methdExit[0] = true;
                    Log.i(TAG, "setTorchBrightness exist");
                }
                if (check.equals(m.getName())) {
                    methdExit[1] = true;
                    Log.i(TAG, "isTorchOpened exist");
                }
            }

            if (methdExit[0] && methdExit[1]) {
                return 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

}
