package pl.qbasso.activities;

import pl.qbasso.custom.ContactsAdapter;
import pl.qbasso.smssender.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;

public class SendSms extends Activity {

	private AutoCompleteTextView input;
	
	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.send_sms_screen);
		input = (AutoCompleteTextView) findViewById(R.id.recipient_input);
		input.setAdapter(new ContactsAdapter(this, R.layout.contact_item));
		input.setThreshold(1);
		input.setOnItemClickListener(itemClickListener);
	}


}
