package pl.qbasso.custom;

import pl.qbasso.fragments.SmsConversation;
import pl.qbasso.interfaces.ItemSeenListener;
import pl.qbasso.models.ConversationModel;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

public class SmsThreadPageAdapter extends FragmentPagerAdapter {

	@Override
	public void finishUpdate(ViewGroup container) {
		super.finishUpdate(container);
	}

	private SparseArray<Fragment> instantiatedFragments = new SparseArray<Fragment>();
	private ConversationModel[] items;
	private ItemSeenListener listener;

	public SmsThreadPageAdapter(FragmentActivity act,
			ConversationModel[] converstaionModels, ItemSeenListener itemSeenListener) {
		super(act.getSupportFragmentManager());
		this.items = converstaionModels;
		this.listener = itemSeenListener;
	}

	@Override
	public Fragment getItem(int arg0) {
		if (instantiatedFragments.get(arg0) != null) {
			return instantiatedFragments.get(arg0);
		} else {
			Bundle b = new Bundle();
			b.putSerializable("thread_info", items[arg0]);
			Fragment f = new SmsConversation(arg0, listener);
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
