
package com.amigo.navi.keyguard.haokan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.android.keyguard.R;

public class KeyguardWallpaperContainer extends FrameLayout {

    private int mTop;
    
    private LinearGradient mLinearGradient;
    private Paint mPaint = new Paint();
    
    private int mScreenHeight = 2560;
    private int mScreenWidth = 1440;  
    int heightOffset = 400; //100dp
    private float mRadius;
    
    float cx,cy;
    
    Bitmap mBitmap = null;
    private float mBlind=0;
    private int mModel;
    
    Drawable bottomDrawable = null;
    
    public KeyguardWallpaperContainer(Context context) {
        this(context,null);
    }

    public KeyguardWallpaperContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyguardWallpaperContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public KeyguardWallpaperContainer(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mScreenHeight = Common.getScreenHeight(getContext().getApplicationContext());
        mScreenWidth =  Common.getScreenWidth(getContext().getApplicationContext());
        mTop = mScreenHeight;
        
        cx = mScreenWidth / 2.0f;
        cy = mScreenHeight / 2.0f;
        heightOffset = getResources().getDimensionPixelSize(R.dimen.haokan_wallpaper_alpha_offset);
        
        mRadius = heightOffset + mScreenHeight;
        
        UIController.getInstance().setmKeyguardWallpaperContainer(this);
        mPaint.setAntiAlias(true);
        mPaint.setXfermode(new PorterDuffXfermode(Mode.DST_ATOP));
        
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.haokan_wallpaper_alpha).copy(Bitmap.Config.ARGB_8888, true);
        
        bottomDrawable = getResources().getDrawable(R.drawable.infozone_background);
        bottomDrawable.setBounds(0, mScreenHeight - getResources().getDimensionPixelSize(R.dimen.ketguard_infozone_height), mScreenWidth, mScreenHeight);
        
    }
    
    public void reset() {
        mTop = mScreenHeight;
		mBlind=0;
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
    private static final int BLINDDEGREE = 150;
    
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        bottomDrawable.draw(canvas);
        if (mModel!=UIController.SCROLL_TO_SECURTY && mTop != mScreenHeight) {
            canvas.drawBitmap(mBitmap, 0, mTop, mPaint);
        }
        
        int color = Color.argb((int)(BLINDDEGREE * mBlind), 0, 0, 0);
		canvas.drawColor(color);
    }
    

    
   public void onKeyguardModelChanged(int top,int maxBoundY, int  model) {
        
        int bitmapHeight = mBitmap.getHeight();
        mTop = (int) (mScreenHeight - top * (bitmapHeight / (float)maxBoundY));
        mModel=model;
        if(model==UIController.SCROLL_TO_SECURTY){
        	mBlind= (float)top/maxBoundY;
        }else if(model==UIController.SECURITY_SUCCESS_UNLOCK){
        	float blind=(float)top/maxBoundY;
        	mBlind= 1-blind;
        }else{
        	mBlind=0;
        }
        postInvalidate();
    }

    
}
