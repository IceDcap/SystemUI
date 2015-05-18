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
import com.amigo.navi.keyguard.network.local.ReadFileFromSD;
import com.amigo.navi.keyguard.network.local.LocalFileOperation;
import com.amigo.navi.keyguard.network.local.utils.DiskUtils;

import android.content.Context;
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
    private ReadFileFromSD mDealWithWallpaperFile = null;
    private ReadFileFromSD mDealWithCategoryFile = null;

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
        LocalFileOperation localFileOperation = new LocalFileOperation(context);
        mDealWithWallpaperFile = new ReadFileFromSD(context, DiskUtils.WALLPAPER_BITMAP_FOLDER, 
                DiskUtils.getCachePath(context.getApplicationContext())
                , localFileOperation);
        mDealWithCategoryFile = new ReadFileFromSD(context, DiskUtils.CATEGORY_BITMAP_FOLDER, 
                DiskUtils.getCachePath(context.getApplicationContext())
                , localFileOperation);
    }
    
    public void initData(){
        saveInitDataToClientDB(mContext);
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        boolean flag = Common.getHaoKanDataInit(mContext);
//                        DebugLog.d(TAG,"initData flag:" + flag);
//                        if(!flag){
//                            boolean copySuccess = copyDataToSD(mContext);
//                            if(savedSuccess && copySuccess){
//                                Common.setHaoKanDataInit(mContext, true);
//                            }
//                        } 
//                    }
//                }).start();
    }  
    
    private final static String DBNAME = "haokan.db";
    private final static String ADDSTR = "/data/data/com.android.systemui/databases/";
    private final static String DB_VERSION = "20150513";
    public boolean saveInitDataToClientDB(Context context) {
        File dbFile = new File(ADDSTR + DBNAME);
        if(dbFile.exists()){
        	if(!DB_VERSION.equals(Common.getDatabaseVersion(mContext))){
        		dbFile.delete();
        	}else{
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
    private static final String WALLPAPER_FILE_NAME = "wallpaper_pics";
    private static final String CATEGORY_FILE_NAME = "category_pics";
    public boolean copyDataToSD(Context context) {
        boolean success = true;
        try {
            String[] strs = context.getAssets().list(WALLPAPER_FILE_NAME);
            DebugLog.d(TAG,"copyDataToSD strs.length:" + strs.length);
            for(int index = 0;index < strs.length;index++){
                if(TextUtils.isEmpty(strs[index])){
                    continue;
                }
                DebugLog.d(TAG,"copyDataToSD strs[index]:" + strs[index]);
               String openPath = WALLPAPER_FILE_NAME + "/" + strs[index];
               boolean flag = mDealWithWallpaperFile.writeToLocal(strs[index], openPath);
               DebugLog.d(TAG,"copyDataToSD flag:" + flag);
            }
        } catch (Exception e) {
            DebugLog.d(TAG,"copyDataToSD error:" + e);
            e.printStackTrace();
            success = false;
        }
        
        try {
            String[] strs = context.getAssets().list(CATEGORY_FILE_NAME);
            for(int index = 0;index < strs.length;index++){
                if(TextUtils.isEmpty(strs[index])){
                    continue;
                }
                String openPath = CATEGORY_FILE_NAME + "/" + strs[index];
                mDealWithCategoryFile.writeToLocal(strs[index], openPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }
        return success;
    }

    
    
}
