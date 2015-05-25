/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
package com.android.systemui.gionee.cc.qs.tiles;

import java.util.Locale;

import android.app.AlarmManager;
import android.app.AlarmManager.AlarmClockInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;

import com.android.systemui.gionee.cc.qs.GnQSTile;
import com.android.systemui.gionee.cc.qs.policy.GnNextAlarmController;
import com.android.systemui.R;
import com.android.systemui.gionee.GnYouJu;

public class GnAlarmTile extends GnQSTile<GnQSTile.BooleanState> {

    private GnNextAlarmController mControler;
    // private AlarmManager.AlarmClockInfo mNextAlarm;
    
    public GnAlarmTile(Host host, String spec) {
        super(host, spec);
        
        mControler = host.getGnNextAlarmController();
    }

    @Override
    public void setListening(boolean listening) {
        if (listening) {
            mControler.addStateChangedCallback(mCallback);
        } else {
            mControler.removeStateChangedCallback(mCallback);
        }
    }

    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    protected void handleClick() {
    	GnYouJu.onEvent(mContext, "Amigo_SystemUI_CC", "GnAlarmTile");
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.android.deskclock",
                "com.android.deskclock.AlarmClock"));
        mHost.startSettingsActivity(intent);
    }

    @Override
    protected void handleLongClick() {
        
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.visible = true;
        /*if (mNextAlarm == null) {
            state.label = mContext.getString(R.string.gn_qs_alarm);
            state.iconId = R.drawable.gn_ic_qs_alarm_off;
        } else {
            state.label = formatNextAlarm(mNextAlarm);
            state.iconId = R.drawable.gn_ic_qs_alarm_on;
        }*/
        state.value = mControler.isNextAlarmEnabled();
        if (state.value) {
            state.label = mControler.getNextAlarm();
            state.iconId = R.drawable.gn_ic_qs_alarm_on;
        } else {
            state.label = mContext.getString(R.string.gn_qs_alarm);
            state.iconId = R.drawable.gn_ic_qs_alarm_off;
        }
    }
    
    private final GnNextAlarmController.Callback mCallback = new GnNextAlarmController.Callback() {

        @Override
        public void onNextAlarmChanged(AlarmClockInfo nextAlarm) {
            // mNextAlarm = nextAlarm;
            refreshState();
        }
        
    };
     
    public String formatNextAlarm(AlarmManager.AlarmClockInfo info) {
        if (info == null) {
            return mContext.getString(R.string.gn_qs_alarm);
        }
        
        // String skeleton = DateFormat.is24HourFormat(mContext) ? "EHm" : "Ehma";
        String skeleton = "EHm";
        String pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), skeleton);
        return DateFormat.format(pattern, info.getTriggerTime()).toString();
    }
}