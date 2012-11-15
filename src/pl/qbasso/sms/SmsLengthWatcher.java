package pl.qbasso.sms;

import pl.qbasso.custom.Utils;
import pl.qbasso.models.SmsModel;
import pl.qbasso.smssender.R;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

public class SmsLengthWatcher implements TextWatcher {
	
	private TextView output;
	private Context ctx;
	
	public SmsLengthWatcher(Context ctx, TextView outputView) {
		output = outputView;
		this.ctx = ctx;
	}

	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub

	}

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub

	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
		int len;
		if (Utils.isAscii(s.toString())) {
			len = SmsModel.ASCII_SMS_LENGTH;
		} else {
			len = SmsModel.UNICODE_SMS_LENGTH;
		}
		int limit = s.length() / len;
		if (s.length() % len != 0) {
			limit++;
		}
		output.setText(ctx.getString(R.string.sms_length, s.length(),
				limit * len, limit));
	}

}
