
package com.amigo.navi.keyguard.haokan;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.Animator.AnimatorListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.KeyguardViewHost;
import com.amigo.navi.keyguard.KeyguardViewHostManager;
import com.amigo.navi.keyguard.haokan.PlayerManager.State;
import com.amigo.navi.keyguard.haokan.db.WallpaperDB;
import com.amigo.navi.keyguard.haokan.entity.Wallpaper;
import com.amigo.navi.keyguard.haokan.entity.WallpaperList;
import com.amigo.navi.keyguard.haokan.menu.ArcLayout;
import com.amigo.navi.keyguard.infozone.AmigoKeyguardInfoZone;
import com.amigo.navi.keyguard.network.FailReason;
import com.amigo.navi.keyguard.network.ImageLoader;
import com.amigo.navi.keyguard.network.ImageLoadingListener;
import com.amigo.navi.keyguard.network.local.DealWithFileFromLocal;
import com.amigo.navi.keyguard.network.local.LocalBitmapOperation;
import com.amigo.navi.keyguard.network.local.LocalFileOperationInterface;
import com.amigo.navi.keyguard.network.local.utils.DiskUtils;
import com.amigo.navi.keyguard.picturepage.adapter.HorizontalAdapter;
import com.amigo.navi.keyguard.picturepage.widget.KeyguardListView;
import com.amigo.navi.keyguard.picturepage.widget.HorizontalListView.OnTouchlListener;
import com.amigo.navi.keyguard.settings.KeyguardSettingsActivity;
import com.amigo.navi.keyguard.settings.KeyguardWallpaper;
import com.amigo.navi.keyguard.util.VibatorUtil;
import com.android.keyguard.KeyguardHostView;
import com.android.keyguard.R;
import java.util.ArrayList;
import java.util.List;

public class UIController implements OnTouchlListener{
    
    private float captionScrollHeight = 500;
    
    private static final String TAG = "haokan";
    private HKMainLayout mHkMainLayout;
    
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
    
    private ObjectAnimator[] infoZoneAnimators = new ObjectAnimator[3];
    
    private boolean mHasMusic = false;
    
    private KeyguardViewHost mKeyguardViewHost;
    
    private KeyguardWallpaperContainer mKeyguardWallpaperContainer;
    
    private HKWebLayout mWebLayout = null;
    
    private Bitmap mWebLayoutBackground = null;
    
