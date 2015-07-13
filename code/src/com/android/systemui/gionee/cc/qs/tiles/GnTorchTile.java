package com.android.systemui.gionee.cc.qs.tiles;

import android.util.Log;

import com.android.systemui.R;
import com.android.systemui.gionee.GnYouJu;
import com.android.systemui.gionee.cc.qs.GnQSTile;
import com.android.systemui.gionee.cc.qs.GnQSTile.BooleanState;
import com.android.systemui.gionee.cc.qs.policy.GnTorchController;
import com.android.systemui.gionee.cc.torch.GnTorchControllerImpl;

public class GnTorchTile extends GnQSTile<BooleanState> {

    private GnTorchControllerImpl mController;
    
    private GnTorchController mGnTorchController;
    
    public GnTorchTile(Host host, String spec) {
        super(host, spec);
        mController = GnTorchControllerImpl.getInstance(mContext);
        mGnTorchController = new GnTorchController(mContext);
    }

    @Override
    public void setListening(boolean listening) {
        if (listening) {
            mController.addStateChangedCallback(mCallback);
            mGnTorchController.addListener(mListener);
        } else {
            mController.removeStateChangedCallback(mCallback);
            mGnTorchController.removeListener(mListener);
        }
    }

    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    protected void handleClick() {
        GnYouJu.onEvent(mContext, "Amigo_SystemUI_CC", "GnTorchTile");
        Log.d(TAG, "GnTorchTile  handleClick");
        mController.handleClick();
    }

    @Override
    protected void handleLongClick() {

    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.value = arg instanceof Boolean ? (Boolean) arg : mController.isTorchOn();        
        if (state.value) {
            state.iconId = R.drawable.gn_ic_qs_torch_on;
        } else {
            state.iconId = R.drawable.gn_ic_qs_torch_off;
        }
        
        state.visible = true;
        state.label = mContext.getString(R.string.gn_qs_torch);
        state.contentDescription = mContext.getString(R.string.gn_qs_torch);
    
        Log.d(TAG, "value = " + state.value);
    }

    private final GnTorchControllerImpl.Callback mCallback = new GnTorchControllerImpl.Callback() {
        
        @Override
        public void onTorchStateChange(boolean enabled) {
            refreshState(enabled);
        }
    };
    
    private final GnTorchController.FlashlightListener mListener = new GnTorchController.FlashlightListener() {
        
        @Override
        public void onFlashlightOff() {
            Log.d(TAG, "onFlashlightOff");
            refreshState(false);
        }
        
        @Override
        public void onFlashlightError() {
            Log.d(TAG, "onFlashlightError");
            refreshState(false);
        }
        
        @Override
        public void onFlashlightAvailabilityChanged(boolean available) {
            Log.d(TAG, "onFlashlightAvailabilityChanged");
            refreshState(false);
        }
    };
}
