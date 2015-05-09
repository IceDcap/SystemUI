package com.amigo.navi.keyguard.network.theardpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {
    public static final String TAG="ThreadPool";
    protected static final int CORE_POOL_SIZE = 3;
    protected static final int MAX_POOL_SIZE = 6;
    protected static final int KEEP_ALIVE_TIME = 5000; // 10 seconds

    protected static int sPrioPolicy = 201;
    protected LinkedBlockingQueue<Runnable> mTaskQueue = new LinkedBlockingQueue<Runnable>();
    protected ExecutorService mExecutor;

    
    public void createExecutor() {
        mExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                mTaskQueue, new ThreadPoolExecutor.DiscardOldestPolicy());
    }

    
    public void submit(Worker worker) {
            mExecutor.execute(worker);
        }
    
}
