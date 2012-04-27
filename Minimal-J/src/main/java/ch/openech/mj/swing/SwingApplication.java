package ch.openech.mj.swing;

import java.lang.reflect.InvocationTargetException;
import java.util.prefs.Preferences;

import javax.swing.SwingUtilities;

import ch.openech.mj.application.ApplicationConfig;
import ch.openech.mj.application.ApplicationContext;
import ch.openech.mj.swing.toolkit.SwingClientToolkit;
import ch.openech.mj.toolkit.ClientToolkit;

public abstract class SwingApplication implements Runnable {

	private static ApplicationContext applicationContext;

	protected SwingApplication() {
		if (applicationContext != null)
			throw new IllegalStateException("Only one instance of SwingApplication allowed");
		applicationContext = new SwingApplicationContext();
	}

	public static ApplicationContext getApplicationContext() {
		return applicationContext;
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

	public class SwingApplicationContext extends ApplicationContext {
		private String user;

		public SwingApplicationContext() {
		}

		@Override
		public void setUser(String user) {
			this.user = user;
		}

		@Override
		public String getUser() {
			return user;
		}
		
		@Override
		public void savePreferences(Object preferences) {
			PreferencesHelper.save(Preferences.userNodeForPackage(SwingApplication.this.getClass()), preferences);
		}

		@Override
		public void loadPreferences(Object preferences) {
			PreferencesHelper.load(Preferences.userNodeForPackage(SwingApplication.this.getClass()), preferences);
		}
	}

}
