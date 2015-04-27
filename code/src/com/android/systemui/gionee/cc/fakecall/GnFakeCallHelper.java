package com.android.systemui.gionee.cc.fakecall;
/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.gionee.cc.GnControlCenterShortcut;

public class GnFakeCallHelper {
    private static final String LOG_TAG = "VirtualCallHelper";
    private static final int INVALID_RES_ID = -1;
    public static final int MSG_CANCLE_PHONE = 6;
    public static final int MSG_SHOW_LISTVIEW = 8;
    
    private GnControlCenterShortcut mShortCut;

    private static volatile boolean isTimerRunning = false;
    private GnTimerListener mTimerListener;
    private static GnFakeCall mVirtualPhone;
    private static long mCurrentTime = 15;
    protected Animation animation;

    private FakeCallHandler mHandler = null;

    private Context mContext;
    private View mContainer;
    public AnimationDrawable animationDrawable;

    private TextView mVirtualCallTextView = null;
    private ImageView mVirtualCallImageView = null;
    private ImageView mVirtualCallImageBg = null;
    private static GnFakeCallHelper mInstance = null;

    public static GnFakeCallHelper getInstance(Context context, View view) {
        mInstance = new GnFakeCallHelper(context, view);
        return mInstance;
    }
    
    public static GnFakeCallHelper getInstance() {
        return mInstance;
    }

    private GnFakeCallHelper(Context context, View callContainer) {
        onCreate(context, callContainer);
    }

    public void setIsTimerRunning(boolean isRunning) {
        isTimerRunning = isRunning;
        setClickView(R.string.gn_fc_cancel_phone, R.drawable.gn_ic_sc_fake_call_cancel);
    }

    private GnUpdateUiStateCallback mUpdateUiStateCallback = new GnUpdateUiStateCallback() {

        @Override
        public void updateUiState() {
            Log.d(LOG_TAG, "updateUiState  isTimerRunning: " + isTimerRunning);
            if (isTimerRunning) {
                Log.d(LOG_TAG, "updateUiState  updateUi 11");
                startCancelAnimation();
                createTimeListener();
            } else {
                Log.d(LOG_TAG, "updateUiState  updateUi 22");
                stopCancelAnimation();
                mTimerListener.cancel();
            }
        }
    };

    public void onCreate(Context context, View callContainer) {
        mContext = context;
        mContainer = callContainer;
        mVirtualCallTextView = (TextView) mContainer.findViewById(R.id.sc_fake_call_txt);
        mVirtualCallImageView = (ImageView) mContainer.findViewById(R.id.sc_fake_call);
        mVirtualCallImageBg = (ImageView) mContainer.findViewById(R.id.sc_fake_call_background);
        setClickView(R.string.gn_sc_fake_call, R.drawable.gn_ic_sc_fake_call);

        mHandler = new FakeCallHandler();
        if(mVirtualPhone==null){
        	synchronized(GnFakeCall.class) {
        		if(mVirtualPhone==null){
                    mVirtualPhone = new GnFakeCall(mContext);
        		}
        	}
        }
        mVirtualPhone.registerUiStateCallback(mUpdateUiStateCallback);

        createTimeListener();
    }

    //TODO:查看是否需要多出实例化
	private void createTimeListener() {
        long millisUntilFinished = GnTimerListener.getMillisUntilFinished();
        long millisInFuture = (millisUntilFinished == 0 ? GnConstants.CALL_PHONE_TIME : millisUntilFinished);
        
        mTimerListener = new GnTimerListener(millisInFuture, 1000) {
            @Override
            public void onTimerRunning(long millisUntilFinished) {
                isTimerRunning = true;
                mCurrentTime = millisUntilFinished / 1000;

                String s = mContext.getResources().getString(R.string.gn_fc_call_tip);
                int length = String.valueOf(mCurrentTime).length();
                String timeString = "";

                if (length == 1) {
                    timeString = "0" + mCurrentTime;
                } else {
                    timeString = mCurrentTime + "";
                }

                mVirtualCallTextView.setText(Html.fromHtml(" <font color=\"#ff9000\">" + timeString + "</font>" + s));
            }

            @Override
            public void onTimerFinsh() {
                isTimerRunning = false;
                setClickView(R.string.gn_sc_fake_call, R.drawable.gn_ic_sc_fake_call);
                stopCancelAnimation();
            }
        };
        if(isTimerRunning){
            mTimerListener.start();
            startCancelAnimation();
        }
    }

