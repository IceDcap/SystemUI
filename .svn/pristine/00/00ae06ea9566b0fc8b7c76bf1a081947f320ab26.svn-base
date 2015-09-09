package com.android.systemui.gionee.cc.torch;

import java.lang.reflect.Method;

import android.content.Context;
import android.util.Log;

public class GnReflectTorchType implements GnTorchType {
    
    private static final String TAG = "GnReflectTorchType";
    
    public static final String POWER_MANAGER_CLASS_NAME = "android.os.PowerManager";
    public static final String METHOD_SET_TORCH_ON = "setTorchBrightness";
    public static final String METHOD_IS_TORCH_OPENED = "isTorchOpened";

    public GnReflectTorchType(Context mContext) {
        super();
    }

    @Override
    public int getType() {
        return isExist();
    }

    private int isExist() {
        try {
            boolean hasMethodSet = false;
            boolean hasMethodCheck = false;
            
            Class c = Class.forName(POWER_MANAGER_CLASS_NAME);
            Method[] methods = c.getDeclaredMethods();
            for (Method m : methods) {
                if (METHOD_SET_TORCH_ON.equals(m.getName())) {
                    hasMethodSet = true;
                    Log.i(TAG, "setTorchBrightness exist");
                }
                if (METHOD_IS_TORCH_OPENED.equals(m.getName())) {
                    hasMethodCheck = true;
                    Log.i(TAG, "isTorchOpened exist");
                }
            }

            if (hasMethodSet && hasMethodCheck) {
                return GnTorchType.LIGHT_TYPE_POWER;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return GnTorchType.LIGHT_TYPE_CAMERA;
    }

}
