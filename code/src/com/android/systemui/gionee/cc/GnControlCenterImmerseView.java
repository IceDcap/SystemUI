package com.android.systemui.gionee.cc;
/*
*
* MODULE DESCRIPTION
*   GnControlCenter
* add by huangwt for Android L at 20150413.
* 
*/

import com.android.systemui.R;
import com.android.systemui.gionee.GnUtil;
import com.android.systemui.gionee.cc.GnControlCenter.Callback;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class GnControlCenterImmerseView extends FrameLayout {

    private static final String TAG = "GnControlCenterImmerseView";
    
    private Context mContext;
    private ImageView mImmerseArrow;
    
    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Log.d(TAG, "handler msg push down out");
                    pushDownOut();
                    break;
                    
                default:
                    break;
            }
        }
    };
    
    private Callback mCallback = new Callback() {

        @Override
        public void dismissPanel() {
            setVisibility(View.GONE);
            GnControlCenter.go(GnControlCenter.STATE_CLOSED);
        }
        
    };
    
    public GnControlCenterImmerseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        
        GnControlCenter.addCallback(mCallback);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        
        mImmerseArrow = (ImageView) findViewById(R.id.immerse_arrow);
        mImmerseArrow.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                pushDownOut();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (GnControlCenter.getState() != GnControlCenter.STATE_IMMERSE_CLOSING 
                && event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            pushDownOut();
        }
        return super.onTouchEvent(event);
    }

    public void pushUpIn() {
        mImmerseArrow.startAnimation(loadAnim(R.anim.gn_push_up_in, ImmerseArrowMoveInListener));
    }
    
    public void pushDownOut() {
        mImmerseArrow.startAnimation(loadAnim(R.anim.gn_push_down_out, ImmerseArrowMoveOutListener));
    }
    
    private Animation.AnimationListener ImmerseArrowMoveInListener = new Animation.AnimationListener() {

        @Override
        public void onAnimationEnd(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            
        }

        @Override
        public void onAnimationStart(Animation animation) {
            Log.d(TAG, "anim start send delay msg");
            mHandler.removeMessages(1);
            mHandler.sendEmptyMessageDelayed(1, 2000);
        }
        
    };
    
    private Animation.AnimationListener ImmerseArrowMoveOutListener = new Animation.AnimationListener() {

        @Override
        public void onAnimationEnd(Animation animation) {
            setVisibility(View.GONE);
            GnControlCenter.go(GnControlCenter.STATE_CLOSED);
            GnUtil.setLockState(GnUtil.STATE_LOCK_UNLOCK);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            
        }

        @Override
        public void onAnimationStart(Animation animation) {
            GnControlCenter.go(GnControlCenter.STATE_IMMERSE_CLOSING);
        }
    };

    private Animation loadAnim(int id, Animation.AnimationListener listener) {
        Animation anim = AnimationUtils.loadAnimation(mContext, id);
        if (listener != null) {
            anim.setAnimationListener(listener);
        }
        return anim;
    }

}
