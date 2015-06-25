/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.systemui.recent;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.CalendarContract.Instances;
import android.util.Log;
import android.view.IWindowManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManagerGlobal;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.systemui.R;
import com.android.systemui.SwipeHelper;
import com.android.systemui.SystemUIApplication;
import com.android.systemui.statusbar.StatusBarPanel;
import com.android.systemui.statusbar.phone.PhoneStatusBar;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.android.internal.util.MemInfoReader;

import android.text.format.Formatter;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;

import com.android.systemui.gionee.GnUtil;

public class RecentsActivity extends Activity implements OnClickListener , OnLongClickListener {
	
	private static final String TAG = "RecentsActivity";
	
    public static final String TOGGLE_RECENTS_INTENT = "com.android.systemui.recent.action.TOGGLE_RECENTS";
    public static final String PRELOAD_INTENT = "com.android.systemui.recent.action.PRELOAD";
    public static final String CANCEL_PRELOAD_INTENT = "com.android.systemui.recent.CANCEL_PRELOAD";
    public static final String CLOSE_RECENTS_INTENT = "com.android.systemui.recent.action.CLOSE";
    public static final String WINDOW_ANIMATION_START_INTENT = "com.android.systemui.recent.action.WINDOW_ANIMATION_START";
    public static final String PRELOAD_PERMISSION = "com.android.systemui.recent.permission.PRELOAD";
    public static final String WAITING_FOR_WINDOW_ANIMATION_PARAM = "com.android.systemui.recent.WAITING_FOR_WINDOW_ANIMATION";
    private static final String WAS_SHOWING = "was_showing";
    private static final String WHITE_LIST_MANAGER = "com.gionee.softmanager.action.WHITE_LIST_MANAGER";
	private static final String PROCESS_WHITE_LIST = "content://com.gionee.systemmanager.oneclean/whitelist";

    private RecentsPanelView mRecentsPanel;
    private IntentFilter mIntentFilter;
    private boolean mShowing;
    private boolean mForeground;
    private RelativeLayout mRecentReleaseLayout;
    private ImageView mRecentAppClearView;
    private GnScanView mGnScanView;
    private Context mContext;
    private TextView mMemoryInfo;
    private TextView mClearNotice;
    private static final int MSG_STOP_PROCESS_DONE = 0;
    private static final int MSG_SCAN = 1;
    private static final int MSG_START_CLEAR = 2;
    private static final int MSG_STOP_CLEAR = 3;
    private Thread mThread;
    private ActivityManager mAm;
    private ActivityManager.MemoryInfo mMemInfo = new MemoryInfo();
    private long mMemoryUsed = 0;
    private static boolean mIsClearBtnLongPress = false;

    private final static String BLUR_IMAGE_FILE = "/data/misc/gionee/";
    
    private float mMemPercent = 0;
    private int mPreAngle = 0;
    private int mAngle = 0;
    private int mStopTime = 0;
    private boolean isClearing = false;
    
