package com.android.systemui.gionee.statusbar;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import amigo.provider.AmigoSettings;
import com.android.systemui.R;
import android.net.TrafficStats;

public class GnNetworkSpeedController extends BroadcastReceiver {
    private static final String TAG = "StatusBar.GnNetworkSpeedController";
    private static final String NETWORK_SPEED_DISPLAY = "network_speed_display";

    private Context mContext;
    private ArrayList<TextView> mLabelViews = new ArrayList<TextView>();
    private boolean misShowNetworkSpeed = false;
    private boolean misOpenSwitch = false;
    private Bundle mBundle = new Bundle();

    private static final int EVENT_REFRESH_VIEW = 4001;

    private static final long BYTES = 1; // B/s
    private static final long KBYTES = BYTES * 1024; // KB/s
    private static final long MBYTES = KBYTES * 1024; // MB/s
    private static final long GBYTES = MBYTES * 1024; // GB/s

    private static final int DELAYTIMESEC = 4; // 4s
    private static final int DELAYTIMEMS = DELAYTIMESEC * 1000; // 4000 ms

    private long mTrafficTx = 0;
    private long mTrafficRx = 0;
    // Whether to open a thread
    private boolean mFlag = false;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_REFRESH_VIEW:
                    updateUI(msg);
                    break;
                default:
                    break;
            }
        }
    };

    public GnNetworkSpeedController(Context context) {
        mContext = context;
        misOpenSwitch = isNetworkSpeedSwitchEnabled();

        register(context);
    }
    
    public void initVisibilityState() {
    	TextView textView = mLabelViews.get(0);
        misOpenSwitch = isNetworkSpeedSwitchEnabled();
        misShowNetworkSpeed = misOpenSwitch && isNetworkAvailable(mContext);
        if (misShowNetworkSpeed) {
			textView.setVisibility(View.VISIBLE);
		} else {
			textView.setVisibility(View.GONE);
		}
	}

    private void register(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(ConnectivityManager.INET_CONDITION_ACTION);
        context.registerReceiver(this, filter);

        context.getContentResolver().registerContentObserver(
                AmigoSettings.getUriFor(AmigoSettings.NETWORK_SPEED_DISPLAY), false,
                mNetworkSpeedChangeObserver);
    }

    private ContentObserver mNetworkSpeedChangeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            Log.d(TAG, "Switch of NetworkSpeed  is changed!");
            misOpenSwitch = isNetworkSpeedSwitchEnabled();
            misShowNetworkSpeed = misOpenSwitch && isNetworkAvailable(mContext);

            handleThread(misShowNetworkSpeed);
        }
    };

    private boolean isNetworkSpeedSwitchEnabled() {
        int config = AmigoSettings.getInt(mContext.getContentResolver(), NETWORK_SPEED_DISPLAY, 0);
        Log.d(TAG, "NetworkSpeedSwitch config " + config);
        return config == 1;
    }

    public void addLabelView(TextView v) {
        mLabelViews.add(v);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.d(TAG, "GnNetworkSpeedController onReceive action is " + action);

        misShowNetworkSpeed = misOpenSwitch && isNetworkAvailable(context);
        Log.d(TAG, "GnNetworkSpeedController mShouldShowNetworkSpeed is " + misShowNetworkSpeed);

        handleThread(misShowNetworkSpeed);
    }

    private void handleThread(boolean showNetworkSpeed) {
        if (showNetworkSpeed && !mFlag) {
            mFlag = true;
            startThread();
            return;
        }

        if (!showNetworkSpeed && mFlag) {
            updateUI(null);
            mFlag = false;
            removeThread();
        }
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }

        NetworkInfo[] info = cm.getAllNetworkInfo();
        if (info != null) {
            for (int i = 0; i < info.length; i++) {
                if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            refresh(true);
            mHandler.postDelayed(mRunnable, DELAYTIMEMS);
        }
    };

    public void startThread() {
    	refresh(false);
        mHandler.postDelayed(mRunnable, DELAYTIMEMS);
    };

    public void removeThread() {
        mHandler.removeCallbacks(mRunnable);
    };

    /**
     * Real-time reading system flow files, update speed
     */
    public void refresh(boolean bChecked) {
    	long newTx = TrafficStats.getTotalTxBytes(); 
    	long newRx = TrafficStats.getTotalRxBytes();
    	long traffic_data = (newTx - mTrafficTx) + (newRx  - mTrafficRx);
    	
    	mTrafficTx = newTx;
    	mTrafficRx = newRx;

    	if (bChecked == false) {
    		traffic_data = 100;
    	}
    	
        Message msg = mHandler.obtainMessage();
        msg.what = EVENT_REFRESH_VIEW;
        Bundle bundle = getBundle();
        bundle.putLong("data", traffic_data);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    private String getNetworkSpeed(Message message) {
        double value = ((double) (message.getData().getLong("data"))) / DELAYTIMESEC;
        String strSpeed = formatSize(value);

        return strSpeed;
    }

    private String formatSize(double value) {
        if (value < 0) {
            value = 0;
        } 
        
        if (value >= 0 && value <MBYTES) {
			value = value / KBYTES;
			return mContext.getString(R.string.networkspeed_flow_kbytes, value);
		} else if (value >= MBYTES && value < GBYTES) {
			value = value / MBYTES;
            return mContext.getString(R.string.networkspeed_flow_mbytes, value);
		} else if(value >= GBYTES) {
			value = value / GBYTES;
	        return mContext.getString(R.string.networkspeed_flow_gbytes, value);
		} else {
			return mContext.getString(R.string.networkspeed_flow_kbytes, 0);
		}
    }

    private void updateUI(Message message) {
        TextView v = mLabelViews.get(0);
        if (misShowNetworkSpeed) {
            v.setText(getNetworkSpeed(message));
            v.setVisibility(View.VISIBLE);
        } else {
            v.setVisibility(View.GONE);
        }
    }

    private Bundle getBundle() {
        if (mBundle == null) {
            mBundle = new Bundle();
        }
        return mBundle;
    }
}
