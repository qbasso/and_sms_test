/*
 * @author JPorzuczek
 */
package pl.qbasso.sms;

import java.util.ArrayList;

import pl.qbasso.activities.ConversationList;
import pl.qbasso.activities.SmsConversationActivity;
import pl.qbasso.models.ConversationModel;
import pl.qbasso.models.SmsModel;
import pl.qbasso.smssender.R;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;

// TODO: Auto-generated Javadoc
/**
 * The Class SmsReceiver.
 */
public class SmsReceiver extends BroadcastReceiver {

	public static final String ACTION_UPDATE = "pl.qbasso.UPDATE";
	public static final String EXTRA_PDUS = "pdus";
	public static final String THREAD_NUMBER = "threadNumber";
	public static final String EXTRA_THREAD_LIST = "threadList";
	public static final String EXTRA_SENDER_DISPLAY_NAME = "sender_display_name";
	public static final String EXTRA_THREAD_ID = "thread_id";
	public static final String EXTRA_SENDER_ADDRESS = "sender_address";
	public static final String EXTRA_MESSAGE_BODY = "message_body";
	public static final String ACTION_RECEIVE_SMS = "android.provider.Telephony.SMS_RECEIVED";
	public static final String ACTION_CANCEL_LIGHT = "pl.qbasso.sms.smsreceiver.CANCEL_LED";
	private static NotificationManager nm;
	private static AlarmManager am;
	private static SmsDbHelper smsDb;
	private static final int NOTIFICATION_ID = 1;
	public static final String EXTRA_CANCEL_ALARM = "cancel_alarm";

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
	 * android.content.Intent)
	 */
	@Override
	public void onReceive(Context ctx, Intent i) {
		SmsMessage msg = null;
		String sender;
		String body;
		long threadId = 0;
		if (smsDb == null) {
			smsDb = new SmsDbHelper(ctx.getContentResolver());
		}
		if (nm == null) {
			nm = (NotificationManager) ctx
					.getSystemService(Activity.NOTIFICATION_SERVICE);
		}
		if (am == null) {
			am = (AlarmManager) ctx.getSystemService(Activity.ALARM_SERVICE);
		}
		if (i.getAction().equals(ACTION_RECEIVE_SMS)) {
			Bundle bundle = i.getExtras();
			am.cancel(PendingIntent.getBroadcast(ctx, 0, new Intent(
					ACTION_CANCEL_LIGHT), 0));
			if (bundle != null) {
				Object[] pdus = (Object[]) bundle.get(EXTRA_PDUS);
				for (Object m : pdus) {
					msg = SmsMessage.createFromPdu((byte[]) m);
					sender = smsDb.getDisplayName(msg.getOriginatingAddress());
					threadId = smsDb.getThreadIdForPhoneNumber(msg
							.getOriginatingAddress());
					body = msg.getMessageBody();
					SmsModel model = new SmsModel(0, threadId,
							msg.getOriginatingAddress(), "",
							System.currentTimeMillis(), body,
							SmsModel.MESSAGE_TYPE_INBOX,
							SmsModel.MESSAGE_NOT_READ, SmsModel.STATUS_COMPLETE);
					model.setAddressDisplayName(sender);
					Uri u = smsDb.insertSms(SmsDbHelper.SMS_URI, model);
					if (threadId == -1) {
						threadId = smsDb.getThreadIdForSmsUri(u);
					}
					Notification n = prepareNotification(ctx,
							msg.getMessageBody(), msg.getOriginatingAddress(),
							threadId, sender, true);

					nm.notify(null, NOTIFICATION_ID, n);
					Intent intent = new Intent(ACTION_UPDATE);
					Cache.delete(threadId);
					Cache.addToRefreshSet(threadId);
					intent.putExtra(EXTRA_THREAD_ID, threadId);
					ctx.sendBroadcast(intent);
				}
			}
			this.abortBroadcast();
		} else if (i.getAction().equals(ACTION_CANCEL_LIGHT)) {
			Notification n = prepareNotification(ctx,
					i.getStringExtra(EXTRA_MESSAGE_BODY),
					i.getStringExtra(EXTRA_SENDER_ADDRESS),
					i.getLongExtra(EXTRA_THREAD_ID, 0),
					i.getStringExtra(EXTRA_SENDER_DISPLAY_NAME), false);
			nm.notify(null, NOTIFICATION_ID, n);
		}

	}

	/**
	 * Prepare notification.
	 * 
	 * @param ctx
	 *            the ctx
	 * @param msg
	 *            the msg
	 * @param threadId
	 *            the thread id
	 * @param senderDisplayName
	 *            the sender
	 * @return the notification
	 */
	private Notification prepareNotification(Context ctx, String messageBody,
			String senderAddress, long threadId, String senderDisplayName,
			boolean lightOn) {
		PendingIntent pi = null;
		Notification n = new Notification();
		Intent smsConversationIntent = new Intent(ctx,
				SmsConversationActivity.class);
		Intent conversationListIntent = new Intent(ctx, ConversationList.class);
		ConversationModel threadModel = new ConversationModel(threadId, 0, "");
		threadModel.setDisplayName(senderDisplayName);
		threadModel.setAddress(senderAddress);
		ArrayList<ConversationModel> list = new ArrayList<ConversationModel>();
		list.add(threadModel);
		smsConversationIntent.putExtra(EXTRA_THREAD_LIST, list.toArray());

		n.icon = R.drawable.ic_launcher;
		n.defaults |= Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND;
		n.flags |= Notification.FLAG_AUTO_CANCEL;
		int number = smsDb.getUnreadCount();
		if (number < 2) {
			smsConversationIntent.putExtra(THREAD_NUMBER, 0);
			// TODO probably not working good enoug when multiple messages will
			// arrive
			smsConversationIntent.putExtra(EXTRA_CANCEL_ALARM, true);
			pi = PendingIntent.getActivity(ctx, 0, smsConversationIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			n.setLatestEventInfo(ctx, senderDisplayName,
					messageBody.length() > 50 ? messageBody.substring(0, 50)
							+ "..." : messageBody, pi);
		} else {
			n.number = number;
			conversationListIntent.putExtra(EXTRA_CANCEL_ALARM, true);
			pi = PendingIntent.getActivity(ctx, 0, conversationListIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			n.setLatestEventInfo(ctx, "Nowe wiadomości!",
					String.format("Masz %s nowych wiadomości", number), pi);

		}
		if (lightOn) {
			Intent intent = new Intent(ACTION_CANCEL_LIGHT);
			intent.putExtra(EXTRA_THREAD_ID, threadId);
			intent.putExtra(EXTRA_SENDER_DISPLAY_NAME, senderDisplayName);
			intent.putExtra(EXTRA_MESSAGE_BODY, messageBody);
			intent.putExtra(EXTRA_SENDER_ADDRESS, senderAddress);
			PendingIntent cancelLightIntent = PendingIntent.getBroadcast(ctx,
					0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			AlarmManager am = (AlarmManager) ctx
					.getSystemService(Activity.ALARM_SERVICE);
			am.set(AlarmManager.RTC,
					System.currentTimeMillis() + 1000 * 2 * 60,
					cancelLightIntent);
			n.flags |= Notification.FLAG_SHOW_LIGHTS;
			n.ledARGB = 0x004800ff;
			n.ledOffMS = 2000;
			n.ledOnMS = 100;
		}
		n.tickerText = messageBody.length() > 50 ? messageBody.substring(0, 50)
				+ "..." : messageBody;

		return n;
	}
}
