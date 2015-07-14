package com.amigo.navi.keyguard.skylight;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import android.appwidget.AppWidgetHostView;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.modules.WeatherInfoModule;
import com.amigo.navi.keyguard.util.AmigoKeyguardUtils;
import com.amigo.navi.keyguard.util.BitmapUtil;
import com.amigo.navi.keyguard.util.DataStatistics;
import com.amigo.navi.keyguard.util.KeyguardWidgetUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.R;


public class SkylightHost extends FrameLayout {

    private static final String LOG_TAG="SkyWindowHost";
    private static final String MUSIC_PACKAGE_NAME="com.android.music";
    private static final String WEATHER_PACKAGE_NAME="com.coolwind.weather";
    
    private static final String ACTION_MUSIC_PLAY_STATE_CHANGED = "com.android.music.playstatechanged";
    private static final String INTENT_MUSIC_ISPLAY_STATE_KEY = "playing";
    
    private static final int UPDATE_WEATHER_INFO = 9;
    private static final int UPDATE_BACKGROUD = 10;
    private static final int MUSIC_PLAYING_STATE_CHANGE=11;
   
    private int mTouchCallTime = 0;
    
    private PowerManager mPowerManager=null;
    Drawable mWallpaperDrawable = null;
    FrameLayout mSkylightLayout;
    RelativeLayout mBgLayout;
    private AppWidgetHostView mMusicWidget = null;
    static SkylightLocation sLocation=new SkylightLocation();
    
//    private Configuration mConfiguration = null;
    private String mOldFontStyle="";
    
    private boolean mIsMusicFrozen=false;
    private boolean mIsMusicPlaying=false;
    
    private LocalReceiver mReceiver=new LocalReceiver();
    
    private Context mContext;
    
    
    private int mBgCount=0;
    private int mBgCurrenIndex=0;
    private Bitmap mBgBitmap=null;
    
    public SkylightHost(Context context) {
        this(context,null);
        
    }

    public SkylightHost(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public SkylightHost(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext=context;
//        mConfiguration = new Configuration(getContext().getResources().getConfiguration());
        mOldFontStyle = AmigoKeyguardUtils.getmOldFontStyle();
        mPowerManager=(PowerManager)context.getSystemService(Context.POWER_SERVICE);
        getSkylightBackgroundConfig();
        initView(context);
        requestFocus();
    }

    
    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.skylight_host_view, this,true);
        mBgLayout=(RelativeLayout)findViewById(R.id.skylight_bg_layout);
        mSkylightLayout = (FrameLayout) findViewById(R.id.skyligt_window);
        RelativeLayout.LayoutParams params=(RelativeLayout.LayoutParams)mSkylightLayout.getLayoutParams();
        params.leftMargin=sLocation.getXaxis();
        params.topMargin=sLocation.getYaxis();
        params.width=sLocation.getWidth();
        params.height=sLocation.getHeight();
        mSkylightLayout.setLayoutParams(params);
        
        mPagerView = (SkyPagerView)findViewById(R.id.zzzzz_gn_navil_id_app_widget_container);
        
        mInfoZone = new SkylightView(context);
        mInfoZone.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mPagerView.addView(mInfoZone);
        
