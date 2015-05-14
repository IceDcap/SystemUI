
package com.amigo.navi.keyguard.haokan.entity;

import android.graphics.Color;
import android.text.TextUtils;

import java.io.Serializable;

import com.amigo.navi.keyguard.haokan.db.DataConstant;

public class Wallpaper implements Serializable{
    private static final long serialVersionUID = 1L;  
    
    private int imgId;
    
    private String displayName;
    
    private String imgName;
    
    private String imgContent;
    
    private String imgSource;
    
    private String imgUrl;
    
    private String urlClick;
    
    private String startTime;
    private String endTime;
    private String urlPv;
    private int isAdvert;
    
    private String backgroundColor;

    private Music music;
    
    private Category category;
    
    
    private String date;
    
    private String festival;
    
    private Caption caption;
    
 
    
    private String favoriteLocalPath;
    
    private boolean favorite;
    
    private boolean locked;
    
    private int isTodayWallpaper;
    
    private float realOrder;
    
    private float showOrder;
    
    private String showTimeBegin;
    
    private String showTimeEnd;
    
    
    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

 
    //0 表示从网上获取的壁纸，1表示固定壁纸
    private int type = WALLPAPER_FROM_WEB;
    public static final int WALLPAPER_FROM_WEB = DataConstant.INTERNET;
    public static final int WALLPAPER_FROM_FIXED_FOLDER = DataConstant.LOCAL;
    
 
    public int getImgId() {
        return imgId;
    }

    public void setImgId(int imgId) {
        this.imgId = imgId;
    }

    public String getImgName() {
        return imgName;
    }

    public void setImgName(String imgName) {
        this.imgName = imgName;
    }

    public String getImgContent() {
        return imgContent;
    }

    public void setImgContent(String imgContent) {
        this.imgContent = imgContent;
    }

    public String getImgSource() {
        return imgSource;
    }

    public void setImgSource(String imgSource) {
        this.imgSource = imgSource;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getUrlClick() {
        return urlClick;
    }

    public void setUrlClick(String urlClick) {
        this.urlClick = urlClick;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getUrlPv() {
        return urlPv;
    }

    public void setUrlPv(String urlPv) {
        this.urlPv = urlPv;
    }

    public int getIsAdvert() {
        return isAdvert;
    }

    public void setIsAdvert(int isAdvert) {
        this.isAdvert = isAdvert;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

 

    public Music getMusic() {
        return music;
    }

    public void setMusic(Music music) {
        this.music = music;
    }

     

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFestival() {
        return festival;
    }

    public void setFestival(String festival) {
        this.festival = festival;
    }

    public Caption getCaption() {
        if (caption == null) {
            String backgroundColor = getBackgroundColor();
            int color = 0xff4d4d4d;
            int contentColor = 0x404d4d4d;
            if (!TextUtils.isEmpty(backgroundColor)) {
                 color = Color.parseColor(backgroundColor);
                 int fortyPercent = ((((color & 0xff000000) >> 24) & 0x0000ff ) / 10 * 4) << 24;
                 contentColor = (color & 0x00ffffff) | fortyPercent;
            }
            caption = new Caption(getImgName(), getImgContent(), getUrlClick(), color, contentColor, getImgSource());
        }
        return caption;
    }


    public void setCaption(Caption caption) {
        this.caption = caption;
    }

    public String getFavoriteLocalPath() {
        return favoriteLocalPath;
    }

    public void setFavoriteLocalPath(String favoriteLocalPath) {
        this.favoriteLocalPath = favoriteLocalPath;
    }
    
 
    public int getType() {
        return type;
    }
 

    public void setType(int type) {
        this.type = type;
    }

    public int getIsTodayWallpaper() {
        return isTodayWallpaper;
    }

    public void setIsTodayWallpaper(int isTodayWallpaper) {
        this.isTodayWallpaper = isTodayWallpaper;
    }

    public float getRealOrder() {
        return realOrder;
    }

    public void setRealOrder(float realOrder) {
        this.realOrder = realOrder;
    }

    public float getShowOrder() {
        return showOrder;
    }

    public void setShowOrder(float showOrder) {
        this.showOrder = showOrder;
    }
    
    
    @Override
    public String toString() {
        
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("  ImgName : ").append(getImgName());
        stringBuffer.append(", favorite : ").append(isFavorite());
        stringBuffer.append(", Locked : ").append(isLocked());
        stringBuffer.append(", categoryName : ").append(getCategory().getTypeName());
        stringBuffer.append(", hasMusic : ").append(getMusic() != null);
        if (getMusic() != null) {
            stringBuffer.append(", MusicName : ").append(getMusic().getmMusicName());
        }
        return stringBuffer.toString();
    }
    
        public String getShowTimeBegin() {
        return showTimeBegin;
    }

    public void setShowTimeBegin(String showTimeBegin) {
        this.showTimeBegin = showTimeBegin;
    }

    public String getShowTimeEnd() {
        return showTimeEnd;
    }

    public void setShowTimeEnd(String showTimeEnd) {
        this.showTimeEnd = showTimeEnd;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    

}
