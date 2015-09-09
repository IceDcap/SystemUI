package com.amigo.navi.keyguard.haokan;


import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.settings.KeyguardSettingsActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class BlankActivity extends Activity {
	
	private static final String TAG = "BlankActivity";
	private static final int REQUEST_CODE = 1001;
	private boolean isLcokApplyWallpaper=true;
	public static final String APPLY_WALLPAPER_LOCK_OR_NOT="applayWallpaperLockOrNot";
	public static final String NEED_SHOW_TOAST_OR_NOT="needShowToastOrNot";
	private boolean isShowToast=false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
	
		DebugLog.d(TAG, "onCreate..isLcokApplyWallpaper="+isLcokApplyWallpaper);
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		Intent wrapperIntent = Intent.createChooser(intent, null);
		startActivityForResult(wrapperIntent, REQUEST_CODE);

		UIController.getInstance().setBlankActivity(this);
		DebugLog.d(TAG, "onCreate");
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		DebugLog.d(TAG, "onResume");
		Intent entranceIntent=getIntent();
		if(entranceIntent!=null){
			isLcokApplyWallpaper=entranceIntent.getBooleanExtra(APPLY_WALLPAPER_LOCK_OR_NOT, true);
			isShowToast=entranceIntent.getBooleanExtra(NEED_SHOW_TOAST_OR_NOT, false);
		}
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		DebugLog.d(TAG, "onPause");
	}
	
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		DebugLog.d(TAG, "onActivityResult  requestCode = " + requestCode + "; resultCode = " + resultCode);
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK
				&& null != data) {
			Uri selectedImage = data.getData();
			 
			Intent intent = new Intent(BlankActivity.this, WallpaperCutActivity.class);
			intent.setAction(Intent.ACTION_ATTACH_DATA);
//			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			intent.setData(selectedImage);
			intent.putExtra(APPLY_WALLPAPER_LOCK_OR_NOT, isLcokApplyWallpaper);
			intent.putExtra(NEED_SHOW_TOAST_OR_NOT, isShowToast);
			startActivity(intent);
		}else {
			if(isLcokApplyWallpaper){
//				UIController.getInstance().lockKeyguardByOther();
			     Intent intent = new Intent(BlankActivity.this, KeyguardSettingsActivity.class);
		         intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		         startActivity(intent);
			}
		}

		finish();

	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		DebugLog.d(TAG, "onDestroy");
		isLcokApplyWallpaper=true;
		isShowToast=false;
	}

	public boolean isLcokApplyWallpaper() {
		return isLcokApplyWallpaper;
	}

	public void setLcokApplyWallpaper(boolean isLcokApplyWallpaper) {
		this.isLcokApplyWallpaper = isLcokApplyWallpaper;
	}
	
	
}
