package org.minimalj.frontend.impl.web;

import java.util.Objects;

import org.minimalj.frontend.page.Page;

public class RoutingHttpHandler implements MjHttpHandler {

	private final String path;

	public RoutingHttpHandler(String path) {
		this.path = Objects.requireNonNull(path);
		if (!path.startsWith("/")) {
			throw new IllegalArgumentException("application path must start with '/'");
		}
		if (!path.endsWith("/")) {
			throw new IllegalArgumentException("application path m end with '/'");
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
		if (Page.validateRoute(path)) {
			ApplicationHttpHandler.handleTemplate(exchange, path);
		}
	}
}
