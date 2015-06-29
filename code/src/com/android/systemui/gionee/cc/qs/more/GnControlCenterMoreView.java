
package com.android.systemui.gionee.cc.qs.more;

import java.util.Collection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.gionee.GnBlurHelper;
import com.android.systemui.gionee.GnFontHelper;
import com.android.systemui.gionee.GnUtil;
import com.android.systemui.gionee.cc.GnControlCenter;
import com.android.systemui.gionee.cc.qs.GnQSTile;

public class GnControlCenterMoreView extends FrameLayout implements View.OnClickListener {

    private static final String TAG = "GnControlCenterMoreView";
    
    private Context mContext;

    private ImageView mClose;
    private ImageView mTipsClose;
    private TextView mMore;

    private LinearLayout mMoreLayout;
    private LinearLayout mBackground;
    private RelativeLayout mHeader;
    private RelativeLayout mTips;
    
    private GnDragGridView mDragGridView;

    private static boolean isOpened = false;
    private boolean isHighDevice = false;
    private boolean isAnimating = false;
    
    Handler mHandler = new Handler();
    
    public GnControlCenterMoreView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        
        isHighDevice = GnUtil.isHighDevice(mContext);
    }

    public void setTiles(Collection<GnQSTile<?>> collection) {
        mDragGridView.setTiles(collection);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Resources res = mContext.getResources();
        LinearLayout.LayoutParams lp;
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    res.getDimensionPixelSize(R.dimen.gn_qs_more_header_height_land));
            updateHeaderTextSize(res, R.dimen.gn_qs_more_header_text_size_land);
        } else {
            lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    res.getDimensionPixelSize(R.dimen.gn_qs_more_header_height));
            updateHeaderTextSize(res, R.dimen.gn_qs_more_header_text_size);
        }
        mHeader.setLayoutParams(lp);
        mMore.setText(R.string.gn_qs_more_title);
        
        updateFontTypeFace(newConfig);
    }
    
    private void updateFontTypeFace(Configuration newConfig) {
        GnFontHelper.resetAmigoFont(newConfig, mMore);
    }

    private void updateHeaderTextSize(Resources res, int size) {
        mMore.setTextSize(TypedValue.COMPLEX_UNIT_PX,
            res.getDimensionPixelSize(size));
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
                if (mTips.isShown()) {
                    return true;
                }
                pushDownOut(true);
                break;
            default:
                break;
        }
        
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mMoreLayout = (LinearLayout) findViewById(R.id.moreview);
        mBackground = (LinearLayout) findViewById(R.id.background);
        mHeader = (RelativeLayout) findViewById(R.id.header);
        mTips = (RelativeLayout) findViewById(R.id.tips_layout);
        
        if (isHighDevice) {
            mBackground.setBackgroundColor(0xBF131313);
        } else {
            mBackground.setBackgroundColor(0xFC202020);
        }
                
        mClose = (ImageView) findViewById(R.id.close);
        mClose.setOnClickListener(this);
        mMore = (TextView) findViewById(R.id.more);
        mTipsClose = (ImageView) findViewById(R.id.tips_close);
        mTipsClose.setOnClickListener(this);

        mDragGridView = (GnDragGridView) findViewById(R.id.dragview);
    }
    
    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private void updateBackground() {
        if (!isHighDevice) {
            return;
        }
        
        synchronized (GnBlurHelper.LOCK) {
            if (GnBlurHelper.mBlur != null && !GnBlurHelper.mBlur.isRecycled()) {
                Log.d(TAG, "setbg blur");
                mMoreLayout.setBackground(new BitmapDrawable(GnBlurHelper.mBlur));
            }
        }
    }

    public void pushUpIn() {
        isAnimating = false;
        updateBackground();
        // mMoreLayout.setSystemUiVisibility(SYSTEM_UI_FLAG_IMMERSIVE | SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        mMoreLayout.startAnimation(loadAnim(R.anim.gn_push_up_in, moveInListener));
    }

    public void pushDownOut(boolean needRecover) {
        if (isAnimating) {
            return;
        }
                
        mMoreLayout.startAnimation(loadAnim(R.anim.gn_push_down_out, moveOutListener));
        
        mHandler.postDelayed(new Runnable() {
            
            @Override
            public void run() {                        
                mDragGridView.onTilesChanged();
            }
        }, 400);
    }

    private Animation loadAnim(int id, Animation.AnimationListener listener) {
        Animation anim = AnimationUtils.loadAnimation(mContext, id);
        if (listener != null) {
            anim.setAnimationListener(listener);
        }
        return anim;
    }
    
    private Animation.AnimationListener moveInListener = new Animation.AnimationListener() {

        @Override
        public void onAnimationEnd(Animation animation) {
            boolean isFirstEnter = GnControlCenter.shouldShowTipsPage();
            if (isFirstEnter) {
                mTips.setVisibility(VISIBLE);
                GnControlCenter.updateFirstEntryFlag();
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }

        @Override
        public void onAnimationStart(Animation animation) {

        }
    };

    private Animation.AnimationListener moveOutListener = new Animation.AnimationListener() {

        @Override
        public void onAnimationEnd(Animation animation) {
            setVisibility(View.GONE);
            mTips.setVisibility(GONE);
            if (isHighDevice) {
                mMoreLayout.setBackgroundColor(0xFA222222);
                GnBlurHelper.releaseBitmap(GnBlurHelper.mBlur);
            }
            setOpen(false);
            isAnimating = false;
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }

        @Override
        public void onAnimationStart(Animation animation) {
            isAnimating = true;
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close:
                pushDownOut(true);
                break;
            case R.id.tips_close:
                mTips.setVisibility(GONE);
                break;
            default:
                break;
        }
    }

    public static boolean isOpen() {
        return isOpened;
    }
    
    public static void setOpen(boolean open) {
        isOpened = open;
    }
}
