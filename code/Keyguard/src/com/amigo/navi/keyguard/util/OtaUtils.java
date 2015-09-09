package com.amigo.navi.keyguard.util;

import java.io.File;

import com.amigo.navi.keyguard.settings.KeyguardSettings;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemProperties;
import android.util.Log;


public class OtaUtils {
	
	private static final String KEY_ROM_VERSION = "rom_version";

	private static final String KEY_AUTO_UPDATE = KeyguardSettings.PF_KEYGUARD_WALLPAPER_UPDATE;
	private static final String KEY_RELATED_AUTO_UPDATE = KeyguardSettings.PF_KEYGUARD_CONNECT;
	
	public static boolean checkRomOta(Context context) {
		boolean c1 = isIdentifiedFileExisted(context);
		boolean c2 = isRomVersionChanged(context);
		Log.d("OtaUtils", String.format("isRomOta %b, %b", c1, c2));
		
		boolean isRomOta = c1 && c2;
		if(isRomOta) {
			onRomOta(context);
		}
		
		return isRomOta;
	}
	
	private static void onRomOta(Context context) {
		checkAutoUpdateState(context);
	}
	
	private static boolean isIdentifiedFileExisted(Context context) {
		return  isOtaFrom44(context) ||
				isOtaFromNaviKeyguard(context) ||
				isOtaFrom51(context) ||
				isOtaFrom50(context);
	}
	
	private static boolean isOtaFrom51(Context context) {
		boolean isOta = false;
		String xmlPath = getPrefFolderPath(context)
				+ "/" + KeyguardSettings.PREFERENCE_NAME + ".xml";
		isOta = new File(xmlPath).exists();
		
		Log.d("OtaUtils", "isOtaFrom51 " + isOta);
		return isOta;
	}
	
	private static boolean isOtaFrom50(Context context) {
		boolean isOta = false;
		
		String xmlPath = getPrefFolderPath(context) + "/first_boot.xml";
		isOta = new File(xmlPath).exists();
		
		Log.d("OtaUtils", "xmlPath " + xmlPath);
		Log.d("OtaUtils", "isOtaFrom50 " + isOta);
		return isOta;
	}
	
	private static boolean isOtaFrom44(Context context) {
		boolean isOta = false;
		
		String xmlPath = getPrefFolderPath(context) + "/tools_sort.xml";
		isOta = new File(xmlPath).exists();
		if(!isOta) {
			String filePath = context.getFilesDir().getPath() + "/WallPaperBlur";
			isOta = new File(filePath).exists();
		}
		
		Log.d("OtaUtils", "isOtaFrom44 " + isOta);
		return isOta;
	}
	
	private static boolean isOtaFromNaviKeyguard(Context context) {
		boolean isOta = false;
		
		String xmlPath = getPrefFolderPath(context) + "/keyguardSettingPreference.xml";
		isOta = new File(xmlPath).exists();
		
		Log.d("OtaUtils", "xmlPath " + xmlPath);
		Log.d("OtaUtils", "isOtaFromNaviKeyguard " + isOta);
		return isOta;
	}
	
	private static String getPrefFolderPath(Context context) {
		return "data/data/" + context.getPackageName() + "/shared_prefs";
	}
	
	private static boolean isRomVersionChanged(Context context) {
		boolean versionChanged = false;
		
		String currentVersion = SystemProperties.get("ro.gn.extvernumber");
		SharedPreferences prefs = KeyguardSettings.getPrefs(context);
		if(currentVersion != null) {
			versionChanged = !currentVersion.equals(prefs.getString(KEY_ROM_VERSION, ""));
		}
		
		if(versionChanged) {
			Log.d("OtaUtils", "version changed save rom version " + currentVersion);
			prefs.edit().putString(KEY_ROM_VERSION, currentVersion).apply();
		}
		
		return versionChanged;
	}
	
	private static boolean checkEverydayPicsState(Context context) {
		boolean closeAutoUpdateSwitcher = false;
		
		Uri uri = Uri.parse("content://navi.providers.setting/everyday");
		Cursor c = context.getContentResolver().query(uri, null, null, null, null);
		Log.d("OtaUtils", "find navi-launcher provider? " + (c != null));
		if(c != null) {
			while (c.moveToNext()) {
				String key = c.getString(c.getColumnIndex("_key"));
				if("wallpaper_everyday".equals(key)) {
					String value = c.getString(c.getColumnIndex("_value"));
					Log.d("OtaUtils", "find everyday-pics state: " + value);
					if("close".equals(value)) {
						if(isSdMounted() && isEverydayPicsFolderExisted()) {
							closeAutoUpdateSwitcher = true;
							Log.d("OtaUtils", "everyday-pics is closed, close auto-update switcher");
						}
					}
				}
			}
			
			c.close();
		}
		
		return closeAutoUpdateSwitcher;
	}
	
	private static boolean isSdMounted() {
		boolean mounted = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
				|| Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);
		Log.d("OtaUtils", "SD mounted? " + mounted);
		return mounted;
	}
	
	// sd_root_path/naviData/wallpaper
	private static boolean isEverydayPicsFolderExisted() {
		String folderPath = Environment.getExternalStorageDirectory().getPath() + "/Amigo/NaviData/Wallpaper";
		boolean folderExisted = new File(folderPath).exists();
		Log.d("OtaUtils", "SD folderExisted? " + folderExisted);
		
		return folderExisted;
	}
	
	private static void checkAutoUpdateState(Context context) {
		SharedPreferences prefs = KeyguardSettings.getPrefs(context);
		boolean isAutoUpdateKeyNotExisted = !prefs.contains(KEY_AUTO_UPDATE);
		if(isAutoUpdateKeyNotExisted) {
			boolean close = checkEverydayPicsState(context);
			if(!close) {
				openAutoUpdateSwitcher(context);
			}
		}
	}
	
	private static void openAutoUpdateSwitcher(Context context) {
		SharedPreferences prefs = KeyguardSettings.getPrefs(context);
		prefs.edit()
			.putBoolean(KEY_AUTO_UPDATE, true)
			.putBoolean(KEY_RELATED_AUTO_UPDATE, true)
			.apply();
	}
	
}