package org.minimalj.frontend.impl.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.minimalj.application.Application;
import org.minimalj.util.StringUtils;

public class MjServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static boolean applicationInitialized;
	
	private synchronized void initializeApplication() {
		if (!applicationInitialized) {
			String applicationName = getServletContext().getInitParameter("Application");
			if (StringUtils.isBlank(applicationName)) {
				throw new IllegalArgumentException("Missing Application parameter");
			}
			Application application = Application.createApplication(applicationName);
			Application.setApplication(application);
			copyPropertiesFromServletContextToSystem();
			applicationInitialized = true;
		}
	}
	
	private void copyPropertiesFromServletContextToSystem() {
		Enumeration<?> propertyNames = getServletContext().getInitParameterNames();
		while (propertyNames.hasMoreElements()) {
			String propertyName = (String) propertyNames.nextElement();
			System.setProperty(propertyName, getServletContext().getInitParameter(propertyName));
		}
	}	

	@Override
	public void init() throws ServletException {
		initializeApplication();
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String contextPath = request.getContextPath();
		String requestURI = request.getRequestURI();
		String uri = requestURI.substring(contextPath.length());

		InputStream inputStream = null;
		if (uri.endsWith("/") || uri.endsWith(".html")) {
			inputStream = this.getClass().getClassLoader().getResourceAsStream("index.html");
			response.setContentType("text/html");

		} else if (uri.endsWith("css")) {
			inputStream = this.getClass().getClassLoader().getResourceAsStream(uri.substring(uri.lastIndexOf("/") + 1));
			response.setContentType("text/css");
			
		} else if (uri.endsWith("js")) {
			inputStream = this.getClass().getClassLoader().getResourceAsStream(uri.substring(uri.lastIndexOf("/") + 1));
			response.setContentType("application/javascript");
			
		} else if (uri.endsWith("/field_error.png")) {
			inputStream = this.getClass().getClassLoader().getResourceAsStream("org/minimalj/util/resources/icons/field_error.png");
			response.setContentType("image/png");
			
		} 
		if (inputStream == null) {
			System.out.println("uri: " + uri);
			response.setStatus(403);
			return;
		}
		
		OutputStream outputStream = response.getOutputStream();
		byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
	}
}