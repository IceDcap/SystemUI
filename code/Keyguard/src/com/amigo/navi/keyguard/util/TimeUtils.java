package com.amigo.navi.keyguard.util;

import android.content.Context;

public class TimeUtils {
      
	  private static boolean isChinese=false;
	
	  public static String secToTime(int time) {  
	        String timeStr = null;  
	        int hour = 0;  
	        int minute = 0;  
	        int second = 0;  
	        if (time <= 60)
	        	if(isChinese){
	        		timeStr= time+"秒";  
	        		  
	        	}else{
	        		timeStr= time+"s";
	        	}
	        	
	        else {  
	            minute = time / 60;  
	            if (minute<60 ) {
	            	second=time% 60;
	            	if(second!=0){
	            		minute=minute+1;	
	            	}
	            	if(isChinese){
	            		timeStr= minute+"分";
	            	}else{
	            		timeStr= minute+"m";
	            	}
	            } else {  
	                hour = minute / 60;  
	                if (hour > 99)   
	                minute = minute % 60;
	                if(isChinese){
	                	timeStr = hour + "小时" + minute + "分"; 
	                }else{
	                	timeStr = hour + "h" + minute + "m";
	                }
	            }  
	        }  
	        return timeStr;  
	    }  
	  
	  public static void getCurrentLocal(Context context){
		  isChinese=context.getResources().getConfiguration().locale.getCountry().equals(
					"CN");
	  }
	  

}
