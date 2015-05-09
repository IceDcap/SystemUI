
package com.amigo.navi.keyguard.haokan.entity;

import java.io.Serializable;

import android.util.Log;

import com.amigo.navi.keyguard.haokan.PlayerManager.State;

public class Music implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    private String mId;// id
    
    private int imgId;
    private int typeId;
    
    private String mMusicName; 
    private String mLocalPath;
 
    private String mPlayerUrl;
  
    private String mDownLoadUrl;
     
    /**
     * 文件名称[含后缀名]
     */
    private String mDisplayName; 
    
    private int mDurationTime; 
    
    private int mSize; 
    
    private String mArtist;
    
    private boolean mIsLocal;
    
    private State mState = State.NULL;
    
    public State getmState() {
        return mState;
    }

    public void setmState(State mState) {
        this.mState = mState;
    }

    public String getmArtist() {
        return mArtist;
    }

    public void setmArtist(String mArtist) {
        this.mArtist = mArtist;
    }


    public boolean isLocal() {
        return mIsLocal;
    }

    public void setLocal(boolean mIsLocal) {
        this.mIsLocal = mIsLocal;
    }

    public String getLocalPath() {
        return mLocalPath;
    }

    public void setLocalPath(String mLocalPath) {
        this.mLocalPath = mLocalPath;
    }

    public String getPlayerUrl() {
        return mPlayerUrl;
    }

    public void setPlayerUrl(String mPlayerUrl) {
        this.mPlayerUrl = mPlayerUrl;
    }

    public String getDownLoadUrl() {
        return mDownLoadUrl;
    }

    public void setDownLoadUrl(String mDownLoadUrl) {
        this.mDownLoadUrl = mDownLoadUrl;
    }

    
     
    public int getDurationTime() {
        return mDurationTime;
    }

    public void setDurationTime(int durationTime) {
        this.mDurationTime = durationTime;
    }

    public void setDisplayName(String displayName) {
        this.mDisplayName = displayName;
    }

    public String getDisplayName() {
        return mDisplayName;
    }
    
    public int getSize() {
        return mSize;
    }

    public void setSize(int size) {
        this.mSize = size;
    }

     

    public String getMusicId() {
        return mId;
    }

    public void setMusicId(String mMusicId) {
        this.mId = mMusicId;
    }

    public String getmMusicName() {
        return mMusicName;
    }

    public void setmMusicName(String mMusicName) {
        this.mMusicName = mMusicName;
    }

    
   
    public int getImgId() {
        return imgId;
    }

    public void setImgId(int imgId) {
        this.imgId = imgId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    @Override
    public String toString() {
        Log.v("MUSIC", "ID=" + mId + ", Name" + mMusicName );
        return super.toString();
    }

}
