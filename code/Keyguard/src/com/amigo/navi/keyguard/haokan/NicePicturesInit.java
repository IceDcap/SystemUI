package com.amigo.navi.keyguard.haokan;

import java.util.ArrayList;
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
import com.amigo.navi.keyguard.network.connect.NetWorkUtils;
import com.amigo.navi.keyguard.network.local.ReadFileFromSD;
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
import com.amigo.navi.keyguard.network.theardpool.LoadDataPool;
import com.amigo.navi.keyguard.network.theardpool.LoadImagePool;
import com.amigo.navi.keyguard.network.theardpool.LoadImageThread;
import com.amigo.navi.keyguard.settings.KeyguardSettings;

public class NicePicturesInit {
    private static final String TAG = "NicePicturesInit";
    private static NicePicturesInit sInitInstance = null;
    private static Context mContext;
    private BroadcastReceiver mReveicer;
    private DownLoadThreadPool mThreadPool = null;
    private DataChangedInterface mDataChangedInterface = null;
    private static String mPath = null;
    private static  final String GET_DATA = "get_data";
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
    		boolean isUpdate = KeyguardSettings.getWallpaperUpadteState(mContext);
            DebugLog.d(TAG,"registerUserID isUpdate:" + isUpdate);
    		if(!isUpdate){
    			return;
    		}
    		boolean isUpdateOnWifi = KeyguardSettings.getOnlyWlanState(mContext);
            DebugLog.d(TAG,"registerUserID isUpdateOnWifi:" + isUpdateOnWifi);
    		if(isUpdateOnWifi){
        		boolean isWifi = NetWorkUtils.isWifi(mContext);
                DebugLog.d(TAG,"registerUserID isWifi:" + isWifi);
        		if(!isWifi){
        			return;
        		}
    		}
            String currentDate = Common.formatCurrentDate();
    		LoadDataPool.getInstance(mContext.getApplicationContext()).stopTaskDiffUrl(currentDate);
            DebugLog.d(TAG,"registerUserID");
            Job job = new Job() {
                private boolean isStop = false;
                @Override
                public void runTask() {
                  String userID = registerUserID();
                  DebugLog.d(TAG,"registerData userID:" + userID);
                  if(!TextUtils.isEmpty(userID)){
                      HKAgent.uploadAllLog();
                      requestPictureList(isStop);
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
            LoadImageThread runnable = new LoadImageThread(currentDate, job);
			LoadDataPool.getInstance(mContext.getApplicationContext()).loadImage(runnable, currentDate);

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
        DealWithFromLocalInterface dealWithBitmap = new ReadFileFromSD(mContext
                ,DiskUtils.CATEGORY_BITMAP_FOLDER,mPath,localFileOperation);
        if(updateDate != date){
            downloadCategoryPicturesFromNet(date,dealWithBitmap,isStop);
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
    private void downloadCategoryPicturesFromNet(int date,DealWithFromLocalInterface dealWithBitmap
            ,boolean isStop) {
        DebugLog.d(TAG,"downloadCategoryPicturesFromNet");
        String result = requestPictureCategoryFromNet();
        DebugLog.d(TAG,"downloadCategoryPicturesFromNet result:" + result);
        if(DownLoadJsonManager.ERROR.equals(result)){
        	return;
        }
        if(TextUtils.isEmpty(result)){
            Common.setUpdateCategoryDate(mContext,date);
            return;
        }
        List<Category> categoryList = JsonUtil.parseJsonToCategory(result);
        DebugLog.d(TAG,"downloadCategoryPicturesFromNet categoryList:" + categoryList.size());
        boolean isFirstBitmapOfCurrentDay = true;
        CategoryDB categoryDB = CategoryDB.getInstance(mContext);
        boolean savedSuccess = false;
    	List<Category> delList = new ArrayList<Category>();
        for(int index = 0;index < categoryList.size();index++){
            DebugLog.d(TAG,"downloadCategoryPicturesFromNet isStop:" + isStop);
            if(isStop){
                break;
            }
            Category category = categoryList.get(index);
            String picUrl = category.getTypeIconUrl();
            if(!TextUtils.isEmpty(picUrl)){
                Bitmap bitmap = DownLoadBitmapManager.getInstance().downLoadBitmap(mContext, picUrl);
                if(bitmap != null ){
                    DebugLog.d(TAG,"downloadCategoryPicturesFromNet2");
                    String key = DiskUtils.constructFileNameByUrl(picUrl);
                    savedSuccess = dealWithBitmap.writeToLocal(key,bitmap);
                    if(!bitmap.isRecycled()){
                    	bitmap.recycle();
                    	System.gc();
                    }                    
                    if(savedSuccess){
                        if(isFirstBitmapOfCurrentDay){
                            categoryDB.insertAfterDeleteAll(categoryList);
                            delList = categoryDB.queryHasCategoryNotToday();
                            Common.setUpdateCategoryDate(mContext,date);
                            isFirstBitmapOfCurrentDay = false;
                        }
                        categoryDB.updateDownLoadFinish(category);

                    }
                }
            }
        }
        delOldCategory(dealWithBitmap, categoryDB, savedSuccess, delList);
    }

	private void delOldCategory(DealWithFromLocalInterface dealWithBitmap,
			CategoryDB categoryDB, boolean savedSuccess, List<Category> delList) {
		if(savedSuccess){
            for(int delIndex = 0;delIndex < delList.size();delIndex++){
            	String picUrl = delList.get(delIndex).getTypeIconUrl();
                String key = DiskUtils.constructFileNameByUrl(picUrl);
                key = DiskUtils.constructFileNameByUrl(delList.get(delIndex).getTypeIconUrl());
                dealWithBitmap.deleteFile(key);
            }
            categoryDB.deleteNotToday();
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
                    if(!bitmap.isRecycled()){
                    	bitmap.recycle();
                    	System.gc();
                    }                  
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
    
    private void requestPictureList(boolean isStop){
                String currentDate = Common.formatCurrentDate();
                int date = Integer.valueOf(currentDate);
                int updateDate = Common.getUpdateWallpaperDate(mContext);
                DebugLog.d(TAG,"requestPictureList updateDate:" + updateDate);
                DebugLog.d(TAG,"requestPictureList date:" + date);       
                LocalFileOperationInterface localFileOperation = new LocalBitmapOperation();
                DealWithFromLocalInterface dealWithBitmap = new ReadFileFromSD(mContext
                        ,DiskUtils.WALLPAPER_BITMAP_FOLDER,mPath,localFileOperation);
                DebugLog.d(TAG,"requestPictureList date == updateDate:" + (date == updateDate));         
                if(updateDate != date){
                    downloadWallpaperPicturesFromNet(date,dealWithBitmap,isStop);
                }else{
                    downloadWallpaperPicturesFromDB(date,dealWithBitmap,isStop);
                }

    }
    

    /**
     * @param date
     */
    private void downloadWallpaperPicturesFromDB(int date,
            DealWithFromLocalInterface dealWithBitmap,boolean isStop) {
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
                    if(!bitmap.isRecycled()){
                    	bitmap.recycle();
                    	System.gc();
                    }                    
                    WallpaperDB wallpaperDB = WallpaperDB.getInstance(mContext);
                    DebugLog.d(TAG,"downloadWallpaperPicturesFromDB savedSuccess:" + savedSuccess);
                    if(savedSuccess){
                        wallpaperDB.updateDownLoadFinish(wallpaper);
//                        notifyDataChanged(picUrl,bitmap);
                    }
                }
            }
        }
    }
    
    private void downloadWallpaperPicturesFromNet(int date, DealWithFromLocalInterface dealWithBitmap
    		,boolean isStop) {
        List<Integer> categoryList = CategoryDB.getInstance(mContext).queryCategoryIDByFavorite();
        if(categoryList.size() == 0){
        	return;
        }
        String result = requestPictureJsonFromNet(categoryList);
        DebugLog.d(TAG,"downloadWallpaperPicturesFromNet result:" + result);
        if(DownLoadJsonManager.ERROR.equals(result) ){
        	return;
        }
        if(TextUtils.isEmpty(result)){
            Common.setUpdateWallpaperDate(mContext,date);
            return;
        }
        DebugLog.d(TAG,"downloadWallpaperPicturesFromNet 1");
        WallpaperList wallpaperList = JsonUtil.parseJsonToWallpaperList(result);
        DebugLog.d(TAG,"downloadWallpaperPicturesFromNet 2");
        boolean isFirstBitmapOfCurrentDay = true;
        boolean savedSuccess = false;
        WallpaperList delList = new WallpaperList();
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
                    String key = DiskUtils.constructFileNameByUrl(picUrl);
                    savedSuccess = dealWithBitmap.writeToLocal(key,bitmap);
                    if(!bitmap.isRecycled()){
                    	bitmap.recycle();
                    	System.gc();
                    }
                    DebugLog.d(TAG,"downloadWallpaperPicturesFromNet savedSuccess:" + savedSuccess);
                    if(savedSuccess){
                        DebugLog.d(TAG,"downloadWallpaperPicturesFromNet isFirstBitmapOfCurrentDay:" + isFirstBitmapOfCurrentDay);
                        if(isFirstBitmapOfCurrentDay){
                            DebugLog.d(TAG,"downloadWallpaperPicturesFromNet 1");
                            delList = wallpaperDB.queryExcludeFixedWallpaper();
                            wallpaperDB.insertAfterDeleteAll(wallpaperList);
                            Common.setUpdateWallpaperDate(mContext,date);
                            isFirstBitmapOfCurrentDay = false;
                        }
                        notifyDataChanged(picUrl,bitmap);
                        wallpaperDB.updateDownLoadFinish(wallpaper);
                        DebugLog.d(TAG,"downloadWallpaperPicturesFromNet 2 wallPaperList:" + delList.size());
                    }
                }
            }
        }
        delOldWallpaper(dealWithBitmap, savedSuccess, delList);
    }

	private void delOldWallpaper(
			DealWithFromLocalInterface dealWithBitmap,
			boolean savedSuccess, WallpaperList delList) {
		if(savedSuccess){
            for(int delIndex = 0;delIndex < delList.size();delIndex++){
                String key = DiskUtils.constructFileNameByUrl(delList.get(delIndex).getImgUrl());
                dealWithBitmap.deleteFile(key);
            }
        }
	}
    
    private String requestPictureJsonFromNet(List<Integer> categoryList){
        String result = DownLoadJsonManager.getInstance().requestPicturesOfCurrentDay(mContext,categoryList);
        return result;
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
