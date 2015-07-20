package com.amigo.navi.keyguard.haokan.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.haokan.Common;
import com.amigo.navi.keyguard.haokan.JsonUtil;
import com.amigo.navi.keyguard.haokan.analysis.Event;
import com.amigo.navi.keyguard.haokan.analysis.MessageModel;
import com.amigo.navi.keyguard.haokan.db.DataConstant.StatisticsColumns;
import com.amigo.navi.keyguard.haokan.entity.EventLogger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StatisticsDB extends BaseDB{

    
    private static final String TAG = "haokan";
    
    private static StatisticsDB sInstance;
    
    public StatisticsDB(Context context) {
        super(context);
    }
    

    public synchronized static StatisticsDB getInstance(Context context){
        if(sInstance == null){
            sInstance = new StatisticsDB(context);
        }
        return sInstance;
    }
    
    /**
     * 
     * @param userLog
     * @return
     */
    public int insertLog(EventLogger event) {

        final SQLiteDatabase db = mWritableDatabase;
        
        StringBuilder sql = new StringBuilder("select _id, count, event, value from statistics where date_time = \"")
                .append(event.getDateTime()).append("\" and img_id = ").append(event.getImgId())
                .append(" and type_id = ").append(event.getTypeId()).append(" and event = ")
                .append(event.getEvent());
        Cursor cursor = db.rawQuery(sql.toString(), null);
        
        ContentValues values = new ContentValues();
        values.put(StatisticsColumns.DATE_TIME, event.getDateTime());
        values.put(StatisticsColumns.IMG_ID, event.getImgId());
        values.put(StatisticsColumns.TYPE_ID, event.getTypeId());
        values.put(StatisticsColumns.EVENT, event.getEvent());
        values.put(StatisticsColumns.VALUE, event.getValue());
        values.put(StatisticsColumns.URL_PV, event.getUrlPv());
        
        int insertId;
        if (cursor.moveToFirst()) {
			if (event.getEvent() == Event.SETTING_AUTO_UPDATE
					|| event.getEvent() == Event.SETTING_ONLY_WIFI) {
				values.put(StatisticsColumns.VALUE, event.getValue());
				insertId = db.update(DataConstant.TABLE_STATISTICS, values,
						"event = ?",
						new String[] { String.valueOf(cursor.getInt(2)) });
			} else {
				values.put(StatisticsColumns.COUNT,
						cursor.getInt(1) + event.getCount());
				insertId = db.update(DataConstant.TABLE_STATISTICS, values,
						"_id = ?",
						new String[] { String.valueOf(cursor.getInt(0)) });
			}
        }else {
            values.put(StatisticsColumns.COUNT, event.getCount());
            insertId = (int) db.insert(DataConstant.TABLE_STATISTICS, null, values);
        }
        if (cursor != null) {
            cursor.close();
        }
 
        return insertId;
    }
    
    public int saveLog(EventLogger event) {
    	int rowId = -1;
    	
    	if(Event.isAccumulatedEvent(event.getEvent())) {
    		rowId = saveAccumulatedLog(event);
    	} else {
    		rowId = saveOneTimeLog(event);
    	}
    	
    	return rowId;
    }
    
    private int saveAccumulatedLog(EventLogger event) {
    	int rowId = -1;
    	
    	String whereClause = "date_time=? AND event=? AND img_id=?";
    	String[] whereArgs = {
    			event.getDateTime(), 
    			Integer.toString(event.getEvent()), 
    			Integer.toString(event.getImgId())
    			};
    	String sql = "SELECT _id, count, event, value FROM statistics WHERE " + whereClause;
        
        final SQLiteDatabase db = mWritableDatabase;
        Cursor cursor = db.rawQuery(sql.toString(), whereArgs);
        
        boolean updateExistedRow = cursor.moveToFirst();
        int newCount = updateExistedRow ? cursor.getInt(1) + event.getCount() : event.getCount();
        int newValue = updateExistedRow ? cursor.getInt(3) + event.getValue() : event.getValue();
        
        ContentValues values = createContentValues(event);
        values.put(StatisticsColumns.COUNT, newCount);
        values.put(StatisticsColumns.VALUE, newValue);
        if(updateExistedRow) {
        	rowId = db.update(DataConstant.TABLE_STATISTICS, values, whereClause, whereArgs);
        } else {
        	rowId = (int) db.insert(DataConstant.TABLE_STATISTICS, null, values);
        }
    	
    	return rowId;
    }
    
    private int saveOneTimeLog(EventLogger event) {
    	int rowId = -1;
    	
    	final SQLiteDatabase db = mWritableDatabase;
    	
    	ContentValues values = createContentValues(event);
        values.put(StatisticsColumns.VALUE, event.getValue());
    	values.put(StatisticsColumns.COUNT, event.getCount());
    	Log.d("DEBUG_STATISTICS", "saveOneTimeLog eventId=" + event.getEvent() + ", time=" + event.getDateTime());
    	rowId = (int) db.insert(DataConstant.TABLE_STATISTICS, null, values);
    	
    	return rowId;
    }
    
    private ContentValues createContentValues(EventLogger event) {
    	ContentValues values = new ContentValues();
        values.put(StatisticsColumns.DATE_TIME, event.getDateTime());
        values.put(StatisticsColumns.IMG_ID, event.getImgId());
        values.put(StatisticsColumns.TYPE_ID, event.getTypeId());
        values.put(StatisticsColumns.EVENT, event.getEvent());
        values.put(StatisticsColumns.VALUE, event.getValue());
        values.put(StatisticsColumns.URL_PV, event.getUrlPv());
        
        return values;
    }
    
    
    public int deleteById(List<Integer> ids) {
        
        final SQLiteDatabase db = mWritableDatabase;
        StringBuilder buider = new StringBuilder();
        for(int i=0; i<ids.size(); i++){
            buider.append("'");
            buider.append(ids.get(i));
            buider.append("'");
            buider.append(",");
        }       
        DebugLog.d(TAG, "buider.toString() = " + buider.toString());
        String list = buider.toString().substring(0, buider.length()-1);
        int deleteCount = db.delete(DataConstant.TABLE_STATISTICS, "_id in ( "+list+" )", null);  
        return deleteCount;
    }
    
    
    
    public ArrayList<MessageModel> getEventMsg() {
        
        final SQLiteDatabase db = mReadableDatabase;
        ArrayList<MessageModel> group = new ArrayList<MessageModel>();
        Cursor cursor = db.rawQuery("select _id, date_time, img_id, type_id, event, count,value,url_pv from statistics", null);
        List<EventLogger> listPv = new ArrayList<EventLogger>();
                
         
        int cursorCount = cursor.getCount();
        
        DebugLog.d(TAG, "cursor.Count = " + cursorCount);
        int num = 0;
        
        HashMap<String, List<EventLogger>> map = new HashMap<String, List<EventLogger>>();
        ArrayList<EventLogger> sLogs = new ArrayList<EventLogger>();
        ArrayList<Integer> ids = new ArrayList<Integer>();
        
        final int[] logYearWeek = new int[2];
        final int[] currentYearWeek = new int[2];
        Common.splitYearWeek(Common.curentTimeYearWeek(), currentYearWeek);
        
        while (cursor != null && cursor.moveToNext()) {
            
            num++;
            
            int id = cursor.getInt(0);
            String dateHour = cursor.getString(1);
            int imgId = cursor.getInt(2);
            int typeId = cursor.getInt(3);
            int event = cursor.getInt(4);
            int count = cursor.getInt(5);
            int value = cursor.getInt(6);
            String urlPv = cursor.getString(7);
            
            if(Event.isWeeklyAccumulatedEvent(event)) {
            	Common.splitYearWeek(dateHour, logYearWeek);
            	if(Common.yearWeekLaterThanCurrent(currentYearWeek, logYearWeek)) {
            		continue;
            	} else {
            		// print logs for weekly events
            		Log.d("DEBUG_STATISTICS_WEEKLY_EVENTS", String.format("eventId=%d, value=%d", event, value));
            	}
            }
            
            ids.add(id);
            
            EventLogger eventLogger = new EventLogger(dateHour, imgId, typeId, event, count, value, urlPv);
            
            
            if (ids.size() < Event.MAX_COUNT) {

                if (event == Event.IMG_SHOW && !TextUtils.isEmpty(urlPv)) {
                    listPv.add(eventLogger);
                }
                
                if (eventLogger.getImgId() == -1 && eventLogger.getTypeId() == -1) {
                    
                    sLogs.add(eventLogger);
                
                }else {
                
                    if (map.containsKey(dateHour)) { 
                        List<EventLogger> list = map.get(dateHour);
                        list.add(eventLogger);
                    }else {
                        List<EventLogger> list = new ArrayList<EventLogger>();
                        list.add(eventLogger);
                        map.put(dateHour, list);
                    }
                    
                }
                
                if (cursorCount == num) { 
                    String jsonData = JsonUtil.logToJsonString(map, sLogs, Common.getUserId(mContext));
                    group.add(new MessageModel(jsonData, ids,listPv));
                }
                
            }else {
                
                String jsonData = JsonUtil.logToJsonString(map, sLogs, Common.getUserId(mContext));
                group.add(new MessageModel(jsonData, ids,listPv));
                
                if (cursorCount > num) {
                    sLogs.clear();
                    map.clear();
                    listPv = new ArrayList<EventLogger>();
                    ids = new ArrayList<Integer>();
                }
            }
            
        }
        
        if (cursor != null) {
            cursor.close();
        }
        
/*        DebugLog.d(TAG, "===============================log==============================================");
        for (int i = 0; i < group.size(); i++) {
            MessageModel messageModel = group.get(i);
            DebugLog.d(TAG, "messageModel.jsonData = " + messageModel.jsonData);
            StringBuffer sb = new StringBuffer();
            
            DebugLog.d(TAG, "messageModel.idList.size() = " + messageModel.ids.size());
            for (int j = 0; j < messageModel.ids.size(); j++) {
                sb.append(messageModel.ids.get(j));
                sb.append(",");
            }
            DebugLog.d(TAG, "messageModel.idList = " + sb.toString());
        }*/
        
        return group;
    }

}
