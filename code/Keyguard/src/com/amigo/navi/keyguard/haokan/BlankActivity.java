package com.amigo.navi.keyguard.haokan;


import com.amigo.navi.keyguard.DebugLog;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class BlankActivity extends Activity {
	
	private static final String TAG = "BlankActivity";
	private static final int REQUEST_CODE = 1001;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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
			 
			Intent intent = new Intent(this, WallpaperCutActivity.class);
			intent.setAction(Intent.ACTION_ATTACH_DATA);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setData(selectedImage);
			startActivity(intent);
		}else {
			UIController.getInstance().lockKeyguardByOther();
		}
		finish();

	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		DebugLog.d(TAG, "onDestroy");
	}
}
