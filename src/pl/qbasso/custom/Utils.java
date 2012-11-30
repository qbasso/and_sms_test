/*
 * @author JPorzuczek
 */
package pl.qbasso.custom;

import java.util.regex.Matcher;

import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.util.TypedValue;

// TODO: Auto-generated Javadoc
/**
 * The Class Utils.
 */
public class Utils {

	/**
	 * Format date.
	 * 
	 * @param date
	 *            the date
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
	 * @param string
	 *            the string
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

	public static String getPhoneNumber(String text) {
		Matcher m = Patterns.PHONE.matcher(text);
		if (m.find()) {
			return m.group();
		}
		return "";
	}

	public static int dpiToPx(DisplayMetrics metrics, int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				metrics);
	}
}
