package com.amigo.navi.keyguard.modules;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import com.gionee.amiweather.library.QueryConstant;
import com.gionee.amiweather.library.WeatherData;
import com.amigo.navi.keyguard.DebugLog;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.google.android.collect.Lists;
import com.android.keyguard.R ;

public class WeatherInfoModule extends KeyguardModuleBase {

    private static final String LOG_TAG = "WeatherInfoModule";
    private static final String WEATHERUPDATE = "com.coolwind.weather.update";
    private static final int MSG_WEATHER_INFO = 0;

    private static WeatherInfoModule sInstance = null;

    private ArrayList<WeatherInfoUpdateCallback> mCallbacks = Lists.newArrayList();

    private WeatherHandler mHandler = new WeatherHandler();

    public synchronized static WeatherInfoModule getInstance(Context context, KeyguardUpdateMonitor updateMonitor) {
        if (sInstance == null) {
            sInstance = new WeatherInfoModule(context, updateMonitor);
        }

        return sInstance;
    }

    protected WeatherInfoModule(Context context, KeyguardUpdateMonitor updateMonitor) {
        super(context, updateMonitor);
        context.getContentResolver().registerContentObserver(QueryConstant.QUERY_WEATHER_UNCASE_LANGUAGE, true,
                new WeatherDataObserver());
    }

    @Override
    protected void initModule() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (intent.getAction().equals(Intent.ACTION_TIME_TICK)
                                || intent.getAction().equals(Intent.ACTION_TIME_CHANGED)
                                || intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                            handleUpdateTime();
                        } else if (intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
                            handleUpdateTime();
                            getWeatherInfo();
                        }
                    }
                });
            }
        };
        mFilter = new IntentFilter();
        // mFilter.addAction(WEATHERUPDATE);
        mFilter.addAction(Intent.ACTION_TIME_TICK);
        mFilter.addAction(Intent.ACTION_TIME_CHANGED);
        mFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        mFilter.addAction(Intent.ACTION_LOCALE_CHANGED);

    }

    public void registerCallback(WeatherInfoUpdateCallback callback) {
        if (!mCallbacks.contains(callback)) {
            mCallbacks.add(callback);
        }
        getWeatherInfo();
    }

    public void unregisterCallback(WeatherInfoUpdateCallback callback) {
        mCallbacks.remove(callback);
    }

    private void handleUpdateWeather(WeatherData data) {
        for (int i = 0; i < mCallbacks.size(); i++) {
            mCallbacks.get(i).updateWeatherInfo(data);
        }
    }

    private void handleUpdateTime() {
        for (int i = 0; i < mCallbacks.size(); i++) {
            mCallbacks.get(i).updateTime();
        }
    }

    public void getWeatherInfo() {
        new Thread() {
            @Override
            public void run() {
                WeatherData data = queryWeatherInfo();
                Message msg = mHandler.obtainMessage(MSG_WEATHER_INFO, data);
                mHandler.sendMessage(msg);
            }
        }.start();
    }

    // private Object mLock=new Object();

    private synchronized WeatherData queryWeatherInfo() {
        WeatherData situation = null;
        Uri uri = QueryConstant.QUERY_WEATHER_UNCASE_LANGUAGE;
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                situation = WeatherData.obtain(cursor);
                cursor.close();
            }
        } catch (Exception e) {
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return situation;

    }

    private class WeatherHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_WEATHER_INFO:
                WeatherData data = (WeatherData) msg.obj;
                handleUpdateWeather(data);
                break;

            default:
                break;
            }
        }
    }

    private class WeatherDataObserver extends ContentObserver {
        public WeatherDataObserver() {
            super(null);
            if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "WeatherDataObserver-------");
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "WeatherDataObserver-------onchange");
            getWeatherInfo();
        }
    }

    public interface WeatherInfoUpdateCallback {
        void updateWeatherInfo(WeatherData data);

        void updateTime();
    }

}
