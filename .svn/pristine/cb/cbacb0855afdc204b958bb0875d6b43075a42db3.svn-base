/*
 * Copyright (C) 2014 The Android Open Source Project
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
 *   Gionee hanbj add for PowerSaver add 20150208
 */

package com.android.systemui.power;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Slog;
import android.view.View;

import com.android.systemui.R;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.phone.SystemUIDialog;

import java.io.PrintWriter;

public class GnPowerNotificationWarnings implements PowerUI.WarningsUI {
    private static final String TAG = PowerUI.TAG + ".Notification";
    private static final boolean DEBUG = PowerUI.DEBUG;

    private static final String TAG_NOTIFICATION = "low_battery";
    private static final int ID_NOTIFICATION = 100;

    private static final int SHOWING_NOTHING = 0;
    private static final int SHOWING_WARNING = 1;
    private static final int SHOWING_INVALID_CHARGER = 2;
    private static final String[] SHOWING_STRINGS = {
        "SHOWING_NOTHING",
        "SHOWING_WARNING",
        "SHOWING_INVALID_CHARGER",
    };

    private static final String GN_POWERSAVER_PACKAGE = "com.gionee.softmanager";
    private static final String GN_POWERSAVER_CLASS = "com.gionee.softmanager.powersaver.activities.PowerManagerMainActivity";
    private static final String POWERSAVERSETTING = "amigo_powermode";
    
    private static final AudioAttributes AUDIO_ATTRIBUTES = new AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .build();

    private final Context mContext;
    private final NotificationManager mNoMan;
    private final PowerManager mPowerMan;
    private final Handler mHandler = new Handler();

    private int mBatteryLevel;
    private int mBucket;
    private long mScreenOffTime;
    private int mShowing;

    private long mBucketDroppedNegativeTimeMs;

    private boolean mWarning;
    private boolean mPlaySound;
    private boolean mInvalidCharger;

    public GnPowerNotificationWarnings(Context context, PhoneStatusBar phoneStatusBar) {
        mContext = context;
        mNoMan = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mPowerMan = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        //mReceiver.init();
        initPowerSaverObserver();
    }

	private void initPowerSaverObserver() {
		ContentObserver obs = new ContentObserver(mHandler) {
			@Override
			public void onChange(boolean selfChange) {
				if (getPowerSaverMode() == 2) {
					mNoMan.cancelAsUser(TAG_NOTIFICATION, ID_NOTIFICATION, UserHandle.CURRENT);
				}
			}
		};
		final ContentResolver resolver = mContext.getContentResolver();
		resolver.registerContentObserver(
				Settings.Global.getUriFor(POWERSAVERSETTING), false, obs, UserHandle.USER_ALL);
	}

    @Override
    public void dump(PrintWriter pw) {
        pw.print("mWarning="); pw.println(mWarning);
        pw.print("mPlaySound="); pw.println(mPlaySound);
        pw.print("mInvalidCharger="); pw.println(mInvalidCharger);
        pw.print("mShowing="); pw.println(SHOWING_STRINGS[mShowing]);
    }

    @Override
    public void update(int batteryLevel, int bucket, long screenOffTime) {
        mBatteryLevel = batteryLevel;
        if (bucket >= 0) {
            mBucketDroppedNegativeTimeMs = 0;
        } else if (bucket < mBucket) {
            mBucketDroppedNegativeTimeMs = System.currentTimeMillis();
        }
        mBucket = bucket;
        mScreenOffTime = screenOffTime;
    }

    @Override
    public void showSaverMode(boolean mode) {

    }

    private void updateNotification() {
        if (DEBUG) Slog.d(TAG, "updateNotification mWarning=" + mWarning + " mPlaySound="
                + mPlaySound + " mInvalidCharger=" + mInvalidCharger);
        if (mInvalidCharger) {
            showInvalidChargerNotification();
            mShowing = SHOWING_INVALID_CHARGER;
        } else if (mWarning) {
            showWarningNotification();
            mShowing = SHOWING_WARNING;
        } else {
            mNoMan.cancel(TAG_NOTIFICATION, ID_NOTIFICATION);
            mShowing = SHOWING_NOTHING;
        }
    }

    private void showInvalidChargerNotification() {
        final Notification.Builder nb = new Notification.Builder(mContext)
                .setSmallIcon(R.drawable.ic_power_low)
                .setWhen(0)
                .setShowWhen(false)
                .setOngoing(true)
                .setContentTitle(mContext.getString(R.string.invalid_charger_title))
                .setContentText(mContext.getString(R.string.invalid_charger_text))
                .setPriority(Notification.PRIORITY_MAX)
                .setCategory(Notification.CATEGORY_SYSTEM)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setColor(mContext.getResources().getColor(
                        com.android.internal.R.color.system_notification_accent_color));
        final Notification n = nb.build();
        if (n.headsUpContentView != null) {
            n.headsUpContentView.setViewVisibility(com.android.internal.R.id.right_icon, View.GONE);
        }
        mNoMan.notifyAsUser(TAG_NOTIFICATION, ID_NOTIFICATION, n, UserHandle.CURRENT);
    }

