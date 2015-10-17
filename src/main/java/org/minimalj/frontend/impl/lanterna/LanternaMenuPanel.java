package org.minimalj.frontend.impl.lanterna;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.ActionGroup;
import org.minimalj.frontend.impl.lanterna.toolkit.LanternaActionAdapater;
import org.minimalj.frontend.impl.lanterna.toolkit.LanternaFrontend;
import org.minimalj.frontend.page.Page;
import org.minimalj.util.resources.Resources;

import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.component.Button;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.component.TextBox;
import com.googlecode.lanterna.gui.dialog.ActionListDialog;
import com.googlecode.lanterna.gui.layout.BorderLayout;
import com.googlecode.lanterna.gui.layout.HorisontalLayout;

public class LanternaMenuPanel extends Panel {
	private final LanternaGUIScreen guiScreen;
	private Panel bar;

	public LanternaMenuPanel(LanternaGUIScreen guiScreen) {
		this.guiScreen = guiScreen;
		
		setBorder(new Border.Invisible());
		setLayoutManager(new BorderLayout());

		Panel panel = new Panel();
		panel.setLayoutManager(new BorderLayout());

		bar = new Panel();
		bar.setLayoutManager(new HorisontalLayout());
		updateMenu(null);

		panel.addComponent(bar, BorderLayout.LEFT);
		panel.addComponent(createSearchField(), BorderLayout.RIGHT);

		addComponent(panel, BorderLayout.TOP);
	}

	public void updateMenu(Page page) {
		bar.removeAllComponents();

		createMenu("application", Application.getApplication().getMenu());

		if (page != null && page.getActions() != null) {
			createMenu("page", page.getActions());
		}
	}

	protected void createMenu(String resourceName, List<Action> actions) {
		String name = Resources.getString("Menu." + resourceName);
		com.googlecode.lanterna.gui.Action newAction = actionGroup(name, actions);
		if (newAction != null) {
			Button button = new Button(newAction.toString(), newAction);
			bar.addComponent(button);
		}
	}

	protected com.googlecode.lanterna.gui.Action actionGroup(String name, List<Action> actions) {
		if (actions.isEmpty())
			return null;
		ActionSelection as = new ActionSelection(name);
		for (Action action : actions) {
			if (action instanceof ActionGroup) {
				ActionGroup subGroup = (ActionGroup) action;
				as.addAction(actionGroup(subGroup.getName(), subGroup.getItems()));
			} else {
				LanternaActionAdapater actionAdapater = new LanternaActionAdapater(guiScreen, action);
				as.addAction(actionAdapater);
			}
		}
		return as;
	}

	private class ActionSelection implements com.googlecode.lanterna.gui.Action {

		private final String item;
		private final List<com.googlecode.lanterna.gui.Action> actions = new ArrayList<>();

		public ActionSelection(String item) {
			this.item = item;
		}

		public void addAction(com.googlecode.lanterna.gui.Action action) {
			actions.add(action);
		}

		@Override
		public void doAction() {
			com.googlecode.lanterna.gui.Action[] actionArray = actions.toArray(new com.googlecode.lanterna.gui.Action[actions.size()]);
			ActionListDialog.showActionListDialog(getGUIScreen(), "Action", "", 0, true, actionArray);
		}

		@Override
		public String toString() {
			return item;
		}
	}

	private TextBox textFieldSearch;

	protected Panel createSearchField() {
		Panel panel = new Panel();
		panel.setLayoutManager(new HorisontalLayout());

		textFieldSearch = new TextBox();
		panel.addComponent(textFieldSearch);

		final Button button = new Button("Search", new LanternaActionAdapater(guiScreen, new SearchAction()));
		panel.addComponent(button);

		return panel;
	}

	protected class SearchAction extends Action {
		@Override
		public void action() {
			LanternaFrontend.setGui(guiScreen);
			String query = textFieldSearch.getText();
			Page searchPage = Application.getApplication().createSearchPage(query);
			Frontend.show(searchPage);
			LanternaFrontend.setGui(null);
		}
	}

}