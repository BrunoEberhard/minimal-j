package org.minimalj.frontend.lanterna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.minimalj.application.Application;
import org.minimalj.frontend.lanterna.component.Select;
import org.minimalj.frontend.lanterna.toolkit.LanternaActionAdapater;
import org.minimalj.frontend.lanterna.toolkit.LanternaClientToolkit;
import org.minimalj.frontend.page.ActionGroup;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.PageLink;
import org.minimalj.frontend.page.type.SearchOf;
import org.minimalj.frontend.toolkit.IAction;
import org.minimalj.frontend.toolkit.ResourceAction;
import org.minimalj.util.GenericUtils;
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

		createMenu("new", Application.getApplication().getActionsNew());

		if (page instanceof ObjectPage) {
			createMenu(((ObjectPage<?>) page).getMenu());
		}

		createMenu("import", Application.getApplication().getActionsImport());
		createMenu("export", Application.getApplication().getActionsExport());
	}

	protected void createMenu(ActionGroup menu) {
		if (menu != null) {
			createMenu(menu.getName(), menu.getItems());
		}
	}

	protected void createMenu(String resourceName, List<IAction> actions) {
		String name = Resources.getString("Menu." + resourceName);
		com.googlecode.lanterna.gui.Action newAction = actionGroup(name, actions);
		if (newAction != null) {
			Button button = new Button(newAction.toString(), newAction);
			bar.addComponent(button);
		}
	}

	protected com.googlecode.lanterna.gui.Action actionGroup(String name, List<IAction> actions) {
		if (actions.isEmpty())
			return null;
		ActionSelection as = new ActionSelection(name);
		for (IAction action : actions) {
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

	private Select<String> comboBoxSearchObject;
	private TextBox textFieldSearch;
	private Map<String, Class<?>> searchClassesByObjectName = new HashMap<String, Class<?>>();

	protected Panel createSearchField() {
		Panel panel = new Panel();
		panel.setLayoutManager(new HorisontalLayout());

		Class<?>[] searchClasses = Application.getApplication().getSearchClasses();
		List<String> objectNameList = new ArrayList<>();
		for (Class<?> searchClass : searchClasses) {
			Class<?> searchedClass = GenericUtils.getTypeArgument(searchClass, SearchOf.class);
			String objectName = Resources.getString(searchedClass);
			objectNameList.add(objectName);
			searchClassesByObjectName.put(objectName, searchClass);
		}

		comboBoxSearchObject = new Select<>();
		comboBoxSearchObject.setObjects(objectNameList);
		panel.addComponent(comboBoxSearchObject);

		textFieldSearch = new TextBox();
		panel.addComponent(textFieldSearch);

		final Button button = new Button("Search", new LanternaActionAdapater(guiScreen, new SearchAction()));
		panel.addComponent(button);

		return panel;
	}

	// private static class SearchCellRenderer extends DefaultListCellRenderer {
	// @Override
	// public Component getListCellRendererComponent(JList list, Object value,
	// int index, boolean isSelected, boolean cellHasFocus) {
	// Class<?> searchClass = (Class<?>) value;
	// value = Resources.getString("Search." + searchClass.getSimpleName());
	// return super.getListCellRendererComponent(list, value, index, isSelected,
	// cellHasFocus);
	// }
	// }

	protected class SearchAction extends ResourceAction {
		@Override
		public void action() {
			LanternaClientToolkit.setGui(guiScreen);
			Class<?> searchObject = searchClassesByObjectName.get(comboBoxSearchObject.getSelectedObject());
			String text = textFieldSearch.getText();
			search((Class<? extends Page>) searchObject, text);
			LanternaClientToolkit.setGui(null);
		}
	}

	public void search(Class<? extends Page> searchClass, String text) {
		LanternaClientToolkit.getGui().show(PageLink.link(searchClass, text));
	}

}