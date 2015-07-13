/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
package com.android.systemui.gionee.cc.camera.service;

import com.android.systemui.gionee.cc.camera.GnCameraUtil;
import com.android.systemui.gionee.cc.camera.GnImageManager;
import com.android.systemui.gionee.cc.camera.GnImageManager.PhotoData;
import com.android.systemui.gionee.cc.camera.GnImageManager.PhotoFileInfo;
import com.android.systemui.R;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;

public class GnShortCutServices extends Service {
    private static final String LOG_TAG = "QkCamera_ShortCutServices";
	
    public static final String DCIM = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DCIM).toString();
    public static final String DIRECTORY = DCIM + "/Camera";

    public class PhotoServiceImpl extends IPhotoService.Stub {
        @Override
        public Uri savePic(int[] degree, byte[] data) throws RemoteException {

        	ContentResolver cr = GnShortCutServices.this.getContentResolver();
        	PhotoFileInfo info = PhotoFileInfo.create(System.currentTimeMillis());
        	PhotoData photoData = new PhotoData(null, data);
        	Uri uri = GnImageManager.savePhoto(cr, info, photoData, degree);
            return uri;
        }

        @Override
        public boolean isSdCardOK() throws RemoteException {
            return GnCameraUtil.hasStorage(getApplicationContext());
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        GnImageManager.setPhotoDirectory(DIRECTORY);
    }

    @Override
    public IBinder onBind(Intent intent) {

        return new PhotoServiceImpl();
    }

}
