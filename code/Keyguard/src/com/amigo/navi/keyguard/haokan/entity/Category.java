
package com.amigo.navi.keyguard.haokan.entity;


import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.Toast;


import java.io.Serializable;

import com.amigo.navi.keyguard.haokan.db.DataConstant;

public class Category implements Serializable{
    private static final long serialVersionUID = 1L; // 定义序列化ID

    private int typeId;

    private String typeName;

    /**
     * 类别图标地址
     */
    private String typeIconUrl;
    
    private boolean favorite;
    
    private int typeIconResId;
    
    private int typeNameResId;
    
    private Bitmap icon;
    
    private String nameID = "";
    //0 表示从网上获取的壁纸，1表示固定壁纸
    public static final int WALLPAPER_FROM_WEB = DataConstant.INTERNET;
    public static final int WALLPAPER_FROM_FIXED_FOLDER = DataConstant.LOCAL;
    private int type = WALLPAPER_FROM_WEB;

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

   
    public int getTypeIconResId() {
        return typeIconResId;
    }

    public void setTypeIconResId(int typeIconResId) {
        this.typeIconResId = typeIconResId;
    }

    public int getTypeNameResId() {
        return typeNameResId;
    }

    public void setTypeNameResId(int typeNameResId) {
        this.typeNameResId = typeNameResId;
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

	public String getNameID() {
		return nameID;
	}

	public void setNameID(String nameID) {
		this.nameID = nameID;
	}

     
    
    

}
