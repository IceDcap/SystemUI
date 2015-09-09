
package com.amigo.navi.keyguard;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.amigo.navi.keyguard.haokan.Common;

public class Guide {

    private static boolean TEST = false;

    public enum GuideState {
        IDLE, LONG_PRESS, CLICK_TITLE, SLIDE_AROUND, SCROLL_UP, NEW_WALLPAPER, SLIDE_FEEDBACK
    }

    private static GuideState guideState = GuideState.IDLE;

    public static final String PREFERENCE_NAME = "com.amigo.navi.keyguard_guide";

    public static final String GUIDE_LONG_PRESS = "guide_long_press";
    public static final String GUIDE_CLICK_TITLE = "guide_click_title";
    public static final String GUIDE_SLIDE_AROUND = "guide_slide_around";
    public static final String GUIDE_SCROLL_UP = "guide_scroll_up";
    public static final String GUIDE_NEW_WALLPAPER = "guide_new_wallpaper";
    public static final String GUIDE_SLIDE_FEEDBACK = "guide_slide_feedback";
    
    private static final String PICS_DOWNLOAD_TIMES = "pics_dl_times";
    private static final String REGUIDE_CLICK_TITLE_TIMES = "re-guide_click_title";
    

    private static boolean NEED_GUIDE_LONG_PRESS = true;
    private static boolean NEED_GUIDE_CLICK_TITLE = true;
    private static boolean NEED_GUIDE_SLIDE_AROUND = true;
    private static boolean NEED_GUIDE_SCROLL_UP = true;
    
    private static boolean NEED_NEW_WALLPAPER = true;
    private static boolean NEED_SLIDE_FEEDBACK = true;
    
    private static final int[] REGUIDE_CLICK_TITLE_DOWNLOAD_TIMES = {3, 5};
    private static final int PRESET_REGUIDE_TIMES = REGUIDE_CLICK_TITLE_DOWNLOAD_TIMES.length;
    private static int sClickTextReguideTimes = 0;

    public static boolean needGuideScrollUp() {
        return NEED_GUIDE_SCROLL_UP;
    }

    public static void setNeedGuideScrollUp(boolean need) {
        NEED_GUIDE_SCROLL_UP = need;
    }

    public static boolean needGuideLongPress() {
        return NEED_GUIDE_LONG_PRESS;
    }

    public static void setNeedGuideLongPress(boolean need) {
        NEED_GUIDE_LONG_PRESS = need;
    }

    public static boolean needGuideClickTitle() {
        return NEED_GUIDE_CLICK_TITLE;
    }

    public static void setNeedGuideClickTitle(boolean need) {
        NEED_GUIDE_CLICK_TITLE = need;
    }

    public static boolean needGuideSlideAround() {
        return NEED_GUIDE_SLIDE_AROUND;
    }

    public static void setNeedGuideSlideAround(boolean need) {
        NEED_GUIDE_SLIDE_AROUND = need;
    }

    public static boolean needGuideNewWallpaper() {
        return NEED_NEW_WALLPAPER;
    }

    public static void setNeedGuideNewWallpaper(boolean need) {
        NEED_NEW_WALLPAPER = need;
    }

    public static boolean needGuideSlideFeedBack() {
        return NEED_SLIDE_FEEDBACK;
    }

    public static void setNeedGuideSlideFeedBack(boolean need) {
        NEED_SLIDE_FEEDBACK = need;
    }

    public static boolean getBooleanSharedConfig(Context context, String key, boolean defValue) {
        SharedPreferences sp = context.getSharedPreferences(
                PREFERENCE_NAME, Context.MODE_PRIVATE);
        boolean value = sp.getBoolean(key, defValue);
        return value;
    }

