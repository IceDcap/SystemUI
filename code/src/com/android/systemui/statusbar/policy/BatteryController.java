/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.provider.Settings;

import com.android.systemui.BatteryMeterView;
import com.android.systemui.R;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class BatteryController extends BroadcastReceiver {
    private static final String TAG = "BatteryController";
    private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);

    private final ArrayList<BatteryStateChangeCallback> mChangeCallbacks = new ArrayList<>();
    private final PowerManager mPowerManager;

    private int mLevel;
    private boolean mPluggedIn;
    private boolean mCharging;
    private boolean mCharged;
    private boolean mPowerSave;
    private int mShowBatteryPercentageType = 0;
    private String mBatteryPercentage = "100%";
    private String mBatteryPercent = "100";
    private int mBatteryLevel = 100;
    private boolean mIsCharge = false;
    private ArrayList<TextView> mLabelViews = new ArrayList<TextView>();
    private ArrayList<ImageView> mIconViews = new ArrayList<ImageView>();
    private Context mContext;
    public static final String BATTERY_PERCENTAGE = "battery_percentage";
    private static final String ACTION_BATTERY_PERCENTAGE_SWITCH = "mediatek.intent.action.BATTERY_PERCENTAGE_SWITCH";

    public BatteryController(Context context) {
        mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mContext = context;
        mShowBatteryPercentageType = Settings.Secure.getInt(context.getContentResolver(),
                BATTERY_PERCENTAGE, 0);
        Log.d(TAG, "BatteryController mShouldShowBatteryPercentage is "
                + mShowBatteryPercentageType);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
        filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGING);
        filter.addAction(ACTION_BATTERY_PERCENTAGE_SWITCH);
        context.registerReceiver(this, filter);

        updatePowerSave();
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("BatteryController state:");
        pw.print("  mLevel="); pw.println(mLevel);
        pw.print("  mPluggedIn="); pw.println(mPluggedIn);
        pw.print("  mCharging="); pw.println(mCharging);
        pw.print("  mCharged="); pw.println(mCharged);
        pw.print("  mPowerSave="); pw.println(mPowerSave);
    }

    public void addStateChangedCallback(BatteryStateChangeCallback cb) {
        mChangeCallbacks.add(cb);
        cb.onBatteryLevelChanged(mLevel, mPluggedIn, mCharging);
    }

    public void removeStateChangedCallback(BatteryStateChangeCallback cb) {
        mChangeCallbacks.remove(cb);
    }

    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
            mLevel = (int)(100f
                    * intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                    / intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100));
            mPluggedIn = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0;

            final int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
                    BatteryManager.BATTERY_STATUS_UNKNOWN);
            mCharged = status == BatteryManager.BATTERY_STATUS_FULL;
            mCharging = mCharged || status == BatteryManager.BATTERY_STATUS_CHARGING;
            mBatteryLevel = mLevel;
            final boolean fulled = (mLevel == 100);
            boolean plugged = false;
            switch (status) {
                case BatteryManager.BATTERY_STATUS_CHARGING:
                case BatteryManager.BATTERY_STATUS_FULL:
                    plugged = true;
                    break;
            }
            mIsCharge = plugged && !fulled;
            final int icon = mIsCharge ? R.drawable.gn_stat_sys_battery_charge 
                    : R.drawable.gn_stat_sys_battery;
            
            int m = mIconViews.size();
            for (int i = 0; i < m; i++) {
                ImageView v = mIconViews.get(i);
                v.setImageResource(icon);
                v.setImageLevel(mLevel);
                v.setContentDescription(mContext.getString(R.string.accessibility_battery_level, mLevel));
            }

            int N = mLabelViews.size();
            for (int i=0; i<N; i++) {
                TextView v = mLabelViews.get(i);
                v.setText(mContext.getString(R.string.status_bar_settings_battery_meter_format,
                        mLevel));
            }

            for (BatteryStateChangeCallback cb : mChangeCallbacks) {
                cb.onBatteryLevelChanged(mLevel, plugged, mCharging);
            }
            
            mBatteryPercentage = getBatteryPercentage(intent);
            mBatteryPercent = getBatteryPercent(intent);
            Log.d(TAG,"mBatteryPercentage is " + mBatteryPercentage + " mShouldShowBatteryPercentage is "
                    + mShowBatteryPercentageType + " mLabelViews.size() " + mLabelViews.size());

            //Gionee <hanbj> <20150208> modify for CR01447587 begin
            if (mLevel <= 15 && mShowBatteryPercentageType == 0) {
                updateBatteryPercentageView(2);
            } else {
                updateBatteryPercentageView(mShowBatteryPercentageType);
            }
            //Gionee <hanbj> <20150208> modify for CR01447587 end

            fireBatteryLevelChanged();
        } else if (action.equals(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)) {
            updatePowerSave();
        } else if (action.equals(PowerManager.ACTION_POWER_SAVE_MODE_CHANGING)) {
            setPowerSave(intent.getBooleanExtra(PowerManager.EXTRA_POWER_SAVE_MODE, false));
        } else if (action.equals(ACTION_BATTERY_PERCENTAGE_SWITCH)) {
            mShowBatteryPercentageType = intent.getIntExtra("state", 0);
            Log.d(TAG, " OnReceive ACTION_BATTERY_PERCENTAGE_SWITCH  mShouldShowBatteryPercentage" +
                    " is " + mShowBatteryPercentageType);
            if (mLevel <= 15 && mShowBatteryPercentageType == 0) {
                updateBatteryPercentageView(2);
            } else {
                updateBatteryPercentageView(mShowBatteryPercentageType);
            }
        }
    }

    public boolean isPowerSave() {
        return mPowerSave;
    }

    private void updatePowerSave() {
        setPowerSave(mPowerManager.isPowerSaveMode());
    }

    private void setPowerSave(boolean powerSave) {
        if (powerSave == mPowerSave) return;
        mPowerSave = powerSave;
        if (DEBUG) Log.d(TAG, "Power save is " + (mPowerSave ? "on" : "off"));
        firePowerSaveChanged();
    }

    private void fireBatteryLevelChanged() {
        final int N = mChangeCallbacks.size();
        for (int i = 0; i < N; i++) {
            mChangeCallbacks.get(i).onBatteryLevelChanged(mLevel, mPluggedIn, mCharging);
        }
    }

    private void firePowerSaveChanged() {
        final int N = mChangeCallbacks.size();
        for (int i = 0; i < N; i++) {
            mChangeCallbacks.get(i).onPowerSaveChanged();
        }
    }

    public interface BatteryStateChangeCallback {
        void onBatteryLevelChanged(int level, boolean pluggedIn, boolean charging);
        void onPowerSaveChanged();
    }
    
    private String getBatteryPercent(Intent intent) {
        int level = intent.getIntExtra("level", 0);
        int scale = intent.getIntExtra("scale", 100);
        return String.valueOf(level * 100 / scale);
    }
    
    public void addLabelView(TextView v) {
        mLabelViews.add(v);
    }

    public void addIconView(ImageView v) {
        mIconViews.add(v);
    }
    
    private  String getBatteryPercentage(Intent batteryChangedIntent) {
        int level = batteryChangedIntent.getIntExtra("level", 0);
        int scale = batteryChangedIntent.getIntExtra("scale", 100);
        return String.valueOf(level * 100 / scale) + "%";
    }
    
    private void updateBatteryPercentageView(int state) {
        ImageView battery = mIconViews.get(0);
        ImageView batteryCharge = mIconViews.get(1);
        TextView percentageTxt = mLabelViews.get(0);
        TextView batteryTxt = mLabelViews.get(1);
        
        final int icon = mIsCharge ? R.drawable.gn_stat_sys_battery_charge 
                : R.drawable.gn_stat_sys_battery;
        
        if (mIsCharge) {
        	batteryCharge.setImageResource(R.drawable.gn_stat_sys_battery_charge_lightning);
        	batteryCharge.setVisibility(View.VISIBLE);
        } else {
        	batteryCharge.setVisibility(View.GONE);
        }
        
        battery.setImageResource(icon);
        battery.setImageLevel(mBatteryLevel);

        switch (state) {
            case 0:
                percentageTxt.setVisibility(View.GONE);
                batteryTxt.setVisibility(View.GONE);
                break;
            case 1:
                percentageTxt.setText(mBatteryPercentage);
                percentageTxt.setVisibility(View.VISIBLE);
                percentageTxt.setPadding(2, 0, 2, 0);
                batteryTxt.setVisibility(View.GONE);
                break;
            case 2:
                percentageTxt.setVisibility(View.GONE);
                if (mIsCharge) {
                    batteryTxt.setVisibility(View.GONE);
                } else {
                    batteryTxt.setVisibility(View.VISIBLE);
                    batteryTxt.setText(mBatteryPercent);
                    battery.setImageResource(R.drawable.gn_stat_sys_battery_null);
                }
                break;
            default:
                break;
        }
    }
}
