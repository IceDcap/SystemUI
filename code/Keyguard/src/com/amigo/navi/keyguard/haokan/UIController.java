
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
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.PowerManager.WakeLock;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.amigo.navi.keyguard.AmigoKeyguardBouncer;
import com.amigo.navi.keyguard.AmigoKeyguardHostView;
import com.amigo.navi.keyguard.AmigoKeyguardPage;
import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.Guide;
import com.amigo.navi.keyguard.Guide.GuideState;
import com.amigo.navi.keyguard.KeyguardViewHost;
import com.amigo.navi.keyguard.KeyguardViewHostManager;
import com.amigo.navi.keyguard.haokan.PlayerManager.State;
import com.amigo.navi.keyguard.haokan.db.DataConstant;
import com.amigo.navi.keyguard.haokan.db.WallpaperDB;
import com.amigo.navi.keyguard.haokan.entity.Category;
import com.amigo.navi.keyguard.haokan.entity.Music;
import com.amigo.navi.keyguard.haokan.entity.Wallpaper;
import com.amigo.navi.keyguard.haokan.entity.WallpaperList;
import com.amigo.navi.keyguard.haokan.menu.ArcLayout;
import com.amigo.navi.keyguard.infozone.AmigoKeyguardInfoZone;
import com.amigo.navi.keyguard.network.local.utils.DiskUtils;
import com.amigo.navi.keyguard.picturepage.adapter.HorizontalAdapter;
import com.amigo.navi.keyguard.picturepage.widget.KeyguardListView;
import com.amigo.navi.keyguard.picturepage.widget.HorizontalListView.OnTouchlListener;
import com.amigo.navi.keyguard.settings.KeyguardSettingsActivity;
import com.amigo.navi.keyguard.settings.KeyguardWallpaper;
import com.amigo.navi.keyguard.util.VibatorUtil;
import com.android.keyguard.KeyguardHostView.OnDismissAction;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.R;

import java.io.File;
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
    
    
    private View mWebViewContainer;
    
    private View mCloseLinkLayout;
    
    private CaptionsView mCaptionsView;
    
    private RelativeLayout mPlayerLayout;
    
