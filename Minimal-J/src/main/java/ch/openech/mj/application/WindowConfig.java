package ch.openech.mj.application;

import ch.openech.mj.page.ActionGroup;
import ch.openech.mj.page.PageContext;

public interface WindowConfig {

	public String getTitle();
	
	public void fillActionGroup(PageContext pageContext, ActionGroup actionGroup);
	
	public Class<?>[] getSearchClasses();
}
