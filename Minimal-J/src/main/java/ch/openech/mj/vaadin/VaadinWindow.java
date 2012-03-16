package ch.openech.mj.vaadin;

import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;

import javax.swing.Action;

import ch.openech.mj.application.ApplicationConfig;
import ch.openech.mj.application.EmptyPage;
import ch.openech.mj.application.WindowConfig;
import ch.openech.mj.page.ActionGroup;
import ch.openech.mj.page.Page;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.page.SeparatorAction;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.vaadin.toolkit.VaadinClientToolkit;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UriFragmentUtility;
import com.vaadin.ui.UriFragmentUtility.FragmentChangedEvent;
import com.vaadin.ui.UriFragmentUtility.FragmentChangedListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class VaadinWindow extends Window implements PageContext {

	private final WindowConfig windowConfig;
	private final VerticalLayout windowContent = new VerticalLayout();
	private final MenuBar menubar = new MenuBar();
	private final ComboBox comboBox = new ComboBox();
	private final TextField textFieldSearch = new TextField();
	private Preferences preferences;
	private final UriFragmentUtility ufu;
	
	private Page visiblePage = new EmptyPage(this);
	private ComponentContainer content;
	
	public VaadinWindow(WindowConfig windowConfig) {
		this.windowConfig = windowConfig;
		
		setContent(windowContent);
		setSizeFull();
		windowContent.setSizeFull();
		setCaption(windowConfig.getTitle());
		
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
		updateMenu();
		
		if (windowConfig.getSearchClasses().length > 0) {
			Component searchComponent = createSearchField();
			nav.addComponent(searchComponent);
			nav.setComponentAlignment(searchComponent, Alignment.MIDDLE_RIGHT);
		}
		
		ufu = new UriFragmentUtility();
		ufu.addListener(new VaadinWindowFragmentChangedListener());
		windowContent.addComponent(ufu);
	}

    public VaadinWindow(VaadinWindow parentVaadinWindow) {
		this(parentVaadinWindow.windowConfig);
	}

	private Component createSearchField() {
		final HorizontalLayout horizontalLayout = new HorizontalLayout();

		comboBox.setNullSelectionAllowed(false);
		for (Class<?> searchClass: windowConfig.getSearchClasses()) {
			comboBox.addItem(searchClass);
			comboBox.setItemCaption(searchClass, Resources.getString("Search." + searchClass.getSimpleName()));
		}
		comboBox.setValue(windowConfig.getSearchClasses()[0]);
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
		ufu.setFragment(pageLink, true);
	}

	private void updateContent(ComponentContainer content) {
		if (this.content != null) {
			windowContent.removeComponent(this.content);
		}
		content.setSizeFull();
		this.content = content;
		windowContent.addComponent(content);
		windowContent.setExpandRatio(content, 1);
		
		updateMenu();
	}

	private void updateMenu() {
		ActionGroup actionGroup = new ActionGroup(null);
		PageContext pageContext = (PageContext) this;
		ApplicationConfig.getApplicationConfig().fillActionGroup(pageContext, actionGroup);
		windowConfig.fillActionGroup(pageContext, actionGroup);
		visiblePage.fillActionGroup(pageContext, actionGroup);
		updateMenu(actionGroup);
	}
	
	private void updateMenu(ActionGroup actions) {
		menubar.removeItems();
		for (Action action : actions.getActions()) {
			if (action instanceof ActionGroup) {
				ActionGroup actionGroup = (ActionGroup) action;
				MenuBar.MenuItem item = menubar.addItem((String) actionGroup.getValue(Action.NAME), null);
				fillMenu(item, actionGroup);
			}
		}
	}
	
	private void fillMenu(MenuBar.MenuItem item, ActionGroup actionGroup) {
		for (Action action : actionGroup.getActions()) {
			if (action instanceof ActionGroup) {
				ActionGroup subGroup = (ActionGroup) action;
				MenuBar.MenuItem subItem = item.addItem((String) subGroup.getValue(Action.NAME), null);
				fillMenu(subItem, subGroup);
			} else if (action instanceof SeparatorAction) {
				item.addSeparator();
			} else {
				item.addItem((String) action.getValue(Action.NAME), null, new ActionCommand(action));
			}
		}
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
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void close() {
		getWindow().executeJavaScript("history.back()");
		// ev. "var backlen=history.length; history.go(-backlen); window.location.href= window.location.href;"
	}
	
	private class VaadinWindowFragmentChangedListener implements FragmentChangedListener {

		@Override
		public void fragmentChanged(FragmentChangedEvent source) {
			String pageLink = source.getUriFragmentUtility().getFragment();
			visiblePage = Page.createPage(VaadinWindow.this, pageLink);
			Component component = VaadinClientToolkit.getComponent(visiblePage.getPanel());
			updateContent((ComponentContainer) component);
		}
	}

	@Override
	public Object getComponent() {
		return this;
	}
	
}
