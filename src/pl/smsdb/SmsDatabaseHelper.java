package pl.smsdb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SmsDatabaseHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;

	private static final String DATABASE_NAME = "pl.qbasso.smsdb";

	public static final String SMS_TABLE_NAME = "text_messages";

	public static final String CONVERSATION_TABLE_NAME = "conversations";

	private static final String CREATE_SMS_TABLE = "create table if not exists "
			+ SMS_TABLE_NAME
			+ "("
			+ "id integer primary key not null autoincrement,"
			+ " thread_id integer not null references conversations (id) on delete cascade,"
			+ " address text not null,"
			+ " date integer,"
			+ " read integer,"
			+ " status integer," + " body text" + ")";

	private static final String CREATE_CONVERSATION_TABLE = "create table if not exists"
			+ CONVERSATION_TABLE_NAME
			+ "("
			+ "id integer primary key not null autoincrement,"
			+ " snippet text,"
			+ " count integer,"
			+ " unread integer,"
			+ " has_draft integer" + ")";

	public SmsDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_CONVERSATION_TABLE);
		db.execSQL(CREATE_SMS_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
