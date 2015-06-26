
package com.amigo.navi.keyguard.haokan.entity;


import android.util.Log;

import com.amigo.navi.keyguard.DebugLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class WallpaperList extends ArrayList<Wallpaper> {

    private static final long serialVersionUID = 1L;

    private boolean hasMore;

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public void quickSort() {
        Collections.sort(this, new TimeComparator());
        
        if (DebugLog.DEBUG) {
            for (int i = 0; i < size(); i++) {
                Wallpaper wallpaper = get(i);
                DebugLog.d("WallpaperList",  "quickSort  " + i + " wallpaper.getImgName()" + wallpaper.getImgName() + " showTimeBegin : " + wallpaper.getShowTimeBegin());
            }
        }
        
    }

    class TimeComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            Wallpaper wallpaper1 = (Wallpaper) o1;
            Wallpaper wallpaper2 = (Wallpaper) o2;
            int flag = wallpaper1.getShowTimeBegin().compareTo(wallpaper2.getShowTimeBegin());
            return flag;
        }
    }
    
    /**
     * 
     * @param src
     * @param dst
     */
    public void reorderLocked(int src, int dst) {
        
        if (src >= size() || dst >= size() || src == dst) {
            return;
        }
        
        Wallpaper srcWallpaper = get(src);
        remove(src);
        add(dst, srcWallpaper);
       
    }
    
    /**
     * 
     * @return
     */
    public int indexOfLocked() {
        for (int i = 0; i < size(); i++) {
            if (get(i).isLocked()) {
                return i;
            }
        }
        return -1;
    }
    

}
