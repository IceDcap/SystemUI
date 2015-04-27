package com.amigo.navi.keyguard.util;

import android.content.Context;
import android.os.Vibrator;

public class VibatorUtil {
	public static void vibator(Context context, long  milliseconds){
		Vibrator vubator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		vubator.vibrate(milliseconds);
	}
}
