
package com.amigo.navi.keyguard.haokan;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.os.Build;
 
import android.widget.Toast;

 

public class CommonUtils {

    private static final String TAG = "change-Utils";
    private static final String DEVICE_ID = "device_id";
    private static final String NET_TYPE = "net_type";
    private static final String NET_NAME = "net_name";
    private static final String PACKAGE_AMI_SYSTEM = "com.gionee.amisystem";
    private static String mVersionName = null;
    public static final int LOLLIPOP_VERSION = 21;

    public static boolean hasNetworkInfo(Context context) {
        ConnectivityManager conMan = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                .getState();
        State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .getState();
        if (wifi == State.CONNECTED) {
            return true;
        }

        if (mobile == State.CONNECTED) {
            return true;
        }
        return false;
    }

    public static int getIntColorFromString(String color, int defaultColor) {
        if (color == null || color.length() != 7) {
            return defaultColor;
        }
        color = color.toLowerCase();
        int[] rgb = new int[7];
        for (int i = 1; i < 6; i = i + 2) {
            int a = color.charAt(i);
            int b = color.charAt(i + 1);
            if (a >= '0' && a <= '9') {
                a = (a - 48) * 10;
            } else if (a >= 'a' && a <= 'f') {
                a = (a - 87) * 10;
            } else {
                a = 0;
            }

            if (b >= '0' && b <= '9') {
                b = b - 48;
            } else if (b >= 'a' && b <= 'f') {
                b = b - 87;
            } else {
                b = 0;
            }
            rgb[i] = a + b;
        }
        return Color.rgb(rgb[1], rgb[3], rgb[5]);
    }

    public static void makeToast(Context context, String str) {
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }

    public static void makeToast(Context context, int str) {
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }

    

    public static void closeFileInputStream(FileInputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e1) {
            }
        }
    }

    public static void closeFileOutputStream(FileOutputStream os) {
        if (os != null) {
            try {
                os.close();
            } catch (IOException e1) {
            }
        }
    }

    

    /**
     * Youju report for NetWork type
     */
    public static Map<String, Object> getNetConnectInfo(Context context) {
        Map<String, Object> map = new HashMap<String, Object>();
        final TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        map.put(DEVICE_ID, tm.getDeviceId());
        map.put(NET_TYPE, getNetworkClass(tm.getNetworkType()));
        map.put(NET_NAME, tm.getNetworkOperatorName());
        return map;
    }

    public static String getNetworkClass(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G";
            default:
                return "none";
        }
    }

    public static Bitmap scaleBitmap(Bitmap bmp, int scaleHeight) {
        Bitmap bm = null;
        if (bmp != null) {
            int h = bmp.getHeight();
            if (h != scaleHeight) {
                float rate = 1.0f * scaleHeight / h;
                int rateW = (int) (bmp.getWidth() * rate);
                try {
                    bm = Bitmap.createScaledBitmap(bmp, rateW, scaleHeight,
                            true);
                } catch (OutOfMemoryError e) {
                    Log.d(TAG, "scaleBitmap e = ", e);
                } catch (Exception e) {
                    Log.d(TAG, "scaleBitmap e = ", e);
                }
                if (bm != bmp) {
                    bmp.recycle();
                    bmp = null;
                }
            } else {
                bm = bmp;
            }
        }
        return bm;
    }

    public static File writeFile(Bitmap bitmap, String path, String name) {
        File file = new File(path);
        if (!file.exists()) {
            if (file.mkdirs()) {
                file.setReadable(true, false);
                if (file.setWritable(true, false)) {
                    file.setExecutable(true, false);
                }
            }

        }
        File paperFile = new File(path + "/" + name);
        BufferedOutputStream bos;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(paperFile));
//            bitmap.compress(format, quality, stream)
            bitmap.compress(Bitmap.CompressFormat.JPEG,
                    100, bos);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            Log.w(TAG, "warning", e);
        }
        paperFile.setExecutable(true, false);
        paperFile.setReadable(true, false);
        // 蛋疼的代码
        boolean isWriteOk = paperFile.setWritable(true, false);
        return paperFile;
    }

    public static boolean writeFiles(Bitmap bitmap, String path, String name) {
        File file = new File(path);
        if (!file.exists()) {
            if (file.mkdirs()) {
                file.setReadable(true, false);
                if (file.setWritable(true, false)) {
                    file.setExecutable(true, false);
                }
            }

        }
        File paperFile = new File(path + "/" + name);
        BufferedOutputStream bos;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(paperFile));
            bitmap.compress(Bitmap.CompressFormat.JPEG,
                    100, bos);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            Log.w(TAG, "warning", e);
            return false;
        }
        paperFile.setExecutable(true, false);
        paperFile.setReadable(true, false);
        boolean isWriteOk = paperFile.setWritable(true, false);
        return true;
    }


    public static String getAppVersion(Context context) {
        if (mVersionName == null) {
            PackageManager packageManager = context.getPackageManager();
            String name = context.getPackageName();
            try {
                PackageInfo packInfo = packageManager.getPackageInfo(name, 0);
                mVersionName = packInfo.versionName;
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return mVersionName;
    }

    
    

    /**
     * ** if System Version above LOLLIPOP
     * 
     * @return
     */
    public static boolean isSystemVersionAboveL() {
        // GioneeLog.debug(TAG, "android.os.Build.VERSION.RELEASE = "
        // + android.os.Build.VERSION.RELEASE);
        boolean isAboveL = Build.VERSION.SDK_INT >= LOLLIPOP_VERSION;
        return isAboveL;
    }

     

    public static void goToDeskTop(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static boolean hasAmiSystem(Context context) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(PACKAGE_AMI_SYSTEM, 0);
        } catch (Exception e) {
            packageInfo = null;
        }
        if (packageInfo == null) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean is2GRam(Context context) {
        if (Build.VERSION.SDK_INT >= 17) {
            ActivityManager am = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            MemoryInfo info = new MemoryInfo();
            am.getMemoryInfo(info);
            if (info.totalMem / 1024 / 1024 > 1536) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

     
}
