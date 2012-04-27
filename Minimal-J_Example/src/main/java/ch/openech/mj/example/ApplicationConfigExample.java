package ch.openech.mj.example;

import java.util.ResourceBundle;

import ch.openech.mj.application.ApplicationConfig;
import ch.openech.mj.edit.EditorDialogAction;
import ch.openech.mj.page.ActionGroup;
import ch.openech.mj.page.PageContext;

public class ApplicationConfigExample extends ApplicationConfig {

	@Override
	public ResourceBundle getResourceBundle() {
		return ResourceBundle.getBundle("ch.openech.mj.example.Application");
	}

	@Override
	public void fillActionGroup(PageContext pageContext, ActionGroup actionGroup) {
		ActionGroup create = actionGroup.getOrCreateActionGroup(ActionGroup.NEW);
		create.add(new EditorDialogAction(new AddBookEditor()));
	}

	@Override
	public String getWindowTitle(PageContext pageContext) {
		return "Minimal-J Example Application";
	}

	@Override
	public Class<?>[] getSearchClasses() {
		return new Class<?>[]{BookTablePage.class};
	}

	@Override
	public Class<?> getPreferencesClass() {
		return null;
	}
	
}
