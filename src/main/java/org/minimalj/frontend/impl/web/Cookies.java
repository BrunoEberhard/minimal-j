package org.minimalj.frontend.impl.web;

import java.io.CharArrayWriter;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.minimalj.util.StringUtils;

/**
 * Based on https://github.com/js-cookie/java-cookie
 * 
 */
public final class Cookies {
	private static String UTF_8 = "UTF-8";
	private static final CookieAttributes defaults = CookieAttributes.empty();
	private final MjHttpExchange httpExchange;

	Cookies(MjHttpExchange httpExchange) {
		this.httpExchange = httpExchange;
	}

	public synchronized String get(String name) {
		if (StringUtils.isEmpty(name)) {
			throw new IllegalArgumentException();
		}

		String cookieHeader = httpExchange.getHeader("cookie");
		if (cookieHeader == null) {
			return null;
		}

		Map<String, String> cookies = getCookies(cookieHeader);
		for (String decodedName : cookies.keySet()) {
			if (!name.equals(decodedName)) {
				continue;
			}
			return cookies.get(decodedName);
		}

		return null;
	}

	public synchronized void set(String name, String value, CookieAttributes attributes) {
		if (StringUtils.isEmpty(name)) {
			throw new IllegalArgumentException();
		}
		if (value == null) {
			throw new IllegalArgumentException();
		}
		if (attributes == null) {
			throw new IllegalArgumentException();
		}

		String encodedName = encode(name);
		String encodedValue = encodeValue(value);

		StringBuilder header = new StringBuilder();
		header.append(encodedName);
		header.append('=');
		header.append(encodedValue);

		attributes = extend(CookieAttributes.empty().path("/"), defaults, attributes);

		String path = attributes.path;
		if (!StringUtils.isEmpty(path)) {
			header.append("; Path=" + path);
		}

		CookieExpiration expires = attributes.expires;
		if (expires != null) {
			header.append("; Expires=" + expires.toExpiresString());
		}

		String domain = attributes.domain;
		if (domain != null) {
			header.append("; Domain=" + domain);
		}

		Boolean secure = attributes.secure;
		if (Boolean.TRUE.equals(secure)) {
			header.append("; Secure");
		}

		Boolean httpOnly = attributes.httpOnly;
		if (Boolean.TRUE.equals(httpOnly)) {
			header.append("; HttpOnly");
		}

		String sameSite = attributes.sameSite;
		if (sameSite != null) {
			header.append("; SameSite=" + sameSite);
		}

		setCookie(header.toString());
	}

	public void set(String name, String value) {
		if (StringUtils.isEmpty(name)) {
			throw new IllegalArgumentException();
		}
		if (value == null) {
			throw new IllegalArgumentException();
		}
		set(name, value, defaults);
	}

	public void remove(String name, CookieAttributes attributes) {
		if (StringUtils.isEmpty(name)) {
			throw new IllegalArgumentException();
		}
		if (attributes == null) {
			throw new IllegalArgumentException();
		}

		set(name, "", extend(attributes, CookieAttributes.empty().expires(CookieExpiration.days(-1))));
	}

	public void remove(String name) {
		if (StringUtils.isEmpty(name)) {
			throw new IllegalArgumentException();
		}
		remove(name, CookieAttributes.empty());
	}

	public CookieAttributes defaults() {
		return this.defaults;
	}

	private CookieAttributes extend(CookieAttributes... mergeables) {
		CookieAttributes result = CookieAttributes.empty();
		for (CookieAttributes mergeable : mergeables) {
			result.merge(mergeable);
		}
		return result;
	}

	private void setCookie(String cookieValue) {
		httpExchange.addHeader("Set-Cookie", cookieValue);
	}

	private String encode(String decoded) {
		return encode(decoded, new HashSet<Integer>());
	}

