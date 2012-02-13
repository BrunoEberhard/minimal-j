package ch.openech.mj.util;

public class IntegerUtils {

	public static int intValue(String s) {
		if (StringUtils.isBlank(s)) {
			return 0;
		} else {
			return Integer.valueOf(s);
		}
	}
}
