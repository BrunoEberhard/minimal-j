package ch.openech.mj.vaadin;

import java.util.List;

import ch.openech.mj.application.MjApplication;
import ch.openech.mj.page.ActionGroup;
import ch.openech.mj.page.Page;
import ch.openech.mj.page.Separator;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.toolkit.IAction;
import ch.openech.mj.toolkit.IAction.ActionChangeListener;

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
		createObjectMenu();
	}
	
	private MenuBar.MenuItem createFileMenu() {
		MenuBar.MenuItem menu = menu("file");
		
		addActions(menu, "new", MjApplication.getApplication().getActionsNew(vaadinWindow));
		menu.addSeparator();
		addActions(menu, "import", MjApplication.getApplication().getActionsImport(vaadinWindow));
		addActions(menu, "export", MjApplication.getApplication().getActionsExport(vaadinWindow));
		return menu;
	}
	
	private MenuBar.MenuItem createObjectMenu() {
		Page visiblePage = vaadinWindow.getVisiblePage();
		if (visiblePage != null) {
			ActionGroup actionGroup = visiblePage.getMenu();
			if (actionGroup != null && actionGroup.getItems() != null) {
				MenuBar.MenuItem menu = addItem(actionGroup.getName(), null);
				addActions(menu, actionGroup.getItems());
				return menu;
			}
		}
		return null;
	}

	//
	
	private MenuBar.MenuItem menu(String resourceName) {
		MenuBar.MenuItem menu = addItem(Resources.getString("Menu." + resourceName + ".text"), null);
		return menu;
	}

	private MenuBar.MenuItem menu(MenuBar.MenuItem menu, String resourceName) {
		return menu.addItem(Resources.getString("Menu." + resourceName + ".text"), null);
	}

	private void addActions(MenuBar.MenuItem menu, String type, List<IAction> actions) {
		MenuBar.MenuItem subMenu = menu(menu, type);
		addActions(subMenu, actions);
	}
	
	private void addActions(MenuBar.MenuItem menu, List<IAction> actions) {
		for (IAction action : actions) {
			if (action instanceof ch.openech.mj.page.ActionGroup) {
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
			action.action(vaadinWindow);
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
