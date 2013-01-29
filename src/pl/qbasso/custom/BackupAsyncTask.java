package pl.qbasso.custom;

import pl.qbasso.activities.AppConstants;
import pl.qbasso.interfaces.ISmsAccess;
import pl.qbassso.smsdb.CustomSmsDbHelper;
import pl.qbassso.smsdb.DefaultSmsProviderHelper;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Looper;
import android.widget.Toast;

public class BackupAsyncTask extends AsyncTask<String, Integer, Integer> {

	public static final int TYPE_BACKUP = 0;
	public static final int TYPE_RESTORE = 1;

	private Context mContext;

	// private BackupProgressDialog mDialog;

	private int mType;

	private Dialog mDialog;

	public interface BackupAsyncTaskListener {
		public void onBackupRestoreComplete();
	}

	private BackupAsyncTaskListener mListener;

	// private static class BackupProgressDialog extends DialogFragment {
	//
	//
	// public static BackupProgressDialog getInstance(int type) {
	// BackupProgressDialog dialog = new BackupProgressDialog();
	// return dialog;
	// }
	//
	// @Override
	// public Dialog onCreateDialog(Bundle savedInstanceState) {
	// Dialog result;
	// AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
	// // switch (type) {
	// // case TYPE_BACKUP:
	// // result =
	// b.setMessage("Please wait while backup is running").setCancelable(true).create();
	// // break;
	// // case TYPE_RESTORE:
	// // result =
	// b.setMessage("Please wait while backup is running").setCancelable(true).create();
	// // break;
	// // default:
	// // break;
	// // }
	// result =
	// b.setMessage("Please wait while backup is running").setCancelable(true).create();
	// return result;
	// }
	//
	// }

	public BackupAsyncTask(Context context, int type, Dialog dialog) {
		mContext = context;
		mDialog = dialog;
		mType = type;
		mListener = (BackupAsyncTaskListener) mContext;
	}

	@Override
	protected void onPreExecute() {
		mDialog.show();
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
	}

	@Override
	protected Integer doInBackground(String... arg0) {
		Looper.prepare();
		ISmsAccess smsDb;

		if (AppConstants.DB == 1) {
			smsDb = new DefaultSmsProviderHelper(mContext.getContentResolver());
		} else {
			smsDb = new CustomSmsDbHelper(mContext.getContentResolver());
		}
		if (mType == TYPE_BACKUP) {
			smsDb.performBackup(arg0[0]);
		} else if (mType == TYPE_RESTORE) {
			final String msg = smsDb.readBackupFile(arg0[0]);
			mListener.onBackupRestoreComplete();
			((Activity) mContext).runOnUiThread(new Runnable() {
				public void run() {
					// TODO Auto-generated method stub
					Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
				}
			});
		}

		return null;
	}

	@Override
	protected void onPostExecute(Integer result) {
		if (mDialog.isShowing()) {
			mDialog.dismiss();
		}
	}

	public void setBackupAsyncTaskListener(BackupAsyncTaskListener listener) {
		mListener = listener;
	}

}
