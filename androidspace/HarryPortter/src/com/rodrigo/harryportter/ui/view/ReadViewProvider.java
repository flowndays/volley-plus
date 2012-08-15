package com.rodrigo.harryportter.ui.view;

import android.os.Handler;

public interface ReadViewProvider {
	public boolean setupParams(String content, int x, int y, int fontSize, int textColor, int bg, int position,
			boolean autoFinish, final Handler drawFinishHandler);

	public void setTheme(int bg, int fontColor);

	public void changeFontSize(int newSize);

	public void changeOrientation(int x, int y);

	public int getPosition();

	public float getPositionPercent();

	public int getTotleSize();

	public void setOnTouchObserver(OnTouchObserver onTouchObserver);

	public void setCurlObserver(CurlObserver curlObserver);
	
	public void jumpTo(double percent);
	
	/**
	 * Observer Interface for page curling
	 * 
	 * @author TangCan
	 * 
	 */
	public interface CurlObserver {
		/**
		 * Called before curling
		 * 
		 * @param currentIndex
		 *            page index before curling
		 * @param isToNext
		 *            is curling to next page
		 */
		void preCurl(int currentIndex, boolean isToNext);

		/**
		 * Called after curling
		 * 
		 * @param currentIndex
		 *            page index after curling
		 */
		void afterCurl(int currentIndex);
	}

	public interface OnTouchObserver {
		void centerClicked();

		boolean firstPagePrevious();

		boolean lastPageNext();
	}
}
