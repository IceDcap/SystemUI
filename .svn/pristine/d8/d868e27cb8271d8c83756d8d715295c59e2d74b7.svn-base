package com.amigo.navi.keyguard.infozone;

import java.util.Hashtable;

import android.content.Context;
import android.graphics.Typeface;


public class FontCache {
    private static Hashtable<String, Typeface> mFontCache = new Hashtable<String, Typeface>();

    public static Typeface get(String name, Context context) {
        
        if (null == name || null == context) {
            throw new NullPointerException("null = name or null = context");
        }
        
        Typeface tf = mFontCache.get(name);
        if(tf == null) {
            tf = Typeface.createFromAsset(context.getAssets(), name);
            mFontCache.put(name, tf);
        }
        return tf;
    }
}
