package pl.qbasso.custom;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes(formKey = "dGl6c2Z1U25yTzZPUjJDUjcyZkhJdlE6MQ")
public class BaseApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		ACRA.init(this);
	}

}
