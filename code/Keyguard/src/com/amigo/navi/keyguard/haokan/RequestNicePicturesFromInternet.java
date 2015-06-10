package com.amigo.navi.keyguard.haokan;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.KWDataCache;
import com.amigo.navi.keyguard.haokan.analysis.HKAgent;
import com.amigo.navi.keyguard.haokan.db.CategoryDB;
import com.amigo.navi.keyguard.haokan.db.WallpaperDB;
import com.amigo.navi.keyguard.haokan.entity.Category;
import com.amigo.navi.keyguard.haokan.entity.Wallpaper;
import com.amigo.navi.keyguard.haokan.entity.WallpaperList;
import com.amigo.navi.keyguard.network.connect.NetWorkUtils;
import com.amigo.navi.keyguard.network.local.DealWithByteFile;
import com.amigo.navi.keyguard.network.local.ReadAndWriteFileFromSD;
import com.amigo.navi.keyguard.network.local.LocalBitmapOperation;
import com.amigo.navi.keyguard.network.local.LocalFileOperationInterface;
import com.amigo.navi.keyguard.network.local.utils.DiskUtils;
import com.amigo.navi.keyguard.network.manager.DownLoadBitmapManager;
import com.amigo.navi.keyguard.network.manager.DownLoadJsonManager;
import com.amigo.navi.keyguard.network.theardpool.Job;
import com.amigo.navi.keyguard.network.theardpool.LoadDataPool;
import com.amigo.navi.keyguard.network.theardpool.LoadImageThread;
import com.amigo.navi.keyguard.settings.KeyguardSettings;

public class RequestNicePicturesFromInternet {
    private static final String TAG = "NicePicturesInit";
    private static RequestNicePicturesFromInternet sInitInstance = null;
    private static Context mContext;
    private static String mPath = null;
    private DealWithByteFile  mDealWithCategory;
    private ReadAndWriteFileFromSD  mDealWithWallpaper;
    public synchronized static RequestNicePicturesFromInternet getInstance(Context context) {

        if (sInitInstance == null) {
            sInitInstance = new RequestNicePicturesFromInternet();
            mContext = context.getApplicationContext();
            mPath = DiskUtils.getCachePath(mContext.getApplicationContext());
        }
        return sInitInstance;
    }
    
    private RequestNicePicturesFromInternet(){

    }
    
    private int mScreenWid = 0;
    private int mScreenHei = 0;
    public void init(){
    	mScreenWid = KWDataCache.getScreenWidth(mContext.getResources());
    	mScreenHei = KWDataCache.getAllScreenHeigt(mContext);
        mDealWithCategory = new DealWithByteFile(mContext
                ,DiskUtils.CATEGORY_BITMAP_FOLDER,mPath);
        LocalFileOperationInterface localFileOperationInterface = new LocalBitmapOperation(mContext);
        mDealWithWallpaper = new ReadAndWriteFileFromSD(mContext
                ,DiskUtils.WALLPAPER_BITMAP_FOLDER,mPath,localFileOperationInterface);
//        registerData();
    }
    
