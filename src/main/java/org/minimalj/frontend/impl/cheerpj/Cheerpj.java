package org.minimalj.frontend.impl.cheerpj;

import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.json.JsonPageManager;
import org.minimalj.model.test.ModelTest;

public class Cheerpj {

	private static JsonPageManager pageManager = new JsonPageManager();
	
	public static String receiveMessage(String inputString) {
		return pageManager.handle(inputString);
	}

	public static void start() {
		ModelTest.exitIfProblems();
		Frontend.setInstance(new JsonFrontend());
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
