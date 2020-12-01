package org.minimalj.frontend.impl.vaadin;

import java.util.List;
import java.util.Optional;

import org.minimalj.application.Application;
import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.ActionGroup;
import org.minimalj.frontend.action.Separator;
import org.minimalj.frontend.impl.util.PageAccess;
import org.minimalj.frontend.impl.vaadin.toolkit.VaadinDialog;
import org.minimalj.frontend.impl.vaadin.toolkit.VaadinEditorLayout;
import org.minimalj.frontend.impl.vaadin.toolkit.VaadinHorizontalLayout;
import org.minimalj.frontend.page.IDialog;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.PageManager;
import org.minimalj.security.Authentication;
import org.minimalj.security.Authorization;
import org.minimalj.security.Subject;
import org.minimalj.util.resources.Resources;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

@Route("/")
@Theme(value = Lumo.class, variant = Lumo.LIGHT)
public class VaadinPageManager extends AppLayout implements PageManager {
	private static final long serialVersionUID = 1L;

	private final VerticalLayout menuLayout = new VerticalLayout();
	private final Authentication authentication = Backend.getInstance().getAuthentication();
	
	public VaadinPageManager() {
		UI.getCurrent().getSession().setAttribute("pageManager", this);

		getElement().getStyle().set("overflow", "hidden");
		
		setPrimarySection(Section.DRAWER);
		addToNavbar(new DrawerToggle());
		
		if (Application.getInstance().hasSearchPages()) {
			TextField textFieldSearch = new TextField();
			textFieldSearch.getStyle().set("margin-left", "auto");
			textFieldSearch.getStyle().set("margin-right", "1em");
			textFieldSearch.addKeyPressListener(Key.ENTER, event -> {
				String query = textFieldSearch.getValue();
				Page page = Application.getInstance().createSearchPage(query);
				show(page);
			});
			addToNavbar(textFieldSearch); 
		}
		
		addToDrawer(menuLayout);
		updateNavigation();

		show(Application.getInstance().createDefaultPage());
	}
	
	private void updateNavigation() {
		menuLayout.setSpacing(false);
		menuLayout.removeAll();
		
		List<Action> actions = Application.getInstance().getNavigation();
		addActions(menuLayout, actions);
	}

	private void addActions(HasComponents container, List<Action> actions) {
		for (Action action : actions) {
			if (action instanceof ActionGroup) {
				container.add(new Label(action.getName()));
				VerticalLayout layout = new VerticalLayout();
				layout.setMargin(true);
				layout.setSpacing(false);
				addActions(layout, ((ActionGroup)action).getItems());
				container.add(layout);
			} else if (action instanceof Separator) {
				continue;
			} else {
				Anchor anchor = new Anchor();
				anchor.setText(action.getName());
				anchor.getElement().addEventListener("click", e -> action.action());
				container.add(anchor);
			}
		}
	}

	@Override
	public void show(Page page) {
		if (!Authorization.hasAccess(Subject.getCurrent(), page)) {
			if (authentication == null) {
				throw new IllegalStateException("Page " + page.getClass().getSimpleName() + " is annotated with @Role but authentication is not configured.");
			}
			authentication.getLoginAction(subject -> show(page)).action();
			return;
		}
		
		Component content = (Component) page.getContent();
		setContent(content);
		
		List<Action> actions = PageAccess.getActions(page);
		if (actions != null && !actions.isEmpty()) {
			ContextMenu menu = new ContextMenu(content);
			for (Action action : actions) {
				if (action instanceof ActionGroup || action instanceof Separator) {
					continue;
				}
				Anchor anchor = new Anchor();
				anchor.setText(action.getName());
				anchor.getElement().addEventListener("click", e -> {action.action(); menu.close();} );
				menu.add(anchor);
			}
		}
	}

	@Override
	public IDialog showDialog(String title, IContent content, Action saveAction, Action closeAction, Action... actions) {
		return new VaadinDialog(title, (Component) content, saveAction, closeAction, actions);
	}

	@Override
	public void showMessage(String text) {
		Notification.show(text);
	}
	
	@Override
	public void showError(String text) {
		Notification.show(text).addThemeVariants(NotificationVariant.LUMO_ERROR);
	}

	@Override
	public Optional<IDialog> showLogin(IContent content, Action loginAction, Action forgetPasswordAction, Action cancelAction) {
		// in this frontend cancel is not possible
		Page page = new Page() {
			@Override
			public IContent getContent() {
				VaadinEditorLayout editorLayout = new VaadinEditorLayout(getTitle(), (Component) content, loginAction, null, loginAction);
				editorLayout.setSizeUndefined();
				VaadinHorizontalLayout centerLayout = new VaadinHorizontalLayout(new IComponent[] {editorLayout});
				centerLayout.setSizeFull();
				centerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
				centerLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
				return centerLayout;
			}
			
			@Override
			public String getTitle() {
				return Resources.getString("Login.title");
			}
		};
		show(page);
		return Optional.empty();
	}

	@Override
	public void login(Subject subject) {
		UI.getCurrent().getSession().setAttribute("subject", subject);
		Subject.setCurrent(subject);
		updateNavigation();
	}

}
