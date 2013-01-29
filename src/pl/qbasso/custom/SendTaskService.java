/*
 * @author JPorzuczek
 */
package pl.qbasso.custom;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

import pl.qbasso.activities.AppConstants;
import pl.qbasso.interfaces.ISmsAccess;
import pl.qbasso.models.SmsModel;
import pl.qbasso.sms.Cache;
import pl.qbasso.sms.SmsSendHelper;
import pl.qbassso.smsdb.CustomSmsDbHelper;
import pl.qbassso.smsdb.DefaultSmsProviderHelper;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.util.Patterns;

// TODO: Auto-generated Javadoc
/**
 * The Class SendTaskService.
 */
public class SendTaskService extends Service {

	/** The Constant EXTRA_POSITION. */
	public static final String EXTRA_POSITION = "adapter_position";

	/** The Constant EXTRA_CLIENT_ID. */
	public static final String EXTRA_CLIENT_ID = "client_id";

	/** The m clients. */
	private HashMap<String, Messenger> mClients = new HashMap<String, Messenger>();

	/** The m message queue. */
	private HashMap<Long, CountDownTimer> mMessageQueue = new HashMap<Long, CountDownTimer>();

	/** The Constant REGISTER. */
	public static final int REGISTER = 0;

	/** The Constant UNREGISTER. */
	public static final int UNREGISTER = 1;

	/** The Constant QUEUE_MESSAGE. */
	public static final int QUEUE_MESSAGE = 2;

	/** The Constant CANCEL_MESSAGE. */
	public static final int CANCEL_MESSAGE = 3;

	/** The Constant COMPLETE_MESSAGE. */
	public static final int COMPLETE_MESSAGE = 4;

	/** The Constant MESSAGE_QUEUE_EMPTY. */
	public static final int MESSAGE_QUEUE_EMPTY = 5;

	/** The Constant MESSAGE_QUEUE_NOT_EMPTY. */
	public static final int MESSAGE_QUEUE_NOT_EMPTY = 6;

	/** The m context. */
	private Context mContext;

	/** The m db helper. */
	private ISmsAccess mDbHelper;

	/** The m send helper. */
	private SmsSendHelper mSendHelper;

	private static final String TAG = "SendTaskService";

