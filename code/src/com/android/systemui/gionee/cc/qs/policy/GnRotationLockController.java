/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
package com.android.systemui.gionee.cc.qs.policy;

import com.android.systemui.statusbar.policy.Listenable;

public interface GnRotationLockController extends Listenable {
    int getRotationLockOrientation();
    boolean isRotationLockAffordanceVisible();
    boolean isRotationLocked();
    void setRotationLocked(boolean locked);
    void addRotationLockControllerCallback(RotationLockControllerCallback callback);
    void removeRotationLockControllerCallback(RotationLockControllerCallback callback);

    public interface RotationLockControllerCallback {
        void onRotationLockStateChanged(boolean rotationLocked, boolean affordanceVisible);
    }
}
