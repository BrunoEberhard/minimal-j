package org.minimalj.frontend.impl.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.minimalj.application.Application;

/**
 * You only need to extend from WebApplication if you want to server special
 * html pages. If you only want to use the regular html Minimal-J UI you can
 * extend from Application directly.
 *
 */
public abstract class WebApplication extends Application {

	/**
	 * @return path where minimal session handling is located. Must start with a '/'
	 *         and end with a '/'. Return <code>null</code> if minimal session
	 *         handling should be disabled.
	 */
	public String getMjHandlerPath() {
		return "/";
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
				handlers = Collections.singletonList(new ResourcesHttpHandler());
			}
		}
		return handlers;
	}

	public static final void handle(MjHttpExchange exchange) {
		for (MjHttpHandler handler : getHandlers()) {
			if (handler.handle(exchange)) {
				return;
			}
		}
		exchange.sendNotfound();
	}

}
