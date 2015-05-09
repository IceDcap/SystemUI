package com.amigo.navi.keyguard.network.theardpool;

import com.amigo.navi.keyguard.DebugLog;

public class DownLoadWorker extends Worker {
    private static final String TAG = "Worker";
    private String mMethod = "GET";
    private String mUrl;
    private FinishCallback mCallback = null;

    public DownLoadWorker(Job job) {
        super(job);
    }
    
    public void run() {
        try {
            DebugLog.d(TAG, "Worker...run"+Thread.currentThread().getName()+"mJob="+(mJob==null));
            mExecThread = Thread.currentThread();
            mJob.runTask();

        } catch (Throwable ex) {
            
        } finally {
            mJob = null;
//            if (mExecThread instanceof CancelableThread) {
//                ((CancelableThread) mExecThread).isThreadCancelled();
//            }
            mExecThread = null;
            if(mCallback != null){
                mCallback.callback(this);
            }
        }
    }
    
    public synchronized void cancel() {
        if (mExecThread == null) {
            mTaskQueue.remove(this);
//            if (mExecThread != null) {
//                ((CancelableThread) mExecThread).cancelThread();
//            }
        } else {
            if (mJob != null) {
                mJob.cancelTask();
            }
        }
        if(mCallback != null){
            mCallback.callback(this);
        }
    }
    
    public boolean compareThread(String method, String url) {
        boolean ret = true;
        if (null == url || null == method)
            return false;
        if (method.compareTo(this.mMethod) != 0) {
            ret = false;
        } else if (url.toString().compareTo(this.mUrl) != 0) {
            ret = false;
        }
        return ret;
    }
    
    public void setMethod(String method){
        mMethod = method;
    }
    
    public String getMethod(){
        return mMethod;
    }
    
    public void setUrl(String url){
        mUrl = url;
    }
    
    public String getUrl(){
        return mUrl;
    }
  
    public interface FinishCallback{
        public void callback(DownLoadWorker worker);
    }
    
    public void setFinishCallback(FinishCallback callback){
        mCallback = callback;
    }
    
}
