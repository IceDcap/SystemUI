package com.amigo.navi.keyguard.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.amigo.navi.keyguard.AppConstants;
import com.amigo.navi.keyguard.DebugLog;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Looper;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;

public class AmigoKeyguardUtils {
    
    public static final String LOG_TAG="AmigoKeyguardUtils";
    
    private static int KEYCODE_HALL_O  = KeyEvent.KEYCODE_UNKNOWN;
    private static int KEYCODE_HALL_C  = KeyEvent.KEYCODE_UNKNOWN;

    public static DisplayMetrics getDisplayMetrics(Context context) {
        DisplayMetrics outMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(outMetrics);

        return outMetrics;
    }

    public static float getPixelDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    public static Bitmap getBlurBitmap(Bitmap blurBitmapOut, int blurRatio) {

        try {
            Class c = Class.forName("amigo.widget.blur.AmigoBlur");
            Method m = c.getMethod("getInstance");
            m.setAccessible(true);
            Object obj = m.invoke(c);
            Method method = c
                    .getMethod("nativeProcessBitmap", Bitmap.class, int.class, int.class, int.class, int.class);
            method.setAccessible(true);
            method.invoke(obj, blurBitmapOut, 24, blurBitmapOut.getWidth(), blurBitmapOut.getHeight(), blurRatio);
        } catch (Exception e) {
            Log.e("BlurBitmap", "getBitmapError-->", e);
        }
        return blurBitmapOut;
    }

