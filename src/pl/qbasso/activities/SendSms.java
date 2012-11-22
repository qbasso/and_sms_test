/*
 * @author JPorzuczek
 */
package pl.qbasso.activities;

import pl.qbasso.custom.ContactsAdapter;
import pl.qbasso.smssender.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;

// TODO: Auto-generated Javadoc
/**
 * The Class SendSms.
 */
public class SendSms extends Activity {

	/** The input. */
	private AutoCompleteTextView input;
	
	/** The item click listener. */
	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			
		}
	};

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
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
