package org.minimalj.test.headless;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.minimalj.application.Application;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.ActionGroup;
import org.minimalj.frontend.action.Separator;
import org.minimalj.test.PageContainerTestFacade.NavigationTestFacade;

class HeadlessNavigationTestFacade implements NavigationTestFacade {
	private Map<String, Runnable> actions = new HashMap<>();
	
	public HeadlessNavigationTestFacade() {
		var actions = Application.getInstance().getNavigation();
		traverse(actions);
	}
	
	public HeadlessNavigationTestFacade(List<Action> actions) {
		traverse(actions);
	}

	private void traverse(List<Action> actions) {
		actions.forEach(action -> {
			if (action instanceof ActionGroup actionGroup) {
				traverse(actionGroup.getItems());
			} else if (!(action instanceof Separator)) {
				HeadlessNavigationTestFacade.this.actions.put(action.getName(), action);
			}
		});
	}
	
	@Override
	public Runnable get(String text) {
		return actions.get(text);
	}
	
	@Override
	public void run(String text) {
		if (actions.containsKey(text)) {
			actions.get(text).run();
		} else {
			throw new IllegalArgumentException("No navigation action with text " + text);
		}
	}
}