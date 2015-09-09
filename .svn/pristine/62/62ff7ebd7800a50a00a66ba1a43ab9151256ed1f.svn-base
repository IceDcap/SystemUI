/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/

package com.android.systemui.gionee.cc.camera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.util.Log;

import com.android.systemui.R;

/**
 * {@code ImageManager} is used to retrieve and store images in the media content provider.
 */
@SuppressWarnings("unused")
public class GnImageManager {
    private static final String TAG = "QkCamera_ImageManager";

//    public static String DCIM = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
//            .toString();

    private static String DIRECTORY = null;

    private static final Uri STORAGE_URI = Images.Media.EXTERNAL_CONTENT_URI; 

    private GnImageManager() {
    }
    
    // Gionee <jiangxiao> <2014-03-21> add for CR01099405 begin
    public static class PhotoFileInfo {
    	private static final String PHOTO_TYPE = "IMG_";
    	private static final String PHOTO_EXT = ".jpg";
    	
    	private static final SimpleDateFormat sFormat = new SimpleDateFormat("yyyyMMdd_kkmmss");
    	private static final Date sDate = new Date();
    	
    	private long mTakenMillis = 0;
    	private String mTitle = null;
    	private String mFileName = null;
    	private String mFullPath = null;
    	private long mFileSize = 0;
    	
    	public static PhotoFileInfo create(long millis) {
    		PhotoFileInfo info = new PhotoFileInfo(millis);
    		
    		return info;
    	}
    	
    	public long getTakenMillis() {
    		return mTakenMillis;
    	}
    	
    	public String getTitle() {
    		return mTitle;
    	}
    	
    	public String getFileName() {
    		return mFileName;
    	}
    	
    	public String getFullPath() {
    		return mFullPath;
    	}
    	
    	public Location getLocation() {
    		return null;
    	}
    	
    	public void setFileSize(final long size) {
    		mFileSize = size;
    	}
    	
    	public long getFileSize() {
    		return mFileSize;
    	}
    	
    	private PhotoFileInfo(long millis) {
    		mTakenMillis = millis;
    		mTitle = createTitle();
    		mFileName = PHOTO_TYPE + mTitle + PHOTO_EXT;
    		mFullPath = (sPhotoDirectory + "/" + getFileName());
    	}
    	
    	private String createTitle() {
    		sDate.setTime(mTakenMillis);
    		String title = sFormat.format(sDate);
    		
    		return title;
    	}
    }
    
    public static class PhotoData {
    	private Bitmap mBitmap = null;
    	private byte[] mJpegData = null;
    	
    	public PhotoData(Bitmap bitmap, byte[] data) {
    		mBitmap = bitmap;
    		mJpegData = data.clone();
    	}
    	
    	public Bitmap getBitmap() {
    		return mBitmap;
    	}
    	
    	public byte[] getData() {
    		return mJpegData;
    	}
    }

    public static Uri savePhoto(ContentResolver cr, PhotoFileInfo info, PhotoData data, int[] rotationDegrees) {
    	boolean writeSuccess = writePhotoToFile(info, data, rotationDegrees);
    	if(!writeSuccess) {
    		Log.i(TAG, "fail to write jpeg to file");
    		return null;
    	}
    	
    	ContentValues values = createPhotoDbRecord(info, rotationDegrees);
    	Uri uri = cr.insert(Images.Media.EXTERNAL_CONTENT_URI, values);
    	
    	return uri;
    }
    // Gionee <jiangxiao> <2014-03-21> add for CR01099405 end

