package org.minimalj.frontend.impl.servlet;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.web.WebApplication;
import org.minimalj.frontend.impl.web.WebServer;

public class MjServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		copyInitParametersToConfiguration(config);
		Frontend.setInstance(new JsonFrontend());
		Application.initApplication(Configuration.get("MjApplication"));

		if (WebServer.useWebSocket) {
			MjWebSocketServlet.addWebSocketEndpoint(config);
		}
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		WebApplication.handle(new HttpServletHttpExchange(request, response));
	}

	private void copyInitParametersToConfiguration(ServletConfig config) {
		Enumeration<?> propertyNames = config.getInitParameterNames();
		while (propertyNames.hasMoreElements()) {
			String propertyName = (String) propertyNames.nextElement();
			Configuration.set(propertyName, config.getInitParameter(propertyName));
		}
	}
}