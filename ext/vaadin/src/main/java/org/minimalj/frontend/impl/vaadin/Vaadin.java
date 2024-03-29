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
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.vaadin.flow.spring.RootMappedCondition;

@SpringBootApplication
public class Vaadin extends SpringBootServletInitializer {

	private static boolean hasUrlMapping;
	
    @Bean(name = "WebApplicationServletRegistration")
    @Conditional(IsWebApplication.class)
    public ServletRegistrationBean<MjWebApplicationServlet> servletRegistrationBean() {
        return new ServletRegistrationBean<>(new MjWebApplicationServlet(), "/*");
    }
	
    public static class IsWebApplication implements Condition {

		@Override
		public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
			return Application.getInstance() instanceof WebApplication && hasUrlMapping;
		}
    }

    public static boolean hasUrlMapping() {
    	return hasUrlMapping;
    }
    
	private static void start(String... args) {
		Frontend.setInstance(new VaadinFrontend());

		String mjHandlerPath = WebApplication.mjHandlerPath();
		hasUrlMapping = !StringUtils.isEmpty(mjHandlerPath) && mjHandlerPath.startsWith("/") && !mjHandlerPath.equals("/");
		if (hasUrlMapping) {
			mjHandlerPath += "*";
			System.setProperty(RootMappedCondition.URL_MAPPING_PROPERTY, mjHandlerPath);
		}
		
		var result = SpringApplication.run(Vaadin.class, args);
		System.out.println(result);
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
