
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
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.android.keyguard.R;

public class GuideClickView extends View {

    private Paint mPaintFirst, mPaintSecond;

    private float mRadiusFirst = 0f, mRadiusSecond = 0f;
    private float mMacRadius;
    private float mFromRadius;
    
    private AnimatorSet mAnimatorSet;

//    private ValueAnimator mAnimatorFirst, mAnimatorSecond;

    public GuideClickView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GuideClickView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public GuideClickView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mPaintFirst = new Paint();
        mPaintFirst.setAntiAlias(true);
        mPaintFirst.setStyle(Paint.Style.FILL);
        mPaintFirst.setColor(0xffffffff);

        mPaintSecond = new Paint();
        mPaintSecond.setAntiAlias(true);
        mPaintSecond.setStyle(Paint.Style.FILL);
        mPaintSecond.setColor(0xffffffff);

        mMacRadius = getResources().getDimensionPixelSize(
                R.dimen.guide_click_title_circle_to_radius);
        mFromRadius = getResources().getDimensionPixelSize(
                R.dimen.guide_click_title_circle_from_radius);

    }

    private float mCircleX = -1;
    private float mCircleY = -1;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mCircleX = getWidth() / 2;
        mCircleY = getHeight() / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mRadiusFirst != 0f) {
            canvas.drawCircle(mCircleX, mCircleY, mRadiusFirst, mPaintFirst);
        }

        if (mRadiusSecond != 0f) {
            canvas.drawCircle(mCircleX, mCircleY, mRadiusSecond, mPaintSecond);
        }

    }

    public void stopAnimator() {

        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
            mAnimatorSet = null;
        }
    }
    
    public void startAnimator() {
        
        if (mAnimatorSet != null) {
            if (mAnimatorSet.isRunning()) {
                mAnimatorSet.cancel();
            }
        }
        
        ValueAnimator mAnimatorFirst = ValueAnimator.ofFloat(mFromRadius, mMacRadius).setDuration(800);
        mAnimatorFirst.setInterpolator(new DecelerateInterpolator());
        mAnimatorFirst.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float Value = (Float) animation.getAnimatedValue();
                mRadiusFirst = Value;
                mPaintFirst.setAlpha((int) (255 * (1.0f - animation.getAnimatedFraction())));
                invalidate();
            }
        });


        ValueAnimator mAnimatorSecond = ValueAnimator.ofFloat(mFromRadius, mMacRadius).setDuration(800);
        mAnimatorSecond.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                
                float Value = (Float) animation.getAnimatedValue();
                mRadiusSecond = Value;
                mPaintSecond.setAlpha((int) (255 * (1.0f - animation.getAnimatedFraction())));
                invalidate();
            }
        });

        mAnimatorSecond.setInterpolator(new DecelerateInterpolator());
        mAnimatorSet = new AnimatorSet();
        mAnimatorSecond.setStartDelay(400);
        mAnimatorSet.play(mAnimatorFirst).with(mAnimatorSecond);
        
        mAnimatorSet.addListener(new AnimatorListener() {
            
            private boolean cancel = false;
            
            @Override
            public void onAnimationStart(Animator arg0) {
                
            }
            
            @Override
            public void onAnimationRepeat(Animator arg0) {
                
            }
            
            @Override
            public void onAnimationEnd(Animator animator) {
                if (!cancel) {
                    animator.setStartDelay(450);
                    animator.start();
                }
                cancel = false;
            }
            
            @Override
            public void onAnimationCancel(Animator arg0) {
                cancel = true;
            }
        });
        
        mAnimatorSet.start();

    }

}
