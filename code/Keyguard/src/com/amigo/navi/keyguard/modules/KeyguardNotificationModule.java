package com.amigo.navi.keyguard.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.ActivityManagerNative;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.NotificationListenerService.RankingMap;
import android.service.notification.StatusBarNotification;
import android.util.SparseBooleanArray;
import android.view.IWindowManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManagerGlobal;
import android.widget.DateTimeView;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.keyguard.KeyguardHostView.OnDismissAction;
//import com.gionee.navi.keyguard.KeyguardViewManager;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.R;
import com.amigo.navi.keyguard.KeyguardViewHost;
import com.amigo.navi.keyguard.KeyguardViewHostManager;
//import com.gionee.navi.keyguard.amigo.AmigoSecurityManager;
import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.haokan.UIController;
import com.amigo.navi.keyguard.notification.ActivatableNotificationView;
import com.amigo.navi.keyguard.notification.ActivatableNotificationView.OnActivatedListener;
import com.amigo.navi.keyguard.notification.ExpandableNotificationRow;
import com.amigo.navi.keyguard.notification.NotificationContentView;
import com.amigo.navi.keyguard.notification.NotificationData;
import com.amigo.navi.keyguard.notification.NotificationData.Entry;
import com.amigo.navi.keyguard.notification.StatusBarIconView;
import com.amigo.navi.keyguard.util.AmigoKeyguardUtils;
import com.amigo.navi.keyguard.util.DataStatistics;

