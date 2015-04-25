package org.minimalj.frontend.vaadin;

import java.util.List;
import java.util.Locale;

import org.minimalj.application.Application;
import org.minimalj.application.ApplicationContext;
import org.minimalj.frontend.page.ActionGroup;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.SearchPage;
import org.minimalj.frontend.toolkit.Action;
import org.minimalj.frontend.toolkit.FormContent;
import org.minimalj.frontend.vaadin.toolkit.VaadinClientToolkit;
import org.minimalj.util.StringUtils;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class VaadinWindow extends Window {
	private static final long serialVersionUID = 1L;

	private final VerticalLayout windowContent = new VerticalLayout();
	private final VaadinMenuBar menubar = new VaadinMenuBar(this);
	private final ComboBox comboBox = new ComboBox();
	private final TextField textFieldSearch = new TextField();
	private final ApplicationContext applicatonContext;
	
	private Page visiblePage;
	private Component content;
	private Panel scrollablePanel;
	private List<Page> pages;
	private int indexInPages;
	
	public VaadinWindow(ApplicationContext context) {
		this.applicatonContext = context;
		
		setLocale(Locale.GERMAN);

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
		
		if (Application.getApplication().getSearchPages().length > 0) {
			Component searchComponent = createSearchField();
			nav.addComponent(searchComponent);
			nav.setComponentAlignment(searchComponent, Alignment.MIDDLE_RIGHT);
		}
		
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(false);
		scrollablePanel = new Panel(layout);
		scrollablePanel.setScrollable(true);
		scrollablePanel.setSizeFull();
	}

	private Component createSearchField() {
		final HorizontalLayout horizontalLayout = new HorizontalLayout();

		comboBox.setNullSelectionAllowed(false);
		for (SearchPage searchPage : Application.getApplication().getSearchPages()) {
			comboBox.addItem(searchPage);
			comboBox.setItemCaption(searchPage, searchPage.getName());
		}
		
		comboBox.setValue(comboBox.getItemIds().iterator().next());
		horizontalLayout.addComponent(comboBox);
		
        textFieldSearch.setWidth("160px");
        horizontalLayout.addComponent(textFieldSearch);
        
        textFieldSearch.addShortcutListener(new ShortcutListener("Search", ShortcutAction.KeyCode.ENTER, null) {
			private static final long serialVersionUID = 1L;

			@Override
			public void handleAction(Object sender, Object target) {
				showSearchPage();
			}
		});
        
        Button button = new Button("Suche");
        button.addListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				showSearchPage();
			}
		});
        horizontalLayout.addComponent(button);
        
        return horizontalLayout;
    }

	private void showSearchPage() {
		SearchPage searchPage = (SearchPage) comboBox.getValue();
		searchPage.setQuery((String) textFieldSearch.getValue());
		show(searchPage);
	}
	
	public void show(Page page) {
		updateContent(page);
	}
	
	public void refresh() {
		updateContent(getVisiblePage());
	}
	
	Page getVisiblePage() {
		return visiblePage;
	}
	
	private void updateContent(Page page) {
		VaadinClientToolkit.setWindow(VaadinWindow.this);
		visiblePage = page;
		Component component = (Component) visiblePage.getContent();
		updateContent(component);
		VaadinClientToolkit.setWindow(null);
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
			this.content.setSizeFull();
		} else if (content != null) {
			if (content instanceof FormContent) {
				content = createFormAlignLayout(content);
			}
			scrollablePanel.removeAllComponents();
			scrollablePanel.addComponent(content);
			this.content = scrollablePanel;
			windowContent.addComponent(scrollablePanel);
			windowContent.setExpandRatio(scrollablePanel, 1);
			this.content.setSizeFull();
		} else {
			this.content = null;
		}
		
		menubar.updateMenu();
		updateWindowTitle();
		VaadinClientToolkit.focusFirstComponent(content);
	}
	
	private Component createFormAlignLayout(Component content) {
		VaadinGridLayout gridLayout = new VaadinGridLayout(3, 3);
		gridLayout.setMargin(false);
		gridLayout.setStyleName("gridForm");
		gridLayout.setSizeFull();
		gridLayout.addComponent(content, 1, 1);
		gridLayout.setRowExpandRatio(0, 0.1f);
		gridLayout.setRowExpandRatio(1, 0.7f);
		gridLayout.setRowExpandRatio(2, 0.2f);
		gridLayout.setColumnExpandRatio(0, 0.3f);
		gridLayout.setColumnExpandRatio(1, 0.0f);
		gridLayout.setColumnExpandRatio(2, 0.7f);
		return gridLayout;
	}
	
	private class VaadinGridLayout extends GridLayout {
		private static final long serialVersionUID = 1L;

		public VaadinGridLayout(int columns, int rows) {
			super(columns, rows);
		}
	}
	
	protected void fillHelpMenu(ActionGroup actionGroup) {
		// 
	}
	
	public void show(List<Page> pages, int index) {
		this.pages = pages;
		this.indexInPages = index;
		show(pages.get(indexInPages));
	}
	
	public boolean top() {
		return pages == null ||indexInPages == 0;
	}

	public boolean bottom() {
		return pages == null || indexInPages == pages.size() - 1;
	}

	public void up() {
		show(pages.get(--indexInPages));
	}

	public void down() {
		show(pages.get(++indexInPages));
	}
	
	protected void updateWindowTitle() {
		Page visiblePage = getVisiblePage();
		String title = Application.getApplication().getName();
		if (visiblePage != null) {
			String pageTitle = visiblePage.getTitle();
			if (!StringUtils.isBlank(pageTitle)) {
				title = title + " - " + pageTitle;
			}
		}
		setCaption(title);
	}
	
	public ApplicationContext getApplicationContext() {
		return applicatonContext;
	}
	
	private Action[] wrapActions(Action[] actions) {
		Action[] wrappedActions = new Action[actions.length];
		for (int i = 0; i<actions.length; i++) {
			wrappedActions[i] = new VaadinActionWrapper(actions[i]);
		}
		return wrappedActions;
	}
	
	public class VaadinActionWrapper extends Action {

		private final Action action;

		public VaadinActionWrapper(Action action) {
			this.action = action;
		}

		public void action() {
			ApplicationContext.setApplicationContext(VaadinWindow.this.applicatonContext);
			action.action();
		}

		@Override
		public String getName() {
			return action.getName();
		}

		@Override
		public String getDescription() {
			return action.getDescription();
		}

		@Override
		public boolean isEnabled() {
			return action.isEnabled();
		}

		@Override
		public void setChangeListener(ActionChangeListener changeListener) {
			action.setChangeListener(changeListener);
		}
	}
	
}
