package com.amigo.navi.keyguard.settings;

import java.util.ArrayList;

import amigo.app.AmigoActivity;
import amigo.app.AmigoAlertDialog;
import amigo.changecolors.ChameleonColorManager;
import amigo.preference.AmigoPreferenceActivity;
import amigo.preference.AmigoSwitchPreference;
import amigo.preference.AmigoPreferenceFrameLayout.LayoutParams;
import amigo.widget.AmigoSwitch;
import amigo.widget.AmigoTextView;
import amigo.app.AmigoActionBar;
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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.haokan.UIController;
import com.amigo.navi.keyguard.haokan.analysis.Event;
import com.amigo.navi.keyguard.haokan.analysis.HKAgent;
import com.android.keyguard.R;

public class KeyguardSettingsActivity extends AmigoActivity {
	private final String TAG = "KeyguardSettingsActivity";
	Bitmap wallpaper;

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
//        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
//		//        set it to be no title 
//        requestWindowFeature(Window.FEATURE_NO_TITLE);   
//         
////        set it to be full screen 
        setTheme(android.R.style.Theme_Material_Light);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,    
//        WindowManager.LayoutParams.FLAG_FULLSCREEN); 
		super.onCreate(arg0); 
//		AmigoActionBar mActionBar = getAmigoActionBar();
//		mActionBar.hide();
		
        if (Build.VERSION.SDK_INT >= 21) {
            this.getWindow().getAttributes().systemUiVisibility |= (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            this.getWindow().setStatusBarColor(Color.TRANSPARENT);
            this.getWindow().setNavigationBarColor(Color.TRANSPARENT);
        }
		
		
		setBlurBackground();
		
		initView();

/*		int topMargin = KWDataCache.getStatusBarHeight();
		end = System.currentTimeMillis();
		last = end - start;
		start = end;
		DebugLog.e(TAG, "KeyguardSettingsActivity onCreate topMargin"+ last);
		
		mSettingTitle = (TextView)findViewById(R.id.setting_title);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		params.topMargin=topMargin;
		params.height = getResources().getDimensionPixelSize(R.dimen.settings_titlebar_height);
		mSettingTitle.setLayoutParams(params);
		end = System.currentTimeMillis();
		last = end - start;
		start = end;
		DebugLog.e(TAG, "KeyguardSettingsActivity onCreate setLayoutParams"+ last);*/
		
		startAppearAnimation();

	}
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
        
        if (mWindowBackgroud != null && !mWindowBackgroud.isRecycled()) {
            mWindowBackgroud.recycle();
        }
    }
	
	private void findViewForAnim(){
		mSettingTitle = (TextView)findViewById(R.id.setting_title);
	    mWallpaperUpdateTitle = (AmigoTextView)findViewById(R.id.wallpaper_update_title);
	    mWallpaperUpdateFirstline = (TextView)findViewById(R.id.wallpaper_update_firstline);
	    mWallpaperUpdateSecondline = (TextView)findViewById(R.id.wallpaper_update_secondline);
	    mOnlyWlanSwitchFirstLine = (TextView)findViewById(R.id.only_wlan_firstline);
	    mDivider = (View)findViewById(R.id.settings_divider);
	    mDoubleDesktopLockTitle = (AmigoTextView)findViewById(R.id.double_desktop_lock_title);
	    mDoubleDesktopLockFirstline = (TextView)findViewById(R.id.double_desktop_lock_firstline);
	    mDoubleDesktopLockSecondline = (TextView)findViewById(R.id.double_desktop_lock_secondline);
	}
	
	private ArrayList<View> getViewGroup(){
		findViewForAnim();
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
			Animation animation = (Animation) AnimationUtils.loadAnimation(KeyguardSettingsActivity.this, R.anim.keyguard_settings_translate_enter); 
			animation.setStartOffset(KeyguardSettings.ANIMATION_DELAY * index);
			viewGroup.get(index).startAnimation(animation);
		}
	}
	
