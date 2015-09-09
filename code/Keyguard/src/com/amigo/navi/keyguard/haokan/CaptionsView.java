
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
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.Guide;
import com.amigo.navi.keyguard.Guide.GuideState;
import com.amigo.navi.keyguard.haokan.analysis.HKAgent;
import com.amigo.navi.keyguard.haokan.analysis.WallpaperStatisticsPolicy;
import com.amigo.navi.keyguard.haokan.entity.Caption;
import com.amigo.navi.keyguard.infozone.AmigoKeyguardInfoZone;
import com.amigo.navi.keyguard.network.NetworkRemind;
import com.amigo.navi.keyguard.network.NetworkRemind.ClickContinueCallback;
import com.amigo.navi.keyguard.settings.KeyguardSettings;
import com.android.keyguard.R;
 

public class CaptionsView extends RelativeLayout {

    private static final String LOG_TAG="CaptionsView";
    
    private ValueAnimator mValueAnimator = new ValueAnimator(); 

    private static final int ANIMATION_DURATION = 150;

    private TextView mTextViewGuid;
    private TextView mTextViewTitle;
    private LinearLayout mLinkLayout;
    private TextView mTextViewLink;
    private boolean mLinkVisible = false;
    
    private int mTitleHeight;
    
    
    private TextView mTextViewContent;
    private boolean mContentVisible = false;
    private Drawable mContentLinkDrawable;
    
    private GradientDrawable mGradientDrawable;
    
    public boolean animRuning = false;
    
    private int mInitialTranslationY;
    
    private boolean mClickLink = false;
    
    private Rect mLinkDrawablebounds = new Rect();
    
    private AnimatorSet animatorSet;
    
    private String mlinkString = null;
    
    private float mAlpha;
    
    private GuideClickView mGuideClickView;
    
    private boolean mKeyguardStyleIsChecked = true;
    private static long mLastClickTime = 0;
    private boolean isEmptyLink;
    private Caption mCaption;

	private Context mContext;
    
//    private boolean mGuideClickViewShowing = false;
    
    public void setDetailLinkVisible(boolean visible) {
        this.mLinkVisible = visible;
        int visibility = visible ? VISIBLE : GONE;
        mLinkLayout.setVisibility(visibility);
    }
    
    public CaptionsView(Context context) {
        this(context, null);
    }

