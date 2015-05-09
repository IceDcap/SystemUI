package com.amigo.navi.keyguard.picturepage.adapter;


import java.util.ArrayList;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.haokan.entity.WallpaperList;
import com.amigo.navi.keyguard.network.ImageLoader;
import com.amigo.navi.keyguard.picturepage.interfacers.OnReloadListener;
import com.amigo.navi.keyguard.picturepage.widget.ImageViewForHeadCompound;


import android.content.Context;
import android.graphics.Bitmap;
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

   public HorizontalAdapter(Context context,WallpaperList wallpaperList,ImageLoader imageLoader){
       this.mInflater = LayoutInflater.from(context);
       updateDataList(wallpaperList);
       this.mImageLoader = imageLoader;
   }

   public void updateDataList(WallpaperList wallpaperList) {
       mWallpaperList = wallpaperList;
    }
   
    @Override
    public int getCount() {
        return mWallpaperList.size();
    }
    
    public WallpaperList getWallpaperList() {
        return mWallpaperList;
    }

    @Override
    public Object getItem(int position) {
        return mWallpaperList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DebugLog.d(TAG, "getView position:" + position);
        DebugLog.d(TAG, "getView mWallpaperList size:" + mWallpaperList.size());
        DebugLog.d(TAG, "getView img url :" + mWallpaperList.get(position).getImgUrl());
        ViewHolder holder = null;
        if (convertView == null) {
            ImageViewForHeadCompound.Config config = new ImageViewForHeadCompound.Config();
            config.setImageLoader(mImageLoader);
            holder=new ViewHolder();  
             
            convertView = mInflater.inflate(R.layout.image_view_show, null);
            holder.img = (ImageViewForHeadCompound)convertView.findViewById(R.id.image);
            holder.img.setConfig(config);
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
//        isAllowLoad = true;
        DebugLog.d("HorizontalListView","makeAndAddView getView begin");
        holder.img.loadImageBitmap(mWallpaperList.get(position),true,isAllowLoad);
        return convertView;
    }

    public final class ViewHolder{
        public ImageViewForHeadCompound img;
    }
 
    public void restore() {
        this.isAllowLoad = true;
    }

    public void lock() {
        this.isAllowLoad = false;
    }

    public void unlock() {
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
    
}
