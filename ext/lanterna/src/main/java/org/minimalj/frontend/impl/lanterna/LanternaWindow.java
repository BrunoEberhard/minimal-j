package org.minimalj.frontend.impl.lanterna;

import java.util.Arrays;

import org.minimalj.application.Application;
import org.minimalj.frontend.impl.lanterna.toolkit.LanternaSwitchContent;
import org.minimalj.frontend.impl.util.History;
import org.minimalj.frontend.impl.util.History.HistoryListener;
import org.minimalj.frontend.page.Page;

import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.BorderLayout.Location;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;

public class LanternaWindow extends BasicWindow {

	private final Panel content = new Panel(new BorderLayout());
	private final LanternaMenuPanel menuPanel;
	private final LanternaSwitchContent switchLayout;
	
	private final History<Page> history;
	private final LanternaPageContextHistoryListener historyListener;
	
	public LanternaWindow() {
		setHints(Arrays.asList(Window.Hint.FULL_SCREEN, Window.Hint.NO_DECORATIONS));
		
		historyListener = new LanternaPageContextHistoryListener();
		history = new History<>(historyListener);

		menuPanel = new LanternaMenuPanel();
		
		content.addComponent(menuPanel, Location.TOP);
		
		switchLayout = new LanternaSwitchContent();
		content.addComponent((Component) switchLayout, Location.CENTER);
		
		history.add(Application.getInstance().createDefaultPage());

		setComponent(content);
	}
	
	void show(Page page) {
		history.add(page);
	}
	
	private class LanternaPageContextHistoryListener implements HistoryListener {
		@Override
		public void onHistoryChanged() {
			Page page = history.getPresent();
			show(page);
			menuPanel.updateMenu(page);
		}

		private void show(Page page) {
			Component pageContent = (Component) page.getContent();
			//if (pageContent instanceof Table) {
				//switchLayout.show(new LanternaTableContainer((Table<?>) pageContent));
					//} else {
				switchLayout.show(pageContent);
			// }
			// Frontend.focusFirstComponent(page.getComponent());
		}
	}

}