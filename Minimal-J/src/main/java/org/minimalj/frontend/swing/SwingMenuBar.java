package org.minimalj.frontend.swing;

import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.text.DefaultEditorKit;

import org.minimalj.application.MjApplication;
import org.minimalj.frontend.page.ActionGroup;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.Separator;
import org.minimalj.frontend.swing.lookAndFeel.LookAndFeelAction;
import org.minimalj.frontend.swing.lookAndFeel.PrintLookAndFeel;
import org.minimalj.frontend.swing.lookAndFeel.TerminalLargeFontLookAndFeel;
import org.minimalj.frontend.swing.lookAndFeel.TerminalLookAndFeel;
import org.minimalj.frontend.swing.toolkit.SwingClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.IAction;
import org.minimalj.util.resources.ResourceHelper;
import org.minimalj.util.resources.Resources;

public class SwingMenuBar extends JMenuBar implements IComponent {
	private static final long serialVersionUID = 1L;
	
	private final SwingTab tab;

	public SwingMenuBar(SwingTab tab) {
		super();
		this.tab = tab;
		updateMenu();
	}
	
	protected void updateMenu() {
		removeAll();
		add(createFileMenu());
		add(createEditMenu());
		add(createViewMenu());
		JMenu objectMenu = createObjectMenu();
		if (objectMenu != null) {
			add(objectMenu);
		}
		add(createWindowMenu());
		if (getParent() != null) {
			getParent().revalidate();
			getParent().repaint();
		}
	}
	
	private JMenu createFileMenu() {
		JMenu menu = menu("file");
		
		List<IAction> actionsNew = MjApplication.getApplication().getActionsNew();
		if (!actionsNew.isEmpty()) {
			addActions(menu, "new", actionsNew);
			menu.addSeparator();
		}
		menu.add(new JMenuItem(tab.frame.closeWindowAction));
		menu.add(new JMenuItem(tab.closeTabAction));
		menu.addSeparator();
		List<IAction> actionsImport = MjApplication.getApplication().getActionsImport();
		if (!actionsImport.isEmpty()) addActions(menu, "import", actionsImport);
		List<IAction> actionsExport = MjApplication.getApplication().getActionsExport();
		if (!actionsExport.isEmpty()) addActions(menu, "export", actionsExport);
		if (!actionsImport.isEmpty() || !actionsExport.isEmpty()) menu.addSeparator();
		menu.add(new JMenuItem(tab.frame.exitAction));
		return menu;
	}
	
	private JMenu createEditMenu() {
		JMenu menu = menu("edit");
		menu.add(new JMenuItem(ResourceHelper.initProperties(new DefaultEditorKit.CutAction(), Resources.getResourceBundle(), "cut")));
		menu.add(new JMenuItem(ResourceHelper.initProperties(new DefaultEditorKit.CopyAction(), Resources.getResourceBundle(), "copy")));
		menu.add(new JMenuItem(ResourceHelper.initProperties(new DefaultEditorKit.PasteAction(), Resources.getResourceBundle(), "paste")));
		return menu;
	}
	
	private JMenu createViewMenu() {
		JMenu menu = menu("view");
		List<IAction> actionsView = MjApplication.getApplication().getActionsView();
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
		menu.add(new JMenuItem(new LookAndFeelAction("Normal")));
		menu.add(new JMenuItem(new LookAndFeelAction("Hoher Kontrast", TerminalLookAndFeel.class.getName())));
		menu.add(new JMenuItem(new LookAndFeelAction("Hoher Kontrast (Gross)", TerminalLargeFontLookAndFeel.class.getName())));
		menu.add(new JMenuItem(new LookAndFeelAction("Druckbar", PrintLookAndFeel.class.getName())));
		return menu;
	}

	private JMenu createObjectMenu() {
		Page visiblePage = tab.getVisiblePage();
		if (visiblePage != null) {
			ActionGroup actionGroup = visiblePage.getMenu();
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
	
	private void addActions(JMenu menu, String type, List<IAction> actions) {
		JMenu subMenu = menu(type);
		addActions(subMenu, actions);
		menu.add(subMenu);
	}
	
	private void addActions(JMenu menu, List<IAction> actions) {
		for (IAction action : actions) {
			if (action instanceof org.minimalj.frontend.page.ActionGroup) {
				org.minimalj.frontend.page.ActionGroup actionGroup = (org.minimalj.frontend.page.ActionGroup) action;
				JMenu subMenu = new JMenu(SwingClientToolkit.adaptAction(action, tab));
				addActions(subMenu, actionGroup.getItems());
				menu.add(subMenu);
			} else if (action instanceof Separator) {
				menu.addSeparator();
			} else {
				menu.add(new JMenuItem(SwingClientToolkit.adaptAction(action, tab)));
			}
		}
	}
	
	
	void onHistoryChanged() {
		updateMenu();
	}

}	
