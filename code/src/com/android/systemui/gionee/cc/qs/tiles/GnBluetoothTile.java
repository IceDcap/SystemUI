/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
package com.android.systemui.gionee.cc.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.android.systemui.R;
import com.android.systemui.gionee.cc.qs.GnQSTile;
import com.android.systemui.gionee.cc.qs.policy.GnBluetoothController;
import com.android.systemui.gionee.cc.qs.policy.GnBluetoothController.PairedDevice;
import com.android.systemui.gionee.GnYouJu;
import java.util.Set;

/** Quick settings tile: Bluetooth **/
public class GnBluetoothTile extends GnQSTile<GnQSTile.BooleanState>  {
    private static final Intent BLUETOOTH_SETTINGS = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);

    private final GnBluetoothController mController;

    public GnBluetoothTile(Host host, String spec) {
        super(host, spec);
        mController = host.getBluetoothController();
    }

    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void setListening(boolean listening) {
        if (listening) {
            mController.addStateChangedCallback(mCallback);
        } else {
            mController.removeStateChangedCallback(mCallback);
        }
    }

    @Override
    protected void handleClick() {
        final boolean isEnabled = (Boolean)mState.value;
        Log.d(TAG, "setBluetoothEnabled " + !isEnabled);
        mController.setBluetoothEnabled(!isEnabled);
        GnYouJu.onEvent(mContext, "Amigo_SystemUI_CC", "GnBluetoothTile");
    }

    @Override
    protected void handleLongClick() {
        mHost.startSettingsActivity(BLUETOOTH_SETTINGS);
    }

    @Override
    protected void handleSecondaryClick() {
        mHost.startSettingsActivity(BLUETOOTH_SETTINGS);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        final boolean supported = mController.isBluetoothSupported();
        final boolean enabled = mController.isBluetoothEnabled();
        final boolean connected = mController.isBluetoothConnected();
        final boolean connecting = mController.isBluetoothConnecting();
        state.visible = supported;
        state.value = enabled;
        state.autoMirrorDrawable = false;
        if (enabled) {
            state.label = null;
            if (connected) {
                state.iconId = R.drawable.gn_ic_qs_bluetooth_on;
                state.contentDescription = mContext.getString(
                        R.string.accessibility_quick_settings_bluetooth_connected);
                state.label = mController.getLastDeviceName();
            } else if (connecting) {
                state.iconId = R.drawable.gn_ic_qs_bluetooth_on;
                state.contentDescription = mContext.getString(
                        R.string.accessibility_quick_settings_bluetooth_connecting);
                state.label = mContext.getString(R.string.quick_settings_bluetooth_label);
            } else {
                state.iconId = R.drawable.gn_ic_qs_bluetooth_on;
                state.contentDescription = mContext.getString(
                        R.string.accessibility_quick_settings_bluetooth_on);
            }
            if (TextUtils.isEmpty(state.label)) {
                state.label = mContext.getString(R.string.quick_settings_bluetooth_label);
            }
        } else {
            state.iconId = R.drawable.gn_ic_qs_bluetooth_off;
            state.label = mContext.getString(R.string.quick_settings_bluetooth_label);
            state.contentDescription = mContext.getString(
                    R.string.accessibility_quick_settings_bluetooth_off);
        }

        String bluetoothName = state.label;
        if (connected) {
            bluetoothName = state.dualLabelContentDescription = mContext.getString(
                    R.string.accessibility_bluetooth_name, state.label);
        }
        state.dualLabelContentDescription = bluetoothName;
    }

    @Override
    protected String composeChangeAnnouncement() {
        if (mState.value) {
            return mContext.getString(R.string.accessibility_quick_settings_bluetooth_changed_on);
        } else {
            return mContext.getString(R.string.accessibility_quick_settings_bluetooth_changed_off);
        }
    }

    private final GnBluetoothController.Callback mCallback = new GnBluetoothController.Callback() {
        @Override
        public void onBluetoothStateChange(boolean enabled, boolean connecting) {
            refreshState();
        }
        @Override
        public void onBluetoothPairedDevicesChanged() {
            refreshState();
        }
    };

}
