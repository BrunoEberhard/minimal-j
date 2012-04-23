package ch.openech.mj.vaadin;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Action;

import ch.openech.mj.application.ApplicationConfig;
import ch.openech.mj.application.ApplicationContext;
import ch.openech.mj.page.ActionGroup;
import ch.openech.mj.page.Page;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.page.SeparatorAction;
import ch.openech.mj.resources.ResourceAction;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.util.StringUtils;
import ch.openech.mj.vaadin.toolkit.VaadinClientToolkit;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UriFragmentUtility;
import com.vaadin.ui.UriFragmentUtility.FragmentChangedEvent;
import com.vaadin.ui.UriFragmentUtility.FragmentChangedListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class VaadinWindow extends Window implements PageContext {

	private final VerticalLayout windowContent = new VerticalLayout();
	private final MenuBar menubar = new MenuBar();
	private final ComboBox comboBox = new ComboBox();
	private final TextField textFieldSearch = new TextField();
	private final UriFragmentUtility ufu;
	
	private Page visiblePage;
	private Component content;
	private Panel scrollablePanel;
	private List<String> pageLinks;
	private int indexInPageLinks;
	
	public VaadinWindow() {
		setContent(windowContent);
		setSizeFull();
		windowContent.setSizeFull();
		updateWindowTitle();
		
		HorizontalLayout nav = new HorizontalLayout();
		windowContent.addComponent(nav);
		nav.setHeight("32px");
		nav.setWidth("100%");
		nav.setStyleName("topbar");
		nav.setSpacing(true);
		nav.setMargin(false, true, false, false);

		nav.addComponent(menubar);
		nav.setExpandRatio(menubar, 1.0F);
		nav.setComponentAlignment(menubar, Alignment.MIDDLE_LEFT);
		
		if (ApplicationConfig.getApplicationConfig().getSearchClasses().length > 0) {
			Component searchComponent = createSearchField();
			nav.addComponent(searchComponent);
			nav.setComponentAlignment(searchComponent, Alignment.MIDDLE_RIGHT);
		}
		
		ufu = new UriFragmentUtility();
		ufu.addListener(new VaadinWindowFragmentChangedListener());
		windowContent.addComponent(ufu);
		
		scrollablePanel = new Panel();
		scrollablePanel.setScrollable(true);
		scrollablePanel.setSizeFull();
	}

	private Component createSearchField() {
		final HorizontalLayout horizontalLayout = new HorizontalLayout();

		comboBox.setNullSelectionAllowed(false);
		for (Class<?> searchClass: ApplicationConfig.getApplicationConfig().getSearchClasses()) {
			comboBox.addItem(searchClass);
			comboBox.setItemCaption(searchClass, Resources.getString("Search." + searchClass.getSimpleName()));
		}
		comboBox.setValue(ApplicationConfig.getApplicationConfig().getSearchClasses()[0]);
		horizontalLayout.addComponent(comboBox);
		
        textFieldSearch.setWidth("160px");
        horizontalLayout.addComponent(textFieldSearch);
        
        textFieldSearch.addShortcutListener(new ShortcutListener("Search", ShortcutAction.KeyCode.ENTER, null) {
			@Override
			public void handleAction(Object sender, Object target) {
				showSearchPage();
			}
		});
        
        Button button = new Button("Suche");
        button.addListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				showSearchPage();
			}
		});
        horizontalLayout.addComponent(button);
        
        return horizontalLayout;
    }

	@SuppressWarnings("unchecked")
	private void showSearchPage() {
		show(Page.link((Class<? extends Page>) comboBox.getValue(), (String) textFieldSearch.getValue()));
	}
	
	@Override
	public void show(String pageLink) {
		boolean sameAsExisting = StringUtils.equals(ufu.getFragment(), pageLink);
		if (!sameAsExisting) {
			ufu.setFragment(pageLink, true);
		} else {
			updateContent(pageLink);
		}
	}

	private void updateContent(String pageLink) {
		visiblePage = Page.createPage(VaadinWindow.this, pageLink);
		Component component = VaadinClientToolkit.getComponent(visiblePage.getPanel());
		updateContent(component);
	}
	
	private void updateContent(Component content) {
		if (this.content != null) {
			// warum funktioniert windowContent.remove(content) nicht ??
			windowContent.removeComponent(windowContent.getComponent(windowContent.getComponentCount()-1));
		}

		if (content instanceof Table) {
			this.content = content;
			windowContent.addComponent(content);
			windowContent.setExpandRatio(content, 1);
		} else {
			scrollablePanel.removeAllComponents();
			scrollablePanel.addComponent(content);
			this.content = scrollablePanel;
			windowContent.addComponent(scrollablePanel);
			windowContent.setExpandRatio(scrollablePanel, 1);
		}
		this.content.setSizeFull();
		
		updateMenu();
		updateWindowTitle();
	}

	private void updateMenu() {
		menubar.removeItems();
		
		ActionGroup actionGroup = new ActionGroup(null);
		fillMenu(actionGroup);
		
		PageContext pageContext = (PageContext) this;
		ApplicationConfig.getApplicationConfig().fillActionGroup(pageContext, actionGroup);
		visiblePage.fillActionGroup(pageContext, actionGroup);
		
		updateMenu(actionGroup);
	}
	
	private void updateMenu(ActionGroup actions) {
		for (Action action : actions.getActions()) {
			if (action instanceof ActionGroup) {
				ActionGroup actionGroup = (ActionGroup) action;
				if (!actionGroup.getActions().isEmpty()) {
					MenuBar.MenuItem item = menubar.addItem((String) actionGroup.getValue(Action.NAME), null);
					fillMenu(item, actionGroup);
				}
			}
		}
	}
	
	private void fillMenu(MenuBar.MenuItem item, ActionGroup actionGroup) {
		for (Action action : actionGroup.getActions()) {
			if (action instanceof ActionGroup) {
				ActionGroup subGroup = (ActionGroup) action;
				if (!actionGroup.getActions().isEmpty()) {
					MenuBar.MenuItem subItem = item.addItem((String) subGroup.getValue(Action.NAME), null);
					fillMenu(subItem, subGroup);
				}
			} else if (action instanceof SeparatorAction) {
				item.addSeparator();
			} else {
				if (!Boolean.FALSE.equals(action.getValue("visible"))) {
					MenuBar.MenuItem menuItem = item.addItem((String) action.getValue(Action.NAME), null, new ActionCommand(action));
					menuItem.setEnabled(!Boolean.FALSE.equals(action.getValue("enabled")));
				}
			}
		}
	}
	
