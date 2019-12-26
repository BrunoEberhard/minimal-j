package org.minimalj.thymeleaf;

import org.minimalj.application.Configuration;
import org.minimalj.frontend.impl.web.MjHttpExchange;
import org.minimalj.frontend.impl.web.MjHttpHandler;
import org.minimalj.frontend.impl.web.ResourcesHttpHandler;
import org.minimalj.frontend.impl.web.WebApplication;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * Use this Handler in {@link WebApplication#createHttpHandler}
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

	protected abstract void handle(ThymeRequest request);

	@Override
	public final boolean handle(MjHttpExchange exchange) {
		ThymeRequest request = new ThymeRequest(templateEngine, exchange);

		handle(request);
		if (request.isResponseSent()) {
			return true;
		} else {
			return resourcesHttpHandler.handle(exchange);
		}
	}

}
