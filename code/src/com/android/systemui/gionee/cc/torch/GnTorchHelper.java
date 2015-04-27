package com.android.systemui.gionee.cc.torch;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.R;
import com.android.systemui.gionee.cc.fakecall.GnUpdateUiStateCallback;

public final class GnTorchHelper {

    private static final String LOG_TAG="FlashLightHelper";
    private static final int INVALID_RES_ID=-1;
    private static final int MSG_START_FLASH = 0;
    private static final int MSG_STOP_FLASH = 1;
    private static final int MSG_START_FLASH_ONLY = 2;
    private static final int MSG_STOP_FLASH_ONLY = 3;
    private static final int ON = 1;
    private static final int OFF = 0;
    
    private static final String FLASH_LIGHT_PKG_NAME="com.gionee.flashlight";

    private Context mContext;
    private final FlashHandler mHandler = new FlashHandler();
    private GnLightManager mLightManager;
    private GnLight mLight;
    private View mTorchContainer=null;
    private ImageView mTorchImageView;
    private TextView mTorchTextView;
    private  static GnTorchHelper mInstance=null;
    
    private SensorManager mSensorMgr;
    private mySensorEventListener mSensorEventListener;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String actions = intent.getAction();
            if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(actions)) {
                TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Service.TELEPHONY_SERVICE);
                if (tm.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK) {
                    if (mSensorEventListener == null) {
                        Log.d(LOG_TAG, "registerProximitySensor()");
                        mSensorEventListener = new mySensorEventListener();
                        mSensorMgr.registerListener(mSensorEventListener,
                                mSensorMgr.getDefaultSensor(Sensor.TYPE_PROXIMITY),
                                SensorManager.SENSOR_DELAY_NORMAL);
                    }
                } else if (tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                    if (mSensorEventListener != null) {
                        Log.d(LOG_TAG, "unregisterProximitySensor()");
                        mSensorMgr.unregisterListener(mSensorEventListener);
                        mSensorEventListener = null;
                    }
                }
            }
        }
        
    };
    
    private class mySensorEventListener implements SensorEventListener {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                float proximity = event.values[0];
                if (proximity <= 0.0) {
                    Log.d(LOG_TAG, "onSensorChanged MSG_STOP_FLASH");
                    mHandler.sendEmptyMessage(MSG_STOP_FLASH);
                }
            }
        }
    }

    public GnTorchHelper(Context context,View container){
        onCreate(context,container);
        
        mContext = context;
        mSensorMgr = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        mContext.registerReceiver(mReceiver, filter);
    } 

    public static GnTorchHelper getInstance(Context context, View container) {
        mInstance = new GnTorchHelper(context, container);
        return mInstance;
    }
    //TODO： 线程安全问题
    public static GnTorchHelper getInstance(){
        return mInstance;
    }

    public void onCreate(Context context,View container) {
        mLightManager = GnLightManager.getInstance(context);
//      mLightManager = new LightManager(context);
        mLight = mLightManager.getLight();
        mLight.registerUiStateCallback(mLightUiCallback);
        mTorchContainer=container;
        mTorchImageView=(ImageView)mTorchContainer.findViewById(R.id.sc_torch);
        mTorchTextView=(TextView)mTorchContainer.findViewById(R.id.sc_torch_txt);

        updateUiState();
    }
    

    public void updateUiState(){
        if (mLight.getLightState()) {
            setOnView();
        } else {
            setOffView();
        }
    }
    
    public void onPhoneStateChanged(String state) {
        if (!mLight.getLightState()) {
            return;
        }
        
        if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
            mHandler.sendEmptyMessage(MSG_START_FLASH);
        } else {
            mHandler.sendEmptyMessage(MSG_STOP_FLASH);
        }
    }

    public void onNextClick() {
        operator();
    }

    public void onDestroy() {
        mLight.unregisterUiStateCallback(mLightUiCallback);
    }

    public synchronized void operator() {
        Log.i(LOG_TAG, "mLight.getLightState(): " + mLight.getLightState());
        operator(!mLight.getLightState());
    }

    private synchronized void operator(boolean flag) {
        killFlashLightAppIfRunning();
        if (flag) {
            setFlashOn();
        } else {
            setFlashOff();
        }
    }

    private void killFlashLightAppIfRunning() {
        boolean isAppRunning=GnShortcutUtils.getAppIsRunning(mTorchContainer.getContext(), FLASH_LIGHT_PKG_NAME);
        Log.d(LOG_TAG, "killFlashLightAppIfRunning-------isAppRunning: "+isAppRunning);
        if (isAppRunning) {
            GnShortcutUtils.killApplication(mTorchContainer.getContext(), FLASH_LIGHT_PKG_NAME);
        }
    }

    private void setFlashOff() {
        mLight.off();
        setOffView();
    }

    private void setOffView() {
        setClickView(INVALID_RES_ID, R.drawable.gn_ic_sc_torch_off);
    }

    public void setFlashOn() {
        mHandler.sendEmptyMessage(MSG_STOP_FLASH_ONLY);
        mHandler.sendEmptyMessageDelayed(MSG_START_FLASH_ONLY, 100);
        setOnView();
    }

    private void setOnView() {
        setClickView(INVALID_RES_ID, R.drawable.gn_ic_sc_torch_on);
    }

    private void setClickView(int stringId, int drawableId) {
        if(stringId!=INVALID_RES_ID){
            mTorchTextView.setText(stringId);
        }
        if(drawableId!=INVALID_RES_ID){
            mTorchImageView.setImageResource(drawableId);
        }
    }
    
    private GnUpdateUiStateCallback mLightUiCallback = new GnUpdateUiStateCallback() {

        @Override
        public void updateUiState() {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    boolean isLightOn = mLight.getLightState();
                    Log.d(LOG_TAG, "updateUiState  isLightOn: " + isLightOn);
                    if (isLightOn) {
                        setOnView();
                    } else {
                        setOffView();
                    }
                }
            });

        }
    };
    class FlashHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
            case MSG_START_FLASH:
                operator(true);
                break;
            case MSG_STOP_FLASH: 
                operator(false);
                break;
            
            case MSG_START_FLASH_ONLY:
                mLight.on();
                break;
            case MSG_STOP_FLASH_ONLY:
                mLight.off();
                break;
            default:
                break;
            }
        }
    }

}
