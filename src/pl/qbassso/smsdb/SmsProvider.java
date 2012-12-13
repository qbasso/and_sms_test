package pl.qbassso.smsdb;

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
		getContext().getContentResolver().notifyChange(arg0, null);
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
		if (code > CONVERSATION_CODE && code <= CONVERSATION_ID_CODE) {
			qb.setTables(SmsDatabaseHelper.SMS_TABLE_NAME);
		} else {
			qb.setTables(SmsDatabaseHelper.CONVERSATION_TABLE_NAME);
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
		if (count>0) {
			getContext().getContentResolver().notifyChange(arg0, null);
		}
		return count;
	}

}