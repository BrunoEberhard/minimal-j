package org.minimalj.frontend.vaadin;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.util.Locale;

import org.minimalj.application.Application;
import org.minimalj.application.Subject;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.FormContent;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.ActionGroup;
import org.minimalj.frontend.page.IDialog;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.PageBrowser;
import org.minimalj.frontend.page.ProgressListener;
import org.minimalj.frontend.vaadin.toolkit.VaadinConfirmDialog;
import org.minimalj.frontend.vaadin.toolkit.VaadinDialog;
import org.minimalj.frontend.vaadin.toolkit.VaadinEditorLayout;
import org.minimalj.frontend.vaadin.toolkit.VaadinExportDialog;
import org.minimalj.frontend.vaadin.toolkit.VaadinFrontend;
import org.minimalj.frontend.vaadin.toolkit.VaadinImportDialog;
import org.minimalj.frontend.vaadin.toolkit.VaadinProgressDialog;
import org.minimalj.frontend.vaadin.toolkit.VaadinSearchPanel;
import org.minimalj.util.StringUtils;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class VaadinWindow extends Window implements PageBrowser {
	private static final long serialVersionUID = 1L;

	private final VerticalLayout windowContent = new VerticalLayout();
	private final VaadinMenuBar menubar = new VaadinMenuBar(this);
	private final TextField textFieldSearch = new TextField();
	private final Subject subject;
	
	private Page visiblePage;
	private Component content;
	private Panel scrollablePanel;
	private int indexInPages;
	
	public VaadinWindow(Subject subject) {
		this.subject = subject;
		
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
		
		Component searchComponent = createSearchField();
		nav.addComponent(searchComponent);
		nav.setComponentAlignment(searchComponent, Alignment.MIDDLE_RIGHT);
		
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(false);
		scrollablePanel = new Panel(layout);
		scrollablePanel.setScrollable(true);
		scrollablePanel.setSizeFull();
	}

	private Component createSearchField() {
		final HorizontalLayout horizontalLayout = new HorizontalLayout();

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
		String query = (String) textFieldSearch.getValue();
		Page searchPage = Application.getApplication().createSearchPage(query);
		show(searchPage);
	}
	
	@Override
	public void show(Page page) {
		updateContent(page);
	}
	
	@Override
	public void refresh() {
		updateContent(getVisiblePage());
	}
	
	Page getVisiblePage() {
		return visiblePage;
	}
	
	@Override
	public void showMessage(String text) {
		// TODO Vaadin zeigt Notifikationen statt Informationsdialog
		showNotification("Information", text, Notification.TYPE_HUMANIZED_MESSAGE);
	}
	
	@Override
	public void showError(String text) {
		// TODO Vaadin zeigt Notifikationen statt Informationsdialog
		showNotification("Fehler", text, Notification.TYPE_ERROR_MESSAGE);
	}
	
	@Override
	public void showConfirmDialog(String message, String title, ConfirmDialogType type, DialogListener listener) {
		Window window = this;
		while (window.getParent() != null) {
			window = window.getParent();
		}
		new VaadinConfirmDialog(window, message, title, type, listener);
	}
	
	@Override
	public IDialog showDialog(String title, IContent content, Action saveAction, Action closeAction, Action... actions) {
		// TODO use saveAction (Enter in TextFields should save the dialog)

		Component component = new VaadinEditorLayout(content, actions);
		component.setSizeFull();

		return createDialog(title, component, closeAction);
	}	
	
	private IDialog createDialog(String title, Component component, Action closeAction) {
		Window window = this;
		// need to find application-level window
		while (window.getParent() != null) {
			window = window.getParent();
		}
		return new VaadinDialog(window, title, (ComponentContainer) component, closeAction);
	}

	public static ProgressListener showProgress(Object parent, String text) {
		Component parentComponent = (Component) parent;
		Window window = parentComponent.getWindow();
		VaadinProgressDialog progressDialog = new VaadinProgressDialog(window, text);
		return progressDialog;
	}
	
	@Override
	public <T> IDialog showSearchDialog(Search<T> index, Object[] keys, TableActionListener<T> listener) {
		VaadinSearchPanel<T> panel = new VaadinSearchPanel<>(index, keys, listener);
		return createDialog(null, panel, null);
	}

	@Override
	public OutputStream store(String buttonText) {
		return new VaadinExportDialog(this, "Export").getOutputStream();
	}

	@Override
	public InputStream load(String buttonText) {
		VaadinImportDialog importDialog = new VaadinImportDialog(this, "Import");
		PipedInputStream inputStream = importDialog.getInputStream();
		return inputStream;
	}
	
	//
	
	private void updateContent(Page page) {
		Frontend.setBrowser(VaadinWindow.this);
		visiblePage = page;
		Component component = (Component) visiblePage.getContent();
		updateContent(component);
		Frontend.setBrowser(null);
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
		VaadinFrontend.focusFirstComponent(content);
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

		@Override
		public void action() {
			Frontend.setBrowser(VaadinWindow.this);
			action.action();
			Frontend.setBrowser(null);
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
