package com.amigo.navi.keyguard.haokan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class NotificationReceiver extends BroadcastReceiver {

    
    public static final String ACTION_MUSIC_CLOSE = "haokan.action.MUSIC_CLOSE";
    public static final String ACTION_PLAYER_OR_PAUSE = "haokan.action.PLAYER_OR_PAUSE";
    
    public static final String TAG = "NotificationReceiver";
    
    public NotificationReceiver(Context context){
        
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        
        String action = intent.getAction();
        Log.v(TAG, "intent.getAction() = " + action);
        if (ACTION_MUSIC_CLOSE.equals(action)) {
            PlayerManager.getInstance().closeNotificationAndMusic();
        }else if (ACTION_PLAYER_OR_PAUSE.equals(action)) {
            PlayerManager.getInstance().pauseOrPlayer();
        }
        
    }

}
