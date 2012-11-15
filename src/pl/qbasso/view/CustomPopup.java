package pl.qbasso.view;

import java.util.ArrayList;
import java.util.List;

import pl.qbasso.interfaces.ActionClickListener;
import pl.qbasso.models.ActionModel;
import pl.qbasso.smssender.R;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

/**
 * @author jakub.porzuczek
 * 
 */
public class CustomPopup {

	/**
	 * 
	 */
	private View mAnchor;
	/**
	 * 
	 */
	private Context mCtx;
	/**
	 * 
	 */
	private PopupWindow mWindow;
	/**
	 * 
	 */
	private LayoutInflater mInflater;
	/**
	 * 
	 */
	private WindowManager mWindowManager;
	/**
	 * 
	 */
	private View mPopupRootView;
	/**
	 * 
	 */
	private ViewGroup mActionsView;
	/**
	 * 
	 */
	private ActionClickListener mActionClickListener;
	/**
	 * 
	 */
	private List<ActionModel> mActionList;

	private OnTouchListener mTouchInterceptor = new OnTouchListener() {

		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
				mWindow.dismiss();
				return true;
			}
			return false;
		}

	};

	/**
	 * @param c
	 * @param rootView
	 */
	public CustomPopup(Context c, View rootView) {
		mAnchor = rootView;
		mCtx = c;
		mActionList = new ArrayList<ActionModel>();
		mInflater = LayoutInflater.from(c);
		mWindowManager = (WindowManager) c
				.getSystemService(Context.WINDOW_SERVICE);
		initPopup(c);
	}

	private void initPopup(Context c) {
		mWindow = new PopupWindow(c);
		mWindow.setBackgroundDrawable(new BitmapDrawable());
		mWindow.setTouchable(true);
		mWindow.setOutsideTouchable(true);
		mWindow.setFocusable(true);
		mWindow.setTouchInterceptor(mTouchInterceptor);
	}

	public void setOnDismissListener(OnDismissListener l) {
		this.mWindow.setOnDismissListener(l);
	}

	/**
	 * @param id
	 */
	public void setContentView(int id) {
		mPopupRootView = mInflater.inflate(id, null);
		mPopupRootView.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		mActionsView = (ViewGroup) mPopupRootView
				.findViewById(R.id.action_buttons);
		mWindow.setContentView(mPopupRootView);
		// this.mWindow.setBackgroundDrawable(mCtx.getResources().getDrawable(
		// R.drawable.rounded_bg));
	}

	/**
	 * @param a
	 */
	public void addAction(ActionModel a) {
		final int actionId = a.getActionId();
		final Bundle b = a.getActionData();
		mActionList.add(a);
		View v = mInflater.inflate(R.layout.action_item, null);
		if (a.getResId() > 0) {
			((TextView) v.findViewById(R.id.action_title))
					.setCompoundDrawablesWithIntrinsicBounds(0, a.getResId(),
							0, 0);
		}
		((TextView) v.findViewById(R.id.action_title)).setText(a.getTitle());
		v.setClickable(true);
		v.setFocusable(true);
		v.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				mActionClickListener.onItemClick(actionId, b);
				mWindow.dismiss();
			}
		});
		mActionsView.addView(v);
	}

	/**
	 * 
	 */
	public void show() {
		boolean bottom = true;
		int[] location = new int[2];
		mAnchor.getLocationOnScreen(location);
		int windowWidth = mWindowManager.getDefaultDisplay().getWidth();
		int windowHeight = mWindowManager.getDefaultDisplay().getHeight();
		mPopupRootView.measure(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		int popupHeight = mPopupRootView.getMeasuredHeight();
		int x = windowWidth / 10;
		int y = location[1] + mAnchor.getMeasuredHeight();
		if (y + popupHeight > windowHeight) {
			y = location[1] - popupHeight;
			bottom = false;
		}
		mWindow.setWidth(LayoutParams.WRAP_CONTENT);
		mWindow.setHeight(LayoutParams.WRAP_CONTENT);
		if (bottom) {
			mWindow.setAnimationStyle(R.style.bottom_anim);
		} else {
			mWindow.setAnimationStyle(R.style.top_anim);
		}
		mWindow.showAtLocation(mAnchor, Gravity.NO_GRAVITY, x, y);
	}

	/**
	 * @param mActionClickListener
	 */
	public void setmActionClickListener(ActionClickListener mActionClickListener) {
		this.mActionClickListener = mActionClickListener;
	}

}
