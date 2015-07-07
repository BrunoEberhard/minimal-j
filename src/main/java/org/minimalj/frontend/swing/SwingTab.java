package org.minimalj.frontend.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import org.minimalj.application.Application;
import org.minimalj.frontend.page.ActionGroup;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.Separator;
import org.minimalj.frontend.swing.component.EditablePanel;
import org.minimalj.frontend.swing.component.History;
import org.minimalj.frontend.swing.component.History.HistoryListener;
import org.minimalj.frontend.swing.component.SwingPageBar;
import org.minimalj.frontend.swing.toolkit.SwingClientToolkit;

public class SwingTab extends EditablePanel {
	private static final long serialVersionUID = 1L;
	
	final SwingFrame frame;
	final Action previousAction, nextAction, refreshAction;
	final Action closeTabAction;

	private final SwingToolBar toolBar;
	private final SwingMenuBar menuBar;
	private final JSplitPane splitPane;
	private final JScrollPane menuScrollPane;
	private final JScrollPane contentScrollPane;
	private final JPanel verticalPanel;
	
	private final History<Page> history;
	private final List<Page> pages;
	private final SwingPageContextHistoryListener historyListener;

	private Page page;

	public SwingTab(SwingFrame frame) {
		super();
		this.frame = frame;

		historyListener = new SwingPageContextHistoryListener();
		history = new History<>(historyListener);

		pages = new ArrayList<Page>();
		
		previousAction = new PreviousPageAction();
		nextAction = new NextPageAction();
		refreshAction = new RefreshAction();

		closeTabAction = new CloseTabAction();
		
		JPanel outerPanel = new JPanel(new BorderLayout());
		
		menuBar = new SwingMenuBar(this);
		outerPanel.add(menuBar, BorderLayout.NORTH);
		setContent(outerPanel);

		JPanel panel = new JPanel(new BorderLayout());
		outerPanel.add(panel, BorderLayout.CENTER);

		toolBar = new SwingToolBar(this);
		panel.add(toolBar, BorderLayout.NORTH);

		splitPane = new JSplitPane();
		splitPane.setBorder(BorderFactory.createEmptyBorder());
		panel.add(splitPane, BorderLayout.CENTER);

		verticalPanel = new JPanel(new VerticalLayoutManager());
		contentScrollPane = new JScrollPane(verticalPanel);
		contentScrollPane.getVerticalScrollBar().setUnitIncrement(20);
		contentScrollPane.setBorder(BorderFactory.createEmptyBorder());
		splitPane.setRightComponent(contentScrollPane);
		
		contentScrollPane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				verticalPanel.revalidate();
			}
		});
		
		ActionTree actionTree = new ActionTree(Application.getApplication().getMenu(), Application.getApplication().getName());
		menuScrollPane = new JScrollPane(actionTree);
		menuScrollPane.setBorder(BorderFactory.createEmptyBorder());
		splitPane.setLeftComponent(menuScrollPane);
		
		splitPane.setDividerLocation(200);
	}
	
	public static SwingTab getActiveTab() {
		Window w = SwingFrame.getActiveWindow();
		if (w instanceof SwingFrame) {
			return ((SwingFrame) w).getVisibleTab();
		}
		return null;
	}

	public Page getVisiblePage() {
		return page;
	}
	
	void onHistoryChanged() {
		updateActions();
		toolBar.onHistoryChanged();
		frame.onHistoryChanged();
	}

	protected void updateActions() {
		if (getVisiblePage() != null) {
			previousAction.setEnabled(hasPast());
			nextAction.setEnabled(hasFuture());
		} else {
			previousAction.setEnabled(false);
			nextAction.setEnabled(false);
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
		replace(getVisiblePage());
	}

	private class CloseTabAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			frame.closeTab();
		}
	}
	
	// PageContext
	
	private class SwingPageContextHistoryListener implements HistoryListener {

		@Override
		public void onHistoryChanged() {
			page = history.getPresent();
			
			closeAllPages();
			addPage(page);
			
			SwingTab.this.onHistoryChanged();
		}
	}
	
	private void addPage(Page page) {
		pages.add(page);
		verticalPanel.add(new SwingPageBar(page.getTitle()));

		JComponent content = (JComponent) page.getContent();
		if (content != null) {
			JPopupMenu menu = createMenu(page.getMenu());
			content.setComponentPopupMenu(menu);
			setInheritMenu(content);
			verticalPanel.add(content, "");
		} else {
			verticalPanel.add(new JPanel(), "");
		}
		verticalPanel.revalidate();
		verticalPanel.repaint();
	}

	private void closeAllPages() {
		verticalPanel.removeAll();
		pages.clear();
	}
	
	public class VerticalLayoutManager implements LayoutManager {

		private Dimension preferredSize = new Dimension(100, 100);
		private Rectangle lastParentBounds = null;
		
		public VerticalLayoutManager() {
		}

		@Override
		public Dimension preferredLayoutSize(Container parent) {
			if (preferredSize == null) {
				layoutContainer(parent);
			}
			return preferredSize;
		}

		@Override
		public Dimension minimumLayoutSize(Container parent) {
			return new Dimension(100, 100);
		}

		@Override
		public void layoutContainer(Container parent) {
			if (lastParentBounds != null && lastParentBounds.equals(parent.getBounds())) return;
			lastParentBounds = parent.getBounds();
			
			int minSum = 0;
			for (Component component : parent.getComponents()) {
				minSum += component.getMinimumSize().height;
			}

			Set<Component> verticallyGrowingComponents = new HashSet<>();
			for (Component component : parent.getComponents()) {
				if (component.getPreferredSize().height > component.getMinimumSize().height) {
					verticallyGrowingComponents.add(component);
				}
			}
			
			// the last one always gets to grow
			if (parent.getComponentCount() > 0) {
				verticallyGrowingComponents.add(parent.getComponent(parent.getComponentCount() - 1));
			}
			
			int y = 0;		
			int x = 0;
			int width = parent.getWidth();
			int widthWithoutIns = width - x;
//			int moreThanMin = parent.getHeight() - minSum;
			int moreThanMin = contentScrollPane.getHeight() - minSum;
			if (moreThanMin < 0) {
				verticallyGrowingComponents.clear();
			}
			for (Component component : parent.getComponents()) {
				int height = component.getMinimumSize().height;
				if (verticallyGrowingComponents.contains(component)) {
					height += moreThanMin / verticallyGrowingComponents.size();
				}
				component.setBounds(x, y, widthWithoutIns, height);
				y += height;
			}
			
			preferredSize = new Dimension(100, y);
		}

		@Override
		public void addLayoutComponent(String name, Component comp) {
			lastParentBounds = null;
			preferredSize = null;
		}

		@Override
		public void removeLayoutComponent(Component comp) {
			lastParentBounds = null;
			preferredSize = null;
		}
	}

	public void add(Page page) {
		history.add(page);
	}

	public void replace(Page page) {
		history.replace(page);
	}

	public Page getPresent() {
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

	public void show(Page page) {
		show(page, true);
	}
	
	public void show(Page page, boolean asTopPage) {
		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						show(page, asTopPage);
					};
				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		} else {
			if (asTopPage) {
				history.add(page);
			} else {
				this.page = page;
				addPage(page);
			}
		}
	}

	public void hide(Page page) {
		int index;
		for (index = 0; index < pages.size(); index++) {
			if (pages.get(index) == page) {
				break;
			}
		}
		for (int index2 = pages.size() - 1; index2 >= index; index2--) {
			pages.remove(index2);
			// remove bar and content
			verticalPanel.remove(verticalPanel.getComponentCount()-1);
			verticalPanel.remove(verticalPanel.getComponentCount()-1);
		}
		verticalPanel.revalidate();
		verticalPanel.repaint();
	}
	
	public boolean isShown(Page page) {
		return pages.contains(page);
	}
	
	private JPopupMenu createMenu(ActionGroup actionGroup) {
		if (actionGroup != null && actionGroup.getItems() != null) {
			JPopupMenu menu = new JPopupMenu(actionGroup.getName());
			addActions(menu, actionGroup.getItems());
			return menu;
		}
		return null;
	}
	
	public static void addActions(JPopupMenu menu, List<org.minimalj.frontend.toolkit.Action> actions) {
		for (org.minimalj.frontend.toolkit.Action action : actions) {
			if (action instanceof org.minimalj.frontend.page.ActionGroup) {
				org.minimalj.frontend.page.ActionGroup actionGroup = (org.minimalj.frontend.page.ActionGroup) action;
				JMenu subMenu = new JMenu(SwingClientToolkit.adaptAction(action));
				SwingMenuBar.addActions(subMenu, actionGroup.getItems());
				menu.add(subMenu);
			} else if (action instanceof Separator) {
				menu.addSeparator();
			} else {
				menu.add(new JMenuItem(SwingClientToolkit.adaptAction(action)));
			}
		}
	}

	private void setInheritMenu(JComponent component) {
		component.setInheritsPopupMenu(true);
		for (Component c : component.getComponents()) {
			if (c instanceof JComponent) {
				setInheritMenu((JComponent) c);
			}
		}
	}
	
	public boolean tryToClose() {
		return tryToCloseDialogs();
	}

}
