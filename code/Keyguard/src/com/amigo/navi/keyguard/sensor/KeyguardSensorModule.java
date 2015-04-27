package com.amigo.navi.keyguard.sensor;

import java.lang.reflect.Field;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.KeyguardViewHostManager;
//import com.gionee.navi.keyguard.amigo.util.DataStatistics;

import amigo.provider.AmigoSettings;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class KeyguardSensorModule implements SensorEventListener {

	private static final String TAG_SENSOR_MODULE = "NaviKg_sensormodule";
	private Sensor mBrush;
	private boolean mIsReg;
	private SensorManager mSensorManager;
	private static KeyguardSensorModule mInstance = null;
	private Context mContext = null;
	private static final int TYPE_UNAVAILABLE = Sensor.TYPE_ACCELEROMETER - 1;
	private static final int SENSOREVENT_UNLOCK_VALUE = 1;
	private static final int REGISTER_REYRY_INIT_FAIL_COUNT = 5;
	private int mSensorType = TYPE_UNAVAILABLE;
	private int mRegisterInitFailCount = -1;
	
//	private static final String SENSOR_MASTER_SWITCH = "gn_dg_switch";
//	private static final String SENSOR_SWITCH = "sdg_unlock";
//	private int mSensorSwitch = 0;
	private static final int SENSOR_SWITCH_ON = 1;
	private static final int SENSOR_SWITCH_OFF = 0;

	public static KeyguardSensorModule getInstance(Context context) {
		// TODO Auto-generated method stub
		if (mInstance == null) {
			mInstance = new KeyguardSensorModule(context);
		}
		return mInstance;
	}

	private KeyguardSensorModule(Context context) {
		if(DebugLog.DEBUG) DebugLog.d(TAG_SENSOR_MODULE, "KeyguardSensorModule()");
		mContext = context;
//		mContext.getContentResolver().registerContentObserver(
//				AmigoSettings.getUriFor(SENSOR_SWITCH), true, mSensorObserver);
//		mSensorSwitch = AmigoSettings.getInt(mContext.getContentResolver(),SENSOR_SWITCH, SENSOR_SWITCH_OFF);
		init();
		mIsReg = false;
	}

	private void init() {
		try {
			if(DebugLog.DEBUG) DebugLog.d(TAG_SENSOR_MODULE, "reflect get sensorType start");
			Class ownerClass = Class.forName("android.hardware.Sensor");
			Field field = ownerClass.getField("TYPE_BRUSH");
			mSensorType = (Integer) field.get(ownerClass);
			if(DebugLog.DEBUG) DebugLog.d(TAG_SENSOR_MODULE, "reflect get sensorType end sensorType is:"
					+ mSensorType);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			mRegisterInitFailCount += 1;
			DebugLog.d(TAG_SENSOR_MODULE, "reflect get sensorType exception:"
					+ e.toString()+"---"+"retrycount:"+mRegisterInitFailCount);
		}
		mSensorManager = (SensorManager) mContext
				.getSystemService(Context.SENSOR_SERVICE);
		mBrush = mSensorManager.getDefaultSensor(mSensorType);
		if(DebugLog.DEBUG) DebugLog.d(TAG_SENSOR_MODULE, "mBrush:" + mBrush);
		
	}

	private void verifyOrUnregisterSensorListener() {
		if (mIsReg) {
			if(DebugLog.DEBUG) DebugLog.d(TAG_SENSOR_MODULE, "unregisterListener() start mIsReg "
					+ mIsReg);
			mSensorManager.unregisterListener(this);
			if(DebugLog.DEBUG) DebugLog.d(TAG_SENSOR_MODULE, "unregisterListener() end");
			mIsReg = false;
		}
	}

	private void verifyOrRegisterSensorListener() {
		if(mSensorType == TYPE_UNAVAILABLE && mRegisterInitFailCount <= REGISTER_REYRY_INIT_FAIL_COUNT){
			init();
		}

		int masterSensorSwitch = AmigoSettings.getInt(mContext.getContentResolver(),AmigoSettings.GN_DG_SWITCH, SENSOR_SWITCH_OFF);
		int sensorSwitch = AmigoSettings.getInt(mContext.getContentResolver(),AmigoSettings.SDG_UNLOCK, SENSOR_SWITCH_OFF);
		
		if(DebugLog.DEBUG) DebugLog.d(TAG_SENSOR_MODULE, "registerListener mIsReg " + mIsReg+"---masterSensorSwitch:"+masterSensorSwitch+"---sensorSwitch:"+sensorSwitch);
		
		if (masterSensorSwitch == SENSOR_SWITCH_ON && sensorSwitch == SENSOR_SWITCH_ON && mSensorType != TYPE_UNAVAILABLE && !mIsReg) {
			
			if(DebugLog.DEBUG) DebugLog.d(TAG_SENSOR_MODULE, "registerListener() start"+"---object of Sensor is null？:"+(mBrush == null));
			mIsReg = mSensorManager.registerListener(this,
					mBrush, SensorManager.SENSOR_DELAY_NORMAL);
			
			if(DebugLog.DEBUG) DebugLog.d(TAG_SENSOR_MODULE, "registerListener() end" + "--"
					+ "registerListener successfully?  " + mIsReg);
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		int value = (int) event.values[0];
		if(DebugLog.DEBUG) DebugLog.d(TAG_SENSOR_MODULE, "onSensorChanged() value:"+value);
		if (value == SENSOREVENT_UNLOCK_VALUE) {
			KeyguardViewHostManager.getInstance().unLockBySensor();
//			DataStatistics.getInstance().gestureUnlock(mContext);
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}
	
//	private ContentObserver mSensorObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
//
//        @Override
//        public boolean deliverSelfNotifications() {
//            return super.deliverSelfNotifications();
//        }
//
//        @Override
//        public void onChange(boolean selfChange) {
//            mSensorSwitch = AmigoSettings.getInt(mContext.getContentResolver(),
//                    SENSOR_SWITCH, SENSOR_SWITCH_OFF);
//          if(DebugLog.DEBUG)  DebugLog.d(TAG_SENSOR_MODULE, "ContentObserver onChange() SensorSwitchState:"+mSensorSwitch);
//           if(mSensorSwitch == SENSOR_SWITCH_OFF){
//        	   verifyOrUnregisterSensorListener();
//           }else{
//        	   verifyOrRegisterSensorListener();
//           }
//        }
//
//    };
    
	public void registerListener() {
		verifyOrRegisterSensorListener();
	}

	public void unRegisterListener() {
		verifyOrUnregisterSensorListener();
//		mContext.getContentResolver()
//				.unregisterContentObserver(mSensorObserver);
	}

}
