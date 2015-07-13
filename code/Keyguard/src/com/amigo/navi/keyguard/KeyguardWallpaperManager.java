package com.amigo.navi.keyguard;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.amigo.navi.keyguard.haokan.BitmapUtil;
import com.amigo.navi.keyguard.haokan.Common;
import com.amigo.navi.keyguard.haokan.FileUtil;
import com.amigo.navi.keyguard.haokan.KeyguardDataModelInit;
import com.amigo.navi.keyguard.haokan.KeyguardWallpaperContainer;
import com.amigo.navi.keyguard.haokan.PlayerManager;
import com.amigo.navi.keyguard.haokan.RequestNicePicturesFromInternet;
import com.amigo.navi.keyguard.haokan.UIController;
import com.amigo.navi.keyguard.haokan.analysis.HKAgent;
import com.amigo.navi.keyguard.haokan.db.DataConstant;
import com.amigo.navi.keyguard.haokan.db.WallpaperDB;
import com.amigo.navi.keyguard.haokan.entity.Category;
import com.amigo.navi.keyguard.haokan.entity.Wallpaper;
import com.amigo.navi.keyguard.haokan.entity.WallpaperList;
import com.amigo.navi.keyguard.haokan.menu.ArcItemButton;
import com.amigo.navi.keyguard.network.ImageLoader;
import com.amigo.navi.keyguard.network.local.utils.DiskUtils;
import com.amigo.navi.keyguard.picturepage.adapter.HorizontalAdapter;
import com.amigo.navi.keyguard.picturepage.widget.KeyguardListView;
import com.amigo.navi.keyguard.picturepage.widget.LoadCacheManager;
import com.android.keyguard.ViewMediatorCallback;
import com.android.keyguard.R;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

public class KeyguardWallpaperManager {

    private static final String TAG = "KeyguardWallpaperManager";
    
    private  WallpaperList mWallpaperList;
    
    
 
    private WallpaperList mDeleteList;
//    public static boolean needDelNotTodayImg = false;

    public int displayPostion = -1;
    public int displayHour = -1;
    
    
    private boolean mDownloading;
    private boolean mDownloadComplete;
    
    private ImageLoader mImageLoader;
    
    private HorizontalAdapter mHorizontalAdapter;
    
    
    private LoadCacheManager mCacheManger;
    
    
    private UIController uiController;
    
//    private RequestNicePicturesFromInternet mNicePicturesInit;
    
    private Context mAppContext;
    
    private KeyguardListView mKeyguardListView;
    
    private KeyguardWallpaperContainer mContainer = null;
    
    private ViewMediatorCallback mViewMediatorCallback;
    
    private int mShowPage;
    
    
    public static final int MSG_UPDATE_HAOKAN_LIST = 3;
 
    public static final int MSG_UPDATE_HAOKAN_LIST_SCREEN_OFF = 4;
    
    public static final int MSG_UPDATE_HAOKAN_LIST_SCREEN_OFF_CLEARCACHE = 5;
    public static final int MSG_UPDATE_HAOKAN_WHEN_DOWNLOAD_COMPLETE = 6;
    
    private WallpaperDB mWallpaperDB;
    
    private boolean isFirst = true;
    
