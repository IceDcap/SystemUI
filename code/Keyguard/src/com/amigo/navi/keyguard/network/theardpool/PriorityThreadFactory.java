package com.amigo.navi.keyguard.network.theardpool;

import android.util.Log;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * A thread factory that creates threads with a given thread priority.
 */
public class PriorityThreadFactory implements ThreadFactory {

    private static final String TAG = "PriorityThreadFactory";
    private final AtomicInteger mNumber = new AtomicInteger();

 public  class CancelableThread extends Thread {
        private boolean mCancelled = false;

        public CancelableThread(Runnable r) {
            super(r);
        }

        public boolean isThreadCancelled() {
            // This method automatically reset "cancelled" flag
            boolean wasCancelled = mCancelled;
            mCancelled = false;
            return wasCancelled;
        }

        public void cancelThread() {
            mCancelled = true;
        }

        public void run() {
            super.run();
        }
    }

    public PriorityThreadFactory() {

    }

    @Override
    public Thread newThread(Runnable r) {
    	Log.d(TAG, "newThread");
        return new CancelableThread(r);
    }

}