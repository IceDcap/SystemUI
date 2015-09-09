/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
package com.android.systemui.gionee.cc.qs.policy;

import android.net.wifi.WifiInfo;

public interface GnWifiController{

    void addWifiStateChangedCallback(WifiStateChangedCallback cb);
    void removeWifiStateChangedCallback(WifiStateChangedCallback cb);
    void setWifiEnabled(boolean enabled);
    String huntForSsid(WifiInfo info);
    
    public interface WifiStateChangedCallback {
        void onWifiStateChanged(boolean enabled, boolean connected, String description);
        void onAirplaneModeChanged(boolean enabled);
    }
    
}