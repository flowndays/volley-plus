package com.rodrigo.harryportter.ui.view;

import java.util.ArrayList;
import java.util.List;

import com.rodrigo.harryportter.util.LayoutUtil;
import com.rodrigo.harryportter.util.StringUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;

public class TextPaginator {

	private Paint mPaint;
	private int mLeftPadding, mTopPadding, mRightPadding, mBottomPadding; // 上下左右的间隔
	// private int rowSpacing; // 行距 ，1/2的字体大小mSp/2
	private int mVisibleWidth;
	private int lineNums;
	List<String> strLines = new ArrayList<String>(); // 存放切好的每一行文字
	String[] paragraphLines; // 存放一个个段落
	private int fontPosition = 0;
	private int pageIndex = -1; // 当前页所在页码，默认为-1
	private int fontSize;
	private float singleTextWidth;
	private int mTextColor = 0xff000000;
	private int mBgColor = 0xffffffff;
	private int mWidth;
	private int mHeight;
	private Context mContext;
	/*
	 * 行距
	 */
	float mLineGapScale = 0.4f;

	public TextPaginator(Context context, int width, int height, int sp, int textColor, int bg) {
		mContext = context;
		// mBgColor = bg;
		mWidth = width;
		mHeight = height;

		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setTextAlign(Align.LEFT);
		fontSize = LayoutUtil.GetPixelBySP(context, sp);
		mLeftPadding = mTopPadding = mRightPadding = mBottomPadding = LayoutUtil.GetPixelBySP(context, 10);
		mPaint.setTextSize(fontSize);
		mPaint.setColor(mTextColor);
		init();
	}

	public int load(String contents) { // 将文字切成一行行,,参数改变是 重新load
		// TODO Auto-generated method stub
		initContents();
		if (StringUtil.isNullOrEmpty(contents))
			return -1;
		paragraphLines = contents.split("\n");
		if (paragraphLines == null)
			return -1;
		return cutText();
	}

	private void initContents() {
		strLines.clear();
		paragraphLines = new String[0];
	}

	private int cutText() {
		strLines.clear();
		singleTextWidth = mPaint.measureText("书");
		int paragraphLength = paragraphLines.length;
		for (int i = 0; i < paragraphLength; i++) {
			if (paragraphLines[i] == null)
				return strLines.size();
			String content = paragraphLines[i].replace("\t\t\t", "        "); // \t可能会变成方框
			while (content.length() > 0) {
				int length = mPaint.breakText(content, true, mVisibleWidth, null);
				strLines.add(content.substring(0, length));
				content = content.substring(length);
			}
		}
		return strLines.size();
	}

	private void init() {
		mVisibleWidth = mWidth - mLeftPadding - mRightPadding;
		lineNums = (int) ((mHeight - mTopPadding - mBottomPadding + fontSize * mLineGapScale) / (fontSize + fontSize
				* mLineGapScale));
	}

	private void countPageIndex() {
		init();
		int lines = cutText();
		int length = 0;
		for (int i = 0; i < lines; i++) {
			length += strLines.get(i).length();
			if (length > fontPosition) {
				pageIndex = i / lineNums;
				return;
			}
		}
	}

	public void setTextSize(int sp) {
		// TODO Auto-generated method stub
		fontSize = LayoutUtil.GetPixelBySP(mContext, sp);
		mPaint.setTextSize(fontSize);
		countPageIndex();
	}

	public void draw(Canvas canvas, int index) {
		// TODO Auto-generated method stub
		int strLength = strLines.size();
		int start = index * lineNums;
		int end = start + lineNums;
		if (start > strLength)
			return;
		int xx = mLeftPadding;
		int yy = mTopPadding + fontSize; // 画笔是从字的底部作为baseline画的,需要加上字的高度
		drawBg(canvas);

		if (end > strLength)
			end = strLength;
		for (int i = start; i < end; i++) {
			String lineString = strLines.get(i);
			int lineLength = lineString.length();
			float jj = mVisibleWidth - mPaint.measureText(lineString); // 一行多余的像素
			if (jj == 0 || jj > singleTextWidth) {
				canvas.drawText(lineString, xx, yy, mPaint); // 如果正好或者多余的像素超出一个字符，则直接画上这一行
			} else {
				int addt = (int) (Math.ceil(jj / lineString.length())); // 每个字符多几个像素
				int a = (int) Math.floor(jj / addt); // 多余的像素加在几个字符上
				canvas.drawText(lineString.substring(0, lineLength - a), xx, yy, mPaint);// 先画不加多余像素的字符
				xx += mPaint.measureText(lineString.substring(0, lineLength - a));
				char[] chartest = lineString.substring(lineLength - a).toCharArray();
				for (char c : chartest) {
					canvas.drawText(String.valueOf(c), xx, yy, mPaint);// 画单个字符
					xx += mPaint.measureText(String.valueOf(c)) + addt;
				}
				xx = mLeftPadding;
			}
			yy += fontSize + fontSize * mLineGapScale;
		}
	}

	/**
	 * 设置背景图片
	 * 
	 * @param canvas
	 */
	private void drawBg(Canvas canvas) {
		canvas.drawColor(mBgColor);
	}

	public void resize(int width, int height) { // 设置画布宽、高，计算可显示宽度、每页行数
		// TODO Auto-generated method stub
		mWidth = width;
		mHeight = height;
		countPageIndex();
	}

	public void setPadding(int padding) {
		// TODO Auto-generated method stub
		mLeftPadding = padding;
		mRightPadding = padding;
		mTopPadding = padding;
		mBottomPadding = padding;
		countPageIndex();
	}

	public int getPageNum() { // 返回总共有多少页
		return (int) Math.ceil((double) strLines.size() / (double) lineNums);
	}

	public void setAnchor(int index) {
		fontPosition = 0;
		int start = index * lineNums;
		start = Math.min(start, strLines.size());
		for (int j = 0; j < start; j++)
			fontPosition += strLines.get(j).length();

		pageIndex = index;
	}

	public int getAnchor() {// 返回字符所在位置
		return fontPosition;
	}

	public int getIndex(int x) { // x:字符所在位置;
		int lines = cutText();
		int length = 0;
		fontPosition = x;
		for (int i = 0; i < lines; i++) {
			length += strLines.get(i).length();
			if (length > x) {
				pageIndex = i / lineNums;
				break;
			}
		}
		return pageIndex;
	}

	public int getCurrentIndex() {
		return pageIndex;
	}

	public void setBg(int bg) { // 设置背景颜色
		mBgColor = bg;
	}

	public void setTextColor(int textColor) { // 设置文字颜色，默认是黑色
		mTextColor = textColor;
		mPaint.setColor(mTextColor);
	}

	public void setLineGap(float lineGapScale) {
		countPageIndex();
	}
}
