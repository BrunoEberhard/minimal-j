package org.minimalj.frontend.impl.servlet;

import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;

@javax.servlet.annotation.HandlesTypes(Application.class)
public class MjServletInitializer implements javax.servlet.ServletContainerInitializer {

	@Override
	public void onStartup(Set<Class<?>> applicationClasses, ServletContext servletContext) throws ServletException {
		copyInitParametersToConfiguration(servletContext);
		
		if (!hasMjServletRegistration(servletContext) && !Configuration.get("MjServletInitializer", "true").equals("false")) {
			applicationClasses = applicationClasses.stream().filter(c -> !Modifier.isAbstract(c.getModifiers())).collect(Collectors.toSet());
			if (applicationClasses.size() == 0) {
				throw new IllegalStateException("No application found");
			} else if (applicationClasses.size() > 1) {
				throw new IllegalStateException("There should be only one Application in classpath");
			}

			ServletRegistration.Dynamic registration = servletContext.addServlet("Minimal-J Servlet", MjServlet.class);
			registration.setInitParameter("MjApplication", applicationClasses.iterator().next().getName());
			registration.addMapping("/*");
		}
	}
	
	private boolean hasMjServletRegistration(ServletContext servletContext) {
		return servletContext.getServletRegistrations().values().stream().anyMatch(r -> MjServlet.class.getName().equals(r.getClassName()));
	}
	
	private void copyInitParametersToConfiguration(ServletContext context) {
		Enumeration<?> propertyNames = context.getInitParameterNames();
		while (propertyNames.hasMoreElements()) {
			String propertyName = (String) propertyNames.nextElement();
			Configuration.set(propertyName, context.getInitParameter(propertyName));
		}
	}
}
