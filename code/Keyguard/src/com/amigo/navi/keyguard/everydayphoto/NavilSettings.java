package com.amigo.navi.keyguard.everydayphoto;

import android.content.Context;
import android.content.SharedPreferences;

public class NavilSettings {
	public static final String PREFERENCE_NAME = "keyguardSettingPreference";
	private static String sVersionName = "0";
	public static final String ISUPDATEWALLPAPER_EVERYDAY = "isopen_update_wallpaper_everyday";
	public static final String ONLY_WLAN = "only_wlan";
	public static final String IS_SET_TODAY_WALLPAPER = "is_set_today_wallpaper";
	public static final String WALLPAPER_EVERYDAY_MD5 = "wallpaper_everyday_md5";
	public static final String SET_TODAY_WALLPAPER_MILLISECOND = "SET_TODAY_WALLPAPER_MILLISECOND";
	public static final String WALLPAPER_DOWNLOAD_OK = "wallpaper_download_ok";
	
//	public static final String ISCHECKING = "isChecking";
	public static final String LAST_CHECK_UPDATE_MILLISECOND = "LAST_CHECK_UPDATE_MILLISECOND";

	public static final String COMMENT1 = "comment1";
	public static final String COMMENT2 = "comment2";
	public static final String COMMENT_EN = "comment_en";
	public static final String COMMENT_CITY_EN = "comment_city_en";

	public static final String USER_ID = "user_id";
	
	public static final String DEFAULT_CATEGORY = "default_category";
	public static final String CATEGORY_PERSONAL = "category_personal";
	public static final String CATEGORY_FAVORITE = "category_favorite";
    
	public static final String HAOKAN_PAGE = "haokan_page";
	public static final String HAOKAN_SELECTION_ID = "haokan_selection_ID";
	public static final String HAOKAN_SAVED_PAGE_URL = "haokan_url";
	
	public static final String UPDATE_CATEGORY_DATE = "category_date";
	public static final String UPDATE_WALLPAPER_DATE = "wallpaper_date";
	public static final String DATA_VERSION = "data_version";
	public static final String IS_DATA_INIT = "data_init";
	public static final String DATABASE_VERSION = "database_ver";
	public static final String LOCK_POSITION = "lock_pos";
	public static final String LOCK_ID = "lock_id";
	public static final String ALARM_TIME = "ALARM_TIME";

	public static void setVersionName(String versionName ){
		sVersionName = versionName;
	}
	
	public static String getVersionName(){
		return sVersionName;
	}
	
    /**
     * @author xuebo
     * @param context
     * @param key
     * @param defValue
     * @return  getSharedPreference boolean config
     */
    public static boolean getBooleanSharedConfig(Context context, String key, boolean defValue){
    	SharedPreferences sp = context.getSharedPreferences(
                PREFERENCE_NAME, Context.MODE_PRIVATE);
        boolean ch = sp.getBoolean(key, defValue);
        return ch;
    }
    /**
     * @author xuebo
     * @param context
     * @param key
     * @param defValue
     * @return  setSharedPreference boolean config
     */
    public static boolean setBooleanSharedConfig(Context context, String key, boolean value){
    	 SharedPreferences sp = context.getSharedPreferences(
                 PREFERENCE_NAME, Context.MODE_PRIVATE);
    	 SharedPreferences.Editor editor = sp.edit();
         editor.putBoolean(key, value);
    	return editor.commit();
    }
    
    
    
    public static long getLongSharedConfig(Context context, String key, Long defValue){
    	SharedPreferences sp = context.getSharedPreferences(
                PREFERENCE_NAME, Context.MODE_PRIVATE);
        Long ch = sp.getLong(key, defValue);
        return ch;
    }
    
    public static boolean setLongSharedConfig(Context context, String key, Long value){
    	 SharedPreferences sp = context.getSharedPreferences(
                 PREFERENCE_NAME, Context.MODE_PRIVATE);
    	 SharedPreferences.Editor editor = sp.edit();
         editor.putLong(key, value);
    	return editor.commit();
    }
    
    public static String getStringSharedConfig(Context context, String key, String defValue){
    	SharedPreferences sp = context.getSharedPreferences(
                PREFERENCE_NAME, Context.MODE_PRIVATE);
        String ch = sp.getString(key, defValue);
        return ch;
    }
    
    public static boolean setStringSharedConfig(Context context, String key, String value){
    	 SharedPreferences sp = context.getSharedPreferences(
                 PREFERENCE_NAME, Context.MODE_PRIVATE);
    	 SharedPreferences.Editor editor = sp.edit();
         editor.putString(key, value);
    	return editor.commit();
    }
    
    public static int getIntSharedConfig(Context context, String key, int defValue){
    	SharedPreferences sp = context.getSharedPreferences(
                PREFERENCE_NAME, Context.MODE_PRIVATE);
        int ch = sp.getInt(key, defValue);
        return ch;
    }
    
    public static boolean setIntSharedConfig(Context context, String key, int value){
    	 SharedPreferences sp = context.getSharedPreferences(
                 PREFERENCE_NAME, Context.MODE_PRIVATE);
    	 SharedPreferences.Editor editor = sp.edit();
         editor.putInt(key, value);
    	return editor.commit();
    }
}
