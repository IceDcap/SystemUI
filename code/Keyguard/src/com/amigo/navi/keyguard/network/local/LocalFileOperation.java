package com.amigo.navi.keyguard.network.local;

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
        if(obj == null){
            return false;
        }
        try {
            InputStream assetsDB = mContext.getAssets().open((String) obj);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = assetsDB.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
            assetsDB.close();
        }catch (IOException e) {
            DebugLog.d(TAG, "saveFile ioerror:" + e);
            return false;
        }
        return true;
    }
}
