package org.minimalj.frontend.lanterna.toolkit;

import org.minimalj.frontend.toolkit.IAction;
import org.minimalj.frontend.toolkit.IComponent;

import com.googlecode.lanterna.gui.Action;

public class LanternaActionAdapater implements Action {

	private final IAction action;
	private final IComponent source;
	
	public LanternaActionAdapater(IAction action, IComponent source) {
		this.action = action;
		this.source = source;
	}

	@Override
	public void doAction() {
		action.action(source);
	}
	
	public String getText() {
		return action.getName();
	}

	@Override
	public String toString() {
		String text = getText();
		return text != null ? text : "";
	}
	
}
