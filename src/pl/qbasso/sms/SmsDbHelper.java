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

	public int updateSmsStatus(Uri u, int smsStatus, int smsType) {
		int result;
		ContentValues values = new ContentValues();
		if (smsStatus != -1) {
			values.put(SmsModel.STATUS, smsStatus);
		}
		if (smsType != -1) {
			values.put(SmsModel.TYPE, smsType);
		}
		result = resolver.update(u, values, null, null);
		return result;
	}

	public Uri insertSms(Uri u, SmsModel m) {
		ContentValues values = new ContentValues();
		values.put(SmsModel.ADDRESS, m.getAddress());
		values.put(SmsModel.BODY, m.getBody());
		values.put(SmsModel.SUBJECT, "");
		values.put(SmsModel.READ, m.getRead());
		if (m.getDate() != 0) {
			values.put(SmsModel.DATE, m.getDate());
		}
		if (m.getThreadId() != -1) {
			values.put(SmsModel.THREAD_ID, m.getThreadId());
		}
		values.put(SmsModel.TYPE, m.getSmsType());
		values.put(SmsModel.STATUS, m.getStatus());
		return resolver.insert(u, values);
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
				getUnreadCount(m);
				checkForDraft(m);
				String displayName = getDisplayName(phoneNumber);
				m.setAddress(phoneNumber);
				m.setDisplayName(getThreadDisplayName(phoneNumber, displayName));
				result.add(m);
			}
			c.close();
		}
		return result;
	}

	private void checkForDraft(ConversationModel m) {
		Cursor c1;
		c1 = resolver.query(SMS_DRAFT_URI, new String[] { SmsModel.BODY },
				SmsModel.THREAD_ID + "= ?",
				new String[] { String.valueOf(m.getThreadId()) }, null);
		if (c1 != null) {
			if (c1.moveToNext()) {
				m.setSnippet(c1.getString(0));
				m.setDraft(true);
			}
			c1.close();
		}
	}

	private void getUnreadCount(ConversationModel m) {
		Cursor c1;
		c1 = resolver.query(
				SMS_URI,
				new String[] { "count(read)" },
				SmsModel.THREAD_ID + "= ? and " + SmsModel.READ + " = ?",
				new String[] { String.valueOf(m.getThreadId()),
						String.valueOf(SmsModel.MESSAGE_NOT_READ) }, null);
		if (c1 != null) {
			if (c1.moveToNext()) {
				m.setUnread(c1.getInt(0));
			}
			c1.close();
		}
	}

	public List<SmsModel> getSmsForThread(long threadId) {
		List<SmsModel> result = new ArrayList<SmsModel>();
		Cursor c = resolver.query(
				SMS_URI,
				new String[] { SmsModel.ID, SmsModel.BODY, SmsModel.ADDRESS,
						SmsModel.DATE, SmsModel.TYPE, SmsModel.READ,
						SmsModel.STATUS },
				SmsModel.THREAD_ID + "=? and " + SmsModel.TYPE + " <> ?",
				new String[] { String.valueOf(threadId),
						String.valueOf(SmsModel.MESSAGE_TYPE_DRAFT) },
				SMS_SORT_ORDER);
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

	public void deleteSms(Uri u, long smsId) {
		resolver.delete(u, SmsModel.ID + "=?",
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

	public long getDraftIdForThread(long threadId) {
		long result = -1;
		Cursor c = resolver.query(
				SMS_DRAFT_URI,
				new String[] { SmsModel.ID },
				SmsModel.THREAD_ID + " = ? and " + SmsModel.TYPE + " = ?",
				new String[] { String.valueOf(threadId),
						String.valueOf(SmsModel.MESSAGE_TYPE_DRAFT) }, null);
		if (c != null) {
			if (c.moveToNext()) {
				result = c.getLong(0);
			}
		}
		return result;
	}

	public String getDraftTextForThread(long threadId) {
		String result = null;
		Cursor c = resolver.query(
				SMS_DRAFT_URI,
				new String[] { SmsModel.BODY },
				SmsModel.THREAD_ID + " = ? and " + SmsModel.TYPE + " = ?",
				new String[] { String.valueOf(threadId),
						String.valueOf(SmsModel.MESSAGE_TYPE_DRAFT) }, null);
		if (c != null) {
			if (c.moveToNext()) {
				result = c.getString(0);
			}
		}
		return result;
	}

	public int updateDraftMessage(long msgId, String body, long date) {
		int result = 0;
		ContentValues cv = new ContentValues();
		cv.put(SmsModel.BODY, body);
		cv.put(SmsModel.DATE, date);
		result = resolver.update(
				Uri.withAppendedPath(SMS_DRAFT_URI, String.valueOf(msgId)), cv,
				null, null);
		return result;
	}

	public int deleteDraftForThread(long threadId) {
		int result;
		result = resolver.delete(
				SMS_URI,
				SmsModel.THREAD_ID + " = ? and " + SmsModel.TYPE + " = ?",
				new String[] { String.valueOf(threadId),
						String.valueOf(SmsModel.MESSAGE_TYPE_DRAFT) });
		return result;
	}
}
