
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
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
 
import com.amigo.navi.keyguard.haokan.CaptionSpannableString.OnClickLinkListener;
import com.amigo.navi.keyguard.haokan.analysis.HKAgent;
import com.amigo.navi.keyguard.haokan.entity.Caption;
import com.amigo.navi.keyguard.haokan.entity.Wallpaper;
import com.amigo.navi.keyguard.haokan.entity.WallpaperList;
import com.amigo.navi.keyguard.infozone.AmigoKeyguardInfoZone;
import com.amigo.navi.keyguard.picturepage.adapter.HorizontalAdapter;
import com.android.keyguard.R;
 

public class CaptionsView extends LinearLayout {

    
    private ValueAnimator mValueAnimator = new ValueAnimator(); 

    private static final int ANIMATION_DURATION = 150;

    private TextView mTextViewTitle;
    
    private int mTitleHeight;
    
    private RelativeLayout mTitleParentView;
    
    private RelativeLayout mTitleContainer;
    
    private RelativeLayout mTitleLeftView,mTitleRightView;
    
    private TextView mTextViewContent;
    private boolean mContentVisible = false;
    private int mGap;
    private Drawable mContentLinkDrawable;
    
    GradientDrawable GradientDrawable1,GradientDrawable2,GradientDrawable3;
    
    public boolean animRuning = false;
    
    private int mInitialTranslationY;
    
    private boolean mClickLink = false;
    
    private Rect mLinkDrawablebounds = new Rect();
    
    public void setContentVisible(boolean visible) {
        if (mContentVisible != visible) {
            this.mContentVisible = visible;
        }
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
        
         GradientDrawable1 = (GradientDrawable) getResources().getDrawable(R.drawable.haokan_title_left);
         GradientDrawable2 = (GradientDrawable) getResources().getDrawable(R.drawable.haokan_title_left);
         GradientDrawable3 = (GradientDrawable) getResources().getDrawable(R.drawable.haokan_title_left);
        
         
         setPivotY(0f);
         setPivotX(0f);
         
         mInitialTranslationY = getResources().getDimensionPixelSize(R.dimen.haokan_caption_layout_translationY);
         
         int left = getResources().getDimensionPixelSize(R.dimen.haokan_caption_link_left);
         int top = getResources().getDimensionPixelSize(R.dimen.haokan_caption_link_top);
         int right = getResources().getDimensionPixelSize(R.dimen.haokan_caption_link_right);
         int bottom = getResources().getDimensionPixelSize(R.dimen.haokan_caption_link_bottom);
         
         mLinkDrawablebounds.set(left, top, right, bottom);
        initUI();
    }
    
    public void onScreenTurnedOff() {
        setContentVisible(false);
        setTranslationY(mInitialTranslationY + mTitleHeight);
    }
    
    public int getInitialTranslationY() {
        return mInitialTranslationY;
    }
    public int getAnimFormTranslationY() {
        return mInitialTranslationY + mTitleHeight;
    }
    

    
    private void initUI() {
        mContentLinkDrawable = getResources().getDrawable(R.drawable.haokan_caption_content_link);
        mGap = getResources().getDimensionPixelSize(R.dimen.haokan_caption_layout_content_margintop);
        mTitleHeight = getResources().getDimensionPixelSize(R.dimen.haokan_caption_layout_title_height);
    }
    
    public void setContentText(Caption caption) {

        CaptionSpannableString string = new CaptionSpannableString(getContext()
                .getApplicationContext(), caption, mContentLinkDrawable,mLinkDrawablebounds);
        mTextViewContent.setText(string);
        mTextViewContent.setMovementMethod(LinkMovementMethod.getInstance());
        mTextViewTitle.setText(caption.getTitle());
        setTitleBackgroundColor(caption.getTitleBackgroundColor());
        string.setmCaptionsView(this);
    }
     
    
    public void setTitleBackgroundColor(int color) {

        GradientDrawable1.setColor(color);
        GradientDrawable2.setColor(color);
        GradientDrawable3.setColor(color);
        mTitleParentView.setBackground(GradientDrawable1);
        mTitleLeftView.setBackground(GradientDrawable2);
        mTitleRightView.setBackground(GradientDrawable3);

    }
    
 
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        
        mTitleParentView = (RelativeLayout) findViewById(R.id.haokan_captions_title_parent);
        mTextViewTitle = (TextView) findViewById(R.id.haokan_captions_title);
        mTextViewContent = (TextView) findViewById(R.id.haokan_captions_content);
        
        mTitleContainer = (RelativeLayout) findViewById(R.id.haokan_captions_title_container);
        
        mTitleLeftView = (RelativeLayout) findViewById(R.id.haokan_captions_title_left);
        mTitleRightView = (RelativeLayout) findViewById(R.id.haokan_captions_title_right);
        
