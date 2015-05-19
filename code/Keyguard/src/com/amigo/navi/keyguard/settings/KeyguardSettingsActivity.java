package com.amigo.navi.keyguard.settings;

import java.util.ArrayList;

import amigo.app.AmigoAlertDialog;
import amigo.widget.AmigoSwitch;
import amigo.widget.AmigoTextView;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.haokan.NicePicturesInit;
import com.amigo.navi.keyguard.haokan.UIController;
import com.amigo.navi.keyguard.haokan.analysis.Event;
import com.amigo.navi.keyguard.haokan.analysis.HKAgent;
import com.android.keyguard.R;
import android.content.Intent;



public class KeyguardSettingsActivity extends Activity {
	private final String TAG = "KeyguardSettingsActivity";
	
	private Bitmap wallpaper;

    private AmigoSwitch mDoubleDesktopLock;
    private AmigoSwitch mKeyguardWallpaperUpdate;
    private AmigoSwitch mOnlyWlanSwitch;
    private TextView mSettingTitle;
    private AmigoTextView mWallpaperUpdateTitle;
    private TextView mWallpaperUpdateFirstline;
    private TextView mWallpaperUpdateSecondline;
    private TextView mOnlyWlanSwitchFirstLine;
    private View mDivider;
    private AmigoTextView mDoubleDesktopLockTitle;
    private TextView mDoubleDesktopLockFirstline;
    private TextView mDoubleDesktopLockSecondline;
    private Bitmap mWindowBackgroud;

	@Override
	protected void onCreate(Bundle arg0) {
 
		super.onCreate(arg0); 
		
		initView();
		
		UIController.getInstance().setKeyguardSettingsActivity(this);
 
		Intent intent = getIntent();
		if (KeyguardSettings.CLEARNOTIFICATION.equals(intent.getStringExtra(KeyguardSettings.CLEARNOTIFICATION))){
			KeyguardSettings.cancelNotification(getApplicationContext());
		}
		
        if (Build.VERSION.SDK_INT >= 21) {
            this.getWindow().getAttributes().systemUiVisibility |= (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            this.getWindow().setStatusBarColor(Color.TRANSPARENT);
            this.getWindow().setNavigationBarColor(Color.TRANSPARENT);
        }
		
		
		setBlurBackground();
		
		
		

	}
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
        
        if (mWindowBackgroud != null && !mWindowBackgroud.isRecycled()) {
            mWindowBackgroud.recycle();
        }
    }
	
	private void findView(){
		mSettingTitle = (TextView)findViewById(R.id.setting_title);
	    mWallpaperUpdateTitle = (AmigoTextView)findViewById(R.id.wallpaper_update_title);
	    mWallpaperUpdateFirstline = (TextView)findViewById(R.id.wallpaper_update_firstline);
	    mWallpaperUpdateSecondline = (TextView)findViewById(R.id.wallpaper_update_secondline);
	    mKeyguardWallpaperUpdate = (AmigoSwitch) findViewById(R.id.settings_switch_wallpaper_update);
	    mOnlyWlanSwitchFirstLine = (TextView)findViewById(R.id.only_wlan_firstline);
	    mOnlyWlanSwitch = (AmigoSwitch) findViewById(R.id.settings_switch_only_wlan);
	    mDivider = (View)findViewById(R.id.settings_divider);
	    mDoubleDesktopLockTitle = (AmigoTextView)findViewById(R.id.double_desktop_lock_title);
	    mDoubleDesktopLockFirstline = (TextView)findViewById(R.id.double_desktop_lock_firstline);
	    mDoubleDesktopLockSecondline = (TextView)findViewById(R.id.double_desktop_lock_secondline);
	    mDoubleDesktopLock = (AmigoSwitch) findViewById(R.id.settings_switch_double_desktop_lock);
 
	}
	
	private ArrayList<View> getViewGroup(){
		
		ArrayList<View> viewGroup = new ArrayList<View>();
		viewGroup.add(mSettingTitle);
		viewGroup.add(mWallpaperUpdateTitle);
		viewGroup.add(mWallpaperUpdateFirstline);
		viewGroup.add(mKeyguardWallpaperUpdate);
		viewGroup.add(mWallpaperUpdateSecondline);
		viewGroup.add(mOnlyWlanSwitchFirstLine);
		viewGroup.add(mOnlyWlanSwitch);
		viewGroup.add(mDivider);
		viewGroup.add(mDoubleDesktopLockTitle);
		viewGroup.add(mDoubleDesktopLockFirstline);
		viewGroup.add(mDoubleDesktopLock);
		viewGroup.add(mDoubleDesktopLockSecondline);
		return viewGroup;
	}
		
