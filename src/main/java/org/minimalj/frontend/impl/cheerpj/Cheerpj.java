package org.minimalj.frontend.impl.cheerpj;

import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.json.JsonPageManager;
import org.minimalj.model.test.ModelTest;

public class Cheerpj {

	private static JsonPageManager pageManager = new JsonPageManager();
	
	public static String receiveMessage(String inputString) {
		checkApplicationReady();
		String result = pageManager.handle(inputString);
		return result;
	}

	/*
	 * Better solution: add 'await' and '.then' to cheerpjRunMain but this would be
	 * be compatible with IE11 (don't forget to add 'sync' to function init() if you
	 * do that)
	 */
	private static void checkApplicationReady() {
		int count = 0;
		while (true) {
			try {
				Application.getInstance();
				break;
			} catch (IllegalStateException e) {
				System.out.println("Wait for application");
				try {
					count++;
					if (count > 600) {
						throw new IllegalStateException("Application does not get ready");
					}
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					// ignore
				}
			}
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
		System.out.println("Start Cheerpj with " + args[0]);
		Application.initApplication(args);
		start();
		System.out.println("Started");
	}
	
}
