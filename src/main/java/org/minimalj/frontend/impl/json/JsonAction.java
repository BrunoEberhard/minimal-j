package org.minimalj.frontend.impl.json;

import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.Action.ActionChangeListener;
import org.minimalj.frontend.action.Action.ValidationAwareAction;
import org.minimalj.frontend.page.Routing;
import org.minimalj.util.StringUtils;

public class JsonAction extends JsonComponent {
	private final Runnable action;
	
	public JsonAction(Action action) {
		super("Action");
		this.action = action;
		put("name", action.getName());
		String description = action.getDescription();
		if (!StringUtils.isEmpty(description)) {
			put("description", description);
		}
		updateEnabled(action);
		action.setChangeListener(new JsonActionChangeListener());
		String route = Routing.getRouteSafe(action);
		if (route != null) {
			put("link", route);
		}
	}
	
	private void updateEnabled(Action action) {
		if (action instanceof ValidationAwareAction) {
			put("enabled", true);
			ValidationAwareAction validationAwareAction = (ValidationAwareAction) action;
			JsonFormContent formContent = (JsonFormContent) validationAwareAction.getForm().getContent();
			put("form", formContent.getId());
		} else {
			put("enabled", action.isEnabled());
		}
	}
	
	public JsonAction(Runnable runnable, String name) {
		super("Action");
		this.action = runnable;
		put("name", name);
		put("enabled", true);
	}

	public void run() {
		// The user should not be able to execute action if it is disabled.
		// Still he could manipulate the DOM to reactivate the action. Check here again.
		boolean enabled = action instanceof Action ? ((Action) action).isEnabled() : true;
		if (enabled) {
			action.run();
		}
	}
	
	public static class JsonActionGroup extends JsonComponent {
		public JsonActionGroup() {
			super("ActionGroup", false);
		}
	}
	
	private class JsonActionChangeListener implements ActionChangeListener {

		@Override
		public void change() {
			updateEnabled((Action) action);
		}
	}
	
}