    public KeyguardViewHost getmKeyguardViewHost() {
        return mKeyguardViewHost;
    }
    public void setmKeyguardViewHost(KeyguardViewHost mKeyguardViewHost) {
        this.mKeyguardViewHost = mKeyguardViewHost;
    }
    public void startCategoryActivity(Context context){
        Intent intent = new Intent(context, HKCategoryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        KeyguardViewHostManager.getInstance().showBouncerOrKeyguardDone();
        context.startActivity(intent);
    }
    
    public void startSettingsActivity(Context context){
        Intent intent = new Intent(context, KeyguardSettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        KeyguardViewHostManager.getInstance().showBouncerOrKeyguardDone();
        context.startActivity(intent);
    }

    public static UIController getInstance() {
        if (instance == null) {
            instance = new UIController();
        }
        return instance;
    }
    
    public UIController() {
    
    }
    
    
    
    public void showWebView(Context context,String link) {
        
        final KeyguardViewHost keyguardHostView = getmKeyguardViewHost();

        if (mWebLayout == null) {
            mWebLayout = (HKWebLayout) LayoutInflater.from(context).inflate(
                    R.layout.haokan_web_layout, null, true);
        }
        mWebLayout.loadurl(link);
        if (keyguardHostView.indexOfChild(mWebLayout) != -1) {
            keyguardHostView.removeView(mWebLayout);
        }
        
        WebView webView = mWebLayout.getmWebView();
 
        keyguardHostView.addView(mWebLayout);
        
        final CaptionsView captionsView = getmCaptionsView();
        final AmigoKeyguardInfoZone infoZone = getmInfozone();
        
        
        ObjectAnimator animatorCaptionsView = ObjectAnimator.ofFloat(captionsView, "alpha", 1f,0f).setDuration(300);
        ObjectAnimator animatorInfoZone = ObjectAnimator.ofFloat(infoZone, "translationY", 0f,infoZone.getMeasuredHeight()).setDuration(300);
        
        ObjectAnimator animatorWebLayout = ObjectAnimator.ofFloat(mWebLayout, "alpha", 0f,1f).setDuration(100);
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
        animatorWebLayout.setStartDelay(500);
        
        ObjectAnimator animatorWebView = ObjectAnimator.ofFloat(webView, "translationY",webView.getTranslationY(),0f).setDuration(300);
        
        ObjectAnimator animatorCloseLink = ObjectAnimator.ofFloat(mWebLayout.getmButtonCloseLink(), "alpha",0,1f).setDuration(300);
        
        
        AnimatorSet set = new AnimatorSet();
        set.play(animatorInfoZone).with(animatorCaptionsView);//.with(animatorWebLayout).with(animatorWebView).with(animatorCloseLink);
        set.play(animatorWebLayout).after(animatorInfoZone);
        set.play(animatorWebView).after(animatorWebLayout);
        set.play(animatorCloseLink).after(animatorWebLayout);
        set.start();
        
        
        
    }
    
    public void removeWebView() {
        
        final CaptionsView captionsView = getmCaptionsView();
        final AmigoKeyguardInfoZone infoZone = getmInfozone();
        
        AnimatorSet set = new AnimatorSet();
        
        ObjectAnimator animatorCaptionsView = ObjectAnimator.ofFloat(captionsView, "alpha", 0f,1f).setDuration(300);
        ObjectAnimator animatorInfoZone = ObjectAnimator.ofFloat(infoZone, "translationY", infoZone.getTranslationY(),0f).setDuration(400);
        
        
        ObjectAnimator animatorWebView = ObjectAnimator.ofFloat(mWebLayout.getmWebView(), "translationY",0f,mWebLayout.getmWebView().getMeasuredHeight()).setDuration(300);
        
        animatorWebView.addListener(new AnimatorListener() {
            
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
                
                if (mWebLayoutBackground != null && !mWebLayoutBackground.isRecycled()) {
                    mWebLayoutBackground.recycle();
                }
                
            }
            
            @Override
            public void onAnimationCancel(Animator arg0) {
                
            }
        });
        
        ObjectAnimator animatorCloseLink = ObjectAnimator.ofFloat(mWebLayout.getmButtonCloseLink(), "alpha",1,0f).setDuration(300);
        
        
        set.play(animatorWebView).with(animatorCloseLink);
        
        set.play(animatorInfoZone).after(animatorWebView);
        set.play(animatorCaptionsView).after(animatorWebView);
        
        set.start();

    }
    
    
    
    
    public void onShowWebView(String link) {
        
        mWebViewContainer.setVisibility(View.VISIBLE);
        mCloseLinkLayout.setVisibility(View.VISIBLE);
        mWebView.loadUrl(link);
        
        
        mInfozone.setVisibility(View.GONE);
        mCaptionsView.setVisibility(View.GONE);
        mPlayerLayout.setVisibility(View.GONE);
        
        mWebViewShowing = true;
    }
    
    public void onCloseWebView() {
        
        mHkMainLayout.setBackground(null);
        mWebViewContainer.setVisibility(View.GONE);
        mCloseLinkLayout.setVisibility(View.GONE);
        
        
        mInfozone.setVisibility(View.VISIBLE);
        mCaptionsView.setVisibility(View.VISIBLE);
        mPlayerLayout.setVisibility(View.VISIBLE);
        mWebViewShowing = false;
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
        
        final View time = mInfozone.getTimeView();
        final View week = mInfozone.getWeekView();
        final View dateFestival = mInfozone.getDateFestivalView();
        
        if (mInfozoneTranslationX == 0f) {
            
            if (mCaptionsView.animatorSet != null) {
                mCaptionsView.animatorSet.cancel();
            }
            int len = infoZoneAnimators.length;
            for (int i = 0; i < len; i++) {
                infoZoneAnimators[i].cancel();
            }
            
            time.setTranslationX(0);
            time.setTranslationY(0);
            week.setTranslationX(0);
            dateFestival.setTranslationX(0);
            mPlayerLayout.setTranslationX(0);
        }
        
        mInfozoneTranslationX +=  dx;
        float translationX = Math.abs(mInfozoneTranslationX);

        if (translationX <= 400) {
            
            time.setTranslationX(translationX / 4.0f);
            week.setTranslationX(translationX / 4.0f);
            dateFestival.setTranslationX(translationX / 4.0f);
            mPlayerLayout.setTranslationX(translationX / 2.0f);
            float alpha = translationX / 400f;
//            Log.v("zhaowei", "translationX = " + translationX + "  alpha = " + alpha);
            mPlayerLayout.setAlpha(1 - alpha);
            
        }else {
            time.setTranslationX(100);
            week.setTranslationX(100);
            dateFestival.setTranslationX(100);
            
            if (mPlayerLayout.getAlpha() != 0f) {
                mPlayerLayout.setAlpha(0);
            }
        }
        
        translationX = Math.abs(x);
        mCaptionsView.move(translationX);

    }
    
    
    
    @Override
    public void OnTouchUp() {
        mInfozoneTranslationX = 0f;
        refreshWallpaperInfo();
        mCaptionsView.OnTouchUpAnimator();
    }
    
    
    
    public void hideArcMenu() {
        mArcLayout.setVisibility(View.GONE);
    }
    
    /** Screen Turned On */
    public void onScreenTurnedOn() {
        onScreenTurnedOnAnimation();
    }
    
    /** Screen Turned Off */
    public void onScreenTurnedOff() {

        
        if (mArcLayout != null) {
            if (mArcLayout.isExpanded()) {
                mArcLayout.reset();
            }
        }
        
        onScreenTurnedOffAnimation();
        
    }
    
    
    public void onSingleTapUp() {
        
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
            if (getmCurrentWallpaper().getImgId() == wallpaper.getImgId()
                    && getmCurrentWallpaper().getCategory().getTypeId() == wallpaper.getCategory()
                    .getTypeId()) {
                Log.v(TAG, "mCurrentWallpaper.getImgId() == wallpaper.getImgId()  return");
                return;
            }
        }
        
        setmCurrentWallpaper(wallpaper);
        
        PlayerManager playerManager = PlayerManager.getInstance();
        if (playerManager.getState() != State.NULL) {
            Log.v(TAG, "player/pause Music");
            mPlayerButton.setState(State.NULL);
            playerManager.stopMusicPlayer(true);//声音渐缓
            mHkMainLayout.hideMusicPlayer(false);
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
        
         
        
        if (!TextUtils.isEmpty(wallpaper.getFestival())) {
            mInfozone.setFestivalText(wallpaper.getFestival());
        }
        
        mCaptionsView.setContentText(wallpaper.getCaption());
    }
    
    
    public void onDown(MotionEvent e) {
        
        Log.v(TAG, "ACTION_DOWN  & startHide");
        if (mArcLayout != null) {
            if (mArcLayout.isExpanded() && !mArcLayout.animatorRunning()) {
                mArcLayout.startHide();
            }
        }
    }
    
    public void onLongPress(float motionDowmX,float motionDowmY) {
        
        
        if (mArcLayout.animatorRunning() || mArcLayout.isExpanded()) {
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
        
        VibatorUtil.amigoVibrate(mArcLayout.getContext().getApplicationContext(),
                VibatorUtil.LOCKSCREEN_MENU_LONG_PRESS, 100);
        
        hideKeyguardNotification();
        

        DebugLog.d(TAG,"onLongPress wallpaper url:" + getmCurrentWallpaper().getImgUrl());
        DebugLog.d(TAG,"onLongPress wallpaper lock:" + getmCurrentWallpaper().isLocked());

        mArcLayout.setmWallpaper(getmCurrentWallpaper());
        
        mArcLayout.refreshItemState();
        

        mArcLayout.startShow();

    }
    
    public Bitmap getCurrentWallpaperBitmap(Wallpaper wallpaper) {

        HorizontalAdapter mWallpaperAdapter = (HorizontalAdapter) getmKeyguardListView().getAdapter(); 
        Bitmap bitmap = mWallpaperAdapter.getWallpaperByUrl(wallpaper.getImgUrl());
        return bitmap;
        
    }
    
    public Bitmap getCurrentWallpaperBitmap() {
        return getCurrentWallpaperBitmap(mCurrentWallpaper);
    }
    
    
   
    public void onKeyguardScrollChanged(int top,int maxBoundY,boolean isSecure) {

        
        float captionsAlpha = 0f;
        float infozoneAlpha = 0f;
        float playerAlpha = 0f;
        float notificationAlpha = 0f;
        
        if (top <= 50) {
            playerAlpha = 1.0f - top / 50;
        }
        
        if (top <= mNotificationMarginTop) {
            notificationAlpha = 1.0f - top / mNotificationMarginTop;
        }
        
        if (top <= captionScrollHeight) {
            infozoneAlpha = 1f;
            captionsAlpha = 1.0f - top / captionScrollHeight;
        }else {
            infozoneAlpha = 1.0f - (top - captionScrollHeight) / (maxBoundY -captionScrollHeight);
        }

        mCaptionsView.setAlpha(captionsAlpha);
        mPlayerLayout.setAlpha(playerAlpha);
        mKeyguardNotificationView.setAlpha(notificationAlpha);
        
        
        if (top <= 0) {
            mInfozone.setAlpha(1f);
            mCaptionsView.setAlpha(1f);
            mPlayerLayout.setAlpha(1f);
            mKeyguardNotificationView.setAlpha(1f);
        }
         
        if (!isSecure) {
            mInfozone.setAlpha(infozoneAlpha);
        }
        mKeyguardWallpaperContainer.onKeyguardScrollChanged(top, maxBoundY,isSecure);
    }
    
    
    public void hideKeyguardNotification() {

        final View view = getmKeyguardNotification();
        
        if (view == null) {
            return;
        }
        
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f).setDuration(400);
        animator.addListener(new AnimatorListener() {
            
            @Override
            public void onAnimationStart(Animator arg0) {
                if (view.getVisibility() != View.VISIBLE) {
                    view.setVisibility(View.VISIBLE);
                }
            }
            
            @Override
            public void onAnimationRepeat(Animator arg0) {
                
            }
            
            @Override
            public void onAnimationEnd(Animator arg0) {
                view.setVisibility(View.GONE);
            }
            
            @Override
            public void onAnimationCancel(Animator arg0) {
                
            }
        });
        animator.start();
        
    }
    
    public void showKeyguardNotification() {

        final View view = getmKeyguardNotification();
        
        if (view == null) {
            return;
        }
        
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).setDuration(400);
        animator.addListener(new AnimatorListener() {
            
            @Override
            public void onAnimationStart(Animator arg0) {
                if (view.getVisibility() != View.VISIBLE) {
                    view.setVisibility(View.VISIBLE);
                }
            }
            
            @Override
            public void onAnimationRepeat(Animator arg0) {
                
            }
            
            @Override
            public void onAnimationEnd(Animator arg0) {
                view.setVisibility(View.VISIBLE);
            }
            
            @Override
            public void onAnimationCancel(Animator arg0) {
                
            }
        });
        animator.start();
    }
    
    
    private void onScreenTurnedOffAnimation() {

        
        mHandle.postDelayed(new Runnable() {

            @Override
            public void run() {

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
        ObjectAnimator animationDate = ObjectAnimator.ofPropertyValuesHolder(textViewDate, pvhTranslateX).setDuration(1233);
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
        
        ObjectAnimator animationWeek = ObjectAnimator.ofPropertyValuesHolder(textViewWeek, pvhTranslateX).setDuration(1233);
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
 
        int titleFormTranslationY = hkCaptionsView.getAnimFormTranslationY();
        
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0f, 1f);
        PropertyValuesHolder pvhTranslationY = PropertyValuesHolder.ofFloat("translationY", titleFormTranslationY, hkCaptionsView.getInitialTranslationY()); 
        ObjectAnimator animationTitle = ObjectAnimator.ofPropertyValuesHolder(hkCaptionsView, alpha, pvhTranslationY).setDuration(333);  
        
        animationTitle.setStartDelay(733);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(animationUp).with(animationDate).with(animationWeek).with(alphaWeek)
                .with(animationTitle);
        infoZoneAnimators[0] = animationUp;
        infoZoneAnimators[1] = animationWeek;
        infoZoneAnimators[2] = animationDate;
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
    
    
    

    public ArcLayout getmArcLayout() {
        return mArcLayout;
    }
    public void setmArcLayout(ArcLayout mArcLayout) {
        this.mArcLayout = mArcLayout;
    }
    public HKMainLayout getmHkMainLayout() {
        return mHkMainLayout;
    }

    public void setmHkMainLayout(HKMainLayout mHkMainLayout) {
        this.mHkMainLayout = mHkMainLayout;
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
    @SuppressLint("NewApi")
    public Bitmap blurBitmap(Bitmap bitmap,boolean recycle){  
        
        Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);  
        RenderScript rs = RenderScript.create(mHkMainLayout.getContext().getApplicationContext());  
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));  
        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);  
        Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);  
        blurScript.setRadius(25.f);  
        blurScript.setInput(allIn);  
        blurScript.forEach(allOut);  
        allOut.copyTo(outBitmap);  
        if (recycle) {
            bitmap.recycle();  
        }
        rs.destroy();  
        return outBitmap;  
    }  
    
    public boolean lockWallpaper(final Context context,final Wallpaper wallpaper) {
        boolean saveSuccess = false;
        DebugLog.d(TAG,"save wallpaper setOnClickListener wallpaper url:" + wallpaper.getImgUrl());
        WallpaperDB wallpaperDB = WallpaperDB.getInstance(context.getApplicationContext());
        boolean success = false;
        Wallpaper oldWallpaper = wallpaperDB.queryPicturesDownLoadedLock();
        if(oldWallpaper != null){
            oldWallpaper.setShowOrder(oldWallpaper.getRealOrder());
            wallpaperDB.updateShowOrder(oldWallpaper);
            oldWallpaper.setLocked(false);
            wallpaperDB.updateLock(oldWallpaper);
        }
        wallpaper.setLocked(true);
        success = WallpaperDB.getInstance(context.getApplicationContext()).updateLock(wallpaper);
        DebugLog.d(TAG,"save wallpaper setOnClickListener success:" + success);
        delNotTodayWallpaper(context);
        return success;
    }
    /**
     * @param context
     * @param wallpaper
     */
    private void loadBitmap(final Context context, final Wallpaper wallpaper) {
        final ImageLoader imageLoader = new ImageLoader(context.getApplicationContext());
        imageLoader.removeItem(DiskUtils.WALLPAPER_Image_KEY);
        LocalFileOperationInterface localFileOperationInterface = new LocalBitmapOperation();
        DealWithFileFromLocal dealWithFromLocalInterface = new DealWithFileFromLocal(context.getApplicationContext(), 
                  DiskUtils.WALLPAPER_BITMAP_FOLDER, DiskUtils.getCachePath(context.getApplicationContext()),
                  localFileOperationInterface);

        ImageLoadingListener mImageLoadingListener = new ImageLoadingListener() {
            
            @Override
            public void onLoadingStarted(String imageUri) {
                
            }
            
            @Override
            public void onLoadingFailed(String imageUri, FailReason failReason) {
                
            }
            
            @Override
            public void onLoadingComplete(String imageUri, Bitmap loadedImage) {
                DebugLog.d(TAG,"save wallpaper setOnClickListener onLoadingComplete");
                LocalFileOperationInterface localFileOperationInterface = new LocalBitmapOperation();
                DealWithFileFromLocal dealWithFromLocalInterface = new DealWithFileFromLocal(context.getApplicationContext(), 
                        DiskUtils.WALLPAPER_OBJECT_FILE_FOLDER, DiskUtils.getSDPath(context.getApplicationContext()),
                        localFileOperationInterface);
                if(loadedImage != null){
                    boolean success = dealWithFromLocalInterface.writeToLocal(DiskUtils.WALLPAPER_Image_KEY, loadedImage);
                    DebugLog.d(TAG,"save wallpaper setOnClickListener onLoadingComplete success:" + success);
                }
            }
            
            @Override
            public void onLoadingCancelled(String imageUri) {
                
            }
        };
        imageLoader.loadImage(wallpaper.getImgUrl(), mImageLoadingListener, dealWithFromLocalInterface, false);
    }
        
    public boolean clearLock(Context context,Wallpaper wallpaper){
        WallpaperDB wallpaperDB = WallpaperDB.getInstance(context.getApplicationContext());
        wallpaper.setLocked(false);
        boolean success = wallpaperDB.updateLock(wallpaper);
        wallpaper.setShowOrder(wallpaper.getRealOrder());
        wallpaperDB.updateShowOrder(wallpaper);
        delNotTodayWallpaper(context);
        return success;
    }
    
    private void delNotTodayWallpaper(Context context){
        WallpaperDB wallpaperDB = WallpaperDB.getInstance(context.getApplicationContext());
//        boolean hasNeedToDel = wallpaperDB.queryHasWallpaperNotTodayAndNotLock();
//        if(hasNeedToDel){
            wallpaperDB.deleteWallpaperNotTodayAndNotLock();
//        }
    }
    
    
}
