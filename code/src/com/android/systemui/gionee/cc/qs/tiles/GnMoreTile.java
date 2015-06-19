/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
package com.android.systemui.gionee.cc.qs.tiles;


import android.util.Log;

import com.android.systemui.gionee.cc.qs.GnQSTile;
import com.android.systemui.R;
import com.android.systemui.gionee.GnYouJu;

public class GnMoreTile extends GnQSTile<GnQSTile.BooleanState> {
    
    private static final String TAG = "GnMoreTile";
    
    public GnMoreTile(Host host, String spec) {
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
        GnYouJu.onEvent(mContext, "Amigo_SystemUI_CC", "GnMoreTile");
        Log.d(TAG, "handleClick");
        mHost.openMoreView();
    }

    @Override
    protected void handleLongClick() {

    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        if (arg == null) {
            Log.d(TAG, "arg == null");
            state.clickable = true;
            state.visible = true;
        } else {
            CallbackInfo info = (CallbackInfo) arg;
            state.clickable = info.clickable;
            state.visible = info.visible;
        }
        
        Log.d(TAG, "state.clickable = " + state.clickable + " state.visible = " + state.visible);
        
        if (state.clickable) {
            state.iconId = R.drawable.gn_ic_qs_more;
            state.label = mContext.getString(R.string.gn_qs_more);
        } else {            
            state.iconId = R.drawable.gn_ic_qs_more_disable;
            state.label = "";
        }
                
        state.value = false;
        state.contentDescription = mContext.getString(R.string.gn_qs_more);
    }
    
    private class CallbackInfo {
        boolean visible;
        boolean clickable;
    }

    public void setVisibleState(boolean visible, boolean clickable) {
        CallbackInfo info = new CallbackInfo();
        info.visible = visible;
        info.clickable = clickable;
        refreshState(info);
    }
}