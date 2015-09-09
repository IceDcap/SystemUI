package com.android.systemui.gionee.cc.fakecall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class GnFakeCallAlarmReceiver extends BroadcastReceiver {
	private static final String TAG = "GnFakeCallAlarmReceiver";
//	public static final String STOP_TIMER_ACTION = "com.android.systemui.gionee.cc.fakecall.stopTimer";
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "stopTimer by alarm");
		GnFakeCallControllerImpl gnFakeCallControllerImpl = GnFakeCallControllerImpl
				.getInstance();
		gnFakeCallControllerImpl.startCall();
//		if (intent.getAction().equals(STOP_TIMER_ACTION)) {
//		} else {
//			// noop
//		}

	}
}
