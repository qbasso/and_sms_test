package pl.qbasso.custom;

import java.util.List;

import pl.qbasso.interfaces.ItemSeenListener;
import pl.qbasso.models.SmsModel;
import pl.qbasso.smssender.R;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SmsAdapter extends ArrayAdapter<SmsModel> {

	private List<SmsModel> items;
	private LayoutInflater inflater;
	private int leftItemResource;
	private int rightItemResource;
	private static final int LEFT = 0;
	private static final int RIGHT = 1;
	private LeftItemHolder leftHolder;
	private RightItemHolder rightHolder;
	private String displayName;
	private ItemSeenListener onItemSeenListener;
	private int id;
	private Context context;

	private static class LeftItemHolder {
		LinearLayout background;
		TextView msgBody;
		TextView msgDate;
	}

	private static class RightItemHolder {
		LinearLayout background;
		TextView msgBody;
		TextView msgDate;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public SmsModel getItem(int position) {
		return items.get(position);
	}

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
			leftHolder.msgDate.setText(Utils.formatDate(item.getDate()));
			if (item.getStatus() == SmsModel.STATUS_WAITING) {
				leftHolder.background.startAnimation(AnimationUtils
						.loadAnimation(context, R.anim.shake));
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
			rightHolder.msgDate.setText(Utils.formatDate(item.getDate()));
			if (item.getStatus() == SmsModel.STATUS_WAITING) {
				rightHolder.background.startAnimation(AnimationUtils
						.loadAnimation(context, R.anim.shake));
			}
			return v;
		}
		return null;
	}

	private void initializeLeftHolder(View view) {
		leftHolder = new LeftItemHolder();
		leftHolder.msgBody = (TextView) view.findViewById(R.id.sms_item_body);
		leftHolder.msgDate = (TextView) view.findViewById(R.id.sms_item_date);
		leftHolder.background = (LinearLayout) view
				.findViewById(R.id.left_item_background);
	}

	private void initializeRightHolder(View view) {
		rightHolder = new RightItemHolder();
		rightHolder.msgBody = (TextView) view.findViewById(R.id.sms_item_body);
		rightHolder.msgDate = (TextView) view.findViewById(R.id.sms_item_date);
		rightHolder.background = (LinearLayout) view
				.findViewById(R.id.right_item_background);
	}

	@Override
	public int getItemViewType(int position) {
		return items.get(position).getSmsType() == SmsModel.MESSAGE_TYPE_SENT ? LEFT
				: RIGHT;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

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
