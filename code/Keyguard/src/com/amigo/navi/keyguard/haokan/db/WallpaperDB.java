package com.amigo.navi.keyguard.haokan.db;

import java.util.ArrayList;
import java.util.List;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.haokan.db.DataConstant.WallpaperColumns;
import com.amigo.navi.keyguard.haokan.entity.Category;
import com.amigo.navi.keyguard.haokan.entity.Music;
import com.amigo.navi.keyguard.haokan.entity.Wallpaper;
import com.amigo.navi.keyguard.haokan.entity.WallpaperList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WpsInfo;
import android.text.TextUtils;
import android.util.Log;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.haokan.db.DataConstant.CategoryColumns;
import com.amigo.navi.keyguard.haokan.db.DataConstant.WallpaperColumns;
import com.amigo.navi.keyguard.haokan.entity.Category;
import com.amigo.navi.keyguard.haokan.entity.Music;
import com.amigo.navi.keyguard.haokan.entity.Wallpaper;
import com.amigo.navi.keyguard.haokan.entity.WallpaperList;


public class WallpaperDB extends BaseDB{
    private static final String TAG = "WallpaperDB";
    public static final String IS_FINISH = WallpaperColumns.DOWNLOAD_PICTURE;
    public static final String TABLE_NAME = DataConstant.TABLE_WALLPAPER;

    public WallpaperDB(Context context) {
        super(context);
    }
    
    private static WallpaperDB sInstance;

    public synchronized static WallpaperDB getInstance(Context context){
        if(sInstance == null){
            sInstance = new WallpaperDB(context);
        }
        return sInstance;
    }
    
    
    public synchronized  void insertWallpapers(WallpaperList list) {
        final SQLiteDatabase db = mWritableDatabase;
        db.beginTransaction();
        insertWallpapersNoTransaction(list);
        db.setTransactionSuccessful();  
        db.endTransaction();  
        
    }


    /**
     * @param list
     * @param db
     */
    private  synchronized  void insertWallpapersNoTransaction(WallpaperList list) {
        final SQLiteDatabase db = mWritableDatabase;
        int index = 1;
        for (Wallpaper wallpaper : list) {
            ContentValues values = getContentValue(index, wallpaper);
            long id = db.insert(DataConstant.TABLE_WALLPAPER, null, values);
            index++;
        }
    }

    public void replaceWallpapers(WallpaperList list) {
        final SQLiteDatabase db = mWritableDatabase;
        db.beginTransaction();
        replaceWallpapersNoTransaction(list);
        db.setTransactionSuccessful();  
        db.endTransaction();  
        
    }

    
    private void replaceWallpapersNoTransaction(WallpaperList list) {
        final SQLiteDatabase db = mWritableDatabase;
        int index = 1;
        for (Wallpaper wallpaper : list) {
            ContentValues values = getContentValue(index, wallpaper);
            long id = db.replace(DataConstant.TABLE_WALLPAPER, null, values);
            index++;
        }
    }

