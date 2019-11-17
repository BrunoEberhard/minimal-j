package org.minimalj.frontend.impl.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

import org.minimalj.application.Configuration;
import org.minimalj.util.resources.Resources;

public class ResourcesHttpHandler implements MjHttpHandler {

	private Map<String, byte[]> resources = new WeakHashMap<>();

	private final Collection<String> allowedSuffixes;

	public ResourcesHttpHandler() {
		this.allowedSuffixes = allowedSuffixes();
	}

	protected Collection<String> allowedSuffixes() {
		return Arrays.asList("html", "css", "js", "png", "jpg", "svg");
	}

	public boolean handle(MjHttpExchange exchange) {
		String path = exchange.getPath();
		int pos = path.lastIndexOf('.');
		if (pos > 0 && pos < path.length() - 1) {
			String suffix = path.substring(pos + 1);
			if (allowedSuffixes.contains(suffix)) {
				if (path.contains("..")) {
					exchange.sendForbidden();
					return true;
				}

				String mimeType = Resources.getMimeType(suffix);
				if (mimeType != null) {
					byte[] bytes = getResource(path);
					if (bytes != null) {
						exchange.sendResponse(200, bytes, mimeType);
						return true;
					}
				}
			}
		}
		return false;
	}

	public static byte[] read(InputStream inputStream) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			int b;
			while ((b = inputStream.read()) >= 0) {
				baos.write(b);
			}
			return baos.toByteArray();
		} catch (IOException x) {
			throw new RuntimeException(x);
		}
	}

	byte[] getResource(String path) {
		byte[] result = resources.get(path);
		if (result == null || Configuration.isDevModeActive()) {
			try (InputStream inputStream = getClass().getResourceAsStream(path)) {
				if (inputStream != null) {
					result = read(inputStream);
				}
				resources.put(path, result);
			} catch (IOException x) {
				throw new RuntimeException(x);
			}
		}
		return result;
	}
}
