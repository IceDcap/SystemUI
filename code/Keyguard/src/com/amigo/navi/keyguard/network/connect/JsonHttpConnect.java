package com.amigo.navi.keyguard.network.connect;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.apache.http.HttpStatus;

import com.amigo.navi.keyguard.DebugLog;


public class JsonHttpConnect {
    private static final String TAG = "JsonHttpConnect";
    private String mRequestType = null;
    private int mTimeOut = 0;
    public static String ENCODE = "Accept-Encoding";
    public static String UA = "User-Agent";
    public static String ENCODE_TYPE = "gzip,deflate";
    private String mUa;
    public String mRequestBody = null;
    public static final String CONNECT_ERROR = "connect_error";
    
    public JsonHttpConnect(int timeOut, String method, String ua,String requestBody) {
        mTimeOut = timeOut;
        mRequestType = method;
        mUa = ua;
        mRequestBody = requestBody;
    }

    public String loadJsonFromInternet(URL conUrl,boolean isNeedCompress) {
        DebugLog.d(TAG,"loadJsonFromInternet begin");
        String result = CONNECT_ERROR;
        int reqCode = ConnectionStatus.NETWORK_EXCEPTION;
        InputStream inputStream = null;
        OutputStream os = null;
        ByteArrayOutputStream byteOutputStream = null;
        boolean retOK = false;
        int timeout = mTimeOut;
        try {
            DebugLog.d(TAG,"loadJsonFromInternet conUrl:" + conUrl.toString());
            HttpURLConnection urlConn = (HttpURLConnection) conUrl
                    .openConnection();
            DebugLog.d(TAG,"loadJsonFromInternet mRequestType:" + mRequestType);
            urlConn.setRequestMethod(mRequestType);
            urlConn.setDoOutput(true);
            if(isNeedCompress){
                urlConn.setRequestProperty(ENCODE, ENCODE_TYPE);
            }
            urlConn.setRequestProperty(UA, mUa);
            DebugLog.d(TAG,"loadJsonFromInternet timeout:" + timeout);
            urlConn.setConnectTimeout(timeout);
            urlConn.setReadTimeout(timeout);
            if (mRequestBody != null) {
                DebugLog.d(TAG,"loadJsonFromInternet mRequestBody:" + mRequestBody);
                urlConn.setRequestProperty("Content-Type", "application/json");
                os = urlConn.getOutputStream();
                os.write(mRequestBody.getBytes());
                os.flush();
                os.close();
            }
            reqCode = urlConn.getResponseCode();
            DebugLog.d(TAG,"loadJsonFromInternet reqCode:" + reqCode);
            if (reqCode == HttpStatus.SC_OK) {
                DebugLog.d(TAG,"loadJsonFromInternet ok");
                inputStream = urlConn.getInputStream();
                result = switchResult(isNeedCompress,byteOutputStream,inputStream);
                DebugLog.d(TAG,"loadJsonFromInternet result:" + result);
                retOK = true;
            }
        } catch (SocketTimeoutException e) {
            DebugLog.d(TAG,"loadJsonFromInternet error1:" + e.getMessage());
            e.printStackTrace();
            reqCode = ConnectionStatus.NETWORK_TIMEOUT;
        } catch (Exception e) {
            DebugLog.d(TAG,"loadJsonFromInternet error2:" + e.getMessage());
            e.printStackTrace();
            reqCode = ConnectionStatus.NETWORK_EXCEPTION;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (null != byteOutputStream) {
                try {
                    byteOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
    
    private String switchResult(boolean isNeedCompress,ByteArrayOutputStream byteOutputStream,
            InputStream inputStream){
        String result = "";
        if(isNeedCompress){
            byteOutputStream = new ByteArrayOutputStream();
            GZIPUtils.decompress(inputStream, byteOutputStream);
            try {
                byteOutputStream.flush();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if(byteOutputStream != null){
                DebugLog.d(TAG,"loadJsonFromInternet getString");
                result = byteOutputStream.toString();
                DebugLog.d(TAG,"loadJsonFromInternet result:" + result);
            }
          }else{
              result = GZIPUtils.inputStream2String(inputStream,"UTF-8");
          }
        return result;
    }
    
}
