package com.android.systemui.gionee.cc.qs;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.android.systemui.R;
import com.android.systemui.gionee.cc.GnControlCenterPanel;

public class GnQSAnimTileView extends GnQSTileView {

    private final View mAnimIcon;
    private boolean mAnimating = false;
    
    public GnQSAnimTileView(Context context) {
        super(context);
        mAnimIcon = createAnimIcon();
        addView(mAnimIcon);
    }

    private View createAnimIcon() {
        final ImageView icon = new ImageView(mContext);
        icon.setId(android.R.id.icon1);
        icon.setScaleType(ScaleType.CENTER_INSIDE);
        icon.setBackgroundResource(R.drawable.gn_sc_fake_call_anim);
        icon.setVisibility(GONE);
        return icon;
    }

    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
        final int w = MeasureSpec.getSize(widthMeasureSpec);
        final int iconSpec = exactly(mIconSizePx);
        mAnimIcon.measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.AT_MOST), iconSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        
        final int w = getMeasuredWidth();
        final int iconLeft = (w - mAnimIcon.getMeasuredWidth()) / 2;
        layout(mAnimIcon, iconLeft, 0);
    }

    @Override
    protected void handleStateChanged(GnQSTile.State state) {

        GnQSTile.AnimBooleanState animState = (GnQSTile.AnimBooleanState) state;
        
        if (GnControlCenterPanel.isMoving() && animState.animating) {
            return;
        }
        
        super.handleStateChanged(state);
        
        if (mIcon instanceof ImageView) {
            if (animState.value) {
                mIcon.setBackgroundResource(R.drawable.gn_ic_qs_tile_bg_enable);
            } else {
                mIcon.setBackgroundResource(R.drawable.gn_ic_qs_tile_bg_disable);
            }
        }
        
        if (animState.value) {
            mLabel.setTextColor(mContext.getResources().getColor(R.color.gn_qs_tile_on));
        } else {
            mLabel.setTextColor(mContext.getResources().getColor(R.color.gn_qs_tile_off));
        }
        
        if (animState.animating && !mAnimating) {
            mAnimating = true;
            mAnimIcon.setVisibility(VISIBLE);
            mAnimIcon.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.gn_anim_sc_fake_call));
        } else if (!animState.animating) {
            mAnimating = false;
            mAnimIcon.clearAnimation();
            mAnimIcon.setVisibility(GONE);
        }
    }
    
}