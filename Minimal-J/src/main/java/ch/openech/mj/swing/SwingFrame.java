package ch.openech.mj.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultEditorKit;

import ch.openech.mj.application.ApplicationConfig;
import ch.openech.mj.application.ApplicationContext;
import ch.openech.mj.edit.EditorPage;
import ch.openech.mj.page.ActionGroup;
import ch.openech.mj.page.Page;
import ch.openech.mj.page.Page.PageListener;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.page.RefreshablePage;
import ch.openech.mj.resources.ResourceAction;
import ch.openech.mj.resources.ResourceHelper;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.swing.component.EditablePanel;
import ch.openech.mj.swing.component.HistoryPanel;
import ch.openech.mj.swing.lookAndFeel.LookAndFeelAction;
import ch.openech.mj.swing.lookAndFeel.PrintLookAndFeel;
import ch.openech.mj.swing.lookAndFeel.TerminalLookAndFeel;
import ch.openech.mj.toolkit.IComponent;

public class SwingFrame extends JFrame implements IComponent {
	private JTabbedPane tabbedPane;
	private JToolBar toolBar;
	private Action previousAction, nextAction, refreshAction, searchAction, upAction, downAction;
	private JMenuItem menuItemToolBarVisible;
	private JComboBox comboBoxSearchObject;
	private JTextField textFieldSearch;
	private HistoryPanelListener historyPanelListener = new HistoryPanelListener();

