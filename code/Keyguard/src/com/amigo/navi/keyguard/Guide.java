
package com.amigo.navi.keyguard;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

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

    private static boolean NEED_GUIDE_LONG_PRESS = true;
    private static boolean NEED_GUIDE_CLICK_TITLE = true;
    private static boolean NEED_GUIDE_SLIDE_AROUND = true;
    private static boolean NEED_GUIDE_SCROLL_UP = true;
    private static boolean NEED_NEW_WALLPAPER = true;
    private static boolean NEED_SLIDE_FEEDBACK = true;

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

    public static void init(Context context) {

        SharedPreferences preferences = context.getSharedPreferences(
                PREFERENCE_NAME, Context.MODE_PRIVATE);

        setNeedGuideLongPress(preferences.getBoolean(GUIDE_LONG_PRESS, true));
        setNeedGuideClickTitle(preferences.getBoolean(GUIDE_CLICK_TITLE, true));
        setNeedGuideSlideAround(preferences.getBoolean(GUIDE_SLIDE_AROUND, true));
        setNeedGuideScrollUp(preferences.getBoolean(GUIDE_SCROLL_UP, true));
        setNeedGuideSlideFeedBack(preferences.getBoolean(GUIDE_SLIDE_FEEDBACK, true));

        Log.v("guide", "Guide init needGuideScrollUp=" + needGuideScrollUp()
                + " needGuideLongPress=" + needGuideLongPress() + " needGuideNewWallpaper="
                + needGuideNewWallpaper() + " needGuideSlideAround=" + needGuideSlideAround()
                + " needGuideSlideFeedBack=" + needGuideSlideFeedBack());

        if (TEST) {
            NEED_GUIDE_LONG_PRESS = false;
            NEED_GUIDE_CLICK_TITLE = false;
            NEED_GUIDE_SLIDE_AROUND = false;
            NEED_GUIDE_SCROLL_UP = false;
            NEED_NEW_WALLPAPER = false;
            NEED_SLIDE_FEEDBACK = false;
            
            
//            NEED_GUIDE_LONG_PRESS = true;
//            NEED_GUIDE_CLICK_TITLE = true;
//            NEED_GUIDE_SLIDE_AROUND = true;
//            NEED_GUIDE_SCROLL_UP = true;
//            NEED_NEW_WALLPAPER = true;
//            NEED_SLIDE_FEEDBACK = true;
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