//	
//	private static void installAdditionalActionListener(Action action, final MenuBar.MenuItem menuItem) {
//		menuItem.setVisible(!Boolean.FALSE.equals(action.getValue("visible")));
//		menuItem.setEnabled(!Boolean.FALSE.equals(action.getValue("enabled")));
//		action.addPropertyChangeListener(new PropertyChangeListener() {
//			@Override
//			public void propertyChange(PropertyChangeEvent evt) {
//				if ("visible".equals(evt.getPropertyName()) && (evt.getNewValue() instanceof Boolean)) {
//					menuItem.setVisible((Boolean) evt.getNewValue());
//				} else if ("enabled".equals(evt.getPropertyName()) && (evt.getNewValue() instanceof Boolean)) {
//					menuItem.setEnabled((Boolean) evt.getNewValue());
//				}
//			}
//		});
//	}
	
	private void fillMenu(ActionGroup actionGroup) {
		ActionGroup file = actionGroup.getOrCreateActionGroup(ActionGroup.FILE);
		fillFileMenu(file);
	
		actionGroup.getOrCreateActionGroup(ActionGroup.OBJECT);
		ActionGroup window = actionGroup.getOrCreateActionGroup(ActionGroup.WINDOW);
		fillWindowMenu(window);
		
		ActionGroup help = actionGroup.getOrCreateActionGroup(ActionGroup.HELP);
		fillHelpMenu(help);
	}
	
	protected void fillFileMenu(ActionGroup actionGroup) {
		actionGroup.getOrCreateActionGroup(ActionGroup.NEW);
		actionGroup.addSeparator();
		actionGroup.getOrCreateActionGroup(ActionGroup.IMPORT);
		actionGroup.getOrCreateActionGroup(ActionGroup.EXPORT);
	}
	
	protected void fillWindowMenu(ActionGroup actionGroup) {
		if (!top()) {
			actionGroup.add(new UpAction());
		}
		if (!bottom()) {
			actionGroup.add(new DownAction());
		}
	}
	
	protected class UpAction extends ResourceAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			up();
		}
	}

	protected class DownAction extends ResourceAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			down();
		}
	}
	
	protected void fillHelpMenu(ActionGroup actionGroup) {
		// 
	}
	
	private class ActionCommand implements Command {
		private final Action action;
		
		public ActionCommand(Action action) {
			this.action = action;
		}

		@Override
		public void menuSelected(MenuItem selectedItem) {
			action.actionPerformed(new ActionEvent(menubar, 0, null));
		}
	}

	@Override
	public PageContext addTab() {
		// How ?
		// didnt work that good:
//			VaadinWindow parentVaadinWindow = (VaadinWindow) parentPageContext;
//			VaadinWindow vaadinWindow = new VaadinWindow();
//			parentVaadinWindow.open(new ExternalResource(vaadinWindow.getURL()), "_new");
//			return vaadinWindow;
		return null;
	}
	
	@Override
	public void close() {
		getWindow().executeJavaScript("history.back()");
		// ev. "var backlen=history.length; history.go(-backlen); window.location.href= window.location.href;"
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
		// getWindow().executeJavaScript("history.back()");
		show(pageLinks.get(--indexInPageLinks));
	}

	public void down() {
		// getWindow().executeJavaScript("history.back()");
		show(pageLinks.get(++indexInPageLinks));
	}
	
	protected void updateWindowTitle() {
		setCaption(ApplicationConfig.getApplicationConfig().getWindowTitle(this));
	}
	
	private class VaadinWindowFragmentChangedListener implements FragmentChangedListener {

		@Override
		public void fragmentChanged(FragmentChangedEvent source) {
			String pageLink = source.getUriFragmentUtility().getFragment();
			updateContent(pageLink);
		}
	}

	@Override
	public Object getComponent() {
		return this;
	}

	@Override
	public ApplicationContext getApplicationContext() {
		return ((MinimalJVaadinApplication) getApplication()).getApplicationContext();
	}
	
}
