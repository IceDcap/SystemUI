package com.android.systemui.gionee.cc.torch;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

public class GnShortcutUtils {

    private static long sLastClickTime;

    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        if (time - sLastClickTime < 500L) {
            return true;
        }
        sLastClickTime = time;
        return false;
    }
    
    
    public static String formatMillisTime(long milliseconds) {
        SimpleDateFormat df=new SimpleDateFormat("mm:ss");
        return df.format(milliseconds);
    }
    
    
    public static void killApplication(Context context,String packageName) {
        try {
            
            ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
            Method method = Class.forName("android.app.ActivityManager")
                    .getMethod("forceStopPackage", String.class);
            method.invoke(am, packageName);
            Log.d("jings", "killOtherApplication----------");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("jings", "killOtherApplication "+e.getMessage());
        }
    }
    
    public static boolean getAppIsRunning(Context context, String packageName) {
        boolean isAppRunning = false;
        if (TextUtils.isEmpty(packageName)) {
            return isAppRunning;
        }
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> runningProcess = am.getRunningAppProcesses();

        for (RunningAppProcessInfo runningAppProcessInfo : runningProcess) {
            String[] pkgList = runningAppProcessInfo.pkgList;
            for (String pkg : pkgList) {
                if (packageName.equals(pkg)) {
                    isAppRunning = true;
                }
            }
        }
        return isAppRunning;
    }
    
}
