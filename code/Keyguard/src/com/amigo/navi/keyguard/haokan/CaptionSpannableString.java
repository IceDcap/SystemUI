package com.amigo.navi.keyguard.haokan;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.haokan.analysis.HKAgent;
import com.amigo.navi.keyguard.haokan.entity.Caption;
import com.android.keyguard.R;


public class CaptionSpannableString extends SpannableStringBuilder{

//    private static final String SPACE_LINK = "     ";
//    private static final String SPACE_SOURCE = " ";
    
    private static final String SPACE_LINK = "  ";
//    private static final String SPACE_SOURCE = "   ";
    
    
//    private static final int IMG_SOURCE_TEXT_COLOR = 0xffff9000;
    private static final int IMG_SOURCE_TEXT_COLOR = 0xccffffff;
    
    private static long mLastClickTime = 0;
    
    private Caption mCaption;
    
    private boolean isEmptyLink;
    
    private Context mContext;
    private CaptionsView mCaptionsView;
   
    private static String  COMPANY = " ©";
    
    public CaptionSpannableString(CharSequence arg0) {
        super(arg0);
    }
    
    public CaptionSpannableString(Context context, Caption caption, Drawable mContentLinkDrawable, Rect linkRect,String link) {
        
        super(caption.getContent());
        mCaption = caption;
        
        boolean isEmptyImgSource = TextUtils.isEmpty(mCaption.getImgSource());
        mContext = context;
        
        isEmptyLink = TextUtils.isEmpty(mCaption.getLink());
        
        int start = 0;
        int end = 0;
        
        end = length();
        setSpan(new BackgroundColorSpan(caption.getContentBackgroundColor()), start, end,Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        if (!isEmptyImgSource) {
            append(COMPANY);
            append(caption.getImgSource());
            
            start = caption.getContent().length();
            end = length();
            setSpan(new ForegroundColorSpan(IMG_SOURCE_TEXT_COLOR), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
 
        }
        
        if (!isEmptyLink) {
            append(SPACE_LINK);
            
            end = length();
            start = end -1;
            mContentLinkDrawable.setBounds(linkRect);
            ImageSpan imageSpan = new ImageSpan(mContentLinkDrawable, ImageSpan.ALIGN_BASELINE);
            setSpan(imageSpan, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            append(link);
            append(" ");
            end = length() - 1;
            start = end - link.length() - 1;
            setSpan(new WebURLSpan(caption.getLink()), start, end,
                  Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
 
            
        }
        
        if (!isEmptyImgSource || !isEmptyLink) {
            end = length();
            start = caption.getContent().length();
            TextAppearanceSpan textAppearanceSpan = new TextAppearanceSpan(mContext, R.style.LinkTextAppearance);
            setSpan(textAppearanceSpan, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        
    }
    
    
    
    
    
    
    private class WebURLSpan extends URLSpan{

        public WebURLSpan(String url) {
            super(url);
        }
        
        @Override
        public void onClick(View widget) {

            if (isFastClick()) {
                return;
            }
            
            DebugLog.d("haokan", "Link onClick " + widget.getId());
            mCaptionsView.setClickLink(true);
            if (isEmptyLink) {
                return;
            }

            if (Common.getNetIsAvailable(mContext)) {
                UIController.getInstance().showWebView(mContext, mCaption.getLink());
                HKAgent.onEventIMGLink(mContext, UIController.getInstance().getmCurrentWallpaper());
            } else {
                UIController.getInstance().showTip(R.string.haokan_tip_check_net);
            }
            HKAgent.onEventIMGLink(mContext, UIController.getInstance().getmCurrentWallpaper());
            
        }


        @Override
        public void updateDrawState(TextPaint ds) {
      
//            super.updateDrawState(ds);
            ds.setColor(0xccffffff);  
            ds.setUnderlineText(false); 
        }
        
    }
    

    public CaptionsView getmCaptionsView() {
        return mCaptionsView;
    }


    public void setmCaptionsView(CaptionsView mCaptionsView) {
        this.mCaptionsView = mCaptionsView;
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
