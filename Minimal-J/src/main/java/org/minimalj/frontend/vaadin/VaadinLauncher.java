package org.minimalj.frontend.vaadin;

import java.util.Locale;
import java.util.ResourceBundle;

import org.minimalj.application.ApplicationContext;
import org.minimalj.application.MjApplication;
import org.minimalj.frontend.page.EmptyPage;
import org.minimalj.frontend.page.PageLink;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.vaadin.toolkit.VaadinClientToolkit;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

import com.vaadin.Application;

/**
 * TODO VaadinApplication should make Preferences persistent
 * 
 * @author Bruno
 *
 */
public class VaadinLauncher extends Application {
	private static final long serialVersionUID = 1L;
	
	private final ApplicationContext applicationContext = new VaadinApplicationContext();
	private static boolean applicationInitialized;
	
	@Override
	public void init() {
		initializeApplication();
		
		setTheme("openech");
		VaadinWindow mainWindow = new VaadinWindow(applicationContext);
		setMainWindow(mainWindow);
		mainWindow.show(PageLink.link(EmptyPage.class));
	}

	private synchronized void initializeApplication() {
		if (!applicationInitialized) {
			String applicationClassName = getProperty("MjApplication");
			if (StringUtils.isBlank(applicationClassName)) {
				throw new IllegalArgumentException("Missing MjApplication parameter");
			}
			try {
				@SuppressWarnings("unchecked")
				Class<? extends MjApplication> applicationClass = (Class<? extends MjApplication>) Class.forName(applicationClassName);
				MjApplication application = applicationClass.newInstance();
				application.init();
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
