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
		
		createFileMenu();
		createViewMenu();
		createObjectMenu();
	}
	
	private void createFileMenu() {
		MenuBar.MenuItem menu = menu("file");

		boolean separator = false;
		List<Action> actionsNew = Application.getApplication().getMenu();
//		if (!actionsNew.isEmpty()) {
//			addActions(menu, "new", actionsNew);
//			separator = true;
//		}
//		List<Action> actionsImport = Application.getApplication().getActionImport();
//		List<Action> actionsExport = Application.getApplication().getActionExport();
//		if (!actionsImport.isEmpty() || !actionsExport.isEmpty()) {
//			if (separator) menu.addSeparator();
//		}
//		if (!actionsImport.isEmpty()) addActions(menu, "import", actionsImport);
//		if (!actionsExport.isEmpty()) addActions(menu, "export", actionsExport);
	}
	
	private void createViewMenu() {
//		List<Action> actionsView = Application.getApplication().getActionView();
//		if (!actionsView.isEmpty()) {
			MenuBar.MenuItem menu = menu("view");
//			addActions(menu, actionsView);
//		}
	}
	
	private void createObjectMenu() {
		Page visiblePage = vaadinWindow.getVisiblePage();
		List<Action> actions = visiblePage.getActions();
		if (actions != null && actions.size() > 0) {
			MenuBar.MenuItem menu = addItem("Actions", null);
			addActions(menu, actions);
		}
	}

	//
	
	private MenuBar.MenuItem menu(String resourceName) {
		MenuBar.MenuItem menu = addItem(Resources.getString("Menu." + resourceName), null);
		return menu;
	}

	private MenuBar.MenuItem menu(MenuBar.MenuItem menu, String resourceName) {
		return menu.addItem(Resources.getString("Menu." + resourceName), null);
	}

	private void addActions(MenuBar.MenuItem menu, String type, List<Action> actions) {
		MenuBar.MenuItem subMenu = menu(menu, type);
		addActions(subMenu, actions);
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
