
package com.amigo.navi.keyguard.haokan.db;

import com.amigo.navi.keyguard.DebugLog;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class HKSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG="HKSQLiteOpenHelper";
    public static final String DATABASE_NAME = "haokan.db";
    
    public static final int DB_VERISON_01 = 1;
    public static final int DB_VERISON_02 = 2;
    public HKSQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DB_VERISON_02);
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
        DebugLog.d(LOG_TAG, "HKSQLiteOpenHelper onCreate ");
        db.execSQL(DataConstant.CREATE_CATEGORY_SQL);
        db.execSQL(DataConstant.CREATE_WALLPAPER_SQL);
        db.execSQL(DataConstant.CREATE_STATISTICS_SQL);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DebugLog.d(LOG_TAG, "onUpgrade oldVersion: "+oldVersion+" newVersion: "+newVersion);
         int version=oldVersion;
         if(version==DB_VERISON_01){
             upgradeFrom1To2(db);
//             version++;
         }
    }
    
    private void upgradeFrom1To2(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE wallpaper ADD "
                 + DataConstant.WallpaperColumns.DETAIL_LINK
                 + " TEXT NOT NULL DEFAULT '';");
    }
    
   

}
