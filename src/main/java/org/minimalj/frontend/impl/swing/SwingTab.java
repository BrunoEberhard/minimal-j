package org.minimalj.frontend.impl.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.action.Separator;
import org.minimalj.frontend.impl.swing.component.EditablePanel;
import org.minimalj.frontend.impl.swing.toolkit.SwingDialog;
import org.minimalj.frontend.impl.swing.toolkit.SwingEditorPanel;
import org.minimalj.frontend.impl.swing.toolkit.SwingFrontend;
import org.minimalj.frontend.impl.swing.toolkit.SwingProgressInternalFrame;
import org.minimalj.frontend.impl.util.History;
import org.minimalj.frontend.impl.util.History.HistoryListener;
import org.minimalj.frontend.impl.util.PageAccess;
import org.minimalj.frontend.page.IDialog;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.PageManager;
import org.minimalj.frontend.page.ProgressListener;
import org.minimalj.frontend.page.Routing;
import org.minimalj.security.Authorization;
import org.minimalj.security.Subject;

public class SwingTab extends EditablePanel implements PageManager {
	private static final long serialVersionUID = 1L;

	public static final int MAX_PAGES_UNLIMITED = 0;
	public static final int MAX_PAGES_ADPATIV = -1;

	final SwingFrame frame;
	final Action previousAction, nextAction, refreshAction, favoriteAction;

	private final JPanel verticalPanel;

	private Subject subject;
	private final History<List<Page>> history;

	private final List<Page> visiblePageAndDetailsList;

	private static final Icon favorite_yes_icon = SwingFrontend.getIcon("favorite_yes.largeIcon");
	private static final Icon favorite_no_icon = SwingFrontend.getIcon("favorite_no.largeIcon");

	public SwingTab(SwingFrame frame) {
		super();
		this.frame = frame;

		SwingTabHistoryListener historyListener = new SwingTabHistoryListener();
		history = new History<>(historyListener);

		visiblePageAndDetailsList = new ArrayList<>();

		previousAction = new PreviousPageAction();
		nextAction = new NextPageAction();
		refreshAction = new RefreshAction();
		favoriteAction = new FavoriteAction();

		verticalPanel = new JPanel(new VerticalLayoutManager());

		setContent(verticalPanel);
	}

	public SwingFrame getFrame() {
		return frame;
	}
	
	public Page getVisiblePage() {
		return visiblePageAndDetailsList.get(0);
	}

	void onHistoryChanged() {
		updateActions();
		frame.onHistoryChanged();
	}

	protected void updateActions() {
		favoriteAction.putValue(Action.LARGE_ICON_KEY, favorite_no_icon);
		if (getVisiblePage() != null) {
			previousAction.setEnabled(hasPast());
			nextAction.setEnabled(hasFuture());
			String route = Routing.getRouteSafe(getVisiblePage());
			if (route != null) {
				favoriteAction.setEnabled(true);
				boolean favorite = frame.favorites.isFavorite(route);
				if (favorite) {
					favoriteAction.putValue(Action.LARGE_ICON_KEY, favorite_yes_icon);
				}
			} else {
				favoriteAction.setEnabled(false);
			}
		} else {
			previousAction.setEnabled(false);
			nextAction.setEnabled(false);
			favoriteAction.setEnabled(false);
		}
	}

	public void updateFavorites(LinkedHashMap<String, String> newFavorites) {
		updateActions();
	}

	//

