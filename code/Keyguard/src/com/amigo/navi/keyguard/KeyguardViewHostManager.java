package com.amigo.navi.keyguard;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;
import android.widget.FrameLayout.LayoutParams;

import com.amigo.navi.keyguard.AmigoKeyguardBouncer.KeyguardBouncerCallback;
import com.amigo.navi.keyguard.haokan.Common;
import com.amigo.navi.keyguard.haokan.KeyguardDataModelInit;
import com.amigo.navi.keyguard.haokan.KeyguardWallpaperContainer;
import com.amigo.navi.keyguard.haokan.RequestNicePicturesFromInternet;
import com.amigo.navi.keyguard.haokan.ShutdownBroadcastReceiver;
import com.amigo.navi.keyguard.haokan.RequestNicePicturesFromInternet.DataChangedInterface;
import com.amigo.navi.keyguard.haokan.UIController;
import com.amigo.navi.keyguard.haokan.analysis.HKAgent;
import com.amigo.navi.keyguard.haokan.db.WallpaperDB;
import com.amigo.navi.keyguard.haokan.entity.Wallpaper;
import com.amigo.navi.keyguard.haokan.entity.WallpaperList;
import com.amigo.navi.keyguard.haokan.menu.ArcLayout;
import com.amigo.navi.keyguard.network.ImageLoader;
import com.amigo.navi.keyguard.network.theardpool.ThreadUtil;
import com.amigo.navi.keyguard.picturepage.adapter.HorizontalAdapter;
import com.amigo.navi.keyguard.picturepage.widget.KeyguardListView;
import com.amigo.navi.keyguard.picturepage.widget.HorizontalListView.OnScrollListener;
import com.amigo.navi.keyguard.picturepage.widget.OnViewTouchListener;
import com.amigo.navi.keyguard.fingerprint.FingerIdentifyManager;
import com.amigo.navi.keyguard.skylight.SkylightActivity;
import com.amigo.navi.keyguard.skylight.SkylightHost;
import com.amigo.navi.keyguard.skylight.SkylightUtil;
import com.amigo.navi.keyguard.util.AmigoKeyguardUtils;
import com.amigo.navi.keyguard.util.DataStatistics;
import com.amigo.navi.keyguard.util.QuickSleepUtil;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.R;
import com.android.keyguard.ViewMediatorCallback;
import com.android.keyguard.KeyguardHostView.OnDismissAction;
import com.android.internal.widget.LockPatternUtils;
import com.gionee.fingerprint.IGnIdentifyCallback;
import android.os.SystemProperties;
import android.provider.Settings;

public class KeyguardViewHostManager {
    private static final String TAG = "KeyguardViewHostManager";
    final static boolean DEBUG=true;
    private static final String LOG_TAG="KeyguardViewHostManager";
    
    private static final int MSG_HIDE_SKYLIGHT=0;
    private static final int MSG_SHOW_SKYLIGHT=1;
    private static final int MSG_CONFIGURATION_CHANGED = 2;
    private static final int MSG_UPDATE_HAOKAN_LIST = 3;
    private static final int MSG_UPDATE_HAOKAN_LIST_SCREEN_ON = 4;
    
    private static KeyguardViewHostManager sInstance=null;
    
    private Context mContext;
    
    private KeyguardViewHost mKeyguardViewHost;
    private SkylightHost mSkylightHost;
    
    private ViewMediatorCallback mViewMediatorCallback;
    
    private boolean mIsSkylightShown=false;
    private LockPatternUtils mLockPatternUtils;
    private MyHandler mHandler=new MyHandler();
    private ViewHostReceiver mReceiver=new ViewHostReceiver();
    private KeyguardNotificationCallback mKeyguardNotificationCallback;
    private RequestNicePicturesFromInternet mNicePicturesInit;
    private FingerIdentifyManager mFingerIdentifyManager;
    // for statistics
    private long timeOnKeyguard = -1;
    private long timeOnKeyguardStart = -1;
//  private boolean IsClearLock = false;
    private static boolean isSuppotFinger=false;
    private static final int MSG_UPDATE_HAOKAN_LIST_SCREEN_OFF = 4;
    public static final String KEYGUARD_LOCK_BY_OTHERAPP="lockByOtherApp";

