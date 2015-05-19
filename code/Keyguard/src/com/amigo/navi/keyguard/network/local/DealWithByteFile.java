package com.amigo.navi.keyguard.network.local;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.KWDataCache;
import com.amigo.navi.keyguard.network.local.utils.DiskUtils;

import android.content.Context;

public class DealWithByteFile{
    private static final String TAG = "DealWithBitmapFromLocal";
    private Context mContext;
    private String mFolderName;
    private String mPath;
    private boolean isNeedSavedToLocal = true;
	private int mScreenWid;
    public DealWithByteFile(Context context,String folderName,String path){
        mContext = context.getApplicationContext();
        mFolderName = folderName;
        mPath = path;
        mScreenWid = KWDataCache.getScreenWidth(context.getResources());
    }
    
    public Object readFromLocal(String key) {
        DebugLog.d(TAG,"readFromLocal url:" + key);
        String file = mPath + File.separator + mFolderName + File.separator + key;
        Object bitmap = DiskUtils.readFile(file,mScreenWid);
        return bitmap;
    }

    public boolean writeToLocal(String key, byte[] obj) {
        if(obj == null){
            return false;
        }
        byte[] bytes = (byte[])obj;
        if(bytes.length == 0){
        	return false;
        }
        DebugLog.d(TAG, "writeToLocal isNeedSavedToLocal:" + isNeedSavedToLocal);
        DebugLog.d(TAG, "writeToLocal mPath:" + mFolderName);
        String path = mPath + File.separator + mFolderName;
        boolean isSuccess = DiskUtils.saveBitmap(bytes,key,path);
        return isSuccess;
    }

    public void deleteFile(String key){
        String file = mPath + File.separator + mFolderName + File.separator + key;
    	DiskUtils.deleteFile(file);
    }
    
}
