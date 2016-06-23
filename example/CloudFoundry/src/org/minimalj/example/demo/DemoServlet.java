package org.minimalj.example.demo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.minimalj.application.Application;
import org.minimalj.backend.Backend;
import org.minimalj.example.empty.EmptyApplication;
import org.minimalj.example.helloworld.HelloWorldApplication;
import org.minimalj.example.helloworld2.GreetingApplication;
import org.minimalj.example.library.MjExampleApplication;
import org.minimalj.example.notes.NotesApplication;
import org.minimalj.example.numbers.NumbersApplication;
import org.minimalj.example.petclinic.PetClinicApplication;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.servlet.MjServlet;

public class DemoServlet extends MjServlet {
	private static final long serialVersionUID = 1L;

	private static boolean applicationInitialized;
	private static JsonFrontend frontend = new JsonFrontend();
	
	private final Map<String, Application> applications = new HashMap<>();
	private final Map<String, Backend> backends = new HashMap<>();
	
	@Override
	protected void initializeApplication() {
		if (!applicationInitialized) {
			applications.put("empty", new EmptyApplication());
			applications.put("notes", new NotesApplication());
			applications.put("helloWorld", new HelloWorldApplication());
			applications.put("greeting", new GreetingApplication());
			applications.put("numbers", new NumbersApplication());
			applications.put("library", new MjExampleApplication());
			applications.put("petClinic", new PetClinicApplication());
			
			applications.keySet().forEach((applicationName) -> backends.put(applicationName, new Backend()));
			
			applicationInitialized = true;
		}
	}
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String contextPath = request.getContextPath();
		String requestURI = request.getRequestURI();
		String uri = requestURI.substring(contextPath.length());
		
		String uriWithoutfile = uri.substring(0, uri.lastIndexOf('/'));
		String applicationName = uriWithoutfile.substring(uriWithoutfile.lastIndexOf('/') + 1);
		
		Frontend.setInstance(frontend);
		Backend.setInstance(backends.get(applicationName));
		Application.setInstance(applications.get(applicationName));

		super.service(request, response);
	}
	
}