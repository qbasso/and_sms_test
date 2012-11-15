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
	
	private static SendTaskService instance;
	
	public SendTaskService() {
		// TODO Auto-generated constructor stub
	}
	
	public static SendTaskService getInstance() {
		if (instance==null) {
			instance = new SendTaskService();
		}
		return instance;
	}

	public static final String EXTRA_POSITION = "adapter_position";
	public static final String EXTRA_CLIENT_ID = "client_id";
	private HashMap<String, Messenger> mClients = new HashMap<String, Messenger>();
	private HashMap<Long, CountDownTimer> mMessageQueue = new HashMap<Long, CountDownTimer>();

	public static final int REGISTER = 0;
	public static final int UNREGISTER = 1;
	public static final int QUEUE_MESSAGE = 2;
	public static final int CANCEL_MESSAGE = 3;
	public static final int COMPLETE_MESSAGE = 4;
	private Context mContext;
	private SmsDbHelper mDbHelper;
	private SmsSendHelper mSendHelper;

	private Handler incomingHandler = new Handler() {
		@Override
		public void handleMessage(Message m) {
			SmsModel smsModel;
			String clientId;
			Message toSend;
			int adapterPosition;
			Bundle b = m.getData();
			Bundle data = new Bundle();
			switch (m.what) {
			case REGISTER:
				mClients.put(b.getString(EXTRA_CLIENT_ID), m.replyTo);
				break;
			case UNREGISTER:
				mClients.remove(m.replyTo);
				break;
			case QUEUE_MESSAGE:
				smsModel = (SmsModel) b
						.getSerializable(SmsSendHelper.EXTRA_MESSAGE);
				adapterPosition = b.getInt(EXTRA_POSITION);
				clientId = b.getString(EXTRA_CLIENT_ID);
				addToQueue(smsModel, adapterPosition, clientId);
				break;
			case CANCEL_MESSAGE:
				smsModel = (SmsModel) b
						.getSerializable(SmsSendHelper.EXTRA_MESSAGE);
				clientId = b.getString(EXTRA_CLIENT_ID);
				if (mMessageQueue.containsKey(smsModel.getId())) {
					mMessageQueue.get(smsModel.getId()).cancel();
					mMessageQueue.remove(smsModel.getId());
					data.putLong(SmsSendHelper.EXTRA_MESSAGE_ID,
							smsModel.getId());
					data.putInt(EXTRA_POSITION,
							b.getInt(EXTRA_POSITION));
					try {
						toSend = Message.obtain(null, CANCEL_MESSAGE);
						toSend.setData(data);
						mClients.get(clientId).send(toSend);
						mDbHelper.deleteSms(smsModel.getId());
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
				break;
			case COMPLETE_MESSAGE:
				smsModel = (SmsModel) b
						.getSerializable(SmsSendHelper.EXTRA_MESSAGE);
				clientId = b.getString(EXTRA_CLIENT_ID);
				data.putLong(SmsSendHelper.EXTRA_MESSAGE_ID, smsModel.getId());
				data.putInt(EXTRA_POSITION, b.getInt(EXTRA_POSITION));
				try {
					toSend = Message.obtain(null, COMPLETE_MESSAGE);
					toSend.setData(data);
					mDbHelper.updateSmsStatus(
							Uri.withAppendedPath(SmsDbHelper.SMS_URI,
									String.valueOf(smsModel.getId())),
							SmsModel.STATUS_PENDING);
					mClients.get(clientId).send(toSend);
					mMessageQueue.remove(smsModel);
					mSendHelper.sendText(mContext, smsModel, false);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				break;
			default:

				break;
			}
		}

		private void addToQueue(final SmsModel message,
				final int adapterPosition, final String clientId) {
			CountDownTimer timer = new CountDownTimer(5000, 1000) {

				private int position = adapterPosition;
				private String id = clientId;
				public SmsModel m = message;

				@Override
				public void onTick(long millisUntilFinished) {
				}

				@Override
				public void onFinish() {
					Bundle b = new Bundle();
					b.putSerializable(SmsSendHelper.EXTRA_MESSAGE, m);
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
		// TODO Auto-generated method stub
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