	public KeyguardViewHostManager(Context context,KeyguardViewHost host,SkylightHost skylight,LockPatternUtils lockPatternUtils,ViewMediatorCallback callback){
        DebugLog.d(TAG,"KeyguardViewHostManager");
      	initVersionName(context);
        mNicePicturesInit = RequestNicePicturesFromInternet.getInstance(context.getApplicationContext());
        mNicePicturesInit.init();
        KeyguardDataModelInit.getInstance(context).initData();
        mContext=context;
        mKeyguardViewHost=host;
        mSkylightHost=skylight;
        mLockPatternUtils = lockPatternUtils;
        DataStatistics.getInstance().onInit(context.getApplicationContext());
        registerReceivers();
        sInstance=this;
        setViewMediatorCallback(callback);
        initKeyguard(callback);
        Common.setPowerSaverMode(getPowerSaverMode() == 2);
        initHorizontalListView();
        addKeyguardArcMenu();
        mFingerIdentifyManager=new FingerIdentifyManager(context);
        ShutdownBroadcastReceiver.setUpdatePage(mUpdatePage);
        mKeyguardViewHost.setConfigChangeCallback(mConfigChangeCallback);
        isSuppotFinger=SystemProperties.get("ro.gn.fingerprint.support").equals("FPC");
		initPowerSaverObserver();
        Log.i(TAG,"isSuppotFinger....isSuppotFinger="+isSuppotFinger);
        
        Guide.init(context);
    }
    
    KeyguardViewHost.ConfigChangeCallback mConfigChangeCallback = new KeyguardViewHost.ConfigChangeCallback() {
		
		@Override
		public void onConfigChange() {
	        initHorizontalListView();
	        addKeyguardArcMenu();
		}
	};
    
    ShutdownBroadcastReceiver.UpdatePage mUpdatePage = new ShutdownBroadcastReceiver.UpdatePage() {
		
		@Override
		public void update() {
//			savePage(true);
		}
	};
    
	private void initVersionName(Context context) {
		try{
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
//            NavilSettings.sVersionName = packInfo.versionName;
        	DebugLog.d(TAG, "KeyguardViewHostManager packInfo.versionName:" + packInfo.versionName);
            Common.setVersionName(packInfo.versionName);
        } catch(Exception e){
        	DebugLog.d(TAG, "KeyguardViewHostManager error:" + e);
        }
	}
    
    private void initThreadUtil() {
        mThreadUtil = new ThreadUtil();
    }

    
    public static KeyguardViewHostManager getInstance(){
    	return sInstance;
    }
    public void initKeyguard(ViewMediatorCallback callback){
        mKeyguardViewHost.initKeyguard(callback, mLockPatternUtils);
        amigoInitUnlockReceiver(callback);
        
    }
    
    public void initKeyguardReset(){
    	mKeyguardViewHost.initKeyguard(mViewMediatorCallback, mLockPatternUtils);
    }
    
    private void registerReceivers(){
        IntentFilter filter=new IntentFilter();
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        mContext.registerReceiver(mReceiver, filter);
    }
    

	private void beginStatics() {
		if (mViewMediatorCallback.isScreenOn() && !mIsSkylightShown && mViewMediatorCallback.isShowing()){
     	   timeOnKeyguardStart = SystemClock.elapsedRealtime() ;
     	}
	}

	private void finishStatistics() {
		if (timeOnKeyguardStart<0){
			return;
		}
		if (!mViewMediatorCallback.isScreenOn() || mIsSkylightShown || !mViewMediatorCallback.isShowing()){
    		timeOnKeyguard = SystemClock.elapsedRealtime() - timeOnKeyguardStart;
            HKAgent.onEventTimeOnKeyguard(mContext, (int) timeOnKeyguard);
            timeOnKeyguardStart = timeOnKeyguard = -1;
      	}
	}
	
    public void show(Bundle options){
//        initSkylightHost();
        mKeyguardViewHost.show(options);
        updateNotifiOnkeyguard(true);
        beginStatics();
		UIController.getInstance().onKeyguardLocked();
        mFingerIdentifyManager.readFingerprintSwitchValue();
        
    }
    
    public void showBouncerOrKeyguard(){
    	mKeyguardViewHost.showBouncerOrKeyguard();
    }
    	 
    
    
    public void hide() {
        DataStatistics.getInstance().unlockScreenWhenHasNotification(mContext);
        destroyAcivityIfNeed();
        mKeyguardViewHost.hide();
        setSkylightHidden();
    	
        finishStatistics();
        updateNotifiOnkeyguard(false);
        releaseCache();
        mContainer.reset();
    }

