package com.amigo.navi.keyguard.haokan.entity;

import java.util.ArrayList;

public class WallpaperList extends ArrayList<Wallpaper>{
    
    private static final long serialVersionUID = 1L;
    
    private boolean hasMore;

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }
    
}
