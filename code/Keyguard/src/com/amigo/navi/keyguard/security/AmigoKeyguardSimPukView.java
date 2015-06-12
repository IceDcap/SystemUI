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

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.KeyguardViewHostManager;
import com.amigo.navi.keyguard.util.VibatorUtil;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.IccCardConstants.State;
import com.android.keyguard.KeyguardConstants;
import com.android.keyguard.KeyguardPinBasedInputView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.R;
import com.android.keyguard.EmergencyCarrierArea;


/**
 * Displays a PIN pad for entering a PUK (Pin Unlock Kode) provided by a carrier.
 */
public class AmigoKeyguardSimPukView extends AmigoKeyguardSimPinPukBaseView {
    private static final String LOG_TAG = "KeyguardSimPukView";
    private static final boolean DEBUG = KeyguardConstants.DEBUG;
    public static final String TAG = "KeyguardSimPukView";

    private ProgressDialog mSimUnlockProgressDialog = null;
    private CheckSimPuk mCheckSimPukThread;
    private String mPukText;
    private String mPinText;
    private StateMachine mStateMachine = new StateMachine();
    private AlertDialog mRemainingAttemptsDialog;
    private int mSubId;
    private LinearLayout  mKeyguardBouncerFrame;

    KeyguardUpdateMonitorCallback mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
        @Override
        public void onSimStateChanged(int subId, int slotId, State simState) {
           if (DEBUG) Log.v(TAG, "onSimStateChanged(subId=" + subId + ",state=" + simState + ")");
           resetState();
       };
    };

    public AmigoKeyguardSimPukView(Context context) {
        this(context, null);
    }

    public AmigoKeyguardSimPukView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private class StateMachine {
        final int ENTER_PUK = 0;
        final int ENTER_PIN = 1;
        final int CONFIRM_PIN = 2;
        final int DONE = 3;
        private int state = ENTER_PUK;

        public void next() {
            int msg = 0;
            if (state == ENTER_PUK) {
                if (checkPuk()) {
                    state = ENTER_PIN;
                    msg = R.string.kg_puk_enter_pin_hint;
                } else {
                    msg = R.string.kg_invalid_sim_puk_hint;
                    
                	VibatorUtil.amigoVibrate(mContext, VibatorUtil.LOCKSCREEN_UNLOCK_CODE_ERROR, VibatorUtil.UNLOCK_ERROR_VIBRATE_TIME);
                }
            } else if (state == ENTER_PIN) {
                if (checkPin()) {
                    state = CONFIRM_PIN;
                    msg = R.string.kg_enter_confirm_pin_hint;
                } else {
                    msg = R.string.kg_invalid_sim_pin_hint;
                    
                	VibatorUtil.amigoVibrate(mContext, VibatorUtil.LOCKSCREEN_UNLOCK_CODE_ERROR, VibatorUtil.UNLOCK_ERROR_VIBRATE_TIME);
                }
            } else if (state == CONFIRM_PIN) {
                if (confirmPin()) {
                    state = DONE;
                    msg = R.string.keyguard_sim_unlock_progress_dialog_message;
                    updateSim();
                } else {
                    state = ENTER_PIN; // try again?
                    msg = R.string.kg_invalid_confirm_pin_hint;
                    
                	VibatorUtil.amigoVibrate(mContext, VibatorUtil.LOCKSCREEN_UNLOCK_CODE_ERROR, VibatorUtil.UNLOCK_ERROR_VIBRATE_TIME);
                }
            }
            resetPasswordText(true);
            if (msg != 0) {
                mSecurityMessageDisplay.setMessage(msg, true);
            }
        }

        void reset() {
            mPinText="";
            mPukText="";
            state = ENTER_PUK;
            KeyguardUpdateMonitor monitor = KeyguardUpdateMonitor.getInstance(mContext);
            mSubId = monitor.getNextSubIdForState(IccCardConstants.State.PUK_REQUIRED);
            if (SubscriptionManager.isValidSubscriptionId(mSubId)) {
                int count = TelephonyManager.getDefault().getSimCount();
                Resources rez = getResources();
                String msg;
                int color = Color.WHITE;
                String simName=getOptrNameUsingSubId(mSubId);
                int degree=getRetryPukCount(mSubId);
                if (DEBUG) Log.v(TAG, "Resetting state..degree="+degree);
               
                if (degree > 0) {
                	int strId = R.string.keyguard_password_enter_puk_code_message;
                	 msg = getContext().getString(strId,simName,degree);
                } else {
                	int strId = R.string.keyguard_password_enter_puk_code_message_minus;
                	msg = getContext().getString(strId,simName);
                }
//                if (count < 2) {
//                    msg = rez.getString(R.string.kg_puk_enter_puk_hint);
//                } else {
//                    SubscriptionInfo info = monitor.getSubscriptionInfoForSubId(mSubId);
//                    CharSequence displayName = info != null ? info.getDisplayName() : "";
//                    msg = rez.getString(R.string.kg_puk_enter_puk_hint_multi, displayName);
//                    if (info != null) {
//                        color = info.getIconTint();
//                    }
//                }
                mSecurityMessageDisplay.setMessage(msg, true);
            }
            mPasswordEntry.requestFocus();
        }
    }

    private String getPukPasswordErrorMessage(int attemptsRemaining) {
        String displayMessage;

//        if (attemptsRemaining == 0) {
//            displayMessage = getContext().getString(R.string.kg_password_wrong_puk_code_dead);
//        } else if (attemptsRemaining > 0) {
//            displayMessage = getContext().getResources()
//                    .getQuantityString(R.plurals.kg_password_wrong_puk_code, attemptsRemaining,
//                            attemptsRemaining);
//        } else {
//            displayMessage = getContext().getString(R.string.kg_password_puk_failed);
//        }
        if (attemptsRemaining == 0) {
            displayMessage = getContext().getString(R.string.kg_password_wrong_puk_code_dead);
        } else if (attemptsRemaining > 0) {
            int strId = R.string.keyguard_password_enter_puk_code_message;
            String simName=getOptrNameUsingSubId(mSubId);
            int degree=getRetryPukCount(mSubId);
            displayMessage = getContext().getString(strId,simName,degree);
        } else {
            displayMessage = getContext().getString(R.string.kg_password_puk_failed);
        }
        if (DEBUG) Log.d(LOG_TAG, "getPukPasswordErrorMessage:"
                + " attemptsRemaining=" + attemptsRemaining + " displayMessage=" + displayMessage);
        return displayMessage;
    }

    public void resetState() {
    	 if (DEBUG) Log.d(LOG_TAG, "resetState");
        super.resetState();
        resetPasswordText(true /* animate */);
        mStateMachine.reset();
    }

    @Override
    protected boolean shouldLockout(long deadline) {
        // SIM PUK doesn't have a timed lockout
        return false;
    }

    @Override
    protected int getPasswordTextViewId() {
        return R.id.simPukEntry;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mSecurityMessageDisplay.setTimeout(0); // don't show ownerinfo/charging status by default
        if (mEcaView instanceof EmergencyCarrierArea) {
            ((EmergencyCarrierArea) mEcaView).setCarrierTextVisible(true);
        }
        addClickListenerToDeleteButton();
        setPasswordEntry();
        setIgnoreButton();
        mKeyguardBouncerFrame=(LinearLayout)findViewById(R.id.keyguard_bouncer_frame);
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
    private abstract class CheckSimPuk extends Thread {

        private final String mPin, mPuk;
        private final int mSubId;

        protected CheckSimPuk(String puk, String pin, int subId) {
            mPuk = puk;
            mPin = pin;
            mSubId = subId;
        }

        abstract void onSimLockChangedResponse(final int result, final int attemptsRemaining);

        @Override
        public void run() {
            try {
                if (DEBUG) Log.v(TAG, "call supplyPukReportResult()");
                final int[] result = ITelephony.Stub.asInterface(ServiceManager
                    .checkService("phone")).supplyPukReportResultForSubscriber(mSubId, mPuk, mPin);
                if (DEBUG) {
                    Log.v(TAG, "supplyPukReportResult returned: " + result[0] + " " + result[1]);
                }
                post(new Runnable() {
                    public void run() {
                        onSimLockChangedResponse(result[0], result[1]);
                    }
                });
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException for supplyPukReportResult:", e);
                post(new Runnable() {
                    public void run() {
                        onSimLockChangedResponse(PhoneConstants.PIN_GENERAL_FAILURE, -1);
                    }
                });
            }
        }
    }

    private Dialog getSimUnlockProgressDialog() {
        if (mSimUnlockProgressDialog == null) {
            mSimUnlockProgressDialog = new ProgressDialog(mContext);
            mSimUnlockProgressDialog.setMessage(
                    mContext.getString(R.string.kg_sim_unlock_progress_dialog_message));
            mSimUnlockProgressDialog.setIndeterminate(true);
            mSimUnlockProgressDialog.setCancelable(false);
            if (!(mContext instanceof Activity)) {
                mSimUnlockProgressDialog.getWindow().setType(
                        WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
            }
        }
        return mSimUnlockProgressDialog;
    }

    private Dialog getPukRemainingAttemptsDialog(int remaining) {
        String msg = getPukPasswordErrorMessage(remaining);
        if (mRemainingAttemptsDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
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

    private boolean checkPuk() {
        // make sure the puk is at least 8 digits long.
        if (mPasswordEntry.getText().length() == 8) {
            mPukText = mPasswordEntry.getText();
            return true;
        }
        return false;
    }

    private boolean checkPin() {
        // make sure the PIN is between 4 and 8 digits
        int length = mPasswordEntry.getText().length();
        if (length >= 4 && length <= 8) {
            mPinText = mPasswordEntry.getText();
            return true;
        }
        return false;
    }

    public boolean confirmPin() {
        return mPinText.equals(mPasswordEntry.getText());
    }

    private void updateSim() {
//        getSimUnlockProgressDialog().show();
        setProgressBarVisible(true);
        if (mCheckSimPukThread == null) {
            mCheckSimPukThread = new CheckSimPuk(mPukText, mPinText, mSubId) {
                void onSimLockChangedResponse(final int result, final int attemptsRemaining) {
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
                                            getPukPasswordErrorMessage(attemptsRemaining), true);
//                                    if (attemptsRemaining <= 2) {
//                                        // this is getting critical - show dialog
//                                        getPukRemainingAttemptsDialog(attemptsRemaining).show();
//                                    } else {
//                                        // show message
//                                        mSecurityMessageDisplay.setMessage(
//                                                getPukPasswordErrorMessage(attemptsRemaining), true);
//                                    }
                                } else {
                                    mSecurityMessageDisplay.setMessage(getContext().getString(
                                            R.string.kg_password_puk_failed), true);
                                }
                                if (DEBUG) Log.d(LOG_TAG, "verifyPasswordAndUnlock "
                                        + " UpdateSim.onSimCheckResponse: "
                                        + " attemptsRemaining=" + attemptsRemaining);
                                mStateMachine.reset();
                                
                            	VibatorUtil.amigoVibrate(mContext, VibatorUtil.LOCKSCREEN_UNLOCK_CODE_ERROR, VibatorUtil.UNLOCK_ERROR_VIBRATE_TIME);
                            }
                            mCheckSimPukThread = null;
                        }
                    });
                }
            };
            mCheckSimPukThread.start();
        }
    }

    @Override
    protected void verifyPasswordAndUnlock() {
    	mKeyguardUpdateMonitor.setIgnoreSimState(mSubId,false);
        mStateMachine.next();
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
				mKeyguardUpdateMonitor.setIgnoreSimState(mSubId,true);
				mKeyguardUpdateMonitor.dismissSimLockState(mSubId);
				if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "ignoreButton after dismissSimLockState simId:"+mSubId+"---");
				mCallback.dismiss(true);
//				mSecurityManager.ignoreSimSecurityView(mSubId);
				// Gionee <jingyn> <2014-08-19> add for CR01358258 end
		        // Gionee <jiangxiao> <2014-06-12> modify for CR01288136 end

			/*	//just used for mtk
				MtkSimUnlockExt.sendSimLockVerifyResult(getContext(), 
						MtkSimUnlockExt.VERIFY_TYPE_PIN, false);*/
			}
		});
	}
	
	
	 @Override
	    public boolean onInterceptTouchEvent(MotionEvent ev) {
	    	if(ev.getY()>=mKeyguardBouncerFrame.getTop()){
	            Log.d("KeyguardPatternUnlockView", "onInterceptTouchEvent.......=");
	            requestDisallowInterceptTouchEvent(true);
	        }
	    	return super.onInterceptTouchEvent(ev);
	    }
}


