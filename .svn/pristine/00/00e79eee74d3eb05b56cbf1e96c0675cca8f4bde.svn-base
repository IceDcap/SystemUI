/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
package com.android.systemui.gionee.cc.qs.tiles;

import amigo.provider.AmigoSettings;
import android.app.StatusBarManager;
import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.util.Log;

import com.android.systemui.gionee.cc.qs.GnQSTile;
import com.android.systemui.R;
import com.android.systemui.gionee.GnYouJu;

public class GnVoiceTile extends GnQSTile<GnQSTile.BooleanState> {
    
    private static final String AMIGO_SETTING_VOICE = "amigo_silent_switch";
    
    public StatusBarManager mService;

    public GnVoiceTile(Host host, String spec) {
        super(host, spec);
        mService = (StatusBarManager)mContext.getSystemService(Context.STATUS_BAR_SERVICE);
    }

    @Override
    public void setListening(boolean listening) {
        if (listening) {
            mContext.getContentResolver().registerContentObserver(AmigoSettings.getUriFor(AMIGO_SETTING_VOICE), 
                    true, mVoiceChangeObserver);
        } else {
            mContext.getContentResolver().unregisterContentObserver(mVoiceChangeObserver);
        }
    }

    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    protected void handleClick() {
    	GnYouJu.onEvent(mContext, "Amigo_SystemUI_CC", "GnVoiceTile");
        boolean value = AmigoSettings.getInt(mContext.getContentResolver(), AMIGO_SETTING_VOICE, 0) == 0;
        Log.d(TAG, "handleClick  value = " + value);
        AmigoSettings.putInt(mContext.getContentResolver(), AMIGO_SETTING_VOICE, value ? 1 : 0);
        
        int volumeMusic = 0;
        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if (value) {
            volumeMusic = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            AmigoSettings.putInt(mContext.getContentResolver(), AmigoSettings.VOLUME_MUSIC,
                    volumeMusic);
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI_WARNINGS);
        } else {
            volumeMusic = AmigoSettings.getInt(mContext.getContentResolver(),
                    AmigoSettings.VOLUME_MUSIC, 12);
            am.setStreamVolume(AudioManager.STREAM_MUSIC, volumeMusic,
                    AudioManager.FLAG_SHOW_UI_WARNINGS);
        }
    }

    @Override
    protected void handleLongClick() {

    }
    
    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.visible = true;
        state.label = mContext.getString(R.string.gn_qs_voice);
        state.contentDescription = mContext.getString(R.string.gn_qs_voice);

        boolean voice = AmigoSettings.getInt(mContext.getContentResolver(), AMIGO_SETTING_VOICE, 0) == 0;
        state.value = voice;
        if (voice) {
            state.iconId = R.drawable.gn_ic_qs_voice_on;
        } else {
            state.iconId = R.drawable.gn_ic_qs_voice_off;
        }

        GnTileHelper.updateVolumeIcon(mService, mContext);     
    }
    
    private ContentObserver mVoiceChangeObserver = new ContentObserver(new Handler()) {

        @Override
        public void onChange(boolean selfChange) {
            Log.d(TAG, "mVoiceChangeObserver onChange");
            refreshState();
        }
    };
}