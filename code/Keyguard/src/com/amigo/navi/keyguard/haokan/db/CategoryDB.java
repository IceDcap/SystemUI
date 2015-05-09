package com.amigo.navi.keyguard.haokan.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.haokan.db.DataConstant.CategoryColumns;
import com.amigo.navi.keyguard.haokan.entity.Category;
import com.amigo.navi.keyguard.haokan.entity.WallpaperList;

import java.util.ArrayList;
import java.util.List;


public class CategoryDB extends BaseDB{
    private static final String TAG = "CategoryDB";
    public static final String TYPE_ID = CategoryColumns.TYPE_ID;
    public static final String TYPE_NAME = CategoryColumns.TYPE_NAME;
    public static final String TYPE_ICON_URL = CategoryColumns.TYPE_ICON_URL;
    public static final String TABLE_NAME = DataConstant.TABLE_CATEGORY;
    public static final String FAVORITE = CategoryColumns.FAVORITE;
    public static final String IS_FINISH = CategoryColumns.DOWNLOAD_PICTURE;
    
    public static final String TYPE_ICON_RESID = CategoryColumns.TYPE_ICON_RESID;
    public static final String TYPE_NAME_RESID = CategoryColumns.TYPE_NAME_RESID;
    
    public CategoryDB(Context context) {
        super(context);
    }
    
    private static CategoryDB sInstance;

    public synchronized static CategoryDB getInstance(Context context){
        DebugLog.d(TAG,"getInstance1");
        if(sInstance == null){
            sInstance = new CategoryDB(context);
        }
        DebugLog.d(TAG,"getInstance2");
        return sInstance;
    }
        
    public void deleteAll() {
        final SQLiteDatabase db = mWritableDatabase;
        db.execSQL("delete from category");
    }
    
    public void insertCategorys(List<Category> list) {
        final SQLiteDatabase db = mWritableDatabase;
        db.beginTransaction();
        insertCategorysNoTransaction(list, db);
        db.setTransactionSuccessful();  
        db.endTransaction();  
        
    }

    /**
     * @param list
     * @param db
     */
    private void insertCategorysNoTransaction(List<Category> list,
            final SQLiteDatabase db) {
        for (Category category : list) {
            ContentValues values = new ContentValues();
            values.put(TYPE_ID, category.getTypeId());
            values.put(TYPE_NAME, category.getTypeName());
            values.put(TYPE_ICON_URL, category.getTypeIconUrl());
            
            values.put(FAVORITE, category.isFavorite());
            values.put(TYPE_ICON_RESID, category.getTypeIconResId());
            values.put(TYPE_NAME_RESID, category.getTypeNameResId());
            
            long id = db.insert(TABLE_NAME, null, values);
        }
    }
    
    public void insertAfterDeleteAll(List<Category> list){
        DebugLog.d(TAG,"insertAfterDeleteAll 1");         
        final SQLiteDatabase db = mWritableDatabase;
        db.beginTransaction();
        deleteAll();
        insertCategorysNoTransaction(list, db);
        db.setTransactionSuccessful();  
        db.endTransaction(); 
        DebugLog.d(TAG,"insertAfterDeleteAll 2");         
    }
    
    public  List<Category> queryCategorys() {
        final SQLiteDatabase db = mWritableDatabase;
        String sql = "select type_id,type_name,type_icon_url,favorite ,type_icon_resid,type_name_resid from " + TABLE_NAME
                + " where download_picture = 1";
        
        List<Category> list = new ArrayList<Category>();
        
        Cursor cursor = db.rawQuery(sql, null);
        
        getCategoryListByCursor(list, cursor);
        if (cursor != null) {
            cursor.close();
        }
        return list;
    }

    /**
     * @param list
     * @param cursor
     */
    private void getCategoryListByCursor(List<Category> list, Cursor cursor) {
        while (cursor.moveToNext()) {
            Category category = new Category();
            int typeId = cursor.getInt(0);
            String typeName = cursor.getString(1);
            String typeIconUrl = cursor.getString(2);
            boolean favorite = cursor.getInt(3) == DataConstant.CATEGORY_FAVORITE_TRUE;
            
            category.setTypeId(typeId);
            category.setTypeName(typeName);
            category.setTypeIconUrl(typeIconUrl);
            category.setFavorite(favorite);
            category.setTypeIconResId(cursor.getInt(4));
            category.setTypeNameResId(cursor.getInt(5));
            list.add(category);
        }
    }
    
    public int updateFavorite(Category category) {
        final SQLiteDatabase db = mWritableDatabase;
        ContentValues values = new ContentValues();
        values.put(FAVORITE, category.isFavorite() ? DataConstant.CATEGORY_FAVORITE_TRUE
                : DataConstant.CATEGORY_FAVORITE_FALSE);
        int id = db.update(TABLE_NAME, values, "type_id = ?", new String[] {
                String.valueOf(category.getTypeId())
        });
        return id;

    }
    
    public void updateDownLoadFinish(Category category){
        final SQLiteDatabase db = mWritableDatabase;
        ContentValues values = new ContentValues();
        values.put(IS_FINISH, DataConstant.DOWN_FINISH);
        int id = db.update(TABLE_NAME, values, "type_id = ?", new String[] {
                String.valueOf(category.getTypeId())
        });
    }
    
    public List<Integer> queryCategoryIDByFavorite() {
        DebugLog.d(TAG,"queryCategoryIDByFavorite 1");
        final SQLiteDatabase db = mReadableDatabase;
        List<Integer> list = new ArrayList<Integer>();
        Cursor cursor = db.rawQuery("select type_id from category where favorite = "
                + DataConstant.CATEGORY_FAVORITE_TRUE, null);
        DebugLog.d(TAG,"queryCategoryIDByFavorite 2");
        while (cursor.moveToNext()) {
            int typeId = cursor.getInt(0);
            list.add(typeId);
        }
        DebugLog.d(TAG,"queryCategoryIDByFavorite 3");
        if (cursor != null) {
            cursor.close();
        }
        DebugLog.d(TAG,"queryCategoryIDByFavorite 4");
        return list;
    }
    
    public List<Category> queryPicturesNoDownLoad() {
        final SQLiteDatabase db = mReadableDatabase;
        Cursor cursor = db.rawQuery("select * from category where " +
        DataConstant.CategoryColumns.DOWNLOAD_PICTURE + " = ?",new String[] {
                String.valueOf(DataConstant.DOWN_NOT_FINISH)
            });
        List<Category> list = new ArrayList<Category>();
        getCategoryListByCursor(list,cursor);
        if (cursor != null) {
            cursor.close();
        }
        return list;
    }
    
}
