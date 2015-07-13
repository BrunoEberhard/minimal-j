package org.minimalj.frontend.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.swing.UIManager;

import org.minimalj.application.Application;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.Separator;
import org.minimalj.frontend.swing.component.EditablePanel;
import org.minimalj.frontend.swing.component.History;
import org.minimalj.frontend.swing.component.History.HistoryListener;
import org.minimalj.frontend.swing.component.SwingDecoration;
import org.minimalj.frontend.swing.toolkit.SwingClientToolkit;

public class SwingTab extends EditablePanel {
	private static final long serialVersionUID = 1L;
	
	final SwingFrame frame;
	final Action previousAction, nextAction, refreshAction;
	final Action closeTabAction;
	final Action toggleMenuAction;

	private final SwingToolBar toolBar;
	private final SwingMenuBar menuBar;
	private final JSplitPane splitPane;
	private final SwingDecoration decoratedMenuPane;
	private final JScrollPane contentScrollPane;
	private final JPanel verticalPanel;
	
	private final History<Page> history;
	private final SwingPageContextHistoryListener historyListener;

	private final List<Page> pageAndDetails;

	public SwingTab(SwingFrame frame) {
		super();
		this.frame = frame;

		historyListener = new SwingPageContextHistoryListener();
		history = new History<>(historyListener);

		pageAndDetails = new ArrayList<Page>();
		
		previousAction = new PreviousPageAction();
		nextAction = new NextPageAction();
		refreshAction = new RefreshAction();

		closeTabAction = new CloseTabAction();
		
		toggleMenuAction = new ToggleMenuAction();
		
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
		
		MenuTree menuTree = new MenuTree(Application.getApplication().getMenu());
		JScrollPane menuScrollPane = new JScrollPane(menuTree);
		menuScrollPane.setBorder(BorderFactory.createEmptyBorder());
		ActionListener menuClosedListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				toggleMenuAction.putValue(Action.SELECTED_KEY, Boolean.FALSE);
				toggleMenuAction.actionPerformed(e);
			}
		};
		decoratedMenuPane = new SwingDecoration(Application.getApplication().getName(), menuScrollPane, menuClosedListener);
		splitPane.setLeftComponent(decoratedMenuPane);
		
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
		return pageAndDetails.get(0);
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
	
	private class ToggleMenuAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		private int lastDividerLocation;
		
		public ToggleMenuAction() {
			putValue(Action.SELECTED_KEY, Boolean.TRUE);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if (Boolean.TRUE.equals(getValue(Action.SELECTED_KEY))) {
				splitPane.setLeftComponent(decoratedMenuPane);
				splitPane.setDividerSize((Integer) UIManager.get("SplitPane.dividerSize"));
				splitPane.setDividerLocation(lastDividerLocation);
			} else {
				lastDividerLocation = splitPane.getDividerLocation();
				splitPane.setLeftComponent(null);
				splitPane.setDividerSize(0);
			}
		}
	}
	
	// PageContext
	
	private class SwingPageContextHistoryListener implements HistoryListener {

		@Override
		public void onHistoryChanged() {
			pageAndDetails.clear();
			verticalPanel.removeAll();

			Page page = history.getPresent();
			addPageOrDetail(page);
			
			SwingTab.this.onHistoryChanged();
		}
	}
	
	public class VerticalLayoutManager implements LayoutManager {

		private Dimension preferredSize = null;
		
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
			List<Component> visibleComponents = new ArrayList<>(Arrays.asList(parent.getComponents()));
			visibleComponents.removeIf((Component component) -> !component.isVisible());
			
			int minSum = 0;
			for (Component component : visibleComponents) {
				minSum += component.getMinimumSize().height;
			}

			Set<Component> verticallyGrowingComponents = new HashSet<>();
			for (Component component : visibleComponents) {
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
			for (Component component : visibleComponents) {
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
			preferredSize = null;
		}

		@Override
		public void removeLayoutComponent(Component comp) {
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
		history.add(page);
	}

	public void showDetail(Page detail) {
		int index = pageAndDetails.indexOf(detail);
		if (index > -1) {
			SwingDecoration decoration = (SwingDecoration) verticalPanel.getComponents()[index];
			decoration.setContentVisible();
			return;
		}
		removeDetailsOf(SwingClientToolkit.getPage());
		addPageOrDetail(detail);
	}
	
	private void addPageOrDetail(Page page) {
		pageAndDetails.add(page);
		ActionListener closeListener = null;
		if (pageAndDetails.size() > 1) {
			closeListener = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					hideDetail(page);
				}
			};
		}
	
		JComponent content = (JComponent) page.getContent();
		if (content != null) {
			JPopupMenu menu = createMenu(page.getActions());
			content.setComponentPopupMenu(menu);
			setInheritMenu(content);
		} else {
			content = new JPanel();
		}
		content.putClientProperty("page", page);

		verticalPanel.add(new SwingDecoration(page.getTitle(), content, closeListener));
		verticalPanel.revalidate();
		verticalPanel.repaint();
	}

	private void removeDetailsOf(Page page) {
		int index = pageAndDetails.indexOf(page);
		removeDetails(index + 1);
	}

	private void removeDetails(int index) {
		for (int index2 = pageAndDetails.size() - 1; index2 >= index; index2--) {
			pageAndDetails.remove(index2);
			verticalPanel.remove(verticalPanel.getComponentCount()-1);
		}
		verticalPanel.revalidate();
		verticalPanel.repaint();
	}

	public boolean isShown(Page detail) {
		return pageAndDetails.contains(detail);
	}
	
	public void hideDetail(Page detail) {
		int index = pageAndDetails.indexOf(detail);
		removeDetails(index);
	}

	private JPopupMenu createMenu(List<org.minimalj.frontend.toolkit.Action> actions) {
		if (actions != null && actions.size() > 0) {
			JPopupMenu menu = new JPopupMenu();
			addActions(menu, actions);
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
