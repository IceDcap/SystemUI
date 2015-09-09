package com.amigo.navi.keyguard.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.ActivityOptions;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews.OnClickHandler;

import com.amigo.navi.keyguard.DebugLog;

public class KeyguardWidgetUtils {
    private final static String LOG_TAG = "KeyguardWidgetUtils";
    
    public static final String PKG_NAME_SHORTCUT_TOOLS = "com.gionee.navil.shortcuttools";
    public static final String CLS_NAME_SHORTCUT_TOOLS = "com.gionee.navil.shortcuttools.ShortcutToolsWidget";
    public static final String CLS_NAME_IMPLGNWIDGET_SHORTCUT_TOOLS = "com.gionee.navil.shortcuttools.ShortcutWidgetFrame";
    
    public static final String PKG_NAME_FANFAN = "com.gionee.fanfan";
    public static final String CLS_NAME_FANFAN = "com.gionee.fanfan.PluginView";
    public static final String CLS_NAME_FANFAN2 = "com.gionee.fanfan.FANFANWidgetProvider";
    public static final String CLS_NAME_IMPLGNWIDGET_FANFAN = "com.gionee.fanfan.view.RootView";
    
    public static final String PKG_NAME_MUSIC = "com.android.music";
    private static final String CLS_NAME_MUSIC = "com.android.music.MediaAppWidgetProvider";
    private static final String CLS_NAME_MUSIC_SKYLIGHT = "skylight_music_widget";
    
	
	
	public static final String KEY_REENABLE_CLS_NAME = "cls_name";
	public static final String VALUE_REENABLE_CLS_NAME = "com.gionee.navi.fanfan";
	
    public static final int MAX_WIDGET_CAN_BE_ADDED = 5;
    
    // widget should impl this interface
    // shouldn't change it to AmigoWidget, because there is 
    // not existed AmigoWidget at all
    private static final String WIDGET_TAG = "GnWidget";
    private ArrayList<Object> mGnWidgets = new ArrayList<Object>();
    
    private static KeyguardWidgetUtils sInstance = null;
    private Context mContext = null;
    
    private static final int APPWIDGET_HOST_ID = 0x4B455947;
    private AppWidgetManager mAppWidgetManager = null;
    private KeyguardAppWidgetHost mAppWidgetHost = null;
    
    public synchronized static KeyguardWidgetUtils getInstance(Context context) {
    	if(sInstance == null) {
    		sInstance = new KeyguardWidgetUtils(context);
    	}
    	
    	return sInstance;
    }
    
    private KeyguardWidgetUtils(Context context) {
    	mContext = context;
    	mAppWidgetManager = AppWidgetManager.getInstance(context);
    	mAppWidgetHost = new KeyguardAppWidgetHost(context, 
    			APPWIDGET_HOST_ID, mOnClickHandler, Looper.myLooper());
    }

