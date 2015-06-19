package com.android.systemui.gionee;

import android.content.Context;
import android.util.Log;

import com.android.internal.util.MemInfoReader;


public class GnUtil {
    
    private static final String TAG = "GnUtil";
    
    private static MemInfoReader mMemInfoReader;
    
    private static MemInfoReader getMemInfoReader() {
        if (mMemInfoReader == null) {
            mMemInfoReader = new MemInfoReader();
        }
        return mMemInfoReader;
    }

    public static long getTotalMemory() {
        getMemInfoReader().readMemInfo();
        return getMemInfoReader().getTotalSize();
    }
    
    public static boolean isHighRamDevice() {
        long sizeM = getTotalMemory() / 1024 / 1024;
        if (sizeM > 1024) {
            return true;
        }
        return false;
    }
    
    public static boolean isHighResolution(Context context) {
        int widthPixels = context.getResources().getDisplayMetrics().widthPixels;
        if (widthPixels > 720) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isHighDevice(Context context) {
        return isHighRamDevice() && isHighResolution(context);
    }
    
    public static final int STATE_LOCK_UNLOCK = 0;
    public static final int STATE_LOCK_BY_NOTIFICATION = 1;
    public static final int STATE_LOCK_BY_CONTROLCENTER = 2;
    public static int sState = STATE_LOCK_UNLOCK;
    
    public static int getLockState() {
        return sState;
    }
    
    public static void setLockState(int state) {
        Log.d(TAG, "set Lock state  " + sState + " -> " + state);
        sState = state;
    }
}
