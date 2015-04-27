package com.android.systemui.gionee.cc.qs.tiles;

import amigo.provider.AmigoSettings;
import android.app.StatusBarManager;
import android.content.Context;
import android.util.Log;

import com.android.systemui.R;

public class GnTileHelper {

    private static final String TAG = "GnTileHelper";
    
    private static int mVolumeIcon = 0;
    private static boolean mVolumeVisible = true;
    
    private static final String AMIGO_SETTING_VIBRATE = "amigo_vibration_switch";
    private static final String AMIGO_SETTING_VOICE = "amigo_silent_switch";
    private static final String SLOT_VOLUME = "volume";
    
    public static void updateVolumeIcon(StatusBarManager service, Context context) {
        boolean vibrate = AmigoSettings.getInt(context.getContentResolver(), AMIGO_SETTING_VIBRATE, 0) == 1;
        boolean voice = AmigoSettings.getInt(context.getContentResolver(), AMIGO_SETTING_VOICE, 0) == 0;

        Log.d(TAG, " vibrate = " + vibrate + "  voice = " + voice);
        
        boolean visible = false;
        int iconId = R.drawable.gn_stat_sys_ringer_vibrate;
        if (voice) {
            visible = false;
        } else if (vibrate) {
            visible = true;
            iconId = R.drawable.gn_stat_sys_ringer_vibrate;
        } else {
            visible = true;
            iconId = R.drawable.gn_stat_sys_ringer_silent;
        }

        if (mVolumeIcon != iconId) {
            service.setIcon(SLOT_VOLUME, iconId, 0, null);
            mVolumeIcon = iconId;
        }
        
        if (mVolumeVisible != visible) {
            service.setIconVisibility(SLOT_VOLUME, visible);
            mVolumeVisible = visible;
        }
    }
}