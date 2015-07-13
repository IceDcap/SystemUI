package com.amigo.navi.keyguard.modules;

import static android.os.BatteryManager.BATTERY_HEALTH_UNKNOWN;
import static android.os.BatteryManager.BATTERY_STATUS_FULL;
import static android.os.BatteryManager.BATTERY_STATUS_UNKNOWN;
import static android.os.BatteryManager.EXTRA_HEALTH;
import static android.os.BatteryManager.EXTRA_LEVEL;
import static android.os.BatteryManager.EXTRA_PLUGGED;
import static android.os.BatteryManager.EXTRA_STATUS;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.BatteryStats;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.format.Formatter;

import com.android.internal.app.IBatteryStats;
import com.android.keyguard.R;
import com.amigo.navi.keyguard.DebugLog;


public class AmigoBatteryStatus {
	private static final String LOG_TAG = "BatteryStatus";
	/**
     * M: Change the threshold to 16 for mediatek device
     */
    static final int LOW_BATTERY_THRESHOLD = 10;
	private static IBatteryStats mBatteryInfo;
    
    public int status;
    public int level;
    private int plugged;
//    private int health;
    
    public AmigoBatteryStatus(int status, int level, int plugged, int health) {
    	getmBatteryInfo();
        this.status = status;
        this.level = level;
        this.plugged = plugged;
//        this.health = health;
    }

	private static void getmBatteryInfo() {
		if(mBatteryInfo == null){
    		mBatteryInfo = IBatteryStats.Stub.asInterface(ServiceManager.getService(BatteryStats.SERVICE_NAME));
    	}
	}
    
	public static AmigoBatteryStatus fromIntent(Intent intent) {
		int status = intent.getIntExtra(EXTRA_STATUS, BATTERY_STATUS_UNKNOWN);
		int level = intent.getIntExtra(EXTRA_LEVEL, 0);
		int plugged = intent.getIntExtra(EXTRA_PLUGGED, 0);
		int health = intent.getIntExtra(EXTRA_HEALTH, BATTERY_HEALTH_UNKNOWN);

		return new AmigoBatteryStatus(status, level, plugged, health);
	}

    /**
     * Determine whether the device is plugged in (USB, power, or wireless).
     * @return true if the device is plugged in.
     */
    public boolean isPluggedIn() {
    	// Gionee <jiangxiao> <2013-11-25> modify for CR00957029 begin
    	// check status before plugged
    	boolean isPlugged = false;
    	if(status ==  BatteryManager.BATTERY_STATUS_CHARGING 
    			|| status ==  BatteryManager.BATTERY_STATUS_FULL) {
    		isPlugged = (plugged == BatteryManager.BATTERY_PLUGGED_AC
    				|| plugged == BatteryManager.BATTERY_PLUGGED_USB
    				|| plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS);	
    	} 

    	return isPlugged;
    	// Gionee <jiangxiao> <2013-11-25> modify for CR00957029 end
    }

    /**
     * Whether or not the device is charged. Note that some devices never return 100% for
     * battery level, so this allows either battery level or status to determine if the
     * battery is charged.
     * @return true if the device is charged
     */
    public boolean isCharged() {
        return status == BATTERY_STATUS_FULL || level >= 100;
    }

    /**
     * Whether battery is low and needs to be charged.
     * @return true if battery is low
     */
    public boolean isBatteryLow() {
        return level < LOW_BATTERY_THRESHOLD;
    }
	
	// methods for KeyguardDefaultPager begin
	
	// Gionee <jiangxiao> <2013-10-23> add for CR00924297 begin
	public String getBatteryInfoText(Context context) {
		String text = "";
		if(isPluggedIn()) {
			if(isCharged()) {
				text = context.getString(R.string.amigo_keyguard_charged);
			} else {
				/*try {
					long chargingTimeRemaining = mBatteryInfo.computeChargeTimeRemaining();
					if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "chargingTimeRemaining:"+chargingTimeRemaining);
					if (chargingTimeRemaining > 0) {
		                String chargingTimeFormatted =
		                        Formatter.formatShortElapsedTime(context, chargingTimeRemaining);
		                text = context.getResources().getString(
		                        R.string.amigo_keyguard_indication_charging_time, chargingTimeFormatted);
		            }else{
		            	 text = context.getResources().getString(R.string.amigo_keyguard_plugged_in);
//		            	text = context.getString(R.string.amigo_keyguard_plugged_in_percent, level);
		            }
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
				text = context.getString(R.string.lockscreen_plugged_in, level);
			}
		} else if(isBatteryLow()) {
			text = context.getString(R.string.amigo_keyguard_low_battery);
		}
		
		if(DebugLog.DEBUGMAYBE) DebugLog.d(LOG_TAG, "getBatteryInfo(): " + text);
		return text;
	}
	// Gionee <jiangxiao> <2013-10-23> add for CR00924297 end
	
	public int getBatteryInfoTextColor(Context context) {
		int colorId = R.color.keyguard_battery_color;
		if(isBatteryLow()) {
			colorId = R.color.keyguard_low_battery_color;
		}
		return context.getResources().getColor(colorId);
	}
	
	// methods for KeyguardDefaultPager end
}
