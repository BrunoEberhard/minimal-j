package org.minimalj.example.demo;

import java.util.HashMap;
import java.util.Map;

import org.minimalj.application.Application;
import org.minimalj.application.ThreadLocalApplication;
import org.minimalj.example.empty.EmptyApplication;
import org.minimalj.example.helloworld.HelloWorldApplication;
import org.minimalj.example.helloworld2.GreetingApplication;
import org.minimalj.example.library.MjExampleApplication;
import org.minimalj.example.minimalclinic.MinimalClinicApplication;
import org.minimalj.example.notes.NotesApplication;
import org.minimalj.example.numbers.NumbersApplication;

public class ExamplesApplication extends ThreadLocalApplication {

	private final Map<String, Application> applications;
	
	public ExamplesApplication() {
		applications = new HashMap<>();
		applications.put("empty", new EmptyApplication());
		applications.put("notes", new NotesApplication());
		applications.put("helloWorld", new HelloWorldApplication());
		applications.put("greeting", new GreetingApplication());
		applications.put("numbers", new NumbersApplication());
		applications.put("library", new MjExampleApplication());
//		applications.put("petClinic", new PetClinicApplication());
		applications.put("minimalClinic", new MinimalClinicApplication());
	}
	
	public void setCurrentApplication(String applicationName) {
		setCurrentApplication(applications.get(applicationName));
		
	}
	
	public static void main(String[] args) {
		DemoWebServer.start(new ExamplesApplication());
	}
}
