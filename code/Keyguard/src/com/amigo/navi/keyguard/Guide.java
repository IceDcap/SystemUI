
package com.amigo.navi.keyguard;

import android.content.Context;
import android.content.SharedPreferences;

public class Guide {

    public static final String PREFERENCE_NAME   = "com.amigo.navi.keyguard_guide";
    
    public static final String GUIDE_LONG_PRESS  = "guide_long_press";
    public static final String GUIDE_CLICK_TITLE = "guide_click_title";
    public static final String GUIDE_SCROLL_UP = "guide_scroll_up";

    private static boolean NEED_GUIDE_LONG_PRESS = true;
    private static boolean NEED_GUIDE_CLICK_TITLE = true;
    private static boolean NEED_GUIDE_SCROLL_UP = true;
    
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
        setNeedGuideScrollUp(preferences.getBoolean(GUIDE_SCROLL_UP, true));
    }
    

}
