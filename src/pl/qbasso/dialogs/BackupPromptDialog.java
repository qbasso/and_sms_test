package pl.qbasso.dialogs;

import pl.qbasso.smssender.R;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

public class BackupPromptDialog extends DialogFragment {

	public static BackupPromptDialog getInstance(int type) {
		BackupPromptDialog dialog = new BackupPromptDialog();
		mType = type;
		return dialog;
	}

	private static int mType;
	private EditText mFileInput;
	private BackupDialogListener mListener;

	private OnClickListener okButtonListener = new OnClickListener() {

		public void onClick(View v) {
			if (mFileInput.length() > 0) {
				mListener.onDialogConfirm(BackupPromptDialog.this, mFileInput
						.getText().toString(), mType);
			}
		}
	};

	private OnClickListener cancelButtonListener = new OnClickListener() {
		public void onClick(View v) {
			BackupPromptDialog.this.dismiss();
		}
	};
	
//	private android.content.DialogInterface.OnClickListener okButtonListener = new DialogInterface.OnClickListener() {
//
//		public void onClick(DialogInterface dialog, int which) {
//			if (mFileInput.length() > 0) {
//				mListener.onDialogConfirm(BackupPromptDialog.this, mFileInput
//						.getText().toString(), mType);
//			}
//		}
//	};
//
//	private android.content.DialogInterface.OnClickListener cancelButtonListener = new DialogInterface.OnClickListener() {
//
//		public void onClick(DialogInterface dialog, int which) {
//			BackupPromptDialog.this.dismiss();
//		}
//	};

	public interface BackupDialogListener {
		public void onDialogConfirm(DialogFragment dialog, String fileName,
				int dialogType);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Dialog.Builder b = new Dialog.Builder(getActivity());
		Dialog dialog = new Dialog(getActivity());
		dialog.setContentView(getActivity().getLayoutInflater().inflate(
				R.layout.backup_prompt_dialog, null));
		dialog.setTitle("Kopia zapasowa");
		// dialog = b.create();
		dialog.show();
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		dialog.getWindow().setAttributes(lp);
		mFileInput = (EditText) dialog.findViewById(R.id.file_input);
		((Button) dialog.findViewById(R.id.ok_button))
				.setOnClickListener(okButtonListener);
		((Button) dialog.findViewById(R.id.cancel_button))
				.setOnClickListener(cancelButtonListener);
		return dialog;
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mListener = (BackupDialogListener) activity;
	}

}
