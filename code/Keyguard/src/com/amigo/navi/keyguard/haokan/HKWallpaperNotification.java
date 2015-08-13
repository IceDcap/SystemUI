package com.amigo.navi.keyguard.haokan;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.RemoteViews;
import android.widget.TextView;
import amigo.app.AmigoAlertDialog;
import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.KeyguardViewHostManager;
import com.amigo.navi.keyguard.KeyguardWallpaperManager;
import com.amigo.navi.keyguard.network.connect.NetWorkUtils;
import com.amigo.navi.keyguard.settings.KeyguardSettings;
import com.amigo.navi.keyguard.util.AmigoKeyguardUtils;
import com.android.keyguard.R;

public class HKWallpaperNotification {
	
	private static final String TAG = HKWallpaperNotification.class.getSimpleName();
	
	private Context mContext;
	
	private static HKWallpaperNotification instance;
	public NotificationManager mNotificationManager;
	public Notification mNotification;
	public RemoteViews mRemoteViews;
	int notifyId = 1010;
	
	public final static String INTENT_BUTTONID_TAG = "button_id";
	public final static String ACTION_ONCLICK = "com.gionee.notifications.intent.action.ButtonClick";
	public final static int BUTTON_DOWNLOAD = 100;
	public final static int BUTTON_CANCEL = 101;
	public final static int SHOW_DIALOG = 102;

	private int mState;
	public final static int DISMISS = 0;
	public final static int UPDATING = 1;
	public final static int PAUSE = 2;
	public final static int SUCCESS = 3;
	public final static int FAILED = 4;
    
    private int mWallpaperTotal = 10;
    private int offex = 0;
	private final int[] POINT_DATE_D_VALUE = { 1, 1, 3, 5, 7, 14, 30 };
    
	public static HKWallpaperNotification getInstance(Context context) {
		if (instance == null) {
			instance = new HKWallpaperNotification(context);
		}
		return instance;
	}

	private HKWallpaperNotification(Context context){
		this.mContext = context;
		
		mNotificationManager = (NotificationManager) mContext.getSystemService(Service.NOTIFICATION_SERVICE);
	}

	public int getmWallpaperTotal() {
		return mWallpaperTotal;
	}

	public void setmWallpaperTotal(int mWallpaperTotal) {
		this.mWallpaperTotal = mWallpaperTotal;
	}

	public int getOffex() {
		return offex;
	}

	public void setOffex(int offex) {
		this.offex = offex;
	}
	
	public int getState() {
		return mState;
	}

	public void setState(int mState) {
		this.mState = mState;
	}

	public void showWallpaperUpdateNotification(){
		KeyguardWallpaperManager keyguardWallpaperManager = KeyguardViewHostManager.getInstance().getKeyguardWallpaperManager();
		
		boolean isShowed = KeyguardSettings.getBooleanSharedConfig(mContext, KeyguardSettings.WALLPAPER_UPDATE_NOTIFICATION_SHOWED, false);
		if (isShowed) {
			DebugLog.d(TAG, "wallpaper update notification : has showed");
			return;
		}
		
		boolean hasTodayWallpaper = !KeyguardSettings.getBooleanSharedConfig(mContext, KeyguardSettings.WALLPAPER_UPDATE_NOTIFICATION_FIRST, false);
		if (hasTodayWallpaper) {
			DebugLog.d(TAG, "wallpaper update notification : has today wallpaper");
			return;
		}
		
		boolean isUpdate = KeyguardSettings.getWallpaperUpadteState(mContext);
		boolean isOnlyWifi = KeyguardSettings.getOnlyWlanState(mContext);
		if (!isUpdate || !isOnlyWifi) {
			DebugLog.d(TAG, "wallpaper update notification : update switch is off");
			return;
		}
		
		boolean isNetWorkAvailable = NetWorkUtils.isNetworkAvailable(mContext);
		boolean isWifi = NetWorkUtils.isWifi(mContext);
		if (!isNetWorkAvailable || isWifi) {
			DebugLog.d(TAG, "wallpaper update notification : net error");
			return;
		}
		
		boolean isUpdating = keyguardWallpaperManager.isDownloading();
		if (isUpdating) {
			DebugLog.d(TAG, "wallpaper update notification : downing");
			return;
		}
		
		createWallpaperNotification();
	}
	
