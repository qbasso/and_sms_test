/*
 * @author JPorzuczek
 */
package pl.qbasso.models;

import java.io.Serializable;


// TODO: Auto-generated Javadoc
/**
 * The Class SmsModel.
 */
public class SmsModel implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	// taken from android sdk source
	/** The Constant ID. */
	public static final String ID = "_id";
	
	/** The Constant TYPE. */
	public static final String TYPE = "type";
	
	/** The Constant MESSAGE_TYPE_ALL. */
	public static final int MESSAGE_TYPE_ALL = 0;
	
	/** The Constant MESSAGE_TYPE_INBOX. */
	public static final int MESSAGE_TYPE_INBOX = 1;
	
	/** The Constant MESSAGE_TYPE_SENT. */
	public static final int MESSAGE_TYPE_SENT = 2;
	
	/** The Constant MESSAGE_TYPE_DRAFT. */
	public static final int MESSAGE_TYPE_DRAFT = 3;
	
	/** The Constant MESSAGE_TYPE_OUTBOX. */
	public static final int MESSAGE_TYPE_OUTBOX = 4;
	
	/** The Constant MESSAGE_TYPE_FAILED. */
	public static final int MESSAGE_TYPE_FAILED = 5;
	
	/** The Constant MESSAGE_TYPE_QUEUED. */
	public static final int MESSAGE_TYPE_QUEUED = 6;
	
	/** The Constant THREAD_ID. */
	public static final String THREAD_ID = "thread_id";
	
	/** The Constant ADDRESS. */
	public static final String ADDRESS = "address";
	
	/** The Constant PERSON_ID. */
	public static final String PERSON_ID = "person";
	
	/** The Constant DATE. */
	public static final String DATE = "date";
	
	/** The Constant DATE_SENT. */
	public static final String DATE_SENT = "date_sent";
	
	/** The Constant READ. */
	public static final String READ = "read";
	
	/** The Constant SEEN. */
	public static final String SEEN = "seen";
	
	/** The Constant STATUS. */
	public static final String STATUS = "status";
	
	/** The Constant STATUS_NONE. */
	public static final int STATUS_NONE = -1;
	
	/** The Constant STATUS_COMPLETE. */
	public static final int STATUS_COMPLETE = 0;
	
	/** The Constant STATUS_WAITING. */
	public static final int STATUS_WAITING = 2;
	
	/** The Constant STATUS_PENDING. */
	public static final int STATUS_PENDING = 32;
	
	/** The Constant STATUS_FAILED. */
	public static final int STATUS_FAILED = 64;
	
	/** The Constant MESSAGE_READ. */
	public static final int MESSAGE_READ = 1;
	
	/** The Constant MESSAGE_NOT_READ. */
	public static final int MESSAGE_NOT_READ = 0;
	
	/** The Constant SUBJECT. */
	public static final String SUBJECT = "subject";
	
	/** The Constant BODY. */
	public static final String BODY = "body";
	
	/** The Constant PERSON. */
	public static final String PERSON = "person";
	
	/** The Constant PROTOCOL. */
	public static final String PROTOCOL = "protocol";
	
	/** The Constant REPLY_PATH_PRESENT. */
	public static final String REPLY_PATH_PRESENT = "reply_path_present";
	
	/** The Constant SERVICE_CENTER. */
	public static final String SERVICE_CENTER = "service_center";
	
	/** The Constant LOCKED. */
	public static final String LOCKED = "locked";
	
	/** The Constant ERROR_CODE. */
	public static final String ERROR_CODE = "error_code";
	
	/** The Constant ASCII_SMS_LENGTH. */
	public static final int ASCII_SMS_LENGTH = 160;
	
	/** The Constant UNICODE_SMS_LENGTH. */
	public static final int UNICODE_SMS_LENGTH = 70;
	
	/** The Constant MULTIPART_SMS_PENALTY_ASCII. */
	public static final int MULTIPART_SMS_PENALTY_ASCII = 7;
	
	/** The Constant MULTIPART_SMS_PENALTY_UNICODE. */
	public static final int MULTIPART_SMS_PENALTY_UNICODE = 3;

	/** The id. */
	private long id;
	
	/** The thread id. */
	private long threadId;
	
	/** The address. */
	private String address;
	
	/** The person. */
	private String person;
	
	/** The date. */
	private long date;
	
	/** The body. */
	private String body;
	
	/** The sms type. */
	private int smsType;
	
	/** The address display name. */
	private String addressDisplayName;
	
	/** The read. */
	private int read;
	
	/** The status. */
	private int status;

	/**
	 * Instantiates a new sms model.
	 */
	public SmsModel() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new sms model.
	 *
	 * @param id the id
	 * @param threadId the thread id
	 * @param address the address
	 * @param person the person
	 * @param date the date
	 * @param body the body
	 * @param type the type
	 * @param read the read
	 * @param status the status
	 */
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
