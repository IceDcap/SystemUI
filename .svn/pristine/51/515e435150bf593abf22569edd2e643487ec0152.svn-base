package com.android.systemui.gionee.cc.torch;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;

public class GnCameraTorch extends GnTorch {
    private static final String TAG = "GnCameraTorch";

    private static Camera mCamera;
    private static Parameters mParameters;

    private static boolean mIsLightOn = false;

    @Override
    public void on() {
        Log.d(TAG, "camera torch on()  " + (mCamera != null));
        if (mCamera == null) {
            try {
                mCamera = Camera.open();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            mParameters = mCamera.getParameters();
        }
        mParameters.getFlashMode();
        Log.d(TAG, "flash mode: " + mParameters.getFlashMode() + "  thread name: "
                + Thread.currentThread().getName());
        mParameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
        mCamera.setParameters(mParameters);
        mIsLightOn = true;
        notifyLightUiState();
    }

    @Override
    public void off() {
        Log.d(TAG, "camera torch off()  " + (mCamera != null) + " " + (mParameters != null));
        if (mCamera != null && mParameters != null) {
            mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(mParameters);
            mIsLightOn = false;
        }

        release();
        notifyLightUiState();
    }

    @Override
    public void release() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mParameters = null;
            mCamera = null;
            mIsLightOn = false;
        }
    }

    @Override
    public boolean getTorchState() {
        return mIsLightOn;
    }
}
