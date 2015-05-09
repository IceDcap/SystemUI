
package com.amigo.navi.keyguard.haokan.entity;


import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.Toast;


import java.io.Serializable;

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

     
    
    

}
