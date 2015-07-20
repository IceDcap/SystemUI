/*******************************************************************************
 * Filename:
 * ---------
 *  EmotionNetworkUtils.java
 *
 * Project:
 * --------
 *   com.amigo.emotion
 *
 * Description:
 * ------------
 *  Network status utils class  
 *
 * Author:
 * -------
 * pengwei@gionee.com
 *
 * Date:
 * 2014.03.05
 ****************************************************************************/

package com.amigo.navi.keyguard.network.connect;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.settings.KeyguardSettings;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.telephony.TelephonyManager;

//Gionee <pengwei><2014-03-05> modify for CR01095632 begin
public class NetWorkUtils {
    private static final String TAG = "NetWorkUtils";
    private static final String TESTING_ENVIRONMENT_FILE_NAME = "keyguard_test";
    private static final String IMMEDIATELY_GET_WALLPAPER_FILE_NAME = "at_once";
    private static final String PATH_DIVIDE = "//";
    private static boolean isInterruptDownload=false;
//    public static boolean isNetworkAvailable(Context context) {
//        ConnectivityManager connectivityManager = (ConnectivityManager) context
//                .getSystemService(Context.CONNECTIVITY_SERVICE);
//        if (null == connectivityManager.getActiveNetworkInfo()) {
//            return false;
//        }
//        return true;
//    }
    
    public static final int NETWORK_TYPE_NULL = 0;
    public static final int NETWORK_TYPE_2G = 1;
    public static final int NETWORK_TYPE_3G = 2;
    public static final int NETWORK_TYPE_4G = 3;
    public static final int NETWORK_TYPE_WIFI = 4;
    public static final int NETWORK_TYPE_OTHER = 5;
    
    
    private static ConnectivityManager getConnMgr(Context context) {
    	ConnectivityManager connMgr = null;
    	if(context != null) {
    		connMgr = (ConnectivityManager) context
    				.getSystemService(Context.CONNECTIVITY_SERVICE);
    	}
    	
    	return connMgr;
    }
    
    private static NetworkInfo getActiveNetworkInfo(Context context) {
    	NetworkInfo ni = null;
    	ConnectivityManager connMgr = getConnMgr(context);
    	if(connMgr != null) {
    		ni = connMgr.getActiveNetworkInfo();
    	}
    	
    	return ni;
    }
    
    private static boolean isWifiNetwork(int type) {
    	return type == ConnectivityManager.TYPE_WIFI;
    }
    
    private static boolean isMobileNetwork(int type) {
    	return type == ConnectivityManager.TYPE_MOBILE
    			|| type == ConnectivityManager.TYPE_MOBILE_MMS
    			|| type == ConnectivityManager.TYPE_MOBILE_SUPL
    			|| type == ConnectivityManager.TYPE_MOBILE_DUN
    			|| type == ConnectivityManager.TYPE_MOBILE_HIPRI;
    }
    
	private static int distingushMobileNetworkSubType(int subType) {
		int generalSubType = NETWORK_TYPE_NULL;

		switch (subType) {
		case TelephonyManager.NETWORK_TYPE_GPRS:
		case TelephonyManager.NETWORK_TYPE_EDGE:
		case TelephonyManager.NETWORK_TYPE_CDMA:
		case TelephonyManager.NETWORK_TYPE_1xRTT:
		case TelephonyManager.NETWORK_TYPE_IDEN:
		case 16: // TelephonyManager.NETWORK_TYPE_GSM @hide
			generalSubType = NETWORK_TYPE_2G;
			break;
		case TelephonyManager.NETWORK_TYPE_UMTS:
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
		case TelephonyManager.NETWORK_TYPE_HSDPA:
		case TelephonyManager.NETWORK_TYPE_HSUPA:
		case TelephonyManager.NETWORK_TYPE_HSPA:
		case TelephonyManager.NETWORK_TYPE_EVDO_B:
		case TelephonyManager.NETWORK_TYPE_EHRPD:
		case TelephonyManager.NETWORK_TYPE_HSPAP:
			generalSubType = NETWORK_TYPE_3G;
			break;
		case TelephonyManager.NETWORK_TYPE_LTE:
			generalSubType = NETWORK_TYPE_4G;
			break;
		default:
			generalSubType = NETWORK_TYPE_OTHER;
			break;
		}

		return generalSubType;
	}
    
