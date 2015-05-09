package com.amigo.navi.keyguard.haokan.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class BaseDB {

    
    private SQLiteOpenHelper mSQLiteOpenHelper;
    protected Context mContext;
    
    protected SQLiteDatabase mWritableDatabase;
    protected SQLiteDatabase mReadableDatabase;   
    
    public BaseDB(Context context){
        mContext = context;
        mSQLiteOpenHelper = HKSQLiteOpenHelper.getInstance(context);
        mWritableDatabase = mSQLiteOpenHelper.getWritableDatabase();
        mReadableDatabase = mSQLiteOpenHelper.getReadableDatabase();
    }
}
