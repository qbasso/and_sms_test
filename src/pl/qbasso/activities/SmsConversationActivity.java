package pl.qbasso.activities;

import pl.qbasso.custom.SmsThreadPageAdapter;
import pl.qbasso.fragments.SmsConversation;
import pl.qbasso.interfaces.ItemSeenListener;
import pl.qbasso.models.ConversationModel;
import pl.qbasso.models.SmsModel;
import pl.qbasso.sms.SmsDbHelper;
import pl.qbasso.sms.SmsLengthWatcher;
import pl.qbasso.smssender.R;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SmsConversationActivity extends FragmentActivity implements
		ItemSeenListener {

	private static final String EXTRA_CONVERSATION_NUMBER = "threadNumber";
	public static final String EXTRA_CONVERSATION_LIST = "threadList";
	private SmsThreadPageAdapter adapter;
	private ViewPager viewPager;
	private ConversationModel[] listInfo;
	private SmsDbHelper helper;
	private EditText smsInput;
	private Button sendButton;
	private TextView smsLength;
	protected Context ctx;

	private OnClickListener sendListener = new OnClickListener() {
		public void onClick(View v) {
			final String messageBody = smsInput.getText().toString();
			if (messageBody.length() > 0) {
				int itemPos = viewPager.getCurrentItem();
				((SmsConversation) adapter.getItem(itemPos)).sendText(
						messageBody, 0);
				InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(smsInput.getWindowToken(), 0);
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
		Object[] l = (Object[]) getIntent().getSerializableExtra(
				EXTRA_CONVERSATION_LIST);
		pageNo = getIntent().getIntExtra(EXTRA_CONVERSATION_NUMBER, 0);
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
		adapter = new SmsThreadPageAdapter(this, listInfo, this, getIntent().getBooleanExtra("send_now", false));
		viewPager.setAdapter(adapter);
		viewPager.setCurrentItem(pageNo);
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
	}

	public void onItemSeen(int adapterId, long messageId) {
		if (viewPager.getCurrentItem() == adapterId) {
			helper.updateSmsRead(messageId, SmsModel.MESSAGE_READ);
			ConversationList.NEED_REFRESH = true;
		}
	}
}
