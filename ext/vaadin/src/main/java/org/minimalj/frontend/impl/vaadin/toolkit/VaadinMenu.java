package org.minimalj.frontend.impl.vaadin.toolkit;

import java.util.List;

import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.Action.ActionChangeListener;
import org.minimalj.frontend.action.ActionGroup;
import org.minimalj.frontend.action.Separator;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.H2;

public class VaadinMenu {

	
//	ContextMenu contextMenu = new ContextMenu();
//
//	Component target = createTargetComponent();
//	contextMenu.setTarget(target);
//
//	Label message = new Label("-");
//
//	contextMenu.addItem("First menu item",
//	        event -> message.setText("Clicked on the first item"));
//
//	MenuItem parent = contextMenu.addItem("Parent item");
//	SubMenu subMenu = parent.getSubMenu();
//
//	subMenu.addItem("Second menu item",
//	        event -> message.setText("Clicked on the second item"));
//
//	subMenu = subMenu.addItem("Parent item").getSubMenu();
//	subMenu.addItem("Third menu item",
//	        event -> message.setText("Clicked on the third item"));
	
	
	public static void createMenu(Component target, List<Action> actions) {
		if (actions != null && actions.size() > 0) {
			ContextMenu menu = new ContextMenu();
			addActions(menu, actions);
			menu.setTarget(target);
		}
	}

	private static void addActions(ContextMenu menu, List<Action> actions) {
		for (Action action : actions) {
			if (action instanceof ActionGroup) {
				ActionGroup actionGroup = (org.minimalj.frontend.action.ActionGroup) action;
				MenuItem parent = menu.addItem(actionGroup.getName());
				SubMenu subMenu = parent.getSubMenu();
				addActions(subMenu, actionGroup.getItems());
			} else if (action instanceof Separator) {
				menu.add(new H2("TODO"));
			} else {
				adaptAction(menu, action);
			}
		}
	}

	// no common interface between MenuItem and ContextMenu
	private static void addActions(SubMenu menu, List<Action> actions) {
		for (Action action : actions) {
			if (action instanceof ActionGroup) {
				ActionGroup actionGroup = (org.minimalj.frontend.action.ActionGroup) action;
				MenuItem parent = menu.addItem(actionGroup.getName());
				SubMenu subMenu = parent.getSubMenu();
				addActions(subMenu, actionGroup.getItems());
			} else if (action instanceof Separator) {
				menu.add(new H2("TODO"));
			} else {
				adaptAction(menu, action);
			}
		}
	}
	
	private static MenuItem adaptAction(SubMenu menu, Action action) {
		MenuItem item = menu.addItem(action.getName(), e -> {
			action.action();
		});
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

	private static MenuItem adaptAction(ContextMenu menu, Action action) {
		MenuItem item = menu.addItem(action.getName(), e -> action.action());
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
