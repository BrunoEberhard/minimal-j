package org.minimalj.frontend.lanterna.toolkit;

import org.minimalj.frontend.lanterna.LanternaGUIScreen;

import com.googlecode.lanterna.gui.Action;

public class LanternaActionAdapater implements Action {

	private final LanternaGUIScreen guiScreen;
	private final org.minimalj.frontend.toolkit.Action action;
	
	public LanternaActionAdapater(LanternaGUIScreen guiScreen, org.minimalj.frontend.toolkit.Action action) {
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
