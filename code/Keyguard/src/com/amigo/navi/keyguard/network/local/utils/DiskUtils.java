
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
import com.amigo.navi.keyguard.KWDataCache;


import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory.Options;
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
        ByteArrayInputStream bis = null;
        boolean flag = false;
        try {
        	DebugLog.d(TAG,"saveBitmap bs:" + bs.length);
             os = new BufferedOutputStream(outputStream, 8 * 1024);
             bis = new ByteArrayInputStream(bs);
             os.write(2);
             byte[] bf = new byte[1024];
             int len = 0;
             while((len = bis.read(bf)) != -1){
                 os.write(bf, 0, len);
             }
             bis.close();
//             os.close();
             flag = true;
         	DebugLog.d(TAG,"saveBitmap bs success");
        } catch (Exception e) {
        	DebugLog.d(TAG,"saveBitmap error:" + e.getStackTrace());
        	flag = false;
            e.printStackTrace();
        }finally{
        	if(bis != null){
        		try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
//            if(os != null){
//                try {
//                    os.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
        }
         return flag;
    }
    
    public static Bitmap decodeBitmap(InputStream is,int screenWid){
        //解密图片
         byte[] ss = getBitmapFromSdkard(is);
         DebugLog.d("HorizontalListView","makeAndAddView decodeBitmap ss:" + ss);
         if(ss == null) {
             return null;
         }
         Options options = new Options();
         // 不去真正解析图片，只是获取图片的宽高
         options.inJustDecodeBounds = true;
         BitmapFactory.decodeByteArray(ss, 0, ss.length, options);
         int imageWidth = options.outWidth;
         int scale = 1;
         if(imageWidth > screenWid){
             scale = imageWidth / screenWid;
         }
         if(scale == 0){
        	 scale = 1;
         }
         options.inJustDecodeBounds = false;
         options.inSampleSize = scale;
         options.inPreferredConfig = Config.RGB_565;
         Bitmap bitmap = BitmapFactory.decodeByteArray(ss, 0, ss.length, options);
         return bitmap;
    }
    
    //解密图片
    public static byte[] getBitmapFromSdkard(InputStream is){
    	ByteArrayOutputStream bos = null;
        try {
            is.read();
            bos = new ByteArrayOutputStream();
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
        }finally{

        	if(bos != null){
        		try {
					bos.close();
		        	if(is != null){
		        		is.close();
		        	}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }
        return null;
    }

    public static byte[] convertBitmap(Bitmap bmp){
		 DebugLog.d(TAG,"convertBitmap 1");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(CompressFormat.JPEG, 100, output);
//            bmp.recycle();//自由选择是否进行回收
        byte[] result = output.toByteArray();//转换成功了
        try {
			DebugLog.d(TAG,"convertBitmap 2");
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
        InputStream is = null;
        try  
        {  
        	int screenWid = KWDataCache.getScreenWidth(context.getResources());
            Options options = new Options();
            // 不去真正解析图片，只是获取图片的宽高
            options.inJustDecodeBounds = true;
        	DebugLog.d(TAG,"getImageFromAssetsFile fileName:" + fileName);
            is = am.open(fileName);  
            BitmapFactory.decodeStream(is, null, options);
            is.close();
            int imageWidth = options.outWidth;
            int scale = 1;
            if(imageWidth > screenWid){
                scale = imageWidth / screenWid;
            }
            if(scale == 0){
           	 	scale = 1;
            }
            options.inJustDecodeBounds = false;
            options.inSampleSize = scale;
            is = am.open(fileName);  
            options.inPreferredConfig = Config.RGB_565;
            image = BitmapFactory.decodeStream(is, null, options);
//            image = BitmapFactory.decodeStream(is);
            DebugLog.d(TAG,"decodeBitmap image:" + image);
            is.close();  
        }  
        catch (IOException e)  
        {  
        	DebugLog.d(TAG,"getImageFromAssetsFile error:" + e.getStackTrace());
            e.printStackTrace();  
        }finally{
        	if(is != null){
        		try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
        	}
        }
    
        return image;  
    
    }  
 
    public static Bitmap readFile(String path,int screenWid){
		FileInputStream fis;
		Bitmap bitmap = null;
		try {
			fis = new FileInputStream(path);
			bitmap = decodeBitmap(fis, screenWid);
			return bitmap;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		return bitmap;
    }
    
	//加密存储
	public static boolean saveBitmap(byte[] bs, String fileName, String path){
		 // 保存文件   
		 File file = new File(path);
		 if(!file.exists()){
			 boolean bo = file.mkdirs();
		 }
		 
		 String imgPath = path + "/" + fileName;
		 FileOutputStream os = null;
		 try {
			 os = new FileOutputStream(imgPath);
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
    
	public static boolean deleteFile(String sPath) {   
		   boolean flag = false;   
		   File file = new File(sPath);   
		    // 路径为文件且不为空则进行删除   
		    if (file.isFile() && file.exists()) {   
		    	flag = file.delete();   
//		        flag = true;   
		    }   
		    return flag;   
		}
		
		public static boolean deleteDirectory(String sPath) {   
			boolean flag = false;
		    if (!sPath.endsWith(File.separator)) {   
		        sPath = sPath + File.separator;   
		    }   
		    File dirFile = new File(sPath);   
		    if (!dirFile.exists() || !dirFile.isDirectory()) {   
		        return false;   
		    }   
		    flag = true;   
		    File[] files = dirFile.listFiles();   
		    if(files == null)return false;
		    for (int i = 0; i < files.length; i++) {   
		        if (files[i].isFile()) {   
		            flag = deleteFile(files[i].getAbsolutePath());   
		            if (!flag) break;   
		        } 
		        else {   
		            flag = deleteDirectory(files[i].getAbsolutePath());   
		            if (!flag) break;   
		        }   
		    }   
		    if (!flag) return false;   
		    if (dirFile.delete()) {   
		        return true;   
		    } else {   
		        return false;   
		    }   
		}  

	    public static Bitmap getImageFromSystem(Context context,String path)  
	    {  
	        Bitmap image = null;  
	        try  
	        {   
	        	int screenWid = KWDataCache.getScreenWidth(context.getResources());
	            Options options = new Options();
	            options.inJustDecodeBounds = true;
	        	BitmapFactory.decodeFile(path, options);
	            int imageWidth = options.outWidth;
	            int scale = 1;
	            if(imageWidth > screenWid){
	                scale = imageWidth / screenWid;
	            }
	            if(scale == 0){
	           	 	scale = 1;
	            }
	            options.inJustDecodeBounds = false;
	            options.inSampleSize = scale;
	            options.inPreferredConfig = Config.RGB_565;
	            DebugLog.d(TAG,"getImageFromSystem path:" + path);
	            image = BitmapFactory.decodeFile(path, options);
	            DebugLog.d(TAG,"getImageFromSystem image:" + image);
	        }  
	        catch (Exception e)  
	        {  
	        	DebugLog.d(TAG,"getImageFromAssetsFile error:" + e.getStackTrace());
	            e.printStackTrace();  
	        }  
	    
	        return image;  
	    
	    }  
		
		public static boolean saveBitmap(Bitmap bt, String key,String path){
			if(bt == null){
				return false;
			}
			 // 保存文件   
			try {
				 DebugLog.d(TAG,"saveBitmap path:" + path);
				 DebugLog.d(TAG,"saveBitmap key:" + key);
				 byte[] bts = convertBitmap(bt);
				 return saveBitmap(bts, key, path);
			} catch (Exception e) {
				DebugLog.d(TAG,"saveBitmap error");
				e.printStackTrace();
			}
			return false;
		}
	    
}
