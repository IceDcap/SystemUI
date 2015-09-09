/*
 *
 * MODULE DESCRIPTION
 * add by huangwt for Android L at 20141210.
 * 
 */

package com.android.systemui.gionee.cc.fakecall;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.android.systemui.R;

public class GnFakeCall {
    
    private static final String LOG_TAG = "GnFakeCall";

    private static Context mContext;
    private static Uri mUri;
    private static int mRingTonePlayTime;
    private static int mVoicePlayTime;
    private static int mMusicID;
    private static String mMusicFile;
    private static boolean isVibrate;
    private static boolean isPlayLoop = true;
    private static SharedPreferences mPref = null;
    private static final String RINGTONE_MANAGER_CLASS_NAME = "android.media.RingtoneManager";
    private static final String METHOD_GET_DEFAULT_RINGTONE_URI = "getDefaultRingtoneUri";

    public GnFakeCall(Context context) {
        mContext = context;
        mRingTonePlayTime = 45;
        mMusicID = 0;
    }

    public Intent createFakeCallIntent() {
        int index = getPosition();
        Log.d(LOG_TAG, "createFakeCallPendingIntent()  index:" + index);
        mUri = getRingtone();
        if (mUri == null) {
            mUri = android.provider.Settings.System.DEFAULT_RINGTONE_URI;
        }

        Bundle bundle = new Bundle();
        bundle.clear();
        bundle.putString("Name", getName(index));
        bundle.putString("Number", "");
        bundle.putString("RingUri", mUri.toString());
        bundle.putInt("RingTonePlayTime", mRingTonePlayTime);
        bundle.putInt("MusicID", mMusicID);
        bundle.putString("MusicFile", mMusicFile);
        bundle.putBoolean("Vibrate", isVibrate);
        bundle.putBoolean("PlayLoop", isPlayLoop);
        bundle.putInt("VoicePlayTime", mVoicePlayTime);

        Intent intent = new Intent(mContext, GnFakeCallActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtras(bundle);
        return intent;
    }

    private static Uri getRingtone() {
		Uri acturlUri = RingtoneManager.getActualDefaultRingtoneUri(mContext,
				RingtoneManager.TYPE_RINGTONE);

		if (isRingtoneExist(acturlUri)) {
			return acturlUri;
		} else {
			// modify by hanjuan for CR01532480 begin
			// Uri defaultUri = RingtoneManager.getDefaultRingtoneUri(mContext,
			// RingtoneManager.TYPE_RINGTONE);
			try {
				RingtoneManager manager = new RingtoneManager(mContext);
				Class c = Class.forName(RINGTONE_MANAGER_CLASS_NAME);
				Method method = c.getMethod(METHOD_GET_DEFAULT_RINGTONE_URI,
						new Class[] { Context.class, int.class });
				Uri uri = (Uri) method.invoke(manager, mContext,
						RingtoneManager.TYPE_RINGTONE);
				Log.i(LOG_TAG, "Get actual default ringtone uri= " + uri);
				return uri;

			} catch (Exception e) {
				e.printStackTrace();
				String uriString = Settings.System.getString(
						mContext.getContentResolver(),
						"mtk_audioprofile_default_ringtone");
				Log.i(LOG_TAG, "Exception: Get actual default ringtone uri= " + uriString);
				return uriString != null ? Uri.parse(uriString) : null;
			}
			// modify by hanjuan for CR01532480 end
		}
    }

    private static boolean isRingtoneExist(Uri uri) {
        if (uri == null) {
            return false;
        }

        try {
            AssetFileDescriptor fd = mContext.getContentResolver()
                    .openAssetFileDescriptor(uri, "r");
            if (fd == null) {
                return false;
            } else {
                try {
                    fd.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static String getName(int index) {
        String name = "";
        String[] array = mContext.getResources().getStringArray(R.array.gn_fc_caller);
        mMusicID = index;
        if (index < 0 || index > array.length - 1) {
            return name;
        }
        return array[index];
    }

    public String getContent(int index) {
        String content = getName(index) + ":" + "\n" + "\n";
        String[] array = mContext.getResources().getStringArray(R.array.content);
        return content + array[index];
    }

    public static int getPosition() {
        if (mPref == null) {
            mPref = mContext.getSharedPreferences("phoneSetting", Context.MODE_WORLD_READABLE
                    | Context.MODE_WORLD_WRITEABLE);
        }

        return mPref.getInt("position", 0);
    }

}
