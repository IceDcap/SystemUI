/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar.phone;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;

import com.android.systemui.R;

public class IconMerger extends LinearLayout {
    private static final String TAG = "IconMerger";
    private static final boolean DEBUG = false;

    private int mIconSize;
    //private View mMoreView;
    private View mMoreDotView;
	private static long lastTime = System.currentTimeMillis();
    ValueAnimator animator;
    private boolean mShouldAnimat = false;

    public IconMerger(Context context, AttributeSet attrs) {
        super(context, attrs);

        mIconSize = context.getResources().getDimensionPixelSize(
                R.dimen.gn_status_bar_icon_size);

        if (DEBUG) {
            setBackgroundColor(0x800099FF);
        }
    }

    public void setOverflowIndicator(View dot) {
        //mMoreView = more;
        mMoreDotView = dot;
        
        animator = ObjectAnimator.ofFloat(mMoreDotView, "alpha", 1, 0, 1);
        animator.setStartDelay(100);
        animator.setDuration(500);
        //animator.setRepeatCount(1);
        animator.setInterpolator(new AccelerateInterpolator());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // we need to constrain this to an integral multiple of our children
        int width = getMeasuredWidth();
        setMeasuredDimension(width - (width % mIconSize), getMeasuredHeight());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        checkOverflow(r - l);
    }

    private void checkOverflow(int width) {
        /*if (mMoreDotView == null) return;

        final int N = getChildCount();
        int visibleChildren = 0;
        for (int i=0; i<N; i++) {
            if (getChildAt(i).getVisibility() != GONE) visibleChildren++;
        }
        final boolean overflowShown = (mMoreDotView.getVisibility() == View.VISIBLE);
        // let's assume we have one more slot if the more icon is already showing
        if (overflowShown) visibleChildren --;
        final boolean moreRequired = N * mIconSize > width;
        if (moreRequired != overflowShown) {
            post(new Runnable() {
                @Override
                public void run() {
                    //mMoreView.setVisibility(moreRequired ? View.VISIBLE : View.GONE);
                    mMoreDotView.setVisibility(moreRequired ? View.VISIBLE : View.GONE);
                    mMoreDotView.animate()
                    .alpha(1f)
                    .setDuration(250)
                    .setInterpolator(new AccelerateInterpolator(2.0f))
                    .setListener(moreRequired ? null : new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator _a) {
                        	mMoreDotView.setVisibility(View.GONE);
                        }
                    })
                    .start();
                }
            });
        }*/
    }
    
    // GIONEE <wujj> <2015-03-17> Modify for CR01455032 begin
    public void setMoreDotAnimator(boolean shouldAnim) {
    	mShouldAnimat = shouldAnim;
    }
    // GIONEE <wujj> <2015-03-17> Modify for CR01455032 end
    
    public void setMoreDotVisibility(boolean visible) {
		mMoreDotView.setVisibility(visible ? View.VISIBLE : View.GONE);
		if (animator.isStarted() || !mShouldAnimat) {
		    return;
		}
		setMoreDotAnimate();
	}
    
    private void setMoreDotAnimate() {
    	long curTime =  System.currentTimeMillis();
    	if (curTime - lastTime < 1500) {
    	    return;
    	}
    	
    	lastTime = curTime;
    	mMoreDotView.setAlpha(0);
        animator.start();
    }
}
