package com.amigo.navi.keyguard.haokan;


import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.amigo.navi.keyguard.haokan.analysis.Event;
import com.amigo.navi.keyguard.haokan.analysis.HKAgent;
import com.amigo.navi.keyguard.haokan.entity.Music;
import com.android.keyguard.R;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerManager {

    
    private String TAG = "haokan";
    
    public enum State{
        NULL, PAUSE, PLAYER, PREPARE
    }
    
    private State mState = State.NULL;
    
    private HKMainLayout mHkMainLayout;
    private PlayerButton mPlayerButton;
    
    private static final int NOTIFICATION_ID = 1005;
    
    
    private TelephonyManager mTelephonyManager;
    private AudioManager mAudioManager;
    private MediaPlayer mMediaPlayer;
    private Context mApplicationContext;
    
    private Music mCurrentMusic;
    
    private NotificationManager mNotificationManager;
    private Notification mNotification;
    
    private int mDuration;
    private static final boolean VOLUME_SLOWLY = true;
    private boolean  isKeepCalling = false;
    private boolean mAudiofocusLossPause = false;
    private int mCurrentDuration = 0;
    
    private Timer mTimer = null; 
    private TimerTask mTimerTask = null;
    
    private static PlayerManager instance = null;

    
    public static PlayerManager getInstance(){
        if(instance == null){
            instance = new PlayerManager();
        }
        return instance;
    }
    
    private PlayerManager() {
        
    }
    
    
    public Music getmCurrentMusic() {
        return mCurrentMusic;
    }

    public void setmCurrentMusic(Music mCurrentMusic) {
        this.mCurrentMusic = mCurrentMusic;
    }



    private NotificationReceiver mNotificationReceiver;
    private PhoneStateReceiver mPhoneStateReceiver;
    
    public void init(Context applicationContext) {
        
        this.mApplicationContext = applicationContext;
        
        mNotificationReceiver = new NotificationReceiver(applicationContext);
        IntentFilter filter = new IntentFilter();
        filter.addAction(NotificationReceiver.ACTION_MUSIC_CLOSE);
        
        filter.addAction(NotificationReceiver.ACTION_PLAYER_OR_PAUSE);
        applicationContext.registerReceiver(mNotificationReceiver, filter);
        
        
        mPhoneStateReceiver = new PhoneStateReceiver();
        IntentFilter filterPhoneState = new IntentFilter();
        filterPhoneState.addAction("android.intent.action.PHONE_STATE");
        filterPhoneState.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        
        applicationContext.registerReceiver(mPhoneStateReceiver, filterPhoneState);
        
        mNotificationManager = (NotificationManager) applicationContext.getSystemService("notification");

        mTelephonyManager = (TelephonyManager)applicationContext.getSystemService(Context.TELEPHONY_SERVICE);  
        
        mAudioManager = (AudioManager) applicationContext.getSystemService(Context.AUDIO_SERVICE);
        
        mAudioManager.registerAudioFocusListener(mAudioFocusListener);
        
    }
   
    public void closeHaokan() {
        mApplicationContext.unregisterReceiver(mNotificationReceiver);
        mApplicationContext.unregisterReceiver(mPhoneStateReceiver);
        stopAndRelease();
    }
    
    
    public void player() {

        initMediaPlayer();
        
        
        if (mState != State.PAUSE) {
            
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.reset();
            
            String dataSource = mCurrentMusic.getLocalPath();
 
            if (!mCurrentMusic.isLocal()) {
                Log.v(TAG, "is not Local music & download music");
                dataSource = mCurrentMusic.getPlayerUrl();
                new DownLoadJob(mApplicationContext, mCurrentMusic).start();
            }else {
                Log.v(TAG, "is Local music");
                if (!new File(dataSource).exists()) {
                    Log.v(TAG, "music file is not exists & download music");
                    dataSource = mCurrentMusic.getPlayerUrl();
                    new DownLoadJob(mApplicationContext, mCurrentMusic).start();
                }
            } 
            Log.v(TAG, "dataSource = " + dataSource);
            
            try {
                mMediaPlayer.setDataSource(dataSource);
                mMediaPlayer.prepare();
                mDuration = mMediaPlayer.getDuration();
                setState(State.PREPARE);
            } catch (IllegalArgumentException | SecurityException | IllegalStateException | IOException e) {
                e.printStackTrace();
            }
            
            HKAgent.onEvent(mApplicationContext, mCurrentMusic.getImgId(), mCurrentMusic.getTypeId(),  Event.PLAYER_MUSIC);
        }
    }
    
    
    private void createNotification() {
        
        if(mCurrentMusic != null){
            if (mNotification == null) {
                mNotification = new Notification();
                mNotification.icon = R.drawable.haokan_music_normal;
                mNotification.when = System.currentTimeMillis();
                mNotification.flags = Notification.FLAG_ONGOING_EVENT; 
            }
            
            mNotification.tickerText = mCurrentMusic.getDisplayName();
             
            if (mNotification.contentView == null) {
                
                Intent intent = new Intent(NotificationReceiver.ACTION_MUSIC_CLOSE);
                PendingIntent pendingIntentClose = PendingIntent.getBroadcast(mApplicationContext, 0, intent, 0);

                Intent intent2 = new Intent(NotificationReceiver.ACTION_PLAYER_OR_PAUSE);
                PendingIntent pendingIntentPlayerOrPause = PendingIntent.getBroadcast(mApplicationContext, 0, intent2, 0);
                
                RemoteViews mRemoteViews = new RemoteViews(mApplicationContext.getPackageName(),
                        R.layout.haokan_notification_layout);
                mRemoteViews.setOnClickPendingIntent(R.id.haokan_notification_close, pendingIntentClose);
                mRemoteViews.setOnClickPendingIntent(R.id.haokan_notification_player_or_pause, pendingIntentPlayerOrPause);
                mNotification.contentView = mRemoteViews;
            }
            
            mNotification.contentView.setTextViewText(R.id.haokan_main_layout_music, mCurrentMusic.getmMusicName());
            mNotification.contentView.setTextViewText(R.id.haokan_main_layout_Artist, mCurrentMusic.getmArtist());
            
            mNotification.contentView.setImageViewBitmap(R.id.haokan_notification_image,
                    Common.compBitmap(UIController.getInstance().getCurrentWallpaperBitmap()));
    
        }
        
    }
    
 
    
    private void notifyNotification() {
        mNotification.contentView.setImageViewResource(R.id.haokan_notification_player_or_pause,
                State.PLAYER == mState ? R.drawable.haokan_notification_music_player
                        : R.drawable.haokan_notification_music_pause);
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }
    
    private void cancelNotification() {
        Log.v(TAG, "cancelNotification");
        try {
            mNotificationManager.cancel(NOTIFICATION_ID);
        } catch (Exception e) {
        }
    }
    
    public void next() {
       
        if (State.NULL != mState) {
            cancelTimeTask();
            cancelNotification();
            setState(State.NULL);
        }
        
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
        
//        if (Integer.parseInt(mCurrentMusic.getMusicId()) == mListMusics.size() - 1) {
//            mCurrentMusic = mListMusics.get(0);
//        }else {
//            mCurrentMusic = mListMusics.get(Integer.parseInt(mCurrentMusic.getMusicId()) + 1);
//        }
        
        
//        getmHkMainLayout().setCurrentMusic(false, null);
    }
    
    public void prev() {
        
        if (State.NULL != mState) {
            cancelTimeTask();
            cancelNotification();
            setState(State.NULL);
        }
        
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }

        
//        if (Integer.parseInt(mCurrentMusic.getMusicId()) == 0) {
//            mCurrentMusic = mListMusics.get(mListMusics.size() - 1);
//        }else {
//            mCurrentMusic = mListMusics.get(Integer.parseInt(mCurrentMusic.getMusicId()) - 1);
//        }
        
//        getmHkMainLayout().setCurrentMusic(false, null);
    }
    
    public void pause() {

        if (mMediaPlayer.isPlaying() && mState == State.PLAYER) {
            

            mMediaPlayer.pause();
            cancelTimeTask();
            abandonAudioFocusIfNeed();
            setState(State.PAUSE);
            notifyNotification();
            
        }
        
        
    }

    private void cancelTimeTask() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
        }
    }
    
    public void pauseOrPlayer() {
        
        if (mCurrentMusic == null) {
            Log.v(TAG, "pauseOrPlayer  mCurrentMusic == null");
            return;
        }
        Log.v(TAG, mCurrentMusic.getmMusicName());
        
        if (mState == State.PAUSE || mCurrentMusic.getmState() == State.PAUSE) {
            Log.v(TAG, "pauseOrPlayer  start");
            start();
            
        }else if (mState == State.PLAYER) {
            Log.v(TAG, "pauseOrPlayer  pause");
            pause();
        }else {
            if (mCurrentMusic.isLocal() || Common.getNetIsAvailable(mApplicationContext)) {
                Log.v(TAG, "pauseOrPlayer  player");
                player();
            }
        }
        isKeepCalling = false;
    }
    
    
    
    private void start() {
        requestAudioFocus();
//        if (VOLUME_SLOWLY) {
//            mMediaPlayer.setVolume(0f,0f);
//            ValueAnimator va = new ValueAnimator();
//            va.setFloatValues(0f, 1f);
//            va.setDuration(2000);
//            va.addUpdateListener(mAnimatorUpdateListener);
//            va.start();
//        }
        if (mState != State.PLAYER) {
            initTimeTask();
        }
        mMediaPlayer.start();
        setState(State.PLAYER);
        notifyNotification();

    }
    
     
   
    private void initMediaPlayer() {

        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setWakeMode(mApplicationContext,PowerManager.PARTIAL_WAKE_LOCK);
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
    }
    
    /**
     * 
     * @param volumeSlowly
     */
    public void stopMusicPlayer(boolean volumeSlowly) {
        
        if (volumeSlowly && State.PLAYER == mState) {
             
                mMediaPlayer.setVolume(0f,0f);
                ValueAnimator va = new ValueAnimator();
                va.setFloatValues(1f, 0f);
                va.setDuration(2000);
                va.addUpdateListener(mAnimatorUpdateListener);
                va.addListener(new AnimatorListener() {
                    
                    @Override
                    public void onAnimationStart(Animator arg0) {
                        
                    }
                    
                    @Override
                    public void onAnimationRepeat(Animator arg0) {
                        
                    }
                    
                    @Override
                    public void onAnimationEnd(Animator arg0) {
                        stopAndRelease();
                    }
                    
                    @Override
                    public void onAnimationCancel(Animator arg0) {
                        
                    }
                });
                
                va.start();
             
        }else {
            stopAndRelease();
        }
 

    }
 
    public void stopAndRelease() {
        

        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            abandonAudioFocusIfNeed();
        }
        if (State.NULL != mState) {
            setState(State.NULL);
            cancelTimeTask();
            cancelNotification();
        }
        
        
