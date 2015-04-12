package org.minimalj.frontend.json;

import org.minimalj.frontend.toolkit.Action;
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
	}

	public void action() {
		action.action();
	}
	
	public static class JsonActionGroup extends JsonComponent {
		private static final long serialVersionUID = 1L;
		
		public JsonActionGroup() {
			super("ActionGroup", false);
		}
	}
	
}
