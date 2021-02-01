package org.minimalj.frontend.impl.swing;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.minimalj.application.Application;
import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.swing.toolkit.SwingFrontend;
import org.minimalj.model.test.ModelTest;

public class Swing implements Runnable {

	private Swing() {
		// private
	}

	@Override
	public void run() {
		Frontend.setInstance(new SwingFrontend());
		Backend.setInstance(new SwingBackend(Backend.create()));

		FrameManager.setSystemLookAndFeel();
		
		Application.getInstance().init();
	}

	public static void start(Application application) {
		Application.setInstance(application);
		start();
	}

	public static void start() {
		ModelTest.exitIfProblems();
		try {
			SwingUtilities.invokeAndWait(new Swing());
		} catch (InvocationTargetException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void main(String... args) {
		Application.initApplication(args);
		start();
	}
}
