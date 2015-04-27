/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
package com.android.systemui.gionee.cc.fakecall;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.android.systemui.R;

public class GnFakeCall {
    private static final String LOG_TAG = "GnFakeCall";


    private static Context mContext;

    private static int mRealIndex = 0;

    private static Uri pickedUri;
    private static int ringTonePlayTime;
    private static int voicePlayTime;
    private static int musicID;
    private static String musicFile;
    private static boolean isVibrate;
    private static boolean isPlayLoop = true;
    private static SharedPreferences mPref = null;


    static List<GnUpdateUiStateCallback> mUiStateCallbacks = new ArrayList<GnUpdateUiStateCallback>();
    
    private static GnFakeCallCountDownTimer mCountDownTimer = new GnFakeCallCountDownTimer(
            GnConstants.CALL_PHONE_TIME, 1000) {

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            if (!GnCallingActivity.getIsVirtualCallIsRunning()) {
                Intent intent = createFakeCallIntent();
                mContext.startActivity(intent);
            }
        }
    };

    public GnFakeCall(Context context) {
        mContext = context;
        ringTonePlayTime = 45;
        musicID = 0;
    }

    public void ringPhone() {
        notifyLightUiState();
        startFakeCall();
        Log.i(LOG_TAG, "------------->ringPhone");
        /*Intent intent = new Intent(ACTION_OPERATE_FAKE_CALL);
        intent.putExtra(Constants.START_TASK_OPTR, true);
        mContext.sendBroadcast(intent);*/
    }

    public void canclePhone() {
        notifyLightUiState();
        cancleFakeCall();
        Log.i(LOG_TAG, "------------->canclePhone");
    }

    public static int getmRealIndex() {
        return mRealIndex;
    }

    public static void setmRealIndex(int index) {
        mRealIndex = index;
    }

    public void notifyLightUiState() {
        for (GnUpdateUiStateCallback callback : mUiStateCallbacks) {
            Log.d(LOG_TAG, "notifyLightUiState--------------");
            callback.updateUiState();
        }
    };

    public void registerUiStateCallback(GnUpdateUiStateCallback callback) {
        if (callback != null) {
            int size = mUiStateCallbacks.size();
            for (int i = size - 1; i >= 0; i--) {
                if (mUiStateCallbacks.get(i) == callback) {
                    mUiStateCallbacks.remove(i);
                }
            }
            mUiStateCallbacks.add(callback);
        }
    };

    public void unregisterUiStateCallback(GnUpdateUiStateCallback callback) {
        mUiStateCallbacks.remove(callback);
    };

    // Gionee <jiangxiao> <2014-01-08> modify for CR00989271 begin
    public void startFakeCall() {
        Log.d(LOG_TAG, "startFakeCall()");
        /*PendingIntent pendingIntent = createFakeCallPendingIntent();
        try {
            if (pendingIntent != null) {
               
                Log.d(LOG_TAG, "start alarm for fake call");
                AlarmManager alarmMgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
                long triggerAt = SystemClock.elapsedRealtime() + Constants.CALL_PHONE_TIME;
                alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pendingIntent);
               
            }
        } catch (Exception e) {
            Log.i(LOG_TAG, "------------->fake call start error!");
            e.printStackTrace();
        }*/
        
        mCountDownTimer.start();
    }

    public void cancleFakeCall() {
        Log.d(LOG_TAG, "cancleFakeCall()");
        /*PendingIntent pendingIntent = createFakeCallPendingIntent();
        if (pendingIntent != null) {
            AlarmManager alarmMgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            alarmMgr.cancel(pendingIntent);
        } else {
            Log.d(LOG_TAG, "can not cancel fake call, pendingIntent is null");
        }*/
        
        mCountDownTimer.cancel();
    }
    
    private static Intent createFakeCallIntent(){
        int index = getPosition();
        Log.d(LOG_TAG, "createFakeCallPendingIntent()  index:" + index);
        // Gionee <huangwt> <2015-4-1> modify for CR01460569 begin
        // pickedUri = android.provider.Settings.System.DEFAULT_RINGTONE_URI;
        pickedUri = getRingtone();
        if (pickedUri == null) {
            pickedUri = android.provider.Settings.System.DEFAULT_RINGTONE_URI;
        }
        // Gionee <huangwt> <2015-4-1> modify for CR01460569 end

        Bundle bundle = new Bundle();
        bundle.clear();
        String name = getName(index);
        bundle.putString("Name", name);
        bundle.putString("Number", "");
        bundle.putString("RingUri", pickedUri.toString());
        bundle.putInt("RingTonePlayTime", ringTonePlayTime);
        bundle.putInt("MusicID", musicID);
        bundle.putString("MusicFile", musicFile);
        bundle.putBoolean("Vibrate", isVibrate);
        bundle.putBoolean("PlayLoop", isPlayLoop);
        bundle.putInt("VoicePlayTime", voicePlayTime);

        Intent intent = new Intent(mContext, GnCallingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtras(bundle);
        return intent;
    }
    
    // Gionee <huangwt> <2015-4-1> add for CR01460569 begin
    private static Uri getRingtone() {
        Uri acturlUri = RingtoneManager.getActualDefaultRingtoneUri(mContext, RingtoneManager.TYPE_RINGTONE);
        
        if (isRingtoneExist(acturlUri)) {
            return acturlUri;
        } else {
            return null;
        }
    }

    private static boolean isRingtoneExist(Uri uri) {
        if (uri == null) {
            return false;
        }
        
        try {
            AssetFileDescriptor fd = mContext.getContentResolver().openAssetFileDescriptor(uri, "r");
            if (fd == null) {
                return false;
            } else {
                try {
                    fd.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    // Gionee <huangwt> <2015-4-1> add for CR01460569 end

    // Gionee <jiangxiao> <2014-01-08> modify for CR00989271 end
    public static String getName(int index) {
        String name = "";
        String[] array = mContext.getResources().getStringArray(R.array.gn_fc_caller);
        musicID = index;
        if (index < 0 || index > array.length - 1) {
            return name;
        }
        return array[index];
    }

    public String getContent(int index) {
        String content = getName(index) + ":" + "\n" + "\n";
        String[] array = mContext.getResources().getStringArray(R.array.content);
        return content + array[index];
    }

    // Gionee <jiangxiao> <2014-01-08> modify for CR00989271 begin
    public static int getPosition() {
        if (mPref == null) {
            mPref = mContext.getSharedPreferences("phoneSetting", Context.MODE_WORLD_READABLE
                    | Context.MODE_WORLD_WRITEABLE);
        }

        return mPref.getInt("position", 0);
    }
    // Gionee <jiangxiao> <2014-01-08> modify for CR00989271 end

}
