/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/

package com.android.systemui.gionee.cc.qs.brightness;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;

public abstract class GnCurrentUserTracker extends BroadcastReceiver {

    private Context mContext;
    private int mCurrentUserId;

    public GnCurrentUserTracker(Context context) {
        mContext = context;
    }

    public int getCurrentUserId() {
        return mCurrentUserId;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_USER_SWITCHED.equals(intent.getAction())) {
            int oldUserId = mCurrentUserId;
            mCurrentUserId = intent.getIntExtra(Intent.EXTRA_USER_HANDLE, 0);
            if (oldUserId != mCurrentUserId) {
                onUserSwitched(mCurrentUserId);
            }
        }
    }

    public void startTracking() {
        mCurrentUserId = ActivityManager.getCurrentUser();
        IntentFilter filter = new IntentFilter(Intent.ACTION_USER_SWITCHED);
        mContext.registerReceiver(this, filter);
    }

    public void stopTracking() {
        mContext.unregisterReceiver(this);
    }

    public abstract void onUserSwitched(int newUserId);

    public boolean isCurrentUserOwner() {
        return mCurrentUserId == UserHandle.USER_OWNER;
    }
}
