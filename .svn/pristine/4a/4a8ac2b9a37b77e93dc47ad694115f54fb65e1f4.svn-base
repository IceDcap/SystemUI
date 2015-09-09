/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.power;

import amigo.provider.AmigoSettings;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.util.Slog;
import com.android.systemui.R;
import com.android.systemui.SystemUI;
import com.android.systemui.gionee.nc.GnNotificationService;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.NotificationData.Entry;
import com.android.systemui.statusbar.phone.PhoneStatusBar;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class PowerUI extends SystemUI {
    static final String TAG = "PowerUI";
    static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);

    private final Handler mHandler = new Handler();
    private final Receiver mReceiver = new Receiver();

    private PowerManager mPowerManager;
    private WarningsUI mWarnings;
    private int mBatteryLevel = 100;
    private int mBatteryStatus = BatteryManager.BATTERY_STATUS_UNKNOWN;
    private int mPlugType = 0;
    private int mInvalidCharger = 0;

    private int mLowBatteryAlertCloseLevel;
    private final int[] mLowBatteryReminderLevels = new int[2];

    private long mScreenOffTime = -1;
    private GnNotificationService mNotificationService = null;

    private final String AMIGO_LOW_BATTERY_ALERT_VALUE = "low_battery_alert_value";

    public void start() {
        mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mScreenOffTime = mPowerManager.isScreenOn() ? -1 : SystemClock.elapsedRealtime();
        mWarnings = new GnPowerNotificationWarnings(mContext, getComponent(PhoneStatusBar.class));

        ContentObserver obs = new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange) {
                updateBatteryWarningLevels();
            }
        };
        final ContentResolver resolver = mContext.getContentResolver();
        resolver.registerContentObserver(Settings.Global.getUriFor(
                Settings.Global.LOW_POWER_MODE_TRIGGER_LEVEL),
                false, obs, UserHandle.USER_ALL);

        resolver.registerContentObserver(
                AmigoSettings.getUriFor(AMIGO_LOW_BATTERY_ALERT_VALUE), false,
                amigoObs, UserHandle.USER_ALL);

        updateBatteryWarningLevels();
        mReceiver.init();
    }

    private void setSaverMode(boolean mode) {
        mWarnings.showSaverMode(mode);
    }

    ContentObserver amigoObs = new ContentObserver(mHandler) {
        @Override
        public void onChange(boolean selfChange) {
            updateBatteryWarningLevels();
        }
    };

    void updateBatteryWarningLevels() {
		// GIONEE <wujj> <2015-03-19> modify for CR01455754 begin
		// int critLevel = mContext.getResources().getInteger(
		// 					com.android.internal.R.integer.config_criticalBatteryWarningLevel);
		int critLevel = mContext.getResources().getInteger(
				R.integer.gn_critical_battery_warning_level);
		// GIONEE <wujj> <2015-03-19> modify for CR01455754 end
        final ContentResolver resolver = mContext.getContentResolver();
        int defWarnLevel = mContext.getResources().getInteger(
                com.android.internal.R.integer.config_lowBatteryWarningLevel);
		
        defWarnLevel = AmigoSettings.getInt(resolver,
                AMIGO_LOW_BATTERY_ALERT_VALUE, defWarnLevel);
       
        int warnLevel = Settings.Global.getInt(resolver,
                Settings.Global.LOW_POWER_MODE_TRIGGER_LEVEL, defWarnLevel);
        if (warnLevel == 0) {
            warnLevel = defWarnLevel;
        }
        if (warnLevel < critLevel) {
            warnLevel = critLevel;
        }

        mLowBatteryReminderLevels[0] = warnLevel;
        mLowBatteryReminderLevels[1] = critLevel;
        mLowBatteryAlertCloseLevel = mLowBatteryReminderLevels[0]
                + mContext.getResources().getInteger(
                        com.android.internal.R.integer.config_lowBatteryCloseWarningBump);
    }

    /**
     * Buckets the battery level.
     *
     * The code in this function is a little weird because I couldn't comprehend
     * the bucket going up when the battery level was going down. --joeo
     *
     * 1 means that the battery is "ok"
     * 0 means that the battery is between "ok" and what we should warn about.
     * less than 0 means that the battery is low
     */
    private int findBatteryLevelBucket(int level) {
        if (level >= mLowBatteryAlertCloseLevel) {
            return 1;
        }
        if (level > mLowBatteryReminderLevels[0]) {
            return 0;
        }
        final int N = mLowBatteryReminderLevels.length;
        for (int i=N-1; i>=0; i--) {
            if (level <= mLowBatteryReminderLevels[i]) {
                return -1-i;
            }
        }
        throw new RuntimeException("not possible!");
    }

    private final class Receiver extends BroadcastReceiver {

        public void init() {
            // Register for Intent broadcasts for...
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_USER_SWITCHED);
            filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGING);
            filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
            mContext.registerReceiver(this, filter, null, mHandler);
            updateSaverMode();
        }

        private void updateSaverMode() {
            setSaverMode(mPowerManager.isPowerSaveMode());
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                final int oldBatteryLevel = mBatteryLevel;
                mBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 100);
                final int oldBatteryStatus = mBatteryStatus;
                mBatteryStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
                        BatteryManager.BATTERY_STATUS_UNKNOWN);
                final int oldPlugType = mPlugType;
                mPlugType = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 1);
                final int oldInvalidCharger = mInvalidCharger;
                mInvalidCharger = intent.getIntExtra(BatteryManager.EXTRA_INVALID_CHARGER, 0);

                final boolean plugged = mPlugType != 0;
                final boolean oldPlugged = oldPlugType != 0;

                int oldBucket = findBatteryLevelBucket(oldBatteryLevel);
                int bucket = findBatteryLevelBucket(mBatteryLevel);
                
                // GIONEE <wujj> <2015-03-19> modify for CR01455754 begin
                mState.level = mBatteryLevel;
                mState.updateState();
                // GIONEE <wujj> <2015-03-19> modify for CR01455754 end

                if (DEBUG) {
                    Slog.d(TAG, "buckets   ....." + mLowBatteryAlertCloseLevel
                            + " .. " + mLowBatteryReminderLevels[0]
                            + " .. " + mLowBatteryReminderLevels[1]);
                    Slog.d(TAG, "level          " + oldBatteryLevel + " --> " + mBatteryLevel);
                    Slog.d(TAG, "status         " + oldBatteryStatus + " --> " + mBatteryStatus);
                    Slog.d(TAG, "plugType       " + oldPlugType + " --> " + mPlugType);
                    Slog.d(TAG, "invalidCharger " + oldInvalidCharger + " --> " + mInvalidCharger);
                    Slog.d(TAG, "bucket         " + oldBucket + " --> " + bucket);
                    Slog.d(TAG, "plugged        " + oldPlugged + " --> " + plugged);
                }

                mWarnings.update(mBatteryLevel, bucket, mScreenOffTime);
                if (oldInvalidCharger == 0 && mInvalidCharger != 0) {
                    Slog.d(TAG, "showing invalid charger warning");
                    mWarnings.showInvalidChargerWarning();
                    return;
                } else if (oldInvalidCharger != 0 && mInvalidCharger == 0) {
                    mWarnings.dismissInvalidChargerWarning();
                } else if (mWarnings.isInvalidChargerWarningShowing()) {
                    // if invalid charger is showing, don't show low battery
                    return;
                }

                if (!plugged
                        && (bucket < oldBucket || oldPlugged || mIsBootUp)
                        && mBatteryStatus != BatteryManager.BATTERY_STATUS_UNKNOWN
                        && bucket < 0) {
                    // only play SFX when the dialog comes up or the bucket changes
                    final boolean playSound = bucket != oldBucket || oldPlugged;
                    // GIONEE <wujj> <2015-03-19> modify for CR01455754 begin
                    if (mState.shouldNotify() || oldPlugged || mIsBootUp) {
                    	mWarnings.showLowBatteryWarning(playSound);
                    	mIsBootUp = false;
                    }
                    Log.v(TAG, "onReceive: mIsBootUp = "+mIsBootUp);
                    // GIONEE <wujj> <2015-03-19> modify for CR01455754 end
                } else if (plugged || (bucket > oldBucket && bucket > 0)) {
                    mWarnings.dismissLowBatteryWarning();
                } else {
                	if (isLowBatteryShowing()) {
                		mWarnings.updateLowBatteryWarning();
                	}
                }
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                mScreenOffTime = SystemClock.elapsedRealtime();
            } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                mScreenOffTime = -1;
            } else if (Intent.ACTION_USER_SWITCHED.equals(action)) {
                mWarnings.userSwitched();
            } else if (PowerManager.ACTION_POWER_SAVE_MODE_CHANGED.equals(action)) {
                updateSaverMode();
            } else if (PowerManager.ACTION_POWER_SAVE_MODE_CHANGING.equals(action)) {
                setSaverMode(intent.getBooleanExtra(PowerManager.EXTRA_POWER_SAVE_MODE, false));
            } else {
                Slog.w(TAG, "unknown intent: " + intent);
            }
        }
    };

    // Gionee <wujj> <2015-04-28> add for CR01469943 begin
    private boolean mIsBootUp;
    @Override
    protected void onBootCompleted() {
    	super.onBootCompleted();
    	mIsBootUp = true;
    	Log.v(TAG, "onBootCompleted: mIsBootUp = "+mIsBootUp);
    }

	// Gionee <wujj> <2015-04-28> add for CR01469943 end
    
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.print("mLowBatteryAlertCloseLevel=");
        pw.println(mLowBatteryAlertCloseLevel);
        pw.print("mLowBatteryReminderLevels=");
        pw.println(Arrays.toString(mLowBatteryReminderLevels));
        pw.print("mBatteryLevel=");
        pw.println(Integer.toString(mBatteryLevel));
        pw.print("mBatteryStatus=");
        pw.println(Integer.toString(mBatteryStatus));
        pw.print("mPlugType=");
        pw.println(Integer.toString(mPlugType));
        pw.print("mInvalidCharger=");
        pw.println(Integer.toString(mInvalidCharger));
        pw.print("mScreenOffTime=");
        pw.print(mScreenOffTime);
        if (mScreenOffTime >= 0) {
            pw.print(" (");
            pw.print(SystemClock.elapsedRealtime() - mScreenOffTime);
            pw.print(" ago)");
        }
        pw.println();
        pw.print("soundTimeout=");
        pw.println(Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.LOW_BATTERY_SOUND_TIMEOUT, 0));
        pw.print("bucket: ");
        pw.println(Integer.toString(findBatteryLevelBucket(mBatteryLevel)));
        mWarnings.dump(pw);
    }

    public interface WarningsUI {
        void update(int batteryLevel, int bucket, long screenOffTime);
        void showSaverMode(boolean mode);
        void dismissLowBatteryWarning();
        void showLowBatteryWarning(boolean playSound);
        void dismissInvalidChargerWarning();
        void showInvalidChargerWarning();
        void updateLowBatteryWarning();
        boolean isInvalidChargerWarningShowing();
        void dump(PrintWriter pw);
        void userSwitched();
    }
    
    // GIONEE <wujj> <2015-03-19> modify for CR01455754 begin
    /** Low battery shows in the following case:
     * 1). Battery level is 15% or %4 when power is not plugged
     * 2). Battery level is below 15% when old state is plugged
     * */
    boolean isLowBatteryShowing() {
    	if (mNotificationService == null) {
    		mNotificationService = GnNotificationService.getService(null);
    	}
    	NotificationData notificationData = mNotificationService.getNotificationData();
    	if (notificationData != null) {
	    	ArrayList<Entry> activeNotifications = notificationData.getActiveNotifications();
	    	final int N = activeNotifications.size();
	    	for(int i = 0; i< N; i++) {
	    		Entry entry = activeNotifications.get(i);
	    		String tag = entry.notification.getTag();
	    		if ("low_battery".equals(tag)) {
	    			return true;
	    		}
	    	}
    	}
    	return false;
    }
    
    final private State mState = new State();
    class State {
    	int level = 100;
    	boolean isNotified =false;
    	
    	boolean shouldNotify() {
    		boolean notify = false;
    		if ((level == mLowBatteryReminderLevels[0]  || level == 4) && !isNotified) {
    			notify = true;
    			isNotified = notify;
    		}
    		return notify;
    	}
    	
    	void updateState() {
    		if ((level != mLowBatteryReminderLevels[0]  && level != 4) ) {
    			isNotified = false;
    		}
    	}
    }
    // GIONEE <wujj> <2015-03-19> modify for CR01455754 end
}

