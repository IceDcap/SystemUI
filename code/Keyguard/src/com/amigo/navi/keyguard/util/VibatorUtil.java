package com.amigo.navi.keyguard.util;

import java.lang.reflect.Method;

import android.content.Context;
import android.os.Vibrator;
import android.util.Log;

public class VibatorUtil {
	private static Vibrator mVibrator = null;
	
	// unlock error，Compatible with general motors
	public static final long UNLOCK_ERROR_VIBRATE_TIME = 100;
	
	// temporary: touch button vibrate time
	public static final long TOUCH_TAP_VIBRATE_TIME = 40;
	
	public static final long LOCKSCREEN_MENU_LONG_PRESS_VIBRATE_TIME = 100;
	
	public static final String LOCKSCREEN_STORYMODE_DISPLAY = "LOCKSCREEN_STORYMODE_DISPLAY";
	public static final String LOCKSCREEN_UNLOCK_CODE_TAP = "LOCKSCREEN_UNLOCK_CODE_TAP";
	public static final String LOCKSCREEN_UNLOCK_CODE_ERROR = "LOCKSCREEN_UNLOCK_CODE_ERROR";
	public static final String LOCKSCREEN_STORYMODE_CLICK = "LOCKSCREEN_STORYMODE_CLICK";
	

	
	public static void vibator(Context context, long  milliseconds){
		if(mVibrator == null){
			mVibrator =  (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE); 
		}
		mVibrator.vibrate(milliseconds);
	}
	
	public static void amigoVibrate(Context context, String effectName,
			long milliseconds) {
		if (mVibrator == null) {
			mVibrator = (Vibrator) context
					.getSystemService(Context.VIBRATOR_SERVICE);
		}
		Method method = null;
		try {
			method = mVibrator.getClass().getMethod("amigoVibrate",
					String.class, long.class);
			method.invoke(mVibrator, effectName, milliseconds);
		} catch (Exception e) {
			Log.e("feng", "failed caurse : " + e.toString());
			vibator(context, milliseconds);
		}
	}
	
	
}