    public void registerData(boolean isCheckFromOnToOff){
    		boolean isUpdate = KeyguardSettings.getWallpaperUpadteState(mContext);
            DebugLog.d(TAG,"registerUserID isUpdate:" + isUpdate);
    		if(!isUpdate){
    			if(isCheckFromOnToOff){
    				HKAgent.uploadAllLog();
    			}
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
                      requestPictureCategory(isStop);
                      requestPictureList(isStop);
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
        	Vector<LoadImageThread> threadList = null;
            threadList = LoadDataPool.getInstance(mContext.getApplicationContext())
            		.getDownLoadThreadList();
            LoadImageThread runnable = new LoadImageThread(currentDate, job,threadList);
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

        if(updateDate != date){
            downloadCategoryPicturesFromNet(date,mDealWithCategory,isStop);
        }else{
            downloadCategoryPicturesFromDB(date,mDealWithCategory,isStop);
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
    private void downloadCategoryPicturesFromNet(int date,DealWithByteFile dealWithBitmap
            ,boolean isStop) {
        DebugLog.d(TAG,"downloadCategoryPicturesFromNet");
        String result = requestPictureCategoryFromNet();
        DebugLog.d(TAG,"downloadCategoryPicturesFromNet result:" + result);
        if(DownLoadJsonManager.ERROR.equals(result)){
        	return;
        }
//        if(TextUtils.isEmpty(result)){
//            Common.setUpdateCategoryDate(mContext,date);
//            return;
//        }
        List<Category> categoryList = JsonUtil.parseJsonToCategory(result);
        DebugLog.d(TAG,"downloadCategoryPicturesFromNet categoryList:" + categoryList.size());
        boolean isFirstBitmapOfCurrentDay = true;
        downloadCategoryImage(date, dealWithBitmap, isStop, categoryList,
				isFirstBitmapOfCurrentDay,mFirstSaveImageForCategory,result);
    }

	private void downloadCategoryImage(int date,
			DealWithByteFile dealWithBitmap, boolean isStop,
			List<Category> categoryList, boolean isFirstBitmapOfCurrentDay
			,FirstSaveCategoryImage firstSaveCategoryImage,String result) {
		ArrayList<Category> delList = new ArrayList<Category>();
		for(int index = 0;index < categoryList.size();index++){
            DebugLog.d(TAG,"downloadCategoryPicturesFromNet isStop:" + isStop);
            if(isStop){
                break;
            }
            Category category = categoryList.get(index);
            String picUrl = category.getTypeIconUrl();
            if(!TextUtils.isEmpty(picUrl)){
                byte[] bitmapByte = DownLoadBitmapManager.getInstance().downLoadBitmapByByte(mContext, picUrl);
                DebugLog.d(TAG,"downloadCategoryPicturesFromNet bitmapByte:" + bitmapByte);
                DebugLog.d(TAG,"downloadCategoryPicturesFromNet2");
                    String key = DiskUtils.constructFileNameByUrl(picUrl);
                    boolean savedSuccess = dealWithBitmap.writeToLocal(key,bitmapByte);                  
                    if(savedSuccess){
                       if(firstSaveCategoryImage != null){
                    	   isFirstBitmapOfCurrentDay = firstSaveCategoryImage.operationAfterSaveImage(categoryList, isFirstBitmapOfCurrentDay, date, delList,
                    			   result);
                       }
                	   CategoryDB.getInstance(mContext).updateDownLoadFinish(category);
                    }
            }
        }
        if(firstSaveCategoryImage != null){
        	firstSaveCategoryImage.delOldImage(dealWithBitmap, !isFirstBitmapOfCurrentDay, delList);
        }
      }

	private void delOldCategory(DealWithByteFile dealWithBitmap, boolean savedSuccess, List<Category> delList) {
		if(savedSuccess){
            for(int delIndex = 0;delIndex < delList.size();delIndex++){
            	String picUrl = delList.get(delIndex).getTypeIconUrl();
                String key = DiskUtils.constructFileNameByUrl(picUrl);
                key = DiskUtils.constructFileNameByUrl(delList.get(delIndex).getTypeIconUrl());
                dealWithBitmap.deleteFile(key);
            }
        }
	}
    
    private void downloadCategoryPicturesFromDB(int date,DealWithByteFile dealWithBitmap,
            boolean isStop) {
        DebugLog.d(TAG,"downloadCategoryPictures");
        CategoryDB categoryDB = CategoryDB.getInstance(mContext);
        List<Category> categoryList = categoryDB.queryPicturesNoDownLoad();
        downloadCategoryImage(date, dealWithBitmap, isStop, categoryList, false, mFirstSaveImageForCategory,null);
    }

 
    
    private void requestPictureList(boolean isStop){
                String currentDate = Common.formatCurrentDate();
                int date = Integer.valueOf(currentDate);
                int updateDate = Common.getUpdateWallpaperDate(mContext);
                DebugLog.d(TAG,"requestPictureList updateDate:" + updateDate);
                DebugLog.d(TAG,"requestPictureList date:" + date);       

                DebugLog.d(TAG,"requestPictureList date == updateDate:" + (date == updateDate));         
                if(updateDate != date){
                    downloadWallpaperPicturesFromNet(mDealWithWallpaper,isStop);
                }else{
                    downloadWallpaperPicturesFromDB(mDealWithWallpaper,isStop);
                }

    }
    

    /**
     * @param date
     */
    private void downloadWallpaperPicturesFromDB(ReadAndWriteFileFromSD dealWithBitmap,boolean isStop) {
        WallpaperList wallpaperList = WallpaperDB.getInstance(mContext).queryPicturesNoDownLoad();
        DebugLog.d(TAG,"downloadWallpaperPicturesFromDB wallpaperList size:" + wallpaperList.size());
        downloadWallpaperImage(0,dealWithBitmap, isStop, wallpaperList,false);
    }
    
    private void downloadWallpaperPicturesFromNet( ReadAndWriteFileFromSD dealWithBitmap
    		,boolean isStop) {
        List<Integer> categoryList = CategoryDB.getInstance(mContext).queryCategoryIDByFavorite();
        if(categoryList.size() == 0){
        	return;
        }
        String result = requestPictureJsonFromNet(categoryList);
        DebugLog.d(TAG,"downloadWallpaperPicturesFromNet result:" + result);
        if(DownLoadJsonManager.ERROR.equals(result)){
        	return;
        }
 
        DebugLog.d(TAG,"downloadWallpaperPicturesFromNet 1");
        WallpaperList wallpaperList = JsonUtil.parseJsonToWallpaperList(result);
        wallpaperList.quickSort();
        DebugLog.d(TAG,"downloadWallpaperPicturesFromNet 2");
        String currentDate = Common.formatCurrentDate();
        int date = Integer.valueOf(currentDate);
        downloadWallpaperImage(date,dealWithBitmap, isStop, wallpaperList, true);
    }
    
	private void downloadWallpaperImage(int date,
			ReadAndWriteFileFromSD dealWithBitmap, boolean isStop,
			WallpaperList wallpaperList, boolean isFirstBitmapOfCurrentDay) {
	    
	    boolean isFirst = isFirstBitmapOfCurrentDay;
	    final WallpaperDB wallpaperDB = WallpaperDB.getInstance(mContext);
		WallpaperList deleteList = null;
		
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
//                    byte[] bitmapByte = DownLoadBitmapManager.getInstance().downLoadBitmapByByte(mContext, picUrl);
                    DebugLog.d(TAG,"downloadWallpaperPicturesFromNet bitmap:" + bitmap);
                    
                    String key = DiskUtils.constructFileNameByUrl(picUrl);
                    boolean savedSuccess = false;
                    if(bitmap != null){
                    	Bitmap cutBitmap = BitmapUtil.getResizedBitmapForSingleScreen(bitmap, mScreenHei, mScreenWid);
                        DebugLog.d(TAG,"downloadWallpaperPicturesFromNet cutBitmap:" + cutBitmap);
                    	savedSuccess = dealWithBitmap.writeToLocal(key,cutBitmap);
                    	BitmapUtil.recycleBitmap(bitmap);
                    	BitmapUtil.recycleBitmap(cutBitmap);
                    	System.gc();
                    }
                    DebugLog.d(TAG,"downloadWallpaperPicturesFromNet savedSuccess:" + savedSuccess);
                    if(savedSuccess){
                        DebugLog.d(TAG,"downloadWallpaperPicturesFromNet isFirstBitmapOfCurrentDay:" + isFirstBitmapOfCurrentDay);
                        
                        if (isFirst) {
                            DebugLog.d(TAG,"downloadWallpaperPicturesFromNet 1");
                            deleteList = wallpaperDB.queryExcludeFixedWallpaper();
                            wallpaperDB.insertAfterDeleteAll(wallpaperList);
                            Common.setUpdateWallpaperDate(mContext,date);
                            UIController.getInstance().setNewWallpaperToDisplay(true);
                            isFirst = false;
                            FileUtil.deleteMusic();
                        }
                        
                        wallpaperDB.updateDownLoadFinish(wallpaper);
                        
                    }
            }
        }
        
        delOldWallpaper(dealWithBitmap, !isFirst, deleteList);
	}

	private void delOldWallpaper(ReadAndWriteFileFromSD dealWithBitmap, boolean savedSuccess, WallpaperList delList) {
		
	    if(savedSuccess && delList != null){
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
    
    public void release(){
        mContext = null;
        sInitInstance = null;
    }
    
    public void shutDownWorkPool(){
//        if(mThreadPool != null){
//            mThreadPool.shutdownPool();
//        }
    	Vector<LoadImageThread> threadList = null;
        threadList = LoadDataPool.getInstance(mContext.getApplicationContext())
        		.getDownLoadThreadList();
        for(int index = 0;index < threadList.size();index++){
        	threadList.get(index).stop();
        }
    }
    
    public interface DataChangedInterface{
        public void onDataChanged(String url,Bitmap bitmap);
    }
    

    private interface FirstSaveCategoryImage{
    	public boolean operationAfterSaveImage(List<Category> list,boolean isFirst,int date,List<Category> delList,String result);
    	public void delOldImage(DealWithByteFile dealWithBitmap,boolean savedSuccess, List<Category> delList);
    }
    
     
	FirstSaveCategoryImage mFirstSaveImageForCategory = new FirstSaveCategoryImage() {
		
		@Override
		public boolean operationAfterSaveImage(List<Category> categoryList,boolean isFirst,
				int date,List<Category> delCategoryList,String result) {
            if(isFirst){
            	CategoryDB categoryDB = CategoryDB.getInstance(mContext);
                categoryDB.insertAfterDeleteAll(categoryList);
                delCategoryList = categoryDB.queryHasCategoryNotToday();
                Common.setUpdateCategoryDate(mContext,date);
                JsonUtil.setCategoryDataVersion(mContext, result);
                isFirst = false;
            }
			return isFirst;
		}

		@Override
		public void delOldImage(DealWithByteFile dealWithBitmap,
				boolean savedSuccess, List<Category> delList) {
			delOldCategory(dealWithBitmap,savedSuccess,delList);
		}
	};
	
}
