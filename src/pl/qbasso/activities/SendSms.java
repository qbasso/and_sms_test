/*
 * @author JPorzuczek
 */
package pl.qbasso.activities;

import java.util.ArrayList;

import pl.qbasso.custom.BaseApplication;
import pl.qbasso.custom.ContactsAdapter;
import pl.qbasso.custom.Utils;
import pl.qbasso.interfaces.ISmsAccess;
import pl.qbasso.models.ConversationModel;
import pl.qbasso.models.SmsModel;
import pl.qbasso.sms.CustomSmsDbHelper;
import pl.qbasso.sms.DefaultSmsProviderHelper;
import pl.qbasso.sms.SmsLengthWatcher;
import pl.qbasso.smssender.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

// TODO: Auto-generated Javadoc
/**
 * The Class SendSms.
 */
public class SendSms extends Activity {

	private static final String EXTRA_MESSAGE_BODY = "message_body";

	/** The input. */
	private AutoCompleteTextView input;

	private AutoCompleteTextView contactInput;

	private ContactsAdapter contactsAdapter;

	private TextView messageLength;

	private EditText messageInput;

	private Button sendSmsButton;

	private Context ctx;

	private OnClickListener sendButtonListener = new OnClickListener() {

		public void onClick(View v) {
			String sender = contactInput.getText().toString();
			String body = messageInput.getText().toString();
			sendMessage(Utils.getPhoneNumber(sender), body);
		}
	};

	private ISmsAccess smsAccessor;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Uri u = getIntent().getData();
		ctx = this;
		if (AppConstants.DB == 1) {
			smsAccessor = new DefaultSmsProviderHelper(getContentResolver());
		} else {
			smsAccessor = new CustomSmsDbHelper(getContentResolver());
		}
		setContentView(R.layout.send_sms_screen);
		contactInput = (AutoCompleteTextView) findViewById(R.id.recipient_input);
		contactsAdapter = new ContactsAdapter(ctx, R.layout.contact_item);
		contactInput.setAdapter(contactsAdapter);
		contactInput.setThreshold(1);
		messageLength = (TextView) findViewById(R.id.sms_thread_sms_length);
		messageInput = (EditText) findViewById(R.id.sms_thread_sms_input);
		messageInput.addTextChangedListener(new SmsLengthWatcher(ctx,
				messageLength));
		sendSmsButton = (Button) findViewById(R.id.sms_thread_sms_send_button);
		sendSmsButton.setOnClickListener(sendButtonListener);
		if (getIntent().getStringExtra(EXTRA_MESSAGE_BODY) != null) {
			messageInput
					.setText(getIntent().getStringExtra(EXTRA_MESSAGE_BODY));
		}
		if (u != null) {
			contactInput.setText(u.getSchemeSpecificPart());
		}
	}

	protected void sendMessage(String phoneNumber, String body) {
		if (body.length() > 0 && phoneNumber.length() > 0) {
			long threadId = smsAccessor.getThreadIdForPhoneNumber(phoneNumber);
			SmsModel m = new SmsModel(0, threadId, phoneNumber, "",
					System.currentTimeMillis(), body,
					SmsModel.MESSAGE_TYPE_QUEUED, SmsModel.MESSAGE_NOT_READ,
					SmsModel.STATUS_WAITING);
			m.setAddressDisplayName(contactsAdapter
					.getCurrentlySelectedDisplayName() != null ? contactsAdapter
					.getCurrentlySelectedDisplayName() : m.getAddress());
			Uri u = smsAccessor.insertSms(m);
			if (threadId == -1) {
				threadId = smsAccessor.getThreadIdForSmsUri(u);
				m.setThreadId(threadId);
			}
			Intent i = new Intent(ctx, SmsConversationActivity.class);
			ConversationModel conversationModel = new ConversationModel(
					threadId, 0, "");
			conversationModel.setAddress(m.getAddress());
			conversationModel.setDisplayName(smsAccessor
					.getDisplayName(phoneNumber));
			ArrayList<ConversationModel> items = new ArrayList<ConversationModel>();
			items.add(conversationModel);
			i.putExtra("threadList", items.toArray());
			i.putExtra("threadNumber", 0);
			i.putExtra("send_now", true);
			overridePendingTransition(android.R.anim.slide_out_right,
					android.R.anim.slide_in_left);
			startActivity(i);
			finish();
		}
	}

}
