package com.amigo.navi.keyguard.network;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.WeakHashMap;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.network.FailReason.FailType;
import com.amigo.navi.keyguard.network.local.DealWithFromLocalInterface;
import com.amigo.navi.keyguard.network.local.utils.DiskUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.LruCache;

public class ImageLoader implements ImageLoaderInterface{
    private static final String LOG_TAG = "ImageLoader";
    private int mSecondMaxCapacity = 8;
    private Context mContext;
    int maxMemory = (int) Runtime.getRuntime().maxMemory();
    int mCacheSize = maxMemory / 4;  
    public ImageLoader(Context context) {
        mContext = context.getApplicationContext();
    }
    

    private LruCache<String, Bitmap> mFirstLevelCache = new LruCache<String, Bitmap>(mCacheSize) {

        @Override
        protected int sizeOf(String key, Bitmap bitmap) { 
            // 重写此方法来衡量每张图片的大小，默认返回图片数量。 
            return bitmap.getByteCount() / 1024; 
        } 
        
        @Override
        protected void entryRemoved(boolean evicted, String key,
                        Bitmap oldValue, Bitmap newValue) {
                    // TODO Auto-generated method stub
            super.entryRemoved(evicted, key, oldValue, newValue);
            if (oldValue != null) { 
                mSecondLevelCache.put(key,new SoftReference<Bitmap>(oldValue));
              }
        }
        
        
        
    };
    
    // 二级缓存，采用的是软应用，只有在内存吃紧的时候软应用才会被回收，有效的避免了oom
    private WeakHashMap<String, SoftReference<Bitmap>> mSecondLevelCache = new WeakHashMap<String, SoftReference<Bitmap>>(
            mSecondMaxCapacity / 2){

            
    };

    public void setSecondMaxCapacity(int maxCapacity) {
        this.mSecondMaxCapacity = maxCapacity;
    }
    
    /**
     * 清理缓存
     */
    public void clearCache() {
        mFirstLevelCache.evictAll();
    }

    public void removeItem(String url){
        mFirstLevelCache.remove(url);
        mSecondLevelCache.remove(url);
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
        synchronized (mFirstLevelCache) {
            mFirstLevelCache.put(url, value);
        }
    }

    
    @Override
    public void loadImage(String url,
            ImageLoadingListener loadingListener,
            DealWithFromLocalInterface dealWithFromLocalInterface,boolean isNeedCache) {
        if(loadingListener != null){
            loadingListener.onLoadingStarted(url);
        }
        if (TextUtils.isEmpty(url)) {
            if(loadingListener != null){
                FailReason failReason = new FailReason(FailType.UNKNOWN, null);
                loadingListener.onLoadingFailed(url, failReason);
            }
            return;
        }
        Bitmap bitmap = loadImageStepByStep(url, loadingListener,
                dealWithFromLocalInterface); 
        DebugLog.d(LOG_TAG,"makeAndAddView loadImage url:" + url + " bitmap:" + bitmap);
        dealWithImage(url, loadingListener, isNeedCache, bitmap,dealWithFromLocalInterface);
    }

    private void dealWithImage(String url,
            ImageLoadingListener loadingListener, boolean isNeedCache,
            Bitmap bitmap,DealWithFromLocalInterface dealWithFromLocalInterface) {
        if(bitmap != null){
            if(isNeedCache){
                addImage2Cache(url, bitmap);
            }
            if(loadingListener != null){
                loadingListener.onLoadingComplete(url, bitmap);
            }
        }else{
            FailReason failReason = new FailReason(FailType.UNKNOWN, null);
            if(loadingListener != null){
                loadingListener.onLoadingFailed(url,failReason);
            }
        }
    }

    private Bitmap loadImageStepByStep(String url,
            ImageLoadingListener loadingListener,
            DealWithFromLocalInterface dealWithFromLocalInterface) {
        DebugLog.d("HorizontalListView","makeAndAddView loadImageStepByStep url:" + url);
        Bitmap bitmap = getBitmapFromCache(url);
        DebugLog.d("HorizontalListView","makeAndAddView loadImageStepByStep1 bitmap:" + bitmap);
        if (bitmap == null) {
            bitmap = loadImageFromLocal(dealWithFromLocalInterface, url);
            DebugLog.d("HorizontalListView","makeAndAddView loadImageStepByStep bitmap:" + bitmap);
        }
        return bitmap;
    }

    public Bitmap loadImageFromLocal(DealWithFromLocalInterface dealWithFromLocalInterface
            ,String url){
        Bitmap bitmap = null;
        String key = DiskUtils.constructFileNameByUrl(url);
        bitmap = (Bitmap) dealWithFromLocalInterface.readFromLocal(key);
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
        bitmap = getFromSecondLevelCache(url);// 从二级缓存中拿
        return bitmap;
    }

    /**
     * 从二级缓存中拿
     * 
     * @param url
     * @return
     */
    private Bitmap getFromSecondLevelCache(String url) {
        Bitmap bitmap = null;
        SoftReference<Bitmap> softReference = mSecondLevelCache.get(url);
        if (softReference != null) {
            bitmap = softReference.get();
            if (bitmap == null) {// 由于内存吃紧，软引用已经被gc回收了
                mSecondLevelCache.remove(url);
            }
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
            if (bitmap != null) {// 将最近访问的元素放到链的头部，提高下一次访问该元素的检索速度（LRU算法）
                mFirstLevelCache.remove(url);
                mFirstLevelCache.put(url, bitmap);
            }
        }
        return bitmap;
    }
    
}