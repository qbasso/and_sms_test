/*
 * @author JPorzuczek
 */
package pl.qbasso.custom;

import java.util.List;

import pl.qbasso.models.ConversationModel;
import pl.qbasso.smssender.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

// TODO: Auto-generated Javadoc
/**
 * The Class ConversationAdapter.
 */
public class ConversationAdapter extends ArrayAdapter<ConversationModel> {

	/** The resource id. */
	private int resourceId;

	/** The items. */
	private List<ConversationModel> items;

	/** The inflater. */
	private LayoutInflater inflater;

	/** The holder. */
	private ItemViewHolder holder;

	/** The ctx. */
	private Context ctx;

	private boolean checked[];

	private boolean mCbEnabled;

	/**
	 * The Class ItemViewHolder.
	 */
	private static class ItemViewHolder {

		/** The contact name. */
		TextView contactName;

		/** The message snippet. */
		TextView messageSnippet;

		/** The background. */
		LinearLayout background;

		/** The unread count. */
		TextView unreadCount;

		TextView date;

		/** The unread icon. */
		RelativeLayout unreadIcon;

		CheckBox cb;
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
	public ConversationModel getItem(int position) {
		return items.get(position);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View,
	 * android.view.ViewGroup)
	 */
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
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
			holder.date.setText(Utils.formatDate(item.getLastModified()));
			holder.messageSnippet.setText(item.isDraft() ? "Robocza: "
					+ item.getSnippet() : item.getSnippet());
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
			if (mCbEnabled) {
				holder.cb
						.setOnCheckedChangeListener(new OnCheckedChangeListener() {
							public void onCheckedChanged(
									CompoundButton buttonView, boolean isChecked) {
								checked[position] = isChecked;
							}
						});
				if (checked[position]) {
					holder.cb.setChecked(true);
				} else {
					holder.cb.setChecked(false);
				}
			} else {
				if (item.getUnread() > 0) {
					holder.unreadCount
							.setText(String.valueOf(item.getUnread()));
					holder.unreadIcon.setVisibility(View.VISIBLE);
				} else {
					holder.unreadIcon.setVisibility(View.GONE);
				}
			}
		}
		return view;
	}

	/**
	 * Initialize holder.
	 * 
	 * @param view
	 *            the view
	 */
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
		holder.date = (TextView) view.findViewById(R.id.thread_item_date);
		if (mCbEnabled) {
			holder.cb = (CheckBox) view.findViewById(R.id.checkbox);
		}
	}

	/**
	 * Instantiates a new conversation adapter.
	 * 
	 * @param context
	 *            the context
	 * @param textViewResourceId
	 *            the text view resource id
	 * @param objects
	 *            the objects
	 */
	public ConversationAdapter(Context context, int textViewResourceId,
			List<ConversationModel> objects, boolean selectable) {
		super(context, textViewResourceId, objects);
		this.resourceId = textViewResourceId;
		this.items = objects;
		this.mCbEnabled = selectable;
		if (selectable) {
			this.checked = new boolean[objects.size()];
			for (int i = 0; i < checked.length; i++) {
				checked[i] = false;
			}
		}
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.ctx = context;
	}

	public boolean[] getChecked() {
		return checked;
	}

}
