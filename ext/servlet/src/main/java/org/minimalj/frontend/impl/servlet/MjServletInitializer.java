package org.minimalj.frontend.impl.servlet;

import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.application.ThreadLocalApplication;
import org.minimalj.frontend.impl.web.WebApplication;
import org.minimalj.frontend.impl.web.WebServer;

@javax.servlet.annotation.HandlesTypes(Application.class)
public class MjServletInitializer implements javax.servlet.ServletContainerInitializer {
	private static final Logger logger = Logger.getLogger(MjServletInitializer.class.getName());

	@Override
	public void onStartup(Set<Class<?>> applicationClasses, ServletContext servletContext) throws ServletException {
		copyInitParametersToConfiguration(servletContext);

		if (Configuration.available("MjApplication")) {
			Application.initApplication(Configuration.get("MjApplication"));
		} else {
			applicationClasses.remove(ThreadLocalApplication.class);
			applicationClasses = applicationClasses.stream().filter(c -> !Modifier.isAbstract(c.getModifiers())).collect(Collectors.toSet());
			if (applicationClasses.size() == 0) {
				throw new IllegalStateException("No application found");
			} else if (applicationClasses.size() > 1) {
				throw new IllegalStateException("There should be only one Application in classpath");
			}
			Application.initApplication(applicationClasses.iterator().next().getName());
		}

		ServletRegistration.Dynamic registration = servletContext.addServlet("Minimal-J Servlet", MjServlet.class);
		registration.addMapping("/");

		if (WebServer.useWebSocket) {
			addWebSocketEndpoint(servletContext);
		}
	}

	private void addWebSocketEndpoint(ServletContext servletContext) {
		Object serverContainerAttribute = servletContext.getAttribute("javax.websocket.server.ServerContainer");
		if (serverContainerAttribute instanceof ServerContainer) {
			ServerContainer serverContainer = (ServerContainer) serverContainerAttribute;
			ServerEndpointConfig config = ServerEndpointConfig.Builder.create(MjWebSocketServlet.class, WebApplication.mjHandlerPath() + "ws").build();
			try {
				serverContainer.addEndpoint(config);
			} catch (DeploymentException e) {
				throw new RuntimeException(e);
			}
		} else {
			logger.warning("WebSockets should be activated but no ServerContainer available on server");
		}
	}

	private void copyInitParametersToConfiguration(ServletContext servletContext) {
		Enumeration<?> propertyNames = servletContext.getInitParameterNames();
		while (propertyNames.hasMoreElements()) {
			String propertyName = (String) propertyNames.nextElement();
			Configuration.set(propertyName, servletContext.getInitParameter(propertyName));
		}
	}

}
