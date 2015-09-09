package com.amigo.navi.keyguard.infozone;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;

import com.android.keyguard.R;

/**
 * @author chenng Utility class, all methods that class-independent
 */
public final class Utils {

	public static String getWeekdayString(Context ctx, int weekDay) {
		switch (weekDay) {
		case 0:
			return ctx.getString(R.string.sunday);
		case 1:
			return ctx.getString(R.string.monday);
		case 2:
			return ctx.getString(R.string.tuesday);
		case 3:
			return ctx.getString(R.string.wednesday);
		case 4:
			return ctx.getString(R.string.thursday);
		case 5:
			return ctx.getString(R.string.friday);
		case 6:
			return ctx.getString(R.string.saturday);
		default:
			return ctx.getString(R.string.sunday);
		}
	}

	public static String getWeekString_US(Context ctx, int week) {
		switch (week) {
		case 1:
			return ctx.getString(R.string.sunday_us);
		case 2:
			return ctx.getString(R.string.monday_us);
		case 3:
			return ctx.getString(R.string.tuesday_us);
		case 4:
			return ctx.getString(R.string.wednesday_us);
		case 5:
			return ctx.getString(R.string.thursday_us);
		case 6:
			return ctx.getString(R.string.friday_us);
		case 7:
			return ctx.getString(R.string.saturday_us);
		default:
			return ctx.getString(R.string.sunday_us);
		}
	}

	public static String getMonthString_US(Context ctx, int month) {
		switch (month) {
		case 0:
			return ctx.getString(R.string.January_us);
		case 1:
			return ctx.getString(R.string.February_us);
		case 2:
			return ctx.getString(R.string.March_us);
		case 3:
			return ctx.getString(R.string.April_us);
		case 4:
			return ctx.getString(R.string.May_us);
		case 5:
			return ctx.getString(R.string.June_us);
		case 6:
			return ctx.getString(R.string.July_us);
		case 7:
			return ctx.getString(R.string.August_us);
		case 8:
			return ctx.getString(R.string.September_us);
		case 9:
			return ctx.getString(R.string.October_us);
		case 10:
			return ctx.getString(R.string.November_us);
		case 11:
			return ctx.getString(R.string.December_us);
		default:
			return ctx.getString(R.string.January_us);
		}
	}

	public static byte[] readStream(InputStream inputStream) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = inputStream.read(buffer)) != -1) {
				baos.write(buffer, 0, len);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (baos != null) {
					baos.close();
				}
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return baos.toByteArray();
	}

	public static long parseStrToTime(String formatTime) {
		Date date = null;
		try {
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			date = formatter.parse(formatTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (date == null) {
			return 0;
		} else {
			return date.getTime();
		}
	}
	
}
