package org.minimalj.frontend.swing;

import java.util.prefs.Preferences;

import javax.swing.SwingUtilities;

import org.minimalj.application.ApplicationContext;
import org.minimalj.application.Launcher;
import org.minimalj.frontend.swing.toolkit.SwingClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit;

public class SwingLauncher extends Launcher implements Runnable {

	private static ApplicationContext applicationContext;

	private SwingLauncher() {
		// private
	}

	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	/**
	 * Initializes application and opens a new frame
	 * 
	 * @param application
	 */
	@Override
	public void run() {
		FrameManager.setSystemLookAndFeel();
		ClientToolkit.setToolkit(new SwingClientToolkit());
		applicationContext = new SwingApplicationContext();
		FrameManager.getInstance().openNavigationFrame();
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
			PreferencesHelper.save(Preferences.userNodeForPackage(SwingLauncher.this.getClass()), preferences);
		}

		@Override
		public void loadPreferences(Object preferences) {
			PreferencesHelper.load(Preferences.userNodeForPackage(SwingLauncher.this.getClass()), preferences);
		}
	}

	public static void main(final String[] args) throws Exception {
		initApplication(args);

		SwingUtilities.invokeAndWait(new SwingLauncher());
	}
	
}
