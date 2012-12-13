package pl.qbasso.fragments;

import java.util.List;
import java.util.UUID;

import pl.qbasso.activities.SendSms;
import pl.qbasso.custom.SendTaskService;
import pl.qbasso.custom.SmsAdapter;
import pl.qbasso.interfaces.ActionClickListener;
import pl.qbasso.interfaces.ItemSeenListener;
import pl.qbasso.interfaces.OnMessageSendCompleteListener;
import pl.qbasso.interfaces.SmsDraftAvailableListener;
import pl.qbasso.models.ActionModel;
import pl.qbasso.models.ConversationModel;
import pl.qbasso.models.SmsModel;
import pl.qbasso.sms.Cache;
import pl.qbasso.sms.SmsDbHelper;
import pl.qbasso.sms.SmsSendHelper;
import pl.qbasso.smssender.R;
import pl.qbasso.view.CustomPopup;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
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
	private static final String TAG = "SmsConversation";
	private static final String EXTRA_MESSAGE_BODY = "message_body";
	private SmsSendHelper helper;
	private SmsModel sendingNow;
	private String clientId;
	protected ItemSeenListener itemSeenListener;
	private SmsDraftAvailableListener draftAvailableListener;
	private Messenger messenger;
	private Messenger mService;
	private NotificationManager nm;

	private OnItemLongClickListener itemLongClickListener = new OnItemLongClickListener() {

		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
			final SmsModel item = items.get(arg2);
			AlertDialog.Builder b = new AlertDialog.Builder(act);
			b.setTitle("Opcje wiadomoœci").setItems(R.array.message_actions,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
							case 0:
								Intent i = new Intent(act, SendSms.class);
								i.putExtra(EXTRA_MESSAGE_BODY, item.getBody());
								dialog.dismiss();
								startActivity(i);
								break;
							default:
								break;
							}
						}
					});
			b.create().show();
			return false;
		}
	};

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
				items.add(smsModel);
				updateItems(false);
				Cache.addToRefreshSet(info.getThreadId(), true);
				break;
			case SendTaskService.CANCEL_MESSAGE:
				updateItems(true);
				SmsModel sm = (SmsModel) m.getData().getSerializable(
						SmsSendHelper.EXTRA_MESSAGE);
				draftAvailableListener.draftTextAvailable(sm.getBody(),
						position);
				if (items.size() == 0) {
					smsAccessor.deleteThread(info.getThreadId());
					draftAvailableListener.draftTextAvailable("", position);
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
						messageBody, SmsModel.MESSAGE_TYPE_QUEUED,
						SmsModel.MESSAGE_READ, SmsModel.STATUS_WAITING);
				Uri u = smsAccessor.insertSms(m);
				m.setAddressDisplayName(info.getDisplayName());
				m.setId(Long.parseLong(u.getLastPathSegment()));
				sendingNow = m;
				Message message = Message.obtain();
				Bundle b = new Bundle();
				b.putSerializable(SmsSendHelper.EXTRA_MESSAGE, m);
				b.putString(EXTRA_CLIENT_ID, clientId);
				b.putInt(EXTRA_ADAPTER_POSITION, items.size());
				message.what = SendTaskService.QUEUE_MESSAGE;
				message.setData(b);
				try {
					mService.send(message);
					items.add(m);
					adapter.notifyDataSetChanged();
					smsList.setSelection(smsList.getCount() - 1);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}, delay);
	}

	protected ActionClickListener mActionClickListener = new ActionClickListener() {

		public void onItemClick(View v, int pos, final Bundle b) {
			switch (pos) {
			case ACTION_DELETE_MESSAGE:
				smsAccessor.deleteSms(b.getLong(EXTRA_MESSAGE_ID));
				Animation anim = AnimationUtils.loadAnimation(act,
						R.anim.slide_right);
				v.startAnimation(anim);
				smsThreadHandler.postDelayed(new Runnable() {
					public void run() {
						items.remove(b.getInt(EXTRA_ADAPTER_POSITION));
						Cache.addToRefreshSet(info.getThreadId(), false);
						if (items.size() == 0) {
							draftAvailableListener.draftTextAvailable("",
									position);
							act.finish();
						}
						adapter.notifyDataSetChanged();

					}
				}, anim.getDuration());
				break;
			case ACTION_DELETE_THREAD:
				smsAccessor.deleteThread(b.getLong(EXTRA_THREAD_ID));
				Cache.delete(info.getThreadId());
				Cache.addToRefreshSet(info.getThreadId(), true);
				draftAvailableListener.draftTextAvailable("", position);
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
						ACTION_DELETE_MESSAGE, b, arg1);
				ActionModel am1 = new ActionModel(
						act.getString(R.string.action_delete_thread), 0,
						ACTION_DELETE_THREAD, b, arg1);
				ActionModel am2 = new ActionModel(
						act.getString(R.string.action_forward), 0,
						ACTION_FORWARD, b, arg1);
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
				Cache.addToRefreshSet(info.getThreadId(), true);
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
		Log.i(TAG,
				String.format("Creating fragment with client id %s", clientId));
		messenger = new Messenger(incomingHandler);
		smsAccessor = new SmsDbHelper(act.getContentResolver());
		bar = (LinearLayout) v.findViewById(R.id.sms_thread_progress_bar);
		smsList = (ListView) v.findViewById(R.id.sms_thread_sms_list);
		smsList.setOnItemClickListener(itemClickListener);
		smsList.setOnItemLongClickListener(itemLongClickListener);
		info = (ConversationModel) getArguments().getSerializable(
				EXTRA_CONVERSATION_INFO);
	}

	public void updateItems(boolean reload) {
		if (reload) {
			items = smsAccessor.getSmsForThread(info.getThreadId());
		}
		adapter = new SmsAdapter(act, R.layout.left_sms_item,
				R.layout.right_sms_item, items, info.getDisplayName(), position);
		adapter.setOnItemSeenListener(itemSeenListener);
		smsList.setAdapter(adapter);
		smsList.setSelection(smsList.getCount() - 1);
	}

	public void updateItem(SmsModel m) {
		items.add(m);
		Cache.addToRefreshSet(m.getThreadId(), true);
		Log.i("SmsConversation",
				String.format("Items after update: %d", items.size()));
		adapter.notifyDataSetChanged();
		smsList.setSelection(smsList.getCount() - 1);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		act.bindService(new Intent(act, SendTaskService.class), connection,
				Context.BIND_AUTO_CREATE);
		smsThreadHandler.post(new Runnable() {
			public void run() {
				updateItems(true);
				bar.setVisibility(View.GONE);
				smsList.setVisibility(View.VISIBLE);
				if (getArguments().getBoolean("send_now") && position == 0) {
					sendFromMainScreen(items.get(items.size() - 1));
				}
			}
		});
	}

	protected void sendFromMainScreen(SmsModel smsModel) {
		Message message = Message.obtain();
		Bundle b = new Bundle();
		b.putSerializable(SmsSendHelper.EXTRA_MESSAGE, smsModel);
		b.putString(EXTRA_CLIENT_ID, clientId);
		b.putInt(EXTRA_ADAPTER_POSITION, items.size() - 1);
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

	@Override
	public void onDestroyView() {
		try {
			Message m = Message.obtain(null, SendTaskService.UNREGISTER);
			Bundle b = new Bundle();
			b.putString(EXTRA_CLIENT_ID, clientId);
			m.setData(b);
			mService.send(m);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		act.unbindService(connection);
		super.onDestroyView();
	}

	@Override
	public void onResume() {
		if (info.isDraft()) {
			draftAvailableListener.draftTextAvailable(info.getSnippet(),
					position);
		}
		super.onResume();
	}

}
