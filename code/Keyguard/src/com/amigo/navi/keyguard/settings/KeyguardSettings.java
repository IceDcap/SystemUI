package com.amigo.navi.keyguard.settings;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class KeyguardSettings {
    public static final String NET_CONNECT = "NetConnect";
    public static final String PF_DOUBLE_DESKTOP_LOCK = "double_desktop_lock";
    public static final String PF_KEYGUARD_WALLPAPER_UPDATE = "keyguard_wallpaper_update";
    public static final String PF_ONLY_WLAN = "only_wlan";
    
    public static final String PF_KEYGUARD_ALERT = "keyguard_isAlert";
    public static final String PF_KEYGUARD_CONNECT = "keyguard_connectNet";
    
    public static final String PREFERENCE_NAME = "com.gionee.navi.keyguard_preferences";
//    public static final String PREFERENCE_NAME_KEYGUARD_ALERT = "com.gionee.navi.keyguard_alert";
//    public static final String PREFERENCE_NAME_KEYGUARD_CONNECT = "com.gionee.navi.keyguard_connection";
	public static final boolean SWITCH_WALLPAPER_UPDATE = false;
	public static final boolean SWITCH_WALLPAPER_WIFI = true;
	public static final boolean SWITCH_DOUBLE_SCREEN = true;
	public static final boolean DIALOG_KEYGUARD_ALERT = true;
	
    public static final String SWITCH_WALLPAPER_UPDATE_OPENED_ONCE = "keyguard_wallpaper_update_opened_once";
	// for statistics
	public static final int SWITCH_WALLPAPER_UPDATE_ON = 2;
	public static final int SWITCH_WALLPAPER_UPDATE_OFF = 1;
	public static final int SWITCH_ONLY_WLAN_ON = 1;
	public static final int SWITCH_ONLY_WLAN_OFF = 2;
	
	public static final long ANIMATION_DELAY = 33; //弹出动画的延时
	public static final long WALLPAPER_UPDATE_ANIMATION_DELAY = 700;//壁纸更新引导动画延时

	public static final String CLEARNOTIFICATION = "clearNotification_KeyguardSettingsActivity";
	public static final int NOTIFICATION_ID_SETTING = 1007;
	private static final String TAG = "KeyguardSettings";
	
	
	public static final String PF_NEED_COPY_WALLPAPER = "pf_need_copy_wallpaper";
	
    
    public static void setDoubleDesktopLockState(Context context,boolean value){
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME,
                Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putBoolean(PF_DOUBLE_DESKTOP_LOCK, value);
		editor.commit();
    }
    
    public static boolean getDoubleDesktopLockState(Context context){
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME,
                Context.MODE_PRIVATE);
        return sp.getBoolean(PF_DOUBLE_DESKTOP_LOCK,
        		SWITCH_DOUBLE_SCREEN);
    }

    
    public static void setOnlyWlanState(Context context,boolean value){
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME,
                Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putBoolean(PF_ONLY_WLAN, value);
		editor.commit();
    }
    
    public static boolean getOnlyWlanState(Context context){
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME,
                Context.MODE_PRIVATE);
        return sp.getBoolean(PF_ONLY_WLAN,
        		SWITCH_WALLPAPER_WIFI);
    }
    
    public static void setWallpaperUpadteState(Context context,boolean value){
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME,
                Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putBoolean(PF_KEYGUARD_WALLPAPER_UPDATE, value);
		editor.commit();
    }
    
    public static boolean getWallpaperUpadteState(Context context){
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME,
                Context.MODE_PRIVATE);
        return sp.getBoolean(PF_KEYGUARD_WALLPAPER_UPDATE,
        		SWITCH_WALLPAPER_UPDATE);
    }
    
    public static void setDialogAlertState(Context context,boolean value){
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME,
                Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putBoolean(PF_KEYGUARD_ALERT, value);
		editor.commit();
    }
    
    public static boolean getDialogAlertState(Context context){
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME,
                Context.MODE_PRIVATE);
        return sp.getBoolean(PF_KEYGUARD_ALERT, DIALOG_KEYGUARD_ALERT
        		);
    }
    public static void setConnectState(Context context,boolean value){
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME,
                Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putBoolean(PF_KEYGUARD_CONNECT, value);
		editor.commit();
    }
    
    public static boolean getConnectState(Context context){
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME,
                Context.MODE_PRIVATE);
        return sp.getBoolean(PF_KEYGUARD_CONNECT,
        		SWITCH_WALLPAPER_UPDATE);
    }
    
    public static void setEverOpened(Context context,boolean value){
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME,
                Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putBoolean(SWITCH_WALLPAPER_UPDATE_OPENED_ONCE, value);
		editor.commit();
    }
    
    public static boolean getEverOpened(Context context){
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME,
                Context.MODE_PRIVATE);
        return sp.getBoolean(SWITCH_WALLPAPER_UPDATE_OPENED_ONCE,
        		SWITCH_WALLPAPER_UPDATE);
    }
    
    public static void cancelNotification(Context context) {
        Log.v(TAG, "cancelNotification");
        NotificationManager mNotificationManager;
        mNotificationManager = (NotificationManager)context.getSystemService("notification");
        try {
            mNotificationManager.cancel(NOTIFICATION_ID_SETTING);
        } catch (Exception e) {
        }
    }
    
    public static boolean getBooleanSharedConfig(Context context, String key, boolean defValue) {
        SharedPreferences sp = context.getSharedPreferences(
                PREFERENCE_NAME, Context.MODE_PRIVATE);
        boolean value = sp.getBoolean(key, defValue);
        return value;
    }

    public static boolean setBooleanSharedConfig(Context context, String key, boolean value) {
        SharedPreferences sp = context.getSharedPreferences(
                PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }

}
