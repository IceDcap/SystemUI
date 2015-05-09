package com.amigo.navi.keyguard.network.local;

import android.graphics.Bitmap;

public interface DealWithFromLocalInterface {
    public Object readFromLocal(String key);
    public boolean writeToLocal(String key,Object obj);
    public boolean deleteAllFile();
    public boolean deleteFile(String key);
}
