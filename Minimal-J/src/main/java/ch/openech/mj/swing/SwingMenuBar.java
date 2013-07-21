package ch.openech.mj.swing;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.text.DefaultEditorKit;

import ch.openech.mj.application.MjApplication;
import ch.openech.mj.page.ActionGroup;
import ch.openech.mj.page.Page;
import ch.openech.mj.resources.ResourceHelper;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.swing.lookAndFeel.LookAndFeelAction;
import ch.openech.mj.swing.lookAndFeel.PrintLookAndFeel;
import ch.openech.mj.swing.lookAndFeel.TerminalLargeFontLookAndFeel;
import ch.openech.mj.swing.lookAndFeel.TerminalLookAndFeel;
import ch.openech.mj.toolkit.IComponent;

public class SwingMenuBar extends JMenuBar implements IComponent {
	private final SwingTab tab;

	public SwingMenuBar(SwingTab tab) {
		super();
		this.tab = tab;
		updateMenu();
	}
	
	protected void updateMenu() {
		ActionGroup actionGroup = new ActionGroup();
		fillMenu(actionGroup);
		
		MjApplication.getApplication().fillActionGroup(tab, actionGroup);
		
		Page visiblePage = tab.getVisiblePage();
		if (visiblePage != null) {
			visiblePage.fillActionGroup(actionGroup.getOrCreateActionGroup(ActionGroup.OBJECT));
		}

		updateMenu(actionGroup);
	}
	
	private void fillMenu(ActionGroup actionGroup) {
		ActionGroup file = actionGroup.getOrCreateActionGroup(ActionGroup.FILE);
		fillFileMenu(file);
	
		ActionGroup edit = actionGroup.getOrCreateActionGroup(ActionGroup.EDIT);
		fillEditMenu(edit);
		
		ActionGroup view = actionGroup.getOrCreateActionGroup(ActionGroup.VIEW);
		fillViewMenu(view);
		
		actionGroup.getOrCreateActionGroup(ActionGroup.OBJECT);

		ActionGroup window = actionGroup.getOrCreateActionGroup(ActionGroup.WINDOW);
		fillWindowMenu(window);
		
		ActionGroup help = actionGroup.getOrCreateActionGroup(ActionGroup.HELP);
		fillHelpMenu(help);
	}

	private void updateMenu(ActionGroup actions) {
		removeAll();
		for (Action action : actions.getActions()) {
			if (action instanceof ActionGroup) {
				ActionGroup actionGroup = (ActionGroup) action;
				if (!actionGroup.getActions().isEmpty()) {
					JMenu menu = new JMenu((String) actionGroup.getValue(Action.NAME));
					// menu.setMnemonic((String) actionGroup.getValue(Action.MNEMONIC_KEY));
					add(menu);
					fillMenu(menu, actionGroup);
				}
			}
		}
		validate();
	}
	
	private void fillMenu(JMenu menu, ActionGroup actionGroup) {
		for (Action action : actionGroup.getActions()) {
			if (action instanceof ActionGroup) {
				ActionGroup subGroup = (ActionGroup) action;
				if (!actionGroup.getActions().isEmpty()) {
					JMenu subMenu = new JMenu((String) subGroup.getValue(Action.NAME));
					fillMenu(subMenu, subGroup);
					menu.add(subMenu);
				}
			} else if (action.getValue(Action.NAME) != null) {
				JMenuItem menuItem = new JMenuItem(action);
				menu.add(menuItem);
			} else {
				menu.addSeparator();
			}
		}
	}
	
	void onHistoryChanged() {
		updateMenu();
	}

	//
	
	protected void fillFileMenu(ActionGroup actionGroup) {
		actionGroup.getOrCreateActionGroup(ActionGroup.NEW);
		actionGroup.addSeparator();
		actionGroup.add(tab.frame.closeWindowAction);
		actionGroup.add(tab.closeTabAction);
		actionGroup.addSeparator();
		actionGroup.getOrCreateActionGroup(ActionGroup.IMPORT);
		actionGroup.getOrCreateActionGroup(ActionGroup.EXPORT);
		actionGroup.addSeparator();
		actionGroup.add(tab.frame.exitAction);
	}
	
	private void fillEditMenu(ActionGroup actionGroup) {
		actionGroup.add(ResourceHelper.initProperties(new DefaultEditorKit.CutAction(), Resources.getResourceBundle(), "cut"));
		actionGroup.add(ResourceHelper.initProperties(new DefaultEditorKit.CopyAction(), Resources.getResourceBundle(), "copy"));
		actionGroup.add(ResourceHelper.initProperties(new DefaultEditorKit.PasteAction(), Resources.getResourceBundle(), "paste"));
	}

	protected void fillViewMenu(ActionGroup actionGroup) {
		actionGroup.add(tab.previousAction);
		actionGroup.add(tab.nextAction);
		actionGroup.add(tab.refreshAction);
//		actionGroup.addSeparator();
//		actionGroup.add(tab.menuItemToolBarVisible);
		actionGroup.addSeparator();
		ActionGroup lookAndFeel = actionGroup.getOrCreateActionGroup("lookAndFeel");
		fillLookAndFeelMenu(lookAndFeel);
	}

	private void fillLookAndFeelMenu(ActionGroup actionGroup) {
		actionGroup.add(new LookAndFeelAction("Normal"));
		actionGroup.add(new LookAndFeelAction("Hoher Kontrast", TerminalLookAndFeel.class.getName()));
		actionGroup.add(new LookAndFeelAction("Hoher Kontrast (Gross)", TerminalLargeFontLookAndFeel.class.getName()));
		actionGroup.add(new LookAndFeelAction("Druckbar", PrintLookAndFeel.class.getName()));
	}
	
	protected void fillWindowMenu(ActionGroup actionGroup) {
		actionGroup.add(tab.frame.newWindowAction);
		actionGroup.add(tab.frame.newTabAction);
	}
	
	protected void fillHelpMenu(ActionGroup actionGroup) {
		// 
	}
}	
