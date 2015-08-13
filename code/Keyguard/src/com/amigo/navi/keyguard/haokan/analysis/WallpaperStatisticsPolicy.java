package com.amigo.navi.keyguard.haokan.analysis;

import com.amigo.navi.keyguard.haokan.UIController;
import com.amigo.navi.keyguard.haokan.entity.Wallpaper;
import com.amigo.navi.keyguard.modules.KeyguardNotificationModule;

import android.os.SystemClock;
import android.util.Log;


public class WallpaperStatisticsPolicy {
    
    private static long sKeyguardShownMillis = -1;
//    private static long sKeyguardNotShownMillis = -1;
    
    private static long sWallpaperShownMillis = -1;
//    private static long sWallpaperNotShownMillis = -1;
    
    private static Wallpaper sWallpaperScrollBegin = null;
//    private static Wallpaper sWallpaperScrollEnd = null;
    
//    private static int sNotificationCount = 0;
    private static long sNotificationShownMillis = -1;
    private static long sNotificationNotShownMillis = -1;
    private static Wallpaper sWallpaperWhenNotificationShown = null;
    private static long sDetailShownMillis = -1;
    private static long sDetailNotShownMillis = -1;
    
    private static long getCurrentMillis() {
    	return SystemClock.elapsedRealtime();
    }
    
    public static void onWallpaperShown(Wallpaper wallpaper) {
    	sKeyguardShownMillis = getCurrentMillis();
    	sWallpaperShownMillis = sKeyguardShownMillis;
    	
    	KeyguardNotificationModule module = KeyguardNotificationModule.getInstance();
    	if(module != null && module.hasNotification()) {
    		onNotificationShown();
    	}
    }
    
    public static void onWallpaperNotShown(Wallpaper wallpaper) {
    	if(sWallpaperShownMillis < 0) return;
    	
    	long gazingDuration = getCurrentMillis() - sWallpaperShownMillis;
    	if(gazingDuration > 0) {
    		Log.d("DEBUG_STATISTIC", "onWallpaperNotShown wallpaper " + wallpaper.getImgId() + ", gazingDuration=" + gazingDuration);
    		HKAgent.onEventImageGazingDuration(wallpaper, gazingDuration);
    	}
    	
    	onNotificationHide();

    	sWallpaperShownMillis = -1;
    }
    
    public static void onWallpaperScrollBegin(Wallpaper wallpaper) {
    	sWallpaperScrollBegin = wallpaper;
    }
    
    public static void onWallpaperScrollEnd(Wallpaper wallpaper) {
    	HKAgent.onEventIMGSwitchedManually(null, wallpaper);
    	
    	if(sWallpaperScrollBegin == null) return;
    	
    	long gazingDuration = getCurrentMillis() - sWallpaperShownMillis;
    	if(gazingDuration > 0) {
    		Log.d("DEBUG_STATISTIC", "onWallpaperScrollBegin wallpaper " + wallpaper.getImgId() + ", gazingDuration=" + gazingDuration);
    		HKAgent.onEventImageGazingDuration(sWallpaperScrollBegin, gazingDuration);
    	}
    	
    	// update for computing gazing duration of next wallpaper
    	sWallpaperShownMillis = getCurrentMillis();
    	
    	sWallpaperScrollBegin = null;
    }
    
    public static void onKeyguardNotiCountChanged(int notiCount) {
    	if(notiCount > 0) {
    		onNotificationShown();
    	} else if(notiCount == 0) {
    		onNotificationHide();
    	}
    }
    
    private static void onNotificationShown() {
    	if(sWallpaperWhenNotificationShown == null) {
    		sNotificationShownMillis = getCurrentMillis();
    		sWallpaperWhenNotificationShown = UIController.getInstance().getmCurrentWallpaper();
    	}
    }
    
    private static void onNotificationHide() {
    	if(sWallpaperWhenNotificationShown != null) {
    		sNotificationNotShownMillis = getCurrentMillis();
    		int coveredMillis = (int) (sNotificationNotShownMillis - sNotificationShownMillis);
    		HKAgent.onEventImageCoveredByNotification(sWallpaperWhenNotificationShown, coveredMillis);
    		
    		sNotificationShownMillis = -1;
    		sNotificationNotShownMillis = -1;
    		sWallpaperWhenNotificationShown = null;
    	}
    }
    
    public static void onDetialActivityShown() {
        sDetailShownMillis = getCurrentMillis();
    }
    
    public static void onDetialActivityNotShown() {
        
        if(sDetailShownMillis < 0) return;
        
        sDetailNotShownMillis = getCurrentMillis();
        int sDetialShowDuration = (int) (sDetailNotShownMillis - sDetailShownMillis);
        HKAgent.onEventDetialActivityShowDuriation(sDetialShowDuration);
        
        sDetailShownMillis = -1;
        sDetailNotShownMillis = -1;
    }
    
}
