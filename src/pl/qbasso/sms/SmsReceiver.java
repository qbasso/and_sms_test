/*
 * @author JPorzuczek
 */
package pl.qbasso.sms;

import java.util.ArrayList;

import pl.qbasso.activities.SmsConversationActivity;
import pl.qbasso.models.ConversationModel;
import pl.qbasso.models.SmsModel;
import pl.qbasso.smssender.R;
import android.app.Activity;
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

	/** The nm. */
	private static NotificationManager nm;

	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context ctx, Intent i) {
		SmsMessage msg = null;
		String sender;
		String body;
		long threadId = 0;
		SmsDbHelper smsDb = new SmsDbHelper(ctx.getContentResolver());
		if (nm == null) {
			nm = (NotificationManager) ctx
					.getSystemService(Activity.NOTIFICATION_SERVICE);
		}
		if (i.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
			Bundle bundle = i.getExtras();
			if (bundle != null) {
				Object[] pdus = (Object[]) bundle.get("pdus");
				for (Object m : pdus) {
					msg = SmsMessage.createFromPdu((byte[]) m);
					sender = smsDb.getDisplayName(msg.getOriginatingAddress());
					threadId = smsDb.getThreadIdForPhoneNumber(msg
							.getOriginatingAddress());
					body = msg.getMessageBody();
					SmsModel model = new SmsModel(0, threadId, sender, "",
							System.currentTimeMillis(), body,
							SmsModel.MESSAGE_TYPE_INBOX,
							SmsModel.MESSAGE_NOT_READ, SmsModel.STATUS_COMPLETE);
					Uri u = smsDb.insertSms(SmsDbHelper.SMS_URI, model);
					if (threadId == -1) {
						threadId = smsDb.getThreadIdForSmsUri(u);
					}
					Notification n = prepareNotification(ctx, msg, threadId,
							sender);
					nm.notify(sender, 0, n);
					Intent intent = new Intent("pl.qbasso.smssender.new_message_arrived");
					intent.putExtra("thread_id", threadId);
					ctx.sendBroadcast(intent);
				}
			}
			this.abortBroadcast();
		}

	}

	/**
	 * Prepare notification.
	 *
	 * @param ctx the ctx
	 * @param msg the msg
	 * @param threadId the thread id
	 * @param sender the sender
	 * @return the notification
	 */
	private Notification prepareNotification(Context ctx, SmsMessage msg,
			long threadId, String sender) {
		Notification n = new Notification();
		Intent pendingIntent = new Intent(ctx, SmsConversationActivity.class);
		ConversationModel threadModel = new ConversationModel(threadId, 0, "");
		threadModel.setDisplayName(sender);
		threadModel.setAddress(msg.getOriginatingAddress());
		ArrayList<ConversationModel> list = new ArrayList<ConversationModel>();
		list.add(threadModel);
		pendingIntent.putExtra("threadList", list.toArray());
		pendingIntent.putExtra("threadNumber", 0);
		PendingIntent pi = PendingIntent.getActivity(ctx, 0, pendingIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		n.icon = R.drawable.ic_launcher;
		n.defaults |= Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND;
		n.ledARGB = 0xff0000ff;
		n.ledOffMS = 1500;
		n.ledOnMS = 200;
		n.tickerText = msg.getMessageBody().length() > 50 ? msg
				.getMessageBody().substring(0, 50) + "..." : msg
				.getMessageBody();
		n.flags |= Notification.FLAG_AUTO_CANCEL;
		n.setLatestEventInfo(ctx, sender,
				msg.getMessageBody().length() > 50 ? msg.getMessageBody()
						.substring(0, 50) + "..." : msg.getMessageBody(), pi);
		return n;
	}

}
