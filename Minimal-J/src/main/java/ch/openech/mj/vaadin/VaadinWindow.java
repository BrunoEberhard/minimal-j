package ch.openech.mj.vaadin;

import java.util.List;
import java.util.Locale;

import ch.openech.mj.application.ApplicationContext;
import ch.openech.mj.application.MjApplication;
import ch.openech.mj.edit.Editor;
import ch.openech.mj.edit.Editor.EditorListener;
import ch.openech.mj.edit.form.IForm;
import ch.openech.mj.page.ActionGroup;
import ch.openech.mj.page.Page;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.page.PageLink;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.ResourceAction;
import ch.openech.mj.util.StringUtils;
import ch.openech.mj.vaadin.toolkit.VaadinClientToolkit;
import ch.openech.mj.vaadin.toolkit.VaadinDialog;
import ch.openech.mj.vaadin.toolkit.VaadinEditorLayout;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UriFragmentUtility;
import com.vaadin.ui.UriFragmentUtility.FragmentChangedEvent;
import com.vaadin.ui.UriFragmentUtility.FragmentChangedListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class VaadinWindow extends Window implements PageContext {
	private static final long serialVersionUID = 1L;

	private final VerticalLayout windowContent = new VerticalLayout();
	private final VaadinMenuBar menubar = new VaadinMenuBar(this);
	private final ComboBox comboBox = new ComboBox();
	private final TextField textFieldSearch = new TextField();
	private final UriFragmentUtility ufu;
	
	private Page visiblePage;
	private Component content;
	private Panel scrollablePanel;
	private List<String> pageLinks;
	private int indexInPageLinks;
	
	private Editor<?> editor;
	
	public VaadinWindow() {
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
		
		if (MjApplication.getApplication().getSearchClasses().length > 0) {
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
		for (Class<?> searchClass: MjApplication.getApplication().getSearchClasses()) {
			comboBox.addItem(searchClass);
			comboBox.setItemCaption(searchClass, Resources.getString("Search." + searchClass.getSimpleName()));
		}
		comboBox.setValue(MjApplication.getApplication().getSearchClasses()[0]);
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
	
	@Override
	public void show(String pageLink) {
		boolean sameAsExisting = StringUtils.equals(ufu.getFragment(), pageLink);
		if (!sameAsExisting) {
			ufu.setFragment(pageLink, true);
		} else {
			updateContent(pageLink);
		}
	}

	Page getVisiblePage() {
		return visiblePage;
	}
	
	private void updateContent(String pageLink) {
		visiblePage = PageLink.createPage(VaadinWindow.this, pageLink);
		Component component = (Component) visiblePage.getComponent();
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
			this.content.setSizeFull();
		} else if (content != null) {
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

	protected class UpAction extends ResourceAction {
		@Override
		public void action(IComponent context) {
			up();
		}
	}

	protected class DownAction extends ResourceAction {
		@Override
		public void action(IComponent context) {
			down();
		}
	}
	
	protected void fillHelpMenu(ActionGroup actionGroup) {
		// 
	}
	
	@Override
	public void show(Editor<?> editor) {
		this.editor = editor;
		
		IForm<?> form = editor.startEditor();
		VaadinEditorLayout layout = new VaadinEditorLayout(form.getComponent(), editor.getActions());
		final VaadinDialog dialog = new VaadinDialog(this, layout, editor.getTitle());

		dialog.setCloseListener(new ch.openech.mj.toolkit.IDialog.CloseListener() {
			@Override
			public boolean close() {
				VaadinWindow.this.editor.checkedClose();
				return VaadinWindow.this.editor.isFinished();
			}
		});
		
		editor.setEditorListener(new EditorListener() {
			@Override
			public void saved(Object saveResult) {
				dialog.closeDialog();
				if (saveResult instanceof String) {
					show((String) saveResult);
				}
			}

			@Override
			public void canceled() {
				dialog.closeDialog();
			}
		});
		dialog.setVisible(true);
		VaadinClientToolkit.focusFirstComponent((Component) form.getComponent());
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
		show(pageLinks.get(--indexInPageLinks));
	}

	public void down() {
		show(pageLinks.get(++indexInPageLinks));
	}
	
	protected void updateWindowTitle() {
		setCaption(MjApplication.getApplication().getWindowTitle(this));
	}
	
	private class VaadinWindowFragmentChangedListener implements FragmentChangedListener {

		private static final long serialVersionUID = 1L;

		@Override
		public void fragmentChanged(FragmentChangedEvent source) {
			String pageLink = source.getUriFragmentUtility().getFragment();
			updateContent(pageLink);
		}
	}

	@Override
	public ApplicationContext getApplicationContext() {
		return ((VaadinLauncher) getApplication()).getApplicationContext();
	}
	
}
