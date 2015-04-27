package com.amigo.navi.keyguard.util;

import android.database.Cursor;

// Gionee <jiangxiao> <2014-05-14> add for CR01237419 begin
public class CursorUtils {
	public static void closeCursor(Cursor cursor) {
		if(cursor != null) {
			cursor.close();
		}
	}
	
	public static String getStringByColumnIndex(Cursor cursor, String columnName) {
		if(cursor == null) return null;
		
		String content = null;
		
		int index = cursor.getColumnIndex(columnName);
		if(index >= 0) {
			content = cursor.getString(index);
		}
		
		return content;
	}
	
	   // Gionee <jingyn> <2014-07-08> modify for CR01315875 begin
    public static int getIntegerByColumnIndex(Cursor cursor, String columnName) {
        if (cursor == null)
            return -1;

        int content = -1;

        int index = cursor.getColumnIndex(columnName);
        if (index >= 0) {
            content = cursor.getInt(index);
        }
        return content;
    }
    // Gionee <jingyn> <2014-07-08> modify for CR01315875 end
	
}

// Gionee <jiangxiao> <2014-05-14> add for CR01237419 end
