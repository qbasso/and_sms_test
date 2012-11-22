/*
 * @author JPorzuczek
 */
package pl.qbasso.custom;

import pl.qbasso.models.ConversationModel;
import pl.qbasso.smssender.R;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

// TODO: Auto-generated Javadoc
/**
 * The Class ConversationLongClickDialog.
 */
public class ConversationLongClickDialog {
	
	/** The m dialog. */
	private Dialog mDialog;
	
	/** The m model. */
	private ConversationModel mModel;
	
	/** The m context. */
	private Context mContext;
	
	/** The on item selected listener. */
	private OnClickListener onItemSelectedListener = new OnClickListener() {
		
		public void onClick(DialogInterface arg0, int arg1) {
			switch (arg1) {
			case 0:
				
				break;

			default:
				break;
			}
		}
	};
	
	/**
	 * Instantiates a new conversation long click dialog.
	 *
	 * @param context the context
	 * @param m the m
	 */
	public ConversationLongClickDialog(Context context, ConversationModel m) {
		mContext = context;
		mModel = m;
	}
	
	/**
	 * Show.
	 */
	public void show() {
		AlertDialog.Builder b = new Builder(mContext);
		b.setItems(R.array.conversation_actions, onItemSelectedListener );
	}
	
}
