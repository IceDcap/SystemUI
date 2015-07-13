package com.amigo.navi.keyguard.picturepage.widget;

import java.util.ArrayList;
import java.util.Vector;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;


import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.haokan.entity.Wallpaper;
import com.amigo.navi.keyguard.haokan.entity.WallpaperList;
import com.amigo.navi.keyguard.network.FailReason;
import com.amigo.navi.keyguard.network.ImageLoader;
import com.amigo.navi.keyguard.network.FailReason.FailType;
import com.amigo.navi.keyguard.network.local.DealWithFromLocalInterface;
import com.amigo.navi.keyguard.network.local.LocalBitmapOperation;
import com.amigo.navi.keyguard.network.local.LocalFileOperationInterface;
import com.amigo.navi.keyguard.network.local.ReadAndWriteFileFromSD;
import com.amigo.navi.keyguard.network.local.ReadFileFromAssets;
import com.amigo.navi.keyguard.network.local.ReuseImage;
import com.amigo.navi.keyguard.network.local.utils.DiskUtils;
import com.amigo.navi.keyguard.network.theardpool.Job;
import com.amigo.navi.keyguard.network.theardpool.LoadImagePool;
import com.amigo.navi.keyguard.network.theardpool.LoadImageThread;

public class LoadCacheManager {
	protected static final String LOG_TAG = "LoadCacheManager";
	private WallpaperList mWallpaperList;
	private Context mContext;
	private ImageLoader mImageLoader;
	private static final String PATH = "wallpaper_pics";
	ArrayList<Wallpaper> mWallpaperListToImageCache = new ArrayList<Wallpaper>();
	ArrayList<Wallpaper> mWallpaperListToThumbCache = new ArrayList<Wallpaper>();
	ArrayList<String> mUrlToBeReserved = new ArrayList<String>();
	private static final String THUMBNAIL_POSTFIX = "_thumbnail";

	private Wallpaper mCurrentWallpaper;
	
	public void refreshCache(Context context, ImageLoader imageLoader,
			WallpaperList wallpaperList, Wallpaper wallpaper,
			boolean isScreenOff) {
		mWallpaperListToImageCache.clear();
		mWallpaperListToThumbCache.clear();
		mContext = context;
		mImageLoader = imageLoader;
		mCurrentWallpaper = wallpaper;
		mImageLoader.mRelease = false;
		mWallpaperList = wallpaperList;
		if (DebugLog.DEBUGMAYBE) {
			for (int i = 0; i < wallpaperList.size(); i++) {
				DebugLog.d(LOG_TAG,
						"refreshCache url wallpaperlist:"
								+ wallpaperList.get(i).getImgName()
								+ wallpaperList.get(i));
			}
		}

		int currentPos = -1;
		for (int i = 0; i < wallpaperList.size(); i++) {
			if (wallpaper.getImgId() == wallpaperList.get(i).getImgId()) {
				currentPos = i;
				break;
			}
		}
		if (DebugLog.DEBUG) {
			DebugLog.d(LOG_TAG, "refreshCache url:" + wallpaper.getImgUrl()
					+ " name:" + wallpaper.getImgName() + currentPos);
		}
		mImageLoader.setmCurrentUrl(wallpaper.getImgUrl());
		getLoadImageList(currentPos, isScreenOff);
		startRemoveFromCache();
		startLoadToCache();

	}

	private void getLoadImageList(int currentPos, boolean isScreenOff) {
		if (isScreenOff) {
			getLoadImageToList(currentPos, false);
			return;
		}
		//getLoadImageToList(currentPos + 1, true);
		//getLoadImageToList(currentPos - 1, true);
		getLoadImageToList(currentPos, true);
		if (mWallpaperList.size() > 1) {
			getLoadImageToList(currentPos, false);
			getLoadImageToList(currentPos + 1, false);
			getLoadImageToList(currentPos - 1, false);
			getLoadImageToList(currentPos + 2, false);
			getLoadImageToList(currentPos - 2, false);
			//getLoadImageToList(currentPos + 3, false);
			//getLoadImageToList(currentPos - 3, false);
		}
	}

