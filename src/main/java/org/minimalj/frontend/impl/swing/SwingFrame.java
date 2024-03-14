package org.minimalj.frontend.impl.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BiConsumer;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;

import org.minimalj.application.Application;
import org.minimalj.application.Application.AuthenticatonMode;
import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.swing.SwingMenuBar.SwingMenuBarProvider;
import org.minimalj.frontend.impl.swing.toolkit.SwingFrontend;
import org.minimalj.frontend.page.EmptyPage;
import org.minimalj.frontend.page.Page;
import org.minimalj.security.Subject;
import org.minimalj.util.StringUtils;

import com.formdev.flatlaf.extras.components.FlatTabbedPane;

public class SwingFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	private Subject subject;
	final SwingFavorites favorites = new SwingFavorites(this::onFavoritesChange);

	private final FlatTabbedPane tabbedPane = new FlatTabbedPane();
	private JScrollPane navigationScrollPane;
	private SwingToolBar toolBar;
	private SwingMenuBar menuBar;
	private FlatTabbedPane navigationTabbedPane;
	private JSplitPane splitPane;

	public static SwingFrame activeFrameOverride = null;

	final Action loginAction, logoutAction, closeWindowAction, exitAction, newWindowAction, newTabAction;
	final CloseTabAction closeTabAction;
	public final NavigationAction navigationAction;
	public final Action toolbarAction;

	public SwingFrame() {
		AuthenticatonMode authenticatonMode = Application.getInstance().getAuthenticatonMode();
		loginAction = authenticatonMode != AuthenticatonMode.NOT_AVAILABLE ? new SwingLoginAction() : null;
		logoutAction = authenticatonMode != AuthenticatonMode.NOT_AVAILABLE && authenticatonMode != AuthenticatonMode.REQUIRED ? new SwingLogoutAction() : null;

		closeWindowAction = new CloseWindowAction();
		closeTabAction = new CloseTabAction();
		exitAction = new ExitAction();
		newWindowAction = new NewWindowAction();
		newTabAction = new NewTabAction();
		navigationAction = new NavigationAction();
		toolbarAction = new ToolbarAction();

		tabbedPane.setHideTabAreaWithOneTab(false);
		tabbedPane.setTabCloseCallback(closeTabAction);
		tabbedPane.getModel().addChangeListener(this::tabSelectionChanged);

		setDefaultSize();
		setLocationRelativeTo(null);
		addWindowListener();
		createContent();

		updateMenuBar();
		addTab();

		updateIcon();
	}

	protected void setDefaultSize() {
		Dimension screenSize = getToolkit().getScreenSize();
		Dimension frameSize = Application.getInstance().getFameSize(screenSize);
		if (frameSize.width == Integer.MAX_VALUE || frameSize.height == Integer.MAX_VALUE) {
			setExtendedState(MAXIMIZED_BOTH);
			setSize(screenSize.width - 20, screenSize.height - 40);
		} else {
			setSize(frameSize);
		}
	}

	private void addWindowListener() {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		WindowListener listener = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				FrameManager.getInstance().closeWindowPerformed(SwingFrame.this);
			}
		};
		addWindowListener(listener);
	}

	protected void createContent() {
		getContentPane().setLayout(new BorderLayout());

		toolBar = new SwingToolBar();
		getContentPane().add(toolBar, BorderLayout.NORTH);

		navigationScrollPane = new JScrollPane();
		navigationScrollPane.setBorder(BorderFactory.createEmptyBorder());
		navigationTabbedPane = new FlatTabbedPane();
		navigationTabbedPane.setTabsClosable(true);
		navigationTabbedPane.addTab(Application.getInstance().getName(), navigationScrollPane);
		navigationTabbedPane.setTabCloseCallback((tab, index) -> navigationAction.close());
		
		splitPane = new JSplitPane();
		splitPane.setBorder(BorderFactory.createEmptyBorder());
		getContentPane().add(splitPane, BorderLayout.CENTER);

		splitPane.setLeftComponent(navigationTabbedPane);
		splitPane.setRightComponent(tabbedPane);

		splitPane.setDividerLocation(200);
	}

	private void tabSelectionChanged(ChangeEvent e) {
		SwingUtilities.invokeLater(() -> tabSelectionChanged((SwingTab) tabbedPane.getSelectedComponent()));
		closeTabAction.setEnabled(tabbedPane.getTabCount() > 1);
		tabbedPane.setTabsClosable(tabbedPane.getTabCount() > 1);
	}

	private void tabSelectionChanged(SwingTab tab) {
		toolBar.setActiveTab(tab);
		menuBar.setActiveTab(tab);
		// TODO resuse NavigationTree
		updateNavigation();
	}
	
	public void updateNavigation() {
		SwingFrontend.run(this, () -> {
			navigationScrollPane.setViewportView(new NavigationTree(Application.getInstance().getNavigation()));
		});
	}

	private void addTab() {
		SwingTab tab = new SwingTab(this);
		tabbedPane.addTab("", tab);
		tabbedPane.setSelectedComponent(tab);
		tab.show(new EmptyPage());
	}

