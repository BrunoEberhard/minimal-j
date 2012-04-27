package ch.openech.mj.application;

import java.util.ResourceBundle;

import ch.openech.mj.page.ActionGroup;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.resources.Resources;

public abstract class ApplicationConfig {

	private static ApplicationConfig applicationConfig;
	
	public static ApplicationConfig getApplicationConfig() {
		if (applicationConfig == null) {
			throw new IllegalStateException("CientApplicationConfig has to be initialized");
		}
		return applicationConfig;
	}

	public static synchronized void setApplicationConfig(ApplicationConfig applicationConfig) {
		if (ApplicationConfig.applicationConfig != null) {
			throw new IllegalStateException("CientApplicationConfig cannot be changed");
		}		
		if (applicationConfig == null) {
			throw new IllegalArgumentException("CientApplicationConfig cannot be null");
		}
		ApplicationConfig.applicationConfig = applicationConfig;
	}
	
	public ApplicationConfig() {
		Resources.addResourceBundle(getResourceBundle());
	}
	
	public abstract ResourceBundle getResourceBundle();

	public abstract String getWindowTitle(PageContext pageContext);
	
	public abstract Class<?> getPreferencesClass();
	
	public abstract Class<?>[] getSearchClasses();
	
	public void fillActionGroup(PageContext pageContext, ActionGroup actionGroup) {
		// should be done in subclass
	}

}
