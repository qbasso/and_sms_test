package pl.qbassso.smsdb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SmsDatabaseHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 48;

	private static final String DATABASE_NAME = "pl.qbasso.smsdb";

	public static final String SMS_TABLE_NAME = "text_messages";

	public static final String CONVERSATION_TABLE_NAME = "conversations";

	private static final String CREATE_SMS_TABLE = "create table if not exists "
			+ SMS_TABLE_NAME
			+ " ("
			+ "_id integer primary key autoincrement,"
			+ " thread_id integer not null references conversations (thread_id) on delete cascade deferrable initially deferred,"
			+ " address text not null,"
			+ " date integer,"
			+ " read integer,"
			+ " status integer default 0, type integer default 0,"
			+ " body text" + ")";

	private static final String CREATE_CONVERSATION_TABLE = "create table if not exists "
			+ CONVERSATION_TABLE_NAME
			+ "("
			+ "thread_id integer primary key autoincrement,"
			+ " snippet text,"
			+ " msg_count integer,"
			+ " date integer,"
			+ " unread integer,"
			+ " has_draft integer" + ")";

	private static final String CREATE_SMS_BEFORE_INSERT_TRIGGER = "create trigger if not exists sms_before_insert_t"
			+ " BEFORE INSERT ON "
			+ SMS_TABLE_NAME
			+ " when new.thread_id = -1"
			+ " BEGIN "
			+ "insert into text_log(log_message) values('sms_before_insert'); "
			+ " insert into "
			+ CONVERSATION_TABLE_NAME
			+ "(snippet, msg_count, date, unread, has_draft) values(new.body, 1, new.date, 0, 0);"
			+ "END;";

	private static final String CREATE_SMS_AFTER_INSERT_TRIGGER = "create trigger if not exists sms_after_insert_t"
			+ " AFTER INSERT ON "
			+ SMS_TABLE_NAME
			+ " BEGIN "
			+ "insert into text_log(log_message) values('sms_after_insert'); "
			+ " update "
			+ SMS_TABLE_NAME
			+ " set thread_id=(select max(thread_id) from conversations) where thread_id = -1; "
			+ " update "
			+ CONVERSATION_TABLE_NAME
			+ " set snippet=new.body, date=new.date where thread_id = new.thread_id; "
			+ " update "
			+ CONVERSATION_TABLE_NAME
			+ " set msg_count=msg_count+1 where thread_id = new.thread_id and new.type <> 3; "
			+ "update "
			+ CONVERSATION_TABLE_NAME
			+ " set has_draft = 1 where thread_id = new.thread_id and new.type = 3; "
			+ "update "
			+ CONVERSATION_TABLE_NAME
			+ " set unread = unread + 1 where thread_id = new.thread_id and new.read = 0; "
			+ "END;";

	private static final String CREATE_SMS_DELETE_TRIGGER = "create trigger if not exists sms_delete_t"
			+ " AFTER DELETE ON "
			+ SMS_TABLE_NAME
			+ " BEGIN "
			+ "update "
			+ CONVERSATION_TABLE_NAME
			+ " set snippet = "
			+ "(select body from text_messages where date = "
			+ "(select max(date) from text_messages where thread_id = old.thread_id)), date = (select max(date) from text_messages where thread_id=old.thread_id) where thread_id = old.thread_id; "
			+ "update "
			+ CONVERSATION_TABLE_NAME
			+ " set msg_count = msg_count - 1"
			+ " where thread_id = old.thread_id and old.type <> 3; "
			+ "update "
			+ CONVERSATION_TABLE_NAME
			+ " set has_draft = 0 where thread_id = old.thread_id and old.type = 3; "
			+ "END;";

	private static final String CREATE_SMS_UPDATE_TRIGGER = "create trigger if not exists sms_update_t"
			+ " AFTER UPDATE ON "
			+ SMS_TABLE_NAME
			+ " BEGIN "
			+ "insert into text_log(log_message) values('sms_update_insert'); "
			+ "update "
			+ CONVERSATION_TABLE_NAME
			+ " set has_draft = 0, snippet ="
			+ "(select body from text_messages where _id = new.thread_id and date = "
			+ "(select max(date) from text_messages where _id = new.thread_id))"
			+ " where thread_id = new.thread_id and old.type = 3 and new.type <> 3; "
			+ "update "
			+ CONVERSATION_TABLE_NAME
			+ " set unread = unread-1 where old.read = 0 and new.read = 1 and thread_id = old.thread_id and unread > 0; "
			+ "END;";

	private static final String CREATE_LOG_TABLE = "create table if not exists text_log(_id integer primary key, log_message text)";

	public SmsDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_CONVERSATION_TABLE);
		db.execSQL(CREATE_SMS_TABLE);
		db.execSQL(CREATE_LOG_TABLE);
		db.execSQL(CREATE_SMS_BEFORE_INSERT_TRIGGER);
		db.execSQL(CREATE_SMS_AFTER_INSERT_TRIGGER);
		db.execSQL(CREATE_SMS_DELETE_TRIGGER);
		db.execSQL(CREATE_SMS_UPDATE_TRIGGER);
		// db.execSQL("insert into conversations(snippet, count, date, unread, has_draft) values('czesc', 0, 123214452, 0, 0)");
		// db.execSQL("insert into text_messages(thread_id, address, date, read, status, type, body) values(1, '791287139', 123214452, 1, 1, 1, 'Czesc')");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TRIGGER IF EXISTS sms_before_insert_t");
		db.execSQL("DROP TRIGGER IF EXISTS sms_after_insert_t");
		db.execSQL("DROP TRIGGER IF EXISTS sms_instead_insert_t");
		db.execSQL("DROP TRIGGER IF EXISTS sms_delete_t");
		db.execSQL("DROP TABLE IF EXISTS text_log");
		db.execSQL("DROP TABLE IF EXISTS " + SMS_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + CONVERSATION_TABLE_NAME);
		onCreate(db);
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		if (!db.isReadOnly()) {
			db.execSQL("PRAGMA foreign_keys = on;");
		}
	}

}
