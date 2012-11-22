/*
 * @author JPorzuczek
 */
package pl.qbasso.custom;

import android.text.format.DateFormat;
import android.text.format.DateUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class Utils.
 */
public class Utils {

	/**
	 * Format date.
	 *
	 * @param date the date
	 * @return the string
	 */
	public static String formatDate(long date) {
		if (DateUtils.isToday(date)) {
			return DateFormat.format("kk:mm", date).toString();
		} else {
			return DateFormat.format("MM/dd/yy kk:mm", date).toString();
		}
	}
	
	/**
	 * Checks if is ascii.
	 *
	 * @param string the string
	 * @return true, if is ascii
	 */
	public static boolean isAscii(String string) {
		for (char c : string.toCharArray()) {
			if (((int) c) > 128) {
				return false;
			}
		}
		return true;
	}
}
