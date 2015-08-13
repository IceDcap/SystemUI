package com.amigo.navi.keyguard.picturepage.adapter;


import java.util.ArrayList;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.haokan.UIController;
import com.amigo.navi.keyguard.haokan.entity.WallpaperList;
import com.amigo.navi.keyguard.network.ImageLoader;
import com.amigo.navi.keyguard.network.local.ReadAndWriteFileFromSD;
import com.amigo.navi.keyguard.network.local.utils.DiskUtils;
import com.amigo.navi.keyguard.picturepage.interfacers.OnReloadListener;
import com.amigo.navi.keyguard.picturepage.widget.ImageViewWithLoadBitmap;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.android.keyguard.R;

public class HorizontalAdapter extends BaseAdapter {
   private static final String TAG = "HorizontalAdapter";
   private WallpaperList mWallpaperList = new WallpaperList();
   private LayoutInflater mInflater;
   private ImageLoader mImageLoader = null;
   private boolean isAllowLoad = true;
   private ArrayList<OnReloadListener> mReloadListeners = new ArrayList<OnReloadListener>();
//   private int[] mIDList = new int[]{R.drawable.a,R.drawable.b,R.drawable.c,R.drawable.d};
//   ArrayList<Bitmap> bitmapList = new ArrayList<Bitmap>();
   private ReadAndWriteFileFromSD mDealWithFromLocalInterface = null;
   ImageViewWithLoadBitmap.Config mConfig = null;
   public HorizontalAdapter(Context context,WallpaperList wallpaperList,ImageLoader imageLoader){
       this.mInflater = LayoutInflater.from(context);
       updateDataList(wallpaperList);
       this.mImageLoader = imageLoader;
       mImageLoader.setmHorizontalAdapter(this);
       mDealWithFromLocalInterface = new ReadAndWriteFileFromSD(context.getApplicationContext(), 
                 DiskUtils.WALLPAPER_BITMAP_FOLDER, DiskUtils.getCachePath(context.getApplicationContext()));
       mConfig = new ImageViewWithLoadBitmap.Config();
       mConfig.startBitmapID = R.drawable.loading;
       mConfig.failBitmapID = R.drawable.loading;
       mConfig.setImageLoader(mImageLoader);
       mConfig.setReadFromSD(mDealWithFromLocalInterface);
//       Bitmap bitmap1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.a);
//       Bitmap bitmap2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.b);
//       Bitmap bitmap3 = BitmapFactory.decodeResource(context.getResources(), R.drawable.c);
//       Bitmap bitmap4 = BitmapFactory.decodeResource(context.getResources(), R.drawable.d);
//       Bitmap bitmap5 = BitmapFactory.decodeResource(context.getResources(), R.drawable.e);
//       Bitmap bitmap6 = BitmapFactory.decodeResource(context.getResources(), R.drawable.f);
//       bitmapList.add(bitmap1);
//       bitmapList.add(bitmap2);
//       bitmapList.add(bitmap3);
//       bitmapList.add(bitmap4);
//       bitmapList.add(bitmap5);
//       bitmapList.add(bitmap6);
   }

   public void updateDataList(WallpaperList wallpaperList) {
	   DebugLog.d(TAG,"updateDataList wallpaperList size:" + wallpaperList.size());
       mWallpaperList = wallpaperList;
    }
   
    @Override
    public int getCount() {
        return mWallpaperList.size();
//    	return bitmapList.size();
    }
    
    public WallpaperList getWallpaperList() {
        return mWallpaperList;
    }

    @Override
    public Object getItem(int position) {
        return mWallpaperList.get(position);
//    	return bitmapList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
		if (DebugLog.DEBUG) {
			DebugLog.d(TAG, "getView position:" + position);
			DebugLog.d(TAG,
					"getView mWallpaperList size:" + mWallpaperList.size());
			DebugLog.d(TAG, "getView img url :"
					+ mWallpaperList.get(position).getImgUrl());
			DebugLog.d(TAG, "getView convertView:" + convertView);
		}
		
        ViewHolder holder = null;
        if (convertView == null) {
            holder=new ViewHolder();              
            convertView = mInflater.inflate(R.layout.image_view_show, null);
            holder.img = (ImageViewWithLoadBitmap)convertView.findViewById(R.id.image);
            holder.img.setConfig(mConfig);
            convertView.setTag(holder);             
        }else {
            holder = (ViewHolder)convertView.getTag();
        }
//        if(!isAllowLoad){
//            if(mReloadListeners.contains((OnReloadListener) holder.img)){
//                mReloadListeners.remove(holder.img);
//            }
//            mReloadListeners.add((OnReloadListener) holder.img);
//        }
		if (DebugLog.DEBUG) {
			DebugLog.d(TAG,"makeAndAddView getView begin");
        }
		
//		int listenerPositon = getListenerPositon(position, isAllowLoad);
        holder.img.loadImageBitmap(mWallpaperList.get(position)/*,listenerPositon*/, isAllowLoad);

        
//        holder.img.setImageResource(mIDList[position]);
//        holder.img.setImageBitmap(bitmapList.get(position));
        return convertView;
    }
    
    
    public final class ViewHolder{
        public ImageViewWithLoadBitmap img;
//        public ImageView img;
    }
 
    public void restore() {
        this.isAllowLoad = true;
    }

    public void lock() {
        if(DebugLog.DEBUG){
        	DebugLog.d(TAG,"lock img");
        }
        this.isAllowLoad = false;
    }

    public void unlock() {
        if(DebugLog.DEBUG){
        	DebugLog.d(TAG,"lock img no");
        }
        this.isAllowLoad = true;

        for(OnReloadListener listerner : mReloadListeners){
            listerner.onReload();
        }
        mReloadListeners.clear();

    }
 
/*    public Bitmap getBitmapFromCache(String url){
        return mImageLoader.getBitmapFromCache(url);
    }*/
    
    
    public Bitmap getWallpaperByUrl(String url) {
        Bitmap bitmap = mImageLoader.getBitmapFromCache(url);
        return bitmap;
    }
    
    public void clearAllLock(){
    	for(int index = 0;index < mWallpaperList.size();index++){
    		mWallpaperList.get(index).setLocked(false);
    	}
    }
    
    public void removeCacheByUrl(String url){
    	mImageLoader.removeItem(url);
    }
    
}
