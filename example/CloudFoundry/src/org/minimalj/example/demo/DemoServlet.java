package org.minimalj.example.demo;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.servlet.MjServlet;

public class DemoServlet extends MjServlet {
	private static final long serialVersionUID = 1L;

	private final InheritableThreadLocal<String> currentApplicationName = new InheritableThreadLocal<>();
	static ExamplesApplication examplesApplication;
	
	@Override
	protected void initializeApplication() {
		if (examplesApplication == null) {
			Frontend.setInstance(new JsonFrontend());
			
			examplesApplication = new ExamplesApplication();
			Application.setInstance(examplesApplication);
		}
	}
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String contextPath = request.getContextPath();
		String requestURI = request.getRequestURI();
		String uri = requestURI.substring(contextPath.length());
		
		String uriWithoutfile = uri.substring(0, uri.lastIndexOf('/'));
		String applicationName = uriWithoutfile.substring(uriWithoutfile.lastIndexOf('/') + 1);
		
		currentApplicationName.set(applicationName);
		examplesApplication.setCurrentApplication(applicationName);

		super.service(request, response);
	}

	@Override
	protected String fillPlaceHolder(String html, Locale locale, String url) {
		html = html.replace("$WS", "../demoWs/" + currentApplicationName.get());
		return super.fillPlaceHolder(html, locale, url);
	}
}