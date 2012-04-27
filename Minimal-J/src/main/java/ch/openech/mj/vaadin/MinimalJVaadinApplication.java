package ch.openech.mj.vaadin;

import java.util.Locale;
import java.util.ResourceBundle;

import ch.openech.mj.application.ApplicationContext;
import ch.openech.mj.application.EmptyPage;
import ch.openech.mj.page.Page;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.vaadin.toolkit.VaadinClientToolkit;

import com.vaadin.Application;

/**
 * TODO VaadinApplication should make Preferences persistent
 * 
 * @author Bruno
 *
 */
public class MinimalJVaadinApplication extends Application {
	private VaadinWindow mainWindow;
	private final ApplicationContext applicationContext = new VaadinAppicationContext();
	
	@Override
	public void init() {
		setTheme("openech");
		
		mainWindow = new VaadinWindow();
		setMainWindow(mainWindow);
		mainWindow.show(Page.link(EmptyPage.class));
	}

	protected ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	static {
		Locale.setDefault(Locale.GERMAN); // TODO correct setting of Locale
		ClientToolkit.setToolkit(new VaadinClientToolkit());
		Resources.addResourceBundle(ResourceBundle.getBundle("ch.openech.mj.resources.MinimalJ"));
	}

	public class VaadinAppicationContext extends ApplicationContext {
		private Object preferences;
		
		@Override
		public void setUser(String user) {
			MinimalJVaadinApplication.this.setUser(user);
		}

		@Override
		public String getUser() {
			return (String) MinimalJVaadinApplication.this.getUser();
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
