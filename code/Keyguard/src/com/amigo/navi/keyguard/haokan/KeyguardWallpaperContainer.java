
package com.amigo.navi.keyguard.haokan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
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
        
//        mPaint.setXfermode(new PorterDuffXfermode(Mode.XOR));
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.haokan_wallpaper_alpha).copy(Bitmap.Config.ALPHA_8, true);
        bottomDrawable = getResources().getDrawable(R.drawable.infozone_background);
        bottomDrawable.setBounds(0, mScreenHeight - getResources().getDimensionPixelSize(R.dimen.ketguard_infozone_height), mScreenWidth, mScreenHeight);
//        Log.v("zhaowei", Common.formatByteToMB(mBitmap.getByteCount()) + "MB");
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
    
    
    
//    int[] colors = new int[] {0xffffffff, 0xffffffff, /*0xddffffff, 0xaaffffff, 0x66ffffff, 0x22ffffff, */0x00ffffff,};
//    
//    float[] stops = new float[] { 0, 0.75f, /*0.75f, 0.80f, 0.85f, 0.90f,*/ 1.0f};
    
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        bottomDrawable.draw(canvas);
        
        if (mModel!=UIController.SCROLL_TO_SECURTY && mTop != mScreenHeight) {
            canvas.drawBitmap(mBitmap, 0, mTop, mPaint);
            
//            RadialGradient mRadialGradient = new RadialGradient(mScreenWidth / 2, mScreenHeight * 3 - mTop, mScreenHeight * 2 ,
//                    colors, stops, Shader.TileMode.CLAMP);
//            
//            mPaint.setShader(mRadialGradient); 
//            canvas.drawCircle(mScreenWidth / 2, mScreenHeight * 3 - mTop, mScreenHeight * 2, mPaint);
        }
        
        
        int color = Color.argb((int)(BLINDDEGREE * mBlind), 0, 0, 0);
		canvas.drawColor(color);
    }
    

    
   public void onKeyguardModelChanged(int top,int maxBoundY, int  model) {
//        mTop = (int) (top * mScreenHeight / (float)maxBoundY);
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
