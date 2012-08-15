package com.rodrigo.harryportter.ui.view.pagecurlno;

import android.graphics.Bitmap;

/**
 * Provider for feeding 'book' with bitmaps which are used for rendering pages.
 */
public interface BitmapProvider {

	/**
	 * Called once new bitmap is needed. Width and height are in pixels telling
	 * the size it will be drawn on screen and following them ensures that
	 * aspect ratio remains. But it's possible to return bitmap of any size
	 * though.<br/>
	 * <br/>
	 * Index is a number between 0 and getBitmapCount() - 1. chapterIndex:
	 */
	public Bitmap getBitmap(int width, int height, int index, boolean anchorCurrent);

	/**
	 * Return number of pages/bitmaps available.
	 */
	public int getBitmapCount();
}
