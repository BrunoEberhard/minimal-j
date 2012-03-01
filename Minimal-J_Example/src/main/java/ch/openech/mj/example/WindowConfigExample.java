package ch.openech.mj.example;

import ch.openech.mj.application.WindowConfig;
import ch.openech.mj.edit.EditorDialogAction;
import ch.openech.mj.page.ActionGroup;
import ch.openech.mj.page.PageContext;

public class WindowConfigExample implements WindowConfig {

	public WindowConfigExample () {
	}
	
	@Override
	public String getTitle() {
		return "Minimal-J Example Application";
	}
	
	@Override
	public void fillActionGroup(PageContext pageContext, ActionGroup actionGroup) {
		ActionGroup create = actionGroup.getOrCreateActionGroup(ActionGroup.NEW);
		create.add(new EditorDialogAction(new AddBookEditor()));
	}
	
	@Override
	public Class<?>[] getSearchClasses() {
		return new Class<?>[]{BookTablePage.class};
	}
	
}
