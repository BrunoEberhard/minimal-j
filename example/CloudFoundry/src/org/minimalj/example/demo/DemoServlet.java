package org.minimalj.example.demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.minimalj.application.Application;
import org.minimalj.example.empty.EmptyApplication;
import org.minimalj.example.helloworld.HelloWorldApplication;
import org.minimalj.example.helloworld2.GreetingApplication;
import org.minimalj.example.library.MjExampleApplication;
import org.minimalj.example.notes.NotesApplication;
import org.minimalj.example.numbers.NumbersApplication;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.json.JsonFrontend;

public class DemoServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static boolean applicationInitialized;
	
	private synchronized void initializeApplication() {
		if (!applicationInitialized) {
			Frontend.setInstance(new JsonFrontend());

			System.setProperty("MjBackend", MultiBackend.class.getName());
			
			MultiApplication application = new MultiApplication();
			application.addApplication("empty", new EmptyApplication());
			application.addApplication("notes", new NotesApplication());
			application.addApplication("helloWorld", new HelloWorldApplication());
			application.addApplication("greeting", new GreetingApplication());
			application.addApplication("numbers", new NumbersApplication());
			application.addApplication("library", new MjExampleApplication());
			
			Application.setApplication(application);
			applicationInitialized = true;
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

		// differs from MjServlet here
		InputStream inputStream = null;
		if (uri.endsWith("/") || uri.endsWith(".html")) {
			inputStream = DemoServlet.class.getResourceAsStream("indexDemo.html");
			response.setContentType("text/html");
		// end change
			
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