	private String encode(String decoded, Set<Integer> exceptions) {
		String encoded = decoded;
		for (int i = 0; i < decoded.length();) {
			int codePoint = decoded.codePointAt(i);
			i += Character.charCount(codePoint);

			boolean isDigit = codePoint >= codePoint("0") && codePoint <= codePoint("9");
			if (isDigit) {
				continue;
			}

			boolean isAsciiUppercaseLetter = codePoint >= codePoint("A") && codePoint <= codePoint("Z");
			if (isAsciiUppercaseLetter) {
				continue;
			}

			boolean isAsciiLowercaseLetter = codePoint >= codePoint("a") && codePoint <= codePoint("z");
			if (isAsciiLowercaseLetter) {
				continue;
			}

			boolean isAllowed = codePoint == codePoint("!") || codePoint == codePoint("#") || codePoint == codePoint("$") || codePoint == codePoint("&") || codePoint == codePoint("'")
					|| codePoint == codePoint("*") || codePoint == codePoint("+") || codePoint == codePoint("-") || codePoint == codePoint(".") || codePoint == codePoint("^")
					|| codePoint == codePoint("_") || codePoint == codePoint("`") || codePoint == codePoint("|") || codePoint == codePoint("~");
			if (isAllowed) {
				continue;
			}

			if (exceptions.contains(codePoint)) {
				continue;
			}

			try {
				String character = new String(Character.toChars(codePoint));
				CharArrayWriter hexSequence = new CharArrayWriter();
				byte[] bytes = character.getBytes(UTF_8);
				for (int bytesIndex = 0; bytesIndex < bytes.length; bytesIndex++) {
					char left = Character.forDigit(bytes[bytesIndex] >> 4 & 0xF, 16);
					char right = Character.forDigit(bytes[bytesIndex] & 0xF, 16);
					hexSequence.append('%').append(left).append(right);
				}
				String target = character.toString();
				String sequence = hexSequence.toString().toUpperCase();
				encoded = encoded.replace(target, sequence);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return encoded;
	}

	private String decode(String encoded) {
		String decoded = encoded;
		Pattern pattern = Pattern.compile("(%[0-9A-Z]{2})+");
		Matcher matcher = pattern.matcher(encoded);
		while (matcher.find()) {
			String encodedChar = matcher.group();
			String[] encodedBytes = encodedChar.split("%");
			byte[] bytes = new byte[encodedBytes.length - 1];
			for (int i = 1; i < encodedBytes.length; i++) {
				String encodedByte = encodedBytes[i];
				bytes[i - 1] = (byte) Integer.parseInt(encodedByte, 16);
			}
			try {
				String decodedChar = new String(bytes, UTF_8);
				decoded = decoded.replace(encodedChar, decodedChar);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return decoded;
	}

	private String encodeValue(String decodedValue) {
		Set<Integer> exceptions = new HashSet<Integer>();
		for (int i = 0; i < decodedValue.length();) {
			int codePoint = decodedValue.codePointAt(i);
			i += Character.charCount(codePoint);

			boolean isIgnorable = false;
			if (codePoint == codePoint("/") || codePoint == codePoint(":")) {
				isIgnorable = true;
			}

			if (codePoint >= codePoint("<") && codePoint <= codePoint("@")) {
				isIgnorable = true;
			}

			if (codePoint == codePoint("[") || codePoint == codePoint("]")) {
				isIgnorable = true;
			}

			if (codePoint == codePoint("{") || codePoint == codePoint("}")) {
				isIgnorable = true;
			}

			if (isIgnorable) {
				exceptions.add(codePoint);
			}
		}

		return encode(decodedValue, exceptions);
	}

	private int codePoint(String character) {
		return character.codePointAt(0);
	}

	private String decodeValue(String encodedValue, String decodedName) {
		String decodedValue = null;

		if (decodedValue == null) {
			decodedValue = decode(encodedValue);
		}

		return decodedValue;
	}

	private Map<String, String> getCookies(String cookieHeader) {
		Map<String, String> result = new HashMap<String, String>();
		String[] cookies = cookieHeader.split("; ");
		for (int i = 0; i < cookies.length; i++) {
			String cookie = cookies[i];
			String encodedName = cookie.split("=")[0];
			String decodedName = decode(encodedName);

			String encodedValue = cookie.substring(cookie.indexOf('=') + 1, cookie.length());
			String decodedValue = decodeValue(encodedValue, decodedName);
			result.put(decodedName, decodedValue);
		}
		return result;
	}

	public static class CookieAttributes {
		public CookieExpiration expires;
		public String path;
		public String domain;
		public Boolean secure;
		public Boolean httpOnly;
		public String sameSite = "strict";

		private CookieAttributes() {
		}

		public CookieAttributes expires(CookieExpiration expiration) {
			expires = expiration;
			return this;
		}

		public CookieAttributes path(String path) {
			this.path = path;
			return this;
		}

		public static CookieAttributes empty() {
			return new CookieAttributes();
		}

		private CookieAttributes merge(CookieAttributes reference) {
			if (reference.path != null) {
				path = reference.path;
			}
			if (reference.domain != null) {
				domain = reference.domain;
			}
			if (reference.expires != null) {
				expires = reference.expires;
			}
			if (reference.secure != null) {
				secure = reference.secure;
			}
			if (reference.httpOnly != null) {
				httpOnly = reference.httpOnly;
			}
			if (reference.sameSite != null) {
				sameSite = reference.sameSite;
			}
			return this;
		}
	}

	public static final class CookieExpiration {
		private DateTimeFormatter EXPIRES_FORMAT = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'").withLocale(Locale.US);
		private final LocalDateTime date;

		private CookieExpiration(LocalDateTime dateTime) {
			this.date = dateTime;
		}

		public static CookieExpiration days(int days) {
			LocalDateTime withDays = LocalDateTime.now().plusDays(days);
			return new CookieExpiration(withDays);
		}

		public static CookieExpiration date(LocalDateTime dateTime) {
			if (dateTime == null) {
				throw new IllegalArgumentException();
			}
			return new CookieExpiration(dateTime);
		}

		public static CookieExpiration date(LocalDate date) {
			if (date == null) {
				throw new IllegalArgumentException();
			}
			LocalDateTime dateTime = date.atStartOfDay();
			return new CookieExpiration(dateTime);
		}

		String toExpiresString() {
			return date.format(EXPIRES_FORMAT);
		}
	}
}
