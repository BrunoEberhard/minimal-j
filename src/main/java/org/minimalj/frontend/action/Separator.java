package org.minimalj.frontend.action;

public class Separator extends Action {

	public Separator() {
		super(null);
	}
	
	@Override
	public void action() {
		throw new IllegalStateException("Separator should not trigger action");
	}

}
