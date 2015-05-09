/*
 * Copyright (C) 2012 The Android Open Source Project
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

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.KeyguardViewHostManager;
import com.amigo.navi.keyguard.util.VibatorUtil;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.IccCardConstants.State;
import com.android.internal.telephony.PhoneConstants;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.keyguard.EmergencyCarrierArea;
import com.android.keyguard.KeyguardConstants;
import com.android.keyguard.KeyguardPinBasedInputView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.R;

/**
 * Displays a PIN pad for unlocking.
 */
public class AmigoKeyguardSimPinView extends AmigoKeyguardSimPinPukBaseView {
    private static final String LOG_TAG = "KeyguardSimPinView";
    private static final boolean DEBUG = KeyguardConstants.DEBUG_SIM_STATES;
    public static final String TAG = "KeyguardSimPinView";

    private ProgressDialog mSimUnlockProgressDialog = null;
    private CheckSimPin mCheckSimPinThread;

    private AlertDialog mRemainingAttemptsDialog;
    private int mSubId;
    
    KeyguardUpdateMonitorCallback mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
        @Override
        public void onSimStateChanged(int subId, int slotId, State simState) {
           if (DEBUG) Log.v(TAG, "onSimStateChanged(subId=" + subId + ",state=" + simState + ")");
           resetState();
       };
    };

    public AmigoKeyguardSimPinView(Context context) {
        this(context, null);
    }

    public AmigoKeyguardSimPinView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void resetState() {
        super.resetState();
        if (DEBUG) Log.v(TAG, "Resetting state");
        KeyguardUpdateMonitor monitor = KeyguardUpdateMonitor.getInstance(mContext);
        mSubId = monitor.getNextSubIdForState(IccCardConstants.State.PIN_REQUIRED);
        if (SubscriptionManager.isValidSubscriptionId(mSubId)) {
            int count = TelephonyManager.getDefault().getSimCount();
            Resources rez = getResources();
            final String msg;
            int color = Color.WHITE;
//            if (count < 2) {
//                msg = rez.getString(R.string.kg_sim_pin_instructions);
//            } else {
                int strId = R.string.keyguard_password_enter_pin_code_message;
                String simName=getOptrNameUsingSubId(mSubId);
                int degree=getRetryPinCount(mSubId);
                msg = getContext().getString(strId,simName,degree);
//                SubscriptionInfo info = monitor.getSubscriptionInfoForSubId(mSubId);
//                CharSequence displayName = info != null ? info.getDisplayName() : ""; // don't crash
//                msg = rez.getString(R.string.kg_sim_pin_instructions_multi, displayName);
//                if (info != null) {
//                    color = info.getIconTint();
//                }
//            }
            mSecurityMessageDisplay.setMessage(msg, true);
//            mSimImageView.setImageTintList(ColorStateList.valueOf(color));
        }
    }
    

    private String getPinPasswordErrorMessage(int attemptsRemaining) {
        String displayMessage;

//        if (attemptsRemaining == 0) {
//            displayMessage = getContext().getString(R.string.kg_password_wrong_pin_code_pukked);
//        } else if (attemptsRemaining > 0) {
//            displayMessage = getContext().getResources()
//                    .getQuantityString(R.plurals.kg_password_wrong_pin_code, attemptsRemaining,
//                            attemptsRemaining);
//        } else {
//            displayMessage = getContext().getString(R.string.kg_password_pin_failed);
//        }
        if (attemptsRemaining > 0) {
            int strId = R.string.keyguard_password_enter_pin_code_message;
            String simName=getOptrNameUsingSubId(mSubId);
            displayMessage = getContext().getString(strId,simName,attemptsRemaining);
            
//            displayMessage = getContext().getResources()
//                    .getQuantityString(R.plurals.kg_password_wrong_pin_code, attemptsRemaining,
//                            attemptsRemaining);
        } else {
            displayMessage = getContext().getString(R.string.kg_password_pin_failed);
        }
        if (DEBUG) Log.d(LOG_TAG, "getPinPasswordErrorMessage:"
                + " attemptsRemaining=" + attemptsRemaining + " displayMessage=" + displayMessage);
        return displayMessage;
    }

    @Override
    protected boolean shouldLockout(long deadline) {
        // SIM PIN doesn't have a timed lockout
        return false;
    }

    @Override
    protected int getPasswordTextViewId() {
        return R.id.simPinEntry;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        addClickListenerToDeleteButton();
        mSecurityMessageDisplay.setTimeout(0); // don't show ownerinfo/charging status by default
        if (mEcaView instanceof EmergencyCarrierArea) {
            ((EmergencyCarrierArea) mEcaView).setCarrierTextVisible(true);
        }
        setPasswordEntry();
        setIgnoreButton();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        KeyguardUpdateMonitor.getInstance(mContext).registerCallback(mUpdateMonitorCallback);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        KeyguardUpdateMonitor.getInstance(mContext).removeCallback(mUpdateMonitorCallback);
    }

    @Override
    public void showUsabilityHint() {
    }

    @Override
    public void onPause(int reason) {
        // dismiss the dialog.
//        if (mSimUnlockProgressDialog != null) {
//            mSimUnlockProgressDialog.dismiss();
//            mSimUnlockProgressDialog = null;
//        }
        setProgressBarVisible(false);
    }

    /**
     * Since the IPC can block, we want to run the request in a separate thread
     * with a callback.
     */
    private abstract class CheckSimPin extends Thread {
        private final String mPin;
        private int mSubId;

        protected CheckSimPin(String pin, int subId) {
            mPin = pin;
            mSubId = subId;
        }

        abstract void onSimCheckResponse(final int result, final int attemptsRemaining);

        @Override
        public void run() {
            try {
                if (DEBUG) {
                    Log.v(TAG, "call supplyPinReportResultForSubscriber(subid=" + mSubId + ")");
                }
                final int[] result = ITelephony.Stub.asInterface(ServiceManager
                        .checkService("phone")).supplyPinReportResultForSubscriber(mSubId, mPin);
                if (DEBUG) {
                    Log.v(TAG, "supplyPinReportResult returned: " + result[0] + " " + result[1]);
                }
                post(new Runnable() {
                    public void run() {
                        onSimCheckResponse(result[0], result[1]);
                    }
                });
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException for supplyPinReportResult:", e);
                post(new Runnable() {
                    public void run() {
                        onSimCheckResponse(PhoneConstants.PIN_GENERAL_FAILURE, -1);
                    }
                });
            }
        }
    }
    
    private void setPasswordEntry() {
//      mPasswordEntry.setKeyListener(DigitsKeyListener.getInstance());
//      mPasswordEntry.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
      mPasswordEntry.requestFocus();
      mPasswordEntry.addTextChangedListener(new TextWatcher() {
          @Override
          public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
              // TODO Auto-generated method stub
              Log.d(LOG_TAG, "onTextChanged");
              
          }
          
          @Override
          public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                  int arg3) {
              // TODO Auto-generated method stub
              Log.d(LOG_TAG, "beforeTextChanged");
          }
          
          @Override
          public void afterTextChanged(Editable arg0) {
              Log.d(LOG_TAG, "afterTextChanged");
              final int pwdLength = mPasswordEntry.getText().length();
              
              resetPinDelete(pwdLength);

          }
      });
  }
    
    private void resetPinDelete(int num){
        if(num > 0){
            ((TextView)pinDelete).setText(getContext().getResources().getString(R.string.keyguard_simple_number_delete));
        }else{
            ((TextView)pinDelete).setText(getContext().getResources().getString(R.string.keyguard_simple_number_cancel));
        }
    }


    
    private Dialog getSimUnlockProgressDialog() {
        if (mSimUnlockProgressDialog == null) {
            mSimUnlockProgressDialog = new ProgressDialog(mContext);
            mSimUnlockProgressDialog.setMessage(
                    mContext.getString(R.string.kg_sim_unlock_progress_dialog_message));
            mSimUnlockProgressDialog.setIndeterminate(true);
            mSimUnlockProgressDialog.setCancelable(false);
            mSimUnlockProgressDialog.getWindow().setType(
                    WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        }
        return mSimUnlockProgressDialog;
    }

    private Dialog getSimRemainingAttemptsDialog(int remaining) {
        String msg = getPinPasswordErrorMessage(remaining);
        if (mRemainingAttemptsDialog == null) {
            Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage(msg);
            builder.setCancelable(false);
            builder.setNeutralButton(R.string.ok, null);
            mRemainingAttemptsDialog = builder.create();
            mRemainingAttemptsDialog.getWindow().setType(
                    WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        } else {
            mRemainingAttemptsDialog.setMessage(msg);
        }
        return mRemainingAttemptsDialog;
    }

    @Override
    protected void verifyPasswordAndUnlock() {
        String entry = mPasswordEntry.getText();

        if (entry.length() < 4) {
            // otherwise, display a message to the user, and don't submit.
            mSecurityMessageDisplay.setMessage(R.string.kg_invalid_sim_pin_hint, true);
            resetPasswordText(true);
            mCallback.userActivity();
            
            VibatorUtil.amigoVibrate(mContext, VibatorUtil.LOCKSCREEN_UNLOCK_CODE_ERROR, VibatorUtil.UNLOCK_ERROR_VIBRATE_TIME);
            
            return;
        }
        
        setProgressBarVisible(true);
//        getSimUnlockProgressDialog().show();

        if (mCheckSimPinThread == null) {
            mCheckSimPinThread = new CheckSimPin(mPasswordEntry.getText(), mSubId) {
                void onSimCheckResponse(final int result, final int attemptsRemaining) {
                    post(new Runnable() {
                        public void run() {
//                            if (mSimUnlockProgressDialog != null) {
//                                mSimUnlockProgressDialog.hide();
//                            }
                            setProgressBarVisible(false);
                            resetPasswordText(true /* animate */);
                            if (result == PhoneConstants.PIN_RESULT_SUCCESS) {
                                KeyguardUpdateMonitor.getInstance(getContext())
                                        .reportSimUnlocked(mSubId);
                                mCallback.dismiss(true);
                            } else {
                                if (result == PhoneConstants.PIN_PASSWORD_INCORRECT) {
                                    mSecurityMessageDisplay.setMessage(
                                            getPinPasswordErrorMessage(attemptsRemaining), true);
//                                    if (attemptsRemaining <= 2) {
//                                        // this is getting critical - show dialog
////                                        getSimRemainingAttemptsDialog(attemptsRemaining).show();
//                                    } else {
//                                        // show message
//                                        mSecurityMessageDisplay.setMessage(
//                                                getPinPasswordErrorMessage(attemptsRemaining), true);
//                                    }
                                } else {
                                    // "PIN operation failed!" - no idea what this was and no way to
                                    // find out. :/
//                                    mSecurityMessageDisplay.setMessage(getContext().getString(
//                                            R.string.kg_password_pin_failed), true);
                                }
                                if (DEBUG) Log.d(LOG_TAG, "verifyPasswordAndUnlock "
                                        + " CheckSimPin.onSimCheckResponse: " + result
                                        + " attemptsRemaining=" + attemptsRemaining);
                                
                                VibatorUtil.amigoVibrate(mContext, VibatorUtil.LOCKSCREEN_UNLOCK_CODE_ERROR, VibatorUtil.UNLOCK_ERROR_VIBRATE_TIME);
                                
                            }
                            mCallback.userActivity();
                            mCheckSimPinThread = null;
                        }
                    });
                }
            };
            mCheckSimPinThread.start();
        }
    }

    @Override
    public void startAppearAnimation() {
    	if(getVisibility() == INVISIBLE){
			setVisibility(VISIBLE);
    	}
    }

    @Override
    public boolean startDisappearAnimation(Runnable finishRunnable) {
        return false;
    }
    
    View pinDelete = null;
    private void addClickListenerToDeleteButton() {
        // The delete button is of the PIN keyboard itself in some (e.g. tablet)
        // layouts, not a separate view
        pinDelete = findViewById(R.id.back_button);
        if (pinDelete != null) {
            pinDelete.setVisibility(View.VISIBLE);
            
            pinDelete.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    // check for time-based lockouts
                    CharSequence str = mPasswordEntry.getText();
                  /*  if (mPasswordEntry.isEnabled()) {
                        if (str.length() > 0) {
                            if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "delete one digit");
                            mPasswordEntry.setText(str.subSequence(0, str.length() - 1).toString());
                        }
                    }*/
                    
                    
                    if (mPasswordEntry.isEnabled()) {
                      mPasswordEntry.deleteLastChar();
                  }

                    if(str.length() <= 0){
                        //go to keyguadhome
                        KeyguardViewHostManager.getInstance().scrollToKeyguardPageByAnimation();
                    }
//                    doHapticKeyClick();
                    VibatorUtil.amigoVibrate(mContext, VibatorUtil.LOCKSCREEN_UNLOCK_CODE_TAP, VibatorUtil.TOUCH_TAP_VIBRATE_TIME);
                }
            });
            
            pinDelete.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    // check for time-based lockouts
                    if (mPasswordEntry.isEnabled()) {
                        if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "delete all digits");
//                        mPasswordEntry.setText("");
                        resetPasswordText(true /* animate */);
                    }
