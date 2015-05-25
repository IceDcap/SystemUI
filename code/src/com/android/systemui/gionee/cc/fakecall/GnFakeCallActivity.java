/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
package com.android.systemui.gionee.cc.fakecall;

import java.util.Timer;
import java.util.TimerTask;

import com.android.systemui.gionee.cc.util.GnRingerModeRecoveryUtils;
import com.android.systemui.gionee.cc.util.GnShortcutUtils;

import amigo.provider.AmigoSettings;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.systemui.R;

public class GnFakeCallActivity extends Activity {
    private static final String LOG_TAG = "CallingActivity";
    private static final String CALLER_NAME_KEY = "callInName";
    private static final String CALLER_NUM_KEY = "callInNum";
    
    // Gionee <huangwt> <20150214> add for CR01448781 begin
    private static final String AMIGO_SETTING_VIBRATE = "amigo_vibration_switch";
    private static final String AMIGO_SETTING_VOICE = "amigo_silent_switch";
    // Gionee <huangwt> <20150214> add for CR01448781 end

    private static boolean sIsVirtualCallIsRunning = false;
    
    private ImageView mHangupButton = null;

    private TextView txtCallinDuration;
    private String mCallInName;
    private String mCallInNum;

    private MediaPlayer mMediaPlayer = null;
    
    private GnRippleBackground mRipple;
    private GnMultiWaveView mMultiWave;

    // Gionee <jiangxiao> <2014-01-09> modify for CR01010716 begin
    private RingtoneArgs mRingtoneArgs = null;
    
    private static class RingtoneArgs {
        private Ringtone mRingtone = null;
        private Vibrator mVibrator = null;
        private int mDuration = GnConstants.DEFAULT_RINGTON_DURATION;

        public void playRingtone() {
            if (mRingtone != null) {
                Log.d(LOG_TAG, "RingtoneArgs: play Ringtone");
                mRingtone.play();
            } else {
                Log.d(LOG_TAG, "RingtoneArgs: no Ringtone can be used");
            }
        }

        // Gionee <jiangxiao> <2014-03-12> add for CR01112819 begin
        public void replayRingtoneIfStopped() {
            if (mRingtone != null && !mRingtone.isPlaying()) {
                mRingtone.play();
            }
        }

        public void releaseRingtone() {
            Log.d(LOG_TAG, "RingtoneArgs: releaseRingtone()");
            if (mRingtone != null) {
                if (mRingtone.isPlaying()) {
                    mRingtone.stop();
                    mRingtone = null;
                }
            }
        }
        // Gionee <jiangxiao> <2014-03-12> add for CR01112819 end

        // Gionee <jiangxiao> <2014-03-12> modify for CR01112819 begin
        public void playVibrator() {
            if (mVibrator != null) {
                Log.d(LOG_TAG, "play vibrator");
                long[] beats2 = {
                        800, 2000, 800, 2000, 800, 2000, 800, 2000, 800, 2000,
                };
                mVibrator.vibrate(beats2, 0);
            }
        }

        // Gionee <jiangxiao> <2014-03-12> modify for CR01112819 end

        // Gionee <jiangxiao> <2014-03-12> add for CR01112819 begin
        public void releaseVibrator() {
            if (mVibrator != null) {
                mVibrator.cancel();
                mVibrator = null;
            }
        }
        // Gionee <jiangxiao> <2014-03-12> add for CR01112819 end

        public int getDuration() {
            return mDuration;
        }

        public boolean isVibrated() {
            return (mVibrator != null);
        }

        private RingtoneArgs(Ringtone ringtone, int duration, Vibrator vibrator) {
            this.mRingtone = ringtone;
            this.mDuration = duration;
            this.mVibrator = vibrator;
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
        public static RingtoneArgs create(Bundle bundle, Context context) {
            Log.d(LOG_TAG, "createRingtoneData()");
            RingtoneArgs args = null;

            Uri uri = Uri.parse(bundle.getString("RingUri", ""));
            if (uri != null) {
                Log.d(LOG_TAG, "ringtone uri is " + uri.toString());
                Ringtone ringtone = RingtoneManager.getRingtone(context, uri);

                int duration = bundle.getInt("RingTonePlayTime",
                        GnConstants.DEFAULT_RINGTON_DURATION);
                Log.d(LOG_TAG, "ringtone duration is " + duration);

                // boolean vibrated = bundle.getBoolean("Vibrate", false);
                AudioManager audioMgr = (AudioManager) context
                        .getSystemService(Context.AUDIO_SERVICE);
                boolean vibrated = audioMgr.getRingerMode() != AudioManager.RINGER_MODE_SILENT;
                Log.d(LOG_TAG, "ringtone vibrated " + vibrated);
                Vibrator vibrator = null;
                if (vibrated) {
                    vibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
                }

                args = new RingtoneArgs(ringtone, duration, vibrator);
            } else {
                Log.d(LOG_TAG, "fail to parse ringtone uri");
            }

            return args;
        }
    }
    // Gionee <jiangxiao> <2014-01-09> modify for CR01010716 end

