package org.minimalj.thymeleaf.page;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.impl.web.MjHttpExchange;
import org.minimalj.frontend.impl.web.WebApplication;
import org.minimalj.frontend.page.Page;
import org.minimalj.util.LocaleContext;

public class ThymePage extends Page {
	private final String path;
	private final String title;

	public ThymePage(String path, String title) {
		this.path = path;
		this.title = title;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public IContent getContent() {
		ThymePageExchange exchange = new ThymePageExchange(path);
		WebApplication.handle(exchange);

		return Frontend.getInstance().createHtmlContent(exchange.getResult());
	}

	public class ThymePageExchange extends MjHttpExchange {
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
		public Locale getLocale() {
			return LocaleContext.getCurrent();
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
			// not implemented
		}

		@Override
		public void sendResponse(int statusCode, String body, String contentType) {
			this.result = body;
		}
	}

}
