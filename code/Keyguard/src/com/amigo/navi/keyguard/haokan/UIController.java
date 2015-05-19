
package com.amigo.navi.keyguard.haokan;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.amigo.navi.keyguard.AmigoKeyguardHostView;
import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.KeyguardViewHost;
import com.amigo.navi.keyguard.KeyguardViewHostManager;
import com.amigo.navi.keyguard.haokan.PlayerManager.State;
import com.amigo.navi.keyguard.haokan.db.WallpaperDB;
import com.amigo.navi.keyguard.haokan.entity.Music;
import com.amigo.navi.keyguard.haokan.entity.Wallpaper;
import com.amigo.navi.keyguard.haokan.entity.WallpaperList;
import com.amigo.navi.keyguard.haokan.menu.ArcLayout;
import com.amigo.navi.keyguard.infozone.AmigoKeyguardInfoZone;
import com.amigo.navi.keyguard.picturepage.adapter.HorizontalAdapter;
import com.amigo.navi.keyguard.picturepage.widget.KeyguardListView;
import com.amigo.navi.keyguard.picturepage.widget.HorizontalListView.OnTouchlListener;
import com.amigo.navi.keyguard.settings.KeyguardSettingsActivity;
import com.amigo.navi.keyguard.util.VibatorUtil;
import com.android.keyguard.KeyguardHostView.OnDismissAction;
import com.android.keyguard.R;
import java.util.ArrayList;
import java.util.List;

public class UIController implements OnTouchlListener{
    
    
    
    private static final String TAG = "haokan";
    
    public List<Animator> mAnimators = new ArrayList<Animator>();
    
    public void addAnimator(Animator animator) {
        mAnimators.add(animator);
    }
    public void clearAnimators() {
        mAnimators.clear();
    }
    
    private PlayerButton mPlayerButton;
    
    private AmigoKeyguardInfoZone mInfozone;
    
    private WebView mWebView;
    
    private View mWebViewContainer;
    
    private View mCloseLinkLayout;
    
    private CaptionsView mCaptionsView;
    
    private RelativeLayout mPlayerLayout;
    
    private boolean mWebViewShowing = false;
    
    private static UIController instance = null;
    
    private Handler mHandle = new Handler();
    
    private ArcLayout mArcLayout;
    
    private KeyguardListView mKeyguardListView;
    
    
    private TextView mTextViewTip;
    
    private View mKeyguardNotificationView;
    
    private float mNotificationMarginTop = 0;
    
    private Wallpaper mCurrentWallpaper;
    
    private float mInfozoneTranslationX = 0f;
    
    private ObjectAnimator[] infoZoneAnimators = new ObjectAnimator[4];
    
    
    private KeyguardViewHost mKeyguardViewHost;
    
    private KeyguardWallpaperContainer mKeyguardWallpaperContainer;
    
    private WebLayout mWebLayout = null;
    
    
    private RelativeLayout mArcMenu;
    
    private CategoryActivity categoryActivity;
    private KeyguardSettingsActivity mKeyguardSettingsActivity;
    
    private AmigoKeyguardHostView mAmigoKeyguardHostView;
    public static final int SCROLL_TO_UNLOCK=0;
    public static final int SCROLL_TO_SECURTY=1;
    public static final int SECURITY_SUCCESS_UNLOCK=2;
    
    private WakeLock mWakeLock = null;
    
    private TextView mTextViewMusicName, mTextViewArtist;
    
    
    public RelativeLayout getmArcMenu() {
        return mArcMenu;
    }
    public void setmArcMenu(RelativeLayout mArcMenu) {
        this.mArcMenu = mArcMenu;
    }
    
