package org.minimalj.thymeleaf;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.minimalj.application.Application;
import org.minimalj.backend.Backend;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.web.Cookies;
import org.minimalj.frontend.impl.web.MjHttpExchange;
import org.minimalj.frontend.impl.web.WebApplicationPage.WebApplicationPageExchange;
import org.minimalj.security.Subject;
import org.minimalj.util.LocaleContext;
import org.minimalj.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

public class ThymeRequest {

	private final TemplateEngine templateEngine;
	private final MjHttpExchange exchange;
	private final String path;
	private Map<String, Object> context;
	private final Map<String, ? extends Collection<String>> parameters;
	private boolean responseSent = false;

	ThymeRequest(TemplateEngine templateEngine, MjHttpExchange exchange) {
		this.templateEngine = templateEngine;
		this.exchange = exchange;
		this.path = StringUtils.isEmpty(exchange.getPath()) ? "/" : exchange.getPath();
		this.parameters = exchange.getParameters();
	}

	private Map<String, Object> getContext() {
		if (context == null) {
			context = new HashMap<>();

			context.put("application", Application.getInstance());
			context.put("subject", Subject.getCurrent());
			context.put("metas", JsonFrontend.getMetas());

			context.put("MINIMALJ-VERSION", Application.class.getPackage().getImplementationVersion());
			context.put("APPLICATION-VERSION", Application.getInstance().getClass().getPackage().getImplementationVersion());

			context.put("thymePage", exchange instanceof WebApplicationPageExchange);

			context.put("parameters", parameters);

			context.put("backend", Backend.getInstance());
		}
		return context;
	}

	public String getPath() {
		return path;
	}

	public Map<String, ? extends Collection<String>> getParameters() {
		return parameters;
	}

	public String getParameter(String name) {
		Collection<String> values = getParameters().get(name);
		return values != null ? values.iterator().next() : null;
	}

	public Cookies getCookies() {
		return exchange.getCookies();
	}

	public void put(String key, Object value) {
		getContext().put(key, value);
	}

	public void sendResponse() {
		sendResponse(getPath());
	}

	public void sendResponse(String templateName) {
		sendResponse(200, templateName);
	}
	
	public void sendResponse(int statusCode, String templateName) {
		if (responseSent) {
			throw new IllegalStateException("Already send response. Cannont use " + templateName);
		}

		Context context = new Context(LocaleContext.getCurrent(), getContext());
		String response = templateEngine.process(templateName, context);
		exchange.sendResponse(statusCode, response, "text/html");
		responseSent = true;
	}

	public boolean isResponseSent() {
		return responseSent;
	}
}