package pl.qbasso.sms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;

import pl.qbasso.models.ConversationModel;

public class Cache {
	private static Cache instance = new Cache();
	private static LinkedHashMap<Long, Integer> sPositions;
	private static LinkedHashMap<Long, Boolean> sRefreshType;
	private static ArrayList<ConversationModel> sConversations;
	private static HashSet<Long> sNeedRefresh;

	public static Cache getInstance() {
		return instance;
	}

	private Cache() {
		sConversations = new ArrayList<ConversationModel>();
		sNeedRefresh = new LinkedHashSet<Long>();
		sPositions = new LinkedHashMap<Long, Integer>();
		sRefreshType = new LinkedHashMap<Long, Boolean>();
	}

	public static synchronized void addToRefreshSet(Long threadId, boolean start) {
		sNeedRefresh.add(threadId);
		sRefreshType.put(threadId, start);
	}

	public static synchronized void clearRefreshSet() {
		sNeedRefresh.clear();
	}

	public static synchronized void put(ConversationModel m, boolean start) {
		if (start) {
			delete(m.getThreadId());
			sConversations.add(0, m);
			rebuildPositions();
		} else {
			int pos = sPositions.get(m.getThreadId());
			sConversations.remove(pos);
			sConversations.add(pos, m);
		}
	}

	public static synchronized void putAll(
			Collection<? extends ConversationModel> c) {
		sPositions.clear();
		sConversations.clear();
		sRefreshType.clear();
		for (ConversationModel conversationModel : c) {
			sConversations.add(conversationModel);
			sPositions.put(conversationModel.getThreadId(),
					sConversations.size() - 1);
		}
	}

	public static synchronized void putInOrder(
			Collection<? extends ConversationModel> c) {
		Object[] models = c.toArray();
		for (int i=models.length-1; i>=0; i--) {
			ConversationModel m = (ConversationModel) models[i];
			Boolean b = sRefreshType.get(m.getThreadId());
			put(m, b != null ? b : true);
		}
		sRefreshType.clear();
		sNeedRefresh.clear();
	}

	public static synchronized void putAllAtBeginnig(
			Collection<? extends ConversationModel> c) {
		int size = c.size();
		int i = 0;
		for (Integer pos : sPositions.values()) {
			pos += size;
		}
		for (ConversationModel conversationModel : c) {
			sConversations.add(i, conversationModel);
			sPositions.put(conversationModel.getThreadId(), i);
			i++;
		}
	}

	public static synchronized ConversationModel get(long threadId) {
		return sConversations.get(sPositions.get(threadId));
	}

	public static synchronized List<ConversationModel> getAll() {
		return sConversations;
	}
	
	public static synchronized void delete(long threadId) {
		int pos = sPositions.get(threadId) != null ? sPositions.get(threadId)
				: -1;
		if (pos != -1) {
			sConversations.remove(pos);
			sPositions.remove(threadId);
		}
	}

	public static boolean needRefresh() {
		return sNeedRefresh.size() > 0 ? true : false;
	}

	public static HashSet<Long> getRefreshList() {
		return sNeedRefresh;
	}
	
	private static void rebuildPositions() {
		sPositions.clear();
		int i = 0;
		for (ConversationModel m : sConversations) {
			sPositions.put(m.getThreadId(), i++);
		}
	}

}
