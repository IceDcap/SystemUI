package com.android.systemui.gionee;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Bitmap.Config;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.WindowManager;

public class GnBlurHelper {
    
    protected static final String TAG = "GnBlurHelper";
    
    public final static float ZOOM_FRAC = 16.0f;
    
    public static Bitmap mBlur = null;
    public static final Object LOCK = new Object();
    private static ExecutorService mExecutorService = Executors.newCachedThreadPool();
    private static final ArrayList<Callback> mCallbacks = new ArrayList<Callback>();
    
    private static GnBlurHelper sInstance = new GnBlurHelper();
    public interface Callback {
        public void completeBlur();
    }
    
    private GnBlurHelper() {
    	
    }
    
    public static GnBlurHelper getBlurHelper() {
    	return sInstance;
    }
    
    public void createBlurBg(final Context context) {
    	mExecutorService.execute(new Runnable() {
			
			@Override
			public void run() {
				Bitmap screenShot = takeScreenShot(context);
				if (screenShot != null) {
					Bitmap zoomBitmap = zoomInBitmap(1 / GnBlurHelper.ZOOM_FRAC, screenShot);
					blur(zoomBitmap, 2, context);
				}
			}
		});
    }
    
    public void blur(final Bitmap background, final int iBlur, final Context context) {
        
        mExecutorService.execute(new Runnable() {
            
            public void run() {          
                
                if (null == background || background.isRecycled()) {
                    Log.d(TAG, "background == null is " + (background == null) + ",may be OutOfMemory.");
                    return;
                }
                
                amigo.widget.blur.AmigoBlur blur = amigo.widget.blur.AmigoBlur.getInstance();
                blur.generateBlurBitmap(background, context.getResources(), iBlur,
                        new amigo.widget.blur.AmigoBlur.BitmapCallback() {
                    
                            @Override
                            public void onComplete(final Bitmap completeBmp) {
                                Log.d(TAG, "completeDrawable = " + (completeBmp != null));
                                
                                synchronized (LOCK) {
                                    if (null != mBlur && !mBlur.isRecycled()) {
                                        Log.d(TAG, "mBlur  recycle");
                                        mBlur.recycle();
                                        mBlur = null;
                                    }
                                }
                                
                                mBlur = zoomInBitmap(ZOOM_FRAC, completeBmp);

								for (Callback callback : mCallbacks) {
									callback.completeBlur();
								}
                                releaseBitmap(background);
                                releaseBitmap(completeBmp);
                            }
                        });
            }
        });
    }
    
    public static void releaseBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
    }
    
    public static void addCallbacks(Callback cb) {
    	mCallbacks.add(cb);
    }

    public static Bitmap zoomInBitmap(float zoom, Bitmap bmp) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        float scale = zoom;
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0,
                width, height, matrix, true);
        return resizedBitmap;
    }
    
    public static Bitmap takeScreenShot(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getRealMetrics(displayMetrics);
        float[] dims = { displayMetrics.widthPixels, displayMetrics.heightPixels };
        float degrees = getDegreesForRotation(display.getRotation());

        boolean needRotate = (degrees > 0f);
        if (needRotate) {
            Matrix matrix = new Matrix();
            matrix.reset();
            matrix.preRotate(-degrees);
            matrix.mapPoints(dims);
            dims[0] = Math.abs(dims[0]);
            dims[1] = Math.abs(dims[1]);
        }
        
        Bitmap bitmap = null;
        // Take the screenshot
        bitmap = SurfaceControl.screenshot((int) dims[0], (int) dims[1]);
        if (bitmap == null) {
            Log.e(TAG, "takeScreenShot bitmap = null");
            return null;
        }
        
        if (needRotate) {
            Bitmap ss = Bitmap.createBitmap(displayMetrics, displayMetrics.widthPixels,
                    displayMetrics.heightPixels, Config.ARGB_8888);
            Canvas c = new Canvas(ss);
            c.translate(ss.getWidth() / 2, ss.getHeight() / 2);
            c.rotate(degrees);
            c.translate(-dims[0] / 2, -dims[1] / 2);
            c.drawBitmap(bitmap, 0, 0, null);
            c.setBitmap(null);
            // Recycle the previous bitmap
            bitmap.recycle();
            bitmap = ss;
        }
        bitmap.setHasAlpha(false);
        bitmap.prepareToDraw();
        
        return bitmap;
    }
    
    private static float getDegreesForRotation(int value) {
        switch (value) {
            case Surface.ROTATION_90:
                return 360f - 90f;
            case Surface.ROTATION_180:
                return 360f - 180f;
            case Surface.ROTATION_270:
                return 360f - 270f;
        }
        return 0f;
    }
    
}