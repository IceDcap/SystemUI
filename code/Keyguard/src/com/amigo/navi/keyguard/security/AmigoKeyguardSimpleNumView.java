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



import java.util.ArrayList;
import java.util.List;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.KeyguardViewHostManager;
import com.amigo.navi.keyguard.fingerprint.FingerIdentifyManager;
import com.amigo.navi.keyguard.util.TimeUtils;
import com.amigo.navi.keyguard.util.VibatorUtil;
import com.android.keyguard.AppearAnimationUtils;
import com.android.keyguard.DisappearAnimationUtils;
import com.android.keyguard.KeyguardPinBasedInputView;
import com.android.keyguard.KeyguardSecurityContainer;
import com.android.keyguard.KeyguardSecurityView;
import com.android.keyguard.KeyguardUpdateMonitor;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.R;
import com.gionee.account.sdk.GioneeAccount;
import com.gionee.account.sdk.listener.verifyListener;
import com.gionee.account.sdk.vo.LoginInfo;


/**
 * Displays a PIN pad for unlocking.
 */
public class AmigoKeyguardSimpleNumView extends KeyguardPinBasedInputView {
    
    private static final String LOG_TAG="AmigoKeyguardSimpleNumView";

    private static final int MIN_PASSWORD_LENGTH_BEFORE_CHECKING = 4;
    
    private final AppearAnimationUtils mAppearAnimationUtils;
    private final DisappearAnimationUtils mDisappearAnimationUtils;
    
    private ImageView[] imgs = null;
    private int [] imgIds = null;
    private LinearLayout ll_images;
    
    private ViewGroup mKeyguardBouncerFrame;
    private ViewGroup mRow0;
    private ViewGroup mRow1;
    private ViewGroup mRow2;
    private ViewGroup mRow3;
    private View mDivider;
    private int mDisappearYTranslation;
    private View[][] mViews;
    private CountDownTimer mSimpleNumViewCountdownTimer = null;
    private boolean isFrozen=false;
    
    // 10 keyboard button id
    private int [] mKeyButtonsId = new int[]{
            R.id.key0,
            R.id.key1,
            R.id.key2,
            R.id.key3,
            R.id.key4,
            R.id.key5,
            R.id.key6,
            R.id.key7,
            R.id.key8,
            R.id.key9
    };
    private ArrayList<Button> mKeyButtons = new ArrayList<Button>();
    private TextView mForgetButton;
    private GioneeAccount mGioneeAccount;

    public AmigoKeyguardSimpleNumView(Context context) {
        this(context, null);
    }

