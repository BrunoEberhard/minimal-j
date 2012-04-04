package ch.openech.mj.vaadin;

import java.util.ResourceBundle;

import ch.openech.mj.resources.Resources;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.vaadin.toolkit.VaadinClientToolkit;

import com.vaadin.Application;

/**
 * TODO VaadinApplication should implement HttpServletRequestListener to make Preferences persistent
 * 
 * @author Bruno
 *
 */
public class MinimalJVaadinApplication extends Application {
	private VaadinWindow mainWindow;

	@Override
	public void init() {
		setTheme("openech");

		mainWindow = new VaadinWindow();
		// TODO Preferences in MinimalJVaadinApplication
		// mainWindow.setPreferences(Preferences.userNodeForPackage(MinimalJVaadinApplication.class));
		setMainWindow(mainWindow);
	}

	static {
		ClientToolkit.setToolkit(new VaadinClientToolkit());
		Resources.addResourceBundle(ResourceBundle.getBundle("ch.openech.mj.resources.MinimalJ"));
	}
	
}
