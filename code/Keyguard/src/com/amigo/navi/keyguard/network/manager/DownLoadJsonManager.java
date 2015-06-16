package com.amigo.navi.keyguard.network.manager;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.haokan.Common;
import com.amigo.navi.keyguard.haokan.JsonUtil;
import com.amigo.navi.keyguard.haokan.analysis.MessageModel;
import com.amigo.navi.keyguard.haokan.entity.Client;
import com.amigo.navi.keyguard.network.connect.ConnectionParameters;
import com.amigo.navi.keyguard.network.connect.GetUaUtils;
import com.amigo.navi.keyguard.network.connect.JsonHttpConnect;
import com.amigo.navi.keyguard.network.connect.NetWorkUtils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

public class DownLoadJsonManager {
    private static final String TAG = "DownLoadJsonManager"; 
    public static String sUrlDomain = "http://t-nav.gionee.com/lockimage/";
    private final static String TEST_URL_DOMAIN = "http://t-nav.gionee.com/lockimage/";
    private final static String PRODUCTION_URL_DOMAIN = "http://nav.gionee.com/lockimage/";
    
    //request register
    private static final String CLIENT_VERSION = "v"; 
    private static final String TIMESTAMP = "t"; 
    private static final String REQUEST_FROM = "f";
    private static final String DATA_VERSION = "dv";
    private static final String SIGN = "s"; 
    private static final String USERID = "u"; 
    private static final String DATE_FROM = "df"; 
    private static final String DATE_TO = "dt"; 
    private static final String TYPE_ID_LIST = "tids"; 
    private static final String SCREEN_SIZE = "ss"; 
    
    private static final String DEVICE_NAME = "dn";
    private static final String DEVICE_IMEI = "imei";
    private static final String DEVICE_ICCID = "iccid";
    private static final String DEVICE_SCREEN_SIZE = "ss";
    private static final String DEVICE_SCREEN_MAC = "mac";
    private static final String DEVICE_SCREEN_os = "os";    
    private static final String REQUEST_REGISTER_ID = "create.do?";
    private static final String REQUEST_PICTURES_CATEGORY = "getTypes.do?";
    private static final String REQUEST_PICTURE_LIST = "getImgs.do?";
    private static final String REQUEST_UPLOAD_LOG = "logUpload.do?";
    private static final String SIGN_DIVIDE = "&";
    private static final String CATEGORY_DIVIDE = ",";
    private static final String TEST_VERSION = "edit_version_1.0";
    private static boolean sIsGetWallpaperImmediately = false;
    public static final String ERROR = "connect_error";
    
    private static DownLoadJsonManager sManager = null;
    
    public synchronized static DownLoadJsonManager getInstance() {

        if (sManager == null) {
            sManager = new DownLoadJsonManager();
        }
        return sManager;
    }
    
    private DownLoadJsonManager(){
        
    }
    
