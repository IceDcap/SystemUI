package com.android.systemui.gionee.cc;
/*
*
* MODULE DESCRIPTION
*   GnControlCenterView 
* add by huangwt for Android L at 20141210.
* 
*/

import java.util.Collection;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.android.systemui.R;
import com.android.systemui.gionee.GnBlurHelper;
import com.android.systemui.gionee.GnUtil;
import com.android.systemui.gionee.cc.GnControlCenter.Callback;
import com.android.systemui.gionee.cc.GnControlCenterPanel.OnDrawerOpenListener;
import com.android.systemui.gionee.cc.qs.GnQSPanel;
import com.android.systemui.gionee.cc.qs.GnQSTile;
import com.android.systemui.gionee.cc.qs.GnQSTileHost;
import com.android.systemui.gionee.cc.qs.more.GnControlCenterMoreView;
import com.android.systemui.gionee.cc.qs.policy.GnBluetoothControllerImpl;
import com.android.systemui.gionee.cc.qs.policy.GnLocationControllerImpl;
import com.android.systemui.gionee.cc.qs.policy.GnMobileDataControllerImpl;
import com.android.systemui.gionee.cc.qs.policy.GnNextAlarmControllerImpl;
import com.android.systemui.gionee.cc.qs.policy.GnRotationLockControllerImpl;
import com.android.systemui.gionee.cc.qs.policy.GnWifiControllerImpl;
import com.android.systemui.statusbar.phone.PhoneStatusBar;


public class GnControlCenterView extends FrameLayout {

    public static final boolean DEBUG = GnControlCenter.DEBUG;
    private static final String TAG = "GnControlCenterView";
    
    // Context
    private Context mContext;
    
    // Orientation
    private int mOldOrientation = Configuration.ORIENTATION_PORTRAIT;

