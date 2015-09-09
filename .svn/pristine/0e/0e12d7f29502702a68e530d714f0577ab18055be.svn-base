package com.android.systemui.screenshot;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Notification;
import android.app.Notification.BigPictureStyle;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.IWindowManager;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;

import com.android.systemui.R;

public class GnSnapshotService {
	final static String TAG = "ScreenSnapshotService";
	final static String ROOT_DIR = "/storage/sdcard0/";
	final static String SCREENSHOTS_DIR_NAME = "Screenshots";
	final static String SCREENSHOT_FILE_NAME_TEMPLATE = "Screenshot_%s.png";
	final static String SCREENSHOT_TEMP_FILE = ROOT_DIR + File.separator + ".tmpscreenshot";

	private File mScreenshotDir;
	private String mImageFileName;
	private String mImageFilePath;
	private Bitmap mBitmap = null;
	// If captureCurrentScreen() is called, then mFromCached == false
	// Else if takeScreenShot() is called, then mFromCached == true.
	// Normally the takeScreenShot() is more effective
	private static boolean mFromCached = true;

	private static Context mContext;
	private static ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

	private static GnSnapshotService sInstance = null;

	/**
	 * The only entry to get ScreenSnapshotService
	 * */
	public static synchronized GnSnapshotService getService(Context context) {
		mContext = context;
		if (sInstance == null) {
			sInstance = new GnSnapshotService();
		}
		return sInstance;
	}

	private GnSnapshotService() {
	}

	final class SavingRunnale implements Runnable {
		private Bitmap bitmap;
		private String path;
		private boolean needNotify;

		public SavingRunnale(final Bitmap bitmap, final String path, final boolean needNotify) {
			this.bitmap = bitmap;
			this.path = path;
			this.needNotify = needNotify;
		}

		@Override
		public void run() {
			savePicture(bitmap, path, needNotify);
		}
	}

