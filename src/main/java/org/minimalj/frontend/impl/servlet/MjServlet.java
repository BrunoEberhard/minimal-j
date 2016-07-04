package org.minimalj.frontend.impl.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.json.JsonPageManager;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

public class MjServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(MjServlet.class.getName());
	
	private static boolean applicationInitialized;
	
	protected void initializeApplication() {
		if (!applicationInitialized) {
			Frontend.setInstance(new JsonFrontend());
			String applicationName = getServletConfig().getInitParameter("Application");
			if (StringUtils.isBlank(applicationName)) {
				throw new IllegalArgumentException("Missing Application parameter");
			}
			Application application = Application.createApplication(applicationName);
			Application.setInstance(application);
			applicationInitialized = true;
		}
	}
	
	private void copyPropertiesFromServletConfigToSystem() {
		Enumeration<?> propertyNames = getServletConfig().getInitParameterNames();
		while (propertyNames.hasMoreElements()) {
			String propertyName = (String) propertyNames.nextElement();
			System.setProperty(propertyName, getServletConfig().getInitParameter(propertyName));
		}
	}	
	
	@Override
	public void init() throws ServletException {
		copyPropertiesFromServletConfigToSystem();
		initializeApplication();
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String contextPath = request.getContextPath();
		String requestURI = request.getRequestURI();
		String uri = requestURI.substring(contextPath.length());

		if (uri.equals("")) {
			response.sendRedirect("./");
			return;
		}
		
		InputStream inputStream = null;
		uri = uri.substring(uri.lastIndexOf('/'), uri.length());
		javax.servlet.http.HttpSession session = request.getSession(true);
		if (uri.equals("/")) {
			session.setAttribute("MjPageManager", new JsonPageManager());
			String htmlTemplate = JsonFrontend.getHtmlTemplate();
			String html = fillPlaceHolder(htmlTemplate, request.getLocale(), request.getRequestURL().toString());
			response.getWriter().write(html);
			response.setContentType("text/html");
			return;
		} else if ("/ajax_request.xml".equals(uri)) {
			String postData = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			JsonPageManager pageManager = (JsonPageManager) session.getAttribute("MjPageManager");
			String result = pageManager.handle(postData);
			response.getWriter().write(result);
			response.setContentType("text/xml");
			return;	
		} else {
			int index = uri.lastIndexOf('.');
			if (index > -1 && index < uri.length()-1) {
				String postfix = uri.substring(index+1);
				String mimeType = Resources.getMimeType(postfix);
				if (mimeType != null) {
					response.setContentType(mimeType);
					inputStream = Resources.getInputStream(uri.substring(1));
				}
			}
		}
		if (inputStream == null) {
			logger.warning("Could not serve: " + uri);
			response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
			return;
		}
		
		OutputStream outputStream = response.getOutputStream();
		byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
	}
	
	protected String fillPlaceHolder(String html, Locale locale, String url) {
		// gives a subclass to replace some texts in the index.html
		return JsonFrontend.fillPlaceHolder(html, locale);
	}
}