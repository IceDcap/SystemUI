package com.amigo.navi.keyguard.picturepage.widget;

import java.util.ArrayList;
import java.util.Vector;

import android.content.Context;
import android.graphics.Bitmap;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.haokan.entity.Wallpaper;
import com.amigo.navi.keyguard.haokan.entity.WallpaperList;
import com.amigo.navi.keyguard.network.ImageLoader;
import com.amigo.navi.keyguard.network.local.DealWithFromLocalInterface;
import com.amigo.navi.keyguard.network.local.LocalBitmapOperation;
import com.amigo.navi.keyguard.network.local.LocalFileOperationInterface;
import com.amigo.navi.keyguard.network.local.ReadAndWriteFileFromSD;
import com.amigo.navi.keyguard.network.local.ReadFileFromAssets;
import com.amigo.navi.keyguard.network.local.utils.DiskUtils;
import com.amigo.navi.keyguard.network.theardpool.Job;
import com.amigo.navi.keyguard.network.theardpool.LoadImagePool;
import com.amigo.navi.keyguard.network.theardpool.LoadImageThread;

public class LoadCacheManager {
	protected static final boolean isPrintLog = true;
	protected static final String LOG_TAG = "LoadCacheManager";
	private WallpaperList mWallpaperList;
	private Context mContext;
	private ImageLoader mImageLoader;
	private static final String PATH = "wallpaper_pics";
	ArrayList<Wallpaper> mWallpaperListToImageCache = new ArrayList<Wallpaper>();
	ArrayList<Wallpaper> mWallpaperListToThumbCache = new ArrayList<Wallpaper>();
	ArrayList<String> mUrlToBeReserved = new ArrayList<String>();
	private static final String THUMBNAIL_POSTFIX = "_thumbnail";

	public void refreshCache(Context context, ImageLoader imageLoader,
			WallpaperList wallpaperList, Wallpaper wallpaper,
			boolean isScreenOff) {
		mWallpaperListToImageCache.clear();
		mWallpaperListToThumbCache.clear();
		mContext = context;
		mImageLoader = imageLoader;

		mWallpaperList = wallpaperList;
		if (isPrintLog) {
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
		if (isPrintLog) {
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
		if (isPrintLog) {
			DebugLog.d(LOG_TAG, "getLoadImageList url:" + wallpaper.getImgUrl()
					+ " name:" + wallpaper.getImgName());
		}
		if (isImage) {
			mWallpaperListToImageCache.add(wallpaper);
		} else {
			mWallpaperListToThumbCache.add(wallpaper);
		}
	}

	private void startLoadToCache() {
		LoadImagePool.getInstance(mContext.getApplicationContext()).cancelAllThread();
		for (int i = 0; i < mWallpaperListToThumbCache.size(); i++) {
			Wallpaper wallpaper = mWallpaperListToThumbCache.get(i);
			if (isPrintLog) {
				DebugLog.d(
						LOG_TAG,
						"startLoadToCache mWallpaperListToThumbCache url:"
								+ wallpaper.getImgUrl() + " name:"
								+ wallpaper.getImgName());
			}
			loadPageToCache(wallpaper, false);
		}
		for (int i = 0; i < mWallpaperListToImageCache.size(); i++) {
			Wallpaper wallpaper = mWallpaperListToImageCache.get(i);
			if (isPrintLog) {
				DebugLog.d(
						LOG_TAG,
						"startLoadToCache mWallpaperListToImageCache url:"
								+ wallpaper.getImgUrl() + " name:"
								+ wallpaper.getImgName());
			}
			loadPageToCache(wallpaper, true);
		}
	}

	private void startRemoveFromCache() {
		mUrlToBeReserved.clear();
		
		for (int i = 0; i < mWallpaperListToImageCache.size(); i++) {
			Wallpaper wallpaper = mWallpaperListToImageCache.get(i);
			String reservedUrl = wallpaper.getImgUrl();
			if (mImageLoader.existInImageCache(reservedUrl)) {
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

	private void loadPageToCache(final Wallpaper wallpaper,
			final boolean isImage) {
		if (mImageLoader != null) {
			String loadUrl = wallpaper.getImgUrl();
			if (isPrintLog) {
				DebugLog.d(LOG_TAG, "loadPageToCache load image thread url:"
						+ wallpaper.getImgUrl());
				DebugLog.d(LOG_TAG,
						"loadPageToCache load image thread getImageName():"
								+ wallpaper.getImgName());
			}

			if (!isImage) {
				loadUrl = loadUrl + THUMBNAIL_POSTFIX;
			}

			if (mImageLoader.existInImageCache(loadUrl)) {
				
				mImageLoader.getImageView(wallpaper.getImgUrl());
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
						if (isPrintLog) {
							DebugLog.d(LOG_TAG, "cancel load image thread url:"
									+ needLoadingUrl + wallpaper.getImgName());
						}
						return;
					}
					if (isPrintLog) {
						DebugLog.d(LOG_TAG, "load image thread url:"
								+ needLoadingUrl + " time begin:"
								+ System.currentTimeMillis());
						DebugLog.d(LOG_TAG, "load image thread getType():"
								+ wallpaper.getType());
						DebugLog.d(LOG_TAG, "load image thread getImageName():"
								+ wallpaper.getImgName());
					}
					if (wallpaper.getType() == Wallpaper.WALLPAPER_FROM_FIXED_FOLDER
							&& (!needLoadingUrl.endsWith(THUMBNAIL_POSTFIX))) {
						ReadFileFromAssets readFileFromAssets =  new ReadFileFromAssets(
								mContext.getApplicationContext(), PATH);
						
						readImageFromLocal = readFileFromAssets;
						bmp = mImageLoader.getBmpFromImageRemoved();
						readFileFromAssets.setmReuseBitmap(bmp);
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
						if(needLoadingUrl.endsWith(THUMBNAIL_POSTFIX)){
							bmp = mImageLoader.getBmpFromThumbRemoved();
							if (isPrintLog) {
								DebugLog.d(
										LOG_TAG,
										"mImageLoader.getBmpFromThumbRemoved():"+ bmp);
							}
							dealWithFromLocalInterface.setmReuseBitmap(bmp);
							
						}else{
							bmp = mImageLoader.getBmpFromImageRemoved();
							if (isPrintLog) {
								DebugLog.d(
										LOG_TAG,
										"mImageLoader.getBmpFromImageRemoved():"+ bmp);
							}
							dealWithFromLocalInterface.setmReuseBitmap(bmp);
						}
					}

					boolean isNullReturned = mImageLoader.loadImageToCache(needLoadingUrl,
							readImageFromLocal, isStop);
					if (isNullReturned) {
						mImageLoader.addBmpToImageRemoved(bmp);
					}
					if (isPrintLog) {
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
					wallpaper.getImgUrl(), job, threadList);
			LoadImagePool.getInstance(mContext.getApplicationContext())
					.loadImage(loadImageThread, wallpaper.getImgUrl());
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
