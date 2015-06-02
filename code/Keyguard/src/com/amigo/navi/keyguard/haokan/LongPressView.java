
package com.amigo.navi.keyguard.haokan;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.android.keyguard.R;

public class LongPressView extends View {
    
    
    private Paint mPaintFirst;
    private Paint mPaintSecond;
    private Paint mPaintThird;
    
    private float STROKE = 10;
    
    
    private float mMaxmRadiusFirst = 50;
    private float mFromRadiusFirst = 5;
    
    private float mMaxmRadiusSecond = 50;
    private float mFromRadiusSecond = 30;
    
    private float mMaxmRadiusThird = 100;
    private float mFromRadiusThird;
    
    
    private float mRadiusFirst = 0;
    private float mRadiusSecond = 0;
    private float mRadiusThird = 0;
    
    private int DEFAULT_ALPHA_FIRST = (int) (255 * 0.5f);
    private int DEFAULT_ALPHA_SECOND = (int) (255 * 0.20f);
    private int DEFAULT_ALPHA_THIRD = (int) (255 * 0.10f);
    
    
    public LongPressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LongPressView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }
   
    public LongPressView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        
        STROKE = getResources().getDimensionPixelSize(R.dimen.guide_long_press_stroke);
        mMaxmRadiusFirst = getResources().getDimensionPixelSize(R.dimen.guide_long_press_firstcircle_maxradius);
        mMaxmRadiusSecond = getResources().getDimensionPixelSize(R.dimen.guide_long_press_secondcircle_maxradius);
        mMaxmRadiusThird = getResources().getDimensionPixelSize(R.dimen.guide_long_press_thirdcircle_maxradius);
        mFromRadiusThird = mMaxmRadiusSecond;
        
        mPaintFirst = new Paint();
        mPaintFirst.setAntiAlias(true);
        mPaintFirst.setStyle(Paint.Style.FILL);
        mPaintFirst.setColor(0xffffffff);
        mPaintFirst.setAlpha(DEFAULT_ALPHA_FIRST);
        
        mPaintSecond = new Paint();
        mPaintSecond.setAntiAlias(true);
        mPaintSecond.setStyle(Paint.Style.FILL);
        mPaintSecond.setStrokeWidth(STROKE);
        mPaintSecond.setColor(0xffffffff);
        mPaintSecond.setAlpha(DEFAULT_ALPHA_SECOND);
        
        
        mPaintThird = new Paint();
        mPaintThird.setAntiAlias(true);
        mPaintThird.setStyle(Paint.Style.STROKE);
        mPaintThird.setStrokeWidth(STROKE);
        mPaintThird.setColor(0xffffffff);
        mPaintThird.setAlpha(DEFAULT_ALPHA_THIRD);
        
        
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        
    }
    
    public Animator createExpandAnimator() {

        
        
        AnimatorSet set = new AnimatorSet();
        
        ValueAnimator animator = ValueAnimator.ofFloat(mFromRadiusFirst, mMaxmRadiusFirst).setDuration(250);  
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new AnimatorUpdateListener() {  
            @Override  
            public void onAnimationUpdate(ValueAnimator animation) {  
                
                float Value = (Float) animation.getAnimatedValue();
                mRadiusFirst = Value;
                 
                invalidate();
            }  
        });  
        
        ValueAnimator animatorSecond = ValueAnimator.ofFloat(0, mMaxmRadiusSecond).setDuration(300);  
        animatorSecond.setInterpolator(new DecelerateInterpolator());
        animatorSecond.addUpdateListener(new AnimatorUpdateListener() {  
            @Override  
            public void onAnimationUpdate(ValueAnimator animation) {  
                
                float Value = (Float) animation.getAnimatedValue();
                mRadiusSecond = Value;
                
                invalidate();
            }  
        });  
        animatorSecond.setStartDelay(200);
        
        ValueAnimator animatorThird = ValueAnimator.ofFloat(mFromRadiusThird,mMaxmRadiusThird).setDuration(200);  
        animatorThird.setInterpolator(new DecelerateInterpolator());
        animatorThird.addUpdateListener(new AnimatorUpdateListener() {  
            @Override  
            public void onAnimationUpdate(ValueAnimator animation) {  
                
                float Value = (Float) animation.getAnimatedValue();
                mRadiusThird = Value;
                invalidate();
            }  
        });  
        set.play(animator).with(animatorSecond);
        set.play(animatorThird).after(animatorSecond);
        return set;
    }
    
    
    public Animator createShrinkAnimator() {

        AnimatorSet set = new AnimatorSet();
        
        ValueAnimator animator = ValueAnimator.ofFloat(mMaxmRadiusFirst, 0).setDuration(150);  
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new AnimatorUpdateListener() {  
            @Override  
            public void onAnimationUpdate(ValueAnimator animation) {  
                
                float Value = (Float) animation.getAnimatedValue();
                mRadiusFirst = Value;
                mPaintFirst.setAlpha((int) (DEFAULT_ALPHA_FIRST * (1.0f - animation.getAnimatedFraction())));
                invalidate();
            }  
        });  
        
        ValueAnimator animatorSecond = ValueAnimator.ofFloat(mMaxmRadiusSecond, 0).setDuration(170);  
        animatorSecond.setInterpolator(new AccelerateInterpolator());
        animatorSecond.addUpdateListener(new AnimatorUpdateListener() {  
            @Override  
            public void onAnimationUpdate(ValueAnimator animation) {  
                
                float Value = (Float) animation.getAnimatedValue();
                mRadiusSecond = Value;
                mPaintSecond.setAlpha((int) (DEFAULT_ALPHA_SECOND * (1.0f - animation.getAnimatedFraction())));
                invalidate();
            }  
        });  
        animatorSecond.setStartDelay(130);
        
        ValueAnimator animatorThird = ValueAnimator.ofFloat(mMaxmRadiusThird,mFromRadiusThird).setDuration(80);  
        animatorThird.setInterpolator(new AccelerateInterpolator());
        animatorThird.addUpdateListener(new AnimatorUpdateListener() {  
            @Override  
            public void onAnimationUpdate(ValueAnimator animation) {  
                
                float Value = (Float) animation.getAnimatedValue();
                mRadiusThird = Value;
                int alpha = (int) (DEFAULT_ALPHA_THIRD * (1.0f - animation.getAnimatedFraction()));
                Log.v("guide", "alpha = " + alpha);
                mPaintThird.setAlpha(alpha);
                invalidate();
            }  
        });  
        set.play(animator).with(animatorSecond);
        set.play(animatorThird).after(animatorSecond);
        set.addListener(new AnimatorListener() {
            
            @Override
            public void onAnimationStart(Animator arg0) {
                
            }
            
            @Override
            public void onAnimationRepeat(Animator arg0) {
                
            }
            
            @Override
            public void onAnimationEnd(Animator arg0) {
                mPaintFirst.setAlpha(DEFAULT_ALPHA_FIRST);
                mPaintSecond.setAlpha(DEFAULT_ALPHA_SECOND);
                mPaintThird.setAlpha(DEFAULT_ALPHA_THIRD);
            }
            
            @Override
            public void onAnimationCancel(Animator arg0) {
                
            }
        });
        return set;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
        
        if (mRadiusFirst != 0f) {
            canvas.drawCircle(getWidth() / 2.0f, getHeight() / 2.0f, mRadiusFirst, mPaintFirst);
        }
        
        if (mRadiusSecond != 0f) {
            canvas.drawCircle(getWidth() / 2.0f, getHeight() / 2.0f, mRadiusSecond, mPaintSecond);
        }
        if (mRadiusThird != mFromRadiusThird) {
            canvas.drawCircle(getWidth() / 2.0f, getHeight() / 2.0f, mRadiusThird, mPaintThird);
        }
    }

}
