package org.minimalj.frontend.impl.lanterna;

import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.lanterna.toolkit.LanternaDialog;
import org.minimalj.frontend.impl.lanterna.toolkit.LanternaSwitchContent;
import org.minimalj.frontend.impl.util.History;
import org.minimalj.frontend.impl.util.History.HistoryListener;
import org.minimalj.frontend.page.IDialog;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.PageBrowser;

import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.dialog.MessageBox;
import com.googlecode.lanterna.gui.layout.BorderLayout;
import com.googlecode.lanterna.screen.Screen;

public class LanternaGUIScreen extends GUIScreen implements PageBrowser {

	private LanternaMenuPanel menuPanel;
	private LanternaSwitchContent switchLayout;
	
	private final Window window;
	private final History<Page> history;
	private final LanternaPageContextHistoryListener historyListener;
	
	private Window windowToOpen = null;
	
	public LanternaGUIScreen(Screen screen) {
		super(screen);
		historyListener = new LanternaPageContextHistoryListener();
		history = new History<>(historyListener);
		
		window = new Window("");
		window.setBorder(new Border.Invisible());
	}
	
	public void init() {
		menuPanel = new LanternaMenuPanel(this);
		
		window.addComponent(menuPanel);

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
			// Frontend.focusFirstComponent(page.getComponent());
		}
	}
	
	@Override
	public void show(Page page) {
		history.add(page);
	}

	@Override
	public void showError(String text) {
		MessageBox.showMessageBox(this, "Error", text);
	}

	@Override
	public void showMessage(String text) {
		MessageBox.showMessageBox(this, "Message", text);

	}

	@Override
	public <T> IDialog showSearchDialog(Search<T> index, Object[] keys, TableActionListener<T> listener) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDialog showDialog(String title, IContent content, Action closeAction, Action saveAction, Action... actions) {
		// TODO use saveAction (Enter in TextFields should save the dialog)
		return new LanternaDialog(this, content, title, closeAction, actions);
	}

	/* as showWindow is blocking the call has to be deferred on actionComplete */
	public void show(Window window) {
		windowToOpen = window;
	}

	public void actionComplete() {
		Window w = windowToOpen;
		windowToOpen = null;
		if (w != null) {
			showWindow(w);
		}
	}
}