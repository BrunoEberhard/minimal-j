package ch.openech.mj.swing;

import java.util.prefs.Preferences;

import javax.swing.SwingUtilities;

import ch.openech.mj.application.ApplicationContext;
import ch.openech.mj.application.MjApplication;
import ch.openech.mj.swing.toolkit.SwingClientToolkit;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.util.StringUtils;

public class SwingLauncher implements Runnable {

	private static String applicationName;
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
		try {
			Class<? extends MjApplication> applicationClass = (Class<? extends MjApplication>) Class.forName(applicationName);
			MjApplication application = applicationClass.newInstance();
			
			FrameManager.setSystemLookAndFeel();
			ClientToolkit.setToolkit(new SwingClientToolkit());
			applicationContext = new SwingApplicationContext();
			application.init();
			FrameManager.getInstance().openNavigationFrame();
		} catch (Exception x) {
			x.printStackTrace();
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
			PreferencesHelper.save(Preferences.userNodeForPackage(SwingLauncher.this.getClass()), preferences);
		}

		@Override
		public void loadPreferences(Object preferences) {
			PreferencesHelper.load(Preferences.userNodeForPackage(SwingLauncher.this.getClass()), preferences);
		}
	}

	public static void main(final String[] args) throws Exception {
		applicationName = System.getProperty("MjApplication");
		if (StringUtils.isBlank(applicationName)) {
			System.err.println("Missing MjApplication parameter");
			System.exit(-1);
		}
		
		SwingUtilities.invokeAndWait(new SwingLauncher());
	}
	
}
