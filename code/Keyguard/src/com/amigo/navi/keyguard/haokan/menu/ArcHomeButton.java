
package com.amigo.navi.keyguard.haokan.menu;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.amigo.navi.keyguard.haokan.UIController;
import com.android.keyguard.R;


public class ArcHomeButton extends RelativeLayout {

    private ImageView mImageView;
    
    private float mRadius1 = 0f,mRadius2 = 0f;
    
    private float mMaxRadius;
    private float mFromRadius;
    
    private Paint mRipplePaint1,mRipplePaint2;
    private float mCircleX = -1;
    private float mCircleY = -1;
    
    @Override
    public void setClickable(boolean clickable) {
        super.setClickable(clickable);
        if (mImageView != null) {
            mImageView.setClickable(clickable);
        }
    }

    public ImageView getmImageView() {
        return mImageView;
    }

    public void setmImageView(ImageView mImageView) {
        this.mImageView = mImageView;
    }

    public ArcHomeButton(Context context) {
        this(context, null);
    }

    public ArcHomeButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArcHomeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ArcHomeButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        
        mRipplePaint1 = new Paint();
        mRipplePaint1.setAntiAlias(true);
        mRipplePaint1.setStyle(Paint.Style.FILL);
        mRipplePaint1.setColor(0xffffffff);
        
        mRipplePaint2 = new Paint();
        mRipplePaint2.setAntiAlias(true);
        mRipplePaint2.setStyle(Paint.Style.FILL);
        mRipplePaint2.setColor(0xffffffff);
    }
    

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mImageView = (ImageView) findViewById(R.id.control_hint);
        mImageView.setScaleX(0.4f);
        mImageView.setScaleY(0.4f);
    }
    
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mRadius1 != 0f ) {
            canvas.drawCircle(mCircleX, mCircleY, mRadius1, mRipplePaint1);
        }
        
        if (mRadius2 != 0f) {
            canvas.drawCircle(mCircleX, mCircleY, mRadius2, mRipplePaint2);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mMaxRadius = getWidth() /2;
        mFromRadius = (float) (mMaxRadius * 0.2);
        mCircleX =  getWidth() /2;
        mCircleY = getWidth() /2;
    }
    
   
    public void closeAnimRun() {
        
        mImageView.setClickable(false);
        ValueAnimator animator = ValueAnimator.ofFloat(mFromRadius, mMaxRadius).setDuration(400);  
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new AnimatorUpdateListener() {  
            @Override  
            public void onAnimationUpdate(ValueAnimator animation) {  
                
                float Value = (Float) animation.getAnimatedValue();
                mRadius1 = Value;
                mRipplePaint1.setAlpha((int) (255 * (1.0f - animation.getAnimatedFraction())));
                invalidate();
            }  
        });  
         
        
        ValueAnimator animator2 = ValueAnimator.ofFloat(mFromRadius, mMaxRadius).setDuration(400);  
        
        animator2.addUpdateListener(new AnimatorUpdateListener() {  
            @Override  
            public void onAnimationUpdate(ValueAnimator animation) {  
                
                float Value = (Float) animation.getAnimatedValue();
                mRadius2 = Value;
                mRipplePaint2.setAlpha((int) (255 * (1.0f - animation.getAnimatedFraction())));
                invalidate();
            }  
        });  
        
        PropertyValuesHolder pvhscaleX = PropertyValuesHolder.ofFloat("scaleX", 1f,0.4f);  
        PropertyValuesHolder pvhscaleY = PropertyValuesHolder.ofFloat("scaleY", 1f,0.4f);  
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(mImageView, pvhscaleX, pvhscaleY).setDuration(200);
        
        objectAnimator.addListener(new AnimatorListener() {
            
            @Override
            public void onAnimationStart(Animator arg0) {
 
            }
            
            @Override
            public void onAnimationRepeat(Animator arg0) {
                
            }
            
            @Override
            public void onAnimationEnd(Animator arg0) {
                mImageView.setVisibility(GONE);
            }
            
            @Override
            public void onAnimationCancel(Animator arg0) {
                
            }
        });
        
        
        animator.setStartDelay(200);
        animator2.setStartDelay(400);
        AnimatorSet set = new AnimatorSet();
        
        set.play(objectAnimator).with(animator2).with(animator);
        set.addListener(new AnimatorListener() {
            
            @Override
            public void onAnimationStart(Animator arg0) {
                
            }
            
            @Override
            public void onAnimationRepeat(Animator arg0) {
                
            }
            
            @Override
            public void onAnimationEnd(Animator arg0) {
                UIController.getInstance().hideArcMenu();
                mImageView.setClickable(true);
            }
            
            @Override
            public void onAnimationCancel(Animator arg0) {
                
            }
        });
        set.start();
        UIController.getInstance().addAnimator(set);
    }
    
    
    public void rippleAnimRun( ){  
        
        mImageView.setClickable(false);
        
        ValueAnimator animator = ValueAnimator.ofFloat(mFromRadius, mMaxRadius).setDuration(400);  
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new AnimatorUpdateListener() {  
            @Override  
            public void onAnimationUpdate(ValueAnimator animation) {  
                float Value = (Float) animation.getAnimatedValue();
                mRadius1 = Value;
                mRipplePaint1.setAlpha((int) (255 * (1.0f - animation.getAnimatedFraction())));
                invalidate();
            }  
        });  
         
        
        ValueAnimator animator2 = ValueAnimator.ofFloat(mFromRadius, mMaxRadius).setDuration(400);  
        
        animator2.addUpdateListener(new AnimatorUpdateListener() {  
            @Override  
            public void onAnimationUpdate(ValueAnimator animation) {  
                float Value = (Float) animation.getAnimatedValue();
                mRadius2 = Value;
                mRipplePaint2.setAlpha((int) (255 * (1.0f - animation.getAnimatedFraction())));
                invalidate();
            }  
        });  
        
        
        PropertyValuesHolder pvhscaleX = PropertyValuesHolder.ofFloat("scaleX", 0.4f,1f);  
        PropertyValuesHolder pvhscaleY = PropertyValuesHolder.ofFloat("scaleY", 0.4f,1f);  
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(mImageView, pvhscaleX, pvhscaleY).setDuration(200);
        
        objectAnimator.addListener(new AnimatorListener() {
            
            @Override
            public void onAnimationStart(Animator arg0) {
                mImageView.setScaleX(0.4f);
                mImageView.setScaleY(0.4f);
                mImageView.setVisibility(VISIBLE);
            }
            
            @Override
            public void onAnimationRepeat(Animator arg0) {
                
            }
            
            @Override
            public void onAnimationEnd(Animator arg0) {
                mImageView.setClickable(true);
            }
            
            @Override
            public void onAnimationCancel(Animator arg0) {
                
            }
        });
        
        
        objectAnimator.setStartDelay(400);
        animator2.setStartDelay(200);
        AnimatorSet set = new AnimatorSet();
        set.play(animator).with(animator2).with(objectAnimator);
        set.start();
        UIController.getInstance().addAnimator(set);
        
    }  
   

    
    
}
