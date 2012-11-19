package pl.qbasso.custom;

import java.util.HashMap;

import pl.qbasso.models.SmsModel;
import pl.qbasso.sms.SmsDbHelper;
import pl.qbasso.sms.SmsSendHelper;
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

public class SendTaskService extends Service {

	public static final String EXTRA_POSITION = "adapter_position";
	public static final String EXTRA_CLIENT_ID = "client_id";
	private HashMap<String, Messenger> mClients = new HashMap<String, Messenger>();
	private HashMap<Long, CountDownTimer> mMessageQueue = new HashMap<Long, CountDownTimer>();

	public static final int REGISTER = 0;
	public static final int UNREGISTER = 1;
	public static final int QUEUE_MESSAGE = 2;
	public static final int CANCEL_MESSAGE = 3;
	public static final int COMPLETE_MESSAGE = 4;
	public static final int MESSAGE_QUEUE_EMPTY = 5;
	public static final int MESSAGE_QUEUE_NOT_EMPTY = 6;
	private Context mContext;
	private SmsDbHelper mDbHelper;
	private SmsSendHelper mSendHelper;

	private Handler incomingHandler = new Handler() {
		@Override
		public void handleMessage(Message m) {
			Bundle b = m.getData();
			switch (m.what) {
			case REGISTER:
				mClients.put(b.getString(EXTRA_CLIENT_ID), m.replyTo);
				break;
			case UNREGISTER:
				mClients.remove(m.replyTo);
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
			smsModel = (SmsModel) b
					.getSerializable(SmsSendHelper.EXTRA_MESSAGE);
			adapterPosition = b.getInt(EXTRA_POSITION);
			clientId = b.getString(EXTRA_CLIENT_ID);
			addToQueue(smsModel, adapterPosition, clientId);
		}

		private void handleCompleteMessage(Bundle b) {
			Bundle data = new Bundle();
			SmsModel smsModel;
			String clientId;
			Message toSend;
			smsModel = (SmsModel) b
					.getSerializable(SmsSendHelper.EXTRA_MESSAGE);
			clientId = b.getString(EXTRA_CLIENT_ID);
			data.putSerializable(SmsSendHelper.EXTRA_MESSAGE, smsModel);
			data.putInt(EXTRA_POSITION, b.getInt(EXTRA_POSITION));
			try {
				mDbHelper.updateSmsStatus(
						Uri.withAppendedPath(SmsDbHelper.SMS_URI,
								String.valueOf(smsModel.getId())),
						SmsModel.STATUS_PENDING, SmsModel.MESSAGE_TYPE_SENT);
				smsModel.setStatus(SmsModel.STATUS_PENDING);
				smsModel.setSmsType(SmsModel.MESSAGE_TYPE_SENT);
				toSend = Message.obtain(null, COMPLETE_MESSAGE);
				toSend.setData(data);
				mClients.get(clientId).send(toSend);
				mMessageQueue.remove(smsModel.getId());
				mSendHelper.sendText(mContext, smsModel, false);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		private void handleCancelMessage(Bundle b) {
			SmsModel smsModel;
			Bundle data = new Bundle();
			String clientId;
			Message toSend;
			smsModel = (SmsModel) b
					.getSerializable(SmsSendHelper.EXTRA_MESSAGE);
			clientId = b.getString(EXTRA_CLIENT_ID);
			if (mMessageQueue.containsKey(smsModel.getId())) {
				mMessageQueue.get(smsModel.getId()).cancel();
				mMessageQueue.remove(smsModel.getId());
				data.putSerializable(SmsSendHelper.EXTRA_MESSAGE, smsModel);
				data.putInt(EXTRA_POSITION, b.getInt(EXTRA_POSITION));
				try {
					toSend = Message.obtain(null, CANCEL_MESSAGE);
					toSend.setData(data);
					mDbHelper.deleteSms(SmsDbHelper.SMS_URI, smsModel.getId());
					mClients.get(clientId).send(toSend);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}

		private void addToQueue(final SmsModel message,
				final int adapterPosition, final String clientId) {
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
			timer.start();
		}
	};

	private Messenger messenger = new Messenger(incomingHandler);

	@Override
	public IBinder onBind(Intent intent) {
		return messenger.getBinder();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
		mDbHelper = new SmsDbHelper(getContentResolver());
		mSendHelper = new SmsSendHelper();
	}

}
