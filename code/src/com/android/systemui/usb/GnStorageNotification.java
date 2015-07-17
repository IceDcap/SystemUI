/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.usb;

import java.io.File;
import java.lang.reflect.Method;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.UserHandle;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Slog;
import android.widget.RemoteViews;

import com.android.systemui.SystemUI;

import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbManager;
import android.os.SystemProperties;

import com.android.systemui.R;

public class GnStorageNotification extends SystemUI {
    private static final String TAG = "GnStorageNotification";
    /**
     * The notification that is shown when a USB mass storage host
     * is connected.
     * <p>
     * This is lazily created, so use {@link #setUsbStorageNotification()}.
     */
    private Notification mUsbStorageNotification;
    private Notification mMediaStorageNotification;
	private UsbManager mUsbManager;
	private StorageManager mStorageManager;
	private boolean flag = true;
	
	//otg notification
    private Handler mAsyncEventHandler;
    private static final String OTG_PATH = "otg";
    private static final String OTG_VOLUME_PATH = "/storage/usbotg";
	private static final String FILEMANAGER_PACKAGE_NAME = "com.gionee.filemanager";
	private static final String FILEMANAGER_CLASS_NAME = "com.gionee.filemanager.FileExplorerTabActivity";
	private static final String ACTION_REMOVE_OTG = "PNW.stopSaver";
	
	private static final int OTG_PREPARE_NOTIFICATION_ID = 1032 ;
	private static final int OTG_MOUNTED_NOTIFICATION_ID = 1033 ;
	private static final int OTG_UNMOUNTED_NOTIFICATION_ID = 1034 ;
	private static final int NOTIFICATION_KEEP_TIME = 3000; 

