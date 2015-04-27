/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
package com.android.systemui.gionee.cc.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.provider.Settings.Secure;
import android.util.Log;

import com.android.systemui.gionee.cc.qs.GnQSTile;
import com.android.systemui.R;
import com.android.systemui.gionee.GnYouJu;

public class GnLauncherTile extends GnQSTile<GnQSTile.BooleanState> {
    
    private static final String TAG = "GnLauncherTile";
    
    private static final String TILES_SETTING = "sysui_qs_tiles";
    
    // public static final String ACTION_CHANGE_DEFALSE_LAUNCHER = "com.gionee.systemui.broadcast.default.launcher";
    public static final String ACTION_CHANGE_DEFALSE_LAUNCHER = "com.gionee.intent.action.SET_DEFAULT_APP_FROM_FRAMEWORK";
    
    private static boolean mVisible = false;
    
    //private boolean mListening;
    
    public GnLauncherTile(Host host) {
        super(host);
    }

    @Override
    public void setListening(boolean listening) {
        /*if (mListening == listening) return;
        mListening = listening;
        if (mListening) {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_CHANGE_DEFALSE_LAUNCHER);
            mContext.registerReceiver(mReceiver, filter);
        } else {
            mContext.unregisterReceiver(mReceiver);
        }*/
    }

    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    protected void handleClick() {
        GnYouJu.onEvent(mContext, "Amigo_SystemUI_CC", "GnLauncherTile");
    }

    @Override
    protected void handleLongClick() {

    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.value = true;
        state.visible = mVisible;
        state.label = mContext.getString(R.string.gn_qs_navi_launcher);
        state.iconId = R.drawable.gn_ic_qs_launcher;
        state.contentDescription = mContext.getString(R.string.gn_qs_navi_launcher);
        
        /*String tileList;
        if (state.visible) {
            tileList = "airplane,wifi,dataconnect,bt,location,rotation,vibrate,voice,screenshot,launcher";
        } else {
            tileList = "airplane,wifi,dataconnect,bt,location,rotation,vibrate,voice,screenshot,alarm,launcher";
        }
        Secure.putString(mContext.getContentResolver(), TILES_SETTING, tileList);*/
    }
    
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            Log.d(TAG, "GnLauncherTile : onReceive " + arg1.getAction());
            
            String packageName = getDefaultLauncher();
            if (packageName == null) {
                mVisible = false;
            } else {
                mVisible = isNotNaviLauncher(packageName);
            }
            Log.d(TAG, "isNotNaviLauncher = " + mVisible);
            
            refreshState();
        }
    };
    
    public String getDefaultLauncher() {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        final ResolveInfo res = mContext.getPackageManager().resolveActivity(intent, 0);

        if (res == null || res.activityInfo == null) {
            // should not happen. A home is always installed, isn't it?
            return null;
        } else if (res.activityInfo.packageName.equals("android")) {
            // No default selected
            return null;
        } else {
            // default launcher
            return res.activityInfo.packageName;
        }
    }
    
    private boolean isNotNaviLauncher(final String packageName) {
        return  !"com.gionee.navil".equals(packageName);
    }
}