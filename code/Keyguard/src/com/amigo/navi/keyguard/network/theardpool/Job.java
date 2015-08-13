package com.amigo.navi.keyguard.network.theardpool;
public interface Job {
    public void runTask();

    public void cancelTask();

    public int getProgress();
    
    public boolean isCanceled();
}