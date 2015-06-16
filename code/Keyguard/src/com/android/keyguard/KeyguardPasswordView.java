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

package com.android.keyguard;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.TextKeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import java.util.List;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.KeyguardViewHostManager;
import com.amigo.navi.keyguard.fingerprint.FingerIdentifyManager;
import com.amigo.navi.keyguard.security.AmigoAccount;
import com.amigo.navi.keyguard.security.AmigoUnBindAcountActivity;
import com.amigo.navi.keyguard.skylight.SkylightActivity;
import com.amigo.navi.keyguard.util.TimeUtils;
import com.amigo.navi.keyguard.util.VibatorUtil;
import com.android.internal.widget.LockPatternUtils;
import com.gionee.account.sdk.GioneeAccount;
import com.gionee.account.sdk.listener.verifyListener;
import com.gionee.account.sdk.vo.LoginInfo;
/**
 * Displays an alphanumeric (latin-1) key entry for the user to enter
 * an unlock password
 */

public class KeyguardPasswordView extends KeyguardAbsKeyInputView
        implements KeyguardSecurityView, OnEditorActionListener, TextWatcher {

    private static final String LOG_TAG="KeyguardPasswordView";
    private final boolean mShowImeAtScreenOn;
    private final int mDisappearYTranslation;

    InputMethodManager mImm;
    private TextView mPasswordEntry;
    private Interpolator mLinearOutSlowInInterpolator;
    private Interpolator mFastOutLinearInInterpolator;
    private CountDownTimer mPasswordViewCountdownTimer = null;
    private TextView forgetButton;
    private GioneeAccount gioneeAccount;
    private boolean isFrozen=false;

    public KeyguardPasswordView(Context context) {
        this(context, null);
    }

    public KeyguardPasswordView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mShowImeAtScreenOn = context.getResources().
                getBoolean(R.bool.kg_show_ime_at_screen_on);
        mDisappearYTranslation = getResources().getDimensionPixelSize(
                R.dimen.disappear_y_translation);
        mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(
                context, android.R.interpolator.linear_out_slow_in);
        mFastOutLinearInInterpolator = AnimationUtils.loadInterpolator(
                context, android.R.interpolator.fast_out_linear_in);
    }

    protected void resetState() {
        long deadline =  mKeyguardUpdateMonitor.getCurDeadLine();
        if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "resetState :deadline="+deadline);
        if (deadline!=0) {
            handleAttemptLockout(deadline);
        }else{
            resetFrozen();
        	resetMessage();
        }

    }
    
    private void resetFrozen(){
        isFrozen=false;
    }
    
    private void resetMessage() {
        mPasswordEntry.setEnabled(true);
    	int retryCount = getUnlokcRetryCount();
    	if(retryCount>0 && retryCount < LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT){
    		mSecurityMessageDisplay.setMessage(getWrongPasswordStringId(retryCount),true, retryCount);
    	}else{
    		displayDefaultSecurityMessage();
    	}
	}
    
    
    @Override
    protected int getPasswordTextViewId() {
        return R.id.passwordEntry;
    }

    @Override
    public boolean needsInput() {
        return true;
    }

    @Override
    public void onResume(final int reason) {
        super.onResume(reason);
        // 解决bug： 使用混合密码，应用有输入法弹出时，灭屏再亮屏输入法未收起问题
        hiddenInput();
        // Wait a bit to focus the field so the focusable flag on the window is already set then.
        if(reason == KeyguardSecurityView.KEYGUARD_HOSTVIEW_SCROLL_AT_UNLOCKH_EIGHT){
        post(new Runnable() {
            @Override
            public void run() {
                if (isShown()) {
                    mPasswordEntry.requestFocus();
                    if ((reason != KeyguardSecurityView.SCREEN_ON || mShowImeAtScreenOn) && mPasswordEntry.isEnabled()) {
                    	showInput();
                    }
                }
            }
        });
        }
    }
  
    private void showInput(){
    	post(new Runnable() {
			@Override
			public void run() {
				mPasswordEntry.setSelected(true);
				mImm.showSoftInput(mPasswordEntry, InputMethodManager.SHOW_IMPLICIT);
			}
		});
    }
    
    

    @Override
    public void onPause(int reason) {
        super.onPause(reason);
        hiddenInput();
        if (reason == KeyguardSecurityView.SCREEN_OFF && mPasswordViewCountdownTimer != null) {
        	mPasswordViewCountdownTimer.cancel();
        	mPasswordViewCountdownTimer = null;
        }
//        setVisibility(INVISIBLE);
    }

	private void hiddenInput() {
		mPasswordEntry.clearFocus();
		boolean hide = mImm.hideSoftInputFromWindow(getWindowToken(), 0);
	    if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "hideSoftInputFromWindow return"+hide);
	}

    @Override
    public void reset() {
        super.reset();
        mPasswordEntry.requestFocus();
    }
    
    @Override
    protected void onDetachedFromWindow() {
    	// TODO Auto-generated method stub
    	super.onDetachedFromWindow();
    	hiddenInput();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        boolean imeOrDeleteButtonVisible = false;

        mImm = (InputMethodManager) getContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);

        mPasswordEntry = (TextView) findViewById(getPasswordTextViewId());
        mPasswordEntry.setKeyListener(TextKeyListener.getInstance());
        mPasswordEntry.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mPasswordEntry.setOnEditorActionListener(this);
        mPasswordEntry.addTextChangedListener(this);

        // Poke the wakelock any time the text is selected or modified
        mPasswordEntry.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mCallback.userActivity();
            }
        });

        // Set selected property on so the view can send accessibility events.
        mPasswordEntry.setSelected(true);

        mPasswordEntry.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                if (mCallback != null) {
                    mCallback.userActivity();
                }
            }
        });

        mPasswordEntry.requestFocus();

        // If there's more than one IME, enable the IME switcher button
        View switchImeButton = findViewById(R.id.switch_ime_button);
        if (switchImeButton != null && hasMultipleEnabledIMEsOrSubtypes(mImm, false)) {
            switchImeButton.setVisibility(View.VISIBLE);
            imeOrDeleteButtonVisible = true;
            switchImeButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    mCallback.userActivity(); // Leave the screen on a bit longer
                    mImm.showInputMethodPicker();
                }
            });
        }

        // If no icon is visible, reset the start margin on the password field so the text is
        // still centered.
        if (!imeOrDeleteButtonVisible) {
            android.view.ViewGroup.LayoutParams params = mPasswordEntry.getLayoutParams();
            if (params instanceof MarginLayoutParams) {
                final MarginLayoutParams mlp = (MarginLayoutParams) params;
                mlp.setMarginStart(0);
                mPasswordEntry.setLayoutParams(params);
            }
        }
        findViewById(R.id.back_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				KeyguardViewHostManager.getInstance().scrollToKeyguardPageByAnimation();
				
				VibatorUtil.amigoVibrate(mContext, VibatorUtil.LOCKSCREEN_UNLOCK_CODE_TAP, VibatorUtil.TOUCH_TAP_VIBRATE_TIME);
			}
		});
        mSecurityMessageDisplay.setTimeout(0); // don't show ownerinfo/charging status by default
        resetState();
        setForgetPasswordButton();
        gioneeAccount = GioneeAccount.getInstance(mContext);
