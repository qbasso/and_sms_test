package pl.qbasso.fragments;

import java.util.List;
import java.util.UUID;

import pl.qbasso.activities.ConversationList;
import pl.qbasso.custom.SendTaskService;
import pl.qbasso.custom.SmsAdapter;
import pl.qbasso.interfaces.ActionClickListener;
import pl.qbasso.interfaces.ItemSeenListener;
import pl.qbasso.interfaces.OnMessageSendCompleteListener;
import pl.qbasso.interfaces.SmsDraftAvailableListener;
import pl.qbasso.models.ActionModel;
import pl.qbasso.models.ConversationModel;
import pl.qbasso.models.SmsModel;
import pl.qbasso.sms.SmsDbHelper;
import pl.qbasso.sms.SmsSendHelper;
import pl.qbasso.smssender.R;
import pl.qbasso.view.CustomPopup;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

public class SmsConversation extends Fragment {

	public static final String EXTRA_CONVERSATION_INFO = "thread_info";
	public static final String EXTRA_FRAGMENT_POSITION = "position";
	public static final String EXTRA_THREAD_ID = "thread_id";
	public static final String EXTRA_MESSAGE_ID = "message_id";
	public static final String EXTRA_ADAPTER_POSITION = "adapter_position";
	public static final String EXTRA_CLIENT_ID = "client_id";
	private int position;
	private LinearLayout bar;
	private SmsDbHelper smsAccessor;
	private ConversationModel info;
	private List<SmsModel> items;
	private ListView smsList;
	private Activity act;
	protected SmsAdapter adapter;
	private static final int ACTION_DELETE_MESSAGE = 0;
	private static final int ACTION_DELETE_THREAD = 1;
	private static final int ACTION_FORWARD = 2;
	private SmsSendHelper helper;
	private SmsModel sendingNow;
	private String clientId;
	protected ItemSeenListener itemSeenListener;
	private OnLongClickListener itemLongClickListener;
	private SmsDraftAvailableListener draftAvailableListener;
	private Messenger messenger;
	private Messenger mService;

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

	private Handler smsThreadHandler = new Handler();

	private Handler incomingHandler = new Handler() {
		@Override
		public void handleMessage(Message m) {
			switch (m.what) {
			case SendTaskService.COMPLETE_MESSAGE:
				SmsModel smsModel = (SmsModel) m.getData().getSerializable(
						SmsSendHelper.EXTRA_MESSAGE);
				int pos = m.getData().getInt(EXTRA_ADAPTER_POSITION);
				items.remove(pos);
				items.add(pos, smsModel);
				updateItems(false);
				break;
			case SendTaskService.CANCEL_MESSAGE:
				updateItems(true);
				SmsModel sm = (SmsModel) m.getData().getSerializable(
						SmsSendHelper.EXTRA_MESSAGE);
				draftAvailableListener.draftTextAvailable(sm.getBody(),
						position);
				if (items.size() == 0) {
					smsAccessor.deleteThread(info.getThreadId());
					act.finish();
				}
				break;
			default:
				break;
			}
		}
	};

