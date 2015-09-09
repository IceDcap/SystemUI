package com.amigo.navi.keyguard.network.theardpool;

public class DownLoadJsonThreadPool {
    private static DownLoadThreadPool sPool;
    public synchronized static DownLoadThreadPool getInstance() {

        if (sPool == null) {
            sPool = new DownLoadThreadPool();
        }
        return sPool;
    }
    
    private DownLoadJsonThreadPool(){
        
    }
}
