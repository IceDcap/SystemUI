package com.amigo.navi.keyguard.haokan;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amigo.navi.keyguard.haokan.entity.Music;
import com.amigo.navi.keyguard.infozone.AmigoKeyguardInfoZone;
import com.amigo.navi.keyguard.picturepage.widget.HorizontalListView.OnTouchlListener;
import com.android.keyguard.R;


public class HKMainLayout extends RelativeLayout {

    private String TAG = "haokan";
    
    private PlayerManager mPlayerManager;
    
    private PlayerButton mPlayerButton;
    
    private AmigoKeyguardInfoZone mInfozone;
    
    private RelativeLayout mLayoutPlayer;
    
    private RelativeLayout mLayoutTitle;
    
    private TextView mTextViewMusicName, mTextViewArtist;
    
    
    private CaptionsView mHkCaptionsView;
    
    private LinearLayout mMusicInfoLayout;
    
    private UIController controller;
//    private WebView mWebView;
    
    
    public HKMainLayout(Context context) {
        this(context, null);
    }

    public HKMainLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HKMainLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    
    public HKMainLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
        
        mInfoZoneMaxTranslationX = getResources().getDimensionPixelSize(R.dimen.haokan_infozone_max_translationX);
        
        UIController.getInstance().setmHkMainLayout(this);
        
        PlayerManager.getInstance().init(getContext().getApplicationContext());
    }
    
   
    
    private void init() {
        mPlayerManager = PlayerManager.getInstance();
        mPlayerManager.setmHkMainLayout(this);
        controller = UIController.getInstance();
    }
    
    
   
    @SuppressLint("JavascriptInterface")
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        
        
        mPlayerButton = (PlayerButton) findViewById(R.id.haokan_page_layout_imageButton);
        mHkCaptionsView = (CaptionsView) findViewById(R.id.haokan_page_layout_captions);
        mLayoutPlayer = (RelativeLayout) findViewById(R.id.haokan_page_layout_player);
        mTextViewMusicName = (TextView) mLayoutPlayer.findViewById(R.id.haokan_page_layout_music);
        mTextViewArtist = (TextView) mLayoutPlayer.findViewById(R.id.haokan_page_layout_Artist);
        mMusicInfoLayout= (LinearLayout) findViewById(R.id.haokan_page_layout_music_info);
        mLayoutTitle =(RelativeLayout) findViewById(R.id.haokan_captions_title_parent); 
        
//        mWebView = (WebView) findViewById(R.id.haokan_layout_webview);
        
//        View mCloseLinkLayout = findViewById(R.id.haokan_layout_close_link);
//        View mWebViewContainer = findViewById(R.id.haokan_layout_webview_container);
        
        
        mPlayerManager.setPlayerButton(mPlayerButton);
        
        mPlayerButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View arg0) {
                PlayerManager.getInstance().pauseOrPlayer();
            }
        });
        
        
 

        TextView textViewTip = (TextView) findViewById(R.id.haokan_page_layout_tip);
        
 
        controller.setmLayoutPlayer(mLayoutPlayer);
        controller.setmHkMainLayout(this);
        controller.setmCaptionsView(mHkCaptionsView);
        controller.setmPlayerButton(mPlayerButton);
        controller.setmTextViewTip(textViewTip);
    }
    
    int count = 0;
    
    
    public void showMusicPlayer(final Music mCurrentMusic) {

        postDelayed(new Runnable() {

            @Override
            public void run() {

                mTextViewMusicName.setAlpha(0f);
                mTextViewArtist.setAlpha(0f);

                mTextViewMusicName.setText(mCurrentMusic.getmMusicName());
                mTextViewMusicName.setTranslationX(100);
                mTextViewMusicName.setVisibility(VISIBLE);

                mTextViewArtist.setText(mCurrentMusic.getmArtist());
                mTextViewArtist.setTranslationX(100);
                mTextViewArtist.setVisibility(VISIBLE);

                PropertyValuesHolder pvhTranslationXName = PropertyValuesHolder.ofFloat(
                        "translationX", 100, 0f);
                PropertyValuesHolder pvhTranslationXArtist = PropertyValuesHolder.ofFloat(
                        "translationX", 100, 0f);
                PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0.1f, 1.0f);

                ObjectAnimator translationName = ObjectAnimator.ofPropertyValuesHolder(
                        mTextViewMusicName, pvhTranslationXName, alpha).setDuration(300);
                ObjectAnimator translationArtist = ObjectAnimator.ofPropertyValuesHolder(
                        mTextViewArtist, pvhTranslationXArtist, alpha).setDuration(300);

                translationName.setInterpolator(new OvershootInterpolator(2.5f));
                translationArtist.setInterpolator(new OvershootInterpolator(2.5f));
                translationArtist.setStartDelay(70);
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.play(translationName).with(translationArtist);
                animatorSet.start();
            }
        }, 200);

    }
    
    
    public void hideMusicPlayer(boolean anim) {

        if (anim) {
            
            postDelayed(new Runnable() {
                
                @Override
                public void run() {
                    
                    PropertyValuesHolder pvhTranslationXName = PropertyValuesHolder.ofFloat("translationX",
                            0f, mTextViewMusicName.getMeasuredWidth());
                    PropertyValuesHolder pvhTranslationXArtist = PropertyValuesHolder.ofFloat(
                            "translationX", 0f, mTextViewArtist.getMeasuredWidth());
                    PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0.0f);
                    
                    ObjectAnimator translationName = ObjectAnimator.ofPropertyValuesHolder(
                            mTextViewMusicName, pvhTranslationXName, alpha).setDuration(700);
                    ObjectAnimator translationArtist = ObjectAnimator.ofPropertyValuesHolder(
                            mTextViewArtist, pvhTranslationXArtist, alpha).setDuration(700);
                    
                    translationArtist.setStartDelay(70);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.play(translationName).with(translationArtist);
                    animatorSet.start();

                }
            }, 100);

        } else {
            mTextViewMusicName.setVisibility(INVISIBLE);
            mTextViewArtist.setVisibility(INVISIBLE);
        }

    }
    

    public void setCurrentMusic(boolean visible, Music mCurrentMusic) {
        mTextViewMusicName.setText(mCurrentMusic.getmMusicName());
        mTextViewArtist.setText(mCurrentMusic.getmArtist());
 
    }
    
 
    Handler mHandler = new Handler();
   

    public CaptionsView getHkCaptionsView() {
        return mHkCaptionsView;
    }

    public void setHkCaptionsView(CaptionsView mHkCaptionsView) {
        this.mHkCaptionsView = mHkCaptionsView;
    }
    
 
    private int mInfoZoneMaxTranslationX = 100;
    
    float InfozoneTranslationX = 0f;
    
    

    public View getmInfozone() {
        return mInfozone;
    }

    public void setmInfozone(AmigoKeyguardInfoZone mInfozone) {
        this.mInfozone = mInfozone;
        controller.setmInfozone(mInfozone);
    }

    
    
}
