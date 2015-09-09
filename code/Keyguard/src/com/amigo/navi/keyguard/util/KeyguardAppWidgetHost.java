package com.amigo.navi.keyguard.util;


import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.os.Looper;
import android.widget.RemoteViews.OnClickHandler;

import com.amigo.navi.keyguard.DebugLog;

public class KeyguardAppWidgetHost extends AppWidgetHost {
	private final static String LOG_TAG = "KeyguardAppWidgetHost";
	
    public KeyguardAppWidgetHost(Context context, int hostId) {
        super(context, hostId);
    }

    public KeyguardAppWidgetHost(Context context, int hostId, OnClickHandler handler, Looper looper) {
        super(context, hostId, handler, looper);
    }
    
//    @Override
//    protected AppWidgetHostView onCreateView(Context context, int appWidgetId,
//            AppWidgetProviderInfo appWidget) {
//        return new KeyguardAppWidgetHostView(context);
//    }

    // Gionee <jiangxiao> <2014-01-16> modify for CR01001591 begin
    @Override
    public void startListening() {
        try {
            super.startListening();
            if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "startListening()");
        } catch(Exception e) {
        	DebugLog.d(LOG_TAG, "fail to invoke startListening()");        	
        }
    }

    @Override
    public void stopListening() {
    	try {
            super.stopListening();
            if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "stopListening");
        } catch(Exception e) {
        	DebugLog.d(LOG_TAG, "fail to invoke stopListening()");        	
        }
    }
    // Gionee <jiangxiao> <2014-01-16> modify for CR01001591 end
}
