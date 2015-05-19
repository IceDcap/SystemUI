/*******************************************************************************
 * Filename:
 * ---------
 *  EmotionDownloadThread.java
 *
 * Project:
 * --------
 *   com.amigo.emotion
 *
 * Description:
 * ------------
 *   心情壁纸下载模块下载基类
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

import java.util.ArrayList;
import java.util.Vector;

import android.content.Context;
import android.util.Log;

public class LoadImageThread implements Runnable {
    private static String LOG_TAG = "EmotionDownloadThread";
    public String mUrl;
    protected Context mCxt;
    private Job mJob = null;
    private Vector<LoadImageThread> mThreadList = null;
    public LoadImageThread(String url,Job job,Vector<LoadImageThread> threadList) {
        this.mUrl = url;
        mJob = job;
        mThreadList = threadList;
    }
    
    
    public void stop(){
    	mJob.cancelTask();
    }
    
    public boolean compareThread(String url) {
        boolean ret = true;
        if (null == url)
            return false;
        if (!url.equals(this.mUrl)) {
            ret = false;
        }
        return ret;
    }

    @Override
    public void run() {
    	mJob.runTask();
        boolean ret = false;
        ret = mThreadList.remove(this);
        Log.d(LOG_TAG, "run() end ret=" + ret);
    }
    


    
}
// Gionee <pengwei><2014-03-05> modify for CR01095632 end