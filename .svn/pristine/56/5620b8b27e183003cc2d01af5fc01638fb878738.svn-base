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
import android.os.Handler;

import com.android.systemui.gionee.cc.qs.GnQSTile;
import com.android.systemui.R;
import com.android.systemui.gionee.GnYouJu;

public class GnVibrateTile extends GnQSTile<GnQSTile.BooleanState> {
    
    private static final String AMIGO_SETTING_VIBRATE = "amigo_vibration_switch";
    
    public StatusBarManager mService;
    
    public GnVibrateTile(Host host, String spec) {
        super(host, spec);
        mService = (StatusBarManager)mContext.getSystemService(Context.STATUS_BAR_SERVICE);
    }

    @Override
    public void setListening(boolean listening) {
        if (listening) {
            mContext.getContentResolver().registerContentObserver(AmigoSettings.getUriFor(AMIGO_SETTING_VIBRATE), 
                    true, mVibrateChangeObserver);
        } else {
            mContext.getContentResolver().unregisterContentObserver(mVibrateChangeObserver);
        }
    }

    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    protected void handleClick() {
    	GnYouJu.onEvent(mContext, "Amigo_SystemUI_CC", "GnVibrateTile");
        boolean value = mState.value;
        AmigoSettings.putInt(mContext.getContentResolver(), AMIGO_SETTING_VIBRATE, value ? 0 : 1);
    }

    @Override
    protected void handleLongClick() {

    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.visible = true;
        state.label = mContext.getString(R.string.gn_qs_vibrate);
        state.contentDescription = mContext.getString(R.string.gn_qs_vibrate);

        boolean vibrate = AmigoSettings.getInt(mContext.getContentResolver(), AMIGO_SETTING_VIBRATE, 0) == 1;
        state.value = vibrate;
        if (vibrate) {
            state.iconId = R.drawable.gn_ic_qs_vibrate_on;
        } else {
            state.iconId = R.drawable.gn_ic_qs_vibrate_off;
        }

        GnTileHelper.updateVolumeIcon(mService, mContext);
    }
    
    private ContentObserver mVibrateChangeObserver = new ContentObserver(new Handler()) {

        @Override
        public void onChange(boolean selfChange) {
            refreshState();
        }
    };
}