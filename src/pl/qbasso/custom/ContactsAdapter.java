package pl.qbasso.custom;

import java.util.ArrayList;
import java.util.List;

import pl.qbasso.models.ContactModel;
import pl.qbasso.smssender.R;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;


public class ContactsAdapter extends ArrayAdapter<ContactModel> implements
		Filterable {

	public ContactsAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		inflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		resolver = context.getContentResolver();
		items = new ArrayList<ContactModel>();
		filteredItems = new ArrayList<ContactModel>();
		resourceId = textViewResourceId;
	}

	private LayoutInflater inflater;
	private List<ContactModel> items;
	private List<ContactModel> filteredItems;
	private int resourceId;
	private Filter filter;
	private ContentResolver resolver;
	private String currentlySelectedDisplayName;
	private int lastConstraintLength = 0;
	private static final int ACTION_RELOAD = 0;
	private static final int ACTION_RESTRICT = 1;

	private void getContacts(CharSequence constraint, int action) {
		switch (action) {
		case ACTION_RELOAD:
			reloadItems(constraint);
			break;
		case ACTION_RESTRICT:
			for (ContactModel item : items) {
				if (item.getDisplayName().toLowerCase()
						.contains(constraint.toString().toLowerCase())
						|| item.getPhoneNumber().toLowerCase()
								.contains(constraint.toString().toLowerCase())) {
					filteredItems.add(item);
				}
			}
		default:
			break;
		}
	}

	private void reloadItems(CharSequence constraint) {
		Cursor c;
		items.clear();
		if (!TextUtils.isDigitsOnly(constraint)) {
			c = resolver.query(Phone.CONTENT_URI, new String[] {
					Phone.DISPLAY_NAME, Phone.NUMBER }, Phone.DISPLAY_NAME
					+ " like ?",
					new String[] { String.format("%%%s%%", constraint) }, null);
			if (c != null) {
				while (c.moveToNext()) {
					items.add(new ContactModel(c.getString(0), c.getString(1)));
				}
			}
		} else {
			c = resolver.query(Phone.CONTENT_URI, new String[] {
					Phone.DISPLAY_NAME, Phone.NUMBER }, Phone.NUMBER
					+ " like ?",
					new String[] { String.format("%%%s%%", constraint) }, null);
			if (c != null) {
				while (c.moveToNext()) {
					items.add(new ContactModel(c.getString(0), c.getString(1)));
				}
			}
		}
		filteredItems.addAll(items);
		c.close();
	}

	@Override
	public Filter getFilter() {
		if (filter == null) {
			filter = new Filter() {
				@Override
				protected FilterResults performFiltering(CharSequence constraint) {
					FilterResults result = new FilterResults();
					filteredItems.clear();
					if (lastConstraintLength == 0) {
						getContacts(constraint, ACTION_RELOAD);
					} else {
						getContacts(constraint, ACTION_RESTRICT);
					}
					lastConstraintLength = constraint.length();
					result.values = filteredItems;
					result.count = filteredItems.size();
					return result;
				}

				@Override
				protected void publishResults(CharSequence constraint,
						FilterResults results) {
					notifyDataSetChanged();
				}

				@Override
				public CharSequence convertResultToString(Object resultValue) {
					currentlySelectedDisplayName = ((ContactModel) resultValue).getDisplayName(); 
					return ((ContactModel) resultValue).getPhoneNumber();

				}
			};
		}
		return filter;
	}

	@Override
	public int getCount() {
		return filteredItems.size();
	}

	@Override
	public ContactModel getItem(int position) {
		return filteredItems.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		ContactModel m = filteredItems.get(position);
		if (convertView == null) {
			v = inflater.inflate(resourceId, null);
		}
		if (m != null) {
			((TextView) v.findViewById(R.id.contact_name)).setText(m
					.getDisplayName());
			((TextView) v.findViewById(R.id.contact_number)).setText(m
					.getPhoneNumber());
		}
		return v;
	}

	public String getCurrentlySelectedDisplayName() {
		return currentlySelectedDisplayName;
	}

	public void setCurrentlySelectedDisplayName(String currentlySelectedDisplayName) {
		this.currentlySelectedDisplayName = currentlySelectedDisplayName;
	}

}
