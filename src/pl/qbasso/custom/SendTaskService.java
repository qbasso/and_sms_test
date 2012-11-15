package pl.qbasso.custom;

import java.util.ArrayList;
import java.util.HashMap;

import pl.qbasso.models.SmsModel;
import pl.qbasso.sms.SmsSendHelper;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

public class SendTaskService extends Service {

	private ArrayList<Messenger> clients = new ArrayList<Messenger>();
	private HashMap<Integer, CountDownTimer> messageQueue = new HashMap<Integer, CountDownTimer>();
	
	private static final int REGISTER = 0;
	private static final int UNREGISTER = 1;
	private static final int QUEUE_MESSAGE = 2;
	private static final int CANCEL_MESSAGE = 3;
	private static final int COMPLETE_MESSAGE = 4;

	private Handler incomingHandler = new Handler() {
		@Override
		public void handleMessage(Message m) {
			Bundle b = m.getData();
			String clientId = b.getString("client_id");
			switch (m.what) {
			case REGISTER:
				clients.add(m.replyTo);
				break;
			case UNREGISTER:
				clients.remove(m.replyTo);
				break;
			case QUEUE_MESSAGE:
				SmsModel msg = (SmsModel) b.getSerializable(SmsSendHelper.EXTRA_MESSAGE);
				addToQueue(msg);
			case COMPLETE_MESSAGE:
				break;
			default:
				
				break;
			}
		}

		private void addToQueue(final SmsModel msg) {
			CountDownTimer timer = new CountDownTimer(5000, 1000) {
				
				@Override
				public void onTick(long millisUntilFinished) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onFinish() {
					Message m = Message.obtain();
					m.what = COMPLETE_MESSAGE;
					m.arg1 = msg.getId();
					
					m.setData(b);
					incomingHandler.sendMessage(m);
				}
			};
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
		// TODO Auto-generated method stub
		super.onCreate();
	}

}
