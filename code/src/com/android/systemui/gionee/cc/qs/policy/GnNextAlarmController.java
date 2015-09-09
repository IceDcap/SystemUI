/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
package com.android.systemui.gionee.cc.qs.policy;

import android.app.AlarmManager;

public interface GnNextAlarmController{
    void addStateChangedCallback(Callback callback);
    void removeStateChangedCallback(Callback callback);
    boolean isNextAlarmEnabled();
    String getNextAlarm();
    
    public interface Callback {
        void onNextAlarmChanged(AlarmManager.AlarmClockInfo nextAlarm);
    }
}
