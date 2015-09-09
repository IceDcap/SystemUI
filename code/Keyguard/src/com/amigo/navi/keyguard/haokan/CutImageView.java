 
package com.amigo.navi.keyguard.haokan;

import android.content.Context;

import android.util.AttributeSet;
import android.widget.ImageView;

public class CutImageView extends ImageView {
    private LayoutChangeListener mLayoutChangeListener = null;

    public CutImageView(Context context) {
        super(context);
    }

    public CutImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CutImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mLayoutChangeListener != null) {
            mLayoutChangeListener.onLayoutChange();
        }
    }

    public void setLayoutChangeListener(LayoutChangeListener listener) {
        mLayoutChangeListener = listener;
    }

    public interface LayoutChangeListener {
        public void onLayoutChange();
    }

}

 