package com.amigo.navi.keyguard.util;

import java.lang.reflect.Method;

import com.amigo.navi.keyguard.DebugLog;

public class ReflectionUtils {
    private final static String LOG_TAG = "ReflectionUtils";
    
    /**
     * set the page index into fanfan or other widgets
     */
    public static void setPageIndex(Object widget, int pageId) {
    	if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "call widget.setScreen(), pageId=" + pageId);
        applyMethod(widget, "setScreen", pageId);
    }

    /**
     * notify fanfan or other widgets the current showing page's index
     */
    public static void setCurrentPageIndex(final Object widget, final int pageId) {
    	if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "call widget.setCurScreen(), pageId=" + pageId);
		new Thread(){
			@Override
			public void run() {
				applyMethod(widget, "setCurScreen", pageId);
			}
		}.start();
		// there is a problem: invoke setCurScreen() method in a thread will
		// cause the fanfan UI broken
    }
    
    // methods for fanfan begin
    
    public static void attachFanfanToKeyguard(Object widget) {
    	if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "call fanfan.onAttachedToLockScreen()");
        applyMethod(widget, "onAttachedToLockScreen");
    }
    
    public static void detachFanfanFromKeyguard(Object widget) {
    	if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "call fanfan.detachedFanfanFromKeyguard()");
        applyMethod(widget, "onDetachedFromLockScreen");
    }
    

    private static void applyMethod(Object ob, String methodName, int value) {
        try {
            Method method = ob.getClass().getMethod(methodName, int.class);
            method.invoke(ob, value);
        } catch (Exception e) {
        	DebugLog.d(LOG_TAG, "ReflectionUtils " + methodName + " error ob=" + ob+"Exception"+e);
        }
    }
    

    private static void applyMethod(Object object, String methodName) {
        try {
            Method method = object.getClass().getMethod(methodName);
            method.invoke(object);
        } catch (Exception e) {
        	DebugLog.d(LOG_TAG, "ReflectionUtils " + methodName + " error ob=" + object+"Exception"+e);
        }
    }
    
    // methods for fanfan end
}
