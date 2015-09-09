package com.android.systemui.gionee.cc.camera;
/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
import java.io.Closeable;
import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.os.ParcelFileDescriptor;

public class GnCameraUtil {
	private static final String LOG_TAG = "CameraUtil";
	
    
    public static boolean hasStorage(Context context) {
        String path = Environment.getExternalStorageDirectory().getPath() + "/.temp";
        boolean flag = false;
        File file = new File(path);
        try {
            if (file.exists()) {
                if(!file.delete()) {
                	return false;
                }
            }
            if(file.createNewFile()) {
                flag = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            flag = false;
        }
        return flag;
    }
    public static void closeSilently(Closeable c) {
        if (c == null)
            return;
        try {
            c.close();
        } catch (Throwable t) {
        }
    }

    public static void closeSilently(ParcelFileDescriptor c) {
        if (c == null)
            return;
        try {
            c.close();
        } catch (Throwable t) {
            // do nothing
        }
    }
}