	public void createWallpaperNotification() {
		DebugLog.d(TAG, "wallpaper update notification : create ok");
		clearNotify(notifyId);
		offex = 0;
	    mWallpaperTotal = 10;
	    setState(PAUSE);
		registerReceiver();
		KeyguardSettings.setBooleanSharedConfig(mContext, KeyguardSettings.WALLPAPER_UPDATE_NOTIFICATION_SHOWED, true);

		if (mNotification == null) {
			mNotification = new Notification();
			mNotification.icon = R.drawable.amigologo;
			mNotification.when = System.currentTimeMillis();
			mNotification.flags = Notification.FLAG_NO_CLEAR;
			mNotification.priority = Notification.PRIORITY_MAX;
			mNotification.tickerText = mContext.getText(R.string.wallpaper_update_notification_content);
		}
		mRemoteViews = new RemoteViews(mContext.getPackageName(), R.layout.wallpaper_update_notification);

		mRemoteViews.setTextViewText(R.id.wallpaper_update_notification_title, mContext.getText(R.string.wallpaper_update_notification_title));
		mRemoteViews.setTextViewText(R.id.wallpaper_update_notification_cnt, mContext.getText(R.string.wallpaper_update_notification_content));
		mRemoteViews.setImageViewResource(R.id.wallpaper_update_notification_imge, R.drawable.haokan_notification_settings);
		mRemoteViews.setImageViewResource(R.id.wallpaper_update_notification_download, R.drawable.update_notification_download);
		mRemoteViews.setImageViewResource(R.id.wallpaper_update_notification_cancel, R.drawable.update_notification_cancel);

		//onclick button
		Intent buttonIntent = new Intent(ACTION_ONCLICK);
		buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_DOWNLOAD);
		PendingIntent intent_download = PendingIntent.getBroadcast(mContext, 1, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		mRemoteViews.setOnClickPendingIntent(R.id.wallpaper_update_notification_download, intent_download);

		buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_CANCEL);
		PendingIntent intent_next = PendingIntent.getBroadcast(mContext, 2, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		mRemoteViews.setOnClickPendingIntent(R.id.wallpaper_update_notification_cancel, intent_next);

		mNotification.contentView = mRemoteViews;
		
		mNotificationManager.notify(notifyId, mNotification);
	}
	
