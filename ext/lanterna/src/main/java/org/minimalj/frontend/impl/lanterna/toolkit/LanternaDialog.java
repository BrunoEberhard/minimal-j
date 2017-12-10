package org.minimalj.frontend.impl.lanterna.toolkit;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.IDialog;

import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;

public class LanternaDialog implements IDialog {

	private final BasicWindow window;
	
	public LanternaDialog(MultiWindowTextGUI gui, IContent content, String title, final Action closeAction, Action[] actions) {
		Component component = new LanternaEditorLayout(content, actions);

		window = new BasicWindow(title);
		window.setComponent(component);
		
		gui.addWindow(window);
	}
	
	@Override
	public void closeDialog() {
		window.close();
	}

}
