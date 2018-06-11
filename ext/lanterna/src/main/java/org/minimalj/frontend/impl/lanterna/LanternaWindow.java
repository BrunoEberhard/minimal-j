package org.minimalj.frontend.impl.lanterna;

import java.util.Arrays;

import org.minimalj.application.Application;
import org.minimalj.frontend.impl.lanterna.toolkit.LanternaSwitch;
import org.minimalj.frontend.impl.util.History;
import org.minimalj.frontend.impl.util.History.HistoryListener;
import org.minimalj.frontend.impl.util.PageAccess;
import org.minimalj.frontend.page.Page;

import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.BorderLayout.Location;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Container;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;

public class LanternaWindow extends BasicWindow {

	private final Panel content = new Panel(new BorderLayout());
	private final LanternaMenuPanel menuPanel;
	private final LanternaSwitch switchLayout;
	
	private final History<Page> history;
	private final LanternaPageContextHistoryListener historyListener;
	
	public LanternaWindow() {
		setHints(Arrays.asList(Window.Hint.FULL_SCREEN, Window.Hint.NO_DECORATIONS));
		
		historyListener = new LanternaPageContextHistoryListener();
		history = new History<>(historyListener);

		menuPanel = new LanternaMenuPanel();
		
		content.addComponent(menuPanel, Location.TOP);
		
		switchLayout = new LanternaSwitch();
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
			Component pageContent = (Component) PageAccess.getContent(page);
			switchLayout.show(pageContent);
			focusFirstInteractable(LanternaWindow.this.getComponent());
			focusFirstInteractable(pageContent);
		}
	}
	
	private void focusFirstInteractable(Component component) {
		Interactable interactable = getFirstInteractable(component);
		if (interactable != null) {
			interactable.takeFocus();
			return;
		}
	}

	private Interactable getFirstInteractable(Component component) {
		if (component instanceof Interactable) {
			return (Interactable) component;
		} else if (component instanceof Container) {
			Container container = (Container) component;
			for (Component c : container.getChildren()) {
				Interactable interactable = getFirstInteractable(c);
				if (interactable != null) {
					return interactable;
				}
			}
		}
		return null;
	}

}