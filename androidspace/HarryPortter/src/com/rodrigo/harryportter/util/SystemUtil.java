package com.rodrigo.harryportter.util;

import android.content.ContentResolver;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

public class SystemUtil {
	/**
	 * 判断是否开启了自动亮度调节
	 */
	public static boolean isAutoBrightness(ContentResolver aContentResolver) {
		boolean automicBrightness = false;
		try {
			/*
			 * 本应按下面的方式写，但是api版本过低时不识别，所以直接写成硬编码 automicBrightness =
			 * Settings.System.getInt(aContentResolver,
			 * Settings.System.SCREEN_BRIGHTNESS_MODE) ==
			 * Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
			 */
			automicBrightness = Settings.System.getInt(aContentResolver, "screen_brightness_mode") == 1;
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		return automicBrightness;
	}

	/**
	 * 开启自动亮度调节
	 */
	public static void enableAutoBrightness(ContentResolver aContentResolver) {
		Settings.System.putInt(aContentResolver, "screen_brightness_mode", 1);
	}

	/**
	 * 关闭自动亮度调节
	 */
	public static void disableAutoBrightness(ContentResolver aContentResolver) {
		Settings.System.putInt(aContentResolver, "screen_brightness_mode", 0);
	}
}
