package com.android.systemui.gionee.cc.qs.tiles;

import java.lang.reflect.Method;

import android.content.Intent;
import android.util.Log;

import com.android.systemui.R;
import com.android.systemui.gionee.GnYouJu;
import com.android.systemui.gionee.cc.camera.GnBlindShootActivity;
import com.android.systemui.gionee.cc.qs.GnQSTile;
import com.android.systemui.gionee.cc.qs.policy.GnCameraController;
import com.android.systemui.gionee.cc.util.GnVibrateUtil;

public class GnCameraTile extends GnQSTile<GnQSTile.BooleanState> {
    
    private final static String CAMERA_PKG = "com.android.camera";
    private final static String CAMERA_CLS = "com.android.camera.CameraLauncher";

    public GnCameraTile(Host host, String spec) {
        super(host, spec);
    }

    @Override
    public void setListening(boolean listening) {

    }

    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public boolean supportsLongClick() {
        return true;
    }

    @Override
    protected void handleClick() {
        GnYouJu.onEvent(mContext, "Amigo_SystemUI_CC", "GnCameraTile");
        Log.d(TAG, "handleClick");
        Intent intent = new Intent();
        intent.setClassName(CAMERA_PKG, CAMERA_CLS);
        mHost.startSettingsActivity(intent);
    }

    @Override
    protected void handleLongClick() {
        if (isCameraRunning()) {
            return;
        }
        
        Log.d(TAG, "handleLongClick");
        GnYouJu.onEvent(mContext, "Amigo_SystemUI_CC", "mCamera_longClicked");
        Intent intent = new Intent(mContext, GnBlindShootActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.value = false;
        state.visible = true;
        state.label = mContext.getString(R.string.gn_qs_camera);
        state.iconId = R.drawable.gn_ic_qs_camera;
        state.contentDescription = mContext.getString(R.string.gn_qs_camera);
    }

    private boolean isCameraRunning() { 
        try { 
            Class c = Class.forName("android.hardware.Camera"); 
            Method m = c.getMethod("isCameraRunning"); 
            boolean isRunning = (Boolean) m.invoke(null); 
            Log.i(TAG, "isCameraRunning = " + isRunning); 
            return isRunning; 
        } catch (Exception e) { 
            Log.i(TAG, "isCameraRunning = " + e); 
            return false; 
        } 
    }
}