    /**
     * A broadcast receiver is used to monitor usb state changes.
     * Why  StorageNotificationEventListener cannot work correctlly?
     * */
    private BroadcastReceiver mUsbStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
            if (action.equals(UsbManager.ACTION_USB_STATE)) {
                boolean mUsbDeviceConnect = intent.getBooleanExtra(UsbManager.USB_CONNECTED, false);
                Log.v(TAG, "UsbAccessoryMode " + mUsbDeviceConnect);
                updateUsbMassStorageNotification(mUsbDeviceConnect, mUsbManager.getDefaultFunction());
            } else if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            	Log.v(TAG,"receive Intent.ACTION_BOOT_COMPLETED");                
                setToMtpByDefault();
            }
        }
    };

    private class StorageNotificationEventListener extends StorageEventListener {
        public void onStorageStateChanged(final String path, final String oldState, final String newState) {
            mAsyncEventHandler.post(new Runnable() {
                @Override
                public void run() {
                    onStorageStateChangedAsync(path, oldState, newState);
                }
            });
        }
    }

    @Override
    public void start() {
		mUsbManager = (UsbManager)mContext.getSystemService(Context.USB_SERVICE);
		
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_STATE);
        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
        mContext.registerReceiver(mUsbStateReceiver, filter);

        HandlerThread thr = new HandlerThread("SystemUI StorageNotification");
        thr.start();
        StorageNotificationEventListener mStorageEventlistener = new StorageNotificationEventListener();
        mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        mStorageManager.registerListener(mStorageEventlistener);
         
        mAsyncEventHandler = new Handler(thr.getLooper()) {
			public void handleMessage(Message msg) {
				cancelOtgNotification(OTG_UNMOUNTED_NOTIFICATION_ID);
            }
		};
		//CR01476963 fj begin
		/*String ICS_STORAGE_PATH_SD1 = "/mnt/sdcard";
	    String ICS_STORAGE_PATH_SD2 = "/mnt/sdcard2";
	    String STORAGE_PATH_SD1 = "/storage/sdcard0";
	    String STORAGE_PATH_SD2 = "/storage/sdcard1";
		onStorageStateChangedAsync(ICS_STORAGE_PATH_SD1, null, Environment.getStorageState(new File(ICS_STORAGE_PATH_SD1)));
		onStorageStateChangedAsync(ICS_STORAGE_PATH_SD2, null, Environment.getStorageState(new File(ICS_STORAGE_PATH_SD2)));
		onStorageStateChangedAsync(STORAGE_PATH_SD1, null, Environment.getStorageState(new File(STORAGE_PATH_SD1)));
		onStorageStateChangedAsync(STORAGE_PATH_SD2, null, Environment.getStorageState(new File(STORAGE_PATH_SD2)));*/
        StorageVolume[] volumes = mStorageManager.getVolumeList();
		for (int i=0; i<volumes.length; i++) {
            String sharePath = volumes[i].getPath();
            String shareState = mStorageManager.getVolumeState(sharePath);
            if (shareState != null) {
                Log.d(TAG, "onStorageStateChanged - sharePath: " + sharePath + " shareState: " + shareState);
                if (shareState.equals(Environment.MEDIA_UNMOUNTABLE) ||
                    shareState.equals(Environment.MEDIA_NOFS)) {
                	mStorageEventlistener.onStorageStateChanged(sharePath, shareState, shareState);
                }
            }
        }
		//CR01476963 fj end
    }

	private boolean isUsbCDRomSupport() {
		String buildVersion = SystemProperties.get("ro.gn.gnprojectid", null);
		if ("CBL8609".equals(buildVersion) || "CBL8605".equals(buildVersion)) {
			return false;
		}
		return true;
	}
    
	private boolean isFirstBoot() {
		if(isInvalidIMEI(mContext)) {
			return false;
		}

		SharedPreferences preferences = mContext.getSharedPreferences("first_boot", Context.MODE_PRIVATE);
		boolean isFirstBoot = preferences.getBoolean("boot_flag", true);
		if (isFirstBoot) {
			Editor editor = preferences.edit();
			editor.putBoolean("boot_flag", false);
			editor.commit();
		}

		Log.v(TAG,"isFirstBoot = " + isFirstBoot);
		return isFirstBoot;
	}
	
	private boolean  isInvalidIMEI(Context context) {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		boolean isInvalidImei = false;
		try {
			Class<?> clazz = Class.forName("android.telephony.TelephonyManager");
			Method mth = clazz.getMethod("gnIsImeiValid");
			isInvalidImei = !(Boolean) mth.invoke(tm);
		} catch (Exception e) {
			String id = tm.getDeviceId();
			Log.v(TAG, "---imei = " + (id == null ? 0 : id.length()));
			isInvalidImei = id == null || (id.matches("9{14}.")|| id.equals("0"));
		}
		Log.v(TAG, "----isInvalidImei = "+isInvalidImei);
        return isInvalidImei;
	}

	private void setToMtpByDefault() {
		if ( !isFirstBoot()) {
			return;
		}
		UsbManager usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
		StringBuilder builder = new StringBuilder();
		builder.append(UsbManager.USB_FUNCTION_MTP);
		if (isUsbCDRomSupport()) {
			builder.append(",");
			builder.append(UsbManager.USB_FUNCTION_MASS_STORAGE);
		}
		String targetFunction = builder.toString();
		usbManager.setCurrentFunction(targetFunction, true);
	}
    
    /**
     * Update the state of the USB mass storage notification
     */
    void updateUsbMassStorageNotification(boolean isConnect, String targetFunction) {
    	if (!isConnect) {
    		setUsbStorageNotification(0, 0, 0, false, false, null);
    		return;
    	}
    	Intent intent = new Intent();
   		intent.setClass(mContext, com.android.systemui.usb.GnUsbStorageActivity.class);
    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	
    	PendingIntent pi = PendingIntent.getActivity(mContext, 0, intent, 0);

        if (UsbManager.USB_FUNCTION_MTP.equals(targetFunction)) {
        	 setUsbStorageNotification(
                     R.string.gn_mtp_notification_title,
                     R.string.gn_usb_storage_content,
                     R.drawable.gn_stat_sys_data_usb,
                     false, true, pi);
        } else if (UsbManager.USB_FUNCTION_PTP.equals(targetFunction)) {
            setUsbStorageNotification(
                    R.string.gn_ptp_notification_title,
                    R.string.gn_ptp_summary,
                    R.drawable.gn_stat_sys_data_usb,
                    false, true, pi);
        } else  {
            setUsbStorageNotification(
                    R.string.gn_usb_notification_title,
                    R.string.gn_usb_notification_message,
                    R.drawable.gn_stat_sys_data_usb,
                    false, true, pi);
        }
    }

    /**
     * Sets the USB storage notification.
     */
    private synchronized void setUsbStorageNotification(int titleId, int messageId, int icon,
        boolean sound, boolean visible, PendingIntent pi) {

        if (!visible && mUsbStorageNotification == null) {
            return;
        }

        NotificationManager notificationManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) {
            return;
        }

        if (visible) {
            CharSequence title = mContext.getResources().getString(titleId);
            CharSequence message = mContext.getResources().getString(messageId);

            if (mUsbStorageNotification == null) {
                mUsbStorageNotification = new Notification();
                mUsbStorageNotification.icon = icon;
                mUsbStorageNotification.when = 0;
            }

            if (sound) {
                mUsbStorageNotification.defaults |= Notification.DEFAULT_SOUND;
            } else {
                mUsbStorageNotification.defaults &= ~Notification.DEFAULT_SOUND;
            }

            mUsbStorageNotification.flags = Notification.FLAG_ONGOING_EVENT;

            mUsbStorageNotification.tickerText = title;
            if (pi == null) {
                Intent intent = new Intent();
                pi = PendingIntent.getBroadcastAsUser(mContext, 0, intent, 0,
                        UserHandle.CURRENT);
            }
            mUsbStorageNotification.color = mContext.getResources().getColor(
                    com.android.internal.R.color.system_notification_accent_color);
            mUsbStorageNotification.setLatestEventInfo(mContext, title, message, pi);
            mUsbStorageNotification.visibility = Notification.VISIBILITY_PUBLIC;
            mUsbStorageNotification.category = Notification.CATEGORY_SYSTEM;
        }
        
        final int notificationId = mUsbStorageNotification.icon;
        if (visible) {
            notificationManager.notifyAsUser(null, notificationId, mUsbStorageNotification, UserHandle.CURRENT);
        } else {
            notificationManager.cancelAsUser(null, notificationId, UserHandle.CURRENT);
        }
    }

    private void onStorageStateChangedAsync(String path, String oldState, String newState) {
        boolean isOtgDevice = path.contains(OTG_PATH);
        /*//test
        if (flag) {
        	newState = Environment.MEDIA_UNMOUNTABLE;
        	flag = !flag;
        } else {
        	newState = Environment.MEDIA_MOUNTED;
        	flag = !flag;
        }
        path = OTG_VOLUME_PATH;
        isOtgDevice = true;*/
        if(isOtgDevice) {
        	if(newState.equals(Environment.MEDIA_CHECKING)) {
                showOtgNotification(
                	R.string.gn_usb_storage_checking_title,
                	R.string.gn_usb_storage_checking_mesage,
                	R.drawable.gn_stat_sys_data_usb,
                    OTG_PREPARE_NOTIFICATION_ID
                );
            }
            else if(newState.equals(Environment.MEDIA_MOUNTED) && oldState.equals(Environment.MEDIA_CHECKING)) {
            	setMediaStorageNotification(0, 0, 0, false, false, null);
                cancelOtgNotification(OTG_PREPARE_NOTIFICATION_ID);
                showOtgOngoingNotification();
            } else if(newState.equals(Environment.MEDIA_UNMOUNTED) && oldState.equals(Environment.MEDIA_MOUNTED)) {
                cancelOtgNotification(OTG_MOUNTED_NOTIFICATION_ID);
                showOtgNotification(
                    R.string.gn_remove_otg,
                    R.string.gn_usb_storage_unmounted,
                    R.drawable.gn_stat_sys_data_usb_remove,
                    OTG_UNMOUNTED_NOTIFICATION_ID
               	);
                mAsyncEventHandler.sendMessageDelayed(new Message(),NOTIFICATION_KEEP_TIME);
            } else if(newState.equals(Environment.MEDIA_REMOVED)){
                cancelOtgNotification(OTG_PREPARE_NOTIFICATION_ID);
                cancelOtgNotification(OTG_MOUNTED_NOTIFICATION_ID);
                mAsyncEventHandler.sendMessageDelayed(new Message(),NOTIFICATION_KEEP_TIME);            
    	    } else if(newState.equals(Environment.MEDIA_BAD_REMOVAL) ){
    	        cancelOtgNotification(OTG_PREPARE_NOTIFICATION_ID);
    	        cancelOtgNotification(OTG_MOUNTED_NOTIFICATION_ID);
    	        cancelOtgNotification(OTG_UNMOUNTED_NOTIFICATION_ID);	        	        
    	    }
        	//CR01492451 fj begin
    	    else if(newState.equals(Environment.MEDIA_NOFS)){
    	    	Intent intent = new Intent();
                intent.setClass(mContext, com.android.internal.app.ExternalMediaFormatActivity.class);
                //the parameter path is received at ExternalMediaFormatActivity.java "String path = getIntent().getStringExtra("PATH");"
                intent.putExtra("PATH",path);
                PendingIntent pi = PendingIntent.getActivity(mContext, 0, intent, 0);

                setMediaStorageNotification(
                        R.string.ext_otg_nofs_notification_title,
                        R.string.ext_otg_nofs_notification_message,
                        R.drawable.gn_stat_sys_data_usb, 
                        true, false, pi);
    	    } else if(newState.equals(Environment.MEDIA_UNMOUNTABLE)){
    	    	Intent intent = new Intent();
                intent.setClass(mContext, com.android.internal.app.ExternalMediaFormatActivity.class);
                intent.putExtra("PATH",path);
                PendingIntent pi = PendingIntent.getActivity(mContext, 0, intent, 0);

                setMediaStorageNotification(
                        R.string.ext_otg_unmountable_notification_title,
                        R.string.ext_otg_unmountable_notification_message,
                        R.drawable.gn_stat_sys_data_usb, 
                        true, false, pi);
    	    }
        	//CR01492451 fj end
        	//CR01476963 fj begin
        } else {
        	if (newState.equals(Environment.MEDIA_MOUNTED)) {
                setMediaStorageNotification(0, 0, 0, false, false, null);
            } else if (newState.equals(Environment.MEDIA_NOFS)) {
                Intent intent = new Intent();
                intent.setClass(mContext, com.android.internal.app.ExternalMediaFormatActivity.class);
                intent.putExtra("PATH",path);
                PendingIntent pi = PendingIntent.getActivity(mContext, 0, intent, 0);

                setMediaStorageNotification(
                        R.string.ext_media_nofs_notification_title,
                        R.string.ext_media_nofs_notification_message,
                        com.android.internal.R.drawable.stat_notify_sdcard_usb, 
                        true, false, pi);
            } else if (newState.equals(Environment.MEDIA_UNMOUNTABLE)) {
                Intent intent = new Intent();
                intent.setClass(mContext, com.android.internal.app.ExternalMediaFormatActivity.class);
                intent.putExtra("PATH",path);
                PendingIntent pi = PendingIntent.getActivity(mContext, 0, intent, 0);

                setMediaStorageNotification(
                        R.string.ext_media_unmountable_notification_title,
                        R.string.ext_media_unmountable_notification_message,
                        com.android.internal.R.drawable.stat_notify_sdcard_usb, 
                        true, false, pi);
            } else {
            	Log.w(TAG, String.format("Ignoring unknown state {%s}", newState));
            }
        }
         //CR01476963 fj end
        
    }
    
    /**
     * Sets the media storage notification.
     */
    private synchronized void setMediaStorageNotification(int titleId, int messageId, int icon, boolean visible,
                                                          boolean dismissable, PendingIntent pi) {

        if (!visible && mMediaStorageNotification == null) {
            return;
        }

        NotificationManager notificationManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) {
            return;
        }

        if (mMediaStorageNotification != null && visible) {
            /*
             * Dismiss the previous notification - we're about to
             * re-use it.
             */
            final int notificationId = mMediaStorageNotification.icon;
            notificationManager.cancel(notificationId);
        }

        if (visible) {
            Resources r = mContext.getResources();
            CharSequence title = r.getText(titleId);
            CharSequence message = r.getText(messageId);

            if (mMediaStorageNotification == null) {
                mMediaStorageNotification = new Notification();
                mMediaStorageNotification.when = 0;
            }

            mMediaStorageNotification.defaults &= ~Notification.DEFAULT_SOUND;

            if (dismissable) {
                mMediaStorageNotification.flags = Notification.FLAG_AUTO_CANCEL;
            } else {
                mMediaStorageNotification.flags = Notification.FLAG_ONGOING_EVENT;
            }

            mMediaStorageNotification.tickerText = title;
            if (pi == null) {
                Intent intent = new Intent();
                pi = PendingIntent.getBroadcastAsUser(mContext, 0, intent, 0,
                        UserHandle.CURRENT);
            }

            mMediaStorageNotification.icon = icon;
            mMediaStorageNotification.color = mContext.getResources().getColor(
                    com.android.internal.R.color.system_notification_accent_color);
            mMediaStorageNotification.setLatestEventInfo(mContext, title, message, pi);
            mMediaStorageNotification.visibility = Notification.VISIBILITY_PUBLIC;
            mMediaStorageNotification.category = Notification.CATEGORY_SYSTEM;
        }

        final int notificationId = mMediaStorageNotification.icon;
        if (visible) {
            notificationManager.notifyAsUser(null, notificationId,
                    mMediaStorageNotification, UserHandle.ALL);
        } else {
            notificationManager.cancelAsUser(null, notificationId, UserHandle.ALL);
        }
    }

    public void showOtgNotification(int titleId, int messageId, int icon, int notificationid) {
        NotificationManager notificationManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE); 
        if (notificationManager == null) {
            return;
        }
        Notification notification = new Notification();
        notification.icon = icon;
        CharSequence title = mContext.getText(titleId);
        CharSequence message = mContext.getText(messageId);
        notification.when = 0;
        notification.defaults &= ~Notification.DEFAULT_SOUND;
        notification.tickerText = title;
        notification.setLatestEventInfo(mContext, title, message, null);
        notificationManager.notifyAsUser(null, notificationid, notification, UserHandle.ALL);
    }

    private void showOtgOngoingNotification() {
        NotificationManager notificationManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE); 
        if (notificationManager == null) {
            return;
        }

        Notification.Builder nb = new Notification.Builder(mContext)
            .setSmallIcon(R.drawable.gn_stat_sys_data_usb)
            .setContentTitle(mContext.getString(R.string.gn_usb_storage_ready_title))
            .setContentText(mContext.getString(R.string.gn_usb_storage_ready_message))
            .setOngoing(true)
            .setShowWhen(false)
            .setCategory(Notification.CATEGORY_SYSTEM)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setColor(mContext.getResources().getColor(com.android.internal.R.color.system_notification_accent_color));
                     
        Intent intent = new Intent();
        intent.setClassName(FILEMANAGER_PACKAGE_NAME,FILEMANAGER_CLASS_NAME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(mContext, 0, intent, 0);
        nb.setContentIntent(pi);

        Intent otgServiceIntent = new Intent(mContext,com.android.systemui.usb.GnOtgService.class);
        otgServiceIntent.putExtra(GnOtgService.VOLUME_PATH,OTG_VOLUME_PATH);
        otgServiceIntent.putExtra(GnOtgService.CMD,GnOtgService.CMD_UNMOUNT);
        PendingIntent otgPindingIntent = PendingIntent.getService(mContext, 0, otgServiceIntent, 0);
        nb.addAction(0, mContext.getString(R.string.gn_remove_otg), otgPindingIntent);
                 
        notificationManager.notifyAsUser(null, OTG_MOUNTED_NOTIFICATION_ID, nb.build(), UserHandle.ALL);
    }

    public void cancelOtgNotification(int notificationid) {
        NotificationManager notificationManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE); 
        if (notificationManager == null) {
            return;
        }
        notificationManager.cancelAsUser(null, notificationid, UserHandle.ALL);
    }
}
