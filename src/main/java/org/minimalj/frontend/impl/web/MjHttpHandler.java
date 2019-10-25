package org.minimalj.frontend.impl.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

import org.minimalj.application.Application;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.json.JsonReader;
import org.minimalj.frontend.impl.json.JsonSessionManager;
import org.minimalj.frontend.page.Page;
import org.minimalj.util.resources.Resources;

public class MjHttpHandler {

	private JsonSessionManager sessionManager = new JsonSessionManager();
	private Map<String, byte[]> resources = new WeakHashMap<>();

	public void handle(MjHttpExchange exchange) {
		try {
			if (Application.getInstance() instanceof WebApplication) {
				boolean handled = ((WebApplication) Application.getInstance()).handle(exchange);
				if (handled) {
					return;
				}
			}
			handle_(exchange);
		} catch (Exception x) {
			x.printStackTrace();
			exchange.sendError();
		}
	}

	private void handle_(MjHttpExchange exchange) throws IOException {
		String path = exchange.getPath();
		if (path.equals("/ajax_request.xml")) {
			@SuppressWarnings("unchecked")
			Map<String, Object> data = (Map<String, Object>) JsonReader.read(exchange.getRequest());
			String response = sessionManager.handle(data);
			exchange.sendResponse(response);
		} else if (path.equals("/application.png")) {
			exchange.sendResponse(read(Application.getInstance().getIcon()));
		} else if (path.contains(".")) {
			if (path.contains("..")) {
				exchange.sendForbidden();
				return;
			}
			
			int index = path.lastIndexOf('.');
			if (index > -1 && index < path.length() - 1) {
				String postfix = path.substring(index + 1);
				String mimeType = Resources.getMimeType(postfix);
				if (mimeType != null) {
					byte[] bytes = getResource(path);
					exchange.sendResponse(bytes);
					return;
				}
			}

			exchange.sendNotfound();
			return;
		} else {
			if (path.length() > 1 && !Page.validateRoute(path.substring(1))) {
				exchange.sendForbidden();
				return;
			}
			String htmlTemplate = JsonFrontend.getHtmlTemplate();
			Locale locale = exchange.getLocale();
			htmlTemplate = htmlTemplate.replace("$SEND", WebServer.useWebSocket ? "sendWebSocket" : "sendAjax");
			String html = JsonFrontend.fillPlaceHolder(htmlTemplate, locale, path);
			exchange.sendResponse(html);
		}
	}

	public JsonSessionManager getSessionManager() {
		return sessionManager;
	}

	public static byte[] read(InputStream inputStream) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int b;
			while ((b = inputStream.read()) >= 0) {
				baos.write(b);
			}
			return baos.toByteArray();
		} catch (IOException x) {
			throw new RuntimeException(x);
		}
	}

	private byte[] getResource(String path) {
		if (!resources.containsKey(path)) {
			try (InputStream inputStream = MjHttpHandler.class.getResourceAsStream(path)) {
				resources.put(path, read(inputStream));
			} catch (IOException x) {
				throw new RuntimeException(x);
			}
		}
		return resources.get(path);
	}
}