    private PhoneStatusBar mStatusBar;
    private List<String> musicApps = null;

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (CLOSE_RECENTS_INTENT.equals(intent.getAction())) {
                if (mRecentsPanel != null && mRecentsPanel.isShowing()) {
                    if (mShowing && !mForeground) {
                        // Captures the case right before we transition to another activity
                        mRecentsPanel.show(false);
                    }
                }
            } else if (WINDOW_ANIMATION_START_INTENT.equals(intent.getAction())) {
                if (mRecentsPanel != null) {
                    mRecentsPanel.onWindowAnimationStart();
                }
            }
        }
    };

    public class TouchOutsideListener implements View.OnTouchListener {
        private StatusBarPanel mPanel;

        public TouchOutsideListener(StatusBarPanel panel) {
            mPanel = panel;
        }

        public boolean onTouch(View v, MotionEvent ev) {
            final int action = ev.getAction();
            if (action == MotionEvent.ACTION_OUTSIDE
                    || (action == MotionEvent.ACTION_DOWN
                    && !mPanel.isInContentArea((int) ev.getX(), (int) ev.getY()))) {
                //dismissAndGoHome();
                return true;
            }
            return false;
        }
    }

    @Override
    public void onPause() {
        overridePendingTransition(
                R.anim.recents_return_to_launcher_enter,
                R.anim.recents_return_to_launcher_exit);
        mForeground = false;
        super.onPause();
    }

    @Override
    public void onStop() {
        mShowing = false;
        if (mRecentsPanel != null) {
        	mRecentsPanel.show(false);
            mRecentsPanel.onUiHidden();
            mRecentsPanel.unRegisterScreenLockObserver(mContext);
            mRecentsPanel.setVisibility(View.GONE);
        }
        super.onStop();
        finish();
    }

    private Bitmap getBlurBitmap () {
        Log.d(TAG, "getBlur start!");
        InputStream fis = null;
        try {
            fis = new FileInputStream(BLUR_IMAGE_FILE + "blur");
            return BitmapFactory.decodeStream(fis);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (fis != null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fis = null;
            }
        }
    }
    
    private void updateWallpaperVisibility(boolean visible) {
/*        int wpflags = visible ? WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER : 0;
        int curflags = getWindow().getAttributes().flags
                & WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER;
        if (wpflags != curflags) {
            getWindow().setFlags(wpflags, WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
        }*/
        if (visible) {
            mRecentsPanel.setBackgroundDrawable(new BitmapDrawable(getBlurBitmap()));
        }
    }

    public static boolean forceOpaqueBackground(Context context) {
        return WallpaperManager.getInstance(context).getWallpaperInfo() != null;
    }

    @Override
    public void onStart() {
        // Hide wallpaper if it's not a static image
        if (forceOpaqueBackground(this) || !GnUtil.isHighDevice(mContext)) {
            updateWallpaperVisibility(false);
        } else {
            updateWallpaperVisibility(true);
        }
        getProcessWhiteList();
        mShowing = true;
        Log.d(TAG, " on Start. mRecentsPanel is null ? = " + mRecentsPanel);
        if (mRecentsPanel != null) {
            // Call and refresh the recent tasks list in case we didn't preload tasks
            // or in case we don't get an onNewIntent
            mRecentsPanel.refreshRecentTasksList();
            mRecentsPanel.refreshViews();
        }
        super.onStart();
    }

    @Override
    public void onResume() {
        mForeground = true;
        updateMemoryInfo();
        mRecentsPanel.updateRecentAppLockState();
        if (mIsClearBtnLongPress) {
			mClearNotice.setVisibility(View.GONE);
		}
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        dismissAndGoBack();
    }

    public void dismissAndGoHome() {
        if (mRecentsPanel != null) {
            Intent homeIntent = new Intent(Intent.ACTION_MAIN, null);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivityAsUser(homeIntent, new UserHandle(UserHandle.USER_CURRENT));
            mRecentsPanel.show(false);
        }
    }

    public void dismissAndGoBack() {
        if (mRecentsPanel != null) {
            final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

            final List<ActivityManager.RecentTaskInfo> recentTasks =
                    am.getRecentTasks(2, ActivityManager.RECENT_IGNORE_UNAVAILABLE
                            | ActivityManager.RECENT_INCLUDE_PROFILES);
/*            final List<ActivityManager.RecentTaskInfo> recentTasks =
                    am.getRecentTasks(2,
                            ActivityManager.RECENT_WITH_EXCLUDED |
                            ActivityManager.RECENT_IGNORE_UNAVAILABLE |
                            ActivityManager.RECENT_INCLUDE_PROFILES);*/
            if (recentTasks.size() > 1 &&
                    mRecentsPanel.simulateClick(recentTasks.get(1).persistentId)) {
                // recents panel will take care of calling show(false) through simulateClick
                finish();
                return;
            }
            mRecentsPanel.show(false);
        }
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        mAm = ( ActivityManager ) mContext.getApplicationContext().getSystemService(
                Context.ACTIVITY_SERVICE);
        getWindow().addPrivateFlags(
                WindowManager.LayoutParams.PRIVATE_FLAG_INHERIT_TRANSLUCENT_DECOR);
        setContentView(R.layout.status_bar_recent_panel);
        mRecentsPanel = (RecentsPanelView) findViewById(R.id.recents_root);
        mRecentsPanel.setOnTouchListener(new TouchOutsideListener(mRecentsPanel));
        mRecentReleaseLayout = (RelativeLayout) mRecentsPanel.findViewById(R.id.recent_release_layout);
        mRecentAppClearView = (ImageView) mRecentsPanel.findViewById(R.id.recent_app_clear);
        mGnScanView = (GnScanView) mRecentsPanel.findViewById(R.id.gn_san_view);
        mMemoryInfo = (TextView) mRecentsPanel.findViewById(R.id.memory_status);
        mClearNotice = (TextView) mRecentsPanel.findViewById(R.id.recent_notice);
        /*mRecentsPanel.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);*/
        mRecentsPanel.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        mStatusBar = ((SystemUIApplication) getApplication())
                .getComponent(PhoneStatusBar.class);

        final RecentTasksLoader recentTasksLoader = RecentTasksLoader.getInstance(this);
        recentTasksLoader.setRecentsPanel(mRecentsPanel, mRecentsPanel);
        mRecentsPanel.setMinSwipeAlpha(
                getResources().getInteger(R.integer.config_recent_item_min_alpha) / 100f);

        if (savedInstanceState == null ||
                savedInstanceState.getBoolean(WAS_SHOWING)) {
            handleIntent(getIntent(), (savedInstanceState == null));
        }
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(CLOSE_RECENTS_INTENT);
        mIntentFilter.addAction(WINDOW_ANIMATION_START_INTENT);
        registerReceiver(mIntentReceiver, mIntentFilter);
        
        LayoutParams lp = (LayoutParams) mRecentReleaseLayout.getLayoutParams();
        if (hasNavigationBar()) {
            lp.setMargins(0, 0, 0, 0);
        } else {
            int navigationBarH = mContext.getResources().getDimensionPixelSize(
                    com.android.internal.R.dimen.navigation_bar_height);
            lp.setMargins(0, 0, 0, navigationBarH);
        }
        mRecentReleaseLayout.setLayoutParams(lp);
        
        mRecentAppClearView.setOnClickListener(this);
        mRecentAppClearView.setOnLongClickListener(this);
        
        mMemoryUsed = getMemoryAvailable();
        SwipeHelper.setRecentsAcitivtyContext(this);
        super.onCreate(savedInstanceState);
    }

    private boolean hasNavigationBar() {
        boolean hasNavigationBar = true;
        try {
            IWindowManager mWindowManagerService = WindowManagerGlobal.getWindowManagerService();
            hasNavigationBar = mWindowManagerService.hasNavigationBar();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return hasNavigationBar;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(WAS_SHOWING, mRecentsPanel.isShowing());
    }

    @Override
    protected void onDestroy() {
        mHandler.removeMessages(MSG_STOP_CLEAR);
        RecentTasksLoader.getInstance(this).setRecentsPanel(null, mRecentsPanel);
        unregisterReceiver(mIntentReceiver);
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent, true);
    }

    private void handleIntent(Intent intent, boolean checkWaitingForAnimationParam) {
        super.onNewIntent(intent);

        if (TOGGLE_RECENTS_INTENT.equals(intent.getAction())) {
            if (mRecentsPanel != null) {
                if (mRecentsPanel.isShowing()) {
                    dismissAndGoBack();
                } else {
                    final RecentTasksLoader recentTasksLoader = RecentTasksLoader.getInstance(this);
                    boolean waitingForWindowAnimation = checkWaitingForAnimationParam &&
                            intent.getBooleanExtra(WAITING_FOR_WINDOW_ANIMATION_PARAM, false);
                    Log.d(TAG, " TOGGLE_RECENTS_INTENT and recentsPanel is not showing,so call RecentsPanel.show(true,**)");
                    mRecentsPanel.show(true, recentTasksLoader.getLoadedTasks(),
                            recentTasksLoader.isFirstScreenful(), waitingForWindowAnimation);
                }
            }
        }
    }

    boolean isForeground() {
        return mForeground;
    }

    boolean isActivityShowing() {
         return mShowing;
    }

	@Override
	public boolean onLongClick(View v) {
		Intent whiteListIntent = new Intent(WHITE_LIST_MANAGER);
		whiteListIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		try {
			finish();
			startActivity(whiteListIntent);
		} catch (ActivityNotFoundException e) {
			Log.d(TAG," Oops, activity of com.gionee.softmanager.action.WHITE_LIST_MANAGER not found");
		}
		mIsClearBtnLongPress = true;
		if (mIsClearBtnLongPress) {
			mClearNotice.setVisibility(View.GONE);
		}
		return true;
	}

    @Override
    public void onClick(View v) {
    	if(isPlayMusic(mContext)) {
    		musicApps = getMusicApps(mContext);
    	}
        isClearing = true;
        mStopTime = 10;
        mMemoryUsed = getMemoryAvailable();
        mHandler.removeMessages(MSG_SCAN);
        mHandler.sendEmptyMessage(MSG_START_CLEAR);
        mRecentsPanel.clearRecentApps();
    }

	private Handler mHandler = new Handler() {

		@Override
        public void handleMessage(Message msg) {
		    
            switch (msg.what) {
                case MSG_SCAN:
                    scanView(msg.what);
                    break;
                case MSG_START_CLEAR:
                    startScanView();
                    break;
                case MSG_STOP_CLEAR:
                    stopScanView();
                    break;
                default:
                    break;
            }
        }

        private void scanView(int what) {
            int currentAngel = mGnScanView.getAngle();
            int angel = currentAngel;
            Log.d(TAG, "angel = " + angel + " mAngle = " + mAngle);
            
            if (Math.abs(currentAngel + mAngle) < 5) {
                mHandler.removeMessages(what);
                return;
            } else if (currentAngel > -mAngle) {
                angel -= 5;
            } else {
                angel += 5;
            }
            
            mGnScanView.setAngle(angel);
            
            mHandler.removeMessages(what);
            mHandler.sendEmptyMessageDelayed(what, 10);
        }
        
        private void startScanView() {
            int currentstartAngle = mGnScanView.getStartAngle();
            int startAngle = currentstartAngle % 360 + 15;
            
            mGnScanView.setAngle(startAngle, mAngle);
            
            mHandler.removeMessages(MSG_START_CLEAR);
            mHandler.sendEmptyMessageDelayed(MSG_START_CLEAR, 10);
        }
        
        private void stopScanView() {
            int currentstartAngle = mGnScanView.getStartAngle() + 10;
            int startAngle = currentstartAngle % 360;
            
            Log.d(TAG, "mAngle = " + mAngle + "  mPreAngle = " + mPreAngle);
            if (mPreAngle > mAngle) {
                mPreAngle -= 5;
            }
            mGnScanView.setAngle(startAngle, mPreAngle);
            
            if (mStopTime < 30) {
                mStopTime += 1;
            }
            
            int stopAngle = 0;
            if (mAngle > GnScanView.START_ANGLE) {
                stopAngle = mAngle;
            } else {
                stopAngle = GnScanView.START_ANGLE - mAngle;
            }
            
            Log.d(TAG, "startAngle = " + startAngle + " stopAngle = " + stopAngle + " mStopTime = " + mStopTime);
            
            if (Math.abs(startAngle - stopAngle) <= 5 && mStopTime >= 30) {
                mHandler.removeMessages(MSG_STOP_CLEAR);
                
                String memoryInfo = String.format(
                        mContext.getResources().getString(R.string.gn_memory_available), 
                        formatMemory(getMemoryAvailable()),
                        formatMemory(getPhoneRamMemory()));
                mMemoryInfo.setText(memoryInfo);

                long memorySavedSize = (getMemoryAvailable() > mMemoryUsed) ? (getMemoryAvailable() - mMemoryUsed)
                        : (mMemoryUsed - getMemoryAvailable());
                String memorySaved = String.format(
                        mContext.getResources().getString(R.string.gn_memory_saved),
                        formatMemory(memorySavedSize));
                Toast.makeText(mContext, memorySaved, Toast.LENGTH_SHORT).show();
                
                finish();
                isClearing = false;
                return;
            }
            
            mHandler.removeMessages(MSG_STOP_CLEAR);
            mHandler.sendEmptyMessageDelayed(MSG_STOP_CLEAR, mStopTime);
        }
        
	};
	
	public void updateMemoryInfo() {
	    if (isClearing) {
	        return;
	    }
	    
        String memoryInfo = String.format(
        		mContext.getResources().getString(R.string.gn_memory_available), 
                formatMemory(getMemoryAvailable()),
                formatMemory(getPhoneRamMemory()));
        mMemoryInfo.setText(memoryInfo);
        
        mAngle = calculateAngle();
        mHandler.sendEmptyMessage(MSG_SCAN);
    }
	
	public void showMemorySaved() {
	    mPreAngle = mAngle;
        mAngle = calculateAngle();
        
        mHandler.removeMessages(MSG_START_CLEAR);
        mHandler.sendEmptyMessage(MSG_STOP_CLEAR);
	}

    private int calculateAngle() {
        mMemPercent = (float)(getPhoneRamMemory() - getMemoryAvailable()) / (float)getPhoneRamMemory();
        return (int) (360 * mMemPercent);
    }
	
	private long getMemoryAvailable() {
		mAm.getMemoryInfo(mMemInfo);
        return mMemInfo.availMem;
	}
	
	public String formatMemory(long size) {
/*        long sizeM = size / 1024 / 1024;

        if (((float)sizeM / 1024.0f) - 1.1 >= 0.0) {
            float sizeG =  ((float)sizeM / 1024.0f);
            return String.format("%.1fG", sizeG);
        }

        return "" + sizeM + "M";*/
		return Formatter.formatFileSize(mContext, size);
    }
	
	public long getPhoneRamMemory() {
        MemInfoReader memInfoReader = new MemInfoReader();
        memInfoReader.readMemInfo();
        long totalSize = memInfoReader.getTotalSize();
        totalSize = translateCapacity(totalSize);
		
        return totalSize;
    }
	
    private long translateCapacity(long capacity) {
        long result = capacity;
        if (capacity < 67108864L) {
            result = 67108864L;
        } else if (capacity < 134217728L) {
            result = 134217728L;
        } else if (capacity < 268435456L) {
            result = 268435456L;
        } else if (capacity < 536870912L) {
            result = 536870912L;
        } else if (capacity < 1073741824L) {
            result = 1073741824L;
        } else if (capacity < 1610612736L) {
			result = 1610612736L;
        } else if (capacity < 2147483648L) {
            result = 2147483648L;
        } else if (capacity < 3221225472L) {
            result = 3221225472L;
        } else if (capacity < 4294967296L) {
            result = 4294967296L;
        } else if (capacity < 8589934592L) {
            result = 8589934592L;
        } else if (capacity < 17179869184L) {
            result = 17179869184L;
        } else if (capacity < 32000000000L) {
            result = 34359738368L;
        }
        return result;
    }
    
    private void getProcessWhiteList() {
    	Cursor cursor = null;
    	try {
			cursor = getContentResolver().query(Uri.parse(PROCESS_WHITE_LIST), null, null, null, null);
    		if (cursor != null && mRecentsPanel != null) {
    			mRecentsPanel.clearLockApp();
    			while(cursor.moveToNext()) {
    				mRecentsPanel.saveLockAppName(cursor.getString(0));
    			}
    			mRecentsPanel.commitWhiteList();
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.d(TAG, " Exception = " + e);
		} finally {
			if(cursor != null) {
				cursor.close();
			}
		}
	}
    
    public void onScreenPinningRequest() {
        if (mStatusBar != null) {
            mStatusBar.showScreenPinningRequest(false);
        }
	}

	private static boolean isPlayMusic(Context context) {
		AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		return am.isMusicActive();
	}

	private static List<String> getMusicApps(Context context) {
		List<String> ls = new ArrayList<String>();
		try {
			Intent intent = new Intent("android.intent.action.VIEW");
			intent.setDataAndType(Uri.parse("file:///android_asset/gionee"), "audio/mpeg");
			List<ResolveInfo> riLists = context.getPackageManager().queryIntentActivities(
							intent,
							PackageManager.MATCH_DEFAULT_ONLY | PackageManager.GET_RESOLVED_FILTER);
			for (ResolveInfo ri : riLists) {
				if (ri != null && ri.activityInfo != null) {
					ls.add(ri.activityInfo.packageName);
				}
			}
			Log.i(TAG, "music app -----> " + ls.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ls;
	}

	public List<String> getMusicAppsList() {
		return musicApps;
	}
}
