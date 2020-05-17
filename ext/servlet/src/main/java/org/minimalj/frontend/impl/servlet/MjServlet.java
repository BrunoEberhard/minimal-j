package org.minimalj.frontend.impl.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
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
import org.minimalj.frontend.impl.web.MjHttpExchange;
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

	public static class HttpServletHttpExchange extends MjHttpExchange {
		private final HttpServletRequest request;
		private final HttpServletResponse response;
		private boolean responseSent;

		public HttpServletHttpExchange(HttpServletRequest request, HttpServletResponse response) {
			this.request = request;
			this.response = response;
		}

		@Override
		public String getPath() {
			String contextPath = request.getServletPath();
			String requestURI = request.getRequestURI();
			String uri = requestURI.substring(contextPath.length());
			return uri;
		}

		@Override
		public InputStream getRequest() {
			try {
				return request.getInputStream();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public Map<String, List<String>> getParameters() {
			return Collections.emptyMap();
		}

		@Override
		public void sendResponse(int statusCode, byte[] bytes, String contentType) {
			response.setStatus(statusCode);
			try {
				OutputStream os = response.getOutputStream();
				responseSent = true;
				os.write(bytes);
				os.close();
			} catch (IOException x) {
				throw new RuntimeException(x);
			}
		}

		@Override
		public void sendResponse(int statusCode, String body, String contentType) {
			sendResponse(statusCode, body.getBytes(Charset.forName("utf-8")), contentType + "; charset=utf-8");
		}

		@Override
		public boolean isResponseSent() {
			return responseSent;
		}
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