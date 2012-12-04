package pl.smsdb;

import pl.qbasso.models.ConversationModel;
import pl.qbasso.models.SmsModel;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class SmsProvider extends ContentProvider {

	private static UriMatcher sMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	private SmsDatabaseDAO mDb;

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		mDb = new SmsDatabaseDAO(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri arg0, String[] arg1, String arg2, String[] arg3,
			String arg4) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		return 0;
	}

	private static class SmsDatabaseDAO {
		private SmsDatabaseHelper mDbHelper;

		public SmsDatabaseDAO(Context ctx) {
			mDbHelper = new SmsDatabaseHelper(ctx);
		}

		public long createConversation(ContentValues cv) {
			SQLiteDatabase db = mDbHelper.getWritableDatabase();
			return db.insert(SmsDatabaseHelper.CONVERSATION_TABLE_NAME, null,
					cv);
		}

		public int deleteConversation(long conversationId) {
			SQLiteDatabase db = mDbHelper.getWritableDatabase();
			return db.delete(SmsDatabaseHelper.CONVERSATION_TABLE_NAME,
					ConversationModel.THREAD_ID + "= ?",
					new String[] { String.valueOf(conversationId) });
		}

		public long createSms(ContentValues cv) {
			SQLiteDatabase db = mDbHelper.getWritableDatabase();
			return db.insert(SmsDatabaseHelper.SMS_TABLE_NAME, null, cv);
		}

		public int deleteSms(long smsId) {
			SQLiteDatabase db = mDbHelper.getWritableDatabase();
			return db.delete(SmsDatabaseHelper.SMS_TABLE_NAME, SmsModel.ID
					+ "= ?", new String[] { String.valueOf(smsId) });
		}

	}

}
