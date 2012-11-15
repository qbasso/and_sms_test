package pl.qbasso.models;

import java.io.Serializable;


public class SmsModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// taken from android sdk source
	public static final String ID = "_id";
	public static final String TYPE = "type";
	public static final int MESSAGE_TYPE_ALL = 0;
	public static final int MESSAGE_TYPE_INBOX = 1;
	public static final int MESSAGE_TYPE_SENT = 2;
	public static final int MESSAGE_TYPE_DRAFT = 3;
	public static final int MESSAGE_TYPE_OUTBOX = 4;
	public static final int MESSAGE_TYPE_FAILED = 5;
	public static final int MESSAGE_TYPE_QUEUED = 6;
	public static final String THREAD_ID = "thread_id";
	public static final String ADDRESS = "address";
	public static final String PERSON_ID = "person";
	public static final String DATE = "date";
	public static final String DATE_SENT = "date_sent";
	public static final String READ = "read";
	public static final String SEEN = "seen";
	public static final String STATUS = "status";
	public static final int STATUS_NONE = -1;
	public static final int STATUS_COMPLETE = 0;
	public static final int STATUS_WAITING = 2;
	public static final int STATUS_PENDING = 32;
	public static final int STATUS_FAILED = 64;
	public static final int MESSAGE_READ = 1;
	public static final int MESSAGE_NOT_READ = 0;
	public static final String SUBJECT = "subject";
	public static final String BODY = "body";
	public static final String PERSON = "person";
	public static final String PROTOCOL = "protocol";
	public static final String REPLY_PATH_PRESENT = "reply_path_present";
	public static final String SERVICE_CENTER = "service_center";
	public static final String LOCKED = "locked";
	public static final String ERROR_CODE = "error_code";
	public static final int ASCII_SMS_LENGTH = 160;
	public static final int UNICODE_SMS_LENGTH = 70;

	private long id;
	private long threadId;
	private String address;
	private String person;
	private long date;
	private String body;
	private int smsType;
	private String addressDisplayName;
	private int read;
	private int status;

	public SmsModel() {
		// TODO Auto-generated constructor stub
	}

	public SmsModel(long id, long threadId, String address, String person,
			long date, String body, int type, int read, int status) {
		this.id = id;
		this.address = address;
		this.body = body;
		this.date = date;
		this.person = person;
		this.threadId = threadId;
		this.smsType = type;
		this.read = read;
		this.status = status;
	}

	public long getThreadId() {
		return threadId;
	}

	public void setThreadId(long threadId) {
		this.threadId = threadId;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPerson() {
		return person;
	}

	public void setPerson(String person) {
		this.person = person;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getSmsType() {
		return smsType;
	}

	public void setSmsType(int smsType) {
		this.smsType = smsType;
	}

	public String getAddressDisplayName() {
		return addressDisplayName;
	}

	public void setAddressDisplayName(String addressDisplayName) {
		this.addressDisplayName = addressDisplayName;
	}

	public int getRead() {
		return read;
	}

	public void setRead(int read) {
		this.read = read;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
}
