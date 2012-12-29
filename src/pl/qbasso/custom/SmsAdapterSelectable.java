package pl.qbasso.custom;

import java.util.List;

import pl.qbasso.interfaces.ItemSeenListener;
import pl.qbasso.models.SmsModel;
import pl.qbasso.smssender.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SmsAdapterSelectable extends ArrayAdapter<SmsModel> {

	/** The items. */
	private List<SmsModel> items;

	/** The inflater. */
	private LayoutInflater inflater;

	/** The left item resource. */
	private int leftItemResource;

	/** The right item resource. */
	private int rightItemResource;

	/** The Constant LEFT. */
	private static final int LEFT = 0;

	/** The Constant RIGHT. */
	private static final int RIGHT = 1;

	/** The left holder. */
	private LeftItemHolder leftHolder;

	/** The right holder. */
	private RightItemHolder rightHolder;

	/** The display name. */
	private String displayName;

	/** The on item seen listener. */
	private ItemSeenListener onItemSeenListener;

	/** The id. */
	private int id;

	private Handler adapterHandler = new Handler();

	private boolean[] checked;

	private Bitmap[] screenShots;

	/** The context. */
	private Context context;

	private static class Holder {
		/** The background. */
		protected LinearLayout background;

		/** The msg body. */
		protected LinkEnabledTextView msgBody;

		/** The msg date. */
		protected TextView msgDate;

		protected ImageView viewScreenshot;

	}

	/**
	 * The Class LeftItemHolder.
	 */
	private static class LeftItemHolder extends Holder {
	}

	/**
	 * The Class RightItemHolder.
	 */
	private static class RightItemHolder extends Holder {
	}

	@Override
	public int getCount() {
		return items.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ArrayAdapter#getItem(int)
	 */
	@Override
	public SmsModel getItem(int position) {
		return items.get(position);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View,
	 * android.view.ViewGroup)
	 */
	@Override
	public View getView(final int position, final View convertView,
			final ViewGroup parent) {
		SmsModel item = items.get(position);
		View view = convertView;
		if (item.getRead() == SmsModel.MESSAGE_NOT_READ) {
			onItemSeenListener.onItemSeen(id, item.getId());
		}
		switch (getItemViewType(position)) {
		case LEFT:
			if (convertView == null) {
				view = inflater.inflate(leftItemResource, null);
				initializeLeftHolder(view);
				view.setTag(leftHolder);
			} else {
				leftHolder = (LeftItemHolder) view.getTag();
			}
			leftHolder.msgBody.setText(item.getBody());
			// this enables custom handing of touch events in
			// @LinkEnabledTextView
			leftHolder.msgDate.setText(Utils.formatDate(item.getDate()));
			if (checked[position] && screenShots[position] != null) {
				leftHolder.background.setVisibility(View.GONE);
				leftHolder.viewScreenshot.setImageBitmap(screenShots[position]);
				leftHolder.viewScreenshot.setVisibility(View.VISIBLE);
				leftHolder.viewScreenshot.setAlpha(128);
			} else {
				leftHolder.viewScreenshot.setVisibility(View.GONE);
				leftHolder.background.setVisibility(View.VISIBLE);
			}
			break;
		case RIGHT:
			if (convertView == null) {
				view = inflater.inflate(rightItemResource, null);
				initializeRightHolder(view);
				view.setTag(rightHolder);
			} else {
				rightHolder = (RightItemHolder) view.getTag();
			}
			rightHolder.msgBody.setText(Html.fromHtml(getContext().getString(
					R.string.message_body, displayName, item.getBody())));
			rightHolder.msgDate.setText(Utils.formatDate(item.getDate()));
			if (checked[position] && screenShots[position] != null) {
				rightHolder.background.setVisibility(View.GONE);
				rightHolder.viewScreenshot
						.setImageBitmap(screenShots[position]);
				rightHolder.viewScreenshot.setVisibility(View.VISIBLE);
				rightHolder.viewScreenshot.setAlpha(128);
			} else {
				rightHolder.viewScreenshot.setVisibility(View.GONE);
				rightHolder.background.setVisibility(View.VISIBLE);
			}
			break;
		}

		view.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Animation a = AnimationUtils.loadAnimation(context,
						R.anim.shrink_sms);
				if (!checked[position]) {
					screenShots[position] = getViewScreenshot(v);
					// v.setAnimation(a);
					// hideViews((ViewGroup) v, View.GONE);
					// ((ViewGroup) v).addView(screenShots[position], 0);
					checked[position] = true;
				} else {
					// hideViews((ViewGroup) v, View.VISIBLE);
					// ((ViewGroup) v).removeViewAt(0);
					checked[position] = false;
					screenShots[position] = null;
				}
				// final View convertView = v;
				// adapterHandler.postDelayed(new Runnable() {
				//
				// public void run() {
				getView(position, v, parent);
				//
				// }
				// }, a.getDuration());
			}
		});
		return view;
	}

	private Bitmap getViewScreenshot(View view) {
		view.setDrawingCacheEnabled(true);
		Bitmap b = Bitmap.createScaledBitmap(view.getDrawingCache(),
				(int) (0.9 * view.getMeasuredWidth()),
				(int) (0.9 * view.getMeasuredHeight()), false);
		view.setDrawingCacheEnabled(false);
		return b;
	}

	private void hideViews(ViewGroup v, int visibility) {
		int childCount = v.getChildCount();
		for (int i = 0; i < childCount; i++) {
			View temp = v.getChildAt(i);
			if (temp instanceof ViewGroup) {
				hideViews((ViewGroup) temp, visibility);
			}
			if ((temp instanceof ImageView && visibility == View.VISIBLE)) {
				;
			} else {
				temp.setVisibility(visibility);
			}
		}
	}

	/**
	 * Initialize left holder.
	 * 
	 * @param view
	 *            the view
	 */
	private void initializeLeftHolder(View view) {
		leftHolder = new LeftItemHolder();
		leftHolder.msgBody = (LinkEnabledTextView) view
				.findViewById(R.id.sms_item_body);
		leftHolder.msgDate = (TextView) view.findViewById(R.id.sms_item_date);
		leftHolder.background = (LinearLayout) view
				.findViewById(R.id.left_item_background);
		leftHolder.viewScreenshot = (ImageView) view
				.findViewById(R.id.screenShot);
	}

	/**
	 * Initialize right holder.
	 * 
	 * @param view
	 *            the view
	 */
	private void initializeRightHolder(View view) {
		rightHolder = new RightItemHolder();
		rightHolder.msgBody = (LinkEnabledTextView) view
				.findViewById(R.id.sms_item_body);

		rightHolder.msgDate = (TextView) view.findViewById(R.id.sms_item_date);
		rightHolder.background = (LinearLayout) view
				.findViewById(R.id.right_item_background);
		rightHolder.viewScreenshot = (ImageView) view
				.findViewById(R.id.screenShot);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.BaseAdapter#getItemViewType(int)
	 */
	@Override
	public int getItemViewType(int position) {
		// Log.i("SmsAdapter", String.format("View position: %d", position));
		int type = items.get(position).getSmsType();
		return type == SmsModel.MESSAGE_TYPE_SENT
				|| type == SmsModel.MESSAGE_TYPE_QUEUED
				|| type == SmsModel.MESSAGE_TYPE_FAILED ? LEFT : RIGHT;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	/**
	 * Instantiates a new sms adapter.
	 * 
	 * @param context
	 *            the context
	 * @param leftItemResource
	 *            the left item resource
	 * @param rightItemResource
	 *            the right item resource
	 * @param objects
	 *            the objects
	 * @param displayName
	 *            the display name
	 * @param position
	 *            the position
	 */
	public SmsAdapterSelectable(Context context, int leftItemResource,
			int rightItemResource, List<SmsModel> objects, String displayName,
			int position) {
		super(context, leftItemResource, objects);
		inflater = LayoutInflater.from(context);
		this.leftItemResource = leftItemResource;
		this.rightItemResource = rightItemResource;
		items = objects;
		checked = new boolean[items.size()];
		screenShots = new Bitmap[items.size()];
		for (int i = 0; i < items.size(); i++) {
			checked[i] = false;
			screenShots[i] = null;
		}
		this.displayName = displayName;
		this.id = position;
		this.context = context;
	}

	public void setOnItemSeenListener(ItemSeenListener listener) {
		this.onItemSeenListener = listener;
	}

	public void setItems(List<SmsModel> items) {
		this.items = items;
	}

	public boolean[] getChecked() {
		return checked;
	}
}
