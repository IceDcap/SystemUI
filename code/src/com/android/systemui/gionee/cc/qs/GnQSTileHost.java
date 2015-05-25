/*
*
* MODULE DESCRIPTION
*   GnQSTileHost 
* add by huangwt for Android L at 20141210.
* 
*/
package com.android.systemui.gionee.cc.qs;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.Settings.Secure;
import android.util.Log;

import com.android.systemui.R;
import com.android.systemui.gionee.cc.GnControlCenterView;
import com.android.systemui.gionee.cc.qs.policy.GnBluetoothController;
import com.android.systemui.gionee.cc.qs.policy.GnLocationController;
import com.android.systemui.gionee.cc.qs.policy.GnMobileDataController;
import com.android.systemui.gionee.cc.qs.policy.GnNextAlarmController;
import com.android.systemui.gionee.cc.qs.policy.GnRotationLockController;
import com.android.systemui.gionee.cc.qs.policy.GnWifiController;
import com.android.systemui.gionee.cc.qs.policy.GnWifiControllerImpl;
import com.android.systemui.gionee.cc.qs.tiles.GnAirplaneModeTile;
import com.android.systemui.gionee.cc.qs.tiles.GnAlarmTile;
import com.android.systemui.gionee.cc.qs.tiles.GnBluetoothTile;
import com.android.systemui.gionee.cc.qs.tiles.GnCalculateTile;
import com.android.systemui.gionee.cc.qs.tiles.GnCameraTile;
import com.android.systemui.gionee.cc.qs.tiles.GnFakeCallTile;
import com.android.systemui.gionee.cc.qs.tiles.GnLauncherTile;
import com.android.systemui.gionee.cc.qs.tiles.GnLocationTile;
import com.android.systemui.gionee.cc.qs.tiles.GnMobileDataTile;
import com.android.systemui.gionee.cc.qs.tiles.GnMoreTile;
import com.android.systemui.gionee.cc.qs.tiles.GnPowerTile;
import com.android.systemui.gionee.cc.qs.tiles.GnRotationLockTile;
import com.android.systemui.gionee.cc.qs.tiles.GnScreenShotTile;
import com.android.systemui.gionee.cc.qs.tiles.GnSettingTile;
import com.android.systemui.gionee.cc.qs.tiles.GnTorchTile;
import com.android.systemui.gionee.cc.qs.tiles.GnVibrateTile;
import com.android.systemui.gionee.cc.qs.tiles.GnVoiceTile;
import com.android.systemui.gionee.cc.qs.tiles.GnWifiTile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Platform implementation of the quick settings tile host **/
public class GnQSTileHost implements GnQSTile.Host {
    private static final String TAG = "QSTileHost";
    private static final boolean DEBUG = true;//Log.isLoggable(TAG, Log.DEBUG);

    private static final String TILES_SETTING = "sysui_qs_tiles";

    private final Context mContext;
    private final GnControlCenterView mGnControlCenterView;
    private final LinkedHashMap<String, GnQSTile<?>> mTiles = new LinkedHashMap<>();
    private final Observer mObserver = new Observer();
    private final GnBluetoothController mBluetooth;
    private final GnLocationController mLocation;
    private final GnRotationLockController mRotation;
    private final GnWifiController mWifi;
    private final GnMobileDataController mMobileData;
    private final GnNextAlarmController mAlarm;
    private final Looper mLooper;

    private Callback mCallback;

    public GnQSTileHost(Context context, GnControlCenterView view,
            GnBluetoothController bluetooth, GnLocationController location, 
            GnRotationLockController rotation, GnWifiController wifi,
            GnMobileDataController mobiledata, GnNextAlarmController alarm) {
        mContext = context;
        mGnControlCenterView = view;
        mBluetooth = bluetooth;
        mLocation = location;
        mRotation = rotation;
        mWifi = wifi;
        mMobileData = mobiledata;
        mAlarm = alarm;
        
        final HandlerThread ht = new HandlerThread(GnQSTileHost.class.getSimpleName());
        ht.start();
        mLooper = ht.getLooper();

        recreateTiles();
        
        mObserver.register();
    }

    @Override
    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    @Override
    public Collection<GnQSTile<?>> getTiles() {
        return mTiles.values();
    }
    
    @Override
    public void startSettingsActivity(final Intent intent) {
        mGnControlCenterView.postStartSettingsActivity(intent, 300); // 400: time for collapse controlcenter 
    }

    @Override
    public void startSettingsActivity(Intent intent, int delay) {
        mGnControlCenterView.postStartSettingsActivity(intent, delay);
    }

    @Override
    public void warn(String message, Throwable t) {
        // already logged
    }

    @Override
    public void collapsePanels() {
        mGnControlCenterView.dismiss();
    }
    
    @Override
    public void openMoreView() {
        mGnControlCenterView.openMoreView();
    }

