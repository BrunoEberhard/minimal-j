package org.minimalj.frontend.impl.web;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.web.MjHttpExchange.LoggingHttpExchange;
import org.minimalj.frontend.page.Routing;
import org.minimalj.util.StringUtils;

/**
 * You only need to extend from WebApplication if you want to serve custom html
 * pages. If you only want to use the regular html Minimal-J UI you can extend
 * from Application directly even if you use the web frontend.
 *
 */
public abstract class WebApplication extends Application {
	private static final Logger logger = Logger.getLogger(WebApplication.class.getName());

	/**
	 * @return path where minimal session handling is located. Return
	 *         <code>null</code> if minimal session handling should be disabled and
	 *         only a custom web application should be served.
	 */
	protected String getMjPath() {
		return "/";
	}

	private static WebApplication webApplication() {
		return (WebApplication) Application.getInstance();
	}

	/**
	 * Framework internal
	 * 
	 * @return location of Minimal-J application. Format like '/path/'. Slash at end
	 *         and beginning. Return <code>null</code> if there is no MJ application
	 *         (pure WebApplication).<br>
	 */
	public static String mjHandlerPath() {
		if (Application.getInstance() instanceof WebApplication) {
			WebApplication webApplication = (WebApplication) Application.getInstance();
			String path = webApplication.getMjPath();
			if (path != null && !path.endsWith("/")) {
				return path + "/";
			} else {
				return path;
			}
		} else {
			return "/";
		}
	}

	protected abstract MjHttpHandler createHttpHandler();

	private static List<MjHttpHandler> handlers;

	private static List<MjHttpHandler> getHandlers() {
		if (handlers == null) {
			boolean isJsonFrontend = Frontend.getInstance() instanceof JsonFrontend;
			String mjHandlerPath = WebApplication.mjHandlerPath();

			handlers = new ArrayList<>();

			// intial html and ajax calls
			if (isJsonFrontend && mjHandlerPath != null) {
				handlers.add(new ApplicationHttpHandler(mjHandlerPath));
			}

			// for applications with custom http handler
			if (Application.getInstance() instanceof WebApplication) {
				WebApplication webApplication = (WebApplication) Application.getInstance();
				handlers.add(webApplication.createHttpHandler());
			}

			// serve the application resources (located in web package)
			handlers.add(new ResourcesHttpHandler());

			// handle routing paths
			if (isJsonFrontend && mjHandlerPath != null && Routing.available()) {
				handlers.add(new RoutingHttpHandler(mjHandlerPath));
			}
		}
		return handlers;
	}

	public static final void handle(MjHttpExchange exchange) {
		if (!callHandlers(exchange)) {
			if (Application.getInstance() instanceof WebApplication) {
				webApplication().sendNotFound(exchange);
			} else {
				sendNotFoundDefault(exchange);
			}
		}
	}

	public static final boolean callHandlers(MjHttpExchange exchange) {
		if (MjHttpExchange.LOG_WEB.isLoggable(Level.FINER)) {
			MjHttpExchange.LOG_WEB.log(Level.FINER, exchange.getPath());
			long start = System.nanoTime();
			if (MjHttpExchange.LOG_WEB.isLoggable(Level.FINEST)) {
				exchange = new LoggingHttpExchange(exchange);
			}
			boolean result = doCallHandlers(exchange);
			MjHttpExchange.LOG_WEB.log(Level.FINER, StringUtils.padLeft("" + ((System.nanoTime() - start) / 1000 / 1000), 5, ' ') + "ms " + exchange.getPath());
			return result;
		} else {
			return doCallHandlers(exchange);
		}
	}

	private static final boolean doCallHandlers(MjHttpExchange exchange) {
		for (MjHttpHandler handler : getHandlers()) {
			try {
				handler.handle(exchange);
				if (exchange.isResponseSent()) {
					return true;
				}
			} catch (Exception x) {
				logger.log(Level.WARNING, x.getLocalizedMessage(), x);
			}
		}
		return false;
	}

	protected void sendNotFound(MjHttpExchange exchange) {
		sendNotFoundDefault(exchange);
	}

	private static void sendNotFoundDefault(MjHttpExchange exchange) {
		exchange.sendResponse(404, "Not found", "text/plain");
	}

}
