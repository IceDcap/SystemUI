
package com.amigo.navi.keyguard.haokan.entity;


import android.graphics.Bitmap;


import java.io.Serializable;

import com.amigo.navi.keyguard.haokan.db.DataConstant;

public class Category implements Serializable{
    private static final long serialVersionUID = 1L;  

    private int typeId;

    private String typeName;
    
    private String typeNameEn;
   
    private String typeIconUrl;
    
    private boolean favorite;
    
    private Bitmap icon;
    
 
    //0 表示从网上获取的壁纸，1表示固定壁纸
    public static final int IMAGE_FROM_WEB = DataConstant.INTERNET;
    public static final int IMAGE_FROM_FIXED_FOLDER = DataConstant.LOCAL_ASSETS;
    private int type = IMAGE_FROM_WEB;
    public static final int PIC_DOWNLOAD_FINISH = DataConstant.DOWNLOAD_FINISH;
    public static final int PIC_DOWNLOAD_NOT_FINISH = DataConstant.NOT_DOWNLOAD;
    private int isPicDownLod;
    private int sort;
    
    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeIconUrl() {
        return typeIconUrl;
    }

    public void setTypeIconUrl(String typeIconUrl) {
        this.typeIconUrl = typeIconUrl;
    }

   
    public Bitmap getIcon() {
        return icon;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

 
	public int getIsPicDownLod() {
		return isPicDownLod;
	}

	public void setIsPicDownLod(int isPicDownLod) {
		this.isPicDownLod = isPicDownLod;
	}

	public int getSort() {
		return sort;
	}

	public void setSort(int sort) {
		this.sort = sort;
	}

    public String getTypeNameEn() {
        return typeNameEn;
    }

    public void setTypeNameEn(String typeNameEn) {
        this.typeNameEn = typeNameEn;
    }

    

}
