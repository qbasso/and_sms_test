package pl.qbasso.custom;

import pl.qbasso.fragments.SmsConversation;
import pl.qbasso.interfaces.ItemSeenListener;
import pl.qbasso.models.ConversationModel;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

public class SmsThreadPageAdapter extends FragmentPagerAdapter {

	public static final String EXTRA_FRAGMENT_POSITION = "position";
	public static final String EXTRA_CONVERSATION_INFO = "thread_info";

	@Override
	public void finishUpdate(ViewGroup container) {
		super.finishUpdate(container);
	}

	private SparseArray<Fragment> instantiatedFragments = new SparseArray<Fragment>();
	private ConversationModel[] items;
	private ItemSeenListener listener;
	private Context context;
	private boolean send;

	public SmsThreadPageAdapter(FragmentActivity act,
			ConversationModel[] converstaionModels,
			ItemSeenListener itemSeenListener, boolean b) {
		super(act.getSupportFragmentManager());
		this.items = converstaionModels;
		this.listener = itemSeenListener;
		this.context = act;
		send = b;
	}

	@Override
	public Fragment getItem(int arg0) {
		if (instantiatedFragments.get(arg0) != null) {
			return instantiatedFragments.get(arg0);
		} else {
			Bundle b = new Bundle();
			b.putSerializable(EXTRA_CONVERSATION_INFO, items[arg0]);
			b.putInt(EXTRA_FRAGMENT_POSITION, arg0);
			b.putBoolean("send_now", send);
			Fragment f = Fragment.instantiate(context, SmsConversation.class.getName(), b);
			f.setArguments(b);			
			instantiatedFragments.put(arg0, f);
			return f;
		}
	}

	@Override
	public int getCount() {
		return items.length;
	}

	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		super.setPrimaryItem(container, position, object);
	}

}
