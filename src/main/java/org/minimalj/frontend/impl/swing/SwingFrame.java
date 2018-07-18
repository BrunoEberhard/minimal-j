package org.minimalj.frontend.impl.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
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
import javax.swing.JComponent;
import javax.swing.JFrame;

import org.minimalj.application.Application;
import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.swing.component.HideableTabbedPane;
import org.minimalj.frontend.page.EmptyPage;
import org.minimalj.frontend.page.Page;
import org.minimalj.security.Authentication.LoginListener;
import org.minimalj.security.Subject;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

public class SwingFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private Subject subject;
	final SwingFavorites favorites = new SwingFavorites(this::onFavoritesChange);
	
	public static SwingFrame activeFrameOverride = null;
	
	private HideableTabbedPane tabbedPane;
	final Action loginAction, closeWindowAction, exitAction, newWindowAction, newWindowWithLoginAction, newTabAction;
	
	public SwingFrame() {
		boolean authenticationActive = Backend.getInstance().isAuthenticationActive();
		loginAction = authenticationActive ? new SwingLoginAction() : null;
		
		closeWindowAction = new CloseWindowAction();
		exitAction = new ExitAction();
		newWindowAction = new NewWindowAction();
		newWindowWithLoginAction = authenticationActive ? new NewWindowWithLoginAction() : null;
		newTabAction = new NewTabAction();
		
		setDefaultSize();
		setLocationRelativeTo(null);
		addWindowListener();
		createContent();

		updateIcon();
		
		// TODO still necessary?
		getRootPane().putClientProperty(SwingFrame.class.getSimpleName(), this);
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
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(createTabbedPane(), BorderLayout.CENTER);
	}

	private JComponent createTabbedPane() {
		tabbedPane = new HideableTabbedPane();
		addTab();
		return tabbedPane;
	}

	private void addTab() {
		SwingTab tab = new SwingTab(this);	
		tabbedPane.addTab("", tab);
		tabbedPane.setSelectedComponent(tab);

		tab.show(new EmptyPage());
	}
	
	public void closeTabActionPerformed() {
		if (getVisibleTab().tryToClose()) {
			closeTab();
			if (tabbedPane.getTabCount() == 0) {
				if (FrameManager.getInstance().askBeforeCloseLastWindow(this)) {
					FrameManager.getInstance().lastTabClosed(SwingFrame.this);
				} else {
					addTab();
				}
			}
		}
	}
	
	public boolean tryToCloseWindow() {
		boolean closable = true;
		for (int i = tabbedPane.getTabCount()-1; i>=0; i--) {
			SwingTab tab = (SwingTab) tabbedPane.getTab(i);
			tabbedPane.setSelectedComponent(tab);
			closable = tab.tryToClose();
			if (!closable) return false;
		}
		closeWindow();
		return true;
	}
	
	public void closeTab(SwingTab tab) {
		tabbedPane.removeTab(tab);
	}
	
	private void closeAllTabs() {
		tabbedPane.removeAllTabs();
	}
	
	public void closeWindow() {
		closeAllTabs();
		setVisible(false);
	}

	public static SwingFrame getActiveWindow() {
		if (activeFrameOverride != null) {
			return activeFrameOverride;
		}
		for (Window w : Window.getWindows()) {
			if (w.isActive()) {
				return (SwingFrame) w;
			}
		}
		return null;
	}

	public SwingTab getVisibleTab() {
		SwingTab tab = (SwingTab) tabbedPane.getSelectedComponent();
		return tab;
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
		List<Page> result = new ArrayList<Page>();
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
	
	public void setSubject(Subject subject) {
		this.subject = subject;
		// Maybe getNavigation in Application should be extended by a subject parameter
		// then next line would not be needed
		Subject.setCurrent(subject);
		favorites.setUser(subject != null ? subject.getName() : null);
		for (int i = 0; i<tabbedPane.getTabCount(); i++) {
			SwingTab swingTab = (SwingTab) tabbedPane.getTab(i);
			swingTab.clearHistory();
			swingTab.show(Application.getInstance().createDefaultPage());
			swingTab.updateNavigation();
		}
		updateWindowTitle();
	}

	public Subject getSubject() {
		return subject;
	}
	
	private void onFavoritesChange(LinkedHashMap<String, String> newFavorites) {
		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			SwingTab tab = (SwingTab) tabbedPane.getTab(i);
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
			SwingTab tab = (SwingTab) tabbedPane.getTab(index);
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
	
	private class SwingLoginAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		public SwingLoginAction() {
			super("LoginAction");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if (tabbedPane.getTabCount() > 1) {
				Frontend.showMessage(Resources.getString("Login.moreThanOneTab"));
				return;
			}
			LoginListener listener = new LoginListener() {
				@Override
				public void loginSucceded(Subject subject) {
					setSubject(subject);
				}
				
				@Override
				public void loginCancelled() {
					// nothing to do. just go on.
				}
			};
			Backend.getInstance().getAuthentication().login(listener);
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
			FrameManager.getInstance().openNavigationFrame(subject);
		}
	}

	private class NewWindowWithLoginAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			FrameManager.getInstance().openNavigationFrame(null);
		}
	}

	private class NewTabAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			addTab();
		}
	}

}