	private void startAppearAnimation(){
	    
		ArrayList<View> viewGroup = getViewGroup();
		
		for (int index = 0; index < viewGroup.size(); index++) {
		    
		    final View v = viewGroup.get(index);

		    AnimationSet set = new AnimationSet(true);  
		    
	        Animation animation = new AlphaAnimation(0.0f, 1.0f);  
	        animation.setDuration(300);  
	        set.addAnimation(animation);  
	        animation = new TranslateAnimation(0, 0, 300, 0); 
	        animation.setDuration(300);  
	        set.addAnimation(animation);  
	        set.setInterpolator(new DecelerateInterpolator());
	        set.setStartOffset(KeyguardSettings.ANIMATION_DELAY * index);
	        set.setAnimationListener(new AnimationListener() {
                
                @Override
                public void onAnimationStart(Animation arg0) {
                    v.setVisibility(View.VISIBLE);
                }
                
                @Override
                public void onAnimationRepeat(Animation arg0) {
                    
                }
                
                @Override
                public void onAnimationEnd(Animation arg0) {
                    
                }
            });
	        v.startAnimation(set);
		}
	}
	
	
    private void initView() {
    	LinearLayout view = (LinearLayout)LayoutInflater.from(this).inflate(
    			R.layout.keyguard_settings_view, null); 
		setContentView(view);
		findView();
		initWallpaperUpdate();
		initDoubleDesktopLock();
		
		view.postDelayed(new Runnable() {
            
            @Override
            public void run() {
                startAppearAnimation();
            }
        }, 50);
    }

	
	
	private void setBlurBackground() {
        this.getWindow().setBackgroundDrawable(null);

        Bitmap bitmap = UIController.getInstance().getCurrentWallpaperBitmap(this);
        mWindowBackgroud = KeyguardWallpaper.getBlurBitmap(bitmap.copy(Bitmap.Config.ARGB_8888, true), 5.0f);
        if (mWindowBackgroud == null) {
            return;
        }
        Drawable drawable = new BitmapDrawable(getResources(), mWindowBackgroud);

        if (drawable != null) {
            ColorMatrix cm = new ColorMatrix();
            float mBlind = 0.6f;
            cm.set(new float[] { mBlind, 0, 0, 0, 0, 0, mBlind, 0, 0, 0, 0, 0,
                    mBlind, 0, 0, 0, 0, 0, 1, 0 });
            drawable.setColorFilter(new ColorMatrixColorFilter(cm));
            this.getWindow().setBackgroundDrawable(drawable);
        }
    }
	
    private void changeWallpaperUpdateData(boolean isopen){
    	if(isopen){
    		mOnlyWlanSwitch.setEnabled(true);
    	}else{
    		mOnlyWlanSwitch.setEnabled(false);
    	}
    }
	
    private void changeDoubleDesktopLockData(boolean isopen){
    	TextView hint = (TextView)findViewById(R.id.double_desktop_lock_secondline);
    	if(isopen){
    		hint.setText(R.string.preference_desktop_lock_on);
    	}else{
    		hint.setText(R.string.preference_desktop_lock_off);
    	}
    }
    
    private void initWallpaperUpdate(){    	
		isAlert = KeyguardSettings.getDialogAlertState(getApplicationContext());
		connectNet = KeyguardSettings.getConnectState(getApplicationContext());
		initKeyguardWallpaperUpdate();
		initOnlyWlan();
    }
    
