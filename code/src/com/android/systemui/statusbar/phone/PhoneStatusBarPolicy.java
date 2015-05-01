/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar.phone;

import android.app.AlarmManager;
import android.app.StatusBarManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.telecom.TelecomManager;
import android.util.Log;

import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.TelephonyIntents;
import com.android.systemui.R;
import com.android.systemui.statusbar.policy.CastController;
import com.android.systemui.statusbar.policy.CastController.CastDevice;
import com.android.systemui.statusbar.policy.HotspotController;
import android.database.ContentObserver;
import amigo.provider.AmigoSettings;
import android.nfc.NfcAdapter;

/**
 * This class contains all of the policy about which icons are installed in the status
 * bar at boot time.  It goes through the normal API for icons, even though it probably
 * strictly doesn't need to.
 */
public class PhoneStatusBarPolicy {
    private static final String TAG = "PhoneStatusBarPolicy";
    private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);

    private static final boolean SHOW_SYNC_ICON = false;

    private static final String SLOT_SYNC_ACTIVE = "sync_active";
    private static final String SLOT_CAST = "cast";
    private static final String SLOT_HOTSPOT = "hotspot";
    private static final String SLOT_BLUETOOTH = "bluetooth";
    private static final String SLOT_TTY = "tty";
    private static final String SLOT_ZEN = "zen";
    private static final String SLOT_VOLUME = "volume";
    private static final String SLOT_CDMA_ERI = "cdma_eri";
    private static final String SLOT_ALARM_CLOCK = "alarm_clock";
    private static final String GN_GUEST_MODE = "guest_mode";
    private static final String GN_SMART_SCREENON = "smart_bright_screen";
    private static final String GN_GLOVE_PATTERN = "glove_patterns";
    private static final String GN_DISTANCE_GESTURE = "distance_gesture";
    private static final String GN_VOICE_MODE = "voice_mode";
    private static final String GN_NFC = "nfc";

    private final Context mContext;
    private final StatusBarManager mService;
    private final Handler mHandler = new Handler();
    private final CastController mCast;
    private final HotspotController mHotspot;
    private NfcAdapter mNfcAdapter;

    // Assume it's all good unless we hear otherwise.  We don't always seem
    // to get broadcasts that it *is* there.
    IccCardConstants.State mSimState = IccCardConstants.State.READY;

    private boolean mZenVisible;
    //private boolean mVolumeVisible;

    private int mZen;

    private boolean mBluetoothEnabled = false;
    private int mic_with_flag = 0;
    private int mic_no_flag = 0;
    
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED)) {
                updateAlarm(intent);
            } else if (action.equals(Intent.ACTION_SYNC_STATE_CHANGED)) {
                updateSyncState(intent);
            } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED) ||
                    action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
                updateBluetooth();
            } else if (action.equals(AudioManager.RINGER_MODE_CHANGED_ACTION) ||
                    action.equals(AudioManager.INTERNAL_RINGER_MODE_CHANGED_ACTION)) {
                updateVolumeZen();
            } else if (action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)) {
                updateSimState(intent);
            } else if (action.equals(TelecomManager.ACTION_CURRENT_TTY_MODE_CHANGED)) {
                updateTTY(intent);
            } else if (action.equals(Intent.ACTION_USER_SWITCHED)) {
                updateAlarm(intent);
            } else if (action.equals(Intent.ACTION_ALARM_CHANGED)) {
				updateAlarm(intent);
			} else if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
				updateHeadSet(intent);
			} else if (action.equals(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)) {
				updateNfc(intent);
			}
        }
    };

    public PhoneStatusBarPolicy(Context context, CastController cast, HotspotController hotspot) {
        mContext = context;
        mCast = cast;
        mHotspot = hotspot;
        mService = (StatusBarManager)context.getSystemService(Context.STATUS_BAR_SERVICE);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(context);

        // listen for broadcasts
        IntentFilter filter = new IntentFilter();
        filter.addAction(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED);
        filter.addAction(Intent.ACTION_SYNC_STATE_CHANGED);
        filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        filter.addAction(AudioManager.INTERNAL_RINGER_MODE_CHANGED_ACTION);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        filter.addAction(TelecomManager.ACTION_CURRENT_TTY_MODE_CHANGED);
        filter.addAction(Intent.ACTION_USER_SWITCHED);
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        filter.addAction(Intent.ACTION_ALARM_CHANGED);
        if(mNfcAdapter != null) {
        	filter.addAction(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);
        }
        mContext.registerReceiver(mIntentReceiver, filter, null, mHandler);
        initObserver();

        // TTY status
        mService.setIcon(SLOT_TTY,  R.drawable.stat_sys_tty_mode, 0, null);
        mService.setIconVisibility(SLOT_TTY, false);

        // Cdma Roaming Indicator, ERI
        mService.setIcon(SLOT_CDMA_ERI, R.drawable.stat_sys_roaming_cdma_0, 0, null);
        mService.setIconVisibility(SLOT_CDMA_ERI, false);

        // bluetooth status
        updateBluetooth();

        // Alarm clock
        mService.setIcon(SLOT_ALARM_CLOCK, R.drawable.gn_stat_sys_alarm, 0, null);
        mService.setIconVisibility(SLOT_ALARM_CLOCK, false);

        // Sync state
        mService.setIcon(SLOT_SYNC_ACTIVE, R.drawable.stat_sys_sync, 0, null);
        mService.setIconVisibility(SLOT_SYNC_ACTIVE, false);
        // "sync_failing" is obsolete: b/1297963

        // zen
        mService.setIcon(SLOT_ZEN, R.drawable.gn_stat_sys_zen_important, 0, null);
        mService.setIconVisibility(SLOT_ZEN, false);

        /*// volume
        mService.setIcon(SLOT_VOLUME, R.drawable.stat_sys_ringer_vibrate, 0, null);
        mService.setIconVisibility(SLOT_VOLUME, false);
        updateVolumeZen();*/
        
        //nfc
		mService.setIcon(GN_NFC, R.drawable.gn_stat_sys_nfc, 0, null);
		mService.setIconVisibility(GN_NFC,isNfcEnabled());

        // guest mode
        updateGuestMode();
        updateSmartScreenOn();
        updateBluetooth();
        updateGDSwitch();
        updateGlovePattern();
        updateVoiceMode();
        updateVolumeZen();

        // cast
        mService.setIcon(SLOT_CAST, R.drawable.stat_sys_cast, 0, null);
        mService.setIconVisibility(SLOT_CAST, false);
        mCast.addCallback(mCastCallback);
    }

	private void initObserver() {
		mContext.getContentResolver().registerContentObserver(
                AmigoSettings.getUriFor(AmigoSettings.GUEST_MODE), false, mGuestModeChangeObserver);
        mContext.getContentResolver().registerContentObserver(
        		AmigoSettings.getUriFor("ssg_smart_light_screen"), false, mSmartScreenOnObserver);
        mContext.getContentResolver().registerContentObserver(
        		AmigoSettings.getUriFor(AmigoSettings.GN_SSG_SWITCH), false, mSSGSwitchObserver);
        mContext.getContentResolver().registerContentObserver(
        		AmigoSettings.getUriFor(AmigoSettings.GLOVE_PATTERNS), false, mGlovePatternObserver);
        mContext.getContentResolver().registerContentObserver(
        		AmigoSettings.getUriFor(AmigoSettings.GN_DG_SWITCH), false, mDistanceGestureObserver);
        mContext.getContentResolver().registerContentObserver(
        		AmigoSettings.getUriFor(AmigoSettings.SDG_DESKTOP_SLIDE), false, mSDGDesktopSlideObserver);
        mContext.getContentResolver().registerContentObserver(
        		AmigoSettings.getUriFor(AmigoSettings.SDG_UNLOCK), false, mSDGUnlockObserver);
        mContext.getContentResolver().registerContentObserver(
        		AmigoSettings.getUriFor(AmigoSettings.SDG_LIGHT_SCREEN), false, mSDGLightScreenObserver);
        mContext.getContentResolver().registerContentObserver(
        		AmigoSettings.getUriFor(AmigoSettings.SDG_BROWSE_PHOTOS_PSENSOR), false, mSDGBrowsePhonePsensorObserver);
        mContext.getContentResolver().registerContentObserver(
        		AmigoSettings.getUriFor(AmigoSettings.SDG_VIDEO_PAUSE_PSENSOR), false, mSDGVideoPauseObserver);
        mContext.getContentResolver().registerContentObserver(
        		AmigoSettings.getUriFor(AmigoSettings.SOUND_CONTROL_SWITCH), false, mVoiceModeObserver);
	}

    public void setZenMode(int zen) {
        mZen = zen;
        updateVolumeZen();
    }

    private void updateAlarm(Intent intent) {
        /*AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        boolean alarmSet = alarmManager.getNextAlarmClock(UserHandle.USER_CURRENT) != null;*/
    	boolean alarmSet = intent.getBooleanExtra("alarmSet", false);
        mService.setIconVisibility(SLOT_ALARM_CLOCK, alarmSet);
    }

    private final void updateSyncState(Intent intent) {
        if (!SHOW_SYNC_ICON) return;
        boolean isActive = intent.getBooleanExtra("active", false);
        mService.setIconVisibility(SLOT_SYNC_ACTIVE, isActive);
    }

    private final void updateSimState(Intent intent) {
        String stateExtra = intent.getStringExtra(IccCardConstants.INTENT_KEY_ICC_STATE);
        if (IccCardConstants.INTENT_VALUE_ICC_ABSENT.equals(stateExtra)) {
            mSimState = IccCardConstants.State.ABSENT;
        }
        else if (IccCardConstants.INTENT_VALUE_ICC_CARD_IO_ERROR.equals(stateExtra)) {
            mSimState = IccCardConstants.State.CARD_IO_ERROR;
        }
        else if (IccCardConstants.INTENT_VALUE_ICC_READY.equals(stateExtra)) {
            mSimState = IccCardConstants.State.READY;
        }
        else if (IccCardConstants.INTENT_VALUE_ICC_LOCKED.equals(stateExtra)) {
            final String lockedReason =
                    intent.getStringExtra(IccCardConstants.INTENT_KEY_LOCKED_REASON);
            if (IccCardConstants.INTENT_VALUE_LOCKED_ON_PIN.equals(lockedReason)) {
                mSimState = IccCardConstants.State.PIN_REQUIRED;
            }
            else if (IccCardConstants.INTENT_VALUE_LOCKED_ON_PUK.equals(lockedReason)) {
                mSimState = IccCardConstants.State.PUK_REQUIRED;
            }
            else {
                mSimState = IccCardConstants.State.NETWORK_LOCKED;
            }
        } else {
            mSimState = IccCardConstants.State.UNKNOWN;
        }
    }

    private final void updateVolumeZen() {
       // AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        boolean zenVisible = false;
        int zenIconId = 0;
        String zenDescription = null;

        /*boolean volumeVisible = false;
        int volumeIconId = 0;
        String volumeDescription = null;*/

        if (mZen == Global.ZEN_MODE_NO_INTERRUPTIONS) {
            zenVisible = true;
            zenIconId = R.drawable.stat_sys_zen_none;
            zenDescription = mContext.getString(R.string.zen_no_interruptions);
        } else if (mZen == Global.ZEN_MODE_IMPORTANT_INTERRUPTIONS) {
            zenVisible = true;
            zenIconId = R.drawable.gn_stat_sys_zen_important;
            zenDescription = mContext.getString(R.string.zen_important_interruptions);
        }

        /*if (mZen != Global.ZEN_MODE_NO_INTERRUPTIONS &&
                audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
            volumeVisible = true;
            volumeIconId = R.drawable.gn_stat_sys_ringer_vibrate;
            volumeDescription = mContext.getString(R.string.accessibility_ringer_vibrate);
        }*/

        if (zenVisible) {
            mService.setIcon(SLOT_ZEN, zenIconId, 0, zenDescription);
        }
        if (zenVisible != mZenVisible) {
            mService.setIconVisibility(SLOT_ZEN, zenVisible);
            mZenVisible = zenVisible;
        }

        /*if (volumeVisible) {
            mService.setIcon(SLOT_VOLUME, volumeIconId, 0, volumeDescription);
        }
        if (volumeVisible != mVolumeVisible) {
            mService.setIconVisibility(SLOT_VOLUME, volumeVisible);
            mVolumeVisible = volumeVisible;
        }*/
    }

    private final void updateBluetooth() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        int iconId = R.drawable.gn_stat_sys_data_bluetooth;
        String contentDescription =
                mContext.getString(R.string.accessibility_bluetooth_disconnected);
        if (adapter != null) {
            mBluetoothEnabled = (adapter.getState() == BluetoothAdapter.STATE_ON);
            if (adapter.getConnectionState() == BluetoothAdapter.STATE_CONNECTED) {
                iconId = R.drawable.gn_stat_sys_data_bluetooth_connected;
                contentDescription = mContext.getString(R.string.accessibility_bluetooth_connected);
            }
        } else {
            mBluetoothEnabled = false;
        }

        mService.setIcon(SLOT_BLUETOOTH, iconId, 0, contentDescription);
        mService.setIconVisibility(SLOT_BLUETOOTH, mBluetoothEnabled);
    }

    private final void updateTTY(Intent intent) {
        int currentTtyMode = intent.getIntExtra(TelecomManager.EXTRA_CURRENT_TTY_MODE,
                TelecomManager.TTY_MODE_OFF);
        boolean enabled = currentTtyMode != TelecomManager.TTY_MODE_OFF;

        if (DEBUG) Log.v(TAG, "updateTTY: enabled: " + enabled);

        if (enabled) {
            // TTY is on
            if (DEBUG) Log.v(TAG, "updateTTY: set TTY on");
            mService.setIcon(SLOT_TTY, R.drawable.stat_sys_tty_mode, 0,
                    mContext.getString(R.string.accessibility_tty_enabled));
            mService.setIconVisibility(SLOT_TTY, true);
        } else {
            // TTY is off
            if (DEBUG) Log.v(TAG, "updateTTY: set TTY off");
            mService.setIconVisibility(SLOT_TTY, false);
        }
    }

    private void updateCast() {
        boolean isCasting = false;
        for (CastDevice device : mCast.getCastDevices()) {
            if (device.state == CastDevice.STATE_CONNECTING
                    || device.state == CastDevice.STATE_CONNECTED) {
                isCasting = true;
                break;
            }
        }
        if (DEBUG) Log.v(TAG, "updateCast: isCasting: " + isCasting);
        if (isCasting) {
            mService.setIcon(SLOT_CAST, R.drawable.stat_sys_cast, 0,
                    mContext.getString(R.string.accessibility_casting));
        }
        mService.setIconVisibility(SLOT_CAST, isCasting);
    }

    private final HotspotController.Callback mHotspotCallback = new HotspotController.Callback() {
        @Override
        public void onHotspotChanged(boolean enabled) {
            mService.setIconVisibility(SLOT_HOTSPOT, enabled);
        }
    };

    private final CastController.Callback mCastCallback = new CastController.Callback() {
        @Override
        public void onCastDevicesChanged() {
            updateCast();
        }
    };
    //guest mode
    private ContentObserver mGuestModeChangeObserver = new ContentObserver(new Handler()){

        @Override
        public void onChange(boolean selfChange) {
            Log.d(TAG, "Guest mode changed!");
            updateGuestMode();
        }
    };
    
    private final void updateGuestMode() {
		int iconId = R.drawable.gn_stat_sys_guest_mode;
		mService.setIcon(GN_GUEST_MODE, iconId, 0, null);
		mService.setIconVisibility(GN_GUEST_MODE, isGuestModeEnabled());
	}
    
    
    private boolean isGuestModeEnabled() {
        int config = AmigoSettings.getInt(mContext.getContentResolver(), AmigoSettings.GUEST_MODE, 0);
        Log.d(TAG, "Load guest mode config " + config);
        return config == 1;
    }

    //smart screen on mode
    private ContentObserver mSmartScreenOnObserver =  new ContentObserver(new Handler()) {
    	
    	@Override
    	public void onChange(boolean selfChange) {
    		Log.d(TAG, " Smart screenOn mode changed!");
    		updateSmartScreenOn();
    	}
    };
    
    private final void updateSmartScreenOn() {
    	int iconId = R.drawable.gn_stat_sys_smart_screenon;
    	
    	mService.setIcon(GN_SMART_SCREENON, iconId, 0, null);
    	mService.setIconVisibility(GN_SMART_SCREENON, isSmartScreenOnEnabled() && isSSGSwitchEnabled());
    }
    
    private boolean isSmartScreenOnEnabled() {
    	int config = AmigoSettings.getInt(mContext.getContentResolver(), "ssg_smart_light_screen", 0);
    	Log.d(TAG, " Load smart screenon mode config " + config);
    	return config == 1;
	}
    
    private ContentObserver mSSGSwitchObserver = new ContentObserver(new Handler()) {

		@Override
		public void onChange(boolean selfChange) {
			Log.d(TAG, " SSGSwitch mode changed");
			updateSmartScreenOn();
		}
	};
	
	private boolean isSSGSwitchEnabled() {
		int config =  AmigoSettings.getInt(mContext.getContentResolver(), AmigoSettings.GN_SSG_SWITCH, 0);
		Log.d(TAG, "Load SSGSwitch config " + config);
		return config == 1;
	}
    
    //glove pattern
    private ContentObserver mGlovePatternObserver = new ContentObserver(new Handler()) {

		@Override
		public void onChange(boolean selfChange) {
			Log.d(TAG, " GlovePattern changed!");
			updateGlovePattern();
		}
	};
	
	private final void updateGlovePattern() {
		int iconId =  R.drawable.gn_stat_sys_glove;
		
		mService.setIcon(GN_GLOVE_PATTERN, iconId, 0, null);
		mService.setIconVisibility(GN_GLOVE_PATTERN, isGlovePatternEnabled());
	}
	
	private boolean isGlovePatternEnabled() {
		int config = AmigoSettings.getInt(mContext.getContentResolver(), AmigoSettings.GLOVE_PATTERNS, 0);
		Log.d(TAG, " Load glove pattern mode config " + config);
		return config == 1;
	}
	
	//gd switch
	private ContentObserver mDistanceGestureObserver = new ContentObserver(new Handler()) {

		@Override
		public void onChange(boolean selfChange) {
			Log.d(TAG, " Distance gesture mode changed!");
			updateGDSwitch();
		}
	};
	
	private ContentObserver mSDGDesktopSlideObserver = new ContentObserver(new Handler()) {

		@Override
		public void onChange(boolean selfChange) {
		updateGDSwitch();	
		}
	};
	
	private ContentObserver mSDGUnlockObserver = new ContentObserver(new Handler()) {

		@Override
		public void onChange(boolean selfChange) {
			updateGDSwitch();
		}
	};
	
	private ContentObserver mSDGLightScreenObserver = new ContentObserver(new Handler()) {

		@Override
		public void onChange(boolean selfChange) {
			updateGDSwitch();
		}
	};
	
	private ContentObserver mSDGBrowsePhonePsensorObserver = new ContentObserver(new Handler()) {

		@Override
		public void onChange(boolean selfChange) {
			updateGDSwitch();
		}
	};
	
	private ContentObserver mSDGVideoPauseObserver = new ContentObserver(new Handler()) {

		@Override
		public void onChange(boolean selfChange) {
			updateGDSwitch();
		}
	};
	
	private final void updateGDSwitch() {
		int iconId = R.drawable.gn_stat_sys_distance_gesture;
		
		mService.setIcon(GN_DISTANCE_GESTURE, iconId, 0, null);
		mService.setIconVisibility(GN_DISTANCE_GESTURE, isDistanceGestureEnabled());
	}
	
	private boolean isDistanceGestureEnabled() {
		int configDG = AmigoSettings.getInt(mContext.getContentResolver(), AmigoSettings.GN_DG_SWITCH, 0);
		int configSDGDesktopSlide = AmigoSettings.getInt(mContext.getContentResolver(), AmigoSettings.SDG_DESKTOP_SLIDE, 0);
		int configSDGUnlock = AmigoSettings.getInt(mContext.getContentResolver(), AmigoSettings.SDG_UNLOCK, 0);
		int configSDGLightScreen = AmigoSettings.getInt(mContext.getContentResolver(), AmigoSettings.SDG_LIGHT_SCREEN, 0);
		int configSDGBrowsePhotoPsensor = AmigoSettings.getInt(mContext.getContentResolver(), AmigoSettings.SDG_BROWSE_PHOTOS_PSENSOR, 0);
		int configSDGVideoPausePsensor = AmigoSettings.getInt(mContext.getContentResolver(), AmigoSettings.SDG_VIDEO_PAUSE_PSENSOR, 0);
		
		Log.d(TAG, " configDG = " + configDG
				+ " configSDGDesktopSlide = " + configSDGDesktopSlide
				+ " configSDGUnlock = " + configSDGUnlock
				+ " configSDGLightScreen = " + configSDGLightScreen
				+ " configSDGBrowsePhotoPsensor = " + configSDGBrowsePhotoPsensor
				+ " configSDGVideoPausePsensor = " + configSDGVideoPausePsensor);
		
		boolean isDGenabled = configDG == 1 && (configSDGDesktopSlide == 1
				|| configSDGUnlock == 1
				|| configSDGLightScreen == 1
				|| configSDGBrowsePhotoPsensor == 1
				|| configSDGVideoPausePsensor == 1);
		return isDGenabled;
	}
	
	//headset
	private final void updateHeadSet(Intent intent) {
        int state = intent.getIntExtra("state", -1);
        int mic = intent.getIntExtra("microphone", -1);
        Log.d(TAG, "updateHeadSet, state=" + state + ", mic=" + mic + ".");
        if (state == -1 || mic == -1) {
            return;
        }
        if (state == 1) {
            if (mic == 1) {
				mic_with_flag = 1;
                mService.setIcon("headset", R.drawable.gn_stat_sys_headset_with_mic, 0, null);
                mService.setIconVisibility("headset", true);
            } else {
            	mic_no_flag = 1;
                mService.setIcon("headset", R.drawable.gn_stat_sys_headset_without_mic, 0, null);
                mService.setIconVisibility("headset", true);
            }
        } else {
			//This path for Headsetphone update to Headset(10->01)
        	if((0==mic) && (1==mic_with_flag) && (1==mic_no_flag)) {
				mic_with_flag = 0;
				mic_no_flag = 0;
        	}else {//This path for Normal case
            	mService.setIconVisibility("headset", false);
				mic_with_flag = 0;
				mic_no_flag = 0;
        	}
        }
    }
	
	//voice mode
    private ContentObserver mVoiceModeObserver = new ContentObserver(new Handler()) {

		@Override
		public void onChange(boolean selfChange) {
			Log.d(TAG, " voice_mode changed!");
			updateVoiceMode();
		}
	};
	
	private final void updateVoiceMode() {
		int iconId =  R.drawable.gn_stat_sys_voice_mode;
		
		mService.setIcon(GN_VOICE_MODE, iconId, 0, null);
		mService.setIconVisibility(GN_VOICE_MODE, isVoiceModeEnabled());
	}
	
	private boolean isVoiceModeEnabled() {
		int config = AmigoSettings.getInt(mContext.getContentResolver(), AmigoSettings.SOUND_CONTROL_SWITCH, 0);
		Log.d(TAG, " Load voice mode config " + config);
		return config == 1;
	}
	
	//nfc
	private final void updateNfc(Intent intent) {
		int newStatus = intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE,
                NfcAdapter.STATE_OFF);
		switch (newStatus) {
        case NfcAdapter.STATE_OFF:
            mService.setIconVisibility(GN_NFC, false);                
            break;
        case NfcAdapter.STATE_ON:
            mService.setIconVisibility(GN_NFC, true);
            break;
    }
	}
	
	private boolean isNfcEnabled() {
		boolean isNfcEnabled = false;
		if(mNfcAdapter == null) {
			mNfcAdapter = NfcAdapter.getDefaultAdapter(mContext);
		} else {
			isNfcEnabled = mNfcAdapter.isEnabled();
		}
		return isNfcEnabled;
	}
}
