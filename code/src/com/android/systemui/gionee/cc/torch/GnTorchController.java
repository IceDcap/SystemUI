package com.android.systemui.gionee.cc.torch;

public interface GnTorchController {
    
    void addStateChangedCallback(Callback cb);
    void removeStateChangedCallback(Callback cb);
    boolean isTorchOn();
    
    public interface Callback {
        void onTorchStateChange(boolean enabled);
    }
}