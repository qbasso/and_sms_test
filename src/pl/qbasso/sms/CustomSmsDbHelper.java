package pl.qbasso.sms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import pl.qbasso.interfaces.ISmsAccess;
import pl.qbasso.models.ConversationModel;
import pl.qbasso.models.SmsModel;
import pl.qbassso.smsdb.SmsProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;

public class CustomSmsDbHelper implements ISmsAccess {
	/** The Constant SMS_URI. */
	public static final Uri SMS_URI = SmsProvider.SMS_URI;
	/** The Constant SMS_OUTBOX_URI. */
	public static final Uri SMS_OUTBOX_URI = SmsProvider.SMS_OUTBOX_URI;
	/** The Constant SMS_INBOX_URI. */
	public static final Uri SMS_INBOX_URI = SmsProvider.SMS_INBOX_URI;
	/** The Constant SMS_DRAFT_URI. */
	public static final Uri SMS_DRAFT_URI = SmsProvider.SMS_DRAFT_URI;
	/** The Constant SMS_CONVERSATIONS_URI. */
	public static final Uri SMS_CONVERSATIONS_URI = SmsProvider.CONVERSATION_CONTENT_URI;
	/** The Constant SMS_SORT_ORDER. */
	public static final String SMS_SORT_ORDER = "date DESC";
	/** The resolver. */
	protected ContentResolver resolver;

