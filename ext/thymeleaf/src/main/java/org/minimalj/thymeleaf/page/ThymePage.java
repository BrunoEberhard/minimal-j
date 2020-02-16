package org.minimalj.thymeleaf.page;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.minimalj.frontend.impl.web.MjHttpExchange;
import org.minimalj.frontend.impl.web.WebApplication;
import org.minimalj.frontend.page.HtmlPage;

/**
 * This page is only needed if you want to use Thymeleaf in Minimal-J pages. It
 * is not needed if you just want to render templates for a WebApplication.
 * 
 * @author bruno
 *
 */
public class ThymePage extends HtmlPage {

	public ThymePage(String route) {
		super(null, Objects.requireNonNull(route));
	}

	protected String getHtml() {
		ThymePageExchange exchange = new ThymePageExchange(getRoute());
		WebApplication.handle(exchange);
		return exchange.getResult();
	}

	public static class ThymePageExchange extends MjHttpExchange {
		private final String path;
		private String result;

		public ThymePageExchange(String path) {
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