        mTextViewTitle.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mContentVisible) {
                    UIController.getInstance().showKeyguardNotification();
                }else {
                    UIController.getInstance().hideKeyguardNotification();
                }
                
                setContentVisibilityAnimation(!mContentVisible);
//                HKAgent.onEvent(getContext().getApplicationContext(), 1, 1, Event.IMG_CLICK_TITLE);
                HKAgent.onEventIMGTitle(getContext().getApplicationContext(), UIController.getInstance().getmCurrentWallpaper());
            }
        });

        mTextViewContent.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Log.v("haokan", "mTextViewContent onClick " + arg0.getId());
                if (mClickLink) {
                    mClickLink = false;
                    return;
                }
                if (mContentVisible) {
                    UIController.getInstance().showKeyguardNotification();
                }
                setContentVisibilityAnimation(!mContentVisible);
                
            }
        });
    
    }
 
 
    public void setContentVisibilityAnimation(boolean visible) {
        if (mContentVisible != visible) {
            this.mContentVisible = visible;
            startTranslationAnimation(visible);
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
            float translationY = (float) animation.getAnimatedValue();
            setTranslationY(translationY);
        }
    };
    

    public TextView getTitleView() {
        return mTextViewTitle;
    }
    
    
//    float mTranslationX;
    
    float mScale;
    float mAlpha;
    /**
     * 
     * @param translationX
     */
    public void move(float translationX) {
        
        
        // 1.整体向左上角收缩
        float percent = translationX / (Common.getScreenWidth(getContext().getApplicationContext())/2);
        float x = (float) (percent * 0.2);
        float scale = 1 - x;
        mScale = scale;
        setScaleX(scale);
        setScaleY(scale);
        
        //2.移动
        float t = -translationX / 10;
        mTitleParentView.setTranslationX(t);
        mTitleLeftView.setTranslationX(-t);
        if (mContentVisible) {
            mTextViewContent.setTranslationX(t);
        }
        
        float alpha = 1 - percent;
        if (alpha <= 1f && alpha >= 0f) {
            mAlpha = alpha;
        }
        //3.透明度变化
        setAlpha(mAlpha);

    }
    
   
    AnimatorSet animatorSet;
    public void OnTouchUpAnimator() {
        
        animRuning = true;
        animatorSet = new AnimatorSet();
        
        UIController controller = UIController.getInstance();
        AmigoKeyguardInfoZone infoZone = controller.getmInfozone();
        final View time = infoZone.getTimeView();
        final View date = infoZone.getDateFestivalView();
        final View week = infoZone.getWeekView();
        
        float infoZoneTranslationX = time.getTranslationX();
        
        //1168
        PropertyValuesHolder pvhtime= PropertyValuesHolder.ofKeyframe(View.TRANSLATION_X,  
                
                Keyframe.ofFloat(0f, infoZoneTranslationX),  
//                Keyframe.ofFloat(.22859f, -3.3f * 3f),  
//                Keyframe.ofFloat(.399828f, 1.4f * 3f), 
//                Keyframe.ofFloat(.571061f, -0.6f * 3f), 
//                Keyframe.ofFloat(.714041f, 0f), 
//                Keyframe.ofFloat(.857020f, -0.6f * 3f),
//                Keyframe.ofFloat(1f, 0f)   
                
                
                Keyframe.ofFloat(.22859f, -4f * 3f),  
                Keyframe.ofFloat(.399828f, 3f * 3f), 
                Keyframe.ofFloat(.571061f, -2f * 3f), 
                Keyframe.ofFloat(.714041f, 1f), 
                Keyframe.ofFloat(.857020f, -0.5f * 3f),
                Keyframe.ofFloat(.927020f, 0.2f * 3f),
                Keyframe.ofFloat(1f, 0f)   
                
        );  
        PropertyValuesHolder pvhdate = PropertyValuesHolder.ofKeyframe(View.TRANSLATION_X,  
                Keyframe.ofFloat(0f, infoZoneTranslationX),  
//                Keyframe.ofFloat(.22859f, -3.3f * 3f),  
//                Keyframe.ofFloat(.399828f, 1.4f * 3f), 
//                Keyframe.ofFloat(.571061f, -0.6f * 3f), 
//                Keyframe.ofFloat(.714041f, 0f), 
//                Keyframe.ofFloat(.857020f, -0.6f * 3f),
//                Keyframe.ofFloat(1f, 0f)  
                
                Keyframe.ofFloat(.22859f, -4f * 3f),  
                Keyframe.ofFloat(.399828f, 3f * 3f), 
                Keyframe.ofFloat(.571061f, -2f * 3f), 
                Keyframe.ofFloat(.714041f, 1f), 
                Keyframe.ofFloat(.857020f, -0.5f * 3f),
                Keyframe.ofFloat(.927020f, 0.2f * 3f),
                Keyframe.ofFloat(1f, 0f)   
                
        
        );  
 
        float translationX = mTitleParentView.getTranslationX();
        
        //背景移动动画
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(mTitleParentView, "translationX", translationX , 0f).setDuration(400);
        
        /**
         * 标题移动动画
         */
        ObjectAnimator animatorTitle = ObjectAnimator.ofFloat(mTextViewTitle, "translationX", 2 * translationX , 0).setDuration(500);
        
        ObjectAnimator animationDate = ObjectAnimator.ofPropertyValuesHolder(date, pvhdate).setDuration(1168);
        ObjectAnimator animationTime = ObjectAnimator.ofPropertyValuesHolder(time, pvhtime).setDuration(1168);
        
        final View playerLayout = UIController.getInstance().getmLayoutPlayer();
        
        animationTime.addUpdateListener(new AnimatorUpdateListener() {
            
            @Override
            public void onAnimationUpdate(ValueAnimator arg0) {
                playerLayout.setTranslationX((float) arg0.getAnimatedValue());
                if (playerLayout.getAlpha() <= arg0.getAnimatedFraction()) {
                    playerLayout.setAlpha(arg0.getAnimatedFraction());
                }
            }
        });
        
        ObjectAnimator animationWeek = ObjectAnimator.ofPropertyValuesHolder(week, pvhdate).setDuration(1168);
        
        animationDate.setStartDelay(67);
        animationWeek.setStartDelay(34);
        
        
        
        
        
        animatorSet.play(objectAnimator1).with(animatorTitle)
                .with(animationWeek).with(animationDate).with(animationTime);

        objectAnimator1.addUpdateListener(new AnimatorUpdateListener() {
            
            @Override
            public void onAnimationUpdate(ValueAnimator arg0) {
        
                float translationX = (float) arg0.getAnimatedValue();
                mTitleLeftView.setTranslationX(-translationX);
                mTextViewContent.setTranslationX(translationX);
            }
        });
        
        animatorTitle.addUpdateListener(new AnimatorUpdateListener() {
            
            @Override
            public void onAnimationUpdate(ValueAnimator arg0) {

                float fraction = arg0.getAnimatedFraction();
                setAlpha(fraction);
                float scale =  mScale + (1.0f - mScale)/mScale * fraction;
                if (scale <= 1f) {
                    setScaleX(scale);
                    setScaleY(scale);
                }
                setAlpha(mAlpha + (1 - mAlpha)/mAlpha * fraction);
                
            }
        });
        
        objectAnimator1.addListener(new AnimatorListener() {
            
            @Override
            public void onAnimationStart(Animator arg0) {
                
            }
            
            @Override
            public void onAnimationRepeat(Animator arg0) {
                
            }
            
            @Override
            public void onAnimationEnd(Animator arg0) {
                mTitleRightView.setTranslationX(0f);
                mTitleLeftView.setTranslationX(0f);
            }
            
            @Override
            public void onAnimationCancel(Animator arg0) {
               
            }
        });
        animatorSet.addListener(new AnimatorListener() {
            
            @Override
            public void onAnimationStart(Animator arg0) {
                
            }
            
            @Override
            public void onAnimationRepeat(Animator arg0) {
                
            }
            
            @Override
            public void onAnimationEnd(Animator arg0) {
                animRuning = false;
            }
            
            @Override
            public void onAnimationCancel(Animator arg0) {
                
            }
        });
        animatorSet.start();
    }
    
    
  
    
    
    public void switchWallpaperAnimation(float translationX) {
        if (translationX == 0) {
            mTitleRightView.setVisibility(VISIBLE);
            mTitleLeftView.setVisibility(VISIBLE);
        }

        getTitleView().setTranslationX(-translationX * 2);
        mTitleRightView.setTranslationX(-translationX * 2);
        
        if (mContentVisible) {
            mTextViewContent.setTranslationX(-translationX * 2);
        }
        
        if (translationX > mTextViewTitle.getMeasuredWidth()/2) {
            mTitleRightView.setVisibility(INVISIBLE);
            mTitleLeftView.setVisibility(INVISIBLE);
        }
    }
    
    public int getRealHeight() {
        return mTextViewTitle.getMeasuredHeight() + mGap + mTextViewContent.getMeasuredHeight();
    }
    
    
    public void startMusicPlayerAnim() {

        AnimatorUpdateListener mAnimatorUpdateListener = new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float translationX = (float) animation.getAnimatedValue();
                mTextViewContent.setTranslationX(translationX);
                mTextViewTitle.setTranslationX(translationX);
            }
        };
        mValueAnimator.setFloatValues(200, 0);
        mValueAnimator.setDuration(5000);
        mValueAnimator.addUpdateListener(mAnimatorUpdateListener);
        mValueAnimator.setInterpolator(new OvershootInterpolator());
        
        mValueAnimator.start();
        
    }


    public void setClickLink(boolean mClickLink) {
        this.mClickLink = mClickLink;
    }
    
    
    

}
