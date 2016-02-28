package org.minimalj.example.demo;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.minimalj.application.Application;
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
	
	@Override
	protected void initializeApplication() {
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
			application.addApplication("petClinic", new PetClinicApplication());
			
			Application.setApplication(application);
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
		DemoContext.setContext(applicationName);
		
		super.service(request, response);
	}
	
}