    private void showWarningNotification() {
    	if (getPowerSaverMode() == 2) {
    		return;
    	}
    	
        final int textRes = R.string.battery_low_percent_format;
        final Notification.Builder nb = new Notification.Builder(mContext)
                .setSmallIcon(R.drawable.ic_power_low)
                // Bump the notification when the bucket dropped.
                .setWhen(mBucketDroppedNegativeTimeMs)
                .setShowWhen(false)
                .setContentTitle(mContext.getString(R.string.battery_low_title))
                .setContentText(mContext.getString(textRes, mBatteryLevel))
                .setOnlyAlertOnce(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setCategory(Notification.CATEGORY_SYSTEM)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setColor(mContext.getResources().getColor(
                        com.android.internal.R.color.battery_saver_mode_color));

        if (getPowerSaverMode() == 0) {
        	Intent intent = new Intent();
        	intent.putExtra("powerWarning", "powerSaveMode");
        	intent.setClassName(GN_POWERSAVER_PACKAGE, GN_POWERSAVER_CLASS);
            nb.addAction(0,
                    mContext.getString(R.string.gn_powersaver),
                    pendingActivity(intent));
        }

        if (mPlaySound) {
            attachLowBatterySound(nb);
            mPlaySound = false;
        }
        final Notification n = nb.build();
        if (n.headsUpContentView != null) {
            n.headsUpContentView.setViewVisibility(com.android.internal.R.id.right_icon, View.GONE);
        }
        mNoMan.notifyAsUser(TAG_NOTIFICATION, ID_NOTIFICATION, n, UserHandle.CURRENT);
    }

    private int getPowerSaverMode() {
    	return Settings.Global.getInt(mContext.getContentResolver(), POWERSAVERSETTING, 0);
    }
    
    private PendingIntent pendingActivity(Intent intent) {
        return PendingIntent.getActivityAsUser(mContext,
                0, intent, 0, null, UserHandle.CURRENT);
    }

    @Override
    public boolean isInvalidChargerWarningShowing() {
        return mInvalidCharger;
    }

    @Override
    public void updateLowBatteryWarning() {
        updateNotification();
    }

    @Override
    public void dismissLowBatteryWarning() {
        if (DEBUG) Slog.d(TAG, "dismissing low battery warning: level=" + mBatteryLevel);
        dismissLowBatteryNotification();
    }

    private void dismissLowBatteryNotification() {
        if (mWarning) Slog.i(TAG, "dismissing low battery notification");
        mWarning = false;
        updateNotification();
    }

    @Override
    public void showLowBatteryWarning(boolean playSound) {
        Slog.i(TAG,
                "show low battery warning: level=" + mBatteryLevel
                + " [" + mBucket + "] playSound=" + playSound);
        mPlaySound = playSound;
        mWarning = true;
        updateNotification();
    }

    private void attachLowBatterySound(Notification.Builder b) {
        final ContentResolver cr = mContext.getContentResolver();

        final int silenceAfter = Settings.Global.getInt(cr,
                Settings.Global.LOW_BATTERY_SOUND_TIMEOUT, 0);
        final long offTime = SystemClock.elapsedRealtime() - mScreenOffTime;
        if (silenceAfter > 0
                && mScreenOffTime > 0
                && offTime > silenceAfter) {
            Slog.i(TAG, "screen off too long (" + offTime + "ms, limit " + silenceAfter
                    + "ms): not waking up the user with low battery sound");
            return;
        }

        if (DEBUG) {
            Slog.d(TAG, "playing low battery sound. pick-a-doop!"); // WOMP-WOMP is deprecated
        }

        if (Settings.Global.getInt(cr, Settings.Global.POWER_SOUNDS_ENABLED, 1) == 1) {
            final String soundPath = Settings.Global.getString(cr,
                    Settings.Global.LOW_BATTERY_SOUND);
            if (soundPath != null) {
                final Uri soundUri = Uri.parse("file://" + soundPath);
                if (soundUri != null) {
                    b.setSound(soundUri, AUDIO_ATTRIBUTES);
                    if (DEBUG) Slog.d(TAG, "playing sound " + soundUri);
                }
            }
        }
    }

    @Override
    public void dismissInvalidChargerWarning() {
        dismissInvalidChargerNotification();
    }

    private void dismissInvalidChargerNotification() {
        if (mInvalidCharger) Slog.i(TAG, "dismissing invalid charger notification");
        mInvalidCharger = false;
        updateNotification();
    }

    @Override
    public void showInvalidChargerWarning() {
        mInvalidCharger = true;
        updateNotification();
    }
	
	@Override
    public void userSwitched() {
        updateNotification();
    }

}
