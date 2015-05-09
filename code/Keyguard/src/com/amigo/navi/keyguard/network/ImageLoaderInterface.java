package com.amigo.navi.keyguard.network;



import com.amigo.navi.keyguard.network.local.DealWithFromLocalInterface;

import android.graphics.Bitmap;

public interface ImageLoaderInterface {
    public void loadImage(String url, ImageLoadingListener loadingListener,
            DealWithFromLocalInterface dealWithFromLocalInterface,boolean isNeedCache);
    public void removeItem(String url);
    public void addImage2Cache(String url, Bitmap value);
    public Bitmap getBitmapFromCache(String url);
}
