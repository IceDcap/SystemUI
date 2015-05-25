package com.android.systemui.gionee.cc.qs.tiles;

import android.content.Intent;

import com.android.systemui.R;
import com.android.systemui.gionee.GnYouJu;
import com.android.systemui.gionee.cc.camera.GnBlindShootActivity;
import com.android.systemui.gionee.cc.qs.GnQSTile;
import com.android.systemui.gionee.cc.qs.policy.GnCameraController;

public class GnCameraTile extends GnQSTile<GnQSTile.BooleanState> {
    
    private final static String CAMERA_PKG = "com.android.camera";
    private final static String CAMERA_CLS = "com.android.camera.CameraLauncher";
    
    private GnCameraController mGnTorchController;

    public GnCameraTile(Host host, String spec) {
        super(host, spec);
        
        mGnTorchController = new GnCameraController(mContext);
    }

    @Override
    public void setListening(boolean listening) {

    }

    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    protected void handleClick() {
        GnYouJu.onEvent(mContext, "Amigo_SystemUI_CC", "GnCameraTile");
        Intent intent = new Intent();
        intent.setClassName(CAMERA_PKG, CAMERA_CLS);
        mHost.startSettingsActivity(intent);
    }

    @Override
    protected void handleLongClick() {
        if (mGnTorchController.isCameraAvailable()) {
            GnYouJu.onEvent(mContext, "Amigo_SystemUI_CC", "mCamera_longClicked");
            Intent intent = new Intent(mContext, GnBlindShootActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.value = false;
        state.visible = true;
        state.label = mContext.getString(R.string.gn_qs_camera);
        state.iconId = R.drawable.gn_ic_qs_camera;
        state.contentDescription = mContext.getString(R.string.gn_qs_camera);
    }

}
