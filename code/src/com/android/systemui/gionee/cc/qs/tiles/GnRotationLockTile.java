/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
package com.android.systemui.gionee.cc.qs.tiles;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;

import com.android.systemui.R;
import com.android.systemui.gionee.cc.qs.GnQSTile;
import com.android.systemui.gionee.cc.qs.policy.GnRotationLockController;
import com.android.systemui.gionee.cc.qs.policy.GnRotationLockController.RotationLockControllerCallback;
import com.android.systemui.gionee.GnYouJu;

/** Quick settings tile: Rotation **/
public class GnRotationLockTile extends GnQSTile<GnQSTile.BooleanState> {

    private final GnRotationLockController mController;

    public GnRotationLockTile(Host host, String spec) {
        super(host, spec);
        mController = host.getRotationLockController();
    }

    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }

    public void setListening(boolean listening) {
        if (mController == null) return;
        if (listening) {
            mController.addRotationLockControllerCallback(mCallback);
        } else {
            mController.removeRotationLockControllerCallback(mCallback);
        }
    }

    @Override
    protected void handleClick() {
    	GnYouJu.onEvent(mContext, "Amigo_SystemUI_CC", "GnRotationLockTile");
    	Log.d(TAG, "handleClick   mController=" + mController + "  mState.value " + mState.value);
        if (mController == null) return;
        mController.setRotationLocked(mState.value);
    }

    @Override
    protected void handleLongClick() {

    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        if (mController == null) return;
        state.visible = mController.isRotationLockAffordanceVisible();
        
        final Resources res = mContext.getResources();
        final boolean rotationLocked = mController.isRotationLocked();
        state.value = !rotationLocked;
        if (rotationLocked) {
            state.icon = res.getDrawable(R.drawable.gn_ic_qs_rotation_off);
        } else {
            state.icon = res.getDrawable(R.drawable.gn_ic_qs_rotation_on);
        }
        
        state.label = mContext.getString(R.string.gn_qs_rotation);
        state.contentDescription = mContext.getString(R.string.gn_qs_rotation);
    }

    /**
     * Get the correct accessibility string based on the state
     *
     * @param idWhenPortrait The id which should be used when locked in portrait.
     * @param idWhenLandscape The id which should be used when locked in landscape.
     * @param idWhenOff The id which should be used when the rotation lock is off.
     * @return
     */
    private String getAccessibilityString(int idWhenPortrait, int idWhenLandscape, int idWhenOff) {
        int stringID;
        if (mState.value) {
            final boolean portrait = mContext.getResources().getConfiguration().orientation
                    != Configuration.ORIENTATION_LANDSCAPE;
            stringID = portrait ? idWhenPortrait: idWhenLandscape;
        } else {
            stringID = idWhenOff;
        }
        return mContext.getString(stringID);
    }

    @Override
    protected String composeChangeAnnouncement() {
        return getAccessibilityString(
                R.string.accessibility_rotation_lock_on_portrait_changed,
                R.string.accessibility_rotation_lock_on_landscape_changed,
                R.string.accessibility_rotation_lock_off_changed);
    }

    private final RotationLockControllerCallback mCallback = new RotationLockControllerCallback() {
        @Override
        public void onRotationLockStateChanged(boolean rotationLocked, boolean affordanceVisible) {
            refreshState();
        }
    };
}
