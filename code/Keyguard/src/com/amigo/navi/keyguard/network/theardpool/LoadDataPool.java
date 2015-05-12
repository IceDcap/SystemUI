/*******************************************************************************
 * Filename:
 * ---------
 *  EmotionDownLoadMgr.java
 *
 * Project:
 * --------
 *   Emotion
 *
 * Description:
 * ------------
 *   心情壁纸下载管理类
 *
 * Author:
 * -------
 * pengwei@gionee.com
 *
 * Date:
 * 2014.03.05
 ****************************************************************************/
//Gionee <pengwei><2014-03-05> modify for CR01095632 begin
package com.amigo.navi.keyguard.network.theardpool;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.amigo.navi.keyguard.DebugLog;

public class LoadDataPool {

    private static String TAG = "LoadDataPool";
    private static LoadDataPool mDownLoadMgr = null;
    private Context mCxt;
    private Object objSync = new Object();

    public static ExecutorService sThreadPool;
    private ArrayList<LoadImageThread> mThreadList = new ArrayList<LoadImageThread>();
    

    LoadDataPool(Context cxt) {
        mCxt = cxt;
    }

    static {
        sThreadPool = Executors.newCachedThreadPool();
    }

    private static void gnDownLoadExecute(Runnable runnable) {
        sThreadPool.execute(runnable);
    }

    public static synchronized LoadDataPool getInstance(Context cxt) {
        if (null == mDownLoadMgr) {
            mDownLoadMgr = new LoadDataPool(cxt);
        }
        return mDownLoadMgr;
    }

    public void loadImage(LoadImageThread runnable,String url) {
            DebugLog.d(TAG,"loadImage url:" + url);
            synchronized (objSync) {
                if (containInThreadPool(url)) {
                    return;
                }
                mThreadList.add(runnable);
            }
            gnDownLoadExecute(runnable);
    }
    
    private boolean containInThreadPool(String url) {
        boolean ret = false;
        LoadImageThread th = null;
        for (int i = 0; i < mThreadList.size(); i++) {
            th = mThreadList.get(i);
            DebugLog.d(TAG,"containInThreadPool th:" + th);
            if (null == th) {
                continue;
            }
            DebugLog.d(TAG,"containInThreadPool url:" + url);
            ret = th.compareThread(url);
        }
        return ret;
    }

    public ArrayList<LoadImageThread> getDownLoadThreadList() {
        return mThreadList;
    }
    
    public void stopTask(String url){
        LoadImageThread th = null;
        for (int i = 0; i < mThreadList.size(); i++) {
            th = mThreadList.get(i);
            DebugLog.d(TAG,"containInThreadPool th:" + th);
            if (null == th) {
                continue;
            }
            DebugLog.d(TAG,"containInThreadPool url:" + url);
            boolean ret = th.compareThread(url);
            if(ret){
            	th.stop();
            }
        }
    }
    
    public void stopTaskDiffUrl(String url){
        LoadImageThread th = null;
        for (int i = 0; i < mThreadList.size(); i++) {
            th = mThreadList.get(i);
            DebugLog.d(TAG,"containInThreadPool th:" + th);
            if (null == th) {
                continue;
            }
            DebugLog.d(TAG,"containInThreadPool url:" + url);
            boolean ret = th.compareThread(url);
            if(!ret){
            	th.stop();
            }
        }
    }
    
    public void stopAllTask(){
        LoadImageThread th = null;
        for (int i = 0; i < mThreadList.size(); i++) {
            th = mThreadList.get(i);
            DebugLog.d(TAG,"containInThreadPool th:" + th);
            if (null == th) {
                continue;
            }
            th.stop();
        }
    }
    
}
// Gionee <pengwei><2014-03-05> modify for CR01095632 end
