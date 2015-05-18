package com.amigo.navi.keyguard.infozone;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.android.keyguard.R;
import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.haokan.UIController;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AmigoKeyguardInfoZone extends FrameLayout{

	private boolean mRegister = false;
//	private int mInScreen = Integer.MIN_VALUE;
	public static final String WEATHERUPDATE = "com.coolwind.weather.update";
	private String TAG = "Weather_NewWidget42";
//	private LinearLayout mLieaLayout;
//	private LinearLayout mWeather_date_info;
//	private LinearLayout mWeatherDate;
//	private FrameLayout mWeather_top;
	private TimeTickReceiver mTimeTickReceiver;
	private Time time = null;
	private Context mContext;
	private TextView mweekdayText;
	private TextView mFestivalText,mFestivalText_US;
	private TextView mDateView;
//	private FrameLayout mShowTimeLayout;

	private TextView mWidgetTimeHour;
	private TextView mWidgetTimeMin;
	
	private TextView mWidgetTimeMiddle,mWidgetTimeMiddle_US;
    
	
	private Typeface mFontTypeRegular;
	private Typeface mFontTypeThin;
	
	private Typeface mFontTypeMedium,mFontTypeLight;
	    
	
	public static final String SHOW_WEATHER_CONDITION_ACTION = "com.coolwind.weather.showweathercondition";
	public static final String SHOW_TIME_INFO_ACTION = "com.coolwind.weather.showtimeinfo";
	public static final String New_WIDGET_CITY_JUMP = "new_widget_city_jump";
	private TextView mTimeFormatFlag;// am or pm
	private TimeFormatObserver mTimeFormatObserver;
	public static final String NEW_WIDGET_CITY_JUMP_ACTION = "com.gionee.dynamicweather.new_widget_city_jump_action";
	public static final String New_WIDGET_CITY_JUMP_ACTION_FLAG = "new_widget_city_jump_flag";
	private Runnable mAnimationRunnable = null;
//	private int mAnimationResId;
//	private String mCity;
	// private boolean mIsSystemProcess;
//	private ScreenOnReceiver mScreenOnReceiver;

	// layout US
	private LanguageReveicer mLanguageReveicer;
	private TextView mWidgetTimeHour_US;
	private TextView mWidgetTimeMin_US;
	private TextView mTimeFormatFlag_US;
	private TextView mDateView_US;
	private TextView mweekdayText_US;
	private LinearLayout mShowTimeLayout_CN;
	private LinearLayout mShowTimeLayout_US;
	
	private LinearLayout mTimeLayout,mTimeLayout_US;
	
	
	private LinearLayout mDateinfoLayout,mDateinfoLayout_US;
	
	private Handler mHandler = new Handler();
	
	private float mMaxTranslationX;
	
	public AmigoKeyguardInfoZone(Context context) {
		super(context);
		init(context);
		initInflate();
	}

	public AmigoKeyguardInfoZone(Context context, AttributeSet attri) {
		super(context, attri);
		init(context);
	}

	private void init(Context context) {
		mContext = context;
		time = new Time();
		mFontTypeRegular = FontCache.get("font/Roboto-Regular.ttf", mContext);
		mFontTypeThin = FontCache.get("font/Roboto-Thin.ttf", mContext);
		
		mFontTypeMedium = FontCache.get("font/Roboto-Medium.ttf", mContext);
		mFontTypeLight = FontCache.get("font/Roboto-Light.ttf", mContext);
		
		mTimeFormatObserver = new TimeFormatObserver(mContext, mHandler);
		UIController.getInstance().setmInfozone(this);
		mMaxTranslationX = getResources().getDimensionPixelSize(R.dimen.keyguard_infozone_max_translationX);
	}
	
	public float getMaxTranslationX() {
	    return mMaxTranslationX;
    }

	private class LanguageReveicer extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {

			changeLanguage();
		}

	}
	
	private void changeLanguage() {
		if (getResources().getConfiguration().locale.getCountry().equals(
				"CN")) {
			if (mShowTimeLayout_CN != null && mShowTimeLayout_US != null) {
				mShowTimeLayout_CN.setAlpha(1.0f);
				mShowTimeLayout_US.setAlpha(0.0f);
			}
			
			setWeekView(mweekdayText);
			setDateFestivalView(mDateinfoLayout);
			setTimeView(mTimeLayout);
			
		} else {
			if (mShowTimeLayout_CN != null && mShowTimeLayout_US != null) {
				mShowTimeLayout_CN.setAlpha(0.0f);
				mShowTimeLayout_US.setAlpha(1.0f);
			}
			
			setWeekView(mweekdayText_US);
			setDateFestivalView(mDateinfoLayout_US);
            setTimeView(mTimeLayout_US);
		}
	}

	private void retrieve() {
		if (isNotNull(mTimeTickReceiver)) {
			mContext.unregisterReceiver(mTimeTickReceiver);
		}
		
		if(isNotNull(mLanguageReveicer)){
		    mContext.unregisterReceiver(mLanguageReveicer);
		}
		unregisterTimeFormatObservers();
		unregisterDateFormatObservers();
		mRegister=false;
	}

	private boolean isNotNull(Object obj) {
		return obj != null;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		initInflate();
	}

	private void initInflate() {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		View view = inflater.inflate(R.layout.amigo_widget42_body, null);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		addView(view, params);

		mDateView = (TextView) view.findViewById(R.id.newwidget41_weatherdate);
		mDateView.setTypeface(mFontTypeLight);
		mTimeFormatFlag = (TextView) view
				.findViewById(R.id.newwidget41_weathertime_format_flag);

		mweekdayText = (TextView) view
				.findViewById(R.id.newwidget41_weatherweekday);
		
		
		mFestivalText = (TextView) view.findViewById(R.id.newwidget41_festival);
		mFestivalText_US = (TextView) view.findViewById(R.id.newwidget41_festival_us);

		mWidgetTimeHour = (TextView) view
				.findViewById(R.id.newwidget41_weathertime_hour);
		mWidgetTimeHour.setTypeface(mFontTypeMedium);
		mWidgetTimeMin = (TextView) view
				.findViewById(R.id.newwidget41_weathertime_min);
		mWidgetTimeMin.setTypeface(mFontTypeThin);
		
		mWidgetTimeMiddle = (TextView) view
                .findViewById(R.id.newwidget41_weathertime_middle);
		mWidgetTimeMiddle.setTypeface(mFontTypeRegular);
		
		mWidgetTimeMiddle.setText(":");
		
		mWidgetTimeMiddle_US = (TextView) view
        .findViewById(R.id.newwidget41_weathertime_middle_us);
		mWidgetTimeMiddle_US.setText(":");
		mWidgetTimeMiddle_US.setTypeface(mFontTypeRegular);
		
		mTimeLayout = (LinearLayout) view
				.findViewById(R.id.newwidget41_weathertime);
		
		mDateinfoLayout = (LinearLayout) view
                .findViewById(R.id.newwidget41_dateinfo);
		
		mDateinfoLayout_US = (LinearLayout) view
                .findViewById(R.id.newwidget41_dateinfo_us);
		
		mTimeLayout_US = (LinearLayout) view
                .findViewById(R.id.newwidget41_weathertime_us);
		
 

		// layout US
		mWidgetTimeHour_US = (TextView) view
				.findViewById(R.id.newwidget41_weathertime_hour_us);
		mWidgetTimeHour_US.setTypeface(mFontTypeMedium);
		mWidgetTimeMin_US = (TextView) view
				.findViewById(R.id.newwidget41_weathertime_min_us);
		mWidgetTimeMin_US.setTypeface(mFontTypeThin);
		mTimeFormatFlag_US = (TextView) view
				.findViewById(R.id.newwidget41_weathertime_format_flag_us);
		mTimeFormatFlag_US.setTypeface(mFontTypeThin);
		mDateView_US = (TextView) view
				.findViewById(R.id.newwidget41_weatherdate_us);
		mDateView_US.setTypeface(mFontTypeLight);
		mweekdayText_US = (TextView) view
				.findViewById(R.id.newwidget41_weatherweekday_us);
		mShowTimeLayout_CN = (LinearLayout) view
				.findViewById(R.id.newwidget41_showtime_cn);
		mShowTimeLayout_US = (LinearLayout) view
				.findViewById(R.id.newwidget41_showtime_us);
		mShowTimeLayout_CN.setAlpha(0.0f);
		mShowTimeLayout_US.setAlpha(0.0f);
		
		formatTimeAndDate();
	}

	private void formatTimeAndDate() {
		setTime();
		setDate();
		changeLanguage();
	}

	private String initialTime() {
		if (time == null) {
			time = new Time();
		}
		time.setToNow();
		return getLunar(getContext());
	}

	private String getLunar(Context ctx) {
		Lunar lunar = new Lunar(ctx);
		lunar.SetSolarDate(time);
		String lun = lunar.GetLunarNYRString();
		return lun;
	}

    class TimeTickReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, final Intent intent) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					if (intent.getAction().equals(Intent.ACTION_TIME_TICK)
							|| intent.getAction().equals(
									Intent.ACTION_TIME_CHANGED)
							|| intent.getAction().equals(
									Intent.ACTION_TIMEZONE_CHANGED)) {
						setTime();
						setDate();
					}
				}
			});
		}
	}

	@Override
	protected void onAttachedToWindow() {
	    super.onAttachedToWindow();
	    if(DebugLog.DEBUG)Log.d(TAG, "NewWidget42 onAttachedToWindow " + this);
	    registeReceivers();
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if(DebugLog.DEBUG)Log.d(TAG, "NewWidget42 onDetachedFromWindow ");
		retrieve();
	}
	
	private void registeReceivers(){
	       if (!mRegister) {
	            mRegister = true;
	            IntentFilter tmpIntentF = new IntentFilter();
	            tmpIntentF.addAction(Intent.ACTION_TIME_TICK);
	            tmpIntentF.addAction(Intent.ACTION_TIME_CHANGED);
	            tmpIntentF.addAction(Intent.ACTION_TIMEZONE_CHANGED);
	            mTimeTickReceiver = new TimeTickReceiver();
	            mContext.registerReceiver(mTimeTickReceiver, tmpIntentF);

	            registerTimeFormatObservers();
	    		registerDateFormatObservers();
	    		
	    		mLanguageReveicer = new LanguageReveicer();
	    		IntentFilter filter = new IntentFilter();
	    		filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
	    		mContext.registerReceiver(mLanguageReveicer, filter);

	        }
	}

	private class TimeFormatObserver extends ContentObserver {


		public TimeFormatObserver(Context context, Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			setTime();
			setDate();
		}

	}
	
	private void setTime() {
		if (time == null) {
			time = new Time();
		}
		time.setToNow();
		
		Date date = new Date();
		boolean is24HourFormat = DateFormat.is24HourFormat(mContext);
		if(DebugLog.DEBUG){Log.d(TAG,
				"is24HourFormat-----" + is24HourFormat + ",hash="
						+ mContext.hashCode() + ",date " + date);}
		setHourFormat(mWidgetTimeHour,date, is24HourFormat);
		SimpleDateFormat timeMinFormat = new SimpleDateFormat("mm");
		mWidgetTimeMin.setText(timeMinFormat.format(date));
//		mWidgetTimeHour.setTypeface(mFontTypeRegular);
//		mWidgetTimeMin.setTypeface(mFontTypeThin);
	 
		
		// us
		setHourFormat(mWidgetTimeHour_US,date, is24HourFormat);
		mWidgetTimeMin_US.setText(timeMinFormat.format(date));
//		mWidgetTimeHour_US.setTypeface(mFontTypeRegular);
//		mWidgetTimeMin_US.setTypeface(mFontTypeThin);
		
		setTimeFormat(is24HourFormat);
	}
	
	private void setHourFormat(TextView widgetTimeHour ,Date date, boolean is24HourFormat) {
        if (is24HourFormat) {
            SimpleDateFormat timeHourFormat24 = new SimpleDateFormat(
                    "HH");
            String hour = timeHourFormat24.format(date);
            widgetTimeHour.setText(hour);
        } else {
            SimpleDateFormat timeHourFormat12 = new SimpleDateFormat("h");
            String hour = timeHourFormat12.format(date);
            widgetTimeHour.setText(hour);
        }
    }
	
	private void setTimeFormat(boolean is24HourFormat) {

		MarginLayoutParams marginLayoutParamsForText = (MarginLayoutParams) mWidgetTimeHour
				.getLayoutParams();
		if (is24HourFormat) {
			// cn
			mTimeFormatFlag.setVisibility(View.GONE);
			marginLayoutParamsForText
					.setMargins(
							mContext.getResources().getInteger(
									R.integer.weathertime_hour_marginleft), 0,
							0, 0);
			// us
			mTimeFormatFlag_US.setVisibility(View.GONE);
		} else {
			// cn
			mTimeFormatFlag.setVisibility(View.VISIBLE);

			// us
			mTimeFormatFlag_US.setVisibility(View.VISIBLE);
			if (time == null) {
				time = new Time();
			}
			time.setToNow();

			if (time.hour >= 0 && time.hour < 12) {
				// cn
				mTimeFormatFlag.setText(mContext
						.getString(R.string.new_widget41_time_am));

				// us
				mTimeFormatFlag_US.setText(mContext
						.getString(R.string.time_unit_am));
			} else {
				mTimeFormatFlag.setText(mContext
						.getString(R.string.new_widget41_time_pm));

				// us
				mTimeFormatFlag_US.setText(mContext
						.getString(R.string.time_unit_pm));
			}
			// cn
			marginLayoutParamsForText.setMargins(0, 0, 0, 0);

			// us
//			mTimeFormatFlag_US.setTypeface(mFontTypeThin);
			
		}
	}
	
	private void setDate() {
		// TODO Auto-generated method stub
		//cn
		String weekDay = Utils.getWeekdayString(mContext, time.weekDay);
		mweekdayText.setText(weekDay);
//		mlunarText.setText(initialTime());
		setDateFormat(mContext);
		
		//us
		mDateView_US.setText(Utils.getMonthString_US(mContext, Calendar
				.getInstance().getTime().getMonth())
				+ Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
		mweekdayText_US.setText(Utils.getWeekString_US(mContext, Calendar
				.getInstance().get(Calendar.DAY_OF_WEEK)));
	}
	
	public void setFestivalText(CharSequence text) {
	    mFestivalText.setText(text);
	    mFestivalText_US.setText(text);
    }

	private void setDateFormat(Context context) {
		java.text.DateFormat dateFormat = DateFormat.getDateFormat(context);
		// Gionee <jingyn> <2014-08-04> add for CR01334148 begin
		if (dateFormat instanceof SimpleDateFormat) {
            String pattern=((SimpleDateFormat) dateFormat).toLocalizedPattern();
            if(DebugLog.DEBUG)Log.d(TAG, "pattern: "+pattern);
            if ("yyyy-M-d".equals(pattern)) {
                ((SimpleDateFormat) dateFormat).applyPattern("yyyy-MM-dd");
            }
        }
		// Gionee <jingyn> <2014-08-04> add for CR01334148 end
		if (mDateView != null) {
			mDateView.setText(dateFormat.format(new Date()));
		}

	}

	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		if(DebugLog.DEBUG)Log.d(TAG, "onVisibilityChanged = " + visibility);
	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
	    if(DebugLog.DEBUG)Log.d(TAG, "onWindowVisibilityChanged = " + visibility);
		if (visibility == View.VISIBLE && mAnimationRunnable != null) {
			mHandler.postAtFrontOfQueue(mAnimationRunnable);
			mAnimationRunnable = null;
		}
		super.onWindowVisibilityChanged(visibility);
	}

	private void registerTimeFormatObservers() {
		Uri TimeUri = Settings.System.getUriFor(Settings.System.TIME_12_24);
		mContext.getContentResolver().registerContentObserver(TimeUri, false,
				mTimeFormatObserver);
	}

	private void unregisterTimeFormatObservers() {
		mContext.getContentResolver().unregisterContentObserver(
				mTimeFormatObserver);
	}

	private void registerDateFormatObservers() {
		Uri DateUri = Settings.System.getUriFor(Settings.System.DATE_FORMAT);
		mContext.getContentResolver().registerContentObserver(DateUri, false,
				mTimeFormatObserver);
	}

	private void unregisterDateFormatObservers() {
		mContext.getContentResolver().unregisterContentObserver(
				mTimeFormatObserver);
	}
	
    
    
    private View mWeekView,mTimeView,mDateFestivalView;

    public View getWeekView() {
        return mWeekView;
    }

    public void setWeekView(View mWeekView) {
        this.mWeekView = mWeekView;
    }

    public View getTimeView() {
        return mTimeView;
    }

    public void setTimeView(View mTimeView) {
        this.mTimeView = mTimeView;
    }

    public View getDateFestivalView() {
        return mDateFestivalView;
    }

    public void setDateFestivalView(View mDateFestivalView) {
        this.mDateFestivalView = mDateFestivalView;
    }
    
    
	
}
