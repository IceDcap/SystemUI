package com.amigo.navi.keyguard.modules;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract;
//import android.provider.Telephony.MmsSms;
import android.text.TextUtils;

import com.android.keyguard.KeyguardUpdateMonitor;
import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.util.CursorUtils;
import com.google.android.collect.Lists;


public class KeyguardMissedInfoModule extends KeyguardModuleBase {
	private static final String LOG_TAG = "MissedInfoModule";
    
	// Gionee <jiangxiao> <2014-04-16> modify for CR01188955 begin
    private static final String QUERY_TOKEN_MESSAGE = "token_message";
    private static final String QUERY_TOKEN_SMS_DETAILS = "token_sms_details";
    private static final String QUERY_TOKEN_CALL = "token_call";
    private static final String QUERY_TOKEN_CALL_DETAILS = "token_call_details";
    
    private static final int DEFAULT_QUERY_DELAY = 1000;
	// Gionee <jiangxiao> <2014-04-16> modify for CR01188955 end
	
	private static KeyguardMissedInfoModule sInstance = null;
	
	private MissMmsObserver mMissMmsObserver;
//	private static final Uri MMS_SMS_CONTENT_URI = MmsSms.CONTENT_URI; // content://mms-sms/
	
	private MissCallObserver mMissCallObserver;
	private static final Uri CALL_LOGS_CONTENT_URI = CallLog.Calls.CONTENT_URI;
	private int mLastMissedCallCount = 0;
	
	private ArrayList<MissedInfoCallback> mCallbacks = Lists.newArrayList();
    
	// Gionee <jiangxiao> <2014-05-20> add for CR01258261 begin
    private boolean mMissedCallCountChanged = false;
	
