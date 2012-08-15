package com.rodrigo.harryportter.ui.view.pagecurlno;

import com.rodrigo.harryportter.ui.view.ReadViewProvider.CurlObserver;
import com.rodrigo.harryportter.ui.view.ReadViewProvider.OnTouchObserver;

public interface ReadPagingView {
	void setBackgroundColor(int i);

	int getCurrentIndex();

	void setOnTouchObserver(OnTouchObserver onTouchObserver);

	void setCurrentIndex(int currentIndex);

	boolean curlToNewContents(int i);

	void updatePages();

	void setBitmapProvider(BitmapProvider bitmapProvider);

	void setCurlObserver(CurlObserver curlObserver);

	/**
	 * Observer interface for handling CurlView size changes.
	 */
	public interface SizeChangedObserver {

		/**
		 * Called once CurlView size changes.
		 */
		public void onSizeChanged(int width, int height);
	}
}
