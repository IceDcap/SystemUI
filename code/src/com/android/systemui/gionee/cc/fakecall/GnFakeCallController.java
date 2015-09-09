package com.android.systemui.gionee.cc.fakecall;

public interface GnFakeCallController {
    void addStateChangeCallback(Callback cb);
    void removeStateChangeCallback(Callback cb);
    
    public interface Callback {
        void onStateChange(String label, boolean enable, boolean animating);
        void collapsePanels();
    }
}