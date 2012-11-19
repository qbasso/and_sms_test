package pl.qbasso.custom;

import java.util.List;

import pl.qbasso.models.ConversationModel;
import pl.qbasso.smssender.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ConversationAdapter extends ArrayAdapter<ConversationModel> {

	private int resourceId;
	private List<ConversationModel> items;
	private LayoutInflater inflater;
	private ItemViewHolder holder;
	private Context ctx;

	private static class ItemViewHolder {
		TextView contactName;
		TextView messageSnippet;
		LinearLayout background;
		TextView unreadCount;
		RelativeLayout unreadIcon;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public ConversationModel getItem(int position) {
		return items.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ConversationModel item;
		if (convertView == null) {
			view = inflater.inflate(resourceId, null);
			initializeHolder(view);
			view.setTag(holder);
		} else {
			holder = (ItemViewHolder) view.getTag();
		}
		item = items.get(position);
		if (item != null) {
			holder.contactName.setText(ctx.getString(R.string.contact_name,
					item.getDisplayName(), item.getCount()));
			holder.messageSnippet.setText(item.isDraft() ? "Robocza: " + item.getSnippet() : item.getSnippet());
			if (item.getUnread() > 0) {
				holder.unreadCount.setText(String.valueOf(item.getUnread()));
				holder.unreadIcon.setVisibility(View.VISIBLE);
			} else {
				holder.unreadIcon.setVisibility(View.GONE);
			}
			if (position == 0) {
				holder.background
						.setBackgroundResource(R.drawable.item_rounded_top_selector);
			} else if (position == items.size() - 1) {
				holder.background
						.setBackgroundResource(R.drawable.item_rounded_bottom_selector);
			} else {
				holder.background
						.setBackgroundResource(R.drawable.item_normal_selector);
			}
		}
		return view;
	}

	private void initializeHolder(View view) {
		holder = new ItemViewHolder();
		holder.contactName = (TextView) view
				.findViewById(R.id.thread_item_contact_name);
		holder.messageSnippet = (TextView) view
				.findViewById(R.id.thread_item_last_message);
		holder.background = (LinearLayout) view
				.findViewById(R.id.thread_item_background);
		holder.unreadCount = (TextView) view
				.findViewById(R.id.thread_item_unread_count);
		holder.unreadIcon = (RelativeLayout) view
				.findViewById(R.id.thread_item_unread_icon);
	}

	public ConversationAdapter(Context context, int textViewResourceId,
			List<ConversationModel> objects) {
		super(context, textViewResourceId, objects);
		this.resourceId = textViewResourceId;
		this.items = objects;
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.ctx = context;
	}

}
