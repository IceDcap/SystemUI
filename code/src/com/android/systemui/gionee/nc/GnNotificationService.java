package com.android.systemui.gionee.nc;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Notification;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Process;
import android.os.UserHandle;
import android.service.notification.NotificationListenerService;
import android.util.Log;

import com.android.systemui.statusbar.BaseStatusBar;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.NotificationData.Entry;

public class GnNotificationService {
	private final static String TAG = "GnNotificationService";
	// A tag to mark whether to use GnNotificationService, it may be replace by
	// another switcher
	public final static boolean SHOW_GNSTYLE_NOTIFICATION = true;

	// Notifications show on notification center
	private ArrayList<ExpandableNotificationRow> mOnGoingRows = new ArrayList<ExpandableNotificationRow>();
	private ArrayList<ExpandableNotificationRow> mImportantRows = new ArrayList<ExpandableNotificationRow>();
	private ArrayList<ExpandableNotificationRow> mOtherRows = new ArrayList<ExpandableNotificationRow>();

	// Singleton of GnNotificationService
	private static GnNotificationService sInstance = null;

	private static BaseStatusBar mBarService = null;
	private NotificationData mNotificationData = null;
	
    private static ArrayList<String> mOnGoingNotificationList = new ArrayList<>();
    
    private static void initGnNotificationOnGoingList(){
    	int id = findOngoingListRes();
    	final String[] arrays = mBarService.mContext.getResources().
    			getStringArray(id);
    	if (arrays != null) {
    		int len = arrays.length;
    		for(int i=0;i<len;i++) {
    			mOnGoingNotificationList.add(arrays[i]);
    		}
    	}
    }
    
    private static int findOngoingListRes() {
    	int id = -1;
    	try {
			Field[] fields = Class.forName("gionee.R$array").getDeclaredFields();
			for(Field field:fields) {
				Log.v(TAG, "found field: "+ field.getName());
				if ("zzzzz_gn_ongoing_list".equals(field.getName())) {
					id = field.getInt(field);
				}
			}
		} catch (Exception e) {
			Log.v(TAG, e.getCause().toString());
			id = com.android.systemui.R.array.zzzzz_gn_ongoing_list;
		} finally {
			Log.v(TAG, "onGoingList resource ID: "+String.format("%dx", id));
			return id;
		}
    }
//    
//    static {
//    	mOnGoingNotificationList.add("com.android.deskclock");
//		mOnGoingNotificationList.add("com.android.music");
//		mOnGoingNotificationList.add("com.kugou.android");
//		mOnGoingNotificationList.add("com.netease.cloudmusic");
//		mOnGoingNotificationList.add("cn.kuwo.player");
//		mOnGoingNotificationList.add("com.duomi.android");
//		mOnGoingNotificationList.add("com.tencent.qqmusic");
//		mOnGoingNotificationList.add("com.gwsoft.imusic.controller");
//		mOnGoingNotificationList.add("fm.xiami.main");
//		mOnGoingNotificationList.add("com.ting.mp3.android");
//		mOnGoingNotificationList.add("com.android.soundrecorder");
//		mOnGoingNotificationList.add("com.mediatek.fmradio");
//    }
    
	/**
	 * Private construct, add load data from share preference
	 * */
	private GnNotificationService() {
	}

	/**
	 * Interface to get GnNotificationService's object.When the first time call
	 * it, mBarService is set to PhoneStatusBar. In other case we need
	 * GnNotificationService where we cannot get barService, just set is as null
	 * if mBarService is null, throw {@link NullpointerException} to avoid
	 * further exceptions
	 * 
	 * @param barSerice
	 *            PhoneStatusBar, sometimes it may be null
	 * @return sInstance GnNotificationService
	 * */
	public static synchronized GnNotificationService getService(BaseStatusBar barService) {
		if (barService != null) {
			mBarService = barService;
		}

		if (mBarService == null) {
			Log.v(TAG, "mBarservice shouldn't be null ! Now throw nullpoint exception");
			throw new NullPointerException();
		}

		if (sInstance == null) {
			sInstance = new GnNotificationService();
			initGnNotificationOnGoingList();
		}
		return sInstance;
	}

	public boolean isLaunchableApp(String name) {
		PackageManager pm = mBarService.mContext.getPackageManager();
		Intent intent = pm.getLaunchIntentForPackage(name);
		return intent != null;
	}
	
