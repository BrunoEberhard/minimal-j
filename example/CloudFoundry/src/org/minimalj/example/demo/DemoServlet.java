package org.minimalj.example.demo;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.minimalj.application.Application;
import org.minimalj.frontend.impl.servlet.MjServlet;

@javax.servlet.annotation.WebServlet(urlPatterns = "/")
public class DemoServlet extends MjServlet {
	private static final long serialVersionUID = 1L;

	private final InheritableThreadLocal<String> currentApplicationName = new InheritableThreadLocal<>();
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		Application.setInstance(new ExamplesApplication());
	}
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String contextPath = request.getContextPath();
		String requestURI = request.getRequestURI();
		String uri = requestURI.substring(contextPath.length());
		
		String uriWithoutfile = uri.substring(0, uri.lastIndexOf('/'));
		String applicationName = uriWithoutfile.substring(uriWithoutfile.lastIndexOf('/') + 1);
		
		currentApplicationName.set(applicationName);
		((ExamplesApplication) Application.getInstance()).setCurrentApplication(applicationName);

		super.service(request, response);
	}

	@Override
	protected String fillPlaceHolder(String html, Locale locale, String url) {
		html = html.replace("$WS", "../ws/" + currentApplicationName.get());
		return super.fillPlaceHolder(html, locale, url);
	}
}