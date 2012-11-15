package pl.qbasso.custom;

import android.text.format.DateFormat;
import android.text.format.DateUtils;

public class Utils {

	public static String formatDate(long date) {
		if (DateUtils.isToday(date)) {
			return DateFormat.format("kk:mm", date).toString();
		} else {
			return DateFormat.format("MM/dd/yy kk:mm", date).toString();
		}
	}
	
	public static boolean isAscii(String string) {
		for (char c : string.toCharArray()) {
			if (((int) c) > 128) {
				return false;
			}
		}
		return true;
	}
}
