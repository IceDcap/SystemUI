package com.amigo.navi.keyguard.network.connect;


import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.haokan.RequestNicePicturesFromInternet;
import com.amigo.navi.keyguard.haokan.PlayerManager;
import com.amigo.navi.keyguard.haokan.TimeControlManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
public class NetworkChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkChangeReceiver";
	private Context mContext;
	@Override
	public void onReceive(Context context, Intent intent) {
	    DebugLog.d(TAG,"test onReceive");
		mContext=context;
		boolean networkState = NetWorkUtils.isNetworkAvailable(mContext);
		PlayerManager.getInstance().netStateChange(networkState);
        DebugLog.d(TAG,"test onReceive networkState:" + networkState);
        RequestNicePicturesFromInternet nicePicturesInit = RequestNicePicturesFromInternet.getInstance(context);
		if(networkState && NetWorkUtils.isDownloadingDataFromInternet(context)){
			NetWorkUtils.setInterruptDownload(false);
			try {
				if(!TimeControlManager.getInstance(context).isFinishUpdateTime()){
//		        TimeControlManager.getInstance().init(mContext);
					TimeControlManager.getInstance(context).cancelUpdateAlarm();
		        	TimeControlManager.getInstance(context).startUpdateAlarm();
				}
			} catch (Exception e) {
				DebugLog.d(TAG,"onReceive error:" + e);
			}	
		    nicePicturesInit.registerData(false);
		}else{
			NetWorkUtils.setInterruptDownload(true);
	        nicePicturesInit.shutDownWorkPool();
	        
		}
	}
}
