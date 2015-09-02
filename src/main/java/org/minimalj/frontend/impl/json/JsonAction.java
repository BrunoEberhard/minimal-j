package org.minimalj.frontend.impl.json;

import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.Action.ActionChangeListener;
import org.minimalj.util.StringUtils;

public class JsonAction extends JsonComponent {
	private static final long serialVersionUID = 1L;
	private final Action action;
	
	public JsonAction(Action action) {
		super("Action");
		this.action = action;
		put("name", action.getName());
		if (!StringUtils.isEmpty(action.getDescription())) {
			put("description", action.getDescription());
		}
		put("enabled", action.isEnabled());
		action.setChangeListener(new JsonActionChangeListener());
	}

	public void action() {
		// The user should not be able to execute action if it is disabled.
		// Still he could manipulate the DOM to reactive the action. Check here again.
		if (Boolean.TRUE.equals(get("enabled"))) {
			action.action();
		}
	}
	
	public static class JsonActionGroup extends JsonComponent {
		private static final long serialVersionUID = 1L;
		
		public JsonActionGroup() {
			super("ActionGroup", false);
		}
	}
	
	private class JsonActionChangeListener implements ActionChangeListener {

		@Override
		public void change() {
			put("enabled", action.isEnabled());
		}
	}
	
}
