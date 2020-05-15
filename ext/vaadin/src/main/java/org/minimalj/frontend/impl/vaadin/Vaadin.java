package org.minimalj.frontend.impl.vaadin;

import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.vaadin.toolkit.VaadinFrontend;
import org.minimalj.frontend.impl.web.WebApplication;
import org.minimalj.util.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

import com.vaadin.flow.spring.RootMappedCondition;

@SpringBootApplication
public class Vaadin extends SpringBootServletInitializer {

    @Bean(name = "WebApplicationServletRegistration")
    public ServletRegistrationBean<MjWebApplicationServlet> servletRegistrationBean() {
        ServletRegistrationBean<MjWebApplicationServlet> registration = new ServletRegistrationBean<>(
                new MjWebApplicationServlet(), "/*");
        return registration;
    }
	
	private static void start(String... args) {
		Frontend.setInstance(new VaadinFrontend());

		String mjHandlerPath = WebApplication.mjHandlerPath();
		if (!StringUtils.isEmpty(mjHandlerPath) && mjHandlerPath.startsWith("/")) {
			if (!mjHandlerPath.isEmpty()) {
				mjHandlerPath += "*";
				System.setProperty(RootMappedCondition.URL_MAPPING_PROPERTY, mjHandlerPath);
			}
		}

		SpringApplication.run(Vaadin.class, args);
	}
	
	public static void start(Application application) {
		Application.setInstance(application);
		start();
	}
	
	public static void main(String... args) {
		Application.initApplication(args);
		start(args);
	}	

}