    public static boolean setBooleanSharedConfig(Context context, String key, boolean value) {
        SharedPreferences sp = context.getSharedPreferences(
                PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }
    
    public static void increaseDownloadTimes(Context context) {
    	if(isClickTextReguideTimesExpired(context)) return;
    	
    	SharedPreferences sp = context.getSharedPreferences(
                PREFERENCE_NAME, Context.MODE_PRIVATE);
    	boolean isClickTitleGuideOn = sp.getBoolean(GUIDE_CLICK_TITLE, true);
    	if(!isClickTitleGuideOn) {
    		int times = sp.getInt(PICS_DOWNLOAD_TIMES, 0) + 1;
    		sp.edit().putInt(PICS_DOWNLOAD_TIMES, times).apply();
    		
    		onDonwloadTimesIncreased(context, times);
    	}
    }
    
    public static void cleanDownloadTimes(Context context) {
    	if(isClickTextReguideTimesExpired(context)) return;
    	
    	SharedPreferences sp = context.getSharedPreferences(
                PREFERENCE_NAME, Context.MODE_PRIVATE);
    	int times = sp.getInt(PICS_DOWNLOAD_TIMES, 0);
    	if(times > 0) {
    		sp.edit().putInt(PICS_DOWNLOAD_TIMES, 0).apply();
    	}
    }
    
    private static void onDonwloadTimesIncreased(Context context, int times) {
    	int presetTimes = REGUIDE_CLICK_TITLE_DOWNLOAD_TIMES[sClickTextReguideTimes];
    	if(times >= presetTimes) {
    		setNeedGuideClickTitle(true);
    		
    		sClickTextReguideTimes++;
    		SharedPreferences sp = context.getSharedPreferences(
    				PREFERENCE_NAME, Context.MODE_PRIVATE);
    		sp.edit()
    			.putBoolean(GUIDE_CLICK_TITLE, true)
    			.putInt(REGUIDE_CLICK_TITLE_TIMES, sClickTextReguideTimes)
    			.putInt(PICS_DOWNLOAD_TIMES, 0)
    			.apply();
    	}
    }
    
    private static boolean isClickTextReguideTimesExpired(Context context) {
    	if(sClickTextReguideTimes < PRESET_REGUIDE_TIMES) {
    		SharedPreferences sp = context.getSharedPreferences(
                    PREFERENCE_NAME, Context.MODE_PRIVATE);
    		sClickTextReguideTimes = sp.getInt(REGUIDE_CLICK_TITLE_TIMES, 0);
    	}
    	
    	boolean isExpired = sClickTextReguideTimes >= PRESET_REGUIDE_TIMES;
    	return isExpired;
    }

    public static void init(Context context) {

        SharedPreferences preferences = context.getSharedPreferences(
                PREFERENCE_NAME, Context.MODE_PRIVATE);

        setNeedGuideLongPress(preferences.getBoolean(GUIDE_LONG_PRESS, true));
        setNeedGuideClickTitle(preferences.getBoolean(GUIDE_CLICK_TITLE, true));
        setNeedGuideSlideAround(preferences.getBoolean(GUIDE_SLIDE_AROUND, true));
        setNeedGuideScrollUp(preferences.getBoolean(GUIDE_SCROLL_UP, true));

        DebugLog.d("guide", "Guide init needGuideScrollUp=" + needGuideScrollUp()
                + " needGuideLongPress=" + needGuideLongPress() + " needGuideNewWallpaper="
                + needGuideNewWallpaper() + " needGuideSlideAround=" + needGuideSlideAround()
                + " needGuideSlideFeedBack=" + needGuideSlideFeedBack());
        
    /*    if (Common.isPowerSaverMode()) {
            NEED_GUIDE_LONG_PRESS = false;
            NEED_GUIDE_CLICK_TITLE = false;
            NEED_GUIDE_SLIDE_AROUND = false;
            NEED_GUIDE_SCROLL_UP = false;
            NEED_NEW_WALLPAPER = false;
            NEED_SLIDE_FEEDBACK = false;
        }*/
        

        if (TEST) {
            NEED_GUIDE_LONG_PRESS = false;
            NEED_GUIDE_CLICK_TITLE = false;
            NEED_GUIDE_SLIDE_AROUND = false;
            NEED_GUIDE_SCROLL_UP = false;
            NEED_NEW_WALLPAPER = false;
            NEED_SLIDE_FEEDBACK = false;
 
        }

    }
    
    
 
    public static void setGuideEnable(Context context, boolean enable) {
        
        if (enable) {
            SharedPreferences preferences = context.getSharedPreferences(
                    PREFERENCE_NAME, Context.MODE_PRIVATE);

            setNeedGuideLongPress(preferences.getBoolean(GUIDE_LONG_PRESS, true));
            setNeedGuideClickTitle(preferences.getBoolean(GUIDE_CLICK_TITLE, true));
            setNeedGuideSlideAround(preferences.getBoolean(GUIDE_SLIDE_AROUND, true));
            setNeedGuideScrollUp(preferences.getBoolean(GUIDE_SCROLL_UP, true));
            NEED_NEW_WALLPAPER = true;
            NEED_SLIDE_FEEDBACK = true;
            
        }else {
            NEED_GUIDE_LONG_PRESS = false;
            NEED_GUIDE_CLICK_TITLE = false;
            NEED_GUIDE_SLIDE_AROUND = false;
            NEED_GUIDE_SCROLL_UP = false;
            NEED_NEW_WALLPAPER = false;
            NEED_SLIDE_FEEDBACK = false;
        }
        
    }
    

    public static GuideState getGuideState() {
        return Guide.guideState;
    }

    public static void setGuideState(GuideState state) {
        Guide.guideState = state;
    }

    public static boolean isIdle() {
        return Guide.guideState == GuideState.IDLE;
    }

    public static void resetIdle() {
        Guide.guideState = GuideState.IDLE;
    }
   

}
