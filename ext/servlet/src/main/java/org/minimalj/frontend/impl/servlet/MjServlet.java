package org.minimalj.frontend.impl.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Enumeration;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.application.ThreadLocalApplication;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.json.JsonPageManager;
import org.minimalj.util.resources.Resources;

@javax.servlet.annotation.WebServlet(urlPatterns = "/")
@javax.servlet.annotation.HandlesTypes(Application.class)
public class MjServlet extends HttpServlet implements javax.servlet.ServletContainerInitializer {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(MjServlet.class.getName());
	
	@Override
    public void init(ServletConfig config) throws ServletException {
		super.init(config);
		copyInitParametersToConfiguration(config.getServletContext());
		Frontend.setInstance(new JsonFrontend());
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
		javax.servlet.http.HttpSession session = request.getSession(true);
		if (uri.endsWith("/")) {
			session.setAttribute("MjPageManager", new JsonPageManager());
			String htmlTemplate = JsonFrontend.getHtmlTemplate();
			String path = URI.create(requestURI).getPath();
			String html = JsonFrontend.fillPlaceHolder(htmlTemplate, path);
			response.getWriter().write(html);
			response.setContentType("text/html");
			return;
		} else if (uri.endsWith("/ajax_request.xml")) {
			String postData = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			JsonPageManager pageManager = (JsonPageManager) session.getAttribute("MjPageManager");
			String result = pageManager.handle(postData);
			response.getWriter().write(result);
			response.setContentType("text/xml");
			return;	
		} else if (uri.endsWith("/application.png")) {			
			response.setContentType("png");
			inputStream = Application.getInstance().getIcon();
		} else {
			int index = uri.lastIndexOf('.');
			if (index > -1 && index < uri.length()-1) {
				String postfix = uri.substring(index+1);
				String mimeType = Resources.getMimeType(postfix);
				if (mimeType != null) {
					response.setContentType(mimeType);
					int pos = uri.lastIndexOf("/");
					inputStream = getClass().getResourceAsStream(uri.substring(pos));
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