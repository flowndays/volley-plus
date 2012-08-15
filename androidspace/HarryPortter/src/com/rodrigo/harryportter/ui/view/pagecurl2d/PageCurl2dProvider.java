package com.rodrigo.harryportter.ui.view.pagecurl2d;

import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;

import com.rodrigo.harryportter.ui.view.ReadViewProvider;
import com.rodrigo.harryportter.ui.view.pagecurl2d.PageCurl2dView.BitmapProvider;
import com.rodrigo.harryportter.util.LayoutUtil;

public class PageCurl2dProvider implements ReadViewProvider {
	/** Called when the activity is first created. */
	private PageCurl2dView mPageView;
	Bitmap mCurPageBitmap, mNextPageBitmap;
	Canvas mCurPageCanvas, mNextPageCanvas;
	BookPageFactory pagefactory;
	Context mCtx;
	OnTouchObserver mOnTouchObserver;
	CurlObserver mCurlObserver;

	public PageCurl2dProvider(Context ctx, PageCurl2dView view) {
		mCtx = ctx;
		mPageView = view;
	}

	@Override
	public void jumpTo(double percent) {
		pagefactory.jumpTo(percent);
		update();
	}

	@Override
	public boolean setupParams(String content, int x, int y, int fontSize, int textColor, int bg, int position,
			boolean autoFinish, Handler drawFinishHandler) {
		mCurPageBitmap = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
		mNextPageBitmap = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);

		mCurPageCanvas = new Canvas(mCurPageBitmap);
		mNextPageCanvas = new Canvas(mNextPageBitmap);
		pagefactory = new BookPageFactory(x, y, bg, textColor, LayoutUtil.GetPixelBySP(mCtx, fontSize));

		try {
			pagefactory.openbook(content, position);
			pagefactory.draw(mCurPageCanvas);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}

		mPageView.setBitmaps(mCurPageBitmap, mCurPageBitmap);
		mPageView.setBitmapProvider(new BitmapProvider() {

			@Override
			public Bitmap getPrevious() {
				if (pagefactory.isfirstPage())
					return null;
				try {
					pagefactory.prePage();
				} catch (IOException e1) {
					e1.printStackTrace();
					return null;
				}
				pagefactory.draw(mNextPageCanvas);
				return mNextPageBitmap;
			}

			@Override
			public Bitmap getNext() {
				if (pagefactory.islastPage())
					return null;
				try {
					pagefactory.nextPage();
				} catch (IOException e1) {
					e1.printStackTrace();
					return null;
				}
				pagefactory.draw(mNextPageCanvas);
				return mNextPageBitmap;
			}

			@Override
			public Bitmap getCurrent() {
				pagefactory.draw(mCurPageCanvas);
				return mCurPageBitmap;
			}
		});

		// mPageView.setOnTouchListener(new OnTouchListener() {
		// @Override
		// public boolean onTouch(View v, MotionEvent e) {
		// // TODO Auto-generated method stub
		//
		// boolean ret = false;
		// if (v == mPageView) {
		// if (e.getAction() == MotionEvent.ACTION_DOWN) {
		// mPageView.abortAnimation();
		// mPageView.calcCornerXY(e.getX(), e.getY());
		//
		// pagefactory.draw(mCurPageCanvas);
		// if (mPageView.DragToRight()) {
		// try {
		// pagefactory.prePage();
		// } catch (IOException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }
		// pagefactory.draw(mNextPageCanvas);
		// if (pagefactory.isfirstPage())
		// return false;
		// } else {
		// try {
		// pagefactory.nextPage();
		// } catch (IOException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }
		// pagefactory.draw(mNextPageCanvas);
		// if (pagefactory.islastPage())
		// return false;
		// }
		// mPageView.setBitmaps(mCurPageBitmap, mNextPageBitmap);
		// } else if (mPageView.DragToRight() && pagefactory.isfirstPage()
		// || (!mPageView.DragToRight() && pagefactory.islastPage())) {
		// return false;
		// } else {
		// ret = mPageView.doTouchEvent(e);
		// mCurlObserver.afterCurl(pagefactory.getPosition());
		// return ret;
		// }
		// }
		// return false;
		// }
		//
		// });
		return true;
	}

	private void update() {
		pagefactory.resetCurrent();
		pagefactory.draw(mCurPageCanvas);
		pagefactory.draw(mNextPageCanvas);
		mPageView.setBitmaps(mCurPageBitmap, mNextPageBitmap);
		mPageView.invalidate();
	}

	@Override
	public void setTheme(int bgColor, int textColor) {
		pagefactory.setUpColor(bgColor, textColor);
		update();
	}

	@Override
	public void changeFontSize(int newSize) {
		pagefactory.setTextSize(LayoutUtil.GetPixelBySP(mCtx, newSize));
		update();
	}

	@Override
	public void changeOrientation(int x, int y) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getPosition() {
		return pagefactory.getPosition();
	}

	@Override
	public float getPositionPercent() {
		return pagefactory.getPositionPercent();
	}

	@Override
	public int getTotleSize() {
		return pagefactory.getTotalSize();
	}

	@Override
	public void setOnTouchObserver(OnTouchObserver onTouchObserver) {
		mOnTouchObserver = onTouchObserver;
	}

	@Override
	public void setCurlObserver(CurlObserver curlObserver) {
		if (mPageView != null)
			mPageView.setCurlObserver(curlObserver);
	}
}