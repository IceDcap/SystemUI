package com.amigo.navi.keyguard.fingerprint;

import java.util.ArrayList;

import com.amigo.navi.keyguard.DebugLog;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class FingerThread  extends Thread{
	
	private static FingerThread instance;
	private static final String TAG="FingerThread";
	
    public static FingerThread getInstance(){
    	if(instance==null){
    		new FingerThread();
    	}
    	return instance;
    }

	private FingerThread(){
    	instance=this;
    	
    }
	
	public void init(){
		this.start();
	}

	public Handler mHandler;
    private static final int EXCUTE_TASK=0;
	@SuppressLint("HandlerLeak")
	@Override
	public void run() {
		DebugLog.d(TAG, "FingerThread..."+Thread.currentThread());
		Looper.prepare();
		mHandler =new Handler(){
			@Override
			public void handleMessage(Message msg) {
				 switch (msg.what) {
					case EXCUTE_TASK:
						DebugLog.d(TAG, "FingerThread..handleMessage."+Thread.currentThread());
						Runnable runnable=(Runnable) msg.obj;
						runnable.run();
						break;

					default:
						break;
					}
			}
		};


		Looper.loop();

	}

	
	public void excuteTask(Runnable runnable){
		Message  msg=mHandler.obtainMessage(EXCUTE_TASK, runnable);
		mHandler.sendMessage(msg);
	}
	
}