        mPageIndicator = (KeyguardPagerIndicator)findViewById(R.id.zzzzz_gn_navil_id_indicator);
        mPagerView.setIndicator(mPageIndicator);
        
//        setWallpaperAsBackground();
//        setBackgroud();
    }
    
    
    public void updateSkylightLocation() {
        if(mSkylightLayout!=null){
            RelativeLayout.LayoutParams params=(RelativeLayout.LayoutParams)mSkylightLayout.getLayoutParams();
            params.leftMargin=sLocation.getXaxis();
            params.topMargin=sLocation.getYaxis();
            params.width=sLocation.getWidth();
            params.height=sLocation.getHeight();
            mSkylightLayout.setLayoutParams(params);
        }
    }

    private void loadBackground() {
		Log.i(LOG_TAG, "loadBackground--------mBgCount:" + mBgCount);
        if(mBgLayout != null){
			if(mBgCount > 0){
				readBgBitmapFromSys(mBgCurrenIndex);
			} else {
				setDefaultBackground();
			}
		}
    }

    private void removeBackground() {
		Log.i(LOG_TAG, "removeBackground--------");
        if(mBgLayout != null){
			Log.i(LOG_TAG, "remove the background image of skylight");
			mBgLayout.setBackground(null);
			BitmapUtil.recycleBitmap(mBgBitmap);
			mBgBitmap = null;
        }
    }

    private void setDefaultBackground() {
		Log.i(LOG_TAG, "setDefaultBackground--------");
        if(mBgLayout != null){
			Drawable bg = getResources().getDrawable(R.drawable.skylight_bg_wallpaper);
			mBgLayout.setBackground(bg);
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case UPDATE_BACKGROUD:
                Bitmap bm=(Bitmap)msg.obj;
                if(mBgCurrenIndex!=msg.arg1){
					Log.e(LOG_TAG, "invalid background index!!");
					setDefaultBackground();
                    BitmapUtil.recycleBitmap(bm);
                    return;
                }
                Bitmap temp=mBgBitmap;
                mBgBitmap=bm;
                mBgLayout.setBackground(new BitmapDrawable(mBgBitmap));
                BitmapUtil.recycleBitmap(temp);
                SkylightUtil.writeSharedPreference(mContext, SkylightUtil.SKYLIGHT_SP, SkylightUtil.BG_CURRENT_INDEX_KEY,
                        mBgCurrenIndex);
                break;
            case UPDATE_WEATHER_INFO:
                Log.d(LOG_TAG, "UPDATE_WEATHER_INFO--------");
                Context context=mContext.getApplicationContext();
                WeatherInfoModule.getInstance(context, KeyguardUpdateMonitor.getInstance(context)).getWeatherInfo();
                break;
            case MUSIC_PLAYING_STATE_CHANGE:
                addMusicWidgetIfNeed();
                break;
            default:
                break;
            }
        }

      
    };
    private SkyPagerView mPagerView;
    private View mInfoZone;
    private KeyguardPagerIndicator mPageIndicator;
    
    private void setWallpaperAsBackground() {
        mSkylightLayout.setBackgroundResource(R.drawable.skylight_bg_wallpaper);
//        new UpdateWallpaperThread().start();
    }

    private class UpdateWallpaperThread extends Thread {

        @Override
        public void run() {
            super.run();
            setBackgroud();
            mHandler.sendEmptyMessage(UPDATE_BACKGROUD);
        }
    }
    
    /**
     * @deprecated don't change along with system wallpaper
     * @date 2014-12-4
     */
    public void notifyWallpaperChanged(){
        setWallpaperAsBackground();
    }

    private void setBackgroud() {
        mWallpaperDrawable=getResources().getDrawable(R.drawable.skylight_bg_wallpaper);
        mSkylightLayout.setBackgroundDrawable(mWallpaperDrawable);
    }
    
    public void showSkylight() {
        if(DebugLog.DEBUG)Log.d(LOG_TAG, "isMusicPlaying: "+mIsMusicPlaying);
		loadBackground();
        if (mIsMusicPlaying) {
            addMusicWidget();
            DataStatistics.getInstance().skylightClose(mContext, DataStatistics.SKYLIGHT_SHOW_MUSIC);
        } else {
            mPagerView.removeView(mMusicWidget);
            KeyguardWidgetUtils.getInstance(mContext).deleteWidget(KeyguardWidgetUtils.WIDGET_NAME_MUSIC_SKYLIGHT);
            mPagerView.resetIndicator();
            mMusicWidget=null;
            DataStatistics.getInstance().skylightClose(mContext, DataStatistics.SKYLIGHT_SHOW_HOME);
        }
       
    }
    
    private void addMusicWidgetIfNeed() {
        boolean isHallOpen=SkylightUtil.getIsHallOpen(mContext);
        DebugLog.d(LOG_TAG, "addMusicWidgetIfNeed()  isHallOpen: "+isHallOpen+"  mIsMusicPlaying: "+mIsMusicPlaying);
        if(mIsMusicPlaying&&!isHallOpen){
            if(mMusicWidget==null){
                addMusicWidget();
            }
        }
    }
    
    /**
     * when music is playing, add music widget
     */
    private void addMusicWidget() {
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "isMusicForzen: "+mIsMusicFrozen+"  is MusicWidget null? "+(mMusicWidget==null));
        if (mIsMusicFrozen) {
            mPagerView.removeView(mMusicWidget);
            KeyguardWidgetUtils.getInstance(mContext).deleteWidget(KeyguardWidgetUtils.WIDGET_NAME_MUSIC_SKYLIGHT);
            mMusicWidget = null;
            mIsMusicPlaying=false;
        } else {
            if (mMusicWidget != null && mMusicWidget.getParent() != null) {
                //((ViewGroup)mMusicWidget.getParent()).removeView(mMusicWidget);
            } else {
                mMusicWidget = KeyguardWidgetUtils.getInstance(mContext).createMusicWidgetForSkylight();
                if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "addMusicWidget  widget is null? " + (mMusicWidget == null));
                if (mMusicWidget != null) {
                    int musicPaddingBottom = getResources().getDimensionPixelSize(R.dimen.skylight_music_padding_bottom);
                    mMusicWidget.setPadding(0, 0, 0, musicPaddingBottom);
                    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                    mPagerView.addView(mMusicWidget, 0, params);
                }else{
                    mIsMusicPlaying=false;
                }
            }
            mPagerView.resetIndicator();
            setToMusicPageIfPlaying();
        }
    }
    
    public void hideSkylight(){
        removeBackground();
    }
    

    private void setToMusicPageIfPlaying(){
        if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "isMusicPlaying: "+mIsMusicPlaying);
        if(mIsMusicPlaying){
            mPagerView.setCurrentPageIndex(0);
            mPagerView.toast();
        }
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        userActivity(ev);
        return super.dispatchTouchEvent(ev);
    }
    
    
    private void userActivity(MotionEvent ev){
        switch (ev.getActionMasked()) {
        case MotionEvent.ACTION_DOWN:
            DebugLog.d(LOG_TAG, "SkylightHost userActivity");
            mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
            break;
        default:
            if (mTouchCallTime++ % 50 == 0) {
                mTouchCallTime = 1;
                mPowerManager.userActivity( SystemClock.uptimeMillis(), false);
            }
            break;
        }
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }
    
    public static final boolean isSkylightSizeExist(){
    	if(sLocation!=null){
    		return ((sLocation.getHeight()>0)&&(sLocation.getWidth()>0));
    	}
    	return false;
    }
    
    public static int getSkylightWidth(){
    	if(sLocation!=null){
    		return sLocation.getWidth();
    	}
    	return 0;
    }
    
    public static int getSkylightHeight(){
        if(sLocation!=null){
            return sLocation.getHeight();
        }
        return 0;
    }
    
    private void getSkylightBackgroundConfig() {
        new ReadSkylightBgConfigThread().start();
    }
    

    private class ReadSkylightBgConfigThread extends Thread {

        @Override
        public void run() {

            File file = new File(SkylightUtil.SKYLIGHT_BG_PATH);
            if (!file.exists()) {
				Log.w(LOG_TAG, "background files don't exist! set default background.");
				setDefaultBackground(); 
                return;
            }
            String[] fileNames = file.list();
            mBgCount = fileNames.length;
            if (mBgCount > 0) {
                mBgCurrenIndex = SkylightUtil.readValueFromSharePreference(mContext, SkylightUtil.SKYLIGHT_SP,
                        SkylightUtil.BG_CURRENT_INDEX_KEY);
                //readBgBitmapFromSys(mBgCurrenIndex); //xfge
            }
        }
    }
    
    protected void readBgBitmapFromSys(int bgIndex) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(SkylightUtil.SKYLIGHT_BG_PATH +bgIndex + ".png");
            Bitmap bm = BitmapFactory.decodeStream(fis);
            DebugLog.d(LOG_TAG, "readBgBitmapFromSys  bgCount: "+mBgCount+"  index: "+bgIndex+" bm: "+bm);
            if (bm == null) {
				Log.e(LOG_TAG, "failed to read background!");
				setDefaultBackground();
                return;
            }
            Message msg = mHandler.obtainMessage(UPDATE_BACKGROUD, bm);
            msg.arg1=bgIndex;
            mHandler.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    protected int getBgCurrentIndex(){
        return mBgCurrenIndex;
    }
    
    protected void setBgCurrentIndex(int index){
        mBgCurrenIndex=index;
    }
    
    protected int getBgCount(){
        DebugLog.d(LOG_TAG, "bgCount: "+mBgCount);
        return mBgCount;
    }

    
    @Override
    protected void onAttachedToWindow() {
    	super.onAttachedToWindow();
    	KeyguardWidgetUtils.getInstance(mContext).startHostListening();
    	registerReceivers();
    	if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "SkylightHost  onAttachedToWindow()");
    }
    
    @Override
    protected void onDetachedFromWindow() {
    	super.onDetachedFromWindow();
    	unregisterReceivers();
    	KeyguardWidgetUtils.getInstance(mContext).stopHostListening();
    	if(DebugLog.DEBUG)DebugLog.d(LOG_TAG, "SkylightHost  onDetachedFromWindow()");
    }
    
    public void onConfigurationChanged() {
        if (DebugLog.DEBUG)
        	DebugLog.d(LOG_TAG, "skylight host onConfigurationChanged------------ ");
        Configuration newConfig = getResources().getConfiguration();
        String currentFontStyle = AmigoKeyguardUtils.getCurrretFontStyle(newConfig, mOldFontStyle);
        boolean isChangeFontStyle = false;
        if (!currentFontStyle.equals(mOldFontStyle)) {
            mOldFontStyle = currentFontStyle;
            isChangeFontStyle = true;
            DebugLog.d(LOG_TAG, "onConfigurationChanged() newConfig....amigoFont1111111=" + currentFontStyle
                    + "oldFontStyle=" + mOldFontStyle);
        }
        DebugLog.d(LOG_TAG, "isChangeFontStyle:" + isChangeFontStyle);
        if (isChangeFontStyle) {
            reloadSkylightView();
        } else {
            DebugLog.d(LOG_TAG, "onConfigurationChanged: congfiguration not change");
        }
    }
    
    private void reloadSkylightView() {
        mPagerView.removeView(mInfoZone);
        mPagerView.removeView(mMusicWidget);
        KeyguardWidgetUtils.getInstance(mContext).deleteWidget(KeyguardWidgetUtils.WIDGET_NAME_MUSIC_SKYLIGHT);
        mInfoZone = null;
        mMusicWidget = null;
        mInfoZone = new SkylightView(getContext());
        mInfoZone.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mPagerView.addView(mInfoZone);
        if (mIsMusicPlaying) {
            addMusicWidget();
        }
    }
    
    private void registerReceivers(){
        IntentFilter packageFilter=new IntentFilter();
        packageFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        packageFilter.addDataScheme("package");
        mContext.registerReceiver(mReceiver, packageFilter);
        
        IntentFilter filter=new IntentFilter();
        filter.addAction(ACTION_MUSIC_PLAY_STATE_CHANGED);
        mContext.registerReceiver(mReceiver, filter);
        
    }
    
    private void unregisterReceivers(){
        mContext.unregisterReceiver(mReceiver);
        
    }
    
    private void getIsMusicAppFrozen(){
        new Thread(){
            public void run() {
                PackageManager pm = mContext.getPackageManager();
                Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                List<ResolveInfo> infos=pm.queryIntentActivities(mainIntent, 0);
                for (ResolveInfo resolveInfo : infos) {
                    String packageName=resolveInfo.activityInfo.packageName;
                    if(MUSIC_PACKAGE_NAME.equals(packageName)){
                        mIsMusicFrozen=false;
                        return;
                    }
                }
                mIsMusicFrozen=true;
            };
        }.start();
      
    }
    
    
    class LocalReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (intent.ACTION_PACKAGE_CHANGED.equals(action) || Intent.ACTION_PACKAGE_REMOVED.equals(action)
                    || Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                final String packageName = intent.getData().getSchemeSpecificPart();
                if (MUSIC_PACKAGE_NAME.equals(packageName)) {
                    getIsMusicAppFrozen();
                    if (DebugLog.DEBUG)
                    	DebugLog.d(LOG_TAG, "packageName: " + packageName + "  action: " + action + " isMusicFrozen: "
                                + mIsMusicFrozen);
                } else if (WEATHER_PACKAGE_NAME.equals(packageName)) {
                    mHandler.sendEmptyMessage(UPDATE_WEATHER_INFO);
                }
                
            }else if(intent.getAction().equals(ACTION_MUSIC_PLAY_STATE_CHANGED)) {
                mIsMusicPlaying = intent.getBooleanExtra(INTENT_MUSIC_ISPLAY_STATE_KEY,false);
                if(DebugLog.DEBUG) DebugLog.d(LOG_TAG, "onReceive ACTION_MUSIC_PLAY_STATE_CHANGED,isPlaying:"+mIsMusicPlaying);
                if(mIsMusicPlaying){
                    mHandler.sendEmptyMessage(MUSIC_PLAYING_STATE_CHANGE);
                }
            }
        }
    }

}

