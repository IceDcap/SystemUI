package com.amigo.navi.keyguard.network.local;

import android.graphics.Bitmap;

public class ReuseImage{

    
    private Bitmap mBitmap;
    
    private boolean isUsed = false;

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap mBitmap) {
        this.mBitmap = mBitmap;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed(boolean isUsed) {
        this.isUsed = isUsed;
    }

    public ReuseImage(Bitmap mBitmap) {
       
        this.mBitmap = mBitmap;
    }
 
    
    
}
