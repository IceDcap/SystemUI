
package com.android.systemui.gionee.cc.qs.more;

import java.util.Collection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.FrameLayout.LayoutParams;

import com.android.systemui.R;
import com.android.systemui.gionee.GnBlurHelper;
import com.android.systemui.gionee.GnUtil;
import com.android.systemui.gionee.cc.qs.GnQSTile;

public class GnControlCenterMoreView extends FrameLayout implements View.OnClickListener {

    private static final String TAG = "GnControlCenterMoreView";
    
    private Context mContext;

    private TextView mCancel;
    private TextView mSave;

    private LinearLayout mMoreLayout;
    private LinearLayout mBackground;
    private RelativeLayout mHeader;
    
    private GnDragGridView mDragGridView;

    private static boolean isOpened = false;
    private boolean isHighDevice = false;
    private boolean isAnimating = false;
    
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action) 
                    || Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)){
                mDragGridView.recovery();
                pushDownOut();
            }
        }
    };

    public GnControlCenterMoreView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        
        isHighDevice = GnUtil.isHighDevice(mContext);
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        mContext.registerReceiver(mReceiver, filter);
    }

    public void initMoreView(Collection<GnQSTile<?>> collection) {
        mDragGridView.setTiles(collection);
        mDragGridView.showMoreView();
    }

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
    }

    private void updateHeaderTextSize(Resources res, int size) {
        mCancel.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                res.getDimensionPixelSize(size));
        mSave.setTextSize(TypedValue.COMPLEX_UNIT_PX,
            res.getDimensionPixelSize(size));
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
                pushDownOut();
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
        
        if (isHighDevice) {
            mBackground.setBackgroundColor(0xBF131313);
        } else {
            mBackground.setBackgroundColor(0xFA222222);
        }
                
        mCancel = (TextView) findViewById(R.id.cancel);
        mSave = (TextView) findViewById(R.id.save);
        mCancel.setOnClickListener(this);
        mSave.setOnClickListener(this);

        mDragGridView = (GnDragGridView) findViewById(R.id.dragview);
    }
    
    private void updateBackground() {
        if (!isHighDevice) {
            return;
        }
        
        synchronized (GnBlurHelper.LOCK) {
            if (GnBlurHelper.mBlur != null && !GnBlurHelper.mBlur.isRecycled()) {
                mMoreLayout.setBackground(new BitmapDrawable(GnBlurHelper.mBlur));
            }
        }
    }

    public void pushUpIn() {
        updateBackground();
        mDragGridView.hideMoreView();
//        mMoreLayout.setSystemUiVisibility(SYSTEM_UI_FLAG_IMMERSIVE | SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        mMoreLayout.startAnimation(loadAnim(R.anim.gn_push_up_in, null));
    }

    public void pushDownOut() {
        if (isAnimating) {
            return;
        }
        
        mMoreLayout.startAnimation(loadAnim(R.anim.gn_push_down_out, moveOutListener));
    }

    private Animation loadAnim(int id, Animation.AnimationListener listener) {
        Animation anim = AnimationUtils.loadAnimation(mContext, id);
        if (listener != null) {
            anim.setAnimationListener(listener);
        }
        return anim;
    }

    private Animation.AnimationListener moveOutListener = new Animation.AnimationListener() {

        @Override
        public void onAnimationEnd(Animation animation) {
            setVisibility(View.GONE);
            mDragGridView.showMoreView();
            mMoreLayout.setBackgroundColor(0x00000000);
            GnBlurHelper.releaseBitmap(GnBlurHelper.mBlur);
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
            case R.id.cancel:
                pushDownOut();
                mDragGridView.recovery();
                break;
            case R.id.save:
                pushDownOut();
                Handler mHandler = new Handler();
                mHandler.postDelayed(new Runnable() {
                    
                    @Override
                    public void run() {                        
                        mDragGridView.onTilesChanged();
                    }
                }, 400);
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
