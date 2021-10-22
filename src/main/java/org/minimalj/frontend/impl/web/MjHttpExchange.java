package org.minimalj.frontend.impl.web;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.Map;

public abstract class MjHttpExchange {
	public abstract String getPath();

	public String getMethod() {
		throw new RuntimeException(this.getClass().getSimpleName() + " does not support method");
	}
	
	public abstract InputStream getRequest();

	public abstract Map<String, ? extends Collection<String>> getParameters();
	
	public String getParameter(String name) {
		Collection<String> values = getParameters().get(name);
		return values != null ? values.iterator().next() : null;
	}
	
	public String getHeader(String name) {
		throw new RuntimeException(this.getClass().getSimpleName() + " does not support request headers");
	}

	public void addHeader(String string, String string2) {
		throw new RuntimeException(this.getClass().getSimpleName() + " does not support response headers");
	}
	
	public abstract void sendResponse(int statusCode, byte[] bytes, String contentType);

	public abstract void sendResponse(int statusCode, String body, String contentType);

	public abstract boolean isResponseSent();

	public void sendForbidden() {
		sendResponse(403, "Forbidden", "text/plain");
	}

	public void sendNotfound() {
		sendResponse(404, "Not found", "text/plain");
	}

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

	protected Map<String, Collection<String>> decodeParameters(String input) {
		if (input == null) {
			return Collections.emptyMap();
		}

		Map<String, Collection<String>> result = new HashMap<>();
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
