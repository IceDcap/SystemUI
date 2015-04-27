/****************************************************************************
 * Filename:
 * ---------
 *  GNAudioManagerHelper.java
 *
 * Project:
 * --------
 *   shortcuttools
 *
 * Description:
 * ------------
 *  manage Phone Ring 
 *
 * Author:
 * -------
 *  2013.08.24 caody@gionee.com 
 *
 ****************************************************************************/
package com.android.systemui.gionee.cc.fakecall;

//Gionee <caody><2013-10-24> modify for CR00928733 begin
import android.media.AudioManager;

public class GnAudioManagerHelper {
    /**
     * get Ring volume
     * 
     * @param audio
     * @return
     */
    public int getInitVolume(AudioManager audio) {
        int volume = audio.getStreamVolume(AudioManager.STREAM_RING);
        return volume;
    }

    /**
     * set Ring volume
     * 
     * @param audio
     * @return
     */
    public void setVolume(AudioManager audio, int volume) {
        audio.setStreamVolume(AudioManager.STREAM_RING, volume, 0);
    }

    /**
     * get Ring type
     * 
     * @param audio
     * @return
     */

    public int getType(AudioManager audio) {
        return audio.getRingerMode();
    }

    /**
     * set Ring type
     * 
     * @param audio
     * @param type
     */

    public void setType(AudioManager audio, int type) {
        audio.setMode(type);
    }

    /**
     * set Ring type :Ring mode
     * 
     * @param audio
     */
    void ring(AudioManager audio) {
        audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
                AudioManager.VIBRATE_SETTING_OFF);
        audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION,
                AudioManager.VIBRATE_SETTING_OFF);
    }

    /**
     * set Ring type :Ring and vibrate mode
     * 
     * @param audio
     */
    void ringAndVibrate(AudioManager audio) {
        audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
                AudioManager.VIBRATE_SETTING_ON);
        audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION,
                AudioManager.VIBRATE_SETTING_ON);
    }

    /**
     * set Ring type : vibrate mode
     * 
     * @param audio
     */
    public void vibrate(AudioManager audio) {
        audio.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
                AudioManager.VIBRATE_SETTING_ON);
        audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION,
                AudioManager.VIBRATE_SETTING_ON);
    }

    /**
     * set Ring type : silent mode
     * 
     * @param audio
     */
    void noRingAndVibrate(AudioManager audio) {
        audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
                AudioManager.VIBRATE_SETTING_OFF);
        audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION,
                AudioManager.VIBRATE_SETTING_OFF);
    }

    /**
     * addVolume ++
     * 
     * @param audio
     */
    public void addVolume(AudioManager audio) {
        audio.adjustVolume(AudioManager.ADJUST_RAISE, 0);
    }

    /**
     * releaseVolume --
     * 
     * @param audio
     */

    public void releaseVolume(AudioManager audio) {
        audio.adjustVolume(AudioManager.ADJUST_LOWER, 0);
    }

}
// Gionee <caody><2013-10-24> modify for CR00928733 end
