/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
package com.android.systemui.gionee.cc.qs.policy;

import java.sql.NClob;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.android.systemui.gionee.cc.GnControlCenter;
import com.android.systemui.statusbar.policy.MobileDataControllerImpl;
import com.android.systemui.statusbar.policy.MobileDataControllerImpl.Callback;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionManager.OnSubscriptionsChangedListener;
import android.util.Log;

import com.android.internal.telephony.TelephonyIntents;

import android.os.Handler;
import android.os.SystemProperties;

public class GnMobileDataControllerImpl extends BroadcastReceiver implements GnMobileDataController {
    
    // debug
    static final String TAG = "GnMobileDataController";
    static final boolean DEBUG = GnControlCenter.DEBUG;
    
    private Context mContext;

    private final MobileDataControllerImpl mMobileDataController;
    
    private final SubscriptionManager mSubscriptionManager;
    
    private Handler mHandler = new Handler();

    private boolean mHasMobileDataFeature;
    
    private boolean isMtkPlatform;
    
    private int mSubId = 0;
    
    ArrayList<MobileDataChangedCallback> mCallbacks = new ArrayList<MobileDataChangedCallback>();
    
    public GnMobileDataControllerImpl(Context context) {
        mContext = context;
        
        ConnectivityManager cm = (ConnectivityManager)mContext.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        mHasMobileDataFeature = cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE);
        
        mSubscriptionManager = SubscriptionManager.from(mContext);
        mSubscriptionManager.addOnSubscriptionsChangedListener(mSubscriptionListener);
        
        mMobileDataController = new MobileDataControllerImpl(mContext);
        mMobileDataController.setCallback(new MobileDataControllerImpl.Callback() {
            @Override
            public void onMobileDataEnabled(boolean enabled) {
                notifyMobileDataEnabled(enabled);
            }
        });
        
        // broadcasts
        IntentFilter filter = new IntentFilter();
        filter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION_IMMEDIATE);
        filter.addAction(ConnectivityManager.INET_CONDITION_ACTION);
        filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        context.registerReceiver(this, filter);
        
        isMtkPlatform = !SystemProperties.get("ro.mediatek.platform", "").isEmpty();
    }
    
    private void notifyMobileDataEnabled(boolean enabled) {

    }
    
    private void notifyMobileDataChange() {
        for (MobileDataChangedCallback cb : mCallbacks) {
            cb.onMobileDataChanged();
        }
    }

    @Override
    public void addMobileDataChangedCallback(MobileDataChangedCallback cb) {
        mCallbacks.add(cb);
        int subId = SubscriptionManager.getDefaultDataSubId();
        if (mSubscriptionManager.isValidSubscriptionId(subId)) {
            mSubId = subId;
            if (isMtkPlatform) {
                String name = Settings.Global.MOBILE_DATA + mSubId;
                mContext.getContentResolver().registerContentObserver(
                        Settings.Global.getUriFor(name), true, mMobileDataChangeObserver);
                Log.d(TAG, "mtk add callback  name = " + name);
            } else {
                int phoneId = SubscriptionManager.getPhoneId(mSubId);
                String name = Settings.Global.MOBILE_DATA + phoneId;
                mContext.getContentResolver().registerContentObserver(
                        Settings.Global.getUriFor(name), true, mMobileDataChangeObserver);
                Log.d(TAG, "qcom add callback  name = " + name);
            }
        }
    }

    @Override
    public void removeMobileDataChangedCallback(MobileDataChangedCallback cb) {
        mCallbacks.remove(cb);
        mContext.getContentResolver().unregisterContentObserver(mMobileDataChangeObserver);
    }

    @Override
    public boolean isMobileDataSupported() {
        return mMobileDataController.isMobileDataSupported();
    }

    @Override
    public boolean isMobileDataEnabled() {
        return mMobileDataController.isMobileDataEnabled();
    }

    @Override
    public boolean isAirplaneEnabled() {
        return Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
    }
    
    @Override
    public void setMobileDataEnabled(boolean enabled) {
        mMobileDataController.setMobileDataEnabled(enabled);
    }
    
    public boolean hasMobileDataFeature() {
        return mHasMobileDataFeature;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (DEBUG) Log.d(TAG, "onReceive action = " + action);
        if (action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    notifyMobileDataChange();
                }
            }, 500);
        } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION_IMMEDIATE) ||
                 action.equals(ConnectivityManager.INET_CONDITION_ACTION)) {
            notifyMobileDataChange();
        } else if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
            notifyMobileDataChange();
        }
    }

    private ContentObserver mMobileDataChangeObserver = new ContentObserver(new Handler()) {

        @Override
        public void onChange(boolean selfChange) {
            Log.d(TAG, "ContentObserver onChange");
            notifyMobileDataChange();
        }
    };

    private final OnSubscriptionsChangedListener mSubscriptionListener = new OnSubscriptionsChangedListener() {
        
        @Override
        public void onSubscriptionsChanged() {
            Log.d(TAG, "onSubscriptionsChanged");
            int subId = SubscriptionManager.getDefaultDataSubId();
            Log.d(TAG, "mSubscriptionListener  subId = " + subId);
            if (SubscriptionManager.isValidSubscriptionId(subId) && mSubId != subId) {
                mSubId = subId;
                mContext.getContentResolver().unregisterContentObserver(mMobileDataChangeObserver);

                if (isMtkPlatform) {
                    String name = Settings.Global.MOBILE_DATA + mSubId;
                    mContext.getContentResolver().registerContentObserver(
                            Settings.Global.getUriFor(name), true, mMobileDataChangeObserver);
                    Log.d(TAG, "mtk name = " + name);
                } else {
                    int phoneId = SubscriptionManager.getPhoneId(mSubId);
                    String name = Settings.Global.MOBILE_DATA + phoneId;
                    mContext.getContentResolver().registerContentObserver(
                            Settings.Global.getUriFor(name), true, mMobileDataChangeObserver);
                    Log.d(TAG, "qcom name = " + name);
                }

                notifyMobileDataChange();
            }
        };
    };
}