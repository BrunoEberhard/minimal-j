package org.minimalj.frontend.impl.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;

public interface MjHttpExchange {
	public String getPath();

	public Locale getLocale();

	public InputStream getRequest() throws IOException;

	public void sendResponse(byte[] bytes) throws IOException;

	public void sendResponse(String body) throws IOException;

	public void sendError();

	public void sendForbidden();

	public void sendNotfound();


	public static Locale getLocale(String userLocale) {
		if (userLocale == null) {
			return Locale.getDefault();
		}
		List<LanguageRange> ranges = Locale.LanguageRange.parse(userLocale);
		for (LanguageRange languageRange : ranges) {
			String localeString = languageRange.getRange();
			return Locale.forLanguageTag(localeString);
		}
		return Locale.getDefault();
	}

}
