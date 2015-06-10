/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
package com.android.systemui.gionee.cc.qs;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.android.systemui.FontSizeUtils;
import com.android.systemui.R;
import com.android.systemui.gionee.GnFontHelper;

/** View that represents a standard quick settings tile. **/
public class GnQSTileView extends ViewGroup {

    protected final Context mContext;
    protected final View mIcon;
    protected final int mIconSizePx;
    private final H mHandler = new H();

    protected TextView mLabel;
    private OnClickListener mClickPrimary;
    private OnLongClickListener mLongClickPrimary;
    
    public GnQSTileView(Context context) {
        
        super(context);

        mContext = context;
        final Resources res = context.getResources();
        mIconSizePx = res.getDimensionPixelSize(R.dimen.gn_qs_tile_icon_size);
        recreateLabel();
        setClipChildren(false);

        mIcon = createIcon();
        addView(mIcon);

        setClickable(true);

        requestLayout();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        requestLayout();
        FontSizeUtils.updateFontSize(mLabel, R.dimen.gn_qs_tile_text_size);
        
        updateFontTypeFace(newConfig);
    }
    
    private void updateFontTypeFace(Configuration newConfig) {
        GnFontHelper.resetAmigoFont(newConfig, mLabel);
    }

    private void recreateLabel() {
        CharSequence labelText = null;
        if (mLabel != null) {
            labelText = mLabel.getText();
            removeView(mLabel);
            mLabel = null;
        }

        final Resources res = mContext.getResources();

        mLabel = new TextView(mContext);
        mLabel.setId(android.R.id.title);
        mLabel.setTextColor(res.getColor(R.color.qs_tile_text));
        mLabel.setGravity(Gravity.CENTER_HORIZONTAL);
        mLabel.setSingleLine();
        mLabel.setPadding(0, 0, 0, 0);
        mLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                res.getDimensionPixelSize(R.dimen.gn_qs_tile_text_size));
        mLabel.setClickable(false);
        if (labelText != null) {
            mLabel.setText(labelText);
        }
        addView(mLabel);
    }

    public void setDual(boolean dual) {
        setOnClickListener(mClickPrimary);
        setOnLongClickListener(mLongClickPrimary);
        setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);

        setFocusable(!dual);
        postInvalidate();
    }
    
    public void setClickListener(OnClickListener clickPrimary) {
        mClickPrimary = clickPrimary;
    }

    public void setLongClickListener(OnLongClickListener longClickPrimary) {
        mLongClickPrimary = longClickPrimary;
    }

    protected View createIcon() {
        final ImageView icon = new ImageView(mContext);
        icon.setId(android.R.id.icon);
        icon.setScaleType(ScaleType.CENTER_INSIDE);
        icon.setBackgroundResource(R.drawable.gn_ic_qs_tile_bg_disable);
        return icon;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int w = MeasureSpec.getSize(widthMeasureSpec);
        final int h = MeasureSpec.getSize(heightMeasureSpec);
        final int iconSpec = exactly(mIconSizePx);
        mIcon.measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.AT_MOST), iconSpec);
        mLabel.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(h, MeasureSpec.AT_MOST));
        setMeasuredDimension(w, h);
    }

    protected static int exactly(int size) {
        return MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int w = getMeasuredWidth();
        final int h = getMeasuredHeight();

        final int iconLeft = (w - mIcon.getMeasuredWidth()) / 2;
        layout(mIcon, iconLeft, 0);
        layout(mLabel, 0, h - mLabel.getMeasuredHeight());
    }

    protected static void layout(View child, int left, int top) {
        child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());
    }

    protected void handleStateChanged(GnQSTile.State state) {
        if (mIcon instanceof ImageView) {
            ImageView iv = (ImageView) mIcon;
            if (state.icon != null) {
                iv.setImageDrawable(state.icon);
            } else if (state.iconId > 0) {
                iv.setImageResource(state.iconId);
            }
        }

        mLabel.setText(state.label);
        setEnabled(state.clickable);
    }

    public void onStateChanged(GnQSTile.State state) {
        mHandler.obtainMessage(H.STATE_CHANGED, state).sendToTarget();
    }

    private class H extends Handler {
        private static final int STATE_CHANGED = 1;
        public H() {
            super(Looper.getMainLooper());
        }
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == STATE_CHANGED) {
                handleStateChanged((GnQSTile.State) msg.obj);
            }
        }
    }
}
