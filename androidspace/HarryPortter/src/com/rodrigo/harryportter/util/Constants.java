package com.rodrigo.harryportter.util;

import com.rodrigo.harryportter.R;

public class Constants {
    public static final String SD_BOOK_DIR = "/sdcard/RodrigoHarryPortter/";

    public static final String PREFERENCE_READ = "read";
    public static final String PREFERENCE_READ_BRIGHTNESS = "brightness";
    public static final String PREFERENCE_READ_BRIGHTNESS_AUTO = "brightnessAuto";
    public static final String PREFERENCE_CATEGORY_DESC = "isdesc";
    public static final String PREFERENCE_READ_AUTODOWNLOAD = "autoDownload";
    public static final String PREFERENCE_READ_AUTODOWNLOAD_NUM = "autoDownloadNum";
    public static final int[] READ_AUTODOWN_NUM = {1, 2, 3, 5, 8, 10};
    public static final String PREFERENCE_READ_CHAPTERNAME_CROLL = "chapterNamesroll";
    public static final String PREFERENCE_READ_SHOW_LAST_CHAPTER_TIP = "showLastChapterTip";
    public static final String PREFERENCE_FULL_SCREEN = "isFullScreen";

    public static final String PREFERENCE_READ_FONTSIZE = "FontSize";
    public static final String PREFERENCE_READ_FONTCOLOR = "FontColor";
    public static final String PREFERENCE_READ_BGCOLOR = "BgColor";

    public static final String PREFERENCE_READ_PAGE_METHOD = "PageMethod";

    public static final String PREFERENCE_READ_AD_LAST_PRESS = "LastPressTime";
    public static final long PRESS_GAP = 3600000;
    public static int[] covers = new int[]{R.drawable.b01, R.drawable.b02, R.drawable.b03, R.drawable.b04, R.drawable.b05, R.drawable.b06, R.drawable.b07};
}
