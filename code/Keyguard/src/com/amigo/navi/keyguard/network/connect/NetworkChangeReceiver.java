package com.amigo.navi.keyguard.network.connect;


import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.haokan.NicePicturesInit;

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
		boolean networkState = isNetworkAvailable(mContext);
        DebugLog.d(TAG,"test onReceive networkState:" + networkState);
        NicePicturesInit nicePicturesInit = NicePicturesInit.getInstance(context);
		if(networkState){
		    nicePicturesInit.registerData();
		}else{
	        nicePicturesInit.shutDownWorkPool();
		}
	}
	
	// 网路是否可用
		public boolean isNetworkAvailable(Context context) {
			try {
				ConnectivityManager cn = (ConnectivityManager) context
						.getSystemService(Context.CONNECTIVITY_SERVICE);
				if (cn != null) {
					NetworkInfo info = cn.getActiveNetworkInfo();
					if (info != null && info.isConnected()) {
						if (info.getState() == NetworkInfo.State.CONNECTED) {
							return true;
						}
					}
				}
			} catch (Exception e) {
				return false;
			}
			return false;

		}
}