	private void getLoadImageToList(int page, boolean isImage) {
		page = getFinalPosition(page);
		final Wallpaper wallpaper = mWallpaperList.get(page);

		if (wallpaper == null) {
			return;
		}
		if (DebugLog.DEBUG) {
			DebugLog.d(LOG_TAG, "getLoadImageList url:" + wallpaper.getImgUrl()
					+ " name:" + wallpaper.getImgName());
		}
		if (isImage) {
			mWallpaperListToImageCache.add(wallpaper);
		} else {
			mWallpaperListToThumbCache.add(wallpaper);
		}
	}

	public void startLoadToCache() {
		LoadImagePool.getInstance(mContext.getApplicationContext()).cancelAllThread();
		for (int i = 0; i < mWallpaperListToThumbCache.size(); i++) {
			Wallpaper wallpaper = mWallpaperListToThumbCache.get(i);
			if (DebugLog.DEBUG) {
				DebugLog.d(
						LOG_TAG,
						"startLoadToCache mWallpaperListToThumbCache url:"
								+ wallpaper.getImgUrl() + " name:"
								+ wallpaper.getImgName());
			}
			loadPageToCache(wallpaper, false, false);
		}
		for (int i = 0; i < mWallpaperListToImageCache.size(); i++) {
			Wallpaper wallpaper = mWallpaperListToImageCache.get(i);
			if (DebugLog.DEBUG) {
				DebugLog.d(
						LOG_TAG,
						"startLoadToCache mWallpaperListToImageCache url:"
								+ wallpaper.getImgUrl() + " name:"
								+ wallpaper.getImgName());
			}
			loadPageToCache(wallpaper, true, false);
		}
	}

	private void startRemoveFromCache() {
		mUrlToBeReserved.clear();
		
		for (int i = 0; i < mWallpaperListToImageCache.size(); i++) {
			Wallpaper wallpaper = mWallpaperListToImageCache.get(i);
			String reservedUrl = wallpaper.getImgUrl();
			if (mImageLoader.existInImageCache(reservedUrl)) {
				if (DebugLog.DEBUG){
					DebugLog.d(LOG_TAG,"mUrlToBeReserved existInImageCache url:" + reservedUrl);
		        }
				mUrlToBeReserved.add(reservedUrl);
			}else{
				mUrlToBeReserved.add(wallpaper.getImgUrl() + THUMBNAIL_POSTFIX);
			}
		}
		
		for (int i = 0; i < mWallpaperListToThumbCache.size(); i++) {
			Wallpaper wallpaper = mWallpaperListToThumbCache.get(i);
			mUrlToBeReserved.add(wallpaper.getImgUrl() + THUMBNAIL_POSTFIX);
		}

		mImageLoader.removeImagefromCache(mUrlToBeReserved);
	}

