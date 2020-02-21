package org.minimalj.frontend.impl.web;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.minimalj.application.Application;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.json.JsonSessionManager;
import org.minimalj.frontend.page.Page;

public class ApplicationHttpHandler implements MjHttpHandler {

	private String path;

	private static final List<String> RESOURCES = Arrays.asList(new String[] { "/dialog-polyfill.css", "/dialog-polyfill.js", "/miniterial.css", "/mj.css" });
	
	private static final ResourcesHttpHandler resourcesHttpHandler = new ResourcesHttpHandler() {
		protected InputStream getInputStream(String path) {
			return getClass().getResourceAsStream(path);
		};
	};
	
	public ApplicationHttpHandler(String path) {
		this.path = Objects.requireNonNull(path);
		if (!path.startsWith("/")) {
			throw new IllegalArgumentException("application path must start with '/'");
		}
		if (!path.endsWith("/")) {
			throw new IllegalArgumentException("application path m end with '/'");
		}
	}

	public void handle(MjHttpExchange exchange) {
		String exchangePath = exchange.getPath();
		if (exchangePath.startsWith(this.path)) {
			handle(exchange, exchangePath.substring(this.path.length() - 1));
		}
	}
	
	private void handle(MjHttpExchange exchange, String path) {
		if (!path.startsWith("/")) {
			throw new IllegalArgumentException(path);
		}
		if (path.equals("/ajax_request.xml")) {
			String response = JsonSessionManager.getInstance().handle(exchange.getRequest());
			exchange.sendResponse(200, response, "application/json");
		} else if (path.equals("/application.png")) {
			exchange.sendResponse(200, ResourcesHttpHandler.read(Application.getInstance().getIcon()), "image/png");
		} else if (RESOURCES.contains(path)) {
			resourcesHttpHandler.handle(exchange, path);
		} else if (Page.validateRoute(path)) {
			handleTemplate(exchange);
		}
	}

	public static void handleTemplate(MjHttpExchange exchange) {
		String htmlTemplate = JsonFrontend.getHtmlTemplate();
		htmlTemplate = htmlTemplate.replace("$SEND", WebServer.useWebSocket ? "sendWebSocket" : "sendAjax");
		String html = JsonFrontend.fillPlaceHolder(htmlTemplate, exchange.getPath());
		exchange.sendResponse(200, html, "text/html");
	}
	
}
