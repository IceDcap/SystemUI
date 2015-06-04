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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.KeyguardViewHostManager;
import com.amigo.navi.keyguard.fingerprint.FingerIdentifyManager;
import com.amigo.navi.keyguard.security.AmigoAccount;
import com.amigo.navi.keyguard.security.AmigoUnBindAcountActivity;
import com.amigo.navi.keyguard.util.TimeUtils;
import com.amigo.navi.keyguard.util.VibatorUtil;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternView;
import com.gionee.account.sdk.GioneeAccount;
import com.gionee.account.sdk.listener.verifyListener;
import com.gionee.account.sdk.vo.LoginInfo;



import java.util.List;

public class KeyguardPatternView extends LinearLayout implements KeyguardSecurityView,
        AppearAnimationCreator<LockPatternView.CellState> {

    private static final String TAG = "SecurityPatternView";
    private static final boolean DEBUG = KeyguardConstants.DEBUG;

    // how long before we clear the wrong pattern
    private static final int PATTERN_CLEAR_TIMEOUT_MS = 2000;

    // how long we stay awake after each key beyond MIN_PATTERN_BEFORE_POKE_WAKELOCK
    private static final int UNLOCK_PATTERN_WAKE_INTERVAL_MS = 7000;

    // how many cells the user has to cross before we poke the wakelock
    private static final int MIN_PATTERN_BEFORE_POKE_WAKELOCK = 2;

    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private final AppearAnimationUtils mAppearAnimationUtils;
    private final DisappearAnimationUtils mDisappearAnimationUtils;

    private CountDownTimer mCountdownTimer = null;
    private LockPatternUtils mLockPatternUtils;
    private LockPatternView mLockPatternView;
    private KeyguardSecurityCallback mCallback;
    private TextView forgetButton;
    private boolean isFrozen=false;

    /**
     * Keeps track of the last time we poked the wake lock during dispatching of the touch event.
     * Initialized to something guaranteed to make us poke the wakelock when the user starts
     * drawing the pattern.
     * @see #dispatchTouchEvent(android.view.MotionEvent)
     */
    private long mLastPokeTime = -UNLOCK_PATTERN_WAKE_INTERVAL_MS;

    /**
     * Useful for clearing out the wrong pattern after a delay
     */
    private Runnable mCancelPatternRunnable = new Runnable() {
        public void run() {
            mLockPatternView.clearPattern();
        }
    };
    private Rect mTempRect = new Rect();
    private SecurityMessageDisplay mSecurityMessageDisplay;
    private View mEcaView;
    private Drawable mBouncerFrame;
    private ViewGroup mKeyguardBouncerFrame;
    private KeyguardMessageArea mHelpMessage;
    private int mDisappearYTranslation;
    private GioneeAccount gioneeAccount;

    enum FooterMode {
        Normal,
        ForgotLockPattern,
        VerifyUnlocked
    }

    public KeyguardPatternView(Context context) {
        this(context, null);
    }

    public KeyguardPatternView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mKeyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(mContext);
        mAppearAnimationUtils = new AppearAnimationUtils(context,
                AppearAnimationUtils.DEFAULT_APPEAR_DURATION, 1.5f /* translationScale */,
                2.0f /* delayScale */, AnimationUtils.loadInterpolator(
                        mContext, android.R.interpolator.linear_out_slow_in));
        mDisappearAnimationUtils = new DisappearAnimationUtils(context,
                125, 1.2f /* translationScale */,
                0.8f /* delayScale */, AnimationUtils.loadInterpolator(
                        mContext, android.R.interpolator.fast_out_linear_in));
        mDisappearYTranslation = getResources().getDimensionPixelSize(
                R.dimen.disappear_y_translation);
    }

    public void setKeyguardCallback(KeyguardSecurityCallback callback) {
        mCallback = callback;
    }

    public void setLockPatternUtils(LockPatternUtils utils) {
        mLockPatternUtils = utils;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mLockPatternUtils = mLockPatternUtils == null
                ? new LockPatternUtils(mContext) : mLockPatternUtils;

        mLockPatternView = (LockPatternView) findViewById(R.id.lockPatternView);
        mLockPatternView.setSaveEnabled(false);
        mLockPatternView.setFocusable(false);
        mLockPatternView.setOnPatternListener(new UnlockPatternListener());

        // stealth mode will be the same for the life of this screen
        mLockPatternView.setInStealthMode(!mLockPatternUtils.isVisiblePatternEnabled());

        // vibrate mode will be the same for the life of this screen
        mLockPatternView.setTactileFeedbackEnabled(mLockPatternUtils.isTactileFeedbackEnabled());

        setFocusableInTouchMode(true);

        mSecurityMessageDisplay = new KeyguardMessageArea.Helper(this);
        mEcaView = findViewById(R.id.keyguard_selector_fade_container);
        View bouncerFrameView = findViewById(R.id.keyguard_bouncer_frame);
        if (bouncerFrameView != null) {
            mBouncerFrame = bouncerFrameView.getBackground();
        }

        mKeyguardBouncerFrame = (ViewGroup) findViewById(R.id.keyguard_bouncer_frame);
        mHelpMessage = (KeyguardMessageArea) findViewById(R.id.keyguard_message_area);
        
        findViewById(R.id.back_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				KeyguardViewHostManager.getInstance().scrollToKeyguardPageByAnimation();
				
				VibatorUtil.amigoVibrate(mContext, VibatorUtil.LOCKSCREEN_UNLOCK_CODE_TAP, VibatorUtil.TOUCH_TAP_VIBRATE_TIME);
			}
		});
        mSecurityMessageDisplay.setTimeout(0); // don't show ownerinfo/charging status by default
        reset();
        setForgetPasswordButton();
        gioneeAccount = GioneeAccount.getInstance(mContext);
        setVisibility(INVISIBLE);
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
 				if(DebugLog.DEBUG) DebugLog.d(TAG, "forgetButton button clicked  ");
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
                  		checkPasswordResult(true, UNLOCK_FAIL_UNKNOW_REASON,null);
					}

					@Override
					public void onCancel(Object o) {

					}
				});
	}

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(ev.getY()>=mLockPatternView.getTop()){
           Log.d("KeyguardPatternUnlockView", "onInterceptTouchEvent.......=");
           requestDisallowInterceptTouchEvent(true);
       }
        return super.onInterceptTouchEvent(ev);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean result = super.onTouchEvent(ev);
        // as long as the user is entering a pattern (i.e sending a touch event that was handled
        // by this screen), keep poking the wake lock so that the screen will stay on.
        final long elapsed = SystemClock.elapsedRealtime() - mLastPokeTime;
        if (result && (elapsed > (UNLOCK_PATTERN_WAKE_INTERVAL_MS - 100))) {
            mLastPokeTime = SystemClock.elapsedRealtime();
        }
        mTempRect.set(0, 0, 0, 0);
        offsetRectIntoDescendantCoords(mLockPatternView, mTempRect);
        ev.offsetLocation(mTempRect.left, mTempRect.top);
        result = mLockPatternView.dispatchTouchEvent(ev) || result;
        ev.offsetLocation(-mTempRect.left, -mTempRect.top);
        return result;
    }

    public void reset() {
        // reset lock pattern
    	
        mLockPatternView.enableInput();
        mLockPatternView.setEnabled(true);
        mLockPatternView.clearPattern();

        // stealth mode will be the same for the life of this screen
        mLockPatternView.setInStealthMode(!mLockPatternUtils.isVisiblePatternEnabled());

        // if the user is currently locked out, enforce it.
        long deadline =  mKeyguardUpdateMonitor.getCurDeadLine();
        if(DebugLog.DEBUG) DebugLog.d(TAG, "reset...deadline="+deadline);
        if (deadline != 0) {
            handleAttemptLockout(deadline);
        } else {
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
    private void displayDefaultSecurityMessage() {
      /*  if (mKeyguardUpdateMonitor.getMaxBiometricUnlockAttemptsReached()) {
            mSecurityMessageDisplay.setMessage(R.string.faceunlock_multiple_failures, true);
        } else {
            mSecurityMessageDisplay.setMessage(R.string.kg_pattern_instructions, false);
        }*/
    	if(KeyguardViewHostManager.isSuppotFinger()&&getFingerSwitchState()){
    		mSecurityMessageDisplay.setMessage(R.string.keyguard_pattern_enter_code_finger, true);
    	}else{ 		
    		mSecurityMessageDisplay.setMessage(R.string.keyguard_pattern_enter_code, true);
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

    @Override
    public void showUsabilityHint() {
    }

    /** TODO: hook this up */
    public void cleanUp() {
        if (DEBUG) Log.v(TAG, "Cleanup() called on " + this);
        mLockPatternUtils = null;
        mLockPatternView.setOnPatternListener(null);
    }

    private class UnlockPatternListener implements LockPatternView.OnPatternListener {

        public void onPatternStart() {
        	if(DebugLog.DEBUG) DebugLog.d(TAG, "UnlockPatternListener--onPatternStart");
            mLockPatternView.removeCallbacks(mCancelPatternRunnable);
        }

        public void onPatternCleared() {
        	if(DebugLog.DEBUG) DebugLog.d(TAG, "UnlockPatternListener--onPatternCleared");
        }

        public void onPatternCellAdded(List<LockPatternView.Cell> pattern) {
        	if(DebugLog.DEBUG) DebugLog.d(TAG, "UnlockPatternListener--onPatternCellAdded");
            mCallback.userActivity();
            VibatorUtil.amigoVibrate(mContext, VibatorUtil.LOCKSCREEN_UNLOCK_CODE_TAP, VibatorUtil.TOUCH_TAP_VIBRATE_TIME);
        }

        public void onPatternDetected(List<LockPatternView.Cell> pattern) {
            Log.d(TAG, "onPatternDetected  checkPattern: "+mLockPatternUtils.checkPattern(pattern));
            handlePatternDetected(pattern);
        }

		
    }
    
    private void handlePatternDetected(List<LockPatternView.Cell> pattern) {
    	int mUnLockFailReason= UNLOCK_FAIL_UNKNOW_REASON;
    	boolean isLockDone=false;
		if (mLockPatternUtils.checkPattern(pattern)) {
			// unLockDone
			isLockDone=true;
			if(DebugLog.DEBUG) DebugLog.d(TAG, "checkPasswordResult unLockPatternDone");
		} else {
			// unlockFail
			if(DebugLog.DEBUG) DebugLog.d(TAG, "checkPasswordResult unLockPatternfailed...LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT="+LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT);
			isLockDone=false;
			if (pattern.size() > MIN_PATTERN_BEFORE_POKE_WAKELOCK) {
			      mCallback.userActivity();
			 }		 
		}
		checkPasswordResult(isLockDone, mUnLockFailReason,pattern);
		
	
	}


	
	public void checkPasswordResult(boolean isLockDone , int unLockFailReason, List<LockPatternView.Cell> pattern) {
		if(DebugLog.DEBUG) DebugLog.d(TAG, "unLockPatternLock UNLOCK_FAIL_REASON_TOO_SHORT.."+LockPatternUtils.MIN_LOCK_PATTERN_SIZE);
		int mUnLockFailReason=unLockFailReason;
		
		if (isLockDone) {
			// unLockDone
			if(DebugLog.DEBUG) DebugLog.d(TAG, "checkPasswordResult unLockPatternDone");
			unLockDone();
		} else {
			// unlockFail
			if (pattern!=null && pattern.size() < LockPatternUtils.MIN_PATTERN_REGISTER_FAIL){
				mUnLockFailReason = UNLOCK_FAIL_REASON_TOO_SHORT;
			}else{	
				 mCallback.reportUnlockAttempt(false);
				 mUnLockFailReason = UNLOCK_FAIL_REASON_INCORRECT; 
				 if(DebugLog.DEBUG) DebugLog.d(TAG, "checkPasswordResult unLockPatternDone..LockPatternUtils.MIN_PATTERN_REGISTER_FAIL="+LockPatternUtils.MIN_PATTERN_REGISTER_FAIL);	
				 if (mKeyguardUpdateMonitor.getFailedUnlockAttempts() >= LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT) {
						mUnLockFailReason = UNLOCK_FAIL_REASON_TIMEOUT;
				 }
			}
			 if(DebugLog.DEBUG) DebugLog.d(TAG, "checkPasswordResult unLockPatternfailed...mKeyguardUpdateMonitor.getFailedUnlockAttempts()="+mKeyguardUpdateMonitor.getFailedUnlockAttempts());
			 onUnlockFail(mUnLockFailReason);
		}
		mCallback.userActivity();		
	}

	private void unLockDone() {
		mCallback.reportUnlockAttempt(true);
		mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Correct);
		if(mCallback.getFingerPrintResult()!=KeyguardSecurityContainer.FINGERPRINT_SUCCESS){
			mCallback.dismiss(true);
		}
		mCallback.reset();
		if(KeyguardViewHostManager.isSuppotFinger()){			
			forgetButton.setVisibility(View.INVISIBLE);
		}
	}


	public void onUnlockFail(int failReason) {
		
		mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);	
		if(DebugLog.DEBUG) DebugLog.d(TAG, "onUnlockFail failReason :"+failReason);
		if(failReason == UNLOCK_FAIL_REASON_TIMEOUT) {
			if(KeyguardViewHostManager.isSuppotFinger()){				
				forgetButton.setVisibility(View.VISIBLE);
			}
		    VibatorUtil.amigoVibrate(mContext, VibatorUtil.LOCKSCREEN_UNLOCK_CODE_ERROR, VibatorUtil.UNLOCK_ERROR_VIBRATE_TIME);
			long deadline = mKeyguardUpdateMonitor.getDeadline();
			if(DebugLog.DEBUG) DebugLog.d(TAG, "onUnlockFail deadline :"+deadline+"LockPatternUtils.FAILED_ATTEMPT_TIMEOUT_MS="+LockPatternUtils.FAILED_ATTEMPT_TIMEOUT_MS);
        	handleAttemptLockout(deadline);	
		} else if(failReason == UNLOCK_FAIL_REASON_INCORRECT) {
			 VibatorUtil.amigoVibrate(mContext, VibatorUtil.LOCKSCREEN_UNLOCK_CODE_ERROR, VibatorUtil.UNLOCK_ERROR_VIBRATE_TIME);
			 int stringId = getWrongPasswordStringId(getUnlokcRetryCount());
			 if(mCallback.getFingerPrintResult()==KeyguardSecurityContainer.FINGERPRINT_FAILED){
				 mSecurityMessageDisplay.setMessage(stringId,true,getUnlokcRetryCount());
			 }else{
				 mSecurityMessageDisplay.setMessage(stringId,true,getUnlokcRetryCount());
			 }
			 
		} else if(failReason == UNLOCK_FAIL_REASON_TOO_SHORT) {
			 mSecurityMessageDisplay.setMessage(R.string.kg_wrong_point_num, true);
		}
		mLockPatternView.postDelayed(mCancelPatternRunnable, PATTERN_CLEAR_TIMEOUT_MS);
	}
	
	private int getUnlokcRetryCount() {
		return LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT - mKeyguardUpdateMonitor.getFailedUnlockAttempts();
	}
	private int getWrongPasswordStringId(int retryCount) {
    	int stringId = R.string.kg_wrong_pattern_time;
    	if(retryCount == 1){
    		stringId = R.string.kg_wrong_pattern_onetime;
    	}
        return stringId;
    }


    private void handleAttemptLockout(long elapsedRealtimeDeadline) {
        mLockPatternView.clearPattern();
        mLockPatternView.setEnabled(false);
        final int index = getTimeOutSize();
        final long elapsedRealtime = System.currentTimeMillis();
        if(mCountdownTimer==null){
	        mCountdownTimer = new CountDownTimer(elapsedRealtimeDeadline - elapsedRealtime, 1000) {
	
	            @Override
	            public void onTick(long millisUntilFinished) {
	                final int secondsRemaining = (int) (millisUntilFinished / 1000);
	                if(DebugLog.DEBUGMAYBE) DebugLog.d(TAG, "onUnlockFail secondsRemaining :"+secondsRemaining);
	                mSecurityMessageDisplay.setMessage(
	                        R.string.amigo_kg_too_many_failed_attempts_countdown, index,true, TimeUtils.secToTime(secondsRemaining,mContext));
	                isFrozen=true;
	            }
	
	            @Override
	            public void onFinish() {
	            	if(DebugLog.DEBUGMAYBE) DebugLog.d(TAG, "onFinish ");
	                mLockPatternView.setEnabled(true);
	                displayDefaultSecurityMessage();
	                mCountdownTimer=null;
	                isFrozen=false;
	            }
	
	        }.start();
        }
    }
    
    private int getTimeOutSize() {
  		return mKeyguardUpdateMonitor.getFailedUnlockAttempts();
  	}
    
    @Override
    public boolean needsInput() {
        return false;
    }

    @Override
    public void onPause(int reason) {
        if (mCountdownTimer != null) {
            mCountdownTimer.cancel();
            mCountdownTimer = null;
        }
        setVisibility(INVISIBLE);
    }

    @Override
    public void onResume(int reason) {
        reset();
    }

    @Override
    public KeyguardSecurityCallback getCallback() {
        return mCallback;
    }

    @Override
    public void showBouncer(int duration) {
        KeyguardSecurityViewHelper.
                showBouncer(mSecurityMessageDisplay, mEcaView, mBouncerFrame, duration);
    }

    @Override
    public void hideBouncer(int duration) {
        KeyguardSecurityViewHelper.
                hideBouncer(mSecurityMessageDisplay, mEcaView, mBouncerFrame, duration);
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
		        mAppearAnimationUtils.startAnimation(
		                mLockPatternView.getCellStates(),
		                new Runnable() {
		                    @Override
		                    public void run() {
		                        enableClipping(true);
		                    }
		                },
		                this);
		        if (!TextUtils.isEmpty(mHelpMessage.getText())) {
		            mAppearAnimationUtils.createAnimation(mHelpMessage, 0,
		                    AppearAnimationUtils.DEFAULT_APPEAR_DURATION,
		                    mAppearAnimationUtils.getStartTranslation(),
		                    true /* appearing */,
		                    mAppearAnimationUtils.getInterpolator(),
		                    null /* finishRunnable */);
		        }
    	}
       
    }

    @Override
    public boolean startDisappearAnimation(final Runnable finishRunnable) {
        mLockPatternView.clearPattern();
        enableClipping(false);
        setTranslationY(0);
        animate()
                .setDuration(300)
                .setInterpolator(mDisappearAnimationUtils.getInterpolator())
                .translationY(-mDisappearAnimationUtils.getStartTranslation());
        mDisappearAnimationUtils.startAnimation(mLockPatternView.getCellStates(),
                new Runnable() {
                    @Override
                    public void run() {
                        enableClipping(true);
                        if (finishRunnable != null) {
                            finishRunnable.run();
                        }
                    }
                }, KeyguardPatternView.this);
        if (!TextUtils.isEmpty(mHelpMessage.getText())) {
            mDisappearAnimationUtils.createAnimation(mHelpMessage, 0,
                    200,
                    - mDisappearAnimationUtils.getStartTranslation() * 3,
                    false /* appearing */,
                    mDisappearAnimationUtils.getInterpolator(),
                    null /* finishRunnable */);
        }
        return true;
    }

    private void enableClipping(boolean enable) {
        setClipChildren(enable);
        mKeyguardBouncerFrame.setClipToPadding(enable);
        mKeyguardBouncerFrame.setClipChildren(enable);
    }

    @Override
    public void createAnimation(final LockPatternView.CellState animatedCell, long delay,
            long duration, float translationY, final boolean appearing,
            Interpolator interpolator,
            final Runnable finishListener) {
        if (appearing) {
            animatedCell.scale = 0.0f;
            animatedCell.alpha = 1.0f;
        }
        animatedCell.translateY = appearing ? translationY : 0;
        ValueAnimator animator = ValueAnimator.ofFloat(animatedCell.translateY,
                appearing ? 0 : translationY);
        animator.setInterpolator(interpolator);
        animator.setDuration(duration);
        animator.setStartDelay(delay);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedFraction = animation.getAnimatedFraction();
                if (appearing) {
                    animatedCell.scale = animatedFraction;
                } else {
                    animatedCell.alpha = 1 - animatedFraction;
                }
                animatedCell.translateY = (float) animation.getAnimatedValue();
                mLockPatternView.invalidate();
            }
        });
        if (finishListener != null) {
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    finishListener.run();
                }
            });

            // Also animate the Emergency call
            mAppearAnimationUtils.createAnimation(mEcaView, delay, duration, translationY,
                    appearing, interpolator, null);
        }
        animator.start();
        mLockPatternView.invalidate();
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

	@Override
	public void fingerPrintFailed() {
		checkPasswordResult(false, UNLOCK_FAIL_UNKNOW_REASON,null);
		
	}

	@Override
	public void fingerPrintSuccess() {
		checkPasswordResult(true, UNLOCK_FAIL_UNKNOW_REASON,null);
		
	}
	@Override
	public boolean isFrozen() {
		return isFrozen;
	}
    
}
