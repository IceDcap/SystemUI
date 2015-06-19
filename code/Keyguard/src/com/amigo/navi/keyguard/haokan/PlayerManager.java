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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.amigo.navi.keyguard.DebugLog;
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
    
    private PlayerButton mPlayerButton;
    
    private static final int NOTIFICATION_ID = 1005;
    
    
    private TelephonyManager mTelephonyManager;
    private AudioManager mAudioManager;
    private MediaPlayer mMediaPlayer;
    private Context mApplicationContext;
    
    private Music mCurrentMusic;
    private Music mPlayingMusic;
    
    private NotificationManager mNotificationManager;
    private Notification mNotification;
    
    private int mDuration;
    private static final boolean VOLUME_SLOWLY = true;
    private boolean isPausedByCalling = false;
    private boolean isPausedByAudiofocusLoss = false;
    private int mCurrentDuration = 0;
    
    private Timer mTimer = null; 
    private TimerTask mTimerTask = null;
    
    private static PlayerManager instance = null;
    
    private NotificationReceiver mNotificationReceiver;
    
    private PhoneStateReceiver mPhoneStateReceiver;

    private boolean isLocalMusic = false;
    
    
    
    public boolean isLocalMusic() {
        return isLocalMusic;
    }

    public void setLocalMusic(boolean isLocalMusic) {
        this.isLocalMusic = isLocalMusic;
    }

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
    
    
    public void player(boolean IsAvailable, boolean isLocal) {

        initMediaPlayer();
        
        if (mState != State.PAUSE) {
            
            UIController.getInstance().showMusicPlayer(getCurrentMusic());
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.reset();
            
            String dataSource = mCurrentMusic.getLocalPath();
 
            if (!isLocal && IsAvailable) {
                DebugLog.d(TAG, "is not Local music & download music");
                dataSource = mCurrentMusic.getPlayerUrl();
                new DownLoadJob(mApplicationContext, mCurrentMusic).start();
            } 
            DebugLog.d(TAG, "dataSource = " + dataSource);
            
            try {
                mMediaPlayer.setDataSource(dataSource);
                mMediaPlayer.prepareAsync();
                setState(State.PREPARE);
                setPlayingMusic(mCurrentMusic);
            } catch (Exception e) {
                Log.e(TAG, "player  Exception");
                e.printStackTrace();
            }
            
            HKAgent.onEvent(mApplicationContext, mCurrentMusic.getImgId(), mCurrentMusic.getTypeId(),  Event.PLAYER_MUSIC);
        }
    }
    
    private Bitmap thumbBitmap = null;
    private void createNotification() {
        
        if (mCurrentMusic == null) {
            return;
        }
        
        Intent intent = new Intent(NotificationReceiver.ACTION_MUSIC_CLOSE);
        PendingIntent pendingIntentClose = PendingIntent.getBroadcast(mApplicationContext, 0, intent, 0);

        Intent intent2 = new Intent(NotificationReceiver.ACTION_PLAYER_OR_PAUSE);
        PendingIntent pendingIntentPlayerOrPause = PendingIntent.getBroadcast(mApplicationContext, 0, intent2, 0);
        RemoteViews remoteViews = new RemoteViews(mApplicationContext.getPackageName(),
              R.layout.haokan_notification_layout);
        remoteViews.setOnClickPendingIntent(R.id.haokan_notification_close, pendingIntentClose);
        remoteViews.setOnClickPendingIntent(R.id.haokan_notification_player_or_pause, pendingIntentPlayerOrPause);
        
        final Notification.Builder builder = new Notification.Builder(mApplicationContext)
        .setSmallIcon(R.drawable.haokan_music_normal)
        .setWhen(0)
        .setShowWhen(false)
        .setOngoing(true)
        .setContent(remoteViews)
        .setAutoCancel(false)
        .setVisibility(Notification.VISIBILITY_SECRET);
        
        remoteViews.setTextViewText(R.id.haokan_main_layout_music, mCurrentMusic.getmMusicName());
        remoteViews.setTextViewText(R.id.haokan_main_layout_Artist, mCurrentMusic.getmArtist());
        thumbBitmap = Common.compBitmap(UIController.getInstance().getCurrentWallpaperBitmap(mApplicationContext, true));
        if (thumbBitmap != null) {
            remoteViews.setImageViewBitmap(R.id.haokan_notification_image, thumbBitmap);
        }
        mNotification = builder.build();
        
    }
    
 
    
    private void notifyNotification() {
        if (mNotification != null && mNotification.contentView != null) {
            mNotification.contentView.setImageViewResource(R.id.haokan_notification_player_or_pause,
                    State.PLAYER == mState ? R.drawable.haokan_notification_music_player
                            : R.drawable.haokan_notification_music_pause);
            mNotificationManager.notify(NOTIFICATION_ID, mNotification);
        }
    }
    
    private void cancelNotification() {
        DebugLog.d(TAG, "cancelNotification");
        mNotificationManager.cancel(NOTIFICATION_ID);
        BitmapUtil.recycleBitmap(thumbBitmap);
    }
    
    
    public void pause() {

        if (mMediaPlayer.isPlaying() && mState == State.PLAYER) {
            

            cancelTimeTask();
            mMediaPlayer.pause();
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
        
//        if (mCurrentMusic == null) {
//            DebugLog.d(TAG, "pauseOrPlayer  mCurrentMusic == null");
//            return;
//        }
//        DebugLog.d(TAG, mCurrentMusic.getmMusicName());
        
        if (mState == State.PAUSE) {
            DebugLog.d(TAG, "pauseOrPlayer  start");
            start();
            
        }else if (mState == State.PLAYER) {
            DebugLog.d(TAG, "pauseOrPlayer  pause");
            pause();
        }else if (mState == State.NULL){
        
            boolean IsAvailable = Common.getNetIsAvailable(mApplicationContext);
             
            setLocalMusic(mCurrentMusic.isLocal() && new File(mCurrentMusic.getLocalPath()).exists());
            
            if (isLocalMusic || IsAvailable) {
                player(IsAvailable, isLocalMusic);
            } else {
                UIController.getInstance().showToast(R.string.haokan_tip_check_net);
            }
             
        }
        isPausedByCalling = false;
    }
    
    
    
    private void start() {
        
        requestAudioFocus();
        mDuration = mMediaPlayer.getDuration();
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
                va.setDuration(800);
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
        setPlayingMusic(null);
        cancelTimeTask();
        mBufferingPercent = 0;
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            abandonAudioFocusIfNeed();
        }
        if (State.NULL != mState) {
            setState(State.NULL);
            cancelNotification();
        }
 
    }
    
    
    public void onScreenTurnedOff() {
        if (mMediaPlayer != null && mState == State.NULL) {
            DebugLog.d(TAG, "screen turned off & release mediaPlayer");
            stopAndRelease();
        }
    }
    
    
    private OnCompletionListener mCompletionListener = new OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            DebugLog.d(TAG, "onCompletion ");
            stopAndRelease();
            UIController.getInstance().hideMusicPlayer(true, isExistMusic());
        }
    };
    
    private int mBufferingPercent = 0;
    
    private OnBufferingUpdateListener mBufferingUpdateListener = new OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            mBufferingPercent = percent;
        }
    };
    
    private OnErrorListener mErrorListener = new OnErrorListener() {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
 
            Log.e(TAG, "MediaPlayer onError what = " + what + " extra = " + extra);
            
            if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN /*&& extra == -2147483648*/) {//-2147483648
                stopAndRelease();
                UIController.getInstance().hideMusicPlayer(true, isExistMusic());
            }
            
            return true;
        }
    };

    
    
    
    private OnPreparedListener mOnPreparedListener = new OnPreparedListener() {
        
        @Override
        public void onPrepared(MediaPlayer arg0) {
            createNotification();
            start();

        }
    };
    
 
    
    private void initTimeTask() {
        
        mTimer = new Timer();
        
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (mMediaPlayer != null){
//                    if (mMediaPlayer.isPlaying()) {
                        if (getState() == State.PLAYER) {
                        mCurrentDuration = mMediaPlayer.getCurrentPosition();
                        DebugLog.d(TAG, "mCurrentDuration  = " + mCurrentDuration);
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
                float Volume = (Float) animation.getAnimatedValue();
                mMediaPlayer.setVolume(Volume, Volume);
            }
        }
    };

    
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
        UIController.getInstance().hideMusicPlayer(false, isExistMusic());
        resetPlayerIcon();
    }
    
    private void resetPlayerIcon() {
        if (mCurrentMusic != null) {
            setState(State.NULL);
        } 
    }
    
    private OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
        
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    Log.d(TAG, "AUDIOFOCUS_LOSS");

                    isPausedByAudiofocusLoss = true;
                    pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");
                    isPausedByAudiofocusLoss = true;
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
        if (!isPausedByAudiofocusLoss) {

            int abandonAudioFocusCode = mAudioManager.abandonAudioFocus(mAudioFocusListener);

            Log.d(TAG,
                    "abandonAudioFocus : AUDIOFOCUS_REQUEST_"
                            + ((abandonAudioFocusCode == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) ? "GRANTED"
                                    : "FAILED"));

        }
        isPausedByAudiofocusLoss = false;
    }
    
    private class PhoneStateReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
             
            if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)
                    || mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_RINGING) {
                if (mState == State.PLAYER) {
                    
                    pause();
                    isPausedByCalling = true;
                }

            } else if (mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                if (mState == State.PAUSE && isPausedByCalling) {
                    start();
                }
                isPausedByCalling = false;
            }
        }
    }
    
    

    public State getState() {
        return mState;
    }

   
    public void netStateChange(boolean connect) {

        DebugLog.d(TAG, "netStateChange " + connect);
        if (!connect) {
            if (mState == State.PLAYER && !isLocalMusic() && mBufferingPercent != 100) {
                stopAndRelease();
                UIController.getInstance().hideMusicPlayer(true, isExistMusic());
            }
        }
    }

    public Music getPlayingMusic() {
        return mPlayingMusic;
    }

    public void setPlayingMusic(Music mPlayingMusic) {
        this.mPlayingMusic = mPlayingMusic;
    }

  
    private boolean isExistMusic() {
        return getmCurrentMusic() != null;

    }
    
}
