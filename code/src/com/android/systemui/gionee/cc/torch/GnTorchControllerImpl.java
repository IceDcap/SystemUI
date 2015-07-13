package com.android.systemui.gionee.cc.torch;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.android.systemui.gionee.cc.torch.GnTorch.GnUpdateUiStateCallback;

import android.app.ActivityManager;
import android.app.Service;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

public final class GnTorchControllerImpl implements GnTorchController {

    private static final String TAG = "GnTorchHelper";
    private static final String FLASH_LIGHT_PKG_NAME = "com.gionee.flashlight";

    private static GnTorchControllerImpl mInstance = null;

    private Context mContext;
    private GnTorchManager mTorchManager;
    private GnTorch mTorch;

    private SensorManager mSensorManager;
    private TorchSensorListener mSensorEventListener;
    
    private final ArrayList<Callback> mCallbacks = new ArrayList<Callback>();    

    private static final int MSG_START_FLASH_ONLY = 0;
    private static final int MSG_STOP_FLASH_ONLY = 1;
    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START_FLASH_ONLY:
                    mTorch.on();
                    break;
                case MSG_STOP_FLASH_ONLY:
                    mTorch.off();
                    break;
                default:
                    break;
            }
        }
    };
    
    @Override
    public boolean isTorchOn() {
        return mTorch.getTorchState();
    }

    @Override
    public void addStateChangedCallback(Callback cb) {
        mCallbacks.add(cb);        
    }

    @Override
    public void removeStateChangedCallback(Callback cb) {
        mCallbacks.remove(cb);        
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String actions = intent.getAction();
            if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(actions)) {
                TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Service.TELEPHONY_SERVICE);
                if (tm.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK) {
                    if (mSensorEventListener == null) {
                        Log.d(TAG, "registerProximitySensor()");
                        mSensorEventListener = new TorchSensorListener();
                        mSensorManager.registerListener(mSensorEventListener,
                                mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY),
                                SensorManager.SENSOR_DELAY_NORMAL);
                    }
                } else if (tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                    if (mSensorEventListener != null) {
                        Log.d(TAG, "unregisterProximitySensor()");
                        mSensorManager.unregisterListener(mSensorEventListener);
                        mSensorEventListener = null;
                    }
                }
            }
        }
    };

    private class TorchSensorListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                float proximity = event.values[0];
                if (proximity <= 0.0) {
                    Log.d(TAG, "onSensorChanged stop torch");
                    mHandler.sendEmptyMessage(MSG_STOP_FLASH_ONLY);
                }
            }
        }
        
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            
        }
    }

    public GnTorchControllerImpl(Context context) {
        mContext = context;
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        mContext.registerReceiver(mReceiver, filter);

        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

        mTorchManager = GnTorchManager.getInstance(mContext);
        mTorch = mTorchManager.getTorch();
        mTorch.registerUiStateCallback(mTorchUiCallback);
        
        mContext.getContentResolver().registerContentObserver(
                Settings.Global.getUriFor("amigo_powermode"), true, mPowerModeObserver);
    }

    public static GnTorchControllerImpl getInstance(Context context) {
        mInstance = new GnTorchControllerImpl(context);
        return mInstance;
    }

    public static GnTorchControllerImpl getInstance() {
        return mInstance;
    }

    public void handleClick() {
        Log.i(TAG, "mTorch.getTorchState(): " + mTorch.getTorchState());
        operator(!mTorch.getTorchState());
    }

    public void onDestroy() {
        mTorch.unregisterUiStateCallback(mTorchUiCallback);
    }

    public synchronized void operator() {
        Log.i(TAG, "mTorch.getLightState(): " + mTorch.getTorchState());
        operator(!mTorch.getTorchState());
    }

    private synchronized void operator(boolean needOpen) {
        killTorchAppIfRunning();
        
        if (needOpen) {
            setTorchOn();
        } else {
            setTorchOff();
        }
    }
    
    public void setTorchOn() {
        mHandler.sendEmptyMessage(MSG_STOP_FLASH_ONLY);
        mHandler.sendEmptyMessageDelayed(MSG_START_FLASH_ONLY, 100);
    }

    private void setTorchOff() {
        mHandler.sendEmptyMessage(MSG_STOP_FLASH_ONLY);
    }

    private GnUpdateUiStateCallback mTorchUiCallback = new GnUpdateUiStateCallback() {

        @Override
        public void updateUiState() {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    boolean isTorchOn = mTorch.getTorchState();
                    Log.d(TAG, "updateUiState  isTorchOn: " + isTorchOn);
                    for (Callback callback : mCallbacks) {
                        callback.onTorchStateChange(isTorchOn);
                    }
                }
            });
        }
    };

    private void killTorchAppIfRunning() {
        boolean isAppRunning = getAppIsRunning(mContext, FLASH_LIGHT_PKG_NAME);
        Log.d(TAG, "killTorchAppIfRunning-------isAppRunning: " + isAppRunning);
        if (isAppRunning) {
            killApplication(mContext, FLASH_LIGHT_PKG_NAME);
        }
    }

    public void killApplication(Context context, String packageName) {
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            Method method = Class.forName("android.app.ActivityManager").getMethod(
                    "forceStopPackage", String.class);
            method.invoke(am, packageName);
            Log.d(TAG, "kill Other app " + packageName);
        } catch (Exception e) {
            Log.d(TAG, "killApplication error");
            e.printStackTrace();
        }
    }
    
    public boolean getAppIsRunning(Context context, String packageName) {
        boolean isAppRunning = false;
        
        if (TextUtils.isEmpty(packageName)) {
            return isAppRunning;
        }
        
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> runningProcess = am.getRunningAppProcesses();

        for (RunningAppProcessInfo runningAppProcessInfo : runningProcess) {
            String[] pkgList = runningAppProcessInfo.pkgList;
            for (String pkg : pkgList) {
                if (packageName.equals(pkg)) {
                    isAppRunning = true;
                }
            }
        }
        
        return isAppRunning;
    }
    
    private ContentObserver mPowerModeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            Log.d(TAG, "mPowerModeObserver selfChange=" + selfChange);
            setTorchOff();
        }
    };
}
