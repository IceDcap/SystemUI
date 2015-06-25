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
    public static final String TODAY_IMG = CategoryColumns.TODAY_IMAGE;
    public static final String SORT = CategoryColumns.SORT;

    
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
        
    public synchronized void deleteAll() {
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
    private synchronized void insertCategorysNoTransaction(List<Category> list,
            final SQLiteDatabase db) {
        for (Category category : list) {
            ContentValues values = new ContentValues();
            values.put(TYPE_ID, category.getTypeId());
            values.put(TYPE_NAME, category.getTypeName());
            values.put(TYPE_ICON_URL, category.getTypeIconUrl());
            
            values.put(FAVORITE, category.isFavorite());
            
            long id = db.insert(TABLE_NAME, null, values);
        }
    }
    
    private synchronized void updateCategorysNoTransaction(List<Category> list,
            final SQLiteDatabase db) {
    	DebugLog.d(TAG,"updateCategorysNoTransaction updateCategorysNoTransaction list.size:" + list.size());
        for (Category category : list) {
            ContentValues values = new ContentValues();
            values.put(TYPE_ID, category.getTypeId());
            values.put(TYPE_NAME, category.getTypeName());
            values.put(TYPE_ICON_URL, category.getTypeIconUrl());
        	DebugLog.d(TAG,"updateCategorysNoTransaction category.getTypeId():" + category.getTypeId());
            Category categoryInDB = queryCategoryByTypeID(category.getTypeId());
        	DebugLog.d(TAG,"updateCategorysNoTransaction categoryInDB:" + categoryInDB);
            if(categoryInDB != null && categoryInDB.getTypeId() != 0){
            	DebugLog.d(TAG,"updateCategorysNoTransaction categoryInDB.getTypeId():" + categoryInDB.getTypeId());
            	DebugLog.d(TAG,"updateCategorysNoTransaction categoryInDB.isFavorite():" + categoryInDB.isFavorite());
            	values.put(FAVORITE, categoryInDB.isFavorite());
            }
            if(categoryInDB != null){
            	values.put(IS_FINISH, categoryInDB.getIsPicDownLod());
            }
            values.put(TODAY_IMG, DataConstant.TODAY_IMAGE);
            values.put(SORT, category.getSort());
            db.replace(TABLE_NAME, null, values);
        }
    }
    
    public synchronized void deleteNotToday() {
        final SQLiteDatabase db = mWritableDatabase;
        db.execSQL("delete from category where " + DataConstant.CategoryColumns.TODAY_IMAGE + "=" + 
        DataConstant.NOT_TODAY_IMAGE);
    }
    
    public synchronized void insertAfterDeleteAll(List<Category> list){
        DebugLog.d(TAG,"insertAfterDeleteAll 1");         
        final SQLiteDatabase db = mWritableDatabase;
        db.beginTransaction();
        updateNotTodayImg();
        updateCategorysNoTransaction(list, db);
        deleteNotToday();
        db.setTransactionSuccessful();  
        db.endTransaction(); 
        DebugLog.d(TAG,"insertAfterDeleteAll 2");         
    }
    
    public List<Category> queryCategorys(){
        final SQLiteDatabase db = mWritableDatabase;
//        String sql = "select * from " + TABLE_NAME
//                + " where download_picture = 1 and " + DataConstant.CategoryColumns.TODAY_IMAGE + "=" + 
//                DataConstant.TODAY_IMAGE + " order by " + DataConstant.CategoryColumns.SORT + " asc";
        String sql = "select * from " + TABLE_NAME
                + " where  " + DataConstant.CategoryColumns.TODAY_IMAGE + "=" + 
                DataConstant.TODAY_IMAGE + " order by " + DataConstant.CategoryColumns.SORT + " asc";
        
        Cursor cursor = db.rawQuery(sql, null);
        List<Category> list = new ArrayList<Category>();
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
            Category category = queryCategory(cursor);
            list.add(category);
        }
    }

	private Category queryCategory(Cursor cursor) {
		Category category = new Category();
		int typeId = cursor.getInt(0);
		String typeName = cursor.getString(1);
		String typeIconUrl = cursor.getString(2);
		boolean favorite = cursor.getInt(3) == DataConstant.CATEGORY_FAVORITE_TRUE;
		
		category.setTypeId(typeId);
		category.setTypeName(typeName);
		category.setTypeIconUrl(typeIconUrl);
		category.setFavorite(favorite);
//		category.setTypeIconResId(cursor.getInt(4));
//		category.setTypeNameResId(cursor.getInt(5));
		category.setNameID(cursor.getString(cursor.getColumnIndex(DataConstant.CategoryColumns.EN_NAME)));
		category.setType(cursor.getInt(cursor.getColumnIndex(DataConstant.CategoryColumns.SAVE_TYPE)));
		return category;
	}
    
    public synchronized  int updateFavorite(Category category) {
        final SQLiteDatabase db = mWritableDatabase;
        ContentValues values = new ContentValues();
        values.put(FAVORITE, category.isFavorite() ? DataConstant.CATEGORY_FAVORITE_TRUE
                : DataConstant.CATEGORY_FAVORITE_FALSE);
        int id = db.update(TABLE_NAME, values, "type_id = ?", new String[] {
                String.valueOf(category.getTypeId())
        });
        return id;

    }
    
    public synchronized void updateDownLoadFinish(Category category){
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
                + DataConstant.CATEGORY_FAVORITE_TRUE + " and " + 
        		DataConstant.CategoryColumns.TODAY_IMAGE + "=" + 
                DataConstant.TODAY_IMAGE, null);
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
        DataConstant.CategoryColumns.DOWNLOAD_PICTURE + " = ?" + 
        		" and " + DataConstant.CategoryColumns.TODAY_IMAGE + 
        		" = ?",new String[] {
                String.valueOf(DataConstant.DOWN_NOT_FINISH),
                String.valueOf(DataConstant.TODAY_IMAGE),
            });
        List<Category> list = new ArrayList<Category>();
        getCategoryListByCursor(list,cursor);
        if (cursor != null) {
            cursor.close();
        }
        return list;
    }
    
    public List<Integer> queryAllCategoryTypeID() {
        final SQLiteDatabase db = mReadableDatabase;
        Cursor cursor = db.rawQuery("select * from category"
            ,null,null);
        List<Integer> list = new ArrayList<Integer>();
        getCategoryID(list,cursor);
        if (cursor != null) {
            cursor.close();
        }
        return list;
    }
    
    public Category queryCategoryByTypeID(int id) {
        final SQLiteDatabase db = mReadableDatabase;
        Cursor cursor = db.rawQuery("select * from category where " +
        DataConstant.CategoryColumns.TYPE_ID + " = ?",new String[] {
                String.valueOf(id)
            });
        Category category = null;
        if(cursor != null && cursor.getCount() > 0){
        	cursor.moveToFirst();
        	category = queryCategory(cursor);
        }
        if(cursor != null){
        	cursor.close();
        }
        return category;
    }
    
    private void getCategoryID(List<Integer> list, Cursor cursor) {
        while (cursor.moveToNext()) {
            int typeId = cursor.getInt(0);
            list.add(typeId);
        }
    }
    
    public synchronized  void updateNotTodayImg(){
        DebugLog.d(TAG,"updateNotTodayImg");         
        final SQLiteDatabase db = mWritableDatabase;
        ContentValues values = new ContentValues();
        values.put(DataConstant.CategoryColumns.TODAY_IMAGE, DataConstant.NOT_TODAY_IMAGE);
        db.update(TABLE_NAME, values, null, null);
    }
    
    public List<Category> queryHasCategoryNotToday(){ 
        final SQLiteDatabase db = mReadableDatabase;
        Cursor cursor = db.rawQuery("select * from category where " +
        DataConstant.WallpaperColumns.TODAY_IMAGE + " = ?",new String[] {
                String.valueOf(DataConstant.NOT_TODAY_IMAGE)
            });
        List<Category> list = new ArrayList<Category>();
        getCategoryListByCursor(list, cursor);        
        if(cursor != null){
        	cursor.close();
        }
        return list;
    }
    
    public boolean queryHasDownLoadImageNotLocalData(){
        final SQLiteDatabase db = mReadableDatabase;
        Cursor cursor = db.rawQuery("select * from category where " +
        DataConstant.CategoryColumns.DOWNLOAD_PICTURE + " = ? and " + 
        DataConstant.CategoryColumns.SAVE_TYPE + " = ?",new String[] {
                String.valueOf(DataConstant.DOWN_FINISH),
                String.valueOf(DataConstant.INTERNET)
            });
        if(cursor != null){
        	if(cursor.getCount() > 0){
                closeCursor(cursor);
        		return true;
        	}
        }
        closeCursor(cursor);
        return false;
    }
    
    private void closeCursor(Cursor cursor){
        if(cursor != null){
            cursor.close();
        }
    }
    
}