    public void init(Context context) {
        
        mAppContext = context.getApplicationContext();
//        mNicePicturesInit = RequestNicePicturesFromInternet.getInstance(mAppContext);
//        mNicePicturesInit.init();
        KeyguardDataModelInit.getInstance(context).initData();
        
        uiController = UIController.getInstance();
        mCacheManger = new LoadCacheManager();
        mImageLoader = new ImageLoader(mAppContext);
        mImageLoader.setCacheManger(mCacheManger);
        
        mImageLoader.setHandler(mHandler);
        mWallpaperList = new WallpaperList();
        mHorizontalAdapter = new HorizontalAdapter(context, mWallpaperList, mImageLoader);
        mKeyguardListView.setAdapter(mHorizontalAdapter);
        mWallpaperDB = WallpaperDB.getInstance(mAppContext);
        
        refreshKeyguardListView(true);
        
        PlayerManager.getInstance().init(mAppContext);
    }
    
    
    private void refreshKeyguardListView(final boolean first) {

        //isFirst = first;
        
        if (first || isDownloading() || isDownloadComplete()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                	WallpaperList wallpaperList = queryWallpaperList();
                    
                    Message msg = mHandler.obtainMessage(MSG_UPDATE_HAOKAN_LIST_SCREEN_OFF,wallpaperList);
                    mHandler.sendMessage(msg);
                }
            }).start();
            
            if (isDownloadComplete()) {
                deleteOldWallpaper();
                setDownloadComplete(false);
            }
        }else {
            Message msg = mHandler.obtainMessage(MSG_UPDATE_HAOKAN_LIST_SCREEN_OFF);
            mHandler.sendMessage(msg);
        }
 
    }
    
    
    
    
    
    
    public void onScreenTurnedOff() {
        DebugLog.d(TAG, "onScreenTurnedOff");
        refreshKeyguardListView(false);
    }
    
    public void onScreenTurnedOn(){
        Wallpaper wallpaper= UIController.getInstance().getmCurrentWallpaper();
        if (wallpaper != null){
            HKAgent.onEventScreenOn(mAppContext, UIController.getInstance().getmCurrentWallpaper());
            HKAgent.onEventIMGShow(mAppContext, UIController.getInstance().getmCurrentWallpaper());
        }

    }
    
    private void refreshPage() {
        
        final WallpaperList wallpaperList = mWallpaperList;
        
        int length = wallpaperList.size();
        if (length == 0) return;

        mShowPage = 0;
        
        int indexOfLocked = wallpaperList.indexOfLocked();

        if (indexOfLocked != -1) {
            
            int dstIndex = mKeyguardListView.getPage();
            wallpaperList.reorderLocked(indexOfLocked, dstIndex);
            mShowPage = dstIndex;
        }else {
            
            int indexOfCurrent = wallpaperList.indexOfCurrent();
            if (indexOfCurrent != -1) {
                mShowPage = indexOfCurrent;
            }else {
 
                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

                if (displayPostion == -1 && displayHour == -1) {
                    displayPostion = 0;
                }else if (displayHour != hour) {
                    displayPostion ++;
                }
                
                displayHour = hour;
                displayPostion = displayPostion % length;
                mShowPage = displayPostion;
            }
            
        }
    }
    
    
    /**
     * @return
     */
    private WallpaperList queryWallpaperList() {
        WallpaperList wallpaperList = WallpaperDB.getInstance(mAppContext)
                .queryPicturesDownLoaded();   
        return wallpaperList;
    }
    
   
    public void onClickLocked(final Wallpaper wallpaper) {
        
        DebugLog.d(TAG, "onClickLocked ImgName = " + wallpaper.getImgName() + " isLocked = " + wallpaper.isLocked());
        
        new Thread(new Runnable() {

            @Override
            public void run() {

                boolean success =  false;
                final boolean locked = wallpaper.isLocked();
                if(locked){
                    mWallpaperList.resetOrder();
                    success = unLockWallpaper(wallpaper);
                }else{
                    success = lockWallpaper(wallpaper);
                    
                }
                DebugLog.d(TAG, "onClickLocked success = " + success);
                if (success) {
                    
                    HKAgent.onEventWallpaperLock(mAppContext, wallpaper);
                    int stringResId = wallpaper.isLocked() ? R.string.haokan_tip_screen_on_show : R.string.haokan_tip_no_lock_show;
                    postShowToast(stringResId, 500);
                     
                }  
                
                if (!locked && Common.SDfree()) {
                    boolean isLocalImage = wallpaper.getImgId() == Wallpaper.WALLPAPER_FROM_PHOTO_ID;
                    String imageFileName = new StringBuffer(FileUtil.getDirectoryFavorite())
                            .append("/").append(Common.currentTimeDate()).append("_")
                            .append(isLocalImage ? wallpaper.getImgName() : wallpaper.getImgId())
                            .append(".png").toString();
                    Bitmap currentWallpaper = getBitmap(wallpaper);
                    if (currentWallpaper != null) {
                        if (FileUtil.saveWallpaper(currentWallpaper, imageFileName)) {
                            Common.insertMediaStore(mAppContext, currentWallpaper.getWidth(),
                                    currentWallpaper.getHeight(), imageFileName);
                        }
                        BitmapUtil.recycleBitmap(currentWallpaper);
                    }
                }
                
            }
        }).start();

    }
    
    
    public void onClickFavorite(final Wallpaper wallpaper) {

        new Thread(new Runnable() {
            
            @Override
            public void run() {
                boolean success = false;
                int stringResId = R.string.haokan_tip_favorite_error;
                if (Common.SDfree()) {
                    
                    Bitmap currentWallpaper = getBitmap(wallpaper);
                    
                    boolean isLocalImage = wallpaper.getImgId() == Wallpaper.WALLPAPER_FROM_PHOTO_ID;
                    
                    String imageFileName = new StringBuffer(FileUtil.getDirectoryFavorite()).append("/").append(Common.currentTimeDate()).append("_")
                            .append(isLocalImage ? wallpaper.getImgName() : wallpaper.getImgId()).append(".png").toString();
                    
                    if (currentWallpaper != null) {
                        success = FileUtil.saveWallpaper(currentWallpaper, imageFileName);
                        BitmapUtil.recycleBitmap(currentWallpaper);
                    }
                    
                    if (success) {
                        wallpaper.setFavoriteLocalPath(imageFileName);
                        wallpaper.setFavorite(true);
                        WallpaperDB.getInstance(mAppContext).updateFavorite(wallpaper);
                        Common.insertMediaStore(mAppContext, currentWallpaper.getWidth(), currentWallpaper.getHeight(), imageFileName);
                        stringResId = R.string.haokan_tip_save_gallery;
                    }
                } else {
                    stringResId = R.string.insufficient_memory;
                }
                
                postShowToast(stringResId, 300);
                
            }
        }).start();
    }
    
    
    
    private void postShowToast(final int stringResId, int delay) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DebugLog.d(TAG, "postShowToast");
                uiController.showToast(stringResId);
            }
        }, delay);
    }
    
    private boolean lockWallpaper(Wallpaper wallpaper) {
        boolean success = false;
        int indexOfLocked = mWallpaperList.indexOfLocked();
        if (indexOfLocked != -1) {
            Wallpaper wallpaperLocked = mWallpaperList.get(indexOfLocked);
            wallpaperLocked.setLocked(false);
            mWallpaperDB.updateLocked(wallpaperLocked);
        }
        wallpaper.setLocked(true);
        success = mWallpaperDB.updateLocked(wallpaper);
        return success;
    }
    
    private boolean unLockWallpaper(Wallpaper wallpaper) {

        
        boolean success = false;
        wallpaper.setLocked(false);
        success = mWallpaperDB.updateLocked(wallpaper);

//        delNotTodayWallpaper(mAppContext);
        
        return success;
        
    }
    
    private void delNotTodayWallpaper(Context context){
        WallpaperDB wallpaperDB = WallpaperDB.getInstance(context.getApplicationContext());           
        Wallpaper wallpaperNotToday = wallpaperDB.queryWallpaperNotTodayAndNotLock();
        if(wallpaperNotToday != null){
            String url = wallpaperNotToday.getImgUrl();
            if(TextUtils.isEmpty(url)){
                DiskUtils.delFile(context,url);
            }
        }
        wallpaperDB.deleteWallpaperNotTodayAndNotLock();
    }
    
    
    
    public void setSrceenLockWallpaper(Bitmap bitmap) {
        
        
        String key = DiskUtils.constructFileNameByUrl(Wallpaper.WALLPAPER_FROM_PHOTO_URL);
        String savePath = DiskUtils.getCachePath(mAppContext) + File.separator + DiskUtils.WALLPAPER_BITMAP_FOLDER;
        boolean success = DiskUtils.saveBitmap(bitmap, key, savePath);
        
        if(bitmap != null && !bitmap.isRecycled()){
            bitmap.recycle();
        }
        
        if(success){
            
            KeyguardListView keyguardListView = getKeyguardListView();
            HorizontalAdapter adapter = (HorizontalAdapter) keyguardListView.getAdapter();
            adapter.removeCacheByUrl(Wallpaper.WALLPAPER_FROM_PHOTO_URL);
            
            Wallpaper wallpaper = new Wallpaper();
            Category category = new Category();
            category.setTypeId(0);
            category.setTypeName("photo");
            wallpaper.setCategory(category);
            wallpaper.setImgId(Wallpaper.WALLPAPER_FROM_PHOTO_ID);
            String imgName = String.valueOf(System.currentTimeMillis());
            wallpaper.setImgName(imgName);
            wallpaper.setImgUrl(Wallpaper.WALLPAPER_FROM_PHOTO_URL);
            wallpaper.setType(Wallpaper.WALLPAPER_FROM_PHOTO);
            wallpaper.setTodayImage(true);
            wallpaper.setLocked(true);
            wallpaper.setRealOrder(0);
            wallpaper.setShowOrder(0);
            wallpaper.setDownloadFinish(DataConstant.DOWNLOAD_FINISH);
            wallpaper.setFavorite(false);
            wallpaper.setShowTimeBegin("NA");
            wallpaper.setShowTimeEnd("NA");
            
            int indexOfLocked = mWallpaperList.indexOfLocked();
            if (indexOfLocked != -1) {
                Wallpaper lockedWallpaper = mWallpaperList.get(indexOfLocked);
                unLockWallpaper(lockedWallpaper);
            }
            
            
            WallpaperDB wallpaperDB = WallpaperDB.getInstance(mAppContext);    
            
            int indexOfLocal = mWallpaperList.indexOfLocal();
            
            if(indexOfLocal == -1){
                wallpaperDB.insertWallpaper(0, wallpaper);
                mWallpaperList.add(wallpaper);
            }else{
                mWallpaperList.remove(indexOfLocal);
                mWallpaperList.add(indexOfLocal, wallpaper);
                wallpaperDB.updateWallpaper(wallpaper);
            }
            
            if (getImageLoader() != null) {
                getImageLoader().removeFirstLevelCache(Wallpaper.WALLPAPER_FROM_PHOTO_URL);
                getImageLoader().removeFirstLevelCache(Wallpaper.WALLPAPER_FROM_PHOTO_URL + ImageLoader.THUMBNAIL_POSTFIX);
            }
            
        }
    }
    
    
    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            
            switch (msg.what) {
                 
                case MSG_UPDATE_HAOKAN_LIST:
                  DebugLog.d(TAG,"update listview mHandler");
                  WallpaperList wallpaperList = (WallpaperList) msg.obj;
                  if(wallpaperList.size() == 0){
                      showSystemWallpaper();
                  }else{
                      
                      int index = msg.arg1;
                      if (index >= 0 && index < wallpaperList.size()) {
                          wallpaperList.remove(index);
                      }
                      if (index >= 0 && index < wallpaperList.size()) {
                          mShowPage  = index;
                      }
                      if (mShowPage >= wallpaperList.size()) {
                          mShowPage  = wallpaperList.size() - 1;
                      }
                      
                      notifyDataSetChanged(wallpaperList, mShowPage);
                      
                      UIController.getInstance().refreshWallpaperInfo();
                      refreshCache(false);
                      
                  }
                    break;
                case MSG_UPDATE_HAOKAN_LIST_SCREEN_OFF:
                    if(msg.obj!=null){
                    	mWallpaperList=(WallpaperList) msg.obj;                    	
                    }
                    updateListView();

                    break;
/*                case MSG_UPDATE_HAOKAN_LIST_SCREEN_OFF_CLEARCACHE:
                    if (!mViewMediatorCallback.isScreenOn()){
                        releaseCache();
                    }
                    break;*/
                    case MSG_UPDATE_HAOKAN_WHEN_DOWNLOAD_COMPLETE:
                        if (!mViewMediatorCallback.isScreenOn()) {
                            refreshKeyguardListView(false);
                        }
                    break;
                default:
                    break;

                }
        }
    };

    public void updateListView() {
        if (isFirst || !mViewMediatorCallback.isScreenOn())
        {
            isFirst = false;
            final WallpaperList wallpapers = mWallpaperList;
            if (wallpapers.size() == 0) {
                showSystemWallpaper();
            } else {
                refreshPage();
                notifyDataSetChanged(wallpapers, mShowPage);
                UIController.getInstance().refreshWallpaperInfo();
                // load the images to cache, which is to be shown after ScreenTurnedOn
                refreshCache(false);
            }
        }
    }


    private void notifyDataSetChanged(WallpaperList wallpapers, int postion) {

        updateHorizontalListLoopState(wallpapers);
        mHorizontalAdapter.updateDataList(wallpapers);
        mKeyguardListView.setPosition(postion);
        mHorizontalAdapter.notifyDataSetChanged();
        
    }
    
    
    private void showSystemWallpaper() {
        
        mKeyguardListView.setVisibility(View.GONE);
        mContainer.setVisibility(View.GONE);
        UIController.getInstance().getHaoKanLayout().setVisibility(View.GONE);
        mViewMediatorCallback.setKeyguardWallpaperShow(true);
        
    }
    
    
    
    public void releaseCache() {
        if(mImageLoader != null){
            mImageLoader.clearCache();
        }
        if (mKeyguardListView != null) {
            mKeyguardListView.removeallchild();
            isFirst = true;
        }
    }
    
    
    private void updateHorizontalListLoopState(WallpaperList wallpaperList) {
        if(wallpaperList.size() <= 1){
            mKeyguardListView.setCanLoop(false);
        }else{
            mKeyguardListView.setCanLoop(true);
        }
    }
    
    public void refreshCache(boolean isScreenOff) {

        Wallpaper wallpaper= UIController.getInstance().getmCurrentWallpaper();
        if (wallpaper != null) {
            mCacheManger.refreshCache(mAppContext, mImageLoader, mHorizontalAdapter.getWallpaperList(), wallpaper, isScreenOff);
        }
    }
    
    public void onScrollEnd() {

        mHorizontalAdapter.unlock();
        Wallpaper wallpaper = null;
        Object obj = mKeyguardListView.getCurrentItem();
        if(obj != null){
            wallpaper = (Wallpaper) obj;
            mCacheManger.refreshCache(mAppContext,mImageLoader, mHorizontalAdapter.getWallpaperList(),wallpaper, false);
            HKAgent.onEventIMGSwitch(mAppContext, wallpaper);
            HKAgent.onEventIMGShow(mAppContext, wallpaper);
        }
    }
    
    public void onScrollBegin() {
        mHorizontalAdapter.lock();
    }
    

    public KeyguardListView getKeyguardListView() {
        return mKeyguardListView;
    }

    public void setKeyguardListView(KeyguardListView mKeyguardListView) {
        this.mKeyguardListView = mKeyguardListView;
    }

    public KeyguardWallpaperContainer getWallpaperContainer() {
        return mContainer;
    }

    public void setWallpaperContainer(KeyguardWallpaperContainer mContainer) {
        this.mContainer = mContainer;
    }


    public ViewMediatorCallback getViewMediatorCallback() {
        return mViewMediatorCallback;
    }


    public void setViewMediatorCallback(ViewMediatorCallback mViewMediatorCallback) {
        this.mViewMediatorCallback = mViewMediatorCallback;
    }


    public boolean isDownloading() {
        return mDownloading;
    }


    public void setDownloading(boolean mDownloading) {
        this.mDownloading = mDownloading;
    }


    public boolean isDownloadComplete() {
        return mDownloadComplete;
    }


    public void setDownloadComplete(boolean mDownloadComplete) {
        this.mDownloadComplete = mDownloadComplete;
    }


    public ImageLoader getImageLoader() {
        return mImageLoader;
    }


    public Bitmap getBitmap(Wallpaper wallpaper) {
        
        Bitmap bitmap = null;
        int type = wallpaper.getType();
        String imgUrl = wallpaper.getImgUrl();
        String filePath = "";
        if (type == Wallpaper.WALLPAPER_FROM_WEB || type == Wallpaper.WALLPAPER_FROM_PHOTO) {
            filePath = DiskUtils.getAbsolutePath(mAppContext, imgUrl);
            File fileLocal = new File(filePath);
            if (fileLocal.exists()) {
                bitmap = DiskUtils.readFile(filePath, KWDataCache.getScreenWidth(mAppContext.getResources()));
            } 
        }else if (type == Wallpaper.WALLPAPER_FROM_FIXED_FOLDER) {
            filePath = FileUtil.SCREENLOCK_WALLPAPER_LOCATION + File.separator + imgUrl;
            File fileLocal = new File(filePath);
            if (fileLocal.exists()) {
                bitmap = DiskUtils.getImageFromSystem(mAppContext, filePath, null);
            } 
        }
        return bitmap;
    }
    
    
    private void deleteOldWallpaper() {
        if (mDeleteList != null) {
            for (int i = 0; i < mDeleteList.size(); i++) {
                String filePath = DiskUtils.getAbsolutePath(mAppContext, mDeleteList.get(i).getImgUrl());
                DiskUtils.deleteFile(filePath);
                DiskUtils.deleteFile(filePath + DiskUtils.THUMBNAIL);
            }
            mDeleteList.clear();
            mDeleteList = null;
        }
    }
    
    public void setDeleteList(WallpaperList list) {
        this.mDeleteList = list;
    }
    
    
    public void onDownLoadComplete() {

        DebugLog.d(TAG, "onDownLoadComplete   isScreenOn : " + mViewMediatorCallback.isScreenOn());
        if (!mViewMediatorCallback.isScreenOn()) {
            Message msg = mHandler.obtainMessage(MSG_UPDATE_HAOKAN_WHEN_DOWNLOAD_COMPLETE);
            mHandler.sendMessage(msg);
        }
    }
    
}
