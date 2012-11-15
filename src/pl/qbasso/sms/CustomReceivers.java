package pl.qbasso.sms;

import pl.qbasso.models.SmsModel;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;

public class CustomReceivers {
	public static class SmsSentReceiver extends BroadcastReceiver {
		private Handler mHandler = new Handler();

		@Override
		public void onReceive(final Context arg0, Intent intent) {
			SmsModel m = null;
			if (intent.getAction().equals(SmsSendHelper.ACTION_SENT)) {
				SmsDbHelper smsAccessor = new SmsDbHelper(
						arg0.getContentResolver());
				m = (SmsModel) intent
						.getSerializableExtra(SmsSendHelper.EXTRA_MESSAGE);
				Intent updateIntent = new Intent(SmsSendHelper.ACTION_UPDATE);
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					m.setSmsType(SmsModel.MESSAGE_TYPE_SENT);
					if (m.getId() == 0) {
						Uri u = smsAccessor.insertSms(m);
						m = smsAccessor.getSingleSms(u);
					} else {
						smsAccessor.updateSmsType(Uri.withAppendedPath(
								SmsDbHelper.SMS_URI, String.valueOf(intent
										.getLongExtra(
												SmsSendHelper.EXTRA_MESSAGE_ID,
												0))),
								SmsModel.MESSAGE_TYPE_SENT);
					}
					updateIntent.putExtra(SmsSendHelper.EXTRA_MESSAGE, m);
					updateIntent.putExtra(
							SmsSendHelper.EXTRA_LAUNCH_CONVERSATION,
							intent.getBooleanExtra(
									SmsSendHelper.EXTRA_LAUNCH_CONVERSATION,
									false));
					arg0.sendBroadcast(updateIntent);
					break;
				default:
					m.setSmsType(SmsModel.MESSAGE_TYPE_FAILED);
					if (m.getId() == 0) {
						final Uri u = smsAccessor.insertSms(m);
						m.setId(Long.parseLong(u.getPath()));
						final SmsModel data = m;
						mHandler.postDelayed(new Runnable() {
							public void run() {
								Intent i = new Intent(
										SmsSendHelper.ACTION_RESEND);
								i.putExtra(SmsSendHelper.EXTRA_MESSAGE, data);
								arg0.sendBroadcast(i);
							}
						}, 5000);
						break;
					} else {
						smsAccessor.updateSmsType(Uri.withAppendedPath(
								SmsDbHelper.SMS_URI, String.valueOf(intent
										.getLongExtra(
												SmsSendHelper.EXTRA_MESSAGE_ID,
												0))),
								SmsModel.MESSAGE_TYPE_SENT);
						updateIntent.putExtra(SmsSendHelper.EXTRA_MESSAGE, m);
						arg0.sendBroadcast(updateIntent);
					}
				}
			} else if (intent.getAction().equals(SmsSendHelper.ACTION_RESEND)) {
				SmsSendHelper h = new SmsSendHelper();
				m = (SmsModel) intent
						.getSerializableExtra(SmsSendHelper.EXTRA_MESSAGE);
				final SmsModel data = m;
				h.sendText(arg0, data, false);
			}
		}
	}

	public class SmsDeliveredReceiver extends BroadcastReceiver {

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
