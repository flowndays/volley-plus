package com.rodrigo.harryportter.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.adview.AdViewInterface;
import com.adview.AdViewLayout;
import com.rodrigo.harryportter.R;
import com.rodrigo.harryportter.db.Book;
import com.rodrigo.harryportter.db.DBInstance;
import com.rodrigo.harryportter.ui.view.ReadViewProvider;
import com.rodrigo.harryportter.ui.view.pagecurl2d.PageCurl2dProvider;
import com.rodrigo.harryportter.ui.view.pagecurl2d.PageCurl2dView;
import com.rodrigo.harryportter.ui.view.pagecurlno.ReadNoAnimationProvider;
import com.rodrigo.harryportter.ui.view.pagecurlno.ReadNoAnimationView;
import com.rodrigo.harryportter.util.Constants;
import com.rodrigo.harryportter.util.Fixer;
import com.rodrigo.harryportter.util.LayoutUtil;
import com.rodrigo.harryportter.util.SystemUtil;

import java.io.*;
import java.math.BigDecimal;
import java.util.Date;

public class ReadActivity extends Activity implements AdViewInterface {
	SharedPreferences mPreferences;
	/*
	 * 底部状态显示条（进度 书名 时间|电量）
	 */
	private TextView mTopBookName; // 顶部书名
	private ImageView mTopProgress; // 顶部阅读进度
	private TextView mTopTime; // 顶部时间
	private TextView mTopElectric; // 顶部电量
	private BroadcastReceiver mBatteryReceiver;

    final static short THEME_DAY = 0;
	final static short THEME_NEIGHT = 1;
	final static short THEME_PROTECT_EYE = 2;

	final static short OPTION_FONT = 0;
	final static short OPTION_BRIGHT = 1;
	final static short OPTION_DAY = 2;
	final static short OPTION_NEIGHT = 3;
	final static short OPTION_DIRECTORY = 4;
	final static short OPTION_JUMP = 5;
	final static short OPTION_SETUP = 6;

	final static short PAGE_METHOD_2D = 0;
	final static short PAGE_METHOD_3D = 1;
	final static short PAGE_METHOD_NONE = 2;

	final static int FONT_COLOR_DAY = 0xff000000;
	final static int FONT_COLOR_NEIGHT = 0xff425363;
	final static int BG_COLOR_DAY = 0xffffffff;
	final static int BG_COLOR_NEIGHT = 0xff182432;

	/*
	 * 用于屏幕点击事件
	 */
	private Coord mScreen = new Coord();

	/**
	 * 坐标
	 * 
	 * @author DevUser
	 */
	static class Coord {
		int x;
		int y;
	}

	private int mFontSize;// 字号
	private int mTextColor = 0;// 字体颜色
	private int mBgColor = 0xffffffff;// 背景颜色
	private int mBright = 100; // 亮度
	private int mBrightSystem = -1; // 系统亮度
	private boolean mIsAutoBright = true; // 是否自动调节亮度
	private short mPageMethod = PAGE_METHOD_2D;

	private short mTheme = THEME_DAY;

	private RelativeLayout mReadWholeLayout; // 整个layout
	private ReadViewProvider mReadViewProvider;

	private String mContent;
	private Book mBook;

	private Handler mLoadFinishHandler;

	private View mContentView = null;

	private AdViewLayout mAdView;

	private boolean mDissMissAD = true;

	private boolean mShowAd = true;

