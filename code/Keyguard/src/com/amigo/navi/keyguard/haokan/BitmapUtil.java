package com.amigo.navi.keyguard.haokan;

import java.io.ByteArrayOutputStream;

import com.amigo.navi.keyguard.DebugLog;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;


public final class BitmapUtil {
	
    private static String TAG = "BitmapUtil";
    
	  public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
	        int width = bm.getWidth();
	        int height = bm.getHeight();
	        float scaleWidth = ((float) newWidth) / width;
	        float scaleHeight = ((float) newHeight) / height;
	        Matrix matrix = new Matrix();
	        matrix.postScale(scaleWidth, scaleHeight);
	        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
	        return resizedBitmap;
	    }
	  
	  
	  /**
	     * @author xuebo
	     * @param bm
	     * @param newHeight
	     * @param newWidth
	     * @return cut Bitmap to SingleScreen wallpaper
	     */
	    public static Bitmap getResizedBitmapForSingleScreen(Bitmap bm, int newHeight, int newWidth) {
	        int width = bm.getWidth();
	        int height = bm.getHeight();
	        if(DebugLog.DEBUG){
	        	DebugLog.e("ddd", "===newHeight==" + newHeight + "==newWidth==" + newWidth + "===bt width==" + width + "===bt height==" +height);
	        }
	        Bitmap afterCutBitmap = null;
	        float newScale = (float) newHeight/(float)newWidth;
	        float sorceScale = (float) height/(float)width;
	    	if(sorceScale > newScale){
	    		int cutHeight = (int) (width * newScale);
	    		afterCutBitmap = Bitmap.createBitmap(bm, 0, (height - cutHeight)/2, width, cutHeight);
	    	}else{
	    		int cutWidth = (int)(height/newScale);
	    		afterCutBitmap = Bitmap.createBitmap(bm, (width - cutWidth)/2, 0, cutWidth, height);
	    	}
	    	
	    	int afterCutWidth = afterCutBitmap.getWidth();
	    	int afterCutHeight = afterCutBitmap.getHeight();
	        float scaleWidth = ((float) newWidth) / afterCutWidth;
	        float scaleHeight = ((float) newHeight) / afterCutHeight;
	        if(DebugLog.DEBUG){
	        	DebugLog.e("ddd", "===afterCutWidth==" + afterCutWidth + "==afterCutHeight==" + afterCutHeight);
	        }
	        Matrix matrix = new Matrix();
	        matrix.postScale(scaleWidth, scaleHeight);
	        Bitmap resizedBitmap = Bitmap.createBitmap(afterCutBitmap, 0, 0, afterCutWidth, afterCutHeight, matrix, false);
	        return resizedBitmap;
	    }
	    
	    
	    
	   public static byte[] convertBitmap(Bitmap bmp){
			ByteArrayOutputStream output = new ByteArrayOutputStream();//初始化一个流对象
	        bmp.compress(CompressFormat.JPEG, 100, output);//把bitmap100%高质量压缩 到 output对象里
	        byte[] result = output.toByteArray();//转换成功了
	        try {
	            output.close();
	            return result;
	        } catch (Exception e) {
	            e.printStackTrace();
	            return null;
	        }
		}
	   
	   public static void recycleBitmap(Bitmap bitmap){
	       if(bitmap != null && !bitmap.isRecycled()){
	           if(DebugLog.DEBUG){
	                DebugLog.d("BitmapUtil", "recycleBitmap hashCode = " + bitmap);
	           }
	           bitmap.recycle();
	           bitmap=null;
	       }
	   }
	   
	   
	   public static Bitmap resizedBitmap(byte[] result , int width, int height) {
	       
           Bitmap bitmap = null;
          
           try {
               
               final BitmapFactory.Options options = new BitmapFactory.Options();
               options.inJustDecodeBounds = true;
               BitmapFactory.decodeByteArray(result, 0, result.length, options);
               options.inPreferredConfig = Bitmap.Config.ARGB_8888;
               
               DebugLog.v(TAG, "options.outWidth = " + options.outWidth + ", options.outHeight = " + options.outHeight);
               
               if (width > height) {
                   options.inDensity = options.outWidth;
                   options.inTargetDensity = 2 * width;
               } else {
                   options.inDensity = options.outHeight;
                   options.inTargetDensity = height;
               }
               
               options.inJustDecodeBounds = false;
               Bitmap bitmapTemp = BitmapFactory.decodeByteArray(result, 0, result.length, options);
               
               int bitmapWidth = bitmapTemp.getWidth();
               int bitmapHeight = bitmapTemp.getHeight();
               DebugLog.v(TAG, " bitmapWidth = " + bitmapWidth + ", bitmapHeight" + bitmapHeight);
               
               if (bitmapWidth > width && bitmapHeight > height) {
                   
                   bitmap = Bitmap.createBitmap(bitmapTemp, (bitmapWidth - width) / 2,
                           (bitmapHeight - height) / 2, width, height);

                   if (bitmapTemp != null && !bitmapTemp.isRecycled()) {
                       bitmapTemp.recycle();
                   }
                   
               }else {
                   bitmap = bitmapTemp;
               }

           } catch (Exception e) {
                Log.v(TAG, "resizedBitmap", e);
           } catch (OutOfMemoryError e) {
                Log.e(TAG, "resizedBitmap", e);
           } 
           
           return bitmap;
       }
	   
}
