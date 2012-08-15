package com.rodrigo.harryportter.ui.view.pagecurlno;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.rodrigo.harryportter.ui.view.ReadViewProvider.CurlObserver;
import com.rodrigo.harryportter.ui.view.ReadViewProvider.OnTouchObserver;
import com.rodrigo.harryportter.util.ReadActionUtil;

public class ReadNoAnimationView extends View implements ReadPagingView {
	private int mPageBitmapWidth = -1;
	private int mPageBitmapHeight = -1;

	private BitmapProvider mBitmapProvider;
	private SizeChangedObserver mSizeChangedObserver;
	protected OnTouchObserver mTouchObserver;

	protected CurlObserver mCurlObserver;
	private int mCurrentIndex = 0;

	private int mBackgroundColor;

	private static final short CURL_DIRECTION_PREVIOUS = -1;
	private static final short CURL_DIRECTION_NEXT = 1;
	private static final short CURL_DIRECTION_NONE = 0;
	private short mCurlDirection = CURL_DIRECTION_NONE;

	/**
	 * Default constructor.
	 */
	public ReadNoAnimationView(Context ctx) {
		super(ctx);
	}

	/**
	 * Default constructor.
	 */
	public ReadNoAnimationView(Context ctx, AttributeSet attrs, int defStyle) {
		super(ctx, attrs, defStyle);
	}

	/**
	 * Default constructor.
	 */
	public ReadNoAnimationView(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mBitmapProvider == null)
			return;
		if (mPageBitmapWidth <= 0) {
			mPageBitmapWidth = canvas.getWidth();
			mPageBitmapHeight = canvas.getHeight();
		}
		Bitmap img = mBitmapProvider.getBitmap(mPageBitmapWidth, mPageBitmapHeight, mCurrentIndex, true);
		canvas.drawBitmap(img, 0, 0, null);
		super.onDraw(canvas);
	}

	@Override
	public void setBackgroundColor(int color) {
		mBackgroundColor = color;
	}

	@Override
	public int getCurrentIndex() {
		return mCurrentIndex;
	}

	@Override
	public void setOnTouchObserver(OnTouchObserver onTouchObserver) {
		mTouchObserver = onTouchObserver;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		guesture.onTouchEvent(event);
		if (event.getAction() == MotionEvent.ACTION_UP) {
			switch (mCurlDirection) {
			case CURL_DIRECTION_NEXT:
				pageNext();
				break;
			case CURL_DIRECTION_PREVIOUS:
				pagePrevious();
			default:
				break;
			}
			mCurlDirection = CURL_DIRECTION_NONE;
		}

		return super.onTouchEvent(event);
	}

	GestureDetector guesture = new GestureDetector(new GestureDetector.OnGestureListener() {

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			if (ReadActionUtil.isPressMiddle(e, mPageBitmapWidth, mPageBitmapHeight)) {
				mTouchObserver.centerClicked();
				return true;
			} else if (ReadActionUtil.isPressPreious(e, mPageBitmapWidth, mPageBitmapHeight)) {
				pagePrevious();
				return true;
			} else {
				pageNext();
				return true;
			}
		}

		@Override
		public void onShowPress(MotionEvent e) {
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			if (distanceX > 10) {
				mCurlDirection = CURL_DIRECTION_NEXT;
			} else if (distanceX < -10) {
				mCurlDirection = CURL_DIRECTION_PREVIOUS;
			}
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			return false;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return false;
		}
	});

	private void pagePrevious() {
		// 翻页前
		preCurl(mCurrentIndex, false);

		if (mCurrentIndex == 0)
			mTouchObserver.firstPagePrevious();
		else {
			mCurrentIndex--;
			invalidate();
			afterCurl(mCurrentIndex);
		}
	}

	private void pageNext() {
		// 翻页前
		preCurl(mCurrentIndex, true);

		if (mCurrentIndex == mBitmapProvider.getBitmapCount() - 1)
			mTouchObserver.lastPageNext();
		else {
			mCurrentIndex++;
			invalidate();
			afterCurl(mCurrentIndex);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			pageNext();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			pagePrevious();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	protected void preCurl(int currentIndex, boolean isToNext) {
		if (mCurlObserver != null) {
			mCurlObserver.preCurl(currentIndex, isToNext);
		}
	}

	public void afterCurl(int currentIndex) {
		if (mCurlObserver != null) {
			mCurlObserver.afterCurl(currentIndex);
		}
	}

	@Override
	public void setCurrentIndex(int currentIndex) {
		mCurrentIndex = currentIndex < 0 ? 0 : currentIndex;
		invalidate();
	}

	@Override
	public boolean curlToNewContents(int index) {
		mCurrentIndex = index;
		invalidate();
		return true;
	}

	@Override
	public void updatePages() {
		invalidate();
	}

	@Override
	public void setBitmapProvider(BitmapProvider bitmapProvider) {
		mBitmapProvider = bitmapProvider;
		mCurrentIndex = 0;
		updateBitmap();
	}

	private void updateBitmap() {
		if (mBitmapProvider == null) {
			return;
		}
		invalidate();
	}

	@Override
	public void setCurlObserver(CurlObserver curlObserver) {
		mCurlObserver = curlObserver;
	}
}