	private boolean mAdTipToasted = false;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.read);
		mPreferences = getSharedPreferences(Constants.PREFERENCE_READ, Activity.MODE_PRIVATE);

		mReadWholeLayout = (RelativeLayout) findViewById(R.id.readLayout);

		/*
		 * 广告
		 */
		/* 下面两行只用于测试,完成后一定要去掉,参考文挡说明 */
		// AdViewTargeting.setUpdateMode(UpdateMode.EVERYTIME); //每次都从服务器取配置
		// AdViewTargeting.setRunMode(RunMode.TEST); //保证所有选中的广告公司都为测试状态
		mAdView = (AdViewLayout) findViewById(R.id.adview_layout);
		if (mDissMissAD) {
			long lastPress = mPreferences.getLong(Constants.PREFERENCE_READ_AD_LAST_PRESS, 0);
			if (System.currentTimeMillis() - lastPress > Constants.PRESS_GAP) {// 超过一小时
				mAdView.setVisibility(View.VISIBLE);
				mAdView.setAdViewInterface(this);
				mShowAd = true;
			} else {
				mShowAd = false;
				mAdView.setAdViewInterface(null);
			}
		} else {
			mAdView.setVisibility(View.VISIBLE);
			mAdView.setAdViewInterface(this);
			mShowAd = true;
		}

		// 广告 end

		/*
		 * 获取屏幕参数
		 */
		prepareScreenParam();

		long bookId = getIntent().getLongExtra("bookId", -1);
		if (bookId == -1) {
			finish();
			return;
		}

		mBook = DBInstance.getInstance(this).selectBookByID(bookId);

		/*
		 * mTextColor = 0xff000000; mBgColor = 0xffffffff; break; case
		 * THEME_NEIGHT: mTextColor = 0xff425363; mBgColor = 0xff182432;
		 */
		mTextColor = mPreferences.getInt(Constants.PREFERENCE_READ_FONTCOLOR, FONT_COLOR_DAY);
		mBgColor = mPreferences.getInt(Constants.PREFERENCE_READ_BGCOLOR, BG_COLOR_DAY);
		mTheme = mTextColor == FONT_COLOR_DAY ? THEME_DAY : THEME_NEIGHT;

		mFontSize = mPreferences.getInt(Constants.PREFERENCE_READ_FONTSIZE, 20);

		mPageMethod = (short) mPreferences.getInt(Constants.PREFERENCE_READ_PAGE_METHOD, PAGE_METHOD_2D);

		initViews();

		prepareContents();

		mLoadFinishHandler = new Handler() {
			/*
			 * 加载完章节内容后的回调函数，不管加载结果如何，都不触发自动下载
			 * 
			 * what:加载结果 arg1：是否需要刷新章节题目页弹出框,arg==1时刷新
			 * 
			 * @see android.os.Handler#handleMessage(android.os.Message)
			 */
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case ReadNoAnimationProvider.DRAW_FINISH:
					break;
				case ReadNoAnimationProvider.DRAW_FAILED:
					break;
				}
			}
		};

		loadContent(true);
	}

	private void prepareContents() {
		if (mPageMethod == PAGE_METHOD_NONE) {
			try {
				InputStream inputStream = new FileInputStream(new File(Constants.SD_BOOK_DIR + "books/"
						+ mBook.getFileName()));

				BufferedInputStream reader = new BufferedInputStream(inputStream, 8000);
				StringBuilder sb = new StringBuilder(8000);
				byte[] b = new byte[8000];

				while (reader.read(b) != -1) {
					sb.append(new String(b, "utf-8"));
				}
				reader.close();
				mContent = Fixer.fixContent(sb.toString());

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			mContent = Constants.SD_BOOK_DIR + "books/" + mBook.getFileName();
		}
	}

	private void loadContent(boolean autoFinish) {
		mReadViewProvider.setupParams(mContent, mScreen.x, mScreen.y, mFontSize, mTextColor, mBgColor,
				mBook.getlReadPlace(), autoFinish, mLoadFinishHandler);
		setReadProgress(mReadViewProvider.getPositionPercent());
	}

	private void prepareScreenParam() {
		/*
		 * 计算屏幕参数
		 */
		Display display = getWindowManager().getDefaultDisplay();
		mScreen.x = display.getWidth();
		mScreen.y = display.getHeight() - LayoutUtil.GetPixelByDIP(this, mShowAd ? 68 : 16);
	}

	@Override
	protected void onPause() {
		/*
		 * 记录上次阅读位置
		 */
		recordLastPosition();

		/*
		 * 处理亮度
		 */
		SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCE_READ, Context.MODE_PRIVATE);
		boolean useSystem = preferences.getBoolean(Constants.PREFERENCE_READ_BRIGHTNESS_AUTO, false);
		if (!useSystem) {
			if (mIsAutoBright) {
				SystemUtil.enableAutoBrightness(getContentResolver());
			} else if (mBright != mBrightSystem) {
				WindowManager.LayoutParams layoutParmas = getWindow().getAttributes();
				layoutParmas.screenBrightness = (float) mBrightSystem / 255;
				getWindow().setAttributes(layoutParmas);
			}
		}

		unregisterReceiver(mBatteryReceiver);
		super.onPause();
	}

	private void recordLastPosition() {
		int position = mReadViewProvider.getPosition();

		mBook.setlReadPlace(position);
		mBook.setSize(mReadViewProvider.getTotleSize());
		DBInstance.getInstance(this).updateBook(mBook);
	}

	@Override
	protected void onResume() {
		mIsAutoBright = SystemUtil.isAutoBrightness(getContentResolver());

		if (!mIsAutoBright) {
			try {
				mBrightSystem = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
			} catch (SettingNotFoundException e) {
				mBrightSystem = 100;
			}
		} else {
			mBrightSystem = -1;// 自动亮度时，系统亮度设为-1
		}

		SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCE_READ, Context.MODE_PRIVATE);
		boolean useSystem = preferences.getBoolean(Constants.PREFERENCE_READ_BRIGHTNESS_AUTO, false);
		if (!useSystem) {
			mBright = preferences.getInt(Constants.PREFERENCE_READ_BRIGHTNESS, mBrightSystem);
			/*
			 * 避免各种原因引起最小亮度太小导致黑屏
			 */
			if (mBright < 2 && mBrightSystem != -1) {
				mBright = 2;
				preferences.edit().putInt(Constants.PREFERENCE_READ_BRIGHTNESS, 2).commit();
			}

			if (mBright != mBrightSystem) {
				WindowManager.LayoutParams layoutParmas = getWindow().getAttributes();
				layoutParmas.screenBrightness = (float) mBright / 255;
				getWindow().setAttributes(layoutParmas);
			}
		} else if (mBrightSystem != -1) {
			WindowManager.LayoutParams layoutParmas = getWindow().getAttributes();
			layoutParmas.screenBrightness = (float) mBrightSystem / 255;
			getWindow().setAttributes(layoutParmas);
		}

		/*
		 * 电量显示
		 */
		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(mBatteryReceiver, filter);
		super.onResume();
	}

	private void initViews() {
		setupContentView();

		/*
		 * 底部状态条
		 */
		// 阅读进度
		mTopProgress = (ImageView) findViewById(R.id.readProgress);
		// mTopProgress.setPadding(0, 0, LayoutUtil.GetPixelByDIP(this, 480),
		// 0);
		setReadProgress(0f);
		// 章节名
		mTopBookName = (TextView) findViewById(R.id.readStatusName);
		mTopBookName.getLayoutParams().width = getWindowManager().getDefaultDisplay().getWidth() / 3;
		mTopBookName.setText(mBook.getName());
		setUpChapterNameScroll(mPreferences.getBoolean(Constants.PREFERENCE_READ_CHAPTERNAME_CROLL, false));

		mTopTime = (TextView) findViewById(R.id.readStatusTime);
		mTopTime.setText(DateFormat.format("kk:mm", new Date()).toString());
		mTopElectric = (TextView) findViewById(R.id.readStatusElectric);

		/*
		 * 时间显示
		 */
		new Thread(new Runnable() {
			Date date;
			String time;

			@Override
			public void run() {
				while (!ReadActivity.this.isFinishing()) {// 只要当前activity还在运行，就保持时间更新
					date = new Date();
					time = DateFormat.format("kk:mm", date).toString();
					mTopTime.post(new Runnable() {

						@Override
						public void run() {
							mTopTime.setText(time);
						}
					});
					try {
						Thread.sleep(60000);// 每分钟更新一次
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
		/*
		 * 电量显示Receiver,这里只是定义，在onResume中注册
		 */
		mBatteryReceiver = new BroadcastReceiver() {
			int scale = -1;
			int level = -1;

			@Override
			public void onReceive(Context context, Intent intent) {
				scale = intent.getIntExtra("scale", -1); // API Level5
															// 以下，需要硬编码，以上可以用BatteryManager.EXTRA_LEVEL常量
				level = intent.getIntExtra("level", -1);
				mTopElectric.setText("电量：" + level * 100 / scale + "%");
			}
		};
	}

	private void setReadProgress(float percent) {
		mTopProgress.setPadding(0, 0, (int) (mScreen.x * (1 - percent)), 0);
		mTopProgress.invalidate();
	}

	public void setUpChapterNameScroll(boolean enable) {
		if (enable) {
			mTopBookName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
			mTopBookName.setSelected(true);
		} else {
			mTopBookName.setSelected(false);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.removeItem(OPTION_NEIGHT);
		menu.removeItem(OPTION_DAY);
		if (mTheme == THEME_DAY)
			menu.add(1, OPTION_NEIGHT, 3, "夜间模式");
		else
			menu.add(1, OPTION_DAY, 3, "白天模式");
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, OPTION_FONT, 0, "字体");
		menu.add(0, OPTION_BRIGHT, 1, "亮度");
		// menu.add(0, OPTION_DIRECTORY, 2, "目录");
		menu.add(0, OPTION_JUMP, 2, "跳转");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case OPTION_FONT:
			showFontDialog();
			break;
		case OPTION_BRIGHT:
			showBrightDialog();
			break;
		case OPTION_DAY:
			mTheme = THEME_DAY;
			setUpTheme();
			break;
		case OPTION_NEIGHT:
			mTheme = THEME_NEIGHT;
			setUpTheme();
			break;
		case OPTION_JUMP:
			showJumpDialog();
			break;
		case OPTION_SETUP:
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void showJumpDialog() {
		final View jumpView = LayoutInflater.from(this).inflate(R.layout.read_jump_dialog, null);

		/*
		 * 进度
		 */
		final TextView percentView = (TextView) jumpView.findViewById(R.id.percentTip);
		percentView.setText(getDoubleWithScale(2, mReadViewProvider.getPositionPercent() * 100) + "%");

		/*
		 * Dialog
		 */
		AlertDialog dialog = new AlertDialog.Builder(this).setTitle("跳转到").setView(jumpView)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				}).show();
		dialog.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		final ZoomControls readJumpZoom = (ZoomControls) jumpView.findViewById(R.id.readJumpZoomControl);
		final SeekBar jumpSeekBar = (SeekBar) jumpView.findViewById(R.id.readJumpSeekBar);

		jumpSeekBar
				.setProgress((int) ((double) mReadViewProvider.getPositionPercent() * (double) jumpSeekBar.getMax()));
		jumpSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				double percent = (double) progress / (double) jumpSeekBar.getMax();
				mReadViewProvider.jumpTo(percent);

				percentView.setText(getDoubleWithScale(5, percent * 100) + "%");
			}
		});

		readJumpZoom.setOnZoomInClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				jumpSeekBar.setProgress(jumpSeekBar.getProgress() + 1);
				double newPercent = (double) jumpSeekBar.getProgress() / (double) jumpSeekBar.getMax();
				percentView.setText(getDoubleWithScale(5, newPercent * 100) + "%");
			}
		});
		readJumpZoom.setOnZoomOutClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				jumpSeekBar.setProgress(jumpSeekBar.getProgress() - 1);
				// mReadViewProvider.getPositionPercent()
				double p = (double) jumpSeekBar.getProgress() / (double) jumpSeekBar.getMax();
				percentView.setText(getDoubleWithScale(5, p * 100) + "%");
			}
		});
	}

	protected double getDoubleWithScale(int scale, double d) {
		return new BigDecimal(d).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();

	}

	private void setUpTheme() {
		switch (mTheme) {
		case THEME_DAY:
			mTextColor = FONT_COLOR_DAY;
			mBgColor = BG_COLOR_DAY;
			break;
		case THEME_NEIGHT:
			mTextColor = FONT_COLOR_NEIGHT;
			mBgColor = BG_COLOR_NEIGHT;
			break;
		}
		Editor edit = mPreferences.edit();
		edit.putInt(Constants.PREFERENCE_READ_BGCOLOR, mBgColor);
		edit.putInt(Constants.PREFERENCE_READ_FONTCOLOR, mTextColor);
		edit.commit();
		// mReadViewProvider.setTheme(mBgColor, mTextColor);
		mReadViewProvider.setTheme(mBgColor, mTextColor);
	}

	private void showFontDialog() {
		final View wordSetupView = LayoutInflater.from(this).inflate(R.layout.read_wordsetup_dialog, null);

		/*
		 * 字号
		 */
		final TextView sizeTip = (TextView) wordSetupView.findViewById(R.id.wordSizeTip);
		sizeTip.setText("" + mFontSize);

		/*
		 * Dialog
		 */
		AlertDialog dialog = new AlertDialog.Builder(this).setTitle("字号调节").setView(wordSetupView)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				}).show();
		dialog.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		final ZoomControls readFontZoom = (ZoomControls) wordSetupView.findViewById(R.id.readFontZoomControl);
		final SeekBar sizeSeekBar = (SeekBar) wordSetupView.findViewById(R.id.readWordSizeSeekBar);

		sizeSeekBar.setProgress(mFontSize);
		sizeSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				mFontSize = sizeSeekBar.getProgress();
				mReadViewProvider.changeFontSize(mFontSize);
				updateFontSize(mFontSize);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (progress <= 10) {
					progress = 10;
					sizeSeekBar.setProgress(progress);
				}
				sizeTip.setText(progress + "");// 这里必须把progress转化成string以后再调用，否则会被当成R中的资源文件
			}
		});

		readFontZoom.setOnZoomInClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (mFontSize >= 50)
					mFontSize = 50;
				else
					mFontSize++;
				sizeTip.setText(mFontSize + "");
				mReadViewProvider.changeFontSize(mFontSize);
				updateFontSize(mFontSize);
			}
		});
		readFontZoom.setOnZoomOutClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (mFontSize <= 10)
					mFontSize = 10;
				else
					mFontSize--;
				sizeTip.setText(mFontSize + "");
				mReadViewProvider.changeFontSize(mFontSize);
				updateFontSize(mFontSize);
			}
		});
	}

	/**
	 * 更新字号
	 * 
	 * @param size
	 */
	private void updateFontSize(int size) {
		Editor editor = mPreferences.edit();
		editor.putInt(Constants.PREFERENCE_READ_FONTSIZE, size);
		editor.commit();
	}

	private void showBrightDialog() {
		final View brightSetupView = LayoutInflater.from(this).inflate(R.layout.read_bright_dialog, null);
		AlertDialog dialog = new AlertDialog.Builder(this).setTitle("亮度调节").setView(brightSetupView)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				}).show();
		dialog.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		/*
		 * 亮度调节
		 */
		final CheckBox sysBright = (CheckBox) brightSetupView.findViewById(R.id.read_setup_bright_system);
		sysBright.setChecked(mPreferences.getBoolean(Constants.PREFERENCE_READ_BRIGHTNESS_AUTO, true));

		final SeekBar brightnessSeekBar = (SeekBar) brightSetupView.findViewById(R.id.read_setup_bright_seekbar);
		brightnessSeekBar.setProgress(sysBright.isChecked() ? mBrightSystem : mBright);

		brightnessSeekBar.setEnabled(!sysBright.isChecked());
		brightnessSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				SharedPreferences preferences = ReadActivity.this.getSharedPreferences(Constants.PREFERENCE_READ,
						Activity.MODE_PRIVATE);
				preferences.edit().putInt(Constants.PREFERENCE_READ_BRIGHTNESS, seekBar.getProgress()).commit();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (progress <= 2) {// 最小亮度
					seekBar.setProgress(2);
					progress = 2;
				}
				float value = progress;
				WindowManager.LayoutParams layoutParmas = ReadActivity.this.getWindow().getAttributes();
				layoutParmas.screenBrightness = value / 255;
				ReadActivity.this.getWindow().setAttributes(layoutParmas);
				mBright = progress;
			}
		});
		sysBright.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					brightnessSeekBar.setEnabled(false);
					brightnessSeekBar.setProgress(mBrightSystem);
					if (mIsAutoBright) {
						WindowManager.LayoutParams layoutParmas = getWindow().getAttributes();
						layoutParmas.screenBrightness = -1;
						getWindow().setAttributes(layoutParmas);
					} else if (mBright != mBrightSystem) {
						WindowManager.LayoutParams layoutParmas = getWindow().getAttributes();
						layoutParmas.screenBrightness = (float) mBrightSystem / 255;
						getWindow().setAttributes(layoutParmas);
						mBright = mBrightSystem;
					}
					mPreferences.edit().putBoolean(Constants.PREFERENCE_READ_BRIGHTNESS_AUTO, true)
							.putInt(Constants.PREFERENCE_READ_BRIGHTNESS, mBrightSystem).commit();

				} else {
					brightnessSeekBar.setEnabled(true);

					mPreferences.edit().putBoolean(Constants.PREFERENCE_READ_BRIGHTNESS_AUTO, false).commit();
				}
			}
		});
	}

	private void setupContentView() {

		switch (mPageMethod) {
		case PAGE_METHOD_2D: {
			mContentView = new PageCurl2dView(this);
			((PageCurl2dView) mContentView).setScreen(mScreen.x, mScreen.y);// TODO:check
																			// if
																			// needed.
			mReadViewProvider = new PageCurl2dProvider(this, (PageCurl2dView) mContentView);
			break;
		}
		case PAGE_METHOD_NONE: {
			mContentView = new ReadNoAnimationView(this);
			mReadViewProvider = new ReadNoAnimationProvider(this, (ReadNoAnimationView) mContentView);
			break;
		}
		}
		mContentView.setId(100);

		LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		lp.setMargins(0, LayoutUtil.GetPixelByDIP(this, 16), 0, LayoutUtil.GetPixelByDIP(this, mShowAd ? 52 : 0));
		mContentView.setLayoutParams(lp);
		mContentView.setBackgroundColor(0);

		if (mReadWholeLayout.getChildAt(0).getId() == 100) {
			mReadWholeLayout.removeViewAt(0);
			mReadViewProvider = null;
		}
		mReadWholeLayout.addView(mContentView, 0);
		/*
		 * 获取焦点，以便响应onKeyDown事件
		 */
		mContentView.setFocusable(true);
		mContentView.setFocusableInTouchMode(true);
		mContentView.requestFocus();
		mContentView.setClickable(true);

		mReadViewProvider.setOnTouchObserver(new ReadViewProvider.OnTouchObserver() {

			@Override
			public boolean lastPageNext() {
				return false;
			}

			@Override
			public boolean firstPagePrevious() {
				return false;
			}

			@Override
			public void centerClicked() {
				ReadActivity.this.openOptionsMenu();
			}
		});

		mReadViewProvider.setCurlObserver(new ReadViewProvider.CurlObserver() {

			@Override
			public void preCurl(int currentIndex, boolean isToNext) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterCurl(int currentIndex) {
				setReadProgress(mReadViewProvider.getPositionPercent());
			}
		});
	}

	private void hideAds() {
		mShowAd = false;

		recordLastPosition();
		LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		lp.setMargins(0, LayoutUtil.GetPixelByDIP(this, 16), 0, 0);
		mContentView.setLayoutParams(lp);
		mAdView.setVisibility(View.INVISIBLE);
		//		mAdView.setAdViewInterface(null);
		prepareScreenParam();
		((PageCurl2dView) mContentView).setScreen(mScreen.x, mScreen.y);
		loadContent(true);
	}

	@Override
	public void onClickAd() {
		if (mDissMissAD) {
			//			hideAds();
			mPreferences.edit().putLong(Constants.PREFERENCE_READ_AD_LAST_PRESS, System.currentTimeMillis()).commit();

			Toast.makeText(this, "重新进入阅读界面就没有广告了哦！", Toast.LENGTH_SHORT).show();
		}

		Log.i("AdViewSample", "onClickAd");
	}

	@Override
	public void onDisplayAd() {
		if (mDissMissAD && !mAdTipToasted && mShowAd) {
			mAdTipToasted = true;
			Toast.makeText(this, "点击广告后重新进入阅读即可在2小时内关闭广告", Toast.LENGTH_SHORT).show();
		}
		Log.i("AdViewSample", "onDisplayAd");
	}
}
