/*
 * @author JPorzuczek
 */
package pl.qbasso.sms;

import pl.qbasso.custom.Utils;
import pl.qbasso.models.SmsModel;
import pl.qbasso.smssender.R;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

// TODO: Auto-generated Javadoc
/**
 * The Class SmsLengthWatcher.
 */
public class SmsLengthWatcher implements TextWatcher {

	/** The output. */
	private TextView output;

	/** The ctx. */
	private Context ctx;

	/**
	 * Instantiates a new sms length watcher.
	 * 
	 * @param ctx
	 *            the ctx
	 * @param outputView
	 *            the output view
	 */
	public SmsLengthWatcher(Context ctx, TextView outputView) {
		output = outputView;
		this.ctx = ctx;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.text.TextWatcher#onTextChanged(java.lang.CharSequence, int,
	 * int, int)
	 */
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
		int len;
		int multipartPenalty;
		if (s.length() > 0) {
			if (Utils.isAscii(s.toString())) {
				len = SmsModel.ASCII_SMS_LENGTH;
				multipartPenalty = SmsModel.MULTIPART_SMS_PENALTY_ASCII;
			} else {
				len = SmsModel.UNICODE_SMS_LENGTH;
				multipartPenalty = SmsModel.MULTIPART_SMS_PENALTY_UNICODE;
			}
			int limit = s.length() / len;
			if (s.length() % len != 0) {
				limit++;
			}
			output.setText(ctx.getString(R.string.sms_length, s.length(), limit
					* len - ((limit - 1) * multipartPenalty), limit));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.text.TextWatcher#afterTextChanged(android.text.Editable)
	 */
	public void afterTextChanged(Editable arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.text.TextWatcher#beforeTextChanged(java.lang.CharSequence,
	 * int, int, int)
	 */
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
	}

}
