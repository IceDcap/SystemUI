/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
package com.android.systemui.gionee.cc.util;

import java.lang.reflect.Method;

import android.content.Context;
import android.os.Vibrator;
import android.util.Log;

public class GnVibrateUtil {

    private static final long DEFAULT_VIBRATE_DURATION = 200;
    public static final String CONTROL_CENTER_LONG_PRESS = "LOCKSCREEN_STORYMODE_DISPLAY";
    public static final long CONTROL_CENTER_LONG_PRESS_TIME=100;
    private static Vibrator mVibrator = null;
    public static void vibrate(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(DEFAULT_VIBRATE_DURATION);
    }

    public static void vibrate(Context context, long vibrateTime) {
    	if(mVibrator == null){
			mVibrator =  (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE); 
		}
		mVibrator.vibrate(vibrateTime);
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
			vibrate(context, milliseconds);
		}
	}
}
