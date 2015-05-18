package com.amigo.navi.keyguard.haokan;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.amigo.navi.keyguard.DebugLog;
import com.android.keyguard.R;



public class CircleImageView extends ImageView {

    private static final ScaleType SCALE_TYPE = ScaleType.CENTER_CROP;

    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private static final int COLORDRAWABLE_DIMENSION = 2;

    private final RectF mDrawableRect = new RectF();

    private final Matrix mShaderMatrix = new Matrix();
    private final Paint mBitmapPaint = new Paint();


    private Bitmap mBitmap;
    private BitmapShader mBitmapShader;
    private int mBitmapWidth;
    private int mBitmapHeight;

    private ColorFilter mColorFilter;

    private boolean mReady;
    private boolean mSetupPending;
    
    private int mRadius;
    private boolean AnimatorRunning = false;
    
    private Drawable mBorderDrawable;
    
    private Paint mPaint = new Paint();

    public CircleImageView(Context context) {
        super(context);

        init();
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
 
        mBorderDrawable = getResources().getDrawable(R.drawable.border_drawable);

        init();
        
        
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(0xff000000);
        mPaint.setAlpha(150);
        mRadius = getResources().getDimensionPixelSize(R.dimen.haokan_circle_imageview_radius);
    }

    private void init() {
        super.setScaleType(SCALE_TYPE);
        mReady = true;

        if (mSetupPending) {
            setup();
            mSetupPending = false;
        }
    }

    @Override
    public ScaleType getScaleType() {
        return SCALE_TYPE;
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (scaleType != SCALE_TYPE) {
            throw new IllegalArgumentException(String.format("ScaleType %s not supported.", scaleType));
        }
    }

    @Override
    public void setAdjustViewBounds(boolean adjustViewBounds) {
        if (adjustViewBounds) {
            throw new IllegalArgumentException("adjustViewBounds not supported.");
        }
    }
   

    @Override
    protected void onDraw(Canvas canvas) {
        if (getDrawable() == null) {
            return;
        }
 
        mBorderDrawable.setBounds(0, 0, getWidth(), getHeight());
        mBorderDrawable.draw(canvas);
        
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, mRadius, mBitmapPaint);
        
        if (AnimatorRunning) {
            canvas.drawCircle(getWidth()/2, getHeight()/2, mRadius, mPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setup();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        mBitmap = bm;
        setup();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        mBitmap = getBitmapFromDrawable(drawable);
        setup();
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        mBitmap = getBitmapFromDrawable(getDrawable());
        setup();
    }
 
 

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            Bitmap bitmap;

            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION, BITMAP_CONFIG);
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), BITMAP_CONFIG);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    private void setup() {
        
        if (!mReady) {
            mSetupPending = true;
            return;
        }
        
        if (mBitmap == null) {
            return;
        }
        
        mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        
        mBitmapPaint.setAntiAlias(true);
        mBitmapPaint.setShader(mBitmapShader);
        
        mBitmapHeight = mBitmap.getHeight();
        mBitmapWidth = mBitmap.getWidth();
        
        mDrawableRect.set(0, 0, getWidth(), getHeight());
        
        updateShaderMatrix();  
        invalidate();
    }

    private void updateShaderMatrix() {
        float scale;
        float dx = 0;
        float dy = 0;

        mShaderMatrix.set(null);
        
        if (mBitmapWidth * mDrawableRect.height() > mDrawableRect.width() * mBitmapHeight) {
            scale = mDrawableRect.height() / (float) mBitmapHeight;
            dx = (mDrawableRect.width() - mBitmapWidth * scale) * 0.5f;
        } else {
            scale = mDrawableRect.width() / (float) mBitmapWidth;
            dy = (mDrawableRect.height() - mBitmapHeight * scale) * 0.5f;
        }

        mShaderMatrix.setScale(scale, scale);
        mShaderMatrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));

        mBitmapShader.setLocalMatrix(mShaderMatrix);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                AnimatorRunning = true;
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                AnimatorRunning = false;
                invalidate();
                break;
                default:
                    break;
        }
        
        return super.onTouchEvent(event);
    }
    
    
    public void bindClickAnimator() {
        ValueAnimator animator = ValueAnimator.ofFloat(150, 0).setDuration(100);  
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new AnimatorUpdateListener() {  
            @Override  
            public void onAnimationUpdate(ValueAnimator animation) {  
                float Value = (Float) animation.getAnimatedValue();
                mPaint.setAlpha((int) Value);
                invalidate();
            }  
        });  
        
        animator.addListener(new AnimatorListener() {
            
            @Override
            public void onAnimationStart(Animator arg0) {
                AnimatorRunning = true;
            }
            
            @Override
            public void onAnimationRepeat(Animator arg0) {
            }
            
            @Override
            public void onAnimationEnd(Animator arg0) {
                AnimatorRunning = false;
                invalidate();
            }
            
            @Override
            public void onAnimationCancel(Animator arg0) {
            }
        });
        
        animator.start();
    }

}
