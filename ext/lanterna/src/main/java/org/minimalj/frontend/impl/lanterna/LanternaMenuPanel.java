package org.minimalj.frontend.impl.lanterna;

import java.util.List;

import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.ActionGroup;
import org.minimalj.frontend.impl.lanterna.toolkit.LanternaFrontend;
import org.minimalj.frontend.page.Page;
import org.minimalj.util.resources.Resources;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.BorderLayout.Location;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.menu.Menu;
import com.googlecode.lanterna.gui2.menu.MenuBar;

public class LanternaMenuPanel extends Panel {
	private MenuBar bar;
	private Menu menuApplication;
	private Menu menuPage;
	
	public LanternaMenuPanel() {
		super(new BorderLayout());
		
		Panel panel = new Panel();
		panel.setLayoutManager(new BorderLayout());
		panel.setPreferredSize(new TerminalSize(Integer.MAX_VALUE, 1));
		
		bar = new MenuBar();
		updateMenu(null);

		panel.addComponent(bar, Location.LEFT);
		
		if (Application.getInstance().hasSearchPages()) {
			panel.addComponent(createSearchField(), Location.RIGHT);
		}

		addComponent(panel, Location.TOP);
	}

	public void updateMenu(Page page) {
		boolean focus = false;
		if (bar.getChildCount() > 0) {
			Button button = (Button) bar.getChildren().iterator().next();
			focus = button.isFocused();
		}
		bar.removeAllComponents();

		createMenu("application", Application.getInstance().getNavigation());

		if (page != null && page.getActions() != null) {
			createMenu("page", page.getActions());
		}
		if (focus) {
			Button button = (Button) bar.getChildren().iterator().next();
			getBasePane().setFocusedInteractable(button);
		}
	}

	protected void createMenu(String resourceName, List<Action> actions) {
		String name = Resources.getString("Menu." + resourceName);
		Menu menu = new Menu(name);
		actionGroup(menu, actions);
		bar.addMenu(menu);
	}
	
	protected void actionGroup(Menu menu, List<Action> actions) {
		if (actions.isEmpty())
			return;

		for (Action action : actions) {
			if (action instanceof ActionGroup) {
				ActionGroup subGroup = (ActionGroup) action;
				Menu subMenu = new Menu(subGroup.getName());
				actionGroup(subMenu, subGroup.getItems());
				menu.addMenuItem(subMenu);
			} else {
				ActionMenuItem menuItem = new ActionMenuItem(this, action);
				menu.addMenuItem(menuItem);
			}
		}
	}

	public class ActionMenuItem implements Runnable {

		private final Component component;
		private final org.minimalj.frontend.action.Action action;
		
		public ActionMenuItem(Component component, org.minimalj.frontend.action.Action action) {
			this.component = component;
			this.action = action;
		}
		
		@Override
		public void run() {
			LanternaFrontend.run(component, () -> action.action());
		}
		
		@Override
		public String toString() {
			String text = action.getName();
			return text != null ? text : "";
		}
	}

	private TextBox textFieldSearch;

	protected Panel createSearchField() {
		Panel panel = new Panel(new LinearLayout(Direction.HORIZONTAL));

		textFieldSearch = new TextBox();
		panel.addComponent(textFieldSearch);

		final Button button = new Button("Search");
		button.addListener(b -> LanternaFrontend.run(b, new SearchAction()));
		panel.addComponent(button);

		return panel;
	}

	protected class SearchAction implements Runnable {
		
		@Override
		public void run() {
			String query = textFieldSearch.getText();
			Page searchPage = Application.getInstance().createSearchPage(query);
			Frontend.show(searchPage);
		}
	}

}