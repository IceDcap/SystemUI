package com.android.systemui.gionee;

import android.os.SystemProperties;

public final class GnFeatureOption
{
    public static boolean GN_CTCC_SUPPORT = "ctcc".equals(SystemProperties.get("ro.gn.custom.operators"));
}