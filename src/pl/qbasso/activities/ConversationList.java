package pl.qbasso.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import pl.qbasso.custom.ContactsAdapter;
import pl.qbasso.custom.ConversationAdapter;
import pl.qbasso.custom.SendTaskService;
import pl.qbasso.custom.SlideHelper;
import pl.qbasso.custom.Utils;
import pl.qbasso.interfaces.ISmsAccess;
import pl.qbasso.interfaces.SlidingViewLoadedListener;
import pl.qbasso.models.ConversationModel;
import pl.qbasso.models.SmsModel;
import pl.qbasso.sms.Cache;
import pl.qbasso.sms.CustomSmsDbHelper;
import pl.qbasso.sms.SmsDbHelper;
import pl.qbasso.sms.SmsLengthWatcher;
import pl.qbasso.sms.SmsReceiver;
import pl.qbasso.sms.SmsSendHelper;
import pl.qbasso.smssender.R;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Intents.Insert;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

/**
 * The Class ConversationList.
 * 
 * @author jakub.porzuczek
 */
public class ConversationList extends Activity { // implements
													// LoaderCallbacks<List<ConversationModel>>
													// {

	/**
	 * The Constant EXTRA_CLIENT_ID. Used when passing intent extra client id to
	 * message sending service
	 */
	protected static final String EXTRA_CLIENT_ID = "client_id";

	private static final String EXTRA_CANCEL_ALARM = "cancel_alarm";

	/** Provides access to sms database */
	private ISmsAccess smsAccessor;

	/** holds reference to conversation list */
	private ListView smsThreadList;

	/** Holds conversation items */
	private List<ConversationModel> items;

	/** The ctx. */
	private Context ctx;

	/** The pd. */
	private ProgressDialog pd;

	/** Reference to create new button. */
	private Button composeButton;

	/** The contact input. */
	private AutoCompleteTextView contactInput;

	/** The message input. */
	private EditText messageInput;

	/** Used to slide new message window */
	private SlideHelper slideHelper;

	/** The send sms button. */
	private Button sendSmsButton;

	/** The need refresh. */
	public static boolean NEED_REFRESH = false;

	/** The message length. */
	protected TextView messageLength;

	/** The messenger. */
	private Messenger messenger;

	/** The m service. */
	private Messenger mService;

	/** The client id. */
	private String clientId;

	/** The conversation adapter. */
	private ConversationAdapter mConversationAdapter;

	private NotificationManager mNotificationManager;

	private ViewFlipper mViewFlipper;

	private ListView mConversationListCb;

	private ConversationAdapter mConversationAdapterCb;

