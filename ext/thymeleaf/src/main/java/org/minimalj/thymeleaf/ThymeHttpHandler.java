package org.minimalj.thymeleaf;

import java.util.HashMap;
import java.util.Map;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.backend.Backend;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.web.MjHttpExchange;
import org.minimalj.frontend.impl.web.MjHttpHandler;
import org.minimalj.security.Subject;
import org.minimalj.thymeleaf.page.ThymePage.ThymePageExchange;
import org.minimalj.util.LocaleContext;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolution;
import org.thymeleaf.templateresource.ClassLoaderTemplateResource;

public abstract class ThymeHttpHandler implements MjHttpHandler {
	private final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
	private final TemplateEngine templateEngine = new TemplateEngine();

	protected String getTemplatesPrefix() {
		return "templates/";
	}

	public ThymeHttpHandler() {
		templateResolver.setPrefix(getTemplatesPrefix());
		templateResolver.setCacheable(!Configuration.isDevModeActive());
		templateEngine.setTemplateResolver(templateResolver);
		templateEngine.setMessageResolver(new MjMessageResolver());
	}

	protected String getTemplateName(MjHttpExchange exchange) {
		String templateName = exchange.getPath();
		if (templateName.endsWith("/")) {
			return templateName + "index.html";
		} else {
			return templateName;
		}
	}

	protected boolean exists(String templateName) {
		TemplateResolution templateResolution = templateResolver.resolveTemplate(templateEngine.getConfiguration(), null, templateName, null);
		if (templateResolution != null) {
			ClassLoaderTemplateResource resource = (ClassLoaderTemplateResource) templateResolution.getTemplateResource();
			return resource != null && resource.exists();
		}
		return false;
	}

	protected Map<String, Object> createContext(MjHttpExchange exchange) {
		Map<String, Object> variables = new HashMap<>();

		variables.put("application", Application.getInstance());
		variables.put("subject", Subject.getCurrent());
		variables.put("metas", JsonFrontend.getMetas());

		variables.put("MINIMALJ-VERSION", Application.class.getPackage().getImplementationVersion());
		variables.put("APPLICATION-VERSION", Application.getInstance().getClass().getPackage().getImplementationVersion());

		variables.put("thymePage", exchange instanceof ThymePageExchange);

		variables.put("parameters", exchange.getParameters());

		variables.put("backend", Backend.getInstance());

		return variables;
	}

	@Override
	public final boolean handle(MjHttpExchange exchange) {
		String templateName = getTemplateName(exchange);
		if (templateName == null || !exists(templateName)) {
			return false;
		}

		Map<String, Object> variables = createContext(exchange);
		Context context = new Context(LocaleContext.getCurrent(), variables);
		String response = templateEngine.process(templateName, context);
		exchange.sendResponse(200, response, "text/html");

		return true;
	}

}
