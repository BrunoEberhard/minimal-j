package org.minimalj.frontend.impl.vaadin;

import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.vaadin.toolkit.VaadinFrontend;
import org.minimalj.frontend.impl.web.WebApplication;
import org.minimalj.util.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import com.vaadin.flow.spring.RootMappedCondition;

@SpringBootApplication
public class Vaadin extends SpringBootServletInitializer {

//    @Bean(name = "WebApplicationServletRegistration")
//    @Conditional(IsWebApplication.class)
//    public ServletRegistrationBean<MjWebApplicationServlet> servletRegistrationBean() {
//        return new ServletRegistrationBean<>(new MjWebApplicationServlet(), "/*");
//    }
//	
//    public static class IsWebApplication implements Condition {
//
//		@Override
//		public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
//			return Application.getInstance() instanceof WebApplication;
//		}
//    }
    
	private static void start(String... args) {
		Frontend.setInstance(new VaadinFrontend());

		String mjHandlerPath = WebApplication.mjHandlerPath();
		if (!StringUtils.isEmpty(mjHandlerPath) && mjHandlerPath.startsWith("/")) {
			if (!mjHandlerPath.isEmpty()) {
				mjHandlerPath += "*";
				System.setProperty(RootMappedCondition.URL_MAPPING_PROPERTY, mjHandlerPath);
			}
		} else {
			System.setProperty(RootMappedCondition.URL_MAPPING_PROPERTY, "/*");
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