	private void releaseCache() {
		if(mImageLoader != null){
        	mImageLoader.clearCache();
        }
	}
    
    public void onScreenTurnedOff(){
        mKeyguardViewHost.onScreenTurnedOff();
        finishStatistics();
        savePage(false);
        updateHorizontalListViewWhenScreenChanged();
        releaseCache();
        cancelFingerIdentify();
    }
    
    public void onScreenTurnedOn(){
        mKeyguardViewHost.onScreenTurnedOn();
        Wallpaper wallpaper= UIController.getInstance().getmCurrentWallpaper();
        if (wallpaper != null){
        	HKAgent.onEventScreenOn(mContext, UIController.getInstance().getmCurrentWallpaper());
        	HKAgent.onEventIMGShow(mContext, UIController.getInstance().getmCurrentWallpaper());
        }
        beginStatics();
        startFingerIdentify();
    }
    
    
	private void refreshKeyguardListView() {
		if(mWallpaperAdapter != null){
    		mWallpaperAdapter.notifyDataSetChanged();
    	}
	}
    public boolean isShowing(){
        if(mViewMediatorCallback != null){
            return mViewMediatorCallback.isShowing();
        }
        return false;
    }
    
    public boolean isShowingAndNotOccluded(){
        if(mViewMediatorCallback != null){
            return mViewMediatorCallback.isShowingAndNotOccluded();
        }
        return false;
    }
    
    public boolean isSecure(){
    	return mKeyguardViewHost.isSecure();
    }
    public boolean isScreenOn(){
        return mViewMediatorCallback.isScreenOn();
    }
    
    public void dismissWithDismissAction(OnDismissAction r){
        mKeyguardViewHost.dismissWithAction(r);
    }
    
    public void dismissWithDismissAction(OnDismissAction r,boolean afterKeyguardGone){
    	mKeyguardNotificationCallback.dismissWithAction(r,afterKeyguardGone);
    }
    
    public void dismiss(){
        mKeyguardViewHost.dismiss();
    }
    
    public void finishIfNoSecure(){
        mKeyguardViewHost.finishIfNoSecure();
    }
    
    public void verifyUnlock(){
        show(null);
//    	dismiss();
    }
    
    private void updateNotifiOnkeyguard(boolean isShow){
        if(isShow){
            KeyguardUpdateMonitor.getInstance(mContext).getNotificationModule().setLastKeyguardShowTime(System.currentTimeMillis());
            KeyguardUpdateMonitor.getInstance(mContext).getNotificationModule().updateNotifications();
        }else{
            KeyguardUpdateMonitor.getInstance(mContext).getNotificationModule().removeAllNotifications();
        }
    }
    
