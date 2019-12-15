package org.minimalj.frontend.impl.web;

import org.minimalj.application.Application;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.json.JsonSessionManager;
import org.minimalj.frontend.page.Page;

public class ApplicationHttpHandler implements MjHttpHandler {

	private ResourcesHttpHandler resourcesHttpHandler = new ResourcesHttpHandler();

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
			String response = JsonSessionManager.getInstance().handle(exchange.getRequest());
			exchange.sendResponse(200, response, "application/json");
			return true;
		} else if (path.equals("/application.png")) {
			exchange.sendResponse(200, ResourcesHttpHandler.read(Application.getInstance().getIcon()), "image/png");
			return true;
		} else if (path.length() <= 1 || Page.validateRoute(path.substring(1))) {
			String htmlTemplate = JsonFrontend.getHtmlTemplate();
			htmlTemplate = htmlTemplate.replace("$SEND", WebServer.useWebSocket ? "sendWebSocket" : "sendAjax");
			String html = JsonFrontend.fillPlaceHolder(htmlTemplate, path.length() < 2 ? "/" : path.substring(1));
			exchange.sendResponse(200, html, "text/html");
			return true;
		} else {
			return resourcesHttpHandler.handle(exchange, path);
		}
	}

}
