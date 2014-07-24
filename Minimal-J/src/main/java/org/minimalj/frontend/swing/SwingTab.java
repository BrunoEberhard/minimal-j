package org.minimalj.frontend.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.minimalj.frontend.edit.Editor;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.PageLink;
import org.minimalj.frontend.page.RefreshablePage;
import org.minimalj.frontend.swing.component.EditablePanel;
import org.minimalj.frontend.swing.component.History;
import org.minimalj.frontend.swing.component.History.HistoryListener;
import org.minimalj.frontend.swing.toolkit.ScrollablePanel;
import org.minimalj.frontend.swing.toolkit.SwingClientToolkit;
import org.minimalj.frontend.swing.toolkit.SwingClientToolkit.SwingLink;
import org.minimalj.frontend.swing.toolkit.SwingFormAlignLayoutManager;
import org.minimalj.frontend.swing.toolkit.SwingScrollPane;
import org.minimalj.frontend.swing.toolkit.SwingSwitchContent;
import org.minimalj.frontend.toolkit.ClientToolkit.IContent;
import org.minimalj.frontend.toolkit.FormContent;

public class SwingTab extends EditablePanel {
	private static final long serialVersionUID = 1L;
	
	final SwingFrame frame;
	final Action previousAction, nextAction, refreshAction, upAction, downAction;
	final Action closeTabAction;
	final JMenuItem menuItemToolBarVisible;

	private final SwingToolBar toolBar;
	private final SwingMenuBar menuBar;
	private final SwingSwitchContent switchContent;
	
	private final History<String> history;
	private final SwingPageContextHistoryListener historyListener;
	private final MouseListener mouseListener;

	private Page page;
	private List<String> pageLinks;
	private int indexInPageLinks;

	private Editor<?> editor;
	
	public SwingTab(SwingFrame frame) {
		super();
		this.frame = frame;

		historyListener = new SwingPageContextHistoryListener();
		history = new History<String>(historyListener);

		mouseListener = new SwingTabMouseListener();
		
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
		switchContent = new SwingSwitchContent();
		panel.add(switchContent, BorderLayout.CENTER);
		setContent(outerPanel);
	}
	
	public Page getVisiblePage() {
		return page;
	}
	
	public Editor<?> getEditor() {
		return editor;
	}
	
	void onHistoryChanged() {
		updateActions();
		menuBar.onHistoryChanged();
		toolBar.onHistoryChanged();
		frame.onHistoryChanged();
	}

	protected void updateActions() {
		if (getVisiblePage() != null && editor == null) {
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
	
	protected class PreviousPageAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			previous();
		}
	}
	
	protected class NextPageAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			next();
		}
	}

	protected class RefreshAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

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

	private class UpAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			up();
		}
	}

	private class DownAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			down();
		}
	}

	private class CloseTabAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			frame.closeTab();
		}
	}
	
	private class MenuItemToolBarVisible extends JCheckBoxMenuItem implements ItemListener {
		private static final long serialVersionUID = 1L;
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
			page = PageLink.createPage(history.getPresent());
			show(page);
			SwingTab.this.onHistoryChanged();
		}

		private void show(Page page) {
			IContent content = page.getContent();
			if (content instanceof FormContent) {
				JPanel panel = new JPanel(new SwingFormAlignLayoutManager());
				panel.add((Component)content);
				content = new SwingScrollPane(new ScrollablePanel(panel));
			}
			switchContent.show(content);
			registerMouseListener((Component) content);
			if (content instanceof JComponent) {
				// this is more about if component is not null (empty page)
				SwingClientToolkit.focusFirstComponent((JComponent) content);
			}
		}
	}
	
	private class SwingTabMouseListener extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			Object source = e.getSource();
			if (source instanceof SwingLink) {
				SwingLink link = (SwingLink) source;
				show(link.getAddress());
			}
		}
		
	}
	
	private void registerMouseListener(Component component) {
		if (component instanceof SwingLink) {
			 ((SwingLink) component).setMouseListener(mouseListener);
		}
		if (component instanceof Container) {
			for (Component c : ((Container) component).getComponents()) {
				registerMouseListener(c);
			}
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
			add(pageLink);
		}
	}
	
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

}