    public KeyguardViewHost getmKeyguardViewHost() {
        return mKeyguardViewHost;
    }
    public void setmKeyguardViewHost(KeyguardViewHost mKeyguardViewHost) {
        this.mKeyguardViewHost = mKeyguardViewHost;
    }
    public void startCategoryActivity(final Context context){
 
        if (getAmigoKeyguardHostView().isSecure()) {
            
            KeyguardViewHostManager.getInstance().dismissWithDismissAction(new OnDismissAction() {
                @Override
                public boolean onDismiss() {
                    Intent intent = new Intent(context, CategoryActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    return true;
                }
            },true);
        }else {
            
            Intent intent = new Intent(context, CategoryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            KeyguardViewHostManager.getInstance().showBouncerOrKeyguardDone();
            context.startActivity(intent);
        }
    }
    
    public void startSettingsActivity(final Context context){
 
        if (getAmigoKeyguardHostView().isSecure()) {
            
            KeyguardViewHostManager.getInstance().dismissWithDismissAction(new OnDismissAction() {
                
                @Override
                public boolean onDismiss() {
                    Intent intent = new Intent(context, KeyguardSettingsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    return true;
                }
            },true);
            
        }else {
            
            Intent intent = new Intent(context, KeyguardSettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            KeyguardViewHostManager.getInstance().showBouncerOrKeyguardDone();
            context.startActivity(intent);
        }
        
    }

    public static UIController getInstance() {
        if (instance == null) {
            instance = new UIController();
        }
        return instance;
    }
    
    public UIController() {
    
    }
    
   
    
    public void wakeLockRelease(){
        if(mWakeLock != null && mWakeLock.isHeld()){
            mWakeLock.release();
        }
    }
    
    public void wakeLockAcquire(){
        if(mWakeLock != null){
            mWakeLock.acquire(30000);
        }
    }
    
    
    public void showWebView(Context context,String link) {
        
        mWebViewShowing = true;
        
        final KeyguardViewHost keyguardHostView = getmKeyguardViewHost();

        if (mWebLayout == null) {
            mWebLayout = (WebLayout) LayoutInflater.from(context).inflate(
                    R.layout.haokan_web_layout, null, true);
        }
        mWebLayout.loadurl(link);
        final WebView webView = mWebLayout.getmWebView();

        webView.postDelayed(new Runnable(){
            @Override
            public void run(){
                webView.clearHistory();
            }
        }, 1000);
        
        
        if (keyguardHostView.indexOfChild(mWebLayout) != -1) {
            keyguardHostView.removeView(mWebLayout);
        }
        
        if (mWakeLock == null) {
            PowerManager powerManager = (PowerManager)context.getApplicationContext().getSystemService(Context.POWER_SERVICE);
            mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "keyguard_webview"); 
            mWakeLock.setReferenceCounted(false);
        }
        wakeLockAcquire();
        
 
        keyguardHostView.addView(mWebLayout);
        
        final CaptionsView captionsView = getmCaptionsView();
        final AmigoKeyguardInfoZone infoZone = getmInfozone();
        
        ObjectAnimator animatorCaptionsView = ObjectAnimator.ofFloat(captionsView, "alpha", 1f,0f).setDuration(400);
        ObjectAnimator animatorInfoZone = ObjectAnimator.ofFloat(infoZone, "translationY", 0f,infoZone.getMeasuredHeight()).setDuration(400);
        
        ObjectAnimator animatorWebLayout = ObjectAnimator.ofFloat(mWebLayout, "alpha", 0f,1f).setDuration(800);
        animatorWebLayout.addListener(new AnimatorListener() {
            
            @Override
            public void onAnimationStart(Animator arg0) {
                mWebLayout.setVisibility(View.VISIBLE);
            }
            
            @Override
            public void onAnimationRepeat(Animator arg0) {
                
            }
            
            @Override
            public void onAnimationEnd(Animator arg0) {
                
            }
            
            @Override
            public void onAnimationCancel(Animator arg0) {
                
            }
        });
        
        ObjectAnimator animatorWebView = ObjectAnimator.ofFloat(webView, "translationY",webView.getTranslationY(),0f).setDuration(300);
        
        
//        PropertyValuesHolder pvhTranslationY = PropertyValuesHolder.ofFloat("translationY", 300.0f, 0f);  
//        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0.0f, 1.0f);
//        ObjectAnimator animatorCloseLink = ObjectAnimator.ofPropertyValuesHolder(mWebLayout.getmButtonCloseLink(), pvhTranslationY,alpha).setDuration(300);
//        mWebLayout.getmButtonCloseLink().setTranslationY(0f);
        ObjectAnimator animatorCloseLink = ObjectAnimator.ofFloat(mWebLayout.getmButtonCloseLink(), "alpha", 0f, 1f).setDuration(300);
        
        AnimatorSet set = new AnimatorSet();
//        set.play(animatorInfoZone).with(animatorCaptionsView); 
//        set.play(animatorWebLayout).after(animatorInfoZone);
//        set.play(animatorWebView).after(animatorWebLayout);
//        set.play(animatorCloseLink).after(animatorWebLayout);
//        set.start();
        
        animatorWebView.setStartDelay(400);
        set.play(animatorInfoZone).with(animatorCaptionsView); 
        set.play(animatorWebLayout).after(animatorInfoZone);
        set.play(animatorWebView).after(animatorInfoZone);
        set.play(animatorCloseLink).after(animatorWebView);
        set.start();
        
    }
    
    public boolean isArcExpanded() {
        if (mArcLayout != null) {
            boolean bool = mArcLayout.isExpanded() || isArcExpanding;
            return bool;
        }
        return false;
    }
    
    public void onBackPress() {
        
        if (mWebViewShowing) {
            removeWebView();
        }

    }
    
    public void removeWebView() {
        wakeLockRelease();
        mWebViewShowing = false;
        
        final CaptionsView captionsView = getmCaptionsView();
        final AmigoKeyguardInfoZone infoZone = getmInfozone();
        
        AnimatorSet set = new AnimatorSet();
        
        ObjectAnimator animatorCaptionsView = ObjectAnimator.ofFloat(captionsView, "alpha", 0f,1f).setDuration(400);
        ObjectAnimator animatorInfoZone = ObjectAnimator.ofFloat(infoZone, "translationY", infoZone.getTranslationY(),0f).setDuration(400);
        
        
        ObjectAnimator animatorWebView = ObjectAnimator.ofFloat(mWebLayout.getmWebView(), "translationY",0f,mWebLayout.getmWebView().getMeasuredHeight()).setDuration(400);
        ObjectAnimator animatorWebLayout = ObjectAnimator.ofFloat(mWebLayout, "alpha", 1.0f, 0f).setDuration(200);
        animatorWebLayout.addListener(new AnimatorListener() {
            
            @Override
            public void onAnimationStart(Animator arg0) {
                
            }
            
            @Override
            public void onAnimationRepeat(Animator arg0) {
                
            }
            
            @Override
            public void onAnimationEnd(Animator arg0) {
                
                final KeyguardViewHost keyguardHostView = getmKeyguardViewHost();

                if (mWebLayout != null) {
                    mWebLayout.setVisibility(View.GONE);
                    if (keyguardHostView.indexOfChild(mWebLayout) != -1) {
                        keyguardHostView.removeView(mWebLayout);
                    }
                }
//                mWebView.loadUrl("about:blank");
            }
            
            @Override
            public void onAnimationCancel(Animator arg0) {
                
            }
        });
        
        ObjectAnimator animatorCloseLink = ObjectAnimator.ofFloat(mWebLayout.getmButtonCloseLink(), "alpha",1,0f).setDuration(300);
//        PropertyValuesHolder pvhTranslationY = PropertyValuesHolder.ofFloat("translationY", 300.0f);  
//        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0.0f);
//        ObjectAnimator animatorCloseLink = ObjectAnimator.ofPropertyValuesHolder(mWebLayout.getmButtonCloseLink(), pvhTranslationY,alpha).setDuration(300);
        
        
//        set.play(animatorWebView).with(animatorCloseLink);
//        set.play(animatorWebLayout).after(animatorWebView);
//        set.play(animatorInfoZone).after(animatorWebLayout);
//        set.play(animatorCaptionsView).after(animatorWebLayout);
        animatorWebView.setStartDelay(150);
        set.play(animatorWebView).with(animatorCloseLink);
        set.play(animatorWebLayout).after(animatorWebView);
        set.play(animatorInfoZone).after(animatorWebLayout);
        set.play(animatorCaptionsView).after(animatorWebLayout);
        
        set.start();

    }
    
 
    public void showTip(int resId) {
        if (mTextViewTip == null) {
            return;
        }
        
        final TextView tip = mTextViewTip;
        
        tip.setText(resId);
        tip.setVisibility(View.VISIBLE);
        
        mHandle.postDelayed(new Runnable() {
            
            @Override
            public void run() {
                tip.setVisibility(View.GONE);
            }
        }, 2000);
 
    }
    
    
    
    @Override
    public void OnTouchMove(int x, int dx) {
        
        final float infozoneMaxTranslationX = mInfozone.getMaxTranslationX();
        final float playerMaxTranslationX = mPlayerButton.getMaxTranslationX();
        
        final View time = mInfozone.getTimeView();
        final View week = mInfozone.getWeekView();
        final View dateFestival = mInfozone.getDateFestivalView();
        
        if (mInfozoneTranslationX == 0f) {
            
            if (mCaptionsView.animatorSet != null) {
                mCaptionsView.animatorSet.cancel();
            }
            int len = infoZoneAnimators.length - 1;
            for (int i = 0; i < len; i++) {
                ObjectAnimator animator = infoZoneAnimators[i];
                if (animator != null) {
                    animator.cancel();
                }
            }
            
            time.setTranslationX(0);
            time.setTranslationY(0);
            week.setTranslationX(0);
            dateFestival.setTranslationX(0);
            mPlayerLayout.setTranslationX(0);
        }
        
        mInfozoneTranslationX +=  dx;
        float translationX = Math.abs(mInfozoneTranslationX);

        if (translationX <= infozoneMaxTranslationX * 4.0f) {
            
            time.setTranslationX(translationX / 4.0f);
            week.setTranslationX(translationX / 4.0f);
            dateFestival.setTranslationX(translationX / 4.0f);
            
            if (getmCurrentWallpaper() != null && getmCurrentWallpaper().getMusic() != null) {
                float playerTranslationX = translationX / (infozoneMaxTranslationX * 4.0f) * playerMaxTranslationX;
                mPlayerLayout.setTranslationX(playerTranslationX);
                float alpha =  1.0f - playerTranslationX / playerMaxTranslationX;
                mPlayerLayout.setAlpha(alpha);
            }
        }else {
            time.setTranslationX(infozoneMaxTranslationX);
            week.setTranslationX(infozoneMaxTranslationX);
            dateFestival.setTranslationX(infozoneMaxTranslationX);
            
            if (mPlayerLayout.getAlpha() != 0f) {
                mPlayerLayout.setAlpha(0);
            }
        }
        
        translationX = Math.abs(x);
        mCaptionsView.onHorizontalMove(translationX);

    }
    
    
    
    @Override
    public void OnTouchUp() {
        mInfozoneTranslationX = 0f;
        refreshWallpaperInfo();
        mCaptionsView.OnTouchUpAnimator();
    }
    
    
    
    public void hideArcMenu() {
        getmArcLayout().setVisibility(View.GONE);
        getmArcMenu().setVisibility(View.GONE);
    }
    
    public void showArcMenu() {
        getmArcLayout().setVisibility(View.VISIBLE);
        getmArcMenu().setVisibility(View.VISIBLE);
    }
    
    
    /** Screen Turned On */
    public void onScreenTurnedOn() {
        if (isScreenOff()) {
            setScreenOff(false);
            if (mWebViewShowing) {
                wakeLockAcquire();
            }
            onScreenTurnedOnAnimation();
        }
    }
    
    private boolean mScreenOff = false;
    
    /** Screen Turned Off */
    public void onScreenTurnedOff() {

        Log.v(TAG, "UIController onScreenTurnedOff");
        setScreenOff(true);
        
        if (mArcLayout != null) {
            if (mArcLayout.isExpanded() || isArcExpanding) {
                Log.v(TAG, "onScreenTurnedOff ArcLayout reset");
                mArcLayout.reset();
            }
        }
        
        onKeyguardLocked();
        
        onScreenTurnedOffAnimation();
        
    }
    
    public void onKeyguardLocked() {
     
        if (categoryActivity != null) {
            if (!categoryActivity.isDestroyed()) {
                Log.v(TAG, "categoryActivity is not destroyed()");
                categoryActivity.finish();
                categoryActivity = null;
            }
        }
        
        if (mKeyguardSettingsActivity != null) {
            if (!mKeyguardSettingsActivity.isDestroyed()) {
                Log.v(TAG, "mKeyguardSettingsActivity is not destroyed()");
                mKeyguardSettingsActivity.finish();
                mKeyguardSettingsActivity = null;
            }
        }
        
        if (mWebViewShowing) {
            final KeyguardViewHost keyguardHostView = getmKeyguardViewHost();

            if (mWebLayout != null) {
                if (keyguardHostView.indexOfChild(mWebLayout) != -1) {
                    keyguardHostView.removeView(mWebLayout);
                }
            }
            mWebViewShowing = false;
            getmInfozone().setAlpha(1.0f);
            getmInfozone().setTranslationY(0f);
            getmCaptionsView().setAlpha(1.0f);
        }

    }
    
    
    public void refreshWallpaperInfo() {
        int pos = getmKeyguardListView().getNextPage();
        DebugLog.d(TAG,"OnTouchUp pos:" + pos);
        Log.v(TAG, "OnTouchUp getPage = " + pos);
        HorizontalAdapter mWallpaperAdapter = (HorizontalAdapter) getmKeyguardListView().getAdapter(); 
        WallpaperList list = mWallpaperAdapter.getWallpaperList();
        Wallpaper wallpaper = null;
        if (list.size() > pos) {
            wallpaper = list.get(pos);
        }
        
        if (wallpaper != null) {
            Log.v(TAG, "refreshWallpaperInfo() : pos = " + pos + "  " + wallpaper.toString());
            refreshWallpaperInfo(wallpaper);
        }

    }
    
    public void refreshWallpaperInfo(final Wallpaper wallpaper) {

        if (wallpaper == null) {
            return;
        }
 
        if (getmCurrentWallpaper() != null) {
            if (getmCurrentWallpaper().getImgId() == wallpaper.getImgId()) {
                Log.v(TAG, "getmCurrentWallpaper().getImgId() == wallpaper.getImgId()  return");
                return;
            }
        }
        
        setmCurrentWallpaper(wallpaper);
        
        PlayerManager playerManager = PlayerManager.getInstance();
        if (playerManager.getState() != State.NULL) {
            Log.v(TAG, "player/pause Music");
            mPlayerButton.setState(State.NULL);
            playerManager.stopMusicPlayer(true); 
            hideMusicPlayer(false);
        }
        
        
        if (wallpaper.getMusic() == null) {
             
            if (mPlayerLayout.getVisibility() == View.VISIBLE) {
                mPlayerLayout.setVisibility(View.GONE);
            }
            
        }else {
             
            if (mPlayerLayout.getVisibility() != View.VISIBLE) {
                mPlayerLayout.setVisibility(View.VISIBLE);
            }
            
            PlayerManager.getInstance().setmCurrentMusic(wallpaper.getMusic());
             
        }
        
        mInfozone.setFestivalText(wallpaper.getFestival());
 
        mCaptionsView.setContentText(wallpaper.getCaption());
    }
    
    
    public void onDown(MotionEvent e) {
        
        if (mArcLayout != null) {
            if (mArcLayout.isExpanded() && !mArcLayout.animatorRunning()) {
                Log.v(TAG, "ACTION_DOWN  & startHide");
                mArcLayout.startHide();
            }
        }
    }
    
    boolean isArcExpanding = false;
    
    public void setArcExpanding(boolean isArcExpanding) {
        this.isArcExpanding = isArcExpanding;
    }
    
    public void onLongPress(float motionDowmX,float motionDowmY) {
        if (mArcLayout.animatorRunning() || mArcLayout.isExpanded()
                || getAmigoKeyguardHostView().getScrollY() != 0) {
            return;
        }
        
        List<Animator> animators = UIController.getInstance().mAnimators;
        
        for (Animator animator : animators) {
            if (animator.isRunning()) {
                animator.cancel();
            }
        }
        animators.clear();

        if (getmCurrentWallpaper() == null) {
            Log.v(TAG, "*************wallpaper == null**************");
            return;
        }
        
        if (!mArcLayout.requestLayout(motionDowmX, motionDowmY)) {
            return;
        }
        
        getmArcMenu().setVisibility(View.VISIBLE);
        
        VibatorUtil.amigoVibrate(mArcLayout.getContext().getApplicationContext(),
                VibatorUtil.LOCKSCREEN_STORYMODE_DISPLAY, 100);
        
        hideKeyguardNotification();
        

//        DebugLog.d(TAG,"onLongPress wallpaper url:" + getmCurrentWallpaper().getImgUrl());
//        DebugLog.d(TAG,"onLongPress wallpaper lock:" + getmCurrentWallpaper().isLocked());

        mArcLayout.setmWallpaper(getmCurrentWallpaper());
        
        mArcLayout.refreshItemState();
        
        isArcExpanding = true;
        mArcLayout.startShow();

    }
    
    public Bitmap getCurrentWallpaperBitmap(Wallpaper wallpaper) {
    	if(wallpaper == null){
    		return null;
    	}
        HorizontalAdapter mWallpaperAdapter = (HorizontalAdapter) getmKeyguardListView().getAdapter(); 
        Bitmap bitmap = mWallpaperAdapter.getWallpaperByUrl(wallpaper.getImgUrl());
        return bitmap;
        
    }
    
    public Bitmap getCurrentWallpaperBitmap(Context context) {
    	Bitmap bitmap = getCurrentWallpaperBitmap(mCurrentWallpaper);
    	if(bitmap == null){
    		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.loading);
    	}
        return bitmap;
    }
    
    
   
    public void onKeyguardScrollChanged(int top,int maxBoundY,int model) {

        
        float captionsAlpha = 0f;
        float infozoneAlpha = 0f;
        float playerAlpha = 0f;
        float notificationAlpha = 0f;
        
        float captionScrollHeight = maxBoundY * 0.3f;
        
        if (top <= 50) {
            playerAlpha = 1.0f - top / 50;
        }

        if (!mCaptionsView.isContentVisible()) {
            
            if (top <= mNotificationMarginTop) {
                notificationAlpha = 1.0f - top / mNotificationMarginTop;
            }
            mKeyguardNotificationView.setAlpha(notificationAlpha);
        }
        
        if (top <= captionScrollHeight) {
            captionsAlpha = 1.0f - top / captionScrollHeight;
        }
      
        mCaptionsView.setAlpha(captionsAlpha);
        mPlayerLayout.setAlpha(playerAlpha);
        
        
        if (top <= 0) {
            mInfozone.setAlpha(1f);
            mCaptionsView.setAlpha(1f);
            mPlayerLayout.setAlpha(1f);
            if (!mCaptionsView.isContentVisible()) {
                mKeyguardNotificationView.setAlpha(1f);
            }
        }
         
        if (model==SCROLL_TO_UNLOCK) {
            infozoneAlpha = 1.0f - top / (float)maxBoundY;
            mInfozone.setAlpha(infozoneAlpha);
        }
        mKeyguardWallpaperContainer.onKeyguardModelChanged(top, maxBoundY,model);
    }
    
    public void securityViewRemoveAnimationUpdating(int top,int maxBoundY){
//    	 float infozoneAlpha = 0f;
//    	infozoneAlpha = 1.0f - top / (float)maxBoundY;
//        mInfozone.setAlpha(infozoneAlpha);
    	mKeyguardWallpaperContainer.onKeyguardModelChanged(top, maxBoundY,SECURITY_SUCCESS_UNLOCK);
    }
    
    public void hideKeyguardNotification() {

        final View view = getmKeyguardNotification();
        
        if (view == null) {
            return;
        }
        
        if (view.getAlpha() == 0f) {
            return;
        }
        
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f).setDuration(400);
 
        animator.start();
        
    }
    
    
    public void showKeyguardNotification() {

        final View view = getmKeyguardNotification();
        if (view == null || getmCaptionsView().isContentVisible()) {
            return;
        }
        
        Log.v(TAG, "hideKeyguardNotification view.getAlpha() = " + view.getAlpha());
        if (view.getAlpha() == 1.0f) {
            return;
        }
        
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).setDuration(400);
    
        animator.start();
    }
    
    
    private void onScreenTurnedOffAnimation() {

        mHandle.postDelayed(new Runnable() {

            @Override
            public void run() {
                
                int len = infoZoneAnimators.length;
                for (int i = 0; i < len; i++) {
                    ObjectAnimator animator = infoZoneAnimators[i];
                    if (animator != null) {
                        animator.cancel();
                    }
                }
                

                final AmigoKeyguardInfoZone infoZone = getmInfozone();

                int toTranslationY = infoZone.getMeasuredHeight();

                infoZone.getTimeView().setTranslationY(toTranslationY);

                View textViewWeek = infoZone.getWeekView();
                View textViewDate = infoZone.getDateFestivalView();

                textViewWeek.setVisibility(View.GONE);
                textViewDate.setVisibility(View.GONE);

                textViewWeek.setTranslationX(-textViewWeek.getMeasuredWidth() - 100);
                textViewDate.setTranslationX(-textViewDate.getMeasuredWidth() - 100);
                if (getmCurrentWallpaper() != null && getmCurrentWallpaper().getMusic() != null) {
                    if (getmCurrentWallpaper().getMusic() != null) {
                        mPlayerLayout.setAlpha(0f);
                    }
                }
                final CaptionsView captionsView = mCaptionsView;
                captionsView.onScreenTurnedOff();

                final KeyguardListView listView = getmKeyguardListView();
                listView.setScaleX(1.11f);
                listView.setScaleY(1.11f);
                
                final View view = getmKeyguardNotification();
                view.setAlpha(1.0f);
                
            }
        }, 120);
    }
    

    
    
