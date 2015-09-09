/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
package com.android.systemui.gionee.cc.qs.tiles;


import android.content.Intent;
import android.util.Log;

import com.android.systemui.gionee.cc.qs.GnQSTile;
import com.android.systemui.R;
import com.android.systemui.gionee.GnYouJu;

public class GnPowerTile extends GnQSTile<GnQSTile.BooleanState> {
    
    private static final String TAG = "GnLauncherTile";

    private static boolean mVisible = true;
    
    private boolean mListening;
    
    public GnPowerTile(Host host, String spec) {
        super(host, spec);
    }

    @Override
    public void setListening(boolean listening) {
        if (mListening == listening) return;
        mListening = listening;
        if (mListening) {
            
        } else {
           
        }
    }

    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    protected void handleClick() {
    	GnYouJu.onEvent(mContext, "Amigo_SystemUI_CC", "GnPowerTile");
        Intent intent = new Intent("gionee.intent.action.SUPERMODE_OPEN");
        mHost.startSettingsActivity(intent);
    }

    @Override
    protected void handleLongClick() {

    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.value = false;
        state.visible = mVisible;
        state.label = mContext.getString(R.string.gn_qs_power);
        state.iconId = R.drawable.gn_ic_qs_power;
        state.contentDescription = mContext.getString(R.string.gn_qs_power);
    }
    
}