package org.minimalj.frontend.impl.web;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.logging.Logger;

public abstract class MjHttpExchange {
	public static final Logger LOG_WEB = Logger.getLogger("WEB");

	public abstract String getPath();

	private Cookies cookies;

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

	public Cookies getCookies() {
		if (cookies == null) {
			cookies = new Cookies(this);
		}
		return cookies;
	}

	public static class LoggingHttpExchange extends MjHttpExchange {

		private final MjHttpExchange delegate;
		private final byte[] input;

		public LoggingHttpExchange(MjHttpExchange delegate) {
			this.delegate = delegate;
			try {
				this.input = WebServer.readAllBytes(delegate.getRequest());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			String contentType = null;
			try {
				contentType = getHeader("Content-Type");
			} catch (Exception x) {
				// ignore;
			}
			if (contentType != null && contentType.startsWith("text")) {
				String requestString = new String(input, StandardCharsets.UTF_8);
				LOG_WEB.finer("Request:\n" + requestString);
			} else {
				LOG_WEB.finer("Request: " + input.length + " bytes of " + getHeader("Content-Type"));
			}
		}

		public String getPath() {
			return delegate.getPath();
		}

		public String getMethod() {
			return delegate.getMethod();
		}

		public InputStream getRequest() {
			return new ByteArrayInputStream(input);
		}

		public Map<String, ? extends Collection<String>> getParameters() {
			return delegate.getParameters();
		}

		public String getParameter(String name) {
			return delegate.getParameter(name);
		}

		public String getHeader(String name) {
			return delegate.getHeader(name);
		}

		public void addHeader(String string, String string2) {
			delegate.addHeader(string, string2);
		}

		public void sendResponse(int statusCode, byte[] bytes, String contentType) {
			LOG_WEB.finer("Response: " + bytes.length + " bytes of " + contentType);
			delegate.sendResponse(statusCode, bytes, contentType);
		}

		public void sendResponse(int statusCode, String body, String contentType) {
			LOG_WEB.finer("Response:\n" + body);
			delegate.sendResponse(statusCode, body, contentType);
		}

		public boolean isResponseSent() {
			return delegate.isResponseSent();
		}

		public void sendForbidden() {
			delegate.sendForbidden();
		}

		public void sendNotfound() {
			delegate.sendNotfound();
		}

	}
}
