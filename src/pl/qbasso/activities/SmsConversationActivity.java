package pl.qbasso.activities;

import pl.qbasso.custom.SmsThreadPageAdapter;
import pl.qbasso.fragments.SmsConversation;
import pl.qbasso.interfaces.ItemSeenListener;
import pl.qbasso.interfaces.SmsDraftAvailableListener;
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
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SmsConversationActivity extends FragmentActivity implements
		ItemSeenListener, SmsDraftAvailableListener {

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
				if (listInfo[itemPos].isDraft()) {
					helper.deleteDraftForThread(listInfo[viewPager
							.getCurrentItem()].getThreadId());
				}
				smsInput.getText().clear();
				InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(smsInput.getWindowToken(), 0);
			}
		}
	};
	private OnPageChangeListener pageChangedListener = new OnPageChangeListener() {

		public void onPageSelected(int arg0) {
			ConversationModel cm = listInfo[arg0];
			if (cm.isDraft()) {
				smsInput.setText(cm.getSnippet());
			}
		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub

		}

		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub

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
		adapter = new SmsThreadPageAdapter(this, listInfo, this, this,
				getIntent().getBooleanExtra("send_now", false));
		viewPager.setAdapter(adapter);
		viewPager.setCurrentItem(pageNo);
		viewPager.setOnPageChangeListener(pageChangedListener);
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
		ConversationModel cm = listInfo[viewPager.getCurrentItem()];
		long msgId = helper.getDraftIdForThread(cm.getThreadId());
		if (smsInput.getText().length() > 0) {
			if (msgId > -1) {
				helper.updateDraftMessage(msgId, smsInput.getText().toString(),
						System.currentTimeMillis());
			} else {
				SmsModel m = new SmsModel(0, cm.getThreadId(), cm.getAddress(),
						"", System.currentTimeMillis(), smsInput.getText()
								.toString(), SmsModel.MESSAGE_TYPE_DRAFT,
						SmsModel.MESSAGE_NOT_READ, SmsModel.STATUS_NONE);
				helper.insertSms(SmsDbHelper.SMS_DRAFT_URI, m);
			}
			Toast.makeText(ctx, "Dodano do wersji roboczych",
					Toast.LENGTH_SHORT).show();
			ConversationList.NEED_REFRESH = true;
		} else {
			if (msgId > -1) {
				helper.deleteSms(SmsDbHelper.SMS_URI, msgId);
				ConversationList.NEED_REFRESH = true;
			}
		}
		super.onPause();
	}

	public void onItemSeen(int adapterId, long messageId) {
		if (viewPager.getCurrentItem() == adapterId) {
			helper.updateSmsRead(messageId, SmsModel.MESSAGE_READ);
			ConversationList.NEED_REFRESH = true;
		}
	}

	@Override
	public void onBackPressed() {

		super.onBackPressed();
	}

	public void draftTextAvailable(String text, int position) {
		if (position == viewPager.getCurrentItem()) {
			smsInput.setText(text);
		}
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();
	}

}