	private ContentValues getContentValue(int index, Wallpaper wallpaper) {
		ContentValues values = new ContentValues();
		
		values.put(DataConstant.WallpaperColumns.TYPE_ID, wallpaper.getCategory().getTypeId());
		values.put(DataConstant.WallpaperColumns.TYPE_NAME, wallpaper.getCategory().getTypeName());
		values.put(DataConstant.WallpaperColumns.IMG_ID, wallpaper.getImgId());
		values.put(DataConstant.WallpaperColumns.IMG_NAME, wallpaper.getImgName());
		values.put(DataConstant.WallpaperColumns.IMG_CONTENT, wallpaper.getImgContent());
		values.put(DataConstant.WallpaperColumns.IMG_SOURCE, wallpaper.getImgSource());
		values.put(DataConstant.WallpaperColumns.IMG_URL, wallpaper.getImgUrl());
		values.put(DataConstant.WallpaperColumns.URL_CLICK, wallpaper.getUrlClick());
		values.put(DataConstant.WallpaperColumns.START_TIME, wallpaper.getStartTime());
		values.put(DataConstant.WallpaperColumns.END_TIME, wallpaper.getEndTime());
		values.put(DataConstant.WallpaperColumns.URL_PV, wallpaper.getUrlPv());
		values.put(DataConstant.WallpaperColumns.ISADVERT, wallpaper.getIsAdvert());
		values.put(DataConstant.WallpaperColumns.BACKGROUND_COLOR, wallpaper.getBackgroundColor());
		if(wallpaper.getMusic() != null){
			values.put(DataConstant.WallpaperColumns.MUSIC_ID, wallpaper.getMusic().getMusicId());
			values.put(DataConstant.WallpaperColumns.MUSIC_NAME, wallpaper.getMusic().getmMusicName());
			values.put(DataConstant.WallpaperColumns.MUSIC_SINGER, wallpaper.getMusic().getmArtist());
			values.put(DataConstant.WallpaperColumns.MUSIC_URL, wallpaper.getMusic().getPlayerUrl());
		}
		values.put(DataConstant.WallpaperColumns.DATE, wallpaper.getDate());
		values.put(DataConstant.WallpaperColumns.FESTIVAL, wallpaper.getFestival());
		values.put(DataConstant.WallpaperColumns.TODAY_IMAGE,DataConstant.TODAY_IMAGE);
		values.put(DataConstant.WallpaperColumns.REAL_ORDER, index);
		values.put(DataConstant.WallpaperColumns.SHOW_ORDER, index);
		values.put(DataConstant.WallpaperColumns.SHOW_TIME_BEGIN, wallpaper.getShowTimeBegin());
		values.put(DataConstant.WallpaperColumns.SHOW_TIME_END, wallpaper.getShowTimeEnd());
		if(wallpaper.isLocked()){
			values.put(DataConstant.WallpaperColumns.LOCK,1);
		}else{
			values.put(DataConstant.WallpaperColumns.LOCK,0);
		}
		values.put(DataConstant.WallpaperColumns.SAVE_TYPE, wallpaper.getType());
		values.put(DataConstant.WallpaperColumns.DOWNLOAD_PICTURE, wallpaper.getDownloadFinish());
		return values;
	}
    
    public void addMusicLocalPath(Music music) {

        final SQLiteDatabase db = mWritableDatabase;
 
        db.execSQL("update wallpaper set music_localpath = '" + music.getLocalPath()
                + "' where type_id = ? and img_id = ? and music_id = ? ", new Object[] {
                music.getTypeId(), music.getImgId(), music.getMusicId()
        });

    }
    
    
    public WallpaperList queryByCategory(Category category) {
        
        final SQLiteDatabase db = mReadableDatabase;
        Cursor cursor = db.rawQuery("select * from wallpaper where typeId = ? order by imgId",
                new String[] {
                    String.valueOf(category.getTypeId())
                });
        WallpaperList list = cursorToWallpaperList(cursor);
        closeCursor(cursor);
        return list;
    }
    
    public WallpaperList queryAll() {
        final SQLiteDatabase db = mReadableDatabase;
        Cursor cursor = db.rawQuery("select * from wallpaper",null);
        WallpaperList list = cursorToWallpaperList(cursor);
        closeCursor(cursor);
        return list;
    }
    
    public WallpaperList queryExcludeFixedWallpaper() {
        DebugLog.d(TAG,"queryExcludeFixedWallpaper 1");
        final SQLiteDatabase db = mReadableDatabase;
        String sql = "select * from wallpaper where " + DataConstant.WallpaperColumns.LOCK + " = ?";
        Cursor cursor = db.rawQuery(sql,
                new String[] {
                    String.valueOf(DataConstant.WALLPAPER_NOT_LOCK)
                });
        DebugLog.d(TAG,"queryExcludeFixedWallpaper 2");
        WallpaperList list = cursorToWallpaperList(cursor);
        closeCursor(cursor);
        return list;
    }
    
