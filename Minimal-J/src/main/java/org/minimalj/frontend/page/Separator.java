package org.minimalj.frontend.page;

import org.minimalj.frontend.toolkit.ClientToolkit.IContext;
import org.minimalj.frontend.toolkit.IAction;

public class Separator implements IAction {

	@Override
	public String getName() {
		return null;
	}

	@Override
	public void action(IContext context) {
		// n/a
	}

	@Override
	public boolean isEnabled() {
		// n/a
		return true;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public void setChangeListener(ActionChangeListener changeListener) {
		// n/a
	}

}
