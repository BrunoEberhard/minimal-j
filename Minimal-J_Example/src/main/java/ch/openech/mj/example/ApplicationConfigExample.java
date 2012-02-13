package ch.openech.mj.example;

import java.util.ResourceBundle;

import ch.openech.mj.application.ApplicationConfig;
import ch.openech.mj.application.WindowConfig;
import ch.openech.mj.page.ActionGroup;
import ch.openech.mj.page.PageContext;

public class ApplicationConfigExample extends ApplicationConfig {

	@Override
	public ResourceBundle getResourceBundle() {
		return ResourceBundle.getBundle("ch.openech.mj.example.Application");
	}

	@Override
	public WindowConfig getInitialWindowConfig() {
		return new WindowConfigExample();
	}
	
	@Override
	public void fillActionGroup(PageContext pageContext, ActionGroup actionGroup) {
		// no application wide actions
	}
	
}
