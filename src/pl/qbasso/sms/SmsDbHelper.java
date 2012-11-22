/*
 * @author JPorzuczek
 */
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

// TODO: Auto-generated Javadoc
/**
 * The Class SmsDbHelper.
 */
public class SmsDbHelper {

	/** The Constant SMS_URI. */
	public static final Uri SMS_URI = Uri.parse("content://sms");

	/** The Constant SMS_OUTBOX_URI. */
	public static final Uri SMS_OUTBOX_URI = Uri.parse("content://sms/sent");

	/** The Constant SMS_INBOX_URI. */
	public static final Uri SMS_INBOX_URI = Uri.parse("content://sms/inbox");

	/** The Constant SMS_DRAFT_URI. */
	public static final Uri SMS_DRAFT_URI = Uri.parse("content://sms/draft");

	/** The Constant SMS_CONVERSATIONS_URI. */
	public static final Uri SMS_CONVERSATIONS_URI = Uri
			.parse("content://sms/conversations");

	/** The Constant SMS_SORT_ORDER. */
	public static final String SMS_SORT_ORDER = "date DESC";

	/** The resolver. */
	private ContentResolver resolver;

	/**
	 * Instantiates a new sms db helper.
	 * 
	 * @param r
	 *            the r
	 */
	public SmsDbHelper(ContentResolver r) {
		this.resolver = r;
	}

	/**
	 * Update sms status.
	 * 
	 * @param u
	 *            the u
	 * @param smsStatus
	 *            the sms status
	 * @param smsType
	 *            the sms type
	 * @return the int
	 */
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

	/**
	 * Insert sms.
	 * 
	 * @param u
	 *            the u
	 * @param m
	 *            the m
	 * @return the uri
	 */
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

	/**
	 * Gets the thread id for sms uri.
	 * 
	 * @param u
	 *            the u
	 * @return the thread id for sms uri
	 */
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

	/**
	 * Gets the thread id for phone number.
	 * 
	 * @param phoneNumber
	 *            the phone number
	 * @return the thread id for phone number
	 */
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

	/**
	 * Gets the address for thread id.
	 * 
	 * @param phoneNumber
	 *            the phone number
	 * @param displayName
	 *            the display name
	 * @return the address for thread id
	 */
	public String getAddressForThreadId(String phoneNumber, String displayName) {
		if (displayName == null) {
			return phoneNumber;
		} else {
			return displayName + String.format("(%s)", phoneNumber);
		}
	}

	/**
	 * Gets the thread display name.
	 * 
	 * @param phoneNumber
	 *            the phone number
	 * @param displayName
	 *            the display name
	 * @return the thread display name
	 */
	private String getThreadDisplayName(String phoneNumber, String displayName) {
		if (!displayName.equals("")) {
			return displayName;
		} else {
			return phoneNumber;
		}
	}

	/**
	 * Gets the display name.
	 * 
	 * @param phoneNumber
	 *            the phone number
	 * @return the display name
	 */
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

	/**
	 * Gets the phone number.
	 * 
	 * @param threadId
	 *            the thread id
	 * @return the phone number
	 */
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

	/**
	 * Check for draft.
	 * 
	 * @param m
	 *            the m
	 */
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

	/**
	 * Gets the unread count.
	 * 
	 * @param m
	 *            the m
	 * @return the unread count
	 */
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

	/**
	 * Gets the sms for thread.
	 * 
	 * @param threadId
	 *            the thread id
	 * @return the sms for thread
	 */
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

	/**
	 * Gets the single sms.
	 * 
	 * @param u
	 *            the u
	 * @return the single sms
	 */
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

	/**
	 * Delete thread.
	 * 
	 * @param threadId
	 *            the thread id
	 */
	public void deleteThread(long threadId) {
		resolver.delete(SMS_URI, SmsModel.THREAD_ID + "=?",
				new String[] { String.valueOf(threadId) });
		// resolver.delete(SMS_CONVERSATIONS_URI, SmsThreadModel.THREAD_ID,
		// new String[] { String.valueOf(threadId) });
	}

	/**
	 * Delete sms.
	 * 
	 * @param u
	 *            the u
	 * @param smsId
	 *            the sms id
	 */
	public void deleteSms(Uri u, long smsId) {
		resolver.delete(u, SmsModel.ID + "=?",
				new String[] { String.valueOf(smsId) });
	}

	/**
	 * Update sms read.
	 * 
	 * @param id
	 *            the id
	 * @param messageRead
	 *            the message read
	 * @return the int
	 */
	public int updateSmsRead(long id, int messageRead) {
		int result;
		Uri u = Uri.withAppendedPath(SMS_URI, String.valueOf(id));
		ContentValues v = new ContentValues();
		v.put(SmsModel.READ, messageRead);
		result = resolver.update(u, v, null, null);
		return result;
	}

	/**
	 * Gets the draft id for thread.
	 * 
	 * @param threadId
	 *            the thread id
	 * @return the draft id for thread
	 */
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

	/**
	 * Gets the draft text for thread.
	 * 
	 * @param threadId
	 *            the thread id
	 * @return the draft text for thread
	 */
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

	/**
	 * Update draft message.
	 * 
	 * @param msgId
	 *            the msg id
	 * @param body
	 *            the body
	 * @param date
	 *            the date
	 * @return the int
	 */
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

	/**
	 * Delete draft for thread.
	 * 
	 * @param threadId
	 *            the thread id
	 * @return the int
	 */
	public int deleteDraftForThread(long threadId) {
		int result;
		result = resolver.delete(
				SMS_URI,
				SmsModel.THREAD_ID + " = ? and " + SmsModel.TYPE + " = ?",
				new String[] { String.valueOf(threadId),
						String.valueOf(SmsModel.MESSAGE_TYPE_DRAFT) });
		return result;
	}

	public List<SmsModel> getMessagesNotSent() {
		List<SmsModel> result = new ArrayList<SmsModel>();
		Cursor c = resolver.query(
				SMS_URI,
				new String[] { SmsModel.ID, SmsModel.THREAD_ID,
						SmsModel.ADDRESS, SmsModel.DATE, SmsModel.BODY,
						SmsModel.TYPE, SmsModel.READ, SmsModel.STATUS },
				SmsModel.STATUS + " = ? or " + SmsModel.STATUS + " = ? or "
						+ SmsModel.STATUS + " = ? ",
				new String[] { String.valueOf(SmsModel.STATUS_WAITING),
						String.valueOf(SmsModel.STATUS_PENDING),
						String.valueOf(SmsModel.STATUS_FAILED) }, null);
		if (c != null) {
			while (c.moveToNext()) {
				SmsModel m = new SmsModel(c.getLong(0), c.getLong(1),
						c.getString(2), "", c.getLong(3), c.getString(4),
						c.getInt(5), c.getInt(6), c.getInt(7));
				result.add(m);
			}
			c.close();
		}
		return result;
	}
}
