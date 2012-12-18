/*
 * @author JPorzuczek
 */
package pl.qbasso.custom;

import pl.qbasso.interfaces.SlidingViewLoadedListener;
import pl.qbasso.smssender.R;
import android.app.Activity;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;

// TODO: Auto-generated Javadoc
/**
 * The Class SlideHelper.
 * 
 * @author Jakub.Porzuczek if status bar is visible id didn't move it to the
 *         right, it only slides window content.
 */
public class SlideHelper {

	/** The m act. */
	private Activity mAct;

	/** The m menu res id. */
	private int mMenuResId;

	/** The m menu view. */
	private View mMenuView;

	/** The Constant MENU_WIDTH. */
	public final static double MENU_WIDTH = 0.8;

	/** The menu size px. */
	public static int menuSizePx;

	/** The content. */
	private RelativeLayout content;

	/** The menu. */
	private LinearLayout menu;

	/** The menu shown. */
	private boolean menuShown = false;

	/** The slide action bar. */
	private boolean slideActionBar;

	/** The m action bar container. */
	private ViewGroup mActionBarContainer;

	/** The sliding view loaded listener. */
	private SlidingViewLoadedListener slidingViewLoadedListener;

	/** The status bar visible. */
	private boolean statusBarVisible = false;
	private int currentContentMargin;

	private static final int ANIMATION_DURATION = 500;
	private static int screenSizePx;

	private static final int GESTURE_THRESHOLD = 5;
	private static final int MODE_DRAG = 0;
	private static final int MODE_OTHER = 1;

	private int touchMode = MODE_OTHER;
	float previousX;
	float startX;
	float diff;

	private Handler h = new Handler() {
		@Override
		public void handleMessage(Message m) {
			afterDragShowAction();
		}

	};

	public boolean isMenuShown() {
		return menuShown;
	}

	public void setMenuShown(boolean mIsMenuShown) {
		this.menuShown = mIsMenuShown;
	}

	/** The m content click listener. */
	private OnClickListener mContentClickListener = new OnClickListener() {
		public void onClick(View v) {
			if (menuShown) {
				if (slideActionBar) {
					hideMenuSlidingActionBar();
				} else {
					hideMenu();
				}
			}
		}
	};

	/**
	 * Instantiates a new slide helper.
	 * 
	 * @param act
	 *            the act
	 * @param menuResId
	 *            the menu res id
	 */
	public SlideHelper(Activity act, int menuResId) {
		this.mAct = act;
		this.mMenuResId = menuResId;
		menu = (LinearLayout) mAct.findViewById(R.id.main_menu);
		content = (RelativeLayout) mAct.findViewById(R.id.main_content);
		mActionBarContainer = (ViewGroup) mAct.findViewById(
				android.R.id.content).getParent();
		Rect r = new Rect();
		mAct.getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
		screenSizePx = r.right - r.left;
		menuSizePx = (int) ((r.right - r.left) * MENU_WIDTH);
		inflateMenu(mMenuResId);
	}

	/**
	 * Inflate menu.
	 * 
	 * @param menuResId
	 *            the menu res id
	 */
	private void inflateMenu(int menuResId) {
		LayoutInflater inflater = (LayoutInflater) mAct
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		mMenuView = inflater.inflate(menuResId, null);

	}

	/**
	 * Show menu.
	 * 
	 * @param slideActionBar
	 *            the slide action bar
	 */
	public void showMenu(boolean slideActionBar) {
		currentContentMargin = menuSizePx;
		if (slideActionBar) {
			showMenuSlidingActionBar();
		} else {
			showMenu();
		}
	}

