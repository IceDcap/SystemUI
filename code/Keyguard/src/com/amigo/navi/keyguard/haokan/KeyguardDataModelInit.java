package com.amigo.navi.keyguard.haokan;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.haokan.db.CategoryDB;
import com.amigo.navi.keyguard.haokan.db.WallpaperDB;
import com.amigo.navi.keyguard.haokan.entity.WallpaperList;
import com.amigo.navi.keyguard.network.local.ReadAndWriteFileFromSD;
import com.amigo.navi.keyguard.network.local.LocalFileOperation;
import com.amigo.navi.keyguard.network.local.utils.DiskUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

public class KeyguardDataModelInit {
    
    private static final String TAG = "KeyguardDataModelInit";
    private Context mContext = null;

    private static KeyguardDataModelInit sInstance = null;
    
    public static synchronized KeyguardDataModelInit getInstance(Context context){
        if(sInstance == null){
            sInstance = new KeyguardDataModelInit(context);
        }
        return sInstance;
    }
    
    private KeyguardDataModelInit(){
        
    }
    
    private KeyguardDataModelInit(Context context){
        mContext = context.getApplicationContext();
    }
    
    public void initData(){
        saveInitDataToClientDB(mContext);
        initAlarm();
        insertLocalDataToDBIfDBNull();
    }  
    
    private void insertLocalDataToDBIfDBNull(){
        boolean flag = WallpaperDB.getInstance(mContext.getApplicationContext())
                .queryHasDownLoadImage(); 
        if(!flag){
            insertLocalDataToDB();
            return;
        }

    }

	private void insertLocalDataToDB() {
		WallpaperList WallpaperList = JsonUtil.getDefaultWallpaperList();
		WallpaperDB.getInstance(mContext).replaceWallpapers(WallpaperList);
	}
    
    public void initAlarm(){
        initBroadcastReceiver();
//        TimeControlManager.getInstance().init(mContext);
        TimeControlManager.getInstance(mContext).startUpdateAlarm();
    }
    
    private BroadcastReceiver mReveicer;
    private void initBroadcastReceiver() {
        if (null != mReveicer) {
            mContext.getApplicationContext().unregisterReceiver(mReveicer);
        }
        mReveicer = new NicePictureReveicer();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.WALLPAPER_TIMING_UPDATE);
//        filter.addAction(Constant.DATA_CHANGE);
        mContext.getApplicationContext().registerReceiver(mReveicer, filter);
    }
    
    private class NicePictureReveicer extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            DebugLog.d(TAG,"onReceive wallpaper alarm");
            String action = intent.getAction();
            if (Constant.WALLPAPER_TIMING_UPDATE.equals(action)) {
                TimeControlManager.getInstance(context).cancelUpdateAlarm();
                TimeControlManager.getInstance(context).startUpdateAlarm();
//                RequestNicePicturesFromInternet.getInstance(mContext.getApplicationContext()).shutDownWorkPool();
                RequestNicePicturesFromInternet.getInstance(mContext.getApplicationContext()).registerData(false);
            } 
        }
    }
    
    public void realse(){
        if(mReveicer != null){
            mContext.unregisterReceiver(mReveicer);
        }
        TimeControlManager.getInstance(mContext).release();
    }
    
    private final static String DBNAME = "haokan.db";
    private final static String ADDSTR = "/data/data/com.android.systemui/databases/";
    private final static String DB_VERSION = "20150525";
    public boolean saveInitDataToClientDB(Context context) {
        File dbFile = new File(ADDSTR + DBNAME);
        if(dbFile.exists()){
        	if(!DB_VERSION.equals(Common.getDatabaseVersion(mContext))){
        			dbFile.delete();
                	Common.setUpdateWallpaperDate(mContext,0);
                	Common.setUpdateCategoryDate(mContext,0);
        	}else{
            	if(isNeedToDelDBWhenImageError(dbFile)){
                    Common.setUpdateCategoryDate(mContext,0);
            	}
                return true;
        	}
        }
        InputStream assetsDB;
        try {
            File fileDir = new File(ADDSTR);
            DebugLog.d(TAG,"saveInitDataToClientDB ADDSTR:" + ADDSTR);
            if(!fileDir.exists()){
                fileDir.mkdir();  
            }
            File file = new File(ADDSTR + DBNAME);
            if(!file.exists()){
                file.createNewFile();  
            }
            assetsDB = context.getAssets().open(DBNAME);
            OutputStream dbOut = new FileOutputStream(ADDSTR + DBNAME);
            byte[] buffer = new byte[1024];
                int length;
                DebugLog.d(TAG,"saveInitDataToClientDB write");
                while ((length = assetsDB.read(buffer)) > 0) {
                    dbOut.write(buffer, 0, length);
                }

                dbOut.flush();
                dbOut.close();
                assetsDB.close();
            	Common.setDatabaseVersion(mContext, DB_VERSION);
                return true;
        } catch (IOException e) {
            DebugLog.d(TAG,"saveInitDataToClientDB error:" + e);
            e.printStackTrace();
        }
        return false;
    }

	private boolean isNeedToDelDBWhenImageError(File dbFile) {
		boolean isNeedToDel = false;
		boolean hasNotLocalData = CategoryDB.getInstance(mContext.getApplicationContext())
          .queryHasDownLoadImageNotLocalData(); 
          String path = DiskUtils.getCachePath(mContext);
          String folder = path + File.separator + DiskUtils.CATEGORY_BITMAP_FOLDER;
          File file = new File(folder);
          DebugLog.d(TAG,"isNeedToDelDBWhenImageError hasNotLocalData:" + hasNotLocalData);
          DebugLog.d(TAG,"isNeedToDelDBWhenImageError file:" + file);
          if(hasNotLocalData && file != null){
              DebugLog.d(TAG,"isNeedToDelDBWhenImageError file.exists():" + file.exists());
          		if(!file.exists()){
          			return true;
          		}
                DebugLog.d(TAG,"isNeedToDelDBWhenImageError file.isDirectory():" + file.isDirectory());
          		if(file.isDirectory() && file.list().length == 0) {
                    DebugLog.d(TAG,"isNeedToDelDBWhenImageError file.list().length:" + file.list().length);
          			return true;
          		}
            }
          	return isNeedToDel;
	}
    
}
