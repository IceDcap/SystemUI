package com.amigo.navi.keyguard.haokan.analysis;

public class Event {


    public static final int ON = 1;
    
    public static final int OFF = 0;
    
    public static final int ONLY_WIFI = 1;
    
    public static final int UNLIMITED = 2;
    
    /** 一次性上传最大数量 */
    public static final int MAX_COUNT = 1000;
    
    
    /**
     * 图片展示(自动轮播/亮屏/手动轮播等图片出现过一次+1)
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
     * 自动更新（1开启 0关闭）
     */
    public static final int SETTING_UPDATE = 31;
    
    /**
     * 下载网络方式（1 仅wifi  2 任意）
     */
    public static final int SETTING_DOWNLOAD = 32;
    
    /**
     * 锁屏解锁时，锁屏停留时间；（从这次亮屏到解锁的时间）单位秒
     */
    public static final int TIME_UNLOCK = 33;
    
    /**
     * 在锁屏界面灭屏时，锁屏停留时间；（从这次亮屏到灭屏的时间）单位秒
     */
    public static final int TIME_SRCEEN_OFF = 34;
    
   
}
