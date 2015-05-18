
package com.amigo.navi.keyguard.haokan;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.keyguard.R;

public class WebLayout extends RelativeLayout {

    
    private WebView mWebView;
    
    private ImageView mButtonCloseLink;
    
    public WebLayout(Context context) {
        super(context);
    }

    public WebLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WebLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public WebLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        
    }

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
     
        UIController.getInstance().setmWebView(mWebView);
        
        setSettings(mWebView.getSettings());
        
        mWebView.setWebViewClient(new WebViewClient() {  
            public boolean shouldOverrideUrlLoading(WebView view, String url) {  
                view.loadUrl(url);  
                return true;  
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
    
    private void setSettings(WebSettings setting) {
        setting.setJavaScriptEnabled(true);
        setting.setDatabaseEnabled(false);   
        setting.setDomStorageEnabled(false);
        setting.setAppCacheEnabled(false);
        setting.setCacheMode(WebSettings.LOAD_NO_CACHE);
        
    }

}
