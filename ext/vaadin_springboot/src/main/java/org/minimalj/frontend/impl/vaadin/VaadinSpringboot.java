package org.minimalj.frontend.impl.vaadin;

import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.vaadin.toolkit.VaadinFrontend;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VaadinSpringboot {

	private static void start() {
		Frontend.setInstance(new VaadinFrontend());
		SpringApplication.run(VaadinSpringboot.class);
	}
	
	public static void start(Application application) {
		Application.setInstance(application);
		start();
	}
	
	public static void main(String... args) {
		Application.initApplication(args);
		start();
	}
	
}
