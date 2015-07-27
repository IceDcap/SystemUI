package com.amigo.navi.keyguard.network.manager;
import java.net.URL;
import java.util.ArrayList;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.network.connect.BitmapHttpConnect;
import com.amigo.navi.keyguard.network.connect.ConnectionParameters;
import com.amigo.navi.keyguard.network.connect.NetWorkUtils;
import com.amigo.navi.keyguard.settings.KeyguardSettings;

import android.content.Context;
import android.graphics.Bitmap;

public class DownLoadBitmapManager {
	private static final String TAG="DownLoadBitmapManager";
    private static DownLoadBitmapManager sManager = null;

    public synchronized static DownLoadBitmapManager getInstance() {

        if (sManager == null) {
            sManager = new DownLoadBitmapManager();
        }
        return sManager;
    }
    
    private DownLoadBitmapManager(){
        
    }
    
    public Bitmap downLoadBitmapOld(Context context,String url){
    	String method = ConnectionParameters.HTTP_GET;
        int timeOut = ConnectionParameters.NET_TIMEOUT;
        if (context != null
                && NetWorkUtils.is2GDataNetworkType(context.getApplicationContext())) {
            timeOut = ConnectionParameters.NET_2G_TIMEOUT;
        }
        BitmapHttpConnect download = new BitmapHttpConnect(timeOut,method);
        URL bitmapUrl = NetWorkUtils.constructRequestURL(url,null);
        return download.loadImageFromInternet(bitmapUrl);
    }
    
    public Bitmap downLoadBitmap(Context context,String url){
    	if(!NetWorkUtils.isDownloadingDataFromInternet(context)){
    		return null;
    	}
        String method = ConnectionParameters.HTTP_GET;
        int timeOut = ConnectionParameters.NET_TIMEOUT;
        if (context != null
                && NetWorkUtils.is2GDataNetworkType(context.getApplicationContext())) {
            timeOut = ConnectionParameters.NET_2G_TIMEOUT;
        }
        BitmapHttpConnect download = new BitmapHttpConnect(timeOut,method);
        URL bitmapUrl = NetWorkUtils.constructRequestURL(url,null);
        return download.loadImageFromInternet(bitmapUrl);
    }
    
    public byte[] downLoadBitmapByByte(Context context,String url){
    	if(!NetWorkUtils.isDownloadingDataFromInternet(context)){
    		return null;
    	}
        String method = ConnectionParameters.HTTP_GET;
        int timeOut = ConnectionParameters.NET_TIMEOUT;
        if (context != null
                && NetWorkUtils.is2GDataNetworkType(context.getApplicationContext())) {
            timeOut = ConnectionParameters.NET_2G_TIMEOUT;
        }
        BitmapHttpConnect download = new BitmapHttpConnect(timeOut,method);
        URL bitmapUrl = NetWorkUtils.constructRequestURL(url,null);
        return download.loadImageFromInternetByByte(bitmapUrl);
    }


    
}