    public WallpaperList queryPicturesNoDownLoad() {
        final SQLiteDatabase db = mReadableDatabase;
        Cursor cursor = db.rawQuery("select * from wallpaper where " +
        DataConstant.WallpaperColumns.DOWNLOAD_PICTURE + " = ?",new String[] {
                String.valueOf(DataConstant.DOWN_NOT_FINISH)
            });
        WallpaperList list = cursorToWallpaperList(cursor);
        closeCursor(cursor);
        return list;
    }
    
    public WallpaperList queryPicturesDownLoaded(){
        final SQLiteDatabase db = mReadableDatabase;
        Cursor cursor = db.rawQuery("select * from wallpaper where " +
        DataConstant.WallpaperColumns.DOWNLOAD_PICTURE + " = ? order by " + 
        DataConstant.WallpaperColumns.SHOW_ORDER + " asc",new String[] {
                String.valueOf(DataConstant.DOWN_FINISH)
            });
        WallpaperList list = cursorToWallpaperList(cursor);
        closeCursor(cursor);
        return list;
    }
    
    public WallpaperList queryPicturesDownLoadedNotLock(){
        final SQLiteDatabase db = mReadableDatabase;
        Cursor cursor = db.rawQuery("select * from wallpaper where " +
        DataConstant.WallpaperColumns.DOWNLOAD_PICTURE + " = ? and " + 
        DataConstant.WallpaperColumns.LOCK + " = ?",new String[] {
                String.valueOf(DataConstant.DOWN_FINISH),
                String.valueOf(DataConstant.WALLPAPER_NOT_LOCK)
            });
        WallpaperList list = cursorToWallpaperList(cursor);
        closeCursor(cursor);
        return list;
    }
    
    public Wallpaper queryPicturesDownLoadedLock(){
        DebugLog.d(TAG,"queryPicturesDownLoadedLock");
        final SQLiteDatabase db = mReadableDatabase;
        Cursor cursor = db.rawQuery("select * from wallpaper where " +
        DataConstant.WallpaperColumns.DOWNLOAD_PICTURE + " = ? and " + 
        DataConstant.WallpaperColumns.LOCK + " = ?",new String[] {
                String.valueOf(DataConstant.DOWN_FINISH),
                String.valueOf(DataConstant.WALLPAPER_LOCK)
            });
        Wallpaper wallpaper = null;
        DebugLog.d(TAG,"queryPicturesDownLoadedLock2");
        if(cursor != null && cursor.moveToFirst()){
            DebugLog.d(TAG,"queryPicturesDownLoadedLock3");
            wallpaper = queryWallpaper(cursor);
        }
        DebugLog.d(TAG,"queryPicturesDownLoadedLock4");
        closeCursor(cursor);
        DebugLog.d(TAG,"queryPicturesDownLoadedLock5");
        return wallpaper;
    }
    
    public ArrayList<String> queryAllUrl(){
        final SQLiteDatabase db = mReadableDatabase;
        Cursor cursor = db.rawQuery("select img_url from wallpaper",null);
        ArrayList<String> list = new ArrayList<String>();
        
        while (cursor != null && cursor.moveToNext()) {
            String imgUrl = cursor.getString(cursor.getColumnIndex(DataConstant.WallpaperColumns.IMG_URL));                 
            list.add(imgUrl);
        }
        if(cursor != null){
            cursor.close();
        }
        return list;
    }
    
    private WallpaperList cursorToWallpaperList(Cursor cursor) {

        WallpaperList list = new WallpaperList();
        
        while (cursor.moveToNext()) {
            Wallpaper wallpaper = queryWallpaper(cursor);
            list.add(wallpaper);
        }
        closeCursor(cursor);
        return list;
        
    }


