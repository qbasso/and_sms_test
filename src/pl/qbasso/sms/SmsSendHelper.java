package pl.qbasso.sms;

import java.util.ArrayList;

import pl.qbasso.interfaces.OnMessageSendCompleteListener;
import pl.qbasso.models.SmsModel;
import pl.qbasso.smssender.R;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SmsSendHelper {

	private CountDownTimer t;
	private static final int MAX_PROGRESS = 5;
	private Dialog dialog;
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

	private OnClickListener cancelListener = new OnClickListener() {

		public void onClick(View arg0) {
			onMessageSendCompleteListener.messageSendComplete(false);
			t.cancel();
			dialog.dismiss();
		}
	};
	private OnMessageSendCompleteListener onMessageSendCompleteListener;

	public void sendTextWithDialog(final Context ctx, final SmsModel m, boolean launchConversation) {
		initPendingIntents(ctx, m, launchConversation);
		dialog = new Dialog(ctx);
		dialog.setContentView(R.layout.sms_cancel_dialog);
		dialog.setTitle("Caution!");
		dialog.setCancelable(false);
		final TextView msg = (TextView) dialog
				.findViewById(R.id.cancel_dialog_message);
		Button cancelButton = (Button) dialog
				.findViewById(R.id.cancel_dialog_button);
		cancelButton.setOnClickListener(cancelListener);
		cancelButton.startAnimation(AnimationUtils.loadAnimation(ctx,
				R.anim.shake));
		final ProgressBar progressBar = (ProgressBar) dialog
				.findViewById(R.id.cancel_dialog_progress);
		progressBar.setMax(MAX_PROGRESS);
		dialog.show();
		t = new CountDownTimer(MAX_PROGRESS * 1000, 1000) {
			@Override
			public void onTick(long millisUntilFinished) {
				int secondsLeft = (int) (millisUntilFinished / 1000);
				msg.setText(ctx.getString(R.string.sending_in_progress_label,
						m.getAddressDisplayName(), secondsLeft));
				progressBar.setProgress(-secondsLeft + MAX_PROGRESS);
			}

			@Override
			public void onFinish() {
				handleSend(m, msg, progressBar);
			}
		};
		t.start();
	}

	private void initPendingIntents(final Context ctx, final SmsModel m, boolean launchConversation) {
		Intent i = new Intent(ACTION_SENT);
		i.putExtra(EXTRA_MESSAGE, m);
		i.putExtra(EXTRA_LAUNCH_CONVERSATION, launchConversation);
		sent = PendingIntent.getBroadcast(ctx, 0, i,
				PendingIntent.FLAG_UPDATE_CURRENT);
		delivered = PendingIntent.getBroadcast(ctx, 0, new Intent(ACTION_DELIVERED),
				PendingIntent.FLAG_UPDATE_CURRENT);
	}

	private void handleSend(SmsModel m, final TextView msg,
			final ProgressBar progressBar) {
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
		onMessageSendCompleteListener.messageSendComplete(true);
		progressBar.setProgress(MAX_PROGRESS);
		dialog.dismiss();
	}
	
	public void sendText(Context ctx, SmsModel m) {
		initPendingIntents(ctx, m, false);
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
	}

	public void setOnMessageSendCompleteListener(OnMessageSendCompleteListener listener) {
		onMessageSendCompleteListener = listener;
		
	}
}
