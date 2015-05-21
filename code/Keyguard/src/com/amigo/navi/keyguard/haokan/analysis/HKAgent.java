package com.amigo.navi.keyguard.haokan.analysis;

import android.content.Context;
import com.amigo.navi.keyguard.haokan.Common;
import com.amigo.navi.keyguard.haokan.entity.EventLogger;
import com.amigo.navi.keyguard.haokan.entity.Wallpaper;


public class HKAgent {
    
    private static LoggerThread mLog;
    private static HKAgent mHkAgent = new HKAgent();

    private HKAgent() {
        mLog = LoggerThread.getInstance();
    }
    
    
    public static void onEvent(final Context context, final EventLogger userLog) {
        mLog.onEvent(context, userLog);
    }
    
    public static void onEvent(final Context context, final int imgId, final int typeId, final int event, final int count) {
        mLog.onEvent(context, Common.currentTimeHour(), imgId, typeId, event, count);
    }
    
    public static void onEvent(final Context context, final String dateHour, final int imgId, final int typeId, final int event, final int count) {
        mLog.onEvent(context, dateHour, imgId, typeId, event, count);
    }
    
    public static void onEvent(final Context context, final int imgId, final int typeId, final int event) {
        onEvent(context, imgId, typeId, event, 1);
    }
    
    public static void onEvent(final Context context, final Wallpaper wallpaper,final int event) {
        onEvent(context, wallpaper.getImgId(), wallpaper.getCategory().getTypeId(), event);
    }
    
//    /** 统计开关打开关闭*/
//    public static void onEvent(final Context context, final int evnet,final int value) {
//        onEvent(context, new EventLogger(Common.currentTimeTime(), evnet,  value));
//    }
//    /** test 统计开关打开关闭*/
//    public static void onEvent(final Context context,final String currentTimeTime, final int evnet,final int value) {
//        onEvent(context, new EventLogger(currentTimeTime, evnet,  value));
//    }
    /** 设置页面统计-壁纸更新*/
    public static void onEventWallpaperUpdate(final Context context, final int evnet, final int value) {
        onEvent(context, new EventLogger(Common.currentTimeDate(), evnet,  value));
    }
    
    /** 设置页面统计-下载方式*/
    public static void onEventOnlyWlan(final Context context, final int evnet, final int value) {
        onEvent(context, new EventLogger(Common.currentTimeDate(), evnet,  value));
    }
    
    public static void onEventTimeOnKeyguard(final Context context, final int value) {
        onEvent(context,new EventLogger(Common.currentTimeTime(), Event.TIME_ONKEYGUARD, value));
    }
    
//    public static void onEventTimeSceenOff(final Context context, final int value) {
//        onEvent(context, new EventLogger(Common.currentTimeTime(), Event.TIME_SRCEEN_OFF, value));
//    }
    
    /** player  Event*/
    public static void onEventPlayer(final Context context, final Wallpaper wallpaper) {
        onEvent(context, wallpaper.getImgId(), wallpaper.getCategory().getTypeId(), Event.PLAYER_MUSIC);
    }
    
    /** IMG  Event*/
    public static void onEventIMGTitle(final Context context, final Wallpaper wallpaper) {
        onEvent(context, wallpaper.getImgId(), wallpaper.getCategory().getTypeId(), Event.IMG_CLICK_TITLE);
    }
    
    public static void onEventIMGLink(final Context context, final Wallpaper wallpaper) {
        onEvent(context, wallpaper.getImgId(), wallpaper.getCategory().getTypeId(), Event.IMG_CLICK_LINK);
    }
    
    public static void onEventIMGFavorite(final Context context, final Wallpaper wallpaper) {
        onEvent(context, wallpaper.getImgId(), wallpaper.getCategory().getTypeId(), Event.IMG_FAVORITE);
    }
    // Wallpaper lock
    public static void onEventWallpaperLock(final Context context, final Wallpaper wallpaper) {
        onEvent(context, wallpaper.getImgId(), wallpaper.getCategory().getTypeId(), Event.IMG_LOCK);
    }
    
    public static void onEventScreenOn(final Context context, final Wallpaper wallpaper) {
        if (wallpaper != null) {
            onEvent(context, wallpaper.getImgId(), wallpaper.getCategory().getTypeId(), Event.IMG_SRCEEN_ON);
        }
    }
    
    public static void onEventIMGShow(final Context context, final Wallpaper wallpaper) {
//        onEvent(context, wallpaper.getImgId(), wallpaper.getCategory().getTypeId(), Event.IMG_SHOW);
        onEvent(context, new EventLogger(Common.currentTimeTime(), wallpaper.getImgId(), wallpaper
                .getCategory().getTypeId(), Event.IMG_SHOW, 1, wallpaper.getUrlPv()));
 }
    
    public static void onEventIMGSwitch(final Context context, final Wallpaper wallpaper) {
        onEvent(context, wallpaper.getImgId(), wallpaper.getCategory().getTypeId(), Event.IMG_SWITCH);
    }
    
    /** upload all log to server */
    public static void uploadAllLog() {
        mLog.sendLogMsg();
    }
    
    
}
