package org.minimalj.frontend.impl.web;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.json.JsonFrontend;

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
			handlers = new ArrayList<>();
			if (Application.getInstance() instanceof WebApplication) {
				WebApplication webApplication = (WebApplication) Application.getInstance();
				
				if (Frontend.getInstance() instanceof JsonFrontend && WebApplication.mjHandlerPath() != null) {
					handlers.add(new ApplicationHttpHandler(WebApplication.mjHandlerPath()));
				}

				handlers.add(webApplication.createHttpHandler());

			} else {
				handlers.add(new ApplicationHttpHandler("/"));
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
		for (MjHttpHandler handler : getHandlers()) {
			try {
				handler.handle(exchange);
				if (exchange.isResponseSent()) {
					return true;
				}
			} catch (Exception x) {
				logger.log(Level.SEVERE,x.getLocalizedMessage(), x);
				webApplication().sendError(exchange, x);
			}
		}
		return false;
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
		sendNotFoundDefault(exchange);
	}
	
	private static void sendNotFoundDefault(MjHttpExchange exchange) {
		exchange.sendResponse(404, "Not found", "text/plain");
	}

}
