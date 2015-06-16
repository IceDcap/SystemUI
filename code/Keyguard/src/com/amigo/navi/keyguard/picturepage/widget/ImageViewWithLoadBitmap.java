package com.amigo.navi.keyguard.picturepage.widget;
import java.io.File;
import java.io.FileInputStream;
import java.util.Vector;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.KWDataCache;
import com.amigo.navi.keyguard.haokan.BitmapUtil;
import com.amigo.navi.keyguard.haokan.FileUtil;
import com.amigo.navi.keyguard.haokan.UIController;
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
import com.amigo.navi.keyguard.network.manager.DownLoadBitmapManager;
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

public class ImageViewWithLoadBitmap extends ImageView implements OnReloadListener, ImageLoadingListener{
    
    public enum State { LOADING, LOADED, CANCELLED, FAILED};
    public enum ShowState { SHOW_NOIMAGE, SHOW_THUMBNAIL, SHOW_IMAGE};
    
//    private Bitmap mImageBitmap;
    private static final String LOG_TAG = "ImageViewWithLoadBitmap";
    // private int mWid = 0;
    // private int mHei = 0;
    private String mUrl = "";
    private Config mConfig;
    private String mColor;
    
    private State mLoadState = State.CANCELLED;

	private int mWidth = 0;
    private int mHeight = 0;
//    private static final String PATH = "wallpaper_pics";
    private static final boolean isPrintLog = true;
	private static final String THUMBNAIL_POSTFIX = "_thumbnail";
    public ImageViewWithLoadBitmap(Context context) {
        super(context);
        init();
    }

    public ImageViewWithLoadBitmap(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImageViewWithLoadBitmap(Context context, AttributeSet attrs,
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
    private static final int REFRESH_LISTVIEW = 3;

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
                loadBitmapCompleteDealWith(url, bitmap);
                break;
            case GET_BITMAP_START:
                //loadStartDealWith(url);
                break;
            default:
                break;
            }
            handlerObj.setBitmap(null);
            handlerObj.setFailReason(null);
            handlerObj.setUrl(null);
        }

    };

    private int mPosOfListener; // 0:left 1.currentPage 2.right
    private Wallpaper mWallPaper;
    private ShowState mShowState = ShowState.SHOW_NOIMAGE;//0:loading 1.thumbnail 2.image
    
    public ShowState getmShowState() {
		return mShowState;
	}

	public void setmShowState(ShowState mShowState) {
		this.mShowState = mShowState;
	}

    public void release() {
        setImageBitmap(null);
        mUrl = null;
        setTag(null);
        setmShowState(ShowState.SHOW_NOIMAGE);
    }
	
    public void loadImageBitmap() {
        if(mWallPaper != null){
            loadImageBitmap(mWallPaper);
        }
    }
    public void loadImageBitmap(Wallpaper wallPaper) {
        loadImageBitmap(wallPaper/*,mPosOfListener*/, true);
    }
    
    public void loadImageBitmap(Wallpaper wallpaper/*, int posOfListener*/,  boolean immediatelyLoad) {
//    	mPosOfListener = posOfListener;
        mWallPaper = wallpaper;
        this.mUrl = wallpaper.getImgUrl();
        this.setTag(mUrl);
        if(isPrintLog){
            DebugLog.d(LOG_TAG,"loadImageBitmap mUrl" + mUrl);
        }
        this.setmShowState(ShowState.SHOW_NOIMAGE);
	    mConfig.mImageLoader.loadImageToView(this/*, posOfListener*/);
        boolean isLoadFromCache = loadFromCache();
        if (!isLoadFromCache){ 
        	boolean isLoadThumbnailFromCache = loadThumbnailFromCache();
        	if (!isLoadThumbnailFromCache){
        		this.setImageResource(mConfig.startBitmapID);
        		this.setmShowState(ShowState.SHOW_NOIMAGE);
        	}else{
        		 this.setmShowState(ShowState.SHOW_THUMBNAIL);
        	}
        }else{
            this.setmShowState(ShowState.SHOW_IMAGE);
        }
    }

    public void setBitmap(Bitmap bitmap) {
        this.setImageBitmap(bitmap);
    }

    
    private void setImageBackground() {
        if (mColor != null && !"".equals(mColor)) {
            this.setBackgroundColor(Color.parseColor(mColor));
        }
    }

    private void loadFailDealWith(String url, FailReason failReason) {
        Log.v(LOG_TAG, "loadFailDealWith url:" + url);
        
        String currentUrl = mConfig.mImageLoader.getmCurrentUrl();
        if (url.equals(mUrl)) {
            if (!currentUrl.equals(mUrl)) {
                return;
            }
        }
        boolean isImage = !url.endsWith(THUMBNAIL_POSTFIX);
        mConfig.mImageLoader.loadPageToCache(getWallPaper(), isImage);
        
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
//            recyleImageBitmap();
    		setImageResource(mConfig.failBitmapID);
        } else {
        	
        }
    }

