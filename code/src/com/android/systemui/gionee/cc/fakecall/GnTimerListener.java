/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
package com.android.systemui.gionee.cc.fakecall;

import android.util.Log;

public abstract class GnTimerListener extends GnFakeCallCountDownTimer{

    private static final String LOG_TAG="TimerListener";
    
    /**
     * just sync virtual call button view's content;neither exact nor reliable value
     */
    private static long sMillisUntilFinished = 0;
    public GnTimerListener(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
    }

    @Override
    public final void onFinish() {
        sMillisUntilFinished = 0;
        onTimerFinsh();
    }

    @Override
    public final void onTick(long millisUntilFinished) {
        Log.d(LOG_TAG, "TimerListener onTick:" + millisUntilFinished);
        sMillisUntilFinished = millisUntilFinished;
        onTimerRunning(millisUntilFinished);
    }
    

    public abstract void onTimerFinsh();

    public abstract void onTimerRunning(long millisUntilFinished);
    
    //should use onCancel() rather than cancel()
    @Override
    public void cancel() {
        super.cancel();
        onFinish();
        sMillisUntilFinished = 0;
    }

    public static long getMillisUntilFinished(){
        return sMillisUntilFinished;
    }
    
}