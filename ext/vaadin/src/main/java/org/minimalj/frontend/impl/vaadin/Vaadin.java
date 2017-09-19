package org.minimalj.frontend.impl.vaadin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.minimalj.application.Application;
import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.Action.ActionChangeListener;
import org.minimalj.frontend.action.ActionGroup;
import org.minimalj.frontend.action.Separator;
import org.minimalj.frontend.impl.swing.component.SwingDecoration;
import org.minimalj.frontend.impl.util.PageStore;
import org.minimalj.frontend.impl.vaadin.toolkit.VaadinDialog;
import org.minimalj.frontend.impl.vaadin.toolkit.VaadinEditorLayout;
import org.minimalj.frontend.impl.vaadin.toolkit.VaadinSearchPanel;
import org.minimalj.frontend.page.IDialog;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.PageManager;
import org.minimalj.security.Authentication.LoginListener;
import org.minimalj.security.AuthenticationFailedPage;
import org.minimalj.security.Subject;
import org.minimalj.util.StringUtils;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.contextmenu.ContextMenu;
import com.vaadin.contextmenu.MenuItem;
import com.vaadin.data.TreeData;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Theme("mjtheme")
@Widgetset("org.minimalj.frontend.impl.vaadin.MjWidgetSet")
// @Widgetset("com.vaadin.DefaultWidgetSet")
public class Vaadin extends UI implements PageManager {
	private static final long serialVersionUID = 1L;

//	private final HistoryExtension history;
	private final PageStore pageStore = new PageStore();
	private Map<String, String> state; // position -> page id

	private HorizontalSplitPanel splitPanel;
	private VerticalLayout verticalScrollPane;
	
	private Tree<Action> navigationTree;
	private TreeData<Action> navigationTreeData = new TreeData<>();
	private float lastSplitPosition = -1;
	
	public Vaadin() {
//		PopStateListener myPopStateListener = new PopStateListener() {
//			@Override
//			public void popState(PopStateEvent event) {
//				if (event.getStateAsJson() != null) {
//					state = event.getStateAsMap();
//					updateContent();				
//				}
//			}
//		};
//		history = HistoryExtension.extend(this, myPopStateListener);
//		history.addPopStateListener(myPopStateListener);
	}
	
	@Override
	protected void init(VaadinRequest request) {
		VerticalLayout outerPanel = new VerticalLayout();
		outerPanel.setMargin(false);
		outerPanel.setSpacing(false);
		
		setContent(outerPanel);
		setSizeFull();
		outerPanel.setSizeFull();
		
		HorizontalLayout topbar = new HorizontalLayout();
		outerPanel.addComponent(topbar);
		outerPanel.setExpandRatio(topbar, 0f);
		topbar.setHeight("5ex");
		topbar.setWidth("100%");
		topbar.setStyleName("topbar");
		topbar.setSpacing(true);
		topbar.setMargin(new MarginInfo(false, true, false, false));

		Button buttonNavigation = new Button(FontAwesome.NAVICON);
		buttonNavigation.addClickListener(e -> {
			if (lastSplitPosition > -1) {
				if (lastSplitPosition < 100) {
					lastSplitPosition = 200;
				}
				splitPanel.setSplitPosition(lastSplitPosition);
				lastSplitPosition = -1;
			} else {
				lastSplitPosition = splitPanel.getSplitPosition();
				splitPanel.setSplitPosition(0);
			}
		});
		topbar.addComponent(buttonNavigation);
		topbar.setComponentAlignment(buttonNavigation, Alignment.MIDDLE_LEFT);

		if (Backend.getInstance().isAuthenticationActive()) {
			Button buttonLogin = new Button(FontAwesome.SIGN_IN);
			buttonLogin.addClickListener(e -> Backend.getInstance().getAuthentication().login(new VaadinLoginListener(null)));
			topbar.addComponent(buttonLogin);
			topbar.setComponentAlignment(buttonLogin, Alignment.MIDDLE_LEFT);
		}
		
		Panel panel = new Panel();
		topbar.addComponent(panel);
		topbar.setExpandRatio(panel, 1.0f);
		
		TextField textFieldSearch = createSearchField();
		textFieldSearch.setEnabled(Application.getInstance().hasSearchPages());
		topbar.addComponent(textFieldSearch);
		topbar.setComponentAlignment(textFieldSearch, Alignment.MIDDLE_RIGHT);
		
		splitPanel = new HorizontalSplitPanel();
		outerPanel.addComponent(splitPanel);
		outerPanel.setExpandRatio(splitPanel, 1.0f);

		navigationTree = new Tree<>(null, navigationTreeData);
		navigationTree.setSelectionMode(SelectionMode.NONE);
		navigationTree.addItemClickListener(event -> event.getItem().action());
		navigationTree.setItemCaptionGenerator(action -> action.getName());
		
		updateNavigation();
		splitPanel.setFirstComponent(navigationTree);
		splitPanel.setSplitPosition(200, Unit.PIXELS);
		
		verticalScrollPane = new VerticalLayout();
		verticalScrollPane.setMargin(false);
		verticalScrollPane.setSpacing(false);
		splitPanel.setSecondComponent(verticalScrollPane);
		
		String route = (String) request.getAttribute("path");
		if (getSubject() == null && Frontend.loginAtStart()) {
			Backend.getInstance().getAuthentication().login(new VaadinLoginListener(route));
		} else {
			Page page = getPage(route);
			show(page);
		}
	}

