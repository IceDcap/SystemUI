/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
package com.android.systemui.gionee.cc.qs.policy;

public interface GnMobileDataController{

    void addMobileDataChangedCallback(MobileDataChangedCallback cb);
    void removeMobileDataChangedCallback(MobileDataChangedCallback cb);
    void setMobileDataEnabled(boolean enabled);
    boolean hasMobileDataFeature();
    boolean isMobileDataSupported();
    boolean isMobileDataEnabled();
    boolean isAirplaneEnabled();
    
    public interface MobileDataChangedCallback {
        void onMobileDataChanged();
    }
    
}