/*
 * @author JPorzuczek
 */
package pl.qbasso.models;

import java.io.Serializable;

// TODO: Auto-generated Javadoc
/**
 * The Class ConversationModel.
 */
public class ConversationModel implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The Constant THREAD_ID. */
	public static final String THREAD_ID = "thread_id";
    
    /** The Constant SNIPPET. */
    public static final String SNIPPET = "snippet";
    
    /** The Constant MESSAGE_COUNT. */
    public static final String MESSAGE_COUNT = "msg_count";
	
	/** The Constant READ. */
	public static final String READ = "read";
	
	public static final String UNREAD = "unread";
	
	public static final String DRAFT = "has_draft";
	
	/** The thread id. */
	private long threadId;
	
	/** The snippet. */
	private String snippet;
	
	private long lastModified;
	
	/** The count. */
	private int count;
	
	/** The address. */
	private String address;
	
	/** The display name. */
	private String displayName;
	
	/** The unread. */
	private int unread = 0;
	
	/** The draft. */
	private boolean draft = false;

	/**
	 * Instantiates a new conversation model.
	 *
	 * @param threadId the thread id
	 * @param count the count
	 * @param snippet the snippet
	 */
	public ConversationModel(long threadId, int count, String snippet) {
		this.threadId = threadId;
		this.snippet = snippet;
		this.count = count;
	}

	public long getThreadId() {
		return threadId;
	}

	public void setThreadId(long threadId) {
		this.threadId = threadId;
	}

	public String getSnippet() {
		return snippet;
	}

	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public int getUnread() {
		return unread;
	}

	public void setUnread(int unread) {
		this.unread = unread;
	}

	public boolean isDraft() {
		return draft;
	}

	public void setDraft(boolean draft) {
		this.draft = draft;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}
}
