package org.minimalj.frontend.impl.web;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
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
	 * @return path where minimal session handling is located. Return
	 *         <code>null</code> if minimal session handling should be disabled and
	 *         only a custom web application should be served.
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
			String path = webApplication.getMjHandlerPath();
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

	protected ResourcesHttpHandler createResourcesHttpHandler() {
		File web = new File("./web");
		if (web.exists() && web.isDirectory()) {
			return new ResourcesHttpHandler() {
				@Override
				public URL getUrl(String path) throws MalformedURLException {
					return new File(web, path).toURI().toURL();
				}
			};
		} else {
			if (!Configuration.isDevModeActive()) {
				logger.warning("In production the web resources should be in separated directory");
			}
			return new ResourcesHttpHandler();
		}
	}

	// TODO clean up
	private static MjHttpHandler webApplicationHandler;
	private static ResourcesHttpHandler resourceHandler;
	private static List<MjHttpHandler> handlers;

	private static List<MjHttpHandler> getHandlers() {
		if (handlers == null) {
			if (Application.getInstance() instanceof WebApplication) {
				WebApplication webApplication = (WebApplication) Application.getInstance();
				handlers = new ArrayList<>();

				if (WebApplication.mjHandlerPath() != null) {
					handlers.add(new ApplicationHttpHandler(WebApplication.mjHandlerPath()));
				}

				handlers.add(getWebApplicationHandler());

				MjHttpHandler resourcesHttpHandler = webApplication.createResourcesHttpHandler();
				if (resourcesHttpHandler != null) {
					handlers.add(resourcesHttpHandler);
				}
			} else {
				handlers = Collections.singletonList(new ApplicationHttpHandler("/"));
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

	public static MjHttpHandler getWebApplicationHandler() {
		if (webApplicationHandler == null && Application.getInstance() instanceof WebApplication) {
			WebApplication webApplication = (WebApplication) Application.getInstance();
			webApplicationHandler = webApplication.createHttpHandler();
		}
		return webApplicationHandler;
	}

	public static ResourcesHttpHandler getResourceHandler() {
		if (resourceHandler == null && Application.getInstance() instanceof WebApplication) {
			WebApplication webApplication = (WebApplication) Application.getInstance();
			resourceHandler = webApplication.createResourcesHttpHandler();
		}
		return resourceHandler;
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
