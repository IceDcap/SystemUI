package com.amigo.navi.keyguard.network.theardpool;

import java.util.concurrent.LinkedBlockingQueue;

import com.amigo.navi.keyguard.DebugLog;


public class Worker implements Runnable, Future {
    private static final String TAG = "Worker";
    protected Job mJob;
    protected Thread mExecThread = null;
    protected LinkedBlockingQueue<Runnable> mTaskQueue = null;
    
    public Worker(Job job) {
        mJob = job;
    }
    
    public void setLinkedBlockingQueue(LinkedBlockingQueue<Runnable> taskQueue){
        mTaskQueue = taskQueue;
    }
        
    // This is called by a thread in the thread pool.
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
    }

    public synchronized int getProcess() {
        if (mJob == null) {
            return -1;
        }
        return mJob.getProgress();
    }
}