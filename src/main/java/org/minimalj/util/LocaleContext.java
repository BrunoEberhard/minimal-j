package org.minimalj.util;

import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.Objects;
import java.util.function.Supplier;

public class LocaleContext {

	private static final InheritableThreadLocal<Supplier<Locale>> locale = new InheritableThreadLocal<>();
	
	public static Locale getCurrent() {
		Supplier<Locale> currentSupplier = locale.get();
		if (currentSupplier != null && currentSupplier.get() != null) {
			return currentSupplier.get();
		}
		return Locale.getDefault();
	}
	
	public static void setLocale(Supplier<Locale> localeSupplier) {
		locale.set(Objects.requireNonNull(localeSupplier));
	}

	public static void resetLocale() {
		locale.set(null);
	}

	// internal, used for web frontends where http request header determines the
	// locale
	public static class AcceptedLanguageLocaleSupplier implements Supplier<Locale> {
		public static final String ACCEPTED_LANGUAGE_HEADER = "accept-language";
		private final Locale locale;

		public AcceptedLanguageLocaleSupplier(String acceptedLanguage) {
			locale = convert(acceptedLanguage);
		}

		@Override
		public Locale get() {
			return locale;
		}

		public static Locale convert(String acceptedLanguage) {
			if (acceptedLanguage == null) {
				return Locale.getDefault();
			}
			List<LanguageRange> ranges = Locale.LanguageRange.parse(acceptedLanguage);
			for (LanguageRange languageRange : ranges) {
				String localeString = languageRange.getRange();
				return Locale.forLanguageTag(localeString);
			}
			return Locale.getDefault();
		}
	}
}