	public SwingFrame() {
		super();

		setDefaultSize();
		setLocationRelativeTo(null);
		addWindowListener();
		initActions();
		createContent();
		updateMenu();
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

	protected void initActions() {
		previousAction = new PreviousPageAction();
		nextAction = new NextPageAction();
		refreshAction = new RefreshAction();
		searchAction = new SearchAction();
		upAction = new UpAction();
		downAction = new DownAction();
	}

	protected void createContent() {
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(createToolBar(), BorderLayout.NORTH);
//		getContentPane().add(createStatusBar(), BorderLayout.SOUTH);
		getRootPane().setJMenuBar(createMenuBar());
		getContentPane().add(createTabbedPane(), BorderLayout.CENTER);
	}

	private JComponent createTabbedPane() {
		tabbedPane = new JTabbedPane();
		tabbedPane.addChangeListener(new TabSelectionListener());
		addDefaultTab();
		return tabbedPane;
	}

	/**
	 * Should be overwritten in specialized frames
	 * 
	 * @return a new instanceof of this Frame class
	 */
	public SwingFrame newFrame() {
		try {
			SwingFrame frame = this.getClass().newInstance();
			return frame;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}

	public void addDefaultTab() {
		PageContext pageContext = addTab();
		pageContext.show(Page.link());
	}
	
	public PageContext addTab() {
		PageContextImpl pageContext = new PageContextImpl();
		
		tabbedPane.addTab("", pageContext);
		tabbedPane.setSelectedComponent(pageContext);
		
		return pageContext;
	}
	
//	public PageContext addTab(Page page) {
//		HistoryPanel tabPanel = new HistoryPanel(historyPanelListener);
//		tabbedPane.addTab(page.getTitle(), page.getTitleIcon(), tabPanel, page.getTitleToolTip());
////		int newIndex = tabbedPane.getTabCount() - 1;
////		TabTitle tabTitle = new TabTitle(page.getTitle(), page.getTitleIcon(), page.getTitleToolTip());
////		tabTitle.setCloseListener(new TabCloseListener(tabPanel));
////		tabbedPane.setTabComponentAt(newIndex, tabTitle);
//		tabbedPane.setSelectedComponent(tabPanel);
//		
//		PageContext pageContext = new PageContextImpl(tabPanel);
//		pageContext.show(page);
//		return pageContext;
//	}
	
//	private class TabCloseListener implements ActionListener {
//		private HistoryPanel tabPanel;
//
//		public TabCloseListener(HistoryPanel tabPanel) {
//			this.tabPanel = tabPanel;
//		}
//
//		@Override
//		public void actionPerformed(ActionEvent e) {
//			closeTabActionPerformed(tabPanel);
//		}
//	}
	
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
		return checkClosable(getVisiblePageContext());
	}
	
	private boolean checkClosable(PageContextImpl pageContextImpl) {
		HistoryPanel historyPanel = pageContextImpl.getHistoryPanel();
		Page visiblePage = historyPanel.getPresent();
		if (visiblePage instanceof EditorPage) {
			EditorPage editorPage = (EditorPage) visiblePage;
			tabbedPane.setSelectedComponent(pageContextImpl);
			editorPage.checkedClose();
		}
		return true;
	}
	
	public boolean tryToCloseWindow() {
		boolean closable = true;
		for (int i = tabbedPane.getTabCount()-1; i>=0; i--) {
			PageContextImpl pageContextImpl = (PageContextImpl) tabbedPane.getComponentAt(i);
			closable = checkClosable(pageContextImpl);
			if (!closable) return false;
		}
		closeWindow();
		return true;
	}
	
	public void closeTab(PageContextImpl pageContextImpl) {
		tabbedPane.remove(pageContextImpl);
	}
	
	public void closeWindow() {
		for (int i = tabbedPane.getTabCount()-1; i>=0; i--) {
			PageContextImpl pageContextImpl = (PageContextImpl)tabbedPane.getComponentAt(i);
			closeTab(pageContextImpl);
		}
		
		setVisible(false);
	}

	public PageContextImpl getVisiblePageContext() {
		return (PageContextImpl) tabbedPane.getSelectedComponent();
	}
	
	public void closeTab() {
		tabbedPane.remove(getVisiblePageContext());
	}

	public Page getVisiblePage() {
		PageContextImpl pageContextImpl = getVisiblePageContext();
		if (pageContextImpl == null) return null;
		return pageContextImpl.getVisiblePage();
	}
	
	public List<Page> getPages() {
		List<Page> result = new ArrayList<Page>();
		for (int i = 0; i<tabbedPane.getTabCount(); i++) {
			PageContextImpl pageContextImpl = (PageContextImpl) tabbedPane.getComponent(i); // myst: getTabComponent returns allways null
			Page page = pageContextImpl.getVisiblePage();
			if (page != null) result.add(page);
		}
		return result;
	}
	
	protected void updateWindowTitle() {
		PageContext pageContext = getVisiblePageContext();
		setTitle(ApplicationConfig.getApplicationConfig().getWindowTitle(pageContext));
	}
	
	protected void updateActions() {
		HistoryPanel historyPanel = getVisiblePageContext().getHistoryPanel();
		if (historyPanel != null  && getVisiblePage() != null && !getVisiblePage().isExclusive()) {
			previousAction.setEnabled(historyPanel.hasPast());
			nextAction.setEnabled(historyPanel.hasFuture());
			refreshAction.setEnabled(getVisiblePage() instanceof RefreshablePage);
			upAction.setEnabled(!getVisiblePageContext().top());
			downAction.setEnabled(!getVisiblePageContext().bottom());
		} else {
			previousAction.setEnabled(false);
			nextAction.setEnabled(false);
			refreshAction.setEnabled(false);
			upAction.setEnabled(false);
			downAction.setEnabled(false);
		}
	}
	
	protected void updateMenu() {
		ActionGroup actionGroup = new ActionGroup(null);
		fillMenu(actionGroup);
		
		PageContext pageContext = getVisiblePageContext();
		ApplicationConfig.getApplicationConfig().fillActionGroup(pageContext, actionGroup);
		
		Page visiblePage = getVisiblePage();
		if (visiblePage != null) {
			visiblePage.fillActionGroup(pageContext, actionGroup);
		}

		updateMenu(actionGroup);
	}
	
	private void fillMenu(ActionGroup actionGroup) {
		ActionGroup file = actionGroup.getOrCreateActionGroup(ActionGroup.FILE);
		fillFileMenu(file);
	
		ActionGroup edit = actionGroup.getOrCreateActionGroup(ActionGroup.EDIT);
		fillEditMenu(edit);
		
		ActionGroup view = actionGroup.getOrCreateActionGroup(ActionGroup.VIEW);
		fillViewMenu(view);
		
		actionGroup.getOrCreateActionGroup(ActionGroup.OBJECT);

		ActionGroup window = actionGroup.getOrCreateActionGroup(ActionGroup.WINDOW);
		fillWindowMenu(window);
		
		ActionGroup help = actionGroup.getOrCreateActionGroup(ActionGroup.HELP);
		fillHelpMenu(help);
	}

	private void updateMenu(ActionGroup actions) {
		JMenuBar menuBar = new JMenuBar();
		for (Action action : actions.getActions()) {
			if (action instanceof ActionGroup) {
				ActionGroup actionGroup = (ActionGroup) action;
				if (!actionGroup.getActions().isEmpty()) {
					JMenu menu = new JMenu((String) actionGroup.getValue(Action.NAME));
					// menu.setMnemonic((String) actionGroup.getValue(Action.MNEMONIC_KEY));
					menuBar.add(menu);
					fillMenu(menu, actionGroup);
				}
			}
		}
		getRootPane().setJMenuBar(menuBar);
		getRootPane().validate();
	}
	
	private void fillMenu(JMenu menu, ActionGroup actionGroup) {
		for (Action action : actionGroup.getActions()) {
			if (action instanceof ActionGroup) {
				ActionGroup subGroup = (ActionGroup) action;
				if (!actionGroup.getActions().isEmpty()) {
					JMenu subMenu = new JMenu((String) subGroup.getValue(Action.NAME));
					fillMenu(subMenu, subGroup);
					menu.add(subMenu);
				}
			} else if (action.getValue(Action.NAME) != null) {
				JMenuItem menuItem = new JMenuItem(action);
				menu.add(menuItem);
			} else {
				menu.addSeparator();
			}
		}
	}
	
	protected void updateTitle() {
		for (int index = 0; index<tabbedPane.getTabCount(); index++) {
			PageContextImpl pageContextImpl = (PageContextImpl) tabbedPane.getComponent(index);
			Page page = pageContextImpl.getVisiblePage();
			tabbedPane.setTitleAt(index, page.getTitle());
			tabbedPane.setIconAt(index, page.getTitleIcon());
			tabbedPane.setToolTipTextAt(index, page.getTitleToolTip());
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

	protected class PreviousPageAction extends ResourceAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			previousPage();
		}
	}
	
	protected void previousPage() {
		getVisiblePageContext().getHistoryPanel().previous();
	}

	protected class NextPageAction extends ResourceAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			nextPage();
		}
	}

	protected class SearchAction extends ResourceAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			Class<? extends Page> searchObject = (Class<? extends Page>) comboBoxSearchObject.getSelectedItem();
			String text = textFieldSearch.getText();
			search(searchObject, text);
		}
	}
	
	public void search(Class<? extends Page> searchClass, String text) {
		getVisiblePageContext().show(Page.link(searchClass, text));
	}
	
	protected void nextPage() {
		getVisiblePageContext().getHistoryPanel().next();
	}
	
	protected class UpAction extends ResourceAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			getVisiblePageContext().up();
		}
	}

	protected class DownAction extends ResourceAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			getVisiblePageContext().down();
		}
	}

	private JToolBar createToolBar() {
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		fillToolBar(toolBar);
		return toolBar;
	}
	
	protected void fillToolBar(JToolBar toolBar) {
		fillToolBarNavigation(toolBar);
		fillToolBarRefresh(toolBar);
		fillToolBarUpDown(toolBar);
		fillToolBarSearch(toolBar);
	}
	
	protected void fillToolBarNavigation(JToolBar toolBar) {
		toolBar.add(previousAction);
		toolBar.add(nextAction);
	}
	
	protected void fillToolBarRefresh(JToolBar toolBar) {
		toolBar.add(refreshAction);
	}
	
	protected void fillToolBarUpDown(JToolBar toolBar) {
		toolBar.add(upAction);
		toolBar.add(downAction);
	}
	
	protected void fillToolBarSearch(JToolBar toolBar) {
		if (ApplicationConfig.getApplicationConfig().getSearchClasses().length > 0) {
			toolBar.add(createSearchField());
		}
	}

	protected JPanel createSearchField() {
		FlowLayout flowLayout = new FlowLayout(FlowLayout.TRAILING);
		flowLayout.setAlignOnBaseline(true);
		JPanel panel = new JPanel(flowLayout);
		comboBoxSearchObject = new JComboBox(ApplicationConfig.getApplicationConfig().getSearchClasses());
		comboBoxSearchObject.setRenderer(new SearchCellRenderer());
		panel.add(comboBoxSearchObject);
		textFieldSearch = new JTextField();
		textFieldSearch.setPreferredSize(new Dimension(200, textFieldSearch.getPreferredSize().height));
		panel.add(textFieldSearch);
		final JButton button = new JButton(searchAction);
		button.setHideActionText(true);
		panel.add(button);
		textFieldSearch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				button.doClick();
			}
		});
		return panel;
	}
	
	private static class SearchCellRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Class<?> searchClass = (Class<?>) value;
			value = Resources.getString("Search." + searchClass.getSimpleName());
			return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		}
	}
	
	private class HistoryPanelListener implements HistoryPanel.HistoryPanelListener {
		@Override
		public void onHistoryChanged() {
			updateWindowTitle();
			 updateActions();
			 updateMenu();
			 updateTitle();
		}
	}

	private class TabSelectionListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			if (getVisiblePageContext() != null) {
				updateActions();
				updateMenu();
			}
		}
	}

	//
	
	public JMenuBar createMenuBar() {
		return new JMenuBar();
	}
	
	protected void fillFileMenu(ActionGroup actionGroup) {
		actionGroup.getOrCreateActionGroup(ActionGroup.NEW);
		actionGroup.addSeparator();
		actionGroup.add(new CloseWindowAction());
		actionGroup.add(new CloseTabAction());
		actionGroup.addSeparator();
		actionGroup.getOrCreateActionGroup(ActionGroup.IMPORT);
		actionGroup.getOrCreateActionGroup(ActionGroup.EXPORT);
		actionGroup.addSeparator();
		actionGroup.add(new ExitAction());
	}
	
	protected class CloseTabAction extends ResourceAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			closeTabActionPerformed();
		}
	}
	
	protected class CloseWindowAction extends ResourceAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			FrameManager.getInstance().closeWindowPerformed(SwingFrame.this);
		}
	}
	
	protected class ExitAction extends ResourceAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			FrameManager.getInstance().exitActionPerformed(SwingFrame.this);
		}
	}

	protected static class NewWindowAction extends ResourceAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			FrameManager.getInstance().openNavigationFrame();
		}
	}

	protected class NewTabAction extends ResourceAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			addDefaultTab();
		}
	}

	private void fillEditMenu(ActionGroup actionGroup) {
		actionGroup.add(ResourceHelper.initProperties(new DefaultEditorKit.CutAction(), Resources.getResourceBundle(), "cut"));
		actionGroup.add(ResourceHelper.initProperties(new DefaultEditorKit.CopyAction(), Resources.getResourceBundle(), "copy"));
		actionGroup.add(ResourceHelper.initProperties(new DefaultEditorKit.PasteAction(), Resources.getResourceBundle(), "paste"));
	}

	protected void fillViewMenu(ActionGroup actionGroup) {
		actionGroup.add(previousAction);
		actionGroup.add(nextAction);
		actionGroup.add(refreshAction);
		actionGroup.addSeparator();
		// TODO Toolbar toggle in SwingFrame
//		menuItemToolBarVisible = new MenuItemToolBarVisible();
//		actionGroup.add(menuItemToolBarVisible);
//		actionGroup.addSeparator();
		ActionGroup lookAndFeel = actionGroup.getOrCreateActionGroup("lookAndFeel");
		fillLookAndFeelMenu(lookAndFeel);
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
	
	private void fillLookAndFeelMenu(ActionGroup actionGroup) {
		actionGroup.add(new LookAndFeelAction("Normal"));
		actionGroup.add(new LookAndFeelAction("Hoher Kontrast", TerminalLookAndFeel.class.getName()));
		actionGroup.add(new LookAndFeelAction("Druckbar", PrintLookAndFeel.class.getName()));
	}
	
	protected void fillWindowMenu(ActionGroup actionGroup) {
		actionGroup.add(new NewWindowAction());
		actionGroup.add(new NewTabAction());
	}
	
	protected void fillHelpMenu(ActionGroup actionGroup) {
		// 
	}
	
	// TODO Swing PageContext should ensure use of EventDispatchThread in every need case
	private class PageContextImpl extends EditablePanel implements PageContext, PageListener {
		private final HistoryPanel historyPanel;
		private List<String> pageLinks;
		private int indexInPageLinks;
		
		public PageContextImpl() {
			historyPanel = new HistoryPanel(historyPanelListener);
			JComponent component = (JComponent) historyPanel.getComponent();
			
			setContent(component);
		}

		public HistoryPanel getHistoryPanel() {
			return historyPanel;
		}

		@Override
		public PageContext addTab() {
			return SwingFrame.this.addTab();
		}

		@Override
		public void closeTab() {
			if (historyPanel.hasPast()) {
				historyPanel.previous();
				historyPanel.dropFuture();
			} else {
				SwingFrame.this.closeTab(this);
			}
		}

		public Page getVisiblePage() {
			return historyPanel.getPresent();
		}

		@Override
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
				if (getVisiblePage() != null && getVisiblePage().isExclusive()) {
					PageContext newPageContext = addTab();
					newPageContext.show(pageLink);
				} else {
					Page page = Page.createPage(PageContextImpl.this, pageLink);
					historyPanel.add(page);
				}
			}
		}

		@Override
		public void show(List<String> pageLinks, int index) {
			this.pageLinks = pageLinks;
			this.indexInPageLinks = index;
			show(pageLinks.get(indexInPageLinks));
		}
		
		public boolean top() {
			return pageLinks == null ||indexInPageLinks == 0;
		}

		public boolean bottom() {
			return pageLinks == null || indexInPageLinks == pageLinks.size() - 1;
		}

		public void up() {
			Page page = Page.createPage(this, pageLinks.get(--indexInPageLinks));
			historyPanel.replace(page);
		}

		public void down() {
			Page page = Page.createPage(this, pageLinks.get(++indexInPageLinks));
			historyPanel.replace(page);
		}

		@Override
		public void onPageTitleChanged(Page page) {
			for (int index = 0; index < tabbedPane.getTabCount(); index++) {
				if (historyPanel.equals(tabbedPane.getComponentAt(index))) {
					tabbedPane.setTitleAt(index, page.getTitle());
					tabbedPane.setIconAt(index, page.getTitleIcon());
					tabbedPane.setToolTipTextAt(index, page.getTitleToolTip());
				}
			}
		}

		@Override
		public IComponent getComponent() {
			return historyPanel.getComponent();
		}

		@Override
		public ApplicationContext getApplicationContext() {
			return SwingApplication.getApplicationContext();
		}

	}
	
}