	public void loadPageToCache(final Wallpaper wallpaper,
			final boolean isImage, final boolean reload) {
		if (mImageLoader != null) {
			String loadUrl = wallpaper.getImgUrl();


			if (!isImage) {
				loadUrl = loadUrl + THUMBNAIL_POSTFIX;
			}

			if (DebugLog.DEBUG) {
				DebugLog.d(LOG_TAG, "loadPageToCache load image thread url:"
						+ loadUrl);
				DebugLog.d(LOG_TAG,
						"loadPageToCache load image thread getImageName():"
								+ wallpaper.getImgName());
			}
			if (mImageLoader.existInImageCache(loadUrl)) {
				if (DebugLog.DEBUG) {
					DebugLog.d(LOG_TAG, "load image thread url existInImageCache:"
							+ loadUrl);
					}
				if (isImage) {
					mImageLoader.getImageView(wallpaper.getImgUrl());
				}
				return;
			}
			final String needLoadingUrl = loadUrl;
			Job job = new Job() {
                private boolean isStop = false;
				@Override
				public void runTask() {
					DealWithFromLocalInterface readImageFromLocal = null;
					Bitmap bmp = null;
					if (isStop){
						if (DebugLog.DEBUG) {
							DebugLog.d(LOG_TAG, "cancel load image thread url:"
									+ needLoadingUrl + wallpaper.getImgName());
						}
						return;
					}
 
					if (DebugLog.DEBUG) {
						DebugLog.d(LOG_TAG, "load image thread url:"
								+ needLoadingUrl + " time begin:"
								+ System.currentTimeMillis());
						DebugLog.d(LOG_TAG, "load image thread getType():"
								+ wallpaper.getType() + "; getImageName():"+ wallpaper.getImgName());
					}
					if (wallpaper.getType() == Wallpaper.WALLPAPER_FROM_FIXED_FOLDER
							&& (!needLoadingUrl.endsWith(THUMBNAIL_POSTFIX))) {
						ReadFileFromAssets readFileFromAssets =  new ReadFileFromAssets(
								mContext.getApplicationContext(), PATH);
						
						readImageFromLocal = readFileFromAssets;
						 
					} else {
						ReadAndWriteFileFromSD dealWithFromLocalInterface = null;
						LocalFileOperationInterface localFileOperationInterface = new LocalBitmapOperation(
								mContext);
						dealWithFromLocalInterface = new ReadAndWriteFileFromSD(
								mContext.getApplicationContext(),
								DiskUtils.WALLPAPER_BITMAP_FOLDER,
								DiskUtils.getCachePath(mContext.getApplicationContext()),
								localFileOperationInterface);
						readImageFromLocal = dealWithFromLocalInterface;
						 
					}
					
					if (isImage) {
					    bmp = mImageLoader.getBmpFromImageRemoved();
                    }else {
                        bmp = mImageLoader.getBmpFromThumbRemoved();
                    }
			        
					
			        if (isStop){
			            mImageLoader.addBmpToImageRemoved(bmp);
			            return;
                    } 
			        ReuseImage reuseImage = new ReuseImage(bmp);
                    readImageFromLocal.setReuseBitmap(reuseImage);

                    Bitmap bitmap = mImageLoader.loadImageFromLocal(readImageFromLocal,
                            needLoadingUrl);
                    boolean isUsed = reuseImage.isUsed();
                    DebugLog.d(LOG_TAG, " isUsed:" + isUsed + " Url = " + needLoadingUrl);
                    if (!isUsed) {
                        mImageLoader.addBmpToImageRemoved(reuseImage.getBitmap());
                    }

                    if (null != bitmap) {
                        mImageLoader.LoadingComplete(needLoadingUrl, bitmap);
                        
                    } else {
                        
                        if (!reload) {
                            FailReason failReason = new FailReason(
                                    FailType.UNKNOWN, null);
                            mImageLoader.LoadingFailed(needLoadingUrl, failReason);
                        }
                    }
					
					

					if (DebugLog.DEBUG) {
						DebugLog.d(
								LOG_TAG,
								"load image thread url:"
										+ needLoadingUrl + " time end:"
										+ System.currentTimeMillis());
					}
				}

				@Override
				public int getProgress() {
					return 0;
				}

				@Override
				public void cancelTask() {
					isStop = true;
				}
			};
			Vector<LoadImageThread> threadList = null;
			threadList = LoadImagePool.getInstance(mContext)
					.getDownLoadThreadList();
			LoadImageThread loadImageThread = new LoadImageThread(
					needLoadingUrl, job, threadList);
			LoadImagePool.getInstance(mContext.getApplicationContext())
					.loadImage(loadImageThread, needLoadingUrl);
		}

	}

	private int getFinalPosition(int position) {
		int size = mWallpaperList.size();
		int pos = position;
		if (position < 0) {
			pos = position + size;
		}
		if (pos >= size) {
			pos = position % size;
		}
		return pos;
	}
}
