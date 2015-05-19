package com.amigo.navi.keyguard.network.local;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.network.local.utils.DiskUtils;

import android.content.Context;
import android.graphics.Bitmap;

public class LocalFileOperation implements LocalFileOperationInterface {
    private static final String TAG = "LocalFileOperation";
    private Context mContext = null; 
    
    public LocalFileOperation(Context context){
        mContext = context.getApplicationContext();
    }
    
    @Override
    public Object readFile(InputStream is) {
        return null;
    }

    @Override
    public boolean saveFile(Object obj, OutputStream os) {
    	boolean flag = false;
        if(obj == null){
            return flag;
        }
        try {
            InputStream assetsDB = mContext.getAssets().open((String) obj);     
            byte[] bts = DiskUtils.Stream2Byte(assetsDB);
            DiskUtils.saveBitmap(bts,os);
            assetsDB.close();
            flag = true;
        }catch (IOException e) {
            DebugLog.d(TAG, "saveFile ioerror:" + e);
            flag = false;
        }
        return flag;
    }

	@Override
	public boolean saveFileByByte(byte[] bytes, OutputStream os) {
		return false;
	}
}
