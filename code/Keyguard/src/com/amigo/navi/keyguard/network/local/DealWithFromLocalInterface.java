package com.amigo.navi.keyguard.network.local;

import android.graphics.Bitmap;

public interface DealWithFromLocalInterface {
    public Bitmap readFromLocal(String key);
    public boolean writeToLocal(String key,Bitmap bitmap);
    public boolean deleteAllFile();
    public boolean deleteFile(String key);
    public void closeCache();
}
