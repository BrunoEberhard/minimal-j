package org.minimalj.frontend.vaadin;

import java.util.List;

import org.minimalj.application.Application;
import org.minimalj.frontend.page.ActionGroup;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.Separator;
import org.minimalj.frontend.toolkit.IAction;
import org.minimalj.frontend.toolkit.IAction.ActionChangeListener;
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
		List<IAction> actionsNew = Application.getApplication().getActionsNew();
		if (!actionsNew.isEmpty()) {
			addActions(menu, "new", actionsNew);
			separator = true;
		}
		List<IAction> actionsImport = Application.getApplication().getActionsImport();
		List<IAction> actionsExport = Application.getApplication().getActionsExport();
		if (!actionsImport.isEmpty() || !actionsExport.isEmpty()) {
			if (separator) menu.addSeparator();
		}
		if (!actionsImport.isEmpty()) addActions(menu, "import", actionsImport);
		if (!actionsExport.isEmpty()) addActions(menu, "export", actionsExport);
	}
	
	private void createViewMenu() {
		List<IAction> actionsView = Application.getApplication().getActionsView();
		if (!actionsView.isEmpty()) {
			MenuBar.MenuItem menu = menu("view");
			addActions(menu, actionsView);
		}
	}
	
	private void createObjectMenu() {
		Page visiblePage = vaadinWindow.getVisiblePage();
		if (visiblePage instanceof ObjectPage) {
			ActionGroup actionGroup = ((ObjectPage<?>) visiblePage).getMenu();
			if (actionGroup != null && actionGroup.getItems() != null) {
				MenuBar.MenuItem menu = addItem(actionGroup.getName(), null);
				addActions(menu, actionGroup.getItems());
			}
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

	private void addActions(MenuBar.MenuItem menu, String type, List<IAction> actions) {
		MenuBar.MenuItem subMenu = menu(menu, type);
		addActions(subMenu, actions);
	}
	
	private void addActions(MenuBar.MenuItem menu, List<IAction> actions) {
		for (IAction action : actions) {
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
		private final IAction action;
		
		public ActionCommand(IAction action) {
			this.action = action;
		}

		@Override
		public void menuSelected(MenuItem selectedItem) {
			VaadinClientToolkit.setWindow(vaadinWindow);
			action.action();
			VaadinClientToolkit.setWindow(null);
		}
	}
	
	private static void installAdditionalActionListener(final IAction action, final MenuBar.MenuItem menuItem) {
		action.setChangeListener(new ActionChangeListener() {
			@Override
			public void change() {
				updateMenuItem(menuItem, action);
			}
		});
	}

	private static void updateMenuItem(final MenuBar.MenuItem menuItem, final IAction action) {
		menuItem.setEnabled(action.isEnabled());
		menuItem.setText(action.getName());
		menuItem.setDescription(action.getDescription());
	}

}