    @Override
    public Looper getLooper() {
        return mLooper;
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public GnBluetoothController getBluetoothController() {
        return mBluetooth;
    }
    
    @Override
    public GnLocationController getLocationController() {
        return mLocation;
    }
    
    @Override
    public GnRotationLockController getRotationLockController() {
        return mRotation;
    }
    
    @Override
    public GnWifiController getGnWifiController() {
        return mWifi;
    }

    @Override
    public GnMobileDataController getGnMobileDataController() {
        return mMobileData;
    }

    @Override
    public GnNextAlarmController getGnNextAlarmController() {
        return mAlarm;
    }
    
    private void recreateTiles() {
        if (DEBUG) Log.d(TAG, "Recreating tiles");
        final List<String> tileSpecs = loadTileSpecs();
        for (Map.Entry<String, GnQSTile<?>> tile : mTiles.entrySet()) {
            if (tileSpecs.contains(tile.getKey())) {
                if (DEBUG) Log.d(TAG, "Destroying tile: " + tile.getKey());
                tile.getValue().destroy();
            }
        }
        final LinkedHashMap<String, GnQSTile<?>> newTiles = new LinkedHashMap<>();
        for (String tileSpec : tileSpecs) {
            if (mTiles.containsKey(tileSpec)) {
                if (DEBUG) Log.d(TAG, "add tile: " + tileSpec);
                newTiles.put(tileSpec, mTiles.get(tileSpec));
            } else {
                if (DEBUG) Log.d(TAG, "Creating tile: " + tileSpec);
                try {
                    newTiles.put(tileSpec, createTile(tileSpec));
                } catch (Throwable t) {
                    Log.w(TAG, "Error creating tile for spec: " + tileSpec, t);
                }
            }
        }
        // if (mTiles.equals(newTiles)) return;
        mTiles.clear();
        mTiles.putAll(newTiles);
        if (mCallback != null) {
            mCallback.onTilesChanged();
        }
    }

    private GnQSTile<?> createTile(String tileSpec) {
        if (tileSpec.equals("bt")) return new GnBluetoothTile(this, tileSpec);
        else if (tileSpec.equals("airplane")) return new GnAirplaneModeTile(this, tileSpec);
        else if (tileSpec.equals("location")) return new GnLocationTile(this, tileSpec);
        else if (tileSpec.equals("rotation")) return new GnRotationLockTile(this, tileSpec);
        else if (tileSpec.equals("wifi")) return new GnWifiTile(this, tileSpec);
        else if (tileSpec.equals("dataconnect")) return new GnMobileDataTile(this, tileSpec);
        else if (tileSpec.equals("vibrate")) return new GnVibrateTile(this, tileSpec);
        else if (tileSpec.equals("voice")) return new GnVoiceTile(this, tileSpec);
        else if (tileSpec.equals("screenshot")) return new GnScreenShotTile(this, tileSpec);
        else if (tileSpec.equals("alarm")) return new GnAlarmTile(this, tileSpec);
//        else if (tileSpec.equals("launcher")) return new GnLauncherTile(this, tileSpec);
        else if (tileSpec.equals("power")) return new GnPowerTile(this, tileSpec);
        else if (tileSpec.equals("setting")) return new GnSettingTile(this, tileSpec);
        else if (tileSpec.equals("more")) return new GnMoreTile(this, tileSpec);
        else if (tileSpec.endsWith("calculate")) return new GnCalculateTile(this, tileSpec);
        else if (tileSpec.endsWith("camera")) return new GnCameraTile(this, tileSpec);
        else if (tileSpec.endsWith("torch")) return new GnTorchTile(this, tileSpec);
        else if (tileSpec.endsWith("fakecall")) return new GnFakeCallTile(this, tileSpec);
        else throw new IllegalArgumentException("Bad tile spec: " + tileSpec);
    }

    private List<String> loadTileSpecs() {
        final Resources res = mContext.getResources();
        final String defaultTileList = res.getString(R.string.gn_quick_settings_tiles_default);
        String tileList = Secure.getStringForUser(mContext.getContentResolver(), TILES_SETTING, 0);
        if (tileList == null) {
            tileList = res.getString(R.string.quick_settings_tiles);
            if (DEBUG) Log.d(TAG, "Loaded tile specs from config: " + tileList);
        } else {
            if (DEBUG) Log.d(TAG, "Loaded tile specs from setting: " + tileList);
        }
        final ArrayList<String> tiles = new ArrayList<String>();
        boolean addedDefault = false;
        for (String tile : tileList.split(",")) {
            tile = tile.trim();
            if (tile.isEmpty()) continue;
            if (tile.equals("default")) {
                if (!addedDefault) {
                    if (DEBUG) Log.d(TAG, "Loaded tile specs from default: " + defaultTileList);
                    tiles.addAll(Arrays.asList(defaultTileList.split(",")));
                    addedDefault = true;
                }
            } else {
                tiles.add(tile);
            }
        }
        return tiles;
    }

    private class Observer extends ContentObserver {
        private boolean mRegistered;

        public Observer() {
            super(new Handler(Looper.getMainLooper()));
        }

        public void register() {
            if (mRegistered) {
                mContext.getContentResolver().unregisterContentObserver(this);
            }
            mContext.getContentResolver().registerContentObserver(Secure.getUriFor(TILES_SETTING),
                    false, this);
            mRegistered = true;
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            recreateTiles();
        }
    }

}