	/** The item long click listener. */
	private OnItemLongClickListener itemLongClckListener = new OnItemLongClickListener() {

		public boolean onItemLongClick(AdapterView<?> arg0, final View v,
				int arg2, long arg3) {
			final ConversationModel item = items.get(arg2);
			AlertDialog.Builder b = new AlertDialog.Builder(ctx);
			b.setTitle("Opcje w¹tku").setItems(R.array.conversation_actions,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0, int arg1) {
							switch (arg1) {
							case 0:
								actionDeleteThread(v, item);
								break;
							case 1:
								actionAddContact(item);
								arg0.dismiss();
								break;
							case 2:
								mConversationAdapterCb = new ConversationAdapter(
										ctx, R.layout.conversation_item_cb,
										items, true);
								mConversationListCb
										.setAdapter(mConversationAdapterCb);
								composeButton.setText("Usuñ");
								mViewFlipper.setDisplayedChild(1);
							default:
								break;
							}
						}
					});
			b.create().show();
			return false;
		}
	};

	/**
	 * start activity to add new contact or edit if it exists
	 * 
	 * @param item
	 *            holding data about conversation
	 */
	private void actionAddContact(ConversationModel item) {
		Intent insert = new Intent(Intent.ACTION_INSERT_OR_EDIT);
		insert.setType(Contacts.CONTENT_ITEM_TYPE);
		insert.putExtra(Insert.PHONE, item.getAddress());
		Cache.addToRefreshSet(item.getThreadId(), false);
		startActivityForResult(insert, 0);
	}

	/**
	 * delete conversation, it's row and play animation
	 * 
	 * @param v
	 *            view to animate
	 * @param item
	 *            conversation model
	 */
	private void actionDeleteThread(View v, final ConversationModel item) {
		Animation anim = AnimationUtils.loadAnimation(ctx, R.anim.slide_right);
		v.startAnimation(anim);
		mainHandler.postDelayed(new Runnable() {
			public void run() {
				removeConversation(item);
			}

		}, anim.getDuration());
	}

	private void removeConversation(final ConversationModel item) {
		smsAccessor.deleteThread(item.getThreadId());
		items.remove(item);
		mConversationAdapter.notifyDataSetChanged();
	}

	/**
	 * handles response from service whether there are some messages that wait
	 * to be sent
	 */
	private Handler incomingHandler = new Handler() {
		@Override
		public void handleMessage(Message m) {
			switch (m.what) {
			case SendTaskService.MESSAGE_QUEUE_EMPTY:
				finish();
				break;
			case SendTaskService.MESSAGE_QUEUE_NOT_EMPTY:
				((Activity) ctx).moveTaskToBack(false);
				break;
			default:
				break;
			}
		}
	};

	/** The connection. */
	private ServiceConnection connection = new ServiceConnection() {
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
		}

		public void onServiceConnected(ComponentName name, IBinder service) {
			try {
				Bundle b = new Bundle();
				b.putString(EXTRA_CLIENT_ID, clientId);
				mService = new Messenger(service);
				Message m = Message.obtain(null, SendTaskService.REGISTER);
				m.replyTo = messenger;
				m.setData(b);
				mService.send(m);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	};

	/** The send button listener. */
	protected OnClickListener sendButtonListener = new OnClickListener() {
		public void onClick(View arg0) {
			String sender = contactInput.getText().toString();
			String body = messageInput.getText().toString();
			sendMessageFromMainScreen(Utils.getPhoneNumber(sender), body);
		}

	};

	/** The update receiver. */
	private BroadcastReceiver updateReceiver;

	/** The contacts adapter. */
	protected ContactsAdapter contactsAdapter;

	/**
	 * Inits the receivers.
	 */
	private void initReceivers() {
		if (updateReceiver == null) {
			updateReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					if (intent.getAction().equals(SmsSendHelper.ACTION_UPDATE)) {
						SmsModel m = (SmsModel) intent
								.getSerializableExtra(SmsSendHelper.EXTRA_MESSAGE);
						updateItems(true);
						if (slideHelper.isMenuShown()) {
							slideHelper.hideMenu(false);
						}
						if (intent.getBooleanExtra(
								SmsSendHelper.EXTRA_LAUNCH_CONVERSATION, false)) {
							ConversationModel cm = new ConversationModel(
									m.getThreadId(), 0, "");
							ArrayList<ConversationModel> list = new ArrayList<ConversationModel>();
							list.add(cm);
							Intent i = new Intent(ctx,
									SmsConversationActivity.class);
							i.putExtra("threadList", list.toArray());
							startActivity(i);
						}
					} else if (intent.getAction().equals(
							SmsReceiver.ACTION_MESSAGE_ARRIVED)) {
						if (slideHelper.isMenuShown()) {
							slideHelper.hideMenu(false);
						}
						mNotificationManager.cancel("",
								SmsReceiver.NOTIFICATION_ID);
						updateItems(true);
					}
				}
			};
		}
		IntentFilter filter = new IntentFilter(SmsSendHelper.ACTION_UPDATE);
		filter.addAction(SmsReceiver.ACTION_MESSAGE_ARRIVED);
		this.registerReceiver(updateReceiver, filter);
	}

	/** The listener. */
	private SlidingViewLoadedListener listener = new SlidingViewLoadedListener() {

		public void onViewLoaded() {
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
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		initReceivers();
		if (Cache.needRefresh()) {
			showProgressDialog();
			updateItems(true);
		}
	}

	/** The main handler. */
	private Handler mainHandler = new Handler() {
		@Override
		public void handleMessage(Message m) {
			switch (m.what) {
			case 0:
				mConversationAdapter = new ConversationAdapter(ctx,
						R.layout.conversation_item, items, false);
				smsThreadList.setOnItemClickListener(smsThreadClickListener);
				smsThreadList.setAdapter(mConversationAdapter);
				pd.dismiss();
				break;
			case 1:
				mConversationAdapter.notifyDataSetChanged();
				break;
			default:
				break;
			}
		}
	};

	/** The sms thread click listener. */
	private OnItemClickListener smsThreadClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			startSmsConversationActivity(arg2, false);
		}

	};

	/** The compose button listener. */
	private OnClickListener composeButtonListener = new OnClickListener() {
		public void onClick(View v) {
			if (mViewFlipper.getDisplayedChild() == 1) {
				// TODO it should be done in separate thread, remove big threads
				// can take a while
				removeSelectedThreads();
			} else {
				if (!slideHelper.isMenuShown()) {
					slideHelper.showMenu(false);
				} else {
					slideHelper.hideMenu(false);
				}
			}
		}

		private void removeSelectedThreads() {
			boolean[] data = mConversationAdapterCb.getChecked();
			mViewFlipper.setDisplayedChild(0);
			showProgressDialog();
			for (int i = 0; i < data.length; i++) {
				if (data[i]) {
					removeConversation(items.get(i));
				}
			}
			composeButton.setText("Nowa...");
			pd.dismiss();
		}
	};

	/**
	 * Update items.
	 * 
	 * @param refresh
	 */
	private void updateItems(final boolean refresh) {
		showProgressDialog();
		new Thread(new Runnable() {
			public void run() {
				if (!refresh) {
					Cache.getInstance();
					Cache.putAll(smsAccessor.getThreads(null));
					items = Cache.getAll();
				} else {
					Cache.putInOrder(smsAccessor.getThreads(Cache
							.getRefreshList()));
					Cache.clearRefreshSet();
					items = Cache.getAll();
				}
				mainHandler.sendEmptyMessage(0);
				for (ConversationModel model : items) {
					smsAccessor.getDetailsForConversation(model);
				}
				mainHandler.sendEmptyMessage(1);
			}
		}).start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.conversation_list);
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		if (getIntent().getBooleanExtra(EXTRA_CANCEL_ALARM, false)) {
			((AlarmManager) getSystemService(ALARM_SERVICE))
					.cancel(PendingIntent.getBroadcast(this, 0, new Intent(
							SmsReceiver.ACTION_CANCEL_LIGHT), 0));
		}
		initViewMembers();
		// getSupportLoaderManager().initLoader(0, null, this).forceLoad();
		showProgressDialog();
		updateItems(false);
	}

	private void initViewMembers() {
		this.ctx = this;
		clientId = UUID.randomUUID().toString();
		messenger = new Messenger(incomingHandler);
		bindService(new Intent(ctx, SendTaskService.class), connection,
				BIND_AUTO_CREATE);
		slideHelper = new SlideHelper(this, R.layout.send_sms_screen);
		slideHelper.setSlidingViewLoadedListener(listener);
		smsThreadList = (ListView) findViewById(R.id.main_thread_list);
		smsThreadList.setOnItemClickListener(smsThreadClickListener);
		smsThreadList.setOnItemLongClickListener(itemLongClckListener);
		mConversationListCb = (ListView) findViewById(R.id.main_thread_list_cb);
		mViewFlipper = (ViewFlipper) findViewById(R.id.viewflip);
		mViewFlipper.setInAnimation(ctx, android.R.anim.slide_in_left);
		mViewFlipper.setOutAnimation(ctx, android.R.anim.slide_out_right);
		if (AppConstants.DB == 1) {
			smsAccessor = new SmsDbHelper(getContentResolver());
		} else {
			smsAccessor = new CustomSmsDbHelper(getContentResolver());
		}
		composeButton = (Button) findViewById(R.id.button_compose_new);
		composeButton.setOnClickListener(composeButtonListener);
		pd = new ProgressDialog(ctx);
	}

	/**
	 * Show progress dialog.
	 */
	private void showProgressDialog() {
		pd.setMessage("Please wait...");
		pd.setCancelable(false);
		pd.show();
	}

	/**
	 * Start sms conversation activity.
	 * 
	 * @param arg2
	 *            the arg2
	 * @param send
	 *            the send
	 */
	private void startSmsConversationActivity(int arg2, boolean send) {
		Intent i = new Intent(ctx, SmsConversationActivity.class);
		i.putExtra("threadList", items.toArray());
		i.putExtra("threadNumber", arg2);
		i.putExtra("send_now", send);
		overridePendingTransition(android.R.anim.slide_out_right,
				android.R.anim.slide_in_left);
		startActivity(i);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		if (slideHelper.isMenuShown()) {
			slideHelper.hideMenu(false);
		} else if (mViewFlipper.getDisplayedChild() == 1) {
			mViewFlipper.setDisplayedChild(0);
			composeButton.setText("Nowa...");
		} else {
			if (isTaskRoot()) {
				try {
					Bundle b = new Bundle();
					b.putString(SendTaskService.EXTRA_CLIENT_ID, clientId);
					Message m = Message.obtain(null,
							SendTaskService.MESSAGE_QUEUE_EMPTY);
					m.setData(b);
					mService.send(m);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			} else {
				super.onBackPressed();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		unregisterReceiver(updateReceiver);
		super.onPause();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		try {
			Message m = Message.obtain(null, SendTaskService.UNREGISTER);
			Bundle b = new Bundle();
			b.putString(EXTRA_CLIENT_ID, clientId);
			m.setData(b);
			mService.send(m);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		unbindService(connection);
		super.onDestroy();
	}

	/**
	 * Send message from main screen.
	 * 
	 * @param sender
	 *            the sender
	 * @param body
	 *            the body
	 */
	private void sendMessageFromMainScreen(String sender, String body) {
		if (body.length() > 0 && sender.length() > 0) {
			long threadId = smsAccessor.getThreadIdForPhoneNumber(sender);
			SmsModel m = new SmsModel(0, threadId, sender, "",
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
					threadId, 1, "");
			conversationModel.setAddress(m.getAddress());
			conversationModel
					.setDisplayName(smsAccessor.getDisplayName(sender));
			ArrayList<ConversationModel> items = new ArrayList<ConversationModel>(
					this.items);
			items.add(0, conversationModel);
			i.putExtra("threadList", items.toArray());
			i.putExtra("threadNumber", 0);
			i.putExtra("send_now", true);
			overridePendingTransition(android.R.anim.slide_out_right,
					android.R.anim.slide_in_left);
			startActivity(i);
		} else {
			Toast.makeText(ctx, "Podaj poprawny numer telefonu!",
					Toast.LENGTH_SHORT).show();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
			updateItems(true);
			clearFields();
		}
	}

	private void clearFields() {
		messageInput.getText().clear();
		contactInput.getText().clear();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		boolean result = slideHelper.handleTouchEvent(ev);
		if (result) {
			return result;
		} else {
			return super.dispatchTouchEvent(ev);
		}
	}

	// public Loader<List<ConversationModel>> onCreateLoader(int arg0, Bundle
	// arg1) {
	// return new ConversationLoader(ctx);
	// }
	//
	// public void onLoadFinished(Loader<List<ConversationModel>> arg0,
	// List<ConversationModel> arg1) {
	// items = arg1;
	// mainHandler.sendEmptyMessage(0);
	// }
	//
	// public void onLoaderReset(Loader<List<ConversationModel>> arg0) {
	// items = new ArrayList<ConversationModel>();
	// mainHandler.sendEmptyMessage(0);
	// }

}