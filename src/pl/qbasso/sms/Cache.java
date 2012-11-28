package pl.qbasso.sms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import pl.qbasso.models.ConversationModel;

public class Cache {
	private static Cache instance = new Cache();
	private static LinkedHashMap<Long, Integer> sPositions;
	private static ArrayList<ConversationModel> sConversations;
	private static HashSet<Long> sNeedRefresh;

	public static Cache getInstance() {
		return instance;
	}

	private Cache() {
		sConversations = new ArrayList<ConversationModel>();
		sNeedRefresh = new LinkedHashSet<Long>();
		sPositions = new LinkedHashMap<Long, Integer>();
	}

	public static synchronized void addToRefreshSet(Long threadId) {
		sNeedRefresh.add(threadId);
	}

	public static synchronized void clearRefreshSet() {
		sNeedRefresh.clear();
	}

	public static synchronized void put(ConversationModel m) {
		if (!sPositions.containsKey(m.getThreadId())) {
			for (Integer i : sPositions.values()) {
				i++;
			}
			sConversations.add(0, m);
			sPositions.put(m.getThreadId(), 0);
		}
	}

	public static synchronized void putAll(
			Collection<? extends ConversationModel> c) {
		for (ConversationModel conversationModel : c) {
			sConversations.add(conversationModel);
			sPositions.put(conversationModel.getThreadId(),
					sConversations.size() - 1);
		}
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

	public static synchronized void delete(ConversationModel m) {
		int pos = sPositions.get(m.getThreadId()) != null ? sPositions.get(m
				.getThreadId()) : -1;
		if (pos != -1) {
			sConversations.remove(pos);
			sPositions.remove(m.getThreadId());
		}
	}

	public static boolean needRefresh() {
		return sNeedRefresh.size() > 0 ? true : false;
	}

	public static HashSet<Long> getRefreshList() {
		return sNeedRefresh;
	}

}
