
package com.amigo.navi.keyguard.network.local.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.amigo.navi.keyguard.DebugLog;


import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;

public class DiskUtils {
    public static final int VERSION = 1;
    public static final String WALLPAPER_BITMAP_FOLDER = "Wallpaper";
    public static final String CATEGORY_BITMAP_FOLDER = "Category";
    public static final String WALLPAPER_OBJECT_FILE_FOLDER = "/Wallpaper/fixed";
    public static final String WALLPAPER_OBJ_KEY = "fixedwallpaper";
    public static final String WALLPAPER_Image_KEY = "wallpaper";
    private static final String TAG = "DiskUtils";
    //加密存储
    public static boolean saveBitmap(byte[] bs,OutputStream outputStream){
        BufferedOutputStream os = null;
        try {
             os = new BufferedOutputStream(outputStream, 8 * 1024);
             ByteArrayInputStream bis = new ByteArrayInputStream(bs);
             os.write(2);
             byte[] bf = new byte[1024];
             int len = 0;
             while((len = bis.read(bf)) != -1){
                 os.write(bf, 0, len);
             }
             bis.close();
             os.close();
             return true;
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(os != null){
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
         return false;
    }
    
    public static Bitmap decodeBitmap(InputStream is){
        //解密图片
         byte[] ss = getBitmapFromSdkard(is);
         DebugLog.d("HorizontalListView","makeAndAddView decodeBitmap ss:" + ss);
         if(ss == null) {
             return null;
         }
         return BitmapFactory.decodeByteArray(ss, 0, ss.length);
    }
    
    //解密图片
    public static byte[] getBitmapFromSdkard(InputStream is){
        try {
            is.read();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int len = 0;
            while((len = is.read(buf)) != -1){
                bos.write(buf, 0, len);
            }
            bos.close();
            is.close();
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] convertBitmap(Bitmap bmp){
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(CompressFormat.JPEG, 100, output);
//            bmp.recycle();//自由选择是否进行回收
        byte[] result = output.toByteArray();//转换成功了
        try {
            output.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getCachePath(Context context){
        String cachePath = "";
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
            || !Environment.isExternalStorageRemovable()) {
          cachePath = context.getExternalCacheDir().getPath();
        } else {
          cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }
    
    public static String getSDPath(Context context){
        return Environment.getExternalStorageDirectory().getPath();
    }
    
    private static final String LOAD_IMAGE_SUFFIX = "file";
    public static String constructFileNameByUrl(String url) {
        if (null == url) {
            return null;
        }
        StringBuffer fileName = new StringBuffer();
        String regString = "[^A-Za-z0-9.]";
        String newUrl = url.replaceAll(regString, "_");
        fileName.append(newUrl);
        return fileName.toString();
    }
 
    public static byte[] Stream2Byte(InputStream is) {
		BufferedInputStream in = null;
		ByteArrayOutputStream out = null;
		try {
			in = new BufferedInputStream(is);
			out = new ByteArrayOutputStream();

			System.out.println("Available bytes:" + in.available());

			byte[] temp = new byte[1024];
			int size = 0;
			while ((size = in.read(temp)) != -1) {
				out.write(temp, 0, size);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		byte[] content = out.toByteArray();
		System.out.println("Readed bytes count:" + content.length);
		return content;
    }
    
    public static Bitmap getImageFromAssetsFile(Context context,String fileName)  
    {  
        Bitmap image = null;  
        AssetManager am = context.getResources().getAssets();  
        try  
        {  
        	DebugLog.d(TAG,"getImageFromAssetsFile fileName:" + fileName);
            InputStream is = am.open(fileName);  
            image = BitmapFactory.decodeStream(is); 
        	DebugLog.d(TAG,"getImageFromAssetsFile image:" + image);
            is.close();  
        }  
        catch (IOException e)  
        {  
        	DebugLog.d(TAG,"getImageFromAssetsFile error:" + e.getStackTrace());
            e.printStackTrace();  
        }  
    
        return image;  
    
    }  
    
}
