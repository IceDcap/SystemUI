package com.amigo.navi.keyguard.skylight;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.modules.KeyguardMissedInfoModule.MissedInfoCallback;
import com.amigo.navi.keyguard.modules.WeatherInfoModule.WeatherInfoUpdateCallback;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.R;
import com.gionee.amiweather.library.StateInt;
import com.gionee.amiweather.library.WeatherData;

//import android.os.AsyncTask;

public class SkylightView extends FrameLayout {

    private static final String LOG_TAG = "SkylightView";
    private static final String strMaxMissCount = "n";
    private static final int maxMissCount = 99;
    private Context mContext;
    private ImageTextButton mMissSMSTextImageBt = null;
    private ImageTextButton mMissCallTextImageBt = null;

    private TextView mWeatherStatustext = null;
    private TextView mTemperatureText = null;
    private TextView mCitytext = null;

    private TextView mHourText = null;
    private TextView mMinuteText = null;
    private TextView mEnAmpmText = null;
    private TextView mCnAmpmText = null;
    private TextView mDateText = null;
    private TextView mWeekText = null;

    private ImageView mWeatherImg = null;

    /*
     * for clock
     */
    private static final int AM_PM_STYLE_NORMAL = 0;
    private static final int AM_PM_STYLE_SMALL = 1;
    private static final int AM_PM_STYLE_GONE = 2;
    private static final int AM_PM_STYLE = AM_PM_STYLE_NORMAL;

    private ResourceManager mResourceManager;

    private Configuration mConfiguration=null;
    private RelativeLayout mWeatherLayout;
    private RelativeLayout mSkylightView;
    
    public SkylightView(Context context) {
        this(context, null);
    }