    // Handler
    private static final int ACTION_DISMISS_CONTROL_CENTER = 1000;
    private static final int ACTION_START_ACTIVITY = 1001;
    private static final int ACTION_SHOW_MORE = 1002;
    private static final int ACTION_HIDE_MORE = 1003;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ACTION_DISMISS_CONTROL_CENTER:
                    mGnControlCenterPanel.animateClose();
                    break;
                case ACTION_START_ACTIVITY:
                    mStatusBar.startActivityDismissingKeyguard((Intent) msg.obj, false, true);
                    break;
                case ACTION_SHOW_MORE:
                    mGnMoreView.setVisibility(View.VISIBLE);
                    mGnMoreView.pushUpIn();
                    break;
                case ACTION_HIDE_MORE:
                    mGnMoreView.pushDownOut(true);
                default:
                    break;
            }
        }
    };

    // view
    private ImageView mHandle = null;
    private GnControlCenterPanel mGnControlCenterPanel;
    
    // status bar
    private PhoneStatusBar mStatusBar;
    
    // More
    private GnControlCenterMoreView mGnMoreView;
    
    // QS
    private GnQSPanel mGnQSPanel;
    
    // QSH
    private GnQSTileHost mGnQSH;
    
    // Controller
    GnBluetoothControllerImpl mBluetoothController;
    GnLocationControllerImpl mLocationController;
    GnRotationLockControllerImpl mRotationLockController;
    GnWifiControllerImpl mGnWifiController;
    GnMobileDataControllerImpl mGnMobileDataController;
    GnNextAlarmControllerImpl mGnNextAlarmController;
    
    private Callback mCallback = new Callback() {
        
        @Override
        public void dismissPanel() {
            if (mGnControlCenterPanel.isOpened()) {
                mHandler.sendEmptyMessage(ACTION_DISMISS_CONTROL_CENTER);
            }
            
            Log.d(TAG, "mGnMoreView.getVisibility() " + mGnMoreView.getVisibility());
            if (mGnMoreView.getVisibility() == VISIBLE) {
                mHandler.sendEmptyMessage(ACTION_HIDE_MORE);
            }
        }
    };

    public GnControlCenterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        
        GnControlCenter.addCallback(mCallback);
    }

    @Override
    public void onFinishInflate() {
        mHandle = (ImageView) findViewById(R.id.handle);
        mGnControlCenterPanel = (GnControlCenterPanel) findViewById(R.id.slidingDrawer);
        mGnControlCenterPanel.setControlCenterView(this);
        mGnControlCenterPanel.setOnDrawerOpenListener(new OnDrawerOpenListener() {
            
            @Override
            public void onDrawerOpened() {
                updateResources();
            }
        });
        
        mBluetoothController = new GnBluetoothControllerImpl(mContext);
        mLocationController = new GnLocationControllerImpl(mContext);
        mRotationLockController = new GnRotationLockControllerImpl(mContext);
        mGnWifiController = new GnWifiControllerImpl(mContext);
        mGnMobileDataController = new GnMobileDataControllerImpl(mContext);
        mGnNextAlarmController = new GnNextAlarmControllerImpl(mContext);
        
        mGnQSPanel = (GnQSPanel) findViewById(R.id.gn_quick_settings_panel);
        if (mGnQSPanel != null) {
            mGnQSH = new GnQSTileHost(mContext, this,
                    mBluetoothController, mLocationController, mRotationLockController,
                    mGnWifiController, mGnMobileDataController, mGnNextAlarmController);
            mGnQSPanel.setHost(mGnQSH);
            mGnQSH.setCallback(new GnQSTileHost.Callback() {
                @Override
                public void onTilesChanged() {
                    mGnQSPanel.setTiles(mGnQSH.getTiles());
                    mGnMoreView.setTiles(mGnQSH.getTiles());
                }
            });

            updateResources();
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        Log.d(TAG, "visibility = " + visibility);
        if (visibility != View.VISIBLE) {
            if (!GnControlCenterMoreView.isOpen()) {
                Log.d(TAG, "releaseBitmap mBlur");
                GnBlurHelper.releaseBitmap(GnBlurHelper.mBlur);                
            }
            
            Log.d(TAG, "cc unlock");
            GnUtil.setLockState(GnUtil.STATE_LOCK_UNLOCK);
            GnControlCenter.go(GnControlCenter.STATE_CLOSED);
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Resources res = mContext.getResources();
        ViewGroup.LayoutParams lp;
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            lp = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,
                    res.getDimensionPixelSize(R.dimen.gn_cc_handle_height_land));
        } else {
            lp = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,
                    res.getDimensionPixelSize(R.dimen.gn_cc_handle_height));
        }
        mHandle.setLayoutParams(lp);
        
        if (mGnControlCenterPanel.isShown() && newConfig.orientation != mOldOrientation) {
            Log.d(TAG, "onConfigurationChanged createBlurBg");
            mOldOrientation = newConfig.orientation;
            
            GnControlCenter.createBlurBg(mContext);
            // Gionee <huangwt> <2015-3-31> add for CR01458422 begin
            GnControlCenter.dismiss();
            // Gionee <huangwt> <2015-3-31> add for CR01458422 end
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
                GnControlCenter.dismiss();
                break;
            default:
                break;
        }
        
        return super.dispatchKeyEvent(event);
    }

    public View getHandle() {
        return mHandle;
    }

    public void postStartSettingsActivity(final Intent intent, int delay) {
        
        if (mGnControlCenterPanel.isOpened()) {
            mHandler.sendEmptyMessage(ACTION_DISMISS_CONTROL_CENTER);
        }
        
        if (mGnMoreView.getVisibility() == VISIBLE) {
            mHandler.sendEmptyMessage(ACTION_HIDE_MORE);
        }
        
        Message msg = mHandler.obtainMessage(ACTION_START_ACTIVITY, intent);
        mHandler.sendMessageDelayed(msg, delay);
    }

    public void updateResources() {
        Collection<GnQSTile<?>> collection = mGnQSH.getTiles();
        for (GnQSTile<?> tile : collection) {
            tile.refreshState();
        }
    }

    public void setBar(PhoneStatusBar statusBar) {
        mStatusBar = statusBar;
    }
        
    public boolean isOpened() {
        return mGnControlCenterPanel.isOpened();
    }
    
    public boolean isFling() {
        return mGnControlCenterPanel.isFling();
    }
    
    public void swiping(MotionEvent event) {
        mGnControlCenterPanel.swiping(event);
    }
    
    public Collection<GnQSTile<?>> getTiles() {
        return mGnQSH.getTiles();
    }

    public void addGnMoreView(GnControlCenterMoreView moreView) {
        mGnMoreView = moreView;
    }

    public void initQS() {
        mHandler.postDelayed(new Runnable() {
            
            @Override
            public void run() {
                Log.d(TAG, "initQS() thread setListening true");
                mGnMoreView.setTiles(mGnQSH.getTiles());
                mGnQSPanel.setTiles(mGnQSH.getTiles());
                mGnQSPanel.setListening(true);
            }
        }, 500);
    }

    public void openMoreView() {
        Log.d(TAG, "openMoreView");
        GnControlCenterMoreView.setOpen(true);
        GnControlCenter.dismiss();
        mHandler.sendEmptyMessageDelayed(ACTION_SHOW_MORE, 300);
    }
}