	// db busy 2
	private static final int MSG_FORCE_QUERY = 1;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if(msg.what == MSG_FORCE_QUERY) {
				handleForceQuery(msg);
			}
		}
	};
	
	private static final long SEND_FORCE_QUERY_CYCLE = 5000;
	
	private long mLastSendForceQueryMessageTime = 0;
	
	private int mQuerySeqMessage = 0;
	private int mQuerySeqCall = 0;
	private int mQuerySeqCallDetails = 0;
	private int mQuerySeqSmsDetails=0;
	
	private void increaseQuerySeq(String token) {
		if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "increaseQuerySeq() token=" + token);
		if(token.equals(QUERY_TOKEN_MESSAGE)) {
			mQuerySeqMessage++;
		} else if(token.equals(QUERY_TOKEN_CALL)) {
			mQuerySeqCall++;
		} else if(token.equals(QUERY_TOKEN_CALL_DETAILS)) {
			mQuerySeqCallDetails++;
		}else if(token.equals(QUERY_TOKEN_SMS_DETAILS)){
            mQuerySeqSmsDetails++;
        }
	}
	
	private int getQuerySeq(String token) {
		int seqNew = 0;
		if(token.equals(QUERY_TOKEN_MESSAGE)) {
			seqNew = mQuerySeqMessage;
		} else if(token.equals(QUERY_TOKEN_CALL)) {
			seqNew = mQuerySeqCall;
		} else if(token.equals(QUERY_TOKEN_CALL_DETAILS)) {
			seqNew = mQuerySeqCallDetails;
		}else if(token.equals(QUERY_TOKEN_SMS_DETAILS)){
            seqNew=mQuerySeqSmsDetails;
        }
		
		return seqNew;
	}
	
	private void sendForceQueryMessage(String token) {
		long currentTime = SystemClock.uptimeMillis();
		if(currentTime - mLastSendForceQueryMessageTime > SEND_FORCE_QUERY_CYCLE) {
			mLastSendForceQueryMessageTime = currentTime;
			
			Message msg = mHandler.obtainMessage(MSG_FORCE_QUERY, token);
			msg.arg1 = getQuerySeq(token);
			mHandler.sendMessageDelayed(msg, SEND_FORCE_QUERY_CYCLE);
			
			if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "sendForceQueryMessage()");
		}
	}
	
	private void handleForceQuery(Message msg) {
		if(msg == null || msg.what != MSG_FORCE_QUERY) {
			return;
		}
		
		String token = (String) msg.obj;
		int seqNew = getQuerySeq(token);
		int seqOld = msg.arg1;
		if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "force query token=" + token
				+ ", seqOld=" + seqOld + ", seqNew=" + seqNew);
		if(seqOld == seqNew) {
			forceQuery(token);
		}
	}
	
	private void forceQuery(String token) {
		if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "forceQuery() token=" + token);
		startQueryTask(QUERY_TOKEN_MESSAGE);
		startQueryTask(QUERY_TOKEN_CALL);
		startQueryTask(QUERY_TOKEN_CALL_DETAILS);
		startQueryTask(QUERY_TOKEN_SMS_DETAILS);
	}
	// Gionee <jiangxiao> <2014-05-20> add for CR01258261 end
	
	public interface MissedInfoCallback {
		void onMissMmsCountChanged(int count);

        void onMissCallCountChanged(int count);
        // Gionee <jingyn> <2014-07-08> modify for CR01315875 begin
        void setMissSmsInfoGone();
        // Gionee <jingyn> <2014-07-08> modify for CR01315875 end
	}
	
	private void closeCursor(Cursor cursor) {
		if(cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		cursor = null;
	}
	
	private class MissedCallQueryThread extends Thread {
		private static final String CONDITION_TYPE_AND_NEW = 
				CallLog.Calls.TYPE + "=" + CallLog.Calls.MISSED_TYPE
				+ " and " + CallLog.Calls.NEW + "=1";
		
		public void run() {
			if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "start to query missed call count");
			int missedCallCount = queryCount(CALL_LOGS_CONTENT_URI, CONDITION_TYPE_AND_NEW);
			mUpdateMonitor.notifyMissedCallCountChanged(missedCallCount);
			// Gionee <jiangxiao> <2014-05-20> modify for CR01258261 begin
			int diff = mLastMissedCallCount - missedCallCount;
			if(diff > 0) {
				// remove missed call details
			}
			mMissedCallCountChanged = (diff != 0);
			if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "mMissedCallCountChanged changed to " + mMissedCallCountChanged);
			// Gionee <jiangxiao> <2014-05-20> modify for CR01258261 end
			mLastMissedCallCount = missedCallCount;
		}
		
		private int queryCount(Uri uri, String condition) {
			int count = 0;
			
			Cursor cursor = null;
			try {
				cursor = mContext.getContentResolver().query(
						uri, null, condition, null, CallLog.Calls.DEFAULT_SORT_ORDER);
				if(cursor != null) {
					count = cursor.getCount();
					if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "missed call count is " + count);
				}
			} catch(SQLiteException e) {
				DebugLog.d(LOG_TAG, "exception happened when query missed call");
				count = 0;
			} catch(Exception e) {
				DebugLog.d(LOG_TAG, "fail to query missed call count");
				count = 0;
			} finally {
				closeCursor(cursor);
			}
			
			return count;
		}
	}
	private int mPreSmsCount=0;
	private class MissedMmsQueryThread extends Thread {
		private final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
        private final Uri MMS_CONTENT_URI = Uri.parse("content://mms");
        private final Uri WAPPUSH_CONTENT_URI = Uri.parse("content://wappush");
        
        // Gionee <jiangxiao> <2014-04-14> add for CR01174556 begin
        private static final String CONDITION_READ = " thread_id  in (select _id from threads ) AND read = 0";
        private static final String CONDITION_READ_OR_SEEN = " thread_id  in (select _id from threads ) AND read = 0 ";
        // Gionee <jiangxiao> <2014-04-14> add for CR01174556 end
        
        // Gionee <jiangxiao> <2014-03-11> modify for CR01111731 begin
		public void run() {
			int count = 0;
			// Gionee <jiangxiao> <2014-04-14> modify for CR01174556 begin
			int smsCount=queryCount(SMS_CONTENT_URI, CONDITION_READ);
            count +=smsCount ;
			count += queryCount(MMS_CONTENT_URI, CONDITION_READ);
			count += queryCount(WAPPUSH_CONTENT_URI, CONDITION_READ_OR_SEEN);
			// Gionee <jiangxiao> <2014-04-14> modify for CR01174556 end
			
			if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "total missed MMS count is " + count);
			mUpdateMonitor.notifyMissedMmsCountChanged(count);
			
			// Gionee <jingyn> <2014-07-08> modify for CR01315875 begin
            if (smsCount>mPreSmsCount) {
                //do nothing
            }else if (smsCount<mPreSmsCount) {
                mUpdateMonitor.notifyMissedSmsInfoGone();
            }
            mPreSmsCount=smsCount;
            // Gionee <jingyn> <2014-07-08> modify for CR01315875 end
		}
		private int queryCount(Uri uri, String condition) {
			int count = 0;
			Cursor cursor = null;
			try {
				cursor = mContext.getContentResolver().query(uri, null, condition, null, null);
				if(cursor != null) {
					count = cursor.getCount();
					if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "missed " + uri.toString() + " count is " + count);
				}
			} catch(SQLiteException e) {
				DebugLog.d(LOG_TAG, "exception happened when query " + uri.toString());
			} catch(Exception e) {
				DebugLog.d(LOG_TAG, "fail to query missed message count");
				count = 0;
			} finally {
				closeCursor(cursor);
			}
			
			return count;
		}
		// Gionee <jiangxiao> <2014-04-14> add for CR01174556 end
	}

	// Gionee <jiangxiao> <2014-04-16> modify for CR01188955 begin
	public void startMissedCallQuery() {
		startQueryTask(QUERY_TOKEN_CALL);
	}
	
	public void startMissedMmsQuery() {
		startQueryTask(QUERY_TOKEN_MESSAGE);
	}
	// Gionee <jingyn> <2014-07-08> modify for CR01315875 begin
    public void startMissedSmsDetailQuery(){
        startQueryTask(QUERY_TOKEN_SMS_DETAILS);
    }
    // Gionee <jingyn> <2014-07-08> modify for CR01315875 end
	
	private void startQueryTask(String type) {
		if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "start query task " + type);
		Thread task = null;
		if(type.equals(QUERY_TOKEN_CALL)) {
			task = new MissedCallQueryThread();
		} else if(type.equals(QUERY_TOKEN_MESSAGE)) {
			task = new MissedMmsQueryThread();
		} else if(type.equals(QUERY_TOKEN_CALL_DETAILS)) {
			task = new MissedCallDetailsThread();
		}else if(type.equals(QUERY_TOKEN_SMS_DETAILS)){
            task=new MissedSmsDetailThread();
        }
		if(task!=null){
			task.start();			
		}
		
		// Gionee <jiangxiao> <2014-05-20> add for CR01258261 end
		// db busy 2
		increaseQuerySeq(type);
		// don't run this thread to reduce loading