//	private void finishAppearAnimation(){
//		ArrayList<View> viewGroup = getViewGroup();
//		for (int index = 0; index < viewGroup.size(); index++) {
//			Animation animation = (Animation) AnimationUtils.loadAnimation(KeyguardSettingsActivity.this, R.anim.keyguard_settings_translate_exit); 
////			animation.setStartOffset(KeyguardSettings.ANIMATION_DELAY * index);
//			viewGroup.get(index).startAnimation(animation);
//		}
//	}
//	
//	private void finishAppearAnimation(){
//		View rootView = ((ViewGroup)this.findViewById(android.R.id.content)).getChildAt(0);
//		Animation animation = (Animation) AnimationUtils.loadAnimation(KeyguardSettingsActivity.this, R.anim.keyguard_settings_translate_exit);
//		rootView.startAnimation(animation);
//	}
	
    private void initView() {
    	LinearLayout view = (LinearLayout)LayoutInflater.from(this).inflate(
    			R.layout.keyguard_settings_view, null); 
//		LayoutParams viewParams = new LayoutParams(
//		LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//////        LayoutParams params = new LayoutParams(0, 0);
//////        params.width = 800;
//////        params.height =1920;
//		int bgColor = getResources().getColor(R.color.keyguard_setting_bg_color); 
//		view.setBackgroundColor(bgColor);
//		
////		LinearLayout layout = new LinearLayout(this);		
////		int bgColor = getResources().getColor(R.color.keyguard_setting_bg_color); 
////		layout.setBackgroundColor(bgColor);
////		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
////				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
////		layout.addView(view,params);
////	
////        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
////        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);          
////        getWindow().setStatusBarColor(Color.TRANSPARENT);
   
		setContentView(view/*,viewParams*/);
		
		initWallpaperUpdate();
		initDoubleDesktopLock();
        //getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		 getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

    }

	@Override
	protected void onResume() {
//		overridePendingTransition(android.R.anim.fade_in, 0);
		super.onResume();
	}

	@Override
	protected void onPause() {
//		overridePendingTransition(0, android.R.anim.fade_out);
//		overridePendingTransition(0, R.anim.keyguard_settings_translate_exit);
		super.onPause();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
 
	
	
	
	private void setBlurBackground() {
        this.getWindow().setBackgroundDrawable(null);

        Bitmap bitmap = UIController.getInstance().getCurrentWallpaperBitmap();
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

	/**
	 * 
	private void setBlurBackground() {
		this.getWindow().setBackgroundDrawable(null);

		if (wallpaper != null) {
			wallpaper.recycle();
			wallpaper = null;
		}

//		wallpaper = KeyguardWallpaper.getWallpaperBmp(this);
		wallpaper = UIController.getInstance().getCurrentWallpaperBitmap();
		if (wallpaper == null) {
			DebugLog.mustLog(TAG, "getWallpaperBmp null");
			return;
		}
		wallpaper = KeyguardWallpaper.getBlurBitmap(wallpaper, 5.0f);
		// ViewUtil.saveMyBitmap("getBlurBitmap", screenBmp);
		if (wallpaper == null) {
			DebugLog.mustLog(TAG, "getBlurBitmap null");
			return;
		}
		Drawable drawable = new BitmapDrawable(getResources(), wallpaper);

		if (drawable != null) {
			ColorMatrix cm = new ColorMatrix();
			float mBlind = 0.6f;
			cm.set(new float[] { mBlind, 0, 0, 0, 0, 0, mBlind, 0, 0, 0, 0, 0,
					mBlind, 0, 0, 0, 0, 0, 1, 0 });
			drawable.setColorFilter(new ColorMatrixColorFilter(cm));
			this.getWindow().setBackgroundDrawable(drawable);
		}
	}
	 */
	
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
    	mKeyguardWallpaperUpdate = (AmigoSwitch) findViewById(R.id.settings_switch_wallpaper_update);
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
    	mOnlyWlanSwitch = (AmigoSwitch) findViewById(R.id.settings_switch_only_wlan);
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
			}});

    }
   
    private void initDoubleDesktopLock() {
    	mDoubleDesktopLock = (AmigoSwitch) findViewById(R.id.settings_switch_double_desktop_lock);
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
