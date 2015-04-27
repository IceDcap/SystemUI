/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
package com.android.systemui.gionee.cc.util;

import android.content.Context;
import android.os.Vibrator;

public class GnVibrateUtil {

    private static final long DEFAULT_VIBRATE_DURATION = 200;

    public static void vibrate(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(DEFAULT_VIBRATE_DURATION);
    }

    public static void vibrate(Context context, long vibrateTime) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(vibrateTime);
    }
}
