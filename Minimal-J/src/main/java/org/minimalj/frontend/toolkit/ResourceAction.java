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
		this.name = actionName == null ? Resources.getString(getClass()) : Resources.getString(actionName);
		this.description = Resources.getString(actionName + ".description", Resources.OPTIONAL);
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
