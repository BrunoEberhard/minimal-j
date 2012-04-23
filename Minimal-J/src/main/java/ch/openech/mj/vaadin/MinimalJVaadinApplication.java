package ch.openech.mj.vaadin;

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
	private final ApplicationContext applicationContext = new ApplicationContext();
	
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
		ClientToolkit.setToolkit(new VaadinClientToolkit());
		Resources.addResourceBundle(ResourceBundle.getBundle("ch.openech.mj.resources.MinimalJ"));
	}

}
