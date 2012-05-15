package ch.openech.mj.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import ch.openech.mj.page.Page;
import ch.openech.mj.page.RefreshablePage;
import ch.openech.mj.resources.ResourceAction;
import ch.openech.mj.swing.component.EditablePanel;
import ch.openech.mj.toolkit.IComponent;

public class SwingTab extends EditablePanel implements IComponent {
	final SwingFrame frame;
	final SwingPageContext pageContext;
	final Action previousAction, nextAction, refreshAction, upAction, downAction;
	final Action closeTabAction;
	final JMenuItem menuItemToolBarVisible;

	private final SwingToolBar toolBar;
	private final SwingMenuBar menuBar;

	public SwingTab(SwingFrame frame) {
		super();
		this.pageContext = new SwingPageContext(this);
		this.frame = frame;

		previousAction = new PreviousPageAction();
		nextAction = new NextPageAction();
		refreshAction = new RefreshAction();
		upAction = new UpAction();
		downAction = new DownAction();

		closeTabAction = new CloseTabAction();
		
		toolBar = new SwingToolBar(this);
		menuBar = new SwingMenuBar(this);

		menuItemToolBarVisible = new MenuItemToolBarVisible();
		
		createContent();
	}

	protected void createContent() {
		JPanel outerPanel = new JPanel(new BorderLayout());
		outerPanel.add(menuBar, BorderLayout.NORTH);
		JPanel panel = new JPanel(new BorderLayout());
		outerPanel.add(panel, BorderLayout.CENTER);
		panel.add(toolBar, BorderLayout.NORTH);
		panel.add(pageContext, BorderLayout.CENTER);
		setContent(outerPanel);
	}

	
	public SwingPageContext getPageContext() {
		return pageContext;
	}
	
	public Page getVisiblePage() {
		return pageContext.getPresent();
	}
	
	void onHistoryChanged() {
		updateActions();
		menuBar.onHistoryChanged();
		toolBar.onHistoryChanged();
		frame.onHistoryChanged();
	}

	protected void updateActions() {
		if (pageContext != null  && getVisiblePage() != null && !getVisiblePage().isExclusive()) {
			previousAction.setEnabled(pageContext.hasPast());
			nextAction.setEnabled(pageContext.hasFuture());
			refreshAction.setEnabled(getVisiblePage() instanceof RefreshablePage);
			upAction.setEnabled(!pageContext.top());
			downAction.setEnabled(!pageContext.bottom());
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
			previousPage();
		}
	}
	
	protected void previousPage() {
		pageContext.previous();
	}

	protected class NextPageAction extends ResourceAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			nextPage();
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

	protected void nextPage() {
		pageContext.next();
	}
	
	private class UpAction extends ResourceAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			pageContext.up();
		}
	}

	private class DownAction extends ResourceAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			pageContext.down();
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
	
}