//    private boolean mWebViewShowing = false;
    
    private static UIController instance = null;
    
    private Handler mHandle = new Handler();
    
    private ArcLayout mArcLayout;
    
    private KeyguardListView mKeyguardListView;
    
    private AmigoKeyguardPage mAmigoKeyguardPage;
    
    private View mKeyguardNotificationView;
    
    private float mNotificationMarginTop = 0;
    
    private Wallpaper mCurrentWallpaper;
    
    private float mInfozoneTranslationX = 0f;
    
    private ObjectAnimator[] infoZoneAnimators = new ObjectAnimator[4];
    
    
    private KeyguardViewHost mKeyguardViewHost;
    
    private KeyguardWallpaperContainer mKeyguardWallpaperContainer;
    
    private RelativeLayout mArcMenu;
    
    private CategoryActivity categoryActivity;
    private KeyguardSettingsActivity mKeyguardSettingsActivity;
    
    private DetailActivity mDetailActivity;
    
    private AmigoKeyguardHostView mAmigoKeyguardHostView;
    public static final int SCROLL_TO_UNLOCK=0;
    public static final int SCROLL_TO_SECURTY=1;
    public static final int SECURITY_SUCCESS_UNLOCK=2;
    
    private WakeLock mWakeLock = null;
    
    private TextView mTextViewMusicName, mTextViewArtist;
    
    private RelativeLayout mHaoKanLayout;
    

    private boolean mGuideLongPressShowing = false;
    
    private GuideLondPressLayout mGuideLongPressLayout;
    
    private RelativeLayout toastView;
    
    private AmigoKeyguardBouncer mKeyguardBouncer;
 
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

    public boolean isSecure() {
        return getAmigoKeyguardHostView().isSecure();
    }
    
    public void startCategoryActivity(final Context context) {

        Intent intent = new Intent(context, CategoryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        context.startActivity(intent);

    }

    public void startSettingsActivity(final Context context) {

//        Intent intent = new Intent(context, KeyguardSettingsActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//        context.startActivity(intent);
        
        
        
        if (UIController.getInstance().isSecure()) {

            KeyguardViewHostManager.getInstance().dismissWithDismissAction(new OnDismissAction() {
                @Override
                public boolean onDismiss() {
                    Intent intent = new Intent(context, KeyguardSettingsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    context.startActivity(intent);
                    
                    return true;
                }
            }, true);
        } else {

            Intent intent = new Intent(context, KeyguardSettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
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


    
    
     
    
    public boolean isArcExpanded() {
        if (mArcLayout != null) {
            boolean bool = mArcLayout.isExpanded() || isArcExpanding;
            return bool;
        }
        return false;
    }
    
    public void onBackPress() {

    }
    
    
     
    public void showToast(int resId) {
        Context context = getmKeyguardViewHost().getContext();
        if (toastView == null) {
            LayoutInflater inflate = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            toastView = (RelativeLayout) inflate.inflate(R.layout.keyguard_toast, null);
        }
        TextView toastText = (TextView) toastView.findViewById(R.id.toast_text);
        toastText.setText(resId);
        
        if (getmKeyguardViewHost().indexOfChild(toastView) == -1) {
            FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            params.topMargin = context.getResources().getDimensionPixelSize(R.dimen.haokan_tip_margin_top);
            getmKeyguardViewHost().addView(toastView, params);
        }
        
        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1.0f);
        alphaAnimation.setDuration(300);
        alphaAnimation.setFillAfter(true);
        toastView.startAnimation(alphaAnimation);
 
        mHandle.postDelayed(new Runnable() {

            @Override
            public void run() {
                
                AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0f);
                alphaAnimation.setDuration(300);
                alphaAnimation.setFillAfter(true);
                toastView.startAnimation(alphaAnimation);
                alphaAnimation.setAnimationListener(new AnimationListener() {
                    
                    @Override
                    public void onAnimationStart(Animation arg0) {
                        
                    }
                    
                    @Override
                    public void onAnimationRepeat(Animation arg0) {
                        
                    }
                    
                    @Override
                    public void onAnimationEnd(Animation arg0) {
                        getmKeyguardViewHost().removeView(toastView);
                    }
                });
            }
        }, 2000);
        
        
 
    }
 
    
    @Override
    public void OnTouchMove(int x, int dx) {
        
        if (Common.isPowerSaverMode()) {
            return;
        }
        
        final float infozoneMaxTranslationX = mInfozone.getMaxTranslationX();
        final float playerMaxTranslationX = mPlayerButton.getMaxTranslationX();
        
        final View time = mInfozone.getTimeView();
        final View week = mInfozone.getWeekView();
        final View dateFestival = mInfozone.getDateFestivalView();
        
        if (mInfozoneTranslationX == 0f) {

            getmCaptionsView().cancelAnimator();
            getmCaptionsView().hideGuideIfNeed();
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
            KeyguardUpdateMonitor.getInstance(getmKeyguardNotification().getContext()).getNotificationModule().removeAllNotifications();
            if (Guide.needGuideSlideAround() && Guide.getGuideState() == GuideState.SLIDE_AROUND) {
                getAmigoKeyguardPage().stopGuideSlideAround();
            }

            if (Guide.needGuideSlideFeedBack() && Guide.isIdle()) {
                getAmigoKeyguardPage().setGuideSlideFeedBackVisibility(View.VISIBLE);
            }
            
        }
        
        mInfozoneTranslationX +=  dx;
        float translationX = Math.abs(mInfozoneTranslationX);

        if (translationX <= infozoneMaxTranslationX * 4.0f) {
            
            time.setTranslationX(translationX / 4.0f);
            week.setTranslationX(translationX / 4.0f);
            dateFestival.setTranslationX(translationX / 4.0f);
            
            if (mPlayerLayout.getVisibility() == View.VISIBLE) {
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
    
    private boolean mNewWallpaperToDisplay = false;
    
    
    @Override
    public void OnTouchUp(boolean change) {
        if (Common.isPowerSaverMode()) {
            return;
        }
//        if (change) {
//            getmCaptionsView().setContentVisibilityAnimation(false);
//        }
        mInfozoneTranslationX = 0f;
        refreshWallpaperInfo();
        mCaptionsView.OnTouchUpAnimator();
        
        getAmigoKeyguardPage().setGuideSlideFeedBackVisibility(View.GONE);

        if (Guide.needGuideSlideAround()) {
             
            Guide.setNeedGuideSlideAround(false);
            Guide.setBooleanSharedConfig(getmKeyguardViewHost().getContext(),
                    Guide.GUIDE_SLIDE_AROUND, false);
        }
        
        if (Guide.getGuideState() == GuideState.NEW_WALLPAPER) {
            getAmigoKeyguardPage().stopGuideNewWallpaper();
        }
 
        if (Guide.needGuideLongPress() && !Guide.needGuideScrollUp() && !Guide.needGuideSlideAround()
                && Guide.isIdle()) {
            getmKeyguardViewHost().postDelayed(mGuideLongPressRunnable, 500);
        }
    }
    
    Runnable mGuideLongPressRunnable = new Runnable() {
        
        @Override
        public void run() {
            showGuideLongPress();
        }
    };
    
    
    
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
            
            onScreenTurnedOnAnimation();
            
            if (!Guide.needGuideScrollUp() && Guide.needGuideSlideAround() && Guide.isIdle()) {
                DebugLog.d("guide", "startGuideSlideAround");
                getAmigoKeyguardPage().startGuideSlideAround();
            }
            
            if (isNewWallpaperToDisplay() && Guide.needGuideNewWallpaper()) {

                DebugLog.d("guide", "isNewWallpaperToDisplay");
                HorizontalAdapter mWallpaperAdapter = (HorizontalAdapter) getmKeyguardListView()
                        .getAdapter();
                WallpaperList list = mWallpaperAdapter.getWallpaperList();
                boolean hasLocked = false;

                for (Wallpaper wallpaper : list) {
                    if (wallpaper.isLocked()) {
                        hasLocked = true;
                        break;
                    }
                }
                
                if (list.size() > 1) {
                    DebugLog.d("guide",
                            "hasLocked = " + hasLocked + " Guide.isIdle() = " + Guide.isIdle());
                    if (hasLocked && Guide.isIdle()) {
                        getAmigoKeyguardPage().startGuideNewWallpaper();
                        setNewWallpaperToDisplay(false);
                    }
                }

            }
            
            
        }
    }
    
    private boolean mScreenOff = false;
    
    /** Screen Turned Off */
    public void onScreenTurnedOff() {

        DebugLog.d(TAG, "UIController onScreenTurnedOff");
        setScreenOff(true);
        
        if (mArcLayout != null) {
            if (mArcLayout.isExpanded() || isArcExpanding) {
                DebugLog.d(TAG, "onScreenTurnedOff ArcLayout reset");
                cancelRunningAnimators();
                mArcLayout.reset();
            }
        }
        
        onKeyguardLocked();
        
        onScreenTurnedOffAnimation();
        
    }
    
    public void onKeyguardLocked() {
     
        if (categoryActivity != null) {
            if (!categoryActivity.isDestroyed()) {
                DebugLog.d(TAG, "categoryActivity is not destroyed()");
                categoryActivity.finish();
                categoryActivity = null;
            }
        }
        
        if (mKeyguardSettingsActivity != null) {
            if (!mKeyguardSettingsActivity.isDestroyed()) {
                DebugLog.d(TAG, "mKeyguardSettingsActivity is not destroyed()");
                mKeyguardSettingsActivity.finish();
                mKeyguardSettingsActivity = null;
            }
        }
        

        
        if (getDetailActivity() != null) {
            if (!getDetailActivity().isDestroyed()) {
                DebugLog.d(TAG, "getDetailActivity is not destroyed()");
                getDetailActivity().finish();
                setDetailActivity(null);
            }
        }
        
        if (Guide.needGuideScrollUp() && Guide.isIdle() && !getKeyguardBouncer().isSimSecure()) {
            getAmigoKeyguardPage().addGuideScrollUpView();
        }
        

    }
    
    
    public void refreshWallpaperInfo() {
        int pos = getmKeyguardListView().getNextPage();
        DebugLog.d(TAG,"refreshWallpaperInfo pos:" + pos);
        DebugLog.d(TAG, "refreshWallpaperInfo getPage = " + pos);
        HorizontalAdapter mWallpaperAdapter = (HorizontalAdapter) getmKeyguardListView().getAdapter(); 
        WallpaperList list = mWallpaperAdapter.getWallpaperList();
        Wallpaper wallpaper = null;
        if (list.size() > pos) {
            wallpaper = list.get(pos);
        }
        
        if (getDetailActivity() != null) {
            if (!getDetailActivity().isDestroyed()) {
                DebugLog.d(TAG, "getDetailActivity is not destroyed()");
                getDetailActivity().finish();
                setDetailActivity(null);
            }
        }
        
        if (wallpaper != null) {
            DebugLog.d(TAG, "refreshWallpaperInfo() : pos = " + pos + "  " + wallpaper.toString());
            
            if (getmCurrentWallpaper() != null) {
                if (getmCurrentWallpaper().getImgId() == wallpaper.getImgId()
                        && getmCurrentWallpaper().getImgId() != Wallpaper.WALLPAPER_FROM_PHOTO_ID) {
                    DebugLog.d(TAG, "getmCurrentWallpaper().getImgId() == wallpaper.getImgId()  return");
                    return;
                }
            }
            
            setmCurrentWallpaper(wallpaper);
            
            PlayerManager playerManager = PlayerManager.getInstance();
            boolean musicIsExist = wallpaper.getMusic() != null;
            if (playerManager.getState() == State.NULL) {
                setPlayerLayoutVisibility(musicIsExist);
            }
     
            PlayerManager.getInstance().setmCurrentMusic(wallpaper.getMusic());
     
            mInfozone.setFestivalText(wallpaper.getFestival());
            mCaptionsView.setContentText(wallpaper.getCaption());
        }
        

    }
    
    
    public void onDown(MotionEvent e) {
        
        if (mArcLayout != null) {
            if (mArcLayout.isExpanded() && !mArcLayout.animatorRunning()) {
                DebugLog.d(TAG, "ACTION_DOWN  & startHide");
                mArcLayout.startHide();
            }
        }
    }
    
    boolean isArcExpanding = false;
    
    public void setArcExpanding(boolean isArcExpanding) {
        this.isArcExpanding = isArcExpanding;
    }
    
    private void cancelRunningAnimators() {
        List<Animator> animators = mAnimators;
        
        for (Animator animator : animators) {
            if (animator.isRunning()) {
                animator.cancel();
            }
        }
        animators.clear();
    }
    
    public void onLongPress(float motionDowmX,float motionDowmY) {
        
        
        if (mArcLayout.animatorRunning() || mArcLayout.isExpanded()
                || getAmigoKeyguardHostView().getScrollY() != 0) {
            return;
        }
        
        if (getmCurrentWallpaper() == null) {
            return;
        }
        
        if (!mArcLayout.requestLayout(motionDowmX, motionDowmY)) {
            return;
        }
        
        VibatorUtil.amigoVibrate(mArcLayout.getContext().getApplicationContext(),
                VibatorUtil.LOCKSCREEN_STORYMODE_DISPLAY, 100);
        
        cancelRunningAnimators();
        KeyguardUpdateMonitor.getInstance(getmKeyguardNotification().getContext())
                .getNotificationModule().removeAllNotifications();
        mArcLayout.setmWallpaper(getmCurrentWallpaper());
        mArcLayout.refreshItemState();
        isArcExpanding = true;
        mArcLayout.startShow();
        
        if (Guide.needGuideLongPress()) {
            Guide.setNeedGuideLongPress(false);
            Guide.setBooleanSharedConfig(getmKeyguardViewHost().getContext(),
                    Guide.GUIDE_LONG_PRESS, false);
        }
        
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
    
   private int mKeyguardScrollY = 0;
   
   private void cancelAnimatorsOnUnlock(int top,int maxBoundY) {
       if (mKeyguardScrollY == 0) {
           getmCaptionsView().cancelAnimator();
            if (Guide.needGuideScrollUp() && Guide.getGuideState() == GuideState.SCROLL_UP) {
                getAmigoKeyguardPage().setGuideScrollUpVisibility(View.GONE);
            }
       }
        
       mKeyguardScrollY = top;
       if (top == maxBoundY) {
           mKeyguardScrollY = 0;
       }
   }
   
    public void onKeyguardScrollChanged(int top,int maxBoundY,int model) {

        
        cancelAnimatorsOnUnlock(top, maxBoundY);
        
        float captionsAlpha = 0f;
        float infozoneAlpha = 0f;
        float playerAlpha = 0f;
        float notificationAlpha = 0f;
        
        float captionScrollHeight = maxBoundY * 0.3f;
        
        if (top <= 50) {
            playerAlpha = 1.0f - top / 50.0f;
           
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
            
            if (Guide.needGuideScrollUp() && Guide.getGuideState() == GuideState.SCROLL_UP) {
                getAmigoKeyguardPage().setGuideScrollUpVisibility(View.VISIBLE);
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
    
    public void securityViewAlphaAnimationUpdating(float alpha){
    	mKeyguardWallpaperContainer.setAlpha(alpha);
    	mInfozone.setAlpha(alpha);
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
        
        DebugLog.d(TAG, "hideKeyguardNotification view.getAlpha() = " + view.getAlpha());
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
         
            final View view = hkCaptionsView.getTitleView();
            
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
        animatorSet.addListener(new AnimatorListener() {
            
            private boolean isCancel  = false;
            
            @Override
            public void onAnimationStart(Animator arg0) {
                
            }
            
            @Override
            public void onAnimationRepeat(Animator arg0) {
                
            }
            
            @Override
            public void onAnimationEnd(Animator arg0) {
                
                if (!isCancel) {
                    
                    if (Guide.needGuideClickTitle() && !Guide.needGuideSlideAround() && !Guide.needGuideLongPress() && Guide.isIdle()) {
                        DebugLog.d("guide", "showGuideClickTitle");
                        getmCaptionsView().startGuide();
                    }
                    if (getmCaptionsView().isGuideClickViewShowing()) {
                        DebugLog.d("guide", "isGuideClickViewShowing showGuideIfNeed");
                        getmCaptionsView().showGuideIfNeed();
                    }
                    
                }
                isCancel = false;
            }
            
            @Override
            public void onAnimationCancel(Animator arg0) {
                isCancel = true;
            }
        });
        
        animatorSet.setStartDelay(200);
        animatorSet.start();
    }
    
    
    public void showMusicPlayer(final Music mCurrentMusic) {

        mHandle.postDelayed(new Runnable() {

            @Override
            public void run() {

                final String musicName = mCurrentMusic.getmMusicName();
                final String musicArtist = mCurrentMusic.getmArtist();
                boolean isEmptyMusicName = TextUtils.isEmpty(musicName);
                boolean isEmptyMusicArtist = TextUtils.isEmpty(musicArtist);
                
                PropertyValuesHolder pvhTranslationX = PropertyValuesHolder.ofFloat(
                        "translationX", 100, 0f);
                PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0.1f, 1.0f);

                AnimatorSet animatorSet = new AnimatorSet();

                if (!isEmptyMusicName) {
                    mTextViewMusicName.setText(musicName);
                    mTextViewMusicName.setVisibility(View.VISIBLE);
                    ObjectAnimator translationName = ObjectAnimator.ofPropertyValuesHolder(
                            mTextViewMusicName, pvhTranslationX, alpha).setDuration(300);
                    translationName.setInterpolator(new OvershootInterpolator(2.5f));
                    animatorSet.play(translationName);
                }else {
                    mTextViewMusicName.setVisibility(View.GONE);
                }
                
                if (!isEmptyMusicArtist) {
                    mTextViewArtist.setText(musicArtist);
                    mTextViewArtist.setVisibility(View.VISIBLE);
                    ObjectAnimator translationArtist = ObjectAnimator.ofPropertyValuesHolder(
                            mTextViewArtist, pvhTranslationX, alpha).setDuration(300);
                    translationArtist.setInterpolator(new OvershootInterpolator(2.5f));
                    translationArtist.setStartDelay(70);
                    animatorSet.play(translationArtist);
                }else {
                    mTextViewArtist.setVisibility(View.GONE);
                }

                if (!isEmptyMusicArtist || !isEmptyMusicName) {
                    animatorSet.start();
                }
            }
        }, 50);

    }
    
    public void setPlayerLayoutVisibility(boolean musicIsExist) {

        if (musicIsExist) {
            if (mPlayerLayout.getVisibility() != View.VISIBLE) {
                mPlayerLayout.setVisibility(View.VISIBLE);
            }
        } else {
            if (mPlayerLayout.getVisibility() == View.VISIBLE) {
                mPlayerLayout.setVisibility(View.GONE);
            }
        }
    }
    
    public void hideMusicPlayer(boolean anim, final boolean hasMusic) {

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
                    animatorSet.addListener(new AnimatorListener() {
                        
                        @Override
                        public void onAnimationStart(Animator arg0) {
                            
                        }
                        
                        @Override
                        public void onAnimationRepeat(Animator arg0) {
                            
                        }
                        
                        @Override
                        public void onAnimationEnd(Animator arg0) {
                            setPlayerLayoutVisibility(hasMusic);
                        }
                        
                        @Override
                        public void onAnimationCancel(Animator arg0) {
                            
                        }
                    });
                    animatorSet.start();
                    
                }
            }, 50);

        } else {
            mTextViewMusicName.setAlpha(0f);
            mTextViewArtist.setAlpha(0f);
            setPlayerLayoutVisibility(hasMusic);
        }

    }
    
    public void onChangePowerSaverMode(boolean saverMode) {
        int visibility = saverMode ? View.GONE : View.VISIBLE;
        getHaoKanLayout().setVisibility(visibility);
        getmKeyguardListView().setVisibility(visibility);
        getmKeyguardWallpaperContainer().setVisibility(visibility);
        
        if (saverMode && !Guide.isIdle()) {
            if (Guide.getGuideState() == GuideState.NEW_WALLPAPER) {
                getAmigoKeyguardPage().stopGuideNewWallpaper();
            }else if (Guide.getGuideState() == GuideState.SLIDE_AROUND) {
                getAmigoKeyguardPage().stopGuideSlideAround();
            }else if (Guide.getGuideState() == GuideState.CLICK_TITLE) {
                getmCaptionsView().stopGuide();
            }
        }
    }
    
    
    
    public boolean isNewWallpaperToDisplay() {
        return mNewWallpaperToDisplay;
    }
    public void setNewWallpaperToDisplay(boolean mNewWallpaperToDisplay) {
        this.mNewWallpaperToDisplay = mNewWallpaperToDisplay;
    }
    
    public void setSrceenLockWallpaper(Context context,Bitmap bitmap, String name) {
    	DebugLog.d(TAG,"setSrceenLockWallpaper bitmap:" + bitmap);
        if(bitmap != null){
        	String key = DiskUtils.constructFileNameByUrl(Wallpaper.WALLPAPER_FROM_PHOTO_URL);
        	String savePath = DiskUtils.getCachePath(context) + File.separator + DiskUtils.WALLPAPER_BITMAP_FOLDER;
        	DebugLog.d(TAG,"setSrceenLockWallpaper key:" + key);
        	DebugLog.d(TAG,"setSrceenLockWallpaper savePath:" + savePath);
        	boolean success = DiskUtils.saveBitmap(bitmap, key, savePath);
        	if(bitmap != null && !bitmap.isRecycled()){
        		bitmap.recycle();
        	}
        	if(success){
        		KeyguardListView keyguardListView = getmKeyguardListView();
        		HorizontalAdapter adapter = (HorizontalAdapter) keyguardListView.getAdapter();
        		adapter.removeCacheByUrl(Wallpaper.WALLPAPER_FROM_PHOTO_URL);
        		Wallpaper wallpaper = new Wallpaper();
                Category category = new Category();
                category.setTypeId(0);
                category.setTypeName("photo");
                wallpaper.setCategory(category);
                wallpaper.setImgId(Wallpaper.WALLPAPER_FROM_PHOTO_ID);
                wallpaper.setImgName(name);
                wallpaper.setImgUrl(Wallpaper.WALLPAPER_FROM_PHOTO_URL);
                wallpaper.setType(Wallpaper.WALLPAPER_FROM_PHOTO);
                wallpaper.setTodayImage(1);
                wallpaper.setLocked(true);
                wallpaper.setRealOrder(0);
                wallpaper.setShowOrder(0);
                wallpaper.setDownloadFinish(DataConstant.DOWNLOAD_FINISH);
                wallpaper.setFavorite(false);
                WallpaperDB.getInstance(context).clearLock();
                WallpaperDB wallpaperDB = WallpaperDB.getInstance(context);    
                Wallpaper oldWallpaper = wallpaperDB.queryPicturesDownLoadedLock();
                DebugLog.d(TAG,"save wallpaper setOnClickListener wallpaper oldWallpaper:" + oldWallpaper);
            	DebugLog.d(TAG,"setSrceenLockWallpaper oldWallpaper:" + oldWallpaper);
                if(oldWallpaper != null){
                    unLockWallpaper(context,oldWallpaper);
                }
                if(!wallpaperDB.queryHasWallpaperFromPhoto()){
                	wallpaperDB.insertWallpaper(0, wallpaper);
                }else{
                	wallpaperDB.updateWallpaper(wallpaper);
                }
        	}
        }      
    }
    
    private void hideGuideLongPress() {

        if (mGuideLongPressLayout == null) {
            return;
        }
        mGuideLongPressLayout.stopGuide();
        setGuideLongPressShowing(false);
    }
    
    
    
    public void showGuideLongPress() {
        if (isGuideLongPressShowing()) {
            return;
        }
        getmKeyguardNotification().setAlpha(0f);
        final int width = getmKeyguardViewHost().getMeasuredWidth();
        final int height = getmKeyguardViewHost().getMeasuredHeight();
        Bitmap background = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(background);
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        getmKeyguardViewHost().draw(canvas);
        background = KeyguardWallpaper.getBlurBitmap(background, 3.0f);
        LayoutInflater inflater = LayoutInflater.from(getmKeyguardViewHost().getContext());
        mGuideLongPressLayout = (GuideLondPressLayout)inflater.inflate(R.layout.guide_long_press, null);
        mGuideLongPressLayout.setBlurBackground(background);
        getmKeyguardViewHost().addView(mGuideLongPressLayout);
        mGuideLongPressLayout.startGuide(); 
    }
    
    
    public RelativeLayout getHaoKanLayout() {
        return mHaoKanLayout;
    }
    public void setHaoKanLayout(RelativeLayout mHaoKanLayout) {
        this.mHaoKanLayout = mHaoKanLayout;
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
    
     
/*	public int getCurrentIndex(boolean isAllowLoad) {
		int currentIndex;
		if (isAllowLoad) {
			currentIndex = mKeyguardListView.getPage();
		} else {
			currentIndex = mKeyguardListView.getPageWhenMoving();
		}
		return currentIndex;
	}*/

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
    
    
    
    public DetailActivity getDetailActivity() {
        return mDetailActivity;
    }
    public void setDetailActivity(DetailActivity mDetailActivity) {
        this.mDetailActivity = mDetailActivity;
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
    
    
    
    public AmigoKeyguardPage getAmigoKeyguardPage() {
        return mAmigoKeyguardPage;
    }
    public void setAmigoKeyguardPage(AmigoKeyguardPage mAmigoKeyguardPage) {
        this.mAmigoKeyguardPage = mAmigoKeyguardPage;
    }
    public boolean isGuideLongPressShowing() {
        return mGuideLongPressShowing;
    }
    public void setGuideLongPressShowing(boolean mGuideLongPressShowing) {
        this.mGuideLongPressShowing = mGuideLongPressShowing;
    }
    public boolean lockWallpaper(final Context context,final Wallpaper wallpaper) {
    	boolean success = false;
//    	if(isLocalData){
//    		success = lockWhenLocalData(context,wallpaper);
//    	}else{
            success = lockWhenNotLocalData(context, wallpaper);
//    	}
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
            unLockWallpaper(context,oldWallpaper);
        }
        wallpaper.setLocked(true);
        DebugLog.d(TAG,"save wallpaper setOnClickListener wallpaper wallpaper id:" + wallpaper.getImgId());
        success = WallpaperDB.getInstance(context.getApplicationContext()).updateLock(wallpaper);
        DebugLog.d(TAG,"save wallpaper setOnClickListener success:" + success);
		return success;
	}
	private boolean unLockWallpaper(final Context context,Wallpaper oldWallpaper) {
		boolean success = false;
		oldWallpaper.setShowOrder(oldWallpaper.getRealOrder());
		oldWallpaper.setLocked(false);
		success = WallpaperDB.getInstance(context).updateShowOrderAndLock(oldWallpaper);
		clearAllLock();
		delNotTodayWallpaper(context);
		return success;
	}
        
    public boolean clearLock(Context context,Wallpaper wallpaper){
    	return unLockWallpaper(context,wallpaper);
    }
    
    private void delNotTodayWallpaper(Context context){
        WallpaperDB wallpaperDB = WallpaperDB.getInstance(context.getApplicationContext());           
        Wallpaper wallpaperNotToday = wallpaperDB.queryWallpaperNotTodayAndNotLock();
        if(wallpaperNotToday != null){
        	String url = wallpaperNotToday.getImgUrl();
        	if(TextUtils.isEmpty(url)){
        		DiskUtils.delFile(context,url);
        	}
        }
        wallpaperDB.deleteWallpaperNotTodayAndNotLock();
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
    public AmigoKeyguardBouncer getKeyguardBouncer() {
        return mKeyguardBouncer;
    }
    public void setKeyguardBouncer(AmigoKeyguardBouncer mKeyguardBouncer) {
        this.mKeyguardBouncer = mKeyguardBouncer;
    }
    
    public void onConfigChange() {
        if (getmCaptionsView() != null && getmCurrentWallpaper() != null) {
            getmCaptionsView().setContentText(getmCurrentWallpaper().getCaption());
        }
    }
    
}
