package org.minimalj.frontend.impl.vaadin;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.application.ThreadLocalApplication;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.vaadin.toolkit.VaadinFrontend;
import org.minimalj.security.Subject;

import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletRequest;

@javax.servlet.annotation.WebServlet(urlPatterns = "/*",
	initParams =
	{
	    @javax.servlet.annotation.WebInitParam(name = "UI", value = "org.minimalj.frontend.impl.vaadin.Vaadin"),
	    @javax.servlet.annotation.WebInitParam(name = "widgetset", value = "org.minimalj.frontend.impl.vaadin.MjWidgetSet"),
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
	
	@Override
	protected VaadinServletRequest createVaadinRequest(HttpServletRequest request) {
		VaadinServletRequest vaadinServletRequest = super.createVaadinRequest(request);
		Subject.setCurrent((Subject) vaadinServletRequest.getWrappedSession().getAttribute("subject"));
		// System.out.println("Set subject to " + (Subject.getCurrent() != null ? Subject.getCurrent().getName() : "-"));
		return vaadinServletRequest;
	}
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			super.service(request, response);
		} finally {
			Subject.setCurrent(null);
		}
	}
	
}
