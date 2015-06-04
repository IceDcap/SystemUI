package com.android.systemui.gionee.statusbar;

import android.util.Log;
import android.util.SparseArray;

import static android.telephony.TelephonyManager.NETWORK_TYPE_1xRTT;
import static android.telephony.TelephonyManager.NETWORK_TYPE_CDMA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EDGE;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EHRPD;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_0;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_A;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_B;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSDPA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSPA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSPAP;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSUPA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_LTE;
import static android.telephony.TelephonyManager.NETWORK_TYPE_UMTS;
import static android.telephony.TelephonyManager.NETWORK_TYPE_UNKNOWN;

/**
 * M: This enum defines the type of network type.
 */
public enum GnNetworkType {

    Type_G(0), Type_3G(1), Type_1X(2), Type_1X3G(3), Type_4G(4), Type_E(5);

    private int mTypeId;

    private GnNetworkType(int typeId) {
        mTypeId = typeId;
    }

    public int getTypeId() {
        return mTypeId;
    }

    private static final String TAG = "NetworkType";
    private static GnNetworkType sDefaultNetworkType;
    private static final SparseArray<GnNetworkType> sNetworkTypeLookup =
            new SparseArray<GnNetworkType>();

    /**
     * Get NetworkType by dataNetType.
     *
     * @param dataNetType DataNet type value.
     * @return NetworkType.
     */
    public static final GnNetworkType get(final int dataNetType) {
        final GnNetworkType networkType = sNetworkTypeLookup.get(dataNetType, sDefaultNetworkType);
        Log.d(TAG, "getNetworkType, dataNetType = " + dataNetType
                + " to NetworkType = " + networkType.name());
        return networkType;
    }
}
