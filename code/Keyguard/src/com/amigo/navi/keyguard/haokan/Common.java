package com.amigo.navi.keyguard.haokan;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.everydayphoto.NavilSettings;
import com.amigo.navi.keyguard.haokan.entity.Client;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Common {

    
    private static String USER_ID = null;
    
    public static final String SECRET = "GIONEECLIENT";
    
    private static final String TAG = "haokan";
    
    
    private static String SCREEN_SIZE = null;
    
    
    private static Client client = null;
    
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH");
    
    private static boolean isPowerSaverMode = false;
    
    public static int displayPostion = -1;
    public static int displayHour = -1;
    
    public static String getUserId(Context context) {
        if (USER_ID == null) {
            USER_ID = NavilSettings.getStringSharedConfig(context, NavilSettings.USER_ID, null);
        }
        return USER_ID;
    }
    
    public static boolean setSharedConfigUserId(Context context, String userId) {
        boolean commit = NavilSettings
                .setStringSharedConfig(context, NavilSettings.USER_ID, userId);
        USER_ID = userId;
        return commit;
    }
    
    public static boolean setSharedConfigDefaultCategory(Context context, boolean isDefault) {
        boolean commit = NavilSettings
                .setBooleanSharedConfig(context, NavilSettings.DEFAULT_CATEGORY, isDefault);
        return commit;
    }
    
    public static boolean isFirstInitCategory(Context context) {
        boolean isDefault = NavilSettings
                .getBooleanSharedConfig(context, NavilSettings.DEFAULT_CATEGORY, true);
        return isDefault;
    }
    
    
    
    public static String getVersionName() {
        return NavilSettings.getVersionName();
    }

    public static void setVersionName(String version) {
    	NavilSettings.setVersionName(version);
    }
    
     
    public static boolean getNetIsAvailable(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo == null) {
            DebugLog.d(TAG, "isAvailable  = " + false);
            return false;
        }
        isAvailable = networkInfo.isAvailable();

        DebugLog.d(TAG, "isAvailable  = " + isAvailable);
        return isAvailable;
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
    
    
    public static boolean deleteFile(String path) {  
        boolean flag = false;  
        File file = new File(path);  
        if (file.isFile() && file.exists()) {  
            file.delete();  
            flag = true;  
        }  
        return flag;  
    }  
 
    public static boolean isExistSdCard(){
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
    
    public static String formatByteToMB(int size){
        float mb = size / 1024f / 1024f;
        return String.format("%.2f",mb);
    }
    
    
    public static String getMD5String(String s){
        String md5 = null;
        try {
            md5 = MD5Util.getMD5String(s);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return md5;
    }
    
    
    
    public static String getMD5String(String... strings) {
        
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < strings.length; i++) {
            buffer.append(strings[i]);
            buffer.append("&");
        }
        String string = buffer.toString().substring(0, buffer.length()-1);
        return getMD5String(string);
    }
    
    
   
    @SuppressLint("NewApi")
    public static String getDensityDpi(Context context) {
        
        if (SCREEN_SIZE == null) {
            
            Configuration config = context.getApplicationContext().getResources().getConfiguration();
            switch (config.densityDpi) {
                case 240:
                    SCREEN_SIZE = "h";
                    break;
                case 320:
                    SCREEN_SIZE = "xh";
                    break;
                case 480:
                    SCREEN_SIZE = "xxh";
                    break;
                case 640:
                    SCREEN_SIZE = "xxxh";
                    break;
                default:
                    SCREEN_SIZE = "xxh";
                    break;
            }
        }
        
        DebugLog.d(TAG, "SCREEN_SIZE = " + SCREEN_SIZE);
        return SCREEN_SIZE;
    }
    
    
    @SuppressLint("NewApi")
    public static String getDensityDpiClip(Context context) {
        
        if (SCREEN_SIZE == null) {
            
            Configuration config = context.getApplicationContext().getResources().getConfiguration();
            switch (config.densityDpi) {
                case 240:
                    SCREEN_SIZE = "h";
                    break;
                case 320:
                    SCREEN_SIZE = "xh";
                    break;
                case 480:
                    SCREEN_SIZE = "xxh";
                    break;
                case 640:
                    SCREEN_SIZE = "xxxh";
                    break;
                default:
                    SCREEN_SIZE = "xxh";
                    break;
            }
        }
        
        return SCREEN_SIZE;
    }
    
    public static String currentTimeHour() {
        return format.format(new Date());
    }
    
    public static Client getClientInfo(Context context) {

        if (client == null) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE); 
            client = new Client(tm.getDeviceId(), tm.getSimSerialNumber(), tm.getLine1Number(), getDensityDpi(context));
        }
        return client;
    }
    
    
    

    public static Bitmap compBitmap(Bitmap image) {
        Bitmap bitmap = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
//        newOpts.inJustDecodeBounds = true;
//        BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = image.getWidth();
        int h = image.getHeight();
        float hh = 400f;
        float ww = 400f;
        int be = 1;
        if (w >= h && w > ww) {
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0) {
            be = 1;
        }
        newOpts.inSampleSize = be;
//        isBm = new ByteArrayInputStream(baos.toByteArray());
        
        try {
            bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "OutOfMemoryError", e);
        } finally{
            
            try {
                baos.close();
                isBm.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        return bitmap;
    }
    
    private static SimpleDateFormat formatTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//YYYY-MM-DD HH:mm:ss
    public static String currentTimeTime() {
        return formatTime.format(new Date());
    }
    
    private static SimpleDateFormat formatTimeDate = new SimpleDateFormat("yyyy-MM-dd");//YYYY-MM-DD
    public static String currentTimeDate() {
        return formatTimeDate.format(new Date());
    }
    
    private static final int INVALID_VALUE = -1;
    
    private static int sScreenWidth = INVALID_VALUE;
    
    private static int sScreenHeight = INVALID_VALUE;
    
    public static int getScreenWidth(Context context){
        if(sScreenWidth == INVALID_VALUE || sScreenHeight == INVALID_VALUE){
            WindowManager mWm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
            Display display = mWm.getDefaultDisplay();
            DisplayMetrics dm = new DisplayMetrics();
            display.getRealMetrics(dm);
            sScreenWidth = dm.widthPixels;
            sScreenHeight = dm.heightPixels;
        }
        return sScreenWidth;
    }
    
    public static int getScreenHeight(Context context){
        if(sScreenWidth == INVALID_VALUE || sScreenHeight == INVALID_VALUE){
            WindowManager mWm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
            Display display = mWm.getDefaultDisplay();
            DisplayMetrics dm = new DisplayMetrics();
            display.getRealMetrics(dm);
            sScreenWidth = dm.widthPixels;
            sScreenHeight = dm.heightPixels;
        }
        return sScreenHeight;
    }       
    
    public static int getUpdateCategoryDate(Context context) {
        int date = 0;
        date = NavilSettings.getIntSharedConfig(context, NavilSettings.UPDATE_CATEGORY_DATE, 0);
        return date;
    }
    
    public static void setUpdateCategoryDate(Context context, int date) {
        NavilSettings
                .setIntSharedConfig(context, NavilSettings.UPDATE_CATEGORY_DATE, date);
    }
    
    public static int getUpdateWallpaperDate(Context context) {
        int date = 0;
        date = NavilSettings.getIntSharedConfig(context, NavilSettings.UPDATE_WALLPAPER_DATE, 0);
        return date;
    }
    
    public static void setUpdateWallpaperDate(Context context, int date) {
        NavilSettings
                .setIntSharedConfig(context, NavilSettings.UPDATE_WALLPAPER_DATE, date);
    }
    
    public static int getDataVersion(Context context) {
        int date = 0;
        date = NavilSettings.getIntSharedConfig(context, NavilSettings.DATA_VERSION, 0);
        return date;
    }
    
    public static void setDataVersion(Context context, int version) {
        NavilSettings
                .setIntSharedConfig(context, NavilSettings.DATA_VERSION, version);
    }
    
    public static int getHaoKanPage(Context context) {
        int page = 0;
        page = NavilSettings.getIntSharedConfig(context, NavilSettings.HAOKAN_PAGE, 0);
        return page;
    }
    
    public static void setHaoKanPage(Context context, int page) {
        NavilSettings
                .setIntSharedConfig(context, NavilSettings.HAOKAN_PAGE, page);
    }
    
    public static String getHaoKanSavedUrl(Context context) {
        String url = "";
        url = NavilSettings.getStringSharedConfig(context, NavilSettings.HAOKAN_SAVED_PAGE_URL, "");
        return url;
    }
    
    public static void setHaoKanDataInit(Context context, boolean flag) {
        NavilSettings
                .setBooleanSharedConfig(context, NavilSettings.IS_DATA_INIT, flag);
    }
    
    public static String formatCurrentDate() {
        return formatCurrentDate("yyyyMMdd");
    }
    
    public static String formatCurrentTime(){
        return formatCurrentDate("HH:mm");
    }
    
    public static String formatCurrentDate(String type) {
        SimpleDateFormat formatForRequset = new SimpleDateFormat(type);
        return formatForRequset.format(new Date());
    }
    
    public static void setHaoKanSavedUrl(Context context, String url) {
        NavilSettings
                .setStringSharedConfig(context, NavilSettings.HAOKAN_SAVED_PAGE_URL, url);
    }
    
    public static boolean getHaoKanDataInit(Context context) {
        boolean isInit = false;
        isInit = NavilSettings.getBooleanSharedConfig(context, NavilSettings.IS_DATA_INIT, false);
        return isInit;
    }
    
    
    public static void setDatabaseVersion(Context context, String version) {
        NavilSettings
                .setStringSharedConfig(context, NavilSettings.DATABASE_VERSION, version);
    }
    
    public static String getDatabaseVersion(Context context) {
        String version = "";
        version = NavilSettings.getStringSharedConfig(context, NavilSettings.DATABASE_VERSION, "");
        return version;
    }
    
    public static void setLockPosition(Context context, int pos) {
        NavilSettings
                .setIntSharedConfig(context, NavilSettings.LOCK_POSITION, pos);
    }
    
    public static int getLockPostion(Context context) {
        int pos = 0;
        pos = NavilSettings.getIntSharedConfig(context, NavilSettings.LOCK_POSITION, -1);
        return pos;
    }
    
    public static void setLockID(Context context, int pos) {
        NavilSettings
                .setIntSharedConfig(context, NavilSettings.LOCK_ID, pos);
    }
    
    public static int getLockID(Context context) {
        int pos = 0;
        pos = NavilSettings.getIntSharedConfig(context, NavilSettings.LOCK_ID, -1);
        return pos;
    }
    
    public static void setUpdateTime(Context context, String time) {
        NavilSettings
                .setStringSharedConfig(context, NavilSettings.ALARM_TIME, time);
    }
    
    public static String getUpdateTime(Context context) {
        String time = "";
        time = NavilSettings.getStringSharedConfig(context, NavilSettings.ALARM_TIME, "");
        return time;
    }

    public static boolean isPowerSaverMode() {
        return isPowerSaverMode;
    }

    public static void setPowerSaverMode(boolean isPowerSaverMode) {
        Common.isPowerSaverMode = isPowerSaverMode;
    }
    
    public static void insertMediaStore(Context context,int width, int height, String imageFileName) {
        long currentTimeMillis = System.currentTimeMillis();
        long dateSeconds = currentTimeMillis / 1000;
        
        ContentValues values = new ContentValues();
        ContentResolver resolver = context.getContentResolver();
        values.put(MediaStore.Images.ImageColumns.DATA, imageFileName);
        values.put(MediaStore.Images.ImageColumns.TITLE, imageFileName);
        values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, imageFileName);
        values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, currentTimeMillis);
        values.put(MediaStore.Images.ImageColumns.DATE_ADDED, dateSeconds);
        values.put(MediaStore.Images.ImageColumns.DATE_MODIFIED, dateSeconds);
        values.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpg");
        if (width != 0 && height != 0) {
            values.put(MediaStore.Images.ImageColumns.WIDTH, width);
            values.put(MediaStore.Images.ImageColumns.HEIGHT, height);
        }
        
        DebugLog.d(TAG,
                "favoriteLocalPath = " + imageFileName + " imageFileName = "
                        + imageFileName + " dateSeconds = " + dateSeconds
                        + " width=" + width
                        + " height = " + width);
        
        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }
    
    
    public static long getSDFreeSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        long blockSize = sf.getBlockSizeLong();
        long freeBlocks = sf.getAvailableBlocksLong();
         
        return (freeBlocks * blockSize) / 1024l / 1024l;  
    }  
    
    public static boolean SDfree() {
        return getSDFreeSize() >= 10l;
    }
    
    public static boolean isFastClick(long duration) {
        long time = System.currentTimeMillis();
        long timeD = time - mLastClickTime;
        mLastClickTime = time;
        if (0 <= timeD && timeD < duration) {
            return true;
        }
        return false;
    }
    private static long mLastClickTime = 0;
}
