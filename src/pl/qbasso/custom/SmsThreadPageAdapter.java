/*
 * @author JPorzuczek
 */
package pl.qbasso.custom;

import pl.qbasso.fragments.SmsConversation;
import pl.qbasso.interfaces.ItemSeenListener;
import pl.qbasso.interfaces.SmsDraftAvailableListener;
import pl.qbasso.models.ConversationModel;
import pl.qbasso.smssender.R;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.util.SparseArray;
import android.view.ViewGroup;

// TODO: Auto-generated Javadoc
/**
 * The Class SmsThreadPageAdapter.
 */
public class SmsThreadPageAdapter extends FragmentPagerAdapter {

	/** The Constant EXTRA_FRAGMENT_POSITION. */
	public static final String EXTRA_FRAGMENT_POSITION = "position";
	
	/** The Constant EXTRA_CONVERSATION_INFO. */
	public static final String EXTRA_CONVERSATION_INFO = "thread_info";

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentPagerAdapter#finishUpdate(android.view.ViewGroup)
	 */
	@Override
	public void finishUpdate(ViewGroup container) {
		super.finishUpdate(container);
	}

	/** The instantiated fragments. */
	private SparseArray<Fragment> instantiatedFragments = new SparseArray<Fragment>();
	
	/** The items. */
	private ConversationModel[] items;
	
	/** The listener. */
	private ItemSeenListener listener;
	
	/** The draft available listener. */
	private SmsDraftAvailableListener draftAvailableListener;
	
	/** The context. */
	private Context context;
	
	/** The send. */
	private boolean send;
	
	/** The pager tab strip. */
	private PagerTabStrip pagerTabStrip;

	/**
	 * Instantiates a new sms thread page adapter.
	 *
	 * @param act the act
	 * @param converstaionModels the converstaion models
	 * @param itemSeenListener the item seen listener
	 * @param draftAvailableListener the draft available listener
	 * @param b the b
	 */
	public SmsThreadPageAdapter(FragmentActivity act,
			ConversationModel[] converstaionModels,
			ItemSeenListener itemSeenListener,
			SmsDraftAvailableListener draftAvailableListener, boolean b) {
		super(act.getSupportFragmentManager());
		this.items = converstaionModels;
		this.listener = itemSeenListener;
		this.draftAvailableListener = draftAvailableListener;
		this.context = act;
		this.pagerTabStrip = (PagerTabStrip) act.findViewById(R.id.content_pages_strip);
		this.pagerTabStrip.setTabIndicatorColorResource(R.color.blue);
		send = b;
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentPagerAdapter#getItem(int)
	 */
	@Override
	public Fragment getItem(int arg0) {
		if (instantiatedFragments.get(arg0) != null) {
			return instantiatedFragments.get(arg0);
		} else {
			Bundle b = new Bundle();
			b.putSerializable(EXTRA_CONVERSATION_INFO, items[arg0]);
			b.putInt(EXTRA_FRAGMENT_POSITION, arg0);
			b.putBoolean("send_now", send);
			Fragment f = Fragment.instantiate(context,
					SmsConversation.class.getName(), b);
			f.setArguments(b);
			((SmsConversation) f).setItemSeenListener(listener);
			((SmsConversation) f)
					.setDraftAvailableListener(draftAvailableListener);
			instantiatedFragments.put(arg0, f);
			return f;
		}
	}

	@Override
	public int getCount() {
		return items.length;
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentPagerAdapter#setPrimaryItem(android.view.ViewGroup, int, java.lang.Object)
	 */
	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		super.setPrimaryItem(container, position, object);
	}

	public SmsDraftAvailableListener getDraftAvailableListener() {
		return draftAvailableListener;
	}

	public void setDraftAvailableListener(
			SmsDraftAvailableListener draftAvailableListener) {
		this.draftAvailableListener = draftAvailableListener;
	}

	/* (non-Javadoc)
	 * @see android.support.v4.view.PagerAdapter#getPageTitle(int)
	 */
	@Override
	public CharSequence getPageTitle(int position) {
		return items[position].getDisplayName();
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		// TODO Auto-generated method stub
		return super.instantiateItem(container, position);
	}
}