    public CaptionsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CaptionsView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }
    
    public CaptionsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        mGradientDrawable = (GradientDrawable) getResources().getDrawable(
                R.drawable.haokan_title_background);

        mInitialTranslationY = getResources().getDimensionPixelSize(
                R.dimen.haokan_caption_layout_translationY);

        int left = getResources().getDimensionPixelSize(R.dimen.haokan_caption_link_left);
        int top = getResources().getDimensionPixelSize(R.dimen.haokan_caption_link_top);
        int right = getResources().getDimensionPixelSize(R.dimen.haokan_caption_link_right);
        int bottom = getResources().getDimensionPixelSize(R.dimen.haokan_caption_link_bottom);

        mLinkDrawablebounds.set(left, top, right, bottom);
        initUI();
        
        mKeyguardStyleIsChecked = KeyguardSettings.getKeyguardStyleSwitch(getContext());
    }
    
    public void onScreenTurnedOff() {
    	mContentVisible = false;
        setDetailLinkVisible(false);
        setTranslationY(mInitialTranslationY + mTitleHeight);
        getTitleView().setAlpha(0f);
        hideGuideIfNeed();
        UIController.getInstance().showKeyguardNotification();
    }
    
    public void onKeyguardShown(){
    	 mContentVisible = false;
    	 setTranslationY(mInitialTranslationY);
    	 UIController.getInstance().showKeyguardNotification();
    }
    
    public int getInitialTranslationY() {
        return mInitialTranslationY;
    }
    public int getAnimFormTranslationY() {
        return mInitialTranslationY + mTitleHeight;
    }
    

    public void setKeyguardStyleIsChecked(boolean isChecked) {
    	mKeyguardStyleIsChecked = isChecked;
    }
    
    
    private void initUI() {
        mContentLinkDrawable = getResources().getDrawable(R.drawable.haokan_caption_content_link);
        mTitleHeight = getResources().getDimensionPixelSize(R.dimen.haokan_caption_layout_title_height);
        mlinkString = getResources().getString(R.string.haokan_detail);
    }
    
    public void setContentText(Caption caption) {
        if(caption == null){
            setVisibility(GONE);
        	return;
        }
        mCaption=caption;
        if (mKeyguardStyleIsChecked) {
            setVisibility(VISIBLE);
        } else {
        	setVisibility(GONE);
        }
        
        CaptionSpannableString string = new CaptionSpannableString(getContext()
                .getApplicationContext(), caption, mContentLinkDrawable,mLinkDrawablebounds,mlinkString);
        
        mTextViewContent.setText(string);
        mTextViewContent.setMovementMethod(LinkMovementMethod.getInstance());
        mTextViewContent.setHighlightColor(getResources().getColor(R.color.haokan_caption_detal_select_color));
        mTextViewTitle.setText(caption.getTitle());
        setTitleBackgroundColor(caption.getTitleBackgroundColor());
        string.setmCaptionsView(this);
        isEmptyLink = TextUtils.isEmpty(caption.getLink());
        if (!isEmptyLink) {
            if (!TextUtils.isEmpty(caption.getmLinkText())) {
                mTextViewLink.setText(caption.getmLinkText());
            } else {
                mTextViewLink.setText(mlinkString);
            }
            setDetailLinkVisible(mContentVisible);
        }else{
            setDetailLinkVisible(false);
        }
    }
     
    
    public void setTitleBackgroundColor(int color) {
        mGradientDrawable.setColor(color);
        mTextViewTitle.setBackground(mGradientDrawable);
    }
    
 
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mTextViewTitle = (TextView) findViewById(R.id.haokan_captions_title);
        mLinkLayout = (LinearLayout) findViewById(R.id.haokan_captions_link_layout);
        mTextViewLink = (TextView) findViewById(R.id.haokan_captions_link);
        mTextViewContent = (TextView) findViewById(R.id.haokan_captions_content);
        mTextViewGuid = (TextView)findViewById(R.id.guid_click_tv);
        LinearLayout titleContainer = (LinearLayout) findViewById(R.id.haokan_captions_title_container);
        titleContainer.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                setContentVisibilityAnimation(!mContentVisible);
                setLinkVisibilityAnimation(!mLinkVisible);
                if (mContentVisible) {
                    UIController.getInstance().hideKeyguardNotification();
                    
                    if (Guide.needGuideClickTitle()) {
                        if (isGuideClickViewShowing()) {
                            stopGuide();
                        }
                        Guide.setBooleanSharedConfig(getContext(), Guide.GUIDE_CLICK_TITLE, false);
                        Guide.setNeedGuideClickTitle(false);
                    }
                    Guide.cleanDownloadTimes(getContext());
                }else {
                    UIController.getInstance().showKeyguardNotification();
                }
                
                HKAgent.onEventIMGTitle(getContext().getApplicationContext(), UIController.getInstance().getmCurrentWallpaper());
            }
        });

         
        mTextViewContent.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                DebugLog.d("haokan", "mTextViewContent onClick " + arg0.getId());
                if (mClickLink) {
                    mClickLink = false;
                    return;
                }
                setContentVisibilityAnimation(!mContentVisible);
                setLinkVisibilityAnimation(!mLinkVisible);
                if (!mContentVisible) {
                    UIController.getInstance().showKeyguardNotification();
                }
                
            }
        });
        
        
        mGuideClickView = (GuideClickView) findViewById(R.id.guide_click_view);
        mLinkLayout.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (isFastClick()) {
                    return;
                }
                setClickLink(true);
                if (isEmptyLink) {
                    return;
                }
                if (Common.getNetIsAvailable(mContext)) {
                    if(NetworkRemind.getInstance(mContext).needShowDialog()){
                        NetworkRemind.getInstance(mContext).registeContinueCallback(new ClickContinueCallback() {
                            
                            @Override
                            public void clickContinue() {
                                startCaption();
                            }
                        });
                        NetworkRemind.getInstance(mContext).alertDialog();
                    }else{
                        startCaption();                 
                    }
                } else {
                    UIController.getInstance().showToast(R.string.haokan_tip_check_net);
                }
                
            }
        });
    
    }
 
    
 
    public boolean isContentVisible() {
        return mContentVisible;
    }
    public boolean isLinkVisible() {
        return mLinkVisible;
    }

    public void setContentVisibilityAnimation(boolean visible) {
        if (mContentVisible != visible) {
            this.mContentVisible = visible;
            startTranslationAnimation(visible);
        }
    }
    
    public void setLinkVisibilityAnimation(boolean visible){
        if(!isEmptyLink){
            setDetailLinkVisible(visible);
        }
    }
    
    public void startTranslationAnimation(boolean visible) {
        
        final int translationY = mInitialTranslationY;
        if (mContentVisible) {
            mValueAnimator.setFloatValues(translationY, 0);
        }else {
            mValueAnimator.setFloatValues(0, translationY);
        }
        mValueAnimator.setDuration(ANIMATION_DURATION);
        mValueAnimator.addUpdateListener(mAnimatorUpdateListener);
        mValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mValueAnimator.start();
        
    }
    
 
    private AnimatorUpdateListener mAnimatorUpdateListener = new AnimatorUpdateListener() {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float translationY = (Float) animation.getAnimatedValue();
            setTranslationY(translationY);
        }
    };
    

    public TextView getTitleView() {
        return mTextViewTitle;
    }
    
    
    /**
     * 
     * @param translationX
     */
    public void onHorizontalMove(float translationX) {
         
        float percent = translationX / (Common.getScreenWidth(getContext().getApplicationContext())/(float)2);
        float alpha = 1.0f - percent;
        if (alpha <= 1f && alpha >= 0f) {
            mAlpha = alpha;
        }
        setAlpha(mAlpha);
    }
    
   
    public void cancelAnimator() {

        if (animatorSet != null) {
            animatorSet.cancel();
            animatorSet = null;
        }
    }
    
    public void OnTouchUpAnimator() {
        
        animRuning = true;
        animatorSet = new AnimatorSet();
        
        UIController controller = UIController.getInstance();
        AmigoKeyguardInfoZone infoZone = controller.getmInfozone();
        final View time = infoZone.getTimeView();
        final View date = infoZone.getDateFestivalView();
        final View week = infoZone.getWeekView();
        
        float infoZoneTranslationX = time.getTranslationX();
//        
//        PropertyValuesHolder pvhtime= PropertyValuesHolder.ofKeyframe(View.TRANSLATION_X,  
//                
//                Keyframe.ofFloat(0f, infoZoneTranslationX),  
//                Keyframe.ofFloat(.22859f, -9f),  
//                Keyframe.ofFloat(.399828f, 6f), 
//                Keyframe.ofFloat(.571061f, -3f), 
//                Keyframe.ofFloat(.714041f, 0.5f), 
//                Keyframe.ofFloat(1f, 0f) 
//                
//        );  
        PropertyValuesHolder pvhtime = PropertyValuesHolder.ofKeyframe(View.TRANSLATION_X,  
                Keyframe.ofFloat(0f, infoZoneTranslationX),  
                Keyframe.ofFloat(.22859f, -9f),  
                Keyframe.ofFloat(.399828f, 6f), 
                Keyframe.ofFloat(.571061f, -3f), 
                Keyframe.ofFloat(.714041f, 0.5f), 
//                Keyframe.ofFloat(.857020f, -1f),
                Keyframe.ofFloat(.927020f, -0.2f),
                Keyframe.ofFloat(1f, 0f) 
                
//                Keyframe.ofFloat(.22859f, -6f),  
//                Keyframe.ofFloat(.399828f, 3f), 
//                Keyframe.ofFloat(.571061f, -1f), 
//                Keyframe.ofFloat(.714041f, 0.2f),
//                
//                Keyframe.ofFloat(.857020f, -0f),
//                Keyframe.ofFloat(.927020f, 0f),
//                Keyframe.ofFloat(1f, 0f) 
                
        
        );  
 
        ObjectAnimator animatorText = ObjectAnimator.ofFloat(this, "alpha", 1.0f).setDuration(1000);
        
//        ObjectAnimator animationDate = ObjectAnimator.ofPropertyValuesHolder(date, pvhdate).setDuration(1168);
        ObjectAnimator animationTime = ObjectAnimator.ofPropertyValuesHolder(time, pvhtime).setDuration(1068);
        
        final View playerLayout = UIController.getInstance().getPlayerLayout();      
        animationTime.addUpdateListener(new AnimatorUpdateListener() {
            
            @Override
            public void onAnimationUpdate(ValueAnimator arg0) {
                playerLayout.setTranslationX((Float) arg0.getAnimatedValue());
                if (playerLayout.getAlpha() <= arg0.getAnimatedFraction()) {
                    playerLayout.setAlpha(arg0.getAnimatedFraction());
                }
            }
        });
        
//        ObjectAnimator animationWeek = ObjectAnimator.ofPropertyValuesHolder(week, pvhdate).setDuration(1168);
        
        ObjectAnimator animationWeek = ObjectAnimator.ofPropertyValuesHolder(week, pvhtime).setDuration(1068);
        ObjectAnimator animationDate = ObjectAnimator.ofPropertyValuesHolder(date, pvhtime).setDuration(1068);
//        animationDate.setStartDelay(67);
//        animationWeek.setStartDelay(34);
        
        animatorSet.play(animatorText).with(animationWeek).with(animationDate).with(animationTime);
     
        animatorSet.addListener(new AnimatorListener() {
            
            private boolean isCancel = false;
            @Override
            public void onAnimationStart(Animator arg0) {
                
            }
            
            @Override
            public void onAnimationRepeat(Animator arg0) {
                
            }
            
            @Override
            public void onAnimationEnd(Animator arg0) {
                animRuning = false;
                
                if (!isCancel) {
                    showGuideIfNeed();
                }
                isCancel = false;
            }
            
            @Override
            public void onAnimationCancel(Animator arg0) {
                isCancel = true;
            }
        });
        animatorSet.start();
    }
    

    public void setClickLink(boolean mClickLink) {
        this.mClickLink = mClickLink;
        if (mClickLink) {
            CharSequence charSequence = mTextViewContent.getText();
            mTextViewContent.setText(charSequence);
            mTextViewContent.invalidate();
        }
    }

    
    
    public void startGuide() {
        setGuideVisibility(VISIBLE);
        changeGuideViewPostion();
        getGuideClickView().startAnimator();
        Guide.setGuideState(GuideState.CLICK_TITLE);
        
    }
    
    public void stopGuide() {
        getGuideClickView().stopAnimator();
        setGuideVisibility(GONE);
        Guide.resetIdle();
    }
    
    private void changeGuideViewPostion() {
        Resources res = getResources();
        int distance = (getTitleView().getMeasuredHeight() - getGuideClickView().getMeasuredHeight()) / 2;
        RelativeLayout.LayoutParams params = (LayoutParams) getGuideClickView().getLayoutParams();
        Log.e("guide", "mGuideClickView.getMeasuredWidth() = " + getGuideClickView().getMeasuredWidth());
        params.leftMargin = getTitleView().getMeasuredWidth() / 2- getGuideClickView().getMeasuredWidth() / 2;
        params.topMargin = (int)(distance + res.getDimension(R.dimen.captions_title_container_paddingTop) + res.getDimension(R.dimen.captions_container_marginTop));
        getGuideClickView().setLayoutParams(params);
    }
    
    public void setGuideVisibility(int visibility) {
    	mTextViewGuid.setVisibility(visibility);
        getGuideClickView().setVisibility(visibility);
    }

    public boolean isGuideClickViewShowing() {
        return Guide.getGuideState() == GuideState.CLICK_TITLE;
    }
 
    
    public void hideGuideIfNeed() {
        if (isGuideClickViewShowing()) {
            setGuideVisibility(GONE);
        }
    }
    
    public void showGuideIfNeed() {
        if (isGuideClickViewShowing()) {
            changeGuideViewPostion();
            setGuideVisibility(VISIBLE);
        }
    }

    public GuideClickView getGuideClickView() {
        return mGuideClickView;
    }

    
    private void startCaption() {
        Intent intent = new Intent(mContext, DetailActivity.class);
        intent.putExtra("link", mCaption.getLink());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        WallpaperStatisticsPolicy.onDetialActivityShown();
        HKAgent.onEventIMGLink(mContext, UIController.getInstance().getmCurrentWallpaper());
    }
    
    public static boolean isFastClick() {
        long time = System.currentTimeMillis();
        long timeD = time - mLastClickTime;
        mLastClickTime = time;
        if (0 <= timeD && timeD < 1500) {
            return true;
        }
        return false;
    }

}
