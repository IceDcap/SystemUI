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

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.animation.Animation;

import com.android.systemui.R;

public class GnFakeCallControllerImpl implements GnFakeCallController {
    
    private static final String TAG = "GnFakeCallController";

    private Timer mTimer = null;
    private TimerTask mTimerTask = null;
    private long mStartTime;
    private long mLeftTime;

    private static GnFakeCall mVirtualPhone;
    protected Animation animation;
    private Context mContext;
    public AnimationDrawable animationDrawable;
    private static GnFakeCallControllerImpl mInstance = null;

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

    public void cancel() {
        stopTimer();
        notifyChanged(mContext.getString(R.string.gn_sc_fake_call), false, false);
    }

    public void ring() {
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
                        final long countTime = (SystemClock.elapsedRealtime() - mStartTime) / 1000;
                        if (countTime >= GnConstants.CALL_PHONE_TIME) {
                            stopTimer();
                            if (!GnFakeCallActivity.getIsVirtualCallIsRunning()) {
                                Intent intent = mVirtualPhone.createFakeCallIntent();
                                mContext.startActivity(intent);
                            }
                            notifyChanged(mContext.getString(R.string.gn_sc_fake_call), false, false);
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
            }
            
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
    }
    
    public String getCurrentTime() {
        String timeString = "";
        String s = mContext.getResources().getString(R.string.gn_fc_call_tip);
        int length = String.valueOf(mLeftTime).length();
        if (length == 1) {
            timeString = "0" + mLeftTime;
        } else {
            timeString = mLeftTime + "";
        }
        
        return timeString + s;
    }
}