    private final int CALL_INCOMING_VIEW = 0;
    private final int CALL_PROCESS_VIEW = 1;
    private final int CALL_END_VIEW = 2;

    private final int CALL_END_EVENT = 1;
    private final int RINGTONG_PLAY_EVENT = 2;
    private final int RINGTONG_PLAY_CHECK = 3;
    private final int VOICE_PLAY_CHECK = 4;
    private final int MSG_UPDATE_TIME = 5;

    private final DelayHandler mHandler = new DelayHandler();
    private int mCallState;
    private int mPlayTime = 1;
    private final int DELAY_TIME = 2;
    private final int DELAY_SECONDS = 1000 * DELAY_TIME;

    // Gionee <jiangxiao> <2014-01-08> add for CR00989271 begin
    private static final int VOICE_RES_ID_WANGSHIFU = 0;
    private static final int VOICE_RES_ID_LIZONE = 1;
    private static final int VOICE_RES_ID_LINGDAO = 2;

    // Gionee <caody><2013-11-19> modify for CR00954727 begin
    private int mVoiceResId = VOICE_RES_ID_LIZONE;
    // Gionee <caody><2013-11-19> modify for CR00954727 end
    // Gionee <jiangxiao> <2014-01-08> add for CR00989271 end

    private AudioManager mAudioManager = null;
    private int mAudioModeBeforePlayFakeVoice = 0;
    private GnAudioManagerHelper mAudioManagerHelper = null;
    private int mMusicStreamVolume = -1;
    // Gionee <jiangxiao> <2014-01-09> modify for CR01010716 begin
    private VoiceArgs mVoiceArgs = null;

    private static class VoiceArgs {
        
        private static final int DEFAULT_PLAY_TIMES = 60;
        private int mTimes = 0;
        private boolean mLoopedPlay = true;

        // Gionee <jiangxiao> <2014-03-12> add for CR01112819 begin
        public int getPlayTimes() {
            return mTimes;
        }

        public boolean isLoopedPlay() {
            return mLoopedPlay;
        }
        // Gionee <jiangxiao> <2014-03-12> add for CR01112819 end

        private VoiceArgs(int times, boolean isLooped) {
            this.mTimes = times;
            this.mLoopedPlay = isLooped;
        }

        public static VoiceArgs create(Bundle bundle) {
            VoiceArgs args = null;
            int times = bundle.getInt("VoicePlayTime", DEFAULT_PLAY_TIMES);
            boolean isLooped = bundle.getBoolean("PlayLoop", true);
            args = new VoiceArgs(times, isLooped);

            return args;
        }
    }
    // Gionee <jiangxiao> <2014-01-09> modify for CR01010716 end



    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;

    public int mRingtoneModeWhenFakeCallStart = 0;

    private SharedPreferences mPref = null;
    
    private SharedPreferences mSettingPref = null;
    private Editor mSettingPrefEditor = null;
    
    private SharedPreferences mPhonePref = null;
    private Editor mPhonePrefEditor = null;
    
    private int curVolume;
    private static int mRingStreamVolume = 0;
    private boolean mNeedChangeRingtoneMode = false;
    private BroadcastReceiver mReceiver;
    
    // Gionee <caody><2013-11-23> modify for CR00947966 begin
    private static final int FACK_CALL_ID = 1;
    private NotificationManager mNotificationManager;
    private Notification mNotification;
    // Gionee <caody><2013-11-23> modify for CR00947966 end
    
    
    // Gionee <caody><2013-12-12> modify for CR00971849 begin
    private boolean mIsHangupButtonClicked = false;
    // Gionee <caody><2013-12-12> modify for CR00971849 end

    // Gionee <jiangxiao> <2014-01-08> modify for CR00989271 begin
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "CallingActivity.oncreate() start");
        
        // Gionee <jingyn><2014-04-16> add for CR01174083 begin
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        // Gionee <jingyn><2014-04-16> add for CR01174083 end

        mIsHangupButtonClicked = false;

        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        registerCallStateListener();
        requestAudioFocus();
        
