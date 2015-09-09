/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
package com.android.systemui.gionee.cc.qs.policy;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;


public class GnNextAlarmControllerImpl extends BroadcastReceiver implements GnNextAlarmController{

    public static final String TAG = "GnNextAlarmControllerImpl";

    private Context mContext;
    private final ArrayList<Callback> mChangeCallbacks = new ArrayList<>();
    
    private AlarmManager mAlarmManager;
    private AlarmManager.AlarmClockInfo mNextAlarm;

    private boolean mAlarmEnabled;
    private String mAlarmText;
    
    private Handler mHandler = new Handler();

    public GnNextAlarmControllerImpl(Context context) {
        mContext = context;
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_USER_SWITCHED);
        filter.addAction(Intent.ACTION_ALARM_CHANGED);
        filter.addAction(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED);
        context.registerReceiver(this, filter);
        updateNextAlarm();
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("NextAlarmController state:");
        pw.print("  mNextAlarm="); pw.println(mNextAlarm);
    }

    @Override
    public void addStateChangedCallback(Callback cb) {
        mChangeCallbacks.add(cb);
        cb.onNextAlarmChanged(mNextAlarm);        
    }

    @Override
    public void removeStateChangedCallback(Callback cb) {
        mChangeCallbacks.remove(cb);
    }

    @Override
    public boolean isNextAlarmEnabled() {
        return mAlarmEnabled;
    }

    @Override
    public String getNextAlarm() {
        return mAlarmText;
    }

    public void onReceive(Context context, final Intent intent) {
        final String action = intent.getAction();
        Log.d(TAG, "action = " + action);
        if (action.equals(Intent.ACTION_USER_SWITCHED)
                || action.equals(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED)) {
            updateNextAlarm();
        } else if (action.equals(Intent.ACTION_ALARM_CHANGED)) {
            mHandler.postDelayed(new Runnable() {
                
                @Override
                public void run() {                    
                    onAlarmChanged(intent);
                }
            }, 500);
        }
    }

    private void updateNextAlarm() {
        mNextAlarm = mAlarmManager.getNextAlarmClock(UserHandle.USER_CURRENT);
        fireNextAlarmChanged();
    }

    private void fireNextAlarmChanged() {
        int n = mChangeCallbacks.size();
        for (int i = 0; i < n; i++) {
            mChangeCallbacks.get(i).onNextAlarmChanged(mNextAlarm);
        }
    }
    
    void onAlarmChanged(Intent intent) {
        mAlarmText = Settings.System.getStringForUser(mContext.getContentResolver(),
                Settings.System.NEXT_ALARM_FORMATTED, UserHandle.USER_CURRENT);
        mAlarmEnabled = intent.getBooleanExtra("alarmSet", false);
        Log.d(TAG, "mAlarmEnabled = " + mAlarmEnabled + " mAlarmText = " + mAlarmText);
        fireNextAlarmChanged();
    }

}
