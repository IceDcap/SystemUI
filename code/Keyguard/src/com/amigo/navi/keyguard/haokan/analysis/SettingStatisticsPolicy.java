package com.amigo.navi.keyguard.haokan.analysis;

public class SettingStatisticsPolicy {

	private static final int AUTO_UPDATE_OFF = 1;
	private static final int AUTO_UPDATE_ON  = 2;
	private static final int ONLY_WIFI_ON  = 1;
	private static final int ONLY_WIFI_OFF = 2;
	private static final int KEYGUARD_OFF = 1;
	private static final int KEYGUARD_ON  = 2;
	private static final int WALLPAPER_TEXT_OFF = 1;
	private static final int WALLPAPER_TEXT_ON  = 2;
	
	public static void onAutoUpdateChanged(boolean switcherOn) {
		int switcherState = switcherOn ? AUTO_UPDATE_ON : AUTO_UPDATE_OFF;
		HKAgent.onEventSwitcherStateChanged(Event.SETTING_AUTO_UPDATE, switcherState);
	}
	
	public static void onOnlyWifiChanged(boolean switcherOn) {
		int switcherState = switcherOn ? ONLY_WIFI_ON : ONLY_WIFI_OFF;
		HKAgent.onEventSwitcherStateChanged(Event.SETTING_ONLY_WIFI, switcherState);
	}
	
	public static void onKeyguardSwitcherChanged(boolean switcherOn) {
		int switcherState = switcherOn ? KEYGUARD_ON : KEYGUARD_OFF;
		HKAgent.onEventSwitcherStateChanged(Event.SETTING_KEYGUARD_SWITCHER, switcherState);
	}
	
	public static void onWallpaperTextChanged(boolean switcherOn) {
		int switcherState = switcherOn ? WALLPAPER_TEXT_ON : WALLPAPER_TEXT_OFF;
		HKAgent.onEventSwitcherStateChanged(Event.SETTING_IMAGE_TEXT, switcherState);
	}
	
}
