/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
package com.android.systemui.gionee.cc.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.provider.Settings.Global;
import android.util.Log;

import com.android.systemui.R;
import com.android.systemui.gionee.cc.qs.GnQSTile;
import com.android.systemui.qs.GlobalSetting;
import com.android.systemui.gionee.GnYouJu;

/** Quick settings tile: Airplane mode **/
public class GnAirplaneModeTile extends GnQSTile<GnQSTile.BooleanState> {
    private final GlobalSetting mSetting;

    private boolean mListening;

    public GnAirplaneModeTile(Host host) {
        super(host);

        mSetting = new GlobalSetting(mContext, mHandler, Global.AIRPLANE_MODE_ON) {
            @Override
            protected void handleValueChanged(int value) {
                Log.d(TAG, "GlobalSetting.handleValueChanged " + value);
                handleRefreshState(value);
            }
        };
    }

    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void handleClick() {
        Log.d(TAG, "mState.value = " + mState.value);
        setEnabled(!mState.value);
        GnYouJu.onEvent(mContext, "Amigo_SystemUI_CC", "GnAirplaneModeTile");
    }

    @Override
    protected void handleLongClick() {

    }

    private void setEnabled(boolean enabled) {
        final ConnectivityManager mgr =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        mgr.setAirplaneMode(enabled);
   
        mHandler.postDelayed(new Runnable() {
            
            @Override
            public void run() {
                handleRefreshState(null);
            }
            
        }, 3000);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        Log.d(TAG, "handleUpdateState arg = " + arg);
        
        final int value = arg instanceof Integer ? (Integer)arg : mSetting.getValue();
        final boolean airplaneMode = value != 0;
        Log.d(TAG, "handleUpdateState " + airplaneMode);
        
        state.value = airplaneMode;
        state.visible = true;
        state.label = mContext.getString(R.string.quick_settings_airplane_mode_label);
        if (airplaneMode) {
            state.iconId =  R.drawable.gn_ic_qs_airplane_on;
            state.contentDescription =  mContext.getString(
                    R.string.accessibility_quick_settings_airplane_on);
        } else {
            state.iconId = R.drawable.gn_ic_qs_airplane_off;
            state.contentDescription =  mContext.getString(
                    R.string.accessibility_quick_settings_airplane_off);
        }
    }

    @Override
    protected String composeChangeAnnouncement() {
        if (mState.value) {
            return mContext.getString(R.string.accessibility_quick_settings_airplane_changed_on);
        } else {
            return mContext.getString(R.string.accessibility_quick_settings_airplane_changed_off);
        }
    }

    public void setListening(boolean listening) {
        if (mListening == listening) return;
        mListening = listening;
        if (listening) {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            mContext.registerReceiver(mReceiver, filter);
        } else {
            mContext.unregisterReceiver(mReceiver);
        }
        mSetting.setListening(listening);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(intent.getAction())) {
                boolean state = intent.getBooleanExtra("state", false);
                Log.d(TAG, "onReceive state = " + state);
                if (state) {                    
                    refreshState(1);
                } else {
                    refreshState(0);
                }
            }
        }
    };
}
