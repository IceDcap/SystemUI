package com.amigo.navi.keyguard.network.local;

import java.io.InputStream;
import java.io.OutputStream;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.KWDataCache;
import com.amigo.navi.keyguard.network.local.utils.DiskUtils;

import android.content.Context;
import android.graphics.Bitmap;

public class LocalBitmapOperation implements LocalFileOperationInterface {
    private static final String TAG = "LocalBitmapOperation";
    private Context context;
    private int mScreenWid = 0;
    public LocalBitmapOperation(Context context) {
    	context = context.getApplicationContext();
    	mScreenWid = KWDataCache.getScreenWidth(context.getResources());
	}
    
    public LocalBitmapOperation() {
	}
    
    @Override
    public Object readFile(InputStream is) {
        return DiskUtils.decodeBitmap(is,mScreenWid);
    }

    @Override
    public boolean saveFile(Object obj, OutputStream os) {
        DebugLog.d(TAG,"saveBitmap obj:" + obj);
        if(obj == null){
            return false;
        }
        Bitmap bitmap = (Bitmap) obj;
        return saveBitmapToLocal(bitmap,os);
    }

    private boolean saveBitmapToLocal(Bitmap bitmap,OutputStream outputStream) {
        DebugLog.d(TAG,"saveBitmapToLocal");
        try {
            byte[] bts = DiskUtils.convertBitmap(bitmap);
            DiskUtils.saveBitmap(bts,outputStream);
            return true;
        } catch (Exception e) {
            DebugLog.d(TAG,"saveBitmapToLocal e:" + e);
        }
        return false;
    }

	@Override
	public boolean saveFileByByte(byte[] bytes, OutputStream os) {
        if(bytes == null || bytes.length == 0){
            return false;
        }
        DebugLog.d(TAG,"saveBitmapToLocal");
        try {
            DebugLog.d(TAG,"saveFileByByte bytes:" + bytes.length);
            DiskUtils.saveBitmap(bytes,os);
            return true;
        } catch (Exception e) {
            DebugLog.d(TAG,"saveBitmapToLocal e:" + e);
        }
        return false;
	}
    
}
