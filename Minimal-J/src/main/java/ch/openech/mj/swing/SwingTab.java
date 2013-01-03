package ch.openech.mj.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ch.openech.mj.application.ApplicationContext;
import ch.openech.mj.page.Page;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.page.RefreshablePage;
import ch.openech.mj.resources.ResourceAction;
import ch.openech.mj.swing.component.EditablePanel;
import ch.openech.mj.swing.component.History;
import ch.openech.mj.swing.component.History.HistoryListener;
import ch.openech.mj.swing.toolkit.SwingSwitchLayout;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;

public class SwingTab extends EditablePanel implements IComponent, PageContext {
	final SwingFrame frame;
	final Action previousAction, nextAction, refreshAction, upAction, downAction;
	final Action closeTabAction;
	final JMenuItem menuItemToolBarVisible;

	private final SwingToolBar toolBar;
	private final SwingMenuBar menuBar;
	private final SwingSwitchLayout switchLayout;
	
	private final History<String> history;
	private final SwingPageContextHistoryListener historyListener;

	private Page page;
	private List<String> pageLinks;
	private int indexInPageLinks;

	public SwingTab(SwingFrame frame) {
		super();
		this.frame = frame;

		historyListener = new SwingPageContextHistoryListener();
		history = new History<String>(historyListener);

		previousAction = new PreviousPageAction();
		nextAction = new NextPageAction();
		refreshAction = new RefreshAction();
		upAction = new UpAction();
		downAction = new DownAction();

		closeTabAction = new CloseTabAction();
		
		toolBar = new SwingToolBar(this);
		menuBar = new SwingMenuBar(this);

		menuItemToolBarVisible = new MenuItemToolBarVisible();
		
		JPanel outerPanel = new JPanel(new BorderLayout());
		outerPanel.add(menuBar, BorderLayout.NORTH);
		JPanel panel = new JPanel(new BorderLayout());
		outerPanel.add(panel, BorderLayout.CENTER);
		panel.add(toolBar, BorderLayout.NORTH);
		switchLayout = new SwingSwitchLayout();
		panel.add(switchLayout, BorderLayout.CENTER);
		setContent(outerPanel);
	}
	
	public Page getVisiblePage() {
		return page;
	}
	
	void onHistoryChanged() {
		updateActions();
		menuBar.onHistoryChanged();
		toolBar.onHistoryChanged();
		frame.onHistoryChanged();
	}

	protected void updateActions() {
		if (getVisiblePage() != null && !getVisiblePage().isExclusive()) {
			previousAction.setEnabled(hasPast());
			nextAction.setEnabled(hasFuture());
			refreshAction.setEnabled(getVisiblePage() instanceof RefreshablePage);
			upAction.setEnabled(!top());
			downAction.setEnabled(!bottom());
		} else {
			previousAction.setEnabled(false);
			nextAction.setEnabled(false);
			refreshAction.setEnabled(false);
			upAction.setEnabled(false);
			downAction.setEnabled(false);
		}
	}

	//
	
	protected class PreviousPageAction extends ResourceAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			previous();
		}
	}
	
	protected class NextPageAction extends ResourceAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			next();
		}
	}

	protected class RefreshAction extends ResourceAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			refresh();
		}
	}
	
	public void refresh() {
		if (getVisiblePage() instanceof RefreshablePage) {
			((RefreshablePage)getVisiblePage()).refresh();
		}
	}

	private class UpAction extends ResourceAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			up();
		}
	}

	private class DownAction extends ResourceAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			down();
		}
	}

	private class CloseTabAction extends ResourceAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			frame.closeTab();
		}
	}
	
	private class MenuItemToolBarVisible extends JCheckBoxMenuItem implements ItemListener {
		private final Preferences preferences =  Preferences.userNodeForPackage(MenuItemToolBarVisible.class).node(MenuItemToolBarVisible.class.getSimpleName());
		
		public MenuItemToolBarVisible() {
			super("Navigation sichtbar");
			addItemListener(this);
			setSelected(preferences.getBoolean("visible", true));
			toolBar.setVisible(isSelected());
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			toolBar.setVisible(isSelected());
			preferences.putBoolean("visible", isSelected());
		}
	}
	
	// PageContext
	
	private class SwingPageContextHistoryListener implements HistoryListener {
		@Override
		public void onHistoryChanged() {
			page = Page.createPage(SwingTab.this, history.getPresent());
			show(page);
			SwingTab.this.onHistoryChanged();
		}

		private void show(Page page) {
			switchLayout.show((IComponent) page.getComponent());
			ClientToolkit.getToolkit().focusFirstComponent(page.getComponent());
		}
	}

	public void add(String pageLink) {
		history.add(pageLink);
	}

	public void replace(String pageLink) {
		history.replace(pageLink);
	}

	public String getPresent() {
		return history.getPresent();
	}

	public boolean hasFuture() {
		return history.hasFuture();
	}

	public boolean hasPast() {
		return history.hasPast();
	}

	public void next() {
		history.next();
	}

	public void previous() {
		history.previous();
	}

	public void dropFuture() {
		history.dropFuture();
	}

	@Override
	public PageContext addTab() {
		return frame.addTab();
	}

	@Override
	public void closeTab() {
		if (hasPast()) {
			previous();
			dropFuture();
		} else {
			frame.closeTab(this);
		}
	}

	@Override
	public void show(final String pageLink) {
		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						show(pageLink);
					};
				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		} else {
			if (pageLinks != null && !pageLinks.contains(pageLink)) {
				pageLinks = null;
			}
			if (page != null && page.isExclusive()) {
				PageContext newPageContext = addTab();
				newPageContext.show(pageLink);
			} else {
				add(pageLink);
			}
		}
	}

	@Override
	public void show(List<String> pageLinks, int index) {
		this.pageLinks = pageLinks;
		this.indexInPageLinks = index;
		show(pageLinks.get(indexInPageLinks));
	}

	public boolean top() {
		return pageLinks == null || indexInPageLinks == 0;
	}

	public boolean bottom() {
		return pageLinks == null || indexInPageLinks == pageLinks.size() - 1;
	}

	public void up() {
		replace(pageLinks.get(--indexInPageLinks));
	}

	public void down() {
		replace(pageLinks.get(++indexInPageLinks));
	}

	@Override
	public ApplicationContext getApplicationContext() {
		return SwingLauncher.getApplicationContext();
	}

	
}
