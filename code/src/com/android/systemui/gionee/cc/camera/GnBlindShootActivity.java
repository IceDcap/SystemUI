package com.android.systemui.gionee.cc.camera;
/*
*
* MODULE DESCRIPTION
*   camera long click 
* add by huangwt for Android L at 20141210.
* 
*/
import java.util.List;

import com.android.systemui.gionee.cc.service.IPhotoService;
import com.android.systemui.gionee.cc.util.GnAppConstants;
import com.android.systemui.gionee.cc.util.GnVibrateUtil;
import com.android.systemui.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GnBlindShootActivity extends Activity {

    private static final String LOG_TAG = "Blind_Shoot";
    private static final int NOTIFICATON_ID=95766219;

    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Parameters mParameters;
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;;

    private int mLastOrientation = 0;
    private int mLastRotation = 0;
    private CameraOrientationEventListener mOrientationEventListener = null;

    private IPhotoService mPhotoService = null;
    private ServiceConnection mServiceConn = null;

    private NotificationManager mNotificationManager;
    private Notification mNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.gn_sc_activity_blind_shoot);
        Log.d("jings", "onCreate: " + SystemClock.elapsedRealtime());
        initSurface();

        new Thread(new Runnable() {

            @Override
            public void run() {
                initNotifacation();
                bindPhotoService();
                mOrientationEventListener = new CameraOrientationEventListener(GnBlindShootActivity.this);
                initCamera();

            }
        }).start();

    }

    @SuppressWarnings("deprecation")
    private void initSurface() {
        mSurfaceView = (SurfaceView) findViewById(R.id.camera_surfaceview);

        mHolder = mSurfaceView.getHolder();
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }

    private void initCamera() {

        if (checkCameraHardware(getApplicationContext())) {
            if (openFacingBackCameraPrior()) {
                Log.d(LOG_TAG, "openCameraSuccess");
                Log.d("jings", "initCamera: " + SystemClock.elapsedRealtime());
                autoFocus();
            } else {
                Log.d(LOG_TAG, "openCameraFailed");
                finish();
            }

        } else {
            finish();
        }
    }

    public static void setOrientation(int cameraId, Camera camera, Parameters parameters) {
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        Log.d(LOG_TAG, "info.orientation=" + info.orientation);

        int degrees = 0;
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            degrees = 90;
            parameters.setRotation(90);
        } else if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            degrees = 90;
            parameters.setRotation(270);
        }
        Log.d(LOG_TAG, "setOrientation(), degrees=" + degrees);

        camera.setDisplayOrientation(degrees);
        camera.setParameters(parameters);
    }

    private void initParameters() {
        try {

            if (mCamera != null) {
                mParameters = mCamera.getParameters();
                setOrientation(mCameraId, mCamera, mParameters);

                List<Size> supportedPreviewSizes = mParameters.getSupportedPreviewSizes();
                Size targetSize = mCamera.new Size(0, 0);
                int indexSize = supportedPreviewSizes.size();
                for (int i = 0; i < indexSize; i++) {
                    Size size = supportedPreviewSizes.get(i);
                    if (size.width > targetSize.width) {
                        targetSize.width = size.width;
                        targetSize.height = size.height;
                        Log.d(LOG_TAG, "size width: " + targetSize.width + "  height: " + targetSize.height);
                    }
                }

                mParameters.setPreviewSize(targetSize.width, targetSize.height);
                mParameters.setPictureSize(targetSize.width, targetSize.height);
                mParameters.setFlashMode(Parameters.FLASH_MODE_OFF);
                mParameters.setPictureFormat(ImageFormat.JPEG);
                mCamera.setParameters(mParameters);
            }
        } catch (Exception e) {
            e.printStackTrace();
            releaseCamera();
            finish();
        }
    }

    private void bindPhotoService() {
        Log.d(LOG_TAG, "bindPhotoService()");
        if (mPhotoService == null || mServiceConn == null) {
            mServiceConn = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Log.d(LOG_TAG, "photo service is connected");
                    mPhotoService = IPhotoService.Stub.asInterface(service);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.d(LOG_TAG, "photo service is disconnected");
                    mPhotoService = null;
                }
            };

            Log.d(LOG_TAG, IPhotoService.class.getName());
            Intent intent = new Intent();
