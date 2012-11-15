package pl.qbasso.activities;

import java.util.ArrayList;
import java.util.List;

import pl.qbasso.custom.ContactsAdapter;
import pl.qbasso.custom.ConversationAdapter;
import pl.qbasso.custom.SendTaskService;
import pl.qbasso.custom.SlideHelper;
import pl.qbasso.interfaces.SlidingViewLoadedListener;
import pl.qbasso.models.ConversationModel;
import pl.qbasso.models.SmsModel;
import pl.qbasso.sms.SmsDbHelper;
import pl.qbasso.sms.SmsLengthWatcher;
import pl.qbasso.sms.SmsSendHelper;
import pl.qbasso.smssender.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class ConversationList extends Activity {

	protected static final String EXTRA_CLIENT_ID = "client_id";
	private SmsDbHelper smsAccessor;
	private ListView smsThreadList;
	private List<ConversationModel> items;
	private Context ctx;
	private ProgressDialog pd;
	private Button composeButton;
	private AutoCompleteTextView contactInput;
	private EditText messageInput;
	private SlideHelper slideHeper;
	private Button sendSmsButton;
	public static boolean NEED_REFRESH = false;
	protected TextView messageLength;

	protected OnItemClickListener autoCompleteItemListener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {

		}
	};

	protected OnClickListener sendButtonListener = new OnClickListener() {
		public void onClick(View arg0) {
			String sender = contactInput.getText().toString();
			String body = messageInput.getText().toString();
			if (body.length() > 0 && sender.length() > 0) {
				SmsModel m = new SmsModel(0, -1, contactInput.getText()
						.toString(), "", System.currentTimeMillis(),
						messageInput.getText().toString(),
						SmsModel.MESSAGE_TYPE_SENT, SmsModel.MESSAGE_READ,
						SmsModel.STATUS_WAITING);
				m.setAddressDisplayName(adapter
						.getCurrentlySelectedDisplayName() != null ? adapter
						.getCurrentlySelectedDisplayName() : m.getAddress());
				// helper.sendText(ctx, m, true);
				Uri u = smsAccessor.insertSms(m);
				long threadId = smsAccessor.getThreadIdForSmsUri(u);
				int existingId = isExistingThread(threadId);
				if (existingId > -1) {
					startSmsConversationActivity(existingId, true);
				} else {
					Intent i = new Intent(ctx, SmsConversationActivity.class);
					ConversationModel conversationModel = new ConversationModel(
							threadId, 0, "");
					conversationModel.setDisplayName(smsAccessor
							.getDisplayName(sender));
					ArrayList<ConversationModel> items = new ArrayList<ConversationModel>();
					items.add(conversationModel);
					i.putExtra("threadList", items.toArray());
					i.putExtra("threadNumber", 0);
					i.putExtra("send_now", true);
					overridePendingTransition(android.R.anim.slide_out_right,
							android.R.anim.slide_in_left);
					startActivity(i);
				}
				// sendingNow = m;
				// m.setAddressDisplayName(info.getDisplayName());
				// m.setId(Long.parseLong(u.getLastPathSegment()));
				// Message message = Message.obtain();
				// Bundle b = new Bundle();
				// b.putSerializable(SmsSendHelper.EXTRA_MESSAGE, m);
				// b.putString(EXTRA_CLIENT_ID, clientId);
				// message.what = SendTaskService.QUEUE_MESSAGE;
				// message.setData(b);
				// try {
				// mService.send(message);
				// items.add(0, m);
				// adapter.notifyDataSetChanged();
				// } catch (RemoteException e) {
				// e.printStackTrace();
				// }
			}
		}
	};
	private BroadcastReceiver updateReceiver;
	protected ContactsAdapter adapter;

	private void initReceivers() {
		if (updateReceiver == null) {
			updateReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					if (intent.getAction().equals(SmsSendHelper.ACTION_UPDATE)) {
						updateItems();
						if (slideHeper.isMenuShown()) {
							slideHeper.hideMenu(false);
							contactInput.getText().clear();
							messageInput.getText().clear();
						}
						if (intent.getBooleanExtra(
								SmsSendHelper.EXTRA_LAUNCH_CONVERSATION, false)) {
							SmsModel m = (SmsModel) intent
									.getSerializableExtra(SmsSendHelper.EXTRA_MESSAGE);
							ConversationModel cm = new ConversationModel(
									m.getThreadId(), 0, "");
							ArrayList<ConversationModel> list = new ArrayList<ConversationModel>();
							list.add(cm);
							Intent i = new Intent(ctx,
									SmsConversationActivity.class);
							i.putExtra("threadList", list.toArray());
							startActivity(i);
						}
					}
				}
			};
		}
		this.registerReceiver(updateReceiver, new IntentFilter(
				SmsSendHelper.ACTION_UPDATE));
	}

	private SlidingViewLoadedListener listener = new SlidingViewLoadedListener() {

		public void onViewLoaded() {
			contactInput = (AutoCompleteTextView) findViewById(R.id.recipient_input);
			adapter = new ContactsAdapter(ctx, R.layout.contact_item);
			contactInput.setAdapter(adapter);
			contactInput.setThreshold(1);
			contactInput.setOnItemClickListener(autoCompleteItemListener);
			messageLength = (TextView) findViewById(R.id.sms_thread_sms_length);
			messageInput = (EditText) findViewById(R.id.sms_thread_sms_input);
			messageInput.addTextChangedListener(new SmsLengthWatcher(ctx,
					messageLength));
			sendSmsButton = (Button) findViewById(R.id.sms_thread_sms_send_button);
			sendSmsButton.setOnClickListener(sendButtonListener);
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		initReceivers();
		if (NEED_REFRESH) {
			showProgressDialog();
			updateItems();
			NEED_REFRESH = false;
		}
	}

	private Handler mainHandler = new Handler() {

		@Override
		public void handleMessage(Message m) {
			ConversationAdapter adapter = new ConversationAdapter(ctx,
					R.layout.conversation_item, items);
			smsThreadList.setOnItemClickListener(smsThreadClickListener);
			smsThreadList.setAdapter(adapter);
			pd.dismiss();
		}
	};

	private OnItemClickListener smsThreadClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			startSmsConversationActivity(arg2, false);
		}

	};
	private OnClickListener composeButtonListener = new OnClickListener() {
		public void onClick(View v) {
			if (!slideHeper.isMenuShown()) {
				slideHeper.showMenu(false);
			} else {
				slideHeper.hideMenu(false);
			}
		}
	};

	private void updateItems() {
		showProgressDialog();
		new Thread(new Runnable() {
			public void run() {
				items = smsAccessor.getThreads();
				mainHandler.sendEmptyMessage(0);
			}
		}).start();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.conversation_list);
		this.ctx = this;
		startService(new Intent(ctx, SendTaskService.class));
		slideHeper = new SlideHelper(this, R.layout.send_sms_screen);
		slideHeper.setSlidingViewLoadedListener(listener);
		smsThreadList = (ListView) findViewById(R.id.main_thread_list);
		smsThreadList.setOnItemClickListener(smsThreadClickListener);
		smsAccessor = new SmsDbHelper(getContentResolver());
		composeButton = (Button) findViewById(R.id.button_compose_new);
		composeButton.setOnClickListener(composeButtonListener);
		pd = new ProgressDialog(ctx);
		showProgressDialog();
		updateItems();
	}

	private void showProgressDialog() {
		pd.setMessage("Please wait...");
		pd.setCancelable(false);
		pd.show();
	}

	private void startSmsConversationActivity(int arg2, boolean send) {
		Intent i = new Intent(ctx, SmsConversationActivity.class);
		i.putExtra("threadList", items.toArray());
		i.putExtra("threadNumber", arg2);
		i.putExtra("send_now", send);
		overridePendingTransition(android.R.anim.slide_out_right,
				android.R.anim.slide_in_left);
		startActivity(i);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onBackPressed() {
		if (slideHeper.isMenuShown()) {
			slideHeper.hideMenu(false);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	private int isExistingThread(long threadId) {
		int result = -1;
		for (ConversationModel conversation : items) {
			if (conversation.getThreadId() == threadId) {
				result = items.indexOf(conversation);
				break;
			}
		}
		return result;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		stopService(new Intent(this, SendTaskService.class));
	}

}