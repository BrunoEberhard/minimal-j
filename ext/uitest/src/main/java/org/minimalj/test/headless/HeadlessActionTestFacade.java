package org.minimalj.test.headless;

import org.minimalj.frontend.action.Action;
import org.minimalj.test.PageContainerTestFacade.ActionTestFacade;

public class HeadlessActionTestFacade implements ActionTestFacade {
	private final Action action;

	public HeadlessActionTestFacade(Action action) {
		this.action = action;
	}

	@Override
	public void run() {
		action.run();
	}

	@Override
	public boolean isEnabled() {
		return action.isEnabled();
	}
}
