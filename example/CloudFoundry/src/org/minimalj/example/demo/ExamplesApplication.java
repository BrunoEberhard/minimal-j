package org.minimalj.example.demo;

import java.util.HashMap;
import java.util.Map;

import org.minimalj.application.Application;
import org.minimalj.example.empty.EmptyApplication;
import org.minimalj.example.helloworld.HelloWorldApplication;
import org.minimalj.example.helloworld2.GreetingApplication;
import org.minimalj.example.library.MjExampleApplication;
import org.minimalj.example.notes.NotesApplication;
import org.minimalj.example.numbers.NumbersApplication;
import org.minimalj.example.petclinic.PetClinicApplication;

public class ExamplesApplication extends CombinedApplication {

	public ExamplesApplication() {
		super(getApplications());
	}
	
	private static Map<String, Application> getApplications() {
		Map<String, Application> applications = new HashMap<>();
		applications.put("empty", new EmptyApplication());
		applications.put("notes", new NotesApplication());
		applications.put("helloWorld", new HelloWorldApplication());
		applications.put("greeting", new GreetingApplication());
		applications.put("numbers", new NumbersApplication());
		applications.put("library", new MjExampleApplication());
		applications.put("petClinic", new PetClinicApplication());
		return applications;
	}

}
