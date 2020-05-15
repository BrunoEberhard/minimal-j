package org.minimalj.frontend.impl.vaadin;

import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.vaadin.toolkit.VaadinFrontend;
import org.minimalj.frontend.impl.web.WebApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

@SpringBootApplication
public class VaadinSpringboot {

	@Bean
	public ServletRegistrationBean exampleServletBean() {
		ServletRegistrationBean bean = new ServletRegistrationBean(new MjWebApplicationServlet(), "/*");
		bean.setLoadOnStartup(1);
		return bean;
	}

	private static void start() {
		Frontend.setInstance(new VaadinFrontend());

		String mjHandlerPath = WebApplication.mjHandlerPath();
		if (!StringUtils.isEmpty(mjHandlerPath) && mjHandlerPath.startsWith("/")) {
			mjHandlerPath = mjHandlerPath.substring(1);
			if (!mjHandlerPath.isEmpty()) {
				System.setProperty("vaadin.servlet.urlMapping", mjHandlerPath);
			}
		}

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
