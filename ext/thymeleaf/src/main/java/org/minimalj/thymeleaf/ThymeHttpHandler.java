package org.minimalj.thymeleaf;

import java.util.HashMap;
import java.util.Map;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.backend.Backend;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.web.MjHttpExchange;
import org.minimalj.frontend.impl.web.MjHttpHandler;
import org.minimalj.frontend.impl.web.ResourcesHttpHandler;
import org.minimalj.frontend.impl.web.WebApplication;
import org.minimalj.security.Subject;
import org.minimalj.thymeleaf.page.ThymePage.ThymePageExchange;
import org.minimalj.util.LocaleContext;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolution;
import org.thymeleaf.templateresource.ClassLoaderTemplateResource;

/**
 * Use this Handler in {@link WebApplication#createHttpHandler} and override at
 * least {@link #createContext(MjHttpExchange)}
 * 
 * @author bruno
 *
 */
public abstract class ThymeHttpHandler implements MjHttpHandler {
	private final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
	private final TemplateEngine templateEngine = new TemplateEngine();
	private final ResourcesHttpHandler resourcesHttpHandler = new ResourcesHttpHandler();

	public ThymeHttpHandler() {
		templateResolver.setCacheable(!Configuration.isDevModeActive());
		templateEngine.setTemplateResolver(templateResolver);
		templateEngine.setMessageResolver(new MjMessageResolver());
	}

	/**
	 * Mapping from httpExchange to a template name. As default the name of the
	 * template matches the path of the request. But the name of the template can
	 * also depend on the request parameters.
	 * 
	 * @param exchange the MjHttpExchange
	 * @return name of the template. The prefix will be added to locate the template
	 *         in the classpath.
	 */
	protected String getTemplateName(MjHttpExchange exchange) {
		String templateName = exchange.getPath();
		if (templateName.endsWith("/")) {
			return templateName + "index.html";
		}
		return templateName.endsWith(".html") ? templateName : null;
	}

	private boolean exists(String templateName) {
		TemplateResolution templateResolution = templateResolver.resolveTemplate(templateEngine.getConfiguration(), null, templateName, null);
		if (templateResolution != null) {
			ClassLoaderTemplateResource resource = (ClassLoaderTemplateResource) templateResolution.getTemplateResource();
			return resource != null && resource.exists();
		}
		return false;
	}

	/**
	 * Fill in the variables to be used in the template. This method is expected to
	 * be overridden and custom variables should be set according to the path or the
	 * parameters in the exchange parameter.
	 * 
	 * @param exchange the MjHttpExchange
	 * @return Map with variables to be used in the templates
	 */
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
			return resourcesHttpHandler.handle(exchange);
		}

		Map<String, Object> variables = createContext(exchange);
		Context context = new Context(LocaleContext.getCurrent(), variables);
		String response = templateEngine.process(templateName, context);
		exchange.sendResponse(200, response, "text/html");

		return true;
	}

}