    public static int getNetworkType(Context context) {
    	int generalType = NETWORK_TYPE_NULL;
    	
    	NetworkInfo ni = getActiveNetworkInfo(context);
    	if(ni != null && ni.isConnected()) {
    		int type = ni.getType();
    		if(isWifiNetwork(type)) {
    			generalType = NETWORK_TYPE_WIFI;
    		} else if(isMobileNetwork(type)) {
    			generalType = distingushMobileNetworkSubType(ni.getSubtype());
    		} else {
    			generalType = NETWORK_TYPE_OTHER;
    		}
    	}
    	
    	return generalType;
    }
    

    public static boolean isMobileDataNetwork(Context context) {
        ConnectivityManager connectivityMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityMgr != null) {
            NetworkInfo netInfo = connectivityMgr.getActiveNetworkInfo();
            if (netInfo != null) {
                switch (netInfo.getType()) {
                case ConnectivityManager.TYPE_MOBILE:
                case ConnectivityManager.TYPE_MOBILE_MMS:
                case ConnectivityManager.TYPE_MOBILE_SUPL:
                case ConnectivityManager.TYPE_MOBILE_DUN:
                case ConnectivityManager.TYPE_MOBILE_HIPRI:
                    return true;
                default:
                    return false;
                }
            }
            return false;
        }
        return false;
    }

    public static boolean is2GDataNetworkType(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            int networkType = tm.getNetworkType();
            boolean is2G = (networkType == TelephonyManager.NETWORK_TYPE_1xRTT
                    || networkType == TelephonyManager.NETWORK_TYPE_CDMA
                    || networkType == TelephonyManager.NETWORK_TYPE_EDGE || networkType == TelephonyManager.NETWORK_TYPE_GPRS);
            return is2G;
        }
        return false;
    }

    public static boolean isWifi(Context context) { 
    	ConnectivityManager connectivityManager = (ConnectivityManager) context 
    	.getSystemService(Context.CONNECTIVITY_SERVICE); 
    	NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo(); 
    	if (activeNetInfo != null 
    	&& activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) { 
    		return true; 
    	} 
    	return false; 
    } 
    
    public static File createSavedFileByUrl(String url, String saveFolder) {
        File file = null;

        return file;
    }
    
    public static boolean testEnvironmentFileOnSDisExist() {
        return testFileOnSDExist(TESTING_ENVIRONMENT_FILE_NAME);
    }
    
    public static boolean testGetWallpaperImmediately() {
        return testFileOnSDExist(IMMEDIATELY_GET_WALLPAPER_FILE_NAME);
    }
    
    public static boolean testFileOnSDExist(String folder){
        String sdPath = Environment.getExternalStorageDirectory().getPath();
    	DebugLog.d(TAG,"testFileOnSDExist sdPath:" + sdPath);
        File file = new File(sdPath + PATH_DIVIDE
                + folder);
        if (file.exists()) {
        	DebugLog.d(TAG,"testFileOnSDExist 1");
            return true;
        }
    	DebugLog.d(TAG,"testFileOnSDExist 2");
        return false;
    }
    
    public static URL constructRequestURL(String reqUrl, String queryStr) {
        URL conUrl = null;
        try {
            if (queryStr == null) {
                conUrl = new URL(reqUrl);
            } else {
                conUrl = new URL((reqUrl + queryStr));
            }
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return conUrl;
    }
    
	// 网路是否可用
	public static boolean isNetworkAvailable(Context context) {
		try {
			ConnectivityManager cn = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (cn != null) {
				NetworkInfo info = cn.getActiveNetworkInfo();
				if (info != null && info.isConnected()) {
					if (info.getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;

	}
	
	
	public static boolean needInterruptDownloadOrNot(){
		 return isInterruptDownload;
	}
	
	public static void setInterruptDownload(boolean isInterrupt){
		isInterruptDownload=isInterrupt;
	}
	
	public static  boolean isDownloadingDataFromInternet(Context context) {
		boolean isUpdate = KeyguardSettings.getWallpaperUpadteState(context);
        DebugLog.d(TAG,"isDownloadingDataFromInternet WallpaperUpadteisUpdate:" + isUpdate);
		if(!isUpdate){
			return false;
		}
    	boolean isUpdateOnWifi = KeyguardSettings.getOnlyWlanState(context);
        DebugLog.d(TAG,"isDownloadingDataFromInternet isUpdateOnWifi:" + isUpdateOnWifi);
		if(isUpdateOnWifi){
    		boolean isWifi = NetWorkUtils.isWifi(context);
            DebugLog.d(TAG,"isDownloadingDataFromInternet isWifi:" + isWifi);
    		if(!isWifi){
    			return false;
    		}
		}
		return true;
	}
    
}
// Gionee <pengwei><2014-03-05> modify for CR01095632 end