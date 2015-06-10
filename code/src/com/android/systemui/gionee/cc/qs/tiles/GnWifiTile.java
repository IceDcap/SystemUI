/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
package com.android.systemui.gionee.cc.qs.tiles;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;

import com.android.systemui.R;
import com.android.systemui.gionee.cc.qs.GnQSTile;
import com.android.systemui.gionee.cc.qs.policy.GnWifiController;
import com.android.systemui.gionee.cc.qs.policy.GnWifiController.WifiStateChangedCallback;
import com.android.systemui.gionee.GnYouJu;

/** Quick settings tile: Wifi **/
public class GnWifiTile extends GnQSTile<GnQSTile.BooleanState> {
    
    private static final Intent WIFI_SETTINGS = new Intent(Settings.ACTION_WIFI_SETTINGS);
    private static final String NONE = "<unknown ssid>";

    private final GnWifiController mController;

    public GnWifiTile(Host host, String spec) {
        super(host, spec);
        mController = host.getGnWifiController();
    }

    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void setListening(boolean listening) {
        if (listening) {
            mController.addWifiStateChangedCallback(mCallback);
        } else {
            mController.removeWifiStateChangedCallback(mCallback);
        }
    }

    @Override
    public boolean supportsLongClick() {
        return true;
    }

    @Override
    protected void handleClick() {
        final boolean isEnabled = (Boolean)mState.value;
        Log.d(TAG, "setWifiEnabled " + !isEnabled);
        mController.setWifiEnabled(!isEnabled);
        GnYouJu.onEvent(mContext, "Amigo_SystemUI_CC", "GnWifiTile");
    }

    @Override
    protected void handleLongClick() {
        // mHost.startSettingsActivity(WIFI_SETTINGS);
        Intent intent = new Intent();
        String packagename = "com.gionee.setting.adapter.wifi";
        String classname = "com.gionee.setting.adapter.wifi.wifiSettingsActivity";
        intent.setClassName(packagename, classname);
        mHost.startSettingsActivity(intent);
    }

    @Override
    protected void handleSecondaryClick() {
        mHost.startSettingsActivity(WIFI_SETTINGS);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.visible = true;
        final Resources r = mContext.getResources();
        
        boolean enable = false;
        boolean wifiConnected = false;
        String enabledDesc = null;

        if (DEBUG) Log.d(TAG, "handleUpdateState arg=" + arg);

        if (arg == null) {
            WifiManager wifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                int wifiState = wifiManager.getWifiState();
                if (WifiManager.WIFI_STATE_ENABLED == wifiState) {
                    enable = true;
                    ConnectivityManager conMan = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                    android.net.NetworkInfo.State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
                    if (wifi == android.net.NetworkInfo.State.CONNECTED) {
                        wifiConnected = true;
                        enabledDesc = mController.huntForSsid(wifiManager.getConnectionInfo());
                    }
                }
            }
        } else {
            CallbackInfo cb = (CallbackInfo) arg;
            enable = cb.enabled;
            wifiConnected = cb.connected && (cb.enabledDesc != null);
            enabledDesc = cb.enabledDesc;
        }
        
        state.value = enable;
        if (enable) {
            if (wifiConnected) {
                state.iconId = R.drawable.gn_ic_qs_wifi_connect;
                state.label = removeDoubleQuotes(enabledDesc);
            } else {
                state.iconId = R.drawable.gn_ic_qs_wifi_disconnect;
                state.label = r.getString(R.string.quick_settings_wifi_label);
            }
        } else {
            state.iconId = R.drawable.gn_ic_qs_wifi_off;
            state.label = r.getString(R.string.quick_settings_wifi_label);
        }
        
        Log.d(TAG, "enable = " + enable + " wifiConnected = " + wifiConnected + "  enabledDesc = " + enabledDesc);
    }

    private static String removeDoubleQuotes(String string) {
        if (string == null) {
            return "WLAN";
        }
        
        final int length = string.length();
        if ((length > 1) && (string.charAt(0) == '"') && (string.charAt(length - 1) == '"')) {
            return string.substring(1, length - 1);
        }
        return string;
    }

    private static final class CallbackInfo {
        boolean enabled;
        boolean connected;
        String enabledDesc;
    }

    private final WifiStateChangedCallback mCallback = new WifiStateChangedCallback() {
        
        @Override
        public void onWifiStateChanged(boolean enabled, boolean connected, String description) {
            if (DEBUG) Log.d(TAG, "onWifiStateChanged enabled=" + enabled + " connected = " + connected
                        + " description = " + description);
            final CallbackInfo info = new CallbackInfo();
            info.enabled = enabled;
            info.connected = connected;
            info.enabledDesc = description;
            refreshState(info);
        }

        @Override
        public void onAirplaneModeChanged(boolean enabled) {
            // noop
        }
    };
    
}
