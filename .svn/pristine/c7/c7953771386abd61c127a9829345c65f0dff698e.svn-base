package com.amigo.navi.keyguard;

import java.lang.reflect.Field;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.SystemProperties;
import android.util.DisplayMetrics;

import com.android.keyguard.R;


public class KWDataCache {
    
	private static final String TAG = "KWDataCache";
	
	public static final int SCREEN_COUNT = 4;
	private static final int INVALID_VALUE = 0;

	public static final int WORKSPACE_ROW_COUNT = 4;
	public static final int WORKSPACE_COL_COUNT = 4;

	private static int sScreenWidth = INVALID_VALUE;
	private static int sScreentHeight = INVALID_VALUE;

	private static int sWorkspaceCellWidth = INVALID_VALUE;
	private static int sWorkSpaceCellHeight = INVALID_VALUE;
	private static int sStatusBarHegiht = INVALID_VALUE;
	private static int sXPageHegiht = INVALID_VALUE;
	
	public static int getScreenWidth(Resources resources){
		if(sScreenWidth == INVALID_VALUE){
			sScreenWidth = resources.getDisplayMetrics().widthPixels;
		}
		if(DebugLog.DEBUG){
			DebugLog.d(TAG, "sScreenWidth=" + sScreenWidth);
		}
		return sScreenWidth;
	}
	
	public static int getScreenHeight(Resources resources){
		if(sScreentHeight == INVALID_VALUE){
			boolean isScreenPortrait = resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
			DisplayMetrics displayMetrics = resources.getDisplayMetrics();
			/*if(isScreenPortrait){
				sScreentHeight = displayMetrics.heightPixels;
			}else{
				sScreentHeight = displayMetrics.widthPixels;
			}*/
			if(displayMetrics.heightPixels > displayMetrics.widthPixels){
				sScreentHeight = displayMetrics.heightPixels;
			}else{
				sScreentHeight = displayMetrics.widthPixels;
			}
		}
		if(DebugLog.DEBUG){
			DebugLog.d(TAG, "getScreenHeight=" + sScreentHeight);
		}
		return sScreentHeight;
	}

	public static int getWorkspaceCellWidth(Resources resources) {
		if (sWorkspaceCellWidth == INVALID_VALUE) {
			sWorkspaceCellWidth = ((getScreenWidth(resources)-2 * resources.getDimensionPixelSize(R.dimen.kg_cell_layout_padding))/ WORKSPACE_COL_COUNT);
			if(DebugLog.DEBUG){
				DebugLog.d(TAG, "getWorkspaceCellWidth.....sWorkspaceCellWidth=" + sWorkspaceCellWidth);
			}
		}
		return sWorkspaceCellWidth;
	}

	public static int getWorkspaceCellHeight(Resources resources) {
		if (sWorkSpaceCellHeight == INVALID_VALUE) {
			sWorkSpaceCellHeight = ((getScreenHeight(resources) - 
					2 * resources.getDimensionPixelSize(R.dimen.kg_cell_layout_padding)
					-resources.getDimensionPixelSize(R.dimen.kg_infozone_height) 
					- getStatusBarHeight()-resources.getDimensionPixelSize(R.dimen.kg_exitworkspace_height)+getNavBarHeight(resources)) 
					/ (WORKSPACE_ROW_COUNT));
			if(DebugLog.DEBUG){
				DebugLog.d(TAG, "getWorkspaceCellHeight.....sWorkSpaceCellHeight="
						+ sWorkSpaceCellHeight);
			}
		}
		return sWorkSpaceCellHeight;
	}

