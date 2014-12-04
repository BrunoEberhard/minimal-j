package org.minimalj.frontend.lanterna;

import org.minimalj.frontend.lanterna.toolkit.LanternaClientToolkit;
import org.minimalj.frontend.lanterna.toolkit.LanternaClientToolkit.LanternaLink;
import org.minimalj.frontend.lanterna.toolkit.LanternaClientToolkit.LanternaLinkListener;
import org.minimalj.frontend.lanterna.toolkit.LanternaSwitchContent;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.PageLink;
import org.minimalj.frontend.swing.component.History;
import org.minimalj.frontend.swing.component.History.HistoryListener;

import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.Container;
import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.layout.BorderLayout;
import com.googlecode.lanterna.screen.Screen;

public class LanternaGUIScreen extends GUIScreen {

	private LanternaMenuPanel menuPanel;
	private LanternaSwitchContent switchLayout;
	
	private final Window window;
	private final History<String> history;
	private final LanternaPageContextHistoryListener historyListener;
	private final LanternaLinkListener linkListener = new LinkListener();
	
	public LanternaGUIScreen(Screen screen) {
		super(screen);
		historyListener = new LanternaPageContextHistoryListener();
		history = new History<String>(historyListener);
		
		window = new Window("");
		window.setBorder(new Border.Invisible());
	}
	
	public void init() {
		menuPanel = new LanternaMenuPanel(this);
		
		window.addComponent((Component) menuPanel);

		switchLayout = new LanternaSwitchContent();
		menuPanel.addComponent((Component) switchLayout, BorderLayout.CENTER);

		history.add(PageLink.DEFAULT);

		showWindow(window, Position.FULL_SCREEN);
	}
	
	private class LanternaPageContextHistoryListener implements HistoryListener {
		@Override
		public void onHistoryChanged() {
			Page page = PageLink.createPage(history.getPresent());
			show(page);
			menuPanel.updateMenu(page);
		}

		private void show(Page page) {
			switchLayout.show((Component) page.getContent());
			registerLinkListener((Component) page.getContent());
			// ClientToolkit.getToolkit().focusFirstComponent(page.getComponent());
		}
	}
	
	private class LinkListener implements LanternaClientToolkit.LanternaLinkListener {

		@Override
		public void action(String address) {
			show(address);
		}
		
	}
	
	private void registerLinkListener(Component component) {
		if (component instanceof LanternaLink) {
			 ((LanternaLink) component).setListener(linkListener);
		}
		if (component instanceof Container) {
			Container container = (Container) component;
			for (int i = 0; i<container.getComponentCount(); i++) {
				registerLinkListener(container.getComponentAt(i));
			}
		}
	}

	public void show(String pageLink) {
		history.add(pageLink);
	}
}