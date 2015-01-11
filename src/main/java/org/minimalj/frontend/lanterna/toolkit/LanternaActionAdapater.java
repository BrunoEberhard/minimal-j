package org.minimalj.frontend.lanterna.toolkit;

import org.minimalj.frontend.lanterna.LanternaGUIScreen;
import org.minimalj.frontend.toolkit.IAction;

import com.googlecode.lanterna.gui.Action;

public class LanternaActionAdapater implements Action {

	private final LanternaGUIScreen guiScreen;
	private final IAction action;
	
	public LanternaActionAdapater(LanternaGUIScreen guiScreen, IAction action) {
		this.guiScreen = guiScreen;
		this.action = action;
	}

	@Override
	public void doAction() {
		LanternaClientToolkit.setGui(guiScreen);
		action.action();
		LanternaClientToolkit.setGui(null);
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
