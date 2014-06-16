package org.minimalj.frontend.vaadin;

import java.io.Serializable;
import java.util.Enumeration;
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
 * Note: this class extends Vaadin - Application not the
 * MjApplication
 */
public class VaadinFrontend extends Application {
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
			String applicationName = getProperty("MjApplication");
			if (StringUtils.isBlank(applicationName)) {
				throw new IllegalArgumentException("Missing MjApplication parameter");
			}
			MjApplication application = MjApplication.createApplication(applicationName);
			MjApplication.setApplication(application);
			copyPropertiesFromWebContextToSystem();
			applicationInitialized = true;
		}
	}
	
	private void copyPropertiesFromWebContextToSystem() {
		Enumeration<?> propertyNames = getPropertyNames();
		while (propertyNames.hasMoreElements()) {
			String propertyName = (String) propertyNames.nextElement();
			System.setProperty(propertyName, getProperty(propertyName));
		}
	}	

	protected ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	static {
		Locale.setDefault(Locale.GERMAN); // TODO correct setting of Locale
		ClientToolkit.setToolkit(new VaadinClientToolkit());
		Resources.addResourceBundle(ResourceBundle.getBundle("org.minimalj.util.resources.MinimalJ"));
	}

	public class VaadinApplicationContext extends ApplicationContext implements Serializable {
		private static final long serialVersionUID = 1L;
		private Object preferences;
		
		@Override
		public void setUser(String user) {
			VaadinFrontend.this.setUser(user);
		}

		@Override
		public String getUser() {
			return (String) VaadinFrontend.this.getUser();
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
