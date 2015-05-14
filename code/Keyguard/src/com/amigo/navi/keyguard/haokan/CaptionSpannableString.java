package com.amigo.navi.keyguard.haokan;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;

import com.amigo.navi.keyguard.haokan.analysis.HKAgent;
import com.amigo.navi.keyguard.haokan.entity.Caption;
import com.android.keyguard.R;


public class CaptionSpannableString extends SpannableStringBuilder{

    private static final String SPACE_LINK = "    ";
    private static final String SPACE_SOURCE = " ";
    
    private static final int IMG_SOURCE_TEXT_COLOR = 0xffff9000;
    
    private Caption mCaption;
    
    private boolean isEmptyLink;
    
    private Context mContext;
    private CaptionsView mCaptionsView;
   
    
    public CaptionSpannableString(CharSequence arg0) {
        super(arg0);
    }


    public CaptionSpannableString(Context context, Caption caption,Drawable mContentLinkDrawable,Rect linkRect) {
        
        super(caption.getContent());
        mCaption = caption;
        
        boolean isEmptyImgSource = TextUtils.isEmpty(mCaption.getImgSource());
        mContext = context;
        
        isEmptyLink = TextUtils.isEmpty(mCaption.getLink());
        
        int start = 0;
        int end = 0;
        if (!isEmptyLink) {
        
            if (!isEmptyImgSource) {
                append(SPACE_SOURCE);
                append(caption.getImgSource());
            }

            append(SPACE_LINK);
            end = length() - SPACE_LINK.length() - caption.getImgSource().length() - SPACE_SOURCE.length();
            setSpan(new BackgroundColorSpan(caption.getContentBackgroundColor()), start, end,Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
           
            if (!isEmptyImgSource) {
            
                start = caption.getContent().length() + SPACE_SOURCE.length();
                end = start + caption.getImgSource().length();
                setSpan(new ForegroundColorSpan(IMG_SOURCE_TEXT_COLOR), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            
            }
            
            mContentLinkDrawable.setBounds(linkRect);
            
            ImageSpan imageSpan = new ImageSpan(mContentLinkDrawable, ImageSpan.ALIGN_BASELINE);

            setSpan(imageSpan, length() - 2, length()-1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            
            setSpan(new WebURLSpan(caption.getLink()), length() - 3, length(),
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }else {
            
            if (!isEmptyImgSource) {
                append(SPACE_SOURCE);
                append(caption.getImgSource());
            }
            
            end = caption.getContent().length();
            
            setSpan(new BackgroundColorSpan(caption.getContentBackgroundColor()), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            
            if (!isEmptyImgSource) {
                
                start = caption.getContent().length() + SPACE_SOURCE.length();
                end = start + caption.getImgSource().length();
                setSpan(new ForegroundColorSpan(IMG_SOURCE_TEXT_COLOR), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                
            }
            
            
        }
        
        
    }
    
    private class WebURLSpan extends URLSpan{

        public WebURLSpan(String url) {
            super(url);
        }
        
        @Override
        public void onClick(View widget) {

            Log.v("haokan", "Link onClick " + widget.getId());
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
    }
    
    
    public interface OnClickLinkListener{
        void onClickLink(View widget, String link);
    }


    public CaptionsView getmCaptionsView() {
        return mCaptionsView;
    }


    public void setmCaptionsView(CaptionsView mCaptionsView) {
        this.mCaptionsView = mCaptionsView;
    }
    
}