	public CustomSmsDbHelper(ContentResolver r) {
		this.resolver = r;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.qbasso.sms.ISmsAccess#updateSmsStatus(android.net.Uri, int, int)
	 */
	public int updateSmsStatus(Uri u, int smsStatus, int smsType) {
		int result;
		ContentValues values = new ContentValues();
		values.put(SmsProvider.COLUMN_SMS_STATUS, smsStatus);
		values.put(SmsProvider.COLUMN_SMS_TYPE, smsType);
		result = resolver.update(u, values, null, null);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.qbasso.sms.ISmsAccess#insertSms(android.net.Uri,
	 * pl.qbasso.models.SmsModel)
	 */
	public Uri insertSms(SmsModel m) {
		ContentValues values = new ContentValues();
		values.put(SmsModel.ADDRESS, m.getAddress());
		values.put(SmsModel.BODY, m.getBody());
		values.put(SmsModel.READ, m.getRead());
		if (m.getDate() != 0) {
			values.put(SmsModel.DATE, m.getDate());
		}
		if (m.getThreadId() != -1) {
			values.put(SmsModel.THREAD_ID, m.getThreadId());
		}
		values.put(SmsModel.TYPE, m.getSmsType());
		values.put(SmsModel.STATUS, m.getStatus());
		return resolver.insert(SMS_URI, values);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.qbasso.sms.ISmsAccess#getThreadIdForSmsUri(android.net.Uri)
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.qbasso.sms.ISmsAccess#getThreadIdForPhoneNumber(java.lang.String)
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.qbasso.sms.ISmsAccess#getAddressForThreadId(java.lang.String,
	 * java.lang.String)
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.qbasso.sms.ISmsAccess#getDisplayName(java.lang.String)
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.qbasso.sms.ISmsAccess#getThreads(java.util.HashSet)
	 */
	public List<ConversationModel> getThreads(HashSet<Long> needRefresh) {
		Cursor c = null;
		List<ConversationModel> result = new ArrayList<ConversationModel>();
		if (needRefresh == null) {
			c = resolver.query(SMS_CONVERSATIONS_URI, null, null, null,
					SMS_SORT_ORDER);
		} else if (needRefresh.size() > 0) {
			c = resolver.query(SMS_CONVERSATIONS_URI, null,
					ConversationModel.THREAD_ID + " in "
							+ buildListofNamedParameters(needRefresh.size()),
					buildListOfParameters(needRefresh), SMS_SORT_ORDER);
		}
		if (c != null) {
			while (c.moveToNext()) {
				ConversationModel m = new ConversationModel(
						c.getLong(SmsProvider.sSmsProjectionMap
								.get(SmsProvider.COLUMN_CONVERSATION_ID)),
						c.getInt(SmsProvider.sConversationProjectionMap
								.get(SmsProvider.COLUMN_CONVERSATION_COUNT)),
						c.getString(SmsProvider.sConversationProjectionMap
								.get(SmsProvider.COLUMN_CONVERSATION_SNIPPET)));
				String phoneNumber = getPhoneNumber(m.getThreadId());
				m.setLastModified(c
						.getLong(SmsProvider.sConversationProjectionMap
								.get(SmsProvider.COLUMN_CONVERSATION_DATE)));
				m.setUnread(c.getInt(SmsProvider.sConversationProjectionMap
						.get(SmsProvider.COLUMN_CONVERSATION_UNREAD)));
				m.setDraft(c.getInt(SmsProvider.sConversationProjectionMap
						.get(SmsProvider.COLUMN_CONVERSATION_DRAFT)) > 0 ? true
						: false);
				String displayName = getDisplayName(phoneNumber);
				m.setAddress(phoneNumber);
				m.setDisplayName(getThreadDisplayName(phoneNumber, displayName));
				result.add(m);
			}
			c.close();
		}
		return result;
	}

	private String[] buildListOfParameters(HashSet<Long> needRefresh) {
		Iterator<Long> it = needRefresh.iterator();
		String[] result = new String[needRefresh.size()];
		for (int i = 0; i < needRefresh.size(); i++) {
			result[i] = String.valueOf(it.next());
		}
		return result;
	}

	private String buildListofNamedParameters(int size) {
		if (size > 0) {
			StringBuilder result = new StringBuilder("(");
			for (int i = 0; i < size; i++) {
				result.append("?, ");
			}
			result.delete(result.length() - 2, result.length());
			result.append(")");
			return result.toString();
		}
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.qbasso.sms.ISmsAccess#getSmsForThread(long)
	 */
	public List<SmsModel> getSmsForThread(long threadId) {
		List<SmsModel> result = new ArrayList<SmsModel>();
		Cursor c = resolver.query(
				SMS_URI,
				new String[] { SmsProvider.COLUMN_SMS_ID,
						SmsProvider.COLUMN_SMS_BODY,
						SmsProvider.COLUMN_SMS_ADDRESS,
						SmsProvider.COLUMN_SMS_DATE,
						SmsProvider.COLUMN_SMS_TYPE,
						SmsProvider.COLUMN_SMS_READ,
						SmsProvider.COLUMN_SMS_STATUS },
				SmsModel.THREAD_ID + "=? and " + SmsModel.TYPE + " <> ?",
				new String[] { String.valueOf(threadId),
						String.valueOf(SmsModel.MESSAGE_TYPE_DRAFT) },
				"date ASC");
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.qbasso.sms.ISmsAccess#getSingleSms(android.net.Uri)
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.qbasso.sms.ISmsAccess#deleteThread(long)
	 */
	public void deleteThread(long threadId) {
		resolver.delete(SMS_URI, SmsModel.THREAD_ID + "=?",
				new String[] { String.valueOf(threadId) });
		// resolver.delete(SMS_CONVERSATIONS_URI, SmsThreadModel.THREAD_ID,
		// new String[] { String.valueOf(threadId) });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.qbasso.sms.ISmsAccess#deleteSms(android.net.Uri, long)
	 */
	public void deleteSms(long smsId) {
		resolver.delete(SMS_URI, SmsModel.ID + "=?",
				new String[] { String.valueOf(smsId) });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.qbasso.sms.ISmsAccess#updateSmsRead(long, int)
	 */
	public int updateSmsRead(long id, int messageRead) {
		int result;
		Uri u = Uri.withAppendedPath(SMS_URI, String.valueOf(id));
		ContentValues v = new ContentValues();
		v.put(SmsModel.READ, messageRead);

		result = resolver.update(u, v, null, null);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.qbasso.sms.ISmsAccess#getDraftIdForThread(long)
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.qbasso.sms.ISmsAccess#getDraftTextForThread(long)
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.qbasso.sms.ISmsAccess#updateDraftMessage(long, java.lang.String,
	 * long)
	 */
	public int updateDraftMessage(long msgId, String body, long date) {
		int result = 0;
		ContentValues cv = new ContentValues();
		cv.put(SmsModel.BODY, body);
		cv.put(SmsModel.DATE, date);
		cv.put(SmsModel.DATE, System.currentTimeMillis());
		result = resolver.update(
				Uri.withAppendedPath(SMS_DRAFT_URI, String.valueOf(msgId)), cv,
				null, null);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.qbasso.sms.ISmsAccess#deleteDraftForThread(long)
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.qbasso.sms.ISmsAccess#getMessagesNotSent()
	 */
	public List<SmsModel> getMessagesNotSent() {
		List<SmsModel> result = new ArrayList<SmsModel>();
		Cursor c = resolver.query(SMS_URI, new String[] { SmsModel.ID,
				SmsModel.THREAD_ID, SmsModel.ADDRESS, SmsModel.DATE,
				SmsModel.BODY, SmsModel.TYPE, SmsModel.READ, SmsModel.STATUS },
				SmsModel.TYPE + " = ? or " + SmsModel.TYPE + " = ? ",
				new String[] { String.valueOf(SmsModel.MESSAGE_TYPE_QUEUED),
						String.valueOf(SmsModel.MESSAGE_TYPE_FAILED) }, null);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.qbasso.sms.ISmsAccess#getUnreadCount()
	 */
	public int getUnreadCount() {
		int result = 0;
		Cursor c = resolver.query(SMS_URI,
				new String[] { String.format("count(%s)", SmsModel.ID) },
				String.format("%s = ?", SmsModel.READ),
				new String[] { String.valueOf(SmsModel.MESSAGE_NOT_READ) },
				null);
		if (c != null) {
			if (c.moveToNext()) {
				result = c.getInt(0);
			}
		}
		return result;
	}

}
