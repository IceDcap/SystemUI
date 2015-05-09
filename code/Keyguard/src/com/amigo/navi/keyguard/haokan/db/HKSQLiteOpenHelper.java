
package com.amigo.navi.keyguard.haokan.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class HKSQLiteOpenHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "haokan.db";
    
    public static final int VERSION = 1;

    public HKSQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }
    
    private static HKSQLiteOpenHelper sInstance;
    
    public static synchronized HKSQLiteOpenHelper getInstance(Context context){
        if(sInstance == null){
            sInstance = new HKSQLiteOpenHelper(context);
        }
        return sInstance;
    }
   
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DataConstant.CREATE_CATEGORY_SQL);
        db.execSQL(DataConstant.CREATE_WALLPAPER_SQL);
        db.execSQL(DataConstant.CREATE_STATISTICS_SQL);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    
   

}
