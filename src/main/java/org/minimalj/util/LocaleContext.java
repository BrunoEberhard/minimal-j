package org.minimalj.util;

import java.util.Locale;

public class LocaleContext {

	private static final InheritableThreadLocal<Locale> locale = new InheritableThreadLocal<>();
	
	public static Locale getCurrent() {
		Locale currentLocale = locale.get();
		if (currentLocale != null) {
			return currentLocale;
		} else {
			return Locale.getDefault();
		}
	}
	
	public static void setCurrent(Locale locale) {
		LocaleContext.locale.set(locale);
	}
}