    private void initKeyguardWallpaperUpdate(){
    	
        mKeyguardWallpaperUpdate.setChecked(connectNet);
        if (connectNet){
        	HKAgent.onEventWallpaperUpdate(getApplicationContext(),Event.SETTING_UPDATE, KeyguardSettings.SWITCH_WALLPAPER_UPDATE_ON);
        }else{
        	HKAgent.onEventWallpaperUpdate(getApplicationContext(),Event.SETTING_UPDATE, KeyguardSettings.SWITCH_WALLPAPER_UPDATE_OFF);
        }
        mKeyguardWallpaperUpdate.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton btnView, boolean isChecked) {
				KeyguardSettings.setWallpaperUpadteState(getApplicationContext(), isChecked);		
				if (isChecked){
	        		if(isAlert){
	        			alertDialog();
	        		}else{
	    				saveConnectState(true);
	        		}
	        		
	        		HKAgent.onEventWallpaperUpdate(getApplicationContext(),Event.SETTING_UPDATE, KeyguardSettings.SWITCH_WALLPAPER_UPDATE_ON);
					
				}else{
	        		saveConnectState(false);
	        		HKAgent.onEventWallpaperUpdate(getApplicationContext(),Event.SETTING_UPDATE, KeyguardSettings.SWITCH_WALLPAPER_UPDATE_OFF);
				}
				
			}});

    }
    
    public void initOnlyWlan(){
    	
        boolean isopen = KeyguardSettings.getOnlyWlanState(this.getApplicationContext());
        mOnlyWlanSwitch.setChecked(isopen);
        if (isopen){
        	HKAgent.onEventOnlyWlan(getApplicationContext(),Event.SETTING_DOWNLOAD, KeyguardSettings.SWITCH_ONLY_WLAN_ON);
        }else{
        	HKAgent.onEventOnlyWlan(getApplicationContext(),Event.SETTING_DOWNLOAD, KeyguardSettings.SWITCH_ONLY_WLAN_OFF);
        }
        mOnlyWlanSwitch.setEnabled(connectNet);
        mOnlyWlanSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton btnView, boolean isChecked) {
				KeyguardSettings.setOnlyWlanState(getApplicationContext(), isChecked);	
				if (isChecked){
	        		HKAgent.onEventOnlyWlan(getApplicationContext(),Event.SETTING_DOWNLOAD, KeyguardSettings.SWITCH_ONLY_WLAN_ON);
				}else{
	        		HKAgent.onEventOnlyWlan(getApplicationContext(),Event.SETTING_DOWNLOAD, KeyguardSettings.SWITCH_ONLY_WLAN_OFF);
				}
				NicePicturesInit.getInstance(getApplicationContext()).registerData();
			}});

    }
   
    private void initDoubleDesktopLock() {
    	
        boolean isopen = KeyguardSettings.getDoubleDesktopLockState(this.getApplicationContext());
        mDoubleDesktopLock.setChecked(isopen);
        changeDoubleDesktopLockData(isopen);
        mDoubleDesktopLock.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton btnView, boolean isChecked) {
				KeyguardSettings.setDoubleDesktopLockState(getApplicationContext(), isChecked);	
				changeDoubleDesktopLockData(isChecked);
			}});

    }
    
    
    private void saveConnectState(boolean connect){
    	if (connect){
    		KeyguardSettings.cancelNotification(getApplicationContext());
    	}
    	changeWallpaperUpdateData(connect);
		connectNet = connect;
		KeyguardSettings.setConnectState(getApplicationContext(),connect);
    }
    
    
    private boolean isAlert = true;
    private boolean connectNet = false;
    private AmigoAlertDialog networkDialog;
	private void alertDialog() {
		View dialogView=LayoutInflater.from(this).inflate(R.layout.net_dialog, null);
		final CheckBox cbDontShowAgain = (CheckBox) dialogView.findViewById(R.id.no_point);
		if(networkDialog != null && networkDialog.isShowing()){
			return;
		}
		
		networkDialog=new AmigoAlertDialog.Builder(this, AmigoAlertDialog.THEME_AMIGO_LIGHT)
		.setTitle(R.string.dialog_information)
		.setView(dialogView)
		.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(cbDontShowAgain.isChecked()){
					isAlert = false;
					KeyguardSettings.setDialogAlertState(getApplicationContext(), isAlert);
				}
				else{
					isAlert = true;
					KeyguardSettings.setDialogAlertState(getApplicationContext(), isAlert);
				}
//				KeyguardSettings.setDialogAlertState(getApplicationContext(), !cbDontShowAgain.isChecked());
				
				saveConnectState(true);
				NicePicturesInit.getInstance(getApplicationContext()).registerData();
				dialog.dismiss();
			}
		}).setNegativeButton(R.string.dialog_cancle, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
					isAlert = true;
					KeyguardSettings.setDialogAlertState(getApplicationContext(), isAlert);
					
					connectNet = false;
					KeyguardSettings.setConnectState(getApplicationContext(),connectNet);
					
					mKeyguardWallpaperUpdate.setChecked(false);
					
					dialog.dismiss();
			}
		}).create();
		networkDialog.show();
		networkDialog.setCancelable(false);
		networkDialog.setCanceledOnTouchOutside(false);
	}   
    

}
