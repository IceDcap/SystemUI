package com.android.systemui.gionee;

import java.lang.reflect.Method;
import java.util.Map;

import android.content.Context;
import android.util.Log;

public class GnYouJu {
	private final static String TAG = "Systemui.YouJu";
	private final static String YOUJU_PACKAGE = "com.gionee.youju.statistics.sdk.YouJuAgent";
	
	/**
	 * Call YouJuAgent.init(Context context)
	 * */
	public static void init(Context context) {
		try {
			Class<?> c = Class.forName(YOUJU_PACKAGE);
			Method method = c.getMethod("init", Context.class);
			method.invoke(c, context);
		} catch (Exception e) {
			Log.v(TAG, "initial error happened");
		}
	}
	
	/**
	 * Call YouJuAgent.init(Context context, String appId, String channelId)
	 * @param appId  SystemUi's appId is defined in Androidmanifest.xml, 
	 * 					which is 12F52463A89D37AAE414184F9B33C8E1
	 * @param channelId SystemUi's appId is defined in Androidmanifest.xml, which is systemui
	 * */
	public static void init(Context context, String appId, String channelId) {
		try {
			Class<?> c = Class.forName(YOUJU_PACKAGE);
			Method method = c.getMethod("init", Context.class, String.class, String.class);
			method.invoke(c, context, appId, channelId);
		} catch (Exception e) {
			Log.v(TAG, "initial error happened");
		}
	}
	
	/**
	 * Call YouJuAgent.setReportUncaughtExceptions(boolean flag)
	 * Collect Application's exception
	 * @param flag TRUE means allow to collect information
	 * */
	public static void setReportUncaughtExceptions(boolean flag) {
		try {
			Class<?> c = Class.forName(YOUJU_PACKAGE);
			Method method = c.getMethod("setReportUncaughtExceptions", boolean.class);
			method.invoke(c, flag);
		} catch (Exception e) {
			Log.v(TAG, "Set report uncaught exception happened");
			e.printStackTrace();
		}
	}
	
	/**
	 * Call YouJuAgent.setContinueSessionMillis(long arg)
	 * Set separation to mark whether is a new entry event for an activity
	 * @param arg time in millisecond
	 * */
	public static void setContinueSessionMillis(long arg) {
		Class<?> c;
		try {
			c = Class.forName(YOUJU_PACKAGE);
			Method method = c.getMethod("setContinueSessionMillis", long.class);
			method.invoke(c, arg);
		} catch (Exception e) {
			Log.v(TAG, "Set continue session error happened");
			e.printStackTrace();
		}
	}
	
	/**
	 * Call YouJuAgent.onResume(Context context)
	 * Collect entry event for an activity
	 * */
	public static void onResume(Context context) {
		try {
			Class<?> c = Class.forName(YOUJU_PACKAGE);
			Method method = c.getMethod("onResume", Context.class);
			method.invoke(c, context);
		} catch (Exception e) {
			Log.v(TAG, "onResume error happened");
			e.printStackTrace();
		}
	}
	
	/**
	 * Call YouJuAgent.onPause(Context context)
	 * Collect entry event for an activity, see {@code onResume}
	 * */
	public static void onPause(Context context) {
		try {
			Class<?> c = Class.forName(YOUJU_PACKAGE);
			Method method = c.getMethod("onPause", Context.class);
			method.invoke(c, context);
		} catch (Exception e) {
			Log.v(TAG, "onResume error happened");
			e.printStackTrace();
		}
	}
	
	/**
	 * Call YouJuAgent.onEvent(...)
	 * Collect the information set by caller,
	 * {@code eventLabel} may contains Chinese word, Characters, '_' or number, but not space, or other keyword in YouJu
	 * KeyWords for YouJu are "newpage", ",",  "￥"  "#”  "?"  "&"
	 * @param context
	 * @param appId SystemUi's appId
	 * @param eventLabel label to mark the current event
	 * @param map event's detail
	 * */
	public static void onEvent(Context context, String appId, String eventLabel, Map<String, Object> map) {
		try {
			Class<?> c = Class.forName(YOUJU_PACKAGE);
			Method method = c.getMethod("onEvent", Context.class, String.class, String.class, Map.class);
			method.invoke(c, context, appId, eventLabel, map);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void onEvent(Context context, String appId, String eventLabel) {
		try {
			Class<?> c = Class.forName(YOUJU_PACKAGE);
			Method method = c.getMethod("onEvent", Context.class, String.class, String.class);
			method.invoke(c, context, appId, eventLabel);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Call YouJuAgent.onError(...)
	 */
	public static void onError(Context context, Exception e) {
		try {
			Class<?> c = Class.forName(YOUJU_PACKAGE);
			Method method = c.getMethod("onError", Context.class, Exception.class);
			method.invoke(c, context, e);
		} catch (Exception local) {
			// TODO Auto-generated catch block
			local.printStackTrace();
		}
	}
}
