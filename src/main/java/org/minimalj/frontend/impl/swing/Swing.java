package org.minimalj.frontend.impl.swing;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.minimalj.application.Application;
import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.swing.toolkit.SwingFrontend;

public class Swing implements Runnable {

	private Swing() {
		// private
	}

	@Override
	public void run() {
		FrameManager.setSystemLookAndFeel();
		Frontend.setInstance(new SwingFrontend());
		Backend.setInstance(new SwingBackend(Backend.create()));
		
		FrameManager.getInstance().openNavigationFrame(null);
	}

	public static void main(final String... args) {
		Application.initApplication(args);

		try {
			SwingUtilities.invokeAndWait(new Swing());
		} catch (InvocationTargetException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
