package com.amigo.navi.keyguard.modules;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

import com.amigo.navi.keyguard.DebugLog;
import com.android.keyguard.KeyguardUpdateMonitor;

abstract public class KeyguardModuleBase {
	private static final String LOG_TAG = "KeyguardModuleBase";
	
    protected BroadcastReceiver mReceiver = null;
    protected IntentFilter mFilter = null;
    protected Context mContext = null;
    protected KeyguardUpdateMonitor mUpdateMonitor = null;
    
    protected KeyguardModuleBase(Context context, KeyguardUpdateMonitor updateMonitor) {
    	mContext = context;
    	mUpdateMonitor = updateMonitor;
    	
    	initModule();
    }
    
    abstract protected void initModule();

    public void registerReceiver() {
    	if(mReceiver != null && mFilter != null) {
    		if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "registerReceiver() for " + this.getClass().getSimpleName());
        	mContext.registerReceiver(mReceiver, mFilter);
    	}
    }
    
    public void unregisterReceiver() {
    	if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "unregisterReceiver()");
    	if(mReceiver != null) {
        	mContext.unregisterReceiver(mReceiver);
    	}
    }
}
