/*
 * @author JPorzuczek
 */
package pl.qbasso.sms;

import java.util.ArrayList;

import pl.qbasso.interfaces.OnMessageSendCompleteListener;
import pl.qbasso.models.SmsModel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

// TODO: Auto-generated Javadoc
/**
 * The Class SmsSendHelper.
 */
public class SmsSendHelper {

	/** The mgr. */
	private SmsManager mgr;
	
	/** The delivered. */
	private PendingIntent delivered;
	
	/** The sent. */
	private PendingIntent sent;
	
	/** The Constant ACTION_SENT. */
	public static final String ACTION_SENT = "pl.qbasso.SENT";
	
	/** The Constant ACTION_DELIVERED. */
	public static final String ACTION_DELIVERED = "pl.qbasso.DELIVERED";
	
	/** The Constant ACTION_RESEND. */
	public static final String ACTION_RESEND = "pl.qbasso.RESEND";
	
	/** The Constant ACTION_UPDATE. */
	public static final String ACTION_UPDATE = "pl.qbasso.UPDATE";
	
	/** The Constant EXTRA_MESSAGE. */
	public static final String EXTRA_MESSAGE = "pl.qbasso.MESSAGE_EXTRA";
	
	/** The Constant EXTRA_MESSAGE_ID. */
	public static final String EXTRA_MESSAGE_ID = "pl.qbasso.MESSAGE_ID_EXTRA";
	
	/** The Constant EXTRA_LAUNCH_CONVERSATION. */
	public static final String EXTRA_LAUNCH_CONVERSATION = "pl.qbasso.LAUNCH_CONVERSATION_EXTRA";

	/**
	 * Instantiates a new sms send helper.
	 */
	public SmsSendHelper() {
		this.mgr = SmsManager.getDefault();
	}

	/** The on message send complete listener. */
	private OnMessageSendCompleteListener onMessageSendCompleteListener;

	/**
	 * Send text.
	 *
	 * @param ctx the ctx
	 * @param m the m
	 * @param launchConversation the launch conversation
	 */
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

	/**
	 * Inits the pending intents.
	 *
	 * @param ctx the ctx
	 * @param m the m
	 * @param launchConversation the launch conversation
	 */
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
