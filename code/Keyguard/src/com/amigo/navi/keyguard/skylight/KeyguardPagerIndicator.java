package com.amigo.navi.keyguard.skylight;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.KWDataCache;
import com.android.keyguard.R ;


public class KeyguardPagerIndicator extends View {
    private final static String LOG_TAG = "KeyguardPagerIndicator";
    
    private int sDisplayW;
    private int sIndicatorW = 1080;
    //Gionee <huangxc><2013-12-18> modify for CR00967751 begin
    private int sIndicatorH = 2;
    private int MARGIN_RINGHT_AND_LEFT = 15;
    //Gionee <huangxc><2013-12-18> modify for CR00967751 end

    private int mCurPage;
    private int mCountPage;
    private int mCurPosX;
    private int mSingleIndicatorW;
    //Gionee <huangxc><2013-12-19> modify for CR00973283 begin
    private int mHalfSingleIndicatorW;
    //Gionee <huangxc><2013-12-19> modify for CR00973283 end
    private int mDx = 0;

    private int mMaxScreenCenter;
    private int mMinScreenCenter;
    private int mInitScreenCenter;

    private Drawable mIndicatorDw;
    private Drawable mIndicatorBg;


    public KeyguardPagerIndicator(Context context) {
        this(context, null);
    }

    public KeyguardPagerIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyguardPagerIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Resources rs = context.getResources();
        //Gionee <huangxc><2013-12-18> modify for CR00967751 begin
        float density = rs.getDisplayMetrics().density;
        sDisplayW = KWDataCache.getScreenWidth(getResources());
        sIndicatorH = (int) (sIndicatorH * density);
        MARGIN_RINGHT_AND_LEFT = (int) (MARGIN_RINGHT_AND_LEFT * density);
        sIndicatorW=getResources().getDisplayMetrics().widthPixels;
        sIndicatorW =sIndicatorW-MARGIN_RINGHT_AND_LEFT*2;
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "sIndicatorW: "+sIndicatorW);
        //Gionee <huangxc><2013-12-18> modify for CR00967751 end
        mIndicatorDw = rs.getDrawable(R.drawable.kw_page_indicator);
        mIndicatorBg = rs.getDrawable(R.drawable.kw_indicator_bg);
    }

    public void setCurPage(int curPage) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "setCurPage curPage=" + curPage);
        mCurPage = curPage;
        requestLayout();
        invalidate();
    }
    

    public void setCountPage(int countPage) {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "setCountPage countPage=" + countPage);
        mCountPage = countPage;
        if (countPage <= 1) {
            setVisibility(INVISIBLE);
        } 
    }


    public void move(int screenCenter) {
        if (screenCenter < mMinScreenCenter) {
            screenCenter = mMinScreenCenter;
        }

        if (screenCenter > mMaxScreenCenter) {
            screenCenter = mMaxScreenCenter;
        }

        int dx = screenCenter - mInitScreenCenter;
        // KeyguardUtils.logD(LOG_TAG, "move dx=" + dx);
        mDx = (dx * sIndicatorW) / (mCountPage * sDisplayW);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mIndicatorBg == null || mIndicatorDw == null) {
            return;
        }
        mIndicatorBg.draw(canvas);
        // Gionee <huangxc><2013-12-19> modify for CR00973283 begin

        if (mSingleIndicatorW != 0) {
            int local = Math.abs(mDx % mSingleIndicatorW);
            int dy = 0;
            if (local < mHalfSingleIndicatorW) {
                dy = local;
            } else {
                dy = -local + mSingleIndicatorW;
            }
            mIndicatorDw.setBounds(mCurPosX + mDx - dy, 0, mCurPosX + mDx + mSingleIndicatorW + dy, sIndicatorH);
            // Gionee <huangxc><2013-12-19> modify for CR00973283 end
            mIndicatorDw.draw(canvas);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(sDisplayW, sIndicatorH);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mCountPage > 0 && mCurPage < mCountPage) {
            int w = sIndicatorW / mCountPage;
            int bgW = mCountPage * w;
            mDx = 0;
            int indicatorLeftX = (sDisplayW - bgW) / 2;
            if (mIndicatorBg != null) {
                mIndicatorBg.setBounds(indicatorLeftX, 0, indicatorLeftX + bgW, sIndicatorH);
            }
            mSingleIndicatorW = w;
            //Gionee <huangxc><2013-12-19> modify for CR00973283 begin
            mHalfSingleIndicatorW = mSingleIndicatorW/2;
            //Gionee <huangxc><2013-12-19> modify for CR00973283 end
            mCurPosX = mCurPage * w + indicatorLeftX;
            mInitScreenCenter = sDisplayW / 2 + mCurPage * sDisplayW;
            mMinScreenCenter = sDisplayW / 2;
            mMaxScreenCenter = sDisplayW / 2 + (mCountPage - 1) * sDisplayW;
        }
    }

    public void onPageSwitch(int newIndex) {
        
    }
    
    public void cleanUp() {
        mIndicatorBg = null;
        mIndicatorDw = null;
    }
}