	BroadcastReceiver clickEventReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ACTION_ONCLICK)) {
				int buttonId = intent.getIntExtra(INTENT_BUTTONID_TAG, 0);
				switch (buttonId) {
				case BUTTON_DOWNLOAD:
					downloadOrPause();
					break;

				case BUTTON_CANCEL:
					clearNotify(notifyId);
					break;
				case SHOW_DIALOG:
						collapseStatusBar(mContext);
						alertDialog();
					break;
				default:
					break;
				}
			}
		}
	};
	
	public void clearNotify(int notifyId) {
		mNotificationManager.cancel(notifyId);
		
		unRegisterReceiver();
		setState(DISMISS);
		RequestNicePicturesFromInternet.getInstance(mContext).shutDownWorkPool();
	}
	
	public void registerReceiver() {
		unRegisterReceiver();

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ACTION_ONCLICK);
		mContext.registerReceiver(clickEventReceiver, intentFilter);
	}

	public void unRegisterReceiver() {
		try {
			mContext.unregisterReceiver(clickEventReceiver);
			mContext.unregisterReceiver(mBatInfoReceiver);
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "clickEventReceiver unRegister failed becase of receiver didn't register");
		}
	}
	
	void downloadOrPause() {
		if (getState() == UPDATING) {
			setState(PAUSE);
			updateHandler.sendEmptyMessage(PAUSE);
		} else {
			setState(UPDATING);
			updateHandler.sendEmptyMessage(UPDATING);
			try {
				if (NetWorkUtils.isNetworkAvailable(mContext)) {
					RequestNicePicturesFromInternet.getInstance(mContext).registerDataInNotification();
				} else {
					updateHandler.sendEmptyMessage(FAILED);
				}
			} catch (Exception e) {
				Log.d(TAG, "wallpaper update failed");
				updateHandler.sendEmptyMessage(FAILED);
			}
		}
	}
	
	public boolean isUpdating(){
		return getState() == UPDATING;
	}

	//needs permission : <uses-permission android:name="android.permission.EXPAND_STATUS_BAR" />
	public static void collapseStatusBar(Context context) {
		try {
			Object statusBarManager = context.getSystemService("statusbar");
			Method collapse;
			if (Build.VERSION.SDK_INT <= 16) {
				collapse = statusBarManager.getClass().getMethod("collapse");
			} else {
				collapse = statusBarManager.getClass().getMethod("collapsePanels");
			}
			collapse.invoke(statusBarManager);
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}
	
	Bitmap getProgressBitmap(int progress, int max){
		float roundWidth = 4f;
		
		Drawable drawable = mContext.getResources().getDrawable(R.drawable.update_notification_pause);
		Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
		bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		
		int centre = bitmap.getWidth() / 2;
		int radius = (int) (centre - roundWidth / 2);
		paint.setColor(mContext.getResources().getColor(R.color.notification_progress_bg));
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(roundWidth-1.5f);
		paint.setAntiAlias(true);
		canvas.drawCircle(centre, centre, radius, paint);
		
		paint.setStrokeWidth(roundWidth);
		paint.setColor(mContext.getResources().getColor(R.color.notification_progress));
		RectF oval = new RectF(centre - radius, centre - radius, centre + radius, centre + radius);
		paint.setStyle(Paint.Style.STROKE);
		if (max != 0) {
			canvas.drawArc(oval, -90, (float) progress / max * 360, false, paint);
		} else {
			canvas.drawArc(oval, -90, 0, false, paint);
		}
		return bitmap;
	}

	public Handler updateHandler = new Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			
			if (getState() == DISMISS) {
				return;
			}
			
			switch (msg.what) {
			case UPDATING:
				if (isUpdating()) {
					Bitmap bitmap = getProgressBitmap(offex, mWallpaperTotal);
					mRemoteViews.setTextViewText(R.id.wallpaper_update_notification_title, mContext.getText(R.string.wallpaper_update_notification_title));
					mRemoteViews.setTextViewText(R.id.wallpaper_update_notification_cnt, mContext.getText(R.string.wallpaper_update_notification_content_update));
					mRemoteViews.setImageViewBitmap(R.id.wallpaper_update_notification_download, bitmap);
					mNotification.contentView = mRemoteViews;
					mNotificationManager.notify(notifyId, mNotification);
				}
				break;
				
			case PAUSE:
				mRemoteViews.setTextViewText(R.id.wallpaper_update_notification_title, mContext.getText(R.string.wallpaper_update_notification_title));
				mRemoteViews.setTextViewText(R.id.wallpaper_update_notification_cnt, mContext.getText(R.string.wallpaper_update_notification_content_pause));
				mRemoteViews.setImageViewResource(R.id.wallpaper_update_notification_download, R.drawable.update_notification_download);
				mNotification.contentView = mRemoteViews;
				mNotificationManager.notify(notifyId, mNotification);
				break;
				
			case SUCCESS:
//				mRemoteViews.setTextViewText(R.id.wallpaper_update_notification_title, mContext.getText(R.string.wallpaper_update_notification_title));
//				mRemoteViews.setTextViewText(R.id.wallpaper_update_notification_cnt, mContext.getText(R.string.wallpaper_update_notification_content_success));
//				mRemoteViews.setImageViewResource(R.id.wallpaper_update_notification_download, -1);
//				mRemoteViews.setOnClickPendingIntent(R.id.wallpaper_update_notification_download, null);
//				mBuilder.setContent(mRemoteViews);
//				mNotificationManager.notify(notifyId, mBuilder.build());
				setState(SUCCESS);
				clearNotify(notifyId);
				break;
				
			case FAILED:
				mRemoteViews.setTextViewText(R.id.wallpaper_update_notification_title, mContext.getText(R.string.wallpaper_update_notification_title));
				mRemoteViews.setTextViewText(R.id.wallpaper_update_notification_cnt, mContext.getText(R.string.wallpaper_update_notification_content_fail));
				mRemoteViews.setImageViewResource(R.id.wallpaper_update_notification_download, R.drawable.update_notification_download);
				mNotification.contentView = mRemoteViews;
				mNotificationManager.notify(notifyId, mNotification);
				setState(FAILED);
				break;

			default:
				break;
			}
		};
	};
	
	   public void showUpdateNotificationWithWlan() {
	        //检查是否符合次数条件
	        int number = KeyguardSettings.getNumberPointUpdateWallpaper(mContext);
	        if (number > 7){
	            DebugLog.d(TAG, "hand update notification :number > 7");
	            return;
	        }
	        //壁纸更新开关是否打开
	        boolean isUpdateOpen = KeyguardSettings
	                .getWallpaperUpadteState(mContext);
	        if (isUpdateOpen){
	            DebugLog.d(TAG, "hand update notification :update switch on" );
	            return;
	        }
	        //是否有无线网络
	        boolean isWifi = NetWorkUtils.isWifi(mContext);
	        if (!isWifi){
	            DebugLog.d(TAG, "hand update notification :don't hava wifi");
	            return;
	        }
	        
	        //是否符合日期条件
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	        Date oldDate;
	        Date todayDate = new Date();
	        String oldDateStr = KeyguardSettings
	                .getTimePointUpdateWallpaper(mContext);
	        DebugLog.d(TAG, "hand update notification Date"+oldDateStr);
	        String todayDateStr = sdf.format(todayDate);
	        //避免点击更新后，同一天再次弹出通知
	        if(todayDateStr.equals(oldDateStr)){
	            return;
	        }
	        if (number > 0) {
	            try {
	                todayDate = sdf.parse(todayDateStr);
	                oldDate = sdf.parse(oldDateStr);
	                int d_value = (int) ((todayDate.getTime() - oldDate
	                        .getTime()) / (24 * 3600 * 1000));
	                DebugLog.d(TAG, "hand update notification :date inconformity condition"+d_value+"number:"+number);
	                if (d_value < POINT_DATE_D_VALUE[number - 1]) {
	                    DebugLog.d(TAG, "hand update notification :date inconformity condition"+d_value+"number:"+number);
	                    return;
	                }
	            } catch (ParseException e) {
	                DebugLog.d(TAG, "hand update notification Date Error");
	                e.printStackTrace();
	            }
	        }
	        //是否有最新壁纸
	        boolean hasTodayWallpaper = !KeyguardSettings.getBooleanSharedConfig(mContext, KeyguardSettings.WALLPAPER_UPDATE_NOTIFICATION_FIRST, false);
	        if (hasTodayWallpaper) {
	            DebugLog.d(TAG, "hand update notification : has today wallpaper");
	            return;
	        }
	        //记录时间和次数，弹出通知
	        KeyguardSettings.saveNumberPointUpdateWallpaper(mContext, ++number);
	        KeyguardSettings.saveTimePointUpdateWallpaper(mContext, todayDateStr);
	        createNotification();
	    }


	   private void createNotification() {
	        DebugLog.d(TAG, "hand update notification start");
	        //清除以前的通知
	        clearNotify(notifyId);
	        //注册广播
	        registerReceiver();
	        if (mNotification == null) {
	            mNotification = new Notification();
	            mNotification.icon = R.drawable.amigologo;
	            mNotification.when = System.currentTimeMillis();
	            mNotification.flags = Notification.FLAG_AUTO_CANCEL;
	            mNotification.priority = Notification.PRIORITY_MAX;
	        }
	        mRemoteViews = new RemoteViews(mContext.getPackageName(),
	                R.layout.wallpaper_update_notification);
	        mRemoteViews.setViewVisibility(R.id.wallpaper_update_notification_download, View.GONE);
	        mRemoteViews.setViewVisibility(R.id.wallpaper_update_notification_cancel, View.GONE);
	        mRemoteViews.setTextViewText(R.id.wallpaper_update_notification_title,
	                mContext.getText(R.string.preference_wallpaper_update_title));
	        mRemoteViews
	                .setTextViewText(
	                        R.id.wallpaper_update_notification_cnt,
	                        mContext.getText(R.string.haokan_notification_settings_content));
	        mRemoteViews.setImageViewResource(
	                R.id.wallpaper_update_notification_imge,
	                R.drawable.haokan_notification_settings);
	        mRemoteViews.setImageViewResource(
	                R.id.wallpaper_update_notification_download,
	                R.drawable.update_notification_download);
	        mRemoteViews.setImageViewResource(
	                R.id.wallpaper_update_notification_cancel,
	                R.drawable.update_notification_cancel);
	        Intent intent = new Intent(ACTION_ONCLICK);
	        //点击事件的添加
	        intent.putExtra(INTENT_BUTTONID_TAG, SHOW_DIALOG);
	        PendingIntent intent_alertDialog = PendingIntent.getBroadcast(mContext,
	                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	        mRemoteViews.setOnClickPendingIntent(R.id.update_notification_layout, intent_alertDialog);

	        mNotification.contentView = mRemoteViews;
	        mNotificationManager.notify(notifyId,
	                mNotification);
	    }

	private AmigoAlertDialog mDialog;

	public void alertDialog() {
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);  
		mContext.registerReceiver(mBatInfoReceiver, filter);  
		
		if (mDialog != null && mDialog.isShowing()) {
			return;
		}
		
		View dialogView = LayoutInflater.from(mContext).inflate(
				R.layout.net_dialog, null);
		final CheckBox cbOpenUpdate = (CheckBox) dialogView
				.findViewById(R.id.no_point);
		cbOpenUpdate.setText(R.string.checkBox_text);
		TextView dialogInfoTitle = (TextView) dialogView.findViewById(R.id.dialog_information_title);
		dialogInfoTitle.setHeight(0);
	    TextView dialogInfoContent = (TextView) dialogView
				.findViewById(R.id.dialog_Allinformation_content);
		dialogInfoContent.setText(R.string.haokan_notification_settings_content);
		
		mDialog = new AmigoAlertDialog.Builder(mContext,
				AmigoAlertDialog.THEME_AMIGO_LIGHT)
				.setTitle(R.string.preference_wallpaper_update_title)
				.setView(dialogView)
				.setPositiveButton(R.string.update_wallpaper,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								afterClickUpdate(cbOpenUpdate.isChecked());
								dialog.dismiss();
							}
						})
				.setNegativeButton(R.string.cancel_update,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						}).create();
		WindowManager.LayoutParams params = mDialog.getWindow()
		        .getAttributes();
		params.type = WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG;
		boolean isKeyguardShowing=KeyguardViewHostManager.getInstance().isShowingAndNotOccluded();
        if (isKeyguardShowing) {
            params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
            params.y = -AmigoKeyguardUtils.getNavBarHeight(mContext);
        }
		mDialog.getWindow().setAttributes(params);
		mDialog.show();
		mDialog.setCancelable(false);
		mDialog.setCanceledOnTouchOutside(false);
	}
	/*
     * 点击dialog更新按钮后的处理
     */
    private void afterClickUpdate(Boolean isCheck) {
        //存入自动更新状态
        if(isCheck){
            KeyguardSettings.setWallpaperUpadteState(mContext, true);
            KeyguardSettings.setConnectState(mContext, true);
        }
        
        setState(PAUSE);
        downloadOrPause();
        //设置两个按钮可见
        mRemoteViews.setViewVisibility(
                R.id.wallpaper_update_notification_download, View.VISIBLE);
        mRemoteViews.setViewVisibility(
                R.id.wallpaper_update_notification_cancel, View.VISIBLE);
         //整个布局的点击事件置为空
        mRemoteViews.setOnClickPendingIntent(R.id.update_notification_layout,
                null);
        //添加两个按钮的点击事件
        Intent intent = new Intent(ACTION_ONCLICK);
        intent.putExtra(INTENT_BUTTONID_TAG, BUTTON_DOWNLOAD);
        PendingIntent intent_download = PendingIntent.getBroadcast(mContext, 1,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(
                R.id.wallpaper_update_notification_download, intent_download);

        intent.putExtra(INTENT_BUTTONID_TAG, BUTTON_CANCEL);
        PendingIntent intent_next = PendingIntent.getBroadcast(mContext, 2,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(
                R.id.wallpaper_update_notification_cancel, intent_next);
        //次数记为0
        KeyguardSettings.saveNumberPointUpdateWallpaper(mContext, 0);
    }

	private final BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {  
	     @Override  
	     public void onReceive(final Context context, final Intent intent) {  
	         final String action = intent.getAction();  
	         if (Intent.ACTION_SCREEN_OFF.equals(action)) {  
	        	 if(mDialog != null){
	        		 mDialog.dismiss();
	        	 }
	         }  
	     }  
	 };  
}