//	public void closeTabActionPerformed() {
//		if (getVisibleTab().tryToClose()) {
//			closeTab();
//			if (tabbedPane.getTabCount() == 0) {
//				if (FrameManager.getInstance().askBeforeCloseLastWindow(this)) {
//					FrameManager.getInstance().lastTabClosed(SwingFrame.this);
//				} else {
//					addTab();
//				}
//			}
//		}
//	}

	public boolean tryToCloseWindow() {
//		boolean closable = true;
//		for (int i = tabbedPane.getTabCount()-1; i>=0; i--) {
//			SwingTab tab = (SwingTab) tabbedPane.getComponentAt(i);
//			tabbedPane.setSelectedIndex(i);
//			closable = tab.tryToClose();
//			if (!closable) return false;
//		}
		closeWindow();
		return true;
	}

	public void closeTab(SwingTab tab) {
		tabbedPane.remove(tab);
	}

	public void closeWindow() {
		setVisible(false);
		dispose();
	}

//	public static SwingFrame getActiveWindow() {
//		if (activeFrameOverride != null) {
//			return activeFrameOverride;
//		}
//		for (Window w : Window.getWindows()) {
//			if (w.isActive()) {
//				return (SwingFrame) w;
//			}
//		}
//		return null;
//	}

	public SwingTab getVisibleTab() {
		SwingTab tab = (SwingTab) tabbedPane.getSelectedComponent();
		return tab;
	}

	public boolean moreThanOneTabOpen() {
		return tabbedPane.getTabCount() > 1;
	}

	public void closeTab() {
		closeTab((SwingTab) tabbedPane.getSelectedComponent());
	}

	public Page getVisiblePage() {
		SwingTab tab = getVisibleTab();
		if (tab == null)
			return null;
		return tab.getVisiblePage();
	}

	public List<Page> getPages() {
		List<Page> result = new ArrayList<>();
		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			SwingTab tab = (SwingTab) tabbedPane.getComponent(i); // myst: getTabComponent returns allways null
			Page page = tab.getVisiblePage();
			if (page != null)
				result.add(page);
		}
		return result;
	}

	void onHistoryChanged() {
		updateTitle();
		updateWindowTitle();
	}

	public void setSubject(Subject subject) {
		if (!SwingUtilities.isEventDispatchThread()) 
			throw new RuntimeException();
		
		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			SwingTab swingTab = (SwingTab) tabbedPane.getComponentAt(i);
			swingTab.setSubject(subject);
		}
		this.subject = subject;
		Subject.setCurrent(subject);

		if (loginAction != null) {
			SwingResourceAction.initProperties(loginAction, subject != null ? "ReloginAction" : "LoginAction");
		}
		if (logoutAction != null) {
			logoutAction.setEnabled(subject != null);
		}

		SwingFrontend.run(this, () -> {
			if (!SwingUtilities.isEventDispatchThread()) {
				throw new IllegalStateException();
			}

			favorites.setUser(subject != null ? subject.getName() : null);
			setSearchEnabled(Application.getInstance().hasSearch());

			updateNavigation();
			updateWindowTitle();
			updateMenuBar();

			Frontend.show(Application.getInstance().createDefaultPage());
		});
	}

	public Subject getSubject() {
		return subject;
	}

	private void onFavoritesChange(LinkedHashMap<String, String> newFavorites) {
		menuBar.updateFavorites(newFavorites);
		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			SwingTab tab = (SwingTab) tabbedPane.getComponentAt(i);
			tab.updateFavorites(newFavorites);
		}
	}

	protected void updateWindowTitle() {
		String title;
		if (tabbedPane.isHideTabAreaWithOneTab() && tabbedPane.getTabCount() == 1) {
			title = tabbedPane.getTitleAt(0);
		} else {
			title = Application.getInstance().getName();
		}
		if (Backend.getInstance().isAuthenticationActive() && subject != null && !StringUtils.isEmpty(subject.getName())) {
			title = title + " - " + subject.getName();
		}
		setTitle(title);
	}

	protected void updateTitle() {
		for (int index = 0; index < tabbedPane.getTabCount(); index++) {
			SwingTab tab = (SwingTab) tabbedPane.getComponentAt(index);
			if (tab == null)
				throw new RuntimeException("Tab null");
			Page page = tab.getVisiblePage();
			if (page == null) {
				throw new RuntimeException("Page null");
			}
			tabbedPane.setTitleAt(index, page.getTitle());
		}
	}

	protected void updateIcon() {
		InputStream inputStream = Application.getInstance().getIcon();
		if (inputStream != null) {
			try {
				setIconImage(ImageIO.read(inputStream));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	protected void updateMenuBar() {
		if (Application.getInstance() instanceof SwingMenuBarProvider) {
			menuBar = ((SwingMenuBarProvider) Application.getInstance()).createMenuBar(this);
			setJMenuBar(menuBar);
		} else if (menuBar != null) {
			menuBar = new SwingMenuBar(this);
			setJMenuBar(menuBar);
		}
	}
	
	public void previous() {
		SwingTab tab = getVisibleTab();
		if (tab != null && tab.hasPast()) {
			tab.previous();
		}
	}

	public void next() {
		SwingTab tab = getVisibleTab();
		if (tab != null && tab.hasFuture()) {
			tab.next();
		}
	}

	private class SwingLoginAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		public SwingLoginAction() {
			super("LoginAction");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			SwingFrontend.run(e, Backend.getInstance().getAuthentication().getLoginAction());
		}
	}

	private class SwingLogoutAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		public SwingLogoutAction() {
			super("LogoutAction");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			SwingFrontend.run(e, () -> Frontend.getInstance().login(null));
		}
	}

	private class CloseWindowAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			tryToCloseWindow();
		}
	}

	private class ExitAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			FrameManager.getInstance().exitActionPerformed(SwingFrame.this);
		}
	}

	private class NewWindowAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			SwingFrame frame = FrameManager.getInstance().openFrame();
			SwingFrontend.run(frame, () -> frame.setSubject(subject));
		}
	}

	private class NewTabAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			addTab();
		}
	}

	private class NavigationAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		private int lastDividerLocation;

		public NavigationAction() {
			putValue(Action.SELECTED_KEY, Boolean.TRUE);
		}

		@Override
		public void actionPerformed(ActionEvent unsed) {
			if (Boolean.TRUE.equals(getValue(Action.SELECTED_KEY))) {
				splitPane.setLeftComponent(navigationTabbedPane);
				splitPane.setDividerSize((Integer) UIManager.get("SplitPane.dividerSize"));
				splitPane.setDividerLocation(lastDividerLocation);
				tabbedPane.setHideTabAreaWithOneTab(false);
			} else {
				lastDividerLocation = splitPane.getDividerLocation();
				splitPane.setLeftComponent(null);
				splitPane.setDividerSize(0);
				tabbedPane.setHideTabAreaWithOneTab(true);
			}
			updateWindowTitle();
		}
		
		public void close() {
			putValue(Action.SELECTED_KEY, false);
			actionPerformed(null);
		}
	}

	private class ToolbarAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		public ToolbarAction() {
			putValue(Action.SELECTED_KEY, Boolean.TRUE);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (Boolean.TRUE.equals(getValue(Action.SELECTED_KEY))) {
				toolBar.setVisible(true);
			} else {
				toolBar.setVisible(false);
			}
		}
	}

	private class CloseTabAction extends SwingResourceAction implements BiConsumer<JTabbedPane, Integer> {
		private static final long serialVersionUID = 1L;

		public CloseTabAction() {
			setEnabled(false);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			// first try to close detail,
			// then tab (if no detail to close),
			// then window (if only one tab left)
			if (!getVisibleTab().close()) {
				if (tabbedPane.getTabCount() > 1) {
					tabbedPane.remove(tabbedPane.getSelectedIndex());
				} else {
					// at the moment this should not happen as CloseTabAction is only enabled if tabCount > 1
					tryToCloseWindow();
				}
			}
		}
		
		@Override
		public void accept(JTabbedPane t, Integer index) {
			if (!getVisibleTab().close()) {
				tabbedPane.remove(index);
			}
		}
	}

	public void setSearchEnabled(boolean hasSearchPages) {
		toolBar.setSearchEnabled(hasSearchPages);
	}

}
