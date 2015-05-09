package com.amigo.navi.keyguard.network.theardpool;

import java.util.Hashtable;
import java.util.Vector;

import com.amigo.navi.keyguard.DebugLog;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

public final class ThreadUtil {
	private static final String TAG = "ThreadUtil";
	
	private boolean mIsOccupy = false;
	private static final String PENDING_RUNNABLE = "pendingRunnable";
	

	private final HandlerThread mWorkThread = new HandlerThread("work-thread");
	{
		mWorkThread.start();
	}
	private final Handler mWorkHandler = new Handler(mWorkThread.getLooper());
	
	private DeferredHandler mUIHandler = new DeferredHandler();
	
	private Hashtable<String, Vector<Runnable>> mRunnablesMap = new Hashtable<String, Vector<Runnable>>();
	
	public enum RunType{
		MAINTHREAD,WORKTHREAD
	}
	
	public void runOnMainThread(Runnable runnable){
		mUIHandler.post(runnable);
	}
	
	public void runOnWorkThread(Runnable runnable){
		if(mIsOccupy){
			addToRunnablesMap(PENDING_RUNNABLE, runnable);
		}else{
			mWorkHandler.post(runnable);
		}
	}
	
	    public void runOnWorkThreadOnlyOne(Runnable runnable){
	        if(mIsOccupy){
	            addToRunnablesMapOnlyOne(PENDING_RUNNABLE, runnable);
	        }else{
	            mWorkHandler.post(runnable);
	        }
	    }
	
	public void runOnWorkThreadIgnoreOccupy(Runnable runnable){
		mWorkHandler.post(runnable);
	}
	
	public void runOnWorkThread(Runnable runnable,long delay){
		mWorkHandler.postDelayed(runnable, delay);
	}
	
	public void addToRunnablesMap(String name ,Runnable runnable){
		if(mRunnablesMap.get(name) == null){
			Vector<Runnable> mRunnables = new Vector<Runnable>();
			mRunnables.add(runnable);
			mRunnablesMap.put(name, mRunnables);
		}else{
			mRunnablesMap.get(name).add(runnable);
		}
	}
	
	   public void addToRunnablesMapOnlyOne(String name ,Runnable runnable){
           DebugLog.d(TAG,"addToRunnablesMapOnlyOne");
	        if(mRunnablesMap.get(name) == null){
	            DebugLog.d(TAG,"addToRunnablesMapOnlyOne1");
	            Vector<Runnable> mRunnables = new Vector<Runnable>();
	            mRunnables.add(runnable);
	            mRunnablesMap.put(name, mRunnables);
	        }else{
	            boolean isContainRunnable = mRunnablesMap.get(name).contains(runnable);
	            DebugLog.d(TAG,"addToRunnablesMapOnlyOne :" + isContainRunnable);
	            if(!isContainRunnable){
	                mRunnablesMap.get(name).add(runnable);
	            }
	        }
	    }
	
	public void runRunnables(String name,RunType type){
		if(mRunnablesMap.get(name) != null){
			Vector<Runnable> mRunnables = mRunnablesMap.get(name);
			for(Runnable r : mRunnables){
				runRunnable(r, type);
			}
			mRunnablesMap.remove(name);
		}
	}
	
	private void runRunnable(Runnable runnable,RunType type){
		if(type == RunType.MAINTHREAD){
			runOnMainThread(runnable);
		}else if(type == RunType.WORKTHREAD){
			runOnWorkThread(runnable);
		}
	}
	
	public void occupyWorkThread(){
		DebugLog.d(TAG, "occupyWorkThread");
		mIsOccupy = true;
	}
	
	public void releaseWorkThread(){
		DebugLog.d(TAG, "releaseWorkThread");
		mIsOccupy = false;
		runRunnables(PENDING_RUNNABLE, RunType.WORKTHREAD);
	}
	
	
//	public void clearRunnables(String name){
//		if(mRunnablesMap.get(name)!=null){
//			mRunnablesMap.put(name, null);
//		}
//	}
	
	public void resetState(){
		mIsOccupy = false;
		mRunnablesMap.clear();
		mWorkHandler.removeCallbacksAndMessages(null);
		mUIHandler.cancel();
	}
	
	public boolean isCallByWorkThread(){
        if (mWorkThread.getThreadId() == Process.myTid()) {
            return true;
        } else {
            return false;
        }
	}
}
