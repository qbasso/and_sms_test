/*
 * @author JPorzuczek
 */
package pl.qbasso.custom;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.qbasso.interfaces.ItemSeenListener;
import pl.qbasso.models.SmsModel;
import pl.qbasso.smssender.R;
import android.content.Context;
import android.os.PatternMatcher;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.text.util.Linkify;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

// TODO: Auto-generated Javadoc
/**
 * The Class SmsAdapter.
 */
public class SmsAdapter extends ArrayAdapter<SmsModel> {

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
	
	/** The context. */
	private Context context;

	/**
	 * The Class LeftItemHolder.
	 */
	private static class LeftItemHolder {
		
		/** The background. */
		LinearLayout background;
		
		/** The msg body. */
		TextView msgBody;
		
		/** The msg date. */
		TextView msgDate;
	}

	/**
	 * The Class RightItemHolder.
	 */
	private static class RightItemHolder {
		
		/** The background. */
		LinearLayout background;
		
		/** The msg body. */
		TextView msgBody;
		
		/** The msg date. */
		TextView msgDate;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	/* (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getItem(int)
	 */
	@Override
	public SmsModel getItem(int position) {
		return items.get(position);
	}

	/* (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SmsModel item = items.get(position);
		if (item.getRead() == SmsModel.MESSAGE_NOT_READ) {
			onItemSeenListener.onItemSeen(id, item.getId());
		}
		switch (getItemViewType(position)) {
		case LEFT:
			View view = convertView;
			if (convertView == null) {
				view = inflater.inflate(leftItemResource, null);
				initializeLeftHolder(view);
				view.setTag(leftHolder);
			} else {
				leftHolder = (LeftItemHolder) view.getTag();
			}
			leftHolder.msgBody.setText(item.getBody());
			leftHolder.msgBody.setAutoLinkMask(Linkify.ALL);
			leftHolder.msgDate.setText(Utils.formatDate(item.getDate()));
			if (item.getStatus() == SmsModel.STATUS_WAITING) {
				leftHolder.background.startAnimation(AnimationUtils
						.loadAnimation(context, R.anim.shake));
			} else {
				leftHolder.background.setAnimation(null);
			}
			return view;
		case RIGHT:
			View v = convertView;
			if (convertView == null) {
				v = inflater.inflate(rightItemResource, null);
				initializeRightHolder(v);
				v.setTag(rightHolder);
			} else {
				rightHolder = (RightItemHolder) v.getTag();
			}
			rightHolder.msgBody.setText(Html.fromHtml(getContext().getString(
					R.string.message_body, displayName, item.getBody())));
			rightHolder.msgBody.setAutoLinkMask(Linkify.PHONE_NUMBERS);
			rightHolder.msgDate.setText(Utils.formatDate(item.getDate()));
			if (item.getStatus() == SmsModel.STATUS_WAITING) {
				rightHolder.background.startAnimation(AnimationUtils
						.loadAnimation(context, R.anim.shake));
			} else {
				rightHolder.background.setAnimation(null);
			}
			return v;
		}
		return null;
	}

	/**
	 * Initialize left holder.
	 *
	 * @param view the view
	 */
	private void initializeLeftHolder(View view) {
		leftHolder = new LeftItemHolder();
		leftHolder.msgBody = (TextView) view.findViewById(R.id.sms_item_body);
		leftHolder.msgDate = (TextView) view.findViewById(R.id.sms_item_date);
		leftHolder.background = (LinearLayout) view
				.findViewById(R.id.left_item_background);
	}

	/**
	 * Initialize right holder.
	 *
	 * @param view the view
	 */
	private void initializeRightHolder(View view) {
		rightHolder = new RightItemHolder();
		rightHolder.msgBody = (TextView) view.findViewById(R.id.sms_item_body);
		rightHolder.msgDate = (TextView) view.findViewById(R.id.sms_item_date);
		rightHolder.background = (LinearLayout) view
				.findViewById(R.id.right_item_background);
	}

	/* (non-Javadoc)
	 * @see android.widget.BaseAdapter#getItemViewType(int)
	 */
	@Override
	public int getItemViewType(int position) {
		return items.get(position).getSmsType() == SmsModel.MESSAGE_TYPE_SENT ? LEFT
				: RIGHT;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	/**
	 * Instantiates a new sms adapter.
	 *
	 * @param context the context
	 * @param leftItemResource the left item resource
	 * @param rightItemResource the right item resource
	 * @param objects the objects
	 * @param displayName the display name
	 * @param position the position
	 */
	public SmsAdapter(Context context, int leftItemResource,
			int rightItemResource, List<SmsModel> objects, String displayName,
			int position) {
		super(context, leftItemResource, objects);
		inflater = LayoutInflater.from(context);
		this.leftItemResource = leftItemResource;
		this.rightItemResource = rightItemResource;
		items = objects;
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
}