	/**
	 * Show menu sliding action bar.
	 */
	private void showMenuSlidingActionBar() {
		android.widget.FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams) mActionBarContainer
				.getLayoutParams();
		params.setMargins(menuSizePx, 0, -menuSizePx, 0);
		mActionBarContainer.setLayoutParams(params);
		TranslateAnimation ta = new TranslateAnimation(-menuSizePx, 0, 0, 0);
		ta.setDuration(500);
		mActionBarContainer.startAnimation(ta);
		params = new LayoutParams(menuSizePx, LayoutParams.MATCH_PARENT,
				Gravity.LEFT);
		mMenuView.setLayoutParams(params);
		((FrameLayout) mActionBarContainer.getParent()).addView(mMenuView);
		mMenuView.startAnimation(ta);
		menuShown = true;
		mActionBarContainer.setOnClickListener(mContentClickListener);
	}

	/**
	 * Show menu.
	 */
	private void showMenu() {
		android.widget.RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) content
				.getLayoutParams();
		params.setMargins(menuSizePx, 0, -menuSizePx, 0);
		content.setLayoutParams(params);
		TranslateAnimation ta = new TranslateAnimation(-menuSizePx, 0, 0, 0);
		ta.setDuration(ANIMATION_DURATION);
		content.startAnimation(ta);
		params = (android.widget.RelativeLayout.LayoutParams) menu
				.getLayoutParams();
		params.width = menuSizePx;
		menu.setLayoutParams(params);
		ViewGroup.LayoutParams p = mMenuView.getLayoutParams();
		p = new ViewGroup.LayoutParams(menuSizePx, -1);
		mMenuView.setLayoutParams(p);
		menu.removeAllViews();
		menu.addView(mMenuView);
		menu.startAnimation(ta);
		menuShown = true;
		slidingViewLoadedListener.onViewLoaded();
	}

	/**
	 * Hide menu.
	 * 
	 * @param slideActionBar
	 *            the slide action bar
	 */
	public void hideMenu(boolean slideActionBar) {
		if (slideActionBar) {
			hideMenuSlidingActionBar();
		} else {
			hideMenu();
		}
	}

	/**
	 * Hide menu sliding action bar.
	 */
	private void hideMenuSlidingActionBar() {
		TranslateAnimation ta = new TranslateAnimation(0, -menuSizePx, 0, 0);
		ta.setDuration(500);
		mMenuView.startAnimation(ta);
		((FrameLayout) mActionBarContainer.getParent()).removeView(mMenuView);
		ta = new TranslateAnimation(menuSizePx, 0, 0, 0);
		ta.setDuration(500);
		mActionBarContainer.setOnClickListener(null);
		LayoutParams params = (LayoutParams) mActionBarContainer
				.getLayoutParams();
		params.setMargins(0, 0, 0, 0);
		mActionBarContainer.startAnimation(ta);
		mActionBarContainer.setLayoutParams(params);
		menuShown = false;
	}

	/**
	 * Hide menu.
	 */
	private void hideMenu() {
		TranslateAnimation ta = new TranslateAnimation(0, -menuSizePx, 0, 0);
		ta.setDuration(ANIMATION_DURATION);
		menu.startAnimation(ta);
		ta = new TranslateAnimation(menuSizePx, 0, 0, 0);
		ta.setDuration(ANIMATION_DURATION);
		content.startAnimation(ta);
		android.widget.RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) content
				.getLayoutParams();
		params.setMargins(0, 0, 0, 0);
		content.setLayoutParams(params);
		menuShown = false;
	}

	public void setMenuAdapter(ListAdapter a) {
		// menuList.setAdapter(a);
		// mMenuView.setAdapter(new ArrayAdapter<String>(mAct,
		// android.R.layout.simple_list_item_1, new String[] { "Item 1",
		// "Item 2", "Item 3" }));
	}

	public void setMenuItemOnClickListener(OnItemClickListener listener) {
		// mMenuView.setOnItemClickListener(listener);
	}

	public SlidingViewLoadedListener getSlidingViewLoadedListener() {
		return slidingViewLoadedListener;
	}

	public void setSlidingViewLoadedListener(
			SlidingViewLoadedListener slidingViewLoadedListener) {
		this.slidingViewLoadedListener = slidingViewLoadedListener;
	}

	public boolean isStatusBarVisible() {
		return statusBarVisible;
	}

	public void setStatusBarVisible(boolean statusBarVisible) {
		this.statusBarVisible = statusBarVisible;
	}

	public boolean handleTouchEvent(MotionEvent ev) {
		if (menuShown
				&& ((ev.getX() > SlideHelper.menuSizePx && ev.getHistorySize() == 0) || (touchMode==MODE_DRAG))) {
			int tempDiff;
			if (ev.getAction() == MotionEvent.ACTION_DOWN
					&& ev.getX() > SlideHelper.menuSizePx && touchMode==MODE_OTHER) {
				touchMode = MODE_DRAG;
				startX = previousX = ev.getX();
			} else if (touchMode == MODE_DRAG
					&& ev.getAction() == MotionEvent.ACTION_UP) {
				tempDiff = (int) Math.abs(startX - ev.getX());
				if (tempDiff > GESTURE_THRESHOLD) {
					if (previousX < screenSizePx / 2) {
						hideAfterDrag();
					} else {
						showAfterDrag();
					}
				} else {
					hideMenu(false);
				}
				touchMode = MODE_OTHER;
			} else if (touchMode == MODE_DRAG) {
				diff = previousX - ev.getX();
				previousX = ev.getX();
				pullContent(-diff);
			} else if (MotionEvent.ACTION_UP == ev.getAction()
					&& ev.getX() == previousX) {
				hideMenu(false);
			}
			return true;
		}
		return false;
	}

	public void pullContent(float direction) {
		if (currentContentMargin + direction < menuSizePx) {
			currentContentMargin += (int) direction;
			content.bringToFront();
			RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) content
					.getLayoutParams();
			params.setMargins(currentContentMargin, 0, -currentContentMargin, 0);
			content.setLayoutParams(params);
		}
	}

	public void hideAfterDrag() {
		TranslateAnimation ta = new TranslateAnimation(currentContentMargin, 0,
				0, 0);
		int animationDuration = (int) (((float) currentContentMargin / (float) screenSizePx) * ANIMATION_DURATION);
		if (animationDuration<0) {
			animationDuration = 100;
		}
		ta.setDuration(animationDuration);
		content.startAnimation(ta);
		RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) content
				.getLayoutParams();
		params.setMargins(0, 0, 0, 0);
		content.setLayoutParams(params);
		menuShown = false;
	}

	public void showAfterDrag() {
		int duration = (int) (((float) currentContentMargin / (float) screenSizePx) * ANIMATION_DURATION);
		TranslateAnimation ta = new TranslateAnimation(0, menuSizePx
				- currentContentMargin, 0, 0);
		ta.setDuration(duration);
		content.startAnimation(ta);
		h.sendEmptyMessageDelayed(0, duration);
	}

	private void afterDragShowAction() {
		RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) content
				.getLayoutParams();
		params.setMargins(menuSizePx, 0, -menuSizePx, 0);
		content.setLayoutParams(params);
		currentContentMargin = menuSizePx;
	}

}
