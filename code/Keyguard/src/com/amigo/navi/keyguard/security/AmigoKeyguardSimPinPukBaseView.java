package com.amigo.navi.keyguard.security;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;

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
}
