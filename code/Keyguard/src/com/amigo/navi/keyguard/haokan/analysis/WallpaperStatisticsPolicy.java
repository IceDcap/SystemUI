package com.amigo.navi.keyguard.haokan.analysis;

import com.amigo.navi.keyguard.haokan.entity.Wallpaper;

import android.os.SystemClock;
import android.util.Log;


public class WallpaperStatisticsPolicy {
    
    private static long sKeyguardShownMillis = -1;
    private static long sKeyguardNotShownMillis = -1;
    
    private static long sWallpaperShownMillis = -1;
    private static long sWallpaperNotShownMillis = -1;
    
    private static Wallpaper sWallpaperScrollBegin = null;
    private static Wallpaper sWallpaperScrollEnd = null;
    
    private static long getCurrentMillis() {
    	return SystemClock.elapsedRealtime();
    }
    
    public static void onWallpaperShown(Wallpaper wallpaper) {
    	sKeyguardShownMillis = getCurrentMillis();
    	sWallpaperShownMillis = sKeyguardShownMillis;
    }
    
    public static void onWallpaperNotShown(Wallpaper wallpaper) {
//    	if(sKeyguardShownMillis > 0) {
//    		sKeyguardNotShownMillis = getCurrentMillis();
//    		int stayMillis = (int) (sKeyguardNotShownMillis - sKeyguardShownMillis);
//    		HKAgent.onEventTimeOnKeyguard(null, stayMillis);
//    		
//    		sKeyguardNotShownMillis = -1;
//    		sKeyguardShownMillis = -1;
//    	}
    	
    	if(sWallpaperShownMillis < 0) return;
    	
    	sWallpaperNotShownMillis = getCurrentMillis();
    	
    	// compute gazing duration of current wallpaper before wallpaper disappear
    	long gazingDuration = computeGazingDuration();
    	if(gazingDuration > 0) {
    		Log.d("DEBUG_STATISTIC", "onWallpaperNotShown wallpaper " + wallpaper.getImgId() + ", gazingDuration=" + gazingDuration);
    		HKAgent.onEventImageGazingDuration(wallpaper, gazingDuration);
    	}

    	sWallpaperShownMillis = -1;
    }
    
    public static void onWallpaperScrollBegin(Wallpaper wallpaper) {
    	sWallpaperScrollBegin = wallpaper;
    }
    
    public static void onWallpaperScrollEnd(Wallpaper wallpaper) {
    	HKAgent.onEventIMGSwitchedManually(null, wallpaper);
    	
    	if(sWallpaperScrollBegin == null) return;
    	
    	sWallpaperScrollEnd = wallpaper;
    	
    	long gazingDuration = computeGazingDuration();
    	if(gazingDuration > 0) {
    		Log.d("DEBUG_STATISTIC", "onWallpaperScrollBegin wallpaper " + wallpaper.getImgId() + ", gazingDuration=" + gazingDuration);
    		HKAgent.onEventImageGazingDuration(sWallpaperScrollBegin, gazingDuration);
    	}
    	
    	// update for computing gazing duration of next wallpaper
    	sWallpaperShownMillis = getCurrentMillis();
    	
    	sWallpaperScrollBegin = null;
    	sWallpaperScrollEnd = null;
    }
    
    private static long computeGazingDuration() {
    	long gazingDuration = 0;
    	
    	boolean validWallpapers = sWallpaperScrollBegin != null && sWallpaperScrollEnd != null;
    	boolean validBeginMillis = sWallpaperShownMillis > 0;
    	if(validWallpapers && validBeginMillis) {
    		int beginImageId = sWallpaperScrollBegin.getImgId();
        	int endImageId = sWallpaperScrollEnd.getImgId();
        	if(beginImageId != endImageId) {
        		gazingDuration = getCurrentMillis() - sWallpaperShownMillis;
        	}
    	}
    	
    	return gazingDuration;
    }
     
}
