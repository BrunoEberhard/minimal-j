package org.minimalj.frontend.action;

public final class Separator extends Action {

	public Separator() {
		super(null);
	}
	
	@Override
	public void run() {
		throw new IllegalStateException("Separator should not trigger action");
	}

}
