package com.amigo.navi.keyguard.haokan.analysis;

public class Event {


    public static final int ON = 1;
    
    public static final int OFF = 0;
    
    public static final int ONLY_WIFI = 1;
    
    public static final int UNLIMITED = 2;
    
    /** 一次性上传最大数量 */
    public static final int MAX_COUNT = 1000;
    
    
    /**
     * 图片展示(亮屏/手动轮播等图片出现过一次+1)
     */
    public static final int IMG_SHOW = 10;
    
    /**
     * 图片点击链接
     */
    public static final int IMG_CLICK_LINK = 11;
    
    /**
     * 图片分享
     */
    public static final int IMG_SHARE = 12;
    /**
     * 图片点赞（默认0，点赞一次+1，取消点赞一次-1）
     */
    public static final int IMG_PRAISE = 13;
    
    /**
     * 取消图片点赞
     */
//    public static final int IMG_UNPRAISE = 14;
    
    /**
     * 图片收藏（默认0，收藏一次+1，取消收藏一次-1）
     */
    public static final int IMG_FAVORITE = 15;
    
    /**
     * 取消图片收藏
     */
//    public static final int IMG_UNFAVORITE = 16;
    
    /**
     * 图片喜欢（默认0，喜欢一次+1，不喜欢一次-1）
     */
    public static final int IMG_LIKE = 17;
    
    /**
     * 图片不喜欢
     */
//    public static final int IMG_UNLIKE = 18;
    
    
    /**
     * 点击图片标题
     */
    public static final int IMG_CLICK_TITLE = 19;
    
    /**
     * 点击音乐播放
     */
    public static final int PLAYER_MUSIC = 20;
    
    
    /**
     * 图片亮屏曝光
     */
    public static final int IMG_SRCEEN_ON = 21;
    
    /**
     * 图片手动切换曝光
     */
    public static final int IMG_SWITCH = 22;
    
    /**
     * 图片设定为亮屏固定显示（count=1）,锁定一次+1
     */
    public static final int IMG_LOCK = 23;
    
    /**
     * 图片停留时间
     */
    public static final int IMG_GAZING = 24;
    
    public static final int IMG_COVERED_BY_NOTI = 25;
    
    public static final int TIME_ON_DETIALACTIVITY = 26;
    /**
     * 自动更新（2开启 1关闭）
     */
    public static final int SETTING_AUTO_UPDATE = 31;
    
    /**
     * 下载网络方式（1 仅wifi  2 任意）
     */
    public static final int SETTING_ONLY_WIFI = 32;
    
    /**
     * 锁屏停留时间；单位毫秒
     */
    public static final int TIME_ONKEYGUARD = 33;
    
    /**
     * 在锁屏界面灭屏时，锁屏停留时间；（从这次亮屏到灭屏的时间）单位秒
     */
//    public static final int TIME_SRCEEN_OFF = 34;
    
    /*
     * 锁屏开关
     */
    
    public static final int SETTING_KEYGUARD_SWITCHER = 35;
    
    public static final int SETTING_IMAGE_TEXT = 36;
    
    public static final int SETTING_LOCALIMG_AS_WALLPAPER = 37;
    
    public static final int NETWORK_REQUEST_WEEKLY_TOTAL_COUNT_2G = 41;
    
    public static final int NETWORK_REQUEST_WEEKLY_SUCCESS_COUNT_2G = 42;
    
    public static final int NETWORK_REQUEST_WEEKLY_MILLIS_COST_2G = 43;
    
    public static final int NETWORK_REQUEST_WEEKLY_THROUGHPUT_2G = 44;
    
    public static final int NETWORK_REQUEST_WEEKLY_TOTAL_COUNT_3G = 45;
    
    public static final int NETWORK_REQUEST_WEEKLY_SUCCESS_COUNT_3G = 46;
    
    public static final int NETWORK_REQUEST_WEEKLY_MILLIS_COST_3G = 47;
    
    public static final int NETWORK_REQUEST_WEEKLY_THROUGHPUT_3G = 48;
    
    public static final int NETWORK_REQUEST_WEEKLY_TOTAL_COUNT_4G = 49;
    
    public static final int NETWORK_REQUEST_WEEKLY_SUCCESS_COUNT_4G = 50;
    
    public static final int NETWORK_REQUEST_WEEKLY_MILLIS_COST_4G = 51;
    
    public static final int NETWORK_REQUEST_WEEKLY_THROUGHPUT_4G = 52;
    
    public static final int NETWORK_REQUEST_WEEKLY_TOTAL_COUNT_WIFI = 53;
    
    public static final int NETWORK_REQUEST_WEEKLY_SUCCESS_COUNT_WIFI = 54;
    
    public static final int NETWORK_REQUEST_WEEKLY_MILLIS_COST_WIFI = 55;
    
    public static final int NETWORK_REQUEST_WEEKLY_THROUGHPUT_WIFI = 56;
    
    public static final int NETWORK_REQUEST_WEEKLY_TOTAL_COUNT = 57;
    
    public static final int NETWORK_REQUEST_WEEKLY_SUCCESS_COUNT = 58;
    
    public static final int NETWORK_REQUEST_WEEKLY_MILLIS_COST = 59;
    
    public static final int NETWORK_REQUEST_WEEKLY_THROUGHPUT = 60;
    
    
    
    public static boolean isOneTimeEvent(int eventId) {
    	if(eventId == SETTING_AUTO_UPDATE) return true;
    	if(eventId == SETTING_ONLY_WIFI) return true;
    	if(eventId == SETTING_KEYGUARD_SWITCHER) return true;
    	if(eventId == SETTING_IMAGE_TEXT) return true;
    	if(eventId == SETTING_LOCALIMG_AS_WALLPAPER) return true;
    	
    	return false;
    }
    
    public static boolean isAccumulatedEvent(int eventId) {
    	return !isOneTimeEvent(eventId);
    }
    
    public static boolean isHourlyAccumulatedEvent(int eventId) {
    	return false;
    }
    
    public static boolean isDailyAccumulatedEvent(int eventId) {
    	return false;
    }
    
    public static boolean isWeeklyAccumulatedEvent(int eventId) {
    	if(eventId >= 41 && eventId <= 60) return true;
    	
    	return false;
    }
   
}
