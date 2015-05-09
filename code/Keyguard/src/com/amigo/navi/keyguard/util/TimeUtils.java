package com.amigo.navi.keyguard.util;

import com.android.keyguard.R;

import android.content.Context;

public class TimeUtils {
      
	  private static boolean isChinese=false;
	
	  public static String secToTime(int time,Context context) {  
	        String timeStr = null;  
	        int hour = 0;  
	        int minute = 0;  
	        int second = 0;  
	        if (time <= 60)
	        		timeStr= time+context.getResources().getString(R.string.second);  

	        else {  
	            minute = time / 60;  
	            if (minute<60 ) {
	            	second=time% 60;
	            	if(second!=0){
	            		minute=minute+1;	
	            	}
	            	timeStr= minute+context.getResources().getString(R.string.minute);

	            } else {  
	                hour = minute / 60;  
	                if (hour > 99)   
	                minute = minute % 60;
	                timeStr = hour + context.getResources().getString(R.string.hour) +minute+ context.getResources().getString(R.string.minute);; 

	            }  
	        }  
	        return timeStr;  
	    }  
	  
	  public static void getCurrentLocal(Context context){
		  isChinese=context.getResources().getConfiguration().locale.getCountry().equals(
					"CN");
	  }
	  

}
