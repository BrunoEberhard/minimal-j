package org.minimalj.frontend.lanterna;

import org.minimalj.application.Application;
import org.minimalj.frontend.lanterna.toolkit.LanternaSwitchContent;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.swing.component.History;
import org.minimalj.frontend.swing.component.History.HistoryListener;

import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.layout.BorderLayout;
import com.googlecode.lanterna.screen.Screen;

public class LanternaGUIScreen extends GUIScreen {

	private LanternaMenuPanel menuPanel;
	private LanternaSwitchContent switchLayout;
	
	private final Window window;
	private final History<Page> history;
	private final LanternaPageContextHistoryListener historyListener;
	
	public LanternaGUIScreen(Screen screen) {
		super(screen);
		historyListener = new LanternaPageContextHistoryListener();
		history = new History<>(historyListener);
		
		window = new Window("");
		window.setBorder(new Border.Invisible());
	}
	
	public void init() {
		menuPanel = new LanternaMenuPanel(this);
		
		window.addComponent((Component) menuPanel);

		switchLayout = new LanternaSwitchContent();
		menuPanel.addComponent((Component) switchLayout, BorderLayout.CENTER);

		history.add(Application.getApplication().createDefaultPage());

		showWindow(window, Position.FULL_SCREEN);
	}
	
	private class LanternaPageContextHistoryListener implements HistoryListener {
		@Override
		public void onHistoryChanged() {
			Page page = history.getPresent();
			show(page);
			menuPanel.updateMenu(page);
		}

		private void show(Page page) {
			switchLayout.show((Component) page.getContent());
			// ClientToolkit.getToolkit().focusFirstComponent(page.getComponent());
		}
	}
	
	public void show(Page page) {
		history.add(page);
	}
}