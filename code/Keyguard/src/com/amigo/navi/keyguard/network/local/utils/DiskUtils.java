
package com.amigo.navi.keyguard.network.local.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.KWDataCache;
import com.amigo.navi.keyguard.network.ImageLoader;
import com.amigo.navi.keyguard.network.local.ReuseImage;


import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory.Options;
import android.os.Environment;
import android.util.Log;

public class DiskUtils {
    public static final int VERSION = 1;
    public static final String WALLPAPER_BITMAP_FOLDER = "Wallpaper";
    public static final String CATEGORY_BITMAP_FOLDER = "Category";
    public static final String WALLPAPER_OBJECT_FILE_FOLDER = "/Wallpaper/fixed";
    public static final String WALLPAPER_OBJ_KEY = "fixedwallpaper";
    public static final String WALLPAPER_Image_KEY = "wallpaper";
    private static final String TAG = "DiskUtils";
    
    public static String THUMBNAIL = ImageLoader.THUMBNAIL_POSTFIX;
    
     
    public static Bitmap decodeBitmap(InputStream is,int screenWid){
    	return decodeBitmap(is, screenWid, null);
    }
    
    public static Bitmap decodeBitmap(InputStream is,int screenWid, ReuseImage reuseImage){
       
         byte[] ss = getBitmapFromSdkard(is);
         DebugLog.d("HorizontalListView","makeAndAddView decodeBitmap ss:" + ss);
         if(ss == null) {
             return null;
         }
         Options options = new Options();
       
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
         DebugLog.d(TAG,"decodeBitmap decodeBitmap imageWidth:" + imageWidth);
         DebugLog.d(TAG,"decodeBitmap decodeBitmap scale:" + scale);
         DebugLog.d(TAG,"decodeBitmap decodeBitmap ss.length:" + ss.length);
         
          
         options.inJustDecodeBounds = false;
         options.inSampleSize = scale;
         options.inPreferredConfig = Config.ARGB_8888;
         
        if (reuseImage != null) {
            Bitmap reuseBitmap = reuseImage.getBitmap();

            if (null != reuseBitmap) {
                DebugLog.d(TAG, "scale = " + scale + " options.outWidth = " + options.outWidth + " ; reuseBitmap.getWidth() = " + reuseBitmap.getWidth());
                if (options.outWidth == reuseBitmap.getWidth()
                        && options.outHeight == reuseBitmap.getHeight()) {
                    reuseImage.setUsed(true);
                    options.inMutable = true;
                    options.inBitmap = reuseBitmap;
                } else {
                    reuseBitmap = null;
                    reuseImage.setUsed(false);
                }
            }
        }
         Bitmap bitmap = null;
         try {
             bitmap = BitmapFactory.decodeByteArray(ss, 0, ss.length, options);
             DebugLog.d(TAG,"decodeBitmap decodeBitmap bitmap.getByteCount:" + bitmap.getByteCount());
         } catch (OutOfMemoryError e) {
            Log.e(TAG, "", e);
         } catch (Exception e) {
             Log.e(TAG, "", e);
          }
         return bitmap;
    }
    
    
    private static Bitmap decodeFileDescriptor(FileInputStream fis, int Width, ReuseImage reuseImage) {
        Bitmap bitmap = null;
        
        try {
            
            FileDescriptor fd = fis.getFD();
            
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inJustDecodeBounds = true;
            options.inSampleSize = 1;
            BitmapFactory.decodeFileDescriptor(fd, null, options);
            if (options.outWidth == -1 || options.outHeight == -1) {
                return decodeBitmap(fis, Width, reuseImage);
            }
            
            int imageWidth = options.outWidth;
            int scale = 1;
            if(imageWidth > Width){
                scale = imageWidth / Width;
            }
            if(scale == 0){
                scale = 1;
            }
            
             
            options.inJustDecodeBounds = false;
            options.inSampleSize = scale;
            options.inPreferredConfig = Config.ARGB_8888;
            
            
           if (reuseImage != null) {
               Bitmap reuseBitmap = reuseImage.getBitmap();

               if (null != reuseBitmap) {
                   DebugLog.d(TAG, "scale = " + scale + " options.outWidth = " + options.outWidth + " ; reuseBitmap.getWidth() = " + reuseBitmap.getWidth());
                   if (options.outWidth == reuseBitmap.getWidth()
                           && options.outHeight == reuseBitmap.getHeight()) {
                       reuseImage.setUsed(true);
                       options.inMutable = true;
                       options.inBitmap = reuseBitmap;
                   } else {
                       reuseBitmap = null;
                       reuseImage.setUsed(false);
                   }
               }
           }
            
           bitmap = BitmapFactory.decodeFileDescriptor(fd, null, options);
            
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException", e);
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
            e.printStackTrace();
        } 
        
        return bitmap;
        
    }
    
    public static Bitmap decodeFileDescriptor(String path, int Width) {
        return decodeFileDescriptor(path, Width, null);
    }
    
