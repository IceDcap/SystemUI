
package com.amigo.navi.keyguard.haokan;

import android.content.Context;

import com.amigo.navi.keyguard.haokan.db.WallpaperDB;
import com.amigo.navi.keyguard.haokan.entity.Music;


public class DownLoadJob {

    private DownLoadTask mDownloadTask;

    private Music mMusic;

    private Context mContext;
    
    public DownLoadJob(Context context, Music music) {
        mMusic = music;
        mContext = context;
    }

    public void start() {
        mDownloadTask = new DownLoadTask(this);
        mDownloadTask.execute();
    }

    public void notifyDownloadStart() {
 
    }

    public void notifyDownloadEnd(boolean success,String localPath) {
        if (!mDownloadTask.isCancelled()) {
            if (success) {
                mMusic.setLocal(true);
                mMusic.setLocalPath(localPath);
                WallpaperDB.getInstance(mContext).addMusicLocalPath(mMusic);
            }
        }
    }

  
    public Music getMusic() {
        return mMusic;
    }

    public void setMusic(Music mMusic) {
        this.mMusic = mMusic;
    }

}
