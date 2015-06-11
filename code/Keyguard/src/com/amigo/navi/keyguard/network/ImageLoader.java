package com.amigo.navi.keyguard.network;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Map.Entry;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.KWDataCache;
import com.amigo.navi.keyguard.network.FailReason.FailType;
import com.amigo.navi.keyguard.network.local.DealWithFromLocalInterface;
import com.amigo.navi.keyguard.network.local.utils.DiskUtils;
import com.amigo.navi.keyguard.picturepage.widget.ImageViewWithLoadBitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.LruCache;

public class ImageLoader implements ImageLoaderInterface{
    private static final String LOG_TAG = "ImageLoader";
    private Context mContext;
    int maxMemory = (int) Runtime.getRuntime().maxMemory() / 1024;
    int mCacheSize = maxMemory / 4;
	private static final boolean PRINT_LOG = true;
	private ArrayList<ImageViewWithLoadBitmap> mImageViewWithLoadBitmapList = new ArrayList<ImageViewWithLoadBitmap>();
	private static final String THUMBNAIL_POSTFIX = "_thumbnail";
    public ImageLoader(Context context) {
    	mContext = context.getApplicationContext();
    }
    
   
    private HashMap<String, Bitmap> mFirstLevelCache = new HashMap<String, Bitmap>(); 
    
    /**
     * 清理缓存
     */
    public void clearCache() {
		mFirstLevelCache.clear();
		synchronized (ThumbRemoved) {
			ThumbRemoved.clear();
		}
		synchronized (ImageRemoved) {
			ImageRemoved.clear();
		}		
		System.gc();
	}

    public void removeItem(String url){
        mFirstLevelCache.remove(url);
    }
    
    /**
     * 放入缓存
     * 
     * @param url
     * @param value
     */
    public void addImage2Cache(String url, Bitmap value) {
        if (value == null || url == null) {
            return;
        }
        DebugLog.d(LOG_TAG,"addImage2Cache url:" + url);
        synchronized (mFirstLevelCache) {
/*        	String thumbUrl = url+THUMBNAIL_POSTFIX;
        	if (!url.endsWith(THUMBNAIL_POSTFIX) && mFirstLevelCache.containsKey(thumbUrl)){
        		Bitmap bmp = mFirstLevelCache.get(thumbUrl);
        		mFirstLevelCache.remove(thumbUrl);
        		ThumbRemoved.add(bmp);
        	}*/
        	mFirstLevelCache.put(url, value);
        }
    }

    
    public boolean existInImageCache(String url){
        DebugLog.d(LOG_TAG,"existInImageCache url:" + url);
    	return mFirstLevelCache.containsKey(url);
    }
    

    public void loadImageToView(ImageViewWithLoadBitmap imageViewWithLoadBitmap){
    	String url = imageViewWithLoadBitmap.getUrl();

        if (TextUtils.isEmpty(url)) {
            if(imageViewWithLoadBitmap != null){
                FailReason failReason = new FailReason(FailType.UNKNOWN, null);
                imageViewWithLoadBitmap.onLoadingFailed(url, failReason);
            }
            return;
        }
		if (!mImageViewWithLoadBitmapList.contains(imageViewWithLoadBitmap)) {
			mImageViewWithLoadBitmapList.add(imageViewWithLoadBitmap);
		}

    }

	public ImageViewWithLoadBitmap findView(String url) {
		if (url == null) {
			return null;
			}
			int size = mImageViewWithLoadBitmapList.size();
			
			for (int i = size - 1; i >= 0; i--) {
				ImageViewWithLoadBitmap view = mImageViewWithLoadBitmapList.get(i);
				if (view != null ) {
					if (url.equals ( view.getUrl() ) ){
						return view;
					}
				}
			}
		return null;
	}
	
	public void getImageView(String url) {
		ImageViewWithLoadBitmap view =  findView(url);
		if (view != null) {
			view.loadImageFromCacheIfNeeded();
		}
	}
	
	public void LoadingComplete(String url, Bitmap bitmap) {
			int size = mImageViewWithLoadBitmapList.size();
			for (int i = size - 1; i >= 0; i--) {
				ImageViewWithLoadBitmap view = mImageViewWithLoadBitmapList.get(i);
				if (view != null) {
					if (url.equals(view.getUrl())
							|| url.equals(view.getUrl() + THUMBNAIL_POSTFIX)) {
						if (bitmap == null) {
							FailReason failReason = new FailReason(
									FailType.UNKNOWN, null);
							view.onLoadingFailed(url, failReason);
						} else {
							view.onLoadingComplete(url, bitmap);
						}
					}
				}
			}
	}

