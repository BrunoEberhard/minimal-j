package org.minimalj.frontend.vaadin;

import java.util.List;
import java.util.Locale;

import org.minimalj.application.ApplicationContext;
import org.minimalj.application.Application;
import org.minimalj.frontend.page.ActionGroup;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.PageLink;
import org.minimalj.frontend.page.type.SearchOf;
import org.minimalj.frontend.toolkit.ClientToolkit.IContent;
import org.minimalj.frontend.toolkit.FormContent;
import org.minimalj.frontend.toolkit.IAction;
import org.minimalj.frontend.toolkit.ResourceAction;
import org.minimalj.frontend.vaadin.toolkit.VaadinClientToolkit;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

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
import com.vaadin.ui.UriFragmentUtility;
import com.vaadin.ui.UriFragmentUtility.FragmentChangedEvent;
import com.vaadin.ui.UriFragmentUtility.FragmentChangedListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class VaadinWindow extends Window {
	private static final long serialVersionUID = 1L;

	private final VerticalLayout windowContent = new VerticalLayout();
	private final VaadinMenuBar menubar = new VaadinMenuBar(this);
	private final ComboBox comboBox = new ComboBox();
	private final TextField textFieldSearch = new TextField();
	private final UriFragmentUtility ufu;
	private final ApplicationContext applicatonContext;
	
	private Page visiblePage;
	private Component content;
	private Panel scrollablePanel;
	private List<String> pageLinks;
	private int indexInPageLinks;
	
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
		
		if (Application.getApplication().getSearchClasses().length > 0) {
			Component searchComponent = createSearchField();
			nav.addComponent(searchComponent);
			nav.setComponentAlignment(searchComponent, Alignment.MIDDLE_RIGHT);
		}
		
		ufu = new UriFragmentUtility();
		ufu.addListener(new VaadinWindowFragmentChangedListener());
		windowContent.addComponent(ufu);
		
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(false);
		scrollablePanel = new Panel(layout);
		scrollablePanel.setScrollable(true);
		scrollablePanel.setSizeFull();
	}

	private Component createSearchField() {
		final HorizontalLayout horizontalLayout = new HorizontalLayout();

		comboBox.setNullSelectionAllowed(false);
		for (Class<?> searchClass: Application.getApplication().getSearchClasses()) {
			comboBox.addItem(searchClass);
			Class<?> searchedClass = GenericUtils.getTypeArgument(searchClass, SearchOf.class);
			comboBox.setItemCaption(searchClass, Resources.getString(searchedClass));
		}
		comboBox.setValue(Application.getApplication().getSearchClasses()[0]);
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

	@SuppressWarnings("unchecked")
	private void showSearchPage() {
		show(PageLink.link((Class<? extends Page>) comboBox.getValue(), (String) textFieldSearch.getValue()));
	}
	
	public void show(String pageLink) {
		boolean sameAsExisting = StringUtils.equals(ufu.getFragment(), pageLink);
		if (!sameAsExisting) {
			ufu.setFragment(pageLink, true);
		} else {
			updateContent(pageLink);
		}
	}
	
	public void refresh() {
		getVisiblePage().refresh();
	}
	
	Page getVisiblePage() {
		return visiblePage;
	}
	
	private void updateContent(String pageLink) {
		VaadinClientToolkit.setWindow(VaadinWindow.this);
		visiblePage = PageLink.createPage(pageLink);
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
	
	private class VaadinGridLayout extends GridLayout implements IContent {
		private static final long serialVersionUID = 1L;

		public VaadinGridLayout(int columns, int rows) {
			super(columns, rows);
		}
	}

	protected class UpAction extends ResourceAction {
		@Override
		public void action() {
			up();
		}
	}

	protected class DownAction extends ResourceAction {
		@Override
		public void action() {
			down();
		}
	}
	
	protected void fillHelpMenu(ActionGroup actionGroup) {
		// 
	}
	
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
		show(pageLinks.get(--indexInPageLinks));
	}

	public void down() {
		show(pageLinks.get(++indexInPageLinks));
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
	
	private class VaadinWindowFragmentChangedListener implements FragmentChangedListener {

		private static final long serialVersionUID = 1L;

		@Override
		public void fragmentChanged(FragmentChangedEvent source) {
			String pageLink = source.getUriFragmentUtility().getFragment();
			updateContent(pageLink);
		}
	}

	public ApplicationContext getApplicationContext() {
		return applicatonContext;
	}
	
	private IAction[] wrapActions(IAction[] actions) {
		IAction[] wrappedActions = new IAction[actions.length];
		for (int i = 0; i<actions.length; i++) {
			wrappedActions[i] = new VaadinActionWrapper(actions[i]);
		}
		return wrappedActions;
	}
	
	public class VaadinActionWrapper implements IAction {

		private final IAction action;

		public VaadinActionWrapper(IAction action) {
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
