package com.amigo.navi.keyguard.network.local;

import java.io.File;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.KWDataCache;
import com.amigo.navi.keyguard.network.local.utils.DiskUtils;

import android.content.Context;
import android.graphics.Bitmap;

public class ReadAndWriteFileFromSD implements DealWithFromLocalInterface{
    private static final String TAG = "DealWithBitmapFromLocal";
    private Context mContext;
    private String mFolderName;
    private String mPath;
    private int mScreenWid = 0;
//    private Bitmap mReuseBitmap = null;
    private ReuseImage mReuseImage;
    
    @Override
    public void setReuseBitmap(ReuseImage reuseImage) {
        this.mReuseImage = reuseImage;
    }

	private LocalFileOperationInterface mLocalFileOperationInterface = null;
    public ReadAndWriteFileFromSD(Context context,String folderName,String path,
            LocalFileOperationInterface localFileOperation){
        mContext = context.getApplicationContext();
        mFolderName = folderName;
        mPath = path;
        mLocalFileOperationInterface = localFileOperation;
        mScreenWid = KWDataCache.getScreenWidth(context.getResources());
    }
    
    @Override
    public Bitmap readFromLocal(String key) {
        DebugLog.d(TAG,"readFromLocal url:" + key);
        String file = mPath + File.separator + mFolderName + File.separator + key;
        Bitmap bitmap = DiskUtils.readFile(file,mScreenWid, mReuseImage);
        return bitmap;
    }

    @Override
    public boolean writeToLocal(String key, Bitmap bitmap) {
        boolean success = false;
        if(bitmap != null){
            String path = mPath + File.separator + mFolderName;
            success = DiskUtils.saveBitmap(bitmap, key, path);
        }
		DebugLog.d(TAG,"writeToLocal success:" + success);
        return success;
    }

    @Override
    public boolean deleteAllFile() {
    	return false;
    }

    @Override
    public boolean deleteFile(String key) {
        String file = mPath + File.separator + mFolderName + File.separator + key;
    	DiskUtils.deleteFile(file);
    	
    	DiskUtils.deleteFile(file + DiskUtils.THUMBNAIL);
        
    	return true;
    }
    
	@Override
	public void closeCache() {

	}
    
}
