package org.minimalj.frontend.impl.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.json.JsonSessionManager;

public class ApplicationHttpHandler implements MjHttpHandler {
	private static final Logger logger = Logger.getLogger(ApplicationHttpHandler.class.getName());

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
			// hier die applikatorischen Fehler abfangen, damit sie von Verbindungsproblemen unterschieden werden?
			try {
				String response = JsonSessionManager.getInstance().handle(exchange.getRequest());
				exchange.sendResponse(200, response, "application/json;charset=UTF-8");
			} catch (Exception x) {
				logger.log(Level.SEVERE, x.getLocalizedMessage(), x);
				sendError(exchange, x);
			}
			break;
		case "/":
			handleTemplate(exchange, path);
			break;
		case "/application.png":
			exchange.sendResponse(200, ResourcesHttpHandler.read(Application.getInstance().getIcon()), "image/png");
			break;
		default:
			if (path.startsWith("/download")) {
				String response = JsonSessionManager.getInstance().export(exchange.getParameter("session"), exchange.getParameter("component"));
				if (response != null) {
					// ms office can better handle iso-8859-1 than utf-8
					exchange.sendResponse(200, response.getBytes(Charset.forName("ISO-8859-1")), "application/csv;charset=ISO-8859-1");
				} else {
					exchange.sendNotfound();
				}
				break;
			}
			resourcesHttpHandler.handle(exchange, path);
		}
	}
	
	protected void sendError(MjHttpExchange exchange, Exception x) {
		if (Configuration.isDevModeActive()) {
			try (StringWriter sw = new StringWriter()) {
				try (PrintWriter pw = new PrintWriter(sw)) {
					exchange.sendResponse(500, sw.toString(), "text/plain");
					return;
				}
			} catch (Exception x2) {
				logger.log(Level.WARNING, "Could not send internal server error response", x2);
			}
		}
		try {
			exchange.sendResponse(500, "Internal server error", "text/plain");
		} catch (Exception x2) {
			logger.log(Level.WARNING, "Could not send internal server error response", x2);
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
