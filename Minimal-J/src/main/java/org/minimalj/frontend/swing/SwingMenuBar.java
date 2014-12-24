package org.minimalj.frontend.swing;

import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.text.DefaultEditorKit;

import org.minimalj.application.Application;
import org.minimalj.frontend.page.ActionGroup;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.Separator;
import org.minimalj.frontend.swing.lookAndFeel.LookAndFeelAction;
import org.minimalj.frontend.swing.lookAndFeel.PrintLookAndFeel;
import org.minimalj.frontend.swing.lookAndFeel.TerminalLargeFontLookAndFeel;
import org.minimalj.frontend.swing.lookAndFeel.TerminalLookAndFeel;
import org.minimalj.frontend.swing.toolkit.SwingClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.IAction;
import org.minimalj.util.resources.Resources;

public class SwingMenuBar extends JMenuBar implements IComponent {
	private static final long serialVersionUID = 1L;
	
	private final SwingTab tab;

	private JMenu fileMenu;
	private JMenu newSubMenu;
	private JMenu objectMenu;
	
	public SwingMenuBar(SwingTab tab) {
		super();
		this.tab = tab;

		fileMenu = createFileMenu();
		add(fileMenu);
		add(createEditMenu());
		add(createViewMenu());
		add(createWindowMenu());
	}
	
	protected void updateMenu() {
		updateFileMenu();
		if (objectMenu != null) {
			remove(objectMenu);
		}
		objectMenu = createObjectMenu();
		if (objectMenu != null) {
			add(objectMenu, 3); // position 3 : after file, edit and display
		}
		if (getParent() != null) {
			getParent().revalidate();
			getParent().repaint();
		}
	}
	
	private void updateFileMenu() {
		if (newSubMenu != null) {
			fileMenu.remove(newSubMenu);
		}
		List<IAction> actionsNew = Application.getApplication().getActionsNew();
		newSubMenu = menu("new", actionsNew);
		if (!actionsNew.isEmpty()) {
			fileMenu.add(newSubMenu, 0);
		}
	}

	private JMenu createFileMenu() {
		JMenu menu = menu("file");
		
		menu.add(new JMenuItem(tab.frame.closeWindowAction));
		menu.add(new JMenuItem(tab.closeTabAction));
		menu.addSeparator();
		List<IAction> actionsImport = Application.getApplication().getActionsImport();
		if (!actionsImport.isEmpty()) menu.add(menu("import", actionsImport));
		List<IAction> actionsExport = Application.getApplication().getActionsExport();
		if (!actionsExport.isEmpty()) menu.add(menu("export", actionsExport));
		if (!actionsImport.isEmpty() || !actionsExport.isEmpty()) menu.addSeparator();
		menu.add(new JMenuItem(tab.frame.exitAction));
		return menu;
	}
	
	private JMenu createEditMenu() {
		JMenu menu = menu("edit");
		menu.add(new JMenuItem(SwingResourceAction.initProperties(new DefaultEditorKit.CutAction(), "cut")));
		menu.add(new JMenuItem(SwingResourceAction.initProperties(new DefaultEditorKit.CopyAction(), "copy")));
		menu.add(new JMenuItem(SwingResourceAction.initProperties(new DefaultEditorKit.PasteAction(), "paste")));
		return menu;
	}
	
	private JMenu createViewMenu() {
		JMenu menu = menu("view");
		List<IAction> actionsView = Application.getApplication().getActionsView();
		if (!actionsView.isEmpty()) {
			addActions(menu, actionsView);
			menu.addSeparator();
		}
		menu.add(new JMenuItem(tab.previousAction));
		menu.add(new JMenuItem(tab.nextAction));
		menu.add(new JMenuItem(tab.refreshAction));
//		menu.addSeparator();
//		menu.add(new JMenuItem(tab.menuItemToolBarVisible));
		menu.addSeparator();
		menu.add(createLookAndFeeldMenu());
		return menu;
	}

	private JMenu createLookAndFeeldMenu() {
		JMenu menu = menu("lookAndFeel");
		menu.add(new JMenuItem(new LookAndFeelAction("system")));
		menu.add(new JMenuItem(new LookAndFeelAction("highContrast", TerminalLookAndFeel.class.getName())));
		menu.add(new JMenuItem(new LookAndFeelAction("highContrastLarge", TerminalLargeFontLookAndFeel.class.getName())));
		menu.add(new JMenuItem(new LookAndFeelAction("print", PrintLookAndFeel.class.getName())));
		return menu;
	}

	private JMenu createObjectMenu() {
		Page visiblePage = tab.getVisiblePage();
		if (visiblePage instanceof ObjectPage) {
			ActionGroup actionGroup = ((ObjectPage<?>) visiblePage).getMenu();
			if (actionGroup != null && actionGroup.getItems() != null) {
				JMenu menu = new JMenu(actionGroup.getName());
				addActions(menu, actionGroup.getItems());
				return menu;
			}
		}
		return null;
	}
	
	private JMenu createWindowMenu() {
		JMenu menu = menu("window");
		menu.add(new JMenuItem(tab.frame.newWindowAction));
		menu.add(new JMenuItem(tab.frame.newTabAction));
		return menu;
	}

	//
	
	private JMenu menu(String resourceName) {
		JMenu menu = new JMenu();
		menu.setText(Resources.getString("Menu." + resourceName));
		return menu;
	}
	
	private JMenu menu(String type, List<IAction> actions) {
		JMenu subMenu = menu(type);
		addActions(subMenu, actions);
		return subMenu;
	}
	
	private void addActions(JMenu menu, List<IAction> actions) {
		for (IAction action : actions) {
			if (action instanceof org.minimalj.frontend.page.ActionGroup) {
				org.minimalj.frontend.page.ActionGroup actionGroup = (org.minimalj.frontend.page.ActionGroup) action;
				JMenu subMenu = new JMenu(SwingClientToolkit.adaptAction(action));
				addActions(subMenu, actionGroup.getItems());
				menu.add(subMenu);
			} else if (action instanceof Separator) {
				menu.addSeparator();
			} else {
				menu.add(new JMenuItem(SwingClientToolkit.adaptAction(action)));
			}
		}
	}
	
	void onHistoryChanged() {
		updateMenu();
	}

}	
