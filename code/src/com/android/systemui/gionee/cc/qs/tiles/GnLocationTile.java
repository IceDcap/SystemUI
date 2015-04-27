/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
package com.android.systemui.gionee.cc.qs.tiles;

import android.util.Log;

import com.android.systemui.R;
import com.android.systemui.gionee.cc.qs.GnQSTile;
import com.android.systemui.gionee.cc.qs.policy.GnLocationController;
import com.android.systemui.gionee.cc.qs.policy.GnLocationController.LocationSettingsChangeCallback;
import com.android.systemui.gionee.GnYouJu;

/** Quick settings tile: Location **/
public class GnLocationTile extends GnQSTile<GnQSTile.BooleanState> {

    private final GnLocationController mController;
    private final Callback mCallback = new Callback();

    public GnLocationTile(Host host) {
        super(host);
        mController = host.getLocationController();
    }

    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void setListening(boolean listening) {
        if (listening) {
            mController.addSettingsChangedCallback(mCallback);
        } else {
            mController.removeSettingsChangedCallback(mCallback);
        }
    }

    @Override
    protected void handleClick() {
        final boolean wasEnabled = (Boolean) mState.value;
        Log.d(TAG, "setLocationEnabled " + !wasEnabled);
        mController.setLocationEnabled(!wasEnabled);
        GnYouJu.onEvent(mContext, "Amigo_SystemUI_CC", "GnLocationTile");
    }

    @Override
    protected void handleLongClick() {

    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        final boolean locationEnabled =  mController.isLocationEnabled();

        state.visible = true;
        state.value = locationEnabled;
        if (locationEnabled) {
            state.iconId = R.drawable.gn_ic_qs_gps_on;
        } else {
            state.iconId = R.drawable.gn_ic_qs_gps_off;
        }
        state.label = mContext.getString(R.string.gn_qs_location);
        state.contentDescription = mContext.getString(
                R.string.gn_qs_location);
    }

    @Override
    protected String composeChangeAnnouncement() {
        if (mState.value) {
            return mContext.getString(R.string.accessibility_quick_settings_location_changed_on);
        } else {
            return mContext.getString(R.string.accessibility_quick_settings_location_changed_off);
        }
    }

    private final class Callback implements LocationSettingsChangeCallback {
        @Override
        public void onLocationSettingsChanged(boolean enabled) {
            refreshState();
        }
    };
}
