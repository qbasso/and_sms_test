/*
 * @author JPorzuczek
 */
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

// TODO: Auto-generated Javadoc
/**
 * The Class ContactsAdapter.
 */
public class ContactsAdapter extends ArrayAdapter<ContactModel> implements
		Filterable {

	private static final String[] CHARACTERS_TO_REPLACE = { "π", "Ê", "Í", "Û",
			"≥", "ü", "ø", "•", "∆", " ", "”", "£", "è", "Ø" };
	private static final CharSequence[] REPLACEMENTS = { "a", "c", "e", "o",
			"l", "z", "z", "A", "C", "E", "O", "L", "Z", "Z" };

	/**
	 * Instantiates a new contacts adapter.
	 * 
	 * @param context
	 *            the context
	 * @param textViewResourceId
	 *            the text view resource id
	 */
	public ContactsAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		inflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		resolver = context.getContentResolver();
		items = new ArrayList<ContactModel>();
		filteredItems = new ArrayList<ContactModel>();
		resourceId = textViewResourceId;
	}

	/** The inflater. */
	private LayoutInflater inflater;

	/** The items. */
	private List<ContactModel> items;

	/** The filtered items. */
	private List<ContactModel> filteredItems;

	/** The resource id. */
	private int resourceId;

	/** The filter. */
	private Filter filter;

	/** The resolver. */
	private ContentResolver resolver;

	/** The currently selected display name. */
	private String currentlySelectedDisplayName;

	/** The last constraint length. */
	private int lastConstraintLength = 0;

	/** The Constant ACTION_RELOAD. */
	private static final int ACTION_RELOAD = 0;

	/** The Constant ACTION_RESTRICT. */
	private static final int ACTION_RESTRICT = 1;

	/**
	 * Gets the contacts.
	 * 
	 * @param constraint
	 *            the constraint
	 * @param action
	 *            the action
	 * @return the contacts
	 */
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

	/**
	 * Reload items.
	 * 
	 * @param constraint
	 *            the constraint
	 */
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
//					constraint = TextUtils.replace(constraint, CHARACTERS_TO_REPLACE, REPLACEMENTS);
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
					currentlySelectedDisplayName = ((ContactModel) resultValue)
							.getDisplayName();
					return String.format("%s (%s)",
							((ContactModel) resultValue).getDisplayName(),
							((ContactModel) resultValue).getPhoneNumber());

				}
			};
		}
		return filter;
	}

	@Override
	public int getCount() {
		return filteredItems.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ArrayAdapter#getItem(int)
	 */
	@Override
	public ContactModel getItem(int position) {
		return filteredItems.get(position);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View,
	 * android.view.ViewGroup)
	 */
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

	public void setCurrentlySelectedDisplayName(
			String currentlySelectedDisplayName) {
		this.currentlySelectedDisplayName = currentlySelectedDisplayName;
	}

}