	/**
	 * Capture current screen, and save it to storage
	 * */
	static public void captureCurrentScreen() {
		mExecutorService.execute(new Runnable() {

			@Override
			public void run() {
				File file = new File(GnSnapshotService.SCREENSHOT_TEMP_FILE);
				if (file.exists()) {
					if(!file.delete()) {
						return;
					}
				}

				mFromCached = false;

				try {
					Runtime.getRuntime().exec(
							"screencap -p " + GnSnapshotService.SCREENSHOT_TEMP_FILE);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Shot the screen by called system API.It seems the class {@link SurfaceControl }is only found
	 * on Gionee platform. And the API Surface.screenshot() is never effect? Why?
	 * 
	 * @param Context
	 * @return Bitmap
	 * */
	public Bitmap takeScreenShot() {
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		DisplayMetrics displayMetrics = new DisplayMetrics();
		display.getRealMetrics(displayMetrics);
		float[] dims = { displayMetrics.widthPixels, displayMetrics.heightPixels };
		float degrees = getDegreesForRotation(display.getRotation());

		mFromCached = true;

		boolean needRotate = (degrees > 0f);
		if (needRotate) {
			Matrix matrix = new Matrix();
			matrix.reset();
			matrix.preRotate(-degrees);
			matrix.mapPoints(dims);
			dims[0] = Math.abs(dims[0]);
			dims[1] = Math.abs(dims[1]);
		}
		
		Bitmap bitmap = null;
		// Take the screenshot
		bitmap = SurfaceControl.screenshot((int) dims[0], (int) dims[1]);
        if (bitmap == null) {
            Log.e(TAG, "takeScreenShot bitmap = null");
            return null;
        }
		
        if (needRotate) {
            Bitmap ss = Bitmap.createBitmap(displayMetrics, displayMetrics.widthPixels,
                    displayMetrics.heightPixels, Config.ARGB_8888);
            Canvas c = new Canvas(ss);
            c.translate(ss.getWidth() / 2, ss.getHeight() / 2);
            c.rotate(degrees);
            c.translate(-dims[0] / 2, -dims[1] / 2);
            c.drawBitmap(bitmap, 0, 0, null);
            c.setBitmap(null);
            // Recycle the previous bitmap
            bitmap.recycle();
            bitmap = ss;
        }
        bitmap.setHasAlpha(false);
        bitmap.prepareToDraw();
		
        boolean showNav = false;
        try {
            IWindowManager wms = WindowManagerGlobal.getWindowManagerService();
            showNav = wms.hasNavigationBar();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        if (showNav) {
            int naviH = mContext.getResources().getDimensionPixelSize(
                    com.android.internal.R.dimen.navigation_bar_height);
            int w = bitmap.getWidth();
            int h = bitmap.getHeight() - naviH;
            Bitmap noNavi = Bitmap.createBitmap(bitmap, 0, 0, w, h);
            bitmap.recycle();
            bitmap = noNavi;
        }
		
        if (mBitmap != null) {
        	mBitmap.recycle();
        }
        
		mBitmap = bitmap;
		// mExecutorService.execute(new SavingRunnale(bitmap, SCREENSHOT_TEMP_FILE, false));
		return bitmap;
	}

	public Bitmap getBitmap() {
		return mBitmap.copy(Config.ARGB_8888, false);
	}

	public int getScreenOrierention() {
		if (mBitmap != null) {
			int width = mBitmap.getWidth();
			int height = mBitmap.getHeight();
			if (width < height) {
				log("ActivityInfo.SCREEN_ORIENTATION_PORTRAIT");
				return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;//Configuration.ORIENTATION_LANDSCAPE;
			}
			log("ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE");
			return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;//Configuration.ORIENTATION_PORTRAIT;
		}
		log("ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED");
		return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;//Configuration.ORIENTATION_UNDEFINED;
	}
	
	/**
	 * Capture the selected area, and save it to File system
	 * 
	 * @param
	 * @return
	 * */
	public void captureSelectedArea(final Location location) {
		String state = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(state)) {
			return;
		}
		mScreenshotDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), SCREENSHOTS_DIR_NAME);
		mScreenshotDir.mkdirs();
		
		String date = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date(System
				.currentTimeMillis()));
		mImageFileName = String.format(SCREENSHOT_FILE_NAME_TEMPLATE, date);
		mImageFilePath = new File(mScreenshotDir, mImageFileName).getAbsolutePath();
		
		Bitmap bitmap = generateBitmap(location, mFromCached);
		mExecutorService.execute(new SavingRunnale(bitmap, mImageFilePath, true));
	}

	private float getDegreesForRotation(int value) {
		switch (value) {
		case Surface.ROTATION_90:
			return 360f - 90f;
		case Surface.ROTATION_180:
			return 360f - 180f;
		case Surface.ROTATION_270:
			return 360f - 270f;
		}
		return 0f;
	}

	/**
	 * Generate bitmap from the selected area
	 * 
	 * @param
	 * @return Bitmap
	 * */
	private Bitmap generateBitmap(Location location, boolean fromCached) {
		Bitmap bitmap = null;
		if (fromCached) {
			bitmap = mBitmap;
		} else {
			File bitmapFile = new File(SCREENSHOT_TEMP_FILE);
			if (bitmapFile.exists() && bitmapFile.canRead()) {
				bitmap = BitmapFactory.decodeFile(SCREENSHOT_TEMP_FILE);
			}
		}
		if (bitmap != null) {
			int bitMapWidth = bitmap.getWidth();
			int bitMapHeight = bitmap.getHeight();
			int width = location.width;
			int height = location.height;
			log(bitMapWidth, bitMapWidth, location);
			
			// GIONEE <wujj> <2015-01-25> modify for CR01441569 begin
			if (location.startX + width > bitMapWidth) {
				width = bitMapWidth - location.startX;
			}
			
			if (location.startY + height > bitMapHeight) {
				height = bitMapHeight - location.startY;
			}
			// GIONEE <wujj> <2015-01-25> modify for CR01441569 end

			if (width <= 0) {
				width = 20;
			}

			if (height <= 0)
				height = 20;
			bitmap = Bitmap.createBitmap(bitmap, location.startX, location.startY, width,
					height);
		}
		return bitmap;
	}

	/**
	 * Save picture to file system.If this happens in clicking save button, then notification should
	 * send to notify user
	 * 
	 * @param Bitmap
	 *            is the captured picture of screen
	 * @param String
	 *            is the file path
	 * @param boolean flag to mark if notification should send or not
	 * */
	private void savePicture(Bitmap bitmap, String path, boolean needNotify) {
		if (bitmap != null) {
			if (needNotify) {
				sendNotification(bitmap);
				updateNotification(bitmap);
			}
			if (!bitmap.isRecycled()) {
				bitmap.recycle();
				bitmap = null;
			}
		}
	}

	/**
	 * Save the captured picture to media database
	 * 
	 * @param Bitmap
	 * @return Uri
	 * */
	private Uri saveToMediaStore(Bitmap bitmap) {
		Uri uri = null;
		try {
			long mImageTime = System.currentTimeMillis();
			long dateSeconds = mImageTime / 1000;
			ContentValues values = new ContentValues();
			ContentResolver resolver = mContext.getContentResolver();
			values.put(MediaStore.Images.ImageColumns.DATA, mImageFilePath);
			values.put(MediaStore.Images.ImageColumns.TITLE, mImageFileName);
			values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME,
					mImageFileName);
			values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, mImageTime);
			values.put(MediaStore.Images.ImageColumns.DATE_ADDED, dateSeconds);
			values.put(MediaStore.Images.ImageColumns.DATE_MODIFIED,
					dateSeconds);
			values.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/png");
			values.put(MediaStore.Images.ImageColumns.WIDTH, bitmap.getWidth());
			values.put(MediaStore.Images.ImageColumns.HEIGHT, bitmap.getWidth());
			uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					values);
			log("uri = " + uri);
			OutputStream out = resolver.openOutputStream(uri);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
			values.clear();
			values.put(MediaStore.Images.ImageColumns.SIZE, new File(
					mImageFilePath).length());
			resolver.update(uri, values, null, null);
		} catch (Exception e) {
			Log.v(TAG, "Bitmap save error!");
		}
		return uri;
	}

	/**
	 * Notification parameters and methods
	 * */
	NotificationManager mNotificationManager;
	Notification.Builder mNotificationBuilder, mPublicNotificationBuilder;
	static final int mNotificationId = GlobalScreenshot.SCREENSHOT_NOTIFICATION_ID; // Notification ID

	/**
	 * Send a notification when save button clicked. It's called before writing file system
	 * */
	private void sendNotification(Bitmap bitmap) {
		mNotificationManager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);

		final long now = System.currentTimeMillis();
		mNotificationBuilder = new Notification.Builder(mContext);
		mNotificationBuilder.setTicker(mContext.getString(R.string.saving));
		mNotificationBuilder.setContentTitle(mContext.getString(R.string.screenshot_saved_title));
		mNotificationBuilder.setContentText(mContext.getString(R.string.saving));
		mNotificationBuilder.setSmallIcon(R.drawable.stat_notify_image);
		mNotificationBuilder.setWhen(now);
		mNotificationBuilder.setAutoCancel(true);
		// Make sure the notification is on the top of notification list usually
		mNotificationBuilder.setPriority(Notification.PRIORITY_HIGH);
		
		// For "public" situations we want to show all the same info but
        // omit the actual screenshot image.
		Resources r = mContext.getResources();
        mPublicNotificationBuilder = new Notification.Builder(mContext)
                .setContentTitle(r.getString(R.string.screenshot_saving_title))
                .setContentText(r.getString(R.string.screenshot_saving_text))
                .setSmallIcon(R.drawable.stat_notify_image)
                //.setCategory(Notification.CATEGORY_PROGRESS)
                .setWhen(now)
                .setColor(r.getColor(
                        com.android.internal.R.color.system_notification_accent_color));

        mNotificationBuilder.setPublicVersion(mPublicNotificationBuilder.build());
		
		Notification notification = mNotificationBuilder.build();
		notification.flags |= Notification.FLAG_NO_CLEAR;
		mNotificationManager.notify(mNotificationId, notification);
	}

	/**
	 * Update the notification after finishing writing file system
	 * */
	private void updateNotification(Bitmap bitmap) {
		Uri uri = saveToMediaStore(bitmap);
		mNotificationBuilder.setTicker(mContext.getText(R.string.saved));
		mNotificationBuilder.setContentText(mImageFileName);

		// Add share event
		Intent shareIntent = createShareIntent(uri);
		mNotificationBuilder.addAction(R.drawable.ic_menu_share,
				mContext.getString(R.string.share), PendingIntent.getActivity(mContext, 0,
						shareIntent, PendingIntent.FLAG_CANCEL_CURRENT));

		// Add TUYA event
		Intent tuyaIntent = createTuyaIntent(uri);
		if (mContext.getPackageManager()
				.queryIntentActivities(tuyaIntent, PackageManager.MATCH_DEFAULT_ONLY).size() != 0) {
			mNotificationBuilder.addAction(R.drawable.gn_icon_brush,
					mContext.getString(R.string.tuya), PendingIntent.getActivity(mContext, 0,
							tuyaIntent, PendingIntent.FLAG_CANCEL_CURRENT));
		}

		// Add content click event
		Intent launchIntent = new Intent(Intent.ACTION_VIEW);
		launchIntent.setDataAndType(uri, "image/png");
		launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mNotificationBuilder.setContentIntent(PendingIntent.getActivity(mContext, 0, launchIntent, 0));

		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float ratio = (float) ((width + 0.0) / (height + width));

		// Add 1 more pixel to avoid width or height equal 0
		Bitmap privView = ThumbnailUtils.extractThumbnail(bitmap,
				(int) (width * ratio) + 1, (int) (height * ratio) + 1,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

		if (privView != null)
			log("privView Bitmap:" + privView.getWidth() + " X " + privView.getHeight());

		BigPictureStyle style = new Notification.BigPictureStyle()
				.bigPicture(null);
		style.setSummaryText(mImageFileName);
		mNotificationBuilder.setStyle(style);
		mNotificationBuilder.setLargeIcon(privView);
		style.bigLargeIcon(null);
		style.bigPicture(privView);
		
		final long now = System.currentTimeMillis();
		mNotificationBuilder.setAutoCancel(true);
		mNotificationBuilder.setWhen(now);
		// Update the text in the public version as well
		Resources r = mContext.getResources();
        mPublicNotificationBuilder
            .setContentTitle(r.getString(R.string.screenshot_saved_title))
            .setContentText(r.getString(R.string.screenshot_saved_text))
            .setContentIntent(PendingIntent.getActivity(mContext, 0, launchIntent, 0))
            .setWhen(now)
            .setAutoCancel(true)
            .setColor(r.getColor(com.android.internal.R.color.system_notification_accent_color));

        mNotificationBuilder.setPublicVersion(mPublicNotificationBuilder.build());
		Notification notification = mNotificationBuilder.build();
		notification.flags &= ~Notification.FLAG_NO_CLEAR;
		mNotificationManager.notify(mNotificationId, notification);
	}

	/**
	 * Create share intent from URI
	 * 
	 * @param Uri
	 * @return Intent
	 */
	private Intent createShareIntent(final Uri uri) {
		Intent sharingIntent = new Intent(Intent.ACTION_SEND);
		sharingIntent.setType("image/png");
		sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
		sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "null");

		Intent chooserIntent = Intent.createChooser(sharingIntent, null);
		chooserIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		return chooserIntent;
	}

	/**
	 * Create TUYA intent from URI
	 * 
	 * @param Uri
	 * @return Intent
	 * */
	private Intent createTuyaIntent(final Uri uri) {
		Intent tuyaIntent = new Intent("action.picture.tool.tuya");
		tuyaIntent.setDataAndType(uri, "image/png");
		tuyaIntent.setDataAndType(uri, "image/jpeg");
		tuyaIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_CLEAR_TASK
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		return tuyaIntent;
	}

	Location mLocation = new Location();
	static final class Location {
		int startX;
		int startY;
		int width;
		int height;
		
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "StartX = "+startX+ " StartY = "+startY + "width = "+width+" height = "+height;
		}
	}
	
	final void log(Object... args) {
		if (args == null)
			return;
		for (Object obj : args) {
			if (obj != null) {
				Log.v(TAG, obj.toString());
			}
		}
	}

}