    public synchronized void register(Object vs) {
        asserts(!mGnWidgets.contains(vs), "vs=" + vs + ",this="+this.hashCode());
        if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "register vs=" + vs + ",this="+this.hashCode());
        mGnWidgets.add(vs);
    }

    public synchronized void unregister(Object vs) {
        asserts(mGnWidgets.contains(vs), "vs=" + vs + ",this="+this.hashCode());
        if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "unregister vs=" + vs + ",this="+this.hashCode());
        mGnWidgets.remove(vs);
    }

    public synchronized void unregisterAll() {
    	if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "unregisterAll this="+this.hashCode());
        mGnWidgets.clear();
    }

    public synchronized boolean has(Object vs) {
        boolean b = mGnWidgets.contains(vs);
        if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "has b = " + b);
        return b;
    }

    public static Object restractAmigoWidget(View view) {
        Object result = null;

        if (isAmigoWidget(view)) {
            result = view;
        } else {
            if (view instanceof ViewGroup) {
                final ViewGroup parent = (ViewGroup) view;
                final int count = parent.getChildCount();
                for (int i = 0; i < count; ++i) {
                    View v = parent.getChildAt(i);
                    result = restractAmigoWidget(v);
                    if (result != null) {
                        break;
                    }
                }
            }
        }
        if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "restractGnWidget result = " + result);
        return result;
    }

    private static boolean isAmigoWidget(Object view) {
        if (view != null) {
            Class<?>[] interfaces = view.getClass().getInterfaces();
            for (Class<?> inf : interfaces) {
                if (WIDGET_TAG.equals(inf.getSimpleName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void asserts(boolean b, String msg) {
        if (!b) {
        	DebugLog.e(LOG_TAG, msg, new Exception());
        }
    }

    public void setCurrentPageIndex(int screenId) {
    	if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "setCurScreen screenId=" + screenId + ",size="+mGnWidgets.size()+",this="+this.hashCode());
        ArrayList<Object> gnWidgets = mGnWidgets;
        for (Object ob : gnWidgets) {
        	if(DebugLog.DEBUG) DebugLog.d(LOG_TAG,"setCurScreen ob="+ob);
            ReflectionUtils.setCurrentPageIndex(ob, screenId);
        }
    }
    
    private OnClickHandler mOnClickHandler = new OnClickHandler() {
        @Override
        public boolean onClickHandler(final View view, final android.app.PendingIntent pendingIntent,
                final Intent fillInIntent) {
        	if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "onClickHandler");
            if (pendingIntent.isActivity()) {
            	if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "onClickHandler 1");

                try {
                    // TODO: Unregister this handler if
                    // PendingIntent.FLAG_ONE_SHOT?
                    Context context = view.getContext();
                    ActivityOptions opts = ActivityOptions.makeScaleUpAnimation(view, 0, 0,
                            view.getMeasuredWidth(), view.getMeasuredHeight());
                    context.startIntentSender(pendingIntent.getIntentSender(), fillInIntent,
                            Intent.FLAG_ACTIVITY_NEW_TASK, Intent.FLAG_ACTIVITY_NEW_TASK, 0, opts.toBundle());
                } catch (IntentSender.SendIntentException e) {
                	Log.d(LOG_TAG, "Cannot send pending intent: ", e);
                } catch (Exception e) {
                	Log.d(LOG_TAG, "Cannot send pending intent due to " + "unknown exception: ", e);
                }
                
//                if (mMediatorCallback != null) {
//                    mMediatorCallback.userActivity(0);
//                    mMediatorCallback.keyguardDone(true);
//                }
                return true;

            } else {
            	if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "onClickHandler 2");
                return super.onClickHandler(view, pendingIntent, fillInIntent);
            }
        }
    };
    
    public AppWidgetManager getAppWidgetManager() {
    	return mAppWidgetManager;
    }
    
    public AppWidgetHost getAppWidgetHost() {
    	return mAppWidgetHost;
    }
    
    public void startHostListening() {
    	mAppWidgetHost.startListening();
    }
    
    public void stopHostListening() {
    	mAppWidgetHost.stopListening();
    }
    
    // methods for fanfan widget begin
    
    public void attachFanfan() {
    	if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "attachFanfan() has been invoked");
    	for(Object widget : mGnWidgets) {
    		String clsName = widget.getClass().getName();
    		if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "widget class name " + clsName);
    		if(clsName.equals(CLS_NAME_IMPLGNWIDGET_FANFAN)) {
    			if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "attachFanfan()");
    			ReflectionUtils.attachFanfanToKeyguard(widget);
    			break;
    		}
    	}
    }
    
    public void detachFanfan() {
    	if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "detachFanfan() has been invoked");
    	for(Object widget : mGnWidgets) {
    		String clsName = widget.getClass().getName();
    		if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "widget class name " + clsName);
    		if(clsName.equals(CLS_NAME_IMPLGNWIDGET_FANFAN)) {
    			if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "detachFanfan()");
    			ReflectionUtils.detachFanfanFromKeyguard(widget);
    			break;
    		}
    	}
    }
    
    // methods for fanfan widget end
    
    
    // methods for widget creation begin
    
    private Map<String, Integer> mWidgetIdMap = new HashMap<String, Integer>();
    
    public AppWidgetHostView createWidget(String pkgName, String clsName) {
    	Integer widgetId = mWidgetIdMap.get(clsName);
    	if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "createWidget(): " + clsName+",widgetId:"+widgetId);
    	if(widgetId == null){
    		widgetId = mAppWidgetHost.allocateAppWidgetId();
    		ComponentName widgetName = new ComponentName(pkgName, clsName);
    		Bundle options = new Bundle();
    		options.putInt(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY,
    				AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD);
    		try {
    			mAppWidgetManager.bindAppWidgetId(widgetId, widgetName, options);
    		} catch(Exception e) {
    			DebugLog.d(LOG_TAG, "fail to bind widget"+"Exception"+e);
    			return null;
    		}
    	}
    	return getWidget(widgetId, clsName);
         
     }
    
    public AppWidgetHostView createMusicWidgetForSkylight() {
        int musicWidgetId = mAppWidgetHost.allocateAppWidgetId();
        ComponentName widgetName = new ComponentName(PKG_NAME_MUSIC, CLS_NAME_MUSIC);
        Bundle options = new Bundle();
        options.putInt(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY,
                SKYLIGHT_MUSIC_WIDGET_INDEX);
        try {
            mAppWidgetManager.bindAppWidgetId(musicWidgetId, widgetName, options);
            DebugLog.d(LOG_TAG, "createMusicWidgetForSkylight musicWidgetId: "+musicWidgetId);
        } catch(Exception e) {
            DebugLog.d(LOG_TAG, "createMusicWidgetForSkylight Exception: "+e.getMessage());
            return null;
        }
        
        AppWidgetProviderInfo widgetInfo = mAppWidgetManager.getAppWidgetInfo(musicWidgetId);
        if(widgetInfo != null) {
            mWidgetIdMap.put(CLS_NAME_MUSIC_SKYLIGHT, musicWidgetId);
            return mAppWidgetHost.createView(mContext, musicWidgetId, widgetInfo);
        } else {
            mAppWidgetHost.deleteAppWidgetId(musicWidgetId);
        }
        
        return null;
    }
    
    
    public AppWidgetHostView getWidget(int widgetId,String clsName) {
		AppWidgetProviderInfo widgetInfo = mAppWidgetManager.getAppWidgetInfo(widgetId);
		if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "getWidget--widgetInfo:"+widgetInfo);
		if(widgetInfo != null) {
			mWidgetIdMap.put(clsName, widgetId);
			return mAppWidgetHost.createView(mContext, widgetId, widgetInfo);
		} else {
			mAppWidgetHost.deleteAppWidgetId(widgetId);
			return null;
		}
    }
    
    public static final int WIDGET_NAME_FANFAN = 0;
    public static final int WIDGET_NAME_SHORTCUT_TOOLS = 1;
    public static final int WIDGET_NAME_MUSIC = 2;
    public static final int WIDGET_NAME_MUSIC_SKYLIGHT = 3;
    private static final int SKYLIGHT_MUSIC_WIDGET_INDEX=3;
    
    public AppWidgetHostView createWidget(int widgetNameId) {
    	if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "createWidget() widgetNameId=" + widgetNameId);
    	AppWidgetHostView widget = null;
    	if(widgetNameId == WIDGET_NAME_FANFAN) {
    		widget = createWidget(PKG_NAME_FANFAN, CLS_NAME_FANFAN);
    	} else if(widgetNameId == WIDGET_NAME_SHORTCUT_TOOLS) {
    		widget = createWidget(PKG_NAME_SHORTCUT_TOOLS, CLS_NAME_SHORTCUT_TOOLS);
    	} else if(widgetNameId == WIDGET_NAME_MUSIC) {
    		widget = createWidget(PKG_NAME_MUSIC, CLS_NAME_MUSIC);
    	}
    	
    	return widget;
    }
    
    public void deleteWidget(int widgetNameId){
    	if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "deleteWidget" + widgetNameId);
    	String clsName = null;
    	if(widgetNameId == WIDGET_NAME_FANFAN) {
    		clsName = CLS_NAME_FANFAN;
    	} else if(widgetNameId == WIDGET_NAME_SHORTCUT_TOOLS) {
    		clsName = CLS_NAME_SHORTCUT_TOOLS;
    	} else if(widgetNameId == WIDGET_NAME_MUSIC) {
    		clsName = CLS_NAME_MUSIC;
    	}else if(widgetNameId == WIDGET_NAME_MUSIC_SKYLIGHT){
    	    clsName = CLS_NAME_MUSIC_SKYLIGHT;
    	}
    	
    	Integer widgetId = mWidgetIdMap.get(clsName);
    	if(widgetId != null){
    		mAppWidgetHost.deleteAppWidgetId(widgetId);
    		mWidgetIdMap.remove(clsName);
    	}
    
    }
    
        
    public static final int WIDGET_PAGE_ID_LAUNCHER = -1;
    public static final int WIDGET_PAGE_ID_FANFAN = 0;
    public static final int WIDGET_PAGE_ID_HOME = 1;
    public static final int WIDGET_PAGE_ID_SHORTCUT_TOOLS = 2;
    
    // methods for widget page id end
}