public class KeyguardNotificationModule extends KeyguardModuleBase 
						implements NotificationData.Environment,
									OnActivatedListener{
	private static String TAG = "KeyguardNotificationModule";
	private static KeyguardNotificationModule mInstance = null;
	private NotificationCallback mCallback = null;
	private NotificationData mNotificationData;
	private final Handler mHandler = new Handler();
	private IStatusBarService mBarService;
	// Notification type share preference name
	private static final String NOTIFICATION_TYPE_PREFRENCE = "notification_types";
//	private static HashMap<String, Integer> mImportantNotificationMap = new HashMap<String, Integer>();
	
    private final SparseBooleanArray mUsersAllowingPrivateNotifications = new SparseBooleanArray();
//    protected int mCurrentUserId = UserHandle.USER_ALL;
    protected DevicePolicyManager mDevicePolicyManager;
//    private UserManager mUserManager;
    private boolean mShowLockscreenNotifications;
//    private static final String SYSTEMUISHARED_CHANGE = "gn.notification.type.changed";
    private static final String SYSTEMUI_NOTIFY_PANEL_STATE_CHANGE = "gn.intent.action.NOTIFY_PANEL_STATE";
    private static final String SYSTEMUI_PACKAGE_NAME = "com.android.systemui";
    private long mLastKeyguardShowTime=0;
    private boolean mIsFirstTimeShow = true;
    
    private static final int STATE_CLOSED = 0;
    private static final int STATE_OPENING = 1;
    private static final int STATE_OPEN = 2;
    
    private boolean isRemoved = false;

	/**
	 * initial the maps early, this is the default type for notifications.
	 * Add other default types here
	 * */
  //GIONEE <Amigo_Keyguard>  jiating <2015-05-13> modify for user another method  begin
/*	static {
		// Initial Important notification map
		mImportantNotificationMap.put("android",
				NotificationType.IMPORTANT.getType());
		mImportantNotificationMap.put("com.android.mms",
				NotificationType.IMPORTANT.getType());
		mImportantNotificationMap.put("com.tencent.qq",
				NotificationType.IMPORTANT.getType());
		mImportantNotificationMap.put("com.tencent.mm",
				NotificationType.IMPORTANT.getType());
		mImportantNotificationMap.put("com.mediatek.mtklogger",
				NotificationType.IMPORTANT.getType());
		mImportantNotificationMap.put("com.android.systemui",
				NotificationType.IMPORTANT.getType());
		// Initial other's
	}*/
	
    //GIONEE <Amigo_Keyguard>  jiating <2015-05-13> modify for user another method  end
	private NotificationListenerService mNotificationListener = new NotificationListenerService() {
		@Override
		public void onNotificationPosted(StatusBarNotification sbn,
				RankingMap rankingMap) {
			if(isKeyguardShowing()){
			    handlNotificationPosted(sbn,rankingMap);
			}
		}

		@Override
		public void onNotificationRemoved(StatusBarNotification sbn,
				RankingMap rankingMap) {
			if(isKeyguardShowing()){
			  handlNotificationRemoved(sbn,rankingMap);
			}
		}

		@Override
		public void onListenerConnected() {
			StatusBarNotification[] notifications = getActiveNotifications();
			RankingMap currentRanking = getCurrentRanking();
			listenerConnected(notifications, currentRanking);
		}

	};
	
	protected void initModule() {
		mNotificationData = new NotificationData(this);
        mBarService = IStatusBarService.Stub.asInterface(
                ServiceManager.getService(Context.STATUS_BAR_SERVICE));
        mDevicePolicyManager = (DevicePolicyManager)mContext.getSystemService(
                Context.DEVICE_POLICY_SERVICE);
       
        mReceiver = new BroadcastReceiver() {
    		@Override
    		public void onReceive(Context context, Intent intent) {
    			String action = intent.getAction();
                if(DevicePolicyManager.ACTION_DEVICE_POLICY_MANAGER_STATE_CHANGED.equals(action)) {
                	if(DebugLog.DEBUG) DebugLog.d(TAG, "onReceive--ACTION_DEVICE_POLICY_MANAGER_STATE_CHANGED");
                    mUsersAllowingPrivateNotifications.clear();
                    updateLockscreenNotificationSetting();
                    updateNotifications();
    			}/*else if(SYSTEMUISHARED_CHANGE.equals(action)){
    				onSystemUisharedChanged();
    			}*/else if(SYSTEMUI_NOTIFY_PANEL_STATE_CHANGE.equals(action)){
    				int state_code = intent.getIntExtra("panel_state", -1);
    				handleCode(state_code);
    			}
    		}
        };
        
        mFilter = new IntentFilter();
        mFilter.addAction(DevicePolicyManager.ACTION_DEVICE_POLICY_MANAGER_STATE_CHANGED);
//        mFilter.addAction(SYSTEMUISHARED_CHANGE);
        mFilter.addAction(SYSTEMUI_NOTIFY_PANEL_STATE_CHANGE);
	}
	
	private void handleCode(int state_code) {
		if(!isKeyguardShowing())return;
		if(state_code == STATE_OPEN){
			removeAllNotifications();
			isRemoved = true;
		}else if(state_code == STATE_CLOSED && isRemoved){
			setLastKeyguardShowTime(System.currentTimeMillis());
			updateNotifications();
			isRemoved = false;
		}
	}

	public static KeyguardNotificationModule getInstance(Context context, KeyguardUpdateMonitor updateMonitor) {
		if(DebugLog.DEBUG) DebugLog.d(TAG, "getInstance-"+mInstance);
		if (mInstance == null) {
			mInstance = new KeyguardNotificationModule(context,updateMonitor);
		}
		return mInstance;
	}

	private KeyguardNotificationModule(Context context, KeyguardUpdateMonitor updateMonitor) {
		super(context, updateMonitor);
	}

	public void init() {
		if(DebugLog.DEBUG) DebugLog.d(TAG, "init");
//        loadNotificationTypes();
        mSettingsObserver.onChange(false); // set up
        mContext.getContentResolver().registerContentObserver(
                Settings.Secure.getUriFor(Settings.Secure.LOCK_SCREEN_SHOW_NOTIFICATIONS), false,
                mSettingsObserver,
                UserHandle.USER_ALL);

        mContext.getContentResolver().registerContentObserver(
                Settings.Secure.getUriFor(Settings.Secure.LOCK_SCREEN_ALLOW_PRIVATE_NOTIFICATIONS),
                true,
                mLockscreenSettingsObserver,
                UserHandle.USER_ALL);
        mUsersAllowingPrivateNotifications.clear();
        updateLockscreenNotificationSetting();
		try {
			mNotificationListener.registerAsSystemService(mContext,
					new ComponentName(mContext.getPackageName(), getClass()
							.getCanonicalName()), UserHandle.USER_ALL);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void listenerConnected(final StatusBarNotification[] notifications,
			final RankingMap currentRanking) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				for (final StatusBarNotification sbn : notifications) {
					addNotification(sbn, currentRanking);
				}
			}
		});
	}
	
	private void handlNotificationRemoved(final StatusBarNotification sbn,
			final RankingMap rankingMap) {
		mHandler.post(new Runnable() {
            @Override
            public void run() {
            	removeNotification(sbn.getKey(), rankingMap);
            }
		});
	}
	
	private void handlNotificationPosted(final StatusBarNotification sbn,final RankingMap rankingMap){
		mHandler.post(new Runnable() {
            @Override
            public void run() {
            	if(DebugLog.DEBUGMAYBE) DebugLog.d(TAG, "handlNotificationPosted");
                Notification n = sbn.getNotification();
                if(DebugLog.DEBUGMAYBE) DebugLog.d(TAG, "handlNotificationPosted......"+n);
                boolean isUpdate = mNotificationData.get(sbn.getKey()) != null;

                // Ignore children of notifications that have a summary, since we're not
                // going to show them anyway. This is true also when the summary is canceled,
                // because children are automatically canceled by NoMan in that case.
                if (n.isGroupChild() &&
                        mNotificationData.isGroupWithSummary(sbn.getGroupKey())) {

                    // Remove existing notification to avoid stale data.
                    if (isUpdate) {
                    	if(DebugLog.DEBUG) DebugLog.d(TAG, "handlNotificationPosted-removeNotification");
                        removeNotification(sbn.getKey(), rankingMap);
                    } else {
                    	if(DebugLog.DEBUG) DebugLog.d(TAG, "handlNotificationPosted-updateRanking");
                        mNotificationData.updateRanking(rankingMap);
                    }
                    return;
                }
                if (isUpdate) {
					updateNotification(sbn, rankingMap);
                } else {
                    addNotification(sbn, rankingMap);
                }
            }
        });
	}
	
    public void updateNotification(final StatusBarNotification notification, final RankingMap ranking) {
        final String key = notification.getKey();
        final Entry oldEntry  = mNotificationData.get(key);
        if(DebugLog.DEBUG) DebugLog.d(TAG, "updateNotification:"+oldEntry+"notification="+notification+"key="+key);
        if (oldEntry == null) {
            return;
        }
        final StatusBarNotification oldNotification = oldEntry.notification;

        // XXX: modify when we do something more intelligent with the two content views
        final RemoteViews oldContentView = oldNotification.getNotification().contentView;
        final Notification n = notification.getNotification();
        final RemoteViews contentView = n.contentView;
        final RemoteViews oldBigContentView = oldNotification.getNotification().bigContentView;
        final RemoteViews bigContentView = n.bigContentView;
        final RemoteViews oldHeadsUpContentView = oldNotification.getNotification().headsUpContentView;
        final RemoteViews headsUpContentView = n.headsUpContentView;
        final Notification oldPublicNotification = oldNotification.getNotification().publicVersion;
        final RemoteViews oldPublicContentView = oldPublicNotification != null
                ? oldPublicNotification.contentView : null;
        final Notification publicNotification = n.publicVersion;
        final RemoteViews publicContentView = publicNotification != null
                ? publicNotification.contentView : null;
        // Can we just reapply the RemoteViews in place?
        // 1U is never null
        boolean contentsUnchanged = oldEntry.expanded != null
                && contentView.getPackage() != null
                && oldContentView.getPackage() != null
                && oldContentView.getPackage().equals(contentView.getPackage())
                && oldContentView.getLayoutId() == contentView.getLayoutId();
        // large view may be null
        boolean bigContentsUnchanged =
                (oldEntry.getBigContentView() == null && bigContentView == null)
                || ((oldEntry.getBigContentView() != null && bigContentView != null)
                    && bigContentView.getPackage() != null
                    && oldBigContentView.getPackage() != null
                    && oldBigContentView.getPackage().equals(bigContentView.getPackage())
                    && oldBigContentView.getLayoutId() == bigContentView.getLayoutId());
        boolean headsUpContentsUnchanged =
                (oldHeadsUpContentView == null && headsUpContentView == null)
                || ((oldHeadsUpContentView != null && headsUpContentView != null)
                    && headsUpContentView.getPackage() != null
                    && oldHeadsUpContentView.getPackage() != null
                    && oldHeadsUpContentView.getPackage().equals(headsUpContentView.getPackage())
                    && oldHeadsUpContentView.getLayoutId() == headsUpContentView.getLayoutId());
        boolean publicUnchanged  =
                (oldPublicContentView == null && publicContentView == null)
                || ((oldPublicContentView != null && publicContentView != null)
                        && publicContentView.getPackage() != null
                        && oldPublicContentView.getPackage() != null
                        && oldPublicContentView.getPackage().equals(publicContentView.getPackage())
                        && oldPublicContentView.getLayoutId() == publicContentView.getLayoutId());

        boolean updateSuccessful = false;
        if (contentsUnchanged && bigContentsUnchanged && headsUpContentsUnchanged
                && publicUnchanged) {
            oldEntry.notification = notification;
            try {
                if (oldEntry.icon != null) {
                    // Update the icon
                    final StatusBarIcon ic = new StatusBarIcon(notification.getPackageName(),
                            notification.getUser(),
                            n.icon,
                            n.iconLevel,
                            n.number,
                            n.tickerText);
                    oldEntry.icon.setNotification(n);
                    if (!oldEntry.icon.set(ic)) {
//                        handleNotificationError(notification, "Couldn't update icon: " + ic);
                        return;
                    }
                }
		        final PendingIntent contentIntent = notification.getNotification().contentIntent;
		        if (contentIntent != null) {
		            final View.OnClickListener listener = makeClicker(contentIntent, notification.getKey());
		            oldEntry.row.setOnClickListener(listener);
		        } else {
		        	oldEntry.row.setOnClickListener(null);
		        }
		        // Reapply the RemoteViews
		        contentView.reapply(mContext, oldEntry.expanded, mOnClickHandler);
		        if (bigContentView != null && oldEntry.getBigContentView() != null) {
		            bigContentView.reapply(mContext, oldEntry.getBigContentView(),
		                    mOnClickHandler);
		        }
		        if (publicContentView != null && oldEntry.getPublicContentView() != null) {
		            publicContentView.reapply(mContext, oldEntry.getPublicContentView(), mOnClickHandler);
		        }
		        oldEntry.row.setStatusBarNotification(notification);
		        oldEntry.row.notifyContentUpdated();
		        oldEntry.row.resetHeight();
                updateNotificationsWithRankingMap(ranking);
                updateSuccessful = true;
            }
            catch (RuntimeException e) {
                // It failed to add cleanly.  Log, and remove the view from the panel.
            	DebugLog.w(TAG, "Couldn't reapply views for package " + contentView.getPackage()+"---"+e);
            }
        }
        if(DebugLog.DEBUGMAYBE) DebugLog.d(TAG, "updateNotification-updateSuccessful:"+updateSuccessful);
        if (!updateSuccessful) {
                oldEntry.notification = notification;
                final StatusBarIcon ic = new StatusBarIcon(notification.getPackageName(),
                        notification.getUser(),
                        n.icon,
                        n.iconLevel,
                        n.number,
                        n.tickerText);
                oldEntry.icon.setNotification(n);
                oldEntry.icon.set(ic);
                if(inflateViews(oldEntry)){
                	if(DebugLog.DEBUGMAYBE) DebugLog.d(TAG, "updateNotification-inflateViews-true");
                    updateNotificationsWithRankingMap(ranking);
                    updateNotificationVetoButton(oldEntry.row, notification);
                }
        }else{
        	// Update the veto button accordingly (and as a result, whether this row is
        	// swipe-dismissable)
        	updateNotificationVetoButton(oldEntry.row, notification);
        }
    }
	
	
    private void removeNotification(String key, RankingMap ranking) {
        NotificationData.Entry entry = mNotificationData.remove(key, ranking);
        if(DebugLog.DEBUGMAYBE) DebugLog.d(TAG, "removeNotification:"+entry+"key="+key);
        if (entry == null) {
            return;
        }
        notifyUpdate();
  }

	private void addNotification(StatusBarNotification notification,
			RankingMap ranking) {
		if(DebugLog.DEBUG) DebugLog.d(TAG, "addNotification:"+notification);
		Entry shadeEntry = createNotificationViews(notification);
		if (shadeEntry == null) {
			return;
		}
		addNotificationViews(shadeEntry, ranking);
	}

	private NotificationData.Entry createNotificationViews(
			StatusBarNotification sbn) {
		// Construct the icon.
		Notification n = sbn.getNotification();
		final StatusBarIconView iconView = new StatusBarIconView(
				mContext,
				sbn.getPackageName() + "/0x" + Integer.toHexString(sbn.getId()),
				n);
		iconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

		final StatusBarIcon ic = new StatusBarIcon(sbn.getPackageName(),
				sbn.getUser(), n.icon, n.iconLevel, n.number, n.tickerText);
		if (!iconView.set(ic)) {
			return null;
		}
		// Construct the expanded view.
		NotificationData.Entry entry = new NotificationData.Entry(sbn, iconView);
		if (!inflateViews(entry)) {
			return null;
		}
		return entry;
	}

	private boolean inflateViews(NotificationData.Entry entry) {
		PackageManager pmUser = getPackageManagerForUser(entry.notification
				.getUser().getIdentifier());
		int maxHeight = mContext.getResources().getDimensionPixelSize(
				R.dimen.notification_max_height);
		int minHeight = mContext.getResources().getDimensionPixelSize(
				R.dimen.notification_min_height);
		final StatusBarNotification sbn = entry.notification;
		RemoteViews contentView = sbn.getNotification().contentView;
		RemoteViews bigContentView = sbn.getNotification().bigContentView;

		if (contentView == null) {
			return false;
		}

		if(DebugLog.DEBUG) DebugLog.v(TAG, "publicNotification: " + sbn.getNotification().publicVersion);

		Notification publicNotification = sbn.getNotification().publicVersion;

		ExpandableNotificationRow row;

		// Stash away previous user expansion state so we can restore it at
		// the end.
		boolean hasUserChangedExpansion = false;
		boolean userExpanded = false;
		boolean userLocked = false;

		if (entry.row != null) {
			row = entry.row;
			hasUserChangedExpansion = row.hasUserChangedExpansion();
			userExpanded = row.isUserExpanded();
			userLocked = row.isUserLocked();
			entry.reset();
			if (hasUserChangedExpansion) {
				row.setUserExpanded(userExpanded);
			}
		} else {
			// create the row view
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = (ExpandableNotificationRow) inflater.inflate(
					R.layout.amigo_keyguard_notification_row, null, false);
			row.setExpansionLogger(null, entry.notification.getKey());
		}
		updateNotificationVetoButton(row, sbn);
		// NB: the large icon is now handled entirely by the template

		// bind the click event to the content area
		NotificationContentView expanded = (NotificationContentView) row
				.findViewById(R.id.expanded);
		NotificationContentView expandedPublic = (NotificationContentView) row
				.findViewById(R.id.expandedPublic);

		// / M: Notification UI Support RTL.
		row.setLayoutDirection(View.LAYOUT_DIRECTION_LOCALE);

		row.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

		PendingIntent contentIntent = sbn.getNotification().contentIntent;
		if (contentIntent != null) {
			final View.OnClickListener listener = makeClicker(contentIntent,sbn.getKey());
			row.setOnClickListener(listener);
		} else {
			row.setOnClickListener(null);
		}

		// set up the adaptive layout
		View contentViewLocal = null;
		View bigContentViewLocal = null;
		try {
			contentViewLocal = contentView.apply(mContext, expanded,
					mOnClickHandler);
			if (bigContentView != null) {
				bigContentViewLocal = bigContentView.apply(mContext, expanded,
						mOnClickHandler);
			}
		} catch (RuntimeException e) {
			final String ident = sbn.getPackageName() + "/0x"
					+ Integer.toHexString(sbn.getId());
			DebugLog.e(TAG, "couldn't inflate view for notification " + ident, e);
			return false;
		}

		if (contentViewLocal != null) {
			contentViewLocal.setIsRootNamespace(true);
			expanded.setContractedChild(contentViewLocal);
		}
		if (bigContentViewLocal != null) {
			bigContentViewLocal.setIsRootNamespace(true);
			expanded.setExpandedChild(bigContentViewLocal);
		}

		// now the public version
		View publicViewLocal = null;
		if (publicNotification != null) {
			try {
				publicViewLocal = publicNotification.contentView.apply(
						mContext, expandedPublic, mOnClickHandler);

				if (publicViewLocal != null) {
					publicViewLocal.setIsRootNamespace(true);
					expandedPublic.setContractedChild(publicViewLocal);
				}
			} catch (RuntimeException e) {
				final String ident = sbn.getPackageName() + "/0x"
						+ Integer.toHexString(sbn.getId());
				DebugLog.e(TAG, "couldn't inflate public view for notification "
						+ ident, e);
				publicViewLocal = null;
			}
		}

		// Extract target SDK version.
		try {
			ApplicationInfo info = pmUser.getApplicationInfo(
					sbn.getPackageName(), 0);
			entry.targetSdk = info.targetSdkVersion;
		} catch (NameNotFoundException ex) {
			if(DebugLog.DEBUG) DebugLog.e(TAG,
					"Failed looking up ApplicationInfo for "
							+ sbn.getPackageName(), ex);
		}

		if (publicViewLocal == null) {
			// Add a basic notification template
			publicViewLocal = LayoutInflater.from(mContext)
					.inflate(R.layout.amigo_keyguard_notification_public_default,
							expandedPublic, false);
			publicViewLocal.setIsRootNamespace(true);
			expandedPublic.setContractedChild(publicViewLocal);

			final TextView title = (TextView) publicViewLocal
					.findViewById(R.id.title);
			try {
				title.setText(pmUser.getApplicationLabel(pmUser
						.getApplicationInfo(
								entry.notification.getPackageName(), 0)));
			} catch (NameNotFoundException e) {
				title.setText(entry.notification.getPackageName());
			}

			final ImageView icon = (ImageView) publicViewLocal
					.findViewById(R.id.icon);
			final ImageView profileBadge = (ImageView) publicViewLocal
					.findViewById(R.id.profile_badge_line3);

			final StatusBarIcon ic = new StatusBarIcon(
					entry.notification.getPackageName(),
					entry.notification.getUser(),
					entry.notification.getNotification().icon,
					entry.notification.getNotification().iconLevel,
					entry.notification.getNotification().number,
					entry.notification.getNotification().tickerText);

			Drawable iconDrawable = StatusBarIconView.getIcon(mContext, ic);
			icon.setImageDrawable(iconDrawable);
			if (entry.targetSdk >= Build.VERSION_CODES.L
			/* || mNotificationColorUtil.isGrayscaleIcon(iconDrawable) */) {
				icon.setBackgroundResource(com.android.internal.R.drawable.notification_icon_legacy_bg);
				int padding = mContext
						.getResources()
						.getDimensionPixelSize(
								com.android.internal.R.dimen.notification_large_icon_circle_padding);
				icon.setPadding(padding, padding, padding, padding);
				if (sbn.getNotification().color != Notification.COLOR_DEFAULT) {
					icon.getBackground().setColorFilter(
							sbn.getNotification().color,
							PorterDuff.Mode.SRC_ATOP);
				}
			}

			if (profileBadge != null) {
				Drawable profileDrawable = mContext
						.getPackageManager()
						.getUserBadgeForDensity(entry.notification.getUser(), 0);
				if (profileDrawable != null) {
					profileBadge.setImageDrawable(profileDrawable);
					profileBadge.setVisibility(View.VISIBLE);
				} else {
					profileBadge.setVisibility(View.GONE);
				}
			}
			try {

				final View privateTime = contentViewLocal
						.findViewById(com.android.internal.R.id.time);
				final DateTimeView time = (DateTimeView) publicViewLocal
						.findViewById(R.id.time);
				if (privateTime != null
						&& privateTime.getVisibility() == View.VISIBLE) {
					time.setVisibility(View.VISIBLE);
					time.setTime(entry.notification.getNotification().when);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			final TextView text = (TextView) publicViewLocal
					.findViewById(R.id.text);
			if (text != null) {
				text.setText(R.string.notification_hidden_text);
				// text.setTextAppearance(mContext,
				// R.style.TextAppearance_Material_Notification_Parenthetical);
			}

			int topPadding = Notification.Builder.calculateTopPadding(mContext,
					false /* hasThreeLines */, mContext.getResources()
							.getConfiguration().fontScale);
			title.setPadding(0, topPadding, 0, 0);

			entry.autoRedacted = true;
		}

		row.setClearable(sbn.isClearable());

		entry.row = row;
		entry.row.setHeightRange(minHeight, maxHeight);
		if (contentIntent != null) {
			 entry.row.setOnActivatedListener(this);
		} else {
			 entry.row.setOnActivatedListener(null);
		}
		
		entry.expanded = contentViewLocal;
		entry.expandedPublic = publicViewLocal;
		entry.setBigContentView(bigContentViewLocal);

		applyColorsAndBackgrounds(sbn, entry);

		// Restore previous flags.
		if (hasUserChangedExpansion) {
			// Note: setUserExpanded() conveniently ignores calls with
			// userExpanded=true if !isExpandable().
			row.setUserExpanded(userExpanded);
		}
		row.setUserLocked(userLocked);
		row.setStatusBarNotification(entry.notification);
		return true;
	}

	private void updateNotificationVetoButton(final View row,
			final StatusBarNotification n) {
		View vetoButton = row.findViewById(R.id.veto);
		if (n.isClearable()) {
			final String _pkg = n.getPackageName();
			final String _tag = n.getTag();
			final int _id = n.getId();
			final int _userId = n.getUserId();
			vetoButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					// Accessibility feedback
				    DataStatistics.getInstance().swipToremoveNotification(mContext);
					v.announceForAccessibility(mContext
							.getString(R.string.accessibility_notification_dismissed));
					try {
						mBarService.onNotificationClear(_pkg, _tag, _id,
								_userId);

					} catch (RemoteException ex) {
						// system process is dead if we're here.
						ex.printStackTrace();
					}
				}
			});
			vetoButton.setVisibility(View.VISIBLE);
		} else {
			vetoButton.setVisibility(View.GONE);
		}
		vetoButton
				.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
		vetoButton.setContentDescription(mContext
				.getString(R.string.accessibility_remove_notification));
	}

    private RemoteViews.OnClickHandler mOnClickHandler = new RemoteViews.OnClickHandler() {
        @Override
        public boolean onClickHandler(
                final View view, final PendingIntent pendingIntent, final Intent fillInIntent) {
            final boolean isActivity = pendingIntent.isActivity();
            if(DebugLog.DEBUG) DebugLog.d(TAG, "mOnClickHandler click,isActivity:"+isActivity);
            if (isActivity) {
                final boolean afterKeyguardGone = AmigoKeyguardUtils.wouldLaunchResolverActivity(mContext, pendingIntent.getIntent());
                if(DebugLog.DEBUG) DebugLog.d(TAG, "mOnClickHandler click,afterkeyguardgone:"+afterKeyguardGone);
                dismissKeyguardThenExecute(new OnDismissAction() {
                    @Override
                    public boolean onDismiss() {
                        if (!afterKeyguardGone) {
                            try {
                                ActivityManagerNative.getDefault()
                                        .keyguardWaitingForActivityDrawn();
                                // The intent we are sending is for the application, which
                                // won't have permission to immediately start an activity after
                                // the user switches to home.  We know it is safe to do at this
                                // point, so make sure new activity switches are now allowed.
                                ActivityManagerNative.getDefault().resumeAppSwitches();
                            } catch (RemoteException e) {
                            }
                        }

                        boolean handled = superOnClickHandler(view, pendingIntent, fillInIntent);
                        overrideActivityPendingAppTransition();
                        return handled;
                    }
                }, afterKeyguardGone);
                return true;
            } else {
                return super.onClickHandler(view, pendingIntent, fillInIntent);
            }
        }

        private boolean superOnClickHandler(View view, PendingIntent pendingIntent,
                Intent fillInIntent) {
            return super.onClickHandler(view, pendingIntent, fillInIntent);
        }
    };
    
    private void overrideActivityPendingAppTransition() {
		try {
			IWindowManager mWindowManagerService = WindowManagerGlobal
					.getWindowManagerService();
			mWindowManagerService
					.overridePendingAppTransition(
							null, 0, 0, null);
		} catch (RemoteException e) {
			DebugLog.w(TAG,
					"Error overriding app transition: "
							+ e);
		}
    }

	private PackageManager getPackageManagerForUser(int userId) {
		Context contextForUser = mContext;
		// UserHandle defines special userId as negative values, e.g. USER_ALL
		if (userId >= 0) {
			try {
				// Create a context for the correct user so if a package isn't
				// installed
				// for user 0 we can still load information about the package.
				contextForUser = mContext.createPackageContextAsUser(
						mContext.getPackageName(), Context.CONTEXT_RESTRICTED,
						new UserHandle(userId));
			} catch (NameNotFoundException e) {
				// Shouldn't fail to find the package name for system ui.
			}
		}
		return contextForUser.getPackageManager();
	}

	private void applyColorsAndBackgrounds(StatusBarNotification sbn,
			NotificationData.Entry entry) {

		if (entry.expanded.getId() != com.android.internal.R.id.status_bar_latest_event_content) {
			// Using custom RemoteViews
			if (entry.targetSdk >= Build.VERSION_CODES.GINGERBREAD
					&& entry.targetSdk < Build.VERSION_CODES.L) {
				entry.row.setShowingLegacyBackground(true);
				entry.legacy = true;
			}
		} else {
			// Using platform templates
			final int color = sbn.getNotification().color;
			if (isMediaNotification(entry)) {
				entry.row
						.setTintColor(color == Notification.COLOR_DEFAULT ? mContext
								.getResources()
								.getColor(
										R.color.notification_material_background_media_default_color)
								: color);
			}
		}

		if (entry.icon != null) {
			if (entry.targetSdk >= Build.VERSION_CODES.L) {
				entry.icon.setColorFilter(mContext.getResources().getColor(
						android.R.color.white));
			} else {
				entry.icon.setColorFilter(null);
			}
		}
	}

	private boolean isMediaNotification(NotificationData.Entry entry) {
		return entry.expandedBig != null
				&& entry.expandedBig
						.findViewById(com.android.internal.R.id.media_actions) != null;
	}

	private void addNotificationViews(Entry entry, RankingMap ranking) {
		if(DebugLog.DEBUGMAYBE) DebugLog.d(TAG, "addNotificationViews:"+entry);
		if (entry == null) {
			return;
		}
		// Add the expanded view and icon.
		mNotificationData.add(entry, ranking);
		updateNotifications();
	}

	public void updateNotifications() {
		updateNotificationsWithRankingMap(null);
	}
	
	private void updateNotificationsWithRankingMap(final RankingMap ranking){
		if(DebugLog.DEBUGMAYBE) DebugLog.d(TAG, "updateNotificationsWithRankingMap");
		processAndUpdateNotifications(ranking);
	}
	
	private void processAndUpdateNotifications(RankingMap ranking){
		if(DebugLog.DEBUGMAYBE) DebugLog.d(TAG, "processAndUpdateNotifications");
//		mNotificationData.filterAndSort();
		mNotificationData.updateRanking(ranking);
		notifyUpdate();
	}
	
	public void removeAllNotifications(){
		if(DebugLog.DEBUG) DebugLog.d(TAG, "removeAllNotifications");
		mNotificationData.getActiveNotifications().clear();
		notifyUpdate();
	}
	
    public NotificationClicker makeClicker(PendingIntent intent, String notificationKey) {
        return new NotificationClicker(intent, notificationKey);
    }

    private class NotificationClicker implements View.OnClickListener {
        private PendingIntent mIntent;
        private final String mNotificationKey;

        public NotificationClicker(PendingIntent intent, String notificationKey) {
            mIntent = intent;
            mNotificationKey = notificationKey;
        }

        public void onClick(final View v) {
            final boolean afterKeyguardGone = mIntent.isActivity() && AmigoKeyguardUtils.wouldLaunchResolverActivity(mContext, mIntent.getIntent());
            if(DebugLog.DEBUG) DebugLog.d(TAG, "NotificationClicker-onclick:"+afterKeyguardGone);
            DataStatistics.getInstance().doubleTapNotification(mContext);
            dismissKeyguardThenExecute(new OnDismissAction() {
                public boolean onDismiss() {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (!afterKeyguardGone) {
                                try {
                                    ActivityManagerNative.getDefault()
                                            .keyguardWaitingForActivityDrawn();
                                    // The intent we are sending is for the application, which
                                    // won't have permission to immediately start an activity after
                                    // the user switches to home.  We know it is safe to do at this
                                    // point, so make sure new activity switches are now allowed.
                                    ActivityManagerNative.getDefault().resumeAppSwitches();
                                } catch (RemoteException e) {
                                }
                            }

                            if (mIntent != null) {
                                try {
                                    mIntent.send();
                                } catch (PendingIntent.CanceledException e) {
                                    // the stack trace isn't very helpful here.
                                    // Just log the exception message.
                                	DebugLog.w(TAG, "Sending contentIntent failed: " +"---"+e);

                                    // TODO: Dismiss Keyguard.
                                }
                                if (mIntent.isActivity()) {
                                	overrideActivityPendingAppTransition();
                                }
                            }

                            try {
                                mBarService.onNotificationClick(mNotificationKey);
                            } catch (RemoteException ex) {
                                // system process is dead if we're here.
                            }
                        }
                    });
                    return mIntent != null && mIntent.isActivity();
                }
            }, afterKeyguardGone);
        }
    }
	
    private void dismissKeyguardThenExecute(final OnDismissAction action,
            boolean afterKeyguardGone) {
    	if(DebugLog.DEBUG) DebugLog.d(TAG, "dismissKeyguardThenExecute:"+afterKeyguardGone);
        KeyguardViewHostManager.getInstance().dismissWithDismissAction(action,afterKeyguardGone);
    }
	
	@Override
	public boolean shouldHideSensitiveContents(int userid) {
		return allowsPrivateNotificationsInPublic();
	}

	@Override
	public boolean isDeviceProvisioned() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isNotificationForCurrentProfiles(StatusBarNotification sbn) {
//        final int thisUserId = mCurrentUserId;
//        final int notificationUserId = sbn.getUserId();
//        DebugLog.d(TAG, String.format("%s: current userid: %d, notification userid: %d",
//        		sbn, thisUserId, notificationUserId));
//        synchronized (mCurrentProfiles) {
//            return notificationUserId == UserHandle.USER_ALL
//                    || mCurrentProfiles.get(notificationUserId) != null;
//        }
		return true;
	}

	@Override
	public String getCurrentMediaNotificationKey() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean isImportantNotification(StatusBarNotification sbn) {
		if(DebugLog.DEBUGMAYBE) DebugLog.d(TAG, "isImportantNotification --sbn " +(sbn==null));
		if (sbn == null)
			return false;
//		String pkgName = sbn.getPackageName();
//		if(DebugLog.DEBUGMAYBE) DebugLog.d(TAG, "isImportantNotification --sbn...pkgName " +pkgName);

		if(sbn.getNotification().priority==Notification.PRIORITY_MAX){
			return true;
		}
		/*if (mImportantNotificationMap.containsKey(pkgName)) {
			return true;
		}*/
		return false;
	}
	
	@Override
	public boolean isNewNotification(StatusBarNotification sbn) {
		if(mIsFirstTimeShow){
			return true;
		}
    	if(sbn.getPostTime() > getLastKeyguardShowTime()){
    		return true;
    	}else{
    		return false;
    	}
	}
	
	@Override
	public boolean isKeyguardShowing() {
		return KeyguardViewHostManager.getInstance().isShowing();
//    	if(KeyguardViewManager.getInstance().isShowing()){
//    		return true;
//    	}else{
//    		return false;
//    	}
	}

	/*private void onSystemUisharedChanged(){
		loadNotificationTypes();
		updateNotifications();
	}*/
	
	/**
	 * Load notification type informations from share preference
	 * */
	/*@SuppressWarnings("unchecked")
	private void loadNotificationTypes() {
		try {
			Context friendContext = mContext.createPackageContext("com.android.systemui",Context.CONTEXT_IGNORE_SECURITY);
			SharedPreferences preferences = friendContext.getSharedPreferences(NOTIFICATION_TYPE_PREFRENCE, Context.MODE_PRIVATE);
			HashMap<String, Integer> notificationList = (HashMap<String, Integer>) preferences.getAll();
//			Iterator<String> iterator = notificationList.keySet().iterator();
			// findbugs：Efficiency is higher than the other
			Iterator<java.util.Map.Entry<String, Integer>> iterator = notificationList.entrySet().iterator();
			while (iterator.hasNext()) {
				java.util.Map.Entry<String, Integer> entry = iterator.next();
				String pkgName = entry.getKey();
				if(DebugLog.DEBUGMAYBE) DebugLog.d(TAG, "loadNotificationTypes,pkgName:"+pkgName+"Type="+entry.getValue());
				if (entry.getValue() == Notification.PRIORITY_MAX NotificationType.IMPORTANT.getType()) {
					mImportantNotificationMap.put(pkgName, NotificationType.IMPORTANT.getType());
				} else {
					mImportantNotificationMap.remove(pkgName);
				}
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}*/
	

	public void registerCallback(NotificationCallback callBack) {
		mCallback = callBack;
	}
	
	public void unRegisterCallback(){
		mCallback = null;
	}

	private void notifyUpdate() {
		if(DebugLog.DEBUGMAYBE) DebugLog.d(TAG, "notifyUpdate,mCallback:"+mCallback);
		if (mCallback != null) {
			mCallback.onUpdateNotifications(mNotificationData.getActiveNotifications());
		}
	}

	public interface NotificationCallback {
		public void onUpdateNotifications(ArrayList<Entry> notifications);
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
	
	
    /**
     * Has the given user chosen to allow their private (full) notifications to be shown even
     * when the lockscreen is in "public" (secure & locked) mode?
     */
	
    public boolean allowsPrivateNotificationsInPublic() {
        final boolean allowed = 0 != Settings.Secure.getInt(
                mContext.getContentResolver(),
                Settings.Secure.LOCK_SCREEN_ALLOW_PRIVATE_NOTIFICATIONS, 0);
        return allowed;
    }
    private final ContentObserver mSettingsObserver = new ContentObserver(mHandler) {
        @Override
        public void onChange(boolean selfChange) {
        	if(DebugLog.DEBUG) DebugLog.d(TAG, "mSettingsObserver--onChange");
            updateLockscreenNotificationSetting();
            updateNotifications();
        }
    };
	
    private final ContentObserver mLockscreenSettingsObserver = new ContentObserver(mHandler) {
        @Override
        public void onChange(boolean selfChange) {
        	if(DebugLog.DEBUG) DebugLog.d(TAG, "mLockscreenSettingsObserver--onChange");
            // We don't know which user changed LOCK_SCREEN_ALLOW_PRIVATE_NOTIFICATIONS,
            // so we just dump our cache ...
            mUsersAllowingPrivateNotifications.clear();
            // ... and refresh all the notifications
            updateNotifications();
        }
    };
    
    private void updateLockscreenNotificationSetting() {
        final boolean show = Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.LOCK_SCREEN_SHOW_NOTIFICATIONS,
                1) != 0;
        final int dpmFlags = mDevicePolicyManager.getKeyguardDisabledFeatures(
                null /* admin *//*, mCurrentUserId*/);
        final boolean allowedByDpm = (dpmFlags
                & DevicePolicyManager.KEYGUARD_DISABLE_SECURE_NOTIFICATIONS) == 0;
        setShowLockscreenNotifications(show && allowedByDpm);
    }
    
    private void setShowLockscreenNotifications(boolean show) {
    	if(DebugLog.DEBUG) DebugLog.d(TAG, "setShowLockscreenNotifications--show:"+show);
        mShowLockscreenNotifications = show;
    }
    
    public boolean packageHasVisibilityOverride(String key) {
        return mNotificationData.getVisibilityOverride(key)
                != NotificationListenerService.Ranking.VISIBILITY_NO_OVERRIDE;
    }
    
    public boolean isLockscreenPublicMode() {
        return (/*KeyguardViewManager.getInstance().isShowing()*/isKeyguardShowing()
        		/*||mStatusBarKeyguardViewManager.isOccluded()*/)
        		
        		// guosb modify for notification begin
        		// TODO add security opinion 
                && KeyguardViewHostManager.getInstance().isSecure();
        // guosb modify for notification end
    }
    
    public boolean isShowLockscreenNotifications(){
    	return mShowLockscreenNotifications;
    }
    
    public boolean isAmbient(String key) {
        return mNotificationData.isAmbient(key);
    }
    
    public boolean hasNotification(){
        boolean hasNotification=false;
        hasNotification=mNotificationData.hasNotification();
        DebugLog.d(TAG, "hasNotifications: "+hasNotification);
        return hasNotification;
    }

    @Override
    public void onActivated(ActivatableNotificationView view) {
    	if(DebugLog.DEBUG) DebugLog.d(TAG, "onActivated--view:"+view);
        mUpdateMonitor.showNotificationClickTip(true,view);
    }

	@Override
	public void onActivationReset(ActivatableNotificationView view) {
		if(DebugLog.DEBUG) DebugLog.d(TAG, "onActivationReset--view:"+view);
		mUpdateMonitor.resetNotificationClick(view);
	}
	

	public long getLastKeyguardShowTime() {
		return mLastKeyguardShowTime;
	}
	
	public void setLastKeyguardShowTime(long time){
		if(DebugLog.DEBUG) DebugLog.d(TAG, "setLastKeyguardShowTime--time:"+time);
		mIsFirstTimeShow = false;
		mLastKeyguardShowTime = time;
	}
    
}
