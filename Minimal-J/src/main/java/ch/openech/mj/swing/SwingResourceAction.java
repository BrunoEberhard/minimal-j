package ch.openech.mj.swing;

import javax.swing.AbstractAction;

import ch.openech.mj.resources.ResourceHelper;
import ch.openech.mj.resources.Resources;

public abstract class SwingResourceAction extends AbstractAction {
	private static final long serialVersionUID = 1L;

	protected SwingResourceAction() {
		String actionName = this.getClass().getSimpleName();
		ResourceHelper.initProperties(this, Resources.getResourceBundle(), actionName);
	}

	protected SwingResourceAction(String actionName) {
		ResourceHelper.initProperties(this, Resources.getResourceBundle(), actionName);
	}
}
