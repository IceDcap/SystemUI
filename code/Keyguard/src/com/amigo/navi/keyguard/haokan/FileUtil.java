
package com.amigo.navi.keyguard.haokan;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.network.local.utils.DiskUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileUtil {
    
    public static final String DIRECTORY_MUSIC          = "/Amigo/ScreenLock/Music/";
    public static final String DIRECTORY_FAVORITE       = "/Amigo/ScreenLock/Favorite";
    public static final String WALLPAPER_XML_LOCATION  = "/system/etc/ScreenLock/wallpaper.xml";
    
    public static final String SCREENLOCK_WALLPAPER_LOCATION  = "/system/etc/ScreenLock";
    
    
    private static final String TAG = "haokan";
    
    /**
     * 
     */
    public static void deleteMusic() {
        String musicPath = Common.getSdCardPath() + DIRECTORY_MUSIC;
        boolean flag = deleteDirectory(musicPath);
        DebugLog.d(TAG, "deleteMusic  flag = " + flag);
    }
    
    public static String getDirectoryFavorite() {
        return getSdCardPath() + DIRECTORY_FAVORITE;
    }
    
    public static boolean saveWallpaper(Bitmap bitmap,String imageFileName) {
      
        boolean success = false;
        String localfile = getSdCardPath() + DIRECTORY_FAVORITE;
        isExistDirectory(localfile);
        DebugLog.d(TAG, imageFileName);
        FileOutputStream out = null;
        
        File file = new File(imageFileName);
        
        if (file != null) {  
            file.delete();  
        }  
        
        try {
            out = new FileOutputStream(file);
//            success = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); 
            success = bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "", e);
            success = false;
        } finally{
        
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return success;
        
    }
    
    
    public static boolean deleteDirectory(String sPath) {

        if (!sPath.endsWith(File.separator)) {
            sPath = sPath + File.separator;
        }
        File dirFile = new File(sPath);

        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;

        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {

            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
            else {
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag)
            return false;
        if (dirFile.delete()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean deleteFile(String sPath) {
        boolean flag = false;
        File file = new File(sPath);
        if (file.isFile() && file.exists()) {
            file.delete();
            flag = true;
        }
        return flag;
    }
    
    public static String getSdCardPath(){
        return Environment.getExternalStorageDirectory().getPath();
    }
    
    
    public static void isExistDirectory(String directoryName) {
        File file = new File(directoryName);
        if (!file.exists()) {
            file.mkdirs();
        }
    }
    
    
    
    public static void copyDefaultWallpaperToGallery(Context context) {

        File fileScreenLock = new File(SCREENLOCK_WALLPAPER_LOCATION);//NOSONAR

        String localfile = getSdCardPath() + DIRECTORY_FAVORITE;
        isExistDirectory(localfile);
        if (fileScreenLock.isDirectory()) {
            File[] files = fileScreenLock.listFiles();
            DebugLog.d(TAG, "files.length = " + files.length);
            for (final File file : files) {

                if (file.isFile()) {
                	String absolutePath = file.getAbsolutePath();
                	if(absolutePath.endsWith(".jpg") || absolutePath.endsWith(".png")){
                		FileInputStream fis = null;
                		FileOutputStream fos = null;
                		FileChannel sourceCh = null;
                		FileChannel destCh = null;
                    try {
                        String destPath = localfile+ absolutePath.substring(absolutePath.lastIndexOf("/"));
                        DebugLog.d(TAG, "destPath = " + destPath);
                        File dest = new File(destPath);
                        if (!dest.exists()) {
                            boolean created = dest.createNewFile();
                            DebugLog.d(TAG, "copyDefaultWallpaperToGallery dest.createNewFile()" + created);
                        }

                        fis = new FileInputStream(file);

//                            DiskUtils.saveDefaultThumbnail(context, fis, file.getName());
                        
                        fos = new FileOutputStream(dest);
                        sourceCh = fis.getChannel();
                        destCh = fos.getChannel();
                        long value = sourceCh.transferTo(0, sourceCh.size(), destCh); 
                        DebugLog.d(TAG, "value = " + value);
                        Common.insertMediaStore(context,0, 0, destPath);
                    } catch (Exception e) {
                    	Log.d(TAG, "", e);
                    }finally{
                    	if(sourceCh != null){
                    		try {
								sourceCh.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                    	}
                    	if(destCh != null){
                    		try {
								destCh.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                    	}
                    	if(fis != null){
                    		try {
								fis.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                    	}
                    	if(fos != null){
                    		try {
								fos.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                    	}
                    }
                	}
                }
            }
        }
    }
    
    public static void saveDefaultWallpaperThumbnail(Context context) {

        File fileScreenLock = new File(SCREENLOCK_WALLPAPER_LOCATION);//NOSONAR
        if (fileScreenLock.exists() && fileScreenLock.isDirectory()) {
            File[] files = fileScreenLock.listFiles();
            DebugLog.d(TAG, "files.length = " + files.length);
            for (final File file : files) {
				if (file.isFile()) {
					String absolutePath = file.getAbsolutePath();
					if (absolutePath.endsWith(".jpg") || absolutePath.endsWith(".png")) {
						FileInputStream fis = null;
						try {
							fis = new FileInputStream(file);
							DiskUtils.saveDefaultThumbnail(context, fis,file.getName());
						} catch (Exception e) {
							Log.d(TAG, "", e);
						}finally{
							if(fis != null){
								try {
									fis.close();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}
				}
            }
        }
    }
    
    
    public static File[] getAllFileFormCache(Context context) {
    	File cacheDir = new File(DiskUtils.getCachePath(context) + File.separator + DiskUtils.WALLPAPER_BITMAP_FOLDER);
    	File[] files = null;
    	if (cacheDir.isDirectory() && cacheDir.exists()) {
    		files = cacheDir.listFiles();
		}
		return files;
	}
    
    
}