	public void loadImageToCache(String url,
			DealWithFromLocalInterface dealWithFromLocalInterface) {
		Bitmap bitmap = loadImageFromLocal(dealWithFromLocalInterface, url);
        if(PRINT_LOG){
            DebugLog.d(LOG_TAG,"loadImageToCache:" + url);
        }
        if (null != bitmap) {
        	addImage2Cache(url, bitmap);
        }
		LoadingComplete(url, bitmap);
		

	}
    
    public Bitmap loadImageFromLocal(DealWithFromLocalInterface dealWithFromLocalInterface
            ,String url){
        Bitmap bitmap = null;
        String key = DiskUtils.constructFileNameByUrl(url);
        bitmap = dealWithFromLocalInterface.readFromLocal(key);
        return bitmap;
    }
    
    /**
     * 返回缓存，如果没有则返回null
     * 
     * @param url
     * @return
     */
    @Override
    public Bitmap getBitmapFromCache(String url) {
        Bitmap bitmap = null;
        bitmap = getFromFirstLevelCache(url);// 从一级缓存中拿
        if (bitmap != null) {
            return bitmap;
        }
        return bitmap;
    }


    /**
     * 从一级缓存中拿
     * 
     * @param url
     * @return
     */
    private Bitmap getFromFirstLevelCache(String url) {
        Bitmap bitmap = null;
        synchronized (mFirstLevelCache) {
            bitmap = mFirstLevelCache.get(url);
        }
        return bitmap;
    }
    
    public void printFirstCacheSize(){
//    	DebugLog.d(LOG_TAG,"printFirstCacheSize mCacheSize:" + mCacheSize);
//    	int maxSize = mFirstLevelCache.maxSize();
//    	DebugLog.d(LOG_TAG,"printFirstCacheSize maxSize:" + maxSize);
    }

	public void removeImagefromCache(ArrayList<String> urlToBeReserved) {
		int screenWid = KWDataCache.getScreenWidth(mContext.getResources());
		synchronized (mFirstLevelCache) {
			Iterator<Entry<String, Bitmap>> iter = mFirstLevelCache.entrySet()
					.iterator();

			ArrayList<String> urlToBeRemove = new ArrayList<String>();
			while (iter.hasNext()) {
				Entry<String, Bitmap> entry = (Entry<String, Bitmap>) iter
						.next();
				String url = (String) entry.getKey();
				if (PRINT_LOG) {
					DebugLog.d(LOG_TAG, "removeImagefromCache url in mFirstLevelCache" + url);
				}
				if (!urlToBeReserved.contains(url)) {
					if (PRINT_LOG) {
						DebugLog.d(LOG_TAG, "removeImagefromCache url" + url);
					}
					urlToBeRemove.add(url);
					
				}
			}

			for (int i = 0; i < urlToBeRemove.size(); i++) {
				String url = urlToBeRemove.get(i);
				Bitmap bitmap = mFirstLevelCache.get(url);
				if(bitmap.getWidth() == screenWid) {
					ImageViewWithLoadBitmap view =  findView(url);
					if (view != null){
						view.loadloadThumbnailFromCacheIfNeeded();
					}
					ImageRemoved.add(bitmap);
				} else {
					ThumbRemoved.add(bitmap);
				} 
				mFirstLevelCache.remove(url);
			}

			urlToBeRemove.clear();
		}
	}
	
	private ArrayList<Bitmap> ImageRemoved = new ArrayList<Bitmap>();
	private ArrayList<Bitmap> ThumbRemoved = new ArrayList<Bitmap>();

	public Bitmap getBmpFromImageRemoved() {
		Bitmap bmp = null;
		synchronized (ImageRemoved) {
			if (!ImageRemoved.isEmpty()) {
				bmp = ImageRemoved.get(0);
				ImageRemoved.remove(0);
			}
		}
		return bmp;
	}

	public Bitmap getBmpFromThumbRemoved() {
		Bitmap bmp = null;
		synchronized (ThumbRemoved) {
			if (!ThumbRemoved.isEmpty()) {
				bmp = ThumbRemoved.get(0);
				ThumbRemoved.remove(0);
			}
		}
		return bmp;
	}

}