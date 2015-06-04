package com.amigo.navi.keyguard.haokan.menu;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.haokan.Common;
import com.amigo.navi.keyguard.haokan.FileUtil;
import com.amigo.navi.keyguard.haokan.UIController;
import com.amigo.navi.keyguard.haokan.analysis.HKAgent;
import com.amigo.navi.keyguard.haokan.db.WallpaperDB;
import com.amigo.navi.keyguard.haokan.entity.Wallpaper;
import com.amigo.navi.keyguard.util.VibatorUtil;
import com.android.keyguard.R;
import java.util.ArrayList;
import java.util.List;


 
public class ArcLayout extends ViewGroup implements View.OnClickListener{
    private static final String TAG = "haokan";
    
    private boolean mExpandAnimatorRunning = false;
    private boolean mShrinkAnimatorRunning = false;
    private boolean mClicKItemAnimatorRunning = false;
    private boolean mItemFeekbackAnimatorRunning = false;
    
 
    private int[][] mMenuItemNameIds = {
            { R.string.haokan_arc_menu_favorite, R.string.haokan_arc_menu_favorite_ok }, 
            { R.string.haokan_arc_menu_lock, R.string.haokan_arc_menu_lock_ok }, 
            { R.string.haokan_arc_menu_subscribe, -1 }, 
            { R.string.haokan_arc_menu_setting, -1 }};
    
    private static int[] MENU_ITEM_DRAWABLES = {
        R.drawable.arcmenu_favorite_background, R.drawable.arcmenu_locked_background,
        R.drawable.arcmenu_subscribe_background,
        R.drawable.arcmenu_setting_background
    };
    
    private ArcHomeButton mArcHomeButton;
    
    private List<ArcItemButton> mArcItems = new ArrayList<ArcItemButton>();
    
    private int mRadiusMax = 0;
    private int mRadiusNormal = 0;
    private int mEdgeDistance = 0;
    private int mTopDistance = 0;
    private Rect MainRect = new Rect();
    
    
    private UIController controller;
    
    /**
     * children will be set the same size.
     */
    private int mChildSize;


    private int mLayoutPadding = 50;

    public static final float DEFAULT_FROM_DEGREES = -28.0f;

    public static final float DEFAULT_TO_DEGREES = -152.0f;

    private float mFromDegrees = DEFAULT_FROM_DEGREES;

    private float mToDegrees = DEFAULT_TO_DEGREES;


    /* the distance between the layout's center and any child's center */
    private int mRadius;

    private boolean mExpanded = false;
    
    private static int WIDTH_PIXELS = 1080;
    private static int HEIGHT_PIXELS = 1920;
    
    public ArcLayout(Context context) {
        this(context,null);
    }

    private int mHomeButtonSize;
    
    private Wallpaper mWallpaper;
    
    private int infozoneHeight;

    public ArcLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mChildSize = getResources().getDimensionPixelSize(R.dimen.menuChildSize);
        mRadiusMax = getResources().getDimensionPixelSize(R.dimen.haokan_arcmenu_radius_max);
        mHomeButtonSize = getResources().getDimensionPixelSize(R.dimen.haokan_arcmenu_home_size);
        mRadiusNormal = getResources().getDimensionPixelSize(R.dimen.haokan_arcmenu_radius);
        WIDTH_PIXELS = Common.getScreenWidth(getContext());
        HEIGHT_PIXELS = Common.getScreenHeight(getContext());
        
        mEdgeDistance = (int) (mRadiusNormal * Math.cos(Math.toRadians(DEFAULT_FROM_DEGREES)));
        
        mTopDistance = (int) (mRadiusMax * Math.sin(Math.toRadians(25))) + mChildSize / 2;
        
        
        MainRect.set(mEdgeDistance, 0, WIDTH_PIXELS - mEdgeDistance, HEIGHT_PIXELS);
        
        infozoneHeight = getResources().getDimensionPixelSize(R.dimen.ketguard_infozone_height);
        