    public String registerUserID(Context context){
        Client client = Common.getClientInfo(context.getApplicationContext()); 
        String jsonData = JsonUtil.clientInfoToJsonString(client);
        long currentTimeMillis = System.currentTimeMillis();
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(CLIENT_VERSION, Common.getVersionName()));
        params.add(new BasicNameValuePair(TIMESTAMP,String.valueOf(currentTimeMillis)));
        params.add(new BasicNameValuePair(REQUEST_FROM,String.valueOf(ConnectionParameters.REQUEST_SOURCE)));
        String md5Secret = Common.getMD5String(Common.getVersionName() + "&" + currentTimeMillis + "&" + Common.SECRET);
        params.add(new BasicNameValuePair(SIGN, md5Secret.toUpperCase()));
        String result = "";
        String method = ConnectionParameters.HTTP_POST;
        result = connectMethod(context, params, method,REQUEST_REGISTER_ID,jsonData,false);
        if(ERROR.equals(result)){
        	result = "";
        }
        return result;
    }

    /**
     * @return
     */
    private String selectionVersion() {
        String versionName = "";
        boolean isImmediately = NetWorkUtils.testGetWallpaperImmediately();
        DebugLog.d(TAG,"selectionVersion isImmediately:" + isImmediately);
        if(isImmediately){
            versionName = TEST_VERSION;
        }else{
            versionName = Common.getVersionName();
        }
        return versionName;
    }
    
    public String requestPictureCategory(Context context){
        DebugLog.d(TAG,"requestPictureCategory");
        long currentTimeMillis = System.currentTimeMillis();
        String userID = Common.getUserId(context.getApplicationContext());
        String versionName = selectionVersion();
    	DebugLog.d(TAG,"requestPictureCategory versionName :" + versionName);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(CLIENT_VERSION, versionName));
        params.add(new BasicNameValuePair(TIMESTAMP,String.valueOf(currentTimeMillis)));
        params.add(new BasicNameValuePair(USERID, userID));
        String dataVersion = String.valueOf(Common.getDataVersion(context));
    	DebugLog.d(TAG,"requestPictureCategory dataVersion :" + dataVersion);
        params.add(new BasicNameValuePair(DATA_VERSION,dataVersion));
        params.add(new BasicNameValuePair(REQUEST_FROM,String.valueOf(ConnectionParameters.REQUEST_SOURCE)));
        String md5Secret = Common.getMD5String(versionName + SIGN_DIVIDE
                + userID + SIGN_DIVIDE + currentTimeMillis + SIGN_DIVIDE + Common.SECRET);     
        params.add(new BasicNameValuePair(SIGN, md5Secret.toUpperCase()));      
        String result = "";
        String method = ConnectionParameters.HTTP_GET;
        result = connectMethod(context, params, method,REQUEST_PICTURES_CATEGORY,null,false);
        DebugLog.d(TAG,"requestPictureCategory result:" + result);
        return result;
    }
    
    public String requestPicturesOfCurrentDay(Context context,List<Integer> categoryList){
        DebugLog.d(TAG,"pics requestPicturesOfCurrentDay");
        long currentTimeMillis = System.currentTimeMillis();
        String category = "";
        for(int index = 0;index < categoryList.size();index++){
            if(index != categoryList.size() - 1){
                category += categoryList.get(index) + CATEGORY_DIVIDE;
            }else{
                category += categoryList.get(index);
            }
        }
    	DebugLog.d(TAG,"requestPicturesOfCurrentDay category :" + category);
        String screenSize = Common.getDensityDpiClip(context.getApplicationContext());
        String currentDate = Common.formatCurrentDate();
        String userID = Common.getUserId(context.getApplicationContext());
        String versionName = selectionVersion();
    	DebugLog.d(TAG,"requestPicturesOfCurrentDay versionName :" + versionName);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(CLIENT_VERSION, versionName));
        params.add(new BasicNameValuePair(USERID, userID));
        params.add(new BasicNameValuePair(DATE_FROM, currentDate));
        params.add(new BasicNameValuePair(DATE_TO, currentDate));
        params.add(new BasicNameValuePair(TYPE_ID_LIST, category));
        params.add(new BasicNameValuePair(SCREEN_SIZE, screenSize));
        params.add(new BasicNameValuePair(REQUEST_FROM,String.valueOf(ConnectionParameters.REQUEST_SOURCE)));
        params.add(new BasicNameValuePair(TIMESTAMP,String.valueOf(currentTimeMillis)));
        String secretStr = versionName + SIGN_DIVIDE
                + userID + SIGN_DIVIDE + currentDate + SIGN_DIVIDE + 
                currentDate + SIGN_DIVIDE;
        secretStr += category + SIGN_DIVIDE;
        secretStr +=  screenSize + SIGN_DIVIDE + currentTimeMillis + SIGN_DIVIDE
                + Common.SECRET;
        String md5Secret = Common.getMD5String(secretStr);
        DebugLog.d(TAG,"requestPicturesOfCurrentDay 3");
        params.add(new BasicNameValuePair(SIGN, md5Secret.toUpperCase()));             
        String result = "";
        String method = ConnectionParameters.HTTP_GET;
        result = connectMethod(context, params, method,REQUEST_PICTURE_LIST,null,true);
        DebugLog.d(TAG,"requestPicturesOfCurrentDay result:" + result);
        return result;
    }
   
    public String uploadLogs(Context context,MessageModel messageModel){
        long currentTimeMillis = System.currentTimeMillis();
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(CLIENT_VERSION, Common.getVersionName()));
        params.add(new BasicNameValuePair(TIMESTAMP,String.valueOf(currentTimeMillis)));
        String md5Secret = Common.getMD5String(Common.getVersionName() + "&" + currentTimeMillis + "&" + Common.SECRET);
        params.add(new BasicNameValuePair(SIGN, md5Secret.toUpperCase()));    
        params.add(new BasicNameValuePair(REQUEST_FROM,String.valueOf(ConnectionParameters.REQUEST_SOURCE)));
        String result = "";
        String method = ConnectionParameters.HTTP_GET;
        result = connectMethod(context, params, method,REQUEST_UPLOAD_LOG,messageModel.jsonData,true);
        return result;
    }
    
    private String connectMethod(Context context, List<NameValuePair> params,
            String method,String requestHead,String requestBody,boolean isNeedCompress) {
        DebugLog.d(TAG,"connectMethod");
        boolean isUploadLogs = false;
        String result;
        String ua = GetUaUtils.getUA(context);
        int timeOut = ConnectionParameters.NET_TIMEOUT;
        if (context != null
                && NetWorkUtils.is2GDataNetworkType(context.getApplicationContext())) {
            timeOut = ConnectionParameters.NET_2G_TIMEOUT;
        }
        String url = getUrl() + requestHead;
        String queryString = URLEncodedUtils.format(params, "utf-8");
        URL jsonUrl = NetWorkUtils.constructRequestURL(url, queryString);
        JsonHttpConnect httpConnect = new JsonHttpConnect(timeOut, method, ua, requestBody);
        if (requestHead.equals(REQUEST_UPLOAD_LOG)){
        	isUploadLogs = true;
        }
        result = httpConnect.loadJsonFromInternet(jsonUrl,isNeedCompress,isUploadLogs);
        if(JsonHttpConnect.JSON_ERROR.equals(result) || JsonHttpConnect.CONNECT_ERROR.endsWith(result)){
        	return ERROR;
        }
        return result;
    }
    
    private String getUrl(){
    	String url = "";
        if (NetWorkUtils.testEnvironmentFileOnSDisExist()) {
        	url = TEST_URL_DOMAIN;
        } else {
        	url = PRODUCTION_URL_DOMAIN;
        }
        DebugLog.d(TAG,"getUrl url:" + url);
        return url;
    }
    
}
