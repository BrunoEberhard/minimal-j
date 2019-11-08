package org.minimalj.frontend.impl.web;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.Map;

public interface MjHttpExchange {
	public String getPath();

	public Locale getLocale();

	public InputStream getRequest();

	public Map<String, List<String>> getParameters();

	public void sendResponse(byte[] bytes, String contentType);

	public void sendResponse(String body, String contentType);

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

	public static Map<String, List<String>> decodeParameters(String input) {
		if (input == null) {
			return Collections.emptyMap();
		}

		Map<String, List<String>> result = new HashMap<>();
		for (String e : input.split("&")) {
			int index = e.indexOf('=');
			String parameterName = null;
			String value = null;

			if (index >= 0) {
				parameterName = decode(e.substring(0, index));
				value = decode(e.substring(index + 1));
			} else {
				parameterName = decode(e);
				value = "";
			}

			if (!result.containsKey(parameterName)) {
				result.put(parameterName, new ArrayList<>());
			}

			result.get(parameterName).add(value);
		}

		return result;
	}

	public static String decode(String str) {
		try {
			return URLDecoder.decode(str, "UTF8");
		} catch (UnsupportedEncodingException ignored) {
			throw new RuntimeException("Unsupported encoding");
		}
	}

}