    public AmigoKeyguardSimpleNumView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAppearAnimationUtils = new AppearAnimationUtils(context);
        mDisappearAnimationUtils = new DisappearAnimationUtils(context,
                125, 0.6f /* translationScale */,
                0.6f /* delayScale */, AnimationUtils.loadInterpolator(
                        mContext, android.R.interpolator.fast_out_linear_in));
        mDisappearYTranslation = getResources().getDimensionPixelSize(
                R.dimen.disappear_y_translation);
    }

    protected void resetState() {
        super.resetState();
        resetPasswordText(true /* animate */);
        refreshImageStat(0);
        setKeyButtonClickEnable(true);
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
    	int retryCount = getUnlokcRetryCount();
    	if(retryCount>0 && retryCount < LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT){
    		mSecurityMessageDisplay.setMessage(getWrongPasswordStringId(retryCount),true, retryCount);
    	}else{
    		displayDefaultSecurityMessage();
    	}
	}

    @Override
    protected int getPasswordTextViewId() {
        return R.id.pinEntry;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initImage();
        setPasswordEntry();
        addClickListenerToDeleteButton();
        mKeyguardBouncerFrame = (ViewGroup) findViewById(R.id.keyguard_bouncer_frame);
        mRow0 = (ViewGroup) findViewById(R.id.row0);
        mRow1 = (ViewGroup) findViewById(R.id.row1);
        mRow2 = (ViewGroup) findViewById(R.id.row2);
        mRow3 = (ViewGroup) findViewById(R.id.row3);
        mDivider = findViewById(R.id.divider);
        mViews = new View[][]{
                new View[]{
                        mRow0, null, null
                },
                new View[]{
                        findViewById(R.id.key1), findViewById(R.id.key2),
                        findViewById(R.id.key3)
                },
                new View[]{
                        findViewById(R.id.key4), findViewById(R.id.key5),
                        findViewById(R.id.key6)
                },
                new View[]{
                        findViewById(R.id.key7), findViewById(R.id.key8),
                        findViewById(R.id.key9)
                },
                new View[]{
                        null, findViewById(R.id.key0), findViewById(R.id.key_enter)
                },
                new View[]{
                        null, mEcaView, null
                }};
        
        for (int i = 0; i < mKeyButtonsId.length; i++) {
            View view = findViewById(mKeyButtonsId[i]);
            if(view instanceof Button){
                mKeyButtons.add((Button)view);
            }
        }
        mSecurityMessageDisplay.setTimeout(0); // don't show ownerinfo/charging status by default
        resetState();
        setForgetPasswordButton();
        mGioneeAccount = GioneeAccount.getInstance(mContext);
        setVisibility(INVISIBLE);
    }
    
    @Override
    public void onResume(int reason) {
        resetState();
    }

    private void setKeyButtonClickEnable(boolean enabled){
        for (Button button : mKeyButtons) {
            button.setClickable(enabled);
            // for CR01459777 begin
            if(!enabled){
                button.setPressed(false);
            }
            // for CR01459777 end
        }
    }
    

    private void setForgetPasswordButton() {
	   	 mForgetButton = (TextView) this.findViewById(R.id.forget_password);
	     if(mForgetButton == null) return;
	     if(KeyguardViewHostManager.isSuppotFinger() && getTimeOutSize()>=5){
	    	 mForgetButton.setVisibility(View.VISIBLE);
	     }
         mForgetButton.setOnClickListener(new View.OnClickListener() {
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
		mGioneeAccount.verifyForSP(mContext, loginInfo,
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
    
    private void initImage(){
        ll_images = (LinearLayout) findViewById(R.id.ll_images);
        imgIds = new int[]{R.id.iv_password_01, R.id.iv_password_02, R.id.iv_password_03, R.id.iv_password_04};
        imgs = new ImageView[imgIds.length];
        for(int i = 0; i < imgIds.length; i++){
            imgs[i] = (ImageView) findViewById(imgIds[i]);
        }
    }
    
    private void refreshImageStat(int pwdLength) {
        for(int i = 0; i < imgs.length; i++){
            if(i < pwdLength){
                imgs[i].setSelected(true);
            }else{
                imgs[i].setSelected(false);
            }
        }
    }
    
    
    private void setPasswordEntry() {
//        mPasswordEntry.setKeyListener(DigitsKeyListener.getInstance());
//        mPasswordEntry.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
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
                refreshImageStat(pwdLength);
                resetPinDelete(pwdLength);
                Log.d(LOG_TAG, "afterTextChanged--> password length is " + pwdLength+"getPasswordText()="+getPasswordText());
                if (pwdLength == MIN_PASSWORD_LENGTH_BEFORE_CHECKING) {
                    // mPasswordEntry.setEnabled(false);
                    setKeyButtonClickEnable(false);
                    // String entry = mPasswordEntry.getText().toString();
                    verifyPasswordAndUnlock();
                }
            }
        });
    }
    
    
    protected void verifyPasswordAndUnlock() {
        String entry = getPasswordText();
        boolean isLockDone=false;
        
        Log.d(LOG_TAG, "verifyPasswordAndUnlock entry: "+entry);
        if (mLockPatternUtils.checkPassword(entry)) {
        	isLockDone=true;    
        } else {
        	isLockDone=false;
           
        }
        checkPasswordResult(isLockDone, UNLOCK_FAIL_UNKNOW_REASON);
    }
    
    
    
    public void checkPasswordResult(boolean isLockDone, int unLockFailReason) {
        if (DebugLog.DEBUG)
            DebugLog.d(LOG_TAG, "unLockPatternLock UNLOCK_FAIL_REASON_TOO_SHORT..");
        int mUnLockFailReason = unLockFailReason;

        if (isLockDone) {
            // unLockDone
            if (DebugLog.DEBUG)
                DebugLog.d(LOG_TAG, "checkPasswordResult unLockPatternDone");
            unLockDone();
        } else {
            // unlockFail
            // to avoid accidental lockout, only count attempts that are long
            // enough to be a
            // real password. This may require some tweaking.
            mCallback.reportUnlockAttempt(false);
            mUnLockFailReason = UNLOCK_FAIL_REASON_INCORRECT;
            if (mKeyguardUpdateMonitor.getFailedUnlockAttempts() >= LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT) {
                mUnLockFailReason = UNLOCK_FAIL_REASON_TIMEOUT;
            }
            if (DebugLog.DEBUG)
                DebugLog.d(LOG_TAG,
                        "checkPasswordResult unLockPatternfailed...mKeyguardUpdateMonitor.getFailedUnlockAttempts()="
                                + mKeyguardUpdateMonitor.getFailedUnlockAttempts());
            onUnlockFail(mUnLockFailReason);
        }
        
        //temp deal with post delay code to reslove resetPassworkText invoke before PasswordTextView#append();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                resetPasswordText(true /* animate */);
            }
        },100);
    }

	private void unLockDone() {
		if(KeyguardViewHostManager.isSuppotFinger()){
			mForgetButton.setVisibility(View.INVISIBLE);
		}
		mCallback.reportUnlockAttempt(true);
		if(mCallback.getFingerPrintResult()!=KeyguardSecurityContainer.FINGERPRINT_SUCCESS){
			mCallback.dismiss(true);
		}
        setKeyButtonClickEnable(true);
		mCallback.reset();
	}


	public void onUnlockFail(int failReason) {
		if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "onUnlockFail failReason :"+failReason);
		failShake(failReason);
		if(failReason == UNLOCK_FAIL_REASON_TIMEOUT) {
			if(KeyguardViewHostManager.isSuppotFinger()){
				mForgetButton.setVisibility(View.VISIBLE);
			}
			 
			long deadline = mKeyguardUpdateMonitor.getDeadline();
			if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "onUnlockFail deadline :"+deadline);
        	handleAttemptLockout(deadline);	
        	
		} else if(failReason == UNLOCK_FAIL_REASON_INCORRECT) {
            setKeyButtonClickEnable(true);
			 int stringId = getWrongPasswordStringId(getUnlokcRetryCount());
			 if(mCallback.getFingerPrintResult()==KeyguardSecurityContainer.FINGERPRINT_FAILED){
				 mSecurityMessageDisplay.setMessage(stringId,true,getUnlokcRetryCount());
			 }else{
				 mSecurityMessageDisplay.setMessage(stringId,true,getUnlokcRetryCount());
			 }
			 
		}
	}
	
	
	 public void handleAttemptLockout(long elapsedRealtimeDeadline) {

		    setKeyButtonClickEnable(false);
	        final int index = getTimeOutSize();
	        final long elapsedRealtime =System.currentTimeMillis();
	        if(DebugLog.DEBUGMAYBE) DebugLog.d(LOG_TAG, "handleAttemptLockout mSimpleNumViewCountdownTimer=:"+(mSimpleNumViewCountdownTimer==null));
	        if(mSimpleNumViewCountdownTimer==null){
	        	mSimpleNumViewCountdownTimer = new CountDownTimer(elapsedRealtimeDeadline - elapsedRealtime, 1000) {
		
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
		            	setKeyButtonClickEnable(true);
		                displayDefaultSecurityMessage();
		                mSimpleNumViewCountdownTimer=null;
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
        if (KeyguardViewHostManager.isSuppotFinger() && getFingerSwitchState()) {
            mSecurityMessageDisplay.setMessage(R.string.keyguard_password_enter_code_finger, true);
        } else {
            mSecurityMessageDisplay.setMessage(R.string.keyguard_password_enter_code, true);
        }
    }

    private boolean getFingerSwitchState(){
        FingerIdentifyManager fingerIdentifyManager=FingerIdentifyManager.getInstance();
        boolean isFingerSwitchOpen=false;
        if(fingerIdentifyManager!=null){
            isFingerSwitchOpen = fingerIdentifyManager.getFingerprintSwitchOpen();
        }
        DebugLog.d(LOG_TAG, "getFingerSwitchState  isFingerSwitchOpen: "+isFingerSwitchOpen);
        return isFingerSwitchOpen;
    }
    
    private int getTimeOutSize() {
  		return mKeyguardUpdateMonitor.getFailedUnlockAttempts();
  	}
    
    
    
    private int getUnlokcRetryCount() {
    	return LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT - mKeyguardUpdateMonitor.getFailedUnlockAttempts();
    }

    private void resetPinDelete(int num){
        if(num > 0){
            ((TextView)pinDelete).setText(getContext().getResources().getString(R.string.keyguard_simple_number_delete));
        }else{
            ((TextView)pinDelete).setText(getContext().getResources().getString(R.string.keyguard_simple_number_cancel));
        }
    }
    
    private void failShake(final int unLockFailReason) {
//        VibatorUtil.vibator(mContext, 100);
    	//VibatorUtil.amigoVibrate(mContext, VibatorUtil.LOCKSCREEN_UNLOCK_CODE_ERROR, VibatorUtil.UNLOCK_ERROR_VIBRATE_TIME);
    	
        ObjectAnimator oan = ObjectAnimator.ofFloat(ll_images, "translationX", new float[] { 0.0F, 25.0F, -25.0F, 25.0F, -25.0F, 15.0F, -15.0F, 6.0F, -6.0F, 0.0F }).setDuration(1000);
        oan.addListener(new AnimatorListener() {
            
            @Override
            public void onAnimationStart(Animator animation) {
            	 VibatorUtil.amigoVibrate(mContext, VibatorUtil.LOCKSCREEN_UNLOCK_CODE_ERROR, VibatorUtil.UNLOCK_ERROR_VIBRATE_TIME);
            }
            
            @Override
            public void onAnimationRepeat(Animator animation) {
            }
            
            @Override
            public void onAnimationEnd(Animator animation) {
                buttonIsNeedEnable(unLockFailReason);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });
        oan.start();        
    }
    
    private void buttonIsNeedEnable(int mUnLockFailReason) {
        // TODO Auto-generated method stub
        if (mUnLockFailReason != UNLOCK_FAIL_REASON_TIMEOUT) {
            setKeyButtonClickEnable(true);
        }
    }
    
    @Override
    public void showUsabilityHint() {
    }

    @Override
    public int getWrongPasswordStringId() {
        return R.string.kg_wrong_password_onetime;
    }

    @Override
    public void startAppearAnimation() {
    	if(getVisibility() == INVISIBLE){
			setVisibility(VISIBLE);
			enableClipping(false);
	        setAlpha(1f);
	        setTranslationY(mAppearAnimationUtils.getStartTranslation());
	        animate()
	                .setDuration(500)
	                .setInterpolator(mAppearAnimationUtils.getInterpolator())
	                .translationY(0);
	        mAppearAnimationUtils.startAnimation(mViews,
	                new Runnable() {
	                    @Override
	                    public void run() {
	                        enableClipping(true);
	                    }
	                });
    	}
        
    }

    @Override
    public boolean startDisappearAnimation(final Runnable finishRunnable) {
        enableClipping(false);
        setTranslationY(0);
        animate()
                .setDuration(280)
                .setInterpolator(mDisappearAnimationUtils.getInterpolator())
                .translationY(mDisappearYTranslation);
        mDisappearAnimationUtils.startAnimation(mViews,
                new Runnable() {
                    @Override
                    public void run() {
                        enableClipping(true);
                        if (finishRunnable != null) {
                            finishRunnable.run();
                        }
                    }
                });
        return true;
    }

    private void enableClipping(boolean enable) {
        mKeyguardBouncerFrame.setClipToPadding(enable);
        mKeyguardBouncerFrame.setClipChildren(enable);
        mRow1.setClipToPadding(enable);
        mRow2.setClipToPadding(enable);
        mRow3.setClipToPadding(enable);
        setClipChildren(enable);
    }

    @Override
    public boolean hasOverlappingRendering() {
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
                    if (mPasswordEntry.isEnabled()) {
                        if (str.length() > 0) {
                            if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "delete one digit");
                            mPasswordEntry.setText(str.subSequence(0, str.length() - 1).toString());
                        }
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
                        mPasswordEntry.setText("");
                    }
//                    doHapticKeyClick();
                    VibatorUtil.amigoVibrate(mContext, VibatorUtil.LOCKSCREEN_UNLOCK_CODE_TAP, VibatorUtil.TOUCH_TAP_VIBRATE_TIME);
                    return true;
                }
            });
        }
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
    public void onPause(int reason) {
    	setVisibility(View.INVISIBLE);
    	if (reason == KeyguardSecurityView.SCREEN_OFF && mSimpleNumViewCountdownTimer != null) {
    		mSimpleNumViewCountdownTimer.cancel();
    		mSimpleNumViewCountdownTimer = null;
        }
    }
    
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
    	if(ev.getY()>=mForgetButton.getTop()){
            Log.d("KeyguardPatternUnlockView", "onInterceptTouchEvent.......=");
            requestDisallowInterceptTouchEvent(true);
        }
    	return super.onInterceptTouchEvent(ev);
    }
    
    @Override
    public boolean isFrozen() {
        return isFrozen;
    }
}
