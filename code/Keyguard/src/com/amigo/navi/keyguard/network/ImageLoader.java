package com.amigo.navi.keyguard.network;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import com.amigo.navi.keyguard.KeyguardViewHostManager;
import com.amigo.navi.keyguard.haokan.BitmapUtil;
import com.amigo.navi.keyguard.haokan.FileUtil;
import com.amigo.navi.keyguard.haokan.db.WallpaperDB;
import com.amigo.navi.keyguard.haokan.entity.Wallpaper;
import com.amigo.navi.keyguard.haokan.entity.WallpaperList;
import com.amigo.navi.keyguard.network.FailReason.FailType;
import com.amigo.navi.keyguard.network.local.DealWithFromLocalInterface;
import com.amigo.navi.keyguard.network.local.utils.DiskUtils;
import com.amigo.navi.keyguard.network.manager.DownLoadBitmapManager;
import com.amigo.navi.keyguard.picturepage.adapter.HorizontalAdapter;
import com.amigo.navi.keyguard.picturepage.widget.ImageViewWithLoadBitmap;
import com.amigo.navi.keyguard.picturepage.widget.LoadCacheManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import android.view.View;

public class ImageLoader implements ImageLoaderInterface{
    private static final String LOG_TAG = "ImageLoader";
    private Context mContext;
    int maxMemory = (int) Runtime.getRuntime().maxMemory() / 1024;
    int mCacheSize = maxMemory / 4;
	private static final boolean PRINT_LOG = true;
	private ArrayList<ImageViewWithLoadBitmap> mImageViewWithLoadBitmapList = new ArrayList<ImageViewWithLoadBitmap>();
	private static final String THUMBNAIL_POSTFIX = "_thumbnail";
	private boolean mNeedInit = true;
	private String mCurrentUrl = null;
	
	private LoadCacheManager mCacheManger;
    public String getmCurrentUrl() {
		return mCurrentUrl;
	}

