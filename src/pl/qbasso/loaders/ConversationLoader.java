package pl.qbasso.loaders;

import java.util.List;

import pl.qbasso.interfaces.ContentNeedRefreshListener;
import pl.qbasso.models.ConversationModel;
import pl.qbasso.sms.Cache;
import pl.qbasso.sms.DefaultSmsProviderHelper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.AsyncTaskLoader;


/**
 * @author jakub.porzuczek
 * not used right now because it is too complicated to queue loader refreshing while containing activity is not on screen
 */
public class ConversationLoader extends
		AsyncTaskLoader<List<ConversationModel>> implements
		ContentNeedRefreshListener {

	private DefaultSmsProviderHelper mHelper;
	private List<ConversationModel> mItems = null;
	private ConversationChangedReceiver mChangeWatcher = null;
	private boolean mRefreshMode = false;
	public static final String ACTION_CONVERSATIONS_CHANGED = "pl.qbasso.loaders.conversationloader.ACTION_CONVERSATIONS_CHANGED";

	private class ConversationChangedReceiver extends BroadcastReceiver {

		private ConversationLoader mLoader;

		public ConversationChangedReceiver(ConversationLoader loader) {
			mLoader = loader;
			loader.getContext().registerReceiver(this,
					new IntentFilter(ACTION_CONVERSATIONS_CHANGED));
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			mLoader.onContentModified(intent.getBooleanExtra("need_refresh",
					false));
		}

	}

	public ConversationLoader(Context context) {
		super(context);
		mHelper = new DefaultSmsProviderHelper(context.getContentResolver());
		Cache.getInstance();
	}

	@Override
	public List<ConversationModel> loadInBackground() {
		if (mRefreshMode) {
			Cache.putAllAtBeginnig(mHelper.getThreads(Cache.getRefreshList()));
			Cache.clearRefreshSet();
			mItems = Cache.getAll();
		} else {
			Cache.getInstance();
			Cache.putAll(mHelper.getThreads(null));
			mItems = Cache.getAll();
		}
		return mItems;
	}

	@Override
	protected void onStopLoading() {
		super.onStopLoading();
	}

	@Override
	protected void onStartLoading() {
		if (mChangeWatcher == null) {
			mChangeWatcher = new ConversationChangedReceiver(this);
		}
		super.onStartLoading();
	}

	@Override
	protected void onReset() {
		if (mChangeWatcher != null) {
			getContext().unregisterReceiver(mChangeWatcher);
		}
		super.onReset();
	}

	public void onContentModified(boolean needRefresh) {
		mRefreshMode = needRefresh;
		forceLoad();
	}

}
