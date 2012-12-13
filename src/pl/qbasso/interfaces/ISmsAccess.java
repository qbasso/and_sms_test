package pl.qbasso.interfaces;

import java.util.HashSet;
import java.util.List;

import pl.qbasso.models.ConversationModel;
import pl.qbasso.models.SmsModel;
import android.net.Uri;

public interface ISmsAccess {

	public abstract int updateSmsStatus(Uri u, int smsStatus, int smsType);

	public abstract Uri insertSms(SmsModel m);

	public abstract long getThreadIdForSmsUri(Uri u);

	public abstract long getThreadIdForPhoneNumber(String phoneNumber);

	public abstract String getAddressForThreadId(String phoneNumber,
			String displayName);

	public abstract String getDisplayName(String phoneNumber);

	public abstract List<ConversationModel> getThreads(HashSet<Long> needRefresh);

	public abstract List<SmsModel> getSmsForThread(long threadId);

	public abstract SmsModel getSingleSms(Uri u);

	public abstract void deleteThread(long threadId);

	public abstract void deleteSms(long smsId);

	public abstract int updateSmsRead(long id, int messageRead);

	public abstract long getDraftIdForThread(long threadId);

	public abstract String getDraftTextForThread(long threadId);

	public abstract int updateDraftMessage(long msgId, String body, long date);

	public abstract int deleteDraftForThread(long threadId);

	public abstract List<SmsModel> getMessagesNotSent();

	public abstract int getUnreadCount();

}