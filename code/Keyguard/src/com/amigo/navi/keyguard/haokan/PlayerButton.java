package com.amigo.navi.keyguard.haokan;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.amigo.navi.keyguard.haokan.PlayerManager.State;
import com.android.keyguard.R;

public class PlayerButton extends View {
    
    
    private State mState = State.NULL;
    
    private RectF mOval;
    
    private float mFraction = 0f;
    
    private int mPadding = 3;
    
    private int mProgressStroke = 6;
    
    private Drawable mDrawablePlayer;
    private Drawable mDrawablePause;
    private Drawable mDrawableNormal;
    
    
    private Paint mPaintCirle;
    
    public float getMaxTranslationX() {
        return Common.getScreenWidth(getContext()) - getLeft();
    }
    
    public void setState(State mState) {
        this.mState = mState;
        if (mState == State.NULL) {
            mFraction = 0;
        }
        invalidate();
    }

    public float getFraction() {
        return mFraction;
    }

    public void setProgress(float mFraction) {
        this.mFraction = mFraction;
        invalidate();
    }

    public PlayerButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaintCirle = new Paint();
        mOval = new RectF();
        
        mPaintCirle.setAntiAlias(true); 
        mPaintCirle.setFlags(Paint.ANTI_ALIAS_FLAG); 
        mPaintCirle.setStyle(Paint.Style.STROKE);
        
        mDrawablePlayer = getResources().getDrawable(R.drawable.haokan_music_player);
        mDrawablePause = getResources().getDrawable(R.drawable.haokan_music_pause);
        mDrawableNormal = getResources().getDrawable(R.drawable.haokan_music_normal);
        
        mProgressStroke = getResources().getDimensionPixelOffset(R.dimen.haokan_music_player_button_stroke);
        mPadding = getResources().getDimensionPixelSize(R.dimen.haokan_music_player_button_padding);
        
        mPaintCirle.setStrokeWidth(mProgressStroke); 
        mPaintCirle.setColor(Color.RED);
        

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int left = (getWidth() - mDrawablePlayer.getIntrinsicWidth()) / 2;
        int top = (getHeight() - mDrawablePlayer.getIntrinsicHeight()) / 2;
        int right =  (getWidth() + mDrawablePlayer.getIntrinsicWidth()) / 2;
        int bottom =  (getHeight() + mDrawablePlayer.getIntrinsicHeight()) / 2;
        
        if (mState == State.NULL) {
            mDrawableNormal.setBounds(left, top, right, bottom);
            mDrawableNormal.draw(canvas);
        }else {
            if (mState == State.PLAYER || mState == State.PREPARE) {
                mDrawablePlayer.setBounds(left, top, right, bottom);
                mDrawablePlayer.draw(canvas);
            }else {
                mDrawablePause.setBounds(left, top, right, bottom);
                mDrawablePause.draw(canvas);
            }
            mOval.set(left + mPadding, top + mPadding, right - mPadding, bottom - mPadding);
            canvas.drawArc(mOval, -90, getFraction() * 360, false, mPaintCirle); 
        }
    }
    
}
