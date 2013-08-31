package ch.openech.mj.page;

import ch.openech.mj.toolkit.IAction;
import ch.openech.mj.toolkit.IComponent;

public class Separator implements IAction {

	@Override
	public String getName() {
		return null;
	}

	@Override
	public void action(IComponent context) {
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
