package ch.openech.mj.resources;

import javax.swing.AbstractAction;

public abstract class ResourceAction extends AbstractAction {

	protected ResourceAction() {
		String actionName = this.getClass().getSimpleName();
		ResourceHelper.initProperties(this, Resources.getResourceBundle(), actionName);
	}

	protected ResourceAction(String actionName) {
		ResourceHelper.initProperties(this, Resources.getResourceBundle(), actionName);
	}
}
