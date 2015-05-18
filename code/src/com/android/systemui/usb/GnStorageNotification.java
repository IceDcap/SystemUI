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
import android.util.Log;
import android.util.Slog;
import android.widget.RemoteViews;

import com.android.systemui.SystemUI;

import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbManager;

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
	private UsbManager mUsbManager;
	
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
        if ( isFirstBoot()) {
        	setToMtpByDefault();
        }

		mUsbManager = (UsbManager)mContext.getSystemService(Context.USB_SERVICE);
		
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_STATE);
        mContext.registerReceiver(mUsbStateReceiver, filter);

        HandlerThread thr = new HandlerThread("SystemUI StorageNotification");
        thr.start();
        StorageNotificationEventListener mStorageEventlistener = new StorageNotificationEventListener();
        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        mStorageManager.registerListener(mStorageEventlistener);
         
        mAsyncEventHandler = new Handler(thr.getLooper()) {
			public void handleMessage(Message msg) {
				cancelOtgNotification(OTG_UNMOUNTED_NOTIFICATION_ID);
            }
		};
    }

	private boolean isFirstBoot() {
		SharedPreferences preferences = mContext.getSharedPreferences("first_boot", Context.MODE_PRIVATE);
		boolean isFirstBoot = preferences.getBoolean("boot_flag", true);
		Log.v(TAG, "isFirstBoot = " + isFirstBoot);
		if (isFirstBoot) {
			Editor editor = preferences.edit();
			editor.putBoolean("boot_flag", false);
			editor.commit();
		}
		return isFirstBoot;
	}

	private void setToMtpByDefault() {
		UsbManager usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
		StringBuilder builder = new StringBuilder();
		builder.append(UsbManager.USB_FUNCTION_MTP);
		builder.append(",");
		builder.append(UsbManager.USB_FUNCTION_MASS_STORAGE);
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

    /**
     * Sets the OTG storage notification.
     */
    private void onStorageStateChangedAsync(String path, String oldState, String newState) {
        boolean isOtgDevice = path.contains(OTG_PATH);
        if(!isOtgDevice) {
       	    return;
        }
         
        if(newState.equals(Environment.MEDIA_CHECKING)) {
            showOtgNotification(
            	R.string.gn_usb_storage_checking_title,
            	R.string.gn_usb_storage_checking_mesage,
            	R.drawable.gn_stat_sys_data_usb,
                OTG_PREPARE_NOTIFICATION_ID
            );
        }
        else if(newState.equals(Environment.MEDIA_MOUNTED) && oldState.equals(Environment.MEDIA_CHECKING)) {
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
