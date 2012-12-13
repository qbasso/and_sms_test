package pl.qbassso.smsdb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SmsDatabaseHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 16;

	private static final String DATABASE_NAME = "pl.qbasso.smsdb";

	public static final String SMS_TABLE_NAME = "text_messages";

	public static final String CONVERSATION_TABLE_NAME = "conversations";

	private static final String CREATE_SMS_TABLE = "create table if not exists "
			+ SMS_TABLE_NAME
			+ " ("
			+ "_id integer primary key autoincrement,"
			+ " thread_id integer not null references conversations (id) on delete cascade,"
			+ " address text not null,"
			+ " date integer,"
			+ " read integer,"
			+ " status integer, type integer," + " body text" + ")";

	private static final String CREATE_CONVERSATION_TABLE = "create table if not exists "
			+ CONVERSATION_TABLE_NAME
			+ "("
			+ "_id integer primary key autoincrement,"
			+ " snippet text,"
			+ " count integer,"
			+ " date integer,"
			+ " unread integer,"
			+ " has_draft integer" + ")";

	private static final String CREATE_SMS_INSERT_TRIGGER = "create trigger if not exists sms_insert_t"
			+ " AFTER INSERT ON "
			+ SMS_TABLE_NAME
			+ " BEGIN "
			+ " update "
			+ CONVERSATION_TABLE_NAME
			+ " set count = count + 1, snippet = new.body, date = new.date where _id = new.thread_id; "
			+ " update "
			+ CONVERSATION_TABLE_NAME
			+ " set unread = unread + 1 where _id = new.tread_id and read = 0; "
			+ "END;";

	private static final String CREATE_SMS_DELETE_TRIGGER = "create trigger if not exists sms_delete_t"
			+ " AFTER DELETE ON "
			+ SMS_TABLE_NAME
			+ " BEGIN "
			+ "update "
			+ CONVERSATION_TABLE_NAME
			+ " set count = count - 1, snippet = "
			+ "(select body from text_messages where date = "
			+ "(select max(date) from text_messages)), date = (select max(date) from text_messages) where _id = old.thread_id; "
			+ "END;";
	
	private static final String CREATE_SMS_UPDATE_TRIGGER = "create trigger if not exists sms_update_t"
			+ " AFTER UPDATE ON "
			+ SMS_TABLE_NAME
			+ " BEGIN "
			+ "update "
			+ CONVERSATION_TABLE_NAME
			+ " set unread = unread - 1 where new.read = 1 and _id = new.thread_id; "
			+ "END;"; 

	public SmsDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_CONVERSATION_TABLE);
		db.execSQL(CREATE_SMS_TABLE);
		db.execSQL(CREATE_SMS_INSERT_TRIGGER);
		db.execSQL(CREATE_SMS_DELETE_TRIGGER);
		db.execSQL(CREATE_SMS_UPDATE_TRIGGER);
		db.execSQL("insert into conversations(snippet, count, date, unread, has_draft) values('czesc', 1, 123214452, 1, 1)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TRIGGER IF EXISTS sms_insert_t");
		db.execSQL("DROP TRIGGER IF EXISTS sms_delete_t");
		db.execSQL("DROP TRIGGER IF EXISTS sms_update_t");
		db.execSQL("DROP TABLE IF EXISTS " + SMS_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + CONVERSATION_TABLE_NAME);
		onCreate(db);
	}

}
