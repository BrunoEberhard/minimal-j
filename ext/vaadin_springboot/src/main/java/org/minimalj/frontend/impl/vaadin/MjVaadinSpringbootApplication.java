package org.minimalj.frontend.impl.vaadin;

import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.vaadin.toolkit.VaadinFrontend;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MjVaadinSpringbootApplication {

	public static void main(String... args) {
		Application.initApplication(args);
		Frontend.setInstance(new VaadinFrontend());
		
		SpringApplication.run(MjVaadinSpringbootApplication.class, args);
	}
	
}
