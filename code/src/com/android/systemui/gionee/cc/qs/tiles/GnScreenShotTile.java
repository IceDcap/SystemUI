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
import com.android.systemui.screenshot.GnSnapshotService;
import com.android.systemui.R;
import com.android.systemui.gionee.GnYouJu;

public class GnScreenShotTile extends GnQSTile<GnQSTile.BooleanState> {

    private static final Intent GNSCREEN_SHOT = new Intent("gn.intent.action.SELECT_SHOT");
    
    public GnScreenShotTile(Host host, String spec) {
        super(host, spec);
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
        Log.d(TAG, "handleClick");
    	GnYouJu.onEvent(mContext, "Amigo_SystemUI_CC", "GnScreenShotTile");
        mHost.collapsePanels();
        
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                GnSnapshotService.getService(mContext).takeScreenShot();
                mHost.startSettingsActivity(GNSCREEN_SHOT);
            }
        }, 500);
        
//        mHost.startSettingsActivity(GNSCREEN_SHOT, 500);
    }

    @Override
    protected void handleLongClick() {

    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.value = false;
        state.visible = true;
        state.label = mContext.getString(R.string.gn_qs_screen_shot);
        state.iconId = R.drawable.gn_ic_qs_screenshot;
        state.contentDescription = mContext.getString(R.string.gn_qs_screen_shot);
    }
    
}
