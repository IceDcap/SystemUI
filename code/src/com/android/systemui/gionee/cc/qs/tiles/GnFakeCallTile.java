package com.android.systemui.gionee.cc.qs.tiles;

import android.util.Log;

import com.android.systemui.R;
import com.android.systemui.gionee.GnYouJu;
import com.android.systemui.gionee.cc.fakecall.GnFakeCallControllerImpl;
import com.android.systemui.gionee.cc.qs.GnQSTile;

public class GnFakeCallTile extends GnQSTile<GnQSTile.AnimBooleanState> {

    private GnFakeCallControllerImpl mController;
    
    public GnFakeCallTile(Host host, String spec) {
        super(host, spec);
        mController = GnFakeCallControllerImpl.getInstance(mContext);
    }

    @Override
    public void setListening(boolean listening) {
        if (listening) {
            mController.addStateChangeCallback(mCallback);
        } else {
            mController.removeStateChangeCallback(mCallback);
        }
    }

    @Override
    protected AnimBooleanState newTileState() {
        return new AnimBooleanState();
    }

    @Override
    protected void handleClick() {
        GnYouJu.onEvent(mContext, "Amigo_SystemUI_CC", "mFakeCall_clicked");
        Log.d(TAG, "handleClick " + mState.value);
        mController.handleClick(mState.value);
    }

    @Override
    protected void handleLongClick() {

    }

    @Override
    protected void handleUpdateState(GnQSTile.AnimBooleanState state, Object arg) {
        state.visible = true;
        
        if (arg == null && !state.animating) {
            state.label = mContext.getString(R.string.gn_qs_fake_call);
            state.value = false;
            state.animating = false;
        } else if (arg != null) {
            CallbackInfo info = (CallbackInfo) arg;
            state.label = info.lable;
            state.value = info.enable;
            state.animating = info.animating;
        } else {
            state.label = mController.getCurrentTime();
        }
        
        if (state.value) {
            state.iconId = R.drawable.gn_ic_qs_fake_call_cancel;
        } else {
            state.iconId = R.drawable.gn_ic_qs_fake_call;
        }
        
        Log.d(TAG, "value = " + state.value + "  lable = " + state.label);
    }
    
    private class CallbackInfo {
        String lable;
        boolean enable;
        boolean animating;
    }
    
    private GnFakeCallControllerImpl.Callback mCallback = new GnFakeCallControllerImpl.Callback() {
        
        @Override
        public void onStateChange(String label, boolean enable, boolean animating) {
            CallbackInfo info = new CallbackInfo();
            info.lable = label;
            info.enable = enable;
            info.animating = animating;
            refreshState(info);
        }

        @Override
        public void collapsePanels() {
            mHost.collapsePanels();
        }
    };

}
