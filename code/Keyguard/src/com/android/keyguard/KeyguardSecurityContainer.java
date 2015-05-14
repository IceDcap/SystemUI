/*
 * Copyright (C) 2014 The Android Open Source Project
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
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.UserHandle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Slog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.KeyguardViewHostManager;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardSecurityModel.SecurityMode;

public class KeyguardSecurityContainer extends FrameLayout implements KeyguardSecurityView {
    private static final boolean DEBUG = KeyguardConstants.DEBUG;
    private static final String TAG = "KeyguardSecurityView";

    private static final int USER_TYPE_PRIMARY = 1;
    private static final int USER_TYPE_WORK_PROFILE = 2;
    private static final int USER_TYPE_SECONDARY_USER = 3;

    private KeyguardSecurityModel mSecurityModel;
    private boolean mEnableFallback; // TODO: This should get the value from KeyguardPatternView
    private LockPatternUtils mLockPatternUtils;

    private KeyguardSecurityViewFlipper mSecurityViewFlipper;
    private boolean mIsVerifyUnlockOnly;
    private SecurityMode mCurrentSecuritySelection = SecurityMode.Invalid;
    private boolean mIsBouncing;
    private SecurityCallback mSecurityCallback;

    private final KeyguardUpdateMonitor mUpdateMonitor;
    private int mReason=-1;
    
    private  int mFingerPrintResult=FINGERPRINT_UNUSED;
    public static final int FINGERPRINT_UNUSED=-1;
    public static final int FINGERPRINT_FAILED=0;
    public static final int FINGERPRINT_SUCCESS=1;
    private SecurityViewRemoveCallback mSecurityViewRemoveCallback;

    // Used to notify the container when something interesting happens.
    public interface SecurityCallback {
        public boolean dismiss(boolean authenticated);
        public void userActivity();
        public void onSecurityModeChanged(SecurityMode securityMode, boolean needsInput);
        public void finish();
    }

    public KeyguardSecurityContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyguardSecurityContainer(Context context) {
        this(context, null, 0);
    }

    public KeyguardSecurityContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mSecurityModel = new KeyguardSecurityModel(context);
        mLockPatternUtils = new LockPatternUtils(context);
        mUpdateMonitor = KeyguardUpdateMonitor.getInstance(mContext);
    }

    public void setSecurityCallback(SecurityCallback callback) {
        mSecurityCallback = callback;
    }

    @Override
    public void onResume(int reason) {
        mReason=reason;
        if (mCurrentSecuritySelection != SecurityMode.None) {
            getSecurityView(mCurrentSecuritySelection).onResume(reason);
            final View currentSecurityView = mSecurityViewFlipper.getCurrentView();
//            currentSecurityView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause(int reason) {
        mReason=reason;
        if (mCurrentSecuritySelection != SecurityMode.None) {
            getSecurityView(mCurrentSecuritySelection).onPause(reason);

        }
    }

    public void startAppearAnimation() {
        if (mCurrentSecuritySelection != SecurityMode.None) {
            getSecurityView(mCurrentSecuritySelection).startAppearAnimation();
        }
    }

    public boolean startDisappearAnimation(Runnable onFinishRunnable) {
        if (mCurrentSecuritySelection != SecurityMode.None) {
            return getSecurityView(mCurrentSecuritySelection).startDisappearAnimation(
                    onFinishRunnable);
        }
        return false;
    }

    void updateSecurityViews(boolean isBouncing) {
        int children = mSecurityViewFlipper.getChildCount();
        for (int i = 0; i < children; i++) {
            updateSecurityView(mSecurityViewFlipper.getChildAt(i), isBouncing);
        }
    }

    public void announceCurrentSecurityMethod() {
        View v = (View) getSecurityView(mCurrentSecuritySelection);
        if (v != null) {
            v.announceForAccessibility(v.getContentDescription());
        }
    }

    public CharSequence getCurrentSecurityModeContentDescription() {
        View v = (View) getSecurityView(mCurrentSecuritySelection);
        if (v != null) {
            return v.getContentDescription();
        }
        return "";
    }

    private KeyguardSecurityView getSecurityView(SecurityMode securityMode) {
        final int securityViewIdForMode = getSecurityViewIdForMode(securityMode);
        KeyguardSecurityView view = null;
        final int children = mSecurityViewFlipper.getChildCount();
        for (int child = 0; child < children; child++) {
            if (mSecurityViewFlipper.getChildAt(child).getId() == securityViewIdForMode) {
                view = ((KeyguardSecurityView)mSecurityViewFlipper.getChildAt(child));
                break;
            }
        }
        int layoutId = getLayoutIdFor(securityMode);
        if (view == null && layoutId != 0) {
            final LayoutInflater inflater = LayoutInflater.from(mContext);
            if (DEBUG) Log.v(TAG, "inflating id = " + layoutId);
            View v = inflater.inflate(layoutId, mSecurityViewFlipper, false);
            mSecurityViewFlipper.addView(v);
            updateSecurityView(v, mIsBouncing);
            view = (KeyguardSecurityView)v;
        }

        return view;
    }

    private void updateSecurityView(View view, boolean isBouncing) {
        mIsBouncing = isBouncing;
        if (view instanceof KeyguardSecurityView) {
            KeyguardSecurityView ksv = (KeyguardSecurityView) view;
            ksv.setKeyguardCallback(mCallback);
            ksv.setLockPatternUtils(mLockPatternUtils);
            if (isBouncing) {
                ksv.showBouncer(0);
            } else {
                ksv.hideBouncer(0);
            }
        } else {
            Log.w(TAG, "View " + view + " is not a KeyguardSecurityView");
        }
    }

    protected void onFinishInflate() {
        mSecurityViewFlipper = (KeyguardSecurityViewFlipper) findViewById(R.id.view_flipper);
        mSecurityViewFlipper.setLockPatternUtils(mLockPatternUtils);
    }

    public void setLockPatternUtils(LockPatternUtils utils) {
        mLockPatternUtils = utils;
        mSecurityModel.setLockPatternUtils(utils);
        mSecurityViewFlipper.setLockPatternUtils(mLockPatternUtils);
    }

    private void showDialog(String title, String message) {
        final AlertDialog dialog = new AlertDialog.Builder(mContext)
            .setTitle(title)
            .setMessage(message)
            .setNeutralButton(R.string.ok, null)
            .create();
        if (!(mContext instanceof Activity)) {
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        }
        dialog.show();
    }

    private void showTimeoutDialog() {
        int timeoutInSeconds = (int) LockPatternUtils.FAILED_ATTEMPT_TIMEOUT_MS / 1000;
        int messageId = 0;

        switch (mSecurityModel.getSecurityMode()) {
            case Pattern:
                messageId = R.string.kg_too_many_failed_pattern_attempts_dialog_message;
                break;
            case PIN:
                messageId = R.string.kg_too_many_failed_simple_attempts_dialog_message;
                break;
            case Password:
                messageId = R.string.kg_too_many_failed_mixed_password_attempts_dialog_message;
                break;
            // These don't have timeout dialogs.
            case Account:
            case Biometric:
            case Invalid:
            case None:
            case SimPin:
            case SimPuk:
                break;
        }

        if (messageId != 0) {
            final String message = mContext.getString(messageId,
                    KeyguardUpdateMonitor.getInstance(mContext).getFailedUnlockAttempts(),
                    timeoutInSeconds);
            showDialog(null, message);
        }
    }

    private void showAlmostAtWipeDialog(int attempts, int remaining, int userType) {
        String message = null;
        switch (userType) {
            case USER_TYPE_PRIMARY:
                message = mContext.getString(R.string.kg_failed_attempts_almost_at_wipe,
                        attempts, remaining);
                break;
            case USER_TYPE_SECONDARY_USER:
                message = mContext.getString(R.string.kg_failed_attempts_almost_at_erase_user,
                        attempts, remaining);
                break;
            case USER_TYPE_WORK_PROFILE:
                message = mContext.getString(R.string.kg_failed_attempts_almost_at_erase_profile,
                        attempts, remaining);
                break;
        }
        showDialog(null, message);
    }

    private void showWipeDialog(int attempts, int userType) {
        String message = null;
        switch (userType) {
            case USER_TYPE_PRIMARY:
                message = mContext.getString(R.string.kg_failed_attempts_now_wiping,
                        attempts);
                break;
            case USER_TYPE_SECONDARY_USER:
                message = mContext.getString(R.string.kg_failed_attempts_now_erasing_user,
                        attempts);
                break;
            case USER_TYPE_WORK_PROFILE:
                message = mContext.getString(R.string.kg_failed_attempts_now_erasing_profile,
                        attempts);
                break;
        }
        showDialog(null, message);
    }

    private void showAlmostAtAccountLoginDialog() {
        final int timeoutInSeconds = (int) LockPatternUtils.FAILED_ATTEMPT_TIMEOUT_MS / 1000;
        final int count = LockPatternUtils.FAILED_ATTEMPTS_BEFORE_RESET
                - LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT;
        String message = mContext.getString(R.string.kg_failed_attempts_almost_at_login,
                count, LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT, timeoutInSeconds);
        showDialog(null, message);
    }

    private void reportFailedUnlockAttempt() {
        final KeyguardUpdateMonitor monitor = KeyguardUpdateMonitor.getInstance(mContext);
        final int failedAttempts = monitor.getFailedUnlockAttempts() + 1; // +1 for this time

        if (DEBUG) Log.d(TAG, "reportFailedPatternAttempt: #" + failedAttempts);

        SecurityMode mode = mSecurityModel.getSecurityMode();
        final boolean usingPattern = mode == KeyguardSecurityModel.SecurityMode.Pattern;
        final int currentUser = mLockPatternUtils.getCurrentUser();
        final DevicePolicyManager dpm = mLockPatternUtils.getDevicePolicyManager();
        final int failedAttemptsBeforeWipe =
                dpm.getMaximumFailedPasswordsForWipe(null, currentUser);

        final int failedAttemptWarning = LockPatternUtils.FAILED_ATTEMPTS_BEFORE_RESET
                - LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT;

        final int remainingBeforeWipe = failedAttemptsBeforeWipe > 0 ?
                (failedAttemptsBeforeWipe - failedAttempts)
                : Integer.MAX_VALUE; // because DPM returns 0 if no restriction
        boolean showTimeout = false;
        if (remainingBeforeWipe < LockPatternUtils.FAILED_ATTEMPTS_BEFORE_WIPE_GRACE) {
            // The user has installed a DevicePolicyManager that requests a user/profile to be wiped
            // N attempts. Once we get below the grace period, we post this dialog every time as a
            // clear warning until the deletion fires.
            // Check which profile has the strictest policy for failed password attempts
            final int expiringUser = dpm.getProfileWithMinimumFailedPasswordsForWipe(currentUser);
            int userType = USER_TYPE_PRIMARY;
            if (expiringUser == currentUser) {
                if (expiringUser != UserHandle.USER_OWNER) {
                    userType = USER_TYPE_SECONDARY_USER;
                }
            } else if (expiringUser != UserHandle.USER_NULL) {
                userType = USER_TYPE_WORK_PROFILE;
            } // If USER_NULL, which shouldn't happen, leave it as USER_TYPE_PRIMARY
            if (remainingBeforeWipe > 0) {
                showAlmostAtWipeDialog(failedAttempts, remainingBeforeWipe, userType);
            } else {
                // Too many attempts. The device will be wiped shortly.
                Slog.i(TAG, "Too many unlock attempts; user " + expiringUser + " will be wiped!");
                showWipeDialog(failedAttempts, userType);
            }
        } else {
            showTimeout =
                failedAttempts >= LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT;
            if (usingPattern && mEnableFallback) {
                if (failedAttempts == failedAttemptWarning) {
                    showAlmostAtAccountLoginDialog();
                    showTimeout = false; // don't show both dialogs
                } else if (failedAttempts >= LockPatternUtils.FAILED_ATTEMPTS_BEFORE_RESET) {
                    mLockPatternUtils.setPermanentlyLocked(true);
                    showSecurityScreen(SecurityMode.Account);
                    // don't show timeout dialog because we show account unlock screen next
                    showTimeout = false;
                }
            }
        }
        monitor.reportFailedUnlockAttempt();
        mLockPatternUtils.reportFailedPasswordAttempt();
        //jiating modify for keyguard begin
       if (showTimeout) {
//            showTimeoutDialog();
    	   monitor.clearFailedUnlockAttempts(false);
        }
      //jiating modify for keyguard end
    }

    /**
     * Shows the primary security screen for the user. This will be either the multi-selector
     * or the user's security method.
     * @param turningOff true if the device is being turned off
     */
    void showPrimarySecurityScreen(boolean turningOff) {
        SecurityMode securityMode = mSecurityModel.getSecurityMode();
        if (DEBUG) Log.v(TAG, "showPrimarySecurityScreen(turningOff=" + turningOff + ")");
        if (!turningOff &&
                KeyguardUpdateMonitor.getInstance(mContext).isAlternateUnlockEnabled()) {
            // If we're not turning off, then allow biometric alternate.
            // We'll reload it when the device comes back on.
            securityMode = mSecurityModel.getAlternateFor(securityMode);
        }
        showSecurityScreen(securityMode);
    }

    /**
     * Shows the backup security screen for the current security mode.  This could be used for
     * password recovery screens but is currently only used for pattern unlock to show the
     * account unlock screen and biometric unlock to show the user's normal unlock.
     */
    private void showBackupSecurityScreen() {
        if (DEBUG) Log.d(TAG, "showBackupSecurity()");
        SecurityMode backup = mSecurityModel.getBackupSecurityMode(mCurrentSecuritySelection);
        showSecurityScreen(backup);
    }

    /**
     * Shows the next security screen if there is one.
     * @param authenticated true if the user entered the correct authentication
     * @param authenticated
     * @return true if keyguard is done
     */
    boolean showNextSecurityScreenOrFinish(boolean authenticated) {
        if (DEBUG) Log.d(TAG, "showNextSecurityScreenOrFinish(" + authenticated + ") mCurrentSecuritySelection: "+mCurrentSecuritySelection);
        boolean finish = false;
        if (mUpdateMonitor.getUserHasTrust(mLockPatternUtils.getCurrentUser())) {
            finish = true;
        } else if (SecurityMode.None == mCurrentSecuritySelection) {
            SecurityMode securityMode = mSecurityModel.getSecurityMode();
            // Allow an alternate, such as biometric unlock
            securityMode = mSecurityModel.getAlternateFor(securityMode);
            if (DEBUG) Log.d(TAG, "mCurrentSecuritySelection None getAlternateFor:"+securityMode);
            if (SecurityMode.None == securityMode) {
                finish = true; // no security required
            } else {
                showSecurityScreen(securityMode); // switch to the alternate security view
            }
        } else if (authenticated) {
            switch (mCurrentSecuritySelection) {
                case Pattern:
                case Password:
                case PIN:
                case Account:
                case Biometric:
                    finish = true;
                    break;

                case SimPin:
                case SimPuk:
                    // Shortcut for SIM PIN/PUK to go to directly to user's security screen or home
                    SecurityMode securityMode = mSecurityModel.getSecurityMode();
                    if (securityMode != SecurityMode.None) {
                        showSecurityScreen(securityMode);
                    } else {
                        finish = true;
                    }
                    break;

                default:
                    Log.v(TAG, "Bad security screen " + mCurrentSecuritySelection + ", fail safe");
                    showPrimarySecurityScreen(false);
                    break;
            }
        }
        if (DEBUG) Log.d(TAG, "showNextSecurityScreenOrFinish  finish:" + finish);
        if (finish) {
        	// unlock animation
        	runRemovewSecurityViewAnimation();
        }else{
            //jingyn <2015-05-13> add for CR01478984 begin
            SecurityMode securityMode = mSecurityModel.getSecurityMode();
            if(!(securityMode==SecurityMode.SimPin|| securityMode == SecurityMode.SimPuk)){
                KeyguardViewHostManager.getInstance().startFingerIdentify();
            }
            //jingyn <2015-05-13> add for CR01478984 end
        }
        return finish;
    }
    
    // guosb add for removeSecuityView animation begin
    
    private boolean animationIsRunning = false;
	private ValueAnimator mSecurityViewRemoveValueAnimator= null;

	@SuppressLint("NewApi")
	public void runRemovewSecurityViewAnimation() {
		if(DEBUG){
			DebugLog.d(TAG, "removeSecurityView");
		}
		if(mSecurityViewRemoveValueAnimator != null && mSecurityViewRemoveValueAnimator.isRunning()){
			if(DebugLog.DEBUG){
				DebugLog.d(TAG, "removeSecurityView mSecurityViewRemoveValueAnimator is running");
			}
			return;
		}
		if(mSecurityViewFlipper == null){
			return;
		}
		final View currentSecurityView = mSecurityViewFlipper.getCurrentView();
		
		if(currentSecurityView == null){
			return;
		}

		final int h = currentSecurityView.getMeasuredHeight();
		if(DebugLog.DEBUG){
			DebugLog.d(TAG, "removeSecurityView mSecurityViewRemoveValueAnimatorh="+h);
		}
		
		mSecurityViewRemoveValueAnimator = ValueAnimator.ofFloat(0,1);
		mSecurityViewRemoveValueAnimator.setDuration(300);
		mSecurityViewRemoveValueAnimator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float f = animation.getAnimatedFraction();
				currentSecurityView.setTranslationY(-h*f);
				currentSecurityView.setAlpha(1-f);
				mSecurityViewRemoveCallback.executeRemoveAmimation((int) (h*f));
			}
		});
		mSecurityViewRemoveValueAnimator.addListener(new AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				//remove view before keyguardDone instead of postDelay
				mSecurityCallback.finish();
				resetSecurityView(currentSecurityView, h);
				currentSecurityView.setVisibility(View.INVISIBLE);
				mSecurityViewRemoveValueAnimator = null;
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				mSecurityCallback.finish();
				resetSecurityView(currentSecurityView, h);
				currentSecurityView.setVisibility(View.INVISIBLE);
				mSecurityViewRemoveValueAnimator = null;
			}
		});
		mSecurityViewRemoveValueAnimator.start();
	}
	
	
	 private void resetSecurityView(View currentSecurityView ,int  h) {
		   currentSecurityView.setTranslationY(0);
		   currentSecurityView.setAlpha(1f);
	 }
	
	// guosb add for removeSecuityView animation end

    /**
     * Switches to the given security view unless it's already being shown, in which case
     * this is a no-op.
     *
     * @param securityMode
     */
    private void showSecurityScreen(SecurityMode securityMode) {
        if (DEBUG) Log.d(TAG, "showSecurityScreen(" + securityMode + ")");

        if (securityMode == mCurrentSecuritySelection) return;

        KeyguardSecurityView oldView = getSecurityView(mCurrentSecuritySelection);
        KeyguardSecurityView newView = getSecurityView(securityMode);

        // Emulate Activity life cycle
        if (oldView != null) {
            oldView.onPause(KeyguardSecurityView.NONE);
            oldView.setKeyguardCallback(mNullCallback); // ignore requests from old view
        }
        if (securityMode != SecurityMode.None) {
            newView.onResume(KeyguardSecurityView.VIEW_REVEALED);
            newView.setKeyguardCallback(mCallback);
        }

        // Find and show this child.
        final int childCount = mSecurityViewFlipper.getChildCount();

        final int securityViewIdForMode = getSecurityViewIdForMode(securityMode);
        for (int i = 0; i < childCount; i++) {
            if (mSecurityViewFlipper.getChildAt(i).getId() == securityViewIdForMode) {
                 mSecurityViewFlipper.setDisplayedChild(i);
                 //jingyn add for set security view  visible begin
                 if(!isPinPukViewId(securityViewIdForMode)){
                     if(mReason==KeyguardSecurityView.KEYGUARD_HOSTVIEW_SCROLL_AT_HOMEPAGE){
                         mSecurityViewFlipper.getChildAt(i).setVisibility(View.INVISIBLE);
                     }
                 }
                 //jingyn add for set security view  visible end
                break;
            }
        }
        
        // guosb add for sliding lock begin 
        if(securityMode == SecurityMode.None){
        	mSecurityViewFlipper.removeAllViews();
        }
        // guosb add for sliding lock end 

        mCurrentSecuritySelection = securityMode;
        mSecurityCallback.onSecurityModeChanged(securityMode,
                securityMode != SecurityMode.None && newView.needsInput());
    }

    private KeyguardSecurityViewFlipper getFlipper() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof KeyguardSecurityViewFlipper) {
                return (KeyguardSecurityViewFlipper) child;
            }
        }
        return null;
    }

    public void showBouncer(int duration) {
        KeyguardSecurityViewFlipper flipper = getFlipper();
        if (flipper != null) {
            flipper.showBouncer(duration);
        }
    }

    public void hideBouncer(int duration) {
        KeyguardSecurityViewFlipper flipper = getFlipper();
        if (flipper != null) {
            flipper.hideBouncer(duration);
        }
    }

    private KeyguardSecurityCallback mCallback = new KeyguardSecurityCallback() {

        public void userActivity() {
            if (mSecurityCallback != null) {
                mSecurityCallback.userActivity();
            }
        }

        public void dismiss(boolean authenticated) {
            mSecurityCallback.dismiss(authenticated);
        }

        public boolean isVerifyUnlockOnly() {
            return mIsVerifyUnlockOnly;
        }

        public void reportUnlockAttempt(boolean success) {
            KeyguardUpdateMonitor monitor = KeyguardUpdateMonitor.getInstance(mContext);
            if (success) {
                monitor.clearFailedUnlockAttempts(true);
                mLockPatternUtils.reportSuccessfulPasswordAttempt();
            } else {
                if (mCurrentSecuritySelection == SecurityMode.Biometric) {
                    monitor.reportFailedBiometricUnlockAttempt();
                } else {
                    KeyguardSecurityContainer.this.reportFailedUnlockAttempt();
                }
            }
        }

        @Override
        public void showBackupSecurity() {
            KeyguardSecurityContainer.this.showBackupSecurityScreen();
        }

		@Override
		public int getFingerPrintResult() {
			// TODO Auto-generated method stub
			return KeyguardSecurityContainer.this.getFingerPrintResult();
		}

		@Override
		public void reset() {
			KeyguardSecurityContainer.this.setFingerPrintResult(FINGERPRINT_UNUSED);
			
		}

    };
    
	public long getLockoutAttemptDeadline() {
	    KeyguardUpdateMonitor monitor = KeyguardUpdateMonitor.getInstance(mContext);
		return monitor.getDeadline();
	}

    // The following is used to ignore callbacks from SecurityViews that are no longer current
    // (e.g. face unlock). This avoids unwanted asynchronous events from messing with the
    // state for the current security method.
    private KeyguardSecurityCallback mNullCallback = new KeyguardSecurityCallback() {
        @Override
        public void userActivity() { }
        @Override
        public void showBackupSecurity() { }
        @Override
        public void reportUnlockAttempt(boolean success) { }
        @Override
        public boolean isVerifyUnlockOnly() { return false; }
        @Override
        public void dismiss(boolean securityVerified) { }
		@Override
		public int getFingerPrintResult() {
			return 0;
		}
		@Override
		public void reset() {
			
		}
    };

    private int getSecurityViewIdForMode(SecurityMode securityMode) {
        switch (securityMode) {
            case Pattern: return R.id.amigo_keyguard_pattern_view;
            case PIN: return R.id.amigo_keyguard_pin_view;
            case Password: return R.id.amigo_keyguard_password_view;
            case Biometric: return R.id.keyguard_face_unlock_view;
            case Account: return R.id.keyguard_account_view;
            case SimPin: return R.id.amigo_keyguard_sim_pin_view;
            case SimPuk: return R.id.amigo_keyguard_sim_puk_view;
        }
        return 0;
    }

    private int getLayoutIdFor(SecurityMode securityMode) {
        switch (securityMode) {
            case Pattern: return R.layout.amigo_keyguard_pattern_view;
            case PIN: return R.layout.amigo_keyguard_pin_view;
            case Password: return R.layout.amigo_keyguard_password_view;
            case Biometric: return R.layout.keyguard_face_unlock_view;
            case Account: return R.layout.keyguard_account_view;
            case SimPin: return R.layout.amigo_keyguard_sim_pin_view;
            case SimPuk: return R.layout.amigo_keyguard_sim_puk_view;
            default:
                return 0;
        }
    }

    public SecurityMode getSecurityMode() {
        return mSecurityModel.getSecurityMode();
    }

    public SecurityMode getCurrentSecurityMode() {
        return mCurrentSecuritySelection;
    }

    public void verifyUnlock() {
        mIsVerifyUnlockOnly = true;
        showSecurityScreen(getSecurityMode());
    }

    public SecurityMode getCurrentSecuritySelection() {
        return mCurrentSecuritySelection;
    }

    public void dismiss(boolean authenticated) {
        mCallback.dismiss(authenticated);
    }

    public boolean needsInput() {
        return mSecurityViewFlipper.needsInput();
    }

    @Override
    public void setKeyguardCallback(KeyguardSecurityCallback callback) {
        mSecurityViewFlipper.setKeyguardCallback(callback);
    }

    @Override
    public void reset() {
        mSecurityViewFlipper.reset();
    }

    @Override
    public KeyguardSecurityCallback getCallback() {
        return mSecurityViewFlipper.getCallback();
    }

    @Override
    public void showUsabilityHint() {
        mSecurityViewFlipper.showUsabilityHint();
    }
    
    
    public void fingerPrintFailed(){
    	mFingerPrintResult=FINGERPRINT_FAILED;
    	if (mCurrentSecuritySelection != SecurityMode.None) {
            getSecurityView(mCurrentSecuritySelection).fingerPrintFailed();
        }
    }
    
    public void fingerPrintSuccess(){
    	mFingerPrintResult=FINGERPRINT_SUCCESS;;
    	if (mCurrentSecuritySelection != SecurityMode.None) {
            getSecurityView(mCurrentSecuritySelection).fingerPrintSuccess();
        }
    }

	public  int getFingerPrintResult() {
		return mFingerPrintResult;
	}

	public  void setFingerPrintResult(int fingerPrintResult) {
		this.mFingerPrintResult = fingerPrintResult;
	}
    
    @Override
    public boolean isFrozen() {
    	if (mCurrentSecuritySelection != SecurityMode.None) {
            return getSecurityView(mCurrentSecuritySelection).isFrozen();
        }
    	return false;
    }
    
    public boolean isPinPukViewId(int viewId) {
        if (viewId == R.layout.amigo_keyguard_sim_pin_view || viewId == R.layout.amigo_keyguard_sim_puk_view) {
            return true;
        }
        return false;
    }

  //GIONEE <Amigo_Keyguard>  jiating <2015-05-12> modify for keyguardBg change when excute SecurityView animation begin
    public interface SecurityViewRemoveCallback {

        public void executeRemoveAmimation(int top);
    }
    
    public void setSecurityViewRemoveCallback(SecurityViewRemoveCallback  callback){
    	mSecurityViewRemoveCallback=callback;
    }
    
    //GIONEE <Amigo_Keyguard>  jiating <2015-05-12> modify for keyguardBg change when excute SecurityView animation end

}