	public void sendText(final String messageBody, int delay) {
		smsThreadHandler.postDelayed(new Runnable() {
			public void run() {
				SmsModel m = new SmsModel(0, info.getThreadId(), info
						.getAddress(), "", System.currentTimeMillis(),
						messageBody, SmsModel.MESSAGE_TYPE_SENT,
						SmsModel.MESSAGE_READ, SmsModel.STATUS_WAITING);
				Uri u = smsAccessor.insertSms(SmsDbHelper.SMS_URI, m);
				m.setAddressDisplayName(info.getDisplayName());
				m.setId(Long.parseLong(u.getLastPathSegment()));
				sendingNow = m;
				Message message = Message.obtain();
				Bundle b = new Bundle();
				b.putSerializable(SmsSendHelper.EXTRA_MESSAGE, m);
				b.putString(EXTRA_CLIENT_ID, clientId);
				message.what = SendTaskService.QUEUE_MESSAGE;
				message.setData(b);
				try {
					mService.send(message);
					items.add(0, m);
					adapter.notifyDataSetChanged();

				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}, delay);
	}

	protected ActionClickListener mActionClickListener = new ActionClickListener() {

		public void onItemClick(int pos, Bundle b) {
			switch (pos) {
			case ACTION_DELETE_MESSAGE:
				smsAccessor.deleteSms(SmsDbHelper.SMS_URI, b.getLong(EXTRA_MESSAGE_ID));
				items.remove(b.getInt(EXTRA_ADAPTER_POSITION));
				if (items.size() == 0) {
					ConversationList.NEED_REFRESH = true;
				}
				adapter.notifyDataSetChanged();
				break;
			case ACTION_DELETE_THREAD:
				smsAccessor.deleteThread(b.getLong(EXTRA_THREAD_ID));
				ConversationList.NEED_REFRESH = true;
				act.finish();
				break;
			case ACTION_FORWARD:
				break;
			default:
				break;
			}

		}
	};

	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			SmsModel m = items.get(arg2);
			if (m.getStatus() == SmsModel.STATUS_WAITING) {
				try {
					Message msg = Message.obtain();
					msg.what = SendTaskService.CANCEL_MESSAGE;
					Bundle b = new Bundle();
					b.putSerializable(SmsSendHelper.EXTRA_MESSAGE, m);
					b.putInt(EXTRA_ADAPTER_POSITION, arg2);
					b.putString(EXTRA_CLIENT_ID, clientId);
					msg.setData(b);
					mService.send(msg);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			} else {
				CustomPopup p = new CustomPopup(act, arg1);
				p.setContentView(R.layout.popup_layout);
				Bundle b = new Bundle();
				b.putLong(EXTRA_MESSAGE_ID, items.get(arg2).getId());
				b.putLong(EXTRA_THREAD_ID, items.get(arg2).getThreadId());
				b.putInt(EXTRA_ADAPTER_POSITION, arg2);
				ActionModel am = new ActionModel(
						act.getString(R.string.action_delete_message), 0,
						ACTION_DELETE_MESSAGE, b);
				ActionModel am1 = new ActionModel(
						act.getString(R.string.action_delete_thread), 0,
						ACTION_DELETE_THREAD, b);
				ActionModel am2 = new ActionModel(
						act.getString(R.string.action_forward), 0,
						ACTION_FORWARD, b);
				p.addAction(am);
				p.addAction(am1);
				p.addAction(am2);
				p.setmActionClickListener(mActionClickListener);
				p.show();
			}
		}
	};

	private OnMessageSendCompleteListener listener = new OnMessageSendCompleteListener() {

		public void messageSendComplete(boolean success) {
			if (success) {
				items.add(0, sendingNow);
				adapter.notifyDataSetChanged();
				ConversationList.NEED_REFRESH = true;
			}
		}
	};

	@Override
	public void onPause() {
		super.onPause();
	}

	private void initializeViewMembers(View v) {
		act = this.getActivity();
		helper = new SmsSendHelper();
		position = getArguments().getInt(EXTRA_FRAGMENT_POSITION);
		helper.setOnMessageSendCompleteListener(listener);
		clientId = UUID.randomUUID().toString();
		messenger = new Messenger(incomingHandler);
		smsAccessor = new SmsDbHelper(act.getContentResolver());
		bar = (LinearLayout) v.findViewById(R.id.sms_thread_progress_bar);
		smsList = (ListView) v.findViewById(R.id.sms_thread_sms_list);
		smsList.setOnItemClickListener(itemClickListener);
		smsList.setOnLongClickListener(itemLongClickListener);
		info = (ConversationModel) getArguments().getSerializable(
				EXTRA_CONVERSATION_INFO);

	}

	private void updateItems(boolean b) {
		if (b) {
			items = smsAccessor.getSmsForThread(info.getThreadId());
		}
		adapter = new SmsAdapter(act, R.layout.left_sms_item,
				R.layout.right_sms_item, items, info.getDisplayName(), position);
		adapter.setOnItemSeenListener(itemSeenListener);
		smsList.setAdapter(adapter);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		act.bindService(new Intent(act, SendTaskService.class), connection,
				Context.BIND_AUTO_CREATE);		
		if (info.isDraft()) {
			draftAvailableListener.draftTextAvailable(info.getSnippet(), position);
		}
		smsThreadHandler.post(new Runnable() {
			public void run() {
				updateItems(true);
				bar.setVisibility(View.GONE);
				smsList.setVisibility(View.VISIBLE);
				if (getArguments().getBoolean("send_now")) {
					sendFromMainScreen(items.get(0));
				}
			}
		});
	}

	protected void sendFromMainScreen(SmsModel smsModel) {
		Message message = Message.obtain();
		Bundle b = new Bundle();
		b.putSerializable(SmsSendHelper.EXTRA_MESSAGE, smsModel);
		b.putString(EXTRA_CLIENT_ID, clientId);
		message.what = SendTaskService.QUEUE_MESSAGE;
		message.setData(b);
		try {
			mService.send(message);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.sms_thread_layout, container, false);
		initializeViewMembers(v);
		return v;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			Message m = Message.obtain(null, SendTaskService.UNREGISTER);
			mService.send(m);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		act.unbindService(connection);
	}

	public ItemSeenListener getItemSeenListener() {
		return itemSeenListener;
	}

	public void setItemSeenListener(ItemSeenListener itemSeenListener) {
		this.itemSeenListener = itemSeenListener;
	}

	public SmsDraftAvailableListener getDraftAvailableListener() {
		return draftAvailableListener;
	}

	public void setDraftAvailableListener(
			SmsDraftAvailableListener draftAvailableListener) {
		this.draftAvailableListener = draftAvailableListener;
	}

}
