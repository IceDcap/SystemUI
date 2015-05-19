package com.amigo.navi.keyguard.haokan;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.amigo.navi.keyguard.DebugLog;

public class TimeControlManager {
    private static final String TAG = "TimeControlManager";
	private static final long DAY = 86400000;//24hour
	private Context mContext;
	private AlarmManager alarmManager;
	private PendingIntent pendingActivityIntent;
	private static TimeControlManager instance = null;
	
	private TimeControlManager(){
		
	}
	
	public static TimeControlManager getInstance(){
		if(instance == null){
			construct();
		}
		return instance;
	}
	
	private static void construct(){
		synchronized (TimeControlManager.class){
			if(instance == null){
				instance = new TimeControlManager();
			}
		}
	}
	
	public void init(Context context){
        DebugLog.d(TAG,"init");
		mContext = context;
		alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
	}
	
	public void startUpdateAlarm(){
        DebugLog.d(TAG,"startAlarm");
		Intent intent = new Intent(Constant.WALLPAPER_TIMING_UPDATE);
		pendingActivityIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 10);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Calendar newCalendar = getTime(calendar);
       long alarmTime =  newCalendar.getTimeInMillis();   
//       long tempTime = alarmTime - System.currentTimeMillis();
       long amendTime = alarmTime + DAY;
       Date d = new Date(amendTime);
       SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
       String time = sdf.format(d);  
       DebugLog.d(TAG,"getTime time:" + time);
       alarmManager.set(AlarmManager.RTC_WAKEUP, amendTime,pendingActivityIntent);
	}
	
	public void cancelUpdateAlarm(){
		if(pendingActivityIntent != null && alarmManager != null)
			alarmManager.cancel(pendingActivityIntent);
	}
	
	public Calendar getTime(Calendar calendar){
	    int min = getRandomeTime(60);
	    int second = getRandomeTime(60);
	    calendar.set(Calendar.MINUTE, min);
	    calendar.set(Calendar.SECOND, second);
	    return calendar;
	}
	
	public static String getStringToday(){
		SimpleDateFormat sp = new SimpleDateFormat("yyyyMMdd");
		String d = sp.format(new Date());
		return d;
	}
	
	public long getSetWallpaperTime(){
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 59);
		c.set(Calendar.MILLISECOND, 999);
	    return c.getTimeInMillis();
	}
	
	public void release(){
		mContext = null;
		releaseSlef();
	}
	
	private static void releaseSlef(){
		instance = null;
	}
	
	public long getRandomeTime(long min){
		long randomTime = (long) (Math.random() * 1000*60*min);
		return randomTime;
	}
	
	public int getRandomeMin(int min){
	        if(min > 60 || min < 0){
	            min = 60;
	        }
	        int randomTime = (int) (Math.random() * min);
	        return randomTime;
	}
	
	public int getRandomeTime(int time){
        if(time > 60 || time < 0){
            time = 60;
        }
         int randomTime = (int) (Math.random() * time);
         return randomTime;
   }
	
}
