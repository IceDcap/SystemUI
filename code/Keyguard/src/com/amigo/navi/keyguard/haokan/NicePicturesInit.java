package com.amigo.navi.keyguard.haokan;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.haokan.analysis.HKAgent;
import com.amigo.navi.keyguard.haokan.db.CategoryDB;
import com.amigo.navi.keyguard.haokan.db.WallpaperDB;
import com.amigo.navi.keyguard.haokan.entity.Category;
import com.amigo.navi.keyguard.haokan.entity.Wallpaper;
import com.amigo.navi.keyguard.haokan.entity.WallpaperList;
import com.amigo.navi.keyguard.network.local.DealWithFileFromLocal;
import com.amigo.navi.keyguard.network.local.DealWithFromLocalInterface;
import com.amigo.navi.keyguard.network.local.LocalBitmapOperation;
import com.amigo.navi.keyguard.network.local.LocalFileOperationInterface;
import com.amigo.navi.keyguard.network.local.utils.DiskUtils;
import com.amigo.navi.keyguard.network.manager.DownLoadBitmapManager;
import com.amigo.navi.keyguard.network.manager.DownLoadJsonManager;
import com.amigo.navi.keyguard.network.theardpool.DownLoadJsonThreadPool;
import com.amigo.navi.keyguard.network.theardpool.DownLoadThreadPool;
import com.amigo.navi.keyguard.network.theardpool.DownLoadWorker;
import com.amigo.navi.keyguard.network.theardpool.Job;

public class NicePicturesInit {
    private static final String TAG = "NicePicturesInit";
    private static NicePicturesInit sInitInstance = null;
    private static Context mContext;
    private BroadcastReceiver mReveicer;
    private DownLoadThreadPool mThreadPool = null;
    private DataChangedInterface mDataChangedInterface = null;
    private static String mPath = null;
    
    public synchronized static NicePicturesInit getInstance(Context context) {

        if (sInitInstance == null) {
            sInitInstance = new NicePicturesInit();
            mContext = context.getApplicationContext();
            mPath = DiskUtils.getCachePath(mContext.getApplicationContext());
        }
        return sInitInstance;
    }
    
    private NicePicturesInit(){
    }
    
    public void init(){
        initBroadcastReceiver();
        TimeControlManager.getInstance().init(mContext);
        TimeControlManager.getInstance().startUpdateAlarm();
    }
    