    public static Bitmap decodeFileDescriptor(String path, int Width, ReuseImage reuseImage) {

        File file = new File(path);
        return decodeFileDescriptor(file, Width, reuseImage);
    }
    
    
    public static Bitmap decodeFileDescriptor(File file, int Width, ReuseImage reuseImage) {

        if (!file.exists()) {
            return null;
        }
        Bitmap bitmap = null;
        try {
            bitmap = decodeFileDescriptor(new FileInputStream(file), Width, reuseImage);
        } catch (FileNotFoundException e) {
            Log.v(TAG, "FileNotFoundException", e);
            e.printStackTrace();
        }
       
        return bitmap;
    }
    
   
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
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "", e);
        } finally{

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
        bmp.compress(CompressFormat.PNG, 100, output);

        byte[] result = output.toByteArray();
        try {
			DebugLog.d(TAG,"convertBitmap 2");
            output.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    
    private static String HAOKAN_DIR = null;
    public static String getCachePath(Context context){
    	if (HAOKAN_DIR == null) {
    		HAOKAN_DIR = context.getFilesDir().getPath() + "/haokan";
		}
    	return HAOKAN_DIR;
 
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
            options.inPreferredConfig = Config.ARGB_8888;
            image = BitmapFactory.decodeStream(is, null, options);
//            image = BitmapFactory.decodeStream(is);
            DebugLog.d(TAG,"decodeBitmap image:" + image);
            is.close();  
        }  
        catch (IOException e)  
        {  
        	DebugLog.d(TAG,"getImageFromAssetsFile error:" + e.getStackTrace());
            e.printStackTrace();  
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "", e);
        } finally{
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
 
 
    
	
	public static boolean saveBitmap(byte[] bs, String fileName, String path){
		
		 File file = new File(path);
		 if(!file.exists()){
			 boolean bo = file.mkdirs();
		 }
		 
		 String imgPath = path + "/" + fileName;
		 FileOutputStream os = null;
		 try {
			 os = new FileOutputStream(imgPath);
			 ByteArrayInputStream bis = new ByteArrayInputStream(bs);
//			 os.write(2);
			 byte[] bf = new byte[1024];
			 int len = 0;
			 while((len = bis.read(bf)) != -1){
				 os.write(bf, 0, len);
			 }
			 bis.close();
			 os.flush();
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
  
		    if (file.isFile() && file.exists()) {   
		    	flag = file.delete();   
//		        flag = true;   
		    }   
		    return flag;   
		}
		
 
	
	public static Bitmap getImageFromSystem(Context context,String path, ReuseImage reuseImage) {
	    int screenWid = KWDataCache.getScreenWidth(context.getResources());
	    return decodeFileDescriptor(path, screenWid, reuseImage);
	}

 
		
		public static boolean saveBitmap(Bitmap bt, String key,String path){
			if(bt == null){
				return false;
			}
		
			try {
				 DebugLog.d(TAG,"saveBitmap path:" + path);
				 DebugLog.d(TAG,"saveBitmap key:" + key);
				 byte[] bts = convertBitmap(bt);
				 saveThumbnail(bts, key, path);
				 return saveBitmap(bts, key, path);
			} catch (Exception e) {
				DebugLog.d(TAG,"saveBitmap error");
				e.printStackTrace();
			}
			return false;
		}
	    
		public static void delFile(Context context,String url){
    		String key = DiskUtils.constructFileNameByUrl(url);
    		String filePath = DiskUtils.getCachePath(context) + File.separator +
    				DiskUtils.WALLPAPER_BITMAP_FOLDER + File.separator + key;
        	DiskUtils.deleteFile(filePath);
        	DiskUtils.deleteFile(filePath + DiskUtils.THUMBNAIL);
		}
		

    public static Bitmap Bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }

    public static byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

  
    public static void saveThumbnail(Bitmap bitmap, String key, String path) {
        byte[] byteArray = convertBitmap(bitmap);
        saveThumbnail(byteArray, key, path);
    }

  
    public static void saveThumbnail(byte[] byteArray, String key, String path) {

        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inSampleSize = 2;
        newOpts.inJustDecodeBounds = false;
        Bitmap thumbnailBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length,
                newOpts);
        DebugLog.d("haokan",
                "saveThumbnail byteArray getWidth = " + thumbnailBitmap.getWidth()
                        + "  getHeight =" + thumbnailBitmap.getHeight());
        byte[] thumbByteArray = convertBitmap(thumbnailBitmap);
        boolean isSuccess = saveBitmap(thumbByteArray, key + THUMBNAIL, path);
		if (!isSuccess) {
			String imgPath = path + "/" + key + THUMBNAIL;
			DiskUtils.deleteFile(imgPath);
		}
        thumbnailBitmap.recycle();
    }
    
    public static void saveDefaultThumbnail(Context context, FileInputStream fis, String absolutePath) {
        FileDescriptor fd;
        String key = constructFileNameByUrl(absolutePath);
        
        String path = getCachePath(context) + File.separator +
                DiskUtils.WALLPAPER_BITMAP_FOLDER;
        try {
            fd = fis.getFD();
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inJustDecodeBounds = false;
            options.inSampleSize = 1;
            Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fd, null, options);
            saveThumbnail(bitmap, key, path);
            bitmap.recycle();
        } catch (Exception e) {
        	Log.d(TAG, "", e);
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
        	Log.d(TAG, "", e);
        }
    }
    

  
 
    public static String constructThumbFileNameByUrl(String url) {
        return constructFileNameByUrl(url) + THUMBNAIL;
    }
    
    public static String getAbsolutePath(Context context, String imgUrl) {
        String fileName = constructFileNameByUrl(imgUrl);
        return new StringBuffer(getCachePath(context)).append(File.separator)
                .append(WALLPAPER_BITMAP_FOLDER).append(File.separator).append(fileName).toString();
    }
    
		
}
