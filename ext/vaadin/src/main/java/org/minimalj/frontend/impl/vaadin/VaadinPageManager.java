package org.minimalj.frontend.impl.vaadin;

import java.util.List;

import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.ActionGroup;
import org.minimalj.frontend.action.Separator;
import org.minimalj.frontend.impl.util.PageAccess;
import org.minimalj.frontend.impl.vaadin.toolkit.VaadinDialog;
import org.minimalj.frontend.page.IDialog;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.PageManager;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;



@Route("")
@Theme(value = Lumo.class, variant = Lumo.LIGHT)
public class VaadinPageManager extends AppLayout implements PageManager {
	private static final long serialVersionUID = 1L;

	private final VerticalLayout menuLayout = new VerticalLayout();
	
	public VaadinPageManager() {
		getElement().getStyle().set("overflow", "hidden");
		
		setPrimarySection(Section.DRAWER);
		addToNavbar(new DrawerToggle());
		 
		addToDrawer(menuLayout);
		updateNavigation();
		show(Application.getInstance().createDefaultPage());
		
		UI.getCurrent().getSession().setAttribute("pageManager", this);
	}

	private void updateNavigation() {
		menuLayout.removeAll();
		
		List<Action> actions = Application.getInstance().getNavigation();
		for (Action action : actions) {
			if (action instanceof ActionGroup || action instanceof Separator) {
				continue;
			}
			Anchor anchor = new Anchor();
			anchor.setText(action.getName());
			anchor.getElement().addEventListener("click", e -> action.action());
			menuLayout.add(anchor);
		}
	}

	@Override
	public void show(Page page) {
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
		return new VaadinDialog(title, (Component) content,  saveAction, closeAction);
	}

	@Override
	public void showMessage(String text) {
		Notification.show(text);
	}
	
	@Override
	public void showError(String text) {
		Notification.show(text).addThemeVariants(NotificationVariant.LUMO_ERROR);
	}	
	
}
