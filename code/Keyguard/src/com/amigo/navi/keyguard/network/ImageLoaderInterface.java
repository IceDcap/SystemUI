package com.amigo.navi.keyguard.network;



import com.amigo.navi.keyguard.haokan.entity.Wallpaper;
import com.amigo.navi.keyguard.network.local.DealWithFromLocalInterface;
import com.amigo.navi.keyguard.picturepage.widget.ImageViewWithLoadBitmap;

import android.graphics.Bitmap;

public interface ImageLoaderInterface {
/*    public void loadImage(String url, ImageLoadingListener loadingListener,
            DealWithFromLocalInterface dealWithFromLocalInterface,boolean isNeedCache);*/
    public void removeItem(String url);
    public void addImage2Cache(String url, Bitmap value);
    public Bitmap getBitmapFromCache(String url);
	public void loadImageToView(ImageViewWithLoadBitmap imageViewWithLoadBitmap/*, int posOfListener*/);
	public void addBmpToImageRemoved(Bitmap loadedImage);
	public String getmCurrentUrl();
	
	public void loadPageToCache(final Wallpaper wallpaper, final boolean isImage);
}