	public void setmCurrentUrl(String mCurrentUrl) {
		this.mCurrentUrl = mCurrentUrl;
	}

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
			mNeedInit = true;
		}	
		synchronized (mImageViewWithLoadBitmapList) {
			mImageViewWithLoadBitmapList.clear();
			DebugLog.d(LOG_TAG,"mImageViewWithLoadBitmapList size :" + mImageViewWithLoadBitmapList.size());
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

	public void getImageView(String url) {
		if (url == null || url != mCurrentUrl) {
			return;
		}
		int size = mImageViewWithLoadBitmapList.size();
		for (int i = size - 1; i >= 0; i--) {
			ImageViewWithLoadBitmap view = mImageViewWithLoadBitmapList.get(i);
			if (view != null) {
				if (url.equals(view.getUrl())) {
					view.loadImageFromCacheIfNeeded();
				}
			}
		}

	}
	
	public void LoadingFailed(String url, FailReason failReason) {
	     
        if(PRINT_LOG){
            DebugLog.d(LOG_TAG,"LoadingFailed url:" + url);
        }
		int size = mImageViewWithLoadBitmapList.size();
		ImageViewWithLoadBitmap imageViewWithLoadBitmap = null;
		for (int i = size - 1; i >= 0; i--) {
			ImageViewWithLoadBitmap view = mImageViewWithLoadBitmapList.get(i);
			if (view != null) {
				if (url.equals(view.getUrl()) || url.equals(view.getUrl() + THUMBNAIL_POSTFIX)) {
				    imageViewWithLoadBitmap = view;
				    break;
				}
			}
		}
		
		if (imageViewWithLoadBitmap != null) {
		    boolean success = onLoadingFailed(imageViewWithLoadBitmap, url, failReason);
		    Log.v("haokan", "LoadingFailed & success = " + success);
		    if (success) {
		        imageViewWithLoadBitmap.onLoadingFailed(url, failReason);
		    }
        }
	}
	
	public void LoadingComplete(String url, Bitmap bitmap) {
        if(PRINT_LOG){
            DebugLog.d(LOG_TAG,"LoadingComplete url:" + url);
        }
			if (url.endsWith(THUMBNAIL_POSTFIX)){
				addImage2Cache(url, bitmap);
			} else {
				if (url.equals(mCurrentUrl)) {
					addImage2Cache(url, bitmap);
				} else {
					addBmpToImageRemoved(bitmap);
					return;
				}
			}
			
			synchronized (mImageViewWithLoadBitmapList) {
				int size = mImageViewWithLoadBitmapList.size();
				for (int i = size - 1; i >= 0; i--) {
					ImageViewWithLoadBitmap view = mImageViewWithLoadBitmapList.get(i);
					if (view != null) {
						if (url.equals(view.getUrl())
								|| url.equals(view.getUrl() + THUMBNAIL_POSTFIX)) {
							{
						        if(PRINT_LOG){
						            DebugLog.d(LOG_TAG,"LoadingComplete size:" + size +";index :" +i);
						        }
								view.onLoadingComplete(url, bitmap);
							}
							
						}
					}
				}
			}
	}

	public boolean loadImageToCache(String url,
			DealWithFromLocalInterface dealWithFromLocalInterface, boolean isStoped) {
		boolean isNullReturned = true;
        if(PRINT_LOG){
            DebugLog.d(LOG_TAG,"loadImageToCache isStoped:" + isStoped);
        }
		if (!isStoped) {
			Bitmap bitmap = loadImageFromLocal(dealWithFromLocalInterface, url);
	        if(PRINT_LOG){
	            DebugLog.d(LOG_TAG,"loadImageToCache:" + url);
	        }
	/*        if (null != bitmap) {
	        	addImage2Cache(url, bitmap);
	        }*/
	        if (null != bitmap) {
	        	LoadingComplete(url, bitmap);
	        	isNullReturned = false;
	        }else {
				FailReason failReason = new FailReason(
						FailType.UNKNOWN, null);
	        	LoadingFailed(url, failReason);
	        }
		} 
		return isNullReturned;
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

					if (url == null) {
						continue;
						}
						int size = mImageViewWithLoadBitmapList.size();
						for (int j = size - 1; j >= 0; j--) {
							ImageViewWithLoadBitmap view = mImageViewWithLoadBitmapList.get(j);
							if (view != null ) {
								if (url.equals (view.getUrl()) ){
									view.loadloadThumbnailFromCacheIfNeeded();
								}
							}
						}
				}  
				addBmpToImageRemoved(bitmap);
				mFirstLevelCache.remove(urlToBeRemove.get(i));
			}

			urlToBeRemove.clear();
		}
	}
	
	private ArrayList<Bitmap> ImageRemoved = new ArrayList<Bitmap>();
	private ArrayList<Bitmap> ThumbRemoved = new ArrayList<Bitmap>();
	private Bitmap mImageReuseBmp = null;

	public Bitmap getBmpFromImageRemoved() {
		Bitmap bmp = null;
		synchronized (ImageRemoved) {
			if (!mNeedInit) {
				while (ImageRemoved.isEmpty()) {
					try {
						DebugLog.d(LOG_TAG, "getBmpFromImageRemoved wait before");
						ImageRemoved.wait();
						DebugLog.d(LOG_TAG, "getBmpFromImageRemoved wait after");
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				bmp = ImageRemoved.get(0);
				ImageRemoved.remove(0);
			}else{
				mNeedInit = false;
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
	
	public void addBmpToImageRemoved(Bitmap bmp) {
	    if (bmp == null) {
            return;
        }
		int screenWid = KWDataCache.getScreenWidth(mContext.getResources());
		if (bmp.getWidth() == screenWid) {
			synchronized (ImageRemoved) {
			    if (ImageRemoved.size() <= 1) {
			        ImageRemoved.add(bmp);
			    }
				ImageRemoved.notify();
				DebugLog.d(LOG_TAG, "getBmpFromImageRemoved notify");
			}
		} else {
		 
		    if (ThumbRemoved.size() <= 2) {
		        ThumbRemoved.add(bmp);
            }
		}
	}

    public LoadCacheManager getCacheManger() {
        return mCacheManger;
    }

    public void setCacheManger(LoadCacheManager mCacheManger) {
        this.mCacheManger = mCacheManger;
    }

    
    private boolean onLoadingFailed(ImageViewWithLoadBitmap view, String url,FailReason failReason) {

        final Wallpaper wallpaper = view.getWallPaper();
        if (wallpaper != null) {
            
            Log.v("haokan", "onLoadingFailed url = " + url);
            boolean isThumbnail = url.endsWith(THUMBNAIL_POSTFIX);
            
            int type = wallpaper.getType();
            
            if (type == Wallpaper.WALLPAPER_FROM_WEB) {
                boolean isExistOriginal = false;
                if (isThumbnail) {
                    isExistOriginal = getThumbWhenLoadingFailed(wallpaper);
                    if (!isExistOriginal) {
                        boolean success = downWallPaperWhenLoadingFailed(wallpaper.getImgUrl());
                        return success;
                    }
                }else {
                    boolean success = downWallPaperWhenLoadingFailed(wallpaper.getImgUrl());
                    return success;
                }
                 
            }else if (type == Wallpaper.WALLPAPER_FROM_PHOTO) {
                
                boolean remove = false;
                if (isThumbnail) {
                    boolean isExistOriginal = getThumbWhenLoadingFailed(wallpaper);
                    if (!isExistOriginal) {
                        remove = true;
                    }
                }else {
                    remove = true;
                }
                
                if (remove) {
                    WallpaperDB.getInstance(mContext).deleteWallpaperByID(
                            Wallpaper.WALLPAPER_FROM_PHOTO_ID);
                    WallpaperList wallpaperList = mHorizontalAdapter.getWallpaperList();
                    int index = -1;
                    for (int i = 0; i < wallpaperList.size(); i++) {
                        if (wallpaperList.get(i).getImgId() == Wallpaper.WALLPAPER_FROM_PHOTO_ID) {
                            index = i;
                            break;
                        }
                    }
                    
                    wallpaperList.remove(index);
                    Message msg = mHandler.obtainMessage(KeyguardViewHostManager.MSG_UPDATE_HAOKAN_LIST);
                    msg.obj = wallpaperList;
                    msg.arg1 = index;
                    mHandler.sendMessage(msg);

                    return false;
                }
                
                
            }else if (type == Wallpaper.WALLPAPER_FROM_FIXED_FOLDER) {
                
                if (isThumbnail) {
                    getThumbWhenLoadingFailed(wallpaper);
                }else {
                    
                }
            }
            
            
        }
        
        return true;
         
        
    }
    private HorizontalAdapter mHorizontalAdapter;
    
    
    public HorizontalAdapter getmHorizontalAdapter() {
        return mHorizontalAdapter;
    }

    public void setmHorizontalAdapter(HorizontalAdapter mHorizontalAdapter) {
        this.mHorizontalAdapter = mHorizontalAdapter;
    }


    private Handler mHandler;
    
    public Handler getmHandler() {
        return mHandler;
    }

    public void setHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    private boolean getThumbWhenLoadingFailed(Wallpaper wallpaper) {
        
        boolean isExistOriginal = true;
        
        String key = null;
        String path = DiskUtils.getCachePath(mContext) + File.separator
                + DiskUtils.WALLPAPER_BITMAP_FOLDER ;
        
        File file = new File(path);
        if(!file.exists()){
            file.mkdirs();
        }
        
        switch (wallpaper.getType()) {
            
            case Wallpaper.WALLPAPER_FROM_PHOTO :
            case Wallpaper.WALLPAPER_FROM_WEB :
                key = DiskUtils.constructFileNameByUrl(wallpaper.getImgUrl());
                File fileLoad = new File(path + File.separator + key);
                if (fileLoad.exists()) {
                    Bitmap bitmap = DiskUtils.readFile(path + File.separator + key, KWDataCache.getScreenWidth(mContext.getResources()));
                    if (bitmap != null) {
                        DiskUtils.saveThumbnail(bitmap, key, path);
                        bitmap.recycle();
                    }
                }else {
                    isExistOriginal = false;
                }
                
                break;
            case Wallpaper.WALLPAPER_FROM_FIXED_FOLDER:
                String imgUrl = wallpaper.getImgUrl(); // Url : /system/etc/ScreenLock/2.jpg
                File fileLocal = new File(FileUtil.SCREENLOCK_WALLPAPER_LOCATION + File.separator + imgUrl);
                FileInputStream fis;
                try {
                    fis = new FileInputStream(fileLocal);
                    DiskUtils.saveDefaultThumbnail(mContext, fis, fileLocal.getName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                 
                break;

            default:
                break;
        }
        
        
        return isExistOriginal;
       
    }

    private boolean downWallPaperWhenLoadingFailed(String imageUri) {
        boolean success = false;
        Bitmap bitmap = null;
        Bitmap bitmapTemp = DownLoadBitmapManager.getInstance().downLoadBitmap(
                mContext, imageUri);
        String key = DiskUtils.constructFileNameByUrl(imageUri);
        if (bitmapTemp != null) {
            int screenWid = KWDataCache.getScreenWidth(mContext
                    .getResources());
            int screenHei = KWDataCache.getAllScreenHeigt(mContext);
            bitmap = BitmapUtil.getResizedBitmapForSingleScreen(bitmapTemp,
                    screenHei, screenWid);
            String path = DiskUtils.getCachePath(mContext) + File.separator
                    + DiskUtils.WALLPAPER_BITMAP_FOLDER;
            

            success = DiskUtils.saveBitmap(bitmap, key, path);

            BitmapUtil.recycleBitmap(bitmapTemp);
            BitmapUtil.recycleBitmap(bitmap);
        }
        
        return success;
    }
	
    @Override
    public void loadPageToCache(Wallpaper wallpaper, boolean isImage) {
        getCacheManger().loadPageToCache(wallpaper, isImage);
    }
	

}