    /**
     * @param cursor
     * @return
     */
    private Wallpaper queryWallpaper(Cursor cursor) {
        Wallpaper wallpaper = new Wallpaper();
        
        int imgId = cursor.getInt(cursor.getColumnIndex(WallpaperColumns.IMG_ID));
        wallpaper.setImgId(imgId);
        
        String imgName = cursor.getString(cursor.getColumnIndex(WallpaperColumns.IMG_NAME));
        wallpaper.setImgName(imgName);
        
        String imgContent = cursor.getString(cursor.getColumnIndex(WallpaperColumns.IMG_CONTENT));
        wallpaper.setImgContent(imgContent);
        
        String imgSource = cursor.getString(cursor.getColumnIndex(WallpaperColumns.IMG_SOURCE));
        wallpaper.setImgSource(imgSource);
        
        String imgUrl = cursor.getString(cursor.getColumnIndex(WallpaperColumns.IMG_URL));
        wallpaper.setImgUrl(imgUrl);
        
        String urlClick = cursor.getString(cursor.getColumnIndex(WallpaperColumns.URL_CLICK));
        wallpaper.setUrlClick(urlClick);
        
        String startTime = cursor.getString(cursor.getColumnIndex(WallpaperColumns.START_TIME));
        wallpaper.setStartTime(startTime);
        
        String endTime = cursor.getString(cursor.getColumnIndex(WallpaperColumns.END_TIME));
        wallpaper.setEndTime(endTime);
        
        String urlPv = cursor.getString(cursor.getColumnIndex(WallpaperColumns.URL_PV));
        wallpaper.setUrlPv(urlPv);
        
        int isAdvert = cursor.getInt(cursor.getColumnIndex(WallpaperColumns.ISADVERT));
        wallpaper.setIsAdvert(isAdvert);
        
        String backgroundColor = cursor.getString(cursor.getColumnIndex(WallpaperColumns.BACKGROUND_COLOR));
        wallpaper.setBackgroundColor(backgroundColor);
        
        int lockState = cursor.getInt(cursor.getColumnIndex(WallpaperColumns.LOCK));
        if(lockState == 0){
            wallpaper.setLocked(false);
        }else{
            wallpaper.setLocked(true);
        }
        
        int isToday = cursor.getInt(cursor.getColumnIndex(WallpaperColumns.TODAY_IMAGE));
        wallpaper.setIsTodayWallpaper(isToday);
        
        float realOrder = cursor.getFloat(cursor.getColumnIndex(WallpaperColumns.REAL_ORDER));
        wallpaper.setRealOrder(realOrder);
        
        float showOrder = cursor.getFloat(cursor.getColumnIndex(WallpaperColumns.SHOW_ORDER));
        wallpaper.setShowOrder(showOrder);
        
        Category mCategory = new Category();
        int typeId = cursor.getInt(cursor.getColumnIndex(WallpaperColumns.TYPE_ID));
        mCategory.setTypeId(typeId);
        
        String typeName = cursor.getString(cursor.getColumnIndex(WallpaperColumns.TYPE_NAME));
        mCategory.setTypeName(typeName);
        wallpaper.setCategory(mCategory);
        
        String musicId = cursor.getString(cursor.getColumnIndex(WallpaperColumns.MUSIC_ID));
        String musicName = cursor.getString(cursor.getColumnIndex(WallpaperColumns.MUSIC_NAME));
        
        if (!TextUtils.isEmpty(musicId) || !TextUtils.isEmpty(musicName)) {
            Music music = new Music();
            music.setTypeId(typeId);
            music.setImgId(imgId);
            music.setMusicId(musicId);
            music.setmMusicName(musicName);
            
            String musicSinger = cursor.getString(cursor.getColumnIndex(WallpaperColumns.MUSIC_SINGER));
            music.setmArtist(musicSinger);
            
            String musicUrl = cursor.getString(cursor.getColumnIndex(WallpaperColumns.MUSIC_URL));
            music.setPlayerUrl(musicUrl);
            music.setDownLoadUrl(musicUrl);
            String musicLocalPath = cursor.getString(cursor.getColumnIndex(WallpaperColumns.MUSIC_LOCALPATH));
            
            music.setLocal(musicLocalPath != null);
            music.setLocalPath(musicLocalPath);
            wallpaper.setMusic(music);
        }else {
            wallpaper.setMusic(null);
        }
        
        
        String date = cursor.getString(cursor.getColumnIndex(WallpaperColumns.DATE));
        wallpaper.setDate(date);
        
        String festival = cursor.getString(cursor.getColumnIndex(WallpaperColumns.FESTIVAL));
        wallpaper.setFestival(festival);
        
        
        int favorite = cursor.getInt(cursor.getColumnIndex(WallpaperColumns.FAVORITE));
        wallpaper.setFavorite(favorite == DataConstant.WALLPAPER_FAVORITE);
        
        if (wallpaper.isFavorite()) {
            String favoriteLocalPath = cursor.getString(cursor.getColumnIndex(WallpaperColumns.FAVORITE_LOCAL_PATH));
            wallpaper.setFavoriteLocalPath(favoriteLocalPath);
        }
        wallpaper.setType(cursor.getInt(cursor.getColumnIndex(DataConstant.WallpaperColumns.SAVE_TYPE)));
        return wallpaper;
    }
    
