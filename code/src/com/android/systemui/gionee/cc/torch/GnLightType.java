package com.android.systemui.gionee.cc.torch;

public interface GnLightType {
	// Gionee <jiangxiao> <2014-01-21> CR01029524 begin
	public static final int LIGHT_TYPE_CAMERA = -1;
	public static final int LIGHT_TYPE_POWER = 0;
	// Gionee <jiangxiao> <2014-01-21> CR01029524 end
    
    public int getType();

}
