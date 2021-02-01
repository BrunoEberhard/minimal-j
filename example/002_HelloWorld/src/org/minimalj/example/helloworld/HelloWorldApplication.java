package org.minimalj.example.helloworld;

import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend;

public class HelloWorldApplication extends Application {

	@Override
	public void init() {
		Frontend.show(new HelloWorldPage());
	}
}
