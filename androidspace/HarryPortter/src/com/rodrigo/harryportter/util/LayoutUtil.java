package com.rodrigo.harryportter.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

/**
 * Created by IntelliJ IDEA. User: AaronYi Date: 11-8-3 Time: 3:43 To change
 * this template use File | Settings | File Templates.
 */
public final class LayoutUtil {

	/**
	 * Return real pixels represented by DIP
	 * 
	 * @param context
	 * @param dp
	 * @return real pixels represented by DIP
	 */
	public static int GetPixelByDIP(Context context, int dp) {
		return (int) (context.getResources().getDisplayMetrics().density * dp + 0.5f);
	}

	public static int GetPixelBySP(Context context, int sp) {
		return (int) (context.getResources().getDisplayMetrics().scaledDensity * sp);
	}

	public static void lockScreenRotation(Activity activity) {
		// Stop the screen orientation changing during an event
		switch (activity.getResources().getConfiguration().orientation) {
		case Configuration.ORIENTATION_PORTRAIT:
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			break;
		case Configuration.ORIENTATION_LANDSCAPE:
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			break;
		}
	}

	public static void unLockScreenRotation(Activity activity) {
		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	}
}
