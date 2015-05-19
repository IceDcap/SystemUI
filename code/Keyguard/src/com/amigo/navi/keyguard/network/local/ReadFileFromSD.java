package com.amigo.navi.keyguard.network.local;

import java.io.File;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.KWDataCache;
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
    private int mScreenWid = 0;
    public ReadFileFromSD(Context context,String folderName,String path,
            LocalFileOperationInterface localFileOperation){
        mContext = context.getApplicationContext();
        mFolderName = folderName;
        mPath = path;
        mScreenWid = KWDataCache.getScreenWidth(context.getResources());
    }
    
    @Override
    public Object readFromLocal(String key) {
        DebugLog.d(TAG,"readFromLocal url:" + key);
        String file = mPath + File.separator + mFolderName + File.separator + key;
        Object bitmap = DiskUtils.readFile(file,mScreenWid);
        return bitmap;
    }

    @Override
    public boolean writeToLocal(String key, Object obj) {
        return false;
    }

    @Override
    public boolean deleteAllFile() {
    	return false;
    }

    @Override
    public boolean deleteFile(String key) {
    	return false;
    }
    
	@Override
	public void closeCache() {

	}
    
}
