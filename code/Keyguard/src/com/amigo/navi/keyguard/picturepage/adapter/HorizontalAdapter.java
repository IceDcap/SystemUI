package com.amigo.navi.keyguard.picturepage.adapter;


import java.util.ArrayList;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.haokan.entity.WallpaperList;
import com.amigo.navi.keyguard.network.ImageLoader;
import com.amigo.navi.keyguard.network.local.LocalBitmapOperation;
import com.amigo.navi.keyguard.network.local.LocalFileOperationInterface;
import com.amigo.navi.keyguard.network.local.ReadAndWriteFileFromSD;
import com.amigo.navi.keyguard.network.local.utils.DiskUtils;
import com.amigo.navi.keyguard.picturepage.interfacers.OnReloadListener;
import com.amigo.navi.keyguard.picturepage.widget.ImageViewForHeadCompound;


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
   ImageViewForHeadCompound.Config mConfig = null;
   public HorizontalAdapter(Context context,WallpaperList wallpaperList,ImageLoader imageLoader){
       this.mInflater = LayoutInflater.from(context);
       updateDataList(wallpaperList);
       this.mImageLoader = imageLoader;
       LocalFileOperationInterface localFileOperationInterface = new LocalBitmapOperation(context);
       mDealWithFromLocalInterface = new ReadAndWriteFileFromSD(context.getApplicationContext(), 
                 DiskUtils.WALLPAPER_BITMAP_FOLDER, DiskUtils.getCachePath(context.getApplicationContext()),
                 localFileOperationInterface);
       mConfig = new ImageViewForHeadCompound.Config();
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
        DebugLog.d(TAG, "getView position:" + position);
//        DebugLog.d(TAG, "getView mWallpaperList size:" + mWallpaperList.size());
//        DebugLog.d(TAG, "getView img url :" + mWallpaperList.get(position).getImgUrl());
        ViewHolder holder = null;
        DebugLog.d(TAG, "getView convertView:" + convertView);
        if (convertView == null) {

            holder=new ViewHolder();  
             
            convertView = mInflater.inflate(R.layout.image_view_show, null);
            holder.img = (ImageViewForHeadCompound)convertView.findViewById(R.id.image);
            holder.img.setConfig(mConfig);
            convertView.setTag(holder);             
        }else {
             
            holder = (ViewHolder)convertView.getTag();
        }
        if(!isAllowLoad){
            if(mReloadListeners.contains((OnReloadListener) holder.img)){
                mReloadListeners.remove(holder.img);
            }
            mReloadListeners.add((OnReloadListener) holder.img);
        }
        DebugLog.d("HorizontalListView","makeAndAddView getView begin");
        holder.img.loadImageBitmap(mWallpaperList.get(position),true,isAllowLoad);
//        holder.img.setImageResource(mIDList[position]);
//        holder.img.setImageBitmap(bitmapList.get(position));
        return convertView;
    }

    public final class ViewHolder{
        public ImageViewForHeadCompound img;
//        public ImageView img;
    }
 
    public void restore() {
        this.isAllowLoad = true;
    }

    public void lock() {
    	DebugLog.d(TAG,"lock img");
        this.isAllowLoad = false;
    }

    public void unlock() {
    	DebugLog.d(TAG,"lock img no");
        this.isAllowLoad = true;

        for(OnReloadListener listerner : mReloadListeners){
            listerner.onReload();
        }
        mReloadListeners.clear();

    }
 
    public Bitmap getBitmapFromCache(String url){
        return mImageLoader.getBitmapFromCache(url);
    }
    
    
    public Bitmap getWallpaperByUrl(String url) {
        Bitmap bitmap = mImageLoader.getBitmapFromCache(url);
        return bitmap;
    }
    
    public void clearAllLock(){
    	for(int index = 0;index < mWallpaperList.size();index++){
    		mWallpaperList.get(index).setLocked(false);
    	}
    }
    
}