//            intent.setAction("com.android.systemui.controlcenter.service.IPhotoService");
            intent.setClassName("com.android.systemui", "com.android.systemui.gionee.cc.service.GnShortCutServices");
            bindService(intent, mServiceConn, Context.BIND_AUTO_CREATE);
        }
    }

    private void autoFocus() {
        // SystemClock.sleep(2000);
        // mCamera.autoFocus(mAutoFocus);
        Log.d(LOG_TAG, "autoFocus: " + SystemClock.elapsedRealtime());
        try {
            mCamera.takePicture(null, null, mPicCallback);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            // has camera
            return true;
        } else {
            // has no camera
            return false;
        }
    }

    private boolean openFacingBackCameraPrior() {

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int camIdx = 0, cameraCount = Camera.getNumberOfCameras(); camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                try {
                    Log.d(LOG_TAG, "tryToOpenCamera");
                    mCamera = Camera.open(camIdx);
                    mCameraId = cameraInfo.facing;
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }

        if (mCamera == null) {
            for (int camIdx = 0, cameraCount = Camera.getNumberOfCameras(); camIdx < cameraCount; camIdx++) {
                Camera.getCameraInfo(camIdx, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    try {
                        mCamera = Camera.open(camIdx);
                        mCameraId = cameraInfo.facing;
                    } catch (RuntimeException e) {
                        return false;
                    }
                }
            }
        }

        try {
            mCamera.setPreviewDisplay(mHolder);
        } catch (Exception e) {
            e.printStackTrace();
            releaseCamera();
        }
        initParameters();
        try {
            mCamera.startPreview();
        } catch (RuntimeException e) {
            e.printStackTrace();
            releaseCamera();
        }
        registerOrientationEventListener();
        return true;
    }

    private AutoFocusCallback mAutoFocus = new AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {
                // camera.takePicture(null, null, mPicCallback);
            }
        }
    };

    private PictureCallback mPicCallback = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d("jings", "onPictureTaken: " + SystemClock.elapsedRealtime());
            GnBlindShootActivity.this.finish();
            unregisterOrientationEventListener();
            storeImage(data);
            GnVibrateUtil.vibrate(GnBlindShootActivity.this);
            releaseCamera();
            Log.d("jings", "onPictureTakened: " + SystemClock.elapsedRealtime());
        }
    };
    
    //TODO:查看是否是异步线程，改成异步线程
    public int storeImage(byte[] data) {
        Log.i(LOG_TAG, "storeImage() begin  threadname: "+Thread.currentThread().getName());
        int result = 0;
        try {
            if (mPhotoService != null) {
                int[] degree = new int[1];
                Uri uri = mPhotoService.savePic(degree, data);
                Log.i(LOG_TAG, "uri is null: " + (uri == null));
                if (uri != null) {
                    Log.i(LOG_TAG, "uri: " + uri.getPath());
                    result = degree[0];
                    sendNotification(uri);
                } else {
                    Log.d(LOG_TAG, "fail to obtain uri");
                }
            } else {
                Log.d(LOG_TAG, "mPhotoService is null");
            }
        } catch (Exception ex) {
            Log.e(LOG_TAG, "fail to store image");
        }

        Log.i(LOG_TAG, "storeImage() end");
        return result;
    }

    private Bitmap createThumbnail(byte[] data, Size s) {
        int ratio = (int) Math.ceil((double) s.width / 80);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = Integer.highestOneBit(ratio);

        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        } catch (OutOfMemoryError e) {
            Log.e(LOG_TAG, "fail to create bitmap");
        }

        return bitmap;
    }

    @SuppressLint("NewApi")
    private void initNotifacation() {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        CharSequence tickerText = getResources().getString(R.string.gn_camera_blind_shoot_success);
        CharSequence contentTitle = getResources().getString(R.string.gn_camera_blind_shoot_success);
        CharSequence contentText = getResources().getString(R.string.gn_camera_blind_shoot_open);

        // use builder to replace Notification.setLatestEventInfo()
        Notification.Builder builder = new Notification.Builder(this);
        mNotification = builder.setContentTitle(contentTitle).setContentText(contentText).setTicker(tickerText)
                .setSmallIcon(R.drawable.gn_sc_contact_pic).build();
        mNotification.flags = (Notification.FLAG_AUTO_CANCEL);
    }

    private void sendNotification(Uri uri) {
        if (mNotification != null) {
            mNotificationManager.cancel(NOTIFICATON_ID);
            Intent intent = new Intent(this, GnNotificationHandleActivity.class); 
            intent.putExtra(GnAppConstants.BLIND_SHOOT_URI_INTENT_KEY, uri);
            PendingIntent contentIntent=PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mNotification.contentIntent=contentIntent;
            mNotificationManager.notify(NOTIFICATON_ID, mNotification);
        }
    }

    private synchronized void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            mParameters = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
        unbindService(mServiceConn);
    }

    private void registerOrientationEventListener() {
        Log.d(LOG_TAG, "registerOrientationEventListener()");
        if (mOrientationEventListener.canDetectOrientation()) {
            mOrientationEventListener.enable();
        }
    }

    private void unregisterOrientationEventListener() {
        Log.d(LOG_TAG, "unregisterOrientationEventListener()");
        mOrientationEventListener.disable();
    }

    private class CameraOrientationEventListener extends OrientationEventListener {
        public CameraOrientationEventListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {

            if (Math.abs(orientation - mLastOrientation) < 30) {
                return;
            }
            mLastOrientation = orientation;
            Log.d(LOG_TAG, "orientation changed to " + mLastOrientation);

            orientation = (orientation + 45) / 90 * 90;
            if (mCamera != null) {
                CameraInfo info = new CameraInfo();
                Camera.getCameraInfo(mCameraId, info);

                int rotation = (info.orientation - orientation + 360) % 360;
                if (mLastRotation != rotation) {
                    Log.d(LOG_TAG, info.orientation + "/" + orientation);
                    Log.d(LOG_TAG, "set rotation to " + rotation);
                    mLastRotation = rotation;

                    // }
                    if (mCameraId == CameraInfo.CAMERA_FACING_FRONT) {
                        if (rotation == 0) {
                            rotation = 180;
                        } else if (rotation == 180) {
                            rotation = 0;
                        } else if (rotation == 270) {
                            rotation = 180;
                        } else if (rotation == 90) {
                            rotation = 270;
                        }
                    } else { // back facing
                        if (rotation == 0) {
                            rotation = 180;
                        } else if (rotation == 180) {
                            rotation = 0;
                        } else if (rotation == 270) {
                            rotation = 270;
                        } else if (rotation == 90) {
                            rotation = 90;
                        }
                    }
                    Log.d(LOG_TAG, "2 - set rotation to " + rotation);

                    try {
                        mParameters.setRotation(rotation);
                        mCamera.setParameters(mParameters);
                    } catch (Exception e) {
                        Log.d(LOG_TAG, "fail to invoke Camera.setParameters()");
                    }
                }
            }
        }
    }
}
