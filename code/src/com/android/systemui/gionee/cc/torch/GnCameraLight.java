package com.android.systemui.gionee.cc.torch;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;

public class GnCameraLight extends GnLight {
    private static final String LOG_TAG = "CameraLight";

    private static Camera mCamera;
    private static Parameters mParameters;
    private static boolean mIsLightOn = false;
    //TODO:在非主线程调用，回调会有问题！！！！
    @Override
    public void on() {
        Log.d(LOG_TAG, "camera light on()  " + (mCamera != null));
        if (mCamera == null) {
            try {
                mCamera = Camera.open();
                // Gionee <jiangxiao> <2014-02-12> delete for CR01047526 begin
                // mCamera.startPreview();
                // Gionee <jiangxiao> <2014-02-12> delete for CR01047526 end
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            mParameters = mCamera.getParameters();
        }
        mParameters.getFlashMode();
        Log.d(LOG_TAG, "flash mode: " + mParameters.getFlashMode() + "  thread name: "
                + Thread.currentThread().getName());
        mParameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
        mCamera.setParameters(mParameters);
        mIsLightOn = true;
        notifyLightUiState();

    }

    @Override
    public void off() {
        Log.d(LOG_TAG, "camera light off()  " + (mCamera != null) + " " + (mParameters != null));
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
    public boolean getLightState() {
        return mIsLightOn;
    }

}
