package com.amigo.navi.keyguard.network.local;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.network.local.manager.DiskLruCache;
import com.amigo.navi.keyguard.network.local.manager.DiskManager;
import com.amigo.navi.keyguard.network.local.utils.DiskUtils;

import android.content.Context;
import android.graphics.Bitmap;

public class ReadFileFromSD implements DealWithFromLocalInterface{
    private static final String TAG = "DealWithBitmapFromLocal";
    private Context mContext;
    private String mFolderName;
    private String mPath;
    private boolean isNeedSavedToLocal = true;
    private LocalFileOperationInterface mLocalFileOperation = null;
    public ReadFileFromSD(Context context,String folderName,String path,
            LocalFileOperationInterface localFileOperation){
        mContext = context.getApplicationContext();
        mFolderName = folderName;
        mPath = path;
        mLocalFileOperation = localFileOperation;
    }
    
    @Override
    public Object readFromLocal(String key) {
        DebugLog.d(TAG,"readFromLocal url:" + key);
        DiskManager diskManager = DiskManager.getInstance();
        diskManager.setLocalFileOperation(mLocalFileOperation);
        DiskLruCache diskLruCache = diskManager.openDiskLruCache(mContext, DiskUtils.VERSION, mFolderName,mPath);
        Object obj = diskManager.readFileFromLocal(diskLruCache, key);
        DebugLog.d(TAG,"readFromLocal obj:" + obj);
        diskManager.close(diskLruCache);
        return obj;
    }

    @Override
    public boolean writeToLocal(String key, Object obj) {
        if(obj == null){
            return false;
        }
        DebugLog.d(TAG, "writeToLocal isNeedSavedToLocal:" + isNeedSavedToLocal);
        DebugLog.d(TAG, "writeToLocal mPath:" + mFolderName);
        DiskManager diskManager = DiskManager.getInstance();
        diskManager.setLocalFileOperation(mLocalFileOperation);
        DiskLruCache diskLruCache = createDiskLruCache(diskManager,DiskUtils.VERSION);
        boolean isSuccess = diskManager.saveFile(diskLruCache, key, obj);
        diskManager.close(diskLruCache);
        return isSuccess;
    }

    /**
     * @param diskManager
     * @return
     */
    private DiskLruCache createDiskLruCache(DiskManager diskManager,int version) {
        DebugLog.d(TAG,"createDiskLruCache diskManager:" + diskManager);
        DebugLog.d(TAG,"createDiskLruCache mPath:" + mFolderName);
//        DiskLruCache diskLruCache = diskManager.getOpenedDiskLruCache(mPath);
        DiskLruCache diskLruCache = null;
        DebugLog.d(TAG,"createDiskLruCache diskLruCache 1:" + diskLruCache);
//        if(diskLruCache == null){
            diskLruCache = diskManager.openDiskLruCache(mContext, version, mFolderName,mPath);
//        }
        DebugLog.d(TAG,"createDiskLruCache diskLruCache 2:" + diskLruCache);
        return diskLruCache;
    }

    @Override
    public boolean deleteAllFile() {
        DiskManager diskManager = DiskManager.getInstance();
        DiskLruCache diskLruCache = createDiskLruCache(diskManager,DiskUtils.VERSION);
        return diskManager.clear(diskLruCache);
    }

    @Override
    public boolean deleteFile(String key) {
        DiskManager diskManager = DiskManager.getInstance();
        DiskLruCache diskLruCache = createDiskLruCache(diskManager,DiskUtils.VERSION);
        return diskManager.delete(diskLruCache, key);
    }
    
}
