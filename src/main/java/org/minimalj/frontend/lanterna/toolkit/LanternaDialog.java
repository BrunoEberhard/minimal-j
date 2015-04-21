package org.minimalj.frontend.lanterna.toolkit;

import org.minimalj.frontend.lanterna.LanternaGUIScreen;
import org.minimalj.frontend.toolkit.Action;
import org.minimalj.frontend.toolkit.ClientToolkit.IContent;
import org.minimalj.frontend.toolkit.IDialog;

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
		screen.showWindow(window);
	}
	
	@Override
	public void closeDialog() {
		window.close();
	}

}
