package com.amigo.navi.keyguard.network.local;

import java.io.File;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.network.local.manager.DiskLruCache;
import com.amigo.navi.keyguard.network.local.manager.DiskManager;
import com.amigo.navi.keyguard.network.local.utils.DiskUtils;

import android.content.Context;
import android.graphics.Bitmap;

public class ReadFileFromAssets implements DealWithFromLocalInterface{
    private static final String TAG = "DealWithBitmapFromLocal";
    private Context mContext;
    private String mPath;
    public ReadFileFromAssets(Context context,String path){
    	mContext = context;
    	mPath = path;
    }
    
    @Override
    public Object readFromLocal(String key) {
    	String path = mPath + File.separator + key + ".jpg";
    	Bitmap bitmap = DiskUtils.getImageFromAssetsFile(mContext.getApplicationContext(),path);
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
    
}
