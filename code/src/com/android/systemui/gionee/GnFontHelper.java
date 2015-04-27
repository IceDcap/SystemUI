package com.android.systemui.gionee;

import java.lang.reflect.Field;

import android.content.res.Configuration;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.widget.TextView;

public class GnFontHelper {
    
    public static Typeface getCurrentFontType(Configuration configuration) {
        
        String fontName = "default";
        Typeface typeface = null;
        
        try {
            Field field = Configuration.class.getDeclaredField("amigoFont");
            field.setAccessible(true);
            fontName = (String) field.get(configuration);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (fontName.equals("default")) {
            typeface = Typeface.DEFAULT;
        } else {
            typeface = Typeface.createFromFile("/system/fonts/amigo/"+ fontName);
        }
        
        return typeface;
    }
    
    public static void resetAmigoFont(Configuration configuration, TextView... textViews) {
        try {
            Field[] fields = Paint.class.getDeclaredFields();
            for (Field field : fields) {
                if ("mIsAmigoFont".equals(field.getName())) {
                    Log.v("resetAmigoFont", "Field = " + field + "before Accessible? " + field.isAccessible());
                    field.setAccessible(true);
                    Log.v("resetAmigoFont", "Field = " + field + "after Accessible? " + field.isAccessible());
                    for (TextView textView : textViews) {
                        field.setBoolean(textView.getPaint(), false);
                    }
                }
            }
        } catch (Exception e) {
            Log.v("resetAmigoFont", "find field exeception");
            e.printStackTrace();
        }
        
        Typeface typeface = getCurrentFontType(configuration);
        for (TextView textView : textViews) {            
            textView.setTypeface(typeface);
        }
    }

}