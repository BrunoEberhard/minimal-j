package org.minimalj.thymeleaf;

import org.minimalj.application.Configuration;
import org.minimalj.frontend.impl.web.MjHttpExchange;
import org.minimalj.frontend.impl.web.MjHttpHandler;
import org.minimalj.frontend.impl.web.ResourcesHttpHandler;
import org.minimalj.frontend.impl.web.WebApplication;
import org.minimalj.util.LocaleContext;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * Use this Handler in {@link WebApplication#createHttpHandler}
 * 
 * @author bruno
 *
 */
public abstract class ThymeHttpHandler implements MjHttpHandler {
	public static final String DEFAULT_PATH = null;

	private final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
	private final TemplateEngine templateEngine = new TemplateEngine();
	private final ResourcesHttpHandler resourcesHttpHandler = new ResourcesHttpHandler();

	public ThymeHttpHandler() {
		templateResolver.setCacheable(!Configuration.isDevModeActive());
		templateEngine.setTemplateResolver(templateResolver);
		templateEngine.setMessageResolver(new MjMessageResolver());
	}

	/**
	 * 
	 * @param request contains path, parameters and variable container for ThymeLeaf
	 * @return name of the template to be used. Must end with ".html" and be
	 *         available in classpath. Return <code>DEFAULT_PATH</code> or
	 *         <code>null</code> if template name should match the path.
	 */
	protected abstract String handle(ThymeRequest request);

	@Override
	public final boolean handle(MjHttpExchange exchange) {
		ThymeRequest request = new ThymeRequest(exchange);

		String templateName = handle(request);

		if (templateName == DEFAULT_PATH) {
			templateName = exchange.getPath();
		}

		if (!templateName.endsWith(".html")) {
			return resourcesHttpHandler.handle(exchange);
		}

		Context context = new Context(LocaleContext.getCurrent(), request.getContext());
		String response = templateEngine.process(templateName, context);
		exchange.sendResponse(200, response, "text/html");

		return true;
	}

}
