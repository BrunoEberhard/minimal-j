package org.minimalj.util;

import java.util.Locale;

public class LocaleContext {

	private static final ThreadLocal<LocaleContext> contextByThread = new ThreadLocal<>();
	
	private Locale locale;
	
	public static Locale getLocale() {
		if (contextByThread.get() != null) {
			return contextByThread.get().locale;
		} else {
			return Locale.getDefault();
		}
	}
	
	public static void setLocale(Locale locale) {
		if (contextByThread.get() == null) {
			contextByThread.set(new LocaleContext());
		}
		contextByThread.get().locale = locale;
	}
}
