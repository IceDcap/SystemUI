
package com.amigo.navi.keyguard.haokan;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.keyguard.R;

public class DetailActivity extends Activity {

    private WebView mWebView;

    private ImageView mButtonCloseLink;

    private FrameLayout mFrameLayout;

    private WakeLock mWakeLock = null;

    private RelativeLayout mWebLayout;
    
    private String link = null;
    
    private View lineView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        setContentView(R.layout.haokan_web_layout);

        if (Build.VERSION.SDK_INT >= 21) {
            this.getWindow().getAttributes().systemUiVisibility |= (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            this.getWindow().setStatusBarColor(Color.TRANSPARENT);
            this.getWindow().setNavigationBarColor(Color.TRANSPARENT);
        }

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        UIController.getInstance().setDetailActivity(this);

        PowerManager powerManager = (PowerManager) getApplicationContext().getSystemService(
                Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
                "keyguard_webview");
        mWakeLock.setReferenceCounted(false);

        link = getIntent().getStringExtra("link");
        
        initUI();
    }
    
    private void initUI() {

        mWebLayout = (RelativeLayout) findViewById(R.id.WebLayout);

        mFrameLayout = (FrameLayout) findViewById(R.id.haokan_layout_webview_container);
        mButtonCloseLink = (ImageView) findViewById(R.id.haokan_layout_close_link);
        lineView = findViewById(R.id.line);
        mButtonCloseLink.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
 
                startExitAnimator();
            }
        });

        mWebView = (WebView) findViewById(R.id.haokan_layout_webview);

        setSettings(mWebView.getSettings());

        mWebView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

        });


        if (link != null) {
            mWebView.loadUrl(link);
        }

        mWebLayout.postDelayed(new Runnable() {

            @Override
            public void run() {
                startEnterAnimator();
            }
        }, 500);
    }

    private void setSettings(WebSettings setting) {
        setting.setJavaScriptEnabled(true);
        setting.setDatabaseEnabled(false);
        setting.setDomStorageEnabled(false);
        setting.setAppCacheEnabled(false);
        setting.setCacheMode(WebSettings.LOAD_NO_CACHE);

    }

    public void wakeLockRelease() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }

    public void wakeLockAcquire() {
        if (mWakeLock != null) {
            mWakeLock.acquire(30000);
        }
    }

    
    @Override
    protected void onResume() {
        
        super.onResume();
        wakeLockAcquire();
    }
    
    
    @Override
    protected void onPause() {
        super.onPause();
        wakeLockRelease();
        
    }
    
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean canGoBack = false;
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                canGoBack = mWebView.canGoBack();
                if (canGoBack) {
                    mWebView.goBack();
                }
            }
        }

        return canGoBack || super.dispatchKeyEvent(event);
    }

    public ImageView getmButtonCloseLink() {
        return mButtonCloseLink;
    }

    public void startEnterAnimator() {

        float translationY = mFrameLayout.getMeasuredHeight();

        PropertyValuesHolder pvhTranslationY = PropertyValuesHolder.ofFloat("translationY",
                translationY, 0f);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0.0f, 1.0f);
        ObjectAnimator animatorWebView = ObjectAnimator.ofPropertyValuesHolder(mWebView,
                pvhTranslationY, alpha).setDuration(300);

        ObjectAnimator animatorCloseLink = ObjectAnimator.ofFloat(getmButtonCloseLink(),
                "alpha", 0f, 1f).setDuration(300);
        animatorCloseLink.addUpdateListener(new AnimatorUpdateListener() {
            
            @Override
            public void onAnimationUpdate(ValueAnimator arg0) {

                lineView.setAlpha(arg0.getAnimatedFraction());
            }
        });

        AnimatorSet set = new AnimatorSet();

        animatorWebView.setStartDelay(400);
        set.play(animatorWebView).with(animatorCloseLink);

        set.start();
    }
    
    private void startExitAnimator() {

        ObjectAnimator animatorWebView = ObjectAnimator.ofFloat(mWebView, "translationY",0f,mFrameLayout.getMeasuredHeight()).setDuration(400);
        ObjectAnimator animatorCloseLink = ObjectAnimator.ofFloat(getmButtonCloseLink(), "alpha",1,0f).setDuration(300);
        
        animatorCloseLink.addUpdateListener(new AnimatorUpdateListener() {
            
            @Override
            public void onAnimationUpdate(ValueAnimator arg0) {

                lineView.setAlpha(1.0f - arg0.getAnimatedFraction());
            }
        });
        
        
        animatorCloseLink.setStartDelay(300);
        
        AnimatorSet set = new AnimatorSet();
        set.play(animatorCloseLink).with(animatorWebView);
        set.addListener(new AnimatorListener() {
            
            @Override
            public void onAnimationStart(Animator arg0) {
                
            }
            
            @Override
            public void onAnimationRepeat(Animator arg0) {
                
            }
            
            @Override
            public void onAnimationEnd(Animator arg0) {
                finish();
            }
            
            @Override
            public void onAnimationCancel(Animator arg0) {
                
            }
        });
        set.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWebView != null) {
            mWebView.destroy();
            mWebView = null;
        }
    }
}
