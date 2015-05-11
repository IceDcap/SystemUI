
package com.amigo.navi.keyguard.haokan;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.keyguard.R;

public class HKWebLayout extends RelativeLayout {

    
    private WebView mWebView;
    
    private ImageView mButtonCloseLink;
    
    public HKWebLayout(Context context) {
        super(context);
    }

    public HKWebLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HKWebLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HKWebLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        
    }

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mButtonCloseLink = (ImageView) findViewById(R.id.haokan_layout_close_link);
        mButtonCloseLink.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                
                UIController.getInstance().removeWebView();
            }
        });
        
        mWebView = (WebView) findViewById(R.id.haokan_layout_webview);
        mWebView.setFocusable(true);
        mWebView.setBackgroundColor(Color.TRANSPARENT);
     
        UIController.getInstance().setmWebView(mWebView);
        
        setSettings(mWebView.getSettings());
        mWebView.addJavascriptInterface(new WebAppInterface(getContext()), "Android");

        mWebView.setWebViewClient(new HKWebViewClient());

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {

            }
        });
        
 

    }
    
    
    
    
    public WebView getmWebView() {
        return mWebView;
    }

    public void setmWebView(WebView mWebView) {
        this.mWebView = mWebView;
    }

    public ImageView getmButtonCloseLink() {
        return mButtonCloseLink;
    }

    public void setmButtonCloseLink(ImageView mButtonCloseLink) {
        this.mButtonCloseLink = mButtonCloseLink;
    }

    public void loadurl(String link) {
        if (mWebView == null) {
            mWebView = (WebView) findViewById(R.id.haokan_layout_webview);
        }
        mWebView.loadUrl(link);
    }

    public class WebAppInterface {
        Context mContext;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c) {
            mContext = c;
        }

        /** Show a toast from the web page */
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }
    }

    final class DemoJavaScriptInterface {

        DemoJavaScriptInterface() {
        }

        /**
         * This is not called on the UI thread. Post a runnable to invoke
         * loadUrl on the UI thread.
         */
        public void clickOnAndroid() {
            new Handler().post(new Runnable() {
                public void run() {
                    // mWebView.loadUrl("javascript:wave()");
                }
            });

        }
    }

    /**
     * Provides a hook for calling "alert" from javascript. Useful for debugging
     * your javascript.
     */
    final class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {

            result.confirm();
            return true;
        }
    }
    
    @SuppressLint("NewApi")
    private void setSettings(WebSettings setting) {
        setting.setJavaScriptEnabled(true);
//        setting.setBuiltInZoomControls(true);
//        setting.setDisplayZoomControls(false);
//        setting.setSupportZoom(true);

//        setting.setDomStorageEnabled(true);
//        setting.setDatabaseEnabled(true);
        
//        setting.setLoadWithOverviewMode(true);
        setting.setUseWideViewPort(true);
        
    }
    
 

}