        mWakeLock = mPowerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, 
                LOG_TAG);
        mWakeLock.setReferenceCounted(false);
        mWakeLock.acquire();
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setIsVirtualCallIsRunning(true);
        mAudioManagerHelper = new GnAudioManagerHelper();
        mPref = getSharedPreferences("phoneSetting", Context.MODE_WORLD_WRITEABLE
                + Context.MODE_WORLD_READABLE);
        mSettingPref = getSharedPreferences("volumeSetting", Context.MODE_PRIVATE);

        mPhonePref = getSharedPreferences("faceCallSetting", Context.MODE_WORLD_READABLE
                | Context.MODE_WORLD_WRITEABLE);
        mPhonePrefEditor = mPhonePref.edit();
        int index = mPref.getInt("position", 0);
        getPhoneData(index);
        
        switchView(CALL_INCOMING_VIEW);
        // enableHomeKeyDispatched(true);
        // Gionee <huangwt> <2015-3-30> modify for CR01456154 begin
        // mHandler.sendDelayedDefault(RINGTONG_PLAY_EVENT);
        mHandler.sendEmptyMessage(RINGTONG_PLAY_EVENT);
        // Gionee <huangwt> <2015-3-30> modify for CR01456154 end
        
        registerProximitySensor();
        
        initBroadcastReceiver();
        initNotifyMessage();
        GnFakeCallControllerImpl virtualCallHelper = GnFakeCallControllerImpl.getInstance();
        if (virtualCallHelper != null) {
            virtualCallHelper.dismissControlCenter();
        }
        
    }
    // Gionee <jiangxiao> <2014-01-08> modify for CR00989271 end

    private void registerCallStateListener() {
        Log.d(LOG_TAG, "registerCallStateListener()");
        final TelephonyManager teleMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        teleMgr.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private void unregisterCallStateListener() {
        Log.d(LOG_TAG, "unregisterCallStateListener()");
        final TelephonyManager teleMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        teleMgr.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
    }

    // audio focus begin
    
    // Gionee <caody><2013-12-13> modify for CR00968733 begin
    private AudioManager mAudioManagerForAudioFocus;
    private OnAudioFocusChangeListener mAudioFocusListener = null;
    // Gionee <caody><2013-12-13> modify for CR00968733 end

    private void requestAudioFocus() {
        mAudioFocusListener = new OnAudioFocusChangeListener() {
            public void onAudioFocusChange(int focusChange) {
                if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {

                }
            }
        };
        
        mAudioManagerForAudioFocus = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManagerForAudioFocus.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_RING,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
    }

    private void abandonAudioFocus() {
        mAudioManagerForAudioFocus.abandonAudioFocus(mAudioFocusListener);
    }
    // audio focus end

    private String getCallStateString(int stateId) {
        String callState = TelephonyManager.EXTRA_STATE_IDLE;
        if (stateId == TelephonyManager.CALL_STATE_RINGING) {
            callState = TelephonyManager.EXTRA_STATE_RINGING;
        } else if (stateId == TelephonyManager.CALL_STATE_OFFHOOK) {
            callState = TelephonyManager.EXTRA_STATE_OFFHOOK;
        }

        return callState;
    }

    PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            Log.d(LOG_TAG, "onCallStateChanged() state=" + getCallStateString(state));

            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    restoreRingtoneMode();
                    break;

                case TelephonyManager.CALL_STATE_RINGING:
                    if (mCallState != CALL_INCOMING_VIEW) {
                        onCallStateBusy();
                    } else {
                        endCall();
                    }
                    // mAudioManager.setMode(AudioManager.MODE_IN_CALL);
                    break;

                case TelephonyManager.CALL_STATE_OFFHOOK:
                    onCallStateBusy();
                    restoreRingtoneMode();
                    endCall();
                    break;

                default:
                    break;
            }
        }

        private void restoreRingtoneMode() {
            int currentRingtoneMode = mAudioManager.getRingerMode();
            if (currentRingtoneMode != mRingtoneModeWhenFakeCallStart) {
                Log.d(LOG_TAG, "restore ringtone mode to " + mRingtoneModeWhenFakeCallStart);
                mAudioManager.setRingerMode(mRingtoneModeWhenFakeCallStart);
            }
        }

        private void onCallStateBusy() {
            Log.d(LOG_TAG, "onCallStateBusy() change mode to vibrate");
            stopFakeCallVoice();
            mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            // mHandler.sendDelayedDefault(CALL_END_EVENT);
        }
    };

    private void initBroadcastReceiver() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(LOG_TAG, "CallingActivity BroadcastReceiver finshself");
                mNotificationManager.cancel(FACK_CALL_ID);
                finish();
                setIsVirtualCallIsRunning(false);
            }
        };

        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction("com.android.gionee.navi.shortcuttools");
        registerReceiver(mReceiver, mFilter);
    }

    // Gionee <jiangxiao> <2014-02-12> modify for CR01046229 begin
    // Gionee <caody><2013-11-23> modify for CR00947966 begin
    @SuppressLint("NewApi")
    private void initNotifyMessage() {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        CharSequence tickerText = getResources().getString(R.string.gn_fc_calling);
        CharSequence contentTitle = getStringTitle();
        CharSequence contentText = getStringContent();
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                GnFakeCallActivity.class), 0);

        // use builder to replace Notification.setLatestEventInfo()
        Notification.Builder builder = new Notification.Builder(this);
        mNotification = builder.setContentTitle(contentTitle).setContentText(contentText)
                .setContentIntent(contentIntent).setTicker(tickerText)
                .setSmallIcon(R.drawable.gn_stat_sys_fake_call).build();
        mNotification.flags = (Notification.FLAG_AUTO_CANCEL | Notification.FLAG_NO_CLEAR);
    }

    private CharSequence getStringTitle() {
        CharSequence title = mCallInName;
        if (TextUtils.isEmpty(title)) {
            title = getResources().getString(R.string.gn_sc_fake_call);
        }
        
        return title;
    }

    private CharSequence getStringContent() {
        CharSequence content = mCallInNum;
        if (TextUtils.isEmpty(content)) {
            content = getResources().getString(R.string.gn_sc_fake_call);
        }
        
        return content;
    }

    public static boolean getIsVirtualCallIsRunning() {
        return sIsVirtualCallIsRunning;
    }

    private static void setIsVirtualCallIsRunning(boolean isRunning) {
        sIsVirtualCallIsRunning = isRunning;
    }
    // Gionee <caody><2013-11-23> modify for CR00947966 end
    // Gionee <jiangxiao> <2014-02-12> modify for CR01046229 end

    /*
     * void enableHomeKeyDispatched(boolean enable) { final Window window =
     * getWindow(); final WindowManager.LayoutParams lp =
     * window.getAttributes(); if (enable) { lp.flags |=
     * WindowManager.LayoutParams.FLAG_HOMEKEY_DISPATCHED; } else { lp.flags &=
     * ~WindowManager.LayoutParams.FLAG_HOMEKEY_DISPATCHED; }
     * window.setAttributes(lp); }
     */

    // Gionee <jiangxiao> <2014-01-08> modify for CR00989271 begin
    // Gionee <caody><2013-9-23> modify for CR00906878 begin
    private void getPhoneData(int index) {
        getCallInfoFromIntent();

        // Gionee <jiangxiao> <2014-03-12> modify for CR01112819 begin
        Bundle bundle = this.getIntent().getExtras();
        if (bundle == null) {
            bundle = new Bundle();
        }
        mRingtoneArgs = RingtoneArgs.create(bundle, this.getBaseContext());
        mVoiceArgs = VoiceArgs.create(bundle);
        // Gionee <jiangxiao> <2014-01-09> modify for CR01010716 end
        bundle.clear();
        // Gionee <jiangxiao> <2014-03-12> modify for CR01112819 end
        
        // Gionee <caody><2013-11-19> modify for CR00954727 begin
        mVoiceResId = 1;// index;// bundle.getInt("MusicID", 0);
        createMediaPlayerByVoiceFileId();
        // Gionee <caody><2013-11-19> modify for CR00954727 end
        
        // should move out of this method
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // mAudioManager.setMode(AudioManager.MODE_NORMAL);
        mRingtoneModeWhenFakeCallStart = mAudioManager.getRingerMode();

        GnRingerModeRecoveryUtils.saveRingerModeArgs(mAudioManager.getRingerMode(),
                mAudioManager.getStreamVolume(AudioManager.STREAM_RING));

        Log.i(LOG_TAG, "save current ringtone mode: " + mRingtoneModeWhenFakeCallStart);
        if (mRingtoneModeWhenFakeCallStart == AudioManager.RINGER_MODE_VIBRATE) {
            /*
             * mIsVibrated = true; mVibrator = (Vibrator) this
             * .getSystemService(Service.VIBRATOR_SERVICE);
             */
        } else {
            if (mRingtoneModeWhenFakeCallStart != AudioManager.RINGER_MODE_SILENT) {
                mNeedChangeRingtoneMode = true;

                // Gionee <huangwt> <20150214> delete for CR01448549 begin
                // adjustRingerVolumeBeforeStartFakeCall();
                // Gionee <huangwt> <20150214> delete for CR01448549 end
            }
        }
    }

    private void adjustRingerVolumeBeforeStartFakeCall() {
        // mUserVolume = mAudioManagerHelper.getInitVolume(mAudioManager);
        mRingStreamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
        Log.i(LOG_TAG, "mRingStreamVolume=" + mRingStreamVolume);

        mMusicStreamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        curVolume = mAudioManagerHelper.getInitVolume(mAudioManager);
        int preVolume = mSettingPref.getInt("volume", 0);
        if (preVolume == 0) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_RING, mMusicStreamVolume, 0);
        } else {
            mAudioManager.setStreamVolume(AudioManager.STREAM_RING, preVolume, 0);
        }
    }
    // Gionee <jiangxiao> <2014-01-08> modify for CR00989271 end
    // Gionee <caody><2013-9-23> modify for CR00906878 end

    private void getCallInfoFromIntent() {
        Intent intent = getIntent();
        mCallInName = intent.getStringExtra(CALLER_NAME_KEY);
        String callInNum = intent.getStringExtra(CALLER_NUM_KEY);
        String unknownName = getResources().getString(R.string.gn_fc_unknown);
        if (TextUtils.isEmpty(mCallInName)) {
            mCallInName = TextUtils.isEmpty(callInNum) ? unknownName : callInNum;
        }
        Log.d(LOG_TAG, "mCallInNum: " + mCallInNum + "  mCallInName:" + mCallInName);
    }

    // Gionee <jiangxiao> <2014-01-08> modify for CR00989271 begin
    private void createMediaPlayerByVoiceFileId() {
        Log.d(LOG_TAG, "createMediaPlayerByVoiceFileId() mVoiceResId=" + mVoiceResId);
        mMediaPlayer = MediaPlayer.create(GnFakeCallActivity.this, getVoiceResId());
        mMediaPlayer.setLooping(true/* mVoiceArgs.isLoopedPlay() */);

        mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                /*
                 * if (mVoiceArgs.isLoopedPlay()) { mHandler.sendMessageDelayed(
                 * mHandler.obtainMessage(VOICE_PLAY_CHECK), 1000 *
                 * mVoicePlayTimes); }
                 */
            }
        });
        
        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.d(LOG_TAG, "some error happened in playback");
                return false;
            }
        });
    }
    
    private int getVoiceResId() {
        int resId = R.raw.lizong;
        switch (mVoiceResId) {
            case 0:
                resId = R.raw.wangshifu;
                break;
            case 1:
                resId = R.raw.lizong;
                break;
            case 2:
                resId = R.raw.lingdao;
                break;
            default:
                break;
        }
        
        return resId;
    }
    // Gionee <jiangxiao> <2014-01-08> modify for CR00989271 end

    // Gionee <jiangxiao> <2014-01-08> modify for CR00989271 begin
    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
    
    private void releaseRingtone() {
        if (mRingtoneArgs != null) {
            mRingtoneArgs.releaseRingtone();
        }
    }
    
    private void releaseVibrator() {
        /*
         * if (mVibrator != null) { mVibrator.cancel(); mVibrator = null; }
         */
        if (mRingtoneArgs != null) {
            mRingtoneArgs.releaseVibrator();
        }
    }
    // Gionee <jiangxiao> <2014-01-08> modify for CR00989271 end

    // Gionee <jiangxiao> <2014-01-08> modify for CR00989271 begin
    private void onHangupButtonClicked() {
        Log.d(LOG_TAG, "onHangupButtonClicked()");
        mIsHangupButtonClicked = true;
        if (mCallState == CALL_INCOMING_VIEW) {
            // Gionee <caody><2013-9-23> modify for CR00906878 begin
            if (mNeedChangeRingtoneMode) {
                // Log.d(LOG_TAG, "change ring stream volume to " +
                // mRingStreamVolume);
                // mAudioManager.setStreamVolume(AudioManager.STREAM_RING,
                // mRingStreamVolume, 0);
            }
            // CallingActivity.this.finish();
        } else if (mCallState == CALL_PROCESS_VIEW) {
            // execute this method in the onDestroy() will cause
            // we can listen the fake call voice for a while
            // so run this method before run onDestroy()
            stopFakeCallVoice();
            releaseMediaPlayer();
        }
        
        GnFakeCallActivity.this.finish();
        setIsVirtualCallIsRunning(false);
        // Gionee <caody><2013-9-23> modify for CR00906878 end
        // android.os.Process.killProcess(android.os.Process.myPid());
    }
    
    private void onAnwserButtonClicked() {
        Log.d(LOG_TAG, "onAnwserButtonClicked()");
        stopRingtone();
        switchView(CALL_PROCESS_VIEW);
    }
    // Gionee <jiangxiao> <2014-01-08> modify for CR00989271 end

    // Gionee <jiangxiao> <2014-01-08> modify for CR00989271 begin
    private void endCall() {
        // stopRingTone();
        // release();
        mHandler.sendDelayedDefault(CALL_END_EVENT);
    }
    // Gionee <jiangxiao> <2014-01-08> modify for CR00989271 end

    // Gionee <jiangxiao> <2014-01-09> modify for CR00989271 begin
    private void switchView(int state) {
        mCallState = state;
        // Gionee <jiangxiao> <2013-12-24> add for CR00990021 begin
        notifyFakeInCallStateChanged(state);
        // Gionee <jiangxiao> <2013-12-24> add for CR00990021 end
        switch (state) {
            case CALL_INCOMING_VIEW:
                switchViewThenInitButton(R.layout.gn_sc_answer_view, false);
                mRipple = (GnRippleBackground) findViewById(R.id.ripple_background);
                mMultiWave = (GnMultiWaveView) findViewById(R.id.glow_pad_view);
                mMultiWave.setVisibility(View.VISIBLE);
                mRipple.startRippleAnimation();
                mMultiWave.setAnswerListener(mAnswerListener);
                mMultiWave.setRippleListener(mRippleListener);
                break;
            case CALL_PROCESS_VIEW:
                switchViewThenInitButton(R.layout.gn_sc_calling_view, true);
                startCountTime();
                initCallingView();
                playFakeCallVoice();
                break;
            case CALL_END_VIEW:
                switchViewThenInitButton(R.layout.gn_sc_callend_view, false);
                break;
            default:
                break;
        }
    }

    Timer timer = new Timer();

    private void startCountTime() {
        final long startTime = SystemClock.elapsedRealtime();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                long durationTime = SystemClock.elapsedRealtime() - startTime;
                String duration = GnShortcutUtils.formatMillisTime(durationTime);
                Message msg = mHandler.obtainMessage();
                msg.obj = duration;
                msg.what = MSG_UPDATE_TIME;
                mHandler.sendMessage(msg);
            }
        };
        timer.schedule(task, 0, 500);
    }
    
    private void switchViewThenInitButton(int layoutId, boolean needSleep) {
        mNeedSleep = needSleep;
        setContentView(layoutId);
    }
    // Gionee <jiangxiao> <2014-01-09> modify for CR00989271 end

    // Gionee <caody><2013-8-25> modify for CR00873432 begin
    private void playFakeCallRingtone() {
        Log.d(LOG_TAG, "playFakeCallRingtone()");
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        // if we change audio mode to CALL, the ring of fake call can not sound
        // any volume
        // mAudioManager.setMode(AudioManager.MODE_IN_CALL);
        
        // Gionee <huangwt> <20150214> modify for CR01448781 begin
        boolean vibrate = AmigoSettings.getInt(getContentResolver(), AMIGO_SETTING_VIBRATE, 0) == 1;
        if (vibrate) {
            mRingtoneArgs.playVibrator();
        }
        
        boolean voice = AmigoSettings.getInt(getContentResolver(), AMIGO_SETTING_VOICE, 0) == 0;
        if (voice) {
            mRingtoneArgs.playRingtone();
        }
        // Gionee <huangwt> <20150214> modify for CR01448781 end

        Log.d(LOG_TAG,
                "after playRingtone(), the ring mode is "
                        + ringModeToString(mAudioManager.getRingerMode()));
        Log.d(LOG_TAG,
                "after playRingtone(), the audio mode is "
                        + audioModeToString(mAudioManager.getMode()));
        Log.d(LOG_TAG,
                "after playRingtone(), the ring volume is "
                        + mAudioManager.getStreamVolume(AudioManager.STREAM_RING));
        // Gionee <jiangxiao> <2014-01-09> modify for CR01010716 end

        mHandler.sendDelayedDefault(RINGTONG_PLAY_CHECK);
    }
    
    private String ringModeToString(int ringerMode) {
        String modeString = "Normal";
        if (ringerMode == AudioManager.RINGER_MODE_SILENT) {
            modeString = "Silent";
        } else if (ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
            modeString = "Vibrate";
        }

        return modeString;
    }
    
    private String audioModeToString(int audioMode) {
        String modeString = "Normal";
        if (audioMode == AudioManager.MODE_CURRENT) {
            modeString = "Current";
        } else if (audioMode == AudioManager.MODE_IN_CALL) {
            modeString = "Call";
        } else if (audioMode == AudioManager.MODE_IN_COMMUNICATION) {
            modeString = "Communication";
        } else if (audioMode == AudioManager.MODE_INVALID) {
            modeString = "Invalid";
        } else if (audioMode == AudioManager.MODE_RINGTONE) {
            modeString = "Ringtone";
        }

        return modeString;
    }
    // Gionee <caody><2013-8-25> modify for CR00873432 end
    
    private void initCallingView() {
        if (mCallState == CALL_INCOMING_VIEW || mCallState == CALL_PROCESS_VIEW) {
            mHangupButton = (ImageView) findViewById(R.id.ibtnHangupDial);
            mHangupButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onHangupButtonClicked();
                }
            });
        }

        txtCallinDuration = (TextView) findViewById(R.id.txtCallDuration);
        txtCallinDuration.setText(mCallInNum);
    }

    // Gionee <jiangxiao> <2014-01-08> modify for CR00989271 begin
    private void release() {
        stopRingtone();
        releaseVibrator();
        releaseMediaPlayer();
        unregisterProximitySensor();
        unregisterReceiver(mReceiver);
        mNotificationManager.cancel(FACK_CALL_ID);
    }
    // Gionee <jiangxiao> <2014-01-08> modify for CR00989271 end

    private void stopRingtone() {
        mHandler.removeMessages(RINGTONG_PLAY_CHECK);
        releaseRingtone();
        releaseVibrator();
    }

    private void stopFakeCallVoice() {
        Log.d(LOG_TAG, "stopFakeCallVoice()");
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            Log.d(LOG_TAG,
                    "before recover audio mode: " + audioModeToString(mAudioManager.getMode()));
            mAudioManager.setMode(mAudioModeBeforePlayFakeVoice);
            Log.d(LOG_TAG, "after recover audio mode: "
                    + audioModeToString(mAudioManager.getMode()));
        }
    }

    private void playFakeCallVoice() {
        Log.d(LOG_TAG, "playFakeCallVoice()");
        if (mMediaPlayer != null) {
            mAudioModeBeforePlayFakeVoice = mAudioManager.getMode();
            Log.d(LOG_TAG, "mAudioModeBeforePlayFakeVoice is "
                    + audioModeToString(mAudioModeBeforePlayFakeVoice));
            mAudioManager.setMode(AudioManager.MODE_IN_CALL);
            // mMediaPlayer.start();
        }
    }

    // Gionee <jiangxiao> <2014-01-08> modify for CR00989271 begin
    protected class DelayHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CALL_END_EVENT:
                    handleCallEndEvent();
                    break;

                case RINGTONG_PLAY_EVENT:
                    handleRingtonePlayEvent();
                    break;

                case RINGTONG_PLAY_CHECK:
                    handleRingtonePlayCheck();
                    break;

                case VOICE_PLAY_CHECK:
                    handleVoicePlayCheck();
                    break;
                case MSG_UPDATE_TIME:
                    String timeDuration = (String) msg.obj;
                    handleRefreshCallTime(timeDuration);
                    break;
                default:
                    break;
            }
        }

        private void handleVoicePlayCheck() {
            Log.d(LOG_TAG, "handleVoicePlayCheck()");
            if (mCallState != CALL_PROCESS_VIEW) {
                return;
            }

            mMediaPlayer.reset();
            // mAudioManager.setMode(AudioManager.MODE_IN_CALL);
            mMediaPlayer.start();
        }

        private void handleRingtonePlayCheck() {
            Log.d(LOG_TAG, "handleRingtonePlayCheck()");
            int msgId = -1;
            // Gionee <jiangxiao> <2014-03-12> add for CR01112819 begin
            if (mRingtoneArgs == null || mCallState != CALL_INCOMING_VIEW) {
                Log.d(LOG_TAG, "handleRingtonePlayCheck() - 1");
                msgId = RINGTONG_PLAY_CHECK;
            } else {
                mPlayTime += DELAY_TIME;
                if (mPlayTime > mRingtoneArgs.getDuration()) {
                    Log.d(LOG_TAG, "handleRingtonePlayCheck() - 2");
                    stopRingtone();
                    msgId = CALL_END_EVENT;
                } else {
                    Log.d(LOG_TAG, "handleRingtonePlayCheck() - 3");
                    mRingtoneArgs.replayRingtoneIfStopped();
                    msgId = RINGTONG_PLAY_CHECK;
                }
            }
            // Gionee <jiangxiao> <2014-03-12> add for CR01112819 end

            mHandler.sendDelayedDefault(msgId);
        }

        private void handleRingtonePlayEvent() {
            Log.d(LOG_TAG, "handleRingtonePlayEvent()");
            playFakeCallRingtone();
        }

        private void handleCallEndEvent() {
            Log.d(LOG_TAG, "handleCallEndEvent()");
            mIsHangupButtonClicked = true;
            GnFakeCallActivity.this.finish();
            setIsVirtualCallIsRunning(false);
        }

        private void handleRefreshCallTime(String timeDuration) {
            String str = GnFakeCallActivity.this.getResources().getString(R.string.gn_fc_incall);
            if (txtCallinDuration != null) {
                if (!TextUtils.isEmpty(timeDuration)) {
                    txtCallinDuration.setText(str + " " + timeDuration);
                } else {
                    txtCallinDuration.setText(str);
                }
            }
        }

        public void sendDelayedDefault(int msgId) {
            mHandler.sendMessageDelayed(mHandler.obtainMessage(msgId), DELAY_SECONDS);
        }
    }
    // Gionee <jiangxiao> <2014-01-08> modify for CR00989271 end

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // int volume = mAudioManagerHelper.getInitVolume(mAudioManager);
        int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                mSettingPrefEditor = mSettingPref.edit();
                mSettingPrefEditor.putInt("volume", volume);
                mSettingPrefEditor.commit();
                break;

            case KeyEvent.KEYCODE_VOLUME_UP:
                mSettingPrefEditor = mSettingPref.edit();
                mSettingPrefEditor.putInt("volume", volume);
                mSettingPrefEditor.commit();
                break;
            case KeyEvent.KEYCODE_BACK:
                return true;
            case KeyEvent.KEYCODE_HOME:
                return true;

        }
        return super.onKeyDown(keyCode, event);
    }

    // Gionee <jiangxiao> <2014-01-08> modify for CR00989271 begin
    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        boolean isBackKey = false;
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            isBackKey = true;
        }
        
        return (isBackKey || super.onKeyLongPress(keyCode, event));
    }

    @Override
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        boolean isBackKey = false;
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            isBackKey = true;
        }
        
        return (isBackKey || super.onKeyMultiple(keyCode, repeatCount, event));
    }
    // Gionee <jiangxiao> <2014-01-08> modify for CR00989271 end

    // Gionee <jiangxiao> <2014-01-08> modify for CR00989271 begin
    // Gionee <caody><2013-11-23> modify for CR00947966 begin
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume()");
        mNotificationManager.cancel(FACK_CALL_ID);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause()");
        sendNotification();
        mWakeLock.release();
    }
    // Gionee <caody><2013-11-23> modify for CR00947966 end

    private void sendNotification() {
        // Gionee <caody><2013-12-11> modify for CR00965689 begin
        if (null != mNotification && null != mNotificationManager) {
            // Gionee <caody><2013-12-12> modify for CR00971849 begin
            if (!mIsHangupButtonClicked) {
                try {
                    mNotificationManager.notify(FACK_CALL_ID, mNotification);
                } catch (Exception e) {
                    Log.d(LOG_TAG, "JE happened on calling mNotificationManager.notify()");
                }
            }
            // Gionee <caody><2013-12-12> modify for CR00971849 begin
        }
        // Gionee <caody><2013-12-11> modify for CR00965689 end
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy()");
        setIsVirtualCallIsRunning(false);
        unregisterCallStateListener();
        
        // Gionee <caody><2013-12-13> modify for CR00968733 begin
        abandonAudioFocus();
        // Gionee <caody><2013-12-13> modify for CR00968733 end
        
        // restore volume will cause the real in call playing ringer
        // mAudioManager.setStreamVolume(AudioManager.STREAM_RING,
        // mRingStreamVolume, 0);
        // mAudioManager.setMode(AudioManager.MODE_NORMAL);
        if (mAudioManager.getMode() != mAudioModeBeforePlayFakeVoice) {
            Log.d(LOG_TAG, "maybe stopFakeCallVoice() don't be executed");
            mAudioManager.setMode(mAudioModeBeforePlayFakeVoice);
        } else {
            Log.d(LOG_TAG, "current audio mode is " + audioModeToString(mAudioManager.getMode()));
        }
        GnRingerModeRecoveryUtils.sendRingerModeRecoveryRequest(this.getBaseContext());
        
        release();
    }
    // Gionee <jiangxiao> <2014-01-08> modify for CR00989271 end

    // Gionee <jiangxiao> <2014-01-08> modify for CR00989271 begin
    // proximity sensor begin

    private boolean mNeedSleep = false;

    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor arg0, int arg1) {
            // TODO Auto-generated method stub
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                if (mNeedSleep) {
                    float proximity = event.values[0];
                    Log.i(LOG_TAG, "--------------->proximity:" + proximity);
                    if (proximity > 0.0) {
                        Log.i(LOG_TAG, "--------------->fast");
                        // Gionee <jingyn><2014-04-16> modify for CR01199894 begin
                        mPowerManager.wakeUp(SystemClock.uptimeMillis());
                        // Gionee <jingyn><2014-04-16> modify for CR01199894 end
                    } else {
                        Log.i(LOG_TAG, "--------------->no fast");

                        mPowerManager.goToSleep(SystemClock.uptimeMillis());
                    }
                }
            }
        }
    };

    private void registerProximitySensor() {
        SensorManager sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorMgr.registerListener(mSensorEventListener,
                sensorMgr.getDefaultSensor(Sensor.TYPE_PROXIMITY),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void unregisterProximitySensor() {
        if (mSensorEventListener != null) {
            Log.d(LOG_TAG, "unregisterProximitySensor()");
            SensorManager sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
            sensorMgr.unregisterListener(mSensorEventListener);
            mSensorEventListener = null;
        }
    }
    // proximity sensor end
    // Gionee <jiangxiao> <2014-01-08> modify for CR00989271 end

    // Gionee <jiangxiao> <2013-12-24> add for CR00990021 begin
    private static final String ACTION_FAKE_INCALL_STATE_CHANGED = "amigo.intent.navi.keyguard.MOCK_INCALL";

    private void notifyFakeInCallStateChanged(int state) {
        int callState = TelephonyManager.CALL_STATE_RINGING;
        if (state == CALL_PROCESS_VIEW) {
            callState = TelephonyManager.CALL_STATE_OFFHOOK;
        } else if (state == CALL_END_VIEW) {
            callState = TelephonyManager.CALL_STATE_IDLE;
        }
        Log.i(LOG_TAG, "notifyFakeInCallStateChanged() callState=" + callState);

        Bundle args = new Bundle();
        args.putInt(TelephonyManager.EXTRA_STATE, callState);

        Intent intent = new Intent(ACTION_FAKE_INCALL_STATE_CHANGED);
        intent.putExtras(args);
        this.sendBroadcast(intent);
    }
    // Gionee <jiangxiao> <2013-12-24> add for CR00990021 end

    private GnMultiWaveView.AnswerListener mAnswerListener = new GnMultiWaveView.AnswerListener() {
        
        @Override
        public void onText() {
            stopRingtone();
            finish();
        }
        
        @Override
        public void onDecline() {
            stopRingtone();
            finish();
        }
        
        @Override
        public void onAnswer(int videoState, Context context) {
            stopRingtone();
            onAnwserButtonClicked();
        }
    };
    
    private GnMultiWaveView.RippleListener mRippleListener = new GnMultiWaveView.RippleListener() {
        
        @Override
        public void stopRipple() {
            if (mRipple != null && mRipple.isRippleAnimationRunning()) {
                mRipple.stopRippleAnimation();
            }
        }
        
        @Override
        public void startRipple() {
            if (mRipple != null && !mRipple.isRippleAnimationRunning()) {
                mRipple.startRippleAnimation();
            }
        }
    };
}

