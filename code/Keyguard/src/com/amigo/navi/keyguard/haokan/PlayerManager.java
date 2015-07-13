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
import com.amigo.navi.keyguard.network.NetworkRemind;
import com.amigo.navi.keyguard.network.NetworkRemind.ClickContinueCallback;
import com.android.keyguard.R;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerManager  implements ClickContinueCallback{

    
    private String TAG = "PlayerManager";
    
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
    private int mCurrentDuration = 0;
    
    private Timer mTimer = null; 
    private TimerTask mTimerTask = null;
    
    private static PlayerManager instance = null;
    
    private NotificationReceiver mNotificationReceiver;
    
    private PhoneStateReceiver mPhoneStateReceiver;

    private boolean isLocalMusic = false;
    
    private Bitmap thumbBitmap = null;
    
    private UIController uiController;
    
    private int mBufferingPercent = 0;
    private PlayerLayout mPlayerLayout;
    
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
        
        uiController = UIController.getInstance();
        
    }
   
    
    public void player(Music music, boolean available, boolean local) {

        initMediaPlayer();
        getPlayerLayout().showMusicName(true, music);
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        mMediaPlayer.reset();

        String dataSource = music.getLocalPath();

        if (!local && available) {
            DebugLog.d(TAG, "is not Local music & download music");
            dataSource = music.getPlayerUrl();
            new DownLoadJob(mApplicationContext, music).start();
        }
        DebugLog.d(TAG, "musicName = " + music.getmMusicName() + "dataSource = " + dataSource);

        if (requestAudioFocus()) {
            
            setPlayingMusic(music);
            
            try {
                mMediaPlayer.setDataSource(dataSource);
                mMediaPlayer.prepareAsync();
                setState(State.PREPARE);
                
            } catch (Exception e) {
                Log.e(TAG, "player  Exception");
                e.printStackTrace();
            }
            
        }
        
        HKAgent.onEvent(mApplicationContext, music.getImgId(), music.getTypeId(),
                Event.PLAYER_MUSIC);
        
    }
    
    
    private void createNotification() {
        
        Music currentMusic = uiController.getmCurrentWallpaper().getMusic();
        if (currentMusic == null) {
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
        
        remoteViews.setTextViewText(R.id.haokan_main_layout_music, currentMusic.getmMusicName());
        remoteViews.setTextViewText(R.id.haokan_main_layout_Artist, currentMusic.getmArtist());
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
        
        cancelTimeTask();
        mMediaPlayer.pause();
        abandonAudioFocusIfNeed();
        setState(State.PAUSE);
        notifyNotification();
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
    
    public void pauseOrPlayer(Music music) {
 
        if (music == null) return;
        State state = music.getmState();
        
        DebugLog.d(TAG, "state = " + state);
        if (state == State.PAUSE) {
            DebugLog.d(TAG, "pauseOrPlayer  start");
            
            if (musicIsPlaying(music)) {
                start();
            }else {
                player(music);
            }
            
        }else if (state == State.PLAYER) {
            DebugLog.d(TAG, "pauseOrPlayer  pause");
            pause();
        }else if (state == State.NULL){
            player(music);
        }
        isPausedByCalling = false;
    }
    
    
    
    public void pauseOrPlayer() {
        if (getPlayingMusic() != null) {
            pauseOrPlayer(getPlayingMusic());
        }
    }
    
    
    private void player(Music music) {
        
        boolean IsAvailable = Common.getNetIsAvailable(mApplicationContext);
        
        setLocalMusic(music.isLocal() && new File(music.getLocalPath()).exists());
        
        if (isLocalMusic() ) {
            
            if (getPlayingMusic() != null) {
                getPlayingMusic().setmState(State.NULL);
            }
            player(music,IsAvailable, isLocalMusic());
        } else if(IsAvailable){
        	if(NetworkRemind.getInstance(mApplicationContext).needShowDialog()){
        		NetworkRemind.getInstance(mApplicationContext).registeContinueCallback(this);
        		NetworkRemind.getInstance(mApplicationContext).alertDialog();
        	}else{
        		 if (getPlayingMusic() != null) {
                     getPlayingMusic().setmState(State.NULL);
                 }
                 player(music,IsAvailable, isLocalMusic());
        	}
        }else {
            uiController.showToast(R.string.haokan_tip_check_net);
        }
    }
    
    
    
    private void start() {
        
        requestAudioFocus();
        mDuration = mMediaPlayer.getDuration();
        if (getState() != State.PLAYER) {
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
    
    public void stopAndRelease() {
        cancelTimeTask();

        abandonAudioFocus();
        setState(State.NULL);
        cancelNotification();
        getPlayingMusic().setProgress(0);
        setPlayingMusic(null);

        mBufferingPercent = 0;
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
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
            getPlayerLayout().hideMusicName(true, musicIsExist());
        }
    };
    
    
    
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
                getPlayerLayout().hideMusicName(true, musicIsExist());
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
                if (mMediaPlayer != null && getState() == State.PLAYER) {
                    mCurrentDuration = mMediaPlayer.getCurrentPosition();
                    mHandler.sendEmptyMessage(0);
                }
            }
        };
        
        mTimer.schedule(mTimerTask, 0, 1000);
    }
         
    
    private Handler mHandler = new Handler() {

        public void handleMessage(android.os.Message msg) {
            if (getState() == State.PLAYER) {
                if (mDuration > 0) {
                    float progress = mCurrentDuration / (float) mDuration;

                    if (currentMusicIsPlaying()) {
                        mPlayerButton.setProgress(progress);
                    }
                    mPlayingMusic.setProgress(progress);
                }
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
    
    private void setState(State state) {
        this.mState = state;
        if (mPlayingMusic != null) {
            mPlayingMusic.setmState(state);
        }
        if (currentMusicIsPlaying()) {
            mPlayerButton.setState(state);
        }
    }
    
    public void closeNotificationAndMusic() {
        cancelNotification();
        stopAndRelease();
        getPlayerLayout().hideMusicName(false, musicIsExist());
        resetPlayerIcon();
    }
    
    private void resetPlayerIcon() {
        mPlayerButton.setState(State.NULL);
    }
    
    
    private boolean pausedByAudiofocusLoss = false;
    private OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
        
        
        public void onAudioFocusChange(int focusChange) {
            
            
            
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                	DebugLog.d(TAG, "AUDIOFOCUS_LOSS");

                    if (getState() == State.PLAYER) {
                        pausedByAudiofocusLoss = true;
                        pause();
                    }
                    
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                	DebugLog.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");
                	 if (getState() == State.PLAYER) {
                	     pausedByAudiofocusLoss = true;
                         pause();
                     }
                    
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                	DebugLog.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                	
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                	DebugLog.d(TAG, "AUDIOFOCUS_GAIN  isPausedByAudiofocusLoss = " + pausedByAudiofocusLoss);
                	if (pausedByAudiofocusLoss) {
                	    start();
                	    pausedByAudiofocusLoss = false;
                    }
                    break;
                default:
                	DebugLog.d(TAG, "Unknown audio focus change code");
                    break;
            }
        }
    };
    
    
    private boolean requestAudioFocus() {
        int audiofocusRequestCode = mAudioManager.requestAudioFocus(mAudioFocusListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        boolean focus = audiofocusRequestCode == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        DebugLog.d(TAG, "requestAudioFocus " + (focus ? "AUDIOFOCUS_REQUEST_GRANTED" : "AUDIOFOCUS_REQUEST_FAILED"));
        return focus;
    }

    private int abandonAudioFocus() {
        return mAudioManager.abandonAudioFocus(mAudioFocusListener);
    }
    
    private void abandonAudioFocusIfNeed() {
        
        if (!pausedByAudiofocusLoss) {
            abandonAudioFocus();
        }
        
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
                    mHandler.postDelayed(new Runnable() {
                        
                        @Override
                        public void run() {
                            start();
                        }
                    }, 2000);
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
            if (getState() == State.PLAYER && !isLocalMusic() && mBufferingPercent != 100) {
                stopAndRelease();
                getPlayerLayout().hideMusicName(true, musicIsExist());
            }
        }
    }

    public Music getPlayingMusic() {
        return mPlayingMusic;
    }

    public void setPlayingMusic(Music mPlayingMusic) {
        this.mPlayingMusic = mPlayingMusic;
    }

  
    private boolean musicIsExist;
    
   
    
    public boolean musicIsExist() {
        return musicIsExist;
    }

    public void setMusicIsExist(boolean musicIsExist) {
        this.musicIsExist = musicIsExist;
    }

    
    
    
    public void changeCurrentMusic(Music music) {
        setCurrentMusic(music);
        boolean isExistMusic = music != null;
        setMusicIsExist(isExistMusic);
        if (!isExistMusic) {
            getPlayerLayout().hideMusicName(false, false);
            mPlayerButton.setState(State.NULL);
        }else {
            
            mPlayerButton.setState(music);
            
            if (musicIsPlaying(music)) {
                mPlayerButton.setState(mPlayingMusic);
                getPlayerLayout().showMusicName(false, music);
            }else {
                getPlayerLayout().hideMusicName(false, true);
            }
            
//            if (music == getPlayingMusic()) {
//                getPlayerLayout().showMusicName(false, music);
//            } else {
//                if (getPlayingMusic() != null) {
//                    if (getPlayingMusic().getImgId() == music.getImgId()) {
//                        mPlayerButton.setState(mPlayingMusic);
//                        getPlayerLayout().showMusicName(false, music);
//                    }else {
//                        getPlayerLayout().hideMusicName(false, true);
//                    }
//                }else {
//                    getPlayerLayout().hideMusicName(false, true);
//                }
//            }
            
        }
    }
    
    

    public Music getCurrentMusic() {
        return mCurrentMusic;
    }

    public void setCurrentMusic(Music mCurrentMusic) {
        this.mCurrentMusic = mCurrentMusic;
    }

    public PlayerLayout getPlayerLayout() {
        return mPlayerLayout;
    }

    public void setPlayerLayout(PlayerLayout mPlayerLayout) {
        this.mPlayerLayout = mPlayerLayout;
    }
    
    private boolean musicIsPlaying(Music music) {
        if (getPlayingMusic() != null && music != null) {
            return getPlayingMusic().getImgId() == music.getImgId();
        }
        return false;
    }
    
    private boolean currentMusicIsPlaying() {
        return musicIsPlaying(getCurrentMusic());
    }

	@Override
	public void clickContinue() {
		if (getPlayingMusic() != null) {
            getPlayingMusic().setmState(State.NULL);
        }
        player(getCurrentMusic(),true, isLocalMusic());
		
	}
    
    
}
