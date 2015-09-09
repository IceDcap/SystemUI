package com.amigo.navi.keyguard.settings;

import com.amigo.navi.keyguard.DebugLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class KeyguardSettingsSetupwizardReceiver extends BroadcastReceiver{

	private static final String TAG = "KeyguardSettingsSetupwizardReceiver";
	private static final String SETUPWIZARD_ACTION = "com.gionee.setupwizard.SECURE_SERVICE";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		DebugLog.d(TAG, "receive:" + intent.getAction());
		if (SETUPWIZARD_ACTION.equals(intent.getAction())){
			KeyguardSettings.setConnectState(context, true);
			KeyguardSettings.setWallpaperUpadteState(context,true);
			KeyguardSettings.setEverOpened(context,true);
			KeyguardSettings.cancelNotification(context);
		}		
	}

}
