package org.minimalj.frontend.impl.web;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.frontend.page.HtmlPage;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.Routing;

/**
 * You only need to extend from WebApplication if you want to serve custom html
 * pages. If you only want to use the regular html Minimal-J UI you can extend
 * from Application directly even if you use the web frontend.
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

	static String mjHandlerPath() {
		if (Application.getInstance() instanceof WebApplication) {
			WebApplication webApplication = (WebApplication) Application.getInstance();
			return webApplication.getMjHandlerPath();
		} else {
			return "/";
		}
	}

	protected abstract MjHttpHandler createHttpHandler();

	private static List<MjHttpHandler> handlers;

	private static List<MjHttpHandler> getHandlers() {
		if (handlers == null) {
			if (Application.getInstance() instanceof WebApplication) {
				WebApplication webApplication = (WebApplication) Application.getInstance();
				handlers = new ArrayList<>();
				if (webApplication.getMjHandlerPath() != null) {
					handlers.add(new ApplicationHttpHandler());
				}
				handlers.add(webApplication.createHttpHandler());
			} else {
				handlers = Collections.singletonList(new ApplicationHttpHandler());
			}
		}
		return handlers;
	}

	public static final void handle(MjHttpExchange exchange) {
		for (MjHttpHandler handler : getHandlers()) {
			try {
				handler.handle(exchange);
				if (exchange.isResponseSent()) {
					return;
				}
			} catch (Exception x) {
				webApplication().sendError(exchange, x);
				logger.log(Level.SEVERE,x.getLocalizedMessage(), x);
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

	@Override
	public Routing createRouting() {
		return new WebRouting();
	}

	public static class WebRouting extends Routing {

		@Override
		protected String getRoute(Page page) {
			if (page instanceof HtmlPage) {
				return ((HtmlPage) page).getRoute();
			}
			return null;
		}

		@Override
		protected Page createPage(String route) {
			RoutingHttpExchange exchange = new RoutingHttpExchange(route);
			handle(exchange);
			if (exchange.isResponseSent()) {
				return new HtmlPage(exchange.getBody(), route);
			} else {
				return null;
			}
		}

	}

	private static class RoutingHttpExchange extends MjHttpExchange {
		private final String url;
		private String body, contentType;

		public RoutingHttpExchange(String url) {
			this.url = url;
		}

		public String getBody() {
			return body;
		}

		public String getUrl() {
			return url;
		}

		@Override
		public boolean isResponseSent() {
			return body != null;
		}

		@Override
		public void sendResponse(int statusCode, String body, String contentType) {
			this.body = body;
			this.contentType = contentType;
		}

		@Override
		public void sendResponse(int statusCode, byte[] bytes, String contentType) {
			sendResponse(statusCode, new String(bytes), contentType);
		}

		@Override
		public String getPath() {
			return url;
		}

		@Override
		public InputStream getRequest() {
			return new ByteArrayInputStream(new byte[0]);
		}

		@Override
		public Map<String, List<String>> getParameters() {
			return Collections.emptyMap();
		}
	}

}
