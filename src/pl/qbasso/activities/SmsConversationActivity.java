package pl.qbasso.activities;

import pl.qbasso.custom.SmsThreadPageAdapter;
import pl.qbasso.fragments.SmsConversation;
import pl.qbasso.interfaces.ItemSeenListener;
import pl.qbasso.models.ConversationModel;
import pl.qbasso.models.SmsModel;
import pl.qbasso.sms.SmsDbHelper;
import pl.qbasso.sms.SmsLengthWatcher;
import pl.qbasso.sms.SmsSendHelper;
import pl.qbasso.smssender.R;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SmsConversationActivity extends FragmentActivity implements
		ItemSeenListener {

	private SmsThreadPageAdapter adapter;
	private ViewPager viewPager;
	private ConversationModel[] listInfo;
	private SmsDbHelper helper;
	private EditText smsInput;
	private Button sendButton;
	private TextView smsLength;
	private BroadcastReceiver updateReceiver;
	protected Context ctx;

	private OnClickListener sendListener = new OnClickListener() {
		public void onClick(View v) {
			final String messageBody = smsInput.getText().toString();
			if (messageBody.length() > 0) {
				int itemPos = viewPager.getCurrentItem();
				((SmsConversation) adapter.getItem(itemPos)).sendText(
						messageBody, 0, 0);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		int pageNo = 0;
		super.onCreate(savedInstanceState);
		ctx = this;
		helper = new SmsDbHelper(getContentResolver());
		setContentView(R.layout.thread_pager);
		Object[] l = (Object[]) getIntent().getSerializableExtra("threadList");
		pageNo = getIntent().getIntExtra("threadNumber", 0);
		listInfo = new ConversationModel[l.length];
		int i = 0;
		for (Object object : l) {
			listInfo[i++] = (ConversationModel) object;
		}
		smsLength = (TextView) findViewById(R.id.sms_thread_sms_length);
		smsLength.setText(getString(R.string.sms_length, 0,
				SmsModel.ASCII_SMS_LENGTH, 0));
		smsInput = (EditText) findViewById(R.id.sms_thread_sms_input);
		smsInput.addTextChangedListener(new SmsLengthWatcher(this, smsLength));
		sendButton = (Button) findViewById(R.id.sms_thread_sms_send_button);
		sendButton.setOnClickListener(sendListener);
		viewPager = (ViewPager) findViewById(R.id.content_pages);
		adapter = new SmsThreadPageAdapter(this, listInfo, this);
		viewPager.setAdapter(adapter);
		viewPager.setCurrentItem(pageNo);
		initReceivers();
	}

	private void initReceivers() {
		if (updateReceiver == null) {
			updateReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					if (intent.getAction().equals(SmsSendHelper.ACTION_UPDATE)) {
//							SmsModel m = (SmsModel) intent
//									.getSerializableExtra(SmsSendHelper.EXTRA_MESSAGE);
//							ConversationModel cm = new ConversationModel(
//									m.getThreadId(), 0, "");
//							int adapterNumberToUpdate = findAdapterByThreadId(m.getThreadId());
//							if (adapterNumberToUpdate >-1) {
//								((SmsConversation)adapter.getItem(adapterNumberToUpdate)).update
//							}							
					}
				}
			};
		}
		registerReceiver(updateReceiver, new IntentFilter(
				SmsSendHelper.ACTION_UPDATE));
	}

	protected int findAdapterByThreadId(long threadId) {
		for (int i = 0; i < listInfo.length; i++) {
			if (listInfo[i].getThreadId() == threadId) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(updateReceiver);
	}

	public void onItemSeen(int adapterId, long messageId) {
		if (viewPager.getCurrentItem() == adapterId) {
			helper.updateSmsRead(messageId, SmsModel.MESSAGE_READ);
			ConversationList.NEED_REFRESH = true;
		}
	}
}
