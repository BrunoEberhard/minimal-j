package org.minimalj.frontend.impl.servlet;

import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.web.WebApplication;
import org.minimalj.frontend.impl.web.WebServer;

public class MjServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(MjServlet.class.getName());

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		copyInitParametersToConfiguration(config);
		Frontend.setInstance(new JsonFrontend());
		Application.initApplication(Configuration.get("MjApplication"));

		if (WebServer.useWebSocket) {
			addWebSocketEndpoint(config);
		}
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		WebApplication.handle(new HttpServletHttpExchange(request, response));
	}

	private void addWebSocketEndpoint(ServletConfig servletConfig) {
		ServletContext servletContext = servletConfig.getServletContext();
		Object serverContainerAttribute = servletContext.getAttribute("javax.websocket.server.ServerContainer");
		if (serverContainerAttribute instanceof ServerContainer) {
			ServerContainer serverContainer = (ServerContainer) serverContainerAttribute;
			for (String mapping : servletConfig.getServletContext().getServletRegistration(servletConfig.getServletName()).getMappings()) {
				if (mapping.endsWith("*")) {
					mapping = mapping.substring(0, mapping.length() - 1);
				}
				if (mapping.endsWith("/")) {
					mapping = mapping.substring(0, mapping.length() - 1);
				}
				ServerEndpointConfig config = ServerEndpointConfig.Builder.create(MjWebSocketServlet.class, mapping + WebApplication.mjHandlerPath() + "ws").build();
				try {
					serverContainer.addEndpoint(config);
				} catch (DeploymentException e) {
					throw new RuntimeException(e);
				}
			}
		} else {
			logger.warning("WebSockets should be activated but no ServerContainer available on server");
		}
	}

	private void copyInitParametersToConfiguration(ServletConfig config) {
		Enumeration<?> propertyNames = config.getInitParameterNames();
		while (propertyNames.hasMoreElements()) {
			String propertyName = (String) propertyNames.nextElement();
			Configuration.set(propertyName, config.getInitParameter(propertyName));
		}
	}
}