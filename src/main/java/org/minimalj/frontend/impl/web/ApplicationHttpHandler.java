package org.minimalj.frontend.impl.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.minimalj.application.Application;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.json.JsonReader;
import org.minimalj.frontend.impl.json.JsonSessionManager;
import org.minimalj.frontend.page.Page;
import org.minimalj.util.resources.Resources;

public class ApplicationHttpHandler implements MjHttpHandler {

	private JsonSessionManager sessionManager = new JsonSessionManager();

	private ResourcesHttpHandler resourceHandler = new ResourcesHttpHandler();
	private static final Collection<String> RESOURCES = Arrays.asList("dialog-polyfill.css", "dialog-polyfill.js", "index.html", "miniterial.css", "mj.css");

	private final String path;

	public ApplicationHttpHandler() {
		this.path = WebApplication.mjHandlerPath();
	}

	public boolean handle(MjHttpExchange exchange) {
		int index = exchange.getPath().lastIndexOf('/');
		if (index < 0) {
			throw new RuntimeException("No '/' in path: " + exchange.getPath());
		}
		String path = exchange.getPath().substring(0, index + 1);
		if (path.startsWith(this.path)) {
			return handle(exchange, exchange.getPath().substring(this.path.length() - 1));
		} else {
			return false;
		}
	}

	private boolean handle(MjHttpExchange exchange, String path) {
		if (path.equals("/ajax_request.xml")) {
			@SuppressWarnings("unchecked")
			Map<String, Object> data = (Map<String, Object>) JsonReader.read(exchange.getRequest());
			String response = sessionManager.handle(data);
			exchange.sendResponse(200, response, "application/json");
			return true;
		} else if (RESOURCES.contains(path.substring(1))) {
			String type = Resources.getMimeType(path.substring(path.lastIndexOf('.') + 1));
			exchange.sendResponse(200, resourceHandler.getResource(path), type);
			return true;
		} else if (path.equals("/application.png")) {
			exchange.sendResponse(200, read(Application.getInstance().getIcon()), "image/png");
			return true;
		} else {
			if (path.length() > 1 && !Page.validateRoute(path.substring(1))) {
				exchange.sendForbidden();
				return true;
			}
			String htmlTemplate = JsonFrontend.getHtmlTemplate();
			htmlTemplate = htmlTemplate.replace("$SEND", WebServer.useWebSocket ? "sendWebSocket" : "sendAjax");
			String html = JsonFrontend.fillPlaceHolder(htmlTemplate, path);
			exchange.sendResponse(200, html, null);
			return true;
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

}
