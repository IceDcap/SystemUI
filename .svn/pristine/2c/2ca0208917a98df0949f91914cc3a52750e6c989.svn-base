
package com.android.systemui.gionee.cc.torch;

import android.content.Context;
import android.util.Log;

public class GnTorchManager {

    private static final String TAG = "GnTorchManager";

    private Context mContext;
    private GnTorchType mTorchType = null;
    private GnTorch mTorch = null;

    private static GnTorchManager mInstance = null;

    public static GnTorchManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new GnTorchManager(context);
        }
        return mInstance;
    }

    public GnTorchManager(Context mContext) {
        this.mContext = mContext;
        initTorch();

    }

    public GnTorch getTorch() {
        return mTorch;
    }

    public boolean getTorchState() {
        boolean lightState = false;
        if (mTorch != null) {
            lightState = mTorch.getTorchState();
        }
        return lightState;
    }

    public void setTorchOn() {
        if (mTorch != null) {
            mTorch.on();
        }
    }

    public void setTorchOff() {
        if (mTorch != null) {
            mTorch.off();
        }
    }

    private void initTorch() {
        mTorchType = new GnReflectTorchType(mContext);
        int type = mTorchType.getType();
        if (type == GnTorchType.LIGHT_TYPE_POWER) {
            Log.d(TAG, "init power torch");
            mTorch = new GnPowerTorch(mContext);
        } else {
            Log.d(TAG, "init camera torch");
            mTorch = new GnCameraTorch();
        }
    }
}
