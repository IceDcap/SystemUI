
package com.amigo.navi.keyguard.haokan;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amigo.navi.keyguard.Guide;
import com.amigo.navi.keyguard.KeyguardViewHost;
import com.android.keyguard.R;

public class GuideLondPressLayout extends RelativeLayout {

    private Bitmap blurBackground;

    private Paint mBlurBackgroundPaint = new Paint();

    private int mAlpha = 0;

    private View hand;
    private LongPressView longPressView;
    private TextView mTextView;
    private CloseView mButtonClose;
    
    private UIController controller;
    
    private AnimatorSet longPressAnimatorSet;
    
    CloseView myButton;

    public GuideLondPressLayout(Context context) {
        this(context, null);
    }

    public GuideLondPressLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GuideLondPressLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public GuideLondPressLayout(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        controller = UIController.getInstance();
    }
    
 
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        hand = findViewById(R.id.hand);
        longPressView = (LongPressView) findViewById(R.id.LongPressView);
        mTextView = (TextView) findViewById(R.id.guide_long_press_tip);
        mButtonClose =  (CloseView) findViewById(R.id.guide_long_press_close);
       
        mButtonClose.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View arg0) {
                stopGuide();
            }
        });
    }

   

    @Override
    protected void dispatchDraw(Canvas canvas) {

        if (blurBackground != null) {
            canvas.drawBitmap(blurBackground, 0, 0, mBlurBackgroundPaint);

            canvas.drawColor(Color.argb((int) (mAlpha * 0.9f), 0, 0, 0));
        }

        super.dispatchDraw(canvas);
    }

    
    
    public void startGuide() {
        
        controller.setGuideLongPressShowing(true);
        createShowAnimator().start();
        longPressAnimatorSet = new AnimatorSet();
        
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0.1f, 1.0f);
        
        float fromTranslationY = getResources().getDimensionPixelSize(R.dimen.guide_long_press_hand_translationY);
        
        PropertyValuesHolder translationY = PropertyValuesHolder.ofFloat("translationY", fromTranslationY, 0f);
        ObjectAnimator handUpAnimator = ObjectAnimator.ofPropertyValuesHolder(hand, alpha, translationY).setDuration(800);

        Animator animatorExpand = longPressView.createExpandAnimator();

        Animator animatorShrink = longPressView.createShrinkAnimator();
        
        
        PropertyValuesHolder alpha2 = PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.1f);
        PropertyValuesHolder translationY2 = PropertyValuesHolder.ofFloat("translationY", 0f, fromTranslationY);
        ObjectAnimator handDownAnimator = ObjectAnimator.ofPropertyValuesHolder(hand, alpha2, translationY2).setDuration(800);
        
        longPressAnimatorSet.play(handUpAnimator);
        longPressAnimatorSet.play(animatorExpand).after(handUpAnimator);
        longPressAnimatorSet.play(animatorShrink).with(handDownAnimator).after(animatorExpand);

        longPressAnimatorSet.addListener(new AnimatorListener() {

            private boolean cancel = false;
            
            @Override
            public void onAnimationStart(Animator arg0) {

            }

            @Override
            public void onAnimationRepeat(Animator arg0) {

            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                if (!cancel) {
                    longPressAnimatorSet.start();
                }
                cancel = false;
            }

            @Override
            public void onAnimationCancel(Animator arg0) {
                cancel = true;
            }
        });
        longPressAnimatorSet.start();
        
        
        
        ObjectAnimator closeAnimator = ObjectAnimator.ofFloat(mButtonClose, "alpha", 1.0f).setDuration(500);

        PropertyValuesHolder alphaText = PropertyValuesHolder.ofFloat("alpha", 1.0f);
        PropertyValuesHolder translationYText = PropertyValuesHolder.ofFloat("translationY", 150f,
                0f);
        ObjectAnimator textAnimator = ObjectAnimator.ofPropertyValuesHolder(mTextView, alphaText,
                translationYText).setDuration(500);

        textAnimator.setStartDelay(800);
        closeAnimator.setStartDelay(800);
        closeAnimator.start();
        textAnimator.start();
        
    }

    public void stopGuide() {
        longPressAnimatorSet.cancel();
        createHideAnimator().start();
    }

    public Animator createShowAnimator() {

        ValueAnimator animator = ValueAnimator.ofInt(0, 255).setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                mAlpha = (int) animation.getAnimatedValue();
                mBlurBackgroundPaint.setAlpha(mAlpha);
                
                invalidate();
            }
        });
        animator.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator arg0) {
                setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {

            }

            @Override
            public void onAnimationEnd(Animator arg0) {

            }

            @Override
            public void onAnimationCancel(Animator arg0) {

            }
        });
        return animator;
    }

    public Animator createHideAnimator() {

        ValueAnimator animator = ValueAnimator.ofInt(mAlpha, 0).setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                mAlpha = (int) animation.getAnimatedValue();
                mBlurBackgroundPaint.setAlpha(mAlpha);
                invalidate();
                
                float fraction = 1.0f - animation.getAnimatedFraction();
                mTextView.setAlpha(fraction);
                mButtonClose.setAlpha(fraction);
                hand.setAlpha(fraction);
            }
        });
        animator.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator arg0) {
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {

            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                remove();
            }

            @Override
            public void onAnimationCancel(Animator arg0) {

            }
        });
        return animator;
    }
    
    public void remove() {
        
        final KeyguardViewHost hostView = controller.getmKeyguardViewHost();
        if (hostView.indexOfChild(this) != -1) {
            hostView.removeView(this);
        }
        if (longPressAnimatorSet != null) {
            longPressAnimatorSet.cancel();
        }
        controller.setGuideLongPressShowing(false);
        controller.getmKeyguardNotification().setAlpha(1.0f);
        Guide.setNeedGuideLongPress(false);
        Guide.setBooleanSharedConfig(getContext(), Guide.GUIDE_LONG_PRESS, false);
    }
    

    public Bitmap getBlurBackground() {
        return blurBackground;
    }

    public void setBlurBackground(Bitmap blurBackground) {
        this.blurBackground = blurBackground;
    }
    
 
}
