/**
 *  Author :  hmg25
 *  Description :
 */
package com.rodrigo.harryportter.ui.view.pagecurl2d;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Vector;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;

public class BookPageFactory {

    private File book_file = null;
    private MappedByteBuffer m_mbBuf = null;
    private int m_FileLen = 0;
    private int m_PositionStart = 0;
    private int m_PositionEnd = 0;
    private String m_strCharsetName = "utf-8";
    private int mWidth;
    private int mHeight;
    private float singleTextWidth;// 一个字的宽度

    private Vector<String> m_lines = new Vector<String>();

    private int m_fontSize = 20;
    private int m_textColor;
    private int m_backColor = 0xffffffff; // 背景颜色
    private int marginWidth = 15; // 左右与边缘的距离
    private int marginHeight = 15; // 上下与边缘的距离

    private int mLineCount; // 每页可以显示的行数
    private float mVisibleHeight; // 绘制内容的宽
    private float mVisibleWidth; // 绘制内容的宽
    private boolean m_isfirstPage, m_islastPage;

    /*
      * 行距
      */
    float mLineGapScale = 0.4f;

    // private int m_nLineSpaceing = 5;

    private Paint mPaint;

    public BookPageFactory(int w, int h, int bgColor, int textColor, int fontSize) {
        // TODO Auto-generated constructor stub
        mWidth = w;
        mHeight = h;
        m_fontSize = fontSize;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextAlign(Align.LEFT);
        mPaint.setTextSize(m_fontSize);
        singleTextWidth = mPaint.measureText("书");
        m_backColor = bgColor;
        m_textColor = textColor;
        mPaint.setColor(m_textColor);
        mVisibleWidth = mWidth - marginWidth * 2;
        mVisibleHeight = mHeight - marginHeight * 2;
        mLineCount = (int) ((mVisibleHeight + m_fontSize * mLineGapScale) / (m_fontSize + m_fontSize * mLineGapScale)); // 可显示的行数
    }

    public void openbook(String strFilePath, int position) throws IOException {
        book_file = new File(strFilePath);
        long lLen = book_file.length();
        m_PositionStart = m_PositionEnd = position;
        m_FileLen = (int) lLen;
        m_mbBuf = new RandomAccessFile(book_file, "r").getChannel().map(FileChannel.MapMode.READ_ONLY, 0, lLen);
    }

    protected byte[] readParagraphBack(int nFromPos) {
        int nEnd = nFromPos;
        int i;
        byte b0, b1;
        if (m_strCharsetName.equals("UTF-16LE")) {
            i = nEnd - 2;
            while (i > 0) {
                b0 = m_mbBuf.get(i);
                b1 = m_mbBuf.get(i + 1);
                if (b0 == 0x0a && b1 == 0x00 && i != nEnd - 2) {
                    i += 2;
                    break;
                }
                i--;
            }

        } else if (m_strCharsetName.equals("UTF-16BE")) {
            i = nEnd - 2;
            while (i > 0) {
                b0 = m_mbBuf.get(i);
                b1 = m_mbBuf.get(i + 1);
                if (b0 == 0x00 && b1 == 0x0a && i != nEnd - 2) {
                    i += 2;
                    break;
                }
                i--;
            }
        } else {
            i = nEnd - 1;
            while (i > 0) {
                b0 = m_mbBuf.get(i);
                if (b0 == 0x0a && i != nEnd - 1) {
                    i++;
                    break;
                }
                i--;
            }
        }
        if (i < 0)
            i = 0;
        int nParaSize = nEnd - i;
        int j;
        byte[] buf = new byte[nParaSize];
        for (j = 0; j < nParaSize; j++) {
            buf[j] = m_mbBuf.get(i + j);
        }
        return buf;
    }

