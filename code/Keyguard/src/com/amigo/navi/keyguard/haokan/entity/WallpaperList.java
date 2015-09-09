package com.amigo.navi.keyguard.haokan.entity;


import com.amigo.navi.keyguard.haokan.Common;
import com.amigo.navi.keyguard.network.local.utils.DiskUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class WallpaperList extends ArrayList<Wallpaper> {

    private static final long serialVersionUID = 1L;

    private boolean hasMore;
    
    private boolean existLocked;

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public void quickSort() {
        
        Collections.sort(this, new Comparator<Wallpaper>(){
            
            @Override
            public int compare(Wallpaper arg0, Wallpaper arg1) {
                return arg0.getSort() - arg1.getSort();
            }
            
        });
        
        for (int i = 0; i < size(); i++) {
            Wallpaper wallpaper = get(i);
            wallpaper.setShowOrder(i + 1);
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
    
    public int indexOfLocal() {
        for (int i = 0; i < size(); i++) {
            if (get(i).getImgId() == Wallpaper.WALLPAPER_FROM_PHOTO_ID) {
                return i;
            }
        }
        return -1;
    }
    
    
    public void resetOrder() {

        Collections.sort(this, new Comparator<Wallpaper>() {
            @Override
            public int compare(Wallpaper arg0, Wallpaper arg1) {
                return arg0.getShowOrder() - arg1.getShowOrder();
            }
        });
        
    }
    
    
    public int indexOfCurrent() {

        Pattern pattern = Pattern.compile("[0-9]{2}:[0-9]{2}");
        
        float currentTime = parseFloat(Common.formatCurrentTime());
        for (int i = 0; i < size(); i++) {
            Wallpaper wallpaper = get(i);
            
            if (!pattern.matcher(wallpaper.getShowTimeBegin()).matches() || !pattern.matcher(wallpaper.getShowTimeEnd()).matches()) {
                continue;
            }
            
            float start = parseFloat(wallpaper.getShowTimeBegin());
            if (start <= currentTime) {
                float end = parseFloat(wallpaper.getShowTimeEnd());
                if (currentTime <= end) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    
    private float parseFloat(String date) {
        return Float.parseFloat(date.replace(":", "."));
    }
    

    public boolean isExistLocked() {
        return existLocked;
    }

    public void setExistLocked(boolean existLocked) {
        this.existLocked = existLocked;
    }
    
    
	public List<String> getfilePaths() {
		 
		Iterator<Wallpaper> it = this.iterator();
		List<String> filePaths = new ArrayList<String>();
		while (it.hasNext()) {
			String url = it.next().getImgUrl();
			filePaths.add(DiskUtils.constructFileNameByUrl(url));
			filePaths.add(DiskUtils.constructThumbFileNameByUrl(url));
		}
		return filePaths;
	}

}