//		try {
//			task.join();
//		} catch (InterruptedException e) {
//			DebugLog.d(LOG_TAG, "fail join query task " + type);
//		}
		// Gionee <jiangxiao> <2014-05-20> add for CR01258261 end
	}
	// Gionee <jiangxiao> <2014-04-16> modify for CR01188955 end
	
	public static KeyguardMissedInfoModule getInstance(Context context, KeyguardUpdateMonitor updateMonitor) {
		if(sInstance == null) {
			sInstance = new KeyguardMissedInfoModule(context, updateMonitor);
		}
		
		return sInstance;
	}
    
    public static KeyguardMissedInfoModule getInstance() {
    	if(sInstance == null) {
    		throw new RuntimeException("KeyguardMissedInfoModule should not be null!");
    	}
    	
    	return sInstance;
    }
	
	protected void initModule() {
	}
	
	private KeyguardMissedInfoModule(Context context, KeyguardUpdateMonitor updateMonitor) {
		super(context, updateMonitor);
		
		// Gionee <jiangxiao> <2014-04-16> modify for CR01188955 begin
		ContentResolver cr = context.getContentResolver();
		
		mMissMmsObserver = new MissMmsObserver();
//		cr.registerContentObserver(MMS_SMS_CONTENT_URI, true, mMissMmsObserver);
        
        mMissCallObserver = new MissCallObserver();
		cr.registerContentObserver(CALL_LOGS_CONTENT_URI, true, mMissCallObserver);
		// Gionee <jiangxiao> <2014-04-16> modify for CR01188955 end
	}
	
	public void handleMissedCallCountChanged(int newCount) {
		for(MissedInfoCallback cb : mCallbacks) {
			cb.onMissCallCountChanged(newCount);
		}
	}
	
	public void handleMissedMmsCountChanged(int newCount) {
		for(MissedInfoCallback cb : mCallbacks) {
			cb.onMissMmsCountChanged(newCount);
		}
	}
	// Gionee <jingyn> <2014-07-08> modify for CR01315875 begin
    public void handleMissedSmsInfoCount() {
        for(MissedInfoCallback cb : mCallbacks) {
            cb.setMissSmsInfoGone();
        }
    }
    // Gionee <jingyn> <2014-07-08> modify for CR01315875 end
    
	
	public void registerCallback(MissedInfoCallback callback) {
		if(!mCallbacks.contains(callback)) {
			mCallbacks.add(callback);
		}
	}
	
	public void unregisterCallback(MissedInfoCallback callback) {
		mCallbacks.remove(callback);
	}
	
	// Gionee <jiangxiao> <2014-04-16> modify for CR01188955 begin
	private void postQueryDelayed(Runnable query, Object token, long delayMillis) {
		mHandler.removeCallbacks(query, token);
		if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "post query for " + token);
		// change elapsedRealtime() to uptimeMillis()
		mHandler.postAtTime(query, token, SystemClock.uptimeMillis() + delayMillis);
		
		// Gionee <jiangxiao> <2014-05-20> add for CR01258261 begin
		// db busy 2
		sendForceQueryMessage((String) token);
		// Gionee <jiangxiao> <2014-05-20> add for CR01258261 end
	}
	// Gionee <jiangxiao> <2014-04-16> modify for CR01188955 end
	
	private class MissMmsObserver extends ContentObserver {
		private Runnable mDoQuery = new Runnable() {
            public void run() {
                startMissedMmsQuery();
            }
        };
		
        public MissMmsObserver() {
            super(mHandler);
        }
        
        // Gionee <jiangxiao> <2014-04-14> modify for CR01188955 begin
        @Override
        public void onChange(boolean selfChange, Uri uri) {
        	if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "mms onChange()  uri: "+uri);
        	postQueryDelayed(mDoQuery, QUERY_TOKEN_MESSAGE, DEFAULT_QUERY_DELAY);
//            postMissedSmsDetailsIfNeed();
        }

        @Override
        public void onChange(boolean selfChange) {
        	onChange(selfChange, null);
        }
        // Gionee <jiangxiao> <2014-04-14> modify for CR01188955 end

    }
	// Gionee <lilg><2013-10-15> add for CR00924013 end
	
    // Gionee <lilg><2013-10-24> add for CR00933519 begin
	// Gionee <jiangxiao> <2014-05-20> modify for CR01258261 end
	// move this runnable out of postMissedCallDetailsIfNeed()
	Runnable doMissedCallDetailsQuery = new Runnable() {
		public void run() {
			startQueryTask(QUERY_TOKEN_CALL_DETAILS);
		}
	};
	// Gionee <jiangxiao> <2014-05-20> modify for CR01258261 end
	
    // Gionee <jingyn> <2014-07-08> modify for CR01315875 begin
    Runnable doMissedSmsDetailQuery=new Runnable() {
        @Override
        public void run() {
            startQueryTask(QUERY_TOKEN_SMS_DETAILS);
        }
    };
    // Gionee <jingyn> <2014-07-08> modify for CR01315875 end
    
    private void postMissedCallDetailsIfNeed() {
    	// Gionee <jiangxiao> <2014-04-16> modify for CR01188955 begin
		postQueryDelayed(doMissedCallDetailsQuery, 
				QUERY_TOKEN_CALL_DETAILS, DEFAULT_QUERY_DELAY);
		// Gionee <jiangxiao> <2014-04-16> modify for CR01188955 end
	}
    // Gionee <jingyn> <2014-07-08> modify for CR01315875 begin
    private void postMissedSmsDetailsIfNeed(){
        postQueryDelayed(doMissedSmsDetailQuery, QUERY_TOKEN_SMS_DETAILS, DEFAULT_QUERY_DELAY);
    }
    // Gionee <jingyn> <2014-07-08> modify for CR01315875 end
    
    // Gionee <jiangxiao> <2014-04-16> modify for CR01188955 begin
    private class MissedCallDetailsThread extends Thread {
    	public void run() {
    		// Gionee <jiangxiao> <2014-05-14> modify for CR01237419 begin
    		String missedCall = findMissedCallNumber();
    		// Gionee <jiangxiao> <2014-05-20> modify for CR01258261 begin
    		if(missedCall != null) {
//    			postCallMessage(missedCall);
    			mMissedCallCountChanged = false;
    		}
    		// Gionee <jiangxiao> <2014-05-20> modify for CR01258261 end
    		// Gionee <jiangxiao> <2014-05-14> modify for CR01237419 end
    	}
    }
    
    // Gionee <jingyn> <2014-07-08> add for CR01317558 begin
    private static class MissedSmsDetailThread extends Thread{
        @Override
        public void run() {
//            MissedSmsInfo smsInfo=findMissedSmsInfo();
        }
    }
    // Gionee <jingyn> <2014-07-08> add for CR01317558 end
	// Gionee <jiangxiao> <2014-04-16> modify for CR01188955 end

    // Gionee <jiangxiao> <2014-05-14> add for CR01237419 begin
    private String findMissedCallNumber() {
    	String missedCall = null;
    	Cursor cursor = null;
    	try {
    		cursor = mContext.getContentResolver().query(
    				Uri.parse("content://call_log/calls"),
		    		null, null, null, CallLog.Calls.DATE + " DESC");
    		if(cursor != null) {
    			while (cursor.moveToNext()) {
    				 String callType = CursorUtils.getStringByColumnIndex(cursor, CallLog.Calls.TYPE);
    				 String isCallNew = CursorUtils.getStringByColumnIndex(cursor, CallLog.Calls.NEW);
    				 String callNumber = CursorUtils.getStringByColumnIndex(cursor, CallLog.Calls.NUMBER);
    				 String cachedName = CursorUtils.getStringByColumnIndex(cursor, CallLog.Calls.CACHED_NAME);
    					 if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "call queried, callType: " + callType + 
	 			    			", isCallNew: " + isCallNew + "callNumber: " + callNumber +
	 			    			", cachedName: " + cachedName);
    				 
    				 if(isNewMissedCall(callType, isCallNew)) {
    					 missedCall = getMissedCallNameOrNumber(cachedName, callNumber);
    					 break;
    				 }
    			}
    		}
    	} catch(SQLException e) {
    		DebugLog.d(LOG_TAG, "fail to query missed call info");
    	} finally {
    		CursorUtils.closeCursor(cursor);
    	}
    	
    	if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "find missed call " + missedCall);
    	return missedCall;
    }
    
    // Gionee <jingyn> <2014-07-08> modify for CR01315875 begin
