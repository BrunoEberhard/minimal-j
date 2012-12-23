package ch.openech.mj.application;

import java.util.ResourceBundle;

import ch.openech.mj.page.ActionGroup;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.resources.Resources;

public abstract class MjApplication {

	private static MjApplication application;
	
	public static MjApplication getApplication() {
		if (application == null) {
			throw new IllegalStateException("CientApplicationConfig has to be initialized");
		}
		return application;
	}
	
	private static synchronized void setApplication(MjApplication application) {
		if (MjApplication.application != null) {
			throw new IllegalStateException("Application cannot be changed");
		}		
		if (application == null) {
			throw new IllegalArgumentException("Application cannot be null");
		}
		MjApplication.application = application;
	}
	
	protected MjApplication() {
		setApplication(this);
		Resources.addResourceBundle(getResourceBundle());
	}
	
	public abstract ResourceBundle getResourceBundle();

	public abstract String getWindowTitle(PageContext pageContext);
	
	public abstract Class<?> getPreferencesClass();
	
	public abstract Class<?>[] getSearchClasses();
	
	public void fillActionGroup(PageContext pageContext, ActionGroup actionGroup) {
		// should be done in subclass
	}
	
	public void init() {
		// should be done in subclass
	}
	
}
