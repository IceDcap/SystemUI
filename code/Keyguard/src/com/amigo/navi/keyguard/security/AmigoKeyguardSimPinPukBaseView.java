package com.amigo.navi.keyguard.security;

import android.content.Context;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.telephony.SubscriptionManager;

import com.amigo.navi.keyguard.util.VibatorUtil;
import com.android.keyguard.KeyguardPinBasedInputView;
import com.android.keyguard.LiftToActivateListener;
import com.android.keyguard.R;

public abstract class AmigoKeyguardSimPinPukBaseView extends KeyguardPinBasedInputView {


    private ProgressBar mProgressBar;
    private View mOkLayout;

    public AmigoKeyguardSimPinPukBaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AmigoKeyguardSimPinPukBaseView(Context context) {
        this(context, null);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mOkLayout = findViewById(R.id.key_enter_layout);
        mOkButton.setOnClickListener(null);
        mOkButton.setClickable(false);
        mOkButton.setEnabled(false);
        mProgressBar = (ProgressBar) findViewById(R.id.waiting_result_dialog);
        // Gionee <jiangxiao> <2013-10-27> add for CR00932677 begin
        if (mOkLayout != null) {
            mOkLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // doHapticKeyClick();
                    VibatorUtil.amigoVibrate(mContext, VibatorUtil.LOCKSCREEN_UNLOCK_CODE_TAP,
                            VibatorUtil.TOUCH_TAP_VIBRATE_TIME);

                    if (mPasswordEntry.isEnabled()) {
                        verifyPasswordAndUnlock();
                    }
                }
            });
            mOkLayout.setOnHoverListener(new LiftToActivateListener(getContext()));
        }
    }
    
    protected void setProgressBarVisible(boolean isVisible) {
        if (mProgressBar == null || mOkButton == null||mPasswordEntry==null) {
            return;
        }
        if (isVisible) {
            mProgressBar.setVisibility(View.VISIBLE);
            mOkButton.setVisibility(View.GONE);
            mPasswordEntry.setEnabled(false);
        } else {
            mProgressBar.setVisibility(View.GONE);
            mOkButton.setVisibility(View.VISIBLE);
            mPasswordEntry.setEnabled(true);
        }
    }
    
    public String getOptrNameUsingSubId(int subId) {
        int slotId = SubscriptionManager.getSlotId(subId);
        if (slotId == 0) {
            return getContext().getString(R.string.keyguard_sim1_name);
        } else if (slotId == 1) {
            return getContext().getString(R.string.keyguard_sim2_name);
        }
        return getContext().getString(R.string.keyguard_sim_name);
    }
    
    public int getRetryPinCount(int subId) {
        int GET_SIM_RETRY_EMPTY = -1;
        int subIndex = SubscriptionManager.getSlotId(subId);
        if (subIndex == 3) {
            return SystemProperties.getInt("gsm.sim.retry.pin1.4", GET_SIM_RETRY_EMPTY);
        } else if (subIndex == 2) {
            return SystemProperties.getInt("gsm.sim.retry.pin1.3", GET_SIM_RETRY_EMPTY);
        } else if (subIndex == 1) {
            return SystemProperties.getInt("gsm.sim.retry.pin1.2", GET_SIM_RETRY_EMPTY);
        } else {
            return SystemProperties.getInt("gsm.sim.retry.pin1", GET_SIM_RETRY_EMPTY);
        }
    }

    public int getRetryPukCount(int subId) {
        int GET_SIM_RETRY_EMPTY = -1; ///M: The default value of the remaining puk count
        int subIndex =SubscriptionManager.getSlotId(subId);

        if (subIndex == 3) {
            return SystemProperties.getInt("gsm.sim.retry.puk1.4", GET_SIM_RETRY_EMPTY);
        } else if (subIndex == 2) {
            return SystemProperties.getInt("gsm.sim.retry.puk1.3", GET_SIM_RETRY_EMPTY);
        } else if (subIndex == 1) {
            return SystemProperties.getInt("gsm.sim.retry.puk1.2", GET_SIM_RETRY_EMPTY);
        } else {
            return SystemProperties.getInt("gsm.sim.retry.puk1", GET_SIM_RETRY_EMPTY);
        }
    }
    
}
