package ch.openech.mj.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;

import ch.openech.mj.application.ApplicationConfig;
import ch.openech.mj.edit.EditorPage;
import ch.openech.mj.page.Page;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.resources.ResourceAction;
import ch.openech.mj.swing.component.HideableTabbedPane;
import ch.openech.mj.toolkit.IComponent;

public class SwingFrame extends JFrame implements IComponent {
	private HideableTabbedPane tabbedPane;
	final Action closeWindowAction, exitAction, newWindowAction, newTabAction;
	
	public SwingFrame() {
		super();

		closeWindowAction = new CloseWindowAction();
		exitAction = new ExitAction();
		newWindowAction = new NewWindowAction();
		newTabAction = new NewTabAction();
		
		setDefaultSize();
		setLocationRelativeTo(null);
		addWindowListener();
		createContent();
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
		addDefaultTab();
		return tabbedPane;
	}

	public void addDefaultTab() {
		SwingTab tab = addTab();
		tab.show(Page.link());
	}
	
	public SwingTab addTab() {
		SwingTab tab = new SwingTab(this);
		
		tabbedPane.addTab("", tab);
		tabbedPane.setSelectedComponent(tab);
		
		return tab;
	}
	
	public void refresh() {
		for (int i = tabbedPane.getTabCount()-1; i>=0; i--) {
			SwingTab tab = (SwingTab) tabbedPane.getTab(i);
			tab.refresh();
		}
	}

	public void closeTabActionPerformed() {
		if (checkClosable()) {
			closeTab();
			if (tabbedPane.getTabCount() == 0) {
				if (FrameManager.getInstance().askBeforeCloseLastWindow(this)) {
					FrameManager.getInstance().lastTabClosed(SwingFrame.this);
				} else {
					addDefaultTab();
				}
			}
		}
	}
	
	/**
	 * Check if actual page can be closed or replaced
	 * 
	 * @return true if visible Page (the one the selected Tab) is not
	 * a EditorPage or if the user agrees to save or cancel the editing
	 */
	public boolean checkClosable() {
		return checkClosable(getVisibleTab());
	}
	
	private boolean checkClosable(SwingTab tab) {
		Page visiblePage = tab.getPresent();
		if (visiblePage instanceof EditorPage) {
			EditorPage editorPage = (EditorPage) visiblePage;
			tabbedPane.setSelectedComponent(tab);
			editorPage.checkedClose();
		}
		return true;
	}
	
	public boolean tryToCloseWindow() {
		boolean closable = true;
		for (int i = tabbedPane.getTabCount()-1; i>=0; i--) {
			SwingTab tab = (SwingTab) tabbedPane.getTab(i);
			closable = checkClosable(tab);
			if (!closable) return false;
		}
		closeWindow();
		return true;
	}
	
	public void closeTab(SwingTab tab) {
		tabbedPane.removeTab(tab);
	}
	
	public void closeWindow() {
		for (int i = tabbedPane.getTabCount()-1; i>=0; i--) {
			SwingTab tab = (SwingTab)tabbedPane.getTab(i);
			closeTab(tab);
		}
		
		setVisible(false);
	}

	public SwingTab getVisibleTab() {
		return (SwingTab) tabbedPane.getSelectedComponent();
	}
	
	public void closeTab() {
		closeTab((SwingTab) tabbedPane.getSelectedComponent());
	}

	public Page getVisiblePage() {
		SwingTab tab = getVisibleTab();
		if (tab == null) return null;
		return tab.getPresent();
	}
	
	public List<Page> getPages() {
		List<Page> result = new ArrayList<Page>();
		for (int i = 0; i<tabbedPane.getTabCount(); i++) {
			SwingTab tab = (SwingTab) tabbedPane.getComponent(i); // myst: getTabComponent returns allways null
			Page page = tab.getPresent();
			if (page != null) result.add(page);
		}
		return result;
	}
	
	void onHistoryChanged() {
		updateWindowTitle();
		updateTitle();
	}
	
	protected void updateWindowTitle() {
		PageContext pageContext = getVisibleTab();
		setTitle(ApplicationConfig.getApplicationConfig().getWindowTitle(pageContext));
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
			tabbedPane.setIconAt(index, page.getTitleIcon());
			tabbedPane.setToolTipTextAt(index, page.getTitleToolTip());
		}
	}
	
	private class CloseWindowAction extends ResourceAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			tryToCloseWindow();
		}
	}
	
	private class ExitAction extends ResourceAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			FrameManager.getInstance().exitActionPerformed(SwingFrame.this);
		}
	}

	private static class NewWindowAction extends ResourceAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			FrameManager.getInstance().openNavigationFrame();
		}
	}

	private class NewTabAction extends ResourceAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			addDefaultTab();
		}
	}

}
