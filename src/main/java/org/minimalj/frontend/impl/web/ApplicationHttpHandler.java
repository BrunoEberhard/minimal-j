package org.minimalj.frontend.impl.web;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import org.minimalj.application.Application;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.json.JsonSessionManager;
import org.minimalj.util.StringUtils;

public class ApplicationHttpHandler implements MjHttpHandler {

	private String path;

	private static final ResourcesHttpHandler resourcesHttpHandler = new ResourcesHttpHandler() {
		@Override
		public URL getUrl(String path) throws IOException {
			return ApplicationHttpHandler.class.getResource(path);
		}
	};

	public ApplicationHttpHandler(String path) {
		this.path = Objects.requireNonNull(path);
		if (!path.startsWith("/")) {
			throw new IllegalArgumentException("application path must start with '/'");
		}
		if (!path.endsWith("/")) {
			throw new IllegalArgumentException("application path must end with '/'");
		}
	}

	@Override
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
		path = path.substring(1);
		if (path.equals("ajax_request.xml")) {
			String response = JsonSessionManager.getInstance().handle(exchange.getRequest());
			exchange.sendResponse(200, response, "application/json");
		} else if (StringUtils.equals(path, "", "index.html")) {
			handleTemplate(exchange);
		} else if (path.equals("application.png")) {
			exchange.sendResponse(200, ResourcesHttpHandler.read(Application.getInstance().getIcon()), "image/png");
		} else {
			resourcesHttpHandler.handle(exchange, path);
		}
	}

	public static void handleTemplate(MjHttpExchange exchange) {
		String htmlTemplate = JsonFrontend.getHtmlTemplate();
		String html = JsonFrontend.fillPlaceHolder(htmlTemplate, exchange.getPath());
		exchange.sendResponse(200, html, "text/html");
	}

}
