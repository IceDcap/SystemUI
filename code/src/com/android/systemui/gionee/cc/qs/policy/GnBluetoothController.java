/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/

package com.android.systemui.gionee.cc.qs.policy;

import java.util.Set;

public interface GnBluetoothController {
    void addStateChangedCallback(Callback callback);
    void removeStateChangedCallback(Callback callback);

    boolean isBluetoothSupported();
    boolean isBluetoothEnabled();
    boolean isBluetoothConnected();
    boolean isBluetoothConnecting();
    String getLastDeviceName();
    void setBluetoothEnabled(boolean enabled);
    Set<PairedDevice> getPairedDevices();
    void connect(PairedDevice device);
    void disconnect(PairedDevice device);

    public interface Callback {
        void onBluetoothStateChange(boolean enabled, boolean connecting);
        void onBluetoothPairedDevicesChanged();
    }

    public static final class PairedDevice {
        public static final int STATE_DISCONNECTED = 0;
        public static final int STATE_CONNECTING = 1;
        public static final int STATE_CONNECTED = 2;
        public static final int STATE_DISCONNECTING = 3;

        public String id;
        public String name;
        public int state = STATE_DISCONNECTED;
        public Object tag;

        public static String stateToString(int state) {
            if (state == STATE_DISCONNECTED) return "STATE_DISCONNECTED";
            if (state == STATE_CONNECTING) return "STATE_CONNECTING";
            if (state == STATE_CONNECTED) return "STATE_CONNECTED";
            if (state == STATE_DISCONNECTING) return "STATE_DISCONNECTING";
            return "UNKNOWN";
        }
    }
}
