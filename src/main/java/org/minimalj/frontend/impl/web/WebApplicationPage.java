package org.minimalj.frontend.impl.web;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.minimalj.frontend.page.HtmlPage;

/**
 * This page is only needed if you want to use pages from the WebApplication in
 * the MJ application.
 *
 */
public class WebApplicationPage extends HtmlPage {

	public WebApplicationPage(String route) {
		super(null, Objects.requireNonNull(route));
	}

	@Override
	protected String getHtml() {
		WebApplicationPageExchange exchange = new WebApplicationPageExchange(getRoute());
		WebApplication.getWebApplicationHandler().handle(exchange);
		return exchange.getResult();
	}

	public static class WebApplicationPageExchange extends MjHttpExchange {
		private final String path;
		private String result;

		public WebApplicationPageExchange(String path) {
			this.path = path;
		}

		public String getResult() {
			return result;
		}

		@Override
		public String getPath() {
			return path;
		}

		@Override
		public InputStream getRequest() {
			return null;
		}

		@Override
		public Map<String, List<String>> getParameters() {
			return Collections.emptyMap();
		}

		@Override
		public void sendResponse(int statusCode, byte[] bytes, String contentType) {
			this.result = new String(Objects.requireNonNull(bytes));
		}

		@Override
		public void sendResponse(int statusCode, String body, String contentType) {
			this.result = Objects.requireNonNull(body);
		}

		@Override
		public boolean isResponseSent() {
			return result != null;
		}
	}

}
