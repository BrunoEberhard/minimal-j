package org.minimalj.frontend.impl.swing;

import javax.swing.SwingUtilities;

import org.minimalj.application.Application;
import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.swing.toolkit.SwingFrontend;
import org.minimalj.security.LoginTransaction;
import org.minimalj.security.Subject;

public class SwingApplication implements Runnable {

	private SwingApplication() {
		// private
	}

	/**
	 * Initializes application and opens a new frame
	 * 
	 */
	@Override
	public void run() {
		FrameManager.setSystemLookAndFeel();
		Frontend.setInstance(new SwingFrontend());
		Backend.setInstance(new SwingBackend(Backend.getInstance()));
		
		Subject anonymousSubject = Backend.getInstance().execute(new LoginTransaction());
		FrameManager.getInstance().openNavigationFrame(anonymousSubject);
	}

	public static void main(final String[] args) throws Exception {
		Application.initApplication(args);

		SwingUtilities.invokeAndWait(new SwingApplication());
	}
	
}
