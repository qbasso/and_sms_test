package pl.qbasso.loaders;

import android.database.ContentObserver;
import android.os.Handler;

public class SmsContentObserver extends ContentObserver {

	private Handler mHandler;
	
	public SmsContentObserver(Handler handler) {
		super(handler);
		mHandler = handler;
	}

	@Override
	public void onChange(boolean selfChange) {
		super.onChange(selfChange);
		mHandler.sendEmptyMessage(0);
	}

}