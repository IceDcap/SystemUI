/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
package com.android.systemui.gionee.cc.util;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;

public class GnRingerModeRecoveryUtils {
	private static final String LOG_TAG = "QkRingerModeRecovery";
    
    public static final String ACTION_RESTORE_RINGER_MODE = "com.gionee.navi.fakecall.restore_ringer_mode";
    public static final String RESTORE_ARGS_RINGER_MODE = "ringer_mode";
    public static final String RESTORE_ARGS_RINGER_VOLUME = "ringer_volume";
    public static final String RESTORE_ARGS_VIBRATE_MODE = "vibrate_mode";
    
    private static Bundle sRingerModeRestoreArgs = new Bundle();
    
    public static void saveRingerModeArgs(int ringerMode, int ringerVolume) {
    	Log.d(LOG_TAG, "saveRingerModeArgs()");
    	sRingerModeRestoreArgs.putInt(RESTORE_ARGS_RINGER_MODE, ringerMode);
    	if(ringerMode != AudioManager.RINGER_MODE_NORMAL) {
    		ringerVolume = -1;
    	}
    	sRingerModeRestoreArgs.putInt(RESTORE_ARGS_RINGER_VOLUME, ringerVolume);
    }
    
    public static void saveRingerModeArgs(int ringerMode, int ringerVolume, int vabrate){
        sRingerModeRestoreArgs.putInt(RESTORE_ARGS_RINGER_MODE, ringerMode);
        if(ringerMode != AudioManager.RINGER_MODE_NORMAL) {
            ringerVolume = -1;
        }
        sRingerModeRestoreArgs.putInt(RESTORE_ARGS_RINGER_VOLUME, ringerVolume);
        sRingerModeRestoreArgs.putInt(RESTORE_ARGS_VIBRATE_MODE, vabrate);
        
        
    }
	
	public static void sendRingerModeRecoveryRequest(Context context) {
		Log.d(LOG_TAG, "sendRingerModeRecoveryRequest()");
		if(context == null) return;

    	Intent intent = new Intent(ACTION_RESTORE_RINGER_MODE);
    	intent.putExtras(sRingerModeRestoreArgs);
		context.sendBroadcast(intent);
	}
	
	public static void handleRingerModeRecoveryRequest(Context context, Bundle args) {
		Log.d(LOG_TAG, "handleRingerModeRecoveryRequest()");
		int ringerMode = args.getInt(RESTORE_ARGS_RINGER_MODE, -1);
		Log.d(LOG_TAG, "ringerMode=" + ringerMode);
		if(ringerMode != -1) {
			AudioManager audioMgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			if(ringerMode == AudioManager.RINGER_MODE_NORMAL) {
				// we should also recover ringer volume
				int ringerVolume = args.getInt(RESTORE_ARGS_RINGER_VOLUME, -1);
				audioMgr.setRingerMode(ringerMode);
				audioMgr.setStreamVolume(AudioManager.STREAM_RING, ringerVolume, 0);
			} else {
				audioMgr.setRingerMode(ringerMode);
			}
		}
	}
}