	/** The incoming handler. */
	private Handler incomingHandler = new Handler() {
		@Override
		public void handleMessage(Message m) {
			Bundle b = m.getData();
			switch (m.what) {
			case REGISTER:
				mClients.put(b.getString(EXTRA_CLIENT_ID), m.replyTo);
				Log.i(TAG, String.format(
						"Client with id %s connected. Total clients: %d",
						b.getString(EXTRA_CLIENT_ID), mClients.size()));
				try {
					m.replyTo.send(Message.obtain(null, REGISTER));
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				break;
			case UNREGISTER:
				mClients.remove(b.getString(EXTRA_CLIENT_ID));
				Log.i(TAG, String.format(
						"Client with id %s unregistered. Total clients: %d",
						b.getString(EXTRA_CLIENT_ID), mClients.size()));
				break;
			case QUEUE_MESSAGE:
				handleQueueMessage(b);
				break;
			case CANCEL_MESSAGE:
				handleCancelMessage(b);
				break;
			case COMPLETE_MESSAGE:
				handleCompleteMessage(b);
				break;
			case MESSAGE_QUEUE_EMPTY:
				handleQueueEmptyMEssage(b);
			default:
				break;
			}
		}

	};

	private void handleQueueEmptyMEssage(Bundle b) {
		String clientId;
		Message toSend;
		clientId = b.getString(EXTRA_CLIENT_ID);
		if (mMessageQueue.isEmpty()) {
			toSend = Message.obtain(null, MESSAGE_QUEUE_EMPTY);
		} else {
			toSend = Message.obtain(null, MESSAGE_QUEUE_NOT_EMPTY);
		}
		try {
			mClients.get(clientId).send(toSend);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void handleQueueMessage(Bundle b) {
		SmsModel smsModel;
		String clientId;
		int adapterPosition;
		smsModel = (SmsModel) b.getSerializable(SmsSendHelper.EXTRA_MESSAGE);
		adapterPosition = b.getInt(EXTRA_POSITION);
		clientId = b.getString(EXTRA_CLIENT_ID);
		addToQueue(smsModel, adapterPosition, clientId);
	}

	private void handleCompleteMessage(Bundle b) {
		Bundle data = new Bundle();
		SmsModel smsModel;
		String clientId;
		Message toSend;
		smsModel = (SmsModel) b.getSerializable(SmsSendHelper.EXTRA_MESSAGE);
		clientId = b.getString(EXTRA_CLIENT_ID);
		data.putSerializable(SmsSendHelper.EXTRA_MESSAGE, smsModel);
		data.putInt(EXTRA_POSITION, b.getInt(EXTRA_POSITION));
		try {
			mDbHelper.updateSmsStatus(smsModel.getId(), SmsModel.STATUS_NONE,
					SmsModel.MESSAGE_TYPE_QUEUED);
			smsModel.setStatus(SmsModel.STATUS_NONE);
			smsModel.setSmsType(SmsModel.MESSAGE_TYPE_QUEUED);
			toSend = Message.obtain(null, COMPLETE_MESSAGE);
			toSend.setData(data);
			if (!clientId.equals("") && mClients.get(clientId) != null) {
				mClients.get(clientId).send(toSend);
			}
			mMessageQueue.remove(smsModel.getId());
			mSendHelper.sendText(mContext, smsModel, false);
			Log.i(TAG, String.format(
					"Message %s:%s sent. Queued messages: %d",
					smsModel.getAddressDisplayName() != null ? smsModel
							.getAddressDisplayName() : smsModel.getAddress(),
					smsModel.getBody(), mMessageQueue.size()));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void handleCancelMessage(Bundle b) {
		SmsModel smsModel;
		Bundle data = new Bundle();
		String clientId;
		Message toSend;
		smsModel = (SmsModel) b.getSerializable(SmsSendHelper.EXTRA_MESSAGE);
		clientId = b.getString(EXTRA_CLIENT_ID);
		if (mMessageQueue.containsKey(smsModel.getId())) {
			mMessageQueue.get(smsModel.getId()).cancel();
			mMessageQueue.remove(smsModel.getId());
			Log.i(TAG, String.format(
					"Message %s:%s canceled. Queued messages: %d",
					smsModel.getAddressDisplayName() != null ? smsModel
							.getAddressDisplayName() : smsModel.getAddress(),
					smsModel.getBody(), mMessageQueue.size()));
			data.putSerializable(SmsSendHelper.EXTRA_MESSAGE, smsModel);
			data.putInt(EXTRA_POSITION, b.getInt(EXTRA_POSITION));
			try {
				toSend = Message.obtain(null, CANCEL_MESSAGE);
				toSend.setData(data);
				mDbHelper.deleteSms(smsModel.getId());
				if (mClients.get(clientId) != null) {
					mClients.get(clientId).send(toSend);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private void addToQueue(final SmsModel message, final int adapterPosition,
			final String clientId) {
		CountDownTimer timer = new CountDownTimer(4000, 1000) {

			private int position = adapterPosition;
			private String id = clientId;
			public SmsModel msg = message;

			@Override
			public void onTick(long millisUntilFinished) {
			}

			@Override
			public void onFinish() {
				Bundle b = new Bundle();
				b.putSerializable(SmsSendHelper.EXTRA_MESSAGE, msg);
				b.putInt(EXTRA_POSITION, position);
				b.putString(EXTRA_CLIENT_ID, id);
				Message m = Message.obtain();
				m.what = COMPLETE_MESSAGE;
				m.setData(b);
				incomingHandler.sendMessage(m);
			}
		};
		mMessageQueue.put(message.getId(), timer);
		Log.i(TAG, String.format(
				"Message %s:%s queued. Queued messages: %d",
				message.getAddressDisplayName() != null ? message
						.getAddressDisplayName() : message.getAddress(),
				message.getBody(), mMessageQueue.size()));
		timer.start();
	}

	/** The messenger. */
	private Messenger messenger = new Messenger(incomingHandler);

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return messenger.getBinder();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onStart(android.content.Intent, int)
	 */
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
		if (AppConstants.DB == 1) {
			mDbHelper = new DefaultSmsProviderHelper(getContentResolver());
		} else {
			mDbHelper = new CustomSmsDbHelper(getContentResolver());
		}
		mSendHelper = new SmsSendHelper();
		List<SmsModel> l = mDbHelper.getMessagesNotSent();
		Iterator<SmsModel> it = l.iterator();
		while (it.hasNext()) {
			SmsModel smsModel = it.next();
			Matcher m = Patterns.PHONE.matcher(smsModel.getAddress());
			if (m.find()
					&& m.end() - m.start() == smsModel.getAddress().length()) {
				smsModel.setDate(System.currentTimeMillis());
				addToQueue(smsModel, 0, "");
			} else {
				mDbHelper.deleteSms(smsModel.getId());
				l.remove(smsModel);
			}
		}
		Log.i(TAG, String.format("Loaded %d messages to send", l.size()));
	}

}
