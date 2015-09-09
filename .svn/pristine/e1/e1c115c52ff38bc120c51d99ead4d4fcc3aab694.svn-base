package com.amigo.navi.keyguard.security;


import java.util.ArrayList;

import com.amigo.navi.keyguard.haokan.UIController;
import com.amigo.navi.keyguard.settings.KeyguardSettings;
import com.amigo.navi.keyguard.settings.KeyguardSettingsActivity;
import com.amigo.navi.keyguard.settings.KeyguardWallpaper;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.R;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import amigo.app.AmigoActivity;
import android.view.View.OnClickListener;
import amigo.app.AmigoActionBar;


public class AmigoUnBindAcountActivity  extends AmigoActivity {
	
	  private Bitmap mWindowBackgroud;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);          
//        getWindow().setStatusBarColor(getResources().getColor(R.color.keyguard_unbind_account_color));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        AmigoActionBar actionBar = getAmigoActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setTitle(R.string.forget_password);


		setContentView(R.layout.amigo_unbind_account);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
/*	private void setActionBar(){
		  AmigoActionBar actionBar = getAmigoActionBar();

	      actionBar.setDisplayHomeAsUpEnabled(true);
	      actionBar.setDisplayShowCustomEnabled(true);
	      actionBar.setTitle(R.string.forget_password);
	      actionBar.setOnBackClickListener(new OnClickListener() {
	          @Override
	          public void onClick(View v) {
	        		finish();
	          }
	      });
	}*/
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	private void setBlurBackground() {
        this.getWindow().setBackgroundDrawable(null);

        Bitmap bitmap = UIController.getInstance().getCurrentWallpaperBitmap(this.getApplicationContext(), true);
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

	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	    if (mWindowBackgroud != null && !mWindowBackgroud.isRecycled()) {
            mWindowBackgroud.recycle();
        }
	}
	
	
	 private KeyguardUpdateMonitorCallback mInfoCallback = new KeyguardUpdateMonitorCallback() {
		   public void onScreenTurnedOff(int why) {
			   finish();
		   };
	 };
	 
	 public void onAttachedToWindow() {
		  KeyguardUpdateMonitor.getInstance(AmigoUnBindAcountActivity.this).registerCallback(mInfoCallback);
	 };
	 
	 @Override
	public void onDetachedFromWindow() {
		// TODO Auto-generated method stub
		super.onDetachedFromWindow();
		  KeyguardUpdateMonitor.getInstance(AmigoUnBindAcountActivity.this).registerCallback(mInfoCallback);
	}
	 
	


}