        initArcMenu();
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        ArcHomeButton arcHomeButton =  (ArcHomeButton) inflater.inflate(R.layout.haokan_arc_home_button, null, true);
        addArcHomeButton(arcHomeButton);
        controller = UIController.getInstance();
        controller.setmArcLayout(this);
        
    }
    

    public Wallpaper getmWallpaper() {
        return mWallpaper;
    }

    public void setmWallpaper(Wallpaper mWallpaper) {
        this.mWallpaper = mWallpaper;
    }
    
    
    public void refreshItemState() {

        if (mWallpaper == null) {
            return;
        }
        
        DebugLog.d(TAG, "refreshItemState() " + mWallpaper.toString());
        ArcItemButton itemFavorite = mArcItems.get(0);
        itemFavorite.setItemSelected(mWallpaper.isFavorite());
         
        ArcItemButton itemLocked = mArcItems.get(1);
        itemLocked.setItemSelected(mWallpaper.isLocked());
        
    }


    private static Rect computeChildFrame(final int centerX, final int centerY, final int radius, final float degrees,
            final int size) {

        final double childCenterX = centerX + radius * Math.cos(Math.toRadians(degrees));
        final double childCenterY = centerY + radius * Math.sin(Math.toRadians(degrees));

        return new Rect((int) (childCenterX - size / 2), (int) (childCenterY - size / 2),
                (int) (childCenterX + size / 2), (int) (childCenterY + size / 2));
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 
        int radius = mRadius;
        final int size = radius * 2 + mChildSize + mLayoutPadding * 2;
        if (mArcItems != null) {
            final int count = mArcItems.size();
            for (int i = 0; i < count; i++) {
                View child = mArcItems.get(i);
                child.measure(MeasureSpec.makeMeasureSpec(mChildSize, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(mChildSize, MeasureSpec.EXACTLY));
            }
        }
        
        if (mArcHomeButton != null) {
            mArcHomeButton.measure(MeasureSpec.makeMeasureSpec(mHomeButtonSize, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(mHomeButtonSize, MeasureSpec.EXACTLY));
        }
        
        setMeasuredDimension(size, size);
    }
    boolean[] needFeekBack = new boolean[]{true,true, false,false };
    
    private void initArcMenu() {
        
        mArcItems.clear();
        final int itemCount = mMenuItemNameIds.length;
        for (int i = 0; i < itemCount; i++) {
            ArcItemButton item = new ArcItemButton(this.getContext());
            item.setTitle(mMenuItemNameIds[i][0]);
            item.setTitleResIds(mMenuItemNameIds[i]);
            item.setBackgroundResource(MENU_ITEM_DRAWABLES[i]);
            item.getmImageView().setId(i);
            item.getmImageView().setOnClickListener(this);
            item.setScaleX(0f);
            item.setScaleY(0f);
            item.setItemSelected(false);
            item.setNeedFeekBack(needFeekBack[i]);
            addView(item);
            mArcItems.add(item);
        }
    }
    
    public void addArcHomeButton(ArcHomeButton arcHomeButton) {
        this.mArcHomeButton = arcHomeButton;
        addView(arcHomeButton);
        
        arcHomeButton.getmImageView().setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View arg0) {

                if (isExpanded() && !animatorRunning()) {
                    DebugLog.d(TAG, "ArcHomeButton ImageView OnClick ");
                    startHide();
                }
            }
        });
    }
    

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int centerX = getWidth() / 2;
        final int centerY = getHeight() / 2;
        
        if (mArcItems != null) {
            
            final int childCount = mArcItems.size();
            Rect frame =  new Rect((int) (centerX - mChildSize / 2), (int) (centerY - mChildSize / 2),
                    (int) (centerX + mChildSize / 2), (int) (centerY + mChildSize / 2));
            for (int i = 0; i < childCount; i++) {
                mArcItems.get(i).layout(frame.left, frame.top, frame.right, frame.bottom);
            }
        }
        
        if (mArcHomeButton != null) {
            mArcHomeButton.layout(centerX - mArcHomeButton.getMeasuredWidth() / 2, centerY
                    - mArcHomeButton.getMeasuredHeight() / 2,
                    centerX + mArcHomeButton.getMeasuredWidth() / 2,
                    centerY + mArcHomeButton.getMeasuredHeight() / 2);
        }
        
    }
     
    
    private AnimatorSet createExpandAnimator(final ArcItemButton child,float fromXDelta, float toXDelta, float fromYDelta, float toYDelta,
            long startOffset, long duration, Interpolator interpolator,final boolean isLast) {
        
        PropertyValuesHolder pvhTranslationX = PropertyValuesHolder.ofFloat("translationX", fromXDelta, toXDelta);  
        PropertyValuesHolder pvhTranslationY = PropertyValuesHolder.ofFloat("translationY", fromYDelta, toYDelta);  
        PropertyValuesHolder pvhscaleX = PropertyValuesHolder.ofFloat("scaleX", 0.5f, 1.0f);
        PropertyValuesHolder pvhscaleY = PropertyValuesHolder.ofFloat("scaleY", 0.5f, 1.0f);
        
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0.0f, 1.0f);
        
        ObjectAnimator Alpha = ObjectAnimator.ofFloat(child.getmTextView(), "alpha", 0.5f, 1f).setDuration(50);
        Alpha.setStartDelay(220);
        
        ObjectAnimator rotation = ObjectAnimator.ofFloat(child, "rotation", -90.0f, 0f).setDuration(210); 
        rotation.setStartDelay(110);
        
        Alpha.addListener(new AnimatorListener() {
            
            @Override
            public void onAnimationStart(Animator arg0) {
 
                child.setAlpha(1f);
                child.getmTextView().setVisibility(VISIBLE);
            }
            
            @Override
            public void onAnimationRepeat(Animator arg0) {
                
            }
            
            @Override
            public void onAnimationEnd(Animator arg0) {
                if (isLast) {
                    mExpanded = true;
                    mExpandAnimatorRunning = false;
                    controller.setArcExpanding(false);
                }
            }
            
            @Override
            public void onAnimationCancel(Animator arg0) {
                
            }
        });
        
        ObjectAnimator translation = ObjectAnimator.ofPropertyValuesHolder(child, pvhTranslationX,
                pvhTranslationY, pvhscaleX, pvhscaleY,alpha).setDuration(320);
        
        AnimatorSet set = new AnimatorSet();
        set.setStartDelay(startOffset);
        set.setInterpolator(interpolator);
        set.play(translation).with(rotation).with(Alpha);
        return set;
    }
    
   
    private  AnimatorSet createShrinkAnimator(final ArcItemButton child, float fromXDelta, float toXDelta, float fromYDelta, float toYDelta,
            long startOffset, long duration, Interpolator interpolator,final boolean isLast) {
        
        PropertyValuesHolder pvhTranslationX = PropertyValuesHolder.ofFloat("translationX", fromXDelta, toXDelta);  
        PropertyValuesHolder pvhTranslationY = PropertyValuesHolder.ofFloat("translationY", fromYDelta, toYDelta);  
        
        PropertyValuesHolder pvhscaleX = PropertyValuesHolder.ofFloat("scaleX", 1.0f, 0f);
        PropertyValuesHolder pvhscaleY = PropertyValuesHolder.ofFloat("scaleY", 1.0f, 0f);
        PropertyValuesHolder pvhalpha = PropertyValuesHolder.ofFloat("alpha", 1.0f, 0f);
        
        ObjectAnimator Alpha = ObjectAnimator.ofFloat(child.getmTextView(), "alpha", 0f).setDuration(10); 
        
        ObjectAnimator rotation = ObjectAnimator.ofFloat(child, "rotation", -180f); 
        
        ObjectAnimator translation = ObjectAnimator.ofPropertyValuesHolder(child, pvhTranslationX, pvhTranslationY,pvhscaleX,pvhscaleY,pvhalpha);  
        
        AnimatorSet set = new AnimatorSet();
        set.setStartDelay(startOffset);
        set.setInterpolator(interpolator);
        set.setDuration(280);
 
        child.getmImageView().setClickable(false);
        set.play(Alpha);
        set.play(translation).with(rotation).after(Alpha);
        set.addListener(new AnimatorListener() {
            
            @Override
            public void onAnimationStart(Animator arg0) {
                
            }
            
            @Override
            public void onAnimationRepeat(Animator arg0) {
                
            }
            
            @Override
            public void onAnimationEnd(Animator arg0) {
//                child.getmTextView().setVisibility(GONE);
                if (isLast) {
                    mShrinkAnimatorRunning = false;
                    mExpanded = false;
                    controller.showKeyguardNotification();
                }
                
                child.getmImageView().setClickable(true);
            }
            
            @Override
            public void onAnimationCancel(Animator arg0) {
                
            }
        });
        return set;
    }
    

 
    private void bindChildAnimation(final ArcItemButton child, final int index, final long duration) {
        
        final boolean expanded = mExpanded;
        final int centerX = getWidth() / 2;
        final int centerY = getHeight() / 2;
        final int childCount = mArcItems.size();
        Interpolator interpolator;
        int startOffset = 50 * index;
        final boolean isLast = index == childCount - 1;
        final boolean isFisrt = index == 0;
        AnimatorSet animatorSet;
        if (!expanded) {
            final int radius = mRadius;
            final float perDegrees = Math.abs(mToDegrees - mFromDegrees) / (childCount - 1);
            Rect frame = computeChildFrame(centerX, centerY, radius, mFromDegrees + index * perDegrees, mChildSize);
            
            final int toXDelta = frame.left - child.getLeft();
            final int toYDelta = frame.top - child.getTop();
            child.setToXDelta(toXDelta);
            child.setToYDelta(toYDelta);
            interpolator = new OvershootInterpolator(3f);
            animatorSet = createExpandAnimator(
                    child, 0, toXDelta, 0, toYDelta, startOffset, duration, interpolator,isFisrt);
            mExpandAnimatorRunning = true;
        }else {
            interpolator =  new AccelerateInterpolator();
            
            animatorSet = createShrinkAnimator(child, child.getToXDelta(), 0,
                    child.getToYDelta(), 0, startOffset, duration, interpolator,isLast);
            mShrinkAnimatorRunning = true;
        }
    
        animatorSet.start();
        controller.addAnimator(animatorSet);
        
    }

    public boolean isExpanded() {
        return mExpanded;
    }
    
 
    
    public boolean requestLayout(float x, float y) {
        
         
        
        float fromDegrees;
        float toDegrees;
        int radius;
        
        if (y >= HEIGHT_PIXELS - infozoneHeight) {
            return false;
        }
        
        if (MainRect.contains((int)x, (int)y)) {
            if (y > mRadiusNormal) {
                fromDegrees = -152;
                toDegrees = -28;
            }else {
                fromDegrees = 28;
                toDegrees = 152;
            }
            radius = mRadiusNormal;
            
        }else if (x < mEdgeDistance) {
            if (y <= mTopDistance) {
                return false;
            }
            
            if (y > HEIGHT_PIXELS / 3) {
                fromDegrees = -62;
                toDegrees = 62;
            }else {
//                fromDegrees = -25;
//                toDegrees = 87;
                fromDegrees = -32;
                toDegrees = 80;
            }
            radius = mRadiusMax;
        }else {
            
            if (y <= mTopDistance) {
                return false;
            }
            
            if (y > HEIGHT_PIXELS / 3) {
                fromDegrees = 118;
                toDegrees = 242;
            }else {
//                fromDegrees = 93;
//                toDegrees = 205;
                
                fromDegrees = 100;
                toDegrees = 212;
            }
            radius = mRadiusMax;
        }
        setRadiusAndDegrees(radius, fromDegrees, toDegrees);
        setTranslationX(x - WIDTH_PIXELS / 2);
        setTranslationY(y - HEIGHT_PIXELS / 2);
        return true;
    }
    
    
    public void setRadiusAndDegrees(int radius, float fromDegrees, float toDegrees) {
        if (mFromDegrees == fromDegrees && mToDegrees == toDegrees && radius == mRadius) {
            return;
        }
        this.mRadius = radius;
        this.mFromDegrees = fromDegrees;
        this.mToDegrees = toDegrees;
        requestLayout();
        invalidate();
    }

    /**
     * switch between expansion and shrinkage
     * 
     * @param showAnimation
     */
    public void switchState() {
        DebugLog.d(TAG, "switchState  mExpanded = " + mExpanded);
        final int childCount = mArcItems.size();
        for (int i = 0; i < childCount; i++) {
            ArcItemButton arcItemButton = mArcItems.get(i);
            bindChildAnimation(arcItemButton, i, 300);
        }
        
        invalidate();
    }

    public void startShow() {

        if (animatorRunning()) {
            return;
        }

        controller.showArcMenu();

        if (mArcHomeButton.getVisibility() != VISIBLE) {
            mArcHomeButton.setVisibility(VISIBLE);
        }

        postDelayed(new Runnable() {
            @Override
            public void run() {
                switchState();
                mArcHomeButton.rippleAnimRun();
            }
        }, 50);

        postDelayed(mCloseMenuRunnable, 5000);
        
    }
    
    
    private Runnable mCloseMenuRunnable = new Runnable() {
        
        @Override
        public void run() {
            if (isExpanded() && !animatorRunning()) {
                startHide();
            }
        }
    };
    
    
    public void startHide() {
        removeCallbacks(mCloseMenuRunnable);
        switchState();
        mArcHomeButton.closeAnimRun();
    }
    
 
    /**
     * 
     * @param clickView
     * @param arcItemButton
     */
    private void bindClickItemAnimator(final View clickView, final ArcItemButton arcItemButton,final ArcMenuAnimatorListener listener) {
        
        mClicKItemAnimatorRunning = true;
        AnimatorSet set = new AnimatorSet();
     
        PropertyValuesHolder clickViewScaleX = PropertyValuesHolder.ofFloat("scaleX", 1.4f);
        PropertyValuesHolder clickViewScaleY = PropertyValuesHolder.ofFloat("scaleY", 1.4f);
        ObjectAnimator objectAnimatorMagnify = ObjectAnimator.ofPropertyValuesHolder(clickView, clickViewScaleX, clickViewScaleY).setDuration(180);
        objectAnimatorMagnify.setInterpolator(new DecelerateInterpolator());
        
        set.play(objectAnimatorMagnify);
        
        AnimatorSet AnimatorSet = new AnimatorSet();
        
        PropertyValuesHolder scaleXNarrow = PropertyValuesHolder.ofFloat("scaleX", 1.0f, 0f);
        PropertyValuesHolder scaleYNarrow = PropertyValuesHolder.ofFloat("scaleY", 1.0f, 0f);
        PropertyValuesHolder alphaFadeOut = PropertyValuesHolder.ofFloat("alpha", 0f);
        List<Animator> list = new ArrayList<Animator>();
        
        clickView.setClickable(false);
        
        int itemCount = mArcItems.size();
        for (int i = 0; i < itemCount; i++) {
            View view = mArcItems.get(i);
            if (view != arcItemButton) {
                ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view, scaleXNarrow, scaleYNarrow, alphaFadeOut).setDuration(200);
                objectAnimator.setInterpolator(new DecelerateInterpolator());
                arcItemButton.getmImageView().setClickable(false);
                list.add(objectAnimator);
            }
        }
      
        ObjectAnimator objectAnimatorNarrow = ObjectAnimator.ofPropertyValuesHolder(mArcHomeButton, scaleYNarrow, scaleXNarrow, alphaFadeOut).setDuration(200);
        objectAnimatorNarrow.setInterpolator(new DecelerateInterpolator());
        list.add(objectAnimatorNarrow);
        
        
        PropertyValuesHolder pvhscaleXNarrow = PropertyValuesHolder.ofFloat("scaleX", 1.4f, 1.0f);
        PropertyValuesHolder pvhscaleYNarrow = PropertyValuesHolder.ofFloat("scaleY", 1.4f, 1.0f);
     
        ObjectAnimator clickViewAnimator = ObjectAnimator.ofPropertyValuesHolder(clickView, pvhscaleXNarrow, pvhscaleYNarrow, alphaFadeOut).setDuration(150);
        clickViewAnimator.setStartDelay(50);
        list.add(clickViewAnimator);
        AnimatorSet.playTogether(list);
        set.play(AnimatorSet).after(objectAnimatorMagnify);
 
        set.addListener(new AnimatorListener() {
            
            @Override
            public void onAnimationStart(Animator arg0) {
                arcItemButton.getmTextView().setVisibility(GONE);
                arcItemButton.getmImageView().setSelected(!arcItemButton.isItemSelected());
            }
            
            @Override
            public void onAnimationRepeat(Animator arg0) {
                
            }
            
            @Override
            public void onAnimationEnd(Animator arg0) {
                
                if (listener != null) {
                    listener.onAnimatorEnd();
                }

                if (!arcItemButton.isNeedFeekBack()) {
                    postDelayed(new Runnable() {
                        
                        @Override
                        public void run() {
                            controller.hideArcMenu();
                        }
                    }, 1000);
                }else {
                    setVisibility(GONE);
                }
                
                mArcHomeButton.getmImageView().setScaleX(0.4f);
                mArcHomeButton.getmImageView().setScaleY(0.4f);
                mArcHomeButton.getmImageView().setVisibility(GONE);
                
                
                int itemCount = mArcItems.size();
                for (int i = 0; i < itemCount; i++) {
                    ArcItemButton child = mArcItems.get(i);
                    child.setTranslationX(0f);
                    child.setTranslationY(0f);
                    child.setRotation(-30f);
                    child.setAlpha(1f);
                    child.getmImageView().setClickable(true);
                }

                clickView.setTranslationX(0f);
                clickView.setTranslationY(0f);
                clickView.setScaleX(1f);
                clickView.setScaleY(1f);
                clickView.setAlpha(1f);
                mExpanded = false;
                
                mArcHomeButton.setScaleX(1f);
                mArcHomeButton.setScaleY(1f);
                mArcHomeButton.setAlpha(1f);

                
                arcItemButton.getmImageView().setSelected(false);

                if (arcItemButton.isItemSelected()) {
                    arcItemButton.setItemSelected(false);
                }
                
                arcItemButton.setScaleX(0f);
                arcItemButton.setScaleY(0f);
                
                mClicKItemAnimatorRunning = false;
                controller.showKeyguardNotification();
            }
            
            @Override
            public void onAnimationCancel(Animator arg0) {
                
            }
        });
        controller.addAnimator(set);
        set.start();
    }
    
    
    public void reset() {
        
        UIController.getInstance().hideArcMenu();
        mArcHomeButton.getmImageView().setScaleX(0.4f);
        mArcHomeButton.getmImageView().setScaleY(0.4f);
        mArcHomeButton.getmImageView().setVisibility(GONE);
        mArcHomeButton.setScaleX(1f);
        mArcHomeButton.setScaleY(1f);
        mArcHomeButton.setAlpha(1f);
        
        
        int itemCount = mArcItems.size();
        for (int i = 0; i < itemCount; i++) {
            ArcItemButton child = mArcItems.get(i);
            child.setTranslationX(0f);
            child.setTranslationY(0f);
            child.setScaleX(0f);
            child.setScaleY(0f);
            child.setAlpha(0f);
            child.getmImageView().setClickable(true);
        }
        mExpanded = false;
    }

    
     
    
    /**
     *   
     * @param arcItemButton
     */
    private void onClickFavorite(final ArcItemButton arcItemButton) {

        new Thread(new Runnable() {
            
            @Override
            public void run() {
                 
                Bitmap currentWallpaper = controller.getCurrentWallpaperBitmap(mWallpaper);
                
          
                boolean isLocalImage = mWallpaper.getImgId() == Wallpaper.WALLPAPER_FROM_PHOTO_ID;
                
                String imageFileName = new StringBuffer(FileUtil.getDirectoryFavorite()).append("/").append(Common.currentTimeDate()).append("_")
                        .append(isLocalImage ? mWallpaper.getImgName() : mWallpaper.getImgId()).append(".jpg").toString();
                
                final boolean success = FileUtil.saveWallpaper(currentWallpaper, imageFileName);
                
                if (success) {
                    mWallpaper.setFavoriteLocalPath(imageFileName);
                    mWallpaper.setFavorite(true);
                    WallpaperDB.getInstance(getContext().getApplicationContext()).updateFavorite(mWallpaper);
                    Common.insertMediaStore(getContext().getApplicationContext(),currentWallpaper.getWidth(), currentWallpaper.getHeight(), imageFileName);
                }
                
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        controller.showToast(success ? R.string.haokan_tip_save_gallery
                                : R.string.haokan_tip_favorite_error);
                    }
                }, 300);
                
            }
        }).start();
    }
    
    
    private void onClickLocked(final ArcItemButton arcItemButton) {

        new Thread(new Runnable() {

            @Override
            public void run() {

                boolean success =  false;
                
                if(mWallpaper.isLocked()){
                    success = controller.clearLock(getContext(),mWallpaper);
                }else{
                    success = controller.lockWallpaper(getContext(), mWallpaper);
                    
                    String imageFileName = new StringBuffer(FileUtil.getDirectoryFavorite()).append("/").append(Common.currentTimeDate()).append("_")
                            .append(mWallpaper.getImgId()).append(".jpg").toString();
                    Bitmap currentWallpaper = controller.getCurrentWallpaperBitmap(mWallpaper);
                    success = FileUtil.saveWallpaper(currentWallpaper, imageFileName);
                    if (success) {
                        Common.insertMediaStore(getContext().getApplicationContext(),currentWallpaper.getWidth(), currentWallpaper.getHeight(), imageFileName);
                    }
                }
                if (success) {
                    
                    HKAgent.onEventWallpaperLock(getContext().getApplicationContext(), mWallpaper);
                    postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            boolean isLocked = mWallpaper.isLocked();
                            controller.showToast(isLocked ? R.string.haokan_tip_screen_on_show : R.string.haokan_tip_no_lock_show);
                        }
                    }, 500);
                }  
            }
        }).start();

    }
    
    
    @Override
    public void onClick(View view) {
        
        if (animatorRunning() || !isExpanded()) {
            return;
        }

        DebugLog.d(TAG, "onClick  view = " + view.getId());
        removeCallbacks(mCloseMenuRunnable);
        final int viewId = view.getId();
        
        final ArcItemButton arcItemButton = mArcItems.get(viewId);
        
        switch (viewId) {
            case 0: 
                if (mWallpaper.isFavorite()) {
                    DebugLog.d(TAG, "onClick  isFavorite  return");
                    if (isExpanded() && !animatorRunning()) {
                        startHide();
                    }
                    return;
                }
                HKAgent.onEventIMGFavorite(getContext().getApplicationContext(), mWallpaper);
                onClickFavorite(arcItemButton);
                break;
            case 1:
                DebugLog.d(TAG,"onLongPress wallpaper url:" + mWallpaper.getImgUrl());
                DebugLog.d(TAG,"onLongPress wallpaper lock:" + mWallpaper.isLocked());
                onClickLocked(arcItemButton);
                break;
            default:
                break;
        }
        
        VibatorUtil.amigoVibrate(getContext().getApplicationContext(),
                VibatorUtil.LOCKSCREEN_STORYMODE_CLICK, 20);
        bindClickItemAnimator(view, arcItemButton, new ArcMenuAnimatorListener() {
            
            @Override
            public void onAnimatorEnd() {
                switch (viewId) {
                    case 2:
                        controller.startCategoryActivity(getContext().getApplicationContext());
                        break;
                    case 3:
                        controller.startSettingsActivity(getContext().getApplicationContext());
                        break;

                    default:
                        break;
                }
            }
        });
        
    }
    
    interface ArcMenuAnimatorListener{
        void onAnimatorEnd();
    }
    
 
    /**
     * 
     * @param arcItemButton
     * @param itemSelect
     * @param stringId
     */
    private void bindClickItemFeekBackAnimator(final ArcItemButton arcItemButton,final boolean itemSelect, final int stringId) {

        if (animatorRunning()) {
            return;
        }
        
        arcItemButton.getmImageView().setClickable(false);
        
        AnimatorSet set = new AnimatorSet();
        
        PropertyValuesHolder scaleXMagnify = PropertyValuesHolder.ofFloat("scaleX", 0.2f, 1.0f);
        PropertyValuesHolder scaleYMagnify = PropertyValuesHolder.ofFloat("scaleY", 0.2f, 1.0f);
        PropertyValuesHolder alphaFadeIn = PropertyValuesHolder.ofFloat("alpha", 0f, 1f);
        
        ObjectAnimator objectAnimatorMagnify = ObjectAnimator.ofPropertyValuesHolder(arcItemButton, scaleXMagnify, scaleYMagnify, alphaFadeIn).setDuration(300);
        objectAnimatorMagnify.setInterpolator(new DecelerateInterpolator());

        PropertyValuesHolder scaleXNarrow = PropertyValuesHolder.ofFloat("scaleX", 1.0f, 0.2f);
        PropertyValuesHolder scaleYNarrow = PropertyValuesHolder.ofFloat("scaleY", 1.0f, 0.2f);
        PropertyValuesHolder alphaFadeOut = PropertyValuesHolder.ofFloat("alpha", 1f, 0f);

        ObjectAnimator objectAnimatorNarrow = ObjectAnimator.ofPropertyValuesHolder(arcItemButton, scaleXNarrow, scaleYNarrow, alphaFadeOut).setDuration(300);
        objectAnimatorNarrow.setInterpolator(new AccelerateInterpolator(3f));
        objectAnimatorNarrow.setStartDelay(500);
        
        set.play(objectAnimatorMagnify);
        set.play(objectAnimatorNarrow).after(objectAnimatorMagnify);
        
        set.addListener(new AnimatorListener() {
            
            @Override
            public void onAnimationStart(Animator arg0) {
                controller.showArcMenu(); 
                
                arcItemButton.getmTextView().setVisibility(VISIBLE);
                arcItemButton.setTranslationX(arcItemButton.getToXDelta());
                arcItemButton.setTranslationY(arcItemButton.getToYDelta());
                arcItemButton.setItemSelected(itemSelect);
                arcItemButton.setRotation(0);
                arcItemButton.getmTextView().setText(stringId);
            }
            
            @Override
            public void onAnimationRepeat(Animator arg0) {
                
            }
            
            @Override
            public void onAnimationEnd(Animator arg0) {
//                setVisibility(GONE);
                UIController.getInstance().hideArcMenu();
                arcItemButton.getmImageView().setClickable(true);
                arcItemButton.setTranslationX(0);
                arcItemButton.setTranslationY(0);
                arcItemButton.setAlpha(1f);
                mItemFeekbackAnimatorRunning = false;
            }
            
            @Override
            public void onAnimationCancel(Animator arg0) {
                
            }
        });
        mItemFeekbackAnimatorRunning = true;
        controller.addAnimator(set);
        set.start();
    }
    
    
    
    public boolean animatorRunning() {
        boolean running = mItemFeekbackAnimatorRunning || mExpandAnimatorRunning || mShrinkAnimatorRunning
                || mClicKItemAnimatorRunning;
        
        if (mItemFeekbackAnimatorRunning) {
            DebugLog.d(TAG, "animatorRunning  mItemFeekbackAnimatorRunning  " + mItemFeekbackAnimatorRunning);
        }
        if (mExpandAnimatorRunning) {
            DebugLog.d(TAG, "animatorRunning  mExpandAnimatorRunning   " + mExpandAnimatorRunning);
        }
        if (mShrinkAnimatorRunning) {
            DebugLog.d(TAG, "animatorRunning  mShrinkAnimatorRunning " + mShrinkAnimatorRunning);
        }
        if (mClicKItemAnimatorRunning) {
            DebugLog.d(TAG, "animatorRunning  mClicKItemAnimatorRunning " + mClicKItemAnimatorRunning);
        }
        return running;
    }

    
    
}