    private void setClickView(int stringId, int drawableId) {
        if (stringId != INVALID_RES_ID) {
            mVirtualCallTextView.setText(stringId);
        }
        if (drawableId != INVALID_RES_ID) {
            mVirtualCallImageView.setImageResource(drawableId);
        }
    }

    public final void startIconAnimator(int animRes) {
        mVirtualCallImageBg.setVisibility(View.VISIBLE);
        mVirtualCallImageBg.startAnimation(AnimationUtils.loadAnimation(mContext, animRes));
//        mVirtualCallImageView.setBackgroundResource(animRes);
//        animationDrawable = (AnimationDrawable) mVirtualCallImageView.getBackground();
//        animationDrawable.start();
    }

    protected void startCancelAnimation() {
        startIconAnimator(R.anim.gn_anim_sc_fake_call);
    }

    protected void stopCancelAnimation() {
        stopIconAnimator();
    }

    public final void stopIconAnimator() {
        mVirtualCallImageBg.clearAnimation();
        mVirtualCallImageBg.setVisibility(View.GONE);
//        try {
//            if (animationDrawable != null) {
//                animationDrawable.stop();
//                mVirtualCallImageView.setBackgroundResource(R.drawable.gn_sc_tile_background);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    protected void ringPhone() {
        mVirtualPhone.ringPhone();
    }

    public synchronized void operator() {
        operator(isTimerRunning);
    }

    private synchronized void operator(boolean flag) {
        if (flag) {
            cancel();
        } else {
//            closePrePhone();
            ring();
        }
    }
    
    public void dismissControlCenter() {
        mShortCut.dismissControlCenter();
    }

    private void cancel() {
        mTimerListener.cancel();
        //updateUiState() need this field's value immediately
        isTimerRunning=false;
        stopCancelAnimation();
        setClickView(R.string.gn_sc_fake_call, R.drawable.gn_ic_sc_fake_call_cancel);
        mVirtualPhone.canclePhone();
    }

    private void ring() {
        Log.d(LOG_TAG, "ring-----");
//        createTimeLisener();
//        mTimerListener.start();
        setClickView(R.string.gn_fc_cancel_phone, R.drawable.gn_ic_sc_fake_call_cancel);
        isTimerRunning=true;
        startCancelAnimation();
        ringPhone();
    }

    public void onNextClick() {
        if (isCallState()) {
            return;
        }
        operator();
    }

    public void onPhoneStateChanged(String state) {
        if (isTimerRunning) {
            if (!TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
                mHandler.sendMessage(Message.obtain(null, MSG_CANCLE_PHONE));
            }
        }
    }
    

    public boolean isCallState() {
        TelephonyManager telephony = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephony.getCallState() == TelephonyManager.CALL_STATE_RINGING) {
            return true;
        }
        if (telephony.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK) {
            return true;
        }
        return false;
    }

    public void onDestroy() {

    }

    class FakeCallHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_SHOW_LISTVIEW:

                break;
            case MSG_CANCLE_PHONE:
                mHandler.removeMessages(MSG_SHOW_LISTVIEW);
                if (isTimerRunning) {
                    cancel();
                }
                break;

            default:
                break;
            }
            super.handleMessage(msg);
        }
    }

    public void setShortCut(GnControlCenterShortcut shortcut) {
        mShortCut = shortcut;
    }

    public void updateResources() {
        if (isTimerRunning) {
            return;
        }
        
        mVirtualCallTextView.setText(R.string.gn_sc_fake_call);
    }
}