    public synchronized  void deleteAllExcludeLock() {
        final SQLiteDatabase db = mWritableDatabase;
        String sql = "delete from wallpaper where " + DataConstant.WallpaperColumns.LOCK +
                " = " + DataConstant.WALLPAPER_NOT_LOCK + " or " + DataConstant.WallpaperColumns.SAVE_TYPE + 
                "=" + Wallpaper.WALLPAPER_FROM_FIXED_FOLDER;
        db.execSQL(sql);
    }
    
    public synchronized  void insertAfterDeleteAll(WallpaperList list){
        DebugLog.d(TAG,"insertAfterDeleteAll 1");         
        final SQLiteDatabase db = mWritableDatabase;
        db.beginTransaction();
        updateNotTodayImg();
        deleteAllExcludeLock();
        resetLockItemOrder();
        insertWallpapersNoTransaction(list);
        db.setTransactionSuccessful();  
        db.endTransaction(); 
        DebugLog.d(TAG,"insertAfterDeleteAll 2");         
    }
    
    public synchronized  void resetLockItemOrder(){
        final SQLiteDatabase db = mWritableDatabase;
        ContentValues values = new ContentValues();
        values.put(DataConstant.WallpaperColumns.REAL_ORDER,0);
        values.put(DataConstant.WallpaperColumns.SHOW_ORDER,0);
        db.update(TABLE_NAME, values, DataConstant.WallpaperColumns.TODAY_IMAGE + " = ? ", new String[] {
                String.valueOf(DataConstant.NOT_TODAY_IMAGE)
        });
    }
    
    public void updateDownLoadFinish(Wallpaper wallpaper){
        updateDownLoadState(wallpaper,DataConstant.DOWN_FINISH);
    }

    public void updateDownLoadNotFinish(Wallpaper wallpaper){
        updateDownLoadState(wallpaper,DataConstant.DOWN_NOT_FINISH);
    }

	private void updateDownLoadState(Wallpaper wallpaper,int state) {
		final SQLiteDatabase db = mWritableDatabase;
        ContentValues values = new ContentValues();
        values.put(IS_FINISH, state);
        db.update(TABLE_NAME, values, "img_id = ?", new String[] {
                String.valueOf(wallpaper.getImgId())
        });
	}
    
 
    public void updateNotTodayImg(){
        final SQLiteDatabase db = mWritableDatabase;
        ContentValues values = new ContentValues();
        values.put(DataConstant.WallpaperColumns.TODAY_IMAGE, DataConstant.NOT_TODAY_IMAGE);
        db.update(TABLE_NAME, values, null,null);
    }
    

    public int updateFavorite(Wallpaper wallpaper) {

        final SQLiteDatabase db = mWritableDatabase;
        ContentValues values = new ContentValues();
        values.put(WallpaperColumns.FAVORITE, wallpaper.isFavorite() ? 1 : 0);
        values.put(WallpaperColumns.FAVORITE_LOCAL_PATH, wallpaper.getFavoriteLocalPath());
        int id = db.update(TABLE_NAME, values, "img_id = ?", new String[] {
                String.valueOf(wallpaper.getImgId())
        });
        
        return id;
    }
    
