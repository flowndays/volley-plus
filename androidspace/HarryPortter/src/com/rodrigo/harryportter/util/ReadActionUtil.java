package com.rodrigo.harryportter.util;

import android.view.MotionEvent;

public class ReadActionUtil {

	public static boolean isPressMiddle(MotionEvent e, int width, int height) {
		float xUp = e.getX();
		float yUp = e.getY();

		return xUp > width / 3 && xUp < width * 2 / 3 && yUp > height / 3 && yUp < 2 * height / 3;
	}

	public static boolean isPressPreious(MotionEvent e, int width, int height) {
		float xUp = e.getX();
		float yUp = e.getY();

		return (xUp < width * 2 / 3 && yUp < height / 3) || (xUp < width / 3 && yUp > height / 3);
	}

	public static boolean isPressNext(MotionEvent e, int width, int height) {
		float xUp = e.getX();
		float yUp = e.getY();

		return (xUp > width / 3 && yUp > height * 2 / 3) || (xUp > width * 2 / 3 && yUp < height * 2 / 3);
	}
}
