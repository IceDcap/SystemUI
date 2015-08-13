package com.android.systemui.gionee.statusbar;

import java.io.FileInputStream;
import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.util.Log;
import android.util.Xml;

import com.android.systemui.R;
import com.android.systemui.statusbar.phone.PhoneStatusBar;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class GnSkyLightStatusBar {
	private static final String TAG = "GnSkyLightStatusBar";

	private Context mContext;
	private final static String ACTION_HALL_STATUS = "android.intent.action.HALL_STATUS";
	private static PhoneStatusBar mPhoneStatusBar;
	private Receiver mReceiver = new Receiver();

	private static final String CHARSET = "UTF-8";
	private static final String SKYLIGHT_LOCATION_FILE="/system/etc/gn_skylight.xml";
    private static final String XML_KEY_XAXIS="xaxis";
    private static int mPadding;
    
	
	public GnSkyLightStatusBar(Context context) {
		super();
		mContext = context;
		readSkylightXaxisFromXml();
		mReceiver.init();
	}
	
	private class Receiver extends BroadcastReceiver {
		
		public void init() {
			IntentFilter filter = new IntentFilter();
			filter.addAction(ACTION_HALL_STATUS);
			
			mContext.registerReceiver(this, filter);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Resources resources = context.getResources();
			int status = intent.getIntExtra("hall_status", 0);
			if(status == 0) {
				mPhoneStatusBar.getStatusBarWindow().setFitsSystemWindows(true);
				mPhoneStatusBar.getStatusBarView().setPadding(mPadding, 0, mPadding, 0);
			} else {
				mPhoneStatusBar.getStatusBarView().setPadding(0, 0, 0, 0);
				mPhoneStatusBar.getStatusBarWindow().setFitsSystemWindows(true);
			}
			Log.d(TAG, " ACTION_HALL_STATUS received and Hall status == " + status);
		}
		
	}

	public void setPhoneStatusBar(PhoneStatusBar statusBar) {
		mPhoneStatusBar = statusBar;
	}

	public int px2dip(Context context, float pxValue) { 
        final float scale = context.getResources().getDisplayMetrics().density; 
        return (int) (pxValue / scale + 0.5f); 
    }
	
	/**
	 * Get xaxis from gn_skylight.xml, In order to config width of StatusBar while skylight showing.
	 */
	public void readSkylightXaxisFromXml() {
		Log.d(TAG, "readSkylightXaxisFromXml------------");
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(SKYLIGHT_LOCATION_FILE);
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(fis, CHARSET);
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					if (XML_KEY_XAXIS.equals(parser.getName())) {
						String xaxisStr = parser.nextText().trim();
//						mPadding = px2dip(mContext, Integer.parseInt(xaxisStr));
						mPadding = Integer.parseInt(xaxisStr);
					}
					break;
				case XmlPullParser.END_TAG:
					break;

				}
				eventType = parser.next();
			}
			
		} catch (Exception e) {
			Log.d(TAG, "exception-----");
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	} 
}
