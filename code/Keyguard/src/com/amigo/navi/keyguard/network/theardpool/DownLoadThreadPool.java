package com.amigo.navi.keyguard.network.theardpool;

import java.util.ArrayList;
import java.util.Vector;

import com.amigo.navi.keyguard.DebugLog;


public class DownLoadThreadPool extends ThreadPool {
    private Vector<DownLoadWorker> mThreadList = new Vector<DownLoadWorker>();
//    private Object objSync = new Object();

    public DownLoadThreadPool() {
        createExecutor();
    }
    
    
    public void submit(final DownLoadWorker worker) {
        DebugLog.d(TAG, "ThreadPool...submit");
        DownLoadWorker.FinishCallback callback = new DownLoadWorker.FinishCallback() {
            @Override
            public void callback(DownLoadWorker worker) {
                mThreadList.remove(worker);
            }
        };
        worker.setFinishCallback(callback);
        mThreadList.add(worker);
        mExecutor.execute(worker);
    }
    
    public void shutdownPool(){
        for(int index = 0;index < mThreadList.size();index++){
            mThreadList.get(index).cancel();
        }
    }
    
}
