package pl.qbassso.smsdb;

import java.util.HashMap;

import pl.qbasso.models.ConversationModel;
import pl.qbasso.models.SmsModel;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class SmsProvider extends ContentProvider {

	private static UriMatcher sMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	private SmsDatabaseHelper mDb;
	private static final int CONVERSATION_CODE = 1;
	private static final int CONVERSATION_ID_CODE = 2;
	private static final int SMS_CODE = 3;
	private static final int SMS_ID_CODE = 4;
	private static final int SMS_OUTBOX_CODE = 5;
	private static final int SMS_OUTBOX_ID_CODE = 6;
	private static final int SMS_INBOX_CODE = 7;
	private static final int SMS_INBOX_ID_CODE = 8;
	private static final int SMS_DRAFT_CODE = 9;
	private static final int SMS_DRAFT_ID_CODE = 10;

	public final static String COLUMN_SMS_ID = "_id";
	public final static String COLUMN_SMS_THREAD_ID = "thread_id";
	public final static String COLUMN_SMS_ADDRESS = "address";
	public final static String COLUMN_SMS_DATE = "date";
	public final static String COLUMN_SMS_READ = "read";
	public final static String COLUMN_SMS_STATUS = "status";
	public static final String COLUMN_SMS_TYPE = "type";
	public final static String COLUMN_SMS_BODY = "body";

	public final static int COLUMN_SMS_ID_NUMBER = 0;
	public final static int COLUMN_SMS_THREAD_ID_NUMBER = 1;
	public final static int COLUMN_SMS_ADDRESS_NUMBER = 2;
	public final static int COLUMN_SMS_DATE_NUMBER = 3;
	public final static int COLUMN_SMS_READ_NUMBER = 4;
	public final static int COLUMN_SMS_STATUS_NUMBER = 5;
	public static final int COLUMN_SMS_TYPE_NUMBER = 6;
	public final static int COLUMN_SMS_BODY_NUMBER = 7;

	public final static String COLUMN_CONVERSATION_ID = "_id";
	public final static String COLUMN_CONVERSATION_SNIPPET = "snippet";
	public final static String COLUMN_CONVERSATION_COUNT = "count";
	public final static String COLUMN_CONVERSATION_DATE = "date";
	public final static String COLUMN_CONVERSATION_UNREAD = "unread";
	public final static String COLUMN_CONVERSATION_DRAFT = "has_draft";

	public final static int COLUMN_CONVERSATION_ID_NUMBER = 0;
	public final static int COLUMN_CONVERSATION_SNIPPET_NUMBER = 1;
	public final static int COLUMN_CONVERSATION_COUNT_NUMBER = 2;
	public final static int COLUMN_CONVERSATION_DATE_NUMBER = 3;
	public final static int COLUMN_CONVERSATION_UNREAD_NUMBER = 4;
	public final static int COLUMN_CONVERSATION_DRAFT_NUMBER = 5;

	private final static String AUTHORITY = "pl.qbasso.smsdb.SmsProvider";
	private final static String SCHEME = "content://";
	private final static String SMS_PATH = "/sms/";
	private final static String SMS_OUTBOX_PATH = "/sms/outbox";
	private final static String SMS_INBOX_PATH = "/sms/inbox";
	private final static String SMS_DRAFT_PATH = "/sms/draft";
	private final static String CONVERSATION_PATH = "/conversation/";
	public final static Uri SMS_URI = Uri.parse(SCHEME + AUTHORITY + SMS_PATH);
	public final static Uri SMS_OUTBOX_URI = Uri.parse(SCHEME + AUTHORITY
			+ SMS_OUTBOX_PATH);
	public final static Uri SMS_INBOX_URI = Uri.parse(SCHEME + AUTHORITY
			+ SMS_INBOX_PATH);
	public final static Uri SMS_DRAFT_URI = Uri.parse(SCHEME + AUTHORITY
			+ SMS_DRAFT_PATH);
	public final static Uri CONVERSATION_CONTENT_URI = Uri.parse(SCHEME
			+ AUTHORITY + CONVERSATION_PATH);

	public static HashMap<String, Integer> sSmsProjectionMap;
	public static HashMap<String, Integer> sConversationProjectionMap;

	static {
		sMatcher.addURI(AUTHORITY, "sms", SMS_CODE);
		sMatcher.addURI(AUTHORITY, "sms/#", SMS_ID_CODE);
		sMatcher.addURI(AUTHORITY, "sms/outbox", SMS_OUTBOX_CODE);
		sMatcher.addURI(AUTHORITY, "sms/outbox/#", SMS_OUTBOX_ID_CODE);
		sMatcher.addURI(AUTHORITY, "sms/inbox", SMS_INBOX_CODE);
		sMatcher.addURI(AUTHORITY, "sms/inbox/#", SMS_INBOX_ID_CODE);
		sMatcher.addURI(AUTHORITY, "sms/draft", SMS_DRAFT_CODE);
		sMatcher.addURI(AUTHORITY, "sms/draft/#", SMS_DRAFT_ID_CODE);
		sMatcher.addURI(AUTHORITY, "conversation", CONVERSATION_CODE);
		sMatcher.addURI(AUTHORITY, "conversation/#", CONVERSATION_ID_CODE);

		sSmsProjectionMap = new HashMap<String, Integer>();
		sSmsProjectionMap.put(COLUMN_SMS_ID, 0);
		sSmsProjectionMap.put(COLUMN_SMS_THREAD_ID, 1);
		sSmsProjectionMap.put(COLUMN_SMS_ADDRESS, 2);
		sSmsProjectionMap.put(COLUMN_SMS_DATE, 3);
		sSmsProjectionMap.put(COLUMN_SMS_READ, 4);
		sSmsProjectionMap.put(COLUMN_SMS_STATUS, 5);
		sSmsProjectionMap.put(COLUMN_SMS_TYPE, 6);
		sSmsProjectionMap.put(COLUMN_SMS_BODY, 7);

		sConversationProjectionMap = new HashMap<String, Integer>();
		sConversationProjectionMap.put(COLUMN_CONVERSATION_ID, 0);
		sConversationProjectionMap.put(COLUMN_CONVERSATION_SNIPPET, 1);
		sConversationProjectionMap.put(COLUMN_CONVERSATION_COUNT, 2);
		sConversationProjectionMap.put(COLUMN_CONVERSATION_DATE, 3);
		sConversationProjectionMap.put(COLUMN_CONVERSATION_UNREAD, 4);
		sConversationProjectionMap.put(COLUMN_CONVERSATION_DRAFT, 5);
	}

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		SQLiteDatabase db = mDb.getWritableDatabase();
		String whereClause;
		int result = -1;
		switch (sMatcher.match(arg0)) {
		case SMS_CODE:
			result = db.delete(SmsDatabaseHelper.SMS_TABLE_NAME, arg1, arg2);
			break;
		case SMS_ID_CODE:
			whereClause = SmsModel.ID + "=" + arg0.getLastPathSegment();
			if (arg1 != null) {
				whereClause += " and " + arg1;
			}
			result = db.delete(SmsDatabaseHelper.SMS_TABLE_NAME, whereClause,
					arg2);
			break;
		case CONVERSATION_CODE:
			result = db.delete(SmsDatabaseHelper.CONVERSATION_TABLE_NAME, arg1,
					arg2);
			break;
		case CONVERSATION_ID_CODE:
			whereClause = ConversationModel.THREAD_ID + "="
					+ arg0.getLastPathSegment();
			if (arg1 != null) {
				whereClause += " and " + arg1;
			}
			result = db.delete(SmsDatabaseHelper.CONVERSATION_TABLE_NAME,
					whereClause, arg2);
			break;
		default:
			break;
		}
		if (result > 0) {
			getContext().getContentResolver().notifyChange(arg0, null);
		}
		return result;
	}

	@Override
	public String getType(Uri arg0) {
		switch (sMatcher.match(arg0)) {
		case SMS_CODE:
			return "vnd.android.cursor.item/vnd.pl.qbasso.smsdb.sms";
		case SMS_ID_CODE:
			return "vnd.android.cursor.item/vnd.pl.qbasso.smsdb.conversation";
		case CONVERSATION_CODE:
			return "vnd.android.cursor.dir/vnd.pl.qbasso.smsdb.sms";
		case CONVERSATION_ID_CODE:
			return "vnd.android.cursor.dir/vnd.pl.qbasso.smsdb.conversation";
		default:
			break;
		}
		return null;
	}

	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		SQLiteDatabase db = mDb.getWritableDatabase();
		Uri u = null;
		long newId = -1;
		ContentValues v = new ContentValues(arg1);
		switch (sMatcher.match(arg0)) {
		case SMS_CODE:
			newId = db.insert(SmsDatabaseHelper.SMS_TABLE_NAME, null, v);
//			if (v.getAsInteger(SmsModel.TYPE) == SmsModel.MESSAGE_TYPE_INBOX) {
//				db.execSQL("update ")
//			}
			if (newId > -1) {
				u = Uri.withAppendedPath(SMS_URI, String.valueOf(newId));
			}
			break;
		case CONVERSATION_CODE:
			newId = db.insert(SmsDatabaseHelper.CONVERSATION_TABLE_NAME, null,
					v);
			if (newId > -1) {
				u = Uri.withAppendedPath(CONVERSATION_CONTENT_URI,
						String.valueOf(newId));
			}
			break;
		default:
			break;
		}
		getContext().getContentResolver().notifyChange(arg0, null);
		return u;
	}

	@Override
	public boolean onCreate() {
		mDb = new SmsDatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri arg0, String[] arg1, String arg2, String[] arg3,
			String arg4) {
		Cursor result = null;
		SQLiteDatabase db = mDb.getReadableDatabase();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		int code = sMatcher.match(arg0);
		switch (code) {
		case SMS_CODE:
			break;
		case SMS_ID_CODE:
			qb.appendWhere(SmsModel.ID + "=" + arg0.getLastPathSegment());
			break;
		case CONVERSATION_CODE:
			break;
		case CONVERSATION_ID_CODE:
			qb.appendWhere(ConversationModel.THREAD_ID + "="
					+ arg0.getLastPathSegment());
			break;
		case SMS_DRAFT_CODE:
			qb.appendWhere(SmsModel.TYPE + "=" + SmsModel.MESSAGE_TYPE_DRAFT);
			break;
		case SMS_DRAFT_ID_CODE:
			qb.appendWhere(SmsModel.TYPE + "=" + SmsModel.MESSAGE_TYPE_DRAFT
					+ " and " + SmsModel.ID + "=" + arg0.getLastPathSegment());
			break;
		case SMS_INBOX_CODE:
			qb.appendWhere(SmsModel.TYPE + "=" + SmsModel.MESSAGE_TYPE_INBOX);
			break;
		case SMS_INBOX_ID_CODE:
			qb.appendWhere(SmsModel.TYPE + "=" + SmsModel.MESSAGE_TYPE_INBOX
					+ " and " + SmsModel.ID + "=" + arg0.getLastPathSegment());
			break;
		case SMS_OUTBOX_CODE:
			qb.appendWhere(SmsModel.TYPE + "=" + SmsModel.MESSAGE_TYPE_OUTBOX);
			break;
		case SMS_OUTBOX_ID_CODE:
			qb.appendWhere(SmsModel.TYPE + "=" + SmsModel.MESSAGE_TYPE_OUTBOX
					+ " and " + SmsModel.ID + "=" + arg0.getLastPathSegment());
			break;
		default:
			break;
		}
		if (code == CONVERSATION_CODE || code == CONVERSATION_ID_CODE) {
			qb.setTables(SmsDatabaseHelper.CONVERSATION_TABLE_NAME);
		} else {
			qb.setTables(SmsDatabaseHelper.SMS_TABLE_NAME);
		}
		result = qb.query(db, arg1, arg2, arg3, null, null, arg4);
		return result;
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		SQLiteDatabase db = mDb.getWritableDatabase();
		int count = 0;
		String whereClause;
		switch (sMatcher.match(arg0)) {
		case SMS_ID_CODE:
			whereClause = SmsModel.ID + "=" + arg0.getLastPathSegment();
			if (arg2 != null) {
				whereClause += " and " + arg2;
			}
			count = db.update(SmsDatabaseHelper.SMS_TABLE_NAME, arg1,
					whereClause, arg3);
			break;
		case CONVERSATION_ID_CODE:
			whereClause = ConversationModel.THREAD_ID + "="
					+ arg0.getLastPathSegment();
			if (arg2 != null) {
				whereClause += " and " + arg2;
			}
			count = db.update(SmsDatabaseHelper.CONVERSATION_TABLE_NAME, arg1,
					whereClause, arg3);
			break;

		default:
			break;
		}
		if (count > 0) {
			getContext().getContentResolver().notifyChange(arg0, null);
		}
		return count;
	}

}
