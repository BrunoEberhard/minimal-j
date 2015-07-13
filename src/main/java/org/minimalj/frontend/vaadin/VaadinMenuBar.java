package org.minimalj.frontend.vaadin;

import java.util.List;

import org.minimalj.application.Application;
import org.minimalj.frontend.page.ActionGroup;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.Separator;
import org.minimalj.frontend.toolkit.Action;
import org.minimalj.frontend.toolkit.Action.ActionChangeListener;
import org.minimalj.frontend.vaadin.toolkit.VaadinClientToolkit;
import org.minimalj.util.resources.Resources;

import com.vaadin.ui.MenuBar;

public class VaadinMenuBar extends MenuBar {
	private static final long serialVersionUID = 1L;

	private final VaadinWindow vaadinWindow;
	
	public VaadinMenuBar(VaadinWindow vaadinWindow) {
		this.vaadinWindow = vaadinWindow;
	}
	
	public void updateMenu() {
		removeItems();
		
		createMenu();
		createObjectMenu();
	}
	
	private void createMenu() {
		List<Action> menuActions = Application.getApplication().getMenu();

		MenuBar.MenuItem menu = menu("application");
		addActions(menu, menuActions);
	}
	
	private void createObjectMenu() {
		Page visiblePage = vaadinWindow.getVisiblePage();
		List<Action> actions = visiblePage.getActions();
		if (actions != null && actions.size() > 0) {
			MenuBar.MenuItem menu = menu("page");
			addActions(menu, actions);
		}
	}

	private MenuBar.MenuItem menu(String resourceName) {
		MenuBar.MenuItem menu = addItem(Resources.getString("Menu." + resourceName), null);
		return menu;
	}

	private void addActions(MenuBar.MenuItem menu, List<Action> actions) {
		for (Action action : actions) {
			if (action instanceof org.minimalj.frontend.page.ActionGroup) {
				ActionGroup actionGroup = (ActionGroup) action;
				MenuBar.MenuItem subMenu = menu.addItem(action.getName(), null);
				addActions(subMenu, actionGroup.getItems());
			} else if (action instanceof Separator) {
				menu.addSeparator();
			} else {
				MenuItem menuItem = menu.addItem(action.getName(), new ActionCommand(action));
				updateMenuItem(menuItem, action);
				installAdditionalActionListener(action, menuItem);
			}
		}
	}
	
	//
	
	private class ActionCommand implements Command {
		private static final long serialVersionUID = 1L;
		private final Action action;
		
		public ActionCommand(Action action) {
			this.action = action;
		}

		@Override
		public void menuSelected(MenuItem selectedItem) {
			VaadinClientToolkit.setWindow(vaadinWindow);
			action.action();
			VaadinClientToolkit.setWindow(null);
		}
	}
	
	private static void installAdditionalActionListener(final Action action, final MenuBar.MenuItem menuItem) {
		action.setChangeListener(new ActionChangeListener() {
			@Override
			public void change() {
				updateMenuItem(menuItem, action);
			}
		});
	}

	private static void updateMenuItem(final MenuBar.MenuItem menuItem, final Action action) {
		menuItem.setEnabled(action.isEnabled());
		menuItem.setText(action.getName());
		menuItem.setDescription(action.getDescription());
	}

}
