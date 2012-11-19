package pl.qbasso.models;

import java.io.Serializable;

public class ConversationModel implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String THREAD_ID = "thread_id";
    public static final String SNIPPET = "snippet";
    public static final String MESSAGE_COUNT = "msg_count";
	public static final String READ = "read";
	private long threadId;
	private String snippet;
	private int count;
	private String address;
	private String displayName;
	private int unread = 0;
	private boolean draft = false;

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
}
