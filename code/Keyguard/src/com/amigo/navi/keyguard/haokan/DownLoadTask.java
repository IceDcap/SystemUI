package com.amigo.navi.keyguard.haokan;

import android.os.AsyncTask;
import android.util.Log;

import com.amigo.navi.keyguard.haokan.entity.Music;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;


public class DownLoadTask extends AsyncTask<Void, Void, Boolean>{

    
    private String TAG = "haokan";
    
    private final int DOWNLOAD_TIMEOUT = 5000; 
    
    private DownLoadJob mJob;
    
    private String localPath = null;
    
    public DownLoadTask(DownLoadJob job){
        this.mJob = job;
    }
    
    @Override
    protected Boolean doInBackground(Void... params) {
        return downloadFile(mJob.getMusic());
    }
    
    
    @Override
    protected void onPostExecute(Boolean result) {
        mJob.notifyDownloadEnd(result,localPath);
        super.onPostExecute(result);
    }
    
    
    
    @Override
    protected void onPreExecute() {
        mJob.notifyDownloadStart();
        super.onPreExecute();
    }
    
    
    private boolean downloadFile(Music music) {
        
        boolean success = false;
        
        File file = createNewFile(music);

        final String musicPath = file.getPath();
        
        HttpURLConnection connection = null;
        RandomAccessFile randomAccessFile = null;
        InputStream in = null;
        
        try {
            URL url = new URL(music.getDownLoadUrl());
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(DOWNLOAD_TIMEOUT);
            connection.setReadTimeout(DOWNLOAD_TIMEOUT);
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setRequestProperty("Accept-Language","zh-CN");
            connection.setRequestProperty("Referer",music.getDownLoadUrl());
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("Connection","Keep-Alive");

            randomAccessFile = new RandomAccessFile(musicPath, "rwd");
            
            connection.connect();
            in = connection.getInputStream();
            byte[] buffer = new byte[1024*8];
            int length = -1;
            int totalSize = connection.getContentLength();
            int completeSize = 0;
            Log.v(TAG, "Music Size : " + Common.formatByteToMB(totalSize) + "MB");
            
            while ((length = in.read(buffer)) != -1) {
                randomAccessFile.write(buffer, 0, length);
                completeSize += length;
//                Log.v(TAG, "download progress " + completeSize * 100 / totalSize);
            }
            if (completeSize == totalSize) {
                Log.v(TAG, music.getmMusicName() + " download over!");
                success = true;
            }
            
        } catch (Exception e) { 
            success = false;
            FileUtil.deleteFile(localPath);
            Log.v(TAG, "Exception deleteFile " + localPath);
        }  finally {
            try {
                if (in != null){
                    in.close();
                }
                if (randomAccessFile != null){
                    randomAccessFile.close();
                }
                if (connection != null){
                    connection.disconnect();
                }
            } catch (IOException e) { 
                Log.v(TAG, "finally  IOException");
            }
        }
        return success;
    }
    
    private File createNewFile(Music music) {

        String localfile = FileUtil.getSdCardPath() + FileUtil.DIRECTORY_MUSIC;
        Common.isExistDirectory(localfile);
        
        localfile += Common.getMD5String(music.getmMusicName());
        localPath = localfile;
        
        File file = new File(localfile);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

}
