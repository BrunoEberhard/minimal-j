package org.minimalj.frontend.toolkit;

import org.minimalj.util.resources.Resources;

public abstract class ResourceAction implements IAction {

	private final String name;
	private final String description;
	
	@Override
	public boolean isEnabled() {
		return true;
	}

	protected ResourceAction() {
		this(null);
	}

	protected ResourceAction(String actionName) {
		if (actionName == null) {
			actionName = getClass().getSimpleName();
		}
		this.name = Resources.getString(actionName + ".text");
		this.description = Resources.getString(actionName + ".description");
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setChangeListener(ActionChangeListener changeListener) {
		// cannot change
	}

}
