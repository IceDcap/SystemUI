
package com.amigo.navi.keyguard.haokan.db;

import com.amigo.navi.keyguard.haokan.entity.Category;


public class DataConstant {
    
 
    
    public static final String TABLE_CATEGORY = "category";
    public static final String TABLE_WALLPAPER = "wallpaper";
    public static final String TABLE_STATISTICS = "statistics";
    public static final String TABLE_FAVORITE = "favorite";
    public static final int LOCAL_PHOTO = 3;
    public static final int LOCAL_ASSETS = 1;
    public static final int LOCAL_SYSTEM_ETC = 1;
    public static final int INTERNET = 0;
    public static final int DOWNLOAD_FINISH = 1;
    public static final int NOT_DOWNLOAD = 0;
    public static final String CREATE_CATEGORY_SQL = "create table category (" +
            "type_id integer primary key," +
            "type_name text," +
            "type_icon_url text," +
            "favorite integer not null default 1," +
            CategoryColumns.DOWNLOAD_PICTURE + " integer default 0,"+
            CategoryColumns.SAVE_TYPE + " integer default 0," + 
            CategoryColumns.EN_NAME + " text," + 
            CategoryColumns.SORT + " integer default 0," + 
            CategoryColumns.TODAY_IMAGE + " integer default 1" + 
            ")";
    
    
    public static class CategoryColumns {

        public static final String TYPE_ID = "type_id";
        public static final String TYPE_NAME = "type_name";
        public static final String TYPE_ICON_URL = "type_icon_url";
        public static final String FAVORITE = "favorite";
        public static final String DOWNLOAD_PICTURE = "download_picture";
        
        public static final String TODAY_IMAGE = "today_img";
        public static final String SAVE_TYPE = "save_type";
        public static final String EN_NAME = "en_name";
        public static final String SORT = "sort";
    }
    
    
    public static final int CATEGORY_FAVORITE_TRUE = 1;
    public static final int CATEGORY_FAVORITE_FALSE = 0;
    

    public static final String CREATE_WALLPAPER_SQL = "create table wallpaper (" +
            "type_id integer," +
            "type_name text," +
            "img_id integer," +
            "img_name text," +
            "img_content text," +
            "img_source text," +
            "img_url text," +
            "url_click text," +
            "start_time text," +
            "end_time text," +
            "url_pv text," +
            "isadvert integer," +
            "background_color text," +
            "music_id text," +
            "music_name text," +
            "music_singer text," +
            "music_url text," +
            "music_localpath text," +
            "date text," +
            "festival text,  " +
            WallpaperColumns.DOWNLOAD_PICTURE + " integer default 0," + 
            "favorite integer default 0," +
            "favorite_local_path text," +
            WallpaperColumns.LOCK + " integer default 0," + 
            WallpaperColumns.TODAY_IMAGE + " integer default 1," + 
            WallpaperColumns.REAL_ORDER + " real," + 
            WallpaperColumns.SHOW_ORDER + " real," +
            WallpaperColumns.SHOW_TIME_BEGIN + " text," +
            WallpaperColumns.SHOW_TIME_END + " text," +
            WallpaperColumns.SAVE_TYPE + " integer default 0," + 
            "primary key (img_id, type_id)" +
            ")";

    public static final int WALLPAPER_LOCK = 1;
    public static final int WALLPAPER_NOT_LOCK = 0;
    
    public static final int WALLPAPER_FAVORITE = 1;
    public static final int WALLPAPER_NOT_FAVORITE = 0;
    
    public static final int TODAY_IMAGE = 1;
    public static final int NOT_TODAY_IMAGE = 0;
    
    public static class WallpaperColumns {//19
        
        public static final String TYPE_ID = "type_id";
        public static final String TYPE_NAME = "type_name";
        public static final String IMG_ID = "img_id";
        public static final String IMG_NAME = "img_name";
        public static final String IMG_CONTENT = "img_content";
        public static final String IMG_SOURCE = "img_source";
        public static final String IMG_URL = "img_url";
        public static final String URL_CLICK = "url_click";
        public static final String START_TIME = "start_time";
        public static final String END_TIME = "end_time";
        public static final String URL_PV = "url_pv";
        public static final String ISADVERT = "isadvert";
        public static final String BACKGROUND_COLOR = "background_color";
        public static final String MUSIC_ID = "music_id";
        public static final String MUSIC_NAME = "music_name";
        public static final String MUSIC_SINGER = "music_singer";
        public static final String MUSIC_URL = "music_url";
        public static final String MUSIC_LOCALPATH = "music_localpath";
        public static final String DATE = "date";
        public static final String FESTIVAL = "festival";
        public static final String DOWNLOAD_PICTURE = "download_picture";
        public static final String FAVORITE = "favorite";
        public static final String FAVORITE_LOCAL_PATH = "favorite_local_path";
        public static final String LOCK = "is_lock";
        public static final String TODAY_IMAGE = "today_img";
        public static final String REAL_ORDER = "real_order";
        public static final String SHOW_ORDER = "show_order";
        public static final String SHOW_TIME_BEGIN = "show_time_begin";
        public static final String SHOW_TIME_END = "show_time_end";
        public static final String SAVE_TYPE = "save_type";
    }

    public static final String CREATE_STATISTICS_SQL = "create table statistics (" +
            "_id integer primary key autoincrement," +
            "date_time text," +
            "img_id integer," +
            "type_id integer," +
            "event integer," +
            "count integer," +
            "value integer," +
            "url_pv text" +
            ")";

    
    
    public static class StatisticsColumns {

        public static final String DATE_TIME = "date_time";
        public static final String IMG_ID = "img_id";
        public static final String TYPE_ID = "type_id";
        public static final String EVENT = "event";
        public static final String COUNT = "count";
        public static final String VALUE = "value";
        public static final String URL_PV = "url_pv";

    }
    
    public static final int DOWN_FINISH = 1;
    public static final int DOWN_NOT_FINISH = 0;

     
    

}