    public SkylightView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SkylightView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        this.setClickable(true);
        mConfiguration=new Configuration(getResources().getConfiguration());
        mResourceManager=new ResourceManager(context);
        LayoutInflater.from(context).inflate(R.layout.skylight_home_view, this, true);
        initViews();
        updateTimeView();
        updateWeatherView(null);
//        updateViewWhenLocalChange(mConfiguration.locale);
    }

    private void initViews() {
        mSkylightView = (RelativeLayout)findViewById(R.id.skylight_view);
        
        // missinfo
        mMissCallTextImageBt = (ImageTextButton) findViewById(R.id.imgtextview_miss_call);
        mMissSMSTextImageBt = (ImageTextButton) findViewById(R.id.imgtextview_miss_sms);
        mMissCallTextImageBt.setVisibility(View.GONE);
        mMissSMSTextImageBt.setVisibility(View.GONE);


        // weather
        mWeatherImg = (ImageView) findViewById(R.id.wether_icon);
        mWeatherStatustext = (TextView) findViewById(R.id.weather_status);
        mTemperatureText = (TextView) findViewById(R.id.temperature);
        mCitytext = (TextView) findViewById(R.id.city);

        mWeatherLayout = (RelativeLayout)findViewById(R.id.weatherLayout);
        mHourText = (TextView) findViewById(R.id.time_hour);
        mMinuteText = (TextView) findViewById(R.id.time_minute);
        mEnAmpmText = (TextView) findViewById(R.id.entime_am_pm);
        mCnAmpmText = (TextView) findViewById(R.id.cntime_am_pm);

        mDateText = (TextView) findViewById(R.id.date_text);
        mWeekText = (TextView) findViewById(R.id.week_text);

        mMissCallTextImageBt.setImageView(R.drawable.skylight_missed_call);

        Typeface mFaceB = Typeface.createFromAsset(mContext.getAssets(), "font/Roboto-Regular.ttf");
        Typeface mFaceT = createTypeface("sans-serif-thin", Typeface.BOLD);
        mHourText.setTypeface(mFaceB);
        mMinuteText.setTypeface(mFaceB);
        mEnAmpmText.setTypeface(mFaceT);
        mCnAmpmText.setTypeface(mFaceT);
        mTemperatureText.setTypeface(mFaceT);

    }

    private Typeface createTypeface(String familyname, int style) {
        Typeface mFace = Typeface.create(familyname, style);
        return mFace;
    }

    private final MissedInfoCallback mMissedInfoCallbacks = new MissedInfoCallback() {
        public void onMissMmsCountChanged(int count) {
            String countStr = String.valueOf(count);
            if (count > maxMissCount) {
                countStr = strMaxMissCount;
            }
            mMissSMSTextImageBt.setTextView(countStr);
            if (count == 0) {
                mMissSMSTextImageBt.setVisibility(View.GONE);
            } else {
                mMissSMSTextImageBt.setVisibility(View.VISIBLE);
            }
        }

        public void onMissCallCountChanged(int count) {
            String countStr = String.valueOf(count);
            if (count > maxMissCount) {
                countStr = strMaxMissCount;
            }
            if(DebugLog.DEBUG)
                Log.d(LOG_TAG, "onMissCallCountChanged count=" + count);
            mMissCallTextImageBt.setTextView(countStr);
            if (count == 0) {
                mMissCallTextImageBt.setVisibility(View.GONE);
            } else {
                mMissCallTextImageBt.setVisibility(View.VISIBLE);
            }
        }
        
        @Override
        public void setMissSmsInfoGone() {

        }
    };

    private final WeatherInfoUpdateCallback mWeatherInfoCallback = new WeatherInfoUpdateCallback() {

        @Override
        public void updateWeatherInfo(WeatherData data) {
            updateWeatherView(data);

        }

        @Override
        public void updateTime() {
            updateTimeView();
        }
    };

    private void updateTimeView() {
//        updateViewByLocal();
        setTime(mContext, new Date());
    }

    
    
    private void updateViewByLocal() {
        Locale oldLocale = mConfiguration.locale;
        Locale newLocale = getResources().getConfiguration().locale;
        if(DebugLog.DEBUG)Log.d(LOG_TAG, "oldLocale: "+oldLocale+"  newLocale: "+newLocale+" isequals:  "+oldLocale.equals(newLocale));
        if (!oldLocale.equals(newLocale)) {
            updateViewWhenLocalChange(newLocale);
            mConfiguration.locale = newLocale;
        }
    }
    
    /**
     * @deprecated
     */
    private void updateViewWhenLocalChange(Locale locale) {
        RelativeLayout.LayoutParams params=(RelativeLayout.LayoutParams)mSkylightView.getLayoutParams();
        if(DebugLog.DEBUG)Log.d(LOG_TAG, "updateViewWhenLocalChange:  "+Locale.CHINA.equals(locale));
        if(Locale.CHINA.equals(locale)){
            mWeatherLayout.setVisibility(View.VISIBLE);
            params.topMargin=getResources().getDimensionPixelSize(R.dimen.skylight_time_margin_top);
        }else{
            mWeatherLayout.setVisibility(View.GONE);
            params.topMargin=getResources().getDimensionPixelSize(R.dimen.skylight_time_margin_top_when_no_weather);
        }
        mSkylightView.setLayoutParams(params);
    }

    private void updateWeatherView(WeatherData forecastData) {
        if(DebugLog.DEBUG)Log.d(LOG_TAG, "updateWeatherView---------focastData: "+((forecastData==null)?"null":forecastData.toString()));
        if (forecastData != null) {
            Log.d(LOG_TAG, "SkylightView temperature: "+forecastData.getLiveTemperatureWithUnit());
            final String cityName = forecastData.getCityName();
            mTemperatureText.setText(forecastData.getLiveTemperatureWithUnit());
            mCitytext.setText(cityName);
            String weather = forecastData.getLiveState();
            mWeatherStatustext.setText(weather);
            int status=forecastData.getLiveStateInt();
            int res=mResourceManager.getWeatherIcon(status);
            mWeatherImg.setImageResource(res);
        } else {
            String unknow = mContext.getString(R.string.message_unknow);
            mTemperatureText.setText(R.string.new_widget41_not_applicable);
            mCitytext.setText(unknow);
            mWeatherStatustext.setText(mContext.getString(R.string.new_widget41_nodata));
            int res=mResourceManager.getWeatherIcon(StateInt.OTHER);
            mWeatherImg.setImageResource(res);
        }
    }


    /*
     * registerReceiver for clock & weather & missed info
     */
    public void registeCallbacks() {
        KeyguardUpdateMonitor updateMonitor = KeyguardUpdateMonitor.getInstance(mContext);
        updateMonitor.registerWeatherInfoCallback(mWeatherInfoCallback);
        updateMonitor.registerMissedInfoCallback(mMissedInfoCallbacks);
        updateMonitor.activeQueryMissedCallCount();
        updateMonitor.activeQueryMissedMmsCount();

    }

    public void unregistCallbacks() {
        KeyguardUpdateMonitor updateMonitor = KeyguardUpdateMonitor.getInstance(mContext);
        updateMonitor.unregisterMissedInfoCallback(mMissedInfoCallbacks);
        updateMonitor.registerWeatherInfoCallback(mWeatherInfoCallback);

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(DebugLog.DEBUG)
            Log.d(LOG_TAG, "onAttachedToWindow");
        registeCallbacks();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(DebugLog.DEBUG)
            Log.d(LOG_TAG, "onDetachedFromWindow");
        unregistCallbacks();
    }


    private CharSequence getDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date curDate = new Date(System.currentTimeMillis());//
        String str = formatter.format(curDate);
        return str;
    }
    private CharSequence getWeek() {
//        SimpleDateFormat formatter = new SimpleDateFormat("EEEE");
        Date curDate = new Date(System.currentTimeMillis());//
        int weekDay=curDate.getDay();
        String str=getResources().getStringArray(R.array.week_array)[weekDay];
        return str;
    }
    
    private void setTime(Context ctx, Date date) {
        boolean is24HourFormat = DateFormat.is24HourFormat(ctx);
        if(DebugLog.DEBUG)Log.d(LOG_TAG, "is24HourFormat-----" + is24HourFormat + ",hash=" + ctx.hashCode() + ",date " + date);
        SimpleDateFormat timeHourFormat12 = new SimpleDateFormat("h:");
        Time time = new Time();
        time.setToNow();
        if (is24HourFormat) {
            final SimpleDateFormat timeHourFormat24 = new SimpleDateFormat("HH:");
            final String hour = timeHourFormat24.format(date);
            mHourText.setText(hour);
            mCnAmpmText.setVisibility(GONE);
            mEnAmpmText.setVisibility(GONE);
        } else {
            final String hour = timeHourFormat12.format(date);
            mHourText.setText(hour);
            setAmpm(time);
          
        }
        SimpleDateFormat timeMinFormat = new SimpleDateFormat("mm");
        mMinuteText.setText(timeMinFormat.format(date));
        mDateText.setText(getDate());
        mWeekText.setText(getWeek());
        
    }
    
    private void setAmpm(Time time) {
        boolean isLocaleCN = Locale.CHINA.equals(mContext.getResources().getConfiguration().locale);

        if (isLocaleCN) {
            mCnAmpmText.setVisibility(VISIBLE);
            mEnAmpmText.setVisibility(GONE);
            if (time.hour >= 0 && time.hour < 12) {
                mCnAmpmText.setText(mContext.getString(R.string.new_widget41_time_am));
            } else {
                mCnAmpmText.setText(mContext.getString(R.string.new_widget41_time_pm));
            }
        } else {
            mCnAmpmText.setVisibility(GONE);
            mEnAmpmText.setVisibility(VISIBLE);
            if (time.hour >= 0 && time.hour < 12) {
                mEnAmpmText.setText(mContext.getString(R.string.time_unit_am));
            } else {
                mEnAmpmText.setText(mContext.getString(R.string.time_unit_pm));
            }
        }
    }

    public void destroyViews() {
        mMissSMSTextImageBt = null;
        mMissCallTextImageBt = null;
    }

    /*
     * for clock update end
     */

}
