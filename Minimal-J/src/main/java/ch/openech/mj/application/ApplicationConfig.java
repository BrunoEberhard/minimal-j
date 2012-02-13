package ch.openech.mj.application;

import java.util.ResourceBundle;

import ch.openech.mj.edit.Editor;
import ch.openech.mj.edit.EditorPage;
import ch.openech.mj.page.ActionGroup;
import ch.openech.mj.page.Page;
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

	public abstract WindowConfig getInitialWindowConfig();

	public WindowConfig[] getWindowConfigs() {
		return new WindowConfig[]{getInitialWindowConfig()};
	}

	public void fillActionGroup(PageContext pageContext, ActionGroup actionGroup) {
		// should be done in subclass
	}
	
	public Page getPage(String... uriFragment) {
		if (uriFragment.length == 2) {
			String type = uriFragment[0];
			String className = uriFragment[1];
			
			if ("edit".equals(type)) {
				try {
					Class<?> clazz = Class.forName(className);
					Editor<?> editor = (Editor<?>) clazz.newInstance();
					return new EditorPage(editor);
				} catch (Exception x) {
					throw new RuntimeException(x);
				}
			} else if ("view".equals(type)) {
//			try {
//				Class<?> clazz = Class.forName(className);
//				Editor<?> editor = (Editor<?>) clazz.newInstance();
//				return new EditorPage(editor);
//			} catch (Exception x) {
//				throw new RuntimeException(x);
//			}
			}
		}
		return null;
	}

}
