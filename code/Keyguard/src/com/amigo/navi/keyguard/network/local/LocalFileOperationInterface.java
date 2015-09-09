package com.amigo.navi.keyguard.network.local;

import java.io.InputStream;
import java.io.OutputStream;

import android.graphics.Bitmap;

public interface LocalFileOperationInterface {
    public boolean saveFile(Object obj,OutputStream os);
    public Object readFile(InputStream is);
    public boolean saveFileByByte(byte[] bytes,OutputStream os);
}
