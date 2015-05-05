package com.amigo.navi.keyguard.skylight;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.KeyguardViewHostManager;

public class SkylightUtil {

    private static final String LOG_TAG = "SkylightUtil";
    private static final int HALL_STATUS_OPEN = 1;
    private static final int HALL_STATUS_CLOSE = 0;
    
    private static final String CHARSET = "UTF-8";
    
    /**
     * fields about gn_skylight.xml 
     */
    private static final String SKYLIGHT_LOCATION_FILE="/system/etc/gn_skylight.xml";
    private static final String XML_KEY_XAXIS="xaxis";
    private static final String XML_KEY_YAXIS="yaxis";
    private static final String XML_KEY_HEIGHT="height";
    private static final String XML_KEY_WIDTH="width";
    
    private static Object sAmigoServerManager=null;
    
    private static int sHallSwitchNodeId=0;//
    
    
	public static boolean getIsHallOpen(Context context) {
		if (sAmigoServerManager == null) {
			sAmigoServerManager = (Object) context
					.getSystemService("amigoserver");
		}
		boolean isHallOpen = true;
		try {
			Class cls = Class
					.forName("android.os.amigoserver.AmigoServerManager");
			Method method = cls.getMethod("GetNodeState", int.class);
			Integer hallValue = (Integer) method
					.invoke(sAmigoServerManager, getHallSwitchNodeId(context));
			if(DebugLog.DEBUG)Log.d(LOG_TAG, "hallValue: "+hallValue);
			isHallOpen = (hallValue != HALL_STATUS_CLOSE);
		} catch (Exception ex) {
			ex.printStackTrace();
			isHallOpen = true;
		}
		return isHallOpen;
	}
    
	private static int getHallSwitchNodeId(Context context){
	    if(sHallSwitchNodeId==0){
	    try {
	        if (sAmigoServerManager == null) {
	            sAmigoServerManager = (Object) context
	                    .getSystemService("amigoserver");
	        }
	        Class cls = Class
                    .forName("android.os.amigoserver.AmigoServerManager");
            Field field= cls.getField("NODE_TYPE_HALL_SWITCH_STATE");
            sHallSwitchNodeId=field.getInt(sAmigoServerManager);
            if(DebugLog.DEBUG)Log.d(LOG_TAG, "getHallSwitchNodeId:   "+sHallSwitchNodeId);
        } catch (Exception e) {
            e.printStackTrace();
        }}
	    return sHallSwitchNodeId;
	}

//    private static String getHallStatus() {
//        String status = HALL_STATUS_OPEN;
//        try {
//            status = readLine(FILENAME_HALL_STATUS);
//            Log.d(LOG_TAG, "status: "+status);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return status;
//    }
//
//    private static String readLine(String filename) throws IOException {
//    	// findbugs
//    	InputStreamReader isr = new InputStreamReader(new FileInputStream(filename), CHARSET);
//    	BufferedReader reader = new BufferedReader(isr,256);  
////        FileReader fr = new FileReader(filename);
////        BufferedReader reader = new BufferedReader(fr, 256);
//        try {
//            return reader.readLine();
//        } finally {
//        	isr.close();
//            reader.close();
//        }
//    }
    
	public static void readSkylightLocationFromXml() {
	    if(DebugLog.DEBUG)Log.d(LOG_TAG, "readSkylightLocationFromXml------------");
		FileInputStream fis = null;
		SkylightLocation location = new SkylightLocation();
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
						location.setXaxis(Integer.parseInt(xaxisStr));
					} else if (XML_KEY_YAXIS.equals(parser.getName())) {
						String yaxisStr = parser.nextText().trim();
						location.setYaxis(Integer.parseInt(yaxisStr));
					} else if (XML_KEY_WIDTH.equals(parser.getName())) {
						String width = parser.nextText().trim();
						location.setWidth(Integer.parseInt(width));
					} else if (XML_KEY_HEIGHT.equals(parser.getName())) {
						String height = parser.nextText().trim();
						location.setHeight(Integer.parseInt(height));
					}
					break;
				case XmlPullParser.END_TAG:
					break;

				}
				eventType = parser.next();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			location.reset();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			Log.d(LOG_TAG, "location lenght: "+location.getHeight());
			if(location.getHeight()>0){
				SkylightHost.sLocation=location;
			}
			updateSkylightHost();
			
		}
	} 
    private static void updateSkylightHost(){
        KeyguardViewHostManager hostManager=KeyguardViewHostManager.getInstance();
        if(hostManager!=null){
            hostManager.updateSKylightLocation();
        }
    }
}
