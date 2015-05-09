package com.amigo.navi.keyguard.network.local;

import java.io.InputStream;
import java.io.OutputStream;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.network.local.utils.DiskUtils;

import android.graphics.Bitmap;

public class LocalBitmapOperation implements LocalFileOperationInterface {
    private static final String TAG = "LocalBitmapOperation";
    
    @Override
    public Object readFile(InputStream is) {
        return DiskUtils.decodeBitmap(is);
    }

    @Override
    public boolean saveFile(Object obj, OutputStream os) {
        if(obj == null){
            return false;
        }
        Bitmap bitmap = (Bitmap) obj;
        return saveBitmapToLocal(bitmap,os);
    }

    private synchronized boolean saveBitmapToLocal(Bitmap bitmap,OutputStream outputStream) {
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
    
}
