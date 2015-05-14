package com.amigo.navi.keyguard.haokan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ShutdownBroadcastReceiver extends BroadcastReceiver {  
 
    private static final String TAG = "ShutdownBroadcastReceiver";  
      
    private static final String ACTION_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN";  
    
    private static UpdatePage mUpdatePage = null;
    
    @Override 
    public void onReceive(Context context, Intent intent) {  
          
        if (intent.getAction().equals(ACTION_SHUTDOWN)) { 
        	if(mUpdatePage != null){
        		mUpdatePage.update();
        	}
        }  
    }  
    
    public static void setUpdatePage(UpdatePage updatePage){
    	mUpdatePage = updatePage;
    }
    
    public interface UpdatePage{
    	public void update();
    }    
}  