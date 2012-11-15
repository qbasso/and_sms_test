package pl.qbasso.sms;

import java.util.ArrayList;
import java.util.List;

import pl.qbasso.models.ConversationModel;
import pl.qbasso.models.SmsModel;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;

public class SmsDbHelper {
	public static final Uri SMS_URI = Uri.parse("content://sms");
	public static final Uri SMS_OUTBOX_URI = Uri.parse("content://sms/sent");
	public static final Uri SMS_INBOX_URI = Uri.parse("content://sms/inbox");
	public static final Uri SMS_DRAFT_URI = Uri.parse("content://sms/draft");
	public static final Uri SMS_CONVERSATIONS_URI = Uri
			.parse("content://sms/conversations");
	public static final String SMS_SORT_ORDER = "date DESC";
	private ContentResolver resolver;

	public SmsDbHelper(ContentResolver r) {
		this.resolver = r;
	}

	public void updateSmsState(Uri u, int smsState) {
		ContentValues values = new ContentValues();
		values.put(SmsModel.TYPE, smsState);
		resolver.update(u, values, null, null);
	}

	public Uri insertSmsToUri(Uri uri, String address, String body,
			String subject, Long date, boolean read, boolean deliveryReport,
			long threadId) {
		ContentValues values = new ContentValues();
		values.put(SmsModel.ADDRESS, address);
		values.put(SmsModel.BODY, body);
		values.put(SmsModel.SUBJECT, subject);
		values.put(SmsModel.READ,
				read ? Integer.valueOf(1) : Integer.valueOf(0));
		if (date != null) {
			values.put(SmsModel.DATE, date);
		}
		if (threadId != -1) {
			values.put(SmsModel.THREAD_ID, threadId);
		}
		if (deliveryReport) {
			values.put(SmsModel.STATUS, SmsModel.STATUS_PENDING);
		}
		return resolver.insert(uri, values);
	}

	public long getThreadIdForSmsUri(Uri u) {
		long result = 0;
		Cursor c = resolver.query(u, new String[] { SmsModel.THREAD_ID }, null,
				null, null);
		if (c != null) {
			if (c.moveToNext()) {
				result = c.getLong(0);
			}
		}
		return result;
	}

	public long getThreadIdForPhoneNumber(String phoneNumber) {
		long result = -1;
		Cursor c = resolver.query(SMS_URI, new String[] { SmsModel.THREAD_ID },
				SmsModel.ADDRESS + "= ?", new String[] { phoneNumber }, null);
		if (c != null) {
			if (c.moveToNext()) {
				result = c.getLong(0);
			}
		}
		return result;
	}

	public String getAddressForThreadId(String phoneNumber, String displayName) {
		if (displayName == null) {
			return phoneNumber;
		} else {
			return displayName + String.format("(%s)", phoneNumber);
		}
	}

	private String getThreadDisplayName(String phoneNumber, String displayName) {
		if (!displayName.equals("")) {
			return displayName;
		} else {
			return phoneNumber;
		}
	}

	public String getDisplayName(String phoneNumber) {
		String displayName = phoneNumber;
		Cursor c;
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(phoneNumber));
		c = resolver.query(uri, new String[] { PhoneLookup.DISPLAY_NAME },
				null, null, null);
		if (c != null) {
			if (c.moveToNext()) {
				displayName = c.getString(0);
			}
			c.close();
		}
		return displayName;
	}

	private String getPhoneNumber(long threadId) {
		String phoneNumber = "";
		Cursor c = resolver.query(SMS_URI, new String[] { SmsModel.ADDRESS },
				SmsModel.THREAD_ID + "= ?",
				new String[] { String.valueOf(threadId) }, null);
		if (c != null) {
			if (c.moveToNext()) {
				phoneNumber = c.getString(0);
			}
			c.close();
		}
		return phoneNumber;
	}

	public List<ConversationModel> getThreads() {
		Cursor c1 = null;
		List<ConversationModel> result = new ArrayList<ConversationModel>();
		Cursor c = resolver.query(SMS_CONVERSATIONS_URI, null, null, null,
				SMS_SORT_ORDER);
		if (c != null) {
			while (c.moveToNext()) {
				ConversationModel m = new ConversationModel(c.getLong(0),
						c.getInt(1), c.getString(2));
				String phoneNumber = getPhoneNumber(m.getThreadId());
				c1 = resolver.query(
						SMS_URI,
						new String[] { "count(read)" },
						SmsModel.THREAD_ID + "= ? and " + SmsModel.READ
								+ " = ?",
						new String[] { String.valueOf(m.getThreadId()),
								String.valueOf(SmsModel.MESSAGE_NOT_READ) },
						null);
				if (c1 != null) {
					if (c1.moveToNext()) {
						m.setUnread(c1.getInt(0));
					}
					c1.close();
				}
				String displayName = getDisplayName(phoneNumber);
				m.setAddress(phoneNumber);
				m.setDisplayName(getThreadDisplayName(phoneNumber, displayName));
				result.add(m);
			}
			c.close();
		}
		return result;
	}

	public List<SmsModel> getSmsForThread(long threadId) {
		List<SmsModel> result = new ArrayList<SmsModel>();
		Cursor c = resolver.query(SMS_URI, new String[] { SmsModel.ID,
				SmsModel.BODY, SmsModel.ADDRESS, SmsModel.DATE, SmsModel.TYPE,
				SmsModel.READ, SmsModel.STATUS }, SmsModel.THREAD_ID + "=?",
				new String[] { String.valueOf(threadId) }, SMS_SORT_ORDER);
		if (c != null) {
			while (c.moveToNext()) {
				result.add(new SmsModel(c.getLong(0), threadId, c.getString(2),
						"", c.getLong(3), c.getString(1), c.getInt(4), c
								.getInt(5), c.getInt(6)));
			}
			c.close();
		}
		return result;
	}

	public SmsModel getSingleSms(Uri u) {
		SmsModel result = null;
		Cursor c = resolver.query(u, new String[] { SmsModel.ID, SmsModel.BODY,
				SmsModel.ADDRESS, SmsModel.DATE, SmsModel.TYPE, SmsModel.READ,
				SmsModel.THREAD_ID, SmsModel.STATUS }, null, null, null);
		if (c != null) {
			if (c.moveToNext()) {
				result = new SmsModel(c.getLong(0), c.getLong(6),
						c.getString(2), "", c.getLong(3), c.getString(1),
						c.getInt(4), c.getInt(5), c.getInt(6));
				result.setAddressDisplayName(getDisplayName(result.getAddress()));
			}
			c.close();
		}
		return result;
	}

	public void deleteThread(long threadId) {
		resolver.delete(SMS_URI, SmsModel.THREAD_ID + "=?",
				new String[] { String.valueOf(threadId) });
		// resolver.delete(SMS_CONVERSATIONS_URI, SmsThreadModel.THREAD_ID,
		// new String[] { String.valueOf(threadId) });
	}

	public void deleteSms(long smsId) {
		resolver.delete(SMS_URI, SmsModel.ID + "=?",
				new String[] { String.valueOf(smsId) });
	}

	public int updateSmsRead(long id, int messageRead) {
		int result;
		Uri u = Uri.withAppendedPath(SMS_URI, String.valueOf(id));
		ContentValues v = new ContentValues();
		v.put(SmsModel.READ, messageRead);
		result = resolver.update(u, v, null, null);
		return result;
	}

}
