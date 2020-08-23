package org.minimalj.thymeleaf.page;

import java.util.HashMap;
import java.util.Map;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.backend.Backend;
import org.minimalj.frontend.impl.web.WebApplicationPage;
import org.minimalj.frontend.page.HtmlPage;
import org.minimalj.security.Subject;
import org.minimalj.thymeleaf.MjMessageResolver;
import org.minimalj.util.LocaleContext;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * This page is only needed if you want to use Thymeleaf in Minimal-J pages. It
 * is not needed if you just want to render templates for a WebApplication. If
 * you want to include pages from a WebApplication use
 * {@link WebApplicationPage}
 * 
 * @author bruno
 *
 */
public class ThymePage extends HtmlPage {
	private static final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
	private static final TemplateEngine templateEngine = new TemplateEngine();

	static {
		templateResolver.setCacheable(!Configuration.isDevModeActive());
		templateResolver.setPrefix(Application.getInstance().getClass().getPackage().getName().replace(".", "/") + "/web/");

		templateEngine.setTemplateResolver(templateResolver);
		templateEngine.setMessageResolver(new MjMessageResolver());
	}

	private String templateName;

	public ThymePage(String templateName) {
		super(null, null);
		this.templateName = templateName;
	}

	@Override
	protected String getHtml() {
		Map<String, Object> variables = new HashMap<>();

		variables.put("application", Application.getInstance());
		variables.put("subject", Subject.getCurrent());

		variables.put("MINIMALJ-VERSION", Application.class.getPackage().getImplementationVersion());
		variables.put("APPLICATION-VERSION", Application.getInstance().getClass().getPackage().getImplementationVersion());

		variables.put("backend", Backend.getInstance());

		Context c = new Context(LocaleContext.getCurrent(), variables);
		return templateEngine.process(templateName, c);
	}

}
