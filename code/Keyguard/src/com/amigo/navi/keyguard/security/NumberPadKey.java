package com.amigo.navi.keyguard.security;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.infozone.FontCache;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.PasswordTextView;
import com.android.keyguard.R;

public class NumberPadKey extends Button {
    private final static String LOG_TAG = "NumberPadKey";

    int mDigit = -1;
    int mTextViewResId;
    PasswordTextView mTextView = null;
    boolean mEnableHaptics;
    private PowerManager mPM;
    
    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View thisView) {
            DebugLog.d(LOG_TAG, "NumberPadKey onClick");
            if (mTextView == null) {
                if (mTextViewResId > 0) {
                    final View v = NumberPadKey.this.getRootView().findViewById(mTextViewResId);
                    if (v != null && v instanceof PasswordTextView) {
                        mTextView = (PasswordTextView) v;
                    }
                }
            }
            // check for time-based lockouts
            if (mTextView != null && mTextView.isEnabled()) {
                mTextView.append(Character.forDigit(mDigit, 10));
//                mTextView.append(String.valueOf(mDigit));
                DebugLog.d(LOG_TAG, "NumberPadKey password: "+mTextView.getText());
            }
            userActivity();
            doHapticKeyClick();
        }
    };

    public void userActivity() {
        mPM.userActivity(SystemClock.uptimeMillis(), false);
    }
    public NumberPadKey(Context context) {
        this(context, null);
    }

    public NumberPadKey(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NumberPadKey(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NumPadKey);
        mDigit = a.getInt(R.styleable.NumPadKey_digit, mDigit);
        setTextViewResId(a.getResourceId(R.styleable.NumPadKey_textView, 0));

        setOnClickListener(mListener);
        // setOnHoverListener(new LiftToActivateListener(context));
        // setAccessibilityDelegate(new ObscureSpeechDelegate(context));

        mEnableHaptics = new LockPatternUtils(context).isTactileFeedbackEnabled();
        mPM = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        
        Typeface mFontTypeThin = FontCache.get("font/Roboto-Thin.ttf", context);

        setTypeface(mFontTypeThin);
        
        setText(String.valueOf(mDigit));
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        // Reset the "announced headset" flag when detached.
       //  ObscureSpeechDelegate.sAnnouncedHeadset = false;
    }

    public void setTextView(PasswordTextView tv) {
        mTextView = tv;
    }

    public void setTextViewResId(int resId) {
        mTextView = null;
        mTextViewResId = resId;
    }

    // Cause a VIRTUAL_KEY vibration
    public void doHapticKeyClick() {
        if (mEnableHaptics) {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,
                    HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                            | HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        }
    }
}
