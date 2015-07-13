package com.amigo.navi.keyguard;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

//import com.gionee.navi.keyguard.everydayphoto.NavilSettings;

import android.util.Log;

public class DebugLog {
	
	public static final boolean DEBUG = true;
	public static final boolean DEBUGMAYBE=false;
	private static final boolean OUTPUT_LOGCAT = true;
//	private static final boolean OUTPUT_FILE = true;
	private static final String TAG = "NaviKeyguard";
	

	public static void d(String tag, String message){
		log(tag+" --> "/*+NavilSettings.getVersionName()*/, message, LOG_LEVEL.DEBUG);
	}
	
    public static void e(String tag, String text, Throwable ex) {
		String logMessage = text + " --> exception --->" + changeCrashInfo2String(ex);
        e(tag, logMessage);
    }
	
	public static void mustLog(String tag, String message){
		e(tag, message);
	}
	
	private enum LOG_LEVEL{
		ERROR,WARN,INFO,DEBUG,VERBOSE
	}
	
    
    public static void e(String tag, String text) {
        log(tag, text, LOG_LEVEL.ERROR);
    }

    public static void i(String tag, String text) {
        log(tag, text, LOG_LEVEL.INFO);
    }

    public static void v(String tag, String text) {
        log(tag, text, LOG_LEVEL.VERBOSE);
    }
    
    public static void w(String tag, String text) {
    	log(tag, text, LOG_LEVEL.WARN);
    }
    

    private static void log(String tag, String msg, LOG_LEVEL level) {
    	String logTag = getLogTag();
		String logMessage = getLogMessage(tag, msg);
		if(OUTPUT_LOGCAT){
			writeLog2Logcat(logTag, logMessage, level);
		}
    }

    private static String getLogTag(){
    	return TAG + " --> " /*+ NavilSettings.sVersionName*/;
    }
    
    private static String getLogMessage(String tag,String message){
    	return tag + " --> " + message;
    }
    private static void writeLog2Logcat(String tag, String msg, LOG_LEVEL level){
    	switch (level) {
		case DEBUG:
			Log.d(tag, msg);
			break;
		case ERROR:
			Log.e(tag, msg);
			break;
		case INFO:
			Log.i(tag, msg);
			break;
		case WARN:
			Log.w(tag, msg);
			break;
		default:
			break;
		}
    }
	
    /**
     * 将Exception的信息转换成String数据
     */
    private static String changeCrashInfo2String(Throwable ex) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        return writer.toString();
    }
}