//                    doHapticKeyClick();
                    VibatorUtil.amigoVibrate(mContext, VibatorUtil.LOCKSCREEN_UNLOCK_CODE_TAP, VibatorUtil.TOUCH_TAP_VIBRATE_TIME);
                    return true;
                }
            });
        }
    }
    
	private void setIgnoreButton() {
        Button ignoreButton = (Button) this.findViewById(R.id.key_ignore);
        if(ignoreButton == null) return;
        
    	ignoreButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "ignore button clicked  mForWhichSim: "+mSubId);
				// Gionee <jiangxiao> <2014-06-12> modify for CR01288136 begin
				// Gionee <jingyn> <2014-08-19> add for CR01358258 begin
				if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "ignoreButton  simId:"+mSubId+"---");
				mKeyguardUpdateMonitor.dismissSimLockState(mSubId);
				if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "ignoreButton after dismissSimLockState simId:"+mSubId+"---");
				mCallback.dismiss(true);
//				mSecurityManager.ignoreSimSecurityView(mSubId);
				// Gionee <jingyn> <2014-08-19> add for CR01358258 end
		        // Gionee <jiangxiao> <2014-06-12> modify for CR01288136 end

			/*	//　just used for mtk
				MtkSimUnlockExt.sendSimLockVerifyResult(getContext(), 
						MtkSimUnlockExt.VERIFY_TYPE_PIN, false);*/
				VibatorUtil.amigoVibrate(mContext, VibatorUtil.LOCKSCREEN_UNLOCK_CODE_TAP, VibatorUtil.TOUCH_TAP_VIBRATE_TIME);
			}
		});
	}
}

