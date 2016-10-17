package org.minimalj.frontend.impl.vaadin;

import java.util.Enumeration;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.application.ThreadLocalApplication;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.vaadin.toolkit.VaadinFrontend;

import com.vaadin.server.VaadinServlet;

@javax.servlet.annotation.WebServlet(urlPatterns = "/*",
	initParams =
	{
	    @javax.servlet.annotation.WebInitParam(name = "UI", value = "org.minimalj.frontend.impl.vaadin.Vaadin"),
	})
@javax.servlet.annotation.HandlesTypes(Application.class)
public class MjVaadinServlet extends VaadinServlet implements javax.servlet.ServletContainerInitializer {
	private static final long serialVersionUID = 1L;

	@Override
    public void init(ServletConfig config) throws ServletException {
		super.init(config);
		copyInitParametersToConfiguration(config.getServletContext());
		Frontend.setInstance(new VaadinFrontend());
    }

	protected void copyInitParametersToConfiguration(ServletContext servletContext) {
		Enumeration<?> propertyNames = servletContext.getInitParameterNames();
		while (propertyNames.hasMoreElements()) {
			String propertyName = (String) propertyNames.nextElement();
			Configuration.set(propertyName, servletContext.getInitParameter(propertyName));
		}
	}	
	
	@Override
	public void onStartup(Set<Class<?>> applicationClasses, ServletContext servletContext) throws ServletException {
		applicationClasses.remove(ThreadLocalApplication.class);
		if (applicationClasses.size() == 0) {
			throw new IllegalStateException("No application found");
		} else if (applicationClasses.size() > 1) {
			return;
			// throw new IllegalStateException("There should be only one Application in classpath");
		}
		Application.initApplication(applicationClasses.iterator().next().getName());
	}
}
