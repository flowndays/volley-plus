package com.rodrigo.harryportter.ui.view.pagecurlno;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;

import com.rodrigo.harryportter.ui.view.ReadViewProvider;
import com.rodrigo.harryportter.ui.view.TextPaginator;
import com.rodrigo.harryportter.util.StringUtil;

public class ReadNoAnimationProvider implements ReadViewProvider {
	public static final int PREVIOUS_CHAPTER = -1;
	public static final int NEXT_CHAPTER = 1;
	public static final int DRAW_FINISH = 1;
	public static final int DRAW_FAILED = 0;
	public static final int DRAW_BAD_CONTENT = 2;// 内容加载失败

	public static final short POSITION_HEAD = -1;
	public static final short POSITION_LASTREAD = 0;// 此常量会被替换成上次阅读的具体位置，所以POSITION_HEAD和POSITION_END不能定义为大于0的数值
	public static final short POSITION_END = -2;

	private Context ctx;
	private String mContent;
	private TextPaginator mPaginator;
	private GestureBitmapProvider mBitmapProvider;

	private ReadPagingView mView;

	public ReadNoAnimationProvider(Context context, ReadPagingView v, AttributeSet attrs, int defStyle) {
		ctx = context;
		mView = v;
		init();
	}

	public ReadNoAnimationProvider(Context context, ReadPagingView v, AttributeSet attrs) {
		ctx = context;
		mView = v;
		init();
	}

	public ReadNoAnimationProvider(Context context, ReadPagingView v) {
		ctx = context;
		mView = v;
		init();
	}

	private void init() {
		// mView.setCurrentIndex(mView.getCurrentIndex());
		// mView.setBackgroundColor(0xFF202830);
	}

	/**
	 * 装载数据，设置当前显示页码，但是不立刻显示。
	 * 
	 * @param content
	 * @param x
	 * @param y
	 * @param fontSize
	 * @param toCompress
	 * @param textColor
	 * @param bg
	 * @param position
	 * @param title
	 * @param titleLayout
	 * @param showTitle
	 * @param drawFinishHandler
	 */
	public boolean setupParams(String content, int x, int y, int fontSize, int textColor, int bg, int position,
			boolean autoFinish, final Handler drawFinishHandler) {
		if (content == null || content.length() == 0 || x <= 0 || y <= 0 || fontSize <= 0 || drawFinishHandler == null)
			return false;

		mContent = content;

		if (mPaginator == null)
			mPaginator = new TextPaginator(ctx, x, y, fontSize, textColor, bg);
		int result = mPaginator.load(mContent);

		if (mBitmapProvider == null) {
			mBitmapProvider = new GestureBitmapProvider();
			mView.setBitmapProvider(mBitmapProvider);
		}

		Message msg = null;

		if (result == -1) {// 是否加载成功
			if (autoFinish) {// 是否直接完成
				mView.setCurrentIndex(0);
				msg = Message.obtain(drawFinishHandler, StringUtil.isNullOrEmpty(content) ? DRAW_FAILED
						: DRAW_BAD_CONTENT, 1, -1);
			} else {// 不直接完成，开始翻页效果，并等待up事件完成翻页效果
				mView.curlToNewContents(0);
				msg = Message.obtain(drawFinishHandler, StringUtil.isNullOrEmpty(content) ? DRAW_FAILED
						: DRAW_BAD_CONTENT, -1, -1);
			}
		} else {
			msg = Message.obtain(drawFinishHandler, DRAW_FINISH, autoFinish ? 1 : -1, -1);

			if (autoFinish) {
				if (position == POSITION_HEAD)
					mView.setCurrentIndex(0);
				else if (position == POSITION_END)
					mView.setCurrentIndex(mPaginator.getPageNum());
				else
					mView.setCurrentIndex(mPaginator.getIndex(position));
			} else {
				if (mPaginator.getPageNum() == 0) {
					mView.curlToNewContents(0);
				} else {
					/*
					 * 在左边隐藏一个页面的时候，因为左边页面会在右边页面之后draw，导致index小了1，故需要加1
					 */
					if (position == POSITION_HEAD || StringUtil.isNullOrEmpty(content)) {
						mView.curlToNewContents(0);
					} else if (position == POSITION_END) {
						mView.curlToNewContents(mPaginator.getPageNum());
					} else {
						// 图片章节的y有用，文字章节的y无用；currentIndex比切出来的图片序号大1
						mView.curlToNewContents(mPaginator.getIndex(position));
					}
				}
			}
		}

		drawFinishHandler.sendMessage(msg);
		return true;
	}

	public int getCurrentIndex() {
		return mView.getCurrentIndex();
	}

	public void setCurlObserver(ReadViewProvider.CurlObserver observer) {
		mView.setCurlObserver(observer);
	}

	public void setTheme(int bg, int fontColor) {
		if (mPaginator != null) {
			mPaginator.setBg(bg);
			mPaginator.setTextColor(fontColor);
			mView.updatePages();
		}
	}

	public void changeFontSize(int newSize) {
		if (mPaginator != null) {
			mPaginator.setTextSize(newSize);
			mView.setCurrentIndex(mPaginator.getCurrentIndex());
		}
	}

	public void changeOrientation(int x, int y) {
		mPaginator.resize(x, y);
		int index = mPaginator.getCurrentIndex();
		mView.setCurrentIndex(index);
	}

	public int getPosition() {
		return mPaginator.getAnchor();
	}

	public float getPositionPercent() {
		if (mPaginator.getPageNum() == 1)
			return 1f;
		else
			return (float) (mView.getCurrentIndex()) / (float) (mPaginator.getPageNum() - 1);
	}

	class GestureBitmapProvider implements BitmapProvider {

		public Bitmap getBitmap(int width, int height, int index, boolean anchorCurrent) {
			if (width <= 0 || height <= 0 || mPaginator == null)
				return null;
			Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(b);

			if (index < 0) {
				return null;
			} else {
				mPaginator.draw(c, index);
			}

			if (!anchorCurrent) {// 把当前获取的页作为锚点，否则把之前页作为锚点
				index++;
				if (index > mPaginator.getPageNum())
					index = mPaginator.getPageNum();
			}
			mPaginator.setAnchor(index);
			return b;
		}

		/**
		 * Return number of pages/bitmaps available.
		 */
		public int getBitmapCount() {
			if (mPaginator == null)
				return -1;
			return mPaginator.getPageNum();
		}
	}

	public void setOnTouchObserver(OnTouchObserver onTouchObserver) {
		mView.setOnTouchObserver(onTouchObserver);
	}

	@Override
	public int getTotleSize() {
		return mContent.length();
	}

	@Override
	public void jumpTo(double percent) {
		// TODO Auto-generated method stub
		
	}
}
