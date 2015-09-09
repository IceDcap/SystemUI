package com.amigo.navi.keyguard.notification;

import java.util.HashMap;


import com.amigo.navi.keyguard.notification.NotificationData.Entry;

import android.os.Handler;
import android.os.Looper;
import android.service.notification.StatusBarNotification;
import android.util.ArrayMap;



public class UpdateNotificationCache {
	
	 private final HashMap<String, UpdateData> mUpdateNotificationDatas = new HashMap<String, UpdateData>();
     private static final int UPDATE_NOTIFICATION=0;

	 
	 
	 
	 public UpdateNotificationCache() {
	 }

	public UpdateData getUpdateNotificationData(String key){
		 return mUpdateNotificationDatas.get(key);
	}
	 
	 public void removeUpdateNotificationData(String key){
		 mUpdateNotificationDatas.remove(key);
	 }
	 
	 public void addUpdateNotificationData(UpdateData updateData){
		 mUpdateNotificationDatas.put(updateData.notification.getKey(), updateData);
	 }
	 
	 public HashMap<String, UpdateData> getNotificationDatas(){
		 return mUpdateNotificationDatas;
	 }

    
	 
}