	private TextField createSearchField() {
		TextField textFieldSearch = new TextField();
        textFieldSearch.setWidth("30ex");
        textFieldSearch.addShortcutListener(new ShortcutListener("Search", ShortcutAction.KeyCode.ENTER, null) {
			private static final long serialVersionUID = 1L;

			@Override
			public void handleAction(Object sender, Object target) {
				if (target == textFieldSearch) {
					String query = textFieldSearch.getValue();
					Page page = Application.getInstance().createSearchPage(query);
					show(page);
				}
			}
		});
        
        return textFieldSearch;
    }
	
	private void updateNavigation() {
		List<Action> actions = Application.getInstance().getNavigation();
		addNavigationActions(actions, null);
	}

	private void addNavigationActions(List<Action> actions, Action parent) {
		for (Action action : actions) {
			addNavigationAction(action, parent);
			if (action instanceof ActionGroup) {
				ActionGroup actionGroup = (ActionGroup) action;
				addNavigationActions(actionGroup.getItems(), actionGroup);
				navigationTree.expand(actionGroup);
			}
		}

	}
	
	private void addNavigationAction(Action action, Action parent) {
		navigationTreeData.addItem(parent, action);
	}
	
	private class VaadinLoginListener implements LoginListener {
		private final String route;

		public VaadinLoginListener(String route) {
			this.route = route;
		}
		
		@Override
		public void loginSucceded(Subject subject) {
			getSession().setAttribute("subject", subject);
//			Vaadin.this.subject = subject;
			Subject.setCurrent(subject);
			VaadinService.getCurrentRequest().getWrappedSession().setAttribute("subject", subject);
			
			updateNavigation();
			Page page = getPage(route);
			show(page);
		}
	
		@Override
		public void loginCancelled() {
			if (getSubject() == null && Application.getInstance().isLoginRequired()) {
				show(new AuthenticationFailedPage());
			}
		};
	}
	
	private Page getPage(String route) {
		Page page = null;
		if (!StringUtils.isEmpty(route)) {
			String[] pathFragments = route.substring(1).split("/");
			page = Application.getInstance().createPage(pathFragments);
		}
		if (page == null) {
			page = Application.getInstance().createDefaultPage();
		}
		return page;
	}
	
	public Subject getSubject() {
		return (Subject) getSession().getAttribute("subject");
	}
	
	@Override
	public void showMessage(String text) {
		com.vaadin.ui.Notification.show(text,
                com.vaadin.ui.Notification.Type.HUMANIZED_MESSAGE);
	}
	
	@Override
	public void showError(String text) {
		com.vaadin.ui.Notification.show(text,
                com.vaadin.ui.Notification.Type.ERROR_MESSAGE);
	}
	
	protected void updateWindowTitle() {
		Page visiblePage = getPageAtLevel(0);
		String title = Application.getInstance().getName();
		if (visiblePage != null) {
			String pageTitle = visiblePage.getTitle();
			if (!StringUtils.isBlank(pageTitle)) {
				title = title + " - " + pageTitle;
			}
		}
		UI.getCurrent().getPage().setTitle(title);
	}

	@Override
	public void show(Page page) {
		String pageId = pageStore.put(page);
		state = new HashMap<>();
		state.put("0", pageId);
		String route = page.getRoute();
		if (!StringUtils.isEmpty(route)) {
			String currentPath = com.vaadin.server.Page.getCurrent().getLocation().getPath();
			if (currentPath.startsWith("/")) {
				currentPath = currentPath.substring(1);
			}
			if (currentPath.endsWith("/")) {
				currentPath = currentPath.substring(0, currentPath.length() - 1);
			}
			if (!currentPath.equals(route)) {
				com.vaadin.server.Page.getCurrent().pushState(route);
			}
		}
		updateContent();
	}
	
	@Override
	public void showDetail(Page mainPage, Page detail) {
		int pos = indexOfDetail(mainPage);
		for (int j = state.size()-1; j>pos; j--) {
			state.remove(String.valueOf(j));
		}

		String detailId = pageStore.put(detail);
		state.put(String.valueOf(state.size()), detailId);

		updateContent();
	}

	@Override
	public void hideDetail(Page detail) {
		int pos = indexOfDetail(detail);
		for (int j = state.size()-1; j>=pos; j--) {
			state.remove(String.valueOf(j));
		}
		
		updateContent();
	}
	
