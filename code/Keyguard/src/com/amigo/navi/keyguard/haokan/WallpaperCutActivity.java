
package com.amigo.navi.keyguard.haokan;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

import amigo.app.AmigoActivity;
import amigo.app.AmigoAlertDialog;
import amigo.widget.AmigoButton;
import amigo.app.AmigoProgressDialog;
import android.app.Activity;
import android.app.Dialog;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.KWDataCache;
import com.amigo.navi.keyguard.KeyguardViewHostManager;
 
import com.amigo.navi.keyguard.haokan.CropImageView;
import com.amigo.navi.keyguard.haokan.CutImageView;
import com.android.keyguard.R;

public class WallpaperCutActivity extends Activity {

    private static final String CUT_LOCK = "lock";
    protected static final String CUT_DESK_MUTI = "desk_muti";
    protected static final String CUT_DESK_SINGLE = "desk_single";
    private static final String CONTENT = "content:";
    private static final String TAG = "haokan";

    private CutImageView mImageView;
    protected CropImageView mCropImageView;

    private AmigoButton mApplyButton;
    private AmigoButton mCancelButton;
    private Button mCutLockButton;
    private Button mCutDeskButton;
    private Button mCutDeskSingelButton;
    private Button mCutDeskMutiButton;
    private View mCorpAllView;
    private View mCorpDeskOnlyView;
    private View mDeskMutiView;

    private String mCutType = CUT_DESK_MUTI;
    protected int mScreenWidth;
    protected int mScreenHeight;
    private int mBitmapWidth;
    private int mBitmapHeight;
    private int mTop;
    private int mViewHeight;
    private int mWidthEdge;
    private static final int PROGRESS_DIALOG = 1;
    private boolean mDestroyed = false;
    protected boolean mInit = false;

    private Handler mHandler = new Handler();
    
    private String filePath = null;

