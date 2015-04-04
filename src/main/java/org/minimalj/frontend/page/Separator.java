package org.minimalj.frontend.page;

import org.minimalj.frontend.toolkit.Action;

public class Separator extends Action {

	@Override
	public String getName() {
		return null;
	}

	@Override
	public void action() {
		throw new IllegalStateException("Separator should not trigger action");
	}

}