//        getmHkMainLayout().hideMusicPlayer(false);
    }
    
    
    public void onScreenTurnedOff() {
        if (mMediaPlayer != null && mState == State.NULL) {
            Log.v(TAG, "screen turned off & release mediaPlayer");
            stopAndRelease();
        }
    }
    
    
    private OnCompletionListener mCompletionListener = new OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            stopAndRelease();
            getmHkMainLayout().hideMusicPlayer(true);
        }
    };
    
    private OnBufferingUpdateListener mBufferingUpdateListener = new OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            
        }
    };
    
    private OnErrorListener mErrorListener = new OnErrorListener() {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Log.v(TAG, "onError  what = " + what);
 
            if (Common.getNetIsAvailable(mApplicationContext)) {
                Log.e(TAG, "onError  网络断开");
            }
            if(what == MediaPlayer.MEDIA_ERROR_UNKNOWN){
                return true;
            }
            return true;
        }
    };

    
    
    
    private OnPreparedListener mOnPreparedListener = new OnPreparedListener() {
        
        @Override
        public void onPrepared(MediaPlayer arg0) {
            createNotification();
            start();
            getmHkMainLayout().showMusicPlayer(getCurrentMusic());
            
        }
    };
    
 
    
    private void initTimeTask() {
        
        mTimer = new Timer();
        
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (mMediaPlayer != null){
                    if (mMediaPlayer.isPlaying()) {
                        mCurrentDuration = mMediaPlayer.getCurrentPosition();
                        mHandler.sendEmptyMessage(0);
                    }
                }
            }
        };
        
        mTimer.schedule(mTimerTask, 0, 1000);
    }
         
    
    private Handler mHandler = new Handler() {
        
        public void handleMessage(android.os.Message msg) {
            final int position = mCurrentDuration;
//            if (mMediaPlayer != null) {
//                position = mMediaPlayer.getCurrentPosition();
//            }
            int duration = mDuration;
            if (duration > 0) {
                mPlayerButton.setProgress(position / (float)duration);
            }
        };
    };
    
    
    AnimatorUpdateListener mAnimatorUpdateListener = new AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            if (mMediaPlayer != null) {
                float Volume = (float) animation.getAnimatedValue();
                mMediaPlayer.setVolume(Volume, Volume);
            }
        }
    };

    public HKMainLayout getmHkMainLayout() {
        return mHkMainLayout;
    }

    public void setmHkMainLayout(HKMainLayout mHkMainLayout) {
        this.mHkMainLayout = mHkMainLayout;
    }
    
    public PlayerButton getPlayerButton() {
        return mPlayerButton;
    }

    public void setPlayerButton(PlayerButton playerButton) {
        this.mPlayerButton = playerButton;
    }
    
    public Music getCurrentMusic() {
        return mCurrentMusic;
    }
    
    private void setState(State state) {
        this.mState = state;
        if (mCurrentMusic != null) {
            mCurrentMusic.setmState(state);
        }
        mPlayerButton.setState(state);
    }
    
    public void closeNotificationAndMusic() {
        cancelNotification();
        stopAndRelease();
        getmHkMainLayout().hideMusicPlayer(false);
    }
    
    
    private OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
        
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    Log.d(TAG, "AUDIOFOCUS_LOSS");

                    mAudiofocusLossPause = true;
                    pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");
                    mAudiofocusLossPause = true;
                    //闹钟
                    pause();
                    
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                    
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    Log.d(TAG, "AUDIOFOCUS_GAIN");
                    
                    start();
                    break;
                default:
                    Log.d(TAG, "Unknown audio focus change code");
                    break;
            }
        }
    };
    
    
    private void requestAudioFocus() {
        int audiofocusRequestCode = mAudioManager.requestAudioFocus(mAudioFocusListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

            Log.d(TAG,
                    "requestAudioFocus : AUDIOFOCUS_REQUEST_"
                            + ((audiofocusRequestCode == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) ? "GRANTED"
                                    : "FAILED"));
    }

    
    private void abandonAudioFocusIfNeed() {
        if (!mAudiofocusLossPause) {

            int abandonAudioFocusCode = mAudioManager.abandonAudioFocus(mAudioFocusListener);

            Log.d(TAG,
                    "abandonAudioFocus : AUDIOFOCUS_REQUEST_"
                            + ((abandonAudioFocusCode == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) ? "GRANTED"
                                    : "FAILED"));

        }
        mAudiofocusLossPause = false;
    }
    
    private class PhoneStateReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
             
            if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)
                    || mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_RINGING) {
                if (mState == State.PLAYER) {
                    pause();
                }
                isKeepCalling = true;

            } else if (mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                if (mState == State.PAUSE && isKeepCalling) {
                    start();
                    isKeepCalling = false;
                }
            }
        }
    }

    public State getState() {
        return mState;
    }

   
    
    
    
}
