package org.minimalj.frontend.impl.lanterna.toolkit;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.lanterna.LanternaGUIScreen;
import org.minimalj.frontend.page.IDialog;

import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.listener.WindowAdapter;

public class LanternaDialog implements IDialog {

	private final Window window;
	
	public LanternaDialog(LanternaGUIScreen screen, IContent content, String title, final Action closeAction, Action[] actions) {
		Component component = new LanternaEditorLayout(screen, content, actions);

		window = new Window(title);
		window.addComponent(component);
		window.addWindowListener(new WindowAdapter() {
			@Override
			public void onWindowClosed(Window window) {
				if (closeAction != null) {
					closeAction.action();
				} else {
					closeDialog();
				}
			}
		});
		
		((LanternaGUIScreen) Frontend.getBrowser()).show(window);
	}
	
	@Override
	public void closeDialog() {
		window.close();
	}

}
