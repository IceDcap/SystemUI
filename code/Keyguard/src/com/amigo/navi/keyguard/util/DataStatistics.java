package com.amigo.navi.keyguard.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.modules.KeyguardNotificationModule;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.gionee.youju.statistics.sdk.YouJuAgent;

public class DataStatistics {

    private static final String LOG_TAG = "DataStatistics";

    /*
     * 1 show home,0 show music widget
     */
    private static final String SKYLIGHT_CLOSE_EVENT_ID = "skylight_homepage";
    public static final String SKYLIGHT_SHOW_HOME="1";
    public static final String SKYLIGHT_SHOW_MUSIC="0";
    /*
     * 1 slide to home,0 slide to music widget
     */
    private static final String SKYLIGHT_SLIDE_EVENT_ID = "skylight_slideto_homepage";
    public static final String SKYLIGHT_SLIDE_TO_HOME="1";
    public static final String SKYLIGHT_SLIDE_TO_MUSIC="0";
    
    private static final String GESTURE_UNLOCK="use_unlock_tg";
    private static final String DOUBLETAP_SLEEP="screenoff_doubleclick";
    private static final String SWIP_TO_REMOVE_NOTIFICATION="delete_notification_card";
    private static final String DOUBLE_TAP_NOTIFICATION="doubleclick_notification_card";
    private static final String EXPAND_NOTIFICATION="expand_notification_card";
    private static final String UNLOCK_WHEN_HAS_NOTIFICATION="unlock";
    
    
    
    private static final String EVENT_TIME_KEY="event_time";

    private static final Object mObject = new Object();

    private static DataStatistics sStatistics;

    public static DataStatistics getInstance() {
        synchronized (mObject) {
            if (sStatistics == null) {
                sStatistics = new DataStatistics();
            }
        }
        return sStatistics;
    }

    public void onResume(Context context) {
        YouJuAgent.onResume(context);
    }

    public void onPause(Context context) {
        YouJuAgent.onPause(context);
    }

    public void onInit(Context context) {
        YouJuAgent.init(context);
        YouJuAgent.setAssociateUserImprovementPlan(context, true);
        DebugLog.d(LOG_TAG, "DataStatistics  onInit");
    }

    public void unlockScreenWhenHasNotification(Context context){
        KeyguardNotificationModule module=KeyguardNotificationModule.getInstance(context, KeyguardUpdateMonitor.getInstance(context));
        if(module.hasNotification()){
            Map<String, Object> map=new HashMap<String,Object>();
            String time=new Date().toLocaleString();
            map.put(EVENT_TIME_KEY, time);
            onEvent(context, UNLOCK_WHEN_HAS_NOTIFICATION, "", map);
        }
    }
    public void expandNotification(Context context){
        Map<String, Object> map=new HashMap<String,Object>();
        String time=new Date().toLocaleString();
        map.put(EVENT_TIME_KEY, time);
        onEvent(context, EXPAND_NOTIFICATION, "", map);
    }
    public void doubleTapNotification(Context context){
        Map<String, Object> map=new HashMap<String,Object>();
        String time=new Date().toLocaleString();
        map.put(EVENT_TIME_KEY, time);
        onEvent(context, DOUBLE_TAP_NOTIFICATION, "", map);
        
    }
    public void swipToremoveNotification(Context context){
        Map<String, Object> map=new HashMap<String,Object>();
        String time=new Date().toLocaleString();
        map.put(EVENT_TIME_KEY, time);
        onEvent(context, SWIP_TO_REMOVE_NOTIFICATION, "", map);
        
    }
    public void doubleTapSleepEvent(Context context){
        Map<String, Object> map=new HashMap<String,Object>();
        String time=new Date().toLocaleString();
        map.put(EVENT_TIME_KEY, time);
        onEvent(context, DOUBLETAP_SLEEP, "", map);
        
    }
    
    public void gestureUnlock(Context context){
        Map<String, Object> map=new HashMap<String,Object>();
        String time=new Date().toLocaleString();
        map.put(EVENT_TIME_KEY, time);
        onEvent(context, GESTURE_UNLOCK, "", map);
    }
    
    
    public void skylightClose(Context context,String action){
        Map<String, Object> map=new HashMap<String,Object>();
        String time=new Date().toLocaleString();
        map.put(EVENT_TIME_KEY, time);
        onEvent(context, SKYLIGHT_CLOSE_EVENT_ID, action, map);
        
    }
    public void skylightSlide(Context context, String action){
        Map<String, Object> map=new HashMap<String,Object>();
        String time=new Date().toLocaleString();
        map.put(EVENT_TIME_KEY, time);
        onEvent(context, SKYLIGHT_SLIDE_EVENT_ID, action, map);
    }

    private void onEvent(Context context, String eventId) {
        try {
            YouJuAgent.onEvent(context, eventId);
            DebugLog.d(LOG_TAG, "onEvent1 eventId: " + eventId);
        } catch (Exception e) {
            DebugLog.e(LOG_TAG, "YouJuAgent onEvent Exception :", e);
        }
    }

    private void onEvent(Context context, String eventId, String eventLabel) {
        try {
            YouJuAgent.onEvent(context, eventId, eventLabel);
            DebugLog.d(LOG_TAG, "onEvent2 eventId: " + eventId + " eventLabel: " + eventLabel);
        } catch (Exception e) {
            DebugLog.e(LOG_TAG, "YouJuAgent onEvent Exception :", e);
        }
    }

    private void onEvent(Context context, String eventId, String eventLabel, Map<String, Object> map) {
        try {
            // Map's Value is only support type  String and Number
            YouJuAgent.onEvent(context, eventId, eventLabel, map);
            DebugLog.d(LOG_TAG, "onEvent3 ");
        } catch (Exception e) {
            DebugLog.e(LOG_TAG, "YouJuAgent onEvent Exception :", e);
        }
    }

}
