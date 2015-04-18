package com.android.systemui.gionee.nc;

import java.util.HashMap;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.android.internal.statusbar.IGnStatusBar;;
import android.util.Log;

public class GnNotificationListenerService extends Service {

	final private String TAG = "GnNotificationListenerService";
	HashMap<String, Integer> mChagedPackages = new HashMap<>();
	GnNotificationListener mListner = null;
	@Override
	public IBinder onBind(Intent intent) {
		Log.v(TAG, "GnNotificationListenerService -- onBind");
		Log.v(TAG, "intent = "+intent);
		// TODO Auto-generated method stub
		if (mListner == null) {
			mListner = new GnNotificationListener();
		}
		return mListner;
	}

	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.v(TAG, "GnNotificationListenerService -- onCreate");
		if (mListner == null) {
			mListner = new GnNotificationListener();
		}
	}
	private class GnNotificationListener extends IGnStatusBar.Stub {
		@Override
		public void onNotificationPriorityChanged(String pkg, int priority) throws android.os.RemoteException{
			GnNotificationService.getService(null).refreshChangeList(pkg, priority);
		}
		
		public void onSwipeFromBottom() throws android.os.RemoteException{}
		public void onPointerEvent(android.view.MotionEvent event) throws android.os.RemoteException{}
		public void showSimIndicator(java.lang.String businessType) throws android.os.RemoteException{}
		public void hideSimIndicator() throws android.os.RemoteException{}
	}
}
