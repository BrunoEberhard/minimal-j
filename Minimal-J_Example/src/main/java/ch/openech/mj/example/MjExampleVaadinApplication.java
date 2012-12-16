package ch.openech.mj.example;

import java.net.URL;
import java.util.Properties;

import ch.openech.mj.application.ApplicationConfig;
import ch.openech.mj.vaadin.MinimalJVaadinApplication;

import com.vaadin.service.ApplicationContext;

public class MjExampleVaadinApplication extends MinimalJVaadinApplication {

	public MjExampleVaadinApplication() {
	}
	
	static {
		ApplicationConfig.setApplicationConfig(new ApplicationConfigExample());
	}

	@Override
	public void init() {
		super.init();
		System.out.println("Init " + this.hashCode());
	}

	@Override
	public void close() {
		super.close();
		System.out.println("Close" + this.hashCode());
	}

	@Override
	public void start(URL applicationUrl, Properties applicationProperties, ApplicationContext context) {
		super.start(applicationUrl, applicationProperties, context);
		System.out.println("start " + this.hashCode());
	}
	
}