    // 读取上一段落
    protected byte[] readParagraphForward(int nFromPos) {
        int nStart = nFromPos;
        int i = nStart;
        byte b0, b1;
        // 根据编码格式判断换行
        if (m_strCharsetName.equals("UTF-16LE")) {
            while (i < m_FileLen - 1) {
                b0 = m_mbBuf.get(i++);
                b1 = m_mbBuf.get(i++);
                if (b0 == 0x0a && b1 == 0x00) {
                    break;
                }
            }
        } else if (m_strCharsetName.equals("UTF-16BE")) {
            while (i < m_FileLen - 1) {
                b0 = m_mbBuf.get(i++);
                b1 = m_mbBuf.get(i++);
                if (b0 == 0x00 && b1 == 0x0a) {
                    break;
                }
            }
        } else {
            while (i < m_FileLen) {
                b0 = m_mbBuf.get(i++);
                if (b0 == 0x0a) {
                    break;
                }
            }
        }
        int nParaSize = i - nStart;
        byte[] buf = new byte[nParaSize];
        for (i = 0; i < nParaSize; i++) {
            buf[i] = m_mbBuf.get(nFromPos + i);
        }
        return buf;
    }

    protected Vector<String> pageDown() {
        String strParagraph = "";
        Vector<String> lines = new Vector<String>();
        while (lines.size() < mLineCount && m_PositionEnd < m_FileLen) {
            byte[] paraBuf = readParagraphForward(m_PositionEnd); // 读取一个段落
            m_PositionEnd += paraBuf.length;
            try {
                strParagraph = new String(paraBuf, m_strCharsetName);
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            String strReturn = "";
            if (strParagraph.equals("\r\n") || strParagraph.equals("\n"))
                continue;

            strParagraph.replaceAll("\t", "");
            if (!strParagraph.startsWith("    "))
                strParagraph = "        " + strParagraph;

            if (strParagraph.indexOf("\r\n") != -1) {
                strReturn = "\r\n";
                strParagraph = strParagraph.replaceAll("\r\n", "");
            } else if (strParagraph.indexOf("\n") != -1) {
                strReturn = "\n";
                strParagraph = strParagraph.replaceAll("\n", "");
            }

            if (strParagraph.length() == 0) {
                lines.add(strParagraph);
            }
            while (strParagraph.length() > 0) {
                int nSize = mPaint.breakText(strParagraph, true, mVisibleWidth, null);
                lines.add(strParagraph.substring(0, nSize));
                strParagraph = strParagraph.substring(nSize);
                if (lines.size() >= mLineCount) {
                    break;
                }
            }
            if (strParagraph.length() != 0) {
                try {
                    m_PositionEnd -= (strParagraph + strReturn).getBytes(m_strCharsetName).length;
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            /*
                * 首行去掉空行
                */
            if (lines.get(0).length() == 0)
                lines.remove(0);
            if (lines.get(0).startsWith("\t") || lines.get(0).startsWith(" ")) {
                String firstLine = lines.get(0);
                lines.remove(0);
                lines.add(0, firstLine.replaceAll("^[ \\t]+", ""));
            }
        }
        return lines;
    }

    protected void pageUp() {
        if (m_PositionStart < 0)
            m_PositionStart = 0;
        Vector<String> lines = new Vector<String>();
        String strParagraph = "";
        while (lines.size() < mLineCount && m_PositionStart > 0) {
            Vector<String> paraLines = new Vector<String>();
            byte[] paraBuf = readParagraphBack(m_PositionStart);
            m_PositionStart -= paraBuf.length;
            try {
                strParagraph = new String(paraBuf, m_strCharsetName);
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (strParagraph.equals("\r\n") || strParagraph.equals("\n"))
                continue;

            strParagraph = strParagraph.replaceAll("\r\n", "");
            strParagraph = strParagraph.replaceAll("\n", "");

            if (strParagraph.length() == 0) {
                paraLines.add(strParagraph);
            }
            while (strParagraph.length() > 0) {
                int nSize = mPaint.breakText(strParagraph, true, mVisibleWidth, null);
                paraLines.add(strParagraph.substring(0, nSize));
                strParagraph = strParagraph.substring(nSize);
            }
            lines.addAll(0, paraLines);
        }
        while (lines.size() > mLineCount) {
            try {
                m_PositionStart += lines.get(0).getBytes(m_strCharsetName).length;
                lines.remove(0);
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        m_PositionEnd = m_PositionStart;
        return;
    }

    public void jumpTo(double percent) {
        m_PositionStart = (int) (m_FileLen * percent);
    }

    protected void prePage() throws IOException {
        m_lines.clear();
        pageUp();
        m_lines = pageDown();
        setFirstAndLast();
    }

    private void setFirstAndLast() {
        if (m_PositionStart <= 0) {
            m_PositionStart = 0;
            m_isfirstPage = true;
            return;
        } else
            m_isfirstPage = false;
        if (m_PositionEnd >= m_FileLen) {
            m_islastPage = true;
            return;
        } else
            m_islastPage = false;
    }

    public void nextPage() throws IOException {
        m_lines.clear();
        m_PositionStart = m_PositionEnd;
        m_lines = pageDown();
        setFirstAndLast();
    }

    public void draw(Canvas canvas) {
        if (m_lines.size() == 0) {
            m_lines = pageDown();
            setFirstAndLast();
        }

        int xx = marginWidth;
        int yy = marginHeight + m_fontSize; // 画笔是从字的底部作为baseline画的,需要加上字的高度

        if (m_lines.size() > 0) {
            canvas.drawColor(m_backColor);// 画背景
            int y = marginHeight;
            for (String strLine : m_lines) {
                // y += m_fontSize + m_fontSize * mLineGapScale;
                // c.drawText(strLine, marginWidth, y, mPaint);
                int lineLength = strLine.length();
                float jj = mVisibleWidth - mPaint.measureText(strLine); // 一行多余的像素
                if (jj == 0 || jj > singleTextWidth) {
                    canvas.drawText(strLine, xx, yy, mPaint); // 如果正好或者多余的像素超出一个字符，则直接画上这一行
                } else {
                    int addt = (int) (Math.ceil(jj / strLine.length())); // 每个字符多几个像素
                    int a = (int) Math.floor(jj / addt); // 多余的像素加在几个字符上
                    canvas.drawText(strLine.substring(0, lineLength - a), xx, yy, mPaint);// 先画不加多余像素的字符
                    xx += mPaint.measureText(strLine.substring(0, lineLength - a));
                    char[] chartest = strLine.substring(lineLength - a).toCharArray();
                    for (char c : chartest) {
                        canvas.drawText(String.valueOf(c), xx, yy, mPaint);// 画单个字符
                        xx += mPaint.measureText(String.valueOf(c)) + addt;
                    }
                    xx = marginWidth;
                }
                yy += m_fontSize + m_fontSize * mLineGapScale;
            }
        }
    }

    public void setUpColor(int bgColor, int textColor) {
        m_backColor = bgColor;
        m_textColor = textColor;
        mPaint.setColor(m_textColor);
    }

    public boolean isfirstPage() {
        return m_isfirstPage;
    }

    public boolean islastPage() {
        return m_islastPage;
    }

    public int getPosition() {
        return m_PositionStart;
    }

    public float getPositionPercent() {
        if (m_islastPage)
            return 1f;
        return (float) m_PositionStart / (float) m_FileLen;
    }

    public int getTotalSize() {
        return m_FileLen;
    }

    public void setTextSize(int newSize) {
        m_fontSize = newSize;
        mPaint.setTextSize(m_fontSize);
        singleTextWidth = mPaint.measureText("书");
        mLineCount = (int) (mVisibleHeight / (m_fontSize + m_fontSize * mLineGapScale)); // 可显示的行数
        m_lines.clear();
    }

    protected void resetCurrent() {
        m_lines.clear();
        m_PositionEnd = m_PositionStart;
    }
}
