/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
package com.android.systemui.gionee.cc.qs.tiles;

import android.content.Intent;
import android.provider.Settings;

import com.android.systemui.gionee.cc.qs.GnQSTile;
import com.android.systemui.R;
import com.android.systemui.gionee.GnYouJu;

public class GnSettingTile extends GnQSTile<GnQSTile.BooleanState> {
    private static final Intent SETTINGS = new Intent(Settings.ACTION_SETTINGS);
    
    public GnSettingTile(Host host) {
        super(host);
    }

    @Override
    public void setListening(boolean listening) {
        
    }

    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    protected void handleClick() {
    	GnYouJu.onEvent(mContext, "Amigo_SystemUI_CC", "GnSettingTile");
        mHost.startSettingsActivity(SETTINGS);
    }

    @Override
    protected void handleLongClick() {

    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.value = true;
        state.visible = true;
        state.label = "setting";
        state.iconId = R.drawable.gn_ic_qs_setting;
        state.contentDescription = "setting";
    }
    
}