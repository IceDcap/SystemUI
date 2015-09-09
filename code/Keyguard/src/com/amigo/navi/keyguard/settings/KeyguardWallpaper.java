package com.amigo.navi.keyguard.settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;


import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.amigo.navi.keyguard.DebugLog;

public class KeyguardWallpaper {
	 private static final String wallpaperName =
	 "/data/data/com.gionee.navi.keyguard/wallpaper.jpg";
//	private static final String wallpaperName = "/sdcard/wallpaper.jpg";
	private static final String TAG = "KeyguardWallpaper";

	// 获取当前壁纸
	public static void getWallpaper(Context context) {
		Bitmap wallpaper = getWallpaperBmp(context);
		saveWallpaper(wallpaper);
	}

	// 获取当前壁纸
	public static Drawable getWallpaperDrawable(Context context) {
		WallpaperManager wallpaperManager = WallpaperManager
				.getInstance(context); // 获取壁纸管理器
		Drawable wallpaperDrawable = wallpaperManager.getDrawable();// 获取当前壁纸
		return wallpaperDrawable;
	}
	
	public static Bitmap getWallpaperBmp(Context context) {
		WallpaperManager wallpaperManager = WallpaperManager
				.getInstance(context); // 获取壁纸管理器
		Drawable wallpaperDrawable = wallpaperManager.getDrawable();// 获取当前壁纸
		BitmapDrawable Bitmapdrawable = (BitmapDrawable) wallpaperDrawable;
		Bitmap bitmap = Bitmapdrawable.getBitmap();
		return bitmap;
	}
	
	private static void saveWallpaper(Bitmap bitmap) {
		File f = new File(wallpaperName);
		try {
			f.createNewFile();
		} catch (IOException e) {
		}
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
		try {
			fOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Bitmap getBlurBitmap(Bitmap blurBitmapOut, float blurRatio) {
		try {
			Class c = Class.forName("amigo.widget.blur.AmigoBlur");
			Method m = c.getMethod("getInstance");
			m.setAccessible(true);
			Object obj = m.invoke(c);
			Method method = c.getMethod("nativeProcessBitmap", Bitmap.class,
					int.class, int.class, int.class, int.class);
			method.setAccessible(true);
			method.invoke(obj, blurBitmapOut, 24, blurBitmapOut.getWidth(),
					blurBitmapOut.getHeight(), (int) blurRatio);
		} catch (Exception e) {
			DebugLog.mustLog(TAG, "getBitmapError-->"+ e);
		}
		DebugLog.mustLog(TAG, "-----getBlurBitmap end");
		return blurBitmapOut;
	}

}
