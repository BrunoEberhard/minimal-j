package ch.openech.mj.swing;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import ch.openech.mj.application.ApplicationConfig;
import ch.openech.mj.swing.toolkit.SwingClientToolkit;
import ch.openech.mj.toolkit.ClientToolkit;

public abstract class SwingApplication implements Runnable {

	protected SwingApplication() {
	}
	
	/**
	 * Initializes application and opens a new frame
	 * 
	 * @param application
	 */
	public static void launch(Runnable runnable, ApplicationConfig applicationConfig) {
		ClientToolkit.setToolkit(new SwingClientToolkit());
		ApplicationConfig.setApplicationConfig(applicationConfig);
		runApplication(runnable);
	}

	private static void runApplication(Runnable runnable) {
		try {
			SwingUtilities.invokeAndWait(runnable);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

}
