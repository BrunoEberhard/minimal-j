package org.minimalj.frontend.lanterna.toolkit;

import org.minimalj.frontend.toolkit.IAction;
import org.minimalj.frontend.toolkit.IComponent;
import org.minimalj.frontend.toolkit.IDialog;

import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.listener.WindowAdapter;

public class LanternaDialog implements IDialog {

	private final GUIScreen screen;
	private final Component content;
	private final String title;
	private Window window;
	
	private CloseListener closeListener;
	
	public LanternaDialog(GUIScreen screen, IComponent content, String title, IAction[] actions) {
		this.screen = screen;
		this.content = new LanternaEditorLayout(content, actions);
		this.title = title;
	}
	
	@Override
	public void setCloseListener(CloseListener closeListener) {
		this.closeListener = closeListener;
	}

	@Override
	public void openDialog() {
		window = new Window(title);
		window.addComponent(content);
		window.addWindowListener(new WindowAdapter() {
			@Override
			public void onWindowClosed(Window window) {
				if (closeListener == null || closeListener.close()) {
					closeListener.close();
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