    public boolean updateLocked(Wallpaper wallpaper,boolean isLocked) {
         return true;
    }
    
 
    public boolean updateLock(Wallpaper wallpaper){
        int count = 0;
        final SQLiteDatabase db = mWritableDatabase;
        ContentValues valueUpdate = new ContentValues();
        boolean lock = wallpaper.isLocked();
        int lockState = 0;
        if(lock){
            lockState = 1;
        }
        DebugLog.d(TAG,"updateLock lockState:" + lockState);
        DebugLog.d(TAG,"updateLock wallpaper.getImgId():" + wallpaper.getImgId());
        valueUpdate.put(DataConstant.WallpaperColumns.LOCK,lockState);
        count = db.update(TABLE_NAME, valueUpdate, DataConstant.WallpaperColumns.IMG_ID + " = ?", new String[] {
                String.valueOf(wallpaper.getImgId())
        });
        DebugLog.d(TAG,"updateLock count:" + count);
        if(count < 0){
            return false;
        }else{
            return true;
        }
    }
    
    public   boolean queryHasWallpaperNotTodayAndNotLock(){ 
        final SQLiteDatabase db = mReadableDatabase;
        Cursor cursor = db.rawQuery("select * from wallpaper where " +
        DataConstant.WallpaperColumns.LOCK + " = ? and " + 
        DataConstant.WallpaperColumns.TODAY_IMAGE + " = ?",new String[] {
                String.valueOf(DataConstant.WALLPAPER_NOT_LOCK),
                String.valueOf(DataConstant.NOT_TODAY_IMAGE)
            });
        Wallpaper wallpaper = null;
        int count = 0;
        if(cursor != null){
            count = cursor.getCount();
        }
        closeCursor(cursor);
        if(count > 0){
            return true;
        }else{
            return false;
        }
    }
    
    public  synchronized   void deleteWallpaperNotTodayAndNotLock(){
        final SQLiteDatabase db = mWritableDatabase;
        String sql = "delete from wallpaper where " + DataConstant.WallpaperColumns.LOCK +
                " = " + DataConstant.WALLPAPER_NOT_LOCK + " and " + DataConstant.WallpaperColumns.TODAY_IMAGE
                + " = " + DataConstant.NOT_TODAY_IMAGE;
        db.execSQL(sql);
    }
    
    public void updateShowOrder(Wallpaper wallpaper){
        final SQLiteDatabase db = mWritableDatabase;
        ContentValues values = new ContentValues();
        values.put(WallpaperColumns.SHOW_ORDER, wallpaper.getShowOrder());
        db.update(TABLE_NAME, values, DataConstant.WallpaperColumns.IMG_ID + 
                " = ?", new String[] {
                String.valueOf(wallpaper.getImgId())
        });
    }
    
    public  synchronized   void clearLock(){
        final SQLiteDatabase db = mWritableDatabase;
        ContentValues values = new ContentValues();
        values.put(DataConstant.WallpaperColumns.LOCK, DataConstant.WALLPAPER_NOT_LOCK);
        db.update(TABLE_NAME, values, DataConstant.WallpaperColumns.LOCK + " = ?", new String[] {
                String.valueOf(DataConstant.WALLPAPER_LOCK)
        });
    }
 
    public   boolean queryHasLockPaper(){
        Wallpaper wallpaper = queryPicturesDownLoadedLock();
        if(wallpaper != null){
            return true;
        }
        return false;
    }
    
