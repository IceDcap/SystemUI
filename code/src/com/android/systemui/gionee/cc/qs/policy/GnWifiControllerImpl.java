/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
package com.android.systemui.gionee.cc.qs.policy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.systemui.R;

public class GnWifiControllerImpl extends BroadcastReceiver implements GnWifiController {

    static final String TAG = "GnWifiControllerImpl";
    public static final String NONE = "<unknown ssid>";

    final WifiManager mWifiManager;
    boolean mWifiEnabled;
    boolean mWifiConnected;
    String mWifiSsid;
    
    private AsyncTask<Void, Void, Void> mAsyncTask;
    private ExecutorService mExecutorService = Executors.newCachedThreadPool();
    ArrayList<WifiStateChangedCallback> mCallbacks = new ArrayList<WifiStateChangedCallback>();
    
    public GnWifiControllerImpl(Context context) {
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        context.registerReceiver(this, filter);
    }
    
    @Override
    public void addWifiStateChangedCallback(WifiStateChangedCallback cb) {
        mCallbacks.add(cb);
    }

    @Override
    public void removeWifiStateChangedCallback(WifiStateChangedCallback cb) {
        mCallbacks.remove(cb);
    }

    @Override
    public void setWifiEnabled(final boolean enabled) {
        Log.d(TAG, "setWifiEnabled = " + enabled);
        mExecutorService.execute(new Runnable() {
            
            @Override
            public void run() {
                // Disable tethering if enabling Wifi
                final int wifiApState = mWifiManager.getWifiApState();
                if (enabled
                        && ((wifiApState == WifiManager.WIFI_AP_STATE_ENABLING) || (wifiApState == WifiManager.WIFI_AP_STATE_ENABLED))) {
                    mWifiManager.setWifiApEnabled(null, false);
                }
                
                Log.d(TAG, "execute setWifiEnabled = " + enabled);
                mWifiManager.setWifiEnabled(enabled);
            }
        });
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
            mWifiEnabled = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN) == WifiManager.WIFI_STATE_ENABLED;
            Log.d(TAG, "updateWifiState mWifiEnabled = " + mWifiEnabled);
        } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            final NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            mWifiConnected = networkInfo != null && networkInfo.isConnected();
            Log.d(TAG, "updateWifiState mWifiConnected = " + mWifiConnected);
            // If Connected grab the signal strength and ssid
            if (mWifiConnected) {
                // try getting it out of the intent first
                WifiInfo info = (WifiInfo) intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                if (info == null) {
                    info = mWifiManager.getConnectionInfo();
                }
                if (info != null) {
                    mWifiSsid = huntForSsid(info);
                } else {
                    mWifiSsid = null;
                }
            } else if (!mWifiConnected) {
                mWifiSsid = null;
            }
            Log.d(TAG, "updateWifiState mWifiSsid = " + mWifiSsid);
        }

        notifyChanged();
    }

    @Override
    public String huntForSsid(WifiInfo info) {
        String ssid = info.getSSID();
        if (ssid != null) {
            if (ssid.equals(NONE)) {
                return null;
            } else {
                return ssid;
            }
        }
        
        // OK, it's not in the connectionInfo; we have to go hunting for it
        List<WifiConfiguration> networks = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration net : networks) {
            if (net.networkId == info.getNetworkId()) {
                return net.SSID;
            }
        }
        return null;
    }
    
    private void notifyChanged() {
        for (WifiStateChangedCallback cb : mCallbacks) {
            notifyChangedCallbacks(cb);
        }
    }

    private void notifyChangedCallbacks(WifiStateChangedCallback cb) {
        boolean wifiEnabled = mWifiEnabled && mWifiConnected;
        String wifiDesc = wifiEnabled ? mWifiSsid : null;

        cb.onWifiStateChanged(mWifiEnabled, mWifiConnected, wifiDesc);
    }
}