    public void startFingerIdentify(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mFingerIdentifyManager.startIdentifyIfNeed();
            }
        });
    }
    
    public void cancelFingerIdentify() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mFingerIdentifyManager.cancel();
            }
        });
    }
    
    public void setViewMediatorCallback(ViewMediatorCallback callback){
        mViewMediatorCallback=callback;
    }
    
    
    public boolean needsFullscreenBouncer(){
        return mKeyguardViewHost.needsFullscreenBouncer();
    }
    
    public void showBouncer(boolean resetSecuritySelection){
        mKeyguardViewHost.showBouncer(resetSecuritySelection);
    }
    
    
    
    private void setIsSkylightShown(boolean isShown){
        mIsSkylightShown=isShown;
    }
    public boolean getIsSkylightShown(){
        return mIsSkylightShown;
    }
    
    public void showSkylight(){
        mHandler.sendEmptyMessage(MSG_SHOW_SKYLIGHT);
    }

    private void handleShowSkylight() {

        if (SkylightUtil.getIsHallOpen(mContext)) {
            return;
        }
        
        cancelFingerIdentify();
        if(DEBUG){Log.d(LOG_TAG, "showSkylight  skylight is null? " + (mSkylightHost == null));}
        mViewMediatorCallback.userActivity();
        if (mSkylightHost != null) {
            mSkylightHost.showSkylight();
            mSkylightHost.setVisibility(View.VISIBLE);
            mKeyguardViewHost.resetHostYToHomePosition();
            startActivityIfNeed();
            mIsSkylightShown=true;
            mViewMediatorCallback.adjustStatusBarLocked();
        }
        
        finishStatistics();
    }
    public void hideSkylight(boolean isGotoUnlock) {
        Message msg=mHandler.obtainMessage(MSG_HIDE_SKYLIGHT, isGotoUnlock);
        mHandler.sendMessage(msg);
     
    }
    private void handleHideSkylight(boolean forceHide){
        boolean isOpen=SkylightUtil.getIsHallOpen(mContext);
        if(!isOpen&&!forceHide){
            return;
        }
        mViewMediatorCallback.userActivity();
        destroyAcivityIfNeed();
        if (mSkylightHost != null) {
            mSkylightHost.hideSkylight();
            boolean isLockScreenDisabled=mLockPatternUtils.isLockScreenDisabled();
            boolean isSimLock=needsFullscreenBouncer();
            DebugLog.d(LOG_TAG, "handleHideSkylight  isLockScreenDisable: "+isLockScreenDisabled);
            if (isLockScreenDisabled&&isOpen&&!isSimLock) {
                unLockByOther(false);
            }else{
                mSkylightHost.setVisibility(View.GONE);
            }
            mIsSkylightShown=false;
            mViewMediatorCallback.adjustStatusBarLocked();
        }
       startFingerIdentify();
       beginStatics();
    }
    
    private void setSkylightHidden(){
        mSkylightHost.setVisibility(View.GONE);
        
    }
    
    
    public void unLockByOther(boolean animation){
        mKeyguardViewHost.unLockByOther(animation);
    }


    /**
     * start an empty activity to pause a running activity(eg. video or game) in activitytask when close skylight
     */
    private List<SkylightActivity> mActivitys=new ArrayList<SkylightActivity>();
    
    protected void startActivityIfNeed() {
        /**
         * if alarm boot , do not start activity 
         */
        if (mViewMediatorCallback.isShowingAndNotOccluded()&&!AmigoKeyguardUtils.isAlarmBoot()) {
            if (mActivitys.size() == 0) {
                Intent intent = new Intent(mContext, SkylightActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    Bundle opts = ActivityOptions.makeCustomAnimation(mContext, 0, 0).toBundle();
                    mContext.startActivity(intent, opts);
                } else {
                    mContext.startActivity(intent);
                }
            }
        }
    }
    
    public void destroyAcivityIfNeed() {
        for (int i = 0; i < mActivitys.size(); i++) {
            DebugLog.d(LOG_TAG, "destroyAcivityIfNeed");
            SkylightActivity activity=mActivitys.get(i);
            if(activity!=null){
                activity.finish();
                activity.overridePendingTransition(0, 0);
            }
        }
        mActivitys.clear();
    }
    
    public void notifySkylightActivityCreated(SkylightActivity activity){
        mActivitys.add(activity);
    }
    

    
    
    public void handleConfigurationChanged() {
    	if(mKeyguardViewHost != null){
    		mKeyguardViewHost.onConfigurationChanged();
    	}
        if(mSkylightHost!=null){
            mSkylightHost.onConfigurationChanged();
        }
    }
    
    
    
    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_HIDE_SKYLIGHT:
                boolean isUnlock = (Boolean) msg.obj;
                handleHideSkylight(isUnlock);
                break;
            case MSG_SHOW_SKYLIGHT:
                handleShowSkylight();
                break;

            case MSG_CONFIGURATION_CHANGED:
                handleConfigurationChanged();
                break;
            case MSG_UPDATE_HAOKAN_LIST:
                DebugLog.d(TAG,"update listview mHandler");
                WallpaperList wallpaperList = (WallpaperList) msg.obj;
                refreshHorizontalListView(wallpaperList); 
                break;
            case MSG_UPDATE_HAOKAN_LIST_SCREEN_OFF:
                WallpaperList wallpapers = (WallpaperList) msg.obj;
                if(wallpapers.size() == 0){
                    mKeyguardListView.setVisibility(View.GONE);
                    mContainer.setVisibility(View.GONE);
                    UIController.getInstance().getHaoKanLayout().setVisibility(View.GONE);
                    mViewMediatorCallback.setKeyguardWallpaperShow(true);
                }else{
                    refreshHorizontalListView(wallpapers); 
                    mKeyguardListView.setPosition(mShowPage);
                    UIController.getInstance().refreshWallpaperInfo();
                }
                break;
            default:
                break;

            }
        }
    }

    private void updateHorizontalListLoopState(WallpaperList wallpaperList) {
        if(wallpaperList.size() <= 1){
            mKeyguardListView.setCanLoop(false);
        }else{
            mKeyguardListView.setCanLoop(true);
        }
    }
    
    private class ViewHostReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            DebugLog.d(LOG_TAG, "onReceive action: "+action);
            if(Intent.ACTION_CONFIGURATION_CHANGED.equals(action)){
                mHandler.sendEmptyMessage(MSG_CONFIGURATION_CHANGED);
            }
        }
        
    }
    
    public void scrollToKeyguardPageByAnimation(){
    	if(mKeyguardViewHost != null){
    		mKeyguardViewHost.scrollToKeyguardPageByAnimation();
    	}
    		
    }
    
    public void scrollToUnlockHeightByOther(boolean withAnim){
    	if(mKeyguardViewHost != null){
    		mKeyguardViewHost.scrollToUnlockHeightByOther(withAnim);
    	}
    }
    
    public boolean isAmigoHostYAtHomePostion(){
    	if(mKeyguardViewHost != null){
    	   return mKeyguardViewHost.isAmigoHostYAtHomePostion();
    	}
    	return false;
    }
    
    public void unLockBySensor(){
    	if(isShowing() && SkylightUtil.getIsHallOpen(mContext) && isAmigoHostYAtHomePostion()){
    		scrollToUnlockHeightByOther(true);
    	}
    }

    public long getUserActivityTimeout(){
    	return mKeyguardViewHost.getUserActivityTimeout();
    }
    
    public boolean keyguardBouncerIsShowing(){
    	return mKeyguardViewHost.keyguardBouncerIsShowing();
    }
    
    public void registerBouncerCallback(KeyguardBouncerCallback bouncerCallback){
    	mKeyguardViewHost.registerBouncerCallback(bouncerCallback);
    }
    
    public void registerKeyguardNotificationCallback(KeyguardNotificationCallback notificationCallback){
    	mKeyguardNotificationCallback=notificationCallback;
    }
    
    public interface KeyguardNotificationCallback {
    	void dismissWithAction(OnDismissAction r, boolean afterKeyguardGone);
    }
    
    public void dismissWithAction(OnDismissAction r, boolean afterKeyguardGone){
    	if(mKeyguardNotificationCallback!=null){
    		mKeyguardNotificationCallback.dismissWithAction(r,afterKeyguardGone);
    	}
    }
    
    public boolean  onBackPress(){
    	if(mKeyguardViewHost!=null){
    		return mKeyguardViewHost.onBackPress();
    	}
    	return false;
    }
    
    public void updateSKylightLocation() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mSkylightHost.updateSkylightLocation();
            }
        });
    }
    
    private ImageLoader mImageLoader = null;
    private KeyguardListView mKeyguardListView;
    private HorizontalAdapter mWallpaperAdapter;
    protected int mShowPage;
	private KeyguardWallpaperContainer mContainer = null;
    private void initHorizontalListView(){
        DebugLog.d(TAG,"initHorizontalListView wallpaperList");
      mKeyguardViewHost.setOnViewTouchListener(mViewTouchListener);
      if(mContainer != null){
          mKeyguardViewHost.addView(mContainer, 0);
          mContainer.reset();
          return;
      }
      mContainer  = new KeyguardWallpaperContainer(mContext.getApplicationContext());
      mKeyguardListView = new KeyguardListView(mContext.getApplicationContext());
      mImageLoader = new ImageLoader(mContext.getApplicationContext());
      WallpaperList wallpaperList = new WallpaperList();
      mWallpaperAdapter = new HorizontalAdapter(mContext.getApplicationContext(), wallpaperList,mImageLoader);
      UIController.getInstance().setmKeyguardListView(mKeyguardListView);
      mKeyguardListView.setAdapter(mWallpaperAdapter);
      updateDataAndRefreshKeyguardListView(true);
      mKeyguardListView.setOnScrollListener(mKeyguardListViewScrollListener);
      mContainer.addView(mKeyguardListView, 0);
      mKeyguardViewHost.addView(mContainer, 0);
//      mKeyguardViewHost.addView(mKeyguardListView, 0);
      RequestNicePicturesFromInternet.getInstance(mContext).setDataChangedListener(mDataChangedListener);
      UIController controller = UIController.getInstance();
      mKeyguardListView.setTouchlListener(controller);
      
      if (Common.isPowerSaverMode()) {
          mKeyguardListView.setVisibility(View.GONE);
          mContainer.setVisibility(View.GONE);
          mViewMediatorCallback.setKeyguardWallpaperShow(true);
      }
    }
    
    private void addKeyguardArcMenu() {

        
        RelativeLayout keyguardArcMenu = new RelativeLayout(mContext);
        ArcLayout arcLayout = new ArcLayout(mContext);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        keyguardArcMenu.addView(arcLayout, params);
        
        params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        mKeyguardViewHost.addView(keyguardArcMenu, params);
        keyguardArcMenu.setVisibility(View.GONE);
        keyguardArcMenu.setBackgroundResource(R.color.haokan_arc_menu_background);
        UIController.getInstance().setmArcMenu(keyguardArcMenu);
        
        
    }
    
    
    
    
    public void showBouncerOrKeyguardDone(){
        if ( mKeyguardViewHost!=null) {
            mKeyguardViewHost.showBouncerOrKeyguardDone(); 
           }
    }
    public void shakeFingerIdentifyTip(){
        mKeyguardViewHost.shakeFingerIdentifyTip();
    }
    public void unlockByFingerIdentify(){
        mKeyguardViewHost.unlockByFingerIdentify();
    }
    private void updateHorizontalListView(){
        boolean isLock = WallpaperDB.getInstance(mContext).queryHasLockPaper();
//        if(isLock){
            WallpaperList wallpaperList = queryWallpaperList();
            querySelectionPageWhenLock(wallpaperList);
            Message msg = mHandler.obtainMessage(MSG_UPDATE_HAOKAN_LIST);
            msg.obj = wallpaperList;
            mHandler.sendMessage(msg);
//        }else{
//            
//        }
    }
    
    private void updateDataAndRefreshKeyguardListView(final boolean isInit){
        DebugLog.d(TAG,"update listview updateDataAndRefreshKeyguardListView");
            new Thread(new Runnable() {
                
                @Override
                public void run() {
                    DebugLog.d(TAG,"updateDataAndRefreshKeyguardListView run");
                    mShowPage = 0;
                    WallpaperDB wallpaperDB = WallpaperDB.getInstance(mContext);
                    WallpaperList wallpaperList = queryWallpaperList();
                    DebugLog.d(TAG,"updateDataAndRefreshKeyguardListView1");
                    DebugLog.d(TAG,"updateDataAndRefreshKeyguardListView size:" + wallpaperList.size());
                    boolean isLock = wallpaperDB.queryHasLockPaper();
                    refreshPageWhenLockOrNot(wallpaperDB, isLock, wallpaperList);
                    Message msg = mHandler.obtainMessage(MSG_UPDATE_HAOKAN_LIST_SCREEN_OFF);
                    msg.obj = wallpaperList;
                    mHandler.sendMessage(msg);
                }

				private void refreshPageWhenLockOrNot(WallpaperDB wallpaperDB,
						boolean isLock, WallpaperList wallpaperList) {
					if(isLock){
					    int page = querySelectionPageWhenLock(wallpaperList);
		                refreshPage(page);
					}else{
					    String currentTime = Common.formatCurrentTime();
					    Wallpaper wallpaper = wallpaperDB.queryDynamicShowWallpaper(currentTime);
					    DebugLog.d(TAG,"updateDataAndRefreshKeyguardListView wallpaper:" + wallpaper);
					    if(wallpaper != null){
					        querySelectionPageWhenNotLock(wallpaperList,wallpaper);
					    }
					}
				}
            }).start();
    }

    private synchronized  void updateDynamicWallpaperOnLocalData(WallpaperList wallpaperList){
    	String time = Common.formatCurrentTime();
    	for(int index = 0;index < wallpaperList.size();index++){
    		String beginTime = wallpaperList.get(index).getShowTimeBegin();
    		String endTime = wallpaperList.get(index).getShowTimeEnd();
    		if(time.compareTo(beginTime) >= 0 && time.compareTo(endTime) < 0){
    			refreshPage(index);
    			break;
    		}
    	}
    }
    private void querySelectionPageWhenNotLock(WallpaperList wallpaperList,Wallpaper wallpaper) {
    	DebugLog.d(TAG,"querySelectionPageWhenNotLock not lock:" + wallpaper.getImgContent());
    	DebugLog.d(TAG,"querySelectionPageWhenNotLock not lock:" + wallpaper.getImgId());
    	for(int index = 0;index < wallpaperList.size();index++){
            if(wallpaperList.get(index).getImgId() == wallpaper.getImgId()){
                refreshPage(index);
            	DebugLog.d(TAG,"querySelectionPageWhenNotLock mShowPage:" + mShowPage);
                break;
            }
        }
    }
    
    /**
     * @param wallpaperList
     */
    private int querySelectionPageWhenLock(WallpaperList wallpaperList) {
        DebugLog.d(TAG,"updateSelectionPage 1");
        int page = 0;
        for(int index = 0;index < wallpaperList.size();index++){
            if(wallpaperList.get(index).isLocked()){
                page = index;
                DebugLog.d(TAG,"updateSelectionPage 2");
                break;
            }
        }
        return page;
    }
    
    private void updateHorizontalListViewWhenScreenChanged(){
        DebugLog.d(TAG,"updateHorizontalListViewWhenScreenChanged");
        if(mKeyguardListView == null){
            return;
        }
        updateDataAndRefreshKeyguardListView(false);
    }
    
    /**
     * @return
     */
    private WallpaperList queryWallpaperList() {
        WallpaperList wallpaperList = WallpaperDB.getInstance(mContext.getApplicationContext())
                .queryPicturesDownLoaded();   
        return wallpaperList;
    }
    
    
      RequestNicePicturesFromInternet.DataChangedInterface mDataChangedListener = new DataChangedInterface() {
        
        @Override
        public void onDataChanged(final String url,final Bitmap bitmap) {
//            mImageLoader.addImage2Cache(url, bitmap);
//            mThreadUtil.runOnWorkThreadOnlyOne(mUpdateHorizontalListViewRunnable);
        }
    };
    
    private static final boolean PRINT_LOG = false;
    private ThreadUtil mThreadUtil = null;
    OnScrollListener mKeyguardListViewScrollListener = new OnScrollListener() {
        @Override
        public void onScrollMoving(int motionX) {
            if(PRINT_LOG){
            	DebugLog.d(TAG,"onScrollMoving");
            }
            QuickSleepUtil.updateWallPaperScrollingState(false);
/*            Object obj = mKeyguardListView.getCurrentItem();
            Wallpaper wallpaper = null;
            if(obj != null){
                wallpaper = (Wallpaper) obj;
            	HKAgent.onEventIMGShow(mContext.getApplicationContext(), wallpaper);
            }*/
        }
        
        @Override
        public void onScrollEnd() {
            if(PRINT_LOG){
            	DebugLog.d(TAG,"onScrollEnd");
            }
            mWallpaperAdapter.unlock();
            Wallpaper wallpaper = null;
            Object obj = mKeyguardListView.getCurrentItem();
            if(obj != null){
                wallpaper = (Wallpaper) obj;
        		HKAgent.onEventIMGSwitch(mContext.getApplicationContext(), wallpaper);
            	HKAgent.onEventIMGShow(mContext.getApplicationContext(), wallpaper);
            }
        }
        
        @Override
        public void onScrollBegin() {
            if(PRINT_LOG){
            	DebugLog.d(TAG,"onScrollBegin");
            }
            mWallpaperAdapter.lock();
            QuickSleepUtil.updateWallPaperScrollingState(true);
        }
    };
    
    OnViewTouchListener mViewTouchListener = new OnViewTouchListener() {
        
        @Override
        public void onTouch(MotionEvent event) {
            if(mKeyguardViewHost.isAmigoHostYAtHomePostion()){
                dispatchTouchEventToListView(event);
            }
        }

        @Override
        public void onInterceptTouch(MotionEvent event) {
            if(mKeyguardViewHost.isAmigoHostYAtHomePostion()){
                mKeyguardListView.interceptTouchEvent(event);
            }
        }
    };
    private int mTouchDownY;
    
    
    private void refreshHorizontalListView(WallpaperList wallpaperList) {
        updateHorizontalListLoopState(wallpaperList);
        mWallpaperAdapter.updateDataList(wallpaperList);
        mWallpaperAdapter.notifyDataSetChanged();
    }
    
    private void dispatchTouchEventToListView(MotionEvent event) {
        int motionX = (int) event.getX();
        DebugLog.d(TAG,"motionMove real motionX:" + motionX);
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            mTouchDownY = 0;
            mKeyguardListView.dealwithTouchEvent(event);
          } 
          DebugLog.d(TAG,"motionMove real is beginScroll:" + mKeyguardListView.isBeginScroll((int) event.getX()));
        if(mKeyguardListView.isBeginScroll()){
          if(event.getAction() != MotionEvent.ACTION_UP){
//                int infozoneHeight = mKeyguardViewHost.getKeyguardPage().getInfozoneViewHeight();
                int infozoneHeight = 0;
                int copyWriter = mKeyguardViewHost.getHkCaptionsViewHeight();
                int scollAtHorizontalHeight = KWDataCache.getAllScreenHeigt(mContext) - infozoneHeight
                        - copyWriter;
                DebugLog.d(TAG,"motionMove real mTouchDownY:" + mTouchDownY);
                DebugLog.d(TAG,"motionMove real scollAtHorizontalHeight:" + scollAtHorizontalHeight);
                if(mTouchDownY < scollAtHorizontalHeight){
                    mKeyguardListView.dealwithTouchEvent(event);
                }
          }
        }
          if(event.getAction() == MotionEvent.ACTION_UP){
             mKeyguardListView.dealwithTouchEvent(event);
          }
    }
    
    
    
    public void fingerPrintFailed() {
        mKeyguardViewHost.fingerPrintFailed();
    }

    public void fingerPrintSuccess() {
        mKeyguardViewHost.fingerPrintSuccess();
    }
    
    public boolean passwordViewIsForzen() {
        return mKeyguardViewHost.passwordViewIsForzen();
    }
	
	
	public void setOccluded(boolean occluded){
    	 if (occluded) {   		 
  	        mKeyguardViewHost.setVisibility(View.GONE);      
         } else {
         	mKeyguardViewHost.setVisibility(View.VISIBLE);
         }    	
    }
    
    public void reset(boolean occluded){
        showBouncerOrKeyguard();
		if(occluded){
       		mKeyguardViewHost.setVisibility(View.GONE); 
    	}
   }
    
	    
	        private void savePage(boolean isShutDown){
		            Wallpaper wallpaper = null;
		            Object obj = mKeyguardListView.getCurrentItem();
		            
		            if(obj != null){
		                wallpaper = (Wallpaper) obj;
		            }
		            WallpaperDB wallpaperDB = WallpaperDB.getInstance(mContext.getApplicationContext());
		            Wallpaper lockWallpaper = wallpaperDB.queryPicturesDownLoadedLock();
		            if(lockWallpaper != null && wallpaper != null && lockWallpaper.getImgId() != wallpaper.getImgId()){
		                DebugLog.d(TAG,"onScrollEnd url:" + wallpaper.getImgUrl());
		                float order = wallpaper.getShowOrder();
		                float floorOrder = (float) Math.floor(order);
		                DebugLog.d(TAG,"onScrollEnd floorOrder:" + floorOrder);
		                float showOrder = floorOrder + 0.5f;
		                lockWallpaper.setShowOrder(showOrder);
		                wallpaperDB.updateShowOrder(lockWallpaper);
		            }      
    }
	    
	    public static boolean isSuppotFinger() {
	    	Log.i(TAG,"isSuppotFinger....isSuppotFinger="+isSuppotFinger);
			return isSuppotFinger;
		}

		public static void setSuppotFinger(boolean isSuppotFinger) {
			KeyguardViewHostManager.isSuppotFinger = isSuppotFinger;
		}
		
		private void refreshPage(int index) {
			mShowPage = index;
		}
    
	    private static final String POWERSAVERSETTING = "amigo_powermode";
		private void initPowerSaverObserver() {
			ContentObserver obs = new ContentObserver(mHandler) {
				@Override
				public void onChange(boolean selfChange) {
				    boolean saverMode = getPowerSaverMode() == 2;
				    Common.setPowerSaverMode(saverMode);
				    UIController.getInstance().onChangePowerSaverMode(saverMode);
				    mViewMediatorCallback.setKeyguardWallpaperShow(saverMode);
				}
			};
			final ContentResolver resolver = mContext.getContentResolver();
			resolver.registerContentObserver(
					Settings.Global.getUriFor(POWERSAVERSETTING), false, obs);
		}
		
	    private int getPowerSaverMode() {
	    	return Settings.Global.getInt(mContext.getContentResolver(), POWERSAVERSETTING, 0);
	    }
	    
	    
	    public void unlockKeyguardByOtherApp(){
	    	unLockByOther(true);
	    }
		
	    
	    private void amigoInitUnlockReceiver(ViewMediatorCallback callback) {
	      	 Log.d(TAG, "amigoInitUnlockReceiver");
	      	AmigoLockOrUnlockReceiver unlockExt = AmigoLockOrUnlockReceiver.getInstance(mContext);
	      	unlockExt.setUnlockCallback(callback);
	     }
}
