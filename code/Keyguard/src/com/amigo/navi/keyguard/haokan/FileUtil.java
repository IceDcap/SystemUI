
package com.amigo.navi.keyguard.haokan;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.amigo.navi.keyguard.DebugLog;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {
    
    public static final String DIRECTORY_MUSIC          = "/Amigo/ScreenLock/Music/";
    public static final String DIRECTORY_FAVORITE       = "/Amigo/ScreenLock/Favorite";
    public static final String WALLPAPER_LOCATION_FILE  = "/system/etc/ScreenLock/wallpaper.xml";
    
    
    private static final String TAG = "haokan";
    
    /**
     * 
     */
    public static void deleteMusic() {
        DebugLog.d(TAG, "delete all local music");
        String musicPath = Common.getSdCardPath() + DIRECTORY_MUSIC;
        boolean flag = deleteDirectory(musicPath);
        Log.v(TAG, "deleteMusic  flag = " + flag);
    }
    
    
    public static String saveWallpaper(Bitmap bitmap,String imageFileName) {
        
        String localfile = getSdCardPath() + DIRECTORY_FAVORITE;
        isExistDirectory(localfile);
        
        FileOutputStream out = null;
        
        StringBuffer sb = new StringBuffer(localfile).append("/").append(imageFileName);
 
        File file = new File(sb.toString());
        
        if (file != null) {  
            file.delete();  
        }  
        
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); 
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally{
        
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return sb.toString();
        
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
    
}
