package org.minimalj.frontend.lanterna.toolkit;

import org.minimalj.frontend.toolkit.ClientToolkit.IContext;
import org.minimalj.frontend.toolkit.IAction;

import com.googlecode.lanterna.gui.Action;

public class LanternaActionAdapater implements Action {

	private final IAction action;
	private final IContext context;
	
	public LanternaActionAdapater(IAction action, IContext context) {
		this.action = action;
		this.context = context;
	}

	@Override
	public void doAction() {
		action.action(context);
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