	protected class PreviousPageAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			SwingFrontend.run(e, SwingTab.this::previous);
		}
	}

	protected class NextPageAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			SwingFrontend.run(e, SwingTab.this::next);
		}
	}

	protected class RefreshAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			List<Page> pages = new ArrayList<>(visiblePageAndDetailsList);
			verticalPanel.removeAll();
			visiblePageAndDetailsList.clear();
			for (Page page : pages) {
				addPageOrDetail(page);
			}
		}
	}

	private class FavoriteAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			Page page = getVisiblePage();
			String route = Routing.getRouteSafe(page);
			if (route != null) {
				frame.favorites.toggleFavorite(route, page.getTitle());
			}
		}
	}

	public boolean close() {
		if (visiblePageAndDetailsList.size() > 1) {
			removeDetails(visiblePageAndDetailsList.size() - 1);
			return true;
		} else {
			return false;
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
		private final int SPACING = 6;

		@Override
		public Dimension preferredLayoutSize(Container parent) {
			return new Dimension(1000, 800);
		}

		@Override
		public Dimension minimumLayoutSize(Container parent) {
			return new Dimension(200, 300);
		}

		@Override
		public void layoutContainer(Container parent) {
			List<Component> visibleComponents = new ArrayList<>(Arrays.asList(parent.getComponents()));
			visibleComponents.removeIf((Component component) -> !component.isVisible());

			int y = parent.getInsets().top;
			int x = parent.getInsets().left;
			int width = parent.getWidth() - parent.getInsets().left - parent.getInsets().right;
			int height = parent.getHeight() - parent.getInsets().top - parent.getInsets().bottom;
			height = height - SPACING * (visibleComponents.size() - 1);

			for (Component component : visibleComponents) {
				component.setBounds(x, y, width, height / visibleComponents.size());
				y += component.getHeight() + SPACING;
			}
		}

		@Override
		public void addLayoutComponent(String name, Component comp) {
		}

		@Override
		public void removeLayoutComponent(Component comp) {
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

	public void setSubject(Subject subject) {
		if (this.subject != null) {
			history.restoreSnapshot();
		}
		history.takeSnapshot();
		this.subject = subject;
	}

	@Override
	public void show(Page page) {
		if (Authorization.hasAccess(Subject.getCurrent(), page)) {
			List<Page> pages = new ArrayList<>();
			pages.add(page);
			history.add(pages);
		} else {
			Frontend.showError("No Access to " + page.getClass().getSimpleName());
		}
	}

	@Override
	public void showDetail(Page mainPage, Page detail) {
		int index = visiblePageAndDetailsList.indexOf(detail);
		if (index > -1) {
//			JTabbedPane decoration = (JTabbedPane) verticalPanel.getComponents()[index];
//			decoration.setTitle(detail.getTitle());
//			decoration.setContentVisible();
//			return;
		}
		removeDetailsOf(mainPage);
		addPageOrDetail(detail);

		history.addQuiet(new ArrayList<>(visiblePageAndDetailsList));
	}

	private void addPageOrDetail(Page page) {
		visiblePageAndDetailsList.add(page);

		JComponent content = (JComponent) PageAccess.getContent(page);
		if (content != null) {
			JPopupMenu menu = createMenu(PageAccess.getActions(page));
			content.setComponentPopupMenu(menu);
			setInheritMenu(content);
		} else {
			content = new JPanel();
		}
		content.putClientProperty("page", page);

		if (verticalPanel.getComponentCount() == 0) {
			verticalPanel.add(content, "");
		} else {
			JTabbedPane tabbedPane = new JTabbedPane();
			tabbedPane.addTab(page.getTitle(), content);
			verticalPanel.add(tabbedPane, "");
		}
		verticalPanel.revalidate();
	}

	private void removeDetailsOf(Page page) {
		int index = visiblePageAndDetailsList.indexOf(page);
		removeDetails(index + 1);
	}

	private void removeDetails(int index) {
		for (int index2 = visiblePageAndDetailsList.size() - 1; index2 >= index; index2--) {
			visiblePageAndDetailsList.remove(index2);
			verticalPanel.remove(verticalPanel.getComponentCount() - 1);
		}
		verticalPanel.revalidate();
	}

	@Override
	public boolean isDetailShown(Page detail) {
		return visiblePageAndDetailsList.contains(detail);
	}

	@Override
	public void hideDetail(Page detail) {
		if (isDetailShown(detail)) {
			int index = visiblePageAndDetailsList.indexOf(detail);
			removeDetails(index);
		}
	}

	@Override
	public void showError(String text) {
		Window window = findWindow();
		JOptionPane.showMessageDialog(window, text, "Fehler", JOptionPane.ERROR_MESSAGE);
	}
	
	@Override
	public void login(Subject subject) {
		frame.setSubject(subject);
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
		SwingDialog dialog = new SwingDialog(frame, title, contentComponent, saveAction, closeAction);
		return dialog;
	}
	
	@Override
	public void showMessage(String text) {
		Window window = findWindow();
		JOptionPane.showMessageDialog(window, text, "Information", JOptionPane.INFORMATION_MESSAGE);
	}

	@Deprecated
	public ProgressListener showProgress(String text) {
		SwingProgressInternalFrame frame = new SwingProgressInternalFrame(text);
		openModalDialog(frame);
		return frame;
	}

	public static JPopupMenu createMenu(List<org.minimalj.frontend.action.Action> actions) {
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
}
