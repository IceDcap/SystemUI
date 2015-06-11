package com.amigo.navi.keyguard.network.local;


import com.amigo.navi.keyguard.network.local.utils.DiskUtils;

import android.content.Context;
import android.graphics.Bitmap;

public class ReadFileFromAssets implements DealWithFromLocalInterface{
    private static final String TAG = "DealWithBitmapFromLocal";
    private Context mContext;
    private String mPath;

	    private Bitmap mReuseBitmap = null;
    
	public void setmReuseBitmap(Bitmap mReuseBitmap) {
		this.mReuseBitmap = mReuseBitmap;
	}
    public ReadFileFromAssets(Context context,String path){
    	mContext = context;
    	mPath = path;
    }
    
    private static final String SYSTEM_FILE_PATH = "/system/etc/ScreenLock/";
    @Override
    public Bitmap readFromLocal(String key) {
     	String path = SYSTEM_FILE_PATH + key;
    	Bitmap bitmap = DiskUtils.getImageFromSystem(mContext.getApplicationContext(),path, mReuseBitmap);
    	return bitmap;
    }

    @Override
    public boolean writeToLocal(String key, Bitmap bitmap) {
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
