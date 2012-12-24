package ch.openech.mj.vaadin;

import java.util.Locale;
import java.util.ResourceBundle;


import ch.openech.mj.application.ApplicationContext;
import ch.openech.mj.application.MjApplication;
import ch.openech.mj.page.EmptyPage;
import ch.openech.mj.page.Page;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.util.StringUtils;
import ch.openech.mj.vaadin.toolkit.VaadinClientToolkit;

import com.vaadin.Application;

/**
 * TODO VaadinApplication should make Preferences persistent
 * 
 * @author Bruno
 *
 */
public class VaadinLauncher extends Application {
	private final ApplicationContext applicationContext = new VaadinApplicationContext();
	private static boolean applicationInitialized;
	
	@Override
	public void init() {
		initializeApplication();
		
		setTheme("openech");
		VaadinWindow mainWindow = new VaadinWindow();
		setMainWindow(mainWindow);
		mainWindow.show(Page.link(EmptyPage.class));
	}

	private synchronized void initializeApplication() {
		if (!applicationInitialized) {
			String applicationClass = getProperty("MjApplication");
			if (StringUtils.isBlank(applicationClass)) {
				throw new IllegalArgumentException("Missing MjApplication parameter");
			}
			try {
				Class<? extends MjApplication> application = (Class<? extends MjApplication>) Class.forName(applicationClass);
				application.newInstance();
			} catch (IllegalAccessException | InstantiationException | ClassNotFoundException x) {
				throw new RuntimeException(x);
			}
			applicationInitialized = true;
		}
	}

	protected ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	static {
		Locale.setDefault(Locale.GERMAN); // TODO correct setting of Locale
		ClientToolkit.setToolkit(new VaadinClientToolkit());
		Resources.addResourceBundle(ResourceBundle.getBundle("ch.openech.mj.resources.MinimalJ"));
	}

	public class VaadinApplicationContext extends ApplicationContext {
		private Object preferences;
		
		@Override
		public void setUser(String user) {
			VaadinLauncher.this.setUser(user);
		}

		@Override
		public String getUser() {
			return (String) VaadinLauncher.this.getUser();
		}

		@Override
		public void loadPreferences(Object preferences) {
			// nothing done (yet)
		}

		@Override
		public void savePreferences(Object preferences) {
			this.preferences = preferences;
		}
	}
}