    public   Wallpaper queryDynamicShowWallpaper(String timeNow){
        final SQLiteDatabase db = mReadableDatabase;
        String sqlCurrentTime = "select * from " + TABLE_NAME + " where " + 
                DataConstant.WallpaperColumns.SHOW_TIME_BEGIN + "<='" + timeNow + 
                "' and " + DataConstant.WallpaperColumns.SHOW_TIME_END + ">'" + timeNow + 
                "' order by " + DataConstant.WallpaperColumns.SHOW_TIME_BEGIN + " asc Limit 1 Offset 0";
        String sqlPreTime = "select * from " + TABLE_NAME + " where " + 
                DataConstant.WallpaperColumns.SHOW_TIME_END + "<='" + timeNow +     
                "' order by " + DataConstant.WallpaperColumns.SHOW_TIME_END + " desc Limit 1 Offset 0";
        String sqlNextTime = "select * from " + TABLE_NAME + " where " + 
                DataConstant.WallpaperColumns.SHOW_TIME_BEGIN + ">='" + timeNow +     
                "' order by " + DataConstant.WallpaperColumns.SHOW_TIME_BEGIN + " asc Limit 1 Offset 0";
        DebugLog.d(TAG,"queryDynamicShowWallpaper sqlCurrentTime:" + sqlCurrentTime);
        DebugLog.d(TAG,"queryDynamicShowWallpaper sqlPreTime:" + sqlPreTime);
        DebugLog.d(TAG,"queryDynamicShowWallpaper sqlNextTime:" + sqlNextTime);
        Cursor cursor = db.rawQuery(sqlCurrentTime,null);
        if(cursor != null){
            Wallpaper wallpaper = null;
            if(cursor.getCount() != 0){
                cursor.moveToFirst();
                wallpaper = queryWallpaper(cursor);
                closeCursor(cursor);
                return wallpaper;
            }
            closeCursor(cursor);
        }
        Cursor preTimeCursor = db.rawQuery(sqlPreTime,null);
        DebugLog.d(TAG,"queryDynamicShowWallpaper preTimeCursor:" + preTimeCursor);
        if(preTimeCursor != null){
            Wallpaper wallpaper = null;
            DebugLog.d(TAG,"queryDynamicShowWallpaper preTimeCursor.getCount():" + preTimeCursor.getCount());
            if(preTimeCursor.getCount() > 0){
                preTimeCursor.moveToFirst();
                wallpaper = queryWallpaper(preTimeCursor);
                closeCursor(preTimeCursor);
                return wallpaper;
            }
            closeCursor(preTimeCursor);
        }
        Cursor nextTimeCursor = db.rawQuery(sqlNextTime,null);
        if(nextTimeCursor != null){
            Wallpaper wallpaper = null;
            if(nextTimeCursor.getCount() != 0){
                nextTimeCursor.moveToFirst();
                wallpaper = queryWallpaper(nextTimeCursor);
                closeCursor(nextTimeCursor);
                return wallpaper;
            }
            closeCursor(nextTimeCursor);
        }
        return null;
    }
    
    public boolean queryHasDownLoadImage(){
        final SQLiteDatabase db = mReadableDatabase;
        Cursor cursor = db.rawQuery("select * from wallpaper where " +
        DataConstant.WallpaperColumns.DOWNLOAD_PICTURE + " = ?",new String[] {
                String.valueOf(DataConstant.DOWN_FINISH)
            });
        if(cursor != null){
        	if(cursor.getCount() > 0){
                closeCursor(cursor);
        		return true;
        	}
        }
        closeCursor(cursor);
        return false;
    }
    
    private void closeCursor(Cursor cursor){
        if(cursor != null){
            cursor.close();
        }
    }
    
    public synchronized  void deleteAll() {
        final SQLiteDatabase db = mWritableDatabase;
        String sql = "delete from wallpaper";
        db.execSQL(sql);
    }
    
    public boolean queryHasDownLoadImageNotLocalData(){
        final SQLiteDatabase db = mReadableDatabase;
        Cursor cursor = db.rawQuery("select * from wallpaper where " +
        DataConstant.WallpaperColumns.DOWNLOAD_PICTURE + " = ? and " + 
        DataConstant.WallpaperColumns.SAVE_TYPE + " = ?",new String[] {
                String.valueOf(DataConstant.DOWN_FINISH),
                String.valueOf(DataConstant.INTERNET)
            });
        if(cursor != null){
        	if(cursor.getCount() > 0){
                closeCursor(cursor);
        		return true;
        	}
        }
        closeCursor(cursor);
        return false;
    }
    
}