	@Override
	public boolean isDetailShown(Page detail) {
		return indexOfDetail(detail) >= 0;
	}

	private int indexOfDetail(Page detail) {
		for (int pos = 0; pos < state.size(); pos++) {
			String id = state.get(String.valueOf(pos)); 
			Page d = pageStore.get(id);
			if (d == detail) {
				return pos;
			}
		}
		return -1;
	}
	
	private void updateContent() {
		verticalScrollPane.removeAllComponents();

		for (int pos = 0; state.containsKey(String.valueOf(pos)); pos++) {
			Component content;
			String pageId = state.get(String.valueOf(pos));
			Page page = pageStore.get(pageId);
			content = (Component) page.getContent();
			createMenu((AbstractComponent) content, page.getActions());
				
			if (content != null) {
				ClickListener closeListener = new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						hideDetail(page);
					}
				};
				Component decoratedContent = new VaadinDecoration(page.getTitle(), content, SwingDecoration.SHOW_MINIMIZE, closeListener);
				verticalScrollPane.addComponent(decoratedContent);
			}
		}
	}
	
	private static class VaadinDecoration extends VerticalLayout {
		private static final long serialVersionUID = 1L;

		public VaadinDecoration(String title, Component content, boolean showMinimize, ClickListener closeListener) {
			setMargin(false);
			setSpacing(false);
			
			HorizontalLayout titleBar = new HorizontalLayout();
			titleBar.setMargin(false);
			titleBar.setWidth("100%");
			Label label = new Label(title);
			titleBar.addComponent(label);
			titleBar.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
			titleBar.setExpandRatio(label, 1.0f);
			
			Button closeButton = new Button(FontAwesome.CLOSE);
			closeButton.addClickListener(closeListener);
			titleBar.addComponent(closeButton);
			titleBar.setComponentAlignment(closeButton, Alignment.MIDDLE_RIGHT);
			
			addComponent(titleBar);
			addComponent(content);
		}
		
	}

	private ContextMenu createMenu(AbstractComponent parentComponent, List<Action> actions) {
		if (actions != null && actions.size() > 0) {
			ContextMenu menu = new ContextMenu(parentComponent, true);
			addActions(menu, actions);
			return menu;
		}
		return null;
	}

	private static void addActions(ContextMenu menu, List<Action> actions) {
		for (Action action : actions) {
			if (action instanceof ActionGroup) {
				ActionGroup actionGroup = (org.minimalj.frontend.action.ActionGroup) action;
				MenuItem subMenu = menu.addItem(actionGroup.getName(), e -> {});
				addActions(subMenu, actionGroup.getItems());
			} else if (action instanceof Separator) {
				menu.addSeparator();
			} else {
				adaptAction(menu, action);
			}
		}
	}

	// no common interface between MenuItem and ContextMenu
	private static void addActions(MenuItem menu, List<Action> actions) {
		for (Action action : actions) {
			if (action instanceof ActionGroup) {
				ActionGroup actionGroup = (org.minimalj.frontend.action.ActionGroup) action;
				MenuItem subMenu = menu.addItem(actionGroup.getName(), e -> {});
				addActions(subMenu, actionGroup.getItems());
			} else if (action instanceof Separator) {
				menu.addSeparator();
			} else {
				adaptAction(menu, action);
			}
		}
	}
	
	private static MenuItem adaptAction(MenuItem menu, Action action) {
		MenuItem item = menu.addItem(action.getName(), e -> {
			action.action();
		});
		action.setChangeListener(new ActionChangeListener() {
			{
				update();
			}
			
			@Override
			public void change() {
				update();
			}

			protected void update() {
				item.setEnabled(action.isEnabled());
				item.setDescription(action.getDescription());
			}
		});
		return item;
	}

	private static MenuItem adaptAction(ContextMenu menu, Action action) {
		MenuItem item = menu.addItem(action.getName(), e -> {
			action.action();
		});
		action.setChangeListener(new ActionChangeListener() {
			{
				update();
			}
			
			@Override
			public void change() {
				update();
			}

			protected void update() {
				item.setEnabled(action.isEnabled());
				item.setDescription(action.getDescription());
			}
		});
		return item;
	}
	
	private Page getPageAtLevel(int level) {
		String pageId = state.get(String.valueOf(level));
		return pageId != null ? pageStore.get(pageId) : null;
	}
	
	@Override
	public IDialog showDialog(String title, IContent content, Action saveAction, Action closeAction, Action... actions) {
		Component component = new VaadinEditorLayout(content, actions);
		return new VaadinDialog((ComponentContainer) component, title, saveAction, closeAction);
	}

	@Override
	public <T> IDialog showSearchDialog(Search<T> search, Object[] keys, TableActionListener<T> listener) {
		Component component = new VaadinSearchPanel<>(search, keys, listener);
		VaadinDialog dialog = new VaadinDialog((ComponentContainer) component, "Search", null, null);
		dialog.setHeight("25em");
		return dialog;
	}
}