	public boolean isSystemPackage(ApplicationInfo ai) {
    	PackageManager pm = mBarService.mContext.getPackageManager();
        try {
			PackageInfo packageInfo = pm.getPackageInfo(ai.packageName,PackageManager.GET_DISABLED_COMPONENTS |
                    PackageManager.GET_UNINSTALLED_PACKAGES |
                    PackageManager.GET_SIGNATURES);
            PackageInfo sys = pm.getPackageInfo("android", PackageManager.GET_SIGNATURES);
            return (packageInfo != null && packageInfo.signatures != null &&
                    sys.signatures[0].equals(packageInfo.signatures[0]));
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    
	/**
	 * This method is extracted from {@link
	 * PhoneStatusBar.gnUpdateNotificationShade()}, put notifications to three
	 * {@link ExpandableNotificationRow} list
	 * @param notificationData NotificationData contains all of notifications
	 * @return void
	 * */
	public void sortNotificationViews(final NotificationData notificationData) {
		if (notificationData == null)
			return;

		clearHistroyViews();
		mNotificationData = notificationData;

		ArrayList<Entry> activeNotifications = notificationData
				.getActiveNotifications();
		final int N = activeNotifications.size();
		for (int i = 0; i < N; i++) {
			Entry ent = activeNotifications.get(i);
			int vis = ent.notification.getNotification().visibility;

			// Display public version of the notification if we need to redact.
			final boolean hideSensitive = !mBarService
					.userAllowsPrivateNotificationsInPublic(ent.notification.getUserId());
			boolean sensitiveNote = vis == Notification.VISIBILITY_PRIVATE;
			boolean sensitivePackage = packageHasVisibilityOverride(
					notificationData, ent.notification.getKey());
			boolean sensitive = (sensitiveNote && hideSensitive)
					|| sensitivePackage;
			boolean showingPublic = sensitive
					&& mBarService.isLockscreenPublicMode();
			ent.row.setSensitive(sensitive);
			if (ent.autoRedacted && ent.legacy) {
				// TODO: Also fade this? Or, maybe easier (and better), provide
				// a dark redacted form
				// for legacy auto redacted notifications.
				if (showingPublic) {
					ent.row.setShowingLegacyBackground(false);
				} else {
					ent.row.setShowingLegacyBackground(true);
				}
			}

			Log.v(TAG, "SortView:"+ent.notification.getNotification());
			String pkg = ent.notification.getPackageName();
			int uid = ent.notification.getUid();
			int priority = Notification.PRIORITY_DEFAULT;
			final boolean isSystemNotification = isUidSystem(uid) || ("android".equals(pkg));
			if (mChagedPackages.containsKey(pkg)) {
				ent.notification.getNotification().priority = mChagedPackages.get(pkg);
			}
			 if (isOnGoingNotification(ent.notification.getNotification(), pkg)
						/*Notification.PRIORITY_LOW == ent.notification.getNotification().priority*/){
				mOnGoingRows.add(ent.row);
			} else if (Notification.PRIORITY_MAX == ent.notification.getNotification().priority) {
				mImportantRows.add(ent.row);
			} else {
				mOtherRows.add(ent.row);
			}
		}
	}

	HashMap<String, Integer> mChagedPackages = new HashMap<>();
    public boolean isUidSystem(int uid) {
        final int appid = UserHandle.getAppId(uid);
        Log.v(TAG, "Check SystemUI: appId = "+appid);
        return (appid == Process.SYSTEM_UID || appid == Process.PHONE_UID || uid == 0);
    }
    
    private boolean isOnGoingNotification(Notification n, String pkg) {
    	if(mOnGoingNotificationList.contains(pkg) &&
    			(n.flags & Notification.FLAG_ONGOING_EVENT) != 0)
    		return true;
    	return false;
    }
    
	public boolean isInChangedList(String pkg) {
		return mChagedPackages.containsKey(pkg);
	}
	public void refreshChangeList(String pkg, int priority) {
		mChagedPackages.put(pkg, priority);
	}
	
	public void resetChangedList(){
		mChagedPackages.clear();
	}
	/**
	 * Empty all the notification list
	 * */
	final private void clearHistroyViews() {
		if (mOnGoingRows != null) {
			mOnGoingRows.clear();
		}
		if (mImportantRows != null) {
			mImportantRows.clear();
		}
		if (mOtherRows != null) {
			mOtherRows.clear();
		}
	}

	private boolean packageHasVisibilityOverride(
			NotificationData notificationData, String key) {
		return notificationData.getVisibilityOverride(key) != NotificationListenerService.Ranking.VISIBILITY_NO_OVERRIDE;
	}

	/**
	 * Get notification count by type
	 * @param type NotificationType
	 * @return Notification count
	 * */
	public int getNotificationCount(NotificationType type) {
		if (NotificationType.IMPORTANT == type) {
			synchronized (mImportantRows) {
				return mImportantRows.size();
			}
		}
		
		if (NotificationType.OTHER == type) {
			synchronized (mOtherRows) {
				return mOtherRows.size();
			}
		}
		
		return 0;
	}
	
	public ArrayList<ExpandableNotificationRow> getNotificationRow(
			NotificationType type) {
		if (NotificationType.ONGOING == type) {
			return mOnGoingRows;
		}
		if (NotificationType.IMPORTANT == type) {
			return mImportantRows;
		}
		return mOtherRows;
	}

	public ArrayList<ExpandableNotificationRow> getNotificationRow(int type) {
		if (NotificationType.ONGOING.getType() == type) {
			return mOnGoingRows;
		}
		if (NotificationType.IMPORTANT.getType() == type) {
			return mImportantRows;
		}
		return mOtherRows;
	}

	public NotificationData getNotificationData() {
		return mNotificationData;
	}

	public enum NotificationType {
		ONGOING(0), IMPORTANT(1), OTHER(2);
		private int type;

		private NotificationType(int type) {
			this.type = type;
		}

		public int getType() {
			return type;
		}

		@Override
		public String toString() {
			if (type == 2)
				return "OTHER";
			if (type == 1)
				return "IMPORTANT";
			return "ONGOING";
		}
	}

}
