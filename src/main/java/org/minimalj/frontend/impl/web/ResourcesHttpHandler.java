package org.minimalj.frontend.impl.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.WeakHashMap;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.util.resources.Resources;

public class ResourcesHttpHandler implements MjHttpHandler {

	private final Map<String, byte[]> resources = new WeakHashMap<>();

	@Override
	public void handle(MjHttpExchange exchange) {
		String path = exchange.getPath();
		handle(exchange, path);
	}

	public void handle(MjHttpExchange exchange, String path) {
		int pos = path.lastIndexOf('.');
		if (pos > 0 && pos < path.length() - 1) {
			String suffix = path.substring(pos + 1);
			if (path.contains("..")) {
				exchange.sendForbidden();
				return;
			}

			String mimeType = Resources.getMimeType(suffix);
			if (mimeType != null) {
				byte[] bytes = getResource(path);
				if (bytes != null) {
					exchange.sendResponse(200, bytes, mimeType);
				}
			}
		}
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

	protected InputStream getInputStream(String path) throws IOException {
		return Application.getInstance().getClass().getResourceAsStream("web/" + path);
	}

	private byte[] getResource(String path) {
		byte[] result = resources.get(path);
		if (result == null || Configuration.isDevModeActive()) {
			try (InputStream inputStream = getInputStream(path)) {
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