    public static Uri addImage(ContentResolver cr, String title, long dateTaken, Location location,
            String directory, String filename, Bitmap source, byte[] jpegData, int[] degree) {
        // We should store image data earlier than insert it to ContentProvider,
        // otherwise we may not be able to generate thumbnail in time.
        Log.i(TAG, "write jpeg data to file");
        OutputStream outputStream = null;
        String filePath = directory + "/" + filename;
        try {
            File dir = new File(directory);
            if (!dir.exists()) {
                if(!dir.mkdirs()) {
                	return null;
                }
            }
            File file = new File(directory, filename);
            outputStream = new FileOutputStream(file);
            if (source != null) {
                source.compress(CompressFormat.JPEG, 75, outputStream);
                degree[0] = 0;
            } else {
                outputStream.write(jpegData);
                degree[0] = getExifOrientation(filePath);
            }
        } catch (FileNotFoundException ex) {
            Log.w(TAG, ex);
            return null;
        } catch (IOException ex) {
            Log.w(TAG, ex);
            return null;
        } finally {
            GnCameraUtil.closeSilently(outputStream);
        }
        Log.i(TAG, "write jpeg end");

        // Read back the compressed file size.
        long size = new File(directory, filename).length();
        
        ContentValues values = new ContentValues(9);
        values.put(Images.Media.TITLE, title);

        // That filename is what will be handed to Gmail when a user shares a
        // photo. Gmail gets the name of the picture attachment from the
        // "DISPLAY_NAME" field.
        values.put(Images.Media.DISPLAY_NAME, filename);
        values.put(Images.Media.DATE_TAKEN, dateTaken);
        values.put(Images.Media.MIME_TYPE, "image/jpeg");
        values.put(Images.Media.ORIENTATION, degree[0]);
        values.put(Images.Media.DATA, filePath);
        values.put(Images.Media.SIZE, size);

        if (location != null) {
            values.put(Images.Media.LATITUDE, location.getLatitude());
            values.put(Images.Media.LONGITUDE, location.getLongitude());
        }

        Uri uri = cr.insert(STORAGE_URI, values);
        Log.i(TAG, "cr.insert" + System.currentTimeMillis());
        return uri;
    }
	
    // Gionee <jiangxiao> <2014-03-21> add for CR01099405 begin
	private static ContentValues createPhotoDbRecord(PhotoFileInfo info, int[] rotationDegrees) {
		ContentValues values = new ContentValues(9);
        values.put(Images.Media.TITLE, info.getTitle());
        values.put(Images.Media.DISPLAY_NAME, info.getFileName());
        values.put(Images.Media.DATE_TAKEN, info.getTakenMillis());
        values.put(Images.Media.MIME_TYPE, "image/jpeg");
        values.put(Images.Media.ORIENTATION, rotationDegrees[0]);
        values.put(Images.Media.DATA, info.getFullPath());
        values.put(Images.Media.SIZE, info.getFileSize());
        Location loc = info.getLocation();
        if(loc != null) {
            values.put(Images.Media.LATITUDE, loc.getLatitude());
            values.put(Images.Media.LONGITUDE, loc.getLongitude());
        }
		
		return values;
	}
	
	private static boolean writePhotoToFile(PhotoFileInfo info, PhotoData data, int[] rotationDegrees) {
		boolean success = true;
		
		OutputStream outputStream = null;
		try {
			File dir = new File(sPhotoDirectory);
            if (!dir.exists()) {
                if(!dir.mkdirs()) {
                	return false;
                }
            }
            File photoFile = new File(sPhotoDirectory, info.getFileName());
            outputStream = new FileOutputStream(photoFile);
            
            if (data.getBitmap() != null) {
            	data.getBitmap().compress(CompressFormat.JPEG, 75, outputStream);
            	if(rotationDegrees != null && rotationDegrees.length > 1) {
            		rotationDegrees[0] = 0;
            	}
            } else if(data.getData() != null) {
                outputStream.write(data.getData());
                if(rotationDegrees != null && rotationDegrees.length > 1) {
                	rotationDegrees[0] = getExifOrientation(info.getFullPath());
                }
            }
            
            info.setFileSize(photoFile.length());
		} catch (FileNotFoundException ex) {
            Log.w(TAG, "FileNotFoundException");
            success = false;
        } catch (IOException ex) {
            Log.w(TAG, "IOException");
            success = false;
        } finally {
            GnCameraUtil.closeSilently(outputStream);
        }
		
		return success;
	}
    // Gionee <jiangxiao> <2014-03-21> add for CR01099405 end

    public static int getExifOrientation(String filepath) {
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filepath);
        } catch (IOException ex) {
            Log.e(TAG, "cannot read exif", ex);
        }
        
        int degree = 0;
        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                // We only recognize a subset of orientation tag values.
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                    default:
                        break;
                }
            }
        }
        return degree;
    }

    // Gionee <jiangxiao> <2014-03-21> add for CR01099405 begin
    private static String sPhotoDirectory = "";
    
    public static void setPhotoDirectory(final String name) {
    	sPhotoDirectory = name;
    }
    // Gionee <jiangxiao> <2014-03-21> add for CR01099405 end

}
