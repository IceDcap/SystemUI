package com.amigo.navi.keyguard.haokan.entity;

import java.io.Serializable;

public class Caption implements Serializable{
    private static final long serialVersionUID = 1L; // 定义序列化ID
    
    private String mTitle;
    
    private String mContent;
    
    private String mLink;
    
    private int mTitleBackgroundColor;
    
    private String imgSource;
    
    private int mContentBackgroundColor;
    
    
    public Caption() {
        // TODO Auto-generated constructor stub
    }

    public Caption(String mTitle, String mContent, String mLink, int mTitleBackgroundColor,
            int mContentBackgroundColor,String imgSource) {
        super();
        this.mTitle = mTitle;
        this.mContent = mContent;
        this.mLink = mLink;
        this.mTitleBackgroundColor = mTitleBackgroundColor;
        this.mContentBackgroundColor = mContentBackgroundColor;
        this.imgSource = imgSource;
    }

    public String getmTitle() {
        return mTitle;
    }
    

    public String getImgSource() {
        return imgSource;
    }

    public void setImgSource(String imgSource) {
        this.imgSource = imgSource;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getmContent() {
        return mContent;
    }

    public void setmContent(String mContent) {
        this.mContent = mContent;
    }

    public String getmLink() {
        return mLink;
    }

    public void setmLink(String mLink) {
        this.mLink = mLink;
    }

    public int getmTitleBackgroundColor() {
        return mTitleBackgroundColor;
    }

    public void setmTitleBackgroundColor(int mTitleBackgroundColor) {
        this.mTitleBackgroundColor = mTitleBackgroundColor;
    }

    public int getmContentBackgroundColor() {
        return mContentBackgroundColor;
    }

    public void setmContentBackgroundColor(int mContentBackgroundColor) {
        this.mContentBackgroundColor = mContentBackgroundColor;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String mContent) {
        this.mContent = mContent;
    }

    public String getLink() {
        return mLink;
    }

    public void setLink(String mLink) {
        this.mLink = mLink;
    }

    public int getTitleBackgroundColor() {
        return mTitleBackgroundColor;
    }

    public void setTitleBackgroundColor(int titleBackgroundColor) {
        this.mTitleBackgroundColor = titleBackgroundColor;
    }

    public int getContentBackgroundColor() {
        return mContentBackgroundColor;
    }

    public void setContentBackgroundColor(int contentBackgroundColor) {
        this.mContentBackgroundColor = contentBackgroundColor;
    }
    
    
}
