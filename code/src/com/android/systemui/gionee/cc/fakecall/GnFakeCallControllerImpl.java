package com.android.systemui.gionee.cc.fakecall;

/*
 *
 * MODULE DESCRIPTION
 * add by huangwt for Android L at 20141210.
 * 
 */
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.animation.Animation;

import com.android.systemui.R;

public class GnFakeCallControllerImpl implements GnFakeCallController {
    
    private static final String TAG = "GnFakeCallController";

    private Timer mTimer = null;
    private TimerTask mTimerTask = null;
    private long mStartTime = 0;
    private long mLeftTime;

    private static GnFakeCall mVirtualPhone;
    protected Animation animation;
    private Context mContext;
    public AnimationDrawable animationDrawable;
    private static GnFakeCallControllerImpl mInstance = null;
    private final int TIME_MARGIN = 500;
    private final int TIME_DELAY = 2000; // time for starting activity
    private final int MSTOS = 1000;      // unit conversion : ms/s

    private final ArrayList<Callback> mCallbacks = new ArrayList<Callback>();

    public static GnFakeCallControllerImpl getInstance(Context context) {
        mInstance = new GnFakeCallControllerImpl(context);
        return mInstance;
    }

    public static GnFakeCallControllerImpl getInstance() {
        return mInstance;
    }

    private GnFakeCallControllerImpl(Context context) {
        mContext = context;
        mVirtualPhone = new GnFakeCall(mContext);
    }

    public void dismissControlCenter() {
        for (Callback cb : mCallbacks) {
            cb.collapsePanels();
        }
    }

    public synchronized void cancel() {
    	resetTimer();
        stopTimer();
        notifyChanged(mContext.getString(R.string.gn_sc_fake_call), false, false);
    }

    public synchronized void ring() {
    	//GnFakeCallAlarmReceiver.isCancel = false;
     	if (isTiming()) {
    		return;
    	}
        notifyChanged(mContext.getString(R.string.gn_fc_cancel_phone), false, true);
        starTimer();
    }

    public void handleClick(boolean enable) {
        if (isCallState()) {
            return;
        }

        if (enable) {
            cancel();
        } else {
            ring();
        }
    }

    public boolean isCallState() {
        TelephonyManager telephony = (TelephonyManager) mContext
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (telephony.getCallState() == TelephonyManager.CALL_STATE_RINGING) {
            return true;
        }
        if (telephony.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK) {
            return true;
        }
        return false;
    }

    private void notifyChanged(String time, boolean enable, boolean animating) {
        for (Callback cb : mCallbacks) {
            cb.onStateChange(time, enable, animating);
        }
    }

    @Override
    public void addStateChangeCallback(Callback cb) {
        mCallbacks.add(cb);
    }

    @Override
    public void removeStateChangeCallback(Callback cb) {
        mCallbacks.remove(cb);
    }

    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }
    
	private void starTimer() {
        try {
            if (mTimer == null) {
                mTimer = new Timer();        		
            }

            if (mTimerTask == null) {
                mTimerTask = new TimerTask() {
                    public void run() {
                
                		final long currentTime = SystemClock.elapsedRealtime();
                		final long countTime = currentTime - mStartTime; //add 500ms, for round 
                		if (countTime >= GnConstants.CALL_PHONE_TIME) {
                		    stopTimer();
                		} else {
                		    mLeftTime = GnConstants.CALL_PHONE_TIME - countTime;
                		    notifyChanged(getCurrentTime(), true, true);
                		}
                		Log.d(TAG, "mLeftTime = " + mLeftTime);
                	
                    }
                };
            }
            
            if (mTimer != null && mTimerTask != null) {
    			mStartTime = SystemClock.elapsedRealtime();
    		
                mTimer.scheduleAtFixedRate(mTimerTask, 0, 1000);
                
                AlarmManager alarm = (AlarmManager) mContext
        				.getSystemService(android.content.Context.ALARM_SERVICE);
        		Intent intent = new Intent(mContext, GnFakeCallAlarmReceiver.class);
        		//intent.setAction(GnFakeCallAlarmReceiver.STOP_TIMER_ACTION); 
        		PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, intent, 0);
        		Log.d(TAG, "set alarm");
                alarm.set(AlarmManager.RTC_WAKEUP, (long) (System.currentTimeMillis() + GnConstants.CALL_PHONE_TIME), pi);
             }
            
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
    }
    
    public String getCurrentTime() {
    	int timeValue = (int)((mLeftTime + TIME_MARGIN )/ MSTOS);
        String s = mContext.getResources().getString(R.string.gn_fc_call_tip);
        String timeString = String.format("%02d",timeValue);
        return timeString + s;
    }
    
    synchronized void startCall() {
    	if (!isTiming()){
    		return;
    	}
		resetTimer();
		if (!GnFakeCallActivity.getIsVirtualCallIsRunning()) {
	        Intent intent = mVirtualPhone.createFakeCallIntent();
	        mContext.startActivity(intent);
	    }
		new Handler().postDelayed(new Runnable() {
			public void run() {
				notifyChanged(mContext.getString(R.string.gn_sc_fake_call),
						false, false);
			}
		}, TIME_DELAY); 

	}

	private void resetTimer() {
		mStartTime = 0;
	}
	
	private boolean isTiming(){
		return mStartTime != 0;
	}

}
