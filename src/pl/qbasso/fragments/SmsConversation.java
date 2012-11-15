package pl.qbasso.fragments;

import java.util.List;

import pl.qbasso.activities.ConversationList;
import pl.qbasso.custom.SmsAdapter;
import pl.qbasso.interfaces.ActionClickListener;
import pl.qbasso.interfaces.ItemSeenListener;
import pl.qbasso.interfaces.OnMessageSendCompleteListener;
import pl.qbasso.models.ActionModel;
import pl.qbasso.models.ConversationModel;
import pl.qbasso.models.SmsModel;
import pl.qbasso.sms.SmsDbHelper;
import pl.qbasso.sms.SmsSendHelper;
import pl.qbasso.smssender.R;
import pl.qbasso.view.CustomPopup;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

public class SmsConversation extends Fragment {

	private int position;
	private LinearLayout bar;
	private SmsDbHelper smsAccessor;
	private ConversationModel info;
	private List<SmsModel> items;
	private ListView smsList;
	private Activity act;
	protected SmsAdapter adapter;
	private static final int ACTION_DELETE_MESSAGE = 0;
	private static final int ACTION_DELETE_THREAD = 1;
	private static final int ACTION_FORWARD = 2;
	private SmsSendHelper helper;
	private SmsModel sendingNow;

	public SmsConversation() {
	}

	public SmsConversation(int position, ItemSeenListener listener) {
		this.position = position;
		itemSeenListener = listener;
	}

	private Handler smsThreadHandler = new Handler();

	public void sendText(final String messageBody, final long msgId, int delay) {
		smsThreadHandler.postDelayed(new Runnable() {
			public void run() {
				SmsModel m = new SmsModel(0, info.getThreadId(), info
						.getAddress(), "", System.currentTimeMillis(),
						messageBody, SmsModel.MESSAGE_TYPE_SENT,
						SmsModel.MESSAGE_READ, SmsModel.STATUS_WAITING);
				sendingNow = m;
				m.setAddressDisplayName(info.getDisplayName());
				helper.sendTextWithDialog(act, m, false);
				items.add(0, m);
				adapter.notifyDataSetChanged();
			}
		}, delay);
	}

	protected ActionClickListener mActionClickListener = new ActionClickListener() {

		public void onItemClick(int pos, Bundle b) {
			switch (pos) {
			case ACTION_DELETE_MESSAGE:
				smsAccessor.deleteSms(b.getLong("message_id"));
				items.remove(b.getInt("adapter_position"));
				if (items.size() == 0) {
					ConversationList.NEED_REFRESH = true;
				}
				adapter.notifyDataSetChanged();
				break;
			case ACTION_DELETE_THREAD:
				smsAccessor.deleteThread(b.getLong("thread_id"));
				ConversationList.NEED_REFRESH = true;
				act.finish();
				break;
			case ACTION_FORWARD:
				break;
			default:
				break;
			}

		}
	};

	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			SmsModel m = items.get(arg2);
			if (m.getStatus() == SmsModel.STATUS_WAITING) {
				items.remove(arg2);
				adapter.notifyDataSetChanged();
			} else {
				CustomPopup p = new CustomPopup(act, arg1);
				p.setContentView(R.layout.popup_layout);
				Bundle b = new Bundle();
				b.putLong("message_id", items.get(arg2).getId());
				b.putLong("thread_id", items.get(arg2).getThreadId());
				b.putInt("adapter_position", arg2);
				ActionModel am = new ActionModel(
						act.getString(R.string.action_delete_message), 0,
						ACTION_DELETE_MESSAGE, b);
				ActionModel am1 = new ActionModel(
						act.getString(R.string.action_delete_thread), 0,
						ACTION_DELETE_THREAD, b);
				ActionModel am2 = new ActionModel(
						act.getString(R.string.action_forward), 0,
						ACTION_FORWARD, b);
				p.addAction(am);
				p.addAction(am1);
				p.addAction(am2);
				p.setmActionClickListener(mActionClickListener);
				p.show();
			}
		}
	};

	protected ItemSeenListener itemSeenListener;
	private OnLongClickListener itemLongClickListener;
	private OnMessageSendCompleteListener listener = new OnMessageSendCompleteListener() {

		public void messageSendComplete(boolean success) {
			if (success) {
				items.add(0, sendingNow);
				adapter.notifyDataSetChanged();
				ConversationList.NEED_REFRESH = true;
			}
		}
	};

	@Override
	public void onPause() {
		super.onPause();
	}

	private void initializeViewMembers(View v) {
		act = this.getActivity();
		helper = new SmsSendHelper();
		helper.setOnMessageSendCompleteListener(listener);
		smsAccessor = new SmsDbHelper(act.getContentResolver());
		bar = (LinearLayout) v.findViewById(R.id.sms_thread_progress_bar);
		smsList = (ListView) v.findViewById(R.id.sms_thread_sms_list);
		smsList.setOnItemClickListener(itemClickListener);
		smsList.setOnLongClickListener(itemLongClickListener);
		info = (ConversationModel) getArguments()
				.getSerializable("thread_info");
	}

	private void updateItems() {
		items = smsAccessor.getSmsForThread(info.getThreadId());
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		smsThreadHandler.post(new Runnable() {
			public void run() {
				updateItems();
				adapter = new SmsAdapter(act, R.layout.left_sms_item,
						R.layout.right_sms_item, items, info.getDisplayName(),
						position);
				adapter.setOnItemSeenListener(itemSeenListener);
				smsList.setAdapter(adapter);
				bar.setVisibility(View.GONE);
				smsList.setVisibility(View.VISIBLE);
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.sms_thread_layout, container, false);
		initializeViewMembers(v);
		return v;
	}

}
