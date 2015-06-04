package com.android.systemui.gionee.cc.fakecall;
/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class GnStartTaskReciver extends BroadcastReceiver {
    
    private static final String TAG = "GnStartTaskReciver";

    @Override
    public void onReceive(Context context, Intent intent) {

        boolean ringFakeCall = intent.getBooleanExtra(GnConstants.START_TASK_OPTR, false);
        
        Log.d(TAG, "ring the phone: " + ringFakeCall);

        GnFakeCallControllerImpl gnFakeCallHelper = GnFakeCallControllerImpl.getInstance();
        if (ringFakeCall) {
            gnFakeCallHelper.ring();
        } else {
            gnFakeCallHelper.cancel();
        }
    }

}
