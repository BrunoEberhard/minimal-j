package ch.openech.mj.swing;

import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.text.DefaultEditorKit;

import ch.openech.mj.application.MjApplication;
import ch.openech.mj.page.ActionGroup;
import ch.openech.mj.page.Page;
import ch.openech.mj.page.Separator;
import ch.openech.mj.resources.ResourceHelper;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.swing.lookAndFeel.LookAndFeelAction;
import ch.openech.mj.swing.lookAndFeel.PrintLookAndFeel;
import ch.openech.mj.swing.lookAndFeel.TerminalLargeFontLookAndFeel;
import ch.openech.mj.swing.lookAndFeel.TerminalLookAndFeel;
import ch.openech.mj.swing.toolkit.SwingClientToolkit;
import ch.openech.mj.toolkit.IAction;
import ch.openech.mj.toolkit.IComponent;

public class SwingMenuBar extends JMenuBar implements IComponent {
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
	}
	
	private JMenu createFileMenu() {
		JMenu menu = menu("file");
		
		addActions(menu, "new", MjApplication.getApplication().getActionsNew(tab));
		menu.addSeparator();
		menu.add(new JMenuItem(tab.frame.closeWindowAction));
		menu.add(new JMenuItem(tab.closeTabAction));
		menu.addSeparator();
		addActions(menu, "import", MjApplication.getApplication().getActionsImport(tab));
		addActions(menu, "export", MjApplication.getApplication().getActionsExport(tab));
		menu.addSeparator();
		menu.add(new JMenuItem(tab.frame.exitAction));
		return menu;
	}
	
	private JMenu createEditMenu() {
		JMenu menu = menu("edit");
		menu.add(new JMenuItem(ResourceHelper.initProperties(new DefaultEditorKit.CutAction(), Resources.getResourceBundle(), "cut")));
		menu.add(new JMenuItem(ResourceHelper.initProperties(new DefaultEditorKit.CutAction(), Resources.getResourceBundle(), "copy")));
		menu.add(new JMenuItem(ResourceHelper.initProperties(new DefaultEditorKit.CutAction(), Resources.getResourceBundle(), "paste")));
		return menu;
	}
	
	private JMenu createViewMenu() {
		JMenu menu = menu("view");
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
		menu.setText(Resources.getString("Menu." + resourceName + ".text"));
		return menu;
	}
	
	private void addActions(JMenu menu, String type, List<IAction> actions) {
		JMenu subMenu = menu(type);
		addActions(subMenu, actions);
		menu.add(subMenu);
	}
	
	private void addActions(JMenu menu, List<IAction> actions) {
		for (IAction action : actions) {
			if (action instanceof ch.openech.mj.page.ActionGroup) {
				ch.openech.mj.page.ActionGroup actionGroup = (ch.openech.mj.page.ActionGroup) action;
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
