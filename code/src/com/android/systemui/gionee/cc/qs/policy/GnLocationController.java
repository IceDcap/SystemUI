/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/

package com.android.systemui.gionee.cc.qs.policy;

public interface GnLocationController {
    boolean isLocationEnabled();
    boolean setLocationEnabled(boolean enabled);
    void addSettingsChangedCallback(LocationSettingsChangeCallback cb);
    void removeSettingsChangedCallback(LocationSettingsChangeCallback cb);

    /**
     * A callback for change in location settings (the user has enabled/disabled location).
     */
    public interface LocationSettingsChangeCallback {
        /**
         * Called whenever location settings change.
         *
         * @param locationEnabled A value of true indicates that at least one type of location
         *                        is enabled in settings.
         */
        void onLocationSettingsChanged(boolean locationEnabled);
    }
}
