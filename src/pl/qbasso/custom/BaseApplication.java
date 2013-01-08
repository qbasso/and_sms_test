package pl.qbasso.custom;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import pl.qbasso.activities.AppConstants;
import pl.qbasso.interfaces.ISmsAccess;
import pl.qbasso.sms.Cache;
import pl.qbasso.sms.CustomSmsDbHelper;
import pl.qbasso.sms.SmsDbHelper;
import android.app.Application;
import android.content.SharedPreferences;

@ReportsCrashes(formKey = "dGl6c2Z1U25yTzZPUjJDUjcyZkhJdlE6MQ")
public class BaseApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		ACRA.init(this);
		Cache.getInstance();
		SharedPreferences pref = getSharedPreferences(AppConstants.SHARED_PREF_FILE, MODE_PRIVATE);
		AppConstants.DB = pref.getInt("db", 1);
		ISmsAccess h = new SmsDbHelper(getContentResolver());
		if (!((SmsDbHelper)h).isProviderAvailable()) {
			pref.edit().putInt("db", 0);
			AppConstants.DB = 0;
		}
		AppConstants.REPORTING = pref.getInt("reporting", 0);
		Cache.load();
	}

	@Override
	public void onTerminate() {
		Cache.save();
		super.onTerminate();
	}

}
