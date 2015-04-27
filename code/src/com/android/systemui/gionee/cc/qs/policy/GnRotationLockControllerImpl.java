/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
package com.android.systemui.gionee.cc.qs.policy;

import android.content.Context;
import android.os.UserHandle;

import com.android.internal.view.RotationPolicy;

import java.util.concurrent.CopyOnWriteArrayList;

/** Platform implementation of the rotation lock controller. **/
public final class GnRotationLockControllerImpl implements GnRotationLockController {
    private final Context mContext;
    private final CopyOnWriteArrayList<RotationLockControllerCallback> mCallbacks =
            new CopyOnWriteArrayList<RotationLockControllerCallback>();

    private final RotationPolicy.RotationPolicyListener mRotationPolicyListener =
            new RotationPolicy.RotationPolicyListener() {
        @Override
        public void onChange() {
            notifyChanged();
        }
    };

    public GnRotationLockControllerImpl(Context context) {
        mContext = context;
        setListening(true);
    }

    public void addRotationLockControllerCallback(RotationLockControllerCallback callback) {
        mCallbacks.add(callback);
        notifyChanged(callback);
    }

    public void removeRotationLockControllerCallback(RotationLockControllerCallback callback) {
        mCallbacks.remove(callback);
    }

    public int getRotationLockOrientation() {
        return RotationPolicy.getRotationLockOrientation(mContext);
    }

    public boolean isRotationLocked() {
        return RotationPolicy.isRotationLocked(mContext);
    }

    public void setRotationLocked(boolean locked) {
        RotationPolicy.setRotationLock(mContext, locked);
    }

    public boolean isRotationLockAffordanceVisible() {
        return RotationPolicy.isRotationLockToggleVisible(mContext);
    }

    @Override
    public void setListening(boolean listening) {
        if (listening) {
            RotationPolicy.registerRotationPolicyListener(mContext, mRotationPolicyListener,
                    UserHandle.USER_ALL);
        } else {
            RotationPolicy.unregisterRotationPolicyListener(mContext, mRotationPolicyListener);
        }
    }

    private void notifyChanged() {
        for (RotationLockControllerCallback callback : mCallbacks) {
            notifyChanged(callback);
        }
    }

    private void notifyChanged(RotationLockControllerCallback callback) {
        callback.onRotationLockStateChanged(RotationPolicy.isRotationLocked(mContext),
                RotationPolicy.isRotationLockToggleVisible(mContext));
    }
}
