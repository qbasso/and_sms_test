package pl.qbasso.sms;

import java.util.ArrayList;

import pl.qbasso.interfaces.OnMessageSendCompleteListener;
import pl.qbasso.models.SmsModel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

public class SmsSendHelper {

	private SmsManager mgr;
	private PendingIntent delivered;
	private PendingIntent sent;
	public static final String ACTION_SENT = "pl.qbasso.SENT";
	public static final String ACTION_DELIVERED = "pl.qbasso.DELIVERED";
	public static final String ACTION_RESEND = "pl.qbasso.RESEND";
	public static final String ACTION_UPDATE = "pl.qbasso.UPDATE";
	public static final String EXTRA_MESSAGE = "pl.qbasso.MESSAGE_EXTRA";
	public static final String EXTRA_MESSAGE_ID = "pl.qbasso.MESSAGE_ID_EXTRA";
	public static final String EXTRA_LAUNCH_CONVERSATION = "pl.qbasso.LAUNCH_CONVERSATION_EXTRA";

	public SmsSendHelper() {
		this.mgr = SmsManager.getDefault();
	}

	private OnMessageSendCompleteListener onMessageSendCompleteListener;

	public void sendText(Context ctx, final SmsModel m,
			boolean launchConversation) {
		initPendingIntents(ctx, m, launchConversation);
		if (m.getBody().length() > 160) {
			ArrayList<PendingIntent> si = new ArrayList<PendingIntent>();
			si.add(sent);
			ArrayList<PendingIntent> di = new ArrayList<PendingIntent>();
			di.add(delivered);
			mgr.sendMultipartTextMessage(m.getAddress(), null,
					mgr.divideMessage(m.getBody()), si, di);
		} else {
			mgr.sendTextMessage(m.getAddress(), null, m.getBody(), sent,
					delivered);
		}
		if (onMessageSendCompleteListener != null) {
			onMessageSendCompleteListener.messageSendComplete(true);
		}
	}

	private void initPendingIntents(final Context ctx, final SmsModel m,
			boolean launchConversation) {
		Intent i = new Intent(ACTION_SENT);
		i.putExtra(EXTRA_MESSAGE, m);
		i.putExtra(EXTRA_LAUNCH_CONVERSATION, launchConversation);
		sent = PendingIntent.getBroadcast(ctx, 0, i,
				PendingIntent.FLAG_UPDATE_CURRENT);
		delivered = PendingIntent.getBroadcast(ctx, 0, new Intent(
				ACTION_DELIVERED), PendingIntent.FLAG_UPDATE_CURRENT);
	}

	public void setOnMessageSendCompleteListener(
			OnMessageSendCompleteListener listener) {
		onMessageSendCompleteListener = listener;

	}
}