//        setVisibility(INVISIBLE);
    }
    

    private void setForgetPasswordButton() {
   	    forgetButton = (TextView) this.findViewById(R.id.forget_password);
        if(forgetButton == null) return;
        if(KeyguardViewHostManager.isSuppotFinger() && getTimeOutSize()>=5){
       	 	forgetButton.setVisibility(View.VISIBLE);
        }
        forgetButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "forgetButton button clicked  ");
 			    LoginInfo  loginInfo= AmigoAccount.getInstance().getAccountNameAndId();
 			    if(loginInfo!=null){
 			    	logAccount(loginInfo);
 			    }else{
 			    	Intent intent = new Intent(mContext, AmigoUnBindAcountActivity.class);
		            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		            mContext.startActivity(intent);
 			    }
			}
		});
		
	}
    
	private void logAccount(LoginInfo  loginInfo) {
		gioneeAccount.verifyForSP(mContext, loginInfo,
				new verifyListener() {

					@Override
					public void onError(Exception e) {

					}

					@Override
					public void onSucess(Object o) {
                          Log.i("jiating","ForgetPasswordButton...onSuccess") ;
                          checkPasswordResult(true, UNLOCK_FAIL_UNKNOW_REASON);
					}

					@Override
					public void onCancel(Object o) {

					}
				});
	}
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(ev.getY()>=mPasswordEntry.getTop()){
           Log.d("KeyguardPatternUnlockView", "onInterceptTouchEvent.......=");
           requestDisallowInterceptTouchEvent(true);
       }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        // send focus to the password field
        return mPasswordEntry.requestFocus(direction, previouslyFocusedRect);
    }

    @Override
    protected void resetPasswordText(boolean animate) {
        mPasswordEntry.setText("");
    }

    @Override
    protected String getPasswordText() {
        return mPasswordEntry.getText().toString();
    }

    @Override
    protected void setPasswordEntryEnabled(boolean enabled) {
        mPasswordEntry.setEnabled(enabled);
    }

    /**
     * Method adapted from com.android.inputmethod.latin.Utils
     *
     * @param imm The input method manager
     * @param shouldIncludeAuxiliarySubtypes
     * @return true if we have multiple IMEs to choose from
     */
    private boolean hasMultipleEnabledIMEsOrSubtypes(InputMethodManager imm,
            final boolean shouldIncludeAuxiliarySubtypes) {
        final List<InputMethodInfo> enabledImis = imm.getEnabledInputMethodList();

        // Number of the filtered IMEs
        int filteredImisCount = 0;

        for (InputMethodInfo imi : enabledImis) {
            // We can return true immediately after we find two or more filtered IMEs.
            if (filteredImisCount > 1) return true;
            final List<InputMethodSubtype> subtypes =
                    imm.getEnabledInputMethodSubtypeList(imi, true);
            // IMEs that have no subtypes should be counted.
            if (subtypes.isEmpty()) {
                ++filteredImisCount;
                continue;
            }

            int auxCount = 0;
            for (InputMethodSubtype subtype : subtypes) {
                if (subtype.isAuxiliary()) {
                    ++auxCount;
                }
            }
            final int nonAuxCount = subtypes.size() - auxCount;

            // IMEs that have one or more non-auxiliary subtypes should be counted.
            // If shouldIncludeAuxiliarySubtypes is true, IMEs that have two or more auxiliary
            // subtypes should be counted as well.
            if (nonAuxCount > 0 || (shouldIncludeAuxiliarySubtypes && auxCount > 1)) {
                ++filteredImisCount;
                continue;
            }
        }

        return filteredImisCount > 1
        // imm.getEnabledInputMethodSubtypeList(null, false) will return the current IME's enabled
        // input method subtype (The current IME should be LatinIME.)
                || imm.getEnabledInputMethodSubtypeList(null, false).size() > 1;
    }

    @Override
    public void showUsabilityHint() {
    }

    @Override
    public int getWrongPasswordStringId() {
        return R.string.kg_wrong_password;
    }

    @Override
    public void startAppearAnimation() {
    	if(getVisibility() == INVISIBLE){
			setVisibility(VISIBLE);
			 setAlpha(0f);
		        setTranslationY(0f);
		        animate()
		                .alpha(1)
		                .withLayer()
		                .setDuration(300)
		                .setInterpolator(mLinearOutSlowInInterpolator);
    	}
       
    }

    @Override
    public boolean startDisappearAnimation(Runnable finishRunnable) {
        animate()
                .alpha(0f)
                .translationY(mDisappearYTranslation)
                .setInterpolator(mFastOutLinearInInterpolator)
                .setDuration(100)
                .withEndAction(finishRunnable);
        return true;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (mCallback != null) {
            mCallback.userActivity();
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        // Check if this was the result of hitting the enter key
        final boolean isSoftImeEvent = event == null
                && (actionId == EditorInfo.IME_NULL
                || actionId == EditorInfo.IME_ACTION_DONE
                || actionId == EditorInfo.IME_ACTION_NEXT);
        final boolean isKeyboardEnterKey = event != null
                && KeyEvent.isConfirmKey(event.getKeyCode())
                && event.getAction() == KeyEvent.ACTION_DOWN;
        if (isSoftImeEvent || isKeyboardEnterKey) {
            verifyPasswordAndUnlock();
            return true;
        }
        return false;
    }
    

    protected void verifyPasswordAndUnlock() {
        String entry = getPasswordText();
        boolean isLockDone=false;
        if(TextUtils.isEmpty(entry)){
        	mSecurityMessageDisplay.setMessage(R.string.kg_complex_number_notnull,true);
			return;
		}
        Log.d(LOG_TAG, "verifyPasswordAndUnlock entry: "+entry);
        if (mLockPatternUtils.checkPassword(entry)) {
        	isLockDone=true;    
        } else {
        	isLockDone=false;
           
        }
        checkPasswordResult(isLockDone, UNLOCK_FAIL_UNKNOW_REASON);
    }
    
    public void checkPasswordResult(boolean isLockDone , int unLockFailReason) {
		if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "unLockPatternLock UNLOCK_FAIL_REASON_TOO_SHORT..");
		int mUnLockFailReason=unLockFailReason;
		
		if (isLockDone) {
			// unLockDone
			if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "checkPasswordResult unLockPatternDone");
			unLockDone();
		} else {
			// unlockFail
	        // to avoid accidental lockout, only count attempts that are long enough to be a
	        // real password. This may require some tweaking.
			if (mUnLockFailReason != UNLOCK_FAIL_REASON_TOO_SHORT) {
				  mCallback.reportUnlockAttempt(false);
			      mUnLockFailReason = UNLOCK_FAIL_REASON_INCORRECT; 
			      if (mKeyguardUpdateMonitor.getFailedUnlockAttempts() >= LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT) {
						mUnLockFailReason = UNLOCK_FAIL_REASON_TIMEOUT;
				  } 
			      if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "checkPasswordResult unLockPatternfailed...mKeyguardUpdateMonitor.getFailedUnlockAttempts()="+mKeyguardUpdateMonitor.getFailedUnlockAttempts());
			      onUnlockFail(mUnLockFailReason);
			}
			failShake(mUnLockFailReason);
		}
			
		resetPasswordText(true /* animate */);		
	}
    
    
    private void failShake(int mUnLockFailReason) {
    	VibatorUtil.amigoVibrate(mContext, VibatorUtil.LOCKSCREEN_UNLOCK_CODE_ERROR, VibatorUtil.UNLOCK_ERROR_VIBRATE_TIME);
		
	}

	private void unLockDone() {
		mCallback.reportUnlockAttempt(true);
		if(mCallback.getFingerPrintResult()!=KeyguardSecurityContainer.FINGERPRINT_SUCCESS){
	        mCallback.dismiss(true);
		}
        hiddenInput();
		mCallback.reset();
		if(KeyguardViewHostManager.isSuppotFinger()){			
			forgetButton.setVisibility(View.INVISIBLE);
		}
	}
    
    public void onUnlockFail(int failReason) {
		if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "onUnlockFail failReason :"+failReason);
		
		if(failReason == UNLOCK_FAIL_REASON_TIMEOUT) {
			if(KeyguardViewHostManager.isSuppotFinger()){
				forgetButton.setVisibility(View.VISIBLE);
			}
			long deadline = mKeyguardUpdateMonitor.getDeadline();
			if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "onUnlockFail deadline :"+deadline);
        	handleAttemptLockout(deadline);	
		} else if(failReason == UNLOCK_FAIL_REASON_INCORRECT) {
			 int stringId = getWrongPasswordStringId(getUnlokcRetryCount());
			 if(mCallback.getFingerPrintResult()==KeyguardSecurityContainer.FINGERPRINT_FAILED){
				 mSecurityMessageDisplay.setMessage(stringId,true,getUnlokcRetryCount());
			 }else{
				 mSecurityMessageDisplay.setMessage(stringId,true,getUnlokcRetryCount());
			 }
			 
		}else if(failReason == UNLOCK_FAIL_REASON_TOO_SHORT) {
//			mSecurityMessageDisplay.setMessage(R.string.kg_number_is_too_short);
		}
	}
    
    public void handleAttemptLockout(long elapsedRealtimeDeadline) {
    	mPasswordEntry.setEnabled(false);
        final int index = getTimeOutSize();
        final long elapsedRealtime = System.currentTimeMillis();
        if(DebugLog.DEBUGMAYBE) DebugLog.d(LOG_TAG, "handleAttemptLockout mSimpleNumViewCountdownTimer=:"+(mPasswordViewCountdownTimer==null));
        if(mPasswordViewCountdownTimer==null){
        	mPasswordViewCountdownTimer = new CountDownTimer(elapsedRealtimeDeadline - elapsedRealtime, 1000) {
	
	            @Override
	            public void onTick(long millisUntilFinished) {
	                final int secondsRemaining = (int) (millisUntilFinished / 1000);
	                if(DebugLog.DEBUGMAYBE) DebugLog.d(LOG_TAG, "onUnlockFail secondsRemaining :"+secondsRemaining);
	                mSecurityMessageDisplay.setMessage(
	                        R.string.amigo_kg_too_many_failed_attempts_countdown, index,true, TimeUtils.secToTime(secondsRemaining,mContext));
	                isFrozen=true;
	            }
	
	            @Override
	            public void onFinish() {
	            	if(DebugLog.DEBUGMAYBE) DebugLog.d(LOG_TAG, "onFinish ");
	                displayDefaultSecurityMessage();
	                mPasswordViewCountdownTimer=null;
	                mPasswordEntry.setEnabled(true);
	                isFrozen=false;
	                KeyguardViewHostManager.getInstance().UnFrozenSecurityLock();
	            }
	
	        }.start();
        }
    }


		private int getWrongPasswordStringId(int retryCount) {
			int stringId = R.string.kg_wrong_password_time;
			if(retryCount == 1){
				stringId = R.string.kg_wrong_password_onetime;
			}
		    return stringId;
		}
		
		  private void displayDefaultSecurityMessage() {
		    
		        if(KeyguardViewHostManager.isSuppotFinger()&&getFingerSwitchState()){
		    		mSecurityMessageDisplay.setMessage(R.string.keyguard_password_enter_code_finger, true);
		    	}else{ 		
		    		mSecurityMessageDisplay.setMessage(R.string.keyguard_password_enter_code, true);
		    	}
		    }
		  
		private boolean getFingerSwitchState(){
		    FingerIdentifyManager fingerIdentifyManager=FingerIdentifyManager.getInstance();
            boolean isFingerSwitchOpen=false;
            if(fingerIdentifyManager!=null){
                isFingerSwitchOpen = fingerIdentifyManager.getFingerprintSwitchOpen();
            }
            return isFingerSwitchOpen;
		}
		
		private int getTimeOutSize() {
				return mKeyguardUpdateMonitor.getFailedUnlockAttempts();
			}
		
		
		
		private int getUnlokcRetryCount() {
			return LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT - mKeyguardUpdateMonitor.getFailedUnlockAttempts();
		}

	@Override
	public void fingerPrintFailed() {
		checkPasswordResult(false, UNLOCK_FAIL_UNKNOW_REASON);
	}

	@Override
	public void fingerPrintSuccess() {
		checkPasswordResult(true, UNLOCK_FAIL_UNKNOW_REASON);
	}
    
    @Override
    public boolean isFrozen() {
    	// TODO Auto-generated method stub
    	return isFrozen;
    }
    
}