    private Runnable mToastRunnable = new Runnable() {
        @Override
        public void run() {
            cancelProgressDialog();
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); 
        requestWindowFeature(Window.FEATURE_NO_TITLE);  
        setContentView(R.layout.cut);

        mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getResources().getDisplayMetrics().heightPixels;
        DebugLog.d(TAG, "mScreenHeight=" + mScreenHeight);

        mImageView = (CutImageView) findViewById(R.id.image);
        mCropImageView = (CropImageView) findViewById(R.id.display_crop);

        
        Intent intent = getIntent();
        if (Intent.ACTION_ATTACH_DATA.equals(intent.getAction())) {
            filePath = intent.getDataString();
        }  
        Bitmap bmp = decodeBitmap(filePath, mScreenWidth, mScreenHeight);
        if (bmp == null) {
            Toast.makeText(getApplicationContext(), R.string.set_screenlock_wallpaper_fail, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            init(bmp);
            mCutType = CUT_DESK_SINGLE;
        }
    }

    private CutImageView.LayoutChangeListener mLayoutListener = new CutImageView.LayoutChangeListener() {

        @Override
        public void onLayoutChange() {
            editViewLayout();
        }
    };

    private void init(Bitmap bmp) {
        mBitmapWidth = bmp.getWidth();
        mBitmapHeight = bmp.getHeight();
        DebugLog.d(TAG, "mBitmapWidth=" + mBitmapWidth+",mBitmapHeight"+mBitmapHeight);
        
        initCutView(bmp);
        mApplyButton = (AmigoButton) findViewById(R.id.apply);
        mApplyButton.setOnClickListener(mApplyOnClickListener);
        mCancelButton = (AmigoButton) findViewById(R.id.cancel);
        mCancelButton.setOnClickListener(mCancelOnClickListener);

        mCutDeskButton = (Button) findViewById(R.id.cut_desk);
        mCutLockButton = (Button) findViewById(R.id.cut_lock);

        mCutDeskMutiButton = (Button) findViewById(R.id.cut_muti);
        mCutDeskSingelButton = (Button) findViewById(R.id.cut_single);

        mCorpAllView = findViewById(R.id.crop_all);
        mCorpDeskOnlyView = findViewById(R.id.crop_desk_only);
        mDeskMutiView = findViewById(R.id.crop_muti_desk);
        
        
        mCutDeskMutiButton.setOnClickListener(mCutDeskMutiOnClickListener);
        mCutDeskSingelButton.setOnClickListener(mCutDeskSingleOnClickListener);
        
    }

    private View.OnClickListener mCutDeskSingleOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            mCutType = CUT_DESK_SINGLE;
            editViewLayout();
        }
    };

    private View.OnClickListener mCutDeskMutiOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            mCutType = CUT_DESK_MUTI;
            editViewLayout();
        }
    };

    protected void setDesktopWallpaper(Bitmap bitmap, boolean isSingle) {
        WallpaperManager wpm = WallpaperManager.getInstance(getApplicationContext());
        Bitmap scaleBitmap = CommonUtils.scaleBitmap(bitmap, mScreenHeight);
        String cachePath = getCacheDir().getAbsolutePath();
        DebugLog.d(TAG, "cachePath=" + cachePath);
        File paperFile = CommonUtils.writeFile(scaleBitmap, cachePath,"Constants.DESK_NAME");
        try {
            int wallpaperWidth = mScreenWidth * 2;
            if (isSingle) {
                wallpaperWidth = mScreenWidth;
            }
            wpm.suggestDesiredDimensions(wallpaperWidth, (int) mScreenHeight);
            wpm.setStream(new FileInputStream(paperFile));
            boolean isDeleteOK = paperFile.delete();
            DebugLog.d(TAG, "setKeyguardWallpaper isDeleteOK=" + isDeleteOK);
        } catch (IOException e) {
            Log.w(TAG, "warning", e);
        }
    }

    private Bitmap decodeBitmap(String filePath, int w, int h) {
        Bitmap bitmap = null;
        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;
        try {
            if (filePath == null) {
                return null;
            }
            if (filePath.startsWith(CONTENT)) {
                return getContentBitmap(filePath, w, h);
            }

            DebugLog.d(TAG, "filePath=" + filePath);
            fileInputStream = new FileInputStream(filePath);
            bufferedInputStream = new BufferedInputStream(
                    fileInputStream);
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(bufferedInputStream, null, options);

            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inJustDecodeBounds = false;
            options.inDensity = options.outWidth;
            options.inTargetDensity = 2 * w;
            fileInputStream = new FileInputStream(filePath);
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            bitmap = BitmapFactory.decodeStream(bufferedInputStream, null,
                    options);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "", e);
        } finally {
            try {
                if (fileInputStream != null)
                    fileInputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "", e);
            }
            try {
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "", e);
            }
        }
        return bitmap;
    }

    private Bitmap getContentBitmap(String url, int w, int h) {
    	DebugLog.d(TAG, "url=" + url);
        Bitmap bitmap = null;
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor = getContentResolver().openFileDescriptor(Uri.parse(url), "r");
            FileDescriptor fd = parcelFileDescriptor.getFileDescriptor();
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(fd, null, options);
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inJustDecodeBounds = false;
            if (w > h) {
                options.inDensity = options.outWidth;
                options.inTargetDensity = 2 * w;
            } else {
                options.inDensity = options.outHeight;
                options.inTargetDensity = h;
            }
            bitmap = BitmapFactory.decodeFileDescriptor(fd, null, options);

        } catch (Exception e) {
            Log.e(TAG, "", e);
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "", e);
        } finally {
            try {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "", e);
            }
        }
        return bitmap;
    }

    private void setSrceenLockWallpaper(Bitmap bitmap) {
 
        if (bitmap != null) {
            KeyguardViewHostManager.getInstance().getKeyguardWallpaperManager().setSrceenLockWallpaper(bitmap);
        }
    }

    private Bitmap getCropBitmap() throws ArithmeticException {
        float widthr = (float) mBitmapWidth / (mScreenWidth - 2 * mWidthEdge);
        float heightr = (float) mBitmapHeight / mViewHeight;

        int xLeft = (int) ((mCropImageView.getXmin() - mWidthEdge) * widthr);
        int xRight = (int) ((mCropImageView.getXmax() - mWidthEdge) * widthr);
        int yBottom = (int) ((mCropImageView.getYmax() - mTop) * heightr);
        int yTop = (int) ((mCropImageView.getYmin() - mTop) * heightr);

        int width = xRight - xLeft;
        int height = yBottom - yTop;

        Bitmap source = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
        Bitmap newBitmap = null;
        Bitmap singleScreen = null;
        try {
            newBitmap = Bitmap.createBitmap(source, xLeft, yTop, width, height);
            DebugLog.d(TAG, "hanjuan="+"xLeft"+xLeft+"yTop"+yTop+"width"+width+"height"+height);
            if (newBitmap != null) {
                singleScreen = BitmapUtil.getResizedBitmapForSingleScreen(newBitmap,
                        KWDataCache.getScreenHeight(getResources()),
                        KWDataCache.getScreenWidth(getResources()));
            }
            BitmapUtil.recycleBitmap(newBitmap);
            BitmapUtil.recycleBitmap(source);
        } catch (OutOfMemoryError e) {
            Log.d(TAG, "", e);
        } catch (Exception e) {
            Log.d(TAG, "", e);
        }
        
        return singleScreen;
    }

    private View.OnClickListener mApplyOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (!Common.isFastClick(2000)) {
                apply();
            }
        }
    };
    
    private AmigoProgressDialog mProgressDialog;
    
    private void showProgressDialog() {
       if (mProgressDialog == null) {
            
            mProgressDialog = new AmigoProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.setting));
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }
    
    private void cancelProgressDialog() {
        if (mProgressDialog != null) {
             if (mProgressDialog.isShowing()) {
                 mProgressDialog.cancel();
            }
         }
     }
    
    
    
    
    private void apply() {
        if (!mInit) {
        	DebugLog.d(TAG, "apply"+"true");
            return;
        }

//        boolean storageDeviceAvail = DeviceStorageManager.getInstance().getStorageDeviceAvail();
//        if (!storageDeviceAvail) {
//            CommonUtils.makeToast(WallpaperCutActivity.this, R.string.no_access_sd);
//            return;
//        }

        showProgressDialog();
         
        final long startTime = System.currentTimeMillis();
        Thread setWallpaperThread = new Thread() {
            @Override
            public void run() {
                applyRun(startTime);
            };
        };
        setWallpaperThread.start();
    }

    private void applyRun(long startTime) {
        Bitmap bmp = getCropBitmap();
//        if (mCutType.equals(CUT_DESK_MUTI)) {
//            setDesktopWallpaper(bmp, false);
//        } else if (mCutType.equals(CUT_DESK_SINGLE)) {
//            setDesktopWallpaper(bmp, true);
//        } else if (mCutType.equals(CUT_LOCK)) {
//            setLockWallpaper(bmp);
//        }
        setSrceenLockWallpaper(bmp);
        long delta = System.currentTimeMillis() - startTime;
        long delay = 0;
        if (delta < 700) {
            delay = 700 - delta;
        }
        mHandler.postDelayed(mToastRunnable, delay);
    }

    private View.OnClickListener mCancelOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            cancelProgressDialog();
            finish();
        }
    };


    private void initCutView(Bitmap bitmap) {
        if (mCutType.equals(CUT_DESK_MUTI)) {
        	DebugLog.d(TAG, "initCutView=" + "if");
            float scaleW = ((float) mScreenWidth) / bitmap.getWidth();
            float rate = 1.0f * bitmap.getWidth() / bitmap.getHeight();
            if (rate > 0.9f) {
                mImageView.setLayoutParams(new FrameLayout.LayoutParams(mScreenWidth,
                        (int) (bitmap.getHeight() * scaleW), Gravity.CENTER));
            } else {
                float height = mScreenHeight / 1.8f;
                mImageView.setLayoutParams(new FrameLayout.LayoutParams((int) (height * rate),
                        (int) (height), Gravity.CENTER));
            }

            mImageView.setScaleType(ScaleType.FIT_XY);
            mImageView.setImageBitmap(bitmap);
        } else {
            mImageView.setScaleType(ScaleType.CENTER_INSIDE);
            mImageView.setImageBitmap(bitmap);
        }
        mImageView.setLayoutChangeListener(mLayoutListener);
    }

    protected void editViewLayout() throws ArithmeticException {
        View view = mImageView;
        float bitmapPropRation = ((float) mBitmapHeight) / mBitmapWidth;
        float singleScreenPropRation = ((float) mScreenHeight) / mScreenWidth;
        float fullScreenProp = singleScreenPropRation / 2;
        int height = (int) view.getY();
        try {
            mTop = height;
            mViewHeight = view.getHeight();
            mWidthEdge = (mScreenWidth - mViewHeight * mBitmapWidth / mBitmapHeight) / 2;
        } catch (ArithmeticException e) {
            e.printStackTrace();
        }

        float singScreenWidth;
        float singScreenHeight;
        float fullScreenWidth;
        float fullScreenHeight;
        if (bitmapPropRation > singleScreenPropRation) {
            singScreenWidth = fullScreenWidth = mScreenWidth - 2 * mWidthEdge;
            singScreenHeight = singScreenWidth * singleScreenPropRation;
            fullScreenHeight = fullScreenWidth * fullScreenProp;
        } else if (bitmapPropRation < fullScreenProp) {
            singScreenHeight = fullScreenHeight = mViewHeight;
            singScreenWidth = singScreenHeight / singleScreenPropRation;
            fullScreenWidth = fullScreenHeight / fullScreenProp;
        } else {
            singScreenHeight = mViewHeight;
            singScreenWidth = singScreenHeight / singleScreenPropRation;
            fullScreenWidth = mScreenWidth - 2 * mWidthEdge;
            fullScreenHeight = fullScreenWidth * fullScreenProp;
        }

        int centerX = mScreenWidth / 2;
        final float fullLeft = Math.max(centerX - fullScreenWidth / 2, 0);
        final float fullRight = Math.min(centerX + fullScreenWidth / 2, mScreenWidth);
        final float singleLeft = Math.max(centerX - singScreenWidth / 2, 0);
        final float singleRight = Math.min(centerX + singScreenWidth / 2, mScreenWidth);

        int centerY = mViewHeight / 2;
        final float fullTop = Math.max(mTop + centerY - fullScreenHeight / 2, mTop);
        final float fullBottom = Math
                .min(mTop + centerY + fullScreenHeight / 2, mTop + mViewHeight);
        final float singleTop = Math.max(mTop + centerY - singScreenHeight / 2, mTop);
        final float singleBottom = Math
                .min(mTop + mViewHeight / 2 + singScreenHeight / 2, mTop + mViewHeight);

        mCropImageView.initXY(mWidthEdge, mScreenWidth - mWidthEdge, mTop, mViewHeight + mTop);

        setCropScrean(fullLeft, fullRight, singleLeft, singleRight, fullTop,
                fullBottom, singleTop, singleBottom);
    }

    protected void setCropScrean(final float fullLeft, final float fullRight,
            final float singleLeft, final float singleRight,
            final float fullTop, final float fullBottom, final float singleTop,
            final float singleBottom) {
        if (mCutType.equals(CUT_DESK_MUTI)) {
            mCropImageView.setFullOrSingScreen(fullLeft, fullRight, fullTop, fullBottom);
        } else {
            mCropImageView.setFullOrSingScreen(singleLeft, singleRight, singleTop, singleBottom);
        }
        mInit = true;
    }

    @Override
    protected void onDestroy() {
        mDestroyed = true;
        super.onDestroy();
    }

   
}

