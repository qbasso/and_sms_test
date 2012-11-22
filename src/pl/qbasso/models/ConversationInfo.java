/*
 * @author JPorzuczek
 */
package pl.qbasso.models;

import java.io.Serializable;

// TODO: Auto-generated Javadoc
/**
 * The Class ConversationInfo.
 */
public class ConversationInfo implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The address. */
	private String address;
	
	/** The thread id. */
	private long threadId;
	
	/** The display name. */
	private String displayName;

	/**
	 * Instantiates a new conversation info.
	 */
	public ConversationInfo() {
	}
	
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public long getThreadId() {
		return threadId;
	}

	public void setThreadId(long threadId) {
		this.threadId = threadId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

}
