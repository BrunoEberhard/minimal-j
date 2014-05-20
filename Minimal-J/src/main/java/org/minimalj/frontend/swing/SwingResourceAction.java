package org.minimalj.frontend.swing;

import javax.swing.AbstractAction;

import org.minimalj.util.resources.ResourceHelper;
import org.minimalj.util.resources.Resources;

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
