package pl.qbasso.activities;

import java.util.ArrayList;
import java.util.List;

import pl.qbasso.custom.SmsAdapterSelectable;
import pl.qbasso.interfaces.ISmsAccess;
import pl.qbasso.models.ConversationModel;
import pl.qbasso.models.SmsModel;
import pl.qbasso.smssender.R;
import pl.qbassso.smsdb.CustomSmsDbHelper;
import pl.qbassso.smsdb.DefaultSmsProviderHelper;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

public class DeleteMultipleSms extends Activity {

	private List<SmsModel> mItems = new ArrayList<SmsModel>();
	private ListView mSmsList;
	private SmsAdapterSelectable mAdapter;
	private Button mConfirmButton;
	private Button mCancelButton;
	private ISmsAccess helper;
	private ConversationModel mInfo;
	private OnClickListener mConfirmListener = new OnClickListener() {

		public void onClick(View v) {
			int counter = 0;
			boolean checked[] = mAdapter.getChecked();
			for (int i = 0; i < checked.length; i++) {
				if (checked[i]) {
					helper.deleteSms(mItems.get(i).getId());
					counter++;
				}
			}
			Intent i = new Intent();
			i.putExtra("items_left", mItems.size() - counter);
			setResult(Activity.RESULT_OK, i);
			finish();
		}
	};
	private OnClickListener mCancelListener = new OnClickListener() {

		public void onClick(View v) {
			setResult(Activity.RESULT_CANCELED);
			finish();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.delete_sms_activity);
		mSmsList = (ListView) findViewById(R.id.sms_thread_sms_list);
		mConfirmButton = (Button) findViewById(R.id.confirm_button);
		mCancelButton = (Button) findViewById(R.id.cancel_button);
		mConfirmButton.setOnClickListener(mConfirmListener);
		mCancelButton.setOnClickListener(mCancelListener);
		if (AppConstants.DB == 1) {
			helper = new DefaultSmsProviderHelper(getContentResolver());
		} else {
			helper = new CustomSmsDbHelper(getContentResolver());
		}
		mInfo = (ConversationModel) getIntent().getSerializableExtra("info");
		mItems = helper.getSmsForThread(mInfo.getThreadId());
		mAdapter = new SmsAdapterSelectable(this, R.layout.left_sms_item,
				R.layout.right_sms_item, mItems, mInfo.getDisplayName(), 0);
		mSmsList.setAdapter(mAdapter);
	}

}
