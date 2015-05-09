package com.amigo.navi.keyguard.network.connect;

import java.lang.reflect.Method;
import java.util.Locale;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

public class GetUaUtils {
    private static String sUa = null;

    public static String getUA(Context context) {
        return gnGetUserAgent(context);
    }
    
    public synchronized static String gnGetUserAgent(Context context) {
        if (null == sUa) {
            sUa = getUaString(context);
        }
        return sUa;
    }
    
    public static String getUaString(Context context) {
        String model = null;
        String uacontent = null;
        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String decodeImei = telephonyManager.getDeviceId();
        String brand = systemPropertiesGet("ro.product.brand", "GiONEE");
        String extModel = systemPropertiesGet("ro.gn.extmodel", "Phone");
        model = systemPropertiesGet("ro.product.model", "Phone");
        Locale local = Locale.getDefault();
        String slocaleInfo = local.getLanguage() + "-"
                + local.getCountry().toLowerCase();
        uacontent = "Mozilla/5.0 (Linux; U; Android "
                + Build.VERSION.RELEASE
                + "; "
                + slocaleInfo
                + ";"
                + brand
                + "-"
                + model
                + "/"
                + extModel
                + " Build/IMM76D) AppleWebKit 534.30 (KHTML,like Gecko) Version/4.0 Mobile Safari/534.30";
        if (null != uacontent && !uacontent.isEmpty()) {
            if (uacontent.endsWith(" ")) {
                uacontent += "Id/" + decodeImei;
            } else {
                uacontent += " Id/" + decodeImei;
            }
        }
        return uacontent;
    }
    
    public static String systemPropertiesGet(String key, String def) {
        init();
    
        String value = null;
    
        try {
            value = (String) mGetMethod.invoke(mClassType, key, def);
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        return value;
    }
    
    public static int systemPropertiesGetInt(String key, int def) {
        init();
    
        int value = def;
        try {
            Integer v = (Integer) mGetIntMethod.invoke(mClassType, key, def);
            value = v.intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }
    
    private static Class<?> mClassType = null;
    private static Method mGetMethod = null;
    private static Method mGetIntMethod = null;
    
    private static void init() {
        try {
            if (mClassType == null) {
                mClassType = Class.forName("android.os.SystemProperties");
    
                mGetMethod = mClassType.getDeclaredMethod("get", String.class,
                        String.class);
                mGetIntMethod = mClassType.getDeclaredMethod("getInt",
                        String.class, int.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }   
}

