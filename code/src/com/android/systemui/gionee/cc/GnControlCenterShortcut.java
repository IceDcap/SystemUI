package com.android.systemui.gionee.cc;
/*
*
* MODULE DESCRIPTION
*   GnControlCenterShortcut 
* add by huangwt for Android L at 20141210.
* 
*/

import com.android.systemui.R;
import com.android.systemui.gionee.GnFontHelper;
import com.android.systemui.gionee.cc.camera.GnBlindShootActivity;
import com.android.systemui.gionee.cc.fakecall.GnFakeCallHelper;
import com.android.systemui.gionee.cc.qs.policy.GnTorchController;
import com.android.systemui.gionee.cc.torch.GnTorchHelper;
import com.android.systemui.gionee.cc.util.GnVibrateUtil;
import com.android.systemui.statusbar.policy.FlashlightController;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;

import com.android.systemui.gionee.GnYouJu;

public class GnControlCenterShortcut extends LinearLayout implements 
        GnTorchController.FlashlightListener, OnClickListener ,OnLongClickListener {
    
	private static final String TAG = "GnControlCenterShortcut";
    private Context mContext;
    private GnControlCenterView mGnControlCenterView;
    
    private ImageView mTorch;
    private ImageView mFakeCall;
    private ImageView mCalculator;
    private ImageView mCamera;
    private TextView mTorchTxt;
    private TextView mFakeCallTxt;
    private TextView mCalculatorTxt;
    private TextView mCameraTxt;
    private RelativeLayout mTorchLayout;
    private RelativeLayout mFakeCallLayout;
    
    private int mPaddingTop;
    private int mPaddingLeft;
    
    private Handler mHandler = new Handler();
    
    static class State {
        public boolean visible;
        public int iconId;
        public Drawable icon;
        public String label;
        public boolean value;
    }
    
    private State mTorchState = new State();
    private GnTorchController mGnTorchController;
    private GnTorchHelper mLightHelper;
    private GnFakeCallHelper mFakeCallHelper;

    private final static String CALCULATOR_PKG = "com.android.calculator2";
    private final static String CALCULATOR_CLS = "com.android.calculator2.Calculator";
    private final static String CAMERA_PKG = "com.android.camera";
    private final static String CAMERA_CLS = "com.android.camera.CameraLauncher";
    
    private static final String AMIGO_SYSTEM_UI_CC = "Amigo_SystemUI_CC";

    public GnControlCenterShortcut(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        
        mPaddingTop = mContext.getResources().getDimensionPixelSize(R.dimen.gn_sc_panel_padding_top);
        mPaddingLeft = mContext.getResources().getDimensionPixelSize(R.dimen.gn_sc_panel_padding_left);
        
        mGnTorchController = new GnTorchController(mContext);
        mGnTorchController.addListener(this);
    }

    @Override
    protected void onFinishInflate() {
        mTorch = (ImageView) findViewById(R.id.sc_torch);
        mCalculator = (ImageView) findViewById(R.id.sc_calculator);
        mCamera = (ImageView) findViewById(R.id.sc_camera);
        mFakeCall = (ImageView) findViewById(R.id.sc_fake_call);
        mTorchTxt = (TextView) findViewById(R.id.sc_torch_txt);
        mFakeCallTxt = (TextView) findViewById(R.id.sc_fake_call_txt);
        mCalculatorTxt = (TextView) findViewById(R.id.sc_calculator_txt);
        mCameraTxt = (TextView) findViewById(R.id.sc_camera_txt);
        mTorchLayout = (RelativeLayout) findViewById(R.id.sc_torch_layout);
        mFakeCallLayout = (RelativeLayout) findViewById(R.id.sc_fake_call_layout);
        
        mLightHelper = GnTorchHelper.getInstance(mContext, mTorchLayout);
        mFakeCallHelper = GnFakeCallHelper.getInstance(mContext, mFakeCallLayout);
        mFakeCallHelper.setShortCut(this);
        
        mTorch.setOnClickListener(this);
        mFakeCall.setOnClickListener(this);
        mCalculator.setOnClickListener(this);
        mCamera.setOnClickListener(this);
        mCamera.setOnLongClickListener(this);
    }
    
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mPaddingLeft = mContext.getResources().getDimensionPixelSize(R.dimen.gn_sc_panel_padding_left_land);
        } else {
            mPaddingLeft = mContext.getResources().getDimensionPixelSize(R.dimen.gn_sc_panel_padding_left);
        }
        setPadding(mPaddingLeft, mPaddingTop, mPaddingLeft, 0);
        
        updateFontTypeFace(newConfig);
        updateFontSize();
    }

    private void updateFontSize() {
        float textsize = mContext.getResources().getDimensionPixelSize(R.dimen.gn_qs_tile_text_size);
        mTorchTxt.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
        mFakeCallTxt.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
        mCalculatorTxt.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
        mCameraTxt.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
    }
    
    private void updateFontTypeFace(Configuration newConfig) {
        GnFontHelper.resetAmigoFont(newConfig, mTorchTxt, mFakeCallTxt, mCalculatorTxt, mCameraTxt);
    }
    
    public void setControlCenter(GnControlCenterView view) {
        mGnControlCenterView = view;
    }

    private void refreshState(final boolean newState) {
        if (mTorch == null) {
            return;
        }
        
        mHandler.post(new Runnable() {
            
            @Override
            public void run() {
                mTorchState.value = newState;
                
                if (newState) {
                    mTorch.setImageResource(R.drawable.gn_ic_sc_torch_on);
                } else {
                    mTorch.setImageResource(R.drawable.gn_ic_sc_torch_off);
                }                
            }
        });
    }

    @Override
    public void onFlashlightOff() {
        Log.d(TAG, "onFlashlightOff");
        refreshState(false);
    }

    @Override
    public void onFlashlightError() {
        Log.d(TAG, "onFlashlightError");
        refreshState(false);
    }

    @Override
    public void onFlashlightAvailabilityChanged(boolean available) {
        Log.d(TAG, "onFlashlightAvailabilityChanged available " + available);
        if (!available) {
            refreshState(false);
        }
    }

    void startCalculatorApp() {
        dismissControlCenter();
        
        Intent calculatorIntent = new Intent();
        calculatorIntent.setClassName(CALCULATOR_PKG, CALCULATOR_CLS);
        startActivitySafely(calculatorIntent);
    }
    
    void startCameraApp(){
        dismissControlCenter();
        
        Intent cameraIntent = new Intent();
        cameraIntent.setClassName(CAMERA_PKG, CAMERA_CLS);
        startActivitySafely(cameraIntent);
    }
    
    public void dismissControlCenter() {
        mGnControlCenterView.dismiss();
    }

    private void startActivitySafely(Intent intent) {
        mGnControlCenterView.postStartSettingsActivity(intent, 0);
    }

    @Override
    public boolean onLongClick(View v) {
        if (v == mCamera) {
            if (mGnTorchController.isCameraAvailable()) {
                GnYouJu.onEvent(mContext, AMIGO_SYSTEM_UI_CC, "mCamera_longClicked");
                // GnVibrateUtil.vibrate(mContext);
                Intent intent = new Intent(mContext, GnBlindShootActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        }
        
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sc_torch:
                /*if (ActivityManager.isUserAMonkey()) {
                    return;
                }
                boolean newState = !mTorchState.value;
                mGnTorchController.setFlashlight(newState);
                refreshState(newState);*/
                mLightHelper.onNextClick();
                GnYouJu.onEvent(mContext, AMIGO_SYSTEM_UI_CC, "mTorch_clicked");
                break;
            case R.id.sc_fake_call:
                mFakeCallHelper.onNextClick();
                GnYouJu.onEvent(mContext, AMIGO_SYSTEM_UI_CC, "mFakeCall_clicked");
                break;
            case R.id.sc_calculator:
                startCalculatorApp();
                GnYouJu.onEvent(mContext, AMIGO_SYSTEM_UI_CC, "mCalculator_clicked");
                break;
            case R.id.sc_camera:
                startCameraApp();
                GnYouJu.onEvent(mContext, AMIGO_SYSTEM_UI_CC, "mCamera_clicked");
                break;
            default:
                break;
        }
    }

    public void updateResources() {
        mTorchTxt.setText(R.string.gn_sc_torch);
        mCalculatorTxt.setText(R.string.gn_sc_calculator);
        mCameraTxt.setText(R.string.gn_sc_camera);
        mFakeCallHelper.updateResources();

        mLightHelper.updateUiState();
    }
    
}