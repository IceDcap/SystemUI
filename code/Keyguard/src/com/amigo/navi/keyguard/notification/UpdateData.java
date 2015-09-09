package com.amigo.navi.keyguard.notification;

import android.service.notification.StatusBarNotification;
import android.service.notification.NotificationListenerService.RankingMap;

public class UpdateData {

	 public StatusBarNotification notification;
	 public RankingMap rankingMap ;
	 public UpdateData(StatusBarNotification n, RankingMap r) {
		notification=n;
		rankingMap=r;
	 }
}
