package com.amigo.navi.keyguard.picturepage.widget;
import java.util.Vector;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.KWDataCache;
import com.amigo.navi.keyguard.haokan.db.WallpaperDB;
import com.amigo.navi.keyguard.haokan.entity.Wallpaper;
import com.amigo.navi.keyguard.network.FailReason;
import com.amigo.navi.keyguard.network.ImageLoaderInterface;
import com.amigo.navi.keyguard.network.ImageLoadingListener;
import com.amigo.navi.keyguard.network.local.DealWithFromLocalInterface;
import com.amigo.navi.keyguard.network.local.ReadFileFromAssets;
import com.amigo.navi.keyguard.network.local.ReadAndWriteFileFromSD;
import com.amigo.navi.keyguard.network.local.LocalBitmapOperation;
import com.amigo.navi.keyguard.network.local.LocalFileOperationInterface;
import com.amigo.navi.keyguard.network.local.utils.DiskUtils;
import com.amigo.navi.keyguard.network.theardpool.Job;
import com.amigo.navi.keyguard.network.theardpool.LoadImagePool;
import com.amigo.navi.keyguard.network.theardpool.LoadImageThread;
import com.amigo.navi.keyguard.picturepage.interfacers.OnReloadListener;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class ImageViewForHeadCompound extends ImageView implements OnReloadListener{
    
    public enum State { LOADING, LOADED, CANCELLED, FAILED};
    
    private Bitmap mImageBitmap;
    private static final String LOG_TAG = "ImageViewForHeadCompound";
    // private int mWid = 0;
    // private int mHei = 0;
    private String mUrl = "";
    private Config mConfig;
    private String mColor;
    
    private State mLoadState = State.CANCELLED;
    
    private int mWidth = 0;
    private int mHeight = 0;
    private static final String PATH = "wallpaper_pics";
    public ImageViewForHeadCompound(Context context) {
        super(context);
        init();
    }

    public ImageViewForHeadCompound(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImageViewForHeadCompound(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mWidth = KWDataCache.getScreenWidth(getResources());
        mHeight = KWDataCache.getAllScreenHeigt(getContext());
    }
    
    public void setConfig(Config config) {
        this.mConfig = config;
    }

    public void setBackGroundColor(String color) {
        if (color != null && !"".equals(color)) {
            this.setBackgroundColor(Color.parseColor(color));
        }
    }

    public void setBackGroundColorAndRemoveImage(Wallpaper paper) {
        if (paper != null) {
            if (mUrl.equals(paper.getImgUrl())) {
                return;
            }
            mUrl = paper.getImgUrl();
            setImageBitmap(null);
        }
    }

    public void setUrl(String url) {
        this.mUrl = url;
    }

    private static final int GET_BITMAP_FAIL = 0;
    private static final int GET_BITMAP_SUCCESS = 1;
    private static final int GET_BITMAP_START = 2;
    private Handler mHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            HandlerObj handlerObj = (HandlerObj) msg.obj;
            String url = handlerObj.getUrl();
            Bitmap bitmap = handlerObj.getBitmap();
            FailReason failReason = handlerObj.getFailReason();
            switch (what) {
            case GET_BITMAP_FAIL:
                loadFailDealWith(url, failReason);
                break;
            case GET_BITMAP_SUCCESS:
                Log.v(LOG_TAG,
                        "handleMessage this.hashCode():" + this.hashCode());
                loadBitmapCompleteDealWith(url, bitmap);
                break;
            case GET_BITMAP_START:
                loadStartDealWith(url);
                break;
            default:
                break;
            }
            handlerObj.setBitmap(null);
            handlerObj.setFailReason(null);
            handlerObj.setUrl(null);
        }

    };

    private int mPosition;
    private Wallpaper mWallPaper;
    private boolean mNeedCache;
    
    public void loadImageBitmap() {
        if(mWallPaper != null){
            loadImageBitmap(mWallPaper, true);
        }
    }

    public void loadImageBitmap(Wallpaper wallPaper, boolean isNeedCache) {
        loadImageBitmap(wallPaper, isNeedCache, true);
    }
    
    public void loadImageBitmap(Wallpaper wallpaper, boolean isNeedCache, boolean immediatelyLoad) {
        mWallPaper = wallpaper;
        mNeedCache = isNeedCache;
        DebugLog.d(LOG_TAG,"makeAndAddView loadImageBitmap mUrl:" + mUrl);
        DebugLog.d(LOG_TAG,"makeAndAddView loadImageBitmap wallpaper.getImgUrl():" + wallpaper.getImgUrl());
        boolean isCurUrl = mUrl.equals(wallpaper.getImgUrl());
        DebugLog.d(LOG_TAG,"makeAndAddView loadImageBitmap wallpaper.isCurUrl:" + isCurUrl);
        if (!isCurUrl) {
            this.setImageResource(mConfig.startBitmapID);
        }
        DebugLog.d(LOG_TAG,"loadImageBitmap mConfig:" + mConfig);
        if (mConfig == null) {
            mConfig = new Config();
        }
        this.mUrl = wallpaper.getImgUrl();
        DebugLog.d(LOG_TAG,"loadImageBitmap mUrl:" + mUrl);
        this.setTag(mUrl);
        DebugLog.d(LOG_TAG,"loadImageBitmap immediatelyLoad:" + immediatelyLoad);
        DebugLog.d(LOG_TAG,"makeAndAddView mUrl:" + mUrl + "loadImageBitmap immediatelyLoad:" + immediatelyLoad);
        if(immediatelyLoad){
            immediateLoadImageBitmap(wallpaper, isNeedCache);
        } else {
            loadFromCache();
        }
    }

    private void immediateLoadImageBitmap(final Wallpaper wallpaper, final boolean isNeedCache) {
        DebugLog.d(LOG_TAG,"makeAndAddView immediateLoadImageBitmap");
        mLoadState = State.LOADING;
        final ImageLoadingListener loadingListener = new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri) {
                DebugLog.d("HorizontalListView","makeAndAddView onLoadingStarted");
                Log.v(LOG_TAG, "loadImageBitmap onLoadingStarted");
                HandlerObj handlerObj = new HandlerObj();
                handlerObj.setUrl(imageUri);
                Message message = mHandle.obtainMessage(GET_BITMAP_START);
                message.obj = handlerObj;
                message.sendToTarget();

            }

            @Override
            public void onLoadingFailed(String imageUri, FailReason failReason) {
                DebugLog.d("HorizontalListView","makeAndAddView onLoadingFailed");
                WallpaperDB.getInstance(getContext().getApplicationContext()).updateDownLoadNotFinish(wallpaper);
                mLoadState = State.FAILED;
                Log.v(LOG_TAG, "loadImageBitmap onLoadingFailed imageUri:" + imageUri);
                HandlerObj handlerObj = new HandlerObj();
                handlerObj.setUrl(imageUri);
                handlerObj.setFailReason(failReason);
                Message message = mHandle.obtainMessage(GET_BITMAP_FAIL);
                message.obj = handlerObj;
                message.sendToTarget();
            }

            @Override
            public void onLoadingComplete(String imageUri, Bitmap loadedImage) {
                // TODO Auto-generated method stub
                mLoadState = State.LOADED;
                Log.v(LOG_TAG, "loadImageBitmap onLoadingComplete imageUri:"
                        + imageUri);
                Log.v(LOG_TAG, "onLoadingComplete loadedImage:" + loadedImage);
                DebugLog.d("HorizontalListView","makeAndAddView imageUri:" + imageUri);
                DebugLog.d("HorizontalListView","makeAndAddView loadedImage:" + loadedImage);
                HandlerObj handlerObj = new HandlerObj();
                handlerObj.setUrl(imageUri);
                handlerObj.setBitmap(loadedImage);
                Message message = mHandle.obtainMessage(GET_BITMAP_SUCCESS);
                message.obj = handlerObj;
                message.sendToTarget();
            }

            @Override
            public void onLoadingCancelled(String imageUri) {
                DebugLog.d("HorizontalListView","makeAndAddView onLoadingCancelled");
                mLoadState = State.CANCELLED;
            }
        };
        if (mConfig.mImageLoader != null) {
            Job job = new Job() {
                
                @Override
                public void runTask() {
                    DealWithFromLocalInterface readImageFromLocal = null;
                    DebugLog.d(LOG_TAG,"load image thread url:" + wallpaper.getImgUrl()  + " time begin:" + System.currentTimeMillis());
                    DebugLog.d(LOG_TAG,"load image thread getType():" + wallpaper.getType());
                    if(wallpaper.getType() == Wallpaper.WALLPAPER_FROM_FIXED_FOLDER){
                	 readImageFromLocal = new ReadFileFromAssets(getContext().getApplicationContext(), PATH);
                 }else{
                	 readImageFromLocal = mConfig.getReadFromSD();
                 }
                 
                  mConfig.mImageLoader.loadImage(wallpaper.getImgUrl(), loadingListener,
                  readImageFromLocal, isNeedCache);       
                  DebugLog.d(LOG_TAG,"load image thread url:" + wallpaper.getImgUrl()  + " time end:" + System.currentTimeMillis());
                }
                
                @Override
                public int getProgress() {
                    return 0;
                }
                
                @Override
                public void cancelTask() {
                    
                }
            };
        	Vector<LoadImageThread> threadList = null;
            threadList = LoadImagePool.getInstance(getContext())
                    .getDownLoadThreadList();
            LoadImageThread loadImageThread = new LoadImageThread(wallpaper.getImgUrl(), job,threadList);
			LoadImagePool.getInstance(getContext().getApplicationContext()).loadImage(loadImageThread, wallpaper.getImgUrl());
        }
    }

    public void setBitmap(Bitmap bitmap) {
        this.setImageBitmap(bitmap);
    }

    
    private void loadStartDealWith(String url) {
        Log.v(LOG_TAG, "loadImageBitmap loadStartDealWith url:" + url);
        Log.v(LOG_TAG, "loadImageBitmap loadStartDealWith this.getTag():"
                + this.getTag());
        Log.v(LOG_TAG, "loadImageBitmap loadStartDealWith this.hashCode():"
                + this.hashCode());
        if (this.getTag() != null && url.equals(this.getTag())) {
            loadStart();
        } else if (this.getTag() == null) {
            loadStart();
        }
    }

    private void loadStart() {
//        if (mConfig.startBitmap != null) {
//            recyleImageBitmap();
//            mImageBitmap = mConfig.startBitmap;
//            setBitmap(mImageBitmap);
//        } else {
////            setImageResource(R.drawable.emotion_loading);
//        }
    	
    DebugLog.d(LOG_TAG,"loadFromCache mUrl:" + mUrl + "loadImageBitmap mConfig.startBitmapID:" + mConfig.startBitmapID);
  	if(mConfig.startBitmapID != 0){
          recyleImageBitmap();
  		  setImageResource(mConfig.startBitmapID);
      } else {

      }
    }

    private void setImageBackground() {
        if (mColor != null && !"".equals(mColor)) {
            this.setBackgroundColor(Color.parseColor(mColor));
        }
    }

    private void loadFailDealWith(String url, FailReason failReason) {
        Log.v(LOG_TAG, "loadFailDealWith url:" + url);
        if (this.getTag() != null && url.equals(this.getTag())) {
            loadFail();
        } else if (this.getTag() == null) {
            loadFail();
        }
    }

    private void loadFail() {
//        if (mConfig.failBitmap != null) {
//            recyleImageBitmap();
//            mImageBitmap = mConfig.failBitmap;
//            setBitmap(mImageBitmap);
//        } else {
////            setImageResource(R.drawable.emotion_load_fail);
//        }
    	
    	if(mConfig.failBitmapID != 0){
            recyleImageBitmap();
    		setImageResource(mConfig.failBitmapID);
        } else {
        	
        }
    }

    private void recyleImageBitmap() {
        mImageBitmap = null;
    }

    public void recyleBitmap() {
        mImageBitmap = null;
        mConfig.setStartBitmap(null);
        mConfig.setFailBitmap(null);
    }

    private void loadBitmapCompleteDealWith(String url, Bitmap loadedImage) {
        if (this.getTag() != null && url.equals(this.getTag())) {
            loadBitmapCompleted(url, loadedImage);
        } else if (this.getTag() == null) {
            loadBitmapCompleted(url, loadedImage);
        }
    }

    private void loadBitmapCompleted(String imageUri, Bitmap loadedImage) {
        if (loadedImage != null) {
            recyleImageBitmap();
            mImageBitmap = loadedImage;
        }
        if (mImageBitmap != null) {
            setBitmap(mImageBitmap);
        } else {
            loadFailDealWith(imageUri, null);
        }
    }

    class HandlerObj {

        private String url;
        private Bitmap bitmap;
        private FailReason failReason;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public void setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        public FailReason getFailReason() {
            return failReason;
        }

        public void setFailReason(FailReason failReason) {
            this.failReason = failReason;
        }
    }

    public static class Config {
        private Bitmap startBitmap;
        private Bitmap failBitmap;
        private int headCompoundMode;
        private ImageLoaderInterface mImageLoader;
        private boolean isNeedReLoadByClick = true;
        private DealWithFromLocalInterface readFromSD;
        private DealWithFromLocalInterface readFromAssets;
        public int startBitmapID;
        public int failBitmapID;
        public Bitmap getStartBitmap() {
            return startBitmap;
        }

        public void setStartBitmap(Bitmap startBitmap) {
            this.startBitmap = startBitmap;
        }

        public Bitmap getFailBitmap() {
            return failBitmap;
        }

        public void setFailBitmap(Bitmap failBitmap) {
            this.failBitmap = failBitmap;
        }

        public int getHeadCompoundMode() {
            return headCompoundMode;
        }

        public void setHeadCompoundMode(int headCompoundMode) {
            this.headCompoundMode = headCompoundMode;
        }

        public ImageLoaderInterface getmImageLoader() {
            return mImageLoader;
        }

        public void setImageLoader(ImageLoaderInterface mImageLoader) {
            this.mImageLoader = mImageLoader;
        }

        public boolean isNeedReLoadByClick() {
            return isNeedReLoadByClick;
        }

        public void setNeedReLoadByClick(boolean isNeedReLoadByClick) {
            this.isNeedReLoadByClick = isNeedReLoadByClick;
        }

		public DealWithFromLocalInterface getReadFromSD() {
			return readFromSD;
		}

		public void setReadFromSD(DealWithFromLocalInterface readFromSD) {
			this.readFromSD = readFromSD;
		}

		public DealWithFromLocalInterface getReadFromAssets() {
			return readFromAssets;
		}

		public void setReadFromAssets(DealWithFromLocalInterface readFromAssets) {
			this.readFromAssets = readFromAssets;
		}

    }
    
    public void addImage2Cache() {
        if (mConfig.mImageLoader != null && mUrl != null && mLoadState == State.LOADED) {
            mConfig.mImageLoader.addImage2Cache(mUrl, mImageBitmap);
        }
    }

    public void removeCache() {
        Log.v(LOG_TAG, "removeCache mImageLoader:" + mConfig.mImageLoader);
        Log.v(LOG_TAG, "removeCache mUrl1:" + mUrl);
        if (mConfig.mImageLoader != null && mUrl != null) {
            Log.v(LOG_TAG, "removeCache mUrl2:" + mUrl);
            mConfig.mImageLoader.removeItem(mUrl);
        }
    }
    

    public void setPosition(int position) {
        this.mPosition = position;
    }

    public String getUrl() {
        return mUrl;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        if (!mConfig.isNeedReLoadByClick) {
            return super.onTouchEvent(event);
        }

        if (mConfig != null && mLoadState == State.FAILED) {
            loadImageBitmap(mWallPaper, mNeedCache);
        }
        return super.onTouchEvent(event);
    }

    private void loadFromCache(){
        DebugLog.d(LOG_TAG,"loadFromCache mUrl:" + mUrl + " mConfig:" + mConfig);
        DebugLog.d(LOG_TAG,"loadFromCache mUrl:" + mUrl + " mConfig.mImageLoader:" + mConfig.mImageLoader);
        if(mConfig == null || mConfig.mImageLoader == null){
            return;
        }
        Bitmap bitmap = mConfig.mImageLoader.getBitmapFromCache(mWallPaper.getImgUrl());
        DebugLog.d(LOG_TAG,"loadFromCache mUrl:" + mUrl + "loadImageBitmap bitmap:" + bitmap);
        if (bitmap != null) {
            this.setUrl(mWallPaper.getImgUrl());
            this.setImageBitmap(bitmap);
        }else{
            this.setBackGroundColorAndRemoveImage(mWallPaper);
            loadStart();
        }
    }

    @Override
    public void onReload() {
        // TODO Auto-generated method stub
        loadImageBitmap();
    }
 
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        widthMeasureSpec  = MeasureSpec.makeMeasureSpec(
                mWidth, MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(
                mHeight, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    
}
