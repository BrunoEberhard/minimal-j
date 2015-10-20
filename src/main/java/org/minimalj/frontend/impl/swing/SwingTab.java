package org.minimalj.frontend.impl.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.action.Separator;
import org.minimalj.frontend.impl.swing.component.EditablePanel;
import org.minimalj.frontend.impl.swing.component.SwingDecoration;
import org.minimalj.frontend.impl.swing.toolkit.SwingEditorPanel;
import org.minimalj.frontend.impl.swing.toolkit.SwingFrontend;
import org.minimalj.frontend.impl.swing.toolkit.SwingInternalFrame;
import org.minimalj.frontend.impl.swing.toolkit.SwingProgressInternalFrame;
import org.minimalj.frontend.impl.swing.toolkit.SwingSearchPanel;
import org.minimalj.frontend.impl.util.History;
import org.minimalj.frontend.impl.util.History.HistoryListener;
import org.minimalj.frontend.page.IDialog;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.PageManager;
import org.minimalj.frontend.page.ProgressListener;
import org.minimalj.security.Subject;

public class SwingTab extends EditablePanel implements PageManager {
	private static final long serialVersionUID = 1L;
	
	final SwingFrame frame;
	final Action previousAction, nextAction, refreshAction;
	final Action closeTabAction;
	final Action navigationAction;
	
	private final SwingToolBar toolBar;
	private final SwingMenuBar menuBar;
	private final JSplitPane splitPane;
	private final SwingDecoration decoratedNavigationPane;
	private final JScrollPane contentScrollPane;
	private final JPanel verticalPanel;
	private final JScrollPane navigationScrollPane;
	
	private final History<List<Page>> history;
	private final SwingTabHistoryListener historyListener;

	private final List<Page> visiblePageAndDetailsList;

