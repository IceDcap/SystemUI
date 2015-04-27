/*  
 *   unmount  otg devices  and  format otg devices
 *    wangyaohui @ 2014.1.18
*/

package com.android.systemui.usb;

import android.util.Log;
import android.app.Service;
import android.content.Intent;
import android.os.storage.IMountService;
import android.os.ServiceManager;
import android.os.RemoteException;
import android.os.IBinder;

public class GnOtgService extends Service {
    public static final String TAG = "GnOtgService";
    public static final String VOLUME_PATH = "volume_path";
    public static final String CMD = "command";
    public static final int CMD_DEFAULT = 0;
    public static final int CMD_UNMOUNT = 1;
    public static final int CMD_FORMAT = 2;
    private IMountService mMountService;

    public void onCreate() {
        Log.e(TAG, "onCreate");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand: " + startId);
        if (intent == null) {
            Log.e(TAG, "intent null, use default: /storage/usbotg");
            new OtgUnmountThread("/storage/usbotg").start();
            return startId;
        }

        String path = intent.getStringExtra(VOLUME_PATH);
        Log.e(TAG, "otg path:" + path);
        int cmd = intent.getIntExtra(CMD, CMD_DEFAULT);
        if (path == null)
            return -2;

        if (cmd == CMD_UNMOUNT) {
            new OtgUnmountThread(path).start();
        }

        return startId;
    }

    public class OtgUnmountThread {
        Thread mThread;
        String mVolumePath;

        public OtgUnmountThread(String volumePath) {
            mVolumePath = volumePath;
            mThread = new Thread() {
                public void run() {
                    doUnmount(mVolumePath);
                }
            };
        }

        public void start() {
            mThread.start();
        }

    }

    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind");
        return null;
    }

    private void doUnmount(String volumePath) {
        IMountService mountService = getMountService();
        try {
            mountService.unmountVolume(volumePath, true, false);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    private synchronized IMountService getMountService() {
        if (mMountService == null) {
            IBinder service = ServiceManager.getService("mount");
            if (service != null) {
                mMountService = IMountService.Stub.asInterface(service);
            } else {
                Log.e(TAG, "Can't get mount service");
            }
        }

        return mMountService;
    }

}
