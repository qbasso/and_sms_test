/*
 * @author JPorzuczek
 */
package pl.qbasso.sms;

import pl.qbasso.activities.AppConstants;
import pl.qbasso.interfaces.ISmsAccess;
import pl.qbasso.models.SmsModel;
import pl.qbassso.smsdb.CustomSmsDbHelper;
import pl.qbassso.smsdb.DefaultSmsProviderHelper;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

/**
 * The Class CustomReceivers.
 */
public class CustomReceivers {

	/**
	 * The Class SmsSentReceiver.
	 */
	public static class SmsSentReceiver extends BroadcastReceiver {

		private static final String TAG = "SmsSentReceiver";
		/** The m handler. */
		private Handler mHandler = new Handler();

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.content.BroadcastReceiver#onReceive(android.content.Context,
		 * android.content.Intent)
		 */
		@Override
		public void onReceive(final Context arg0, Intent intent) {
			if (intent.getAction().equals(SmsSendHelper.ACTION_SENT)) {
				handleActionSend(arg0, intent);
			} else if (intent.getAction().equals(SmsSendHelper.ACTION_RESEND)) {
				handleActionResend(arg0, intent);
			}
		}

		/**
		 * Handle action resend.
		 * 
		 * @param arg0
		 *            the arg0
		 * @param intent
		 *            the intent
		 */
		private void handleActionResend(final Context arg0, Intent intent) {
			SmsModel m;
			SmsSendHelper h = new SmsSendHelper();
			m = (SmsModel) intent
					.getSerializableExtra(SmsSendHelper.EXTRA_MESSAGE);
			final SmsModel data = m;
			h.sendText(arg0, data, false);
			Log.i(TAG, String.format(
					"Attept to resend message %s:%s",
					m.getAddressDisplayName() != null ? m
							.getAddressDisplayName() : m.getAddress(), m
							.getBody()));
		}

		/**
		 * Handle action send.
		 * 
		 * @param arg0
		 *            the arg0
		 * @param intent
		 *            the intent
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
		 * @param arg0
		 *            the arg0
		 * @param intent
		 *            the intent
		 */
		private void sendActionFailed(final Context arg0, Intent intent) {
			SmsModel m;
			ISmsAccess smsAccessor;
			if (AppConstants.DB == 1) {
				smsAccessor = new DefaultSmsProviderHelper(arg0.getContentResolver());
			} else {
				smsAccessor = new CustomSmsDbHelper(arg0.getContentResolver());
			}
			m = (SmsModel) intent
					.getSerializableExtra(SmsSendHelper.EXTRA_MESSAGE);
			// Intent updateIntent = new Intent(SmsSendHelper.ACTION_UPDATE);
			m.setSmsType(SmsModel.MESSAGE_TYPE_FAILED);
			smsAccessor.updateSmsStatus(m.getId(), SmsModel.STATUS_NONE,
					SmsModel.MESSAGE_TYPE_FAILED);
			// TODO probably update intent needed here as well
			// final Uri u = smsAccessor.insertSms(SmsDbHelper.SMS_URI, m);
			// m.setId(Long.parseLong(u.getPath()));
			// if (m.getId() == 0) {
			Log.i(TAG, String.format(
					"Send failed for message %s:%s",
					m.getAddressDisplayName() != null ? m
							.getAddressDisplayName() : m.getAddress(), m
							.getBody()));
			final SmsModel data = m;
			mHandler.postDelayed(new Runnable() {
				public void run() {
					Intent i = new Intent(SmsSendHelper.ACTION_RESEND);
					i.putExtra(SmsSendHelper.EXTRA_MESSAGE, data);
					arg0.sendBroadcast(i);
				}
			}, 5000);

			// } else {
			// smsAccessor.updateSmsStatus(
			// Uri.withAppendedPath(SmsDbHelper.SMS_URI,
			// String.valueOf(m.getId())),
			// SmsModel.STATUS_PENDING,
			// SmsModel.MESSAGE_TYPE_SENT);
			// updateIntent.putExtra(SmsSendHelper.EXTRA_MESSAGE, m);
			// Cache.delete(m.getThreadId());
			// Cache.addToRefreshSet(m.getThreadId());
			// arg0.sendBroadcast(updateIntent);
			// }
		}

		/**
		 * Send action ok.
		 * 
		 * @param arg0
		 *            the arg0
		 * @param intent
		 *            the intent
		 */
		private void sendActionOk(final Context arg0, Intent intent) {
			SmsModel m;
			ISmsAccess smsAccessor;
			if (AppConstants.DB == 1) {
				smsAccessor = new DefaultSmsProviderHelper(arg0.getContentResolver());
			} else {
				smsAccessor = new CustomSmsDbHelper(arg0.getContentResolver());
			}
			m = (SmsModel) intent
					.getSerializableExtra(SmsSendHelper.EXTRA_MESSAGE);
			Intent updateIntent = new Intent(SmsSendHelper.ACTION_UPDATE);
			m.setSmsType(SmsModel.MESSAGE_TYPE_SENT);
			m.setStatus(SmsModel.STATUS_NONE);
			if (m.getId() == 0) {
				Uri u = smsAccessor.insertSms(m);
				m = smsAccessor.getSingleSms(u);
			} else {
				smsAccessor.updateSmsStatus(m.getId(), SmsModel.STATUS_NONE,
						SmsModel.MESSAGE_TYPE_SENT);
			}
			Log.i(TAG, String.format(
					"Send complete for message %s:%s",
					m.getAddressDisplayName() != null ? m
							.getAddressDisplayName() : m.getAddress(), m
							.getBody()));
			Cache.addToRefreshSet(m.getThreadId(), true);
			updateIntent.putExtra(SmsSendHelper.EXTRA_MESSAGE, m);
			updateIntent.putExtra(SmsSendHelper.EXTRA_LAUNCH_CONVERSATION,
					intent.getBooleanExtra(
							SmsSendHelper.EXTRA_LAUNCH_CONVERSATION, false));
			arg0.sendBroadcast(updateIntent);
		}
	}

	/**
	 * The Class SmsDeliveredReceiver.
	 */
	public class SmsDeliveredReceiver extends BroadcastReceiver {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.content.BroadcastReceiver#onReceive(android.content.Context,
		 * android.content.Intent)
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
