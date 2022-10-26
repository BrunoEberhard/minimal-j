package org.minimalj.frontend.impl.web;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import org.minimalj.application.Application;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.json.JsonSessionManager;

public class ApplicationHttpHandler implements MjHttpHandler {

	private String path;

	private static final ResourcesHttpHandler resourcesHttpHandler = new ResourcesHttpHandler() {
		@Override
		public URL getUrl(String path) throws IOException {
			return ApplicationHttpHandler.class.getResource(path.substring(1));
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
		switch (path) {
		case "/ajax_request.json":
			String response = JsonSessionManager.getInstance().handle(exchange.getRequest());
			exchange.sendResponse(200, response, "application/json;charset=UTF-8");
			break;
		case "/":
			handleTemplate(exchange, path);
			break;
		case "/application.png":
			exchange.sendResponse(200, ResourcesHttpHandler.read(Application.getInstance().getIcon()), "image/png");
			break;
		default:
			if (path.startsWith("/download")) {
				response = JsonSessionManager.getInstance().export(exchange.getParameter("session"), exchange.getParameter("component"));
				if (response != null) {
					exchange.sendResponse(200, response, "application/csv;charset=UTF-8");
				} else {
					exchange.sendNotfound();
				}
				break;
			}
			resourcesHttpHandler.handle(exchange, path);
		}
	}

	public static void handleTemplate(MjHttpExchange exchange, String path) {
		String htmlTemplate = JsonFrontend.getHtmlTemplate();
		String html = JsonFrontend.fillPlaceHolder(htmlTemplate, path);
		exchange.addHeader("Content-Security-Policy", "frame-ancestors 'none'");
		exchange.addHeader("X-Frame-Options", "DENY");
		exchange.addHeader("X-Content-Type-Options", "nosniff");
		exchange.addHeader("Strict-Transport-Security", "max-age=63072000");
		exchange.sendResponse(200, html, "text/html;charset=UTF-8");
	}

}
