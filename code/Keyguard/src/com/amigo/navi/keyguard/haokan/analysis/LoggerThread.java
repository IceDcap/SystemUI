package com.amigo.navi.keyguard.haokan.analysis;

import android.content.Context;
import android.os.Handler.Callback;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.haokan.db.StatisticsDB;
import com.amigo.navi.keyguard.haokan.entity.EventLogger;
import com.amigo.navi.keyguard.network.connect.JsonHttpConnect;
import com.amigo.navi.keyguard.network.manager.DownLoadJsonManager;
import com.amigo.navi.keyguard.network.theardpool.DownLoadJsonThreadPool;
import com.amigo.navi.keyguard.network.theardpool.DownLoadThreadPool;
import com.amigo.navi.keyguard.network.theardpool.DownLoadWorker;
import com.amigo.navi.keyguard.network.theardpool.Job;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LoggerThread extends HandlerThread implements Callback{

    
    private static LoggerThread loggerThread;

    private Handler handler;
    private Context mContext;
    
    public static final int SAVE_LOG = 1;
    public static final int SEND_LOG = 2;

	private static final String TAG = "haokan";
    
    private LoggerThread() {
        super("STATISTICS Thread");
        start();
        handler = new Handler(this.getLooper(), this);
    }

    public static synchronized LoggerThread getInstance() {
        if (loggerThread == null) {
            loggerThread = new LoggerThread();
        }
        return loggerThread;
    }
    
    
    
    public void onEvent(final Context context, final EventLogger userLog) {
        mContext = context;
        saveLogMsg(userLog);
    }
    
    
    public void onEvent(final Context context, final String dateHour, final int imgId, final int typeId, final int event, final int count) {
        mContext = context;
        saveLogMsg(new EventLogger(dateHour, imgId, typeId, event, count));
    }
    
    
    @Override
    public boolean handleMessage(Message msg) {
        if (!Thread.currentThread().isInterrupted()) {
            
            switch (msg.what) {
                case SAVE_LOG:
                    if (msg.obj != null) {
                        EventLogger userLog = (EventLogger) msg.obj;
                        StatisticsDB.getInstance(mContext).insertLog(userLog);
                    }
                    break;
                case SEND_LOG:
//                    //将数据包装 ArrayList<MessageModel>
//                    ArrayList<MessageModel> list = StatisticsDB.getInstance(mContext).getEventMsg();
//                    uploadAllLog(list);
                	uploadAllLogs();
                    break;
                default:
                    break;
            }
        }
        return false;
    }
    
    /** upload all log to server */
    private boolean uploadLog(final MessageModel messageModel) {

        String result = DownLoadJsonManager.getInstance().uploadLogs(mContext,messageModel);
        
        try {
            List<EventLogger> listPv = messageModel.getListPv();
            int len = listPv.size();
            DebugLog.d(TAG, "listPv size = " + len);
            for (EventLogger eventLogger : listPv) {
                int count = eventLogger.getCount();
                URL url = new URL(eventLogger.getUrlPv());
                for (int i = 0; i < count; i++) {
                    url.openConnection();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "listPv openConnection Exception");
        }
        
        DebugLog.d(TAG,"user log result = " + result);
        if (!result.equals(JsonHttpConnect.CONNECT_ERROR)){
        	StatisticsDB.getInstance(mContext).deleteById(messageModel.ids);
        }
        return false;
        
    }
    
    private boolean uploadAllLog(final ArrayList<MessageModel> list) {
        for (int i = 0; i < list.size(); i++) {
            uploadLog(list.get(i));
        }
        return false;
        
    }
    
	public void uploadAllLogs() {
		Job job = new Job() {
			@Override
			public void runTask() {
				ArrayList<MessageModel> list = StatisticsDB.getInstance(
						mContext).getEventMsg();
				uploadAllLog(list);
			}

			@Override
			public int getProgress() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public void cancelTask() {
				// TODO Auto-generated method stub

			}
		};
		DownLoadWorker worker = new DownLoadWorker(job);
		DownLoadThreadPool threadPool = DownLoadJsonThreadPool.getInstance();
		threadPool.submit(worker);
	}
    
    public void saveLogMsg(EventLogger userLog) {
        Message msg = new Message();
        msg.what = SAVE_LOG;
        msg.obj = userLog;
        handler.sendMessage(msg);
    }
    
    
    public void sendLogMsg() {
 
        Message msg = new Message();
        msg.what = SEND_LOG;
        handler.sendMessage(msg);
    }
    

}
