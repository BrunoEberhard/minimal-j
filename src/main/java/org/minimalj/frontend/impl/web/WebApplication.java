package org.minimalj.frontend.impl.web;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;

/**
 * You only need to extend from WebApplication if you want to serve special html
 * pages. If you only want to use the regular html Minimal-J UI you can extend
 * from Application directly.
 *
 */
public abstract class WebApplication extends Application {
	private static final Logger logger = Logger.getLogger(WebApplication.class.getName());

	/**
	 * @return path where minimal session handling is located. Must start with a '/'
	 *         and end with a '/'. Return <code>null</code> if minimal session
	 *         handling should be disabled.
	 */
	public String getMjHandlerPath() {
		return "/";
	}

	private static WebApplication webApplication() {
		return (WebApplication) Application.getInstance();
	}

	public static String mjHandlerPath() {
		if (Application.getInstance() instanceof WebApplication) {
			WebApplication webApplication = (WebApplication) Application.getInstance();
			return webApplication.getMjHandlerPath();
		} else {
			return "/";
		}
	}

	public abstract List<MjHttpHandler> createHttpHandlers();

	private static List<MjHttpHandler> handlers;

	private static List<MjHttpHandler> getHandlers() {
		if (handlers == null) {
			if (Application.getInstance() instanceof WebApplication) {
				WebApplication webApplication = (WebApplication) Application.getInstance();
				if (webApplication.getMjHandlerPath() != null) {
					handlers = new ArrayList<>();
					handlers.addAll(webApplication.createHttpHandlers());
					handlers.add(new ApplicationHttpHandler());
				} else {
					return webApplication.createHttpHandlers();
				}
			} else {
				handlers = Arrays.asList(new ApplicationHttpHandler(), new ResourcesHttpHandler());
			}
		}
		return handlers;
	}

	public static final void handle(MjHttpExchange exchange) {
		for (MjHttpHandler handler : getHandlers()) {
			try {
				if (handler.handle(exchange)) {
					return;
				}
			} catch (Exception x) {
				webApplication().sendError(exchange, x);
			}
		}
		webApplication().sendNotFound(exchange);
	}

	protected void sendError(MjHttpExchange exchange, Exception x) {
		if (Configuration.isDevModeActive()) {
			try (StringWriter sw = new StringWriter()) {
				try (PrintWriter pw = new PrintWriter(sw)) {
					x.printStackTrace(pw);
					exchange.sendResponse(500, sw.toString(), "text/plain");
				}
			} catch (Exception x2) {
				logger.log(Level.SEVERE, "Could not send internal server error response", x2);
			}
		}
		exchange.sendResponse(500, "Internal server error", "text/plain");
	}

	protected void sendNotFound(MjHttpExchange exchange) {
		exchange.sendResponse(404, "Not found", "text/plain");
	}

}