    private void initBroadcastReceiver() {
        if (null != mReveicer) {
            mContext.unregisterReceiver(mReveicer);
        }
        mReveicer = new NicePictureReveicer();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.WALLPAPER_TIMING_UPDATE);
        mContext.registerReceiver(mReveicer, filter);
    }
    
    public void registerData(){
            DebugLog.d(TAG,"registerUserID");
            Job job = new Job() {
                private boolean isStop = false;
                @Override
                public void runTask() {
                  String userID = registerUserID();
                  DebugLog.d(TAG,"registerData userID:" + userID);
                  if(!TextUtils.isEmpty(userID)){
                      HKAgent.uploadAllLog();
                      requestPictureList();
                      requestPictureCategory(isStop);
                  }
                }
                
                @Override
                public int getProgress() {
                    return 0;
                }
                
                @Override
                public void cancelTask() {
                    isStop = true;
                }
            };
            DownLoadWorker worker = new DownLoadWorker(job);
            mThreadPool = DownLoadJsonThreadPool.getInstance();
            mThreadPool.submit(worker);
            
            
    }
    
    private String registerUserID() {
        String userID = Common.getUserId(mContext);
          DebugLog.d(TAG,"userID  = " + userID);
          if(TextUtils.isEmpty(userID)){
            String result = DownLoadJsonManager.getInstance().registerUserID(mContext);
            DebugLog.d(TAG,"userID result = " + result);
            userID = JsonUtil.parseJsonToUserId(result);
            DebugLog.d(TAG,"userID result userID:" + userID);
            Common.setSharedConfigUserId(mContext, userID);
          }
        return userID;
    }
    
    private void requestPictureCategory(boolean isStop){
        DebugLog.d(TAG,"requestPictureCategory");
        String currentDate = Common.formatCurrentDate();
        int date = Integer.valueOf(currentDate);
        int updateDate = Common.getUpdateCategoryDate(mContext);
        DebugLog.d(TAG,"requestPictureCategory updateDate:" + updateDate);
        DebugLog.d(TAG,"requestPictureCategory updateDate:" + date);
        LocalFileOperationInterface localFileOperation = new LocalBitmapOperation();
        DealWithFromLocalInterface dealWithBitmap = new DealWithFileFromLocal(mContext
                ,DiskUtils.CATEGORY_BITMAP_FOLDER,mPath,localFileOperation);
        if(updateDate != date){
            String result = requestPictureCategoryFromNet();
            DebugLog.d(TAG,"requestPictureCategory result:" + result);
            downloadCategoryPicturesFromNet(date,result,dealWithBitmap,isStop);
        }else{
            downloadCategoryPicturesFromDB(date,dealWithBitmap,isStop);
        }
    }

    /**
     * @param date
     */
    private String requestPictureCategoryFromNet() {
        String result = DownLoadJsonManager.getInstance().requestPictureCategory(mContext);
        return result;
    }

    /**
     * @param date
     * @param dealWithBitmap
     * @param result
     */
    private void downloadCategoryPicturesFromNet(int date, String result,DealWithFromLocalInterface dealWithBitmap
            ,boolean isStop) {
        DebugLog.d(TAG,"downloadCategoryPictures");
        List<Category> categoryList = JsonUtil.parseJsonToCategory(result);
        DebugLog.d(TAG,"downloadCategoryPictures categoryList:" + categoryList.size());
        boolean isFirstBitmapOfCurrentDay = true;
        CategoryDB categoryDB = null;
        for(int index = 0;index < categoryList.size();index++){
            DebugLog.d(TAG,"downloadCategoryPicturesFromNet isStop:" + isStop);
            if(isStop){
                break;
            }
            Category category = categoryList.get(index);
            String picUrl = category.getTypeIconUrl();
            if(!TextUtils.isEmpty(picUrl)){
                categoryDB = CategoryDB.getInstance(mContext);
                Bitmap bitmap = DownLoadBitmapManager.getInstance().downLoadBitmap(mContext, picUrl);
                if(bitmap != null ){
                    DebugLog.d(TAG,"downloadCategoryPictures2");
                    if(isFirstBitmapOfCurrentDay){
                        dealWithBitmap.deleteAllFile();
                    }
                    String key = DiskUtils.constructFileNameByUrl(picUrl);
                    boolean savedSuccess = dealWithBitmap.writeToLocal(key,bitmap);
                    bitmap.recycle();
                    if(savedSuccess){
                        if(isFirstBitmapOfCurrentDay){
                            categoryDB.insertAfterDeleteAll(categoryList);
                            Common.setUpdateCategoryDate(mContext,date);
                            isFirstBitmapOfCurrentDay = false;
                        }
                        categoryDB.updateDownLoadFinish(category);
                    }
                }
            }
        }
    }
    
    private void downloadCategoryPicturesFromDB(int date,DealWithFromLocalInterface dealWithBitmap,
            boolean isStop) {
        DebugLog.d(TAG,"downloadCategoryPictures");
        CategoryDB categoryDB = CategoryDB.getInstance(mContext);
        List<Category> categoryList = categoryDB.queryPicturesNoDownLoad();
        for(int index = 0;index < categoryList.size();index++){
            DebugLog.d(TAG,"downloadCategoryPicturesFromDB isStop:" + isStop);
            if(isStop){
                break;
            }
            Category category = categoryList.get(index);
            String picUrl = category.getTypeIconUrl();
            if(!TextUtils.isEmpty(picUrl)){
                Bitmap bitmap = DownLoadBitmapManager.getInstance().downLoadBitmap(mContext, picUrl);
                if(bitmap != null){
                    String key = DiskUtils.constructFileNameByUrl(picUrl);
                    boolean savedSuccess = dealWithBitmap.writeToLocal(key,bitmap);
                    bitmap.recycle();
                    if(savedSuccess){
                        categoryDB.updateDownLoadFinish(category);
                    }
                }
            }
        }
    }

    /**
     * 
     */
    private void notifyDataChanged(String url,Bitmap bitmap) {
        if(mDataChangedInterface != null){
            mDataChangedInterface.onDataChanged(url,bitmap);
        }
    }
    
    private void requestPictureList(){
        final List<Integer> categoryList = CategoryDB.getInstance(mContext).queryCategoryIDByFavorite();
        DebugLog.d(TAG,"requestPictureList");
        Job job = new Job() {
            private boolean isStop = false;
            @Override
            public void runTask() {
                String currentDate = Common.formatCurrentDate();
                int date = Integer.valueOf(currentDate);
                int updateDate = Common.getUpdateWallpaperDate(mContext);
                DebugLog.d(TAG,"requestPictureList updateDate:" + updateDate);
                DebugLog.d(TAG,"requestPictureList date:" + date);       
                LocalFileOperationInterface localFileOperation = new LocalBitmapOperation();
                DealWithFromLocalInterface dealWithBitmap = new DealWithFileFromLocal(mContext
                        ,DiskUtils.WALLPAPER_BITMAP_FOLDER,mPath,localFileOperation);
                DebugLog.d(TAG,"requestPictureList date == updateDate:" + (date == updateDate));         
                if(updateDate != date){
                    String result = requestPictureJsonFromNet(categoryList);
                    DebugLog.d(TAG,"requestPictureList result:" + result);
                    downloadWallpaperPicturesFromNet(date,result,dealWithBitmap);
                }else{
                    downloadWallpaperPicturesFromDB(date,dealWithBitmap);
                }
            }

            /**
             * @param date
             */
            private void downloadWallpaperPicturesFromDB(int date,
                    DealWithFromLocalInterface dealWithBitmap) {
                WallpaperList wallpaperList = WallpaperDB.getInstance(mContext).queryPicturesNoDownLoad();
                DebugLog.d(TAG,"downloadWallpaperPicturesFromDB wallpaperList size:" + wallpaperList.size());
                for(int index = 0;index < wallpaperList.size();index++){
                    DebugLog.d(TAG,"downloadWallpaperPicturesFromDB isStop:" + isStop);
                    if(isStop){
                        break;
                    }
                    Wallpaper wallpaper = wallpaperList.get(index);
                    String picUrl = wallpaper.getImgUrl();
                    DebugLog.d(TAG,"downloadWallpaperPicturesFromDB picUrl:" + picUrl);
                    if(!TextUtils.isEmpty(picUrl)){
                        Bitmap bitmap = DownLoadBitmapManager.getInstance().downLoadBitmap(mContext, picUrl);
                        DebugLog.d(TAG,"downloadWallpaperPicturesFromDB bitmap:" + bitmap);
                        if(bitmap != null){
                            String key = DiskUtils.constructFileNameByUrl(picUrl);
                            boolean savedSuccess = dealWithBitmap.writeToLocal(key,bitmap);
                            WallpaperDB wallpaperDB = WallpaperDB.getInstance(mContext);
                            DebugLog.d(TAG,"downloadWallpaperPicturesFromDB savedSuccess:" + savedSuccess);
                            if(savedSuccess){
                                wallpaperDB.updateDownLoadFinish(wallpaper);
                                notifyDataChanged(picUrl,bitmap);
                            }
                        }
                    }
                }
            }
            
            private void downloadWallpaperPicturesFromNet(int date, String result,DealWithFromLocalInterface dealWithBitmap) {
                DebugLog.d(TAG,"downloadWallpaperPicturesFromNet result:" + result);
                WallpaperList wallpaperList = JsonUtil.parseJsonToWallpaperList(result);
                boolean isFirstBitmapOfCurrentDay = true;
                for(int index = 0;index < wallpaperList.size();index++){
                    DebugLog.d(TAG,"downloadWallpaperPicturesFromNet isStop:" + isStop);
                    if(isStop){
                        break;
                    }
                    Wallpaper wallpaper = wallpaperList.get(index);
                    String picUrl = wallpaper.getImgUrl();
                    DebugLog.d(TAG,"downloadWallpaperPicturesFromNet picUrl:" + picUrl);
                    if(!TextUtils.isEmpty(picUrl)){
                        Bitmap bitmap = DownLoadBitmapManager.getInstance().downLoadBitmap(mContext, picUrl);
                        DebugLog.d(TAG,"downloadWallpaperPicturesFromNet bitmap:" + bitmap);
                        if(bitmap != null){
                            WallpaperDB wallpaperDB = WallpaperDB.getInstance(mContext);
                            if(isFirstBitmapOfCurrentDay){
                                DebugLog.d(TAG,"downloadWallpaperPicturesFromNet 1");
                                WallpaperList wallPaperList = wallpaperDB.queryExcludeFixedWallpaper();
                                DebugLog.d(TAG,"downloadWallpaperPicturesFromNet 2 wallPaperList:" + wallPaperList.size());
                                for(int delIndex = 0;delIndex < wallPaperList.size();delIndex++){
                                    String key = DiskUtils.constructFileNameByUrl(wallPaperList.get(delIndex).getImgUrl());
                                    dealWithBitmap.deleteFile(key);
                                }
                            }
                            String key = DiskUtils.constructFileNameByUrl(picUrl);
                            boolean savedSuccess = dealWithBitmap.writeToLocal(key,bitmap);
                            DebugLog.d(TAG,"downloadWallpaperPicturesFromNet savedSuccess:" + savedSuccess);
                            if(savedSuccess){
                                DebugLog.d(TAG,"downloadWallpaperPicturesFromNet isFirstBitmapOfCurrentDay:" + isFirstBitmapOfCurrentDay);
                                if(isFirstBitmapOfCurrentDay){
                                    wallpaperDB.insertAfterDeleteAll(wallpaperList);
                                    FileUtil.deleteMusic();
                                    Common.setUpdateWallpaperDate(mContext,date);
                                    isFirstBitmapOfCurrentDay = false;
                                }
                                notifyDataChanged(picUrl,bitmap);
                                wallpaperDB.updateDownLoadFinish(wallpaper);
                            }
                        }
                    }
                }
            }
            
            private String requestPictureJsonFromNet(List<Integer> categoryList){
                String result = DownLoadJsonManager.getInstance().requestPicturesOfCurrentDay(mContext,categoryList);
                return result;
            }
            
            @Override
            public int getProgress() {
                return 0;
            }
            
            @Override
            public void cancelTask() {
                isStop = true;
            }
        };
        DownLoadWorker worker = new DownLoadWorker(job);
        DebugLog.d(TAG,"downloadWallpaperPictures submit");
        mThreadPool.submit(worker);
    }
    

    

    
    private class NicePictureReveicer extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            DebugLog.d(TAG,"onReceive wallpaper alarm");
            String action = intent.getAction();
            if (action.equals(Constant.WALLPAPER_TIMING_UPDATE)) {
                shutDownWorkPool();
                registerData();
                TimeControlManager.getInstance().cancelUpdateAlarm();
                TimeControlManager.getInstance().startUpdateAlarm();
            } 
        }
    }
    
    public void release(){
        if(mReveicer != null){
            mContext.unregisterReceiver(mReveicer);
        }
        TimeControlManager.getInstance().release();
        mContext = null;
        sInitInstance = null;
    }
    
    public void shutDownWorkPool(){
        if(mThreadPool != null){
            mThreadPool.shutdownPool();
        }
    }
    
    public interface DataChangedInterface{
        public void onDataChanged(String url,Bitmap bitmap);
    }
    
    public void setDataChangedListener(DataChangedInterface dataChangedInterface){
        mDataChangedInterface = dataChangedInterface;
    }
    
}
