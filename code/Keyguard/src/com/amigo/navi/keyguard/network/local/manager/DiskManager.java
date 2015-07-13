package com.amigo.navi.keyguard.network.local.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.network.local.LocalFileOperationInterface;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

public class DiskManager {
    private static final String TAG = "DiskManager";
    private static DiskManager instance = null;
    private static HashMap<String,DiskLruCache> sDiskCacheList = new HashMap<String, DiskLruCache>();
    private LocalFileOperationInterface mLocalFileOperationInterface = null;
    
    public synchronized static DiskManager getInstance(){
        if(instance == null){
            instance = new DiskManager();
        }
        return instance;
    }
    
    private DiskManager() {
        
    }
    
    public synchronized DiskLruCache getOpenedDiskLruCache(String path){
        if(sDiskCacheList.get(path) != null){
            return sDiskCacheList.get(path);
        }
        return null;
    }
    
    public void setLocalFileOperation(LocalFileOperationInterface localFileOperationInterface){
        mLocalFileOperationInterface = localFileOperationInterface;
    }
    
    public synchronized DiskLruCache openDiskLruCache(Context context,int version,String folderName
            ,String path){
        DebugLog.d(TAG,"openDiskLruCache");
        DiskLruCache diskLruCache = null;
        try {
          File cacheDir = getDiskCacheDir(context, folderName,path);
          DebugLog.d(TAG,"createDiskLruCache openDiskLruCache");
          if (!cacheDir.exists()) {
              cacheDir.mkdirs();
          }
          diskLruCache = DiskLruCache.open(cacheDir, version, 1, 1024 * 1024 * 1024);
          DebugLog.d(TAG,"createDiskLruCache diskLruCache:" + diskLruCache);
//          sDiskCacheList.put(path,diskLruCache);
        } catch (IOException e) {
            DebugLog.d(TAG,"createDiskLruCache openDiskLruCache e:" + e);
          e.printStackTrace();
        }
        return diskLruCache;
    }
    
    public synchronized boolean saveFile(DiskLruCache cache,String key,Object obj){
        if(mLocalFileOperationInterface == null){
            return false;
        }
        DebugLog.d(TAG,"saveFile");
        DebugLog.d(TAG,"saveFile key:" + key);
        boolean flag = false;
        OutputStream outputStream = null;
        try {
            DebugLog.d(TAG,"saveFile begin");
            DiskLruCache.Editor editor = cache.edit(key);
            DebugLog.d(TAG,"saveBitmap editor:" + editor);
            if (editor != null) {
              outputStream = editor.newOutputStream(0);
              if (mLocalFileOperationInterface.saveFile(obj, outputStream)) {
                flag = true;
                editor.commit();
              } else {
                editor.abort();
              }
            }
            cache.flush();
            if(outputStream != null){
                outputStream.close();
            }
          } catch (IOException e) {
        	DebugLog.d(TAG,"saveFile error:" + e);
            e.printStackTrace();
            flag = false;
          }finally{
        	  if(outputStream != null){
        		  try {
					outputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	  }
          }
        return flag;
    }
    
    public synchronized boolean saveFileByByte(DiskLruCache cache,String key,byte[] bytes){
        if(mLocalFileOperationInterface == null){
            return false;
        }
        DebugLog.d(TAG,"saveFile");
        DebugLog.d(TAG,"saveFile key:" + key);
        boolean flag = false;
        OutputStream outputStream = null;
        try {
            DebugLog.d(TAG,"saveFile begin");
            DiskLruCache.Editor editor = cache.edit(key);
            DebugLog.d(TAG,"saveFile editor:" + editor);
            DebugLog.d(TAG,"saveFile bytes:" + bytes.length);
            if (editor != null) {
              outputStream = editor.newOutputStream(0);
              if (mLocalFileOperationInterface.saveFileByByte(bytes, outputStream)) {
                flag = true;
                editor.commit();
              } else {
                editor.abort();
              }
            }
            cache.flush();
            if(outputStream != null){
                outputStream.close();
            }
          } catch (IOException e) {
        	DebugLog.d(TAG,"saveFile error:" + e);
            e.printStackTrace();
            flag = false;
          }finally{
              if(outputStream != null){
                  try {
					outputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
              }
          }
        return flag;
    }
    
    public synchronized Object readFileFromLocal(DiskLruCache cache,String key){
        DebugLog.d(TAG,"readBitmapFromLocal");
        if(mLocalFileOperationInterface == null){
            return null;
        }
        InputStream is = null;
        try {
            DebugLog.d("HorizontalListView","makeAndAddView readBitmapFromLocal key:" + key);
            DiskLruCache.Snapshot snapShot = cache.get(key);
            DebugLog.d(TAG,"readBitmapFromLocal snapShot:" + snapShot);
            DebugLog.d("HorizontalListView","makeAndAddView readBitmapFromLocal snapShot:" + snapShot);
            if (snapShot != null) {
              DebugLog.d(TAG,"readBitmapFromLocal begin");
              is = snapShot.getInputStream(0);
              Object obj = mLocalFileOperationInterface.readFile(is);
              is.close();
              return obj;
            }
          } catch (IOException e) {
            DebugLog.d("HorizontalListView","makeAndAddView readBitmapFromLocal error:");
            e.printStackTrace();
          }finally{
        	  if(is != null){
        		  try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	  }
          }
        return null;
    }
    
    public synchronized boolean clear(DiskLruCache cache){
        boolean isDelSuccess = false;
        DebugLog.d(TAG,"clear");
        try {
            cache.delete();
            isDelSuccess = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isDelSuccess;
    }
    
    public boolean delete(DiskLruCache cache,String key){
        boolean isDelSuccess = false;
        DebugLog.d(TAG,"disk delete");
        try {
            DebugLog.d(TAG,"disk delete key:" + key);
            isDelSuccess = cache.remove(key);
        } catch (IOException e) {
            e.printStackTrace();
            DebugLog.d(TAG,"disk delete error:" + e);
        }
        return isDelSuccess;
    }
    
    public synchronized void close(DiskLruCache cache){
        DebugLog.d(TAG,"close");
    try {   
            cache.close();
//            sDiskCacheList.remove(cache.getDirectory().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static File getDiskCacheDir(Context context, String uniqueName,String path) {
        return new File(path + File.separator + uniqueName);
      }
    
}
