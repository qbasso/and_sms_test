/*
 * @author JPorzuczek
 */
package pl.qbasso.activities;

import pl.qbasso.custom.SmsThreadPageAdapter;
import pl.qbasso.fragments.SmsConversation;
import pl.qbasso.interfaces.ItemSeenListener;
import pl.qbasso.interfaces.SmsDraftAvailableListener;
import pl.qbasso.models.ConversationModel;
import pl.qbasso.models.SmsModel;
import pl.qbasso.sms.Cache;
import pl.qbasso.sms.SmsDbHelper;
import pl.qbasso.sms.SmsLengthWatcher;
import pl.qbasso.sms.SmsReceiver;
import pl.qbasso.smssender.R;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

// TODO: Auto-generated Javadoc
/**
 * The Class SmsConversationActivity.
 */
public class SmsConversationActivity extends FragmentActivity implements
		ItemSeenListener, SmsDraftAvailableListener {

	/** The Constant EXTRA_CONVERSATION_NUMBER. */
	private static final String EXTRA_CONVERSATION_NUMBER = "threadNumber";

	/** The Constant EXTRA_CONVERSATION_LIST. */
	public static final String EXTRA_CONVERSATION_LIST = "threadList";

	/** The adapter. */
	private SmsThreadPageAdapter adapter;

	/** The view pager. */
	private ViewPager viewPager;

	/** The list info. */
	private ConversationModel[] listInfo;

	/** The helper. */
	private SmsDbHelper helper;

	/** The sms input. */
	private EditText smsInput;

	/** The send button. */
	private Button sendButton;

	/** The sms length. */
	private TextView smsLength;

	/** The ctx. */
	protected Context ctx;
	
	private NotificationManager nm;

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			int fragmentId = findAdapterByThreadId(intent.getLongExtra(
					"thread_id", -1));
			if (fragmentId == viewPager.getCurrentItem()) {
				((SmsConversation) adapter.getItem(fragmentId))
						.updateItems(true);
				nm.cancelAll();
			}

		}
	};

	/** The send listener. */
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

	/** The page changed listener. */
	private OnPageChangeListener pageChangedListener = new OnPageChangeListener() {

		public void onPageSelected(int arg0) {
			ConversationModel cm = listInfo[arg0];
			if (cm.isDraft()) {
				smsInput.setText(cm.getSnippet());
			} else {
				smsInput.setText("");
			}
		}

		public void onPageScrollStateChanged(int arg0) {
		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		int pageNo = 0;
		super.onCreate(savedInstanceState);
		ctx = this;
		nm = (NotificationManager) getSystemService(Activity.NOTIFICATION_SERVICE);
		helper = new SmsDbHelper(getContentResolver());
		setContentView(R.layout.thread_pager);
		if (getIntent().getBooleanExtra(SmsReceiver.EXTRA_CANCEL_ALARM, false)) {
			AlarmManager am = (AlarmManager) ctx.getSystemService(Activity.ALARM_SERVICE);
			Intent intent = new Intent(SmsReceiver.ACTION_CANCEL_LIGHT);
			am.cancel(PendingIntent.getBroadcast(ctx, 0, intent, 0));
		}
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

	/**
	 * Find adapter by thread id.
	 * 
	 * @param threadId
	 *            the thread id
	 * @return the int
	 */
	protected int findAdapterByThreadId(long threadId) {
		for (int i = 0; i < listInfo.length; i++) {
			if (listInfo[i].getThreadId() == threadId) {
				return i;
			}
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onPause()
	 */
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
			Cache.delete(cm.getThreadId());
			Cache.addToRefreshSet(cm.getThreadId());
		} else {
			if (msgId > -1) {
				helper.deleteSms(SmsDbHelper.SMS_URI, msgId);
				Cache.delete(cm.getThreadId());
				Cache.addToRefreshSet(cm.getThreadId());
			}
		}
		unregisterReceiver(receiver);
		super.onPause();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.qbasso.interfaces.ItemSeenListener#onItemSeen(int, long)
	 */
	public void onItemSeen(int adapterId, long messageId) {
		if (viewPager.getCurrentItem() == adapterId) {
			helper.updateSmsRead(messageId, SmsModel.MESSAGE_READ);
			Cache.delete(listInfo[viewPager.getCurrentItem()].getThreadId());
			Cache.addToRefreshSet(listInfo[viewPager.getCurrentItem()].getThreadId());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pl.qbasso.interfaces.SmsDraftAvailableListener#draftTextAvailable(java
	 * .lang.String, int)
	 */
	public void draftTextAvailable(String text, int position) {
		if (position == viewPager.getCurrentItem()) {
			smsInput.setText(text);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {

		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(receiver, new IntentFilter(
				"pl.qbasso.smssender.new_message_arrived"));
	}

}