//    static class MissedSmsInfo{
//        private String personId;
//        private String phoneNumber;
//        private String smsContent;
//    } 
    // Gionee <jingyn> <2014-07-08> modify for CR01315875 end

 // Gionee <jingyn> <2014-07-08> add for CR01317558 begin
//    private MissedSmsInfo findMissedSmsInfo() {
//        Log.d(LOG_TAG, "findMissedSmsInfo");
//        MissedSmsInfo smsInfo = null;
//        Cursor cursor = null;
//        try {
//            Uri uri = Telephony.Sms.CONTENT_URI;
//            String selection = "read != 1";
//            String srotOrder = Conversations.DEFAULT_SORT_ORDER + " LIMIT 1";
//            cursor = mContext.getContentResolver().query(uri, null, selection, null, srotOrder);
//            if (cursor != null) {
//                while (cursor.moveToNext()) {
//                    int threadId = CursorUtils.getIntegerByColumnIndex(cursor, Telephony.Sms.THREAD_ID);
//                    String address = CursorUtils.getStringByColumnIndex(cursor, Telephony.Sms.ADDRESS);
//                    String smsContent = CursorUtils.getStringByColumnIndex(cursor, Telephony.Sms.BODY);
//                    DebugLog.d(LOG_TAG, "sms queried111  " + "  smsContent: " + smsContent + " address:"
//                            + address + " thread_Id:" + threadId);
//                    if (address != null) {
//                        smsInfo = new MissedSmsInfo();
//                        String contact = findContactByNumber(address);
//                        if (!TextUtils.isEmpty(contact)) {
//                            address = contact;
//                        }
//                        if (checkSmsIsEncrypt(threadId, true)) {
//                            smsContent = "";
//                        }
//                        if (smsContent == null) {
//                            smsContent = "";
//                        }
//                        DebugLog.d(LOG_TAG, "sms queried222  " + "  smsContent: " + smsContent + " address:"
//                                + address + " thread_Id:" + threadId);
//                        smsInfo.phoneNumber = address;
//                        smsInfo.smsContent = smsContent;
//                    }
//                }
//            }
//        } catch (SQLException e) {
//            DebugLog.d(LOG_TAG, "fail to query missed call info");
//            e.printStackTrace();
//        } finally {
//            CursorUtils.closeCursor(cursor);
//        }
//        return smsInfo;
//    }
    // Gionee <jingyn> <2014-07-08> add for CR01317558 end
    
    // Gionee <jingyn> <2014-07-08> add for CR01317558 begin
    /**
     *
     * @param threadId
     * @param versionNew: false--> backward compatible old encrypt provider uri
     * @return
     */
    private boolean checkSmsIsEncrypt(int threadId,boolean versionNew) {
        Cursor cursor = null;
        boolean isEncrypt=false;
        Uri uri=null;
        try {
            if (versionNew) {
                uri = Uri.parse("content://mms-feature/encryp");
                String selection = "encryp_threadid = " + threadId;
                cursor = mContext.getContentResolver().query(uri, null, selection, null, null);
                    if (cursor!=null) {
                        if (cursor.getCount()>0) {
                            isEncrypt= true;
                        }
                    }else{
                        isEncrypt=checkSmsIsEncrypt(threadId, false);
                    }
            }else{
//                uri =Threads.CONTENT_URI.buildUpon().appendQueryParameter("simple", "true").build();
//                String selection=Threads._ID+"="+threadId;
//                cursor=mContext.getContentResolver().query(uri, new String[]{"encryption"}, selection, null, null);
//                if (cursor!=null) {
//                    while (cursor.moveToNext()) {
//                        int encyption=CursorUtils.getIntegerByColumnIndex(cursor, "encryption");
//                        DebugLog.d(LOG_TAG, "encryption:"+encyption);
//                        if (1==encyption) {
//                            isEncrypt=true;
//                        }else{
//                            
//                        }
//                    }
//                }
                
            }
            if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "versionNew:"+versionNew+"  cursor count:  "+(cursor==null?"null":cursor.getCount()));
           
            
        } catch (Exception e) {
            e.printStackTrace();
            if (!isEncrypt) {
                isEncrypt=checkSmsIsEncrypt(threadId, false);
            }
        } finally {
            CursorUtils.closeCursor(cursor);
        }
        return isEncrypt;
    }
    // Gionee <jingyn> <2014-07-08> add for CR01317558 end

    // Gionee <jingyn> <2014-07-15> add for CR01321599 begin
    private String findContactByNumber(String number) {
        String name="";
        Cursor cursor=null;
        if (number.startsWith("+86")) {
            number=number.substring(3);
        }
        try {
            Uri uri=ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            
            String selection = ContactsContract.CommonDataKinds.Phone.NUMBER+" like '%"+number+"'";
            cursor = mContext.getContentResolver().query(
                    uri,null,selection, null, null);
            if(cursor != null) {
                while (cursor.moveToNext()) {
                    name=CursorUtils.getStringByColumnIndex(cursor, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                }
            }
        } catch(SQLException e) {
        	DebugLog.d(LOG_TAG, "fail to query missed call info");
            e.printStackTrace();
        } finally {
            CursorUtils.closeCursor(cursor);
        }
        return name;
        
    }
    // Gionee <jingyn> <2014-07-15> add for CR01321599 end
    private boolean isNewMissedCall(String type, String isNew) {
		boolean isMissed = false;
		boolean isNewCall = false;
		try {
			if(type != null) {
				isMissed = (Integer.parseInt(type) == Calls.MISSED_TYPE);
			}
			if(isNew != null) {
				isNewCall = (Integer.parseInt(isNew) > 0);
			}
		} catch(NumberFormatException e) {
			DebugLog.d(LOG_TAG, "fail to parseInt(), type=" + type
					+ ", isNew=" + isNew);
		}
		
		return isMissed && isNewCall;
	}
	
	private String getMissedCallNameOrNumber(String cachedName, String callNumber) {
		String result = null;
		if(!TextUtils.isEmpty(cachedName)) {
			result = cachedName;
		} else if(callNumber.length() > 5) {
			result = callNumber;
		}
		
		return result;
	}
	
//	private void postCallMessage(String detailInfo) {
//		DebugLog.d(LOG_TAG, "postCallMessage()");
//		Intent intent = new Intent(
//				KeyguardInfoZone.ACTION_NEW_MISSED_INFO);
//		intent.putExtra(KeyguardInfoZone.EXTRA_PKG, 
//				KeyguardInfoZone.MISSED_INFO_CALL);
//		intent.putExtra(KeyguardInfoZone.EXTRA_CLS, 
//				"com.gionee.navi.keyguard.widget.KeyguardMissedCallWidget");
//		intent.putExtra(KeyguardInfoZone.EXTRA_DETAIL, detailInfo);
//		
//		mContext.sendBroadcast(intent);
//	}
	// Gionee <jiangxiao> <2014-05-14> add for CR01237419 end
	
	// Gionee <jingyn> <2014-07-08> add for CR01317558 begin
//    private void postSmsMessage(MissedSmsInfo info){
//        DebugLog.d(LOG_TAG, "postSmsMessage()");
//        Intent intent = new Intent(
//                KeyguardInfoZone.ACTION_NEW_MISSED_INFO);
//        intent.putExtra(KeyguardInfoZone.EXTRA_PKG, 
//                KeyguardInfoZone.MISSED_INFO_SMS);
//        intent.putExtra(KeyguardInfoZone.EXTRA_CLS, 
//                "");
//        intent.putExtra(KeyguardInfoZone.EXTRA_DETAIL, info.phoneNumber);
//        intent.putExtra(KeyguardInfoZone.EXTRA_CONTENT, info.smsContent);
//        
//        mContext.sendBroadcast(intent);
//    }
    // Gionee <jingyn> <2014-07-08> add for CR01317558 end
    
	
	// Gionee <jiangxiao> <2014-04-16> modify for CR01188955 begin
    private class MissCallObserver extends ContentObserver {
		private Runnable mDoQuery = new Runnable() {
            public void run() {
            	startMissedCallQuery();
            }
        };
		
        public MissCallObserver() {
            super(mHandler);
        }

        @Override
        public void onChange(boolean selfChange) {
        	onChange(selfChange, null);
        }
        
        @Override
        public void onChange(boolean selfChange, Uri uri) {
        	postQueryDelayed(mDoQuery, QUERY_TOKEN_CALL, DEFAULT_QUERY_DELAY);
        	postMissedCallDetailsIfNeed();
        }
    }
	// Gionee <jiangxiao> <2014-04-16> modify for CR01188955 end
    
}