	public static int getStatusBarHeight() {
		if (sStatusBarHegiht == INVALID_VALUE) {
			sStatusBarHegiht = Resources.getSystem().getDimensionPixelSize(
					Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android"));
			if(DebugLog.DEBUG){
				DebugLog.d(TAG, "getStatusBarHeight.....mStatusBarHegiht="
						+ sStatusBarHegiht+"Build.DISPLAY="+Build.DISPLAY+"Build.MODEL="+Build.MODEL);
			}
		}
		return sStatusBarHegiht;
	}
	
	public static float getAppIconSize() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public static int getXPageHeight(Resources resources){
		if (sXPageHegiht == INVALID_VALUE) {
			sXPageHegiht = getScreenHeight(resources)/*-getStatusBarHeight()*/
								- resources.getDimensionPixelSize(R.dimen.ketguard_infozone_height)
/*								- resources.getDimensionPixelSize(R.dimen.kg_infozone_margin_bottom)
								- resources.getDimensionPixelSize(R.dimen.kg_infozone_margin_top_at_desk)*/
								+getNavBarHeight(resources);
		}
		if(DebugLog.DEBUG){
			DebugLog.d(TAG, "getXPageHeight=" + sXPageHegiht);
		}
		return sXPageHegiht;
	}
	
	
	 private static int WallpaperHeight = INVALID_VALUE;
	    public static int getShowWallpaperHeight(Context context){
	    	if(WallpaperHeight == INVALID_VALUE && context != null){
	    		WallpaperManager wpm = WallpaperManager.getInstance(context);
	    		WallpaperHeight =  wpm.getDesiredMinimumHeight();
	    	}else{
	    		WallpaperHeight = getAllScreenHeigt(context);	
	    	}
	    	return WallpaperHeight;
	    }
	    
	    
	    private static int REAL_SCREEN_HEIGT = INVALID_VALUE;
	    public static int getAllScreenHeigt(Context context) {
	    	if(REAL_SCREEN_HEIGT == INVALID_VALUE && context != null){
	    		REAL_SCREEN_HEIGT=getScreenHeight(context.getResources())+getNavBarHeight(context.getResources());
	    		
	    	}
	        return REAL_SCREEN_HEIGT;
	    }
	    
	    /**
	     * get id by reflection,because of the resource id is mutability
	     * 
	     */
	    public static int getNavBarHeight(Context context) {

	        int navBarHeight = 0;
	        try {
	            Class<?> clazz = Class.forName("com.android.internal.R$bool");
	            Field field = clazz.getField("config_showNavigationBar");
	            int id = field.getInt(null);
	            boolean hasNavBar = context.getResources().getBoolean(id);
	            String navBarOverride = SystemProperties.get("qemu.hw.mainkeys");
	            if ("1".equals(navBarOverride)) {
	                hasNavBar = false;
	            } else if ("0".equals(navBarOverride)) {
	                hasNavBar = true;
	            }
	            if (hasNavBar) {
	                Class<?> navBarclazz = Class.forName("com.android.internal.R$dimen");
	                Field navBarfield = navBarclazz.getField("navigation_bar_height");
	                int navBarHeightId = navBarfield.getInt(null);
	                navBarHeight=context.getResources().getDimensionPixelSize(navBarHeightId);

	            }
	            if(DebugLog.DEBUG) DebugLog.d(TAG, "hasNavBar: " + hasNavBar + "  navBarHeight: " + navBarHeight + " id1:  "
	                    + com.android.internal.R.bool.config_showNavigationBar + "  id2: " + id);
	        } catch (Exception e) {
	            e.printStackTrace();
	            DebugLog.e(TAG, "getNavBarHeight e："+e.toString());
	        }
	        return navBarHeight;
	    }
	    
	    public static int getNavBarHeight(Resources resources) {
	        int navBarHeight = 0;
	        try {
	            Class<?> clazz = Class.forName("com.android.internal.R$bool");
	            Field field = clazz.getField("config_showNavigationBar");
	            int id = field.getInt(null);
	            boolean hasNavBar = resources.getBoolean(id);
	            String navBarOverride = SystemProperties.get("qemu.hw.mainkeys");
	            if ("1".equals(navBarOverride)) {
	                hasNavBar = false;
	            } else if ("0".equals(navBarOverride)) {
	                hasNavBar = true;
	            }
	            if (hasNavBar) {
	                Class<?> navBarclazz = Class.forName("com.android.internal.R$dimen");
	                Field navBarfield = navBarclazz.getField("navigation_bar_height");
	                int navBarHeightId = navBarfield.getInt(null);
	                navBarHeight=resources.getDimensionPixelSize(navBarHeightId);

	            }
	            if(DebugLog.DEBUG) DebugLog.d(TAG, "hasNavBar: " + hasNavBar + "  navBarHeight: " + navBarHeight + " id1:  "
	                    + com.android.internal.R.bool.config_showNavigationBar + "  id2: " + id);
	        } catch (Exception e) {
	            e.printStackTrace();
	            DebugLog.e(TAG, "getNavBarHeight e："+e.toString());
	        }
	        return navBarHeight;
	    }
	    
	
}
