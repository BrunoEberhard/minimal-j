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

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;

import org.minimalj.application.Application;
import org.minimalj.frontend.impl.swing.toolkit.SwingFrontend;
import org.minimalj.frontend.page.EmptyPage;
import org.minimalj.frontend.page.Page;
import org.minimalj.security.Subject;
import org.minimalj.util.StringUtils;

public class SwingFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private Subject subject;
	final SwingFavorites favorites = new SwingFavorites(this::onFavoritesChange);
	
	private final JTabbedPane tabbedPane = new JTabbedPane();
	private JScrollPane navigationScrollPane;
	private SwingToolBar toolBar;
	private SwingMenuBar menuBar;
	private JTabbedPane navigationTabbedPane;
	private JSplitPane splitPane;

	public static SwingFrame activeFrameOverride = null;
	
	final Action closeWindowAction, exitAction, newWindowAction, newTabAction, closeTabAction, navigationAction;
	
	public SwingFrame() {
		closeWindowAction = new CloseWindowAction();
		closeTabAction = new CloseTabAction();
		exitAction = new ExitAction();
		newWindowAction = new NewWindowAction();
		newTabAction = new NewTabAction();
		navigationAction = new NavigationAction();
		
		tabbedPane.getModel().addChangeListener(this::tabSelectionChanged);

		setDefaultSize();
		setLocationRelativeTo(null);
		addWindowListener();
		createContent();

		addTab();

		updateIcon();
	}
	
	protected void setDefaultSize() {
		Dimension screenSize = getToolkit().getScreenSize();
		if (screenSize.width < 1280 || screenSize.height < 1024) {
			setExtendedState(MAXIMIZED_BOTH);
			setSize(screenSize.width - 20, screenSize.height - 40);
		} else {
			int width = Math.min(screenSize.width - 40, 1300);
			int height = Math.min(screenSize.height - 40, 1000);
			setSize(width, height);
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
		menuBar = new SwingMenuBar(this);
		setJMenuBar(menuBar);

		getContentPane().setLayout(new BorderLayout());

		toolBar = new SwingToolBar();
		getContentPane().add(toolBar, BorderLayout.NORTH);

		navigationScrollPane = new JScrollPane();
		navigationScrollPane.setBorder(BorderFactory.createEmptyBorder());
//		ActionListener navigationClosedListener = e -> {
//			navigationAction.putValue(Action.SELECTED_KEY, Boolean.FALSE);
//			navigationAction.actionPerformed(e);
//		};
//		decoratedNavigationPane = new SwingDecoration(Application.getInstance().getName(), navigationScrollPane, SwingDecoration.HIDE_MINIMIZE, navigationClosedListener);
		navigationTabbedPane = new JTabbedPane();
		navigationTabbedPane.addTab(Application.getInstance().getName(), navigationScrollPane);

		splitPane = new JSplitPane();
		splitPane.setBorder(BorderFactory.createEmptyBorder());
		getContentPane().add(splitPane, BorderLayout.CENTER);

		splitPane.setLeftComponent(navigationTabbedPane);
		splitPane.setRightComponent(tabbedPane);

		splitPane.setDividerLocation(200);
	}

	private void tabSelectionChanged(ChangeEvent event) {
		toolBar.setActiveTab((SwingTab) tabbedPane.getSelectedComponent());
		menuBar.setActiveTab((SwingTab) tabbedPane.getSelectedComponent());
	}

	public void updateNavigation() {
		navigationScrollPane.setViewportView(new NavigationTree(Application.getInstance().getNavigation()));
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
		if (tab == null) return null;
		return tab.getVisiblePage();
	}
	
	public List<Page> getPages() {
		List<Page> result = new ArrayList<>();
		for (int i = 0; i<tabbedPane.getTabCount(); i++) {
			SwingTab tab = (SwingTab) tabbedPane.getComponent(i); // myst: getTabComponent returns allways null
			Page page = tab.getVisiblePage();
			if (page != null) result.add(page);
		}
		return result;
	}
	
	void onHistoryChanged() {
		updateTitle();
	}
	
	public void intialize(Subject subject) {
		this.subject = subject;
		Subject.setCurrent(subject);
		favorites.setUser(subject != null ? subject.getName() : null);
		for (int i = 0; i<tabbedPane.getTabCount(); i++) {
			SwingTab swingTab = (SwingTab) tabbedPane.getComponentAt(i);
			swingTab.clearHistory();
		}
		updateNavigation();
		updateWindowTitle();
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
		String title = Application.getInstance().getName();
		if (subject != null && !StringUtils.isEmpty(subject.getName())) {
			title = title + " - " + subject.getName();
		}
		setTitle(title);
	}
	
	protected void updateTitle() {
		for (int index = 0; index<tabbedPane.getTabCount(); index++) {
			SwingTab tab = (SwingTab) tabbedPane.getComponentAt(index);
			if (tab == null) throw new RuntimeException("Tab null");
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
			SwingFrontend.run(frame, () -> frame.intialize(subject));
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
		public void actionPerformed(ActionEvent e) {
			if (Boolean.TRUE.equals(getValue(Action.SELECTED_KEY))) {
				splitPane.setLeftComponent(navigationTabbedPane);
				splitPane.setDividerSize((Integer) UIManager.get("SplitPane.dividerSize"));
				splitPane.setDividerLocation(lastDividerLocation);
			} else {
				lastDividerLocation = splitPane.getDividerLocation();
				splitPane.setLeftComponent(null);
				splitPane.setDividerSize(0);
			}
		}
	}

	private class CloseTabAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			// first try to close detail,
			// then tab (if no detail to close),
			// then window (if only one tab left)
			if (!getVisibleTab().close()) {
				if (tabbedPane.getTabCount() > 1) {
					tabbedPane.remove(tabbedPane.getSelectedIndex());
				} else {
					tryToCloseWindow();
				}
			}
		}
	}

	public void setSearchEnabled(boolean hasSearchPages) {
		toolBar.setSearchEnabled(hasSearchPages);
	}

}
