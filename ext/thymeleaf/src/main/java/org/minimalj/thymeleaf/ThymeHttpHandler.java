package org.minimalj.thymeleaf;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.web.MjHttpExchange;
import org.minimalj.frontend.impl.web.MjHttpHandler;
import org.minimalj.security.Subject;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

public abstract class ThymeHttpHandler implements MjHttpHandler {
	private static final Logger logger = Logger.getLogger(ThymeHttpHandler.class.getName());

	private final TemplateEngine templateEngine = new TemplateEngine();

	protected String getTemplatesPrefix() {
		return "templates/";
	}

	public ThymeHttpHandler() {
		ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
		templateResolver.setPrefix(getTemplatesPrefix());
		templateResolver.setCacheable(!Configuration.isDevModeActive());
		templateEngine.setTemplateResolver(templateResolver);
		templateEngine.setMessageResolver(new MjMessageResolver());
	}

	public abstract String handle(String path, Object input);

	public void fillContext(String path, Context context) {
		// can be empty
	}

	@Override
	public final boolean handle(MjHttpExchange exchange) {
		String path = exchange.getPath();
		Object input = exchange.getParameters();

		String template = handle(path, input);
		if (template == null) {
			return false;
		}

		Map<String, Object> variables = new HashMap<>();
		variables.put("input", input);
		variables.put("application", Application.getInstance());
		variables.put("subject", Subject.getCurrent());
		variables.put("metas", JsonFrontend.getMetas());

		variables.put("MINIMALJ-VERSION", Application.class.getPackage().getImplementationVersion());
		variables.put("APPLICATION-VERSION", Application.getInstance().getClass().getPackage().getImplementationVersion());

		Context context = new Context(exchange.getLocale(), variables);
		fillContext(path, context);

		try {
			exchange.sendResponse(templateEngine.process(template, context), "text/html");
		} catch (Exception x) {
			x.printStackTrace();
		}
		return true;
	}


}
