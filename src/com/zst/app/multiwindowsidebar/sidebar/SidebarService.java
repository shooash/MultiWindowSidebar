package com.zst.app.multiwindowsidebar.sidebar;

import com.zst.app.multiwindowsidebar.Common;
import com.zst.app.multiwindowsidebar.IntentUtil;
import com.zst.app.multiwindowsidebar.MainActivity;
import com.zst.app.multiwindowsidebar.R;
import com.zst.app.multiwindowsidebar.Util;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class SidebarService extends Service {
	// Thanks: https://github.com/EatHeat/FloatingExample

	public static boolean isRunning;
	public static boolean isStoppable;
	public static boolean isSidebarShown;

	public WindowManager mWindowManager;
	public Handler mHandler;
	
	private SharedPreferences main_prefs;
	
	// Preference Settings
	public Boolean mBarOnRight = null;
	public int mAnimationTime;
	public int mLabelSize;
	public int mLabelColor;
	public int mAppColumns;
	public int mTabSize;

	private static SidebarHolderView mShownSidebar;
	private static SidebarHiddenView mHiddenSidebar;
	
	@Override 
	public void onCreate() {
		super.onCreate();
		isRunning = true;
		isStoppable = false;
		
		mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		mHandler = new Handler();
		
		if (!isSidebarShown && mHiddenSidebar == null && mShownSidebar == null) {
			// Non-refreshable settings
			main_prefs = getSharedPreferences(Common.KEY_PREFERENCE_MAIN,
					Context.MODE_PRIVATE);
			refreshTheme();
			
			mMargin = main_prefs.getInt(Common.PREF_KEY_SIDEBAR_MARGIN, Common.PREF_DEF_SIDEBAR_MARGIN);
			
			mHiddenSidebar = new SidebarHiddenView(this);
			mShownSidebar = new SidebarHolderView(this);
			isSidebarShown = true;
			
			refreshSettings();
			
			hideBar();
		}
	}
	
	// All the refreshable settings go here
	@SuppressWarnings("deprecation")
	public void refreshSettings() {
		SharedPreferences app_prefs = getSharedPreferences(Common.KEY_PREFERENCE_APPS, Context.MODE_PRIVATE);
		if (main_prefs == null) {
			main_prefs = getSharedPreferences(Common.KEY_PREFERENCE_MAIN, Context.MODE_PRIVATE);
		}
		
		boolean bar_on_right = Integer.parseInt(main_prefs.getString(Common.PREF_KEY_SIDEBAR_POSITION,
				Common.PREF_DEF_SIDEBAR_POSITION)) == 1;
		if (mBarOnRight == null || bar_on_right != mBarOnRight) {
			mBarOnRight = bar_on_right;
			mHiddenSidebar.refreshBarSide();
			mShownSidebar.refreshBarSide();
			killBars();
			hideBar();
		}
		
		mTabSize = main_prefs.getInt(Common.PREF_KEY_TAB_SIZE,
				Common.PREF_DEF_TAB_SIZE);
		mShownSidebar.setTabSize(mTabSize);
		mHiddenSidebar.setTabSize(mTabSize);
		
		final int hidden_tab_alpha_percentage = main_prefs.getInt(Common.PREF_KEY_TAB_ALPHA_HIDDEN,
				Common.PREF_DEF_TAB_ALPHA_HIDDEN);
		mHiddenSidebar.setTabAlpha(hidden_tab_alpha_percentage * 0.01f);
		
		final int speed = main_prefs.getInt(Common.PREF_KEY_ANIM_TIME,
				Common.PREF_DEF_ANIM_TIME);
		mAnimationTime = speed;
		
		IntentUtil.sLaunchModeDrag = Integer.parseInt(main_prefs.getString(Common.PREF_KEY_DRAG_LAUNCH_MODE,
				Common.PREF_DEF_DRAG_LAUNCH_MODE));
		IntentUtil.sLaunchModeTap = Integer.parseInt(main_prefs.getString(Common.PREF_KEY_TAP_LAUNCH_MODE,
				Common.PREF_DEF_TAP_LAUNCH_MODE));

		final int label_size = main_prefs.getInt(Common.PREF_KEY_LABEL_SIZE,
				Common.PREF_DEF_LABEL_SIZE);
		mLabelSize = label_size;
		final int label_color = Util.parseColorFromString(main_prefs.getString(Common.PREF_KEY_LABEL_COLOR,
				Common.PREF_DEF_LABEL_COLOR), Common.PREF_DEF_LABEL_COLOR);
		mLabelColor = label_color;
		
		// Add the foreground notification
		if (main_prefs.getBoolean(Common.PREF_KEY_KEEP_IN_BG, Common.PREF_DEF_KEEP_IN_BG)) {
			PendingIntent intent = PendingIntent.getActivity(this, 0,
					new Intent(this, MainActivity.class), 0);
			Notification.Builder n  = new Notification.Builder(this)
				.setContentTitle(getResources().getText(R.string.app_name_running))
				.setSmallIcon(R.drawable.transparent)
				.setContentIntent(intent);
			
			if (Build.VERSION.SDK_INT >= 16) {
				n.setPriority(Notification.PRIORITY_MIN);
				startForeground(99999, n.build());
			} else {
				startForeground(99999, n.getNotification());
			}
			
		} else {
			stopForeground(true);
		}
		
		mAppColumns = main_prefs.getInt(Common.PREF_KEY_COLUMN_NUMBER,
				Common.PREF_DEF_COLUMN_NUMBER);
		mShownSidebar.applySidebarWidth(80);
		//TODO add option for customizing each column width
		
		mShownSidebar.addApps(app_prefs, getPackageManager());
	}
	
	public void refreshTheme() {
		ThemeSetting.setTheme(Integer.parseInt(main_prefs.getString(
				Common.PREF_KEY_SIDEBAR_THEME, Common.PREF_DEF_SIDEBAR_THEME)));
		
		if (mHiddenSidebar != null) {
			mHiddenSidebar.refreshBarSide();
		}
		
		if (mShownSidebar != null) {
			mShownSidebar.refreshBarSide();
			mShownSidebar.refreshTabMargin(mBarOnRight);
			mShownSidebar.refreshMenuButtonIcons();
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		if (intent != null && intent.getBooleanExtra(Common.EXTRA_REFRESH_SERVICE, false)) {
			refreshSettings();
		}
		if (intent != null && intent.getBooleanExtra(Common.EXTRA_REFRESH_THEME, false)) {
			refreshTheme();
		}
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		isRunning = false;
		
		if (main_prefs != null && mMargin >= 0) {
			main_prefs.edit().putInt(Common.PREF_KEY_SIDEBAR_MARGIN, mMargin).commit();
		}
		
		if (isStoppable) {
			isSidebarShown = false;
			isStoppable = false;
			mHiddenSidebar.animateView(false);
			mShownSidebar.animateView(false);
			new Handler().postDelayed(new Runnable () {
				@Override
				public void run() {
					killBars();
					mHiddenSidebar = null;
					mShownSidebar = null;
				}
			}, 500);
		} else {
			Intent i = new Intent(Common.PKG_THIS + ".START");
			sendBroadcast(i);
			// try to restart silently as we are killed by the system
		}
	}
	
	public static Intent stopSidebar(Context ctx) {
		isStoppable = true;
		Intent i = new Intent(ctx, SidebarService.class);
		ctx.stopService(i);
		return i;
	}
	
	public void safelyRemoveView(View v) {
		try {
			mWindowManager.removeView(v);
		} catch (Exception e) {
			// so we dont crash
		};
	}
	
	public void changeSidebarPosition(boolean barOnRight) {
		mBarOnRight = barOnRight;
		main_prefs.edit()
				.putString(Common.PREF_KEY_SIDEBAR_POSITION, barOnRight ? "1" : "0")
				.commit();
		mHiddenSidebar.refreshBarSide();
		mShownSidebar.refreshBarSide();
	}
	
	public void addView(View v) {
		final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				v instanceof SidebarHolderView ? WindowManager.LayoutParams.MATCH_PARENT
						: WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
				WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
				WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
				WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
				WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
				WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
				PixelFormat.TRANSLUCENT);
		if (mBarOnRight) {
			params.gravity = Gravity.TOP | Gravity.RIGHT;
		} else {
			params.gravity = Gravity.TOP | Gravity.LEFT;
		}
		// TODO params.alpha
		try{
			mWindowManager.addView(v, params);
		} catch (Exception e) {
			// so we dont crash
		};
	}
	
	public void showBar() {
		mHiddenSidebar.animateView(false);
		mShownSidebar.animateView(true);
	}
	
	public void hideBar() {
		mHiddenSidebar.animateView(true);
		mShownSidebar.animateView(false);
		try {
			mShownSidebar.setMarginFromTop(mMargin);
			mHiddenSidebar.setMarginFromTop(mMargin);
		} catch (Exception e) {
		}
	}
	
	public void killBars() {
		safelyRemoveView(mHiddenSidebar);
		safelyRemoveView(mShownSidebar);
	}
	
	private int mOldMargin = Common.PREF_DEF_SIDEBAR_MARGIN;
	private int mMargin = Common.PREF_DEF_SIDEBAR_MARGIN;
	private float mInitialPosition;
	private float mInitialSwipe;
	public boolean tabTouchEvent(MotionEvent event, boolean currentlyOpen) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mInitialSwipe = event.getRawX();
			mInitialPosition = event.getRawY();
			mOldMargin = mMargin;
			break;
		case MotionEvent.ACTION_MOVE:
			float final_position = event.getRawY();
			if (final_position > getResources().getDisplayMetrics().heightPixels - 20) {
				final_position = getResources().getDisplayMetrics().heightPixels - 20;
			}
			mMargin = (int) (mOldMargin + final_position - mInitialPosition);
			if (mMargin < 0) {
				mMargin = 0;
			} // make sure it doesn't go beyond top of screen
			try {
				mShownSidebar.setMarginFromTop(mMargin);
				mHiddenSidebar.setMarginFromTop(mMargin);
			} catch (Exception e) {
			}
			if (Math.abs(mOldMargin - mMargin) > Util.dp(18, this)) {
				return true;
			} else {
				int sensitivity = Util.dp(mTabSize * 2, this);
				if (mInitialSwipe == 0 || !currentlyOpen) {
				} else if (mInitialSwipe - event.getRawX() > sensitivity) { //right to left
					if (!mBarOnRight) {
						hideBar();
						return true;
					}
				} else if (mInitialSwipe - event.getRawX() < -sensitivity) { //left to right
					if (mBarOnRight) {
						hideBar();
						return true;
					}
				}
			}
			break;
		}
		return false;
	}
}
