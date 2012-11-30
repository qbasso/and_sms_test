package pl.qbasso.custom;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import pl.qbasso.smssender.R;

import android.app.Application;

@ReportsCrashes(formKey = "dGl6c2Z1U25yTzZPUjJDUjcyZkhJdlE6MQ", mode = ReportingInteractionMode.TOAST, resNotifText = R.string.crash_occured)
public class BaseApplication extends Application {

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		ACRA.init(this);
	}

}
