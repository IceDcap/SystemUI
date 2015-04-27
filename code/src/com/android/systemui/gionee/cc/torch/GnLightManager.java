package com.android.systemui.gionee.cc.torch;

import android.content.Context;
import android.util.Log;

//TODO:考虑此类和Light类
public class GnLightManager {
	private static final String LOG_TAG = "QkLight";
    private static final String TAG = "ReflectLightType";
    
    private Context mContext;
    private GnLightType mLightType = null;
    private GnLight mLight = null;

    private static GnLightManager mInstance=null;
    public static GnLightManager getInstance(Context context){
    	if(mInstance==null){
    		mInstance=new GnLightManager(context);
    	}
    	return mInstance;
    }
    
    public GnLightManager(Context mContext) {
        this.mContext = mContext;
        initLight();
       
    }

    public GnLight getLight() {
        return mLight;
    }
    
    public boolean getLigetState(){
        boolean lightState=false;
        if(mLight!=null){
            lightState=mLight.getLightState();
        }
        return lightState;
    }
    
    public void setLightOn(){
        if(mLight!=null){
            mLight.on();
        }
    }
    public void setLightOff(){
        if(mLight!=null){
            mLight.off();
        }
        
    }
    

    // Gionee <jiangxiao> <2014-01-21> CR01029524 begin
    private void initLight() {
        mLightType = new GnReflectLightType(mContext);
        int type = mLightType.getType();
        if(type == GnLightType.LIGHT_TYPE_POWER) {
        	Log.d(LOG_TAG, "init power light");
        	mLight = new GnPowerLight(mContext);
        } else {
        	Log.d(LOG_TAG, "init camera light");
        	mLight = new GnCameraLight();
        }
    }
	// Gionee <jiangxiao> <2014-01-21> CR01029524 end

}
