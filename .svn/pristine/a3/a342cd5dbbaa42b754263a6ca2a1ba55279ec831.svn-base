package com.amigo.navi.keyguard;



import com.android.keyguard.ViewMediatorCallback;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;


public class AmigoLockOrUnlockReceiver {
	private static final String TAG="AmigoExternalTriggerLock";
	private static final String ACTION_DISABLE_KEYGUARD = "com.gionee.action.DISABLE_KEYGUARD";
	private static final String ACTION_REENABLE_KEYGUARD = "com.gionee.action.REENABLE_KEYGUARD";
	
	UnlockReceiver mReceiver = null;
	ViewMediatorCallback mCallback = null;
	
	private static AmigoLockOrUnlockReceiver sInstance = null;
	
	public static AmigoLockOrUnlockReceiver getInstance(Context context) {
		if(sInstance == null) {
			sInstance = new AmigoLockOrUnlockReceiver(context);
		}
		
		return sInstance;
	}
	
	private AmigoLockOrUnlockReceiver(Context context) {
		if(context != null) {
			initUnlockReceiver(context);
		}
	}
	
	private void initUnlockReceiver(Context context) {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_DISABLE_KEYGUARD);
		filter.addAction(ACTION_REENABLE_KEYGUARD);
		mReceiver = new UnlockReceiver();
		
		context.registerReceiver(mReceiver, filter);
		if(DebugLog.DEBUG) DebugLog.d(TAG, "unlock receiver registered");
	}
	
	public void setUnlockCallback(ViewMediatorCallback callback) {
		mCallback = callback;
	}
	
	private class UnlockReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			String sender = intent.getStringExtra("sender");
			if(DebugLog.DEBUG) DebugLog.d("sender", "UnlockReceiver sender:"+sender+"action="+action);
			if(mCallback!=null){
				if(action.equals(ACTION_DISABLE_KEYGUARD)) {
					if(DebugLog.DEBUG) DebugLog.d(TAG, "receive ACTION_DISABLE_KEYGUARD");
					mCallback.unlockKeyguardByOtherApp();
				} else if(action.equals(ACTION_REENABLE_KEYGUARD)) {
					if(DebugLog.DEBUG) DebugLog.d(TAG, "receive ACTION_REENABLE_KEYGUARD");
					mCallback.lockKeyguardByOtherApp();
				}
			}
		}
	}
	
	public interface AmigoUnlockCallback {
		
		void unlockKeyguardByOtherApp();			
		void lockKeyguardByOtherApp();
	}
}