//    private void recyleImageBitmap() {
//        mImageBitmap = null;
//    }

    public void recyleBitmap() {
//        mImageBitmap = null;
        mConfig.setStartBitmap(null);
        mConfig.setFailBitmap(null);
    }
    
	private void loadBitmapCompleteDealWith(String url, Bitmap loadedImage) {
        if(isPrintLog){
            DebugLog.d(LOG_TAG,"loadBitmapCompleteDealWith url:" + url);
        }
        
        String currentUrl = mConfig.mImageLoader.getmCurrentUrl();
        if (url.equals(mUrl)) {
        	if (!currentUrl.equals(mUrl)) {
        		//mConfig.mImageLoader.addBmpToImageRemoved(loadedImage);
        		return;
        	}
        	//mConfig.mImageLoader.addImage2Cache(url, loadedImage);
        }
        
        
		if (this.getmShowState() == ShowState.SHOW_IMAGE) {
	        if(isPrintLog){
	            DebugLog.d(LOG_TAG,"loadBitmapCompleteDealWith url SHOW_IMAGE");
	        }
//			if (url.equals(this.getUrl())) {
//				if (loadedImage != null) {
//					recyleImageBitmap();
//					mImageBitmap = loadedImage;
//					setBitmap(mImageBitmap);
//					setmShowState(ShowState.SHOW_IMAGE);
//				}
//			}
			return;
		}

		loadBitmapCompleted(url, loadedImage);
		
	}

	private void loadBitmapCompleted(String url, Bitmap loadedImage) {
		if (isPrintLog) {
			DebugLog.d(LOG_TAG, "loadBitmapCompleted url " + url);
		}
		if (url.equals(this.getUrl())) {
			if (loadedImage != null) {
				boolean isSuccess = loadFromCache();
				if (isSuccess) {
					setmShowState(ShowState.SHOW_IMAGE);
				}
				if (isPrintLog) {
					DebugLog.d(LOG_TAG, "loadBitmapCompleted url SHOW_IMAGE");
				}
			}
		} else if (url.equals(this.getUrl() + THUMBNAIL_POSTFIX)) {
			if (loadedImage != null) {
				boolean isSuccess = loadThumbnailFromCache();
				if (isSuccess) {
					setmShowState(ShowState.SHOW_THUMBNAIL);
				}
				if (isPrintLog) {
					DebugLog.d(LOG_TAG,
							"loadBitmapCompleted url SHOW_THUMBNAIL");
				}
			}
		} else {

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
    

    public void setPosOfListener(int position) {
        this.mPosOfListener = position;
    }
    
    public int getPosOfListener(){
    	return mPosOfListener;
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
            loadImageBitmap(mWallPaper);
        }
        return super.onTouchEvent(event);
    }

    private boolean loadFromCache(){
        DebugLog.d(LOG_TAG,"loadFromCache mUrl:" + mUrl + " mConfig:" + mConfig);
        DebugLog.d(LOG_TAG,"loadFromCache mUrl:" + mUrl + " mConfig.mImageLoader:" + mConfig.mImageLoader);
        if(mConfig == null || mConfig.mImageLoader == null){
            return false;
        }
        Bitmap bitmap = mConfig.mImageLoader.getBitmapFromCache(mWallPaper.getImgUrl());
        DebugLog.d(LOG_TAG,"loadFromCache mUrl:" + mUrl + "loadImageBitmap bitmap:" + bitmap);
        if (bitmap != null) {
            this.setUrl(mWallPaper.getImgUrl());
            this.setImageBitmap(bitmap);
            return true;
        }else{
//            this.setBackGroundColorAndRemoveImage(mWallPaper);
//            loadStart();
            return false;
        }
    }

	public void loadloadThumbnailFromCacheIfNeeded() {
		if (ShowState.SHOW_IMAGE == getmShowState()) {
			boolean isRefresh = loadThumbnailFromCache();
			if (isPrintLog) {
				DebugLog.d(LOG_TAG, "loadThumbnailToRefresh isRefresh:"
						+ isRefresh);
			}
			if (isRefresh) {
				setmShowState(ShowState.SHOW_THUMBNAIL);
			}else{
        		this.setImageResource(mConfig.startBitmapID);
				setmShowState(ShowState.SHOW_NOIMAGE);
			}
		}
	}

	public void loadImageFromCacheIfNeeded() {
		if (ShowState.SHOW_IMAGE != getmShowState()) {
			boolean isRefresh = loadFromCache();
			if (isPrintLog) {
				DebugLog.d(LOG_TAG, "loadImageToRefresh isRefresh:" + isRefresh);
			}
			if (isRefresh) {
				setmShowState(ShowState.SHOW_IMAGE);
			}
		}
	}
/*	public boolean isStateShowImage() {
		return ShowState.SHOW_IMAGE == mShowState;
	}*/
	
    private boolean loadThumbnailFromCache(){
        DebugLog.d(LOG_TAG,"loadThumbnailFromCache loadFromCache mUrl:" + mUrl + " mConfig:" + mConfig);
        DebugLog.d(LOG_TAG,"loadThumbnailFromCache loadFromCache mUrl:" + mUrl + " mConfig.mImageLoader:" + mConfig.mImageLoader);
        if(mConfig == null || mConfig.mImageLoader == null){
            return false;
        }
        Bitmap bitmap = mConfig.mImageLoader.getBitmapFromCache(mWallPaper.getImgUrl()+ THUMBNAIL_POSTFIX);
        DebugLog.d(LOG_TAG,"loadThumbnailFromCache loadFromCache mUrl:" + mUrl + "loadImageBitmap bitmap:" + bitmap);
        if (bitmap != null) {
            this.setUrl(mWallPaper.getImgUrl());
            this.setImageBitmap(bitmap);
            return true;
        }else{
//            this.setBackGroundColorAndRemoveImage(mWallPaper);
//            loadStart();
            return false;
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

	@Override
	public void onLoadingStarted(String imageUri) {
/*		if (isPrintLog) {
			DebugLog.d(LOG_TAG, "onLoadingStarted imageUri:" + imageUri);
		}
		HandlerObj handlerObj = new HandlerObj();
		handlerObj.setUrl(imageUri);
		Message message = mHandle.obtainMessage(GET_BITMAP_START);
		message.obj = handlerObj;
		message.sendToTarget();*/

	}

	@Override
	public void onLoadingFailed(String imageUri, FailReason failReason) {
		// WallpaperDB.getInstance(getContext().getApplicationContext()).updateDownLoadNotFinish(wallpaper);
		mLoadState = State.FAILED;
		if (isPrintLog) {
			DebugLog.d(LOG_TAG, "onLoadingFailed imageUri:" + imageUri);
		}
		 
		int what = GET_BITMAP_FAIL;

		HandlerObj handlerObj = new HandlerObj();
		handlerObj.setUrl(imageUri);
		handlerObj.setFailReason(failReason);
		Message message = mHandle.obtainMessage(what);
		message.obj = handlerObj;
		message.sendToTarget();
	}

	 

	@Override
	public void onLoadingComplete(String imageUri, Bitmap loadedImage) {
		mLoadState = State.LOADED;
		if (isPrintLog) {
			DebugLog.d(LOG_TAG, "onLoadingComplete imageUri:" + imageUri);
			DebugLog.d(LOG_TAG, "onLoadingComplete loadedImage:" + loadedImage);
		}
		HandlerObj handlerObj = new HandlerObj();
		handlerObj.setUrl(imageUri);
		handlerObj.setBitmap(loadedImage);
		Message message = mHandle.obtainMessage(GET_BITMAP_SUCCESS);
		message.obj = handlerObj;
		message.sendToTarget();
	}

	@Override
	public void onLoadingCancelled(String imageUri) {
		if (isPrintLog) {
			DebugLog.d(LOG_TAG, "onLoadingCancelled onLoadingCancelled");
		}
		mLoadState = State.CANCELLED;
	}

    public Wallpaper getWallPaper() {
        return mWallPaper;
    }

    public void setWallPaper(Wallpaper mWallPaper) {
        this.mWallPaper = mWallPaper;
    }
 
	
}