    /**
     * get id by reflection,because of the resource id is mutability
     * 
     */
    public static int getNavBarHeight(Context context) {

        int navBarHeight = 0;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$bool");
            Field field = clazz.getField("config_showNavigationBar");
            int id = field.getInt(null);
            boolean hasNavBar = context.getResources().getBoolean(id);
            String navBarOverride = SystemProperties.get("qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavBar = true;
            }
            if (hasNavBar) {
                Class<?> navBarclazz = Class.forName("com.android.internal.R$dimen");
                Field navBarfield = navBarclazz.getField("navigation_bar_height");
                int navBarHeightId = navBarfield.getInt(null);
                navBarHeight=context.getResources().getDimensionPixelSize(navBarHeightId);

            }
            if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "hasNavBar: " + hasNavBar + "  navBarHeight: " + navBarHeight + " id1:  "
                    + com.android.internal.R.bool.config_showNavigationBar + "  id2: " + id);
        } catch (Exception e) {
            e.printStackTrace();
            DebugLog.e(LOG_TAG, "getNavBarHeight e："+e.toString());
        }
        return navBarHeight;
    }
    
    public static int getNavBarHeight(Resources resources) {
        int navBarHeight = 0;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$bool");
            Field field = clazz.getField("config_showNavigationBar");
            int id = field.getInt(null);
            boolean hasNavBar = resources.getBoolean(id);
            String navBarOverride = SystemProperties.get("qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavBar = true;
            }
            if (hasNavBar) {
                Class<?> navBarclazz = Class.forName("com.android.internal.R$dimen");
                Field navBarfield = navBarclazz.getField("navigation_bar_height");
                int navBarHeightId = navBarfield.getInt(null);
                navBarHeight=resources.getDimensionPixelSize(navBarHeightId);

            }
            if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "hasNavBar: " + hasNavBar + "  navBarHeight: " + navBarHeight + " id1:  "
                    + com.android.internal.R.bool.config_showNavigationBar + "  id2: " + id);
        } catch (Exception e) {
            e.printStackTrace();
            DebugLog.e(LOG_TAG, "getNavBarHeight e："+e.toString());
        }
        return navBarHeight;
    }

    public static int getStatusBarHeight(Context context) {

        int statusBarHeight = 0;
        try {
            Class<?> statusBarclazz = Class.forName("com.android.internal.R$dimen");
            Field statusBarfield = statusBarclazz.getField("status_bar_height");
            int statusBarHeightId = statusBarfield.getInt(null);
            statusBarHeight = context.getResources().getDimensionPixelSize(statusBarHeightId);

        } catch (Exception e) {
            e.printStackTrace();
            DebugLog.e(LOG_TAG, "getStatusBarHeight e："+e.toString());
        }
        return statusBarHeight;
    }
    
    public static int getDisplayHeight(Context context){
    	int displayHeight = 0;
    	Resources resources = context.getResources();
    	boolean isScreenPortrait = resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    	DisplayMetrics metrics = resources.getDisplayMetrics();
    	if(isScreenPortrait){
    		displayHeight = metrics.heightPixels;
    	}else{
    		displayHeight = metrics.widthPixels;
    	}
    	displayHeight += getNavBarHeight(context);
    	if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "getDisplayHeight  height: "+displayHeight);
        return displayHeight;
    }
    
    public static String getmOldFontStyle() {
    	String mOldFontStyle = SystemProperties.get(AppConstants.AMIGOFONT_KEY, AppConstants.AMIGOFONT_DEFAULT_VALUE);
    	if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "getmOldFontStyle..mOldFontStyle="+mOldFontStyle);            	
        return mOldFontStyle;
	}
    
    public static Field invokeObjField(String fieldName, Configuration newConfig) {

		try {
			Field nameFile = newConfig.getClass().getField(fieldName);
			if(DebugLog.DEBUG) DebugLog.d(LOG_TAG,
					"onConfigurationChanged() newConfig....nameFile="
							+ nameFile.toString()
							+ "nameFile.get(newConfig)="
							+ nameFile.get(newConfig).toString());
			return nameFile;

		} catch (Exception e) {
			DebugLog.e(LOG_TAG, "onConfigurationChanged() "
					+ fieldName + " error ob=" + newConfig.getClass(), e);
			return null;
		}

	}
    
    public static String getCurrretFontStyle(Configuration newConfig,String oldFontStyle){
    	Field newField=invokeObjField(AppConstants.AMIGOFONT_FIELDNAME,newConfig);
		String newFieldValue=oldFontStyle;
		if(newField!=null ){
			try {
				 newFieldValue=newField.get(newConfig).toString();
				
				 if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "onConfigurationChanged() newConfig....amigoFont1111111="+newFieldValue+"oldFontStyle="+oldFontStyle);
				
			}catch (IllegalAccessException e) {
				// TODO: handle exception
			}catch(Exception e){
				
			}
		}
		
		return newFieldValue;
    }

    public static void killApplication(Context context,String packageName) {
        try {
            
            ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
            Method method = Class.forName("android.app.ActivityManager")
                    .getMethod("forceStopPackage", String.class);
            method.invoke(am, packageName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public static boolean getAppIsRunning(Context context, String packageName) {
        boolean isAppRunning = false;
        if (TextUtils.isEmpty(packageName)) {
            return isAppRunning;
        }
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> taskInfos = am.getRunningTasks(Integer.MAX_VALUE);

        for (int i = 0; i < taskInfos.size(); i++) {
            String pkg = taskInfos.get(i).topActivity.getPackageName();
            if (packageName.equals(pkg)) {
                isAppRunning = true;
                break;
            }
        }
        return isAppRunning;
    }
    
    public static boolean getProcessIsRunning(Context context, String processName) {
        boolean isProcessRunning = false;
        if (TextUtils.isEmpty(processName)) {
            return isProcessRunning;
        }
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> runningProcess = am.getRunningAppProcesses();
        
        for (RunningAppProcessInfo runningAppProcessInfo : runningProcess) {
            String pkg = runningAppProcessInfo.processName;
            if (processName.equals(pkg)) {
                isProcessRunning = true;
                break;
            }
        }
        return isProcessRunning;
    }
    
    
    /**
     *  Get the subscription from the intent.
     * MSimConstants.SUBSCRIPTION_KEY
     */
    
    public static String getSuscriptionKey(){
    	String key="";
		try {
			Class<?> clazz=Class.forName("com.android.internal.telephony.MSimConstants");
			Field field=clazz.getField("SUBSCRIPTION_KEY");
			field.setAccessible(true);
			key=(String)field.get(clazz);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "getSuscriptionKey:  "+key);
		return key;
    }
    
	public static int getHallOpenKey() {
		if (KEYCODE_HALL_O == KeyEvent.KEYCODE_UNKNOWN) {
			try {
				Class<?> clazz = Class.forName("android.view.KeyEvent");
				Field field = clazz.getField("KEYCODE_HALL_O");
				field.setAccessible(true);
				KEYCODE_HALL_O = (Integer) field.get(clazz);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "getHallOpenKey:  " + KEYCODE_HALL_O);
		return KEYCODE_HALL_O;
	}
	
	public static int getHallCloseKey() {
		if (KEYCODE_HALL_C == KeyEvent.KEYCODE_UNKNOWN) {
			try {
				Class<?> clazz = Class.forName("android.view.KeyEvent");
				Field field = clazz.getField("KEYCODE_HALL_C");
				field.setAccessible(true);
				KEYCODE_HALL_C = (Integer) field.get(clazz);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "getHallCloseKey:  " + KEYCODE_HALL_C);
		return KEYCODE_HALL_C;

	}
	
	public static boolean isMainThread(){
	    return (Looper.getMainLooper()==Looper.myLooper());
	}
	
	
    /**
     * Return AirPlane mode is on or not.
     * 
     * @param context
     *            the context
     * @return airplane mode is on or not
     */
    public static boolean isAirplaneModeOn(Context context) {
        boolean airplaneModeOn = Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON,
                0) != 0;
        DebugLog.d(LOG_TAG, "isAirplaneModeOn() = " + airplaneModeOn);
        return airplaneModeOn;
    }
    
    public static boolean isAlarmBoot() {
        String bootReason = SystemProperties.get("sys.boot.reason");
        boolean ret = (bootReason != null && bootReason.equals("1")) ? true : false;
        return ret;
    }
    
    
    public static boolean wouldLaunchResolverActivity(Context ctx, Intent intent) {
        PackageManager packageManager = ctx.getPackageManager();
        final List<ResolveInfo> appList = packageManager.queryIntentActivities(
                intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (appList.size() == 0) {
            return false;
        }
        ResolveInfo resolved = packageManager.resolveActivity(intent,
                PackageManager.MATCH_DEFAULT_ONLY | PackageManager.GET_META_DATA);
        return wouldLaunchResolverActivity(resolved, appList);
    }

    private static boolean wouldLaunchResolverActivity(
            ResolveInfo resolved, List<ResolveInfo> appList) {
        // If the list contains the above resolved activity, then it can't be
        // ResolverActivity itself.
        for (int i = 0; i < appList.size(); i++) {
            ResolveInfo tmp = appList.get(i);
            if (tmp.activityInfo.name.equals(resolved.activityInfo.name)
                    && tmp.activityInfo.packageName.equals(resolved.activityInfo.packageName)) {
                return false;
            }
        }
        return true;
    }
}
