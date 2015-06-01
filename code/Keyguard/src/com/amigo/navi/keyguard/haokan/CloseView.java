package com.amigo.navi.keyguard.haokan;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class CloseView extends ImageView{


    public CloseView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
         super.onTouchEvent(event);
         return true;
    }

}
