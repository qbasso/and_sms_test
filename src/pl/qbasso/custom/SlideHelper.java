package pl.qbasso.custom;

import pl.qbasso.interfaces.SlidingViewLoadedListener;
import pl.qbasso.smssender.R;
import android.app.Activity;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ListAdapter;


/**
 * @author Jakub.Porzuczek
 *	if status bar is visible id didn't move it to the right, it only slides window content.
 */
public class SlideHelper {
	private Activity mAct;
	private int mMenuResId;
	private View mMenuView;
	private final static double MENU_WIDTH = 0.8;
	private static int menuSizePx;
	private LinearLayout content;
	private LinearLayout menu;
	private boolean menuShown = false;
	private boolean slideActionBar;
	private ViewGroup mActionBarContainer;
	private SlidingViewLoadedListener slidingViewLoadedListener;
	private boolean statusBarVisible = false;

	public boolean isMenuShown() {
		return menuShown;
	}

	public void setMenuShown(boolean mIsMenuShown) {
		this.menuShown = mIsMenuShown;
	}

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

	public SlideHelper(Activity act, int menuResId) {
		this.mAct = act;
		this.mMenuResId = menuResId;
		menu = (LinearLayout) mAct.findViewById(R.id.main_menu);
		content = (LinearLayout) mAct.findViewById(R.id.main_content);
		mActionBarContainer = (ViewGroup) mAct.findViewById(android.R.id.content)
				.getParent();
		Rect r = new Rect();
		mAct.getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
		menuSizePx = (int) ((r.right - r.left) * MENU_WIDTH);
		inflateMenu(mMenuResId);
	}

	private void inflateMenu(int menuResId) {
		LayoutInflater inflater = (LayoutInflater) mAct
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		mMenuView = inflater.inflate(menuResId, null);

	}

	public void showMenu(boolean slideActionBar) {
		if (slideActionBar) {
			showMenuSlidingActionBar();
		} else {
			showMenu();
		}
	}

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

	private void showMenu() {
		android.widget.RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) content
				.getLayoutParams();
		params.setMargins(menuSizePx, 0, -menuSizePx, 0);
		content.setLayoutParams(params);
		TranslateAnimation ta = new TranslateAnimation(-menuSizePx, 0, 0, 0);
		ta.setDuration(500);
		content.startAnimation(ta);
		
		params = (android.widget.RelativeLayout.LayoutParams) menu
				.getLayoutParams();
		params.width = menuSizePx;
		menu.setLayoutParams(params);
		ViewGroup.LayoutParams p = mMenuView.getLayoutParams();
		p = new ViewGroup.LayoutParams(menuSizePx, -1);
		mMenuView.setLayoutParams(p);
		menu.addView(mMenuView);		
		menu.startAnimation(ta);
		menuShown = true;
		content.setOnClickListener(mContentClickListener);
		slidingViewLoadedListener.onViewLoaded();
	}

	public void hideMenu(boolean slideActionBar) {
		if (slideActionBar) {
			hideMenuSlidingActionBar();
		} else {
			hideMenu();
		}
	}

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

	private void hideMenu() {
		TranslateAnimation ta = new TranslateAnimation(0, -menuSizePx, 0, 0);
		ta.setDuration(500);
		android.widget.RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) menu
				.getLayoutParams();
		params.width = 0;
		menu.startAnimation(ta);

		menu.setLayoutParams(params);
		ta = new TranslateAnimation(menuSizePx, 0, 0, 0);
		ta.setDuration(500);
		content.setOnClickListener(null);
		content.startAnimation(ta);
		params = (android.widget.RelativeLayout.LayoutParams) content
				.getLayoutParams();
		params.setMargins(0, 0, 0, 0);
		content.setLayoutParams(params);
		menu.removeView(mMenuView);
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

}
