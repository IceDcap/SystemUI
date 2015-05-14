package com.android.systemui.gionee;

import java.io.File;
import java.io.IOException;

import android.os.StatFs;
import android.util.Log;

public class GnStorageHepler {

    private static final String STORAGE_SDCARD0_DIR = File.separator + "storage" + File.separator + "sdcard0";
    private static final String STORAGE_SDCARD1_DIR = File.separator + "storage" + File.separator + "sdcard1";
    private static final String STORAGE_EMULATED_DIR = File.separator + "storage" + File.separator + "emulated" + File.separator + "0";

    public static boolean isSpaceAvai() {
        long remainedSize = getRemainedSize() / 1204 / 1024;
        Log.d("hwt", "remainedSize = " + remainedSize);
        if (remainedSize > 2) {
            return true;
        } else {
            return false;
        }
    }
    
    public static long getRemainedSize() {
        StatFs statFs = new StatFs(getCanonicalPath());
        long blockSize = statFs.getBlockSize();
        long availableBlocks = statFs.getAvailableBlocks();

        return availableBlocks * blockSize;
    }

    public static String getCanonicalPath() {
        try {
            if (storageExist(STORAGE_EMULATED_DIR)) {
                return new File(STORAGE_EMULATED_DIR).getCanonicalPath();
            } else if (storageExist(STORAGE_SDCARD0_DIR)) {
                return new File(STORAGE_SDCARD0_DIR).getCanonicalPath();
            } else if (storageExist(STORAGE_SDCARD1_DIR)) {
                Log.i("GnStorageHepler", "InternalStorage not exist  , SdCard exist ");
                return new File(STORAGE_SDCARD1_DIR).getCanonicalPath();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static boolean storageExist(String path) {
        File file = new File(path);
        return file.exists() && file.canRead() && file.canWrite();
    }

}