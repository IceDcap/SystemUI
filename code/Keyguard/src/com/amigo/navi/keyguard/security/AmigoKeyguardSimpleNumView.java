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

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.KeyguardViewHostManager;
import com.amigo.navi.keyguard.util.VibatorUtil;
import com.android.keyguard.AppearAnimationUtils;
import com.android.keyguard.DisappearAnimationUtils;
import com.android.keyguard.KeyguardPinBasedInputView;
import com.android.keyguard.KeyguardUpdateMonitor;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.util.Log;
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
        if (KeyguardUpdateMonitor.getInstance(mContext).getMaxBiometricUnlockAttemptsReached()) {
            mSecurityMessageDisplay.setMessage(R.string.faceunlock_multiple_failures, true);
        } else {
            mSecurityMessageDisplay.setMessage(R.string.kg_pin_instructions, false);
        }
        
        long deadline = mLockPatternUtils.getLockoutAttemptDeadline();
        if (!shouldLockout(deadline)) {
            setKeyButtonClickEnable(true);
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
                postDelayed(new Runnable() {
                    public void run() {
                        Log.d(LOG_TAG, "afterTextChanged--> password length is " + pwdLength);
                        if(pwdLength == MIN_PASSWORD_LENGTH_BEFORE_CHECKING) {
//                          mPasswordEntry.setEnabled(false);
                            setKeyButtonClickEnable(false);
//                            String entry = mPasswordEntry.getText().toString();
                            verifyPasswordAndUnlock();
                        }
                    }
                }, 100);
            }
        });
    }
    
    
    protected void verifyPasswordAndUnlock() {
        String entry = getPasswordText();
        Log.d("jing_test", "verifyPasswordAndUnlock entry: "+entry);
        if (mLockPatternUtils.checkPassword(entry)) {
            mCallback.reportUnlockAttempt(true);
            mCallback.dismiss(true);
            setKeyButtonClickEnable(true);
        } else {
            if (entry.length() == MIN_PASSWORD_LENGTH_BEFORE_CHECKING ) {
                // to avoid accidental lockout, only count attempts that are long enough to be a
                // real password. This may require some tweaking.
                mCallback.reportUnlockAttempt(false);
                int attempts = KeyguardUpdateMonitor.getInstance(mContext).getFailedUnlockAttempts();
                if (0 == (attempts % LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT)) {
                    long deadline = mLockPatternUtils.setLockoutAttemptDeadline();
                    failShake(UNLOCK_FAIL_REASON_TIMEOUT);
                    handleAttemptLockout(deadline);
                }else{
                    failShake(UNLOCK_FAIL_REASON_INCORRECT);
                    setKeyButtonClickEnable(true);
                }
            }
            int retryCount=getUnlokcRetryCount();
            if(retryCount!=0){
                mSecurityMessageDisplay.setMessage(getWrongPasswordStringId(),true,retryCount);
            }
        }
        resetPasswordText(true /* animate */);
    }
    
    private int getUnlokcRetryCount() {
        return LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT - KeyguardUpdateMonitor.getInstance(mContext).getFailedUnlockAttempts();
    }

    private void resetPinDelete(int num){
        if(num > 0){
            ((TextView)pinDelete).setText(getContext().getResources().getString(R.string.keyguard_simple_number_delete));
        }else{
            ((TextView)pinDelete).setText(getContext().getResources().getString(R.string.keyguard_simple_number_cancel));
        }
    }
    
    private void failShake(final int unLockFailReason) {
        VibatorUtil.vibator(mContext, 100);
        ObjectAnimator oan = ObjectAnimator.ofFloat(ll_images, "translationX", new float[] { 0.0F, 25.0F, -25.0F, 25.0F, -25.0F, 15.0F, -15.0F, 6.0F, -6.0F, 0.0F }).setDuration(1000);
        oan.addListener(new AnimatorListener() {
            
            @Override
            public void onAnimationStart(Animator animation) {
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
                    doHapticKeyClick();
                }
            });
            
            pinDelete.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    // check for time-based lockouts
                    if (mPasswordEntry.isEnabled()) {
                        if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "delete all digits");
                        mPasswordEntry.setText("");
                    }
                    doHapticKeyClick();
                    return true;
                }
            });
        }
    }
    
}
