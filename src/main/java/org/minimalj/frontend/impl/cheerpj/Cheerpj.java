package org.minimalj.frontend.impl.cheerpj;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.json.JsonPageManager;
import org.minimalj.model.test.ModelTest;

public class Cheerpj {

	private static JsonPageManager pageManager = new JsonPageManager();
	
	public static String receiveMessage(String inputString) {
		try {
			return pageManager.handle(inputString);
		} catch (Throwable t) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			pw.flush();
			sw.flush();
			return sw.toString();
		}
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
