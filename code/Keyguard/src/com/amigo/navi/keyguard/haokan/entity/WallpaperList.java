package com.amigo.navi.keyguard.haokan.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class WallpaperList extends ArrayList<Wallpaper>{
    
    private static final long serialVersionUID = 1L;
    
    private boolean hasMore;

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }
    
    public void quickSort(){
        Collections.sort(this, new TimeComparator());
    }
    
    class TimeComparator implements Comparator
    {
        public int compare(Object o1, Object o2)
        {
        	Wallpaper wallpaper1 = (Wallpaper) o1;
        	Wallpaper wallpaper2 = (Wallpaper) o2;
        	int flag = wallpaper1.getShowTimeBegin().compareTo(wallpaper2.getShowTimeBegin());
        	return flag;
        }
    }
    
}
