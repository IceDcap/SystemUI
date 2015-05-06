/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.amigo.navi.keyguard.security;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.telecom.TelecomManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amigo.navi.keyguard.util.VibatorUtil;
import com.android.internal.R;
import com.android.internal.telephony.IccCardConstants.State;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;

/**
 * This class implements a smart emergency button that updates itself based
 * on telephony state.  When the phone is idle, it is an emergency call button.
 * When there's a call in progress, it presents an appropriate message and
 * allows the user to return to the call.
 */
public class AmigoEmergencyButton extends TextView {
    private static final String ACTION_EMERGENCY_DIAL = "com.android.phone.EmergencyDialer.DIAL";

    KeyguardUpdateMonitorCallback mInfoCallback = new KeyguardUpdateMonitorCallback() {

        @Override
        public void onSimStateChanged(int subId, int slotId, State simState) {
            updateEmergencyCallButton();
        }

        @Override
        public void onPhoneStateChanged(int phoneState) {
            updateEmergencyCallButton();
        }
    };
    private LockPatternUtils mLockPatternUtils;
    private PowerManager mPowerManager;

    public AmigoEmergencyButton(Context context) {
        this(context, null);
    }

    public AmigoEmergencyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        KeyguardUpdateMonitor.getInstance(mContext).registerCallback(mInfoCallback);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        KeyguardUpdateMonitor.getInstance(mContext).removeCallback(mInfoCallback);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mLockPatternUtils = new LockPatternUtils(mContext);
        mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                takeEmergencyCallAction();
                
                VibatorUtil.amigoVibrate(mContext, VibatorUtil.LOCKSCREEN_UNLOCK_CODE_TAP, VibatorUtil.TOUCH_TAP_VIBRATE_TIME);
                
            }
        });
        updateEmergencyCallButton();
    }

    /**
     * Shows the emergency dialer or returns the user to the existing call.
     */
    public void takeEmergencyCallAction() {
        // TODO: implement a shorter timeout once new PowerManager API is ready.
        // should be the equivalent to the old userActivity(EMERGENCY_CALL_TIMEOUT)
        mPowerManager.userActivity(SystemClock.uptimeMillis(), true);
        if (mLockPatternUtils.isInCall()) {
            mLockPatternUtils.resumeCall();
        } else {
            final boolean bypassHandler = true;
            KeyguardUpdateMonitor.getInstance(mContext).reportEmergencyCallAction(bypassHandler);
            Intent intent = new Intent(ACTION_EMERGENCY_DIAL);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            getContext().startActivityAsUser(intent,
                    new UserHandle(mLockPatternUtils.getCurrentUser()));
        }
    }

    private void updateEmergencyCallButton() {
        boolean enabled = false;
        if (mLockPatternUtils.isInCall()) {
            enabled = true; // always show "return to call" if phone is off-hook
        } else if (mLockPatternUtils.isEmergencyCallCapable()) {
            final boolean simLocked = KeyguardUpdateMonitor.getInstance(mContext).isSimPinVoiceSecure();
            if (simLocked) {
                // Some countries can't handle emergency calls while SIM is locked.
                enabled = mLockPatternUtils.isEmergencyCallEnabledWhileSimLocked();
            } else {
                // True if we need to show a secure screen (pin/pattern/SIM pin/SIM puk);
                // hides emergency button on "Slide" screen if device is not secure.
                enabled = mLockPatternUtils.isSecure();
            }
        }
//        mLockPatternUtils.updateEmergencyCallButtonState(this, enabled, false);
          updateEmergencyCallButtonState(this, enabled, false);
    }
    
    
    /**
     * Sets the emergency button visibility based on isEmergencyCallCapable().
     *
     * If the emergency button is visible, sets the text on the emergency button
     * to indicate what action will be taken.
     *
     * If there's currently a call in progress, the button will take them to the call
     * @param text The button to update
     * @param shown Indicates whether the given screen wants the emergency button to show at all
     * @param showIcon Indicates whether to show a phone icon for the button.
     */
    public void updateEmergencyCallButtonState(TextView text, boolean shown, boolean showIcon) {
        if (isEmergencyCallCapable() && shown) {
            text.setVisibility(View.VISIBLE);
        } else {
            text.setVisibility(View.GONE);
            return;
        }

        int textId;
        if (isInCall()) {
            // show "return to call" text and show phone icon
            textId = R.string.lockscreen_return_to_call;
            int phoneCallIcon = showIcon ? R.drawable.stat_sys_phone_call : 0;
            text.setCompoundDrawablesWithIntrinsicBounds(phoneCallIcon, 0, 0, 0);
        } else {
            textId = R.string.lockscreen_emergency_call;
            int emergencyIcon = showIcon ? R.drawable.ic_emergency : 0;
            text.setCompoundDrawablesWithIntrinsicBounds(emergencyIcon, 0, 0, 0);
        }
        text.setText(textId);
    }
    
    public boolean isEmergencyCallCapable() {
        return mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_voice_capable);
    }
    
    /**
     * @return {@code true} if there is a call currently in progress, {@code false} otherwise.
     */
    public boolean isInCall() {
        return getTelecommManager().isInCall();
    }

    private TelecomManager getTelecommManager() {
        return (TelecomManager) mContext.getSystemService(Context.TELECOM_SERVICE);
    }

}
