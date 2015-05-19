package com.amigo.navi.keyguard.network.local;

public interface DealWithFromLocalInterface {
    public Object readFromLocal(String key);
    public boolean writeToLocal(String key,Object obj);
    public boolean deleteAllFile();
    public boolean deleteFile(String key);
    public void closeCache();
}
