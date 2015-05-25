/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
package com.android.systemui.gionee.cc.qs.tiles;

import java.util.List;

import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Resources;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;

import com.android.systemui.R;
import com.android.systemui.gionee.cc.qs.GnQSTile;
import com.android.systemui.gionee.cc.qs.policy.GnMobileDataController;
import com.android.systemui.gionee.cc.qs.policy.GnMobileDataController.MobileDataChangedCallback;
//import com.android.systemui.gionee.statusbar.util.GnSIMHelper;
import com.android.systemui.gionee.GnYouJu;

/** Quick settings tile: Cellular **/
public class GnMobileDataTile extends GnQSTile<GnQSTile.BooleanState> {
    private static final Intent CELLULAR_SETTINGS = new Intent().setComponent(new ComponentName(
            "com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity"));

    private final GnMobileDataController mController;
    private final SubscriptionManager mSubscriptionManager;

    public GnMobileDataTile(Host host, String spec) {
        super(host, spec);
        mController = host.getGnMobileDataController();
        mSubscriptionManager = SubscriptionManager.from(mContext);
    }

    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void setListening(boolean listening) {
        if (listening) {
            mController.addMobileDataChangedCallback(mCallback);
        } else {
            mController.removeMobileDataChangedCallback(mCallback);
        }
    }

    @Override
    protected void handleClick() {
        Log.d(TAG, "setMobileDataEnabled " + !mState.value);
        mController.setMobileDataEnabled(!mState.value);
        GnYouJu.onEvent(mContext, "Amigo_SystemUI_CC", "GnMobileDataTile");
    }

    @Override
    protected void handleLongClick() {
        Intent intent = new Intent("com.android.settings.sim.SIM_SUB_INFO_SETTINGS");
        mHost.startSettingsActivity(intent);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        final Resources r = mContext.getResources();

        boolean airplaneEnabled = mController.isAirplaneEnabled();
        boolean noSim = true;
        List<SubscriptionInfo> subscriptions = mSubscriptionManager.getActiveSubscriptionInfoList();
        Log.d(TAG, "subscriptions = " + subscriptions);
        if (subscriptions != null) {
            Log.d(TAG, "subscriptions.size() = " + subscriptions.size());
            if (subscriptions.size() > 0) {
                noSim = false;
            }
        }
        
        state.value = mController.isMobileDataEnabled() && !noSim && !airplaneEnabled;
        state.label = r.getString(R.string.gn_qs_data_connect);
        state.visible = mController.hasMobileDataFeature();
        state.clickable = noSim || airplaneEnabled ? false : true;
        state.iconId = noSim || airplaneEnabled ? R.drawable.gn_ic_qs_no_sim
                : state.value ? R.drawable.gn_ic_qs_dataconnect_on
                : R.drawable.gn_ic_qs_dataconnect_off;
        
        Log.d(TAG, "state.value = " + state.value + "  noSim = " + noSim + "  airplaneEnabled = " + airplaneEnabled);
    }

    private final MobileDataChangedCallback mCallback = new MobileDataChangedCallback() {

        @Override
        public void onMobileDataChanged() {
                refreshState();
        }
    };
}
