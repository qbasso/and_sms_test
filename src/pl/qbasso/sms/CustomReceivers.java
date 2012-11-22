/*
 * @author JPorzuczek
 */
package pl.qbasso.sms;

import pl.qbasso.models.SmsModel;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;

// TODO: Auto-generated Javadoc
/**
 * The Class CustomReceivers.
 */
public class CustomReceivers {
	
	/**
	 * The Class SmsSentReceiver.
	 */
	public static class SmsSentReceiver extends BroadcastReceiver {
		
		/** The m handler. */
		private Handler mHandler = new Handler();

		/* (non-Javadoc)
		 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
		 */
		@Override
		public void onReceive(final Context arg0, Intent intent) {
			SmsModel m = null;
			if (intent.getAction().equals(SmsSendHelper.ACTION_SENT)) {
				handleActionSend(arg0, intent);
			} else if (intent.getAction().equals(SmsSendHelper.ACTION_RESEND)) {
				handleActionResend(arg0, intent);
			}
		}

		/**
		 * Handle action resend.
		 *
		 * @param arg0 the arg0
		 * @param intent the intent
		 */
		private void handleActionResend(final Context arg0, Intent intent) {
			SmsModel m;
			SmsSendHelper h = new SmsSendHelper();
			m = (SmsModel) intent
					.getSerializableExtra(SmsSendHelper.EXTRA_MESSAGE);
			final SmsModel data = m;
			h.sendText(arg0, data, false);
		}

		/**
		 * Handle action send.
		 *
		 * @param arg0 the arg0
		 * @param intent the intent
		 */
		private void handleActionSend(final Context arg0, Intent intent) {
		
			switch (getResultCode()) {
			case Activity.RESULT_OK:
				sendActionOk(arg0, intent);
				break;
			default:
				sendActionFailed(arg0, intent);
				break;
			}
		}

		/**
		 * Send action failed.
		 *
		 * @param arg0 the arg0
		 * @param intent the intent
		 */
		private void sendActionFailed(final Context arg0, Intent intent) {
			SmsModel m;
			SmsDbHelper smsAccessor = new SmsDbHelper(
					arg0.getContentResolver());
			m = (SmsModel) intent
					.getSerializableExtra(SmsSendHelper.EXTRA_MESSAGE);
			Intent updateIntent = new Intent(SmsSendHelper.ACTION_UPDATE);
			m.setSmsType(SmsModel.MESSAGE_TYPE_FAILED);
			final Uri u = smsAccessor.insertSms(SmsDbHelper.SMS_URI, m);
			m.setId(Long.parseLong(u.getPath()));
			if (m.getId() == 0) {
				final SmsModel data = m;
				mHandler.postDelayed(new Runnable() {
					public void run() {
						Intent i = new Intent(
								SmsSendHelper.ACTION_RESEND);
						i.putExtra(SmsSendHelper.EXTRA_MESSAGE, data);
						arg0.sendBroadcast(i);
					}
				}, 5000);
				
			} else {
				smsAccessor.updateSmsStatus(
						Uri.withAppendedPath(SmsDbHelper.SMS_URI,
								String.valueOf(m.getId())),
						SmsModel.STATUS_PENDING,
						SmsModel.MESSAGE_TYPE_SENT);
				updateIntent.putExtra(SmsSendHelper.EXTRA_MESSAGE, m);
				arg0.sendBroadcast(updateIntent);
			}
		}

		/**
		 * Send action ok.
		 *
		 * @param arg0 the arg0
		 * @param intent the intent
		 */
		private void sendActionOk(final Context arg0, Intent intent) {
			SmsModel m;
			SmsDbHelper smsAccessor = new SmsDbHelper(
					arg0.getContentResolver());
			m = (SmsModel) intent
					.getSerializableExtra(SmsSendHelper.EXTRA_MESSAGE);
			Intent updateIntent = new Intent(SmsSendHelper.ACTION_UPDATE);
			m.setSmsType(SmsModel.MESSAGE_TYPE_SENT);
			if (m.getId() == 0) {
				Uri u = smsAccessor.insertSms(SmsDbHelper.SMS_URI, m);
				m = smsAccessor.getSingleSms(u);
			} else {
				smsAccessor.updateSmsStatus(
						Uri.withAppendedPath(SmsDbHelper.SMS_URI,
								String.valueOf(m.getId())),
						SmsModel.STATUS_COMPLETE,
						SmsModel.MESSAGE_TYPE_SENT);
			}
			updateIntent.putExtra(SmsSendHelper.EXTRA_MESSAGE, m);
			updateIntent.putExtra(
					SmsSendHelper.EXTRA_LAUNCH_CONVERSATION,
					intent.getBooleanExtra(
							SmsSendHelper.EXTRA_LAUNCH_CONVERSATION,
							false));
			arg0.sendBroadcast(updateIntent);
		}
	}

	/**
	 * The Class SmsDeliveredReceiver.
	 */
	public class SmsDeliveredReceiver extends BroadcastReceiver {

		/* (non-Javadoc)
		 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
		 */
		@Override
		public void onReceive(Context context, Intent intent) {
			switch (getResultCode()) {
			case Activity.RESULT_OK:
				break;
			default:
				break;
			}
		}

	}
}
