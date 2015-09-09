/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/

package com.android.systemui.gionee.cc.qs.brightness;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

public class GnToggleSeekBar extends SeekBar {
    public GnToggleSeekBar(Context context) {
        super(context);
    }

    public GnToggleSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GnToggleSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            setEnabled(true);
        }

        return super.onTouchEvent(event);
    }
}
