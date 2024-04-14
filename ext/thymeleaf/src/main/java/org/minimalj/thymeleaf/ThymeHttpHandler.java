package org.minimalj.thymeleaf;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.frontend.impl.web.MjHttpExchange;
import org.minimalj.frontend.impl.web.MjHttpHandler;
import org.minimalj.frontend.impl.web.WebApplication;
import org.minimalj.frontend.impl.web.WebApplicationPage.WebApplicationPageExchange;
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

	private final MjHttpHandler next;
	
	public ThymeHttpHandler() {
		this(null);
	}
	
	public ThymeHttpHandler(MjHttpHandler next) {
		this.next = next;
		
		templateResolver.setCacheable(!Configuration.isDevModeActive());
		templateResolver.setPrefix(Application.getInstance().getClass().getPackage().getName().replace(".", "/") + "/web/");

		templateEngine.setTemplateResolver(templateResolver);
		templateEngine.setMessageResolver(new MjMessageResolver());
	}

	protected abstract void handle(ThymeRequest request);

	@Override
	public final void handle(MjHttpExchange exchange) {
		ThymeRequest request = new ThymeRequest(templateEngine, exchange);
		request.put("isApplication", exchange instanceof WebApplicationPageExchange);

		handle(request);
		
		if (!request.isResponseSent() && next != null) {
			next.handle(exchange);
		}
	}

}
