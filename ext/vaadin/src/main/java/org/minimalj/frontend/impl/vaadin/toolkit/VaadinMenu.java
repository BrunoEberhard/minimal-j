package org.minimalj.frontend.impl.vaadin.toolkit;

import java.util.List;

import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.Action.ActionChangeListener;
import org.minimalj.frontend.action.ActionGroup;
import org.minimalj.frontend.action.Separator;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.HasMenuItems;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Hr;

public class VaadinMenu {

	public static ContextMenu createMenu(Component target, List<Action> actions) {
		if (actions != null && actions.size() > 0) {
			ContextMenu menu = new ContextMenu();
			addActions(menu, actions);
			menu.setTarget(target);
			return menu;
		}
		return null;
	}

	public static void addActions(ContextMenu menu, List<Action> actions) {
		for (Action action : actions) {
			if (action instanceof ActionGroup) {
				ActionGroup actionGroup = (org.minimalj.frontend.action.ActionGroup) action;
				MenuItem groupItem = menu.addItem(actionGroup.getName());
				SubMenu subMenu = groupItem.getSubMenu();
				addActions(subMenu, actionGroup.getItems());
			} else if (action instanceof Separator) {
				menu.add(new Hr());
			} else {
				adaptAction(menu, action);
			}
		}
	}

	// no common interface between MenuItem and ContextMenu
	public static void addActions(SubMenu menu, List<Action> actions) {
		for (Action action : actions) {
			if (action instanceof ActionGroup) {
				ActionGroup actionGroup = (org.minimalj.frontend.action.ActionGroup) action;
				MenuItem groupItem = menu.addItem(actionGroup.getName());
				SubMenu subMenu = groupItem.getSubMenu();
				addActions(subMenu, actionGroup.getItems());
			} else if (action instanceof Separator) {
				menu.add(new Hr());
			} else {
				adaptAction(menu, action);
			}
		}
	}
	
	public static MenuItem adaptAction(HasMenuItems menu, Action action) {
		MenuItem item = menu.addItem(action.getName(), e -> action.run());
		item.setEnabled(action.isEnabled());
		action.setChangeListener(new ActionChangeListener() {
			{
				update();
			}
			
			@Override
			public void change() {
				update();
			}

			protected void update() {
				item.setEnabled(action.isEnabled());
				item.setText(action.getName());
			}
		});
		return item;
	}
	
}