	public SwingTab(SwingFrame frame) {
		super();
		this.frame = frame;

		historyListener = new SwingTabHistoryListener();
		history = new History<>(historyListener);

		visiblePageAndDetailsList = new ArrayList<Page>();
		
		previousAction = new PreviousPageAction();
		nextAction = new NextPageAction();
		refreshAction = new RefreshAction();

		closeTabAction = new CloseTabAction();
		
		navigationAction = new NavigationAction();
		
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
		
		navigationScrollPane = new JScrollPane();
		navigationScrollPane.setBorder(BorderFactory.createEmptyBorder());
		ActionListener navigationClosedListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				navigationAction.putValue(Action.SELECTED_KEY, Boolean.FALSE);
				navigationAction.actionPerformed(e);
			}
		};
		decoratedNavigationPane = new SwingDecoration(Application.getApplication().getName(), navigationScrollPane, SwingDecoration.HIDE_MINIMIZE, navigationClosedListener);
		splitPane.setLeftComponent(decoratedNavigationPane);
		
		splitPane.setDividerLocation(200);
	}
	
	public void updateNavigation() {
		navigationScrollPane.setViewportView(new NavigationTree(Application.getApplication().getMenu()));
	}

	public Page getVisiblePage() {
		return visiblePageAndDetailsList.get(0);
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
			// not implemented at the moment
			// replace(getVisiblePage());
		}
	}
	
	private class CloseTabAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			frame.closeTab();
		}
	}
	
	private class NavigationAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		private int lastDividerLocation;
		
		public NavigationAction() {
			putValue(Action.SELECTED_KEY, Boolean.TRUE);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if (Boolean.TRUE.equals(getValue(Action.SELECTED_KEY))) {
				splitPane.setLeftComponent(decoratedNavigationPane);
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
	
	private class SwingTabHistoryListener implements HistoryListener {
		
		@Override
		public void onHistoryChanged() {
			visiblePageAndDetailsList.clear();
			verticalPanel.removeAll();
			
			for (Page page : history.getPresent()) {
				addPageOrDetail(page);
			}
			
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

	@Override
	public void show(Page page) {
		List<Page> pages = new ArrayList<>();
		pages.add(page);
		history.add(pages);
	}

	@Override
	public void showDetail(Page mainPage, Page detail) {
		int index = visiblePageAndDetailsList.indexOf(detail);
		if (index > -1) {
			SwingDecoration decoration = (SwingDecoration) verticalPanel.getComponents()[index];
			decoration.setTitle(detail.getTitle());
			decoration.setContentVisible();
			return;
		}
		removeDetailsOf(mainPage);
		addPageOrDetail(detail);

		List<Page> pages = new ArrayList<>();
		pages.addAll(history.getPresent());
		pages.add(detail);
		history.addQuiet(pages);
	}
	
	private void addPageOrDetail(Page page) {
		visiblePageAndDetailsList.add(page);
		ActionListener closeListener = null;
		if (visiblePageAndDetailsList.size() > 1) {
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

		verticalPanel.add(new SwingDecoration(page.getTitle(), content, SwingDecoration.SHOW_MINIMIZE, closeListener), "");
		verticalPanel.revalidate();
	}

	private void removeDetailsOf(Page page) {
		int index = visiblePageAndDetailsList.indexOf(page);
		removeDetails(index + 1);
	}

	private void removeDetails(int index) {
		for (int index2 = visiblePageAndDetailsList.size() - 1; index2 >= index; index2--) {
			visiblePageAndDetailsList.remove(index2);
			verticalPanel.remove(verticalPanel.getComponentCount()-1);
		}
		verticalPanel.revalidate();
	}

	@Override
	public boolean isDetailShown(Page detail) {
		return visiblePageAndDetailsList.contains(detail);
	}
	
	@Override
	public void hideDetail(Page detail) {
		int index = visiblePageAndDetailsList.indexOf(detail);
		removeDetails(index);
	}

	@Override
	public void showError(String text) {
		Window window = findWindow();
		JOptionPane.showMessageDialog(window, text, "Fehler", JOptionPane.ERROR_MESSAGE);
	}
	
	private Window findWindow() {
		Component parentComponent = this;
		while (parentComponent != null && !(parentComponent instanceof Window)) {
			if (parentComponent instanceof JPopupMenu) {
				parentComponent = ((JPopupMenu) parentComponent).getInvoker();
			} else {
				parentComponent = parentComponent.getParent();
			}
		}
		return (Window) parentComponent;
	}

	@Override
	public IDialog showDialog(String title, IContent content, org.minimalj.frontend.action.Action saveAction, org.minimalj.frontend.action.Action closeAction, org.minimalj.frontend.action.Action... actions) {
		JComponent contentComponent = new SwingEditorPanel(content, actions);
		return createDialog(title, contentComponent, saveAction, closeAction);
	}

	private IDialog createDialog(String title, JComponent content, org.minimalj.frontend.action.Action saveAction, org.minimalj.frontend.action.Action closeAction) {
		return new SwingInternalFrame(this, title, content, saveAction, closeAction);
	}
	
	@Override
	public void showMessage(String text) {
		Window window = findWindow();
		JOptionPane.showMessageDialog(window, text, "Information", JOptionPane.INFORMATION_MESSAGE);
	}
	
	@Override
	public <T> IDialog showSearchDialog(Search<T> index, Object[] keys, TableActionListener<T> listener) {
		SwingSearchPanel<T> panel = new SwingSearchPanel<T>(index, keys, listener);
		return createDialog(null, panel, null, null);
	}

	@Deprecated
	public ProgressListener showProgress(String text) {
		SwingProgressInternalFrame frame = new SwingProgressInternalFrame(text);
		openModalDialog(frame);
		return frame;
	}
	
	private JPopupMenu createMenu(List<org.minimalj.frontend.action.Action> actions) {
		if (actions != null && actions.size() > 0) {
			JPopupMenu menu = new JPopupMenu();
			addActions(menu, actions);
			return menu;
		}
		return null;
	}
	
	public static void addActions(JPopupMenu menu, List<org.minimalj.frontend.action.Action> actions) {
		for (org.minimalj.frontend.action.Action action : actions) {
			if (action instanceof org.minimalj.frontend.action.ActionGroup) {
				org.minimalj.frontend.action.ActionGroup actionGroup = (org.minimalj.frontend.action.ActionGroup) action;
				JMenu subMenu = new JMenu(SwingFrontend.adaptAction(action));
				SwingMenuBar.addActions(subMenu, actionGroup.getItems());
				menu.add(subMenu);
			} else if (action instanceof Separator) {
				menu.addSeparator();
			} else {
				menu.add(new JMenuItem(SwingFrontend.adaptAction(action)));
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
	
	@Override
	public Subject getSubject() {
		return frame.getSubject();
	}

	@Override
	public void setSubject(Subject subject) {
		frame.setSubject(subject);
	}
}
