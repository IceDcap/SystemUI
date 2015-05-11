package com.amigo.navi.keyguard.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

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
	// for statistics
	public static final int SWITCH_WALLPAPER_UPDATE_ON = 2;
	public static final int SWITCH_WALLPAPER_UPDATE_OFF = 1;
	public static final int SWITCH_ONLY_WLAN_ON = 1;
	public static final int SWITCH_ONLY_WLAN_OFF = 2;
	
	public static final long ANIMATION_DELAY = 33;

	public static final String CLEARNOTIFICATION = "clearNotification_KeyguardSettingsActivity";
	public static final int NOTIFICATION_ID_SETTING = 1007;
	
	
    
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

}