    private void onScreenTurnedOnAnimation() {

        final CaptionsView hkCaptionsView = getmCaptionsView();
        final AmigoKeyguardInfoZone infoZone = getmInfozone();
        
        final View textViewWeek = infoZone.getWeekView();
        final View textViewDate = infoZone.getDateFestivalView();
        
        int zoneFormTranslationY = infoZone.getMeasuredHeight();
        
        PropertyValuesHolder pvhTranslateY= PropertyValuesHolder.ofKeyframe(View.TRANSLATION_Y,  
 
    
//                Keyframe.ofFloat(0f, zoneFormTranslationY),  
//                Keyframe.ofFloat(.35691f, -8.3f * 3f),  
//                Keyframe.ofFloat(.53598f, 3f * 3f),  
//                Keyframe.ofFloat(.71428f, -0.6f * 3f),  
//                Keyframe.ofFloat(.89258f, 0.4f * 3f),  
//                Keyframe.ofFloat(1f, 0f)  
                Keyframe.ofFloat(0f, zoneFormTranslationY),  
                Keyframe.ofFloat(.35691f, -25f),  
                Keyframe.ofFloat(.53598f, 10f),  
                Keyframe.ofFloat(.71428f, -5f),  
                Keyframe.ofFloat(.89258f, 2.5f),  
                Keyframe.ofFloat(1f, 0f)  
                
        );  
        
        ObjectAnimator animationUp = ObjectAnimator.ofPropertyValuesHolder(getmInfozone().getTimeView(), pvhTranslateY).setDuration(931);
        
        PropertyValuesHolder pvhTranslateX= PropertyValuesHolder.ofKeyframe(View.TRANSLATION_X,  
                
                Keyframe.ofFloat(0f, -200f),  
                Keyframe.ofFloat(.18897f, 9.3f * 3),  
                Keyframe.ofFloat(.35117f, -4.7f * 3),  
                Keyframe.ofFloat(.51338f, 2.3f * 3),  
                Keyframe.ofFloat(.67558f, -0.7f * 3),  
                Keyframe.ofFloat(.83779f, 0.3f * 3),  
                Keyframe.ofFloat(1f, 0f)  
                
        );  
        
 
        ObjectAnimator alphaWeek = ObjectAnimator.ofFloat(textViewWeek, "alpha", 0f,1f).setDuration(500);
       
        alphaWeek.setStartDelay(266);
        ObjectAnimator animationDate = ObjectAnimator.ofPropertyValuesHolder(textViewDate, pvhTranslateX).setDuration(1333);
        animationDate.setStartDelay(300);
        
         
        animationDate.addListener(new AnimatorListener() {
            
            @Override
            public void onAnimationStart(Animator arg0) {
                textViewDate.setVisibility(View.VISIBLE);
            }
            
            @Override
            public void onAnimationRepeat(Animator arg0) {
                
            }
            
            @Override
            public void onAnimationEnd(Animator arg0) {
                
            }
            
            @Override
            public void onAnimationCancel(Animator arg0) {
                
            }
        });
        
        ObjectAnimator animationWeek = ObjectAnimator.ofPropertyValuesHolder(textViewWeek, pvhTranslateX).setDuration(1333);
        animationWeek.setStartDelay(266);
        
        animationWeek.addListener(new AnimatorListener() {
            
            @Override
            public void onAnimationStart(Animator arg0) {
                textViewWeek.setVisibility(View.VISIBLE);
            }
            
            @Override
            public void onAnimationRepeat(Animator arg0) {
                
            }
            
            @Override
            public void onAnimationEnd(Animator arg0) {
                
            }
            
            @Override
            public void onAnimationCancel(Animator arg0) {
                
            }
        });
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(animationUp).with(animationDate).with(animationWeek).with(alphaWeek);

        infoZoneAnimators[0] = animationUp;
        infoZoneAnimators[1] = animationWeek;
        infoZoneAnimators[2] = animationDate;
        if (hkCaptionsView != null) {
            int titleFormTranslationY = hkCaptionsView.getAnimFormTranslationY();
          
            PropertyValuesHolder pvhTranslationY = PropertyValuesHolder.ofFloat("translationY", titleFormTranslationY, hkCaptionsView.getInitialTranslationY()); 
            ObjectAnimator animationTitle = ObjectAnimator.ofPropertyValuesHolder(hkCaptionsView, pvhTranslationY).setDuration(333);  
         
            final View view = hkCaptionsView.getTitleContainer();
            
            animationTitle.setStartDelay(733);
            
            animationTitle.addUpdateListener(new AnimatorUpdateListener() {
                
                @Override
                public void onAnimationUpdate(ValueAnimator arg0) {
                    view.setAlpha(arg0.getAnimatedFraction());
                }
            });
            infoZoneAnimators[3] = animationTitle;
            animatorSet.play(animationTitle);
        }
        
        if (getmCurrentWallpaper() != null) {
            if (getmCurrentWallpaper().getMusic() != null) {
                ObjectAnimator playerButtonAlpha = ObjectAnimator.ofFloat(mPlayerLayout, "alpha", 1f).setDuration(200);
                playerButtonAlpha.setStartDelay(266);
                animatorSet.play(playerButtonAlpha);
            }
        }

        final KeyguardListView listView = getmKeyguardListView();
        if (listView.getScaleX() != 1.0f || listView.getScaleY() != 1.0f) {
            PropertyValuesHolder pvhscaleX = PropertyValuesHolder.ofFloat("scaleX", listView.getScaleX(), 1.0f);
            PropertyValuesHolder pvhscaleY = PropertyValuesHolder.ofFloat("scaleY", listView.getScaleY(), 1.0f);
            ObjectAnimator listViewScaleAnimator = ObjectAnimator.ofPropertyValuesHolder(listView,pvhscaleX, pvhscaleY).setDuration(650);
            listViewScaleAnimator.setInterpolator(new DecelerateInterpolator());
            animatorSet.play(listViewScaleAnimator);
        }
        
        
        animatorSet.setStartDelay(200);
        animatorSet.start();
    }
    
    
    public void showMusicPlayer(final Music mCurrentMusic) {

        mHandle.postDelayed(new Runnable() {

            @Override
            public void run() {

                mTextViewMusicName.setText(mCurrentMusic.getmMusicName());
 
                mTextViewArtist.setText(mCurrentMusic.getmArtist());
 
                PropertyValuesHolder pvhTranslationX = PropertyValuesHolder.ofFloat(
                        "translationX", 100, 0f);
 
                PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0.1f, 1.0f);

                ObjectAnimator translationName = ObjectAnimator.ofPropertyValuesHolder(
                        mTextViewMusicName, pvhTranslationX, alpha).setDuration(300);
                ObjectAnimator translationArtist = ObjectAnimator.ofPropertyValuesHolder(
                        mTextViewArtist, pvhTranslationX, alpha).setDuration(300);

                translationName.setInterpolator(new OvershootInterpolator(2.5f));
                translationArtist.setInterpolator(new OvershootInterpolator(2.5f));
                translationArtist.setStartDelay(70);
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.play(translationName).with(translationArtist);
                animatorSet.start();
            }
        }, 50);

    }
    
    
    public void hideMusicPlayer(boolean anim) {

        if (anim) {
            
            mHandle.postDelayed(new Runnable() {
                
                @Override
                public void run() {
                    
                    PropertyValuesHolder pvhTranslationX = PropertyValuesHolder.ofFloat("translationX",
                            0f, 100);
                    
                    PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0.0f);
                    
                    ObjectAnimator translationName = ObjectAnimator.ofPropertyValuesHolder(
                            mTextViewMusicName, pvhTranslationX, alpha).setDuration(300);
                    ObjectAnimator translationArtist = ObjectAnimator.ofPropertyValuesHolder(
                            mTextViewArtist, pvhTranslationX, alpha).setDuration(300);
                    
                    translationArtist.setStartDelay(70);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.play(translationName).with(translationArtist);
                    animatorSet.start();

                }
            }, 50);

        } else {
            mTextViewMusicName.setAlpha(0f);
            mTextViewArtist.setAlpha(0f);
        }

    }
    
    
    

    public ArcLayout getmArcLayout() {
        return mArcLayout;
    }
    public void setmArcLayout(ArcLayout mArcLayout) {
        this.mArcLayout = mArcLayout;
    }

    public AmigoKeyguardInfoZone getmInfozone() {
        return mInfozone;
    }

    public void setmInfozone(AmigoKeyguardInfoZone mInfozone) {
        this.mInfozone = mInfozone;
    }

    public WebView getmWebView() {
        return mWebView;
    }

    public void setmWebView(WebView mWebView) {
        this.mWebView = mWebView;
    }


    public View getmWebViewContainer() {
        return mWebViewContainer;
    }


    public void setmWebViewContainer(View mWebViewContainer) {
        this.mWebViewContainer = mWebViewContainer;
    }


    public View getmCloseLinkLayout() {
        return mCloseLinkLayout;
    }


    public void setmCloseLinkLayout(View mCloseLinkLayout) {
        this.mCloseLinkLayout = mCloseLinkLayout;
    }


    public CaptionsView getmCaptionsView() {
        return mCaptionsView;
    }


    public void setmCaptionsView(CaptionsView mCaptionsView) {
        this.mCaptionsView = mCaptionsView;
    }

    
    public RelativeLayout getmLayoutPlayer() {
        return mPlayerLayout;
    }


    public void setmLayoutPlayer(RelativeLayout mLayoutPlayer) {
        this.mPlayerLayout = mLayoutPlayer;
    }
    
    public Wallpaper getmCurrentWallpaper() {
        return mCurrentWallpaper;
    }
    
    public void setmCurrentWallpaper(Wallpaper mCurrentWallpaper) {
        this.mCurrentWallpaper = mCurrentWallpaper;
    }
    
    public TextView getmTextViewTip() {
        return mTextViewTip;
    }
    public void setmTextViewTip(TextView mTextViewTip) {
        this.mTextViewTip = mTextViewTip;
    }
    public KeyguardListView getmKeyguardListView() {
        return mKeyguardListView;
    }
    public void setmKeyguardListView(KeyguardListView mKeyguardListView) {
        this.mKeyguardListView = mKeyguardListView;
    }
    public PlayerButton getmPlayerButton() {
        return mPlayerButton;
    }
    public void setmPlayerButton(PlayerButton mPlayerButton) {
        this.mPlayerButton = mPlayerButton;
    }
    
    public View getmKeyguardNotification() {
        return mKeyguardNotificationView;
    }
    
    public void setmKeyguardNotificationView(View mKeyguardNotification) {
        this.mKeyguardNotificationView = mKeyguardNotification;
    }
    
    
    
    public KeyguardWallpaperContainer getmKeyguardWallpaperContainer() {
        return mKeyguardWallpaperContainer;
    }
    public void setmKeyguardWallpaperContainer(KeyguardWallpaperContainer mKeyguardWallpaperContainer) {
        this.mKeyguardWallpaperContainer = mKeyguardWallpaperContainer;
    }
    public float getNotificationMarginTop() {
        return mNotificationMarginTop;
    }
    public void setNotificationMarginTop(float mNotificationMarginTop) {
        this.mNotificationMarginTop = mNotificationMarginTop;
    }
    
    
    
    public CategoryActivity getCategoryActivity() {
        return categoryActivity;
    }
    public void setCategoryActivity(CategoryActivity categoryActivity) {
        this.categoryActivity = categoryActivity;
    }
    
    public boolean isScreenOff() {
        return mScreenOff;
    }
    
    public void setScreenOff(boolean mScreenOff) {
        this.mScreenOff = mScreenOff;
    }
     
 
    public void setKeyguardSettingsActivity(KeyguardSettingsActivity mKeyguardSettingsActivity) {
        this.mKeyguardSettingsActivity = mKeyguardSettingsActivity;
    }
    public AmigoKeyguardHostView getAmigoKeyguardHostView() {
        return mAmigoKeyguardHostView;
    }
    public void setAmigoKeyguardHostView(AmigoKeyguardHostView mAmigoKeyguardHostView) {
        this.mAmigoKeyguardHostView = mAmigoKeyguardHostView;
    }
    
    
    public TextView getmTextViewMusicName() {
        return mTextViewMusicName;
    }
    
    public void setmTextViewMusicName(TextView mTextViewMusicName) {
        this.mTextViewMusicName = mTextViewMusicName;
    }
    
    public TextView getmTextViewArtist() {
        return mTextViewArtist;
    }
    
    public void setmTextViewArtist(TextView mTextViewArtist) {
        this.mTextViewArtist = mTextViewArtist;
    }
    
    public boolean lockWallpaper(final Context context,final Wallpaper wallpaper) {
    	boolean success = false;
    	if(isLocalData){
    		success = lockWhenLocalData(context,wallpaper);
    	}else{
            success = lockWhenNotLocalData(context, wallpaper);
    	}
        return success;
    }
    
    private boolean lockWhenLocalData(Context context,Wallpaper wallpaper){
    	if(mLockWallpaper == null || (mLockWallpaper.getImgId() != wallpaper.getImgId())){
        	if(mLockWallpaper != null){
        		HorizontalAdapter adapter = (HorizontalAdapter) mKeyguardListView.getAdapter();
        		WallpaperList wallpaperList = adapter.getWallpaperList();
        		wallpaperList.remove(mLockWallpaper);
        		wallpaperList.add(mLockWallpaper.getImgId(),mLockWallpaper);
        		mLockWallpaper.setLocked(false);
        	}
        	wallpaper.setLocked(true);
        	mLockWallpaper = wallpaper;
    		Common.setLockID(context, wallpaper.getImgId());
    		int page = mKeyguardListView.getPage();
    		Common.setLockPosition(context, page);
    	}
    	return true;
    }
    
	private boolean lockWhenNotLocalData(final Context context,
			final Wallpaper wallpaper) {
		boolean saveSuccess = false;
        DebugLog.d(TAG,"save wallpaper setOnClickListener wallpaper url:" + wallpaper.getImgUrl());
        WallpaperDB wallpaperDB = WallpaperDB.getInstance(context.getApplicationContext());
        boolean success = false;
        DebugLog.d(TAG,"save wallpaper setOnClickListener wallpaper 1");
        Wallpaper oldWallpaper = wallpaperDB.queryPicturesDownLoadedLock();
        DebugLog.d(TAG,"save wallpaper setOnClickListener wallpaper oldWallpaper:" + oldWallpaper);
        if(oldWallpaper != null){
            oldWallpaper.setShowOrder(oldWallpaper.getRealOrder());
            wallpaperDB.updateShowOrder(oldWallpaper);
            oldWallpaper.setLocked(false);
            wallpaperDB.updateLock(oldWallpaper);
            clearAllLock();
        }
        wallpaper.setLocked(true);
        DebugLog.d(TAG,"save wallpaper setOnClickListener wallpaper wallpaper id:" + wallpaper.getImgId());
        success = WallpaperDB.getInstance(context.getApplicationContext()).updateLock(wallpaper);
        DebugLog.d(TAG,"save wallpaper setOnClickListener success:" + success);
        delNotTodayWallpaper(context);
		return success;
	}
        
    public boolean clearLock(Context context,Wallpaper wallpaper){
    	boolean success = false;
    	if(isLocalData){
    		Common.setLockID(context, -1);
    		Common.setLockPosition(context, -1);
    		mLockWallpaper.setLocked(false);
    		mLockWallpaper = null;
    		success = true;
    	}else{
            WallpaperDB wallpaperDB = WallpaperDB.getInstance(context.getApplicationContext());
            wallpaper.setLocked(false);
            success = wallpaperDB.updateLock(wallpaper);
            wallpaper.setShowOrder(wallpaper.getRealOrder());
            wallpaperDB.updateShowOrder(wallpaper);
            delNotTodayWallpaper(context);
    	}
        return success;
    }
    
    private void delNotTodayWallpaper(Context context){
        WallpaperDB wallpaperDB = WallpaperDB.getInstance(context.getApplicationContext());
//        boolean hasNeedToDel = wallpaperDB.queryHasWallpaperNotTodayAndNotLock();
//        if(hasNeedToDel){
            wallpaperDB.deleteWallpaperNotTodayAndNotLock();
//        }
    }
    
    private void clearAllLock(){
        	HorizontalAdapter adapter = (HorizontalAdapter) mKeyguardListView.getAdapter();
        	adapter.clearAllLock();
    }
    
    private Wallpaper mLockWallpaper = null;
    public void setLockWallpaper(Wallpaper wallpaper){
    	mLockWallpaper = wallpaper;
    }
    
    public Wallpaper getLockWallpaper(){
    	return mLockWallpaper;
    }
    
    private boolean isLocalData = false;
    public void setLocalData(boolean isLocal){
    	isLocalData = isLocal;
    }
    
    public boolean getLocalData(){
    	return isLocalData;
    }
    
}
