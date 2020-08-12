package org.minimalj.example.demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.logging.Logger;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.web.WebApplication;
import org.minimalj.frontend.impl.web.WebServer;
import org.minimalj.util.LocaleContext;
import org.minimalj.util.LocaleContext.AcceptedLanguageLocaleSupplier;
import org.minimalj.util.StringUtils;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;

public class DemoWebServer {
	private static final Logger LOG = Logger.getLogger(DemoWebServer.class.getName());

	private static ExamplesApplication application;

	private static class WebServerHttpExchange extends WebServer.WebServerHttpExchange {
		private String path;

		private WebServerHttpExchange(HttpExchange exchange) {
			super(exchange);
		}

		@Override
		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}
	}

	private static void handle(HttpExchange exchange) {
		try {
			LocaleContext.setLocale(new AcceptedLanguageLocaleSupplier(exchange.getRequestHeaders().getFirst(AcceptedLanguageLocaleSupplier.ACCEPTED_LANGUAGE_HEADER)));
			WebServerHttpExchange mjHttpExchange = new WebServerHttpExchange(exchange);

			URI uri = exchange.getRequestURI();
			String path = uri.getPath();
			if (path.length() < 2) {
				mjHttpExchange.sendResponse(400, path + " invalid", "text/plain");
				return;
			}
			int applicationNameEnd = path.indexOf('/', 1);
			if (applicationNameEnd < 0) {
				mjHttpExchange.sendResponse(400, path + " invalid", "text/plain");
				return;
			}

			String applicationName = path.substring(1, applicationNameEnd);
			application.setCurrentApplication(applicationName);

			String uriWithoutApplicationName = path.substring(applicationNameEnd);
			mjHttpExchange.setPath(uriWithoutApplicationName);

			WebApplication.handle(mjHttpExchange);
		} finally {
			LocaleContext.setLocale(null);
		}
	}

	private static void start(boolean secure) {
		int port = getPort(secure);
		if (port > 0) {
			LOG.info("Start " + Application.getInstance().getClass().getSimpleName() + " web frontend on port " + port + (secure ? " (Secure)" : ""));
			try {
				InetSocketAddress addr = new InetSocketAddress(port);
				HttpServer server = secure ? HttpsServer.create(addr, 0) : HttpServer.create(addr, 0);
				HttpContext context = server.createContext("/");
				context.setHandler(DemoWebServer::handle);
				server.start();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static int getPort(boolean secure) {
		String portString = Configuration.get("MjFrontendPort" + (secure ? "Ssl" : ""), secure ? "-1" : "8080");
		return !StringUtils.isEmpty(portString) ? Integer.valueOf(portString) : -1;
	}

	public static void start() {
		Frontend.setInstance(new JsonFrontend());

		start(!WebServer.SECURE);
		start(WebServer.SECURE);
	}

	public static void start(ExamplesApplication application) {
		DemoWebServer.application = application;

		Application.setInstance(application);
		start();
	}

	public static void main(String... args) {
		Application.initApplication(args);
		start();
	}

}