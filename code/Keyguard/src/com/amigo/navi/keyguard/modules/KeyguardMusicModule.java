package com.amigo.navi.keyguard.modules;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.amigo.navi.keyguard.DebugLog;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.R ;
import com.google.android.collect.Lists;

public class KeyguardMusicModule extends KeyguardModuleBase {
    private static final String LOG_TAG = "KeyguardMusicModule";
    
//    private final static String MUSIC_PKG = "com.android.music";
//    private final static String MUSIC_SERVICE = "com.android.music.MediaPlaybackService";
    
    private static final String ACTION_MUSIC_PLAY_STATE_CHANGED = "com.android.music.playstatechanged";
    private static final String INTENT_MUSIC_ISPLAY_STATE_KEY = "playing";
    private static final long DELAY_CEHCK_MUSIC_STATE = 500;
    
//    private IMediaPlaybackService mMusicService;
    private boolean mIsBindService = false;
    private boolean isPlaying = false;
    
    private static KeyguardMusicModule sInstance = null;
    
    public static KeyguardMusicModule getInstance(Context context, KeyguardUpdateMonitor updateMonitor) {
    	if(sInstance == null) {
    		sInstance = new KeyguardMusicModule(context, updateMonitor);
    	}
    	
    	return sInstance;
    }
    
    public static KeyguardMusicModule getInstance() {
    	if(sInstance == null) {
    		throw new RuntimeException("KeyguardMusicModule should not be null!");
    	}
    	
    	return sInstance;
    }
    
    protected void initModule() {
        mReceiver = new BroadcastReceiver() {
    		@Override
    		public void onReceive(Context context, Intent intent) {
    			if(intent.getAction().equals(ACTION_MUSIC_PLAY_STATE_CHANGED)) {
    				isPlaying = intent.getBooleanExtra(INTENT_MUSIC_ISPLAY_STATE_KEY,false);
//    				mUpdateMonitor.notifyMusicPlayStateChanged(DELAY_CEHCK_MUSIC_STATE);
    				if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "onReceive ACTION_MUSIC_PLAY_STATE_CHANGED,isPlaying:"+isPlaying);
    			}
    		}
        };
        
        mFilter = new IntentFilter();
        mFilter.addAction(ACTION_MUSIC_PLAY_STATE_CHANGED);
    }
    
    private KeyguardMusicModule(Context context, KeyguardUpdateMonitor updateMonitor) {
    	super(context, updateMonitor);
    	mCallbacks = Lists.newArrayList();
    }
    
    public interface MusicCallback {
    	void play(boolean playing);
    }

    
    

    public boolean isMusicPlayingForSkylight(){
        return isPlaying;
    }
    
    public void updateMusicPlayingStateBySkylignt(boolean isPlaying){
        this.isPlaying=isPlaying;
    }
    
    public boolean isMusicPlaying() {
    	if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "isMusicPlaying--isPlaying:"+isPlaying);
        return isPlaying;
    }
    private void notifyMusicCallback(boolean playing) {
    	if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "notifyMusicCallback(), playing=" + playing);
    	for(MusicCallback cb : mCallbacks) {
    		cb.play(playing);
    	}
    }
    
    public void handleMusicServiceConnected() {
    	if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "handleMusicServiceConnected()");
    	notifyMusicCallback(isMusicPlaying());
    	handleMusicServiceDisconnected();
    }
    
    public void handleMusicServiceDisconnected() {
    	if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "handleMusicServiceDisconnected()");
    	if(mIsBindService) {
    		mIsBindService = false;
    	}
    }
    
    public void handleMusicPlayStateChanged() {
    	if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "handleMusicPlayStateChanged()");
		notifyMusicCallback(isMusicPlaying());
    }
    
    private ArrayList<MusicCallback> mCallbacks = Lists.newArrayList();
    
    public void registerCallback(MusicCallback cb) {
    	if(!mCallbacks.contains(cb)) {
        	mCallbacks.add(cb);
    	}
    }
    
    public void unregisterCallback(MusicCallback cb) {
    	mCallbacks.remove(cb);
